package com.meituan.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板实体类
 */
@Data
@TableName("t_template")
public class Template {
    
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
     * 模板名称
     */
    private String templateName;
    
    /**
     * 模板类型：IMPORT/EXPORT/MEITUAN
     */
    private String templateType;
    
    /**
     * MinIO对象名称（文件路径）
     */
    private String filePath;
    
    /**
     * MinIO预签名URL
     */
    private String fileUrl;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
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
     * 模板类型枚举
     */
    public enum TemplateType {
        IMPORT("IMPORT", "导入模板"),
        EXPORT("EXPORT", "导出模板"),
        MEITUAN("MEITUAN", "美团模板");
        
        private final String code;
        private final String description;
        
        TemplateType(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
