package com.meituan.product.client;

import com.meituan.product.dto.MeituanApiResponse;
import com.meituan.product.dto.ProductDTO;
import com.meituan.product.exception.MeituanApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 美团API客户端
 */
@Slf4j
@Component
public class MeituanApiClient {
    
    @Value("${meituan.api.base-url}")
    private String baseUrl;
    
    @Value("${meituan.api.timeout}")
    private Integer timeout;
    
    private final RestTemplate restTemplate;
    
    public MeituanApiClient() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * 批量上传商品到美团平台
     * 
     * @param products 商品列表
     * @param accessToken 访问令牌
     * @return API响应
     */
    @Retryable(
        value = {MeituanApiException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public MeituanApiResponse<Map<String, Object>> uploadProducts(
            List<ProductDTO> products, 
            String accessToken) {
        
        log.info("开始调用美团API上传商品，数量：{}", products.size());
        
        try {
            // 构建请求
            String url = baseUrl + "/products/batch-upload";
            HttpHeaders headers = createHeaders(accessToken);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("products", products);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // 发送请求
            ResponseEntity<MeituanApiResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                MeituanApiResponse.class
            );
            
            MeituanApiResponse<Map<String, Object>> apiResponse = response.getBody();
            
            if (apiResponse == null || !apiResponse.isSuccess()) {
                String errorCode = apiResponse != null ? apiResponse.getCode() : "UNKNOWN";
                String errorMsg = apiResponse != null ? apiResponse.getMessage() : "未知错误";
                throw new MeituanApiException("美团API调用失败：" + errorMsg, errorCode);
            }
            
            log.info("成功上传商品到美团平台");
            return apiResponse;
            
        } catch (Exception e) {
            log.error("调用美团API失败", e);
            if (e instanceof MeituanApiException) {
                throw e;
            }
            throw new MeituanApiException("调用美团API失败：" + e.getMessage(), "NETWORK_ERROR", e);
        }
    }
    
    /**
     * 清空商家的所有商品
     * 
     * @param merchantId 商家ID
     * @param accessToken 访问令牌
     * @return API响应
     */
    @Retryable(
        value = {MeituanApiException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public MeituanApiResponse<Map<String, Object>> deleteAllProducts(
            String merchantId, 
            String accessToken) {
        
        log.info("开始调用美团API清空商品，商家ID：{}", merchantId);
        
        try {
            // 构建请求
            String url = baseUrl + "/products/clear";
            HttpHeaders headers = createHeaders(accessToken);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("merchantId", merchantId);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // 发送请求
            ResponseEntity<MeituanApiResponse> response = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                request,
                MeituanApiResponse.class
            );
            
            MeituanApiResponse<Map<String, Object>> apiResponse = response.getBody();
            
            if (apiResponse == null || !apiResponse.isSuccess()) {
                String errorCode = apiResponse != null ? apiResponse.getCode() : "UNKNOWN";
                String errorMsg = apiResponse != null ? apiResponse.getMessage() : "未知错误";
                throw new MeituanApiException("美团API调用失败：" + errorMsg, errorCode);
            }
            
            log.info("成功清空商家商品");
            return apiResponse;
            
        } catch (Exception e) {
            log.error("调用美团API失败", e);
            if (e instanceof MeituanApiException) {
                throw e;
            }
            throw new MeituanApiException("调用美团API失败：" + e.getMessage(), "NETWORK_ERROR", e);
        }
    }
    
    /**
     * 验证访问令牌
     * 
     * @param accessToken 访问令牌
     * @return 是否有效
     */
    public boolean validateAccessToken(String accessToken) {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            return false;
        }
        
        try {
            String url = baseUrl + "/auth/validate";
            HttpHeaders headers = createHeaders(accessToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            ResponseEntity<MeituanApiResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                MeituanApiResponse.class
            );
            
            MeituanApiResponse apiResponse = response.getBody();
            return apiResponse != null && apiResponse.isSuccess();
            
        } catch (Exception e) {
            log.error("验证访问令牌失败", e);
            return false;
        }
    }
    
    /**
     * 创建请求头
     */
    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("User-Agent", "Meituan-Product-Upload-Tool/1.0");
        return headers;
    }
}
