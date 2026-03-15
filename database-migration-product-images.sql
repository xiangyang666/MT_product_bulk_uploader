-- 商品图片字段迁移
-- 添加 product_images 字段用于存储多张图片的JSON数组

USE meituan_product;

-- 添加商品图片字段
ALTER TABLE t_product 
ADD COLUMN product_images TEXT COMMENT '商品图片URL列表（JSON数组，最多5张）' 
AFTER image_url;

-- 查看表结构确认
DESC t_product;

-- 验证字段是否添加成功
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'meituan_product'
  AND TABLE_NAME = 't_product'
  AND COLUMN_NAME = 'product_images';
