package com.meituan.product.dto;

import lombok.Data;

/**
 * 生成模板响应DTO
 */
@Data
public class GenerateTemplateResponse {
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 商品数量
     */
    private Integer productCount;
    
    public GenerateTemplateResponse(String fileName, Long fileSize, Integer productCount) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.productCount = productCount;
    }
}
