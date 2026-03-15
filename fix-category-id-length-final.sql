-- ============================================
-- 修复 category_id 字段长度问题
-- 创建时间: 2026-03-03
-- 说明: 解决 "Data too long for column 'category_id'" 错误
-- ============================================

USE meituan_product;

-- 1. 查看当前字段定义
SELECT
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'meituan_product'
  AND TABLE_NAME = 't_product'
  AND COLUMN_NAME = 'category_id';

-- 2. 修改 category_id 字段长度（扩大到 VARCHAR(200)，足够容纳各类格式）
ALTER TABLE t_product
MODIFY COLUMN category_id VARCHAR(200) NOT NULL COMMENT '类目ID';

-- 3. 同时检查并修复其他可能超长的字段
-- 合规状态（可能包含较长文本）
ALTER TABLE t_product MODIFY COLUMN compliance_status VARCHAR(100) DEFAULT '合规' COMMENT '合规状态';

-- 售卖状态
ALTER TABLE t_product MODIFY COLUMN sale_status VARCHAR(50) DEFAULT '在售' COMMENT '售卖状态';

-- 发货模式
ALTER TABLE t_product MODIFY COLUMN delivery_mode VARCHAR(50) DEFAULT '即时配送' COMMENT '发货模式';

-- 审核状态
ALTER TABLE t_product MODIFY COLUMN audit_status VARCHAR(50) DEFAULT '待审核' COMMENT '审核状态';

-- 卖点展示期
ALTER TABLE t_product MODIFY COLUMN selling_point_period VARCHAR(200) COMMENT '卖点展示期';

-- 预售的可配送时间
ALTER TABLE t_product MODIFY COLUMN presale_delivery_time VARCHAR(500) COMMENT '预售的可配送时间';

-- 可售时间
ALTER TABLE t_product MODIFY COLUMN available_time VARCHAR(500) COMMENT '可售时间';

-- 商品属性（可能包含较长JSON）
ALTER TABLE t_product MODIFY COLUMN product_attributes TEXT COMMENT '商品属性(JSON格式)';

-- 4. 验证修改结果
SELECT
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'meituan_product'
  AND TABLE_NAME = 't_product'
  AND COLUMN_NAME IN (
    'category_id',
    'compliance_status',
    'sale_status',
    'delivery_mode',
    'audit_status',
    'selling_point_period',
    'presale_delivery_time',
    'available_time'
  )
ORDER BY ORDINAL_POSITION;

SELECT '✅ 字段长度修复完成' AS message;
SELECT '⚠️ 请重启后端服务以使更改生效' AS reminder;