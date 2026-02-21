package com.meituan.product.dto;

import lombok.Data;

import java.util.List;

/**
 * 生成模板请求DTO
 */
@Data
public class GenerateTemplateRequest {
    
    /**
     * 商品ID列表
     */
    private List<Long> productIds;
    
    /**
     * 商家ID
     */
    private Long merchantId;
}
