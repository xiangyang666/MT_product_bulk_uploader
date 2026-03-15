-- 检查admin-plus用户的密码哈希
USE meituan_management;

SELECT 
    id,
    username,
    password,
    role,
    status,
    created_at,
    updated_at
FROM `user` 
WHERE username = 'admin-plus';
