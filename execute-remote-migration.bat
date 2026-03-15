@echo off
chcp 65001 >nul
echo ========================================
echo 执行远程数据库迁移脚本
echo ========================================
echo.
echo 远程服务器: 106.55.102.48:3306
echo 数据库: meituan_product
echo.

mysql -h 106.55.102.48 -P 3306 -u root -pmysql_G4EcQ6 meituan_product < database-migration-no-reason-return-tag.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo ✅ 数据库迁移成功！
    echo ========================================
    echo.
    echo 正在验证字段是否添加成功...
    echo.
    mysql -h 106.55.102.48 -P 3306 -u root -pmysql_G4EcQ6 -e "USE meituan_product; SHOW COLUMNS FROM t_product LIKE 'no_reason_return%%';"
    echo.
    echo 正在查看数据转换结果...
    echo.
    mysql -h 106.55.102.48 -P 3306 -u root -pmysql_G4EcQ6 -e "USE meituan_product; SELECT no_reason_return, no_reason_return_tag_id, COUNT(*) as count FROM t_product GROUP BY no_reason_return, no_reason_return_tag_id;"
) else (
    echo.
    echo ========================================
    echo ❌ 数据库迁移失败！
    echo ========================================
    echo 错误代码: %ERRORLEVEL%
)

echo.
pause
