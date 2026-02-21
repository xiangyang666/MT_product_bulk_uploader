package com.meituan.product.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品DTO（用于美团API）
 */
@Data
public class ProductDTO {
    
    /**
     * 商品名称
     */
    private String productName;
    
    /**
     * 类目ID
     */
    private String categoryId;
    
    /**
     * 价格
     */
    private BigDecimal price;
    
    /**
     * 库存
     */
    private Integer stock;
    
    /**
     * 商品描述
     */
    private String description;
    
    /**
     * 商品图片URL
     */
    private String imageUrl;
}
