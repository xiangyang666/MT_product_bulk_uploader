package com.meituan.product.config;

import com.meituan.product.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * MinIO初始化器
 * 应用启动时自动创建存储桶
 * 
 * 注意：如果MinIO服务未启动，此组件会被禁用以避免启动失败
 */
@Slf4j
// @Component  // 临时禁用MinIO初始化，避免启动失败
@RequiredArgsConstructor
public class MinioInitializer implements CommandLineRunner {
    
    private final MinioService minioService;
    
    @Override
    public void run(String... args) {
        try {
            log.info("开始初始化MinIO存储桶...");
            minioService.initBucket();
            log.info("MinIO存储桶初始化完成");
        } catch (Exception e) {
            log.error("MinIO存储桶初始化失败", e);
        }
    }
}
