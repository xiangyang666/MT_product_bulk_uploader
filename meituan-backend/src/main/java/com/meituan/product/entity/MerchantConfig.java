package com.meituan.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家配置实体类
 */
@Data
@TableName("t_merchant_config")
public class MerchantConfig {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 商家ID
     */
    private Long merchantId;
    
    /**
     * 商家名称
     */
    private String merchantName;
    
    /**
     * 美团AppKey
     */
    private String meituanAppKey;
    
    /**
     * 美团AppSecret
     */
    private String meituanAppSecret;
    
    /**
     * 访问令牌
     */
    private String accessToken;
    
    /**
     * 令牌过期时间
     */
    private LocalDateTime tokenExpireTime;
    
    /**
     * 模板配置（JSON格式）
     * 注意：此字段有自定义的 getter/setter 来确保 JSON 有效性
     */
    private String templateConfig;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
    
    /**
     * 逻辑删除标记：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
    
    /**
     * 构造函数 - 初始化 templateConfig 为空 JSON 对象
     */
    public MerchantConfig() {
        this.templateConfig = "{}";
    }
    
    /**
     * 自定义 Getter - 确保永远不返回 null
     * 
     * @return 模板配置 JSON 字符串，如果为 null 则返回 "{}"
     */
    public String getTemplateConfig() {
        return templateConfig != null ? templateConfig : "{}";
    }
    
    /**
     * 自定义 Setter - 自动清理 null 和空字符串
     * 将 null 或空字符串转换为有效的空 JSON 对象 "{}"
     * 
     * @param templateConfig 模板配置 JSON 字符串
     */
    public void setTemplateConfig(String templateConfig) {
        this.templateConfig = sanitizeJson(templateConfig);
    }
    
    /**
     * 私有方法 - 清理 JSON 字符串
     * 将 null、空字符串或纯空白字符串转换为 "{}"
     * 
     * @param json 原始 JSON 字符串
     * @return 清理后的 JSON 字符串
     */
    private String sanitizeJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return "{}";
        }
        return json;
    }
}
