package com.meituan.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.meituan.product.common.ApiResponse;
import com.meituan.product.dto.*;
import com.meituan.product.entity.User;
import com.meituan.product.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 成员管理控制器
 * 提供成员的增删改查API
 */
@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    
    private final MemberService memberService;
    
    /**
     * 查询成员列表（带筛选和分页）
     * 
     * @param username 用户名筛选（可选）
     * @param role 角色筛选（可选）
     * @param status 状态筛选（可选）
     * @param page 页码，默认1
     * @param size 每页大小，默认10
     * @return 成员列表分页结果
     */
    @GetMapping
    public ApiResponse<IPage<UserDTO>> listMembers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        log.info("查询成员列表: username={}, role={}, status={}, page={}, size={}",
                username, role, status, page, size);
        
        try {
            // TODO: 从认证上下文获取当前用户
            // 临时方案：使用默认超级管理员
            User currentUser = getCurrentUser();
            
            IPage<UserDTO> result = memberService.listMembers(
                    username, role, status, page, size, currentUser);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("查询成员列表失败", e);
            return ApiResponse.error("查询成员列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取成员详情
     * 
     * @param id 成员ID
     * @return 成员详情
     */
    @GetMapping("/{id}")
    public ApiResponse<UserDTO> getMember(@PathVariable Long id) {
        log.info("获取成员详情: id={}", id);
        
        try {
            UserDTO user = memberService.getMemberById(id);
            return ApiResponse.success(user);
        } catch (IllegalArgumentException e) {
            log.warn("获取成员详情失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("获取成员详情失败", e);
            return ApiResponse.error("获取成员详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建成员（仅超级管理员）
     * 
     * @param request 创建请求
     * @return 创建的成员信息
     */
    @PostMapping
    public ApiResponse<UserDTO> createMember(@Valid @RequestBody CreateMemberRequest request) {
        log.info("创建成员: username={}, role={}", request.getUsername(), request.getRole());
        
        try {
            // TODO: 从认证上下文获取当前用户
            User currentUser = getCurrentUser();
            
            UserDTO user = memberService.createMember(request, currentUser);
            return ApiResponse.success("创建成员成功", user);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("创建成员失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("创建成员失败", e);
            return ApiResponse.error("创建成员失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新成员信息
     * 
     * @param id 成员ID
     * @param request 更新请求
     * @return 更新后的成员信息
     */
    @PutMapping("/{id}")
    public ApiResponse<UserDTO> updateMember(
            @PathVariable Long id,
            @RequestBody UpdateMemberRequest request) {
        
        log.info("更新成员: id={}", id);
        
        try {
            // TODO: 从认证上下文获取当前用户
            User currentUser = getCurrentUser();
            
            UserDTO user = memberService.updateMember(id, request, currentUser);
            return ApiResponse.success("更新成员成功", user);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("更新成员失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新成员失败", e);
            return ApiResponse.error("更新成员失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除成员
     * 
     * @param id 成员ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMember(@PathVariable Long id) {
        log.info("删除成员: id={}", id);
        
        try {
            // TODO: 从认证上下文获取当前用户
            User currentUser = getCurrentUser();
            
            memberService.deleteMember(id, currentUser);
            return ApiResponse.success("删除成员成功", null);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("删除成员失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("删除成员失败", e);
            return ApiResponse.error("删除成员失败: " + e.getMessage());
        }
    }
    
    /**
     * 修改成员密码
     * 
     * @param id 成员ID
     * @param request 密码修改请求
     * @return 修改结果
     */
    @PutMapping("/{id}/password")
    public ApiResponse<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) {
        
        log.info("修改成员密码: id={}", id);
        
        try {
            // TODO: 从认证上下文获取当前用户
            User currentUser = getCurrentUser();
            
            memberService.changePassword(id, request.getNewPassword(), currentUser);
            return ApiResponse.success("修改密码成功", null);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("修改密码失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return ApiResponse.error("修改密码失败: " + e.getMessage());
        }
    }
    
    /**
     * 修改成员状态（启用/禁用）
     * 
     * @param id 成员ID
     * @param request 状态修改请求
     * @return 修改结果
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Void> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeStatusRequest request) {
        
        log.info("修改成员状态: id={}, status={}", id, request.getStatus());
        
        try {
            // TODO: 从认证上下文获取当前用户
            User currentUser = getCurrentUser();
            
            memberService.changeStatus(id, request.getStatus(), currentUser);
            return ApiResponse.success("修改状态成功", null);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("修改状态失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("修改状态失败", e);
            return ApiResponse.error("修改状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前登录用户
     * 从Spring Security的认证上下文中获取
     * 
     * @return 当前用户
     */
    private User getCurrentUser() {
        try {
            // 从 Spring Security 上下文获取认证信息
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                log.debug("从认证上下文获取用户: {}", username);
                
                User user = memberService.getUserByUsername(username);
                if (user != null) {
                    return user;
                }
            }
            
            // 如果无法从上下文获取，抛出异常
            throw new IllegalStateException("未找到当前登录用户信息");
        } catch (Exception e) {
            log.error("获取当前用户失败", e);
            throw new IllegalStateException("获取当前用户失败: " + e.getMessage());
        }
    }
}
