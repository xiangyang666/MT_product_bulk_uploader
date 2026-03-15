package com.meituan.product.service;

import com.meituan.product.dto.*;
import com.meituan.product.enums.FormatType;
import com.meituan.product.enums.MatchType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 自适应映射引擎
 * 协调整个自适应映射过程，是核心协调器
 */
@Slf4j
@Component
public class AdaptiveMappingEngine {
    
    private final MappingRuleRepository ruleRepository;
    private final SemanticMatcher semanticMatcher;
    private final ConfidenceCalculator confidenceCalculator;
    
    public AdaptiveMappingEngine(
            MappingRuleRepository ruleRepository,
            SemanticMatcher semanticMatcher,
            ConfidenceCalculator confidenceCalculator) {
        this.ruleRepository = ruleRepository;
        this.semanticMatcher = semanticMatcher;
        this.confidenceCalculator = confidenceCalculator;
    }
    
    /**
     * 构建字段映射
     * 
     * @param headers Excel表头列表
     * @param formatType 格式类型（MEITUAN / STANDARD）
     * @return 映射结果
     */
    public MappingResult buildMapping(List<String> headers, FormatType formatType) {
        long startTime = System.currentTimeMillis();
        
        log.info("开始构建字段映射: 格式={}, 列数={}", formatType, headers.size());
        
        MappingResult result = new MappingResult(formatType);
        
        try {
            // 1. 加载映射规则
            Map<String, FieldMappingRule> rules = ruleRepository.getAllRules();
            log.info("加载了 {} 个映射规则", rules.size());
            
            // 2. 为每个表头列寻找最佳匹配
            Map<String, List<ColumnMapping>> candidateMappings = new HashMap<>();
            
            for (int i = 0; i < headers.size(); i++) {
                String columnName = headers.get(i);
                if (columnName == null || columnName.trim().isEmpty()) {
                    continue;
                }
                
                log.debug("处理列[{}]: '{}'", i, columnName);
                
                // 尝试匹配每个规则
                for (FieldMappingRule rule : rules.values()) {
                    ColumnMapping mapping = findBestMatch(columnName, i, rule);
                    if (mapping != null) {
                        candidateMappings
                            .computeIfAbsent(rule.getFieldName(), k -> new ArrayList<>())
                            .add(mapping);
                    }
                }
            }
            
            // 3. 选择最佳映射（处理冲突）
            Map<String, ColumnMapping> finalMappings = resolveMappingConflicts(candidateMappings, rules);
            
            // 4. 添加映射到结果
            for (ColumnMapping mapping : finalMappings.values()) {
                result.addMapping(mapping);
                log.info("映射成功: {} -> '{}' (索引:{}, 置信度:{}, 类型:{})",
                    mapping.getFieldName(), mapping.getColumnName(), 
                    mapping.getColumnIndex(), mapping.getConfidence(), 
                    mapping.getMatchType().getDisplayName());
            }
            
            // 5. 验证映射结果
            validateMapping(result, rules);
            
            // 6. 记录耗时
            long duration = System.currentTimeMillis() - startTime;
            result.setBuildTime(duration);
            
            log.info("映射构建完成: 成功={}, 警告={}, 错误={}, 耗时={}ms",
                result.getMappings().size(), result.getWarnings().size(), 
                result.getErrors().size(), duration);
            
        } catch (Exception e) {
            log.error("构建映射失败: {}", e.getMessage(), e);
            result.setValid(false);
            result.addError(new MappingError("系统错误", "映射构建过程中发生异常: " + e.getMessage()));
        }
        
        return result;
    }
    
    /**
     * 为单个列名寻找最佳匹配
     * 
     * @param columnName 列名
     * @param columnIndex 列索引
     * @param rule 映射规则
     * @return 列映射，如果不匹配返回null
     */
    private ColumnMapping findBestMatch(String columnName, int columnIndex, FieldMappingRule rule) {
        ColumnMapping bestMapping = null;
        double bestConfidence = 0.0;
        MatchType bestMatchType = null;
        
        // 1. 尝试精确匹配
        if (semanticMatcher.exactMatch(columnName, rule)) {
            bestConfidence = MatchType.EXACT.getBaseConfidence();
            bestMatchType = MatchType.EXACT;
            log.debug("  -> 精确匹配: {} (置信度: {})", rule.getFieldName(), bestConfidence);
        }
        
        // 2. 尝试标准化匹配
        if (bestMatchType == null && semanticMatcher.normalizedMatch(columnName, rule)) {
            bestConfidence = MatchType.NORMALIZED.getBaseConfidence();
            bestMatchType = MatchType.NORMALIZED;
            log.debug("  -> 标准化匹配: {} (置信度: {})", rule.getFieldName(), bestConfidence);
        }
        
        // 3. 尝试关键词匹配
        if (bestMatchType == null) {
            double keywordScore = semanticMatcher.keywordMatch(columnName, rule);
            if (keywordScore > 0) {
                bestConfidence = confidenceCalculator.calculateKeywordConfidence(keywordScore);
                bestMatchType = MatchType.KEYWORD;
                log.debug("  -> 关键词匹配: {} (置信度: {})", rule.getFieldName(), bestConfidence);
            }
        }
        
        // 4. 尝试模糊匹配
        if (bestMatchType == null) {
            double fuzzyScore = semanticMatcher.fuzzyMatch(columnName, rule);
            if (fuzzyScore > 0) {
                bestConfidence = confidenceCalculator.calculateFuzzyConfidence(fuzzyScore);
                bestMatchType = MatchType.FUZZY;
                log.debug("  -> 模糊匹配: {} (置信度: {})", rule.getFieldName(), bestConfidence);
            }
        }
        
        // 如果找到匹配，创建映射对象
        if (bestMatchType != null && confidenceCalculator.isAcceptable(bestConfidence)) {
            // 根据优先级微调置信度
            bestConfidence = confidenceCalculator.adjustByPriority(bestConfidence, rule);
            
            bestMapping = new ColumnMapping(
                rule.getFieldName(),
                columnName,
                columnIndex,
                bestConfidence,
                bestMatchType,
                rule.isRequired()
            );
        }
        
        return bestMapping;
    }
    
