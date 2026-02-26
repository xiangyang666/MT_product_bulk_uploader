-- 数据库迁移脚本：添加模板查询索引
-- 功能：优化模板状态查询性能
-- 日期：2026-02-26

USE meituan_product;

-- 检查索引是否已存在
SELECT COUNT(*) INTO @index_exists
FROM information_schema.statistics
WHERE table_schema = 'meituan_product'
  AND table_name = 'template'
  AND index_name = 'idx_merchant_template_type_time';

-- 如果索引不存在，则创建
SET @sql = IF(@index_exists = 0,
    'CREATE INDEX idx_merchant_template_type_time ON template(merchant_id, template_type, created_time DESC)',
    'SELECT "索引已存在，跳过创建" AS message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 验证索引创建结果
SELECT 
    table_name AS '表名',
    index_name AS '索引名',
    column_name AS '列名',
    seq_in_index AS '列顺序',
    collation AS '排序方式'
FROM information_schema.statistics
WHERE table_schema = 'meituan_product'
  AND table_name = 'template'
  AND index_name = 'idx_merchant_template_type_time'
ORDER BY seq_in_index;

-- 分析索引效果
EXPLAIN SELECT * 
FROM template 
WHERE merchant_id = 1 
  AND template_type = 'MEITUAN' 
ORDER BY created_time DESC 
LIMIT 1;

SELECT '索引创建完成！' AS '状态';
