-- 仅创建数据库（最简单版本）
-- 如果数据库已存在会报错，但不影响

CREATE DATABASE `meituan_product` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

-- 验证
SHOW DATABASES LIKE 'meituan%';

SELECT '数据库创建完成！请执行 database-reinit-safe.sql 来创建表' AS message;
