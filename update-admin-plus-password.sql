-- 更新admin-plus用户密码
-- 密码：123456

USE meituan_management;

-- 查看当前admin-plus用户信息
SELECT id, username, role, status, created_at 
FROM `user` 
WHERE username = 'admin-plus';

-- 更新密码为 123456
-- 使用已验证有效的BCrypt哈希
UPDATE `user` 
SET `password` = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
    `updated_at` = CURRENT_TIMESTAMP,
    `updated_by` = 'system'
WHERE `username` = 'admin-plus';

-- 验证更新结果
SELECT id, username, role, status, 
       LEFT(password, 20) as password_hash,
       updated_at
FROM `user` 
WHERE username = 'admin-plus';

SELECT '密码已更新为: 123456' AS message;
