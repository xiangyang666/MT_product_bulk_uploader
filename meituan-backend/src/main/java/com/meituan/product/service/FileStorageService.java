package com.meituan.product.service;

import com.meituan.product.entity.GeneratedFile;
import com.meituan.product.mapper.GeneratedFileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件存储服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {
    
    private final GeneratedFileMapper generatedFileMapper;
    
    @Value("${file.upload.template-path:./uploads/templates/}")
    private String templatePath;
    
    @Value("${file.upload.retention-days:30}")
    private Integer retentionDays;
    
    /**
     * 保存生成的模板文件
     * 
     * @param fileName 文件名
     * @param fileData 文件数据
     * @param merchantId 商家ID
     * @param productCount 商品数量
     * @return 文件记录
     */
    public GeneratedFile saveTemplateFile(String fileName, byte[] fileData, Long merchantId, Integer productCount) {
        try {
            // 确保目录存在
            Path dirPath = Paths.get(templatePath);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            
            // 保存文件
            String filePath = templatePath + fileName;
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(fileData);
            }
            
            // 记录到数据库
            GeneratedFile file = new GeneratedFile();
            file.setMerchantId(merchantId);
            file.setFileName(fileName);
            file.setFilePath(filePath);
            file.setFileSize((long) fileData.length);
            file.setFileType("TEMPLATE");
            file.setProductCount(productCount);
            file.setCreatedAt(LocalDateTime.now());
            file.setExpiresAt(LocalDateTime.now().plusDays(retentionDays));
            file.setDownloadCount(0);
            
            generatedFileMapper.insert(file);
            
            log.info("文件保存成功：{}，大小：{} 字节", fileName, fileData.length);
            
            return file;
        } catch (IOException e) {
            log.error("保存文件失败：{}", fileName, e);
            throw new RuntimeException("保存文件失败", e);
        }
    }
    
    /**
     * 获取最近生成的文件列表
     * 
     * @param merchantId 商家ID
     * @param limit 限制数量
     * @return 文件列表
     */
    public List<com.meituan.product.dto.GeneratedFileDTO> getRecentFiles(Long merchantId, Integer limit) {
        List<GeneratedFile> files = generatedFileMapper.selectRecentByMerchantId(merchantId, limit);
        
        return files.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    /**
     * 获取文件数据
     * 
     * @param fileId 文件ID
     * @return 文件数据
     */
    public byte[] getFileData(Long fileId) {
        GeneratedFile file = generatedFileMapper.selectById(fileId);
        if (file == null) {
            throw new RuntimeException("文件不存在");
        }
        
        // 检查文件是否过期
        if (file.getExpiresAt() != null && file.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("文件已过期");
        }
        
        try {
            Path path = Paths.get(file.getFilePath());
            if (!Files.exists(path)) {
                throw new RuntimeException("文件不存在");
            }
            
            // 增加下载次数
            generatedFileMapper.incrementDownloadCount(fileId);
            
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("读取文件失败：{}", file.getFilePath(), e);
            throw new RuntimeException("读取文件失败", e);
        }
    }
    
    /**
     * 根据ID获取文件信息
     * 
     * @param fileId 文件ID
     * @return 文件信息
     */
    public GeneratedFile getFileById(Long fileId) {
        GeneratedFile file = generatedFileMapper.selectById(fileId);
        if (file == null) {
            throw new RuntimeException("文件不存在");
        }
        return file;
    }
    
    /**
     * 转换为 DTO
     */
    private com.meituan.product.dto.GeneratedFileDTO convertToDTO(GeneratedFile file) {
        com.meituan.product.dto.GeneratedFileDTO dto = new com.meituan.product.dto.GeneratedFileDTO();
        dto.setId(file.getId());
        dto.setFileName(file.getFileName());
        dto.setFileSize(file.getFileSize());
        dto.setFileSizeFormatted(formatFileSize(file.getFileSize()));
        dto.setProductCount(file.getProductCount());
        dto.setCreatedAt(file.getCreatedAt());
        dto.setDownloadCount(file.getDownloadCount());
        dto.setExpired(file.getExpiresAt() != null && file.getExpiresAt().isBefore(LocalDateTime.now()));
        return dto;
    }
    
    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";
        
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = bytes;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", size, units[unitIndex]);
    }
    
    /**
     * 清理过期文件
     */
    public void cleanupExpiredFiles() {
        List<GeneratedFile> expiredFiles = generatedFileMapper.selectExpiredFiles(LocalDateTime.now());
        
        for (GeneratedFile file : expiredFiles) {
            try {
                // 删除物理文件
                Path path = Paths.get(file.getFilePath());
                if (Files.exists(path)) {
                    Files.delete(path);
                }
                
                // 删除数据库记录
                generatedFileMapper.deleteById(file.getId());
                
                log.info("清理过期文件：{}", file.getFileName());
            } catch (Exception e) {
                log.error("清理文件失败：{}", file.getFileName(), e);
            }
        }
    }
}
