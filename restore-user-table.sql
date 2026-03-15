-- 恢复user表并只创建admin-plus用户
-- 密码：123456

USE meituan_management;

-- 删除user表（如果存在）
DROP TABLE IF EXISTS `user`;

-- 重新创建user表
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
  `real_name` VARCHAR(50) COMMENT '真实姓名',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `merchant_id` BIGINT COMMENT '关联商家ID',
  `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色：SUPER_ADMIN-超级管理员，ADMIN-管理员，USER-普通用户',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `last_login_at` DATETIME COMMENT '最后登录时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(50) COMMENT '创建人',
  `updated_by` VARCHAR(50) COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  INDEX `idx_merchant_id` (`merchant_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入admin-plus用户
-- 用户名: admin-plus
-- 密码: 123456 (BCrypt加密)
-- 角色: SUPER_ADMIN
INSERT INTO `user` (`id`, `username`, `password`, `real_name`, `email`, `merchant_id`, `role`, `status`, `created_by`, `updated_by`) 
VALUES (
  1, 
  'admin-plus', 
  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 
  '超级管理员', 
  'admin-plus@example.com', 
  1, 
  'SUPER_ADMIN', 
  1,
  'system',
  'system'
);

-- 验证结果
SELECT id, username, real_name, email, role, status, created_at 
FROM `user`;

-- 显示成功信息
SELECT 'user表已成功恢复，admin-plus用户已创建' AS message;
SELECT '用户名: admin-plus' AS info;
SELECT '密码: 123456' AS info;
SELECT '角色: SUPER_ADMIN' AS info;
