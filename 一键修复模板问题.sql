-- 一键修复模板预览问题
-- 执行方式：mysql -u root -p meituan_product < 一键修复模板问题.sql

USE meituan_product;

-- ============================================
-- 步骤 1：查看当前状态
-- ============================================
SELECT '========== 当前模板状态 ==========' AS '';

SELECT 
    id,
    template_name,
    CASE 
        WHEN deleted = 1 THEN '❌ 已删除'
        ELSE '✅ 正常'
    END AS status,
    created_time
FROM t_template
ORDER BY id;

SELECT 
    COUNT(*) AS total_count,
    SUM(CASE WHEN deleted = 0 THEN 1 ELSE 0 END) AS active_count,
    SUM(CASE WHEN deleted = 1 THEN 1 ELSE 0 END) AS deleted_count
FROM t_template;

-- ============================================
-- 步骤 2：物理删除逻辑删除的记录
-- ============================================
SELECT '========== 开始清理 ==========' AS '';

DELETE FROM t_template WHERE deleted = 1;

SELECT CONCAT('✅ 已删除 ', ROW_COUNT(), ' 条逻辑删除的记录') AS result;

-- ============================================
-- 步骤 3：查看清理后的状态
-- ============================================
SELECT '========== 清理后的状态 ==========' AS '';

SELECT 
    id,
    template_name,
    template_type,
    file_path,
    created_time
FROM t_template
WHERE deleted = 0
ORDER BY id;

SELECT COUNT(*) AS remaining_templates FROM t_template WHERE deleted = 0;

-- ============================================
-- 步骤 4：验证文件路径
-- ============================================
SELECT '========== 验证文件路径 ==========' AS '';

SELECT 
    id,
    template_name,
    CASE 
        WHEN file_path LIKE '%?%' THEN '❌ 包含URL参数'
        WHEN file_path LIKE 'http%' THEN '❌ 是完整URL'
        WHEN file_path IS NULL OR file_path = '' THEN '❌ 路径为空'
        ELSE '✅ 路径正常'
    END AS path_status,
    file_path
FROM t_template
WHERE deleted = 0;

-- ============================================
-- 完成
-- ============================================
SELECT '========== 修复完成 ==========' AS '';
SELECT '✅ 请刷新前端页面（Ctrl + F5）' AS next_step;
SELECT '✅ 然后重新上传模板并测试预览功能' AS next_step;
