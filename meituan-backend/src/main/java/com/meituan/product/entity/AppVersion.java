package com.meituan.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应用版本实体类
 */
@Data
@TableName("app_version")
public class AppVersion {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 版本号 (e.g., 1.0.0)
     */
    private String version;
    
    /**
     * 平台: Windows, macOS
     */
    private String platform;
    
    /**
     * 原始文件名
     */
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * MinIO存储路径
     */
    private String filePath;
    
    /**
     * 是否最新版本: 0-否, 1-是
     */
    private Integer isLatest;
    
    /**
     * 下载次数
     */
    private Integer downloadCount;
    
    /**
     * 版本发布说明
     */
    private String releaseNotes;
    
    /**
     * 上传者用户ID
     */
    private Long uploadedBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
