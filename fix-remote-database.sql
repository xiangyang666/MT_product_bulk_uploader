-- 修复远程数据库 - 一次性执行所有修复
-- 执行命令: mysql -h 106.55.102.48 -P 3306 -u root -pmysql_G4EcQ6 meituan_product < fix-remote-database.sql

USE meituan_product;

-- ============================================
-- 1. 添加缺失的字段
-- ============================================

-- 检查 created_by 字段是否存在
SET @col_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'meituan_product' 
    AND TABLE_NAME = 'user' 
    AND COLUMN_NAME = 'created_by'
);

-- 如果不存在则添加
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `user` ADD COLUMN `created_by` VARCHAR(50) COMMENT ''创建者用户名'' AFTER `updated_at`',
    'SELECT ''created_by 字段已存在'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查 updated_by 字段是否存在
SET @col_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'meituan_product' 
    AND TABLE_NAME = 'user' 
    AND COLUMN_NAME = 'updated_by'
);

-- 如果不存在则添加
SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE `user` ADD COLUMN `updated_by` VARCHAR(50) COMMENT ''更新者用户名'' AFTER `created_by`',
    'SELECT ''updated_by 字段已存在'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- 2. 修复 admin 用户角色
-- ============================================

-- 更新 admin 用户角色为 SUPER_ADMIN
UPDATE user 
SET role = 'SUPER_ADMIN', 
    status = 1,
    updated_at = NOW()
WHERE username = 'admin';

-- ============================================
-- 3. 验证修复结果
-- ============================================

-- 查看表结构
SELECT '=== User 表结构 ===' AS info;
DESC user;

-- 查看 admin 用户信息
SELECT '=== Admin 用户信息 ===' AS info;
SELECT id, username, role, status, created_at, updated_at 
FROM user 
WHERE username = 'admin';

SELECT '✅ 数据库修复完成！' AS message;
