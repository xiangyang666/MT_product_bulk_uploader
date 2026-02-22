-- 创建公司信息表
CREATE TABLE IF NOT EXISTS t_company_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    company_name VARCHAR(100) COMMENT '公司名称',
    slogan VARCHAR(200) COMMENT '口号',
    logo VARCHAR(500) COMMENT 'Logo URL',
    intro_para1 TEXT COMMENT '介绍段落1',
    intro_para2 TEXT COMMENT '介绍段落2',
    intro_para3 TEXT COMMENT '介绍段落3',
    vision TEXT COMMENT '愿景',
    mission TEXT COMMENT '使命',
    `values` TEXT COMMENT '价值观',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(50) COMMENT '电话',
    address VARCHAR(200) COMMENT '地址',
    wechat_qrcode VARCHAR(500) COMMENT '微信二维码URL',
    icp_number VARCHAR(100) COMMENT 'ICP备案号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公司信息表';

-- 插入默认数据
INSERT INTO t_company_info (id, company_name, slogan, intro_para1, intro_para2, intro_para3, vision, mission, `values`, email, phone, address)
VALUES (
    1,
    '云创智链科技',
    '高效、智能的商品管理解决方案',
    '专注于为企业提供高效的商品管理工具',
    '支持批量导入、自动格式识别、一键上传',
    '完全免费，立即下载使用',
    '成为最受欢迎的商品管理工具',
    '让商品管理更简单高效',
    '用户至上，持续创新',
    'support@example.com',
    '400-123-4567',
    '中国'
) ON DUPLICATE KEY UPDATE company_name = company_name;
