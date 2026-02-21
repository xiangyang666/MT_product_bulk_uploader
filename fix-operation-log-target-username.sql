-- 修复 operation_log 表 - 添加 target_username 字段
-- 执行方式：在 MySQL 客户端或 Navicat 中直接运行

USE `meituan_product`;

-- 添加 target_username 字段
ALTER TABLE `operation_log` 
ADD COLUMN `target_username` VARCHAR(50) COMMENT '目标用户名（用于成员管理操作）' AFTER `target_id`;

-- 验证字段
SHOW COLUMNS FROM `operation_log` WHERE Field='target_username';

SELECT '✓ 字段添加成功！' AS result;
