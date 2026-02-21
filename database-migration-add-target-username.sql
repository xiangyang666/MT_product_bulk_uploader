-- ============================================
-- 数据库迁移：添加 target_username 字段到 operation_log 表
-- 用途：支持成员管理操作日志记录目标用户名
-- 日期：2026-02-17
-- ============================================

USE `meituan_product`;

-- 检查字段是否已存在，如果不存在则添加
SET @dbname = DATABASE();
SET @tablename = 'operation_log';
SET @columnname = 'target_username';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(50) COMMENT ''目标用户名（用于成员管理操作）'' AFTER target_id')
));

PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 验证字段是否添加成功
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'meituan_product'
  AND TABLE_NAME = 'operation_log'
  AND COLUMN_NAME = 'target_username';

SELECT '✓ target_username 字段添加完成' AS status;
