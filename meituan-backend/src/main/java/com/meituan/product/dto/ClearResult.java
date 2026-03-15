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

    /**
     * 成功删除的数量（美团平台）
     */
    private Integer meituanDeletedCount;

    /**
     * 删除失败的数量（美团平台）
     */
    private Integer meituanFailedCount;

    /**
     * 美团平台删除失败的原因
     */
    private String errorMessage;

    public ClearResult(Integer deletedCount, Boolean success, String message) {
        this.deletedCount = deletedCount;
        this.success = success;
        this.message = message;
    }

    public ClearResult(Integer deletedCount, Boolean success, String message,
                       Integer meituanDeletedCount, Integer meituanFailedCount, String errorMessage) {
        this.deletedCount = deletedCount;
        this.success = success;
        this.message = message;
        this.meituanDeletedCount = meituanDeletedCount;
        this.meituanFailedCount = meituanFailedCount;
        this.errorMessage = errorMessage;
    }

    public static ClearResult success(Integer deletedCount) {
        return new ClearResult(deletedCount, true, "清空成功");
    }

    public static ClearResult success(Integer deletedCount, Integer meituanDeletedCount,
                                       Integer meituanFailedCount, String message) {
        return new ClearResult(deletedCount, true, message, meituanDeletedCount, meituanFailedCount, null);
    }

    public static ClearResult failure(String message) {
        return new ClearResult(0, false, message);
    }

    public static ClearResult failure(String message, Integer meituanDeletedCount,
                                       Integer meituanFailedCount, String errorMessage) {
        return new ClearResult(0, false, message, meituanDeletedCount, meituanFailedCount, errorMessage);
    }
}
