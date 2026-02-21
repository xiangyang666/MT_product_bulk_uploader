@echo off
echo ========================================
echo 生成模板错误诊断工具
echo ========================================
echo.

echo [检查1] 测试后端是否运行...
echo.
curl -s http://localhost:8080/actuator/health
if errorlevel 1 (
    echo.
    echo ❌ 后端未运行！
    echo.
    echo 解决方法：
    echo 1. 双击运行 start-backend.bat
    echo 2. 等待30-60秒直到看到 "Started MeituanProductApplication"
    echo 3. 重新运行此诊断脚本
    echo.
    pause
    exit /b 1
) else (
    echo.
    echo ✅ 后端正在运行
    echo.
)

echo [检查2] 测试生成模板API...
echo.
echo 正在测试API（使用商品ID 1,2,3）...
curl -X POST http://localhost:8080/api/products/generate-template ^
  -H "Content-Type: application/json" ^
  -d "{\"productIds\": [1, 2, 3]}" ^
  -o test_template.xlsx ^
  -w "\nHTTP状态码: %%{http_code}\n"

echo.
if exist test_template.xlsx (
    echo ✅ API测试成功！文件已生成：test_template.xlsx
    echo.
    echo 这说明后端API工作正常。
    echo 问题可能在前端请求或数据上。
    echo.
    echo 请检查：
    echo 1. 前端是否选择了有效的商品ID
    echo 2. 浏览器F12中的Network请求详情
    del test_template.xlsx
) else (
    echo ❌ API测试失败！
    echo.
    echo 可能的原因：
    echo 1. 数据库中没有ID为1,2,3的商品
    echo 2. TemplateConfig加载失败
    echo 3. 其他后端错误
    echo.
    echo 请查看后端命令行窗口中的错误日志！
    echo 查找关键词：ERROR, Exception, 生成模板失败
)

echo.
echo ========================================
echo 诊断完成
echo ========================================
echo.
pause
