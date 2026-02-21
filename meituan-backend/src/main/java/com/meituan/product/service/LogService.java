package com.meituan.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.meituan.product.entity.OperationLog;
import com.meituan.product.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {
    
    private final OperationLogMapper operationLogMapper;
    
    /**
     * 查询操作日志（支持过滤、搜索和分页）
     * 
     * @param userId 用户ID
     * @param operationType 操作类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param keyword 关键词（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    public Map<String, Object> queryLogs(Long userId, String operationType,
                                        LocalDateTime startTime, LocalDateTime endTime,
                                        String keyword, Integer page, Integer size) {
        
        log.info("查询操作日志，用户ID：{}，操作类型：{}，开始时间：{}，结束时间：{}，关键词：{}，页码：{}，每页大小：{}", 
                userId, operationType, startTime, endTime, keyword, page, size);
        
        // 构建查询条件
        LambdaQueryWrapper<OperationLog> queryWrapper = new LambdaQueryWrapper<>();
        
        // 用户ID过滤（如果提供）
        if (userId != null) {
            queryWrapper.eq(OperationLog::getUserId, userId);
        }
        
        // 操作类型过滤
        if (operationType != null && !operationType.trim().isEmpty()) {
            queryWrapper.eq(OperationLog::getOperationType, operationType);
        }
        
        // 时间范围过滤
        if (startTime != null) {
            queryWrapper.ge(OperationLog::getCreatedAt, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(OperationLog::getCreatedAt, endTime);
        }
        
        // 关键词搜索（在操作描述中搜索）
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like(OperationLog::getOperationDesc, keyword.trim());
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(OperationLog::getCreatedAt);
        
        // 分页查询
        Page<OperationLog> pageParam = new Page<>(page, size);
        IPage<OperationLog> pageResult = operationLogMapper.selectPage(pageParam, queryWrapper);
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        result.put("pages", pageResult.getPages());
        
        log.info("查询操作日志完成，总数：{}，当前页：{}，每页大小：{}", 
                pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
        
        return result;
    }
    
    /**
     * 获取日志详情
     * 
     * @param id 日志ID
     * @return 日志详情
     */
    public OperationLog getLogDetail(Long id) {
        log.info("查询日志详情，日志ID：{}", id);
        return operationLogMapper.selectById(id);
    }
    
    /**
     * 获取最近的操作日志
     * 
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 日志列表
     */
    public List<OperationLog> getRecentLogs(Long userId, Integer limit) {
        log.info("查询最近操作日志，用户ID：{}，数量：{}", userId, limit);
        
        LambdaQueryWrapper<OperationLog> queryWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            queryWrapper.eq(OperationLog::getUserId, userId);
        }
        queryWrapper.orderByDesc(OperationLog::getCreatedAt)
                   .last("LIMIT " + limit);
        
        return operationLogMapper.selectList(queryWrapper);
    }
    
    /**
     * 统计操作日志
     * 
     * @param userId 用户ID
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 统计结果
     */
    public Map<String, Object> getLogStatistics(Long userId, 
                                               LocalDateTime startTime, 
                                               LocalDateTime endTime) {
        log.info("统计操作日志，用户ID：{}，开始时间：{}，结束时间：{}", 
                userId, startTime, endTime);
        
        LambdaQueryWrapper<OperationLog> queryWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            queryWrapper.eq(OperationLog::getUserId, userId);
        }
        
        if (startTime != null) {
            queryWrapper.ge(OperationLog::getCreatedAt, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(OperationLog::getCreatedAt, endTime);
        }
        
        List<OperationLog> logs = operationLogMapper.selectList(queryWrapper);
        
        // 统计各种操作类型的数量
        Map<String, Long> typeCount = new HashMap<>();
        long successCount = 0;
        long failedCount = 0;
        
        for (OperationLog log : logs) {
            // 统计操作类型
            String type = log.getOperationType();
            typeCount.put(type, typeCount.getOrDefault(type, 0L) + 1);
            
            // 统计成功失败
            if (log.getResult() != null) {
                if (log.getResult() == 1) {
                    successCount++;
                } else if (log.getResult() == 0) {
                    failedCount++;
                }
            }
        }
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", logs.size());
        statistics.put("successCount", successCount);
        statistics.put("failedCount", failedCount);
        statistics.put("typeCount", typeCount);
        
        log.info("操作日志统计完成，总数：{}，成功：{}，失败：{}", 
                logs.size(), successCount, failedCount);
        
        return statistics;
    }
}
