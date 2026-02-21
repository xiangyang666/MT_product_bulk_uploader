package com.meituan.product.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户DTO（不包含密码字段，用于返回给前端）
 */
@Data
public class UserDTO {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
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
    
    /**
     * 关联商家ID
     */
    private Long merchantId;
    
    /**
     * 角色：SUPER_ADMIN, ADMIN, USER
     */
    private String role;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 创建者用户名
     */
    private String createdBy;
    
    /**
     * 更新者用户名
     */
    private String updatedBy;
}
