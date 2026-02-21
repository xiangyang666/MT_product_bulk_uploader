-- ============================================
-- 成员管理模块 - 数据库迁移脚本
-- 创建时间: 2026-02-14
-- 版本: v1.0
-- 说明: 扩展user表以支持成员管理和RBAC功能
-- ============================================

USE `meituan_product`;

-- ============================================
-- 1. 备份现有user表（可选，生产环境建议执行）
-- ============================================
-- CREATE TABLE `user_backup_20260214` AS SELECT * FROM `user`;

-- ============================================
-- 2. 修改user表结构
-- ============================================

-- 检查并添加 role 字段（如果不存在）
-- 注意：现有表已有role字段，但需要修改默认值和注释
ALTER TABLE `user` 
MODIFY COLUMN `role` VARCHAR(20) NOT NULL DEFAULT 'USER' 
COMMENT '角色：SUPER_ADMIN-超级管理员，ADMIN-管理员，USER-普通用户';

-- 检查并添加 status 字段（如果不存在）
-- 注意：现有表已有status字段，保持不变

-- 使用存储过程安全添加字段
DELIMITER $

DROP PROCEDURE IF EXISTS add_user_columns$
CREATE PROCEDURE add_user_columns()
BEGIN
  DECLARE created_by_exists INT DEFAULT 0;
  DECLARE updated_by_exists INT DEFAULT 0;
  
  -- 检查 created_by 字段是否存在
  SELECT COUNT(*) INTO created_by_exists
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'user'
    AND column_name = 'created_by';
  
  -- 如果不存在则添加
  IF created_by_exists = 0 THEN
    ALTER TABLE `user` 
    ADD COLUMN `created_by` VARCHAR(50) NULL COMMENT '创建者用户名' 
    AFTER `updated_at`;
  END IF;
  
  -- 检查 updated_by 字段是否存在
  SELECT COUNT(*) INTO updated_by_exists
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'user'
    AND column_name = 'updated_by';
  
  -- 如果不存在则添加
  IF updated_by_exists = 0 THEN
    ALTER TABLE `user` 
    ADD COLUMN `updated_by` VARCHAR(50) NULL COMMENT '更新者用户名' 
    AFTER `created_by`;
  END IF;
END$

DELIMITER ;

-- 执行存储过程
CALL add_user_columns();

-- 删除存储过程
DROP PROCEDURE IF EXISTS add_user_columns;

-- ============================================
-- 3. 创建索引
-- ============================================

-- 为 role 字段创建索引（如果不存在）
-- 使用存储过程来安全创建索引
DELIMITER $$

DROP PROCEDURE IF EXISTS create_index_if_not_exists$$
CREATE PROCEDURE create_index_if_not_exists()
BEGIN
  DECLARE index_exists INT DEFAULT 0;
  
  -- 检查 idx_role 索引是否存在
  SELECT COUNT(*) INTO index_exists
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'user'
    AND index_name = 'idx_role';
  
  -- 如果不存在则创建
  IF index_exists = 0 THEN
    ALTER TABLE `user` ADD INDEX `idx_role` (`role`);
  END IF;
END$$

DELIMITER ;

-- 执行存储过程
CALL create_index_if_not_exists();

-- 删除存储过程
DROP PROCEDURE IF EXISTS create_index_if_not_exists;

-- 为 status 字段创建索引（如果已存在则跳过）
-- 注意：如果status字段已有索引，此步骤会被跳过

-- 为 username 字段创建唯一索引（如果已存在则跳过）
-- 注意：如果username字段已有唯一索引，此步骤会被跳过

-- ============================================
-- 4. 更新现有数据
-- ============================================

-- 将现有的 ADMIN 角色更新为 SUPER_ADMIN（如果需要）
-- UPDATE `user` SET `role` = 'SUPER_ADMIN' WHERE `role` = 'ADMIN';

-- 为现有用户设置默认创建者（可选）
UPDATE `user` SET `created_by` = 'system' WHERE `created_by` IS NULL;

-- ============================================
-- 5. 扩展 operation_log 表以支持成员管理操作
-- ============================================

-- 添加成员管理相关的操作类型注释
ALTER TABLE `operation_log` 
MODIFY COLUMN `operation_type` VARCHAR(50) NOT NULL 
COMMENT '操作类型：IMPORT-导入，UPLOAD-上传，DELETE-删除，GENERATE-生成模板，MEMBER_CREATE-创建成员，MEMBER_UPDATE-更新成员，MEMBER_DELETE-删除成员，MEMBER_PASSWORD_CHANGE-修改密码，MEMBER_STATUS_CHANGE-修改状态';

-- 使用存储过程安全添加字段
DELIMITER $

DROP PROCEDURE IF EXISTS add_operation_log_columns$
CREATE PROCEDURE add_operation_log_columns()
BEGIN
  DECLARE target_username_exists INT DEFAULT 0;
  
  -- 检查 target_username 字段是否存在
  SELECT COUNT(*) INTO target_username_exists
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'operation_log'
    AND column_name = 'target_username';
  
  -- 如果不存在则添加
  IF target_username_exists = 0 THEN
    ALTER TABLE `operation_log` 
    ADD COLUMN `target_username` VARCHAR(50) NULL COMMENT '目标用户名（用于成员管理操作）' 
    AFTER `target_id`;
  END IF;
END$

DELIMITER ;

-- 执行存储过程
CALL add_operation_log_columns();

-- 删除存储过程
DROP PROCEDURE IF EXISTS add_operation_log_columns;

