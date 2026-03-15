package com.meituan.product.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meituan.product.entity.Product;
import com.meituan.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 商品图片导出服务
 * 按照美团规范导出商品图片
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImageExportService {

    private final ProductMapper productMapper;
    private final ObjectMapper objectMapper;

    // 图片大小限制：5MB
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    
    // 最多导出图片数量
    private static final int MAX_EXPORT_COUNT = 300;

    /**
     * 导出商品图片为ZIP
     *
     * @param merchantId 商家ID
     * @param namingType 命名方式：PRODUCT_NAME, BARCODE, STORE_CODE
     * @param productIds 商品ID列表（可选，为空则导出全部）
     * @return ZIP文件路径
     */
    public File exportProductImages(Long merchantId, String namingType, List<Long> productIds) throws Exception {
        // 获取商品列表
        List<Product> products;
        if (productIds != null && !productIds.isEmpty()) {
            products = new ArrayList<>();
            for (Long productId : productIds) {
                Product product = productMapper.selectById(productId);
                if (product != null && product.getMerchantId().equals(merchantId)) {
                    products.add(product);
                }
            }
        } else {
            products = productMapper.selectByMerchantId(merchantId);
        }

        if (products.isEmpty()) {
            throw new RuntimeException("没有找到商品数据");
        }

        // 创建临时目录
        Path tempDir = Files.createTempDirectory("product_images_");
        Path imagesDir = tempDir.resolve("images");
        Files.createDirectories(imagesDir);

        int totalImageCount = 0;
        int successCount = 0;
        int failedCount = 0;

        try {
            // 处理每个商品的图片
            for (Product product : products) {
                String productImages = product.getProductImages();
                if (productImages == null || productImages.trim().isEmpty()) {
                    continue;
                }

                // 解析图片URL列表
                List<String> imageUrls = parseImageUrls(productImages);
                if (imageUrls.isEmpty()) {
                    continue;
                }

                // 检查是否超过最大导出数量
                if (totalImageCount + imageUrls.size() > MAX_EXPORT_COUNT) {
                    log.warn("图片数量超过{}张限制，已导出{}张", MAX_EXPORT_COUNT, totalImageCount);
                    break;
                }

                // 获取商品命名基础
                String baseName = getProductBaseName(product, namingType);
                if (baseName == null || baseName.isEmpty()) {
                    log.warn("商品ID {} 无法获取命名基础，跳过", product.getId());
                    continue;
                }

                // 处理文件名中的特殊字符
                baseName = sanitizeFileName(baseName);

                // 下载并处理每张图片
                for (int i = 0; i < imageUrls.size(); i++) {
                    String imageUrl = imageUrls.get(i);
                    totalImageCount++;

                    try {
                        // 生成文件名
                        String fileName = String.format("%s-%d.jpg", baseName, i + 1);
                        Path imagePath = imagesDir.resolve(fileName);

                        // 下载并处理图片
                        boolean success = downloadAndProcessImage(imageUrl, imagePath.toFile());
                        if (success) {
                            successCount++;
                            log.info("成功处理图片: {}", fileName);
                        } else {
                            failedCount++;
                            log.warn("处理图片失败: {}", imageUrl);
                        }
                    } catch (Exception e) {
                        failedCount++;
                        log.error("处理图片异常: {}", imageUrl, e);
                    }
                }
            }

            log.info("图片处理完成：总计{}张，成功{}张，失败{}张", totalImageCount, successCount, failedCount);

            // 打包成ZIP
            File zipFile = new File(tempDir.toFile(), "product_images_" + System.currentTimeMillis() + ".zip");
            zipDirectory(imagesDir.toFile(), zipFile);

            return zipFile;

        } finally {
            // 清理临时图片文件
            deleteDirectory(imagesDir.toFile());
        }
    }

    /**
     * 解析图片URL列表
     */
    private List<String> parseImageUrls(String productImages) {
        List<String> urls = new ArrayList<>();
        try {
            // 尝试解析JSON数组
            if (productImages.startsWith("[")) {
                urls = objectMapper.readValue(productImages, new TypeReference<List<String>>() {});
            } else {
                // 单个URL
                urls.add(productImages);
            }
        } catch (Exception e) {
            log.error("解析图片URL失败: {}", productImages, e);
        }
        return urls;
    }

    /**
     * 获取商品命名基础
     */
    private String getProductBaseName(Product product, String namingType) {
        switch (namingType) {
            case "PRODUCT_NAME":
                return product.getProductName();
            case "BARCODE":
                return product.getUpcEan();
            case "STORE_CODE":
                return product.getStoreCode();
            default:
                return product.getProductName();
        }
    }

    /**
     * 处理文件名中的特殊字符
     * Windows不支持的字符：/ * ? " < > | :
     * 按美团规范：/ 替换为 _，* 替换为 ^
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "";
        }
        return fileName
                .replace("/", "_")
                .replace("*", "^")
                .replace("?", "")
                .replace("\"", "")
                .replace("<", "")
                .replace(">", "")
                .replace("|", "")
                .replace(":", "")
                .trim();
    }

    /**
     * 下载并处理图片
     */
    private boolean downloadAndProcessImage(String imageUrl, File outputFile) {
        try {
            // 下载图片
            URL url = new URL(imageUrl);
            BufferedImage image = ImageIO.read(url);
            if (image == null) {
                log.warn("无法读取图片: {}", imageUrl);
                return false;
            }

            // 检查图片大小
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            long imageSize = baos.size();

            // 如果图片超过5M，进行压缩
            if (imageSize > MAX_IMAGE_SIZE) {
                log.info("图片大小{}MB，需要压缩", imageSize / 1024.0 / 1024.0);
                compressImage(image, outputFile);
            } else {
                // 直接保存
                ImageIO.write(image, "jpg", outputFile);
            }

            return true;
        } catch (Exception e) {
            log.error("下载图片失败: {}", imageUrl, e);
            return false;
        }
    }

    /**
     * 压缩图片到5M以下
     */
    private void compressImage(BufferedImage image, File outputFile) throws IOException {
        // 使用Thumbnailator压缩
        // 质量设置为0.85，保持较好的图片质量
        Thumbnails.of(image)
                .scale(1.0)  // 保持原尺寸
                .outputQuality(0.85)
                .outputFormat("jpg")
                .toFile(outputFile);

        // 检查压缩后的大小
        long compressedSize = outputFile.length();
        log.info("压缩后图片大小: {}MB", compressedSize / 1024.0 / 1024.0);

        // 如果还是超过5M，降低质量
        if (compressedSize > MAX_IMAGE_SIZE) {
            Thumbnails.of(image)
                    .scale(1.0)
                    .outputQuality(0.7)
                    .outputFormat("jpg")
                    .toFile(outputFile);
            log.info("二次压缩后图片大小: {}MB", outputFile.length() / 1024.0 / 1024.0);
        }
    }

    /**
     * 将目录打包成ZIP
     */
    private void zipDirectory(File sourceDir, File zipFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            File[] files = sourceDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        addToZip(file, file.getName(), zos);
                    }
                }
            }
        }
    }

    /**
     * 添加文件到ZIP
     */
    private void addToZip(File file, String fileName, ZipOutputStream zos) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }

            zos.closeEntry();
        }
    }

    /**
     * 删除目录及其内容
     */
    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}
