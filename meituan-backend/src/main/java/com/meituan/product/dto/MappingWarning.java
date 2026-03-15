package com.meituan.product.dto;

import lombok.Data;

/**
 * 映射警告
 * 表示一个可能不准确的映射
 */
@Data
public class MappingWarning {
    /**
     * 字段名
     */
    private String fieldName;
    
    /**
     * 列名
     */
    private String columnName;
    
    /**
     * 置信度
     */
    private double confidence;
    
    /**
     * 警告信息
     */
    private String message;
    
    /**
     * 构造函数
     */
    public MappingWarning() {
    }
    
    /**
     * 构造函数
     */
    public MappingWarning(String fieldName, String columnName, double confidence, String message) {
        this.fieldName = fieldName;
        this.columnName = columnName;
        this.confidence = confidence;
        this.message = message;
    }
}
