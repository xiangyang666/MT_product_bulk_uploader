package com.meituan.product.dto;

import lombok.Data;

/**
 * 更新成员请求DTO
 */
@Data
public class UpdateMemberRequest {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 角色：SUPER_ADMIN, ADMIN, USER
     */
    private String role;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
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
