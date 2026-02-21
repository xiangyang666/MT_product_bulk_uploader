@echo off
echo ========================================
echo 正在打包 Landing Page (生产环境)
echo ========================================
echo.

echo [1/2] 清理旧的构建文件...
if exist dist rmdir /s /q dist

echo [2/2] 开始构建...
call npm run build

echo.
echo ========================================
echo 打包完成!
echo 构建文件位于: dist 目录
echo ========================================
pause
