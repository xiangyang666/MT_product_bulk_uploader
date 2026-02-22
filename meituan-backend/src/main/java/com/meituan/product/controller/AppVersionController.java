package com.meituan.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meituan.product.dto.AppVersionDTO;
import com.meituan.product.dto.AppVersionUploadRequest;
import com.meituan.product.service.AppVersionService;
import com.meituan.product.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 应用版本管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/app-versions")
@RequiredArgsConstructor
public class AppVersionController {
    
    private final AppVersionService appVersionService;
    private final JwtUtil jwtUtil;
    
    /**
     * 上传新版本
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadVersion(
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute AppVersionUploadRequest request,
            HttpServletRequest httpRequest) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前用户ID
            Long userId = getUserIdFromRequest(httpRequest);
            
            // 上传版本
            AppVersionDTO dto = appVersionService.uploadVersion(file, request, userId);
            
            response.put("code", 200);
            response.put("message", "版本上传成功");
            response.put("data", dto);
            
            log.info("用户 {} 上传版本: {} - {}", userId, dto.getPlatform(), dto.getVersion());
            
        } catch (Exception e) {
            log.error("版本上传失败", e);
            response.put("code", 500);
            response.put("message", "版本上传失败: " + e.getMessage());
            response.put("data", null);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取版本列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listVersions(
            @RequestParam(required = false) String platform,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Page<AppVersionDTO> result = appVersionService.listVersions(platform, page, size);
            
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", result);
            
        } catch (Exception e) {
            log.error("查询版本列表失败", e);
            response.put("code", 500);
            response.put("message", "查询失败: " + e.getMessage());
            response.put("data", null);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取最新版本
     */
    @GetMapping("/latest/{platform}")
    public ResponseEntity<Map<String, Object>> getLatestVersion(@PathVariable String platform) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            AppVersionDTO dto = appVersionService.getLatestVersion(platform);
            
            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", dto);
            
        } catch (Exception e) {
            log.error("查询最新版本失败", e);
            response.put("code", 404);
            response.put("message", e.getMessage());
            response.put("data", null);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 设置为最新版本
     */
    @PutMapping("/{id}/set-latest")
    public ResponseEntity<Map<String, Object>> setLatest(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证管理员权限
            Long userId = getUserIdFromRequest(httpRequest);
            
            appVersionService.setLatest(id);
            
            response.put("code", 200);
            response.put("message", "设置成功");
            response.put("data", null);
            
            log.info("用户 {} 设置版本 {} 为最新", userId, id);
            
        } catch (Exception e) {
            log.error("设置最新版本失败", e);
            response.put("code", 500);
            response.put("message", "设置失败: " + e.getMessage());
            response.put("data", null);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新版本信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateVersion(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest httpRequest) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证管理员权限
            Long userId = getUserIdFromRequest(httpRequest);
            
            String version = requestBody.get("version");
            String releaseNotes = requestBody.get("releaseNotes");
            
            appVersionService.updateVersion(id, version, releaseNotes);
            
            response.put("code", 200);
            response.put("message", "更新成功");
            response.put("data", null);
            
            log.info("用户 {} 更新版本 {} 信息", userId, id);
            
        } catch (Exception e) {
            log.error("更新版本信息失败", e);
            response.put("code", 500);
            response.put("message", "更新失败: " + e.getMessage());
            response.put("data", null);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除版本
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteVersion(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证管理员权限
            Long userId = getUserIdFromRequest(httpRequest);
            
            appVersionService.deleteVersion(id);
            
            response.put("code", 200);
            response.put("message", "删除成功");
            response.put("data", null);
            
            log.info("用户 {} 删除版本 {}", userId, id);
            
        } catch (Exception e) {
            log.error("删除版本失败", e);
            response.put("code", 500);
            response.put("message", "删除失败: " + e.getMessage());
            response.put("data", null);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 生成下载链接
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Map<String, Object>> downloadVersion(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String url = appVersionService.generateDownloadUrl(id);
            
            response.put("code", 200);
            response.put("message", "获取下载链接成功");
            response.put("data", url);
            
        } catch (Exception e) {
            log.error("生成下载链接失败", e);
            response.put("code", 500);
            response.put("message", "生成下载链接失败: " + e.getMessage());
            response.put("data", null);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new RuntimeException("未授权访问");
    }
}
