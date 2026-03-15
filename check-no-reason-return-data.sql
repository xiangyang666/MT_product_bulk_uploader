-- 检查无理由退货字段的数据问题
-- 执行此脚本可以诊断无理由退货字段的映射问题

USE meituan_product;

-- 1. 查看所有无理由退货字段的分布情况
SELECT
    COALESCE(no_reason_return_tag_id, 'NULL') as no_reason_return_tag_id,
    no_reason_return,
    COUNT(*) as count,
    GROUP_CONCAT(id ORDER BY id SEPARATOR ',') as sample_ids
FROM t_product
GROUP BY no_reason_return_tag_id, no_reason_return
ORDER BY count DESC;

-- 2. 查看最近导入的商品的无理由退货字段
SELECT
    id,
    product_name,
    no_reason_return,
    no_reason_return_tag_id,
    created_time
FROM t_product
WHERE deleted = 0
ORDER BY created_time DESC
LIMIT 20;

-- 3. 检查是否存在数据不一致的情况
-- （no_reason_return=1 但 tagId 不是 1300030901）
SELECT
    id,
    product_name,
    no_reason_return,
    no_reason_return_tag_id,
    created_time
FROM t_product
WHERE deleted = 0
  AND no_reason_return = 1
  AND no_reason_return_tag_id != '1300030901'
ORDER BY created_time DESC;

-- 4. 检查 tagId 为 NULL 的情况
SELECT
    id,
    product_name,
    no_reason_return,
    no_reason_return_tag_id,
    created_time
FROM t_product
WHERE deleted = 0
  AND (no_reason_return_tag_id IS NULL OR no_reason_return_tag_id = '')
ORDER BY created_time DESC
LIMIT 10;

-- 5. 显示所有可能的 tagId 值及其含义
SELECT '标签ID说明：' AS info;
SELECT '1300030895' AS tag_id, '不支持7天无理由退货' AS description
UNION ALL
SELECT '1300030902', '7天无理由退货（一次性包装破损不支持）'
UNION ALL
SELECT '1300030903', '7天无理由退货（激活后不支持）'
UNION ALL
SELECT '1300030904', '7天无理由退货（使用后不支持）'
UNION ALL
SELECT '1300030905', '7天无理由退货（安装后不支持）'
UNION ALL
SELECT '1300030906', '7天无理由退货（定制类不支持）'
UNION ALL
SELECT '1300030901', '7天无理由退货';
