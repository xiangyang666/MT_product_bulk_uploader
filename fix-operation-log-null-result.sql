-- 修复操作日志表中 result 字段为 NULL 的记录
-- 将 NULL 值更新为 1（成功）

-- 查看有多少条记录的 result 为 NULL
SELECT COUNT(*) as null_result_count 
FROM operation_log 
WHERE result IS NULL;

-- 更新 result 为 NULL 的记录，设置为 1（成功）
UPDATE operation_log 
SET result = 1 
WHERE result IS NULL;

-- 验证更新结果
SELECT COUNT(*) as null_result_count_after 
FROM operation_log 
WHERE result IS NULL;

-- 查看最近的操作日志
SELECT 
    id,
    operation_type,
    operation_desc,
    result,
    created_at
FROM operation_log 
ORDER BY created_at DESC 
LIMIT 10;
