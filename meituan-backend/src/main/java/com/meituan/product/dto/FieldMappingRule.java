package com.meituan.product.dto;

import lombok.Data;
import java.util.List;

/**
 * 字段映射规则
 * 定义如何将Excel列名映射到系统字段
 */
@Data
public class FieldMappingRule {
    /**
     * 系统字段名（如：productName, categoryId）
     */
    private String fieldName;
    
    /**
     * 显示名称（如：商品名称、类目ID）
     */
    private String displayName;
    
    /**
     * 是否为必需字段
     */
    private boolean required;
    
    /**
     * 精确匹配的列名列表
     * 例如：["商品名称", "商品名"]
     */
    private List<String> exactMatches;
    
    /**
     * 关键词列表
     * 例如：["商品", "名称", "product", "name"]
     */
    private List<String> keywords;
    
    /**
     * 关键词组合列表
     * 例如：[["类目", "ID"], ["分类", "编号"]]
     * 表示列名需要同时包含组合中的所有关键词
     */
    private List<List<String>> keywordCombinations;
    
    /**
     * 同义词列表
     * 例如：["商品名", "产品名称", "货品名称"]
     */
    private List<String> synonyms;
    
    /**
     * 优先级（用于冲突解决，数值越大优先级越高）
     */
    private int priority;
}