-- ============================================
-- 6. 创建默认超级管理员（如果不存在）
-- ============================================

-- 使用存储过程安全创建默认管理员
DELIMITER $

DROP PROCEDURE IF EXISTS create_default_admin$
CREATE PROCEDURE create_default_admin()
BEGIN
  DECLARE admin_exists INT DEFAULT 0;
  
  -- 检查是否已存在admin用户
  SELECT COUNT(*) INTO admin_exists
  FROM `user`
  WHERE `username` = 'admin';
  
  -- 如果不存在则创建
  -- 默认用户名: admin
  -- 默认密码: 123456 (BCrypt加密后的值)
  -- 注意：首次登录后应立即修改密码
  IF admin_exists = 0 THEN
    INSERT INTO `user` (
      `username`, 
      `password`, 
      `real_name`, 
      `email`, 
      `role`, 
      `status`, 
      `created_by`,
      `created_at`
    ) VALUES (
      'admin',
      '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
      '系统管理员',
      'admin@example.com',
      'SUPER_ADMIN',
      1,
      'system',
      NOW()
    );
  ELSE
    -- 如果admin用户已存在，确保其角色为SUPER_ADMIN
    UPDATE `user` 
    SET `role` = 'SUPER_ADMIN',
        `status` = 1
    WHERE `username` = 'admin';
  END IF;
END$

DELIMITER ;

-- 执行存储过程
CALL create_default_admin();

-- 删除存储过程
DROP PROCEDURE IF EXISTS create_default_admin;

-- ============================================
-- 7. 创建成员管理相关视图
-- ============================================

-- 成员统计视图
CREATE OR REPLACE VIEW `v_member_stats` AS
SELECT 
  `role`,
  COUNT(*) AS member_count,
  SUM(CASE WHEN `status` = 1 THEN 1 ELSE 0 END) AS active_count,
  SUM(CASE WHEN `status` = 0 THEN 1 ELSE 0 END) AS disabled_count,
  MAX(`created_at`) AS last_created_at
FROM `user`
GROUP BY `role`;

-- 成员操作日志视图
CREATE OR REPLACE VIEW `v_member_operation_log` AS
SELECT 
  ol.`id`,
  ol.`username` AS operator,
  ol.`operation_type`,
  ol.`operation_desc`,
  ol.`target_username`,
  ol.`result`,
  ol.`error_msg`,
  ol.`created_at`
FROM `operation_log` ol
WHERE ol.`operation_type` IN (
  'MEMBER_CREATE', 
  'MEMBER_UPDATE', 
  'MEMBER_DELETE', 
  'MEMBER_PASSWORD_CHANGE', 
  'MEMBER_STATUS_CHANGE'
)
ORDER BY ol.`created_at` DESC;

-- ============================================
-- 8. 验证迁移结果
-- ============================================

-- 查看user表结构
SHOW CREATE TABLE `user`;

-- 查看索引
SHOW INDEX FROM `user`;

-- 查看成员统计
SELECT * FROM `v_member_stats`;

-- 查看超级管理员账号
SELECT 
  `id`, 
  `username`, 
  `role`, 
  `status`, 
  `created_at`,
  `created_by`
FROM `user` 
WHERE `role` = 'SUPER_ADMIN';

-- ============================================
-- 9. 回滚脚本（如果需要）
-- ============================================

/*
-- 回滚步骤（谨慎使用）：

-- 1. 删除新增的字段
ALTER TABLE `user` DROP COLUMN `created_by`;
ALTER TABLE `user` DROP COLUMN `updated_by`;

-- 2. 删除新增的索引
DROP INDEX `idx_role` ON `user`;

-- 3. 删除新增的字段（operation_log）
ALTER TABLE `operation_log` DROP COLUMN `target_username`;

-- 4. 删除视图
DROP VIEW IF EXISTS `v_member_stats`;
DROP VIEW IF EXISTS `v_member_operation_log`;

-- 5. 恢复备份数据（如果有）
-- DROP TABLE `user`;
-- RENAME TABLE `user_backup_20260214` TO `user`;
*/

-- ============================================
-- 10. 完成
-- ============================================

SELECT '成员管理模块数据库迁移完成！' AS message;
SELECT 
  (SELECT COUNT(*) FROM `user`) AS total_users,
  (SELECT COUNT(*) FROM `user` WHERE `role` = 'SUPER_ADMIN') AS super_admins,
  (SELECT COUNT(*) FROM `user` WHERE `role` = 'ADMIN') AS admins,
  (SELECT COUNT(*) FROM `user` WHERE `role` = 'USER') AS regular_users;

-- ============================================
-- 使用说明
-- ============================================

/*
执行方式：
1. 命令行执行：
   mysql -u root -p meituan_product < database-migration-member-management.sql

2. MySQL客户端执行：
   USE meituan_product;
   SOURCE database-migration-member-management.sql;

3. 使用数据库管理工具（如Navicat、DBeaver）导入执行

注意事项：
1. 执行前请备份数据库
2. 默认超级管理员账号：admin / 123456
3. 首次登录后请立即修改默认密码
4. 生产环境建议修改默认密码的BCrypt值
5. 确保应用程序配置了正确的数据库连接

安全建议：
1. 修改默认管理员密码
2. 定期审查用户权限
3. 启用操作日志审计
4. 使用强密码策略
5. 定期备份数据库

BCrypt密码生成（Java示例）：
String password = "YourPassword";
String encoded = new BCryptPasswordEncoder(10).encode(password);
System.out.println(encoded);
*/
