package com.meituan.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密码请求DTO
 */
@Data
public class ChangePasswordRequest {
    
    /**
     * 当前密码（用于自己修改密码时验证）
     */
    private String currentPassword;
    
    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, message = "密码长度至少为8个字符")
    private String newPassword;
}
