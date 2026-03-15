package com.meituan.product.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * 阿里云OSS文件存储服务
 * 用于商品图片上传
 */
@Slf4j
@Service
public class AliyunOssService {

    @Value("${aliyun.oss.endpoint:}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret:}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucket-name:}")
    private String bucketName;

    @Value("${aliyun.oss.base-url:}")
    private String baseUrl;

    private OSS ossClient;

    /**
     * 初始化OSS客户端
     */
    @PostConstruct
    public void init() {
        if (endpoint != null && !endpoint.isEmpty() && 
            accessKeyId != null && !accessKeyId.isEmpty()) {
            try {
                ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
                log.info("阿里云OSS客户端初始化成功");
            } catch (Exception e) {
                log.error("阿里云OSS客户端初始化失败", e);
            }
        } else {
            log.warn("阿里云OSS配置未设置，图片上传功能将不可用");
        }
    }

    /**
     * 销毁OSS客户端
     */
    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("阿里云OSS客户端已关闭");
        }
    }

    /**
     * 上传文件到OSS
     *
     * @param file 文件
     * @param folder 文件夹路径（如：products/images）
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (ossClient == null) {
            throw new RuntimeException("OSS客户端未初始化，请检查配置");
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

        // 构建完整的对象名称
        String objectName = folder + "/" + fileName;

        try (InputStream inputStream = file.getInputStream()) {
            // 上传文件到OSS
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
            PutObjectResult result = ossClient.putObject(putObjectRequest);

            // 返回文件访问URL
            String fileUrl = baseUrl + "/" + objectName;
            log.info("文件上传成功: {}", fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除OSS文件
     *
     * @param fileUrl 文件URL
     */
    public void deleteFile(String fileUrl) {
        if (ossClient == null) {
            log.warn("OSS客户端未初始化，无法删除文件");
            return;
        }

        try {
            // 从URL中提取对象名称
            String objectName = extractObjectName(fileUrl);
            if (objectName != null && !objectName.isEmpty()) {
                ossClient.deleteObject(bucketName, objectName);
                log.info("文件删除成功: {}", fileUrl);
            }
        } catch (Exception e) {
            log.error("文件删除失败: {}", fileUrl, e);
        }
    }

    /**
     * 从URL中提取对象名称
     *
     * @param fileUrl 文件URL
     * @return 对象名称
     */
    private String extractObjectName(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }

        // 移除baseUrl前缀
        if (fileUrl.startsWith(baseUrl)) {
            return fileUrl.substring(baseUrl.length() + 1);
        }

        // 如果URL包含bucket名称，提取对象名称
        if (fileUrl.contains(bucketName)) {
            int index = fileUrl.indexOf(bucketName) + bucketName.length() + 1;
            if (index < fileUrl.length()) {
                String remaining = fileUrl.substring(index);
                // 移除查询参数
                int queryIndex = remaining.indexOf("?");
                if (queryIndex > 0) {
                    return remaining.substring(0, queryIndex);
                }
                return remaining;
            }
        }

        return null;
    }

    /**
     * 检查OSS是否已配置
     *
     * @return true-已配置，false-未配置
     */
    public boolean isConfigured() {
        return ossClient != null;
    }
}
