-- 检查商品数据
USE meituan_product;

-- 1. 统计商品总数
SELECT 
    COUNT(*) AS total_products,
    SUM(CASE WHEN deleted = 0 THEN 1 ELSE 0 END) AS active_products,
    SUM(CASE WHEN deleted = 1 THEN 1 ELSE 0 END) AS deleted_products
FROM t_product;

-- 2. 查看前10条商品数据
SELECT 
    id,
    product_name,
    category_id,
    price,
    stock,
    status,
    deleted,
    created_time
FROM t_product
WHERE deleted = 0
ORDER BY id
LIMIT 10;

-- 3. 按状态统计
SELECT 
    CASE 
        WHEN status = 0 THEN '待上传'
        WHEN status = 1 THEN '已上传'
        WHEN status = 2 THEN '上传失败'
        ELSE '未知'
    END AS status_name,
    COUNT(*) AS count
FROM t_product
WHERE deleted = 0
GROUP BY status;

-- 4. 检查是否有空数据
SELECT 
    COUNT(*) AS products_with_empty_name
FROM t_product
WHERE deleted = 0 AND (product_name IS NULL OR product_name = '');

SELECT 
    COUNT(*) AS products_with_empty_category
FROM t_product
WHERE deleted = 0 AND (category_id IS NULL OR category_id = '');
