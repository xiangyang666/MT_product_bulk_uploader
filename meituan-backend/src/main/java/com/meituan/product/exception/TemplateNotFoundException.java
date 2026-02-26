package com.meituan.product.exception;

import lombok.Getter;

/**
 * 模板未找到异常
 * 当商家没有上传美团模板时抛出此异常
 */
@Getter
public class TemplateNotFoundException extends RuntimeException {
    
    private final Long merchantId;
    
    public TemplateNotFoundException(Long merchantId) {
        super("未找到商家的美团模板，商家ID: " + merchantId);
        this.merchantId = merchantId;
    }
    
    public TemplateNotFoundException(Long merchantId, String message) {
        super(message);
        this.merchantId = merchantId;
    }
}
