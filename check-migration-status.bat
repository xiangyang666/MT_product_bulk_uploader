@echo off
chcp 65001 >nul
echo ========================================
echo 检查数据库迁移状态
echo ========================================
echo.

echo 1. 检查字段是否存在...
mysql -h 106.55.102.48 -P 3306 -u root -pmysql_G4EcQ6 meituan_product -e "SHOW COLUMNS FROM t_product LIKE 'no_reason_return%%';"

echo.
echo 2. 查看商品总数...
mysql -h 106.55.102.48 -P 3306 -u root -pmysql_G4EcQ6 meituan_product -e "SELECT COUNT(*) as total_products FROM t_product;"

echo.
echo 3. 查看标签ID分布...
mysql -h 106.55.102.48 -P 3306 -u root -pmysql_G4EcQ6 meituan_product -e "SELECT no_reason_return_tag_id, COUNT(*) as count FROM t_product GROUP BY no_reason_return_tag_id LIMIT 10;"

echo.
echo 4. 查看最近5条商品...
mysql -h 106.55.102.48 -P 3306 -u root -pmysql_G4EcQ6 meituan_product -e "SELECT id, LEFT(product_name, 40) as name, no_reason_return_tag_id FROM t_product ORDER BY id DESC LIMIT 5;"

echo.
pause
