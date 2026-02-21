package com.meituan.product.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改状态请求DTO
 */
@Data
public class ChangeStatusRequest {
    
    /**
     * 状态：0-禁用，1-启用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
