-- 管理功能数据库迁移脚本
-- 创建时间: 2026-02-09
-- 功能: 添加模板管理表和优化操作日志索引

USE `meituan_product`;

-- ============================================
-- 1. 创建模板表
-- ============================================
DROP TABLE IF EXISTS `t_template`;

CREATE TABLE `t_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `template_name` VARCHAR(255) NOT NULL COMMENT '模板名称',
  `template_type` VARCHAR(50) NOT NULL COMMENT '模板类型：IMPORT-导入模板，EXPORT-导出模板，MEITUAN-美团模板',
  `file_path` VARCHAR(500) NOT NULL COMMENT 'MinIO对象名称',
  `file_url` VARCHAR(1000) COMMENT 'MinIO预签名URL',
  `file_size` BIGINT COMMENT '文件大小（字节）',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  INDEX `idx_merchant_id` (`merchant_id`),
  INDEX `idx_template_type` (`template_type`),
  INDEX `idx_created_time` (`created_time`),
  INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模板表';

-- ============================================
-- 2. 创建商家配置表（如果不存在）
-- ============================================
CREATE TABLE IF NOT EXISTS `t_merchant_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `merchant_name` VARCHAR(100) COMMENT '商家名称',
  `meituan_app_key` VARCHAR(100) COMMENT '美团AppKey',
  `meituan_app_secret` VARCHAR(100) COMMENT '美团AppSecret',
  `access_token` VARCHAR(500) COMMENT '访问令牌',
  `token_expire_time` DATETIME COMMENT '令牌过期时间',
  `template_config` TEXT COMMENT '模板配置（JSON格式）',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_id` (`merchant_id`),
  INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家配置表';

-- ============================================
-- 3. 创建操作日志表（如果不存在）
-- ============================================
CREATE TABLE IF NOT EXISTS `t_operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型：IMPORT/GENERATE/UPLOAD/CLEAR/SETTINGS_UPDATE',
  `operation_detail` VARCHAR(500) COMMENT '操作详情',
  `success_count` INT DEFAULT 0 COMMENT '成功数量',
  `failed_count` INT DEFAULT 0 COMMENT '失败数量',
  `duration` BIGINT COMMENT '耗时（毫秒）',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-进行中，1-成功，2-失败',
  `error_message` TEXT COMMENT '错误信息',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  INDEX `idx_merchant_id` (`merchant_id`),
  INDEX `idx_operation_type` (`operation_type`),
  INDEX `idx_created_time` (`created_time`),
  INDEX `idx_status` (`status`),
  INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ============================================
-- 4. 添加操作日志表的created_time索引（如果不存在）
-- ============================================
-- 检查索引是否存在，如果不存在则创建
SET @index_exists = (
  SELECT COUNT(1) 
  FROM INFORMATION_SCHEMA.STATISTICS 
  WHERE TABLE_SCHEMA = 'meituan_product' 
    AND TABLE_NAME = 't_operation_log' 
    AND INDEX_NAME = 'idx_created_time'
);

SET @sql = IF(@index_exists = 0, 
  'CREATE INDEX idx_created_time ON t_operation_log(created_time)', 
  'SELECT "Index already exists" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- 5. 插入测试数据
-- ============================================

-- 插入默认商家配置（如果不存在）
INSERT INTO `t_merchant_config` (`merchant_id`, `merchant_name`, `meituan_app_key`, `meituan_app_secret`)
SELECT 1, '测试商家', 'test_app_key', 'test_app_secret'
WHERE NOT EXISTS (SELECT 1 FROM `t_merchant_config` WHERE `merchant_id` = 1);

-- 插入测试模板数据
INSERT INTO `t_template` (`merchant_id`, `template_name`, `template_type`, `file_path`, `file_size`) VALUES
(1, '标准商品导入模板', 'IMPORT', 'templates/standard_import_template.xlsx', 15360),
(1, '美团商品上传模板', 'MEITUAN', 'templates/meituan_upload_template.xlsx', 18432),
(1, '商品导出模板', 'EXPORT', 'templates/product_export_template.xlsx', 12288);

-- 插入测试操作日志
INSERT INTO `t_operation_log` (`merchant_id`, `operation_type`, `operation_detail`, `success_count`, `failed_count`, `duration`, `status`) VALUES
(1, 'IMPORT', '导入商品数据：成功5条', 5, 0, 1250, 1),
(1, 'GENERATE', '生成美团上传模板', 1, 0, 850, 1),
(1, 'UPLOAD', '批量上传商品到美团：成功3条，失败2条', 3, 2, 3500, 1),
(1, 'SETTINGS_UPDATE', '更新商家配置', 1, 0, 120, 1);

-- ============================================
-- 6. 验证
-- ============================================

-- 查看新创建的表
SHOW TABLES LIKE 't_%';

-- 查看模板表结构
DESCRIBE t_template;

-- 查看模板数据
SELECT * FROM t_template;

-- 查看操作日志索引
SHOW INDEX FROM t_operation_log;

-- 查看操作日志数据
SELECT * FROM t_operation_log ORDER BY created_time DESC LIMIT 10;

-- 查看商家配置
SELECT * FROM t_merchant_config;

-- ============================================
-- 完成
-- ============================================
SELECT '管理功能数据库迁移完成！' AS message;
SELECT COUNT(*) AS template_count FROM t_template;
SELECT COUNT(*) AS log_count FROM t_operation_log;
SELECT COUNT(*) AS config_count FROM t_merchant_config;
