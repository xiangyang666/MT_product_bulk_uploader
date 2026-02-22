@echo off
echo 正在创建公司信息表...
mysql -h 106.55.102.48 -u root -pmysql_G4EcQ6 meituan_product < create-company-info-table.sql
if %errorlevel% equ 0 (
    echo 公司信息表创建成功！
) else (
    echo 创建失败，请检查数据库连接
)
pause
