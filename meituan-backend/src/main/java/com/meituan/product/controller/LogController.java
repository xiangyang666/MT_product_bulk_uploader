package com.meituan.product.controller;

import com.meituan.product.common.ApiResponse;
import com.meituan.product.entity.OperationLog;
import com.meituan.product.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 操作日志控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {
    
    private final LogService logService;
    
    /**
     * 查询操作日志
     * 
     * @param merchantId 商家ID
     * @param operationType 操作类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param keyword 关键词（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> queryLogs(
            @RequestParam(value = "merchantId", defaultValue = "1") Long merchantId,
            @RequestParam(value = "operationType", required = false) String operationType,
            @RequestParam(value = "startTime", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        
        log.info("接收到查询日志请求，商家ID：{}，操作类型：{}，开始时间：{}，结束时间：{}，关键词：{}，页码：{}，每页大小：{}", 
                merchantId, operationType, startTime, endTime, keyword, page, size);
        
        try {
            Map<String, Object> result = logService.queryLogs(
                    merchantId, operationType, startTime, endTime, keyword, page, size);
            
            return ApiResponse.success(result);
            
        } catch (Exception e) {
            log.error("查询日志失败", e);
            return ApiResponse.error(500, "查询日志失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取日志详情
     * 
     * @param id 日志ID
     * @return 日志详情
     */
    @GetMapping("/{id}")
    public ApiResponse<OperationLog> getLogDetail(@PathVariable Long id) {
        log.info("接收到查询日志详情请求，日志ID：{}", id);
        
        try {
            OperationLog log = logService.getLogDetail(id);
            
            if (log == null) {
                return ApiResponse.error(404, "日志不存在");
            }
            
            return ApiResponse.success(log);
            
        } catch (Exception e) {
            log.error("查询日志详情失败", e);
            return ApiResponse.error(500, "查询日志详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取最近的操作日志
     * 
     * @param merchantId 商家ID
     * @param limit 数量限制
     * @return 日志列表
     */
    @GetMapping("/recent")
    public ApiResponse<List<OperationLog>> getRecentLogs(
            @RequestParam(value = "merchantId", defaultValue = "1") Long merchantId,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        
        log.info("接收到查询最近日志请求，商家ID：{}，数量：{}", merchantId, limit);
        
        try {
            List<OperationLog> logs = logService.getRecentLogs(merchantId, limit);
            return ApiResponse.success(logs);
            
        } catch (Exception e) {
            log.error("查询最近日志失败", e);
            return ApiResponse.error(500, "查询最近日志失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取日志统计
     * 
     * @param merchantId 商家ID
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 统计结果
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getLogStatistics(
            @RequestParam(value = "merchantId", defaultValue = "1") Long merchantId,
            @RequestParam(value = "startTime", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        log.info("接收到查询日志统计请求，商家ID：{}，开始时间：{}，结束时间：{}", 
                merchantId, startTime, endTime);
        
        try {
            Map<String, Object> statistics = logService.getLogStatistics(
                    merchantId, startTime, endTime);
            
            return ApiResponse.success(statistics);
            
        } catch (Exception e) {
            log.error("查询日志统计失败", e);
            return ApiResponse.error(500, "查询日志统计失败：" + e.getMessage());
        }
    }
}
