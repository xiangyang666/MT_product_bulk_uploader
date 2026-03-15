package com.meituan.product.dto;

import com.meituan.product.enums.MatchType;
import lombok.Data;

/**
 * 列映射结果
 * 表示一个Excel列到系统字段的映射关系
 */
@Data
public class ColumnMapping {
    /**
     * 系统字段名（如：productName）
     */
    private String fieldName;
    
    /**
     * Excel列名（如：商品名称）
     */
    private String columnName;
    
    /**
     * 列索引（从0开始）
     */
    private int columnIndex;
    
    /**
     * 映射置信度（0-1之间）
     */
    private double confidence;
    
    /**
     * 匹配类型
     */
    private MatchType matchType;
    
    /**
     * 是否为必需字段
     */
    private boolean required;
    
    /**
     * 构造函数
     */
    public ColumnMapping() {
    }
    
    /**
     * 构造函数
     */
    public ColumnMapping(String fieldName, String columnName, int columnIndex, 
                        double confidence, MatchType matchType, boolean required) {
        this.fieldName = fieldName;
        this.columnName = columnName;
        this.columnIndex = columnIndex;
        this.confidence = confidence;
        this.matchType = matchType;
        this.required = required;
    }
}
