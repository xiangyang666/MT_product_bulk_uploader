-- 为远程数据库的 user 表添加缺失的字段
-- 执行命令: mysql -h 106.55.102.48 -P 3306 -u root -pmysql_G4EcQ6 meituan_product < add-missing-user-columns.sql

USE meituan_product;

-- 添加 created_by 字段
ALTER TABLE `user` 
ADD COLUMN `created_by` VARCHAR(50) COMMENT '创建者用户名' AFTER `updated_at`;

-- 添加 updated_by 字段
ALTER TABLE `user` 
ADD COLUMN `updated_by` VARCHAR(50) COMMENT '更新者用户名' AFTER `created_by`;

-- 验证字段已添加
DESC `user`;

SELECT '字段添加完成！' AS message;
