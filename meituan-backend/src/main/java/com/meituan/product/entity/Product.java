package com.meituan.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 商品实体类
 */
@Data
@TableName("t_product")
public class Product {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // ============================================
    // 基础信息字段
    // ============================================
    
    /**
     * SKU ID
     */
    private String skuId;
    
    /**
     * 条形码(upc/ean等)
     */
    private String upcEan;
    
    /**
     * 商品类目名称
     */
    private String categoryName;
    
    /**
     * 商家ID
     */
    private Long merchantId;
    
    /**
     * 商品名称
     */
    private String productName;
    
    /**
     * 类目ID
     */
    private String categoryId;
    
    /**
     * APP SPU编码
     */
    private String appSpuCode;
    
    // ============================================
    // 图片视频字段
    // ============================================
    
    /**
     * 商品图片URL
     */
    private String productImage;
    
    /**
     * 封面视频URL
     */
    private String coverVideo;
    
    /**
     * 规格图URL
     */
    private String specImage;
    
    /**
     * 规格图URL
     */
    private String specImageUrl;
    
    /**
     * 商品图片URL（原有字段，保留兼容性）
     */
    private String imageUrl;
    
    // ============================================
    // 分类库存字段
    // ============================================
    
    /**
     * 店内分类
     */
    private String storeCategory;
    
    /**
     * 所处店内分类数量
     */
    private Integer storeCategoryCount;
    
    /**
     * 规格名称
     */
    private String specName;
    
    /**
     * 店内码/货号
     */
    private String storeCode;
    
    /**
     * 价格
     */
    private BigDecimal price;
    
    /**
     * 库存
     */
    private Integer stock;
    
    /**
     * 售卖状态
     */
    private String saleStatus;
    
    /**
     * 月售数量
     */
    private Integer monthlySales;
    
    /**
     * 重量
     */
    private BigDecimal weight;
    
    /**
     * 重量单位
     */
    private String weightUnit;
    
    /**
     * 起购数
     */
    private Integer minPurchase;
    
    /**
     * 货架码/位置码
     */
    private String shelfCode;
    
    // ============================================
    // 详情描述字段
    // ============================================
    
    /**
     * 商品描述
     */
    private String description;
    
    /**
     * 商品卖点
     */
    private String sellingPoint;
    
    /**
     * 卖点展示期
     */
    private String sellingPointPeriod;
    
    /**
     * 文字详情
     */
    private String textDetail;
    
    /**
     * 图片详情(多个URL用逗号分隔)
     */
    private String imageDetail;
    
    /**
     * 品牌商图片详情
     */
    private String brandImageDetail;
    
    // ============================================
    // 日期相关字段
    // ============================================
    
    /**
     * 生产日期
     */
    private LocalDate productionDate;
    
    /**
     * 到期日期
     */
    private LocalDate expiryDate;
    
    /**
     * 是否临期：0-否，1-是
     */
    private Integer isNearExpiry;
    
    /**
     * 是否过期：0-否，1-是
     */
    private Integer isExpired;
    
    // ============================================
    // 配送时间字段
    // ============================================
    
    /**
     * 发货模式
     */
    private String deliveryMode;
    
    /**
     * 预售的可配送时间
     */
    private String presaleDeliveryTime;
    
    /**
     * 可售时间
     */
    private String availableTime;
    
    // ============================================
    // 商品属性字段
    // ============================================
    
    /**
     * 商品属性(JSON格式)
     */
    private String productAttributes;
    
    /**
     * 力荐：0-否，1-是
     */
    private Integer isRecommended;
    
    /**
     * 无理由退货：0-否，1-是
     */
    private Integer noReasonReturn;
    
    /**
     * 组合商品：0-否，1-是
     */
    private Integer isCombo;
    
    /**
     * 参与的组合商品(多个用逗号分隔)
     */
    private String comboProducts;
    
    /**
     * 是否四轮配送：0-否，1-是
     */
    private Integer isFourWheelDelivery;
    
    // ============================================
    // 合规审核字段
    // ============================================
    
    /**
     * 合规状态
     */
    private String complianceStatus;
    
    /**
     * 违规下架：0-否，1-是
     */
    private Integer violationOffline;
    
    /**
     * 必填信息缺失：0-否，1-是
     */
    private Integer missingRequiredInfo;
    
    /**
     * 审核状态
     */
    private String auditStatus;
    
    // ============================================
    // 系统字段
    // ============================================
    
    /**
     * 状态：0-待上传，1-已上传，2-上传失败
     */
    private Integer status;
    
    /**
     * 美团商品ID
     */
    private String meituanProductId;
    
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
     * 商品状态枚举
     */
    public enum Status {
        PENDING(0, "待上传"),
        UPLOADED(1, "已上传"),
        FAILED(2, "上传失败");
        
        private final Integer code;
        private final String description;
        
        Status(Integer code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public Integer getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
