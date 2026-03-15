-- 验证无理由退货字段修��是否成功
-- 在重新导入数据后执行此脚本

USE meituan_product;

-- 1. 检查字段是否存在
SELECT '=== 1. 检查字段 ===' AS '';
SHOW COLUMNS FROM t_product LIKE 'no_reason_return_tag_id';

-- 2. 查看所有可能的标签ID分布
SELECT '=== 2. 标签ID分布 ===' AS '';
SELECT
    COALESCE(no_reason_return_tag_id, 'NULL') AS tag_id,
    no_reason_return AS boolean_value,
    COUNT(*) AS count
FROM t_product
WHERE deleted = 0
GROUP BY no_reason_return_tag_id, no_reason_return
ORDER BY tag_id;

-- 3. 查看最近导入的商品示例
SELECT '=== 3. 最近导入的商品（前10条）===' AS '';
SELECT
    id,
    LEFT(product_name, 30) AS product_name,
    no_reason_return,
    no_reason_return_tag_id,
    CASE no_reason_return_tag_id
        WHEN '1300030895' THEN '不支持7天无理由退货'
        WHEN '1300030901' THEN '7天无理由退货'
        WHEN '1300030902' THEN '7天无理由退货（一次性包装破损不支持）✅'
        WHEN '1300030903' THEN '7天无理由退货（激活后不支持）'
        WHEN '1300030904' THEN '7天无理由退货（使用后不支持）'
        WHEN '1300030905' THEN '7天无理由退货（安装后不支持）'
        WHEN '1300030906' THEN '7天无理由退货（定制类不支持）'
        ELSE '未知'
    END AS tag_meaning,
    created_time
FROM t_product
WHERE deleted = 0
ORDER BY created_time DESC
LIMIT 10;

-- 4. 检查是否有NULL值
SELECT '=== 4. 数据完整性检查 ===' AS '';
SELECT
    COUNT(*) AS total_products,
    SUM(CASE WHEN no_reason_return_tag_id IS NULL THEN 1 ELSE 0 END) AS null_tag_count,
    SUM(CASE WHEN no_reason_return_tag_id IS NOT NULL THEN 1 ELSE 0 END) AS has_tag_count,
    CONCAT(ROUND(SUM(CASE WHEN no_reason_return_tag_id IS NOT NULL THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2), '%') AS completion_rate
FROM t_product
WHERE deleted = 0;

-- 5. 标签说明
SELECT '=== 5. 标签ID说明 ===' AS '';
SELECT
    tag_id,
    description
FROM (
    SELECT '1300030895' AS tag_id, '不支持7天无理由退货' AS description
    UNION ALL SELECT '1300030901', '7天无理由退货（通用）'
    UNION ALL SELECT '1300030902', '7天无理由退货（一次性包装破损不支持）✅ 正确的映射'
    UNION ALL SELECT '1300030903', '7天无理由退货（激活后不支持）'
    UNION ALL SELECT '1300030904', '7天无理由退货（使用后不支持）'
    UNION ALL SELECT '1300030905', '7天无理由退货（安装后不支持）'
    UNION ALL SELECT '1300030906', '7天无理由退货（定制类不支持）'
) AS tags
ORDER BY tag_id;

-- 6. 诊断建议
SELECT '=== 6. 诊断建议 ===' AS '';
SELECT
    CASE
        WHEN (SELECT COUNT(*) FROM t_product WHERE deleted = 0 AND no_reason_return_tag_id IS NULL) > 0 THEN
            '⚠️  发现NULL值：需要重新导入数据或检查映射配置'
        WHEN (SELECT COUNT(*) FROM t_product WHERE deleted = 0 AND no_reason_return_tag_id = '1300030902') > 0 THEN
            '✅ 修复成功：找到1300030902标签，映射配置正确'
        WHEN (SELECT COUNT(*) FROM t_product WHERE deleted = 0) = 0 THEN
            '⚠️  数据为空：请先导入包含无理由退货字段的Excel'
        ELSE
            'ℹ️  数据存在但可能未包含"一次性包装破损"标签，请测试导入该标签'
    END AS diagnosis;
