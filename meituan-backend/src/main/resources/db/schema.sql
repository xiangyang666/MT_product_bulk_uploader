-- 美团商品批量上传管理工具 - 数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS meituan_product 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE meituan_product;

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    email VARCHAR(100) COMMENT '邮箱',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 商品表
CREATE TABLE IF NOT EXISTS t_product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    merchant_id BIGINT NOT NULL COMMENT '商家ID',
    product_name VARCHAR(255) NOT NULL COMMENT '商品名称',
    category_id VARCHAR(50) NOT NULL COMMENT '类目ID',
    price DECIMAL(10, 2) NOT NULL COMMENT '价格',
    stock INT DEFAULT 0 COMMENT '库存',
    description TEXT COMMENT '商品描述',
    image_url VARCHAR(500) COMMENT '商品图片URL',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待上传，1-已上传，2-上传失败',
    meituan_product_id VARCHAR(100) COMMENT '美团商品ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS t_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    merchant_id BIGINT NOT NULL COMMENT '商家ID',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型：IMPORT/GENERATE/UPLOAD/CLEAR',
    operation_detail TEXT COMMENT '操作详情',
    success_count INT DEFAULT 0 COMMENT '成功数量',
    failed_count INT DEFAULT 0 COMMENT '失败数量',
    duration BIGINT COMMENT '耗时（毫秒）',
    status TINYINT DEFAULT 0 COMMENT '状态：0-进行中，1-成功，2-失败',
    error_message TEXT COMMENT '错误信息',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 商家配置表
CREATE TABLE IF NOT EXISTS t_merchant_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    merchant_id BIGINT NOT NULL UNIQUE COMMENT '商家ID',
    merchant_name VARCHAR(255) NOT NULL COMMENT '商家名称',
    meituan_app_key VARCHAR(255) COMMENT '美团AppKey',
    meituan_app_secret VARCHAR(255) COMMENT '美团AppSecret',
    access_token VARCHAR(500) COMMENT '访问令牌',
    token_expire_time DATETIME COMMENT '令牌过期时间',
    template_config JSON COMMENT '模板配置',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家配置表';

-- 插入测试数据（可选）
INSERT INTO t_merchant_config (merchant_id, merchant_name, meituan_app_key, meituan_app_secret) 
VALUES (1, '测试商家', 'test_app_key', 'test_app_secret')
ON DUPLICATE KEY UPDATE merchant_name = '测试商家';
