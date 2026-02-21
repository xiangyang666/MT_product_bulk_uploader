-- 插入测试商品数据
USE meituan_product;

-- 清空现有商品（可选）
-- DELETE FROM t_product WHERE merchant_id = 1;

-- 插入10个测试商品
INSERT INTO t_product (merchant_id, product_name, category_id, price, stock, description, image_url, status, deleted, created_time, updated_time)
VALUES 
(1, '苹果 iPhone 15 Pro 256GB', '2000010001', 7999.00, 100, '全新苹果iPhone 15 Pro 256GB 深空黑色', 'https://example.com/iphone15.jpg', 0, 0, NOW(), NOW()),
(1, '华为 Mate 60 Pro 512GB', '2000010001', 6999.00, 80, '华为Mate 60 Pro 12GB+512GB 雅川青', 'https://example.com/mate60.jpg', 0, 0, NOW(), NOW()),
(1, '小米14 Ultra 16GB+1TB', '2000010001', 6499.00, 50, '小米14 Ultra 徕卡光学镜头 钛金属版', 'https://example.com/mi14.jpg', 0, 0, NOW(), NOW()),
(1, 'OPPO Find X7 Ultra', '2000010001', 5999.00, 60, 'OPPO Find X7 Ultra 哈苏影像 16GB+512GB', 'https://example.com/oppo.jpg', 0, 0, NOW(), NOW()),
(1, 'vivo X100 Pro 16GB', '2000010001', 5499.00, 70, 'vivo X100 Pro 蔡司光学 16GB+512GB', 'https://example.com/vivo.jpg', 0, 0, NOW(), NOW()),
(1, '三星 Galaxy S24 Ultra', '2000010001', 8999.00, 40, '三星Galaxy S24 Ultra 12GB+512GB 钛灰色', 'https://example.com/samsung.jpg', 0, 0, NOW(), NOW()),
(1, 'MacBook Pro 14英寸 M3', '2000020001', 14999.00, 30, 'MacBook Pro 14英寸 M3芯片 16GB+512GB', 'https://example.com/macbook.jpg', 0, 0, NOW(), NOW()),
(1, '戴尔 XPS 15 笔记本', '2000020001', 12999.00, 25, '戴尔XPS 15 i7-13700H 32GB+1TB', 'https://example.com/dell.jpg', 0, 0, NOW(), NOW()),
(1, 'iPad Pro 12.9英寸 M2', '2000030001', 8999.00, 50, 'iPad Pro 12.9英寸 M2芯片 256GB WiFi版', 'https://example.com/ipad.jpg', 0, 0, NOW(), NOW()),
(1, 'AirPods Pro 2代', '2000040001', 1899.00, 200, 'AirPods Pro 第二代 主动降噪 无线充电', 'https://example.com/airpods.jpg', 0, 0, NOW(), NOW());

-- 查看插入的商品
SELECT 
    id,
    product_name,
    category_id,
    price,
    stock,
    status,
    CASE 
        WHEN status = 0 THEN '待上传'
        WHEN status = 1 THEN '已上传'
        WHEN status = 2 THEN '上传失败'
    END AS status_text
FROM t_product
WHERE deleted = 0
ORDER BY id DESC
LIMIT 10;

-- 统计商品数量
SELECT COUNT(*) AS total_products FROM t_product WHERE deleted = 0;
