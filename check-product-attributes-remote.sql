-- 连接到远程数据库并检查类目属性
-- mysql -h 106.55.102.48 -u root -pmysql_G4EcQ6 meituan_product

-- 检查显示器类商品的类目属性
SELECT 
    id,
    LEFT(product_name, 40) as product_name,
    category_name,
    LENGTH(product_attributes) as attr_length,
    product_attributes
FROM t_product
WHERE merchant_id = 1
  AND (category_name LIKE '%显示器%' OR category_name LIKE '%显卡%')
ORDER BY id DESC
LIMIT 10;

-- 检查类目属性为空或过短的商品
SELECT 
    id,
    LEFT(product_name, 40) as product_name,
    category_name,
    LENGTH(product_attributes) as attr_length,
    product_attributes
FROM t_product
WHERE merchant_id = 1
  AND (product_attributes IS NULL OR LENGTH(product_attributes) < 50)
ORDER BY id DESC
LIMIT 10;
