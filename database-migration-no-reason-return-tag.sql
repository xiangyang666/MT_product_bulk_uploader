-- 添加无理由退货标签ID字段
-- 执行前请先备份数据库！

USE meituan_product;

-- 添加无理由退货标签ID字段（存储美团平台的标签ID）
ALTER TABLE t_product ADD COLUMN IF NOT EXISTS no_reason_return_tag_id VARCHAR(20) DEFAULT NULL COMMENT '无理由退货标签ID' AFTER no_reason_return;

-- 将现有的 no_reason_return 值转换为标签ID
-- 0 -> 1300030895（不支持7天无理由退货）
-- 1 -> 1300030901（7天无理由退货）
UPDATE t_product
SET no_reason_return_tag_id = CASE
    WHEN no_reason_return = 1 THEN '1300030901'
    ELSE '1300030895'
END
WHERE no_reason_return_tag_id IS NULL;

-- 验证更新结果
SELECT 
    no_reason_return,
    no_reason_return_tag_id,
    COUNT(*) as count
FROM t_product
GROUP BY no_reason_return, no_reason_return_tag_id;
