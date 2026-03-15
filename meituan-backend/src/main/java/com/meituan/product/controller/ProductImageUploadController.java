package com.meituan.product.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meituan.product.entity.Product;
import com.meituan.product.mapper.ProductMapper;
import com.meituan.product.service.AliyunOssService;
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
 * 商品图片上传Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductImageUploadController {

    private final AliyunOssService aliyunOssService;
    private final ProductMapper productMapper;
    private final ObjectMapper objectMapper;

    /**
     * 上传商品图片
     *
     * @param productId 商品ID
     * @param file 图片文件
     * @return 图片URL
     */
    @PostMapping("/{productId}/images/upload")
    public ResponseEntity<Map<String, Object>> uploadProductImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file) {
        try {
            // 检查商品是否存在
            Product product = productMapper.selectById(productId);
            if (product == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "message", "商品不存在"
                ));
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "message", "只能上传图片文件"
                ));
            }

            // 检查文件大小（10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "message", "图片大小不能超过10MB"
                ));
            }

            // 获取当前图片列表
            List<String> imageUrls = parseImageUrls(product.getProductImages());

            // 检查图片数量限制
            if (imageUrls.size() >= 5) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "message", "每个商品最多只能上传5张图片"
                ));
            }

            // 上传到OSS
            String imageUrl = aliyunOssService.uploadFile(file, "products/images");
            imageUrls.add(imageUrl);

            // 更新数据库
            product.setProductImages(objectMapper.writeValueAsString(imageUrls));
            productMapper.updateById(product);

            log.info("商品图片上传成功：productId={}, imageUrl={}", productId, imageUrl);

            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "上传成功",
                    "data", Map.of(
                            "imageUrl", imageUrl,
                            "imageUrls", imageUrls
                    )
            ));

        } catch (Exception e) {
            log.error("上传商品图片失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "code", 500,
                    "message", "上传失败：" + e.getMessage()
            ));
        }
    }

    /**
     * 删除商品图片
     *
     * @param productId 商品ID
     * @param imageUrl 图片URL
     * @return 结果
     */
    @DeleteMapping("/{productId}/images")
    public ResponseEntity<Map<String, Object>> deleteProductImage(
            @PathVariable Long productId,
            @RequestParam String imageUrl) {
        try {
            // 检查商品是否存在
            Product product = productMapper.selectById(productId);
            if (product == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "message", "商品不存在"
                ));
            }

            // 获取当前图片列表
            List<String> imageUrls = parseImageUrls(product.getProductImages());

            // 删除指定图片
            if (!imageUrls.remove(imageUrl)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "message", "图片不存在"
                ));
            }

            // 从OSS删除
            aliyunOssService.deleteFile(imageUrl);

            // 更新数据库
            product.setProductImages(imageUrls.isEmpty() ? null : objectMapper.writeValueAsString(imageUrls));
            productMapper.updateById(product);

            log.info("商品图片删除成功：productId={}, imageUrl={}", productId, imageUrl);

            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "删除成功",
                    "data", Map.of("imageUrls", imageUrls)
            ));

        } catch (Exception e) {
            log.error("删除商品图片失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "code", 500,
                    "message", "删除失败：" + e.getMessage()
            ));
        }
    }

    /**
     * 获取商品图片列表
     *
     * @param productId 商品ID
     * @return 图片列表
     */
    @GetMapping("/{productId}/images")
    public ResponseEntity<Map<String, Object>> getProductImages(@PathVariable Long productId) {
        try {
            Product product = productMapper.selectById(productId);
            if (product == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "message", "商品不存在"
                ));
            }

            List<String> imageUrls = parseImageUrls(product.getProductImages());

            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "获取成功",
                    "data", Map.of("imageUrls", imageUrls)
            ));

        } catch (Exception e) {
            log.error("获取商品图片失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "code", 500,
                    "message", "获取失败：" + e.getMessage()
            ));
        }
    }

    /**
     * 解析图片URL列表
     */
    private List<String> parseImageUrls(String productImages) {
        List<String> urls = new ArrayList<>();
        if (productImages != null && !productImages.trim().isEmpty()) {
            try {
                if (productImages.startsWith("[")) {
                    urls = objectMapper.readValue(productImages, new TypeReference<List<String>>() {});
                } else {
                    urls.add(productImages);
                }
            } catch (Exception e) {
                log.error("解析图片URL失败: {}", productImages, e);
            }
        }
        return urls;
    }
}
