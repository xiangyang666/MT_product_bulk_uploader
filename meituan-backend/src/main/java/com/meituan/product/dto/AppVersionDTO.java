package com.meituan.product.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应用版本 DTO
 */
@Data
public class AppVersionDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 版本号
     */
    private String version;
    
    /**
     * 平台
     */
    private String platform;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 格式化的文件大小 (e.g., "103.8 MB")
     */
    private String fileSizeFormatted;
    
    /**
     * 是否最新版本
     */
    private Boolean isLatest;
    
    /**
     * 下载次数
     */
    private Integer downloadCount;
    
    /**
     * 版本发布说明
     */
    private String releaseNotes;
    
    /**
     * 上传者用户名
     */
    private String uploadedBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
