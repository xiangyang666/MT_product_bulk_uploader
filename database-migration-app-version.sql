-- ============================================
-- 数据库迁移：应用版本管理表
-- 用途：支持上传和管理桌面应用安装包（exe/dmg）
-- 日期：2026-02-17
-- ============================================

USE `meituan_product`;

-- 创建应用版本管理表
CREATE TABLE IF NOT EXISTS `app_version` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `version` VARCHAR(20) NOT NULL COMMENT '版本号 (e.g., 1.0.0)',
  `platform` VARCHAR(20) NOT NULL COMMENT '平台: Windows, macOS',
  `file_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `file_size` BIGINT NOT NULL COMMENT '文件大小（字节）',
  `file_path` VARCHAR(500) NOT NULL COMMENT 'MinIO存储路径',
  `is_latest` TINYINT DEFAULT 0 COMMENT '是否最新版本: 0-否, 1-是',
  `download_count` INT DEFAULT 0 COMMENT '下载次数',
  `release_notes` TEXT COMMENT '版本发布说明',
  `uploaded_by` BIGINT COMMENT '上传者用户ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_version_platform` (`version`, `platform`),
  INDEX `idx_platform_latest` (`platform`, `is_latest`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用版本管理表';

-- 验证表是否创建成功
SELECT 
    TABLE_NAME,
    TABLE_COMMENT,
    TABLE_ROWS
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'meituan_product'
  AND TABLE_NAME = 'app_version';

-- 显示表结构
SHOW COLUMNS FROM `app_version`;

SELECT '✓ app_version 表创建完成' AS status;
