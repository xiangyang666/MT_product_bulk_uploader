package com.meituan.product.config;

import com.meituan.product.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置类
 * 配置密码加密和访问控制
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 配置BCrypt密码编码器
     * strength=10 表示加密强度
     * 
     * @return BCryptPasswordEncoder实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // 禁用CSRF保护
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 无状态会话
            )
            .authorizeHttpRequests(auth -> auth
                // 允许匿名访问的接口（登录注册）
                .requestMatchers("/api/auth/**").permitAll()
                // 允许匿名访问公司信息接口（landing page需要）
                .requestMatchers("/api/company").permitAll()
                // 允许匿名访问最新版本信息接口（landing page下载需要）
                .requestMatchers("/api/app-versions/latest/**").permitAll()
                // 允许匿名访问下载链接接口（landing page下载需要）
                .requestMatchers("/api/app-versions/*/download").permitAll()
                // 其他所有 API 都需要认证
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .cors(cors -> {}) // 启用CORS
            // 添加 JWT 过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
