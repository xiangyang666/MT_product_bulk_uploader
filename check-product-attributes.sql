-- 检查商品的类目属性字段
SELECT 
    id,
    LEFT(product_name, 30) as product_name,
    category_name,
    LENGTH(product_attributes) as attr_length,
    product_attributes
FROM t_product
WHERE merchant_id = 1
ORDER BY id DESC
LIMIT 10;
