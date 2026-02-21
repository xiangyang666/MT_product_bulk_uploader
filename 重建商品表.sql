-- ============================================
-- 重建商品表 (t_product)
-- ============================================
-- 说明：如果不小心删除了商品表，使用此脚本重建
-- 执行时间：2026-02-09
-- ============================================

USE meituan_product;

-- ============================================
-- 1. 删除旧表（如果存在）
-- ============================================
DROP TABLE IF EXISTS `t_product`;

-- ============================================
-- 2. 创建商品表
-- ============================================
CREATE TABLE `t_product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `product_name` VARCHAR(255) NOT NULL COMMENT '商品名称',
  `category_id` VARCHAR(20) NOT NULL COMMENT '类目ID（10位数字）',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格（元）',
  `stock` INT DEFAULT 0 COMMENT '库存数量',
  `description` TEXT COMMENT '商品描述',
  `image_url` VARCHAR(500) COMMENT '商品图片URL',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待上传，1-已上传，2-上传失败',
  `meituan_product_id` VARCHAR(50) COMMENT '美团商品ID',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  INDEX `idx_merchant_id` (`merchant_id`),
  INDEX `idx_category_id` (`category_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_deleted` (`deleted`),
  INDEX `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ============================================
-- 3. 插入测试商品数据（20个）
-- ============================================

INSERT INTO `t_product` 
(`merchant_id`, `product_name`, `category_id`, `price`, `stock`, `description`, `image_url`, `status`, `deleted`) 
VALUES
-- 手机类
(1, '苹果 iPhone 15 Pro 256GB 深空黑色', '2000010001', 7999.00, 100, '全新苹果iPhone 15 Pro，A17 Pro芯片，钛金属设计，支持5G', 'https://example.com/iphone15.jpg', 0, 0),
(1, '华为 Mate 60 Pro 12GB+512GB 雅川青', '2000010001', 6999.00, 80, '华为Mate 60 Pro，卫星通信，鸿蒙4.0系统', 'https://example.com/mate60.jpg', 0, 0),
(1, '小米14 Ultra 16GB+512GB 钛金属', '2000010001', 6499.00, 120, '小米14 Ultra，徕卡光学镜头，骁龙8 Gen3处理器', 'https://example.com/mi14.jpg', 0, 0),
(1, 'OPPO Find X7 Ultra 16GB+512GB 大漠银月', '2000010001', 5999.00, 90, 'OPPO Find X7 Ultra，哈苏影像系统，天玑9300', 'https://example.com/findx7.jpg', 0, 0),
(1, 'vivo X100 Pro 16GB+512GB 落日橙', '2000010001', 5499.00, 110, 'vivo X100 Pro，蔡司光学镜头，天玑9300处理器', 'https://example.com/vivox100.jpg', 0, 0),
(1, '三星 Galaxy S24 Ultra 12GB+512GB 钛灰色', '2000010001', 8999.00, 60, '三星Galaxy S24 Ultra，AI智能助手，骁龙8 Gen3', 'https://example.com/galaxys24.jpg', 0, 0),
(1, '荣耀 Magic6 Pro 16GB+512GB 青海湖蓝', '2000010001', 5699.00, 85, '荣耀Magic6 Pro，鹰眼相机，骁龙8 Gen3', 'https://example.com/magic6.jpg', 0, 0),
(1, '一加 12 16GB+512GB 岩黑', '2000010001', 4999.00, 95, '一加12，哈苏影像，骁龙8 Gen3，120Hz屏幕', 'https://example.com/oneplus12.jpg', 0, 0),

-- 笔记本电脑类
(1, 'MacBook Pro 14英寸 M3 16GB+512GB', '2000020001', 14999.00, 50, 'MacBook Pro 14英寸，M3芯片，Liquid视网膜显示屏', 'https://example.com/macbook14.jpg', 0, 0),
(1, 'MacBook Air 15英寸 M3 16GB+512GB', '2000020001', 12999.00, 45, 'MacBook Air 15英寸，M3芯片，超薄设计', 'https://example.com/macbookair15.jpg', 0, 0),
(1, '戴尔 XPS 15 i7-13700H 32GB+1TB', '2000020001', 12999.00, 40, '戴尔XPS 15，4K OLED屏幕，RTX 4060显卡', 'https://example.com/dellxps15.jpg', 0, 0),
(1, '联想 ThinkPad X1 Carbon i7 32GB+1TB', '2000020001', 11999.00, 35, '联想ThinkPad X1 Carbon，商务旗舰，碳纤维机身', 'https://example.com/thinkpadx1.jpg', 0, 0),
(1, '华硕 ROG 幻16 i9-13980HX RTX4090', '2000020001', 19999.00, 25, '华硕ROG幻16，游戏本旗舰，240Hz屏幕', 'https://example.com/rog16.jpg', 0, 0),

