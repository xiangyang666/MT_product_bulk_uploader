package com.meituan.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.meituan.product.entity.DevToolsConfig;
import com.meituan.product.mapper.DevToolsConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 开发者工具服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DevToolsService {
    
    private final DevToolsConfigMapper devToolsConfigMapper;
    private final PasswordEncoder passwordEncoder;
    
    private static final String DEV_TOOLS_PASSWORD_KEY = "dev_tools_password";
    
    /**
     * 设置开发者工具密码
     * 
     * @param password 新密码（明文）
     * @param username 操作用户名
     */
    @Transactional(rollbackFor = Exception.class)
    public void setDevToolsPassword(String password, String username) {
        log.info("设置开发者工具密码，操作人：{}", username);
        
        // 加密密码
        String encodedPassword = passwordEncoder.encode(password);
        
        // 查询配置是否存在
        LambdaQueryWrapper<DevToolsConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DevToolsConfig::getConfigKey, DEV_TOOLS_PASSWORD_KEY);
        DevToolsConfig config = devToolsConfigMapper.selectOne(queryWrapper);
        
        if (config == null) {
            // 不存在则创建
            config = new DevToolsConfig();
            config.setConfigKey(DEV_TOOLS_PASSWORD_KEY);
            config.setConfigValue(encodedPassword);
            config.setDescription("开发者工具访问密码（BCrypt加密）");
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
            config.setCreatedBy(username);
            config.setUpdatedBy(username);
            
            devToolsConfigMapper.insert(config);
            log.info("创建开发者工具密码配置成功");
        } else {
            // 存在则更新
            config.setConfigValue(encodedPassword);
            config.setUpdatedAt(LocalDateTime.now());
            config.setUpdatedBy(username);
            
            devToolsConfigMapper.updateById(config);
            log.info("更新开发者工具密码配置成功");
        }
    }
    
    /**
     * 验证开发者工具密码
     * 
     * @param password 待验证的密码（明文）
     * @return 是否验证通过
     */
    public boolean verifyDevToolsPassword(String password) {
        log.info("验证开发者工具密码");
        
        // 查询配置
        LambdaQueryWrapper<DevToolsConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DevToolsConfig::getConfigKey, DEV_TOOLS_PASSWORD_KEY);
        DevToolsConfig config = devToolsConfigMapper.selectOne(queryWrapper);
        
        if (config == null || config.getConfigValue() == null || config.getConfigValue().isEmpty()) {
            log.warn("开发者工具密码未设置");
            return false;
        }
        
        // 验证密码
        boolean matches = passwordEncoder.matches(password, config.getConfigValue());
        log.info("密码验证结果：{}", matches ? "通过" : "失败");
        
        return matches;
    }
    
    /**
     * 检查是否已设置开发者工具密码
     * 
     * @return 是否已设置
     */
    public boolean hasDevToolsPassword() {
        LambdaQueryWrapper<DevToolsConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DevToolsConfig::getConfigKey, DEV_TOOLS_PASSWORD_KEY);
        DevToolsConfig config = devToolsConfigMapper.selectOne(queryWrapper);
        
        boolean hasPassword = config != null && 
                             config.getConfigValue() != null && 
                             !config.getConfigValue().isEmpty();
        
        log.debug("开发者工具密码设置状态：{}", hasPassword ? "已设置" : "未设置");
        return hasPassword;
    }
}
