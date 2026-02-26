package com.meituan.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 开发者工具密码验证请求
 */
@Data
public class DevToolsPasswordVerifyRequest {
    
    /**
     * 待验证的密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
