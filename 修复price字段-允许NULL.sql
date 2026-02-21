-- ============================================
-- 修复 price 字段 - 允许 NULL 或设置默认值
-- 创建时间: 2026-02-09
-- 说明: 解决批量导入时 "Column 'price' cannot be null" 错误
-- ============================================

USE meituan_product;

-- 方案1：允许 price 字段为 NULL（推荐）
-- 这样即使Excel中没有价格，也可以导入，后续可以手动补充
ALTER TABLE t_product MODIFY COLUMN price DECIMAL(10,2) NULL COMMENT '价格';

-- 方案2：设置默认值为 0（备选）
-- 如果你希望所有商品都必须有价格，可以使用这个方案
-- ALTER TABLE t_product MODIFY COLUMN price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '价格';

-- 验证修改
DESC t_product;

-- 查看 price 字段的详细信息
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'meituan_product'
  AND TABLE_NAME = 't_product'
  AND COLUMN_NAME = 'price';

SELECT '✅ price 字段已修改为允许 NULL' AS message;
