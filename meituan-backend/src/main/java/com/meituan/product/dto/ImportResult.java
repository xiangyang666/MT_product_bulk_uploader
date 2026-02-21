package com.meituan.product.dto;

import com.meituan.product.entity.Product;
import com.meituan.product.enums.FormatType;
import com.meituan.product.exception.DataValidationException;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入结果DTO
 */
@Data
public class ImportResult {
    
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
     * 导入的商品列表
     */
    private List<Product> products;
    
    /**
     * 错误列表（简单字符串格式，保持向后兼容）
     */
    private List<String> errors;
    
    /**
     * 详细错误列表（新增）
     */
    private List<ErrorDetail> errorDetails;
    
    /**
     * 识别的格式类型（新增）
     */
    private FormatType formatType;
    
    /**
     * 是否还有更多错误（新增）
     */
    private Boolean hasMoreErrors;
    
    /**
     * 剩余错误数量（新增）
     */
    private Integer remainingErrorCount;
    
    /**
     * 处理耗时（毫秒）（新增）
     */
    private Long duration;
    
    public ImportResult() {
        this.products = new ArrayList<>();
        this.errors = new ArrayList<>();
        this.errorDetails = new ArrayList<>();
        this.hasMoreErrors = false;
        this.remainingErrorCount = 0;
    }
    
    /**
     * 创建成功结果
     */
    public static ImportResult success(List<Product> products) {
        ImportResult result = new ImportResult();
        result.setProducts(products);
        result.setTotalCount(products.size());
        result.setSuccessCount(products.size());
        result.setFailedCount(0);
        result.setFormatType(FormatType.STANDARD); // 默认标准格式
        return result;
    }
    
    /**
     * 创建失败结果
     */
    public static ImportResult failure(List<DataValidationException.ValidationError> validationErrors) {
        ImportResult result = new ImportResult();
        result.setTotalCount(0);
        result.setSuccessCount(0);
        result.setFailedCount(validationErrors.size());
        result.setFormatType(FormatType.STANDARD); // 默认标准格式
        
        List<String> errors = new ArrayList<>();
        List<ErrorDetail> errorDetails = new ArrayList<>();
        
        for (DataValidationException.ValidationError error : validationErrors) {
            errors.add(error.toString());
            errorDetails.add(new ErrorDetail(
                error.getRow(),
                error.getField(),
                error.getMessage()
            ));
        }
        
        result.setErrors(errors);
        result.setErrorDetails(errorDetails);
        
        return result;
    }
    
    /**
     * 添加错误详情（限制最多100条）
     */
    public void addErrorDetail(ErrorDetail errorDetail) {
        if (this.errorDetails.size() < 100) {
            this.errorDetails.add(errorDetail);
            this.errors.add(errorDetail.toString());
        } else if (this.errorDetails.size() == 100) {
            this.hasMoreErrors = true;
        }
        
        if (this.hasMoreErrors) {
            this.remainingErrorCount++;
        }
    }
}
