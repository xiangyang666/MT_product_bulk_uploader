-- 添加模板查询索引（简化版）
-- 用途：优化模板状态查询性能

USE meituan_product;

-- 创建复合索引
CREATE INDEX IF NOT EXISTS idx_merchant_template_type_time 
ON template(merchant_id, template_type, created_time DESC);

-- 显示创建结果
SHOW INDEX FROM template WHERE Key_name = 'idx_merchant_template_type_time';

-- 测试索引效果
EXPLAIN SELECT * 
FROM template 
WHERE merchant_id = 1 
  AND template_type = 'MEITUAN' 
ORDER BY created_time DESC 
LIMIT 1;
