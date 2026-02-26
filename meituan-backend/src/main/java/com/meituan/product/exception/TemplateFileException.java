package com.meituan.product.exception;

/**
 * 模板文件异常
 * 当模板文件损坏、丢失或读取失败时抛出此异常
 */
public class TemplateFileException extends RuntimeException {
    
    public TemplateFileException(String message) {
        super(message);
    }
    
    public TemplateFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
