package com.meituan.product.controller;

import com.meituan.product.common.ApiResponse;
import com.meituan.product.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 图片上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {
    
    private final MinioService minioService;
    
    /**
     * 上传商品图片
     * 
     * @param file 图片文件
     * @param merchantId 商家ID（可选，用于分类存储）
     * @return 图片URL
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "merchantId", required = false) Long merchantId) {
        
        try {
            // 验证文件
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "文件不能为空"));
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "只支持图片文件"));
            }
            
            // 验证文件大小（最大5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "图片大小不能超过5MB"));
            }
            
            // 构建文件夹路径
            String folder = merchantId != null ? "products/" + merchantId : "products";
            
            // 上传文件
            String imageUrl = minioService.uploadFile(file, folder);
            
            Map<String, String> result = new HashMap<>();
            result.put("imageUrl", imageUrl);
            result.put("fileName", file.getOriginalFilename());
            
            log.info("图片上传成功: {}", imageUrl);
            
            return ResponseEntity.ok(ApiResponse.success("图片上传成功", result));
            
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "图片上传失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除图片
     * 
     * @param objectName 对象名称
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@RequestParam("objectName") String objectName) {
        try {
            minioService.deleteFile(objectName);
            return ResponseEntity.ok(ApiResponse.success("图片删除成功", null));
        } catch (Exception e) {
            log.error("图片删除失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "图片删除失败: " + e.getMessage()));
        }
    }
}
