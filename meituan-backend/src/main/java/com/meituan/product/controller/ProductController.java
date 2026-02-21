package com.meituan.product.controller;

import com.meituan.product.common.ApiResponse;
import com.meituan.product.dto.ClearRequest;
import com.meituan.product.dto.ClearResult;
import com.meituan.product.dto.GenerateTemplateRequest;
import com.meituan.product.dto.GenerateTemplateResponse;
import com.meituan.product.dto.ImportResult;
import com.meituan.product.dto.ProductStats;
import com.meituan.product.dto.UploadRequest;
import com.meituan.product.dto.UploadResult;
import com.meituan.product.entity.OperationLog;
import com.meituan.product.entity.Product;
import com.meituan.product.service.OperationLogService;
import com.meituan.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 商品控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    private final OperationLogService operationLogService;
    private final com.meituan.product.service.FileStorageService fileStorageService;
    
    /**
     * 获取商品统计数据
     * 
     * @param merchantId 商家ID（可选，如果不传则使用默认值1）
     * @return 统计数据
     */
    @GetMapping("/stats")
    public ApiResponse<ProductStats> getProductStats(
            @RequestParam(value = "merchantId", required = false) Long merchantId) {
        log.info("查询商品统计数据，商家ID：{}", merchantId);
        
        try {
            // 如果没有传 merchantId，使用默认值 1
            if (merchantId == null) {
                merchantId = 1L;
            }
            
            ProductStats stats = productService.getProductStats(merchantId);
            
            return ApiResponse.success(stats);
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return ApiResponse.error(500, "获取统计数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取最近导入的商品预览
     * 
     * @param merchantId 商家ID（可选，默认为1）
     * @param limit 限制数量（可选，默认为10）
     * @return 最近商品列表
     */
    @GetMapping("/recent")
    public ApiResponse<List<Product>> getRecentProducts(
            @RequestParam(value = "merchantId", required = false) Long merchantId,
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
        log.info("查询最近导入的商品，商家ID：{}，限制数量：{}", merchantId, limit);
        
        try {
            // 如果没有传 merchantId，使用默认值 1
            if (merchantId == null) {
                merchantId = 1L;
            }
            
            List<Product> products = productService.getRecentProducts(merchantId, limit);
            
            return ApiResponse.success(products);
        } catch (Exception e) {
            log.error("获取最近商品失败", e);
            return ApiResponse.error(500, "获取最近商品失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取最近操作历史
     * 
     * @param merchantId 商家ID（可选，默认为1）
     * @param type 操作类型（可选，如GENERATE_ALL）
     * @param limit 限制数量（可选，默认为3）
     * @return 操作历史列表
     */
    @GetMapping("/operation-logs/recent")
    public ApiResponse<List<com.meituan.product.dto.OperationLogDTO>> getRecentOperations(
            @RequestParam(value = "merchantId", required = false) Long merchantId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "limit", required = false, defaultValue = "3") Integer limit) {
        log.info("查询最近操作历史，商家ID：{}，操作类型：{}，限制数量：{}", merchantId, type, limit);
        
        try {
            // 如果没有传 merchantId，使用默认值 1
            if (merchantId == null) {
                merchantId = 1L;
            }
            
            List<OperationLog> logs = operationLogService.getRecentOperations(merchantId, type, limit);
            
            // 转换为 DTO
            List<com.meituan.product.dto.OperationLogDTO> dtoList = logs.stream().map(log -> {
                com.meituan.product.dto.OperationLogDTO dto = new com.meituan.product.dto.OperationLogDTO();
                dto.setId(log.getId());
                dto.setOperationType(log.getOperationType());
                dto.setOperationDesc(log.getOperationDesc());
                dto.setOperationTime(log.getCreatedAt());
                
                // 处理 result 可能为 null 的情况
                Integer result = log.getResult();
                if (result != null && result == 1) {
                    dto.setStatus("SUCCESS");
                } else if (result != null && result == 0) {
                    dto.setStatus("FAILED");
                } else {
                    // 如果 result 为 null，默认为成功
                    dto.setStatus("SUCCESS");
                }
                
                dto.setErrorMsg(log.getErrorMsg());
                dto.setDuration(log.getDuration());
                
                // 从操作描述中提取商品数量
                String desc = log.getOperationDesc();
                if (desc != null && desc.contains("共") && desc.contains("个商品")) {
                    try {
                        String countStr = desc.substring(desc.indexOf("共") + 1, desc.indexOf("个商品"));
                        dto.setProductCount(Integer.parseInt(countStr));
                    } catch (Exception e) {
                        dto.setProductCount(0);
                    }
                } else {
                    dto.setProductCount(0);
                }
                
                return dto;
            }).collect(java.util.stream.Collectors.toList());
            
            return ApiResponse.success(dtoList);
        } catch (Exception e) {
            log.error("获取操作历史失败", e);
            return ApiResponse.error(500, "获取操作历史失败：" + e.getMessage());
        }
    }
    
    /**
     * 下载导入模板
     * 
     * @return Excel模板文件
     */
    @GetMapping("/download-template")
    public ResponseEntity<byte[]> downloadTemplate() {
        log.info("接收到下载模板请求");
        
        try {
            // 生成空模板
            byte[] templateData = productService.generateImportTemplate();
            
            // 生成文件名
            String fileName = "商品导入模板.xlsx";
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                new String(fileName.getBytes("UTF-8"), "ISO-8859-1"));
            headers.setContentLength(templateData.length);
            
            log.info("成功生成模板文件：{}，大小：{} 字节", fileName, templateData.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(templateData);
                    
        } catch (Exception e) {
            log.error("生成模板失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 预览Excel数据
     * 
     * @param file Excel文件
     * @param merchantId 商家ID
     * @return 预览结果（包含格式识别信息和前20行数据）
     */
    @PostMapping("/preview")
    public ApiResponse<java.util.Map<String, Object>> previewProducts(
            @RequestParam("file") MultipartFile file,
            @RequestParam("merchantId") Long merchantId) {
        
        log.info("接收到预览请求，商家ID：{}，文件名：{}", merchantId, file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return ApiResponse.error(400, "文件不能为空");
        }
        
        if (merchantId == null) {
            return ApiResponse.error(400, "商家ID不能为空");
        }
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 预览Excel文件（只解析，不导入数据库）
            ImportResult result = productService.previewExcel(file, merchantId);
            
            long duration = System.currentTimeMillis() - startTime;
            
            // 限制预览数据为前20行
            List<Product> previewProducts = result.getProducts();
            if (previewProducts.size() > 20) {
                previewProducts = previewProducts.subList(0, 20);
            }
            
            // 构建预览响应
            java.util.Map<String, Object> previewResult = new java.util.HashMap<>();
            previewResult.put("formatType", result.getFormatType());
            previewResult.put("products", previewProducts);
            previewResult.put("totalCount", result.getTotalCount());
            previewResult.put("successCount", result.getSuccessCount());
            previewResult.put("failedCount", result.getFailedCount());
            previewResult.put("previewCount", previewProducts.size());
            previewResult.put("hasMoreData", result.getProducts().size() > 20);
            previewResult.put("duration", duration);
            
            // 添加格式识别提示信息
            String formatMessage = "";
            if (result.getFormatType() == com.meituan.product.enums.FormatType.MEITUAN) {
                formatMessage = "已识别为美团格式，系统将自动转换数据";
            } else if (result.getFormatType() == com.meituan.product.enums.FormatType.STANDARD) {
                formatMessage = "已识别为标准格式";
            } else {
                formatMessage = "未识别格式类型";
            }
            previewResult.put("formatMessage", formatMessage);
            
            // 添加错误详情（限制前20条）
            if (result.getErrorDetails() != null && !result.getErrorDetails().isEmpty()) {
                List<com.meituan.product.dto.ErrorDetail> previewErrors = result.getErrorDetails();
                if (previewErrors.size() > 20) {
                    previewErrors = previewErrors.subList(0, 20);
                }
                previewResult.put("errors", previewErrors);
                previewResult.put("hasMoreErrors", result.getHasMoreErrors());
                previewResult.put("remainingErrorCount", result.getRemainingErrorCount());
            }
            
            log.info("成功预览Excel文件，格式：{}，总计：{}行，成功：{}行，失败：{}行，耗时：{}ms", 
                result.getFormatType(), result.getTotalCount(), result.getSuccessCount(), 
                result.getFailedCount(), duration);
            
            String message = String.format("预览成功 - %s（预计成功%d行，%d行存在错误）", 
                formatMessage, result.getSuccessCount(), result.getFailedCount());
            
            return ApiResponse.success(message, previewResult);
            
        } catch (com.meituan.product.exception.DataValidationException e) {
            log.error("Excel数据验证失败", e);
            // 返回详细的验证错误信息
            StringBuilder errorMsg = new StringBuilder("数据验证失败：\n");
            for (com.meituan.product.exception.DataValidationException.ValidationError error : e.getErrors()) {
                errorMsg.append(error.toString()).append("\n");
            }
            return ApiResponse.error(400, errorMsg.toString());
        } catch (com.meituan.product.exception.FileFormatException e) {
            log.error("文件格式错误", e);
            return ApiResponse.error(400, "文件格式错误：" + e.getMessage());
        } catch (Exception e) {
            log.error("预览Excel文件失败", e);
            return ApiResponse.error(500, "预览失败：" + e.getMessage());
        }
    }
    
    /**
     * 导入商品数据
     * 
     * @param file Excel文件
     * @param merchantId 商家ID
     * @return 导入结果（包含格式识别信息和详细错误报告）
     */
    @PostMapping("/import")
    public ApiResponse<ImportResult> importProducts(
            @RequestParam("file") MultipartFile file,
            @RequestParam("merchantId") Long merchantId) {
        
        log.info("接收到导入请求，商家ID：{}，文件名：{}", merchantId, file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return ApiResponse.error(400, "文件不能为空");
        }
        
        if (merchantId == null) {
            return ApiResponse.error(400, "商家ID不能为空");
        }
        
        long startTime = System.currentTimeMillis();
        
        ImportResult result = productService.importFromExcel(file, merchantId);
        
        long duration = System.currentTimeMillis() - startTime;
        result.setDuration(duration);
        
        // 构建响应消息
        String formatInfo = "";
        if (result.getFormatType() == com.meituan.product.enums.FormatType.MEITUAN) {
            formatInfo = "（美团格式）";
        } else if (result.getFormatType() == com.meituan.product.enums.FormatType.STANDARD) {
            formatInfo = "（标准格式）";
        }
        
        String message;
        if (result.getFailedCount() > 0) {
            message = String.format("导入完成%s - 成功%d条，失败%d条，耗时%dms", 
                formatInfo, result.getSuccessCount(), result.getFailedCount(), duration);
        } else {
            message = String.format("成功导入%d条商品数据%s，耗时%dms", 
                result.getSuccessCount(), formatInfo, duration);
        }
        
        log.info("导入完成 - 格式：{}，总计：{}行，成功：{}行，失败：{}行，耗时：{}ms", 
            result.getFormatType(), result.getTotalCount(), result.getSuccessCount(), 
            result.getFailedCount(), duration);
        
        return ApiResponse.success(message, result);
    }
    
    /**
     * 获取商品列表（支持分页、搜索和日期筛选）
     * 
     * @param page 页码（可选，默认1）
     * @param size 每页大小（可选，默认20）
     * @param keyword 搜索关键词（可选）
     * @param startDate 开始日期（可选，格式：YYYY-MM-DD）
     * @param endDate 结束日期（可选，格式：YYYY-MM-DD）
     * @return 商品列表
     */
    @GetMapping
    public ApiResponse<java.util.Map<String, Object>> getProducts(
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        log.info("查询商品列表，页码：{}，每页大小：{}，关键词：{}，开始日期：{}，结束日期：{}", 
                page, size, keyword, startDate, endDate);
        
        try {
            // 使用默认商家ID = 1
            Long merchantId = 1L;
            
            // 获取所有商品
            List<Product> allProducts = productService.getProductsByMerchantId(merchantId);
            
            // 如果有日期范围，进行过滤
            if (startDate != null && endDate != null) {
                try {
                    LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
                    LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
                    
                    allProducts = allProducts.stream()
                        .filter(p -> {
                            if (p.getCreatedTime() == null) return false;
                            return !p.getCreatedTime().isBefore(startDateTime) && 
                                   !p.getCreatedTime().isAfter(endDateTime);
                        })
                        .collect(java.util.stream.Collectors.toList());
                    
                    log.info("日期筛选后商品数量：{}", allProducts.size());
                } catch (Exception e) {
                    log.error("日期解析失败", e);
                }
            }
            
            // 如果有搜索关键词，进行过滤
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchKeyword = keyword.trim().toLowerCase();
                allProducts = allProducts.stream()
                    .filter(p -> 
                        (p.getProductName() != null && p.getProductName().toLowerCase().contains(searchKeyword)) ||
                        (p.getCategoryId() != null && p.getCategoryId().toLowerCase().contains(searchKeyword))
                    )
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // 计算总数
            int total = allProducts.size();
            
            // 手动分页
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, total);
            
            List<Product> pagedProducts;
            if (startIndex >= total) {
                pagedProducts = new java.util.ArrayList<>();
            } else {
                pagedProducts = allProducts.subList(startIndex, endIndex);
            }
            
            // 构建响应
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("list", pagedProducts);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("查询商品列表失败", e);
            return ApiResponse.error(500, "查询商品列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取商品详情
     * 
     * @param id 商品ID
     * @return 商品详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Product> getProduct(@PathVariable Long id) {
        log.info("查询商品详情，商品ID：{}", id);
        
        Product product = productService.getProductById(id);
        if (product == null) {
            return ApiResponse.error(404, "商品不存在");
        }
        
        return ApiResponse.success(product);
    }
    
    /**
     * 生成美团上传模板
     * 
     * @param request 生成模板请求
     * @return Excel文件
     */
    @PostMapping("/generate-template")
    public ResponseEntity<byte[]> generateTemplate(@RequestBody GenerateTemplateRequest request) {
        log.info("接收到生成模板请求，商品数量：{}", request.getProductIds().size());
        
        if (request.getProductIds() == null || request.getProductIds().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            // 生成模板
            byte[] excelData = productService.generateMeituanTemplate(request.getProductIds());
            
            // 生成文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String fileName = "meituan_template_" + timestamp + ".xlsx";
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(excelData.length);
            
            log.info("成功生成模板文件：{}，大小：{} 字节", fileName, excelData.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
                    
        } catch (Exception e) {
            log.error("生成模板失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 生成全部商品的美团上传模板
     * 
     * @param merchantId 商家ID（可选，默认为1）
     * @return Excel文件
     */
    @PostMapping("/generate-all-template")
    public ResponseEntity<byte[]> generateAllTemplate(
            @RequestParam(value = "merchantId", required = false) Long merchantId) {
        log.info("接收到生成全部商品模板请求，商家ID：{}", merchantId);
        
        try {
            // 如果没有传 merchantId，使用默认值 1
            if (merchantId == null) {
                merchantId = 1L;
            }
            
            // 生成模板
            byte[] excelData = productService.generateAllProductsTemplate(merchantId);
            
            // 生成文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String fileName = "meituan_all_products_" + timestamp + ".xlsx";
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(excelData.length);
            
            log.info("成功生成全部商品模板文件：{}，大小：{} 字节", fileName, excelData.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
                    
        } catch (IllegalArgumentException e) {
            log.error("生成模板失败：{}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("生成模板失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 批量上传商品到美团
     * 
     * @param request 上传请求
     * @return 上传结果
     */
    @PostMapping("/upload")
    public ApiResponse<UploadResult> uploadProducts(@RequestBody UploadRequest request) {
        log.info("接收到批量上传请求，商品数量：{}", request.getProductIds().size());
        
        if (request.getProductIds() == null || request.getProductIds().isEmpty()) {
            return ApiResponse.error(400, "商品ID列表不能为空");
        }
        
        if (request.getAccessToken() == null || request.getAccessToken().trim().isEmpty()) {
            return ApiResponse.error(400, "访问令牌不能为空");
        }
        
        UploadResult result = productService.uploadToMeituan(
            request.getProductIds(), 
            request.getAccessToken()
        );
        
        if (result.getFailedCount() > 0) {
            return ApiResponse.success(
                String.format("上传完成，成功：%d，失败：%d", 
                    result.getSuccessCount(), result.getFailedCount()), 
                result
            );
        }
        
        return ApiResponse.success(
            String.format("成功上传%d条商品，耗时：%dms", 
                result.getSuccessCount(), result.getDuration()), 
            result
        );
    }
    
    /**
     * 清空商品
     * 
     * @param request 清空请求
     * @return 清空结果
     */
    @DeleteMapping("/clear")
    public ApiResponse<ClearResult> clearProducts(@RequestBody ClearRequest request) {
        log.info("接收到清空商品请求，商家ID：{}", request.getMerchantId());
        
        if (request.getMerchantId() == null) {
            return ApiResponse.error(400, "商家ID不能为空");
        }
        
        if (request.getAccessToken() == null || request.getAccessToken().trim().isEmpty()) {
            return ApiResponse.error(400, "访问令牌不能为空");
        }
        
        ClearResult result = productService.clearAllProducts(
            request.getMerchantId(), 
            request.getAccessToken()
        );
        
        return ApiResponse.success(
            String.format("成功清空%d条商品", result.getDeletedCount()), 
            result
        );
    }
    
    /**
     * 删除单个商品
     * 
     * @param id 商品ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteProduct(@PathVariable Long id) {
        log.info("接收到删除商品请求，商品ID：{}", id);
        
        try {
            boolean success = productService.removeById(id);
            if (success) {
                log.info("成功删除商品，ID：{}", id);
                return ApiResponse.success("删除成功");
            } else {
                log.warn("删除商品失败，商品不存在，ID：{}", id);
                return ApiResponse.error(404, "商品不存在");
            }
        } catch (Exception e) {
            log.error("删除商品失败，ID：{}", id, e);
            return ApiResponse.error(500, "删除失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量删除商品
     * 
     * @param ids 商品ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public ApiResponse<java.util.Map<String, Object>> batchDeleteProducts(@RequestBody List<Long> ids) {
        log.info("接收到批量删除商品请求，商品数量：{}", ids.size());
        
        if (ids == null || ids.isEmpty()) {
            return ApiResponse.error(400, "商品ID列表不能为空");
        }
        
        try {
            boolean success = productService.removeByIds(ids);
            
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("deletedCount", ids.size());
            result.put("success", success);
            
            if (success) {
                log.info("成功批量删除商品，数量：{}", ids.size());
                return ApiResponse.success(String.format("成功删除%d个商品", ids.size()), result);
            } else {
                log.warn("批量删除商品失败");
                return ApiResponse.error(500, "批量删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除商品失败", e);
            return ApiResponse.error(500, "批量删除失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取历史生成文件列表
     * 
     * @param merchantId 商家ID
     * @param limit 限制数量
     * @return 文件列表
     */
    @GetMapping("/generated-files/recent")
    public ApiResponse<List<com.meituan.product.dto.GeneratedFileDTO>> getRecentGeneratedFiles(
            @RequestParam(value = "merchantId", required = false, defaultValue = "1") Long merchantId,
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
        log.info("查询历史生成文件，商家ID：{}，限制数量：{}", merchantId, limit);
        
        try {
            List<com.meituan.product.dto.GeneratedFileDTO> files = fileStorageService.getRecentFiles(merchantId, limit);
            return ApiResponse.success(files);
        } catch (Exception e) {
            log.error("获取历史文件失败", e);
            return ApiResponse.error(500, "获取历史文件失败：" + e.getMessage());
        }
    }
    
    /**
     * 下载历史文件
     * 
     * @param fileId 文件ID
     * @return 文件数据
     */
    @GetMapping("/generated-files/{fileId}/download")
    public ResponseEntity<byte[]> downloadGeneratedFile(@PathVariable Long fileId) {
        log.info("下载历史文件，文件ID：{}", fileId);
        
        try {
            byte[] fileData = fileStorageService.getFileData(fileId);
            com.meituan.product.entity.GeneratedFile file = fileStorageService.getFileById(fileId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                java.net.URLEncoder.encode(file.getFileName(), "UTF-8"));
            
            log.info("文件下载成功：{}", file.getFileName());
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(fileData);
        } catch (Exception e) {
            log.error("下载文件失败，文件ID：{}", fileId, e);
            return ResponseEntity.status(500).build();
        }
    }
}
