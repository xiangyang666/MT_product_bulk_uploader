package com.meituan.product.service;

import com.meituan.product.entity.OperationLog;
import com.meituan.product.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {
    
    private final OperationLogMapper operationLogMapper;
    
    /**
     * 获取最近操作记录
     * 
     * @param merchantId 商家ID
     * @param operationType 操作类型（可选）
     * @param limit 限制数量
     * @return 操作日志列表
     */
    public List<OperationLog> getRecentOperations(Long merchantId, String operationType, int limit) {
        log.info("查询最近操作记录，商家ID：{}，操作类型：{}，限制数量：{}", 
                merchantId, operationType, limit);
        
        if (merchantId == null) {
            throw new IllegalArgumentException("商家ID不能为空");
        }
        
        if (limit <= 0) {
            throw new IllegalArgumentException("限制数量必须大于0");
        }
        
        List<OperationLog> logs = operationLogMapper.selectRecent(merchantId, operationType, limit);
        
        log.info("查询到{}条操作记录", logs.size());
        
        return logs;
    }
    
    /**
     * 记录操作日志
     * 
     * @param operationLog 操作日志
     * @return 插入的记录数
     */
    public int saveLog(OperationLog operationLog) {
        log.info("保存操作日志：{}", operationLog);
        return operationLogMapper.insert(operationLog);
    }
    
    /**
     * 记录操作日志（简化方法，用于成员管理）
     * 
     * @param userId 操作用户ID
     * @param username 操作用户名
     * @param operationType 操作类型
     * @param operationDesc 操作描述
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param targetUsername 目标用户名
     * @param result 结果（0-失败，1-成功）
     * @param errorMsg 错误信息
     */
    public void log(Long userId, String username, String operationType, String operationDesc,
                   String targetType, String targetId, String targetUsername, 
                   Integer result, String errorMsg) {
        try {
            OperationLog log = new OperationLog();
            log.setUserId(userId);
            log.setUsername(username);
            log.setOperationType(operationType);
            log.setOperationDesc(operationDesc);
            log.setTargetType(targetType);
            log.setTargetId(targetId);
            // 注意：需要确保OperationLog实体有targetUsername字段
            // 如果没有，可以将其存储在operationDesc中
            log.setResult(result);
            log.setErrorMsg(errorMsg);
            log.setCreatedAt(LocalDateTime.now());
            
            operationLogMapper.insert(log);
        } catch (Exception e) {
            // 日志记录失败不应影响主业务
            log.error("记录操作日志失败", e);
        }
    }
}
