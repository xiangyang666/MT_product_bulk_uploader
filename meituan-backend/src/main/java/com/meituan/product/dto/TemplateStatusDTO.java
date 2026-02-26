package com.meituan.product.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 模板状态DTO
 * 用于返回商家的美团模板状态信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateStatusDTO {
    
    /**
     * 是否有可用的美团模板
     */
    private Boolean hasTemplate;
    
    /**
     * 模板名称
     */
    private String templateName;
    
    /**
     * 上传时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 模板类型
     */
    private String templateType;
}
