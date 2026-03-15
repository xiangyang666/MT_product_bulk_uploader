package com.meituan.product.controller;

import com.meituan.product.service.ProductImageExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 商品图片管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageExportService productImageExportService;

    /**
     * 导出商品图片
     *
     * @param request 请求参数
     * @return ZIP文件
     */
    @PostMapping("/export-images")
    public ResponseEntity<Resource> exportProductImages(@RequestBody Map<String, Object> request) {
        try {
            // 解析参数
            Long merchantId = Long.valueOf(request.getOrDefault("merchantId", 1).toString());
            String namingType = request.getOrDefault("namingType", "PRODUCT_NAME").toString();
            
            @SuppressWarnings("unchecked")
            List<Long> productIds = (List<Long>) request.get("productIds");

            log.info("开始导出商品图片：merchantId={}, namingType={}, productIds={}", 
                    merchantId, namingType, productIds);

            // 导出图片
            File zipFile = productImageExportService.exportProductImages(merchantId, namingType, productIds);

            // 返回ZIP文件
            Resource resource = new FileSystemResource(zipFile);
            String fileName = URLEncoder.encode("商品图片_" + System.currentTimeMillis() + ".zip", 
                    StandardCharsets.UTF_8.toString());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(zipFile.length())
                    .body(resource);

        } catch (Exception e) {
            log.error("导出商品图片失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取图片导出状态
     *
     * @param merchantId 商家ID
     * @return 统计信息
     */
    @GetMapping("/images/stats")
    public ResponseEntity<Map<String, Object>> getImageStats(@RequestParam Long merchantId) {
        try {
            // TODO: 实现统计逻辑
            // 返回有图片的商品数量、总图片数量等
            return ResponseEntity.ok(Map.of(
                    "totalProducts", 0,
                    "productsWithImages", 0,
                    "totalImages", 0
            ));
        } catch (Exception e) {
            log.error("获取图片统计失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
