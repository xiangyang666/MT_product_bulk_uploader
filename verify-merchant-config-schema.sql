-- ============================================
-- 验证商家配置表架构 - JSON 字段兼容性测试
-- ============================================

USE meituan_product;

-- 1. 检查 template_config 列的类型
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'meituan_product'
  AND TABLE_NAME = 't_merchant_config'
  AND COLUMN_NAME = 'template_config';

-- 2. 测试插入空 JSON 对象
-- 创建测试记录
INSERT INTO t_merchant_config (
    merchant_id, 
    merchant_name, 
    template_config,
    created_time,
    updated_time
) VALUES (
    999,
    '测试商家-JSON验证',
    '{}',
    NOW(),
    NOW()
);

-- 验证插入成功
SELECT 
    id,
    merchant_id,
    merchant_name,
    template_config,
    created_time
FROM t_merchant_config
WHERE merchant_id = 999;

-- 3. 测试更新为空 JSON 对象
UPDATE t_merchant_config
SET template_config = '{}',
    updated_time = NOW()
WHERE merchant_id = 999;

-- 验证更新成功
SELECT 
    id,
    merchant_id,
    template_config,
    updated_time
FROM t_merchant_config
WHERE merchant_id = 999;

-- 4. 测试更新为有效 JSON
UPDATE t_merchant_config
SET template_config = '{"key":"value","number":123}',
    updated_time = NOW()
WHERE merchant_id = 999;

-- 验证更新成功
SELECT 
    id,
    merchant_id,
    template_config
FROM t_merchant_config
WHERE merchant_id = 999;

-- 5. 清理测试数据
DELETE FROM t_merchant_config WHERE merchant_id = 999;

-- 6. 检查现有数据中是否有 NULL 或空的 template_config
SELECT 
    id,
    merchant_id,
    merchant_name,
    template_config,
    CASE 
        WHEN template_config IS NULL THEN 'NULL'
        WHEN template_config = '' THEN 'EMPTY STRING'
        WHEN template_config = '{}' THEN 'EMPTY JSON'
        ELSE 'HAS DATA'
    END AS config_status
FROM t_merchant_config
ORDER BY id;

-- 7. 如果需要，更新所有 NULL 值为空 JSON 对象（可选）
-- UPDATE t_merchant_config 
-- SET template_config = '{}' 
-- WHERE template_config IS NULL OR template_config = '';

-- ============================================
-- 测试结果说明：
-- 1. 如果所有操作都成功，说明数据库架构兼容
-- 2. 如果插入/更新失败，需要检查列类型
-- 3. 建议列类型为 JSON 或 TEXT
-- ============================================
