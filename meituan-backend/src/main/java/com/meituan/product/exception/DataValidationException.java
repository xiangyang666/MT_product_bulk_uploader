package com.meituan.product.exception;

import lombok.Getter;

import java.util.List;

/**
 * 数据验证异常
 */
@Getter
public class DataValidationException extends RuntimeException {
    
    private final List<ValidationError> errors;
    
    public DataValidationException(String message, List<ValidationError> errors) {
        super(message);
        this.errors = errors;
    }
    
    /**
     * 验证错误详情
     */
    @Getter
    public static class ValidationError {
        private final int row;
        private final String field;
        private final String message;
        
        public ValidationError(int row, String field, String message) {
            this.row = row;
            this.field = field;
            this.message = message;
        }
        
        @Override
        public String toString() {
            return String.format("第%d行，字段[%s]：%s", row, field, message);
        }
    }
}
