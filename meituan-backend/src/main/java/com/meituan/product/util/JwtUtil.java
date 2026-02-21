package com.meituan.product.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 用于生成和验证 JWT Token
 */
@Component
public class JwtUtil {
    
    // JWT 密钥（生产环境应该从配置文件读取）
    private static final String SECRET_KEY = "meituan-product-upload-secret-key-2024-very-long-secret-key-for-jwt";
    
    // Token 有效期：7天（毫秒）
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000;
    
    /**
     * 生成密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 生成 JWT Token
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param role 用户角色
     * @return JWT Token
     */
    public String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + EXPIRATION_TIME);
        
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * 从 Token 中解析 Claims
     * 
     * @param token JWT Token
     * @return Claims
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 验证 Token 是否有效
     * 
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims == null) {
                return false;
            }
            
            // 检查是否过期
            Date expiration = claims.getExpiration();
            return expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 从 Token 中获取用户名
     * 
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getSubject() : null;
    }
    
    /**
     * 从 Token 中获取用户ID
     * 
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            Object userId = claims.get("userId");
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            } else if (userId instanceof Long) {
                return (Long) userId;
            }
        }
        return null;
    }
    
    /**
     * 从 Token 中获取用户角色
     * 
     * @param token JWT Token
     * @return 用户角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("role") : null;
    }
}
