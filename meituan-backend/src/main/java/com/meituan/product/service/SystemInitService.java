package com.meituan.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.meituan.product.config.MemberManagementConfig;
import com.meituan.product.entity.User;
import com.meituan.product.mapper.UserMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 系统初始化服务
 * 负责系统启动时的初始化工作，如创建默认超级管理员
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemInitService {
    
    private final UserMapper userMapper;
    private final PasswordService passwordService;
    private final MemberManagementConfig config;
    
    /**
     * 系统启动时执行初始化
     */
    @PostConstruct
    @Transactional(rollbackFor = Exception.class)
    public void init() {
        log.info("开始系统初始化...");
        
        try {
            // 初始化默认超级管理员
            initDefaultSuperAdmin();
            
            log.info("系统初始化完成");
        } catch (Exception e) {
            log.error("系统初始化失败", e);
            // 不抛出异常，避免影响系统启动
        }
    }
    
    /**
     * 初始化默认超级管理员
     * 如果数据库中没有超级管理员，则创建默认账号
     */
    private void initDefaultSuperAdmin() {
        log.info("检查默认超级管理员...");
        
        // 检查是否已存在超级管理员
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getRole, PermissionService.ROLE_SUPER_ADMIN);
        Long count = userMapper.selectCount(queryWrapper);
        
        if (count > 0) {
            log.info("超级管理员已存在，跳过初始化");
            return;
        }
        
        // 获取配置
        MemberManagementConfig.DefaultAdmin adminConfig = config.getDefaultAdmin();
        
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> usernameQuery = new LambdaQueryWrapper<>();
        usernameQuery.eq(User::getUsername, adminConfig.getUsername());
        User existingUser = userMapper.selectOne(usernameQuery);
        
        if (existingUser != null) {
            log.warn("用户名 {} 已存在，但不是超级管理员，跳过初始化", adminConfig.getUsername());
            return;
        }
        
        // 创建默认超级管理员
        User admin = new User();
        admin.setUsername(adminConfig.getUsername());
        admin.setPassword(passwordService.encodePassword(adminConfig.getPassword()));
        admin.setRealName(adminConfig.getRealName());
        admin.setEmail(adminConfig.getEmail());
        admin.setRole(PermissionService.ROLE_SUPER_ADMIN);
        admin.setStatus(1); // 启用
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        admin.setCreatedBy("system");
        admin.setUpdatedBy("system");
        
        int result = userMapper.insert(admin);
        
        if (result > 0) {
            log.info("默认超级管理员创建成功: username={}, id={}", 
                    admin.getUsername(), admin.getId());
            log.warn("⚠️ 请在首次登录后立即修改默认密码！");
        } else {
            log.error("默认超级管理员创建失败");
        }
    }
}
