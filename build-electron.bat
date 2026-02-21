@echo off
echo ========================================
echo 美团商品上传工具 - Electron 桌面应用打包
echo ========================================
echo.

cd meituan-frontend

echo [1/5] 检查 Node.js 和 npm...
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
echo [2/5] 安装依赖（如果需要）...
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
echo [3/5] 构建前端资源...
call npm run build
if errorlevel 1 (
    echo 前端构建失败！
    pause
    exit /b 1
)

echo.
echo [4/5] 打包 Electron 应用（Windows）...
call npm run electron:build:win
if errorlevel 1 (
    echo Electron 打包失败！
    pause
    exit /b 1
)

echo.
echo [5/5] 打包完成！
echo.
echo ========================================
echo 输出目录:
echo meituan-frontend\dist-electron
echo ========================================
echo.
echo 安装包位置:
dir dist-electron\*.exe /b 2>nul
if errorlevel 1 (
    echo 未找到 .exe 文件
) else (
    for %%f in (dist-electron\*.exe) do (
        echo %%~nxf - %%~zf 字节
    )
)
echo.

echo 使用说明:
echo 1. 在 dist-electron 目录找到安装包
echo 2. 双击安装包进行安装
echo 3. 安装后即可使用桌面应用
echo.

pause
