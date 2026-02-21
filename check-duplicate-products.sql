-- 检查是否有重复的商品数据
-- 按照商品名称、类目ID、SKU ID分组，查找重复记录

SELECT 
    product_name,
    category_id,
    sku_id,
    COUNT(*) as duplicate_count,
    GROUP_CONCAT(id) as product_ids,
    MIN(created_time) as first_import_time,
    MAX(created_time) as last_import_time
FROM products
WHERE merchant_id = 1
GROUP BY product_name, category_id, sku_id
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC
LIMIT 50;

-- 查看今天导入的商品总数
SELECT 
    DATE(created_time) as import_date,
    COUNT(*) as product_count
FROM products
WHERE merchant_id = 1
GROUP BY DATE(created_time)
ORDER BY import_date DESC;

-- 查看商品总数
SELECT COUNT(*) as total_products FROM products WHERE merchant_id = 1;

-- 查看最近导入的商品（按创建时间倒序）
SELECT 
    id,
    product_name,
    category_id,
    sku_id,
    created_time
FROM products
WHERE merchant_id = 1
ORDER BY created_time DESC
LIMIT 20;
