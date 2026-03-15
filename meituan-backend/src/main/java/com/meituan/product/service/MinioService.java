package com.meituan.product.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO文件存储服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {
    
    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.port}")
    private Integer port;

    /**
     * 初始化存储桶
     */
    public void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("创建MinIO存储桶成功: {}", bucketName);

                // 设置存储桶为公共读取
                setBucketPublicReadPolicy();
            } else {
                log.info("MinIO存储桶已存在: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("初始化MinIO存储桶失败", e);
            throw new RuntimeException("初始化MinIO存储桶失败: " + e.getMessage());
        }
    }

    /**
     * 设置存储桶为公共读取策略
     */
    private void setBucketPublicReadPolicy() {
        try {
            String policy = String.format("""
                {
                    "Version": "2012-10-17",
                    "Statement": [
                        {
                            "Effect": "Allow",
                            "Principal": {"AWS": "*"},
                            "Action": ["s3:GetObject"],
                            "Resource": ["arn:aws:s3:::%s/*"]
                        }
                    ]
                }
                """, bucketName);

            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(policy)
                            .build()
            );
            log.info("设置存储桶公共读取策略成功: {}", bucketName);
        } catch (Exception e) {
            log.warn("设置存储桶公共读取策略失败（可能已设置）: {}", e.getMessage());
        }
    }
    
    /**
     * 上传文件
     * 
     * @param file 文件
     * @param folder 文件夹路径（可选）
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : "";
            String fileName = UUID.randomUUID().toString() + extension;
            
            // 构建对象名称
            String objectName = folder != null && !folder.isEmpty() 
                    ? folder + "/" + fileName 
                    : fileName;
            
            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            
            log.info("文件上传成功: {}", objectName);
            
            // 返回文件访问URL
            return getFileUrl(objectName);
            
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传输入流
     * 
     * @param inputStream 输入流
     * @param objectName 对象名称
     * @param contentType 内容类型
     * @return 文件访问URL
     */
    public String uploadStream(InputStream inputStream, String objectName, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, -1, 10485760) // 10MB part size
                            .contentType(contentType)
                            .build()
            );
            
            log.info("流上传成功: {}", objectName);
            return getFileUrl(objectName);
            
        } catch (Exception e) {
            log.error("流上传失败", e);
            throw new RuntimeException("流上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取文件访问URL
     *
     * @param objectName 对象名称
     * @return 文件访问URL
     */
    public String getFileUrl(String objectName) {
        try {
            // 使用公共访问URL（永久有效，不需要签名）
            // 移除 endpoint 中的 http:// 或 https:// 前缀
            String cleanEndpoint = endpoint.replaceFirst("^https?://", "");
            // 组合完整的访问地址：http://host:port/bucket/objectName
            return "http://" + cleanEndpoint + ":" + port + "/" + bucketName + "/" + objectName;
        } catch (Exception e) {
            log.error("获取文件URL失败", e);
            throw new RuntimeException("获取文件URL失败: " + e.getMessage());
        }
    }

    /**
     * 获取预签名URL（用于私有文件）
     *
     * @param objectName 对象名称
     * @param expiryDays 有效期（天）
     * @return 文件访问URL
     */
    public String getPresignedUrl(String objectName, int expiryDays) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiryDays, TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取预签名URL失败", e);
            throw new RuntimeException("获取预签名URL失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除文件
     * 
     * @param objectName 对象名称
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.info("文件删除成功: {}", objectName);
        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 下载文件
     * 
     * @param objectName 对象名称
     * @return 文件输入流
     */
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("文件下载失败", e);
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查文件是否存在
     * 
     * @param objectName 对象名称
     * @return 是否存在
     */
    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
