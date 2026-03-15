-- ========================================
-- 快速修复：类目属性不完整问题
-- ========================================
-- 连接命令：mysql -h 106.55.102.48 -u root -pmysql_G4EcQ6 meituan_product

-- ========================================
-- 步骤1：诊断 - 查找所有类目属性不完整的商品
-- ========================================
SELECT 
    '=== 类目属性不完整的商品 ===' as title;

SELECT 
    id,
    LEFT(product_name, 40) as product_name,
    category_name,
    LENGTH(product_attributes) as attr_length,
    product_attributes
FROM t_product
WHERE merchant_id = 1
  AND (product_attributes IS NULL OR LENGTH(product_attributes) < 50)
ORDER BY category_name, id;

-- ========================================
-- 步骤2：统计 - 按类目统计不完整商品数量
-- ========================================
SELECT 
    '=== 按类目统计不完整商品 ===' as title;

SELECT 
    category_name,
    COUNT(*) as incomplete_count,
    ROUND(AVG(LENGTH(COALESCE(product_attributes, ''))), 0) as avg_length
FROM t_product
WHERE merchant_id = 1
  AND (product_attributes IS NULL OR LENGTH(product_attributes) < 50)
GROUP BY category_name
ORDER BY incomplete_count DESC;

-- ========================================
-- 步骤3：参考 - 查看每个类目的完整示例
-- ========================================
SELECT 
    '=== 每个类目的完整示例（供参考） ===' as title;

SELECT 
    category_name,
    LEFT(product_name, 40) as product_name,
    LENGTH(product_attributes) as attr_length,
    product_attributes
FROM t_product
WHERE merchant_id = 1
  AND LENGTH(COALESCE(product_attributes, '')) > 100
GROUP BY category_name
LIMIT 20;

-- ========================================
-- 步骤4：修复 - 手动补全类目属性
-- ========================================
-- 注意：以下UPDATE语句需要根据实际情况修改！
-- 请先查看上面的查询结果，确定需要补全的商品ID和类目属性

-- 示例1：补全显卡类商品（技嘉）
-- UPDATE t_product
-- SET product_attributes = '品牌：技嘉（AORUS）。芯片型号：RTX 4090。显存容量：24GB。显存类型：GDDR6X。接口类型：PCI-E 4.0。散热方式：风冷。'
-- WHERE id IN (604107, 604108, 604109, 604110);

-- 示例2：补全显卡类商品（七彩虹）
-- UPDATE t_product
-- SET product_attributes = '品牌：七彩虹（COLORFUL）。芯片型号：RTX 4080。显存容量：16GB。显存类型：GDDR6X。接口类型：PCI-E 4.0。散热方式：风冷。'
-- WHERE id IN (604112, 604113);

-- 示例3：批量补全同一类目的商品
-- UPDATE t_product
-- SET product_attributes = CONCAT(
--     COALESCE(product_attributes, ''),
--     '芯片型号：未知。显存容量：未知。显存类型：未知。接口类型：PCI-E。'
-- )
-- WHERE merchant_id = 1
--   AND category_name LIKE '%显卡%'
--   AND (product_attributes IS NULL OR LENGTH(product_attributes) < 50);

-- ========================================
-- 步骤5：验证 - 检查修复结果
-- ========================================
SELECT 
    '=== 验证修复结果 ===' as title;

SELECT 
    id,
    LEFT(product_name, 40) as product_name,
    category_name,
    LENGTH(product_attributes) as attr_length,
    product_attributes
FROM t_product
WHERE merchant_id = 1
  AND id IN (604107, 604108, 604109, 604110, 604112, 604113)
ORDER BY id;

-- ========================================
-- 步骤6：最终检查 - 确认没有遗漏
-- ========================================
SELECT 
    '=== 最终检查：还有多少商品类目属性不完整？ ===' as title;

SELECT 
    COUNT(*) as remaining_incomplete_count
FROM t_product
WHERE merchant_id = 1
  AND (product_attributes IS NULL OR LENGTH(product_attributes) < 50);

-- ========================================
-- 常见类目的必填属性参考
-- ========================================
/*
显示器类：
品牌：xxx。
分辨率：1920*1080。
屏幕比例：16:9。
尺寸（英寸）：27。
能效等级：1级。
接口：HDMI。
屏幕刷新率（Hz）：60。
面板：IPS。
曲率：平面。

显卡类：
品牌：xxx。
芯片型号：RTX 4090。
显存容量：24GB。
显存类型：GDDR6X。
接口类型：PCI-E 4.0。
散热方式：风冷。

消毒柜类：
品牌：xxx。
类型：茶杯消毒柜。
能效等级：一级能效。
容量：10L。
层数：3层及以上。
安装方式：柜式。
*/
