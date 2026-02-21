@echo off
echo ========================================
echo 美团商品上传工具 - 前端打包脚本
echo ========================================
echo.

cd meituan-frontend

echo [1/4] 检查 Node.js 和 npm...
node --version
if errorlevel 1 (
    echo 错误：未找到 Node.js，请先安装 Node.js
    pause
    exit /b 1
)

npm --version
if errorlevel 1 (
    echo 错误：未找到 npm
    pause
    exit /b 1
)

echo.
echo [2/4] 安装依赖（如果需要）...
if not exist "node_modules" (
    echo 正在安装依赖...
    call npm install
    if errorlevel 1 (
        echo 依赖安装失败！
        pause
        exit /b 1
    )
) else (
    echo 依赖已存在，跳过安装
)

echo.
echo [3/4] 构建生产版本...
call npm run build
if errorlevel 1 (
    echo 构建失败！
    pause
    exit /b 1
)

echo.
echo [4/4] 构建完成！
echo.
echo ========================================
echo 输出目录:
echo meituan-frontend\dist
echo ========================================
echo.
echo 文件列表:
dir dist /b
echo.

echo 部署说明:
echo 1. 将 dist 目录下的所有文件上传到 Web 服务器
echo 2. 配置 Nginx 或其他 Web 服务器指向 dist 目录
echo 3. 确保后端 API 地址正确: http://106.55.102.48:8080
echo.

pause
