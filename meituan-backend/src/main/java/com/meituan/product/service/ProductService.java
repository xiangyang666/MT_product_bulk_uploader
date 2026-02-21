package com.meituan.product.service;

import com.meituan.product.client.MeituanApiClient;
import com.meituan.product.dto.ClearResult;
import com.meituan.product.dto.ImportResult;
import com.meituan.product.dto.ProductDTO;
import com.meituan.product.dto.ProductStats;
import com.meituan.product.dto.UploadResult;
import com.meituan.product.entity.OperationLog;
import com.meituan.product.entity.Product;
import com.meituan.product.exception.DataValidationException;
import com.meituan.product.exception.MeituanApiException;
import com.meituan.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ExcelService excelService;
    private final ProductMapper productMapper;
    private final MeituanApiClient meituanApiClient;
    private final OperationLogService operationLogService;
    private final com.meituan.product.mapper.OperationLogMapper operationLogMapper;
    private final FileStorageService fileStorageService;
    
    @Value("${meituan.upload.batch-size:500}")
    private Integer batchSize;
    
    /**
     * 获取商品统计信息
     * 
     * @param merchantId 商家ID
     * @return 统计信息
     */
    public ProductStats getProductStats(Long merchantId) {
        log.info("查询商品统计信息，商家ID：{}", merchantId);
        
        if (merchantId == null) {
            throw new IllegalArgumentException("商家ID不能为空");
        }
        
        ProductStats stats = productMapper.getStats(merchantId);
        
        // 如果没有数据，返回全0的统计
        if (stats == null) {
            stats = new ProductStats();
        }
        
        log.info("商品统计 - 总数：{}，待上传：{}，已上传：{}，失败：{}", 
                stats.getTotalCount(), stats.getPendingCount(), 
                stats.getUploadedCount(), stats.getFailedCount());
        
        return stats;
    }
    
    /**
     * 获取最近导入的商品
     * 
     * @param merchantId 商家ID
     * @param limit 限制数量
     * @return 商品列表
     */
    public List<Product> getRecentProducts(Long merchantId, int limit) {
        log.info("查询最近导入的商品，商家ID：{}，限制数量：{}", merchantId, limit);
        
        if (merchantId == null) {
            throw new IllegalArgumentException("商家ID不能为空");
        }
        
        if (limit <= 0) {
            throw new IllegalArgumentException("限制数量必须大于0");
        }
        
        List<Product> products = productMapper.selectRecent(merchantId, limit);
        
        log.info("查询到{}条最近导入的商品", products.size());
        
        return products;
    }
    
    /**
     * 生成导入模板
     * 
     * @return Excel模板字节数组
     */
    public byte[] generateImportTemplate() {
        log.info("开始生成导入模板");
        
        // 创建示例商品数据
        List<Product> sampleProducts = new ArrayList<>();
        
        Product sample1 = new Product();
        sample1.setProductName("苹果 iPhone 15 Pro");
        sample1.setCategoryId("2000010001");
        sample1.setPrice(java.math.BigDecimal.valueOf(7999.00));
        sample1.setStock(100);
        sample1.setDescription("全新苹果iPhone 15 Pro 256GB 深空黑色");
        sample1.setImageUrl("https://example.com/iphone15.jpg");
        sampleProducts.add(sample1);
        
        Product sample2 = new Product();
        sample2.setProductName("华为 Mate 60 Pro");
        sample2.setCategoryId("2000010001");
        sample2.setPrice(java.math.BigDecimal.valueOf(6999.00));
        sample2.setStock(80);
        sample2.setDescription("华为Mate 60 Pro 12GB+512GB 雅川青");
        sample2.setImageUrl("https://example.com/mate60.jpg");
        sampleProducts.add(sample2);
        
        // 使用 ExcelService 生成模板
        return excelService.generateImportTemplate(sampleProducts);
    }
    
    /**
     * 解析Excel文件（仅预览，不保存）
     * 
     * @param file Excel文件
     * @return 商品列表
     */
    public List<Product> parseExcelFile(MultipartFile file) {
        log.info("开始解析Excel文件：{}", file.getOriginalFilename());
        
        try {
            List<Product> products = excelService.parseExcel(file);
            log.info("成功解析{}条商品数据", products.size());
            return products;
        } catch (Exception e) {
            log.error("解析Excel文件失败", e);
            throw e;
        }
    }
    
    /**
     * 从Excel文件导入商品数据
     * 
     * @param file Excel文件
     * @param merchantId 商家ID
     * @return 导入结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ImportResult importFromExcel(MultipartFile file, Long merchantId) {
        log.info("开始导入Excel文件，商家ID：{}，文件名：{}", merchantId, file.getOriginalFilename());
        
        try {
            // 1. 解析Excel文件
            List<Product> products = excelService.parseExcel(file);
            log.info("成功解析{}条商品数据", products.size());
            
            // 2. 设置商家ID
            products.forEach(product -> product.setMerchantId(merchantId));
            
            // 3. 批量插入数据库
            if (!products.isEmpty()) {
                int insertCount = productMapper.batchInsert(products);
                log.info("成功导入{}条商品到数据库", insertCount);
            }
            
            return ImportResult.success(products);
            
        } catch (DataValidationException e) {
            log.error("数据验证失败", e);
            return ImportResult.failure(e.getErrors());
        } catch (Exception e) {
            log.error("导入失败", e);
            throw e;
        }
    }
    
    /**
     * 预览Excel文件（只解析不导入）
     * 
     * @param file Excel文件
     * @param merchantId 商家ID
     * @return 预览结果
     */
    public ImportResult previewExcel(MultipartFile file, Long merchantId) {
        log.info("开始预览Excel文件，商家ID：{}，文件名：{}", merchantId, file.getOriginalFilename());
        
        try {
            // 只解析Excel文件，不插入数据库
            List<Product> products = excelService.parseExcel(file);
            log.info("成功解析{}条商品数据（预览模式，未导入数据库）", products.size());
            
            // 设置商家ID（仅用于预览显示）
            products.forEach(product -> product.setMerchantId(merchantId));
            
            return ImportResult.success(products);
            
        } catch (DataValidationException e) {
            log.error("数据验证失败", e);
            return ImportResult.failure(e.getErrors());
        } catch (Exception e) {
            log.error("预览失败", e);
            throw e;
        }
    }
    
    /**
     * 根据商家ID查询商品列表
     * 
     * @param merchantId 商家ID
     * @return 商品列表
     */
    public List<Product> getProductsByMerchantId(Long merchantId) {
        return productMapper.selectByMerchantId(merchantId);
    }
    
    /**
     * 根据ID查询商品
     * 
     * @param id 商品ID
     * @return 商品信息
     */
    public Product getProductById(Long id) {
        return productMapper.selectById(id);
    }
    
    /**
     * 根据ID列表查询商品
     * 
     * @param ids 商品ID列表
     * @return 商品列表
     */
    public List<Product> getProductsByIds(List<Long> ids) {
        return productMapper.selectBatchIds(ids);
    }
    
    /**
     * 生成美团上传模板
     * 
     * @param productIds 商品ID列表
     * @return Excel文件字节数组
     */
    public byte[] generateMeituanTemplate(List<Long> productIds) {
        log.info("开始生成美团上传模板，商品ID数量：{}", productIds.size());
        
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("商品ID列表不能为空");
        }
        
        // 查询商品数据
        List<Product> products = productMapper.selectBatchIds(productIds);
        
        if (products.isEmpty()) {
            throw new IllegalArgumentException("未找到指定的商品数据");
        }
        
        // 获取商家ID（从第一个商品获取）
        Long merchantId = products.get(0).getMerchantId();
        
        // 生成模板（使用用户上传的模板）
        return excelService.generateMeituanTemplateFromUserTemplate(products, merchantId);
    }
    
    /**
     * 生成全部商品的美团上传模板
     * 
     * @param merchantId 商家ID
     * @return Excel文件字节数组
     */
    public byte[] generateAllProductsTemplate(Long merchantId) {
        log.info("开始生成全部商品模板，商家ID：{}", merchantId);
        
        if (merchantId == null) {
            throw new IllegalArgumentException("商家ID不能为空");
        }
        
        long startTime = System.currentTimeMillis();
        
        // 查询商品总数
        ProductStats stats = productMapper.getStats(merchantId);
        if (stats == null || stats.getTotalCount() == 0) {
            throw new IllegalArgumentException("暂无商品数据，请先导入商品");
        }
        
        log.info("商家共有{}个商品，开始生成模板", stats.getTotalCount());
        
        // 查询所有商品
        List<Product> products = productMapper.selectByMerchantId(merchantId);
        
        if (products.isEmpty()) {
            throw new IllegalArgumentException("未找到商品数据");
        }
        
        // 生成模板（使用用户上传的模板）
        byte[] excelData = excelService.generateMeituanTemplateFromUserTemplate(products, merchantId);
        
        // 生成文件名
        String timestamp = LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("meituan_all_products_%s.xlsx", timestamp);
        
        // 保存文件到服务器
        try {
            fileStorageService.saveTemplateFile(fileName, excelData, merchantId, products.size());
            log.info("文件保存成功：{}", fileName);
        } catch (Exception e) {
            log.error("保存文件记录失败", e);
            // 不影响主流程
        }
        
        // 生成成功后，将所有商品状态更新为"已上传"（status = 1）
        try {
            int updatedCount = productMapper.updateStatusByMerchantId(merchantId, 1);
            log.info("成功更新{}个商品状态为已上传", updatedCount);
        } catch (Exception e) {
            log.error("更新商品状态失败", e);
            // 不影响模板生成，继续返回
        }
        
        // 记录操作日志
        try {
            long duration = System.currentTimeMillis() - startTime;
            OperationLog operationLog = new OperationLog();
            operationLog.setUserId(1L); // 默认用户ID，实际应从上下文获取
            operationLog.setUsername("admin"); // 默认用户名，实际应从上下文获取
            operationLog.setOperationType("GENERATE_ALL");
            operationLog.setOperationDesc(String.format("生成全部商品模板，共%d个商品", products.size()));
            operationLog.setTargetType("PRODUCT");
            operationLog.setTargetId(String.valueOf(merchantId));
            operationLog.setResult(1); // 1-成功
            operationLog.setDuration((int) duration);
            operationLog.setCreatedAt(LocalDateTime.now());
            
            operationLogMapper.insert(operationLog);
            log.info("操作日志记录成功");
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
            // 日志记录失败不影响主业务
        }
        
        return excelData;
    }
    
    /**
     * 批量上传商品到美团平台
     * 
     * @param productIds 商品ID列表
     * @param accessToken 访问令牌
     * @return 上传结果
     */
    @Transactional(rollbackFor = Exception.class)
    public UploadResult uploadToMeituan(List<Long> productIds, String accessToken) {
        log.info("开始批量上传商品到美团，商品数量：{}", productIds.size());
        
        long startTime = System.currentTimeMillis();
        
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("商品ID列表不能为空");
        }
        
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new IllegalArgumentException("访问令牌不能为空");
        }
        
        // 查询商品数据
        List<Product> products = productMapper.selectBatchIds(productIds);
        
        if (products.isEmpty()) {
            throw new IllegalArgumentException("未找到指定的商品数据");
        }
        
        // 分批上传
        List<List<Product>> batches = partitionList(products, batchSize);
        log.info("商品分为{}批上传，每批{}条", batches.size(), batchSize);
        
        int totalCount = products.size();
        int successCount = 0;
        List<Product> failedProducts = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (int i = 0; i < batches.size(); i++) {
            List<Product> batch = batches.get(i);
            log.info("正在上传第{}/{}批，数量：{}", i + 1, batches.size(), batch.size());
            
            try {
                // 转换为DTO
                List<ProductDTO> productDTOs = batch.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
                
                // 调用美团API
                meituanApiClient.uploadProducts(productDTOs, accessToken);
                
                // 更新商品状态为已上传
                batch.forEach(product -> {
                    product.setStatus(Product.Status.UPLOADED.getCode());
                    productMapper.updateById(product);
                });
                
                successCount += batch.size();
                log.info("第{}批上传成功", i + 1);
                
            } catch (MeituanApiException e) {
                log.error("第{}批上传失败", i + 1, e);
                
                // 更新商品状态为上传失败
                batch.forEach(product -> {
                    product.setStatus(Product.Status.FAILED.getCode());
                    productMapper.updateById(product);
                    failedProducts.add(product);
                });
                
                errors.add(String.format("第%d批上传失败：%s", i + 1, e.getMessage()));
                
                // 继续上传剩余批次
                continue;
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        
        log.info("批量上传完成，总数：{}，成功：{}，失败：{}，耗时：{}ms", 
                totalCount, successCount, failedProducts.size(), duration);
        
        if (failedProducts.isEmpty()) {
            return UploadResult.success(totalCount, duration);
        } else {
            return UploadResult.partial(totalCount, successCount, failedProducts, errors, duration);
        }
    }
    
    /**
     * 转换Product为ProductDTO
     */
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setProductName(product.getProductName());
        dto.setCategoryId(product.getCategoryId());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setDescription(product.getDescription());
        dto.setImageUrl(product.getImageUrl());
        return dto;
    }
    
    /**
     * 将列表分批
     */
    private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            int end = Math.min(i + batchSize, list.size());
            batches.add(list.subList(i, end));
        }
        return batches;
    }
    
    /**
     * 清空商家的所有商品
     * 
     * @param merchantId 商家ID
     * @param accessToken 访问令牌
     * @return 清空结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ClearResult clearAllProducts(Long merchantId, String accessToken) {
        log.info("开始清空商品，商家ID：{}", merchantId);
        
        if (merchantId == null) {
            throw new IllegalArgumentException("商家ID不能为空");
        }
        
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new IllegalArgumentException("访问令牌不能为空");
        }
        
        // 验证访问令牌（简单验证，实际应该更严格）
        if (!"admin123".equals(accessToken)) {
            throw new IllegalArgumentException("访问令牌无效");
        }
        
        try {
            // 清空本地数据库商品（不调用美团API，因为美团API不支持批量删除）
            int deletedCount = productMapper.deleteByMerchantId(merchantId);
            log.info("成功清空本地数据库商品，商家ID：{}，数量：{}", merchantId, deletedCount);
            
            return ClearResult.success(deletedCount);
            
        } catch (Exception e) {
            log.error("清空商品失败，商家ID：{}", merchantId, e);
            throw new RuntimeException("清空商品失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 删除单个商品（逻辑删除）
     * 
     * @param id 商品ID
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Long id) {
        log.info("删除商品，ID：{}", id);
        
        if (id == null) {
            throw new IllegalArgumentException("商品ID不能为空");
        }
        
        try {
            int result = productMapper.deleteById(id);
            boolean success = result > 0;
            
            if (success) {
                log.info("成功删除商品，ID：{}", id);
            } else {
                log.warn("删除商品失败，商品不存在，ID：{}", id);
            }
            
            return success;
        } catch (Exception e) {
            log.error("删除商品失败，ID：{}", id, e);
            throw new RuntimeException("删除商品失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 批量删除商品（逻辑删除）
     * 
     * @param ids 商品ID列表
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(List<Long> ids) {
        log.info("批量删除商品，数量：{}", ids.size());
        
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("商品ID列表不能为空");
        }
        
        try {
            int result = productMapper.deleteBatchIds(ids);
            boolean success = result > 0;
            
            if (success) {
                log.info("成功批量删除商品，数量：{}", result);
            } else {
                log.warn("批量删除商品失败，没有商品被删除");
            }
            
            return success;
        } catch (Exception e) {
            log.error("批量删除商品失败", e);
            throw new RuntimeException("批量删除商品失败：" + e.getMessage(), e);
        }
    }
}
