-- 清空商品表数据
-- 使用此脚本清空所有商品数据，以便重新导入

USE `meituan_product`;

-- 清空商品表
TRUNCATE TABLE `t_product`;

-- 验证清空结果
SELECT COUNT(*) AS remaining_products FROM `t_product`;

-- 显示完成消息
SELECT '商品表已清空，可以重新导入数据' AS message;
