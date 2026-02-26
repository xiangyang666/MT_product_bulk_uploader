package com.meituan.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.meituan.product.dto.TemplateStatusDTO;
import com.meituan.product.entity.OperationLog;
import com.meituan.product.entity.Template;
import com.meituan.product.mapper.OperationLogMapper;
import com.meituan.product.mapper.TemplateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模板服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {
    
    private final TemplateMapper templateMapper;
    private final MinioService minioService;
    private final OperationLogMapper operationLogMapper;
    
    /**
     * 获取商家的所有模板
     * 
     * @param merchantId 商家ID
     * @return 模板列表
     */
    public List<Template> getTemplatesByMerchantId(Long merchantId) {
        log.info("查询商家模板列表，商家ID：{}", merchantId);
        
        LambdaQueryWrapper<Template> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Template::getMerchantId, merchantId)
                   .orderByDesc(Template::getCreatedTime);
        
        return templateMapper.selectList(queryWrapper);
    }
    
    /**
     * 获取模板状态
     * 查询商家是否有可用的美团模板
     * 
     * @param merchantId 商家ID
     * @return 模板状态DTO
     */
    public TemplateStatusDTO getTemplateStatus(Long merchantId) {
        log.info("查询商家模板状态，商家ID：{}", merchantId);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 查询最新的美团模板
            Template template = findLatestMeituanTemplate(merchantId);
            
            if (template == null) {
                log.info("商家没有美团模板，商家ID：{}", merchantId);
                return TemplateStatusDTO.builder()
                        .hasTemplate(false)
                        .build();
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("查询模板状态成功，商家ID：{}，模板名称：{}，耗时：{}ms", 
                    merchantId, template.getTemplateName(), duration);
            
            return TemplateStatusDTO.builder()
                    .hasTemplate(true)
                    .templateName(template.getTemplateName())
                    .uploadTime(template.getCreatedTime())
                    .fileSize(template.getFileSize())
                    .templateType(template.getTemplateType())
                    .build();
                    
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("查询模板状态失败，商家ID：{}，耗时：{}ms", merchantId, duration, e);
            throw new RuntimeException("查询模板状态失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 查找最新的美团模板
     * 
     * @param merchantId 商家ID
     * @return 模板对象，如果不存在返回null
     */
    public Template findLatestMeituanTemplate(Long merchantId) {
        LambdaQueryWrapper<Template> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Template::getMerchantId, merchantId)
                   .eq(Template::getTemplateType, "MEITUAN")
                   .orderByDesc(Template::getCreatedTime)
                   .last("LIMIT 1");
        
        return templateMapper.selectOne(queryWrapper);
    }
    
    /**
     * 根据ID获取模板
     * 
     * @param id 模板ID
     * @return 模板
     */
    public Template getTemplateById(Long id) {
        log.info("查询模板详情，模板ID：{}", id);
        return templateMapper.selectById(id);
    }
    
    /**
     * 上传模板
     * 
     * @param file 模板文件
     * @param templateName 模板名称
     * @param templateType 模板类型
     * @param merchantId 商家ID
     * @return 模板信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Template uploadTemplate(MultipartFile file, String templateName, 
                                   String templateType, Long merchantId) {
        log.info("上传模板，商家ID：{}，模板名称：{}，模板类型：{}", 
                merchantId, templateName, templateType);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 验证文件格式
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || 
                (!originalFilename.endsWith(".xlsx") && 
                 !originalFilename.endsWith(".xls") && 
                 !originalFilename.endsWith(".csv"))) {
                throw new IllegalArgumentException("不支持的文件格式，仅支持 .xlsx, .xls, .csv 文件");
            }
            
            // 验证文件大小（最大10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("文件大小超过限制，最大支持10MB");
            }
            
            // 上传到MinIO
            String fileUrl = minioService.uploadFile(file, "templates");
            
            // 从URL中提取文件路径
            String filePath = extractFilePathFromUrl(fileUrl);
            
            // 创建模板记录
            Template template = new Template();
            template.setMerchantId(merchantId);
            template.setTemplateName(templateName);
            template.setTemplateType(templateType);
            template.setFilePath(filePath);
            template.setFileUrl(fileUrl);
            template.setFileSize(file.getSize());
            
            templateMapper.insert(template);
            
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("模板上传成功，模板ID：{}，耗时：{}ms", template.getId(), duration);
            
            return template;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("模板上传失败，耗时：{}ms", duration, e);
            
            throw new RuntimeException("模板上传失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 下载模板
     * 
     * @param id 模板ID
     * @return 文件字节数组
     */
    public byte[] downloadTemplate(Long id) {
        log.info("下载模板，模板ID：{}", id);
        
        try {
            Template template = templateMapper.selectById(id);
            if (template == null) {
                throw new IllegalArgumentException("模板不存在");
            }
            
            // 获取文件路径，优先使用 filePath，如果为空则使用 fileUrl
            String objectName = getObjectName(template);
            log.debug("下载文件，对象名称：{}", objectName);
            
            // 从MinIO下载文件
            InputStream inputStream = minioService.downloadFile(objectName);
            
            // 转换为字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            inputStream.close();
            
            log.info("模板下载成功，模板ID：{}", id);
            
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            log.error("模板下载失败，模板ID：{}", id, e);
            throw new RuntimeException("模板下载失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 删除模板
     * 
     * @param id 模板ID
     * @param merchantId 商家ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplate(Long id, Long merchantId) {
        log.info("删除模板，模板ID：{}，商家ID：{}", id, merchantId);
        
        long startTime = System.currentTimeMillis();
        
        try {
            Template template = templateMapper.selectById(id);
            if (template == null) {
                throw new IllegalArgumentException("模板不存在");
            }
            
            // 验证商家权限
            if (!template.getMerchantId().equals(merchantId)) {
                throw new IllegalArgumentException("无权删除该模板");
            }
            
            // 获取文件路径
            String objectName = getObjectName(template);
            log.debug("删除文件，对象名称：{}", objectName);
            
            // 从MinIO删除文件
            minioService.deleteFile(objectName);
            
            // 逻辑删除数据库记录
            templateMapper.deleteById(id);
            
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("模板删除成功，模板ID：{}，耗时：{}ms", id, duration);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("模板删除失败，模板ID：{}，耗时：{}ms", id, duration, e);
            
            throw new RuntimeException("模板删除失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 预览模板结构
     * 
     * @param id 模板ID
     * @return 模板预览信息
     */
    public Map<String, Object> previewTemplate(Long id) {
        log.info("预览模板，模板ID：{}", id);
        
        try {
            Template template = templateMapper.selectById(id);
            if (template == null) {
                throw new IllegalArgumentException("模板不存在");
            }
            
            // 获取文件路径，优先使用 filePath，如果为空则使用 fileUrl
            String objectName = getObjectName(template);
            log.debug("预览文件，对象名称：{}", objectName);
            
            // 从MinIO下载文件
            InputStream inputStream = minioService.downloadFile(objectName);
            
            // 解析Excel文件
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            
            // 获取表头
            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            if (headerRow != null) {
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    headers.add(headerRow.getCell(i).getStringCellValue());
                }
            }
            
            // 获取数据行数
            int dataRows = sheet.getLastRowNum();
            
            workbook.close();
            inputStream.close();
            
            // 构建预览信息
            Map<String, Object> preview = new HashMap<>();
            preview.put("templateId", template.getId());
            preview.put("templateName", template.getTemplateName());
            preview.put("templateType", template.getTemplateType());
            preview.put("headers", headers);
            preview.put("columnCount", headers.size());
            preview.put("dataRowCount", dataRows);
            
            log.info("模板预览成功，模板ID：{}，列数：{}，数据行数：{}", 
                    id, headers.size(), dataRows);
            
            return preview;
            
        } catch (Exception e) {
            log.error("模板预览失败，模板ID：{}", id, e);
            throw new RuntimeException("模板预览失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 获取MinIO对象名称
     * 优先使用 filePath，如果为空或无效则使用 fileUrl
     * 
     * @param template 模板对象
     * @return MinIO对象名称
     */
    private String getObjectName(Template template) {
        String filePath = template.getFilePath();
        String fileUrl = template.getFileUrl();
        
        log.debug("模板ID：{}，filePath：{}，fileUrl：{}", 
                template.getId(), filePath, fileUrl);
        
        // 如果 filePath 不为空且不是完整URL，直接使用
        if (filePath != null && !filePath.isEmpty() && !filePath.startsWith("http")) {
            return filePath;
        }
        
        // 如果 filePath 为空或是完整URL，尝试从 fileUrl 提取
        if (fileUrl != null && !fileUrl.isEmpty()) {
            return extractFilePathFromUrl(fileUrl);
        }
        
        // 如果都为空，抛出异常
        throw new IllegalStateException("模板文件路径为空，模板ID：" + template.getId());
    }
    
    /**
     * 从URL中提取文件路径
     * 
     * @param url MinIO预签名URL
     * @return 文件路径（不包含bucket名称和URL参数）
     */
    private String extractFilePathFromUrl(String url) {
        try {
            // URL格式: http://localhost:9000/bucket-name/path/to/file?params
            // 需要提取: path/to/file
            
            // 1. 移除URL参数（?后面的部分）
            String urlWithoutParams = url.split("\\?")[0];
            log.debug("移除参数后的URL：{}", urlWithoutParams);
            
            // 2. 分割URL路径
            String[] parts = urlWithoutParams.split("/");
            StringBuilder path = new StringBuilder();
            boolean foundBucket = false;
            
            // 3. 查找bucket名称后的路径部分
            for (String part : parts) {
                if (foundBucket && !part.isEmpty()) {
                    if (path.length() > 0) {
                        path.append("/");
                    }
                    path.append(part);
                }
                // 找到bucket名称（meituan-files）后，开始收集路径
                if (part.equals("meituan-files")) {
                    foundBucket = true;
                }
            }
            
            String extractedPath = path.toString();
            
            // 4. 如果提取失败，尝试其他方法
            if (extractedPath.isEmpty()) {
                log.warn("方法1提取失败，尝试方法2：直接从最后几个路径段提取");
                // 尝试从URL末尾提取路径（假设格式为 .../folder/filename）
                if (parts.length >= 2) {
                    // 取最后两个部分（folder/filename）
                    extractedPath = parts[parts.length - 2] + "/" + parts[parts.length - 1];
                    log.debug("方法2提取结果：{}", extractedPath);
                }
            }
            
            // 5. 如果还是为空，抛出异常
            if (extractedPath.isEmpty()) {
                throw new IllegalStateException("无法从URL提取有效的文件路径：" + url);
            }
            
            log.debug("从URL提取文件路径成功：{} -> {}", url, extractedPath);
            return extractedPath;
            
        } catch (Exception e) {
            log.error("从URL提取文件路径失败：{}", url, e);
            throw new RuntimeException("无法提取文件路径：" + e.getMessage(), e);
        }
    }
    
}
