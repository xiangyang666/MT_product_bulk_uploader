-- 只保留admin-plus用户，删除其他所有用户
-- 执行前请确保已备份数据库

USE meituan_management;

-- 查看当前所有用户
SELECT id, username, role, status, created_at 
FROM user 
ORDER BY id;

-- 删除除admin-plus之外的所有用户
DELETE FROM user 
WHERE username != 'admin-plus';

-- 验证结果 - 应该只剩下admin-plus
SELECT id, username, role, status, created_at 
FROM user 
ORDER BY id;

-- 如果需要重置admin-plus的ID为1，可以执行以下操作（可选）
-- 注意：这会修改主键，请谨慎操作
/*
-- 临时禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 更新admin-plus的ID为1
UPDATE user SET id = 1 WHERE username = 'admin-plus';

-- 重置自增ID
ALTER TABLE user AUTO_INCREMENT = 2;

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 验证最终结果
SELECT id, username, role, status, created_at 
FROM user 
ORDER BY id;
*/
