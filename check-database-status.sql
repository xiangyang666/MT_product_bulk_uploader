-- 检查数据库状态

USE meituan_product;

-- 查看所有表
SHOW TABLES;

-- 查看每个表的记录数
SELECT 'product' AS table_name, COUNT(*) AS record_count FROM product
UNION ALL
SELECT 'merchant', COUNT(*) FROM merchant
UNION ALL
SELECT 'user', COUNT(*) FROM user
UNION ALL
SELECT 'operation_log', COUNT(*) FROM operation_log;
