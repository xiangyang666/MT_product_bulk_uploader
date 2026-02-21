package com.meituan.product.controller;

import com.meituan.product.common.ApiResponse;
import com.meituan.product.entity.Template;
import com.meituan.product.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 模板控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {
    
    private final TemplateService templateService;
    
    /**
     * 获取模板列表
     * 
     * @param merchantId 商家ID
     * @return 模板列表
     */
    @GetMapping
    public ApiResponse<List<Template>> listTemplates(
            @RequestParam(value = "merchantId", defaultValue = "1") Long merchantId) {
        log.info("查询模板列表，商家ID：{}", merchantId);
        
        try {
            List<Template> templates = templateService.getTemplatesByMerchantId(merchantId);
            return ApiResponse.success(templates);
        } catch (Exception e) {
            log.error("查询模板列表失败", e);
            return ApiResponse.error(500, "查询模板列表失败：" + e.getMessage());
        }
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
    @PostMapping("/upload")
    public ApiResponse<Template> uploadTemplate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("templateName") String templateName,
            @RequestParam("templateType") String templateType,
            @RequestParam(value = "merchantId", defaultValue = "1") Long merchantId) {
        
        log.info("接收到上传模板请求，商家ID：{}，模板名称：{}，模板类型：{}，文件名：{}", 
                merchantId, templateName, templateType, file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return ApiResponse.error(400, "文件不能为空");
        }
        
        if (templateName == null || templateName.trim().isEmpty()) {
            return ApiResponse.error(400, "模板名称不能为空");
        }
        
        if (templateType == null || templateType.trim().isEmpty()) {
            return ApiResponse.error(400, "模板类型不能为空");
        }
        
        try {
            Template template = templateService.uploadTemplate(
                    file, templateName, templateType, merchantId);
            
            return ApiResponse.success("模板上传成功", template);
            
        } catch (IllegalArgumentException e) {
            log.error("模板上传参数错误", e);
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("模板上传失败", e);
            return ApiResponse.error(500, "模板上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 下载模板
     * 
     * @param id 模板ID
     * @return 模板文件
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadTemplate(@PathVariable Long id) {
        log.info("接收到下载模板请求，模板ID：{}", id);
        
        try {
            // 获取模板信息
            Template template = templateService.getTemplateById(id);
            if (template == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 下载文件
            byte[] fileData = templateService.downloadTemplate(id);
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            
            // 设置文件名
            String fileName = template.getTemplateName();
            if (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls")) {
                fileName += ".xlsx";
            }
            
            headers.setContentDispositionFormData("attachment", 
                    new String(fileName.getBytes("UTF-8"), "ISO-8859-1"));
            headers.setContentLength(fileData.length);
            
            log.info("模板下载成功，模板ID：{}，文件名：{}，大小：{} 字节", 
                    id, fileName, fileData.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileData);
                    
        } catch (Exception e) {
            log.error("模板下载失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 删除模板
     * 
     * @param id 模板ID
     * @param merchantId 商家ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteTemplate(
            @PathVariable Long id,
            @RequestParam(value = "merchantId", defaultValue = "1") Long merchantId) {
        
        log.info("接收到删除模板请求，模板ID：{}，商家ID：{}", id, merchantId);
        
        try {
            templateService.deleteTemplate(id, merchantId);
            return ApiResponse.success("模板删除成功");
            
        } catch (IllegalArgumentException e) {
            log.error("模板删除参数错误", e);
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("模板删除失败", e);
            return ApiResponse.error(500, "模板删除失败：" + e.getMessage());
        }
    }
    
    /**
     * 预览模板
     * 
     * @param id 模板ID
     * @return 模板预览信息
     */
    @GetMapping("/{id}/preview")
    public ApiResponse<Map<String, Object>> previewTemplate(@PathVariable Long id) {
        log.info("接收到预览模板请求，模板ID：{}", id);
        
        try {
            Map<String, Object> preview = templateService.previewTemplate(id);
            return ApiResponse.success(preview);
            
        } catch (IllegalArgumentException e) {
            log.error("模板预览参数错误", e);
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("模板预览失败", e);
            return ApiResponse.error(500, "模板预览失败：" + e.getMessage());
        }
    }
}
