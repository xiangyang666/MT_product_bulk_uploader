package com.meituan.product.dto;

import lombok.Data;

/**
 * 清空结果DTO
 */
@Data
public class ClearResult {
    
    /**
     * 删除的商品数量
     */
    private Integer deletedCount;
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 消息
     */
    private String message;
    
    public ClearResult(Integer deletedCount, Boolean success, String message) {
        this.deletedCount = deletedCount;
        this.success = success;
        this.message = message;
    }
    
    public static ClearResult success(Integer deletedCount) {
        return new ClearResult(deletedCount, true, "清空成功");
    }
    
    public static ClearResult failure(String message) {
        return new ClearResult(0, false, message);
    }
}
