@echo off
echo ========================================
echo MinIO 对象存储服务启动
echo ========================================
echo.

echo 配置信息:
echo - API端口: 9000
echo - 控制台端口: 9001
echo - 用户名: minio_cf4STY
echo - 密码: minio_ZGBzK7
echo - 数据目录: %CD%\minio-data
echo.

echo 创建数据目录...
if not exist "minio-data" mkdir minio-data

echo.
echo 启动MinIO服务器...
echo 控制台地址: http://localhost:9001
echo API地址: http://localhost:9000
echo.

set MINIO_ROOT_USER=minio_cf4STY
set MINIO_ROOT_PASSWORD=minio_ZGBzK7

minio.exe server minio-data --console-address ":9001"

pause
