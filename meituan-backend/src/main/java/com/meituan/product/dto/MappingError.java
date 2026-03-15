package com.meituan.product.dto;

import lombok.Data;
import java.util.List;

/**
 * 映射错误
 * 表示一个必需字段未能成功映射
 */
@Data
public class MappingError {
    /**
     * 字段名
     */
    private String fieldName;
    
    /**
     * 错误信息
     */
    private String message;
    
    /**
     * 建议的列名（可能的匹配）
     */
    private List<String> suggestions;
    
    /**
     * 构造函数
     */
    public MappingError() {
    }
    
    /**
     * 构造函数
     */
    public MappingError(String fieldName, String message) {
        this.fieldName = fieldName;
        this.message = message;
    }
    
    /**
     * 构造函数
     */
    public MappingError(String fieldName, String message, List<String> suggestions) {
        this.fieldName = fieldName;
        this.message = message;
        this.suggestions = suggestions;
    }
}
