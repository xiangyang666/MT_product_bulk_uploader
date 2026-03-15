package com.meituan.product.enums;

/**
 * 字段映射匹配类型
 * 用于标识列名与字段的匹配方式
 */
public enum MatchType {
    /**
     * 精确匹配 - 列名完全相同
     * 置信度: 1.0
     */
    EXACT("精确匹配", 1.0),
    
    /**
     * 标准化匹配 - 去除空格、标点后匹配
     * 置信度: 0.95
     */
    NORMALIZED("标准化匹配", 0.95),
    
    /**
     * 关键词匹配 - 包含所有关键词
     * 置信度: 0.8-0.9
     */
    KEYWORD("关键词匹配", 0.85),
    
    /**
     * 模糊匹配 - 基于字符串相似度
     * 置信度: 0.5-0.8
     */
    FUZZY("模糊匹配", 0.65),
    
    /**
     * 手动指定 - 用户手动调整的映射
     * 置信度: 1.0
     */
    MANUAL("手动指定", 1.0);
    
    private final String displayName;
    private final double baseConfidence;
    
    MatchType(String displayName, double baseConfidence) {
        this.displayName = displayName;
        this.baseConfidence = baseConfidence;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public double getBaseConfidence() {
        return baseConfidence;
    }
}
