-- ============================================
-- 修复字段长度问题
-- 创建时间: 2026-02-09
-- 说明: 扩大可能超长的字段长度
-- ============================================

USE meituan_product;

-- 1. 扩大 compliance_status 字段长度
ALTER TABLE t_product MODIFY COLUMN compliance_status VARCHAR(100) DEFAULT '合规' COMMENT '合规状态';

-- 2. 扩大其他可能超长的字段
ALTER TABLE t_product MODIFY COLUMN sale_status VARCHAR(50) DEFAULT '在售' COMMENT '售卖状态';
ALTER TABLE t_product MODIFY COLUMN delivery_mode VARCHAR(50) DEFAULT '即时配送' COMMENT '发货模式';
ALTER TABLE t_product MODIFY COLUMN audit_status VARCHAR(50) DEFAULT '待审核' COMMENT '审核状态';

-- 3. 扩大文本字段长度
ALTER TABLE t_product MODIFY COLUMN selling_point_period VARCHAR(100) COMMENT '卖点展示期';
ALTER TABLE t_product MODIFY COLUMN presale_delivery_time VARCHAR(200) COMMENT '预售的可配送时间';
ALTER TABLE t_product MODIFY COLUMN available_time VARCHAR(200) COMMENT '可售时间';

-- 4. 允许 price 字段为 NULL（如果还没执行）
ALTER TABLE t_product MODIFY COLUMN price DECIMAL(10,2) NULL COMMENT '价格';

-- 验证修改
DESC t_product;

-- 查看修改后的字段信息
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'meituan_product'
  AND TABLE_NAME = 't_product'
  AND COLUMN_NAME IN ('compliance_status', 'sale_status', 'delivery_mode', 'audit_status', 'price');

SELECT '✅ 字段长度已扩大' AS message;
