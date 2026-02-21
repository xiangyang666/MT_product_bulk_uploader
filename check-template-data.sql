-- 检查模板表中的数据
USE meituan_product;

-- 查看所有模板记录
SELECT 
    id,
    merchant_id,
    template_name,
    template_type,
    file_path,
    file_url,
    file_size,
    created_time
FROM t_template
WHERE deleted = 0
ORDER BY created_time DESC;

-- 检查 file_path 是否包含 URL 特殊字符
SELECT 
    id,
    template_name,
    file_path,
    CASE 
        WHEN file_path LIKE '%?%' THEN 'Contains ?'
        WHEN file_path LIKE 'http%' THEN 'Is URL'
        ELSE 'Clean path'
    END AS path_status
FROM t_template
WHERE deleted = 0;
