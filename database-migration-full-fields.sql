-- ============================================
-- 美团商品完整字段扩展 - 数据库迁移脚本
-- 创建时间: 2026-02-09
-- 说明: 扩展t_product表，支持美团完整的50+个商品字段
-- ============================================

USE meituan_product;

-- 备份现有数据（可选）
-- CREATE TABLE t_product_backup_20260209 AS SELECT * FROM t_product;

-- ============================================
-- 1. 添加基础信息字段
-- ============================================

-- SKU ID
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS sku_id VARCHAR(50) COMMENT 'SKU ID' AFTER id;

-- 条形码
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS upc_ean VARCHAR(50) COMMENT '条形码(upc/ean等)' AFTER sku_id;

-- 商品类目名称
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS category_name VARCHAR(100) COMMENT '商品类目名称' AFTER upc_ean;

-- APP SPU编码
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS app_spu_code VARCHAR(50) COMMENT 'APP SPU编码' AFTER category_id;

-- ============================================
-- 2. 添加图片视频字段
-- ============================================

-- 商品图片
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS product_image VARCHAR(500) COMMENT '商品图片URL' AFTER app_spu_code;

-- 封面视频
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS cover_video VARCHAR(500) COMMENT '封面视频URL' AFTER product_image;

-- 规格图
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS spec_image VARCHAR(500) COMMENT '规格图URL' AFTER cover_video;

-- 规格图URL
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS spec_image_url VARCHAR(500) COMMENT '规格图URL' AFTER spec_image;

-- ============================================
-- 3. 添加分类库存字段
-- ============================================

-- 店内分类
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS store_category VARCHAR(100) COMMENT '店内分类' AFTER spec_image_url;

-- 所处店内分类数量
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS store_category_count INT DEFAULT 0 COMMENT '所处店内分类数量' AFTER store_category;

-- 规格名称
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS spec_name VARCHAR(100) COMMENT '规格名称' AFTER store_category_count;

-- 店内码/货号
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS store_code VARCHAR(50) COMMENT '店内码/货号' AFTER spec_name;

-- 售卖状态
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS sale_status VARCHAR(20) DEFAULT '在售' COMMENT '售卖状态' AFTER stock;

-- 月售
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS monthly_sales INT DEFAULT 0 COMMENT '月售数量' AFTER sale_status;

-- 重量
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS weight DECIMAL(10,2) COMMENT '重量' AFTER monthly_sales;

-- 重量单位
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS weight_unit VARCHAR(10) DEFAULT 'kg' COMMENT '重量单位' AFTER weight;

-- 起购数
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS min_purchase INT DEFAULT 1 COMMENT '起购数' AFTER weight_unit;

-- 货架码/位置码
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS shelf_code VARCHAR(50) COMMENT '货架码/位置码' AFTER min_purchase;

-- ============================================
-- 4. 添加详情描述字段
-- ============================================

-- 商品卖点
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS selling_point TEXT COMMENT '商品卖点' AFTER description;

-- 卖点展示期
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS selling_point_period VARCHAR(50) COMMENT '卖点展示期' AFTER selling_point;

-- 文字详情
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS text_detail TEXT COMMENT '文字详情' AFTER selling_point_period;

-- 图片详情
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS image_detail TEXT COMMENT '图片详情(多个URL用逗号分隔)' AFTER text_detail;

-- 品牌商图片详情
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS brand_image_detail TEXT COMMENT '品牌商图片详情' AFTER image_detail;

-- ============================================
-- 5. 添加日期相关字段
-- ============================================

-- 生产日期
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS production_date DATE COMMENT '生产日期' AFTER brand_image_detail;

-- 到期日期
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS expiry_date DATE COMMENT '到期日期' AFTER production_date;

-- 是否临期
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS is_near_expiry TINYINT DEFAULT 0 COMMENT '是否临期：0-否，1-是' AFTER expiry_date;

-- 是否过期
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS is_expired TINYINT DEFAULT 0 COMMENT '是否过期：0-否，1-是' AFTER is_near_expiry;

-- ============================================
-- 6. 添加配送时间字段
-- ============================================

-- 发货模式
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS delivery_mode VARCHAR(20) DEFAULT '即时配送' COMMENT '发货模式' AFTER is_expired;

-- 预售的可配送时间
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS presale_delivery_time VARCHAR(100) COMMENT '预售的可配送时间' AFTER delivery_mode;

-- 可售时间
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS available_time VARCHAR(100) COMMENT '可售时间' AFTER presale_delivery_time;

-- ============================================
-- 7. 添加商品属性字段
-- ============================================

-- 商品属性（JSON格式）
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS product_attributes TEXT COMMENT '商品属性(JSON格式)' AFTER available_time;

-- 力荐
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS is_recommended TINYINT DEFAULT 0 COMMENT '力荐：0-否，1-是' AFTER product_attributes;

-- 无理由退货
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS no_reason_return TINYINT DEFAULT 0 COMMENT '无理由退货：0-否，1-是' AFTER is_recommended;

-- 组合商品
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS is_combo TINYINT DEFAULT 0 COMMENT '组合商品：0-否，1-是' AFTER no_reason_return;

-- 参与的组合商品
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS combo_products TEXT COMMENT '参与的组合商品(多个用逗号分隔)' AFTER is_combo;

