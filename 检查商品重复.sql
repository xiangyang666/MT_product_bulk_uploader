-- 简单查询商品数量和前10条数据
SELECT COUNT(*) as total FROM t_product WHERE merchant_id = 1;

SELECT 
    id,
    product_name,
    category_id,
    price
FROM t_product
WHERE merchant_id = 1
ORDER BY id
LIMIT 20;
