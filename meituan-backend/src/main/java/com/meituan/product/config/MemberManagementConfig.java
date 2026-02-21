package com.meituan.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 成员管理配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "member-management")
public class MemberManagementConfig {
    
    /**
     * 默认管理员配置
     */
    private DefaultAdmin defaultAdmin = new DefaultAdmin();
    
    /**
     * 密码配置
     */
    private Password password = new Password();
    
    @Data
    public static class DefaultAdmin {
        /**
         * 默认管理员用户名
         */
        private String username = "admin";
        
        /**
         * 默认管理员密码
         */
        private String password = "Admin@123456";
        
        /**
         * 默认管理员真实姓名
         */
        private String realName = "系统管理员";
        
        /**
         * 默认管理员邮箱
         */
        private String email = "admin@example.com";
    }
    
    @Data
    public static class Password {
        /**
         * 密码最小长度
         */
        private int minLength = 8;
        
        /**
         * BCrypt加密强度
         */
        private int bcryptStrength = 10;
    }
}
