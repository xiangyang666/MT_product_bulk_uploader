package com.meituan.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 */
@Data
@TableName("operation_log")
public class OperationLog {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 操作用户ID
     */
    private Long userId;
    
    /**
     * 操作用户名
     */
    private String username;
    
    /**
     * 操作类型：IMPORT-导入，UPLOAD-上传，DELETE-删除，GENERATE-生成模板，
     * MEMBER_CREATE-创建成员，MEMBER_UPDATE-更新成员，MEMBER_DELETE-删除成员，
     * MEMBER_PASSWORD_CHANGE-修改密码，MEMBER_STATUS_CHANGE-修改状态
     */
    private String operationType;
    
    /**
     * 操作描述
     */
    private String operationDesc;
    
    /**
     * 目标类型：PRODUCT-商品，USER-用户
     */
    private String targetType;
    
    /**
     * 目标ID
     */
    private String targetId;
    
    /**
     * 目标用户名（用于成员管理操作）
     */
    private String targetUsername;
    
    /**
     * 结果：0-失败，1-成功
     */
    private Integer result;
    
    /**
     * 错误信息
     */
    private String errorMsg;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 耗时（毫秒）
     */
    private Integer duration;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
