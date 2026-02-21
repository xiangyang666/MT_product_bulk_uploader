@echo off
echo ========================================
echo 美团商品批量上传管理工具 - 一键启动
echo ========================================
echo.

echo 提示: 此脚本将依次启动以下服务:
echo 1. 后端Spring Boot服务
echo 2. 前端Electron应用
echo.
echo 注意: MySQL、Redis、MinIO已部署在远程服务器上
echo 服务器地址: 106.55.102.48
echo.
echo 请确保已完成以下准备工作:
echo - 已安装Java 17+
echo - 已安装Maven 3.6+
echo - 已安装Node.js 16+
echo - 已创建数据库并导入表结构
echo - 远程服务器可访问
echo.

pause

echo.
echo [步骤 1/2] 启动后端服务...
echo.
start "后端服务" cmd /k "start-backend.bat"
timeout /t 5

echo.
echo [步骤 2/2] 启动前端应用...
echo 等待30秒让后端完全启动...
timeout /t 30
echo.
start "前端应用" cmd /k "start-frontend.bat"

echo.
echo ========================================
echo 所有服务启动完成！
echo ========================================
echo.
echo 服务地址:
echo - 远程MySQL: 106.55.102.48:3306
echo - 远程Redis: 106.55.102.48:6379
echo - 远程MinIO控制台: http://106.55.102.48:9001
echo - 后端API: http://localhost:8080
echo - 前端应用: 将自动打开Electron窗口
echo.
echo 按任意键关闭此窗口...
pause > nul
