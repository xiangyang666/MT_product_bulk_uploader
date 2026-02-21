package com.meituan.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 生成的文件记录实体类
 */
@Data
@TableName("generated_files")
public class GeneratedFile {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 商家ID
     */
    private Long merchantId;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件路径
     */
    private String filePath;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 文件类型：TEMPLATE-模板文件
     */
    private String fileType;
    
    /**
     * 商品数量
     */
    private Integer productCount;
    
    /**
     * 生成时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;
    
    /**
     * 下载次数
     */
    private Integer downloadCount;
}
