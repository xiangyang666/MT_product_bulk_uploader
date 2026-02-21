package com.meituan.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建成员请求DTO
 */
@Data
public class CreateMemberRequest {
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, message = "密码长度至少为8个字符")
    private String password;
    
    /**
     * 角色：SUPER_ADMIN, ADMIN, USER
     */
    @NotBlank(message = "角色不能为空")
    private String role;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
}
