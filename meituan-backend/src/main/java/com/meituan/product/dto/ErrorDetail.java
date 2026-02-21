package com.meituan.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 错误详情
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetail {
    
    /**
     * 行号
     */
    private Integer rowNum;
    
    /**
     * 字段名
     */
    private String fieldName;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 原始值
     */
    private String originalValue;
    
    /**
     * 构造函数（不包含原始值）
     */
    public ErrorDetail(Integer rowNum, String fieldName, String errorMessage) {
        this.rowNum = rowNum;
        this.fieldName = fieldName;
        this.errorMessage = errorMessage;
    }
    
    @Override
    public String toString() {
        if (originalValue != null && !originalValue.isEmpty()) {
            return String.format("第%d行，字段[%s]：%s（原始值：%s）", 
                rowNum, fieldName, errorMessage, originalValue);
        } else {
            return String.format("第%d行，字段[%s]：%s", 
                rowNum, fieldName, errorMessage);
        }
    }
}
