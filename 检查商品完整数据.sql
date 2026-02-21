-- 检查商品数据的完整性
-- 查看前5条商品的所有字段

SELECT 
    id,
    upc_ean,
    product_name,
    spec_name,
    price,
    stock,
    category_id,
    category_name,
    brand,
    origin,
    weight,
    weight_unit,
    store_code,
    store_category,
    shelf_code,
    min_purchase,
    sale_status,
    selling_point,
    selling_point_display_period,
    text_detail,
    available_time,
    pre_sale_delivery_time,
    product_attributes,
    no_reason_return,
    app_food_code,
    production_date,
    expiry_date,
    is_near_expiry,
    status
FROM t_product
LIMIT 5;

-- 统计哪些字段有数据
SELECT 
    COUNT(*) as total_products,
    COUNT(upc_ean) as has_upc_ean,
    COUNT(brand) as has_brand,
    COUNT(origin) as has_origin,
    COUNT(weight) as has_weight,
    COUNT(store_code) as has_store_code,
    COUNT(selling_point) as has_selling_point
FROM t_product;
