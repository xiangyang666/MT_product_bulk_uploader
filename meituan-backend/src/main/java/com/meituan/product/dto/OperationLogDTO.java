package com.meituan.product.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志DTO（用于前端展示）
 */
@Data
public class OperationLogDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 操作类型
     */
    private String operationType;
    
    /**
     * 操作描述
     */
    private String operationDesc;
    
    /**
     * 商品数量
     */
    private Integer productCount;
    
    /**
     * 操作时间
     */
    private LocalDateTime operationTime;
    
    /**
     * 状态：SUCCESS-成功，FAILED-失败
     */
    private String status;
    
    /**
     * 错误信息
     */
    private String errorMsg;
    
    /**
     * 耗时（毫秒）
     */
    private Integer duration;
}
