package com.meituan.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.meituan.product.entity.MerchantConfig;
import com.meituan.product.entity.OperationLog;
import com.meituan.product.mapper.MerchantConfigMapper;
import com.meituan.product.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统设置服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsService {
    
    private final MerchantConfigMapper merchantConfigMapper;
    private final OperationLogMapper operationLogMapper;
    
    /**
     * 获取商家配置
     * 
     * @param merchantId 商家ID
     * @return 商家配置
     */
    public MerchantConfig getMerchantConfig(Long merchantId) {
        log.info("查询商家配置，商家ID：{}", merchantId);
        
        LambdaQueryWrapper<MerchantConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantConfig::getMerchantId, merchantId);
        
        MerchantConfig config = merchantConfigMapper.selectOne(queryWrapper);
        
        if (config == null) {
            log.info("商家配置不存在，创建默认配置，商家ID：{}", merchantId);
            config = createDefaultConfig(merchantId);
        }
        
        return config;
    }
    
    /**
     * 更新商家配置
     * 
     * @param config 商家配置
     * @return 更新后的配置
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantConfig updateMerchantConfig(MerchantConfig config) {
        log.info("更新商家配置，商家ID：{}", config.getMerchantId());
        
        long startTime = System.currentTimeMillis();
        MerchantConfig existingConfig = null;
        
        try {
            // 清理 JSON 配置
            sanitizeConfigJson(config);
            
            // 验证配置
            validateConfig(config);
            
            // 查询现有配置
            LambdaQueryWrapper<MerchantConfig> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MerchantConfig::getMerchantId, config.getMerchantId());
            existingConfig = merchantConfigMapper.selectOne(queryWrapper);
            
            if (existingConfig == null) {
                // 新建配置
                merchantConfigMapper.insert(config);
                log.info("创建商家配置成功，商家ID：{}", config.getMerchantId());
            } else {
                // 更新配置
                config.setId(existingConfig.getId());
                merchantConfigMapper.updateById(config);
                log.info("更新商家配置成功，商家ID：{}", config.getMerchantId());
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("更新商家配置成功，商家ID：{}，耗时：{}ms", config.getMerchantId(), duration);
            
            return config;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("更新商家配置失败，耗时：{}ms，错误：{}", duration, e.getMessage(), e);
            
            // 尝试错误恢复
            if (e.getMessage() != null && e.getMessage().contains("JSON")) {
                log.warn("检测到 JSON 相关错误，尝试使用默认 JSON 值重试，商家ID：{}", config.getMerchantId());
                return retryWithDefaultJson(config, existingConfig);
            }
            
            throw new RuntimeException("更新商家配置失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 使用默认 JSON 值重试更新
     * 
     * @param config 商家配置
     * @param existingConfig 现有配置（可能为 null）
     * @return 更新后的配置
     */
    private MerchantConfig retryWithDefaultJson(MerchantConfig config, MerchantConfig existingConfig) {
        try {
            // 强制设置为空 JSON 对象
            config.setTemplateConfig("{}");
            
            // 如果 existingConfig 为 null，重新查询以确保准确性
            if (existingConfig == null) {
                LambdaQueryWrapper<MerchantConfig> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(MerchantConfig::getMerchantId, config.getMerchantId());
                existingConfig = merchantConfigMapper.selectOne(queryWrapper);
            }
            
            if (existingConfig == null) {
                // 确保 ID 为 null，避免主键冲突
                config.setId(null);
                merchantConfigMapper.insert(config);
                log.info("使用默认 JSON 创建商家配置成功，商家ID：{}", config.getMerchantId());
            } else {
                // 更新配置
                config.setId(existingConfig.getId());
                merchantConfigMapper.updateById(config);
                log.info("使用默认 JSON 更新商家配置成功，商家ID：{}", config.getMerchantId());
            }
            
            return config;
            
        } catch (Exception retryException) {
            log.error("使用默认 JSON 重试失败，商家ID：{}", config.getMerchantId(), retryException);
            throw new RuntimeException("配置保存失败，请联系系统管理员。错误详情：" + retryException.getMessage(), retryException);
        }
    }
    
    /**
     * 重置为默认配置
     * 
     * @param merchantId 商家ID
     * @return 默认配置
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantConfig resetToDefaults(Long merchantId) {
        log.info("重置商家配置为默认值，商家ID：{}", merchantId);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 查询现有配置
            LambdaQueryWrapper<MerchantConfig> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MerchantConfig::getMerchantId, merchantId);
            MerchantConfig existingConfig = merchantConfigMapper.selectOne(queryWrapper);
            
            // 创建默认配置
            MerchantConfig defaultConfig = new MerchantConfig();
            defaultConfig.setMerchantId(merchantId);
            defaultConfig.setMerchantName("默认商家");
            defaultConfig.setMeituanAppKey("");
            defaultConfig.setMeituanAppSecret("");
            defaultConfig.setAccessToken("");
            defaultConfig.setTemplateConfig("{}");
            
            if (existingConfig == null) {
                // 新建配置
                merchantConfigMapper.insert(defaultConfig);
            } else {
                // 更新为默认值
                defaultConfig.setId(existingConfig.getId());
                merchantConfigMapper.updateById(defaultConfig);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("重置商家配置成功，商家ID：{}，耗时：{}ms", merchantId, duration);
            
            return defaultConfig;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("重置商家配置失败，耗时：{}ms", duration, e);
            
            throw new RuntimeException("重置商家配置失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 创建默认配置
     * 
     * @param merchantId 商家ID
     * @return 默认配置
     */
    private MerchantConfig createDefaultConfig(Long merchantId) {
        MerchantConfig config = new MerchantConfig();
        config.setMerchantId(merchantId);
        config.setMerchantName("默认商家");
        config.setMeituanAppKey("");
        config.setMeituanAppSecret("");
        config.setAccessToken("");
        config.setTemplateConfig("{}");
        
        merchantConfigMapper.insert(config);
        
        return config;
    }
    
    /**
     * 验证配置
     * 
     * @param config 商家配置
     */
    private void validateConfig(MerchantConfig config) {
        if (config.getMerchantId() == null) {
            throw new IllegalArgumentException("商家ID不能为空");
        }
        
        if (config.getMerchantName() != null && config.getMerchantName().length() > 100) {
            throw new IllegalArgumentException("商家名称长度不能超过100个字符");
        }
        
        if (config.getMeituanAppKey() != null && config.getMeituanAppKey().length() > 100) {
            throw new IllegalArgumentException("美团AppKey长度不能超过100个字符");
        }
        
        if (config.getMeituanAppSecret() != null && config.getMeituanAppSecret().length() > 100) {
            throw new IllegalArgumentException("美团AppSecret长度不能超过100个字符");
        }
        
        if (config.getAccessToken() != null && config.getAccessToken().length() > 500) {
            throw new IllegalArgumentException("访问令牌长度不能超过500个字符");
        }
        
        // 验证模板配置是否为有效的JSON（简单验证）
        if (config.getTemplateConfig() != null && !config.getTemplateConfig().trim().isEmpty()) {
            String templateConfig = config.getTemplateConfig().trim();
            if (!templateConfig.startsWith("{") && !templateConfig.startsWith("[")) {
                throw new IllegalArgumentException("模板配置必须是有效的JSON格式");
            }
        }
    }
    
    /**
     * 清理配置中的 JSON 字段
     * 确保 templateConfig 永远不为 null 或空字符串
     * 
     * @param config 商家配置
     */
    private void sanitizeConfigJson(MerchantConfig config) {
        if (config.getTemplateConfig() == null || config.getTemplateConfig().trim().isEmpty()) {
            log.warn("检测到空的 template_config，已自动设置为空 JSON 对象，商家ID：{}", config.getMerchantId());
            config.setTemplateConfig("{}");
        }
    }
}
