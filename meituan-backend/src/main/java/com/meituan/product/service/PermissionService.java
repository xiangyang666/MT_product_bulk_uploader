package com.meituan.product.service;

import com.meituan.product.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限服务类
 * 实现基于角色的访问控制（RBAC）
 */
@Slf4j
@Service
public class PermissionService {
    
    /**
     * 角色常量
     */
    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";
    
    /**
     * 检查用户是否可以查看目标成员
     * 
     * @param currentUser 当前用户
     * @param targetUser 目标用户
     * @return 是否有权限
     */
    public boolean canViewMember(User currentUser, User targetUser) {
        if (currentUser == null || targetUser == null) {
            return false;
        }
        
        String currentRole = currentUser.getRole();
        String targetRole = targetUser.getRole();
        
        // 超级管理员可以查看所有人
        if (ROLE_SUPER_ADMIN.equals(currentRole)) {
            return true;
        }
        
        // 管理员只能查看普通用户
        if (ROLE_ADMIN.equals(currentRole)) {
            return ROLE_USER.equals(targetRole);
        }
        
        // 普通用户只能查看自己
        return currentUser.getId().equals(targetUser.getId());
    }
    
    /**
     * 检查用户是否可以编辑目标成员
     * 
     * @param currentUser 当前用户
     * @param targetUser 目标用户
     * @return 是否有权限
     */
    public boolean canEditMember(User currentUser, User targetUser) {
        if (currentUser == null || targetUser == null) {
            return false;
        }
        
        String currentRole = currentUser.getRole();
        String targetRole = targetUser.getRole();
        
        // 超级管理员可以编辑所有人（除了不能删除自己）
        if (ROLE_SUPER_ADMIN.equals(currentRole)) {
            return true;
        }
        
        // 管理员只能编辑普通用户
        if (ROLE_ADMIN.equals(currentRole)) {
            return ROLE_USER.equals(targetRole);
        }
        
        // 普通用户只能编辑自己的部分信息
        return currentUser.getId().equals(targetUser.getId());
    }
    
    /**
     * 检查用户是否可以删除目标成员
     * 
     * @param currentUser 当前用户
     * @param targetUser 目标用户
     * @return 是否有权限
     */
    public boolean canDeleteMember(User currentUser, User targetUser) {
        if (currentUser == null || targetUser == null) {
            return false;
        }
        
        // 不能删除自己
        if (currentUser.getId().equals(targetUser.getId())) {
            log.warn("用户尝试删除自己的账号: {}", currentUser.getUsername());
            return false;
        }
        
        String currentRole = currentUser.getRole();
        String targetRole = targetUser.getRole();
        
        // 超级管理员可以删除其他所有人
        if (ROLE_SUPER_ADMIN.equals(currentRole)) {
            return true;
        }
        
        // 管理员只能删除普通用户
        if (ROLE_ADMIN.equals(currentRole)) {
            return ROLE_USER.equals(targetRole);
        }
        
        // 普通用户不能删除任何人
        return false;
    }
    
    /**
     * 检查用户是否可以修改目标成员的角色
     * 
     * @param currentUser 当前用户
     * @param targetUser 目标用户
     * @return 是否有权限
     */
    public boolean canChangeRole(User currentUser, User targetUser) {
        if (currentUser == null || targetUser == null) {
            return false;
        }
        
        String currentRole = currentUser.getRole();
        
        // 只有超级管理员可以修改角色
        if (ROLE_SUPER_ADMIN.equals(currentRole)) {
            return true;
        }
        
        // 管理员和普通用户都不能修改角色
        log.warn("用户 {} 尝试修改角色，但权限不足", currentUser.getUsername());
        return false;
    }
    
    /**
     * 根据当前用户权限过滤可见的成员列表
     * 
     * @param members 成员列表
     * @param currentUser 当前用户
     * @return 过滤后的成员列表
     */
    public List<User> filterVisibleMembers(List<User> members, User currentUser) {
        if (members == null || currentUser == null) {
            return List.of();
        }
        
        String currentRole = currentUser.getRole();
        
        // 超级管理员可以看到所有人
        if (ROLE_SUPER_ADMIN.equals(currentRole)) {
            return members;
        }
        
        // 管理员只能看到普通用户
        if (ROLE_ADMIN.equals(currentRole)) {
            return members.stream()
                .filter(user -> ROLE_USER.equals(user.getRole()))
                .collect(Collectors.toList());
        }
        
        // 普通用户只能看到自己
        return members.stream()
            .filter(user -> user.getId().equals(currentUser.getId()))
            .collect(Collectors.toList());
    }
    
    /**
     * 检查用户是否为超级管理员
     * 
     * @param user 用户
     * @return 是否为超级管理员
     */
    public boolean isSuperAdmin(User user) {
        return user != null && ROLE_SUPER_ADMIN.equals(user.getRole());
    }
    
    /**
     * 检查用户是否为管理员（包括超级管理员）
     * 
     * @param user 用户
     * @return 是否为管理员
     */
    public boolean isAdmin(User user) {
        if (user == null) {
            return false;
        }
        String role = user.getRole();
        return ROLE_SUPER_ADMIN.equals(role) || ROLE_ADMIN.equals(role);
    }
    
    /**
     * 检查角色是否有效
     * 
     * @param role 角色
     * @return 是否有效
     */
    public boolean isValidRole(String role) {
        return ROLE_SUPER_ADMIN.equals(role) || 
               ROLE_ADMIN.equals(role) || 
               ROLE_USER.equals(role);
    }
}
