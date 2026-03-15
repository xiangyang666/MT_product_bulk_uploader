-- 修复admin-plus密码
-- 使用已验证有效的密码哈希
-- 密码：123456

USE meituan_management;

-- 方案1：使用database-init.sql中的哈希
UPDATE `user` 
SET `password` = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
    `updated_at` = CURRENT_TIMESTAMP
WHERE `username` = 'admin-plus';

-- 验证更新
SELECT 
    username,
    LEFT(password, 30) as password_prefix,
    role,
    status
FROM `user` 
WHERE username = 'admin-plus';

SELECT '密码已更新，请使用以下凭据登录：' AS message;
SELECT '用户名: admin-plus' AS info;
SELECT '密码: 123456' AS info;
