-- 修复可售时间和无理由退货字段的默认值
-- 执行前请先备份数据库！

USE meituan_product;

-- 更新所有商品的可售时间为"全天"
UPDATE t_product
SET available_time = '全天'
WHERE merchant_id = 1
  AND deleted = 0
  AND (available_time IS NULL OR available_time = '');

-- 更新所有商品的无理由退货为 0（不支持）
UPDATE t_product
SET no_reason_return = 0
WHERE merchant_id = 1
  AND deleted = 0
  AND no_reason_return IS NULL;

-- 验证更新结果
SELECT
  available_time,
  no_reason_return,
  COUNT(*) as count
FROM t_product
WHERE merchant_id = 1 AND deleted = 0
GROUP BY available_time, no_reason_return;
