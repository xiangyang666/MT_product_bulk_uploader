@echo off
echo ========================================
echo 美团商品批量上传管理工具 - 前端启动
echo ========================================
echo.

echo [1/2] 检查Node.js环境...
node -v
if errorlevel 1 (
    echo 错误: 未找到Node.js环境，请先安装Node.js 16+
    pause
    exit /b 1
)
echo.

echo [2/2] 启动前端应用...
cd meituan-frontend

echo 检查依赖是否已安装...
if not exist "node_modules" (
    echo 首次运行，正在安装依赖...
    npm install
)

echo 启动Electron应用...
npm run electron:dev

pause
