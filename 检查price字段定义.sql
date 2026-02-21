-- 检查 t_product 表中 price 字段的定义
USE meituan_product;

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

-- 如果 price 字段不允许 NULL，修改为允许 NULL 或设置默认值
-- 方案1：允许 NULL
-- ALTER TABLE t_product MODIFY COLUMN price DECIMAL(10,2) NULL COMMENT '价格';

-- 方案2：设置默认值为 0
-- ALTER TABLE t_product MODIFY COLUMN price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '价格';
