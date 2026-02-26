package com.meituan.product.exception;

import com.meituan.product.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理文件格式异常
     */
    @ExceptionHandler(FileFormatException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileFormatException(FileFormatException e) {
        log.error("文件格式错误", e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, "文件格式错误：" + e.getMessage()));
    }
    
    /**
     * 处理模板未找到异常
     */
    @ExceptionHandler(TemplateNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleTemplateNotFoundException(TemplateNotFoundException e) {
        log.error("模板未找到异常: 商家ID={}, 错误={}", e.getMerchantId(), e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, "请先上传美团模板"));
    }
    
    /**
     * 处理模板文件异常
     */
    @ExceptionHandler(TemplateFileException.class)
    public ResponseEntity<ApiResponse<Void>> handleTemplateFileException(TemplateFileException e) {
        log.error("模板文件异常: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, e.getMessage()));
    }
    
    /**
     * 处理数据验证异常
     */
    @ExceptionHandler(DataValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataValidationException(DataValidationException e) {
        log.error("数据验证失败", e);
        
        // 构建错误详情
        StringBuilder errorDetail = new StringBuilder("数据验证失败：\n");
        for (DataValidationException.ValidationError error : e.getErrors()) {
            errorDetail.append(error.toString()).append("\n");
        }
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, errorDetail.toString()));
    }
    
    /**
     * 处理文件大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("文件大小超限", e);
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.error(413, "文件过大，请选择小于10MB的文件"));
    }
    
    /**
     * 处理参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("参数错误", e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, "参数错误：" + e.getMessage()));
    }
    
    /**
     * 处理美团API异常
     */
    @ExceptionHandler(MeituanApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleMeituanApiException(MeituanApiException e) {
        log.error("美团API调用失败", e);
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.error(502, "美团API调用失败：" + e.getMessage()));
    }
    
    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("系统错误", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "系统错误，请联系管理员"));
    }
}
