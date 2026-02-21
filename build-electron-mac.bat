@echo off
echo ========================================
echo 美团商品上传工具 - Electron macOS 应用打包
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
echo [4/5] 打包 Electron 应用（macOS - x64 和 arm64）...
call npm run electron:build:mac
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
dir dist-electron\*.dmg /b 2>nul
if errorlevel 1 (
    echo 未找到 .dmg 文件
) else (
    for %%f in (dist-electron\*.dmg) do (
        echo %%~nxf - %%~zf 字节
    )
)
echo.

echo 使用说明:
echo 1. 在 dist-electron 目录找到 .dmg 安装包
echo 2. 将 .dmg 文件传输到 Mac 电脑
echo 3. 在 Mac 上双击 .dmg 文件进行安装
echo 4. 拖动应用到 Applications 文件夹
echo.
echo 注意：
echo - 生成了 x64 (Intel) 和 arm64 (Apple Silicon) 两个版本
echo - 在 Windows 上打包 macOS 应用可能需要额外配置
echo.

pause

