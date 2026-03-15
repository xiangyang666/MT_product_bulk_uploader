-- 验证数据库迁移结果

USE meituan_product;

-- 1. 检查字段是否存在
SELECT '=== 检查字段 ===' as '';
SHOW COLUMNS FROM t_product LIKE 'no_reason_return%';

-- 2. 查看数据分布
SELECT '=== 数据分布 ===' as '';
SELECT 
    no_reason_return,
    no_reason_return_tag_id,
    COUNT(*) as count
FROM t_product
GROUP BY no_reason_return, no_reason_return_tag_id;

-- 3. 查看最近的商品
SELECT '=== 最近5条商品 ===' as '';
SELECT 
    id,
    LEFT(product_name, 30) as product_name,
    no_reason_return,
    no_reason_return_tag_id
FROM t_product
ORDER BY id DESC
LIMIT 5;

-- 4. 统计NULL值
SELECT '=== NULL值统计 ===' as '';
SELECT 
    COUNT(*) as total_products,
    SUM(CASE WHEN no_reason_return_tag_id IS NULL THEN 1 ELSE 0 END) as null_tag_id_count,
    SUM(CASE WHEN no_reason_return_tag_id IS NOT NULL THEN 1 ELSE 0 END) as has_tag_id_count
FROM t_product;
