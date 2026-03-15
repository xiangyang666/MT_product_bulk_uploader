package com.meituan.product.service;

import com.meituan.product.dto.FieldMappingRule;
import com.meituan.product.enums.MatchType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 置信度计算器
 * 根据匹配类型和其他因素计算映射的置信度
 */
@Slf4j
@Component
public class ConfidenceCalculator {
    
    // 置信度阈值
    private static final double MIN_CONFIDENCE = 0.0;
    private static final double MAX_CONFIDENCE = 1.0;
    
    // 历史加成
    private static final double HISTORY_BONUS = 0.05;
    
    /**
     * 计算映射置信度
     * 
     * @param columnName 列名
     * @param rule 映射规则
     * @param matchType 匹配类型
     * @return 置信度（0-1）
     */
    public double calculateConfidence(String columnName, FieldMappingRule rule, MatchType matchType) {
        if (matchType == null) {
            log.warn("匹配类型为空，返回最低置信度");
            return MIN_CONFIDENCE;
        }
        
        // 获取基础置信度
        double baseConfidence = matchType.getBaseConfidence();
        
        log.debug("计算置信度: 列名='{}', 字段='{}', 匹配类型={}, 基础置信度={}", 
            columnName, rule != null ? rule.getFieldName() : "null", matchType.getDisplayName(), baseConfidence);
        
        // 确保置信度在有效范围内
        double confidence = ensureValidRange(baseConfidence);
        
        return confidence;
    }
    
    /**
     * 计算关键词匹配的置信度
     * 根据匹配的关键词数量调整置信度
     * 
     * @param keywordScore 关键词匹配得分（来自SemanticMatcher）
     * @return 置信度（0-1）
     */
    public double calculateKeywordConfidence(double keywordScore) {
        // keywordScore已经在0.7-0.95范围内
        return ensureValidRange(keywordScore);
    }
    
    /**
     * 计算模糊匹配的置信度
     * 根据字符串相似度调整置信度
     * 
     * @param fuzzyScore 模糊匹配得分（来自SemanticMatcher）
     * @return 置信度（0-1）
     */
    public double calculateFuzzyConfidence(double fuzzyScore) {
        // fuzzyScore已经在0.5-0.8范围内
        return ensureValidRange(fuzzyScore);
    }
    
    /**
     * 根据历史数据调整置信度
     * 如果该列名历史上成功映射过，增加置信度
     * 
     * @param baseConfidence 基础置信度
     * @param columnName 列名
     * @param fieldName 字段名
     * @return 调整后的置信度
     */
    public double adjustByHistory(double baseConfidence, String columnName, String fieldName) {
        // TODO: 实现历史数据查询（任务14）
        // 目前暂不调整，直接返回基础置信度
        
        // 未来实现：
        // 1. 查询mapping_history表
        // 2. 检查该列名是否成功映射过该字段
        // 3. 如果成功次数 > 0，增加HISTORY_BONUS
        
        log.debug("历史调整（未实现）: 列名='{}', 字段='{}', 基础置信度={}", 
            columnName, fieldName, baseConfidence);
        
        return baseConfidence;
    }
    
    /**
     * 根据字段优先级调整置信度
     * 优先级更高的字段在冲突时会被优先选择
     * 
     * @param baseConfidence 基础置信度
     * @param rule 映射规则
     * @return 调整后的置信度
     */
    public double adjustByPriority(double baseConfidence, FieldMappingRule rule) {
        if (rule == null) {
            return baseConfidence;
        }
        
        // 优先级调整很小，避免影响主要的匹配逻辑
        // 优先级范围通常是0-100，我们将其映射到0-0.02的调整范围
        double priorityAdjustment = (rule.getPriority() / 100.0) * 0.02;
        
        double adjusted = baseConfidence + priorityAdjustment;
        
        log.debug("优先级调整: 字段='{}', 优先级={}, 调整前={}, 调整后={}", 
            rule.getFieldName(), rule.getPriority(), baseConfidence, adjusted);
        
        return ensureValidRange(adjusted);
    }
    
    /**
     * 确保置信度在有效范围内[0, 1]
     * 
     * @param confidence 原始置信度
     * @return 范围内的置信度
     */
    private double ensureValidRange(double confidence) {
        if (confidence < MIN_CONFIDENCE) {
            log.warn("置信度 {} 小于最小值 {}，调整为最小值", confidence, MIN_CONFIDENCE);
            return MIN_CONFIDENCE;
        }
        if (confidence > MAX_CONFIDENCE) {
            log.warn("置信度 {} 大于最大值 {}，调整为最大值", confidence, MAX_CONFIDENCE);
            return MAX_CONFIDENCE;
        }
        return confidence;
    }
    
    /**
     * 判断置信度是否需要警告
     * 置信度低于0.8时应该生成警告
     * 
     * @param confidence 置信度
     * @return 是否需要警告
     */
    public boolean needsWarning(double confidence) {
        return confidence < 0.8;
    }
    
    /**
     * 判断置信度是否可接受
     * 置信度低于0.5时认为不可接受
     * 
     * @param confidence 置信度
     * @return 是否可接受
     */
    public boolean isAcceptable(double confidence) {
        return confidence >= 0.5;
    }
    
    /**
     * 获取置信度的描述
     * 
     * @param confidence 置信度
     * @return 描述文本
     */
    public String getConfidenceDescription(double confidence) {
        if (confidence >= 0.95) {
            return "非常高";
        } else if (confidence >= 0.8) {
            return "高";
        } else if (confidence >= 0.6) {
            return "中等";
        } else if (confidence >= 0.5) {
            return "较低";
        } else {
            return "很低";
        }
    }
}
