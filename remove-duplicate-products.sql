-- 清理重复的商品数据
-- 保留每组重复数据中 ID 最小的那条记录（最早导入的）

-- 步骤1：创建临时表存储要删除的ID
CREATE TEMPORARY TABLE IF NOT EXISTS temp_duplicate_ids AS
SELECT p1.id
FROM products p1
INNER JOIN products p2 ON 
    p1.product_name = p2.product_name 
    AND p1.category_id = p2.category_id 
    AND p1.sku_id = p2.sku_id
    AND p1.merchant_id = p2.merchant_id
    AND p1.id > p2.id  -- 保留ID较小的记录
WHERE p1.merchant_id = 1;

-- 步骤2：查看将要删除的记录数量
SELECT COUNT(*) as will_delete_count FROM temp_duplicate_ids;

-- 步骤3：删除重复记录（取消注释下面的语句来执行删除）
-- DELETE FROM products WHERE id IN (SELECT id FROM temp_duplicate_ids);

-- 步骤4：验证删除后的结果
-- SELECT COUNT(*) as remaining_products FROM products WHERE merchant_id = 1;

-- 步骤5：清理临时表
-- DROP TEMPORARY TABLE IF EXISTS temp_duplicate_ids;

-- ============================================
-- 如果你想直接删除所有重复数据，可以使用下面的一条语句：
-- ============================================
/*
DELETE p1 FROM products p1
INNER JOIN products p2 ON 
    p1.product_name = p2.product_name 
    AND p1.category_id = p2.category_id 
    AND p1.sku_id = p2.sku_id
    AND p1.merchant_id = p2.merchant_id
    AND p1.id > p2.id
WHERE p1.merchant_id = 1;
*/

-- ============================================
-- 或者，如果你想删除今天导入的所有数据（保留之前的）：
-- ============================================
/*
DELETE FROM products 
WHERE merchant_id = 1 
AND DATE(created_time) = CURDATE();
*/