    /**
     * 解决映射冲突
     * 当多个列都可能映射到同一字段时，选择置信度最高的
     * 
     * @param candidateMappings 候选映射（字段名 -> 候选列映射列表）
     * @param rules 映射规则
     * @return 最终映射（字段名 -> 列映射）
     */
    private Map<String, ColumnMapping> resolveMappingConflicts(
            Map<String, List<ColumnMapping>> candidateMappings,
            Map<String, FieldMappingRule> rules) {
        
        Map<String, ColumnMapping> finalMappings = new HashMap<>();
        
        for (Map.Entry<String, List<ColumnMapping>> entry : candidateMappings.entrySet()) {
            String fieldName = entry.getKey();
            List<ColumnMapping> candidates = entry.getValue();
            
            if (candidates.isEmpty()) {
                continue;
            }
            
            // 按置信度排序，选择最高的
            candidates.sort((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()));
            
            ColumnMapping bestMapping = candidates.get(0);
            
            // 如果有多个候选且置信度接近，记录警告
            if (candidates.size() > 1) {
                ColumnMapping secondBest = candidates.get(1);
                double confidenceDiff = bestMapping.getConfidence() - secondBest.getConfidence();
                
                if (confidenceDiff < 0.1) {
                    log.warn("字段 {} 有多个接近的候选: '{}' ({}) vs '{}' ({})",
                        fieldName, 
                        bestMapping.getColumnName(), bestMapping.getConfidence(),
                        secondBest.getColumnName(), secondBest.getConfidence());
                }
            }
            
            finalMappings.put(fieldName, bestMapping);
        }
        
        return finalMappings;
    }
    
    /**
     * 验证映射结果
     * 检查必需字段是否都已映射，生成警告和错误
     * 
     * @param result 映射结果
     * @param rules 映射规则
     */
    private void validateMapping(MappingResult result, Map<String, FieldMappingRule> rules) {
        // 检查必需字段
        List<FieldMappingRule> requiredRules = ruleRepository.getRequiredRules();
        boolean allRequiredMapped = true;
        
        for (FieldMappingRule rule : requiredRules) {
            if (!result.getMappings().containsKey(rule.getFieldName())) {
                allRequiredMapped = false;
                
                MappingError error = new MappingError(
                    rule.getFieldName(),
                    String.format("必需字段 '%s' 未能映射", rule.getDisplayName())
                );
                result.addError(error);
                
                log.error("必需字段未映射: {} ({})", rule.getFieldName(), rule.getDisplayName());
            }
        }
        
        // 检查低置信度映射（生成警告）
        for (ColumnMapping mapping : result.getMappings().values()) {
            if (confidenceCalculator.needsWarning(mapping.getConfidence())) {
                MappingWarning warning = new MappingWarning(
                    mapping.getFieldName(),
                    mapping.getColumnName(),
                    mapping.getConfidence(),
                    String.format("列名 '%s' 与字段 '%s' 的匹配置信度较低 (%.2f)，请确认是否正确",
                        mapping.getColumnName(),
                        rules.get(mapping.getFieldName()).getDisplayName(),
                        mapping.getConfidence())
                );
                result.addWarning(warning);
                
                log.warn("低置信度映射: {} -> '{}' ({})",
                    mapping.getFieldName(), mapping.getColumnName(), mapping.getConfidence());
            }
        }
        
        // 设置映射是否有效
        result.setValid(allRequiredMapped && !result.hasErrors());
    }
    
    /**
     * 验证映射结果（公共接口）
     * 
     * @param mappingResult 映射结果
     * @return 验证结果（是否包含所有必需字段）
     */
    public boolean validateMapping(MappingResult mappingResult) {
        if (mappingResult == null) {
            return false;
        }
        
        List<FieldMappingRule> requiredRules = ruleRepository.getRequiredRules();
        
        for (FieldMappingRule rule : requiredRules) {
            if (!mappingResult.getMappings().containsKey(rule.getFieldName())) {
                log.error("验证失败: 缺少必需字段 {}", rule.getFieldName());
                return false;
            }
        }
        
        return true;
    }
}
