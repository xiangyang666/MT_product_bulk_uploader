package com.meituan.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.meituan.product.entity.User;
import com.meituan.product.mapper.UserMapper;
import com.meituan.product.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginForm) {
        String username = loginForm.get("username");
        String password = loginForm.get("password");
        
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        
        Map<String, Object> response = new HashMap<>();
        
        if (user == null) {
            response.put("code", 400);
            response.put("message", "用户不存在");
            return response;
        }
        
        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() == 0) {
            response.put("code", 403);
            response.put("message", "账号已被禁用，请联系管理员");
            return response;
        }
        
        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.put("code", 400);
            response.put("message", "密码错误");
            return response;
        }
        
        // 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole());
        data.put("userInfo", userInfo);
        
        response.put("code", 200);
        response.put("message", "登录成功");
        response.put("data", data);
        
        return response;
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> registerForm) {
        String username = registerForm.get("username");
        String password = registerForm.get("password");
        
        Map<String, Object> response = new HashMap<>();
        
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User existUser = userMapper.selectOne(wrapper);
        
        if (existUser != null) {
            response.put("code", 400);
            response.put("message", "用户名已存在");
            return response;
        }
        
        // 加密密码
        String encodedPassword = passwordEncoder.encode(password);
        
        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setRole("USER");
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        userMapper.insert(user);
        
        response.put("code", 200);
        response.put("message", "注册成功");
        response.put("data", null);
        
        return response;
    }
    
    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Map<String, Object> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "退出成功");
        response.put("data", null);
        return response;
    }
    
    /**
     * 获取用户信息
     */
    @GetMapping("/userinfo")
    public Map<String, Object> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId != null) {
                User user = userMapper.selectById(userId);
                if (user != null) {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("id", user.getId());
                    userInfo.put("username", user.getUsername());
                    userInfo.put("email", user.getEmail());
                    userInfo.put("role", user.getRole());
                    
                    response.put("code", 200);
                    response.put("message", "获取成功");
                    response.put("data", userInfo);
                    return response;
                }
            }
        }
        
        response.put("code", 401);
        response.put("message", "未授权");
        response.put("data", null);
        return response;
    }
}
