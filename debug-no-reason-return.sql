-- 调试无理由退货字段
-- 检查数据库中实际存储的值

USE meituan_product;

-- 查看最近导入的商品数据
SELECT 
    id,
    product_name,
    no_reason_return,
    no_reason_return_tag_id,
    created_time
FROM t_product
ORDER BY id DESC
LIMIT 20;

-- 查看包含"一次性包装破损"的商品
SELECT 
    id,
    product_name,
    no_reason_return,
    no_reason_return_tag_id,
    created_time
FROM t_product
WHERE product_name LIKE '%一次性%' 
   OR no_reason_return_tag_id LIKE '%1300030902%'
ORDER BY id DESC;

-- 统计各个标签ID的数量
SELECT 
    no_reason_return_tag_id,
    COUNT(*) as count,
    CASE no_reason_return_tag_id
        WHEN '1300030895' THEN '不支持7天无理由退货'
        WHEN '1300030902' THEN '7天无理由退货（一次性包装破损不支持）'
        WHEN '1300030903' THEN '7天无理由退货（激活后不支持）'
        WHEN '1300030904' THEN '7天无理由退货（使用后不支持）'
        WHEN '1300030905' THEN '7天无理由退货（安装后不支持）'
        WHEN '1300030906' THEN '7天无理由退货（定制类不支持）'
        WHEN '1300030901' THEN '7天无理由退货'
        ELSE '未知或NULL'
    END as tag_name
FROM t_product
GROUP BY no_reason_return_tag_id
ORDER BY count DESC;
