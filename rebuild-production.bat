@echo off
chcp 65001 >nul
echo ========================================
echo 重新打包生产环境 Electron 应用
echo ========================================
echo.

cd meituan-frontend

echo [1/3] 清理旧的构建文件...
if exist dist rmdir /s /q dist
if exist dist-electron rmdir /s /q dist-electron

echo.
echo [2/3] 使用生产环境配置构建前端...
call npm run build

echo.
echo [3/3] 打包 Electron 应用...
call npm run electron:build:win

echo.
echo ========================================
echo 打包完成！
echo 输出目录: meituan-frontend\dist-electron
echo ========================================
pause
