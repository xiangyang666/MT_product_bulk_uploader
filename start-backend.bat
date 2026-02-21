@echo off
echo ========================================
echo 美团商品批量上传管理工具 - 后端启动
echo ========================================
echo.

echo [1/3] 检查Java环境...
java -version
if errorlevel 1 (
    echo 错误: 未找到Java环境，请先安装Java 17+
    pause
    exit /b 1
)
echo.

echo [2/3] 检查Maven环境...
mvn -version
if errorlevel 1 (
    echo 错误: 未找到Maven环境，请先安装Maven 3.6+
    pause
    exit /b 1
)
echo.

echo [3/3] 启动后端服务...
cd meituan-backend
mvn spring-boot:run

pause
