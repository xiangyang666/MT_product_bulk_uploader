-- 查看模板数据状态
USE meituan_product;

-- 查看所有模板（包括已删除的）
SELECT 
    id,
    merchant_id,
    template_name,
    template_type,
    file_path,
    file_url,
    file_size,
    created_time,
    deleted,
    CASE 
        WHEN deleted = 1 THEN '❌ 已删除'
        ELSE '✅ 正常'
    END AS status
FROM t_template
ORDER BY id;

-- 统计模板数量
SELECT 
    COUNT(*) AS total,
    SUM(CASE WHEN deleted = 0 THEN 1 ELSE 0 END) AS active,
    SUM(CASE WHEN deleted = 1 THEN 1 ELSE 0 END) AS deleted_count
FROM t_template;

-- 查看未删除的模板
SELECT 
    id,
    template_name,
    file_path,
    created_time
FROM t_template
WHERE deleted = 0
ORDER BY id;
