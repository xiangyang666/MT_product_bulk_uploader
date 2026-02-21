package com.meituan.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meituan.product.dto.AppVersionDTO;
import com.meituan.product.dto.AppVersionUploadRequest;
import com.meituan.product.entity.AppVersion;
import com.meituan.product.entity.User;
import com.meituan.product.mapper.AppVersionMapper;
import com.meituan.product.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 应用版本服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppVersionService {
    
    private final AppVersionMapper appVersionMapper;
    private final UserMapper userMapper;
    private final MinioService minioService;
    
    private static final long MAX_FILE_SIZE = 500 * 1024 * 1024; // 500MB
    
    /**
     * 上传新版本
     */
    @Transactional(rollbackFor = Exception.class)
    public AppVersionDTO uploadVersion(MultipartFile file, AppVersionUploadRequest request, Long userId) {
        // 验证文件
        validateFile(file);
        
        // 检测平台
        String platform = detectPlatform(file.getOriginalFilename());
        
        // 检查版本是否已存在
        LambdaQueryWrapper<AppVersion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppVersion::getVersion, request.getVersion())
                   .eq(AppVersion::getPlatform, platform);
        if (appVersionMapper.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("该版本号已存在");
        }
        
        // 构建 MinIO 存储路径
        String objectName = String.format("app-versions/%s/%s/%s", 
                platform, request.getVersion(), file.getOriginalFilename());
        
        // 上传文件到 MinIO
        try {
            minioService.uploadStream(file.getInputStream(), objectName, file.getContentType());
        } catch (Exception e) {
            log.error("文件上传到 MinIO 失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
        
        // 创建版本记录
        AppVersion appVersion = new AppVersion();
        appVersion.setVersion(request.getVersion());
        appVersion.setPlatform(platform);
        appVersion.setFileName(file.getOriginalFilename());
        appVersion.setFileSize(file.getSize());
        appVersion.setFilePath(objectName);
        appVersion.setReleaseNotes(request.getReleaseNotes());
        appVersion.setUploadedBy(userId);
        appVersion.setDownloadCount(0);
        
        // 检查是否是该平台的第一个版本，如果是则自动设为最新
        LambdaQueryWrapper<AppVersion> platformQuery = new LambdaQueryWrapper<>();
        platformQuery.eq(AppVersion::getPlatform, platform);
        long count = appVersionMapper.selectCount(platformQuery);
        appVersion.setIsLatest(count == 0 ? 1 : 0);
        
        appVersionMapper.insert(appVersion);
        
        log.info("版本上传成功: {} - {}", platform, request.getVersion());
        
        return convertToDTO(appVersion);
    }
    
    /**
     * 获取版本列表
     */
    public Page<AppVersionDTO> listVersions(String platform, Integer page, Integer size) {
        Page<AppVersion> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AppVersion> queryWrapper = new LambdaQueryWrapper<>();
        
        if (platform != null && !platform.isEmpty()) {
            queryWrapper.eq(AppVersion::getPlatform, platform);
        }
        
        queryWrapper.orderByDesc(AppVersion::getCreatedAt);
        
        Page<AppVersion> result = appVersionMapper.selectPage(pageParam, queryWrapper);
        
        Page<AppVersionDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<AppVersionDTO> dtoList = result.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }
    
    /**
     * 获取最新版本
     */
    public AppVersionDTO getLatestVersion(String platform) {
        LambdaQueryWrapper<AppVersion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppVersion::getPlatform, platform)
                   .eq(AppVersion::getIsLatest, 1);
        
        AppVersion appVersion = appVersionMapper.selectOne(queryWrapper);
        if (appVersion == null) {
            throw new RuntimeException("未找到该平台的最新版本");
        }
        
        return convertToDTO(appVersion);
    }
    
    /**
     * 设置为最新版本
     */
    @Transactional(rollbackFor = Exception.class)
    public void setLatest(Long id) {
        AppVersion appVersion = appVersionMapper.selectById(id);
        if (appVersion == null) {
            throw new RuntimeException("版本不存在");
        }
        
        // 取消该平台的其他最新版本标记
        LambdaUpdateWrapper<AppVersion> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AppVersion::getPlatform, appVersion.getPlatform())
                    .set(AppVersion::getIsLatest, 0);
        appVersionMapper.update(null, updateWrapper);
        
        // 设置当前版本为最新
        appVersion.setIsLatest(1);
        appVersionMapper.updateById(appVersion);
        
        log.info("设置最新版本: {} - {}", appVersion.getPlatform(), appVersion.getVersion());
    }
    
    /**
     * 删除版本
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteVersion(Long id) {
        AppVersion appVersion = appVersionMapper.selectById(id);
        if (appVersion == null) {
            throw new RuntimeException("版本不存在");
        }
        
        boolean wasLatest = appVersion.getIsLatest() == 1;
        String platform = appVersion.getPlatform();
        
        // 删除 MinIO 文件
        try {
            minioService.deleteFile(appVersion.getFilePath());
        } catch (Exception e) {
            log.error("删除 MinIO 文件失败，继续删除数据库记录", e);
        }
        
        // 删除数据库记录
        appVersionMapper.deleteById(id);
        
        // 如果删除的是最新版本，自动提升下一个版本
        if (wasLatest) {
            promoteNextVersion(platform);
        }
        
        log.info("版本删除成功: {} - {}", appVersion.getPlatform(), appVersion.getVersion());
    }
    
    /**
     * 生成下载 URL
     */
    public String generateDownloadUrl(Long id) {
        AppVersion appVersion = appVersionMapper.selectById(id);
        if (appVersion == null) {
            throw new RuntimeException("版本不存在");
        }
        
        // 生成预签名 URL（1小时有效期）
        String url = minioService.getFileUrl(appVersion.getFilePath());
        
        // 增加下载计数
        incrementDownloadCount(id);
        
        return url;
    }
    
    /**
     * 增加下载计数
     */
    public void incrementDownloadCount(Long id) {
        AppVersion appVersion = appVersionMapper.selectById(id);
        if (appVersion != null) {
            appVersion.setDownloadCount(appVersion.getDownloadCount() + 1);
            appVersionMapper.updateById(appVersion);
        }
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new RuntimeException("文件名不能为空");
        }
        
        // 检查文件类型
        if (!filename.endsWith(".exe") && !filename.endsWith(".dmg")) {
            throw new RuntimeException("仅支持 .exe 和 .dmg 文件");
        }
        
        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("文件大小超过限制（最大 500MB）");
        }
    }
    
    /**
     * 检测平台
     */
    private String detectPlatform(String filename) {
        if (filename == null) {
            throw new RuntimeException("无法检测平台：文件名为空");
        }
        
        if (filename.endsWith(".exe")) {
            return "Windows";
        } else if (filename.endsWith(".dmg")) {
            return "macOS";
        } else {
            throw new RuntimeException("不支持的文件类型");
        }
    }
    
    /**
     * 提升下一个版本为最新
     */
    private void promoteNextVersion(String platform) {
        LambdaQueryWrapper<AppVersion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppVersion::getPlatform, platform)
                   .orderByDesc(AppVersion::getCreatedAt)
                   .last("LIMIT 1");
        
        AppVersion nextVersion = appVersionMapper.selectOne(queryWrapper);
        if (nextVersion != null) {
            nextVersion.setIsLatest(1);
            appVersionMapper.updateById(nextVersion);
            log.info("自动提升最新版本: {} - {}", platform, nextVersion.getVersion());
        }
    }
    
    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) {
            return "0 B";
        }
        
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = bytes.doubleValue();
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(size) + " " + units[unitIndex];
    }
    
    /**
     * 转换为 DTO
     */
    private AppVersionDTO convertToDTO(AppVersion appVersion) {
        AppVersionDTO dto = new AppVersionDTO();
        dto.setId(appVersion.getId());
        dto.setVersion(appVersion.getVersion());
        dto.setPlatform(appVersion.getPlatform());
        dto.setFileName(appVersion.getFileName());
        dto.setFileSize(appVersion.getFileSize());
        dto.setFileSizeFormatted(formatFileSize(appVersion.getFileSize()));
        dto.setIsLatest(appVersion.getIsLatest() == 1);
        dto.setDownloadCount(appVersion.getDownloadCount());
        dto.setReleaseNotes(appVersion.getReleaseNotes());
        dto.setCreatedAt(appVersion.getCreatedAt());
        
        // 获取上传者用户名
        if (appVersion.getUploadedBy() != null) {
            User user = userMapper.selectById(appVersion.getUploadedBy());
            if (user != null) {
                dto.setUploadedBy(user.getUsername());
            }
        }
        
        return dto;
    }
}
