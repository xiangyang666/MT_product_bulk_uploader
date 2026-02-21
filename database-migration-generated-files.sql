-- 创建生成文件记录表
CREATE TABLE IF NOT EXISTS generated_files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    merchant_id BIGINT NOT NULL COMMENT '商家ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    file_type VARCHAR(50) NOT NULL DEFAULT 'TEMPLATE' COMMENT '文件类型',
    product_count INT NOT NULL DEFAULT 0 COMMENT '商品数量',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    expires_at DATETIME NULL COMMENT '过期时间',
    download_count INT NOT NULL DEFAULT 0 COMMENT '下载次数',
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_created_at (created_at),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生成的文件记录表';
