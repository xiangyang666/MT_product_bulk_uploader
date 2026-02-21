@echo off
echo ========================================
echo 美团商品上传工具 - 后端打包脚本
echo ========================================
echo.

cd meituan-backend

echo [1/3] 清理旧的构建文件...
call mvn clean
if errorlevel 1 (
    echo 清理失败！
    pause
    exit /b 1
)

echo.
echo [2/3] 编译并打包项目（跳过测试）...
call mvn package -DskipTests -Dmaven.test.skip=true
if errorlevel 1 (
    echo 打包失败！
    pause
    exit /b 1
)

echo.
echo [3/3] 打包完成！
echo.
echo ========================================
echo 输出文件位置:
echo meituan-backend\target\app.jar
echo ========================================
echo.
echo 文件大小:
dir target\app.jar | find "app.jar"
echo.

echo 启动命令:
echo java -jar target\app.jar
echo.
echo 部署到服务器:
echo scp target\app.jar root@106.55.102.48:/opt/meituan/
echo.

pause
