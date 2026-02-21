-- ============================================
-- 更新管理员密码为 123456
-- ============================================

USE `meituan_product`;

-- BCrypt hash for password "123456" (strength=10)
-- 生成方式: new BCryptPasswordEncoder(10).encode("123456")
UPDATE `user` 
SET `password` = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG'
WHERE `username` = 'admin';

-- 验证更新
SELECT 
  `id`, 
  `username`, 
  `role`, 
  `status`,
  LEFT(`password`, 20) AS password_prefix,
  `created_at`
FROM `user` 
WHERE `username` = 'admin';

SELECT '管理员密码已更新为: 123456' AS message;
