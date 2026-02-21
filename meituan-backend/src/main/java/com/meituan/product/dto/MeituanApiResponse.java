package com.meituan.product.dto;

import lombok.Data;

/**
 * 美团API响应
 */
@Data
public class MeituanApiResponse<T> {
    
    /**
     * 响应码
     */
    private String code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return "0".equals(code) || "200".equals(code);
    }
}
