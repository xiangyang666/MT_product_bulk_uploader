-- 美团商品批量上传管理工具 - 安全重新初始化脚本
-- 创建时间: 2026-02-16
-- 说明: 此脚本可以安全地重复执行，会先删除已存在的对象

-- ============================================
-- 1. 使用数据库
-- ============================================
USE `meituan_product`;

-- ============================================
-- 2. 删除已存在的存储过程
-- ============================================
DROP PROCEDURE IF EXISTS `sp_batch_update_product_status`;
DROP PROCEDURE IF EXISTS `sp_clear_merchant_products`;

-- ============================================
-- 3. 删除已存在的视图
-- ============================================
DROP VIEW IF EXISTS `v_product_stats`;

-- ============================================
-- 4. 重新创建商品表
-- ============================================
DROP TABLE IF EXISTS `product`;

CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `product_name` VARCHAR(255) NOT NULL COMMENT '商品名称',
  `category_id` VARCHAR(20) NOT NULL COMMENT '类目ID（10位数字）',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格（元）',
  `stock` INT DEFAULT 0 COMMENT '库存数量',
  `description` TEXT COMMENT '商品描述',
  `image_url` VARCHAR(500) COMMENT '商品图片URL',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待上传，1-已上传，2-上传失败',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_merchant_id` (`merchant_id`),
  INDEX `idx_category_id` (`category_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ============================================
-- 5. 重新创建商家表
-- ============================================
DROP TABLE IF EXISTS `merchant`;

CREATE TABLE `merchant` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_name` VARCHAR(100) NOT NULL COMMENT '商家名称',
  `contact_name` VARCHAR(50) COMMENT '联系人姓名',
  `contact_phone` VARCHAR(20) COMMENT '联系电话',
  `email` VARCHAR(100) COMMENT '邮箱',
  `meituan_merchant_id` VARCHAR(50) COMMENT '美团商家ID',
  `access_token` VARCHAR(500) COMMENT '美团访问令牌',
  `token_expires_at` DATETIME COMMENT '令牌过期时间',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meituan_merchant_id` (`meituan_merchant_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家表';

-- ============================================
-- 6. 重新创建用户表
-- ============================================
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
  `real_name` VARCHAR(50) COMMENT '真实姓名',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `merchant_id` BIGINT COMMENT '关联商家ID',
  `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色：ADMIN-管理员，USER-普通用户',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `last_login_at` DATETIME COMMENT '最后登录时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  INDEX `idx_merchant_id` (`merchant_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 7. 重新创建操作日志表
-- ============================================
DROP TABLE IF EXISTS `operation_log`;

CREATE TABLE `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT COMMENT '操作用户ID',
  `username` VARCHAR(50) COMMENT '操作用户名',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型：IMPORT-导入，UPLOAD-上传，DELETE-删除，GENERATE-生成模板',
  `operation_desc` VARCHAR(500) COMMENT '操作描述',
  `target_type` VARCHAR(50) COMMENT '目标类型：PRODUCT-商品',
  `target_id` VARCHAR(100) COMMENT '目标ID',
  `result` TINYINT DEFAULT 1 COMMENT '结果：0-失败，1-成功',
  `error_msg` TEXT COMMENT '错误信息',
  `ip_address` VARCHAR(50) COMMENT 'IP地址',
  `duration` INT COMMENT '耗时（毫秒）',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_operation_type` (`operation_type`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ============================================
-- 8. 插入测试数据
-- ============================================

-- 插入测试商家
INSERT INTO `merchant` (`id`, `merchant_name`, `contact_name`, `contact_phone`, `email`, `meituan_merchant_id`, `status`) 
VALUES (1, '测试商家', '张三', '13800138000', 'test@example.com', 'MT001', 1);

-- 插入测试用户（密码：123456）
INSERT INTO `user` (`id`, `username`, `password`, `real_name`, `email`, `merchant_id`, `role`, `status`) 
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 'admin@example.com', 1, 'ADMIN', 1);

-- 插入测试商品数据
INSERT INTO `product` (`merchant_id`, `product_name`, `category_id`, `price`, `stock`, `description`, `image_url`, `status`) VALUES
(1, '苹果 iPhone 15 Pro 256GB 深空黑色', '2000010001', 7999.00, 100, '全新苹果iPhone 15 Pro，A17 Pro芯片，钛金属设计', 'https://example.com/iphone15.jpg', 0),
(1, '华为 Mate 60 Pro 12GB+512GB 雅川青', '2000010001', 6999.00, 80, '华为Mate 60 Pro，卫星通信，鸿蒙4.0', 'https://example.com/mate60.jpg', 0),
(1, '小米14 Ultra 16GB+512GB 钛金属', '2000010001', 6499.00, 120, '小米14 Ultra，徕卡光学镜头，骁龙8 Gen3', 'https://example.com/mi14.jpg', 0),
(1, 'OPPO Find X7 Ultra 16GB+512GB 大漠银月', '2000010001', 5999.00, 90, 'OPPO Find X7 Ultra，哈苏影像，天玑9300', 'https://example.com/findx7.jpg', 0),
(1, 'vivo X100 Pro 16GB+512GB 落日橙', '2000010001', 5499.00, 110, 'vivo X100 Pro，蔡司光学，天玑9300', 'https://example.com/vivox100.jpg', 0);

-- ============================================
-- 9. 创建视图
-- ============================================

CREATE VIEW `v_product_stats` AS
SELECT 
  merchant_id,
  COUNT(*) AS total_count,
  SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) AS pending_count,
  SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS uploaded_count,
  SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS failed_count,
  SUM(stock) AS total_stock,
  AVG(price) AS avg_price,
  MIN(price) AS min_price,
  MAX(price) AS max_price
FROM product
GROUP BY merchant_id;

-- ============================================
-- 10. 创建存储过程
-- ============================================

DELIMITER $$

CREATE PROCEDURE `sp_batch_update_product_status`(
  IN p_merchant_id BIGINT,
  IN p_old_status TINYINT,
  IN p_new_status TINYINT
)
BEGIN
  UPDATE product 
  SET status = p_new_status, updated_at = NOW()
  WHERE merchant_id = p_merchant_id AND status = p_old_status;
  
  SELECT ROW_COUNT() AS affected_rows;
END$$

CREATE PROCEDURE `sp_clear_merchant_products`(
  IN p_merchant_id BIGINT
)
BEGIN
  DECLARE v_count INT;
  
  SELECT COUNT(*) INTO v_count FROM product WHERE merchant_id = p_merchant_id;
  
  DELETE FROM product WHERE merchant_id = p_merchant_id;
  
  SELECT v_count AS deleted_count;
END$$

DELIMITER ;

-- ============================================
-- 11. 验证
-- ============================================

SELECT '数据库重新初始化完成！' AS message;
SELECT COUNT(*) AS product_count FROM product;
SELECT COUNT(*) AS merchant_count FROM merchant;
SELECT COUNT(*) AS user_count FROM user;
