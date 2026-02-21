package com.meituan.product.dto;

import lombok.Data;

/**
 * 清空请求DTO
 */
@Data
public class ClearRequest {
    
    /**
     * 商家ID
     */
    private Long merchantId;
    
    /**
     * 访问令牌
     */
    private String accessToken;
}
