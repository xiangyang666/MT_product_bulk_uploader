package com.meituan.product.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 应用版本上传请求
 */
@Data
public class AppVersionUploadRequest {
    
    /**
     * 版本号 (格式: X.Y.Z)
     */
    @NotBlank(message = "版本号不能为空")
    @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+$", message = "版本号格式错误，应为 X.Y.Z")
    private String version;
    
    /**
     * 版本发布说明（可选）
     */
    private String releaseNotes;
}
