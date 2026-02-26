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
     * admin-plus 可以查看所有成员
     * 超级管理员可以查看所有成员
     * 
     * @param currentUser 当前用户
     * @param targetUser 目标用户
     * @return 是否有权限
     */
    public boolean canViewMember(User currentUser, User targetUser) {
        if (currentUser == null || targetUser == null) {
            return false;
        }
        
        // admin-plus 可以查看所有成员
        if (isAdminPlus(currentUser)) {
            return true;
        }
        
        // 超级管理员可以查看所有成员
        if (isSuperAdmin(currentUser)) {
            return true;
        }
        
        // 其他用户只能查看自己
        return currentUser.getId().equals(targetUser.getId());
    }
    
    /**
     * 检查用户是否可以编辑目标成员
     * admin-plus 可以编辑所有成员
     * 超级管理员可以编辑除了 admin-plus 之外的所有成员
     * 
     * @param currentUser 当前用户
     * @param targetUser 目标用户
     * @return 是否有权限
     */
    public boolean canEditMember(User currentUser, User targetUser) {
        if (currentUser == null || targetUser == null) {
            return false;
        }
        
        // admin-plus 可以编辑所有成员
        if (isAdminPlus(currentUser)) {
            return true;
        }
        
        // 超级管理员不能编辑 admin-plus
        if (isSuperAdmin(currentUser) && !isAdminPlus(targetUser)) {
            return true;
        }
        
        // 其他用户只能编辑自己的部分信息
        return currentUser.getId().equals(targetUser.getId());
    }
    
    /**
     * 检查用户是否可以删除目标成员
     * admin-plus 可以删除所有成员（除了自己）
     * 超级管理员可以删除除了 admin-plus 之外的所有成员
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
        
        // admin-plus 可以删除所有成员
        if (isAdminPlus(currentUser)) {
            return true;
        }
        
        // 超级管理员不能删除 admin-plus
        if (isSuperAdmin(currentUser) && !isAdminPlus(targetUser)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 检查用户是否可以修改目标成员的角色
     * admin-plus 可以修改所有成员的角色
     * 超级管理员可以修改除了 admin-plus 之外的所有成员的角色
     * 
     * @param currentUser 当前用户
     * @param targetUser 目标用户
     * @return 是否有权限
     */
    public boolean canChangeRole(User currentUser, User targetUser) {
        if (currentUser == null || targetUser == null) {
            return false;
        }
        
        // admin-plus 可以修改所有角色
        if (isAdminPlus(currentUser)) {
            return true;
        }
        
        // 超级管理员不能修改 admin-plus 的角色
        if (isSuperAdmin(currentUser) && !isAdminPlus(targetUser)) {
            return true;
        }
        
        log.warn("用户 {} 尝试修改角色，但权限不足", currentUser.getUsername());
        return false;
    }
    
    /**
     * 根据当前用户权限过滤可见的成员列表
     * admin-plus 和超级管理员可以看到所有成员
     * 
     * @param members 成员列表
     * @param currentUser 当前用户
     * @return 过滤后的成员列表
     */
    public List<User> filterVisibleMembers(List<User> members, User currentUser) {
        if (members == null || currentUser == null) {
            return List.of();
        }
        
        // admin-plus 可以看到所有成员
        if (isAdminPlus(currentUser)) {
            return members;
        }
        
        // 超级管理员可以看到所有成员
        if (isSuperAdmin(currentUser)) {
            return members;
        }
        
        // 其他用户只能看到自己
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
     * 检查用户是否为 admin-plus 账号（最高权限账号）
     * 只有 admin-plus 账号才能管理成员
     * 
     * @param user 用户
     * @return 是否为 admin-plus 账号
     */
    public boolean isAdminPlus(User user) {
        return user != null && "admin-plus".equals(user.getUsername());
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
