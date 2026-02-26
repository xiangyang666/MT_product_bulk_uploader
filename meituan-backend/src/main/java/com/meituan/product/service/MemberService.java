package com.meituan.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meituan.product.dto.*;
import com.meituan.product.entity.User;
import com.meituan.product.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 成员管理服务类
 * 提供成员的增删改查和权限管理功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    
    private final UserMapper userMapper;
    private final PasswordService passwordService;
    private final PermissionService permissionService;
    private final OperationLogService operationLogService;
    
    /**
     * 查询成员列表（带权限过滤和分页）
     * 
     * @param username 用户名筛选（可选）
     * @param role 角色筛选（可选）
     * @param status 状态筛选（可选）
     * @param page 页码
     * @param size 每页大小
     * @param currentUser 当前用户
     * @return 成员列表分页结果
     */
    public IPage<UserDTO> listMembers(String username, String role, Integer status,
                                      Integer page, Integer size, User currentUser) {
        log.info("查询成员列表: username={}, role={}, status={}, page={}, size={}, currentUser={}",
                username, role, status, page, size, currentUser.getUsername());
        
        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 根据当前用户权限过滤
        // admin-plus 和超级管理员可以看到所有成员
        if (!permissionService.isAdminPlus(currentUser) && !permissionService.isSuperAdmin(currentUser)) {
            // 其他用户只能看到自己
            queryWrapper.eq(User::getId, currentUser.getId());
        }
        
        // 添加筛选条件
        if (username != null && !username.isEmpty()) {
            queryWrapper.like(User::getUsername, username);
        }
        if (role != null && !role.isEmpty()) {
            queryWrapper.eq(User::getRole, role);
        }
        if (status != null) {
            queryWrapper.eq(User::getStatus, status);
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(User::getCreatedAt);
        
        // 分页查询
        Page<User> pageRequest = new Page<>(page, size);
        IPage<User> userPage = userMapper.selectPage(pageRequest, queryWrapper);
        
        // 转换为DTO
        IPage<UserDTO> dtoPage = userPage.convert(this::convertToDTO);
        
        log.info("查询成员列表成功，共 {} 条记录", dtoPage.getTotal());
        return dtoPage;
    }
    
    /**
     * 创建成员
     * 
     * @param request 创建请求
     * @param currentUser 当前用户
     * @return 创建的成员DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDTO createMember(CreateMemberRequest request, User currentUser) {
        log.info("创建成员: username={}, role={}, operator={}",
                request.getUsername(), request.getRole(), currentUser.getUsername());
        
        // 验证权限：admin-plus 和超级管理员可以创建成员
        if (!permissionService.isAdminPlus(currentUser) && !permissionService.isSuperAdmin(currentUser)) {
            throw new IllegalStateException("权限不足，只有超级管理员可以创建成员");
        }
        
        // 验证角色有效性
        if (!permissionService.isValidRole(request.getRole())) {
            throw new IllegalArgumentException("无效的角色: " + request.getRole());
        }
        
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, request.getUsername());
        Long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new IllegalArgumentException("用户名已存在: " + request.getUsername());
        }
        
        // 创建用户对象
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordService.encodePassword(request.getPassword()));
        user.setRole(request.getRole());
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(1); // 默认启用
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setCreatedBy(currentUser.getUsername());
        user.setUpdatedBy(currentUser.getUsername());
        
        // 保存到数据库
        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new RuntimeException("创建成员失败");
        }
        
        // 记录操作日志
        operationLogService.log(
                currentUser.getId(),
                currentUser.getUsername(),
                "MEMBER_CREATE",
                "创建成员: " + user.getUsername(),
                "USER",
                user.getId().toString(),
                user.getUsername(),
                1,
                null
        );
        
        log.info("创建成员成功: id={}, username={}", user.getId(), user.getUsername());
        return convertToDTO(user);
    }
    
    /**
     * 更新成员信息
     * 
     * @param id 成员ID
     * @param request 更新请求
     * @param currentUser 当前用户
     * @return 更新后的成员DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDTO updateMember(Long id, UpdateMemberRequest request, User currentUser) {
        log.info("更新成员: id={}, operator={}", id, currentUser.getUsername());
        
        // 查询目标用户
        User targetUser = userMapper.selectById(id);
        if (targetUser == null) {
            throw new IllegalArgumentException("用户不存在: " + id);
        }
        
        // 验证权限
        if (!permissionService.canEditMember(currentUser, targetUser)) {
            throw new IllegalStateException("权限不足，无法编辑该成员");
        }
        
        // 如果要修改角色，检查权限
        if (request.getRole() != null && !request.getRole().equals(targetUser.getRole())) {
            if (!permissionService.canChangeRole(currentUser, targetUser)) {
                throw new IllegalStateException("权限不足，无法修改角色");
            }
            
            // 验证角色有效性
            if (!permissionService.isValidRole(request.getRole())) {
                throw new IllegalArgumentException("无效的角色: " + request.getRole());
            }
            
            targetUser.setRole(request.getRole());
        }
        
        // 更新其他字段
        if (request.getUsername() != null && !request.getUsername().equals(targetUser.getUsername())) {
            // 检查新用户名是否已存在
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, request.getUsername());
            queryWrapper.ne(User::getId, id);
            Long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new IllegalArgumentException("用户名已存在: " + request.getUsername());
            }
            targetUser.setUsername(request.getUsername());
        }
        
        if (request.getStatus() != null) {
            targetUser.setStatus(request.getStatus());
        }
        if (request.getRealName() != null) {
            targetUser.setRealName(request.getRealName());
        }
        if (request.getEmail() != null) {
            targetUser.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            targetUser.setPhone(request.getPhone());
        }
        
        targetUser.setUpdatedAt(LocalDateTime.now());
        targetUser.setUpdatedBy(currentUser.getUsername());
        
        // 更新到数据库
        int result = userMapper.updateById(targetUser);
        if (result <= 0) {
            throw new RuntimeException("更新成员失败");
        }
        
        // 记录操作日志
        operationLogService.log(
                currentUser.getId(),
                currentUser.getUsername(),
                "MEMBER_UPDATE",
                "更新成员: " + targetUser.getUsername(),
                "USER",
                targetUser.getId().toString(),
                targetUser.getUsername(),
                1,
                null
        );
        
        log.info("更新成员成功: id={}, username={}", targetUser.getId(), targetUser.getUsername());
        return convertToDTO(targetUser);
    }
    
    /**
     * 删除成员
     * 
     * @param id 成员ID
     * @param currentUser 当前用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteMember(Long id, User currentUser) {
        log.info("删除成员: id={}, operator={}", id, currentUser.getUsername());
        
        // 查询目标用户
        User targetUser = userMapper.selectById(id);
        if (targetUser == null) {
            throw new IllegalArgumentException("用户不存在: " + id);
        }
        
        // 验证权限
        if (!permissionService.canDeleteMember(currentUser, targetUser)) {
            if (currentUser.getId().equals(targetUser.getId())) {
                throw new IllegalStateException("不能删除当前登录账号");
            }
            throw new IllegalStateException("权限不足，无法删除该成员");
        }
        
        String targetUsername = targetUser.getUsername();
        
        // 删除用户
        int result = userMapper.deleteById(id);
        if (result <= 0) {
            throw new RuntimeException("删除成员失败");
        }
        
        // 记录操作日志
        operationLogService.log(
                currentUser.getId(),
                currentUser.getUsername(),
                "MEMBER_DELETE",
                "删除成员: " + targetUsername,
                "USER",
                id.toString(),
                targetUsername,
                1,
                null
        );
        
        log.info("删除成员成功: id={}, username={}", id, targetUsername);
    }
    
    /**
     * 修改成员密码
     * 
     * @param id 成员ID
     * @param newPassword 新密码
     * @param currentUser 当前用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long id, String newPassword, User currentUser) {
        log.info("修改成员密码: id={}, operator={}", id, currentUser.getUsername());
        
        // 查询目标用户
        User targetUser = userMapper.selectById(id);
        if (targetUser == null) {
            throw new IllegalArgumentException("用户不存在: " + id);
        }
        
        // 验证权限：只有超级管理员可以修改他人密码
        if (!currentUser.getId().equals(targetUser.getId()) && 
            !permissionService.isSuperAdmin(currentUser)) {
            throw new IllegalStateException("权限不足，无法修改该成员的密码");
        }
        
        // 加密新密码
        String encodedPassword = passwordService.encodePassword(newPassword);
        
        // 更新密码
        targetUser.setPassword(encodedPassword);
        targetUser.setUpdatedAt(LocalDateTime.now());
        targetUser.setUpdatedBy(currentUser.getUsername());
        
        int result = userMapper.updateById(targetUser);
        if (result <= 0) {
            throw new RuntimeException("修改密码失败");
        }
        
        // 记录操作日志
        operationLogService.log(
                currentUser.getId(),
                currentUser.getUsername(),
                "MEMBER_PASSWORD_CHANGE",
                "修改成员密码: " + targetUser.getUsername(),
                "USER",
                targetUser.getId().toString(),
                targetUser.getUsername(),
                1,
                null
        );
        
        log.info("修改成员密码成功: id={}, username={}", targetUser.getId(), targetUser.getUsername());
    }
    
    /**
     * 修改成员状态
     * 
     * @param id 成员ID
     * @param status 新状态
     * @param currentUser 当前用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status, User currentUser) {
        log.info("修改成员状态: id={}, status={}, operator={}", id, status, currentUser.getUsername());
        
        // 查询目标用户
        User targetUser = userMapper.selectById(id);
        if (targetUser == null) {
            throw new IllegalArgumentException("用户不存在: " + id);
        }
        
        // 验证权限：只有超级管理员可以修改状态
        if (!permissionService.isSuperAdmin(currentUser)) {
            throw new IllegalStateException("权限不足，只有超级管理员可以修改成员状态");
        }
        
        // 不能禁用自己
        if (currentUser.getId().equals(targetUser.getId()) && status == 0) {
            throw new IllegalStateException("不能禁用当前登录账号");
        }
        
        // 更新状态
        targetUser.setStatus(status);
        targetUser.setUpdatedAt(LocalDateTime.now());
        targetUser.setUpdatedBy(currentUser.getUsername());
        
        int result = userMapper.updateById(targetUser);
        if (result <= 0) {
            throw new RuntimeException("修改状态失败");
        }
        
        // 记录操作日志
        String statusDesc = status == 1 ? "启用" : "禁用";
        operationLogService.log(
                currentUser.getId(),
                currentUser.getUsername(),
                "MEMBER_STATUS_CHANGE",
                statusDesc + "成员: " + targetUser.getUsername(),
                "USER",
                targetUser.getId().toString(),
                targetUser.getUsername(),
                1,
                null
        );
        
        log.info("修改成员状态成功: id={}, username={}, status={}", 
                targetUser.getId(), targetUser.getUsername(), status);
    }
    
    /**
     * 根据ID获取成员
     * 
     * @param id 成员ID
     * @return 成员DTO
     */
    public UserDTO getMemberById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在: " + id);
        }
        return convertToDTO(user);
    }
    
    /**
     * 根据用户名获取用户
     * 
     * @param username 用户名
     * @return 用户实体
     */
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return userMapper.selectOne(queryWrapper);
    }
    
    /**
     * 将User实体转换为UserDTO
     * 
     * @param user 用户实体
     * @return 用户DTO
     */
    private UserDTO convertToDTO(User user) {
        if (user == null) {
            return null;
        }
        
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        // 密码字段不复制，确保安全
        return dto;
    }
}
