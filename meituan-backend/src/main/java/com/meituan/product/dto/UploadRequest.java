package com.meituan.product.dto;

import lombok.Data;

import java.util.List;

/**
 * 上传请求DTO
 */
@Data
public class UploadRequest {
    
    /**
     * 商品ID列表
     */
    private List<Long> productIds;
    
    /**
     * 商家ID
     */
    private Long merchantId;
    
    /**
     * 访问令牌
     */
    private String accessToken;
}
