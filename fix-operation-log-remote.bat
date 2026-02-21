@echo off
echo ========================================
echo 修复远程数据库 operation_log 表
echo 添加 target_username 字段
echo ========================================
echo.

echo 连接到远程数据库: 106.55.102.48:3306
echo 数据库: meituan_product
echo.

echo 正在添加 target_username 字段...
mysql -h 106.55.102.48 -P 3306 -u root -pmysql_G4EcQ6 meituan_product -e "ALTER TABLE operation_log ADD COLUMN target_username VARCHAR(50) COMMENT '目标用户名（用于成员管理操作）' AFTER target_id;"

if errorlevel 1 (
    echo.
    echo 字段可能已存在，检查字段状态...
)

echo.
echo 验证字段是否存在...
mysql -h 106.55.102.48 -P 3306 -u root -pmysql_G4EcQ6 meituan_product -e "SHOW COLUMNS FROM operation_log WHERE Field='target_username';"

echo.
echo ========================================
echo 完成！
echo ========================================
pause