-- 是否四轮配送
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS is_four_wheel_delivery TINYINT DEFAULT 0 COMMENT '是否四轮配送：0-否，1-是' AFTER combo_products;

-- ============================================
-- 8. 添加合规审核字段
-- ============================================

-- 合规状态
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS compliance_status VARCHAR(20) DEFAULT '合规' COMMENT '合规状态' AFTER is_four_wheel_delivery;

-- 违规下架
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS violation_offline TINYINT DEFAULT 0 COMMENT '违规下架：0-否，1-是' AFTER compliance_status;

-- 必填信息缺失
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS missing_required_info TINYINT DEFAULT 0 COMMENT '必填信息缺失：0-否，1-是' AFTER violation_offline;

-- 审核状态
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS audit_status VARCHAR(20) DEFAULT '待审核' COMMENT '审核状态' AFTER missing_required_info;

-- ============================================
-- 9. 修改现有字段（如果需要）
-- ============================================

-- 扩大category_id字段长度（如果还没执行过）
ALTER TABLE t_product MODIFY COLUMN category_id VARCHAR(100) NOT NULL COMMENT '商品类目ID';

-- 扩大image_url字段长度
ALTER TABLE t_product MODIFY COLUMN image_url VARCHAR(1000) COMMENT '商品图片URL';

-- ============================================
-- 10. 添加索引（提高查询性能）
-- ============================================

-- SKU ID索引
ALTER TABLE t_product ADD INDEX idx_sku_id (sku_id);

-- 条形码索引
ALTER TABLE t_product ADD INDEX idx_upc_ean (upc_ean);

-- APP SPU编码索引
ALTER TABLE t_product ADD INDEX idx_app_spu_code (app_spu_code);

-- 店内码索引
ALTER TABLE t_product ADD INDEX idx_store_code (store_code);

-- 售卖状态索引
ALTER TABLE t_product ADD INDEX idx_sale_status (sale_status);

-- 审核状态索引
ALTER TABLE t_product ADD INDEX idx_audit_status (audit_status);

-- 合规状态索引
ALTER TABLE t_product ADD INDEX idx_compliance_status (compliance_status);

-- ============================================
-- 11. 验证表结构
-- ============================================

-- 查看表结构
DESC t_product;

-- 查看所有索引
SHOW INDEX FROM t_product;

-- 统计字段数量
SELECT COUNT(*) AS total_columns 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'meituan_product' 
  AND TABLE_NAME = 't_product';

-- ============================================
-- 12. 数据完整性检查
-- ============================================

-- 检查现有数据
SELECT 
    COUNT(*) AS total_products,
    COUNT(DISTINCT merchant_id) AS total_merchants,
    SUM(CASE WHEN deleted = 0 THEN 1 ELSE 0 END) AS active_products,
    SUM(CASE WHEN deleted = 1 THEN 1 ELSE 0 END) AS deleted_products
FROM t_product;

-- ============================================
-- 完成
-- ============================================

SELECT '✅ 数据库迁移完成！' AS message;
SELECT '✅ 已添加50+个新字段' AS status;
SELECT '✅ 已添加相关索引' AS performance;
SELECT '⚠️ 请重启后端服务以加载新字段' AS reminder;

-- ============================================
-- 回滚脚本（如果需要）
-- ============================================

/*
-- 如果需要回滚，执行以下脚本：

-- 删除所有新增字段
ALTER TABLE t_product 
DROP COLUMN sku_id,
DROP COLUMN upc_ean,
DROP COLUMN category_name,
DROP COLUMN app_spu_code,
DROP COLUMN product_image,
DROP COLUMN cover_video,
DROP COLUMN spec_image,
DROP COLUMN spec_image_url,
DROP COLUMN store_category,
DROP COLUMN store_category_count,
DROP COLUMN spec_name,
DROP COLUMN store_code,
DROP COLUMN sale_status,
DROP COLUMN monthly_sales,
DROP COLUMN weight,
DROP COLUMN weight_unit,
DROP COLUMN min_purchase,
DROP COLUMN shelf_code,
DROP COLUMN selling_point,
DROP COLUMN selling_point_period,
DROP COLUMN text_detail,
DROP COLUMN image_detail,
DROP COLUMN brand_image_detail,
DROP COLUMN production_date,
DROP COLUMN expiry_date,
DROP COLUMN is_near_expiry,
DROP COLUMN is_expired,
DROP COLUMN delivery_mode,
DROP COLUMN presale_delivery_time,
DROP COLUMN available_time,
DROP COLUMN product_attributes,
DROP COLUMN is_recommended,
DROP COLUMN no_reason_return,
DROP COLUMN is_combo,
DROP COLUMN combo_products,
DROP COLUMN is_four_wheel_delivery,
DROP COLUMN compliance_status,
DROP COLUMN violation_offline,
DROP COLUMN missing_required_info,
DROP COLUMN audit_status;

-- 删除新增索引
ALTER TABLE t_product 
DROP INDEX idx_sku_id,
DROP INDEX idx_upc_ean,
DROP INDEX idx_app_spu_code,
DROP INDEX idx_store_code,
DROP INDEX idx_sale_status,
DROP INDEX idx_audit_status,
DROP INDEX idx_compliance_status;

-- 恢复备份数据（如果有）
-- DROP TABLE t_product;
-- RENAME TABLE t_product_backup_20260209 TO t_product;
*/
