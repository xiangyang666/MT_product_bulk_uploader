@echo off
echo ========================================
echo 导入开发者工具密码功能数据库
echo ========================================
echo.

mysql -u root -p123456 meituan_product < database-migration-dev-tools-password.sql

echo.
echo ========================================
echo 验证表是否创建成功
echo ========================================
mysql -u root -p123456 meituan_product -e "SHOW TABLES LIKE 'dev_tools_config';"

echo.
echo ========================================
echo 查看表结构
echo ========================================
mysql -u root -p123456 meituan_product -e "DESC dev_tools_config;"

echo.
echo ========================================
echo 查看初始数据
echo ========================================
mysql -u root -p123456 meituan_product -e "SELECT * FROM dev_tools_config;"

echo.
echo 导入完成！
pause
