-- 修复类目ID字段长度问题
USE meituan_product;

-- 1. 查看当前字段定义
SHOW CREATE TABLE t_product;

-- 2. 查看 category_id 字段的详细信息
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'meituan_product'
  AND TABLE_NAME = 't_product'
  AND COLUMN_NAME = 'category_id';

-- 3. 修改 category_id 字段长度（从 VARCHAR(20) 改为 VARCHAR(100)）
ALTER TABLE t_product 
MODIFY COLUMN category_id VARCHAR(100) NOT NULL COMMENT '类目ID';

-- 4. 验证修改结果
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'meituan_product'
  AND TABLE_NAME = 't_product'
  AND COLUMN_NAME = 'category_id';

-- 5. 查看表结构
DESC t_product;

SELECT '✅ category_id 字段长度已修改为 VARCHAR(100)' AS message;