-- 平板电脑类
(1, 'iPad Pro 12.9英寸 M2 256GB WiFi版', '2000030001', 8999.00, 70, 'iPad Pro 12.9英寸，M2芯片，Liquid视网膜XDR显示屏', 'https://example.com/ipadpro12.jpg', 0, 0),
(1, 'iPad Air 11英寸 M2 256GB WiFi版', '2000030001', 5499.00, 80, 'iPad Air 11英寸，M2芯片，支持Apple Pencil Pro', 'https://example.com/ipadair11.jpg', 0, 0),
(1, '华为 MatePad Pro 13.2英寸 12GB+512GB', '2000030001', 5999.00, 55, '华为MatePad Pro，鸿蒙4.0，OLED柔性屏', 'https://example.com/matepadpro.jpg', 0, 0),
(1, '小米平板6 Pro 12.4英寸 12GB+512GB', '2000030001', 3299.00, 90, '小米平板6 Pro，144Hz高刷屏，骁龙8+ Gen1', 'https://example.com/mipad6.jpg', 0, 0),

-- 耳机类
(1, 'AirPods Pro 2代 主动降噪', '2000040001', 1899.00, 200, 'AirPods Pro 第二代，主动降噪，无线充电盒', 'https://example.com/airpodspro2.jpg', 0, 0),
(1, 'Sony WH-1000XM5 无线降噪耳机', '2000040001', 2499.00, 150, 'Sony WH-1000XM5，业界顶级降噪，30小时续航', 'https://example.com/sonywh1000xm5.jpg', 0, 0),
(1, 'Bose QuietComfort Ultra 降噪耳机', '2000040001', 2999.00, 120, 'Bose QuietComfort Ultra，沉浸式音频，空间音频', 'https://example.com/boseqc.jpg', 0, 0);

-- ============================================
-- 4. 验证数据
-- ============================================

-- 查看插入的商品数量
SELECT COUNT(*) AS total_products FROM t_product WHERE deleted = 0;

-- 查看所有商品
SELECT 
    id,
    product_name,
    category_id,
    price,
    stock,
    CASE 
        WHEN status = 0 THEN '待上传'
        WHEN status = 1 THEN '已上传'
        WHEN status = 2 THEN '上传失败'
        ELSE '未知'
    END AS status_text,
    created_time
FROM t_product
WHERE deleted = 0
ORDER BY id;

-- 按类目统计
SELECT 
    category_id,
    COUNT(*) AS product_count,
    AVG(price) AS avg_price,
    SUM(stock) AS total_stock
FROM t_product
WHERE deleted = 0
GROUP BY category_id;

-- 查看最新的10个商品
SELECT 
    id,
    product_name,
    price,
    stock
FROM t_product
WHERE deleted = 0
ORDER BY id DESC
LIMIT 10;

-- ============================================
-- 5. 完成提示
-- ============================================

SELECT '✅ 商品表重建完成！' AS message;
SELECT '✅ 已插入20个测试商品' AS message;
SELECT '✅ 现在可以使用批量上传功能了' AS message;

-- ============================================
-- 使用说明
-- ============================================

/*
执行此脚本后：

1. 商品表 (t_product) 已重建
2. 已插入20个测试商品数据
3. 包含以下类目：
   - 2000010001: 手机（8个）
   - 2000020001: 笔记本电脑（5个）
   - 2000030001: 平板电脑（4个）
   - 2000040001: 耳机（3个）

下一步操作：
1. 刷新前端页面（按F5）
2. 进入"批量上传"页面
3. 勾选商品
4. 点击"生成模板"
5. 文件自动下载

如果需要更多测试数据，可以：
1. 使用"批量导入"功能导入Excel
2. 或者修改此脚本添加更多INSERT语句
*/

