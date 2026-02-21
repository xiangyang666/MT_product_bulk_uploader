package com.meituan.product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 密码服务类
 * 提供密码加密、验证和强度检查功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordService {
    
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 密码最小长度
     */
    private static final int MIN_PASSWORD_LENGTH = 8;
    
    /**
     * 加密密码
     * 
     * @param rawPassword 明文密码
     * @return BCrypt加密后的密码
     */
    public String encodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        // 验证密码强度
        validatePasswordStrength(rawPassword);
        
        // 使用BCrypt加密
        String encodedPassword = passwordEncoder.encode(rawPassword);
        log.debug("密码加密成功");
        
        return encodedPassword;
    }
    
    /**
     * 验证密码
     * 
     * @param rawPassword 明文密码
     * @param encodedPassword BCrypt加密后的密码
     * @return 密码是否匹配
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        
        try {
            boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
            log.debug("密码验证结果: {}", matches);
            return matches;
        } catch (Exception e) {
            log.error("密码验证失败", e);
            return false;
        }
    }
    
    /**
     * 验证密码强度
     * 
     * @param password 密码
     * @throws IllegalArgumentException 如果密码不符合要求
     */
    public void validatePasswordStrength(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                String.format("密码长度至少为%d个字符", MIN_PASSWORD_LENGTH)
            );
        }
        
        // 可以添加更多密码强度检查规则
        // 例如：必须包含大小写字母、数字、特殊字符等
    }
    
    /**
     * 检查密码是否为BCrypt格式
     * 
     * @param password 密码
     * @return 是否为BCrypt格式
     */
    public boolean isBCryptFormat(String password) {
        if (password == null) {
            return false;
        }
        
        // BCrypt密码格式：$2a$, $2b$, 或 $2y$ 开头
        return password.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }
}
