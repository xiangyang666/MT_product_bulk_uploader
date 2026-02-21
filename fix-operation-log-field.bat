@echo off
echo ========================================
echo 修复 operation_log 表 - 添加 target_username 字段
echo ========================================
echo.

echo 正在添加 target_username 字段...
mysql -u root -p123456 meituan_product -e "ALTER TABLE operation_log ADD COLUMN IF NOT EXISTS target_username VARCHAR(50) COMMENT '目标用户名（用于成员管理操作）' AFTER target_id;"

if errorlevel 1 (
    echo.
    echo 尝试使用标准 ALTER TABLE 语句...
    mysql -u root -p123456 meituan_product -e "ALTER TABLE operation_log ADD COLUMN target_username VARCHAR(50) COMMENT '目标用户名（用于成员管理操作）' AFTER target_id;"
)

echo.
echo 验证字段是否添加成功...
mysql -u root -p123456 meituan_product -e "SHOW COLUMNS FROM operation_log WHERE Field='target_username';"

echo.
echo ========================================
echo 完成！
echo ========================================
pause
