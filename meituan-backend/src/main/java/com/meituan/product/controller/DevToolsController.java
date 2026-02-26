package com.meituan.product.controller;

import com.meituan.product.common.ApiResponse;
import com.meituan.product.dto.DevToolsPasswordRequest;
import com.meituan.product.dto.DevToolsPasswordVerifyRequest;
import com.meituan.product.service.DevToolsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 开发者工具控制器
 * 只有 admin-plus 账号可以访问
 */
@Slf4j
@RestController
@RequestMapping("/api/dev-tools")
@RequiredArgsConstructor
public class DevToolsController {
    
    private final DevToolsService devToolsService;
    
    /**
     * 设置开发者工具密码
     * 只有 admin-plus 可以设置
     * 
     * @param request 密码设置请求
     * @return 设置结果
     */
    @PostMapping("/password")
    public ApiResponse<Void> setPassword(@Valid @RequestBody DevToolsPasswordRequest request) {
        try {
            // 获取当前用户
            String username = getCurrentUsername();
            
            // 验证权限：只有 admin-plus 可以设置
            if (!"admin-plus".equals(username)) {
                log.warn("用户 {} 尝试设置开发者工具密码，但权限不足", username);
                return ApiResponse.error("权限不足，只有 admin-plus 可以设置开发者工具密码");
            }
            
            // 设置密码
            devToolsService.setDevToolsPassword(request.getPassword(), username);
            
            log.info("用户 {} 成功设置开发者工具密码", username);
            return ApiResponse.success("开发者工具密码设置成功", null);
            
        } catch (Exception e) {
            log.error("设置开发者工具密码失败", e);
            return ApiResponse.error("设置开发者工具密码失败：" + e.getMessage());
        }
    }
    
    /**
     * 验证开发者工具密码
     * 
     * @param request 密码验证请求
     * @return 验证结果
     */
    @PostMapping("/password/verify")
    public ApiResponse<Map<String, Boolean>> verifyPassword(
            @Valid @RequestBody DevToolsPasswordVerifyRequest request) {
        try {
            boolean isValid = devToolsService.verifyDevToolsPassword(request.getPassword());
            
            Map<String, Boolean> result = new HashMap<>();
            result.put("valid", isValid);
            
            return ApiResponse.success(isValid ? "密码验证通过" : "密码验证失败", result);
            
        } catch (Exception e) {
            log.error("验证开发者工具密码失败", e);
            return ApiResponse.error("验证开发者工具密码失败：" + e.getMessage());
        }
    }
    
    /**
     * 检查是否已设置开发者工具密码
     * 
     * @return 设置状态
     */
    @GetMapping("/password/status")
    public ApiResponse<Map<String, Boolean>> getPasswordStatus() {
        try {
            boolean hasPassword = devToolsService.hasDevToolsPassword();
            
            Map<String, Boolean> result = new HashMap<>();
            result.put("hasPassword", hasPassword);
            
            return ApiResponse.success(result);
            
        } catch (Exception e) {
            log.error("获取开发者工具密码状态失败", e);
            return ApiResponse.error("获取开发者工具密码状态失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取当前登录用户名
     * 
     * @return 用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        throw new IllegalStateException("未找到当前登录用户信息");
    }
}
