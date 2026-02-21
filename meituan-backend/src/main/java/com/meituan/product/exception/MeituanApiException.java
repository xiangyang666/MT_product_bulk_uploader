package com.meituan.product.exception;

import lombok.Getter;

/**
 * 美团API异常
 */
@Getter
public class MeituanApiException extends RuntimeException {
    
    /**
     * API错误码
     */
    private final String apiErrorCode;
    
    public MeituanApiException(String message, String apiErrorCode) {
        super(message);
        this.apiErrorCode = apiErrorCode;
    }
    
    public MeituanApiException(String message, String apiErrorCode, Throwable cause) {
        super(message, cause);
        this.apiErrorCode = apiErrorCode;
    }
}
