package com.meituan.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公司信息实体类
 */
@Data
@TableName("t_company_info")
public class CompanyInfo {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 公司名称
     */
    private String companyName;
    
    /**
     * 口号
     */
    private String slogan;
    
    /**
     * Logo URL
     */
    private String logo;
    
    /**
     * 介绍段落1
     */
    private String introPara1;
    
    /**
     * 介绍段落2
     */
    private String introPara2;
    
    /**
     * 介绍段落3
     */
    private String introPara3;
    
    /**
     * 愿景
     */
    private String vision;
    
    /**
     * 使命
     */
    private String mission;
    
    /**
     * 价值观
     */
    @TableField("`values`")
    private String values;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 电话
     */
    private String phone;
    
    /**
     * 地址
     */
    private String address;
    
    /**
     * 微信二维码URL
     */
    private String wechatQrcode;
    
    /**
     * ICP备案号
     */
    private String icpNumber;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
