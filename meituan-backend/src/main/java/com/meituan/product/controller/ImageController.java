package com.meituan.product.controller;

import com.meituan.product.common.ApiResponse;
import com.meituan.product.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
     * 批量上传商品图片
     *
     * @param files 图片文件列表
     * @param merchantId 商家ID（可选，用于分类存储）
     * @return 图片URL列表
     */
    @PostMapping("/batch-upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchUploadImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "merchantId", required = false) Long merchantId) {

        try {
            // 验证文件列表
            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "文件列表不能为空"));
            }

            // 限制批量上传数量（最多100张）
            if (files.length > 100) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "批量上传最多支持100张图片"));
            }

            // 构建文件夹路径
            String folder = merchantId != null ? "products/" + merchantId : "products";

            List<Map<String, String>> uploadedImages = new ArrayList<>();
            List<String> errorFiles = new ArrayList<>();

            // 逐个上传文件
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];

                try {
                    // 验证单个文件
                    if (file.isEmpty()) {
                        errorFiles.add(file.getOriginalFilename() + "：文件为空");
                        continue;
                    }

                    // 验证文件类型
                    String contentType = file.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        errorFiles.add(file.getOriginalFilename() + "：不是图片文件");
                        continue;
                    }

                    // 验证文件大小（最大5MB）
                    if (file.getSize() > 5 * 1024 * 1024) {
                        errorFiles.add(file.getOriginalFilename() + "：超过5MB限制");
                        continue;
                    }

                    // 上传文件
                    String imageUrl = minioService.uploadFile(file, folder);

                    Map<String, String> imageInfo = new HashMap<>();
                    imageInfo.put("imageUrl", imageUrl);
                    imageInfo.put("fileName", file.getOriginalFilename());
                    imageInfo.put("size", String.format("%.2fKB", file.getSize() / 1024.0));

                    uploadedImages.add(imageInfo);

                    log.info("图片上传成功 [{}/{}]：{}", i + 1, files.length, imageUrl);

                } catch (Exception e) {
                    log.error("上传图片失败：{}", file.getOriginalFilename(), e);
                    errorFiles.add(file.getOriginalFilename() + "：" + e.getMessage());
                }
            }

            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("uploadedImages", uploadedImages);
            result.put("errorFiles", errorFiles);
            result.put("totalCount", files.length);
            result.put("successCount", uploadedImages.size());
            result.put("errorCount", errorFiles.size());

            String message = String.format("批量上传完成：成功 %d 张，失败 %d 张",
                    uploadedImages.size(), errorFiles.size());

            log.info("批量上传完成：总数 {}，成功 {}，失败 {}",
                    files.length, uploadedImages.size(), errorFiles.size());

            return ResponseEntity.ok(ApiResponse.success(message, result));

        } catch (Exception e) {
            log.error("批量上传图片失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "批量上传失败: " + e.getMessage()));
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
