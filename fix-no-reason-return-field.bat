@echo off
chcp 65001 >nul
REM 无理由退货字段映射问题 - 完整修复脚本 (Windows版本)
REM 问题：导入"7天无理由退货（一次性包装破损不支持）"后，导出显示为"1300030895"而非"1300030902"

echo ========================================
echo 无理由退货字段映射问题修复
echo ========================================
echo.

set DB_HOST=106.55.102.48
set DB_PORT=3306
set DB_NAME=meituan_product
set DB_USER=root
set DB_PASS=mysql_G4EcQ6

echo 步骤 1/6: 检查数据库字段...
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS% %DB_NAME% -e "SHOW COLUMNS FROM t_product LIKE 'no_reason_return_tag_id';" 2>&1 | findstr /v "Warning"

if errorlevel 1 (
    echo ❌ 字段不存在，正在添加...
    mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS% %DB_NAME% -e "ALTER TABLE t_product ADD COLUMN no_reason_return_tag_id VARCHAR(20) DEFAULT NULL COMMENT '无理由退货标签ID' AFTER no_reason_return;" 2>&1 | findstr /v "Warning"
    echo ✅ 字段添加成功
) else (
    echo ✅ 字段已存在
)

echo.
echo 步骤 2/6: 查看现有数据...
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS% %DB_NAME% -e "SELECT COUNT(*) as '当前商品数量' FROM t_product WHERE merchant_id = 1 AND deleted = 0;" 2>&1 | findstr /v "Warning"

echo.
set /p confirm="确认删除这些数据并重新导入吗？(y/n): "
if /i "%confirm%"=="y" (
    mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS% %DB_NAME% -e "DELETE FROM t_product WHERE merchant_id = 1;" 2>&1 | findstr /v "Warning"
    echo ✅ 数据清理完成
) else (
    echo ⚠️  跳过数据清理
)

echo.
echo 步骤 3/6: 重新编译应用...
cd meituan-backend
call mvn clean package -DskipTests -q
if errorlevel 1 (
    echo ❌ 编译失败
    pause
    exit /b 1
)
echo ✅ 编译成功

echo.
echo 步骤 4/6: 停止现有应用...
for /f "tokens=2" %%i in ('tasklist ^| findstr /i "meituan-product.*jar"') do set PID=%%i
if defined PID (
    taskkill /F /PID %PID% >nul 2>&1
    echo ✅ 应用已停止
    timeout /t 3 /nobreak >nul
) else (
    echo ⚠️  未找到运行中的应用
)

echo.
echo 步骤 5/6: 启动应用...
start /B java -jar target\meituan-product-1.0.0.jar > ..\springboot-restart.log 2>&1
echo ✅ 应用启动中...
timeout /t 5 /nobreak >nul

echo.
echo 步骤 6/6: 验证修复...
echo 等待应用完全启动（10秒）...
timeout /t 10 /nobreak >nul

echo.
echo === 最近的日志 ===
tail -30 ..\springboot-restart.log | findstr /i "Started MeituanProductApplication 无理由退货 noreasonreturn"

echo.
echo ========================================
echo ✅ 修复完成！
echo ========================================
echo.
echo 下一步：
echo 1. 在浏览器中访问应用：http://localhost:8080
echo 2. 重新导入Excel文件（包含无理由退货字段）
echo 3. 导出Excel文件，检查无理由退货列
echo.
echo 预期结果：
echo - 导入: 7天无理由退货（一次性包装破损不支持）
echo - 导出: 1300030902  ✅
echo.
echo 如果问题仍然存在，查看日志：
echo   tail -f meituan-backend\springboot-restart.log
echo.
pause
