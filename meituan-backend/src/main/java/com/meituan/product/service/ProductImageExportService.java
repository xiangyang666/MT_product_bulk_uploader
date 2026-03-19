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
            // 按商品名称分组，每个名称只取第一个商品的图片
            java.util.Map<String, List<String>> productImageMap = new java.util.LinkedHashMap<>();

            for (Product product : products) {
                // 获取商品命名基础
                String baseName = getProductBaseName(product, namingType);
                if (baseName == null || baseName.isEmpty()) {
                    log.warn("商品ID {} 无法获取命名基础，跳过", product.getId());
                    continue;
                }

                // 处理文件名中的特殊字符
                baseName = sanitizeFileName(baseName);

                // 如果该商品名称已经存在（已有商品处理过），则跳过
                if (productImageMap.containsKey(baseName)) {
                    log.debug("商品ID {} (名称: {}) 已存在，跳过", product.getId(), baseName);
                    continue;
                }

                String productImages = product.getProductImages();
                if (productImages == null || productImages.trim().isEmpty()) {
                    log.debug("商品ID {} (名称: {}) 没有图片，跳过", product.getId(), baseName);
                    continue;
                }

                // 解析图片URL列表
                List<String> imageUrls = parseImageUrls(productImages);
                if (imageUrls.isEmpty()) {
                    log.debug("商品ID {} (名称: {}) 图片解析为空，跳过", product.getId(), baseName);
                    continue;
                }

                // 只添加第一个出现的商品图片
                productImageMap.put(baseName, imageUrls);
                log.debug("商品ID {} (名称: {}) 添加了 {} 张图片", product.getId(), baseName, imageUrls.size());
            }

            log.info("按商品名称分组后，共{}个不同的商品名称", productImageMap.size());

            // 处理每个商品名称的图片
            for (java.util.Map.Entry<String, List<String>> entry : productImageMap.entrySet()) {
                String baseName = entry.getKey();
                List<String> imageUrls = entry.getValue(); // 直接使用List，无需转换

                // 检查是否超过最大导出数量
                if (totalImageCount + imageUrls.size() > MAX_EXPORT_COUNT) {
                    log.warn("图片数量超过{}张限制，已导出{}张", MAX_EXPORT_COUNT, totalImageCount);
                    break;
                }

                log.info("处理商品: {}，共{}张图片", baseName, imageUrls.size());

                // 下载并处理每张图片
                for (int i = 0; i < imageUrls.size(); i++) {
                    String imageUrl = imageUrls.get(i);
                    totalImageCount++;

                    try {
                        // 生成文件名
                        String fileName = String.format("%s-%d.jpg", baseName, i + 1);
                        Path imagePath = imagesDir.resolve(fileName);

                        log.info("准备下载图片[{}]: {} -> {}", i + 1, imageUrl, fileName);

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

            // 在打包前记录临时目录状态
            File[] imageFiles = imagesDir.toFile().listFiles();
            if (imageFiles != null) {
                log.info("准备打包ZIP，临时目录中有{}个图片文件：", imageFiles.length);
                for (File imgFile : imageFiles) {
                    log.info("  - {}，大小：{}字节", imgFile.getName(), imgFile.length());
                }
            } else {
                log.warn("临时目录中没有找到图片文件：{}", imagesDir.toFile().getAbsolutePath());
            }

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
                log.info("解析JSON数组成功，共{}张图片: {}", urls.size(),
                         urls.stream().limit(5).collect(java.util.stream.Collectors.joining(" | ")));
            } else {
                // 单个URL
                urls.add(productImages);
                log.info("解析为单个URL: {}", productImages);
            }
        } catch (Exception e) {
            log.error("解析图片URL失败: {}, 内容: {}", productImages, e);
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
            BufferedImage originalImage = ImageIO.read(url);
            if (originalImage == null) {
                log.warn("无法读取图片: {}", imageUrl);
                return false;
            }

            // 检查图片是否有透明通道（PNG格式），如果有则需要转换
            BufferedImage image = originalImage;
            if (originalImage.getColorModel().hasAlpha()) {
                log.debug("图片包含透明通道，进行转换: {}", imageUrl);
                // 创建RGB模式的图片（去除透明通道，白色背景）
                image = new BufferedImage(
                    originalImage.getWidth(),
                    originalImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB
                );
                // 用白色背景填充
                java.awt.Graphics2D g2d = image.createGraphics();
                g2d.setColor(java.awt.Color.WHITE);
                g2d.fillRect(0, 0, originalImage.getWidth(), originalImage.getHeight());
                // 绘制原图
                g2d.drawImage(originalImage, 0, 0, null);
                g2d.dispose();
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

            // 验证文件是否保存成功
            if (outputFile.exists() && outputFile.length() > 0) {
                log.debug("图片保存成功: {}，大小: {}字节", outputFile.getName(), outputFile.length());
            } else {
                log.error("图片保存失败，文件不存在或为空: {}", outputFile.getAbsolutePath());
                return false;
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
                log.info("开始打包ZIP文件，源目录文件数量：{}", files.length);
                int fileCount = 0;
                for (File file : files) {
                    if (file.isFile()) {
                        log.debug("添加文件到ZIP：{}，大小：{}字节", file.getName(), file.length());
                        addToZip(file, file.getName(), zos);
                        fileCount++;
                    }
                }
                log.info("ZIP打包完成，共添加{}个文件到ZIP：{}", fileCount, zipFile.getAbsolutePath());
            } else {
                log.warn("源目录为空或不存在：{}", sourceDir.getAbsolutePath());
            }
        }

        // 验证ZIP文件
        if (zipFile.exists() && zipFile.length() > 0) {
            log.info("ZIP文件创建成功：{}，大小：{}字节", zipFile.getName(), zipFile.length());
        } else {
            log.error("ZIP文件创建失败：{}，存在：{}，大小：{}",
                     zipFile.getName(), zipFile.exists(), zipFile.length());
        }
    }

    /**
     * 添加文件到ZIP
     */
    private void addToZip(File file, String fileName, ZipOutputStream zos) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            // 检查文件名长度，防止Windows文件名过长问题
            if (fileName.length() > 200) {
                String originalName = fileName;
                fileName = fileName.substring(0, 200);
                log.warn("文件名过长已截断：原长度{}，截断后：{} -> {}",
                        originalName.length(), originalName, fileName);
            }

            ZipEntry zipEntry = new ZipEntry(fileName);
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[8192];
            int length;
            long totalBytes = 0;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
                totalBytes += length;
            }

            zos.closeEntry();
            log.debug("成功添加文件到ZIP：{}，大小：{}字节", fileName, totalBytes);
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
