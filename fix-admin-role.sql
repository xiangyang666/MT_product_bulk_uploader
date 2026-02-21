-- 修复 admin 用户角色为超级管理员
-- 执行命令: mysql -h 106.55.102.48 -P 3306 -u root -pmysql_G4EcQ6 meituan_product < fix-admin-role.sql

USE meituan_product;

-- 查看当前 admin 用户信息
SELECT id, username, role, status FROM user WHERE username = 'admin';

-- 更新 admin 用户角色为 SUPER_ADMIN
UPDATE user 
SET role = 'SUPER_ADMIN', 
    status = 1,
    updated_at = NOW()
WHERE username = 'admin';

-- 验证更新结果
SELECT id, username, role, status, created_at, updated_at 
FROM user 
WHERE username = 'admin';

SELECT '✅ admin 用户角色已更新为 SUPER_ADMIN' AS message;
