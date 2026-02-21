package com.meituan.product.dto;

import lombok.Data;

/**
 * 商品统计信息DTO
 */
@Data
public class ProductStats {
    
    /**
     * 商品总数
     */
    private Long totalCount;
    
    /**
     * 待上传数量（status = 0 或 null）
     */
    private Long pendingCount;
    
    /**
     * 已上传数量（status = 1）
     */
    private Long uploadedCount;
    
    /**
     * 上传失败数量（status = 2）
     */
    private Long failedCount;
    
    public ProductStats() {
        this.totalCount = 0L;
        this.pendingCount = 0L;
        this.uploadedCount = 0L;
        this.failedCount = 0L;
    }
    
    public ProductStats(Long totalCount, Long pendingCount, Long uploadedCount, Long failedCount) {
        this.totalCount = totalCount != null ? totalCount : 0L;
        this.pendingCount = pendingCount != null ? pendingCount : 0L;
        this.uploadedCount = uploadedCount != null ? uploadedCount : 0L;
        this.failedCount = failedCount != null ? failedCount : 0L;
    }
}
