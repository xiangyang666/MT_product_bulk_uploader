package com.meituan.product.controller;

import com.meituan.product.common.ApiResponse;
import com.meituan.product.dto.ChangePasswordRequest;
import com.meituan.product.dto.UpdateMemberRequest;
import com.meituan.product.dto.UserDTO;
import com.meituan.product.entity.User;
import com.meituan.product.service.MemberService;
import com.meituan.product.service.PasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 个人资料管理控制器
 * 提供当前用户的个人信息查询和修改功能
 */
@Slf4j
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    
    private final MemberService memberService;
    private final PasswordService passwordService;
    
    /**
     * 获取当前用户信息
     * 
     * @param username 用户名（从前端传递）
     * @return 当前用户信息
     */
    @GetMapping
    public ApiResponse<UserDTO> getProfile(@RequestParam(required = false) String username) {
        log.info("获取当前用户信息: {}", username);
        
        try {
            // 如果没有传递用户名，使用默认的admin（临时方案）
            if (username == null || username.isEmpty()) {
                username = "admin";
            }
            
            User currentUser = memberService.getUserByUsername(username);
            if (currentUser == null) {
                return ApiResponse.error("用户不存在");
            }
            
            UserDTO userDTO = memberService.getMemberById(currentUser.getId());
            return ApiResponse.success(userDTO);
        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            return ApiResponse.error("获取用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新个人信息
     * 注意：普通用户不能修改自己的角色
     * 
     * @param request 更新请求
     * @return 更新后的用户信息
     */
    @PutMapping
    public ApiResponse<UserDTO> updateProfile(@RequestBody UpdateMemberRequest request) {
        log.info("更新个人信息");
        
        try {
            // TODO: 从认证上下文获取当前用户
            User currentUser = getCurrentUser();
            
            // 防止普通用户修改自己的角色
            if (request.getRole() != null && 
                !request.getRole().equals(currentUser.getRole())) {
                return ApiResponse.error("不能修改自己的角色");
            }
            
            UserDTO userDTO = memberService.updateMember(
                    currentUser.getId(), request, currentUser);
            
            return ApiResponse.success("更新个人信息成功", userDTO);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("更新个人信息失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新个人信息失败", e);
            return ApiResponse.error("更新个人信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 修改个人密码
     * 需要验证当前密码
     * 
     * @param request 密码修改请求
     * @param username 用户名（从前端传递）
     * @return 修改结果
     */
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestParam(required = false) String username) {
        log.info("修改个人密码: {}", username);
        
        try {
            // 如果没有传递用户名，使用默认的admin（临时方案）
            if (username == null || username.isEmpty()) {
                username = "admin";
            }
            
            User currentUser = memberService.getUserByUsername(username);
            if (currentUser == null) {
                return ApiResponse.error("用户不存在");
            }
            
            // 验证当前密码
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isEmpty()) {
                return ApiResponse.error("请输入当前密码");
            }
            
            log.info("验证密码 - 用户: {}, 输入密码长度: {}, 数据库密码: {}", 
                    username, request.getCurrentPassword().length(), 
                    currentUser.getPassword().substring(0, 20) + "...");
            
            boolean isValid = passwordService.verifyPassword(
                    request.getCurrentPassword(), currentUser.getPassword());
            
            if (!isValid) {
                return ApiResponse.error("当前密码错误");
            }
            
            // 修改密码
            memberService.changePassword(
                    currentUser.getId(), request.getNewPassword(), currentUser);
            
            return ApiResponse.success("修改密码成功，请重新登录", null);
        } catch (IllegalArgumentException e) {
            log.warn("修改密码失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return ApiResponse.error("修改密码失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前登录用户
     * TODO: 实现真实的认证逻辑，从JWT或Session中获取
     * 
     * @return 当前用户
     */
    private User getCurrentUser() {
        // 临时实现：返回默认超级管理员
        // 在实际应用中，应该从Spring Security的认证上下文中获取
        User user = memberService.getUserByUsername("admin");
        if (user == null) {
            // 如果admin用户不存在，创建一个临时的超级管理员对象
            user = new User();
            user.setId(1L);
            user.setUsername("admin");
            user.setRole("SUPER_ADMIN");
        }
        return user;
    }
}
