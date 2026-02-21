package com.meituan.product.enums;

/**
 * Excel文件格式类型
 */
public enum FormatType {
    /**
     * 美团格式（50+列）
     */
    MEITUAN("美团格式"),
    
    /**
     * 标准格式（6列）
     */
    STANDARD("标准格式"),
    
    /**
     * 未知格式
     */
    UNKNOWN("未知格式");
    
    private final String description;
    
    FormatType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
