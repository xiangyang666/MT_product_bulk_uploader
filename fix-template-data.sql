-- 修复模板数据脚本
-- 用途：清理损坏的模板数据，准备重新上传

USE meituan_product;

-- ============================================
-- 方案 1：删除用户上传的模板（保留测试数据）
-- ============================================
-- 测试数据的 ID 通常是 1, 2, 3
-- 用户上传的模板 ID 从 4 开始

-- 查看当前所有模板
SELECT 
    id,
    template_name,
    template_type,
    file_path,
    CASE 
        WHEN file_path LIKE '%?%' THEN '❌ 包含URL参数'
        WHEN file_path LIKE 'http%' THEN '❌ 是完整URL'
        ELSE '✅ 正常路径'
    END AS path_status,
    created_time
FROM t_template
WHERE deleted = 0
ORDER BY id;

-- 删除用户上传的模板（ID > 3）
-- DELETE FROM t_template WHERE id > 3;

-- ============================================
-- 方案 2：清空所有模板（包括测试数据）
-- ============================================
-- 如果要完全重新开始，使用这个命令
-- TRUNCATE TABLE t_template;

-- ============================================
-- 方案 3：只删除路径异常的模板
-- ============================================
-- 删除 file_path 包含 URL 参数的记录
-- DELETE FROM t_template 
-- WHERE file_path LIKE '%?%' 
--    OR file_path LIKE 'http%';

-- ============================================
-- 验证清理结果
-- ============================================
SELECT 
    COUNT(*) AS total_templates,
    SUM(CASE WHEN file_path LIKE '%?%' OR file_path LIKE 'http%' THEN 1 ELSE 0 END) AS bad_templates,
    SUM(CASE WHEN file_path NOT LIKE '%?%' AND file_path NOT LIKE 'http%' THEN 1 ELSE 0 END) AS good_templates
FROM t_template
WHERE deleted = 0;

-- 查看剩余的模板
SELECT id, template_name, file_path 
FROM t_template 
WHERE deleted = 0 
ORDER BY id;

-- ============================================
-- 使用说明
-- ============================================
-- 1. 先运行查询语句，查看当前数据状态
-- 2. 根据需要选择一个方案，取消注释对应的 DELETE 或 TRUNCATE 语句
-- 3. 执行清理命令
-- 4. 运行验证查询，确认清理结果
-- 5. 重启后端服务
-- 6. 重新上传模板文件

SELECT '✅ 脚本准备完成，请根据需要选择清理方案' AS message;
