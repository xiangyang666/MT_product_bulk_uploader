-- 检查商品数据问题
USE meituan_product;

-- 1. 检查ID 6464, 6465, 6466 的商品是否存在
SELECT 
    id,
    product_name,
    deleted,
    CASE 
        WHEN deleted = 1 THEN '❌ 已删除'
        ELSE '✅ 正常'
    END AS status
FROM t_product
WHERE id IN (6464, 6465, 6466);

-- 2. 查看所有未删除的商品
SELECT 
    id,
    product_name,
    category_id,
    price,
    status
FROM t_product
WHERE deleted = 0
ORDER BY id;

-- 3. 统计商品数量
SELECT 
    COUNT(*) AS total,
    SUM(CASE WHEN deleted = 0 THEN 1 ELSE 0 END) AS active,
    SUM(CASE WHEN deleted = 1 THEN 1 ELSE 0 END) AS deleted_count
FROM t_product;

-- 4. 查看最新的10个商品ID
SELECT 
    id,
    product_name,
    created_time
FROM t_product
WHERE deleted = 0
ORDER BY id DESC
LIMIT 10;

-- 5. 如果ID 6464, 6465, 6466 被标记为删除，恢复它们
-- UPDATE t_product SET deleted = 0 WHERE id IN (6464, 6465, 6466);
