-- 创建开发者工具密码配置表
-- 用于存储 admin-plus 设置的开发者工具访问密码

-- 检查表是否存在
SELECT COUNT(*) INTO @table_exists 
FROM information_schema.tables 
WHERE table_schema = DATABASE() 
AND table_name = 'dev_tools_config';

-- 如果表不存在，则创建
SET @create_table = IF(@table_exists = 0,
    'CREATE TABLE `dev_tools_config` (
        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT ''主键ID'',
        `config_key` VARCHAR(100) NOT NULL COMMENT ''配置键'',
        `config_value` VARCHAR(500) NOT NULL COMMENT ''配置值（加密存储）'',
        `description` VARCHAR(200) DEFAULT NULL COMMENT ''配置描述'',
        `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
        `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'',
        `created_by` VARCHAR(50) DEFAULT NULL COMMENT ''创建人'',
        `updated_by` VARCHAR(50) DEFAULT NULL COMMENT ''更新人'',
        PRIMARY KEY (`id`),
        UNIQUE KEY `uk_config_key` (`config_key`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT=''开发者工具配置表'';',
    'SELECT ''Table dev_tools_config already exists'' AS message;'
);

PREPARE stmt FROM @create_table;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 插入默认配置（如果不存在）
INSERT IGNORE INTO `dev_tools_config` (`config_key`, `config_value`, `description`, `created_by`, `updated_by`)
VALUES ('dev_tools_password', '', '开发者工具访问密码（BCrypt加密）', 'system', 'system');

-- 验证
SELECT 
    id,
    config_key,
    description,
    CASE 
        WHEN config_value = '' THEN '未设置'
        ELSE '已设置'
    END AS password_status,
    created_at,
    updated_at
FROM dev_tools_config
WHERE config_key = 'dev_tools_password';

SELECT '开发者工具密码配置表创建完成' AS result;
