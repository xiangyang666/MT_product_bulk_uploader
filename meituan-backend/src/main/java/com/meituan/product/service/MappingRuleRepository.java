package com.meituan.product.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meituan.product.dto.FieldMappingRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 映射规则仓库
 * 负责加载和管理字段映射规则配置
 */
@Slf4j
@Component
public class MappingRuleRepository {
    
    private static final String DEFAULT_CONFIG_PATH = "mapping-rules.json";
    
    private final ObjectMapper objectMapper;
    
    // 缓存的映射规则（字段名 -> 规则）
    private Map<String, FieldMappingRule> rulesCache;
    
    // 配置文件路径
    private String configPath = DEFAULT_CONFIG_PATH;
    
    public MappingRuleRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * 初始化时加载规则
     */
    @PostConstruct
    public void init() {
        loadRules();
    }
    
    /**
     * 加载映射规则
     * 
     * @return 映射规则集合（字段名 -> 规则）
     */
    public Map<String, FieldMappingRule> loadRules() {
        log.info("开始加载映射规则配置: {}", configPath);
        
        try {
            // 尝试从classpath加载配置文件
            ClassPathResource resource = new ClassPathResource(configPath);
            
            if (!resource.exists()) {
                log.warn("配置文件不存在: {}，使用默认规则", configPath);
                rulesCache = createDefaultRules();
                return rulesCache;
            }
            
            try (InputStream inputStream = resource.getInputStream()) {
                JsonNode rootNode = objectMapper.readTree(inputStream);
                
                // 验证配置文件格式
                if (!rootNode.has("rules")) {
                    log.error("配置文件格式错误：缺少'rules'节点，使用默认规则");
                    rulesCache = createDefaultRules();
                    return rulesCache;
                }
                
                // 解析规则
                Map<String, FieldMappingRule> rules = new HashMap<>();
                JsonNode rulesNode = rootNode.get("rules");
                
                Iterator<Map.Entry<String, JsonNode>> fields = rulesNode.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    String fieldName = entry.getKey();
                    JsonNode ruleNode = entry.getValue();
                    
                    try {
                        FieldMappingRule rule = parseRule(fieldName, ruleNode);
                        rules.put(fieldName, rule);
                        log.debug("加载规则: {} -> {}", fieldName, rule.getDisplayName());
                    } catch (Exception e) {
                        log.error("解析规则失败: {}, 错误: {}", fieldName, e.getMessage());
                    }
                }
                
                log.info("成功加载 {} 个映射规则", rules.size());
                rulesCache = rules;
                return rules;
                
            }
        } catch (IOException e) {
            log.error("加载配置文件失败: {}, 错误: {}, 使用默认规则", configPath, e.getMessage());
            rulesCache = createDefaultRules();
            return rulesCache;
        }
    }
    
    /**
     * 解析单个规则
     */
    private FieldMappingRule parseRule(String fieldName, JsonNode ruleNode) {
        FieldMappingRule rule = new FieldMappingRule();
        
        rule.setFieldName(fieldName);
        rule.setDisplayName(ruleNode.has("displayName") ? ruleNode.get("displayName").asText() : fieldName);
        rule.setRequired(ruleNode.has("required") && ruleNode.get("required").asBoolean());
        rule.setPriority(ruleNode.has("priority") ? ruleNode.get("priority").asInt() : 50);
        
        // 解析exactMatches
        if (ruleNode.has("exactMatches")) {
            List<String> exactMatches = new ArrayList<>();
            ruleNode.get("exactMatches").forEach(node -> exactMatches.add(node.asText()));
            rule.setExactMatches(exactMatches);
        }
        
        // 解析keywords
        if (ruleNode.has("keywords")) {
            List<String> keywords = new ArrayList<>();
            ruleNode.get("keywords").forEach(node -> keywords.add(node.asText()));
            rule.setKeywords(keywords);
        }
        
        // 解析keywordCombinations
        if (ruleNode.has("keywordCombinations")) {
            List<List<String>> combinations = new ArrayList<>();
            ruleNode.get("keywordCombinations").forEach(arrayNode -> {
                List<String> combination = new ArrayList<>();
                arrayNode.forEach(node -> combination.add(node.asText()));
                combinations.add(combination);
            });
            rule.setKeywordCombinations(combinations);
        }
        
        // 解析synonyms
        if (ruleNode.has("synonyms")) {
            List<String> synonyms = new ArrayList<>();
            ruleNode.get("synonyms").forEach(node -> synonyms.add(node.asText()));
            rule.setSynonyms(synonyms);
        }
        
        return rule;
    }
    
    /**
     * 重新加载规则（热加载）
     */
    public void reloadRules() {
        log.info("重新加载映射规则");
        loadRules();
    }
    
    /**
     * 获取字段的映射规则
     * 
     * @param fieldName 系统字段名
     * @return 映射规则，如果不存在返回null
     */
    public FieldMappingRule getRule(String fieldName) {
        if (rulesCache == null) {
            loadRules();
        }
        return rulesCache.get(fieldName);
    }
    
    /**
     * 获取所有规则
     * 
     * @return 所有映射规则
     */
    public Map<String, FieldMappingRule> getAllRules() {
        if (rulesCache == null) {
            loadRules();
        }
        return new HashMap<>(rulesCache);
    }
    
    /**
     * 获取所有必需字段的规则
     * 
     * @return 必需字段的规则列表
     */
    public List<FieldMappingRule> getRequiredRules() {
        if (rulesCache == null) {
            loadRules();
        }
        
        List<FieldMappingRule> requiredRules = new ArrayList<>();
        for (FieldMappingRule rule : rulesCache.values()) {
            if (rule.isRequired()) {
                requiredRules.add(rule);
            }
        }
        return requiredRules;
    }
    
    /**
     * 设置配置文件路径
     * 
     * @param configPath 配置文件路径
     */
    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }
    
    /**
     * 创建默认规则（当配置文件加载失败时使用）
     * 
     * @return 默认规则集合
     */
    private Map<String, FieldMappingRule> createDefaultRules() {
        log.info("创建默认映射规则");
        
        Map<String, FieldMappingRule> rules = new HashMap<>();
        
        // 商品名称（必需）
        FieldMappingRule productNameRule = new FieldMappingRule();
        productNameRule.setFieldName("productName");
        productNameRule.setDisplayName("商品名称");
        productNameRule.setRequired(true);
        productNameRule.setExactMatches(Arrays.asList("商品名称", "商品名", "名称"));
        productNameRule.setKeywords(Arrays.asList("商品", "名称", "品名"));
        productNameRule.setPriority(100);
        rules.put("productName", productNameRule);
        
        // 类目ID（必需）
        FieldMappingRule categoryIdRule = new FieldMappingRule();
        categoryIdRule.setFieldName("categoryId");
        categoryIdRule.setDisplayName("类目ID");
        categoryIdRule.setRequired(true);
        categoryIdRule.setExactMatches(Arrays.asList("商品类目ID", "类目ID", "商品三级类目ID"));
        categoryIdRule.setKeywords(Arrays.asList("类目", "分类", "ID"));
        categoryIdRule.setKeywordCombinations(Arrays.asList(
            Arrays.asList("类目", "ID"),
            Arrays.asList("分类", "ID")
        ));
        categoryIdRule.setPriority(100);
        rules.put("categoryId", categoryIdRule);
        
        // 价格（必需）
        FieldMappingRule priceRule = new FieldMappingRule();
        priceRule.setFieldName("price");
        priceRule.setDisplayName("价格");
        priceRule.setRequired(true);
        priceRule.setExactMatches(Arrays.asList("价格", "价格（元）", "单价"));
        priceRule.setKeywords(Arrays.asList("价格", "单价"));
        priceRule.setPriority(100);
        rules.put("price", priceRule);
        
        // 库存（可选）
        FieldMappingRule stockRule = new FieldMappingRule();
        stockRule.setFieldName("stock");
        stockRule.setDisplayName("库存");
        stockRule.setRequired(false);
        stockRule.setExactMatches(Arrays.asList("库存", "库存数量"));
        stockRule.setKeywords(Arrays.asList("库存", "数量"));
        stockRule.setPriority(80);
        rules.put("stock", stockRule);
        
        // 重量（可选）- 支持v1.3.6和v1.3.7
        FieldMappingRule weightRule = new FieldMappingRule();
        weightRule.setFieldName("weight");
        weightRule.setDisplayName("重量");
        weightRule.setRequired(false);
        weightRule.setExactMatches(Arrays.asList("重量", "毛重", "净重"));
        weightRule.setKeywords(Arrays.asList("重量", "毛重", "净重"));
        weightRule.setPriority(70);
        rules.put("weight", weightRule);
        
        // 商品描述（可选）
        FieldMappingRule descriptionRule = new FieldMappingRule();
        descriptionRule.setFieldName("description");
        descriptionRule.setDisplayName("商品描述");
        descriptionRule.setRequired(false);
        descriptionRule.setExactMatches(Arrays.asList("商品描述", "文字详情", "描述"));
        descriptionRule.setKeywords(Arrays.asList("描述", "详情", "说明"));
        descriptionRule.setPriority(60);
        rules.put("description", descriptionRule);
        
        log.info("创建了 {} 个默认规则", rules.size());
        return rules;
    }
}
