package com.meituan.product.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 生成文件DTO
 */
@Data
public class GeneratedFileDTO {
    
    /**
     * 文件ID
     */
    private Long id;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 文件大小（格式化，如 "2.5 MB"）
     */
    private String fileSizeFormatted;
    
    /**
     * 商品数量
     */
    private Integer productCount;
    
    /**
     * 生成时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 下载次数
     */
    private Integer downloadCount;
    
    /**
     * 是否已过期
     */
    private Boolean expired;
}
