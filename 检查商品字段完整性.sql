-- 检查商品字段完整性
USE meituan_product;

-- 1. 查看所有商品的字段情况
SELECT 
    id,
    product_name,
    category_id,
    price,
    stock,
    description,
    image_url,
    CASE 
        WHEN product_name IS NULL OR product_name = '' THEN '❌'
        ELSE '✅'
    END AS has_name,
    CASE 
        WHEN category_id IS NULL OR category_id = '' THEN '❌'
        ELSE '✅'
    END AS has_category,
    CASE 
        WHEN price IS NULL OR price = 0 THEN '❌'
        ELSE '✅'
    END AS has_price,
    CASE 
        WHEN description IS NULL OR description = '' THEN '❌'
        ELSE '✅'
    END AS has_description,
    CASE 
        WHEN image_url IS NULL OR image_url = '' THEN '❌'
        ELSE '✅'
    END AS has_image
FROM t_product
WHERE deleted = 0
ORDER BY id DESC
LIMIT 20;

-- 2. 统计空字段的商品数量
SELECT 
    COUNT(*) AS total_products,
    SUM(CASE WHEN product_name IS NULL OR product_name = '' THEN 1 ELSE 0 END) AS empty_name,
    SUM(CASE WHEN category_id IS NULL OR category_id = '' THEN 1 ELSE 0 END) AS empty_category,
    SUM(CASE WHEN price IS NULL OR price = 0 THEN 1 ELSE 0 END) AS empty_price,
    SUM(CASE WHEN stock IS NULL THEN 1 ELSE 0 END) AS empty_stock,
    SUM(CASE WHEN description IS NULL OR description = '' THEN 1 ELSE 0 END) AS empty_description,
    SUM(CASE WHEN image_url IS NULL OR image_url = '' THEN 1 ELSE 0 END) AS empty_image
FROM t_product
WHERE deleted = 0;

-- 3. 查看具体哪些商品有空字段
SELECT 
    id,
    product_name,
    '描述为空' AS issue
FROM t_product
WHERE deleted = 0 AND (description IS NULL OR description = '')
LIMIT 10;

SELECT 
    id,
    product_name,
    '图片URL为空' AS issue
FROM t_product
WHERE deleted = 0 AND (image_url IS NULL OR image_url = '')
LIMIT 10;
