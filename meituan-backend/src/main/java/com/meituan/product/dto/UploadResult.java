package com.meituan.product.dto;

import com.meituan.product.entity.Product;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 上传结果DTO
 */
@Data
public class UploadResult {
    
    /**
     * 总数量
     */
    private Integer totalCount;
    
    /**
     * 成功数量
     */
    private Integer successCount;
    
    /**
     * 失败数量
     */
    private Integer failedCount;
    
    /**
     * 失败的商品列表
     */
    private List<Product> failedProducts;
    
    /**
     * 耗时（毫秒）
     */
    private Long duration;
    
    /**
     * 错误信息列表
     */
    private List<String> errors;
    
    public UploadResult() {
        this.failedProducts = new ArrayList<>();
        this.errors = new ArrayList<>();
    }
    
    /**
     * 创建成功结果
     */
    public static UploadResult success(int totalCount, long duration) {
        UploadResult result = new UploadResult();
        result.setTotalCount(totalCount);
        result.setSuccessCount(totalCount);
        result.setFailedCount(0);
        result.setDuration(duration);
        return result;
    }
    
    /**
     * 创建部分成功结果
     */
    public static UploadResult partial(int totalCount, int successCount, 
                                      List<Product> failedProducts, 
                                      List<String> errors, long duration) {
        UploadResult result = new UploadResult();
        result.setTotalCount(totalCount);
        result.setSuccessCount(successCount);
        result.setFailedCount(failedProducts.size());
        result.setFailedProducts(failedProducts);
        result.setErrors(errors);
        result.setDuration(duration);
        return result;
    }
    
    /**
     * 合并两个结果
     */
    public static UploadResult merge(UploadResult r1, UploadResult r2) {
        UploadResult result = new UploadResult();
        result.setTotalCount(r1.getTotalCount() + r2.getTotalCount());
        result.setSuccessCount(r1.getSuccessCount() + r2.getSuccessCount());
        result.setFailedCount(r1.getFailedCount() + r2.getFailedCount());
        
        List<Product> failedProducts = new ArrayList<>();
        failedProducts.addAll(r1.getFailedProducts());
        failedProducts.addAll(r2.getFailedProducts());
        result.setFailedProducts(failedProducts);
        
        List<String> errors = new ArrayList<>();
        errors.addAll(r1.getErrors());
        errors.addAll(r2.getErrors());
        result.setErrors(errors);
        
        result.setDuration(r1.getDuration() + r2.getDuration());
        return result;
    }
}
