@echo off
chcp 65001 >nul
echo ========================================
echo 美团商品上传工具 - Windows 打包脚本
echo ========================================
echo.

echo [1/3] 检查依赖...
if not exist "node_modules" (
    echo 未找到 node_modules，正在安装依赖...
    call pnpm install
    if errorlevel 1 (
        echo 依赖安装失败，请检查网络连接
        pause
        exit /b 1
    )
) else (
    echo 依赖已安装
)
echo.

echo [2/3] 构建前端代码...
call pnpm run build
if errorlevel 1 (
    echo 前端构建失败
    pause
    exit /b 1
)
echo.

echo [3/3] 打包 Windows 应用...
call pnpm run electron:build:win
if errorlevel 1 (
    echo 打包失败
    pause
    exit /b 1
)
echo.

echo ========================================
echo 打包完成！
echo 安装包位置: dist-electron\
echo ========================================
pause

