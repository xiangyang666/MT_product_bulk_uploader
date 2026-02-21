USE `meituan_product`;

-- 添加 target_username 字段
ALTER TABLE `operation_log` 
ADD COLUMN `target_username` VARCHAR(50) COMMENT '目标用户名（用于成员管理操作）' AFTER `target_id`;

-- 验证
SELECT '✓ target_username 字段添加完成' AS status;
