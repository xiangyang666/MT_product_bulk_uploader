package com.meituan.product.controller;

import com.meituan.product.common.ApiResponse;
import com.meituan.product.entity.MerchantConfig;
import com.meituan.product.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 系统设置控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {
    
    private final SettingsService settingsService;
    
    /**
     * 获取商家配置
     * 
     * @param merchantId 商家ID
     * @return 商家配置
     */
    @GetMapping
    public ApiResponse<MerchantConfig> getSettings(
            @RequestParam(value = "merchantId", defaultValue = "1") Long merchantId) {
        
        log.info("接收到查询商家配置请求，商家ID：{}", merchantId);
        
        try {
            MerchantConfig config = settingsService.getMerchantConfig(merchantId);
            return ApiResponse.success(config);
            
        } catch (Exception e) {
            log.error("查询商家配置失败", e);
            return ApiResponse.error(500, "查询商家配置失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新商家配置
     * 
     * @param config 商家配置
     * @return 更新后的配置
     */
    @PutMapping
    public ApiResponse<MerchantConfig> updateSettings(@RequestBody MerchantConfig config) {
        log.info("接收到更新商家配置请求，商家ID：{}", config.getMerchantId());
        
        try {
            MerchantConfig updatedConfig = settingsService.updateMerchantConfig(config);
            return ApiResponse.success("配置更新成功", updatedConfig);
            
        } catch (IllegalArgumentException e) {
            log.error("配置参数错误", e);
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("更新商家配置失败", e);
            return ApiResponse.error(500, "更新商家配置失败：" + e.getMessage());
        }
    }
    
    /**
     * 重置为默认配置
     * 
     * @param merchantId 商家ID
     * @return 默认配置
     */
    @PostMapping("/reset")
    public ApiResponse<MerchantConfig> resetSettings(
            @RequestParam(value = "merchantId", defaultValue = "1") Long merchantId) {
        
        log.info("接收到重置商家配置请求，商家ID：{}", merchantId);
        
        try {
            MerchantConfig config = settingsService.resetToDefaults(merchantId);
            return ApiResponse.success("配置已重置为默认值", config);
            
        } catch (Exception e) {
            log.error("重置商家配置失败", e);
            return ApiResponse.error(500, "重置商家配置失败：" + e.getMessage());
        }
    }
}
