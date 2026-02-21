package com.meituan.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 支持成员管理和基于角色的访问控制（RBAC）
 */
@Data
@TableName("user")
public class User {
    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户名（唯一）
     */
    private String username;
    
    /**
     * 密码（BCrypt加密）
     */
    private String password;
    
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
     * 角色：SUPER_ADMIN-超级管理员，ADMIN-管理员，USER-普通用户
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
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
