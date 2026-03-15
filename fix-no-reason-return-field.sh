#!/bin/bash

# 无理由退货��段映射问题 - 完整修复脚本
# 问题：导入"7天无理由退货（一次性包装破损不支持）"后，导出显示为"1300030895"而非"1300030902"

echo "========================================"
echo "无理由退货字段映射问题修复"
echo "========================================"
echo ""

DB_HOST="106.55.102.48"
DB_PORT="3306"
DB_NAME="meituan_product"
DB_USER="root"
DB_PASS="mysql_G4EcQ6"

echo "步骤 1/6: 检查数据库字段..."
mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME -e "SHOW COLUMNS FROM t_product LIKE 'no_reason_return_tag_id';" 2>&1 | grep -v "Warning"

if [ $? -ne 0 ]; then
    echo "❌ 字段不存在，正在添加..."
    mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME << 'EOF' 2>&1 | grep -v "Warning"
ALTER TABLE t_product ADD COLUMN no_reason_return_tag_id VARCHAR(20) DEFAULT NULL COMMENT '无理由退货标签ID' AFTER no_reason_return;
EOF
    echo "✅ 字段添加成功"
else
    echo "✅ 字段已存在"
fi

echo ""
echo "步骤 2/6: 清理旧数据（merchant_id=1的数据）..."
mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME << 'EOF' 2>&1 | grep -v "Warning"
-- 备份查询（实际执行前会显示影响的行数）
SELECT COUNT(*) as '将删除的商品数量' FROM t_product WHERE merchant_id = 1 AND deleted = 0;
EOF

echo ""
read -p "确认删除这些数据吗？(y/n): " confirm
if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
    mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME << 'EOF' 2>&1 | grep -v "Warning"
DELETE FROM t_product WHERE merchant_id = 1;
EOF
    echo "✅ 数据清理完成"
else
    echo "⚠️  跳过数据清理"
fi

echo ""
echo "步骤 3/6: 重新编译应用..."
cd meituan-backend
mvn clean package -DskipTests -q
if [ $? -eq 0 ]; then
    echo "✅ 编译成功"
else
    echo "❌ 编译失败"
    exit 1
fi

echo ""
echo "步骤 4/6: 停止现有应用..."
# 查找Java进程并停止
PID=$(ps aux | grep '[m]eituan-product.*jar' | awk '{print $2}')
if [ -n "$PID" ]; then
    kill $PID
    echo "✅ 应用已停止 (PID: $PID)"
    sleep 3
else
    echo "⚠️  未找到运行中的应用"
fi

echo ""
echo "步骤 5/6: 启动应用..."
# 后台启动应用
nohup java -jar target/meituan-product-1.0.0.jar > springboot-restart.log 2>&1 &
NEW_PID=$!
echo "✅ 应用启动中... (PID: $NEW_PID)"
sleep 5

# 检查应用是否启动成功
if ps -p $NEW_PID > /dev/null; then
    echo "✅ 应用启动成功"
else
    echo "❌ 应用启动失败，查看日志:"
    tail -50 springboot-restart.log
    exit 1
fi

echo ""
echo "步骤 6/6: 验证修复..."
echo "等待应用完全启动（10秒）..."
sleep 10

# 检查日志
echo ""
echo "=== 最近的日志 ==="
tail -30 springboot-restart.log | grep -E "Started MeituanProductApplication|无理由退货|noreasonreturn"

echo ""
echo "========================================"
echo "✅ 修复完成！"
echo "========================================"
echo ""
echo "下一步："
echo "1. 在浏览器中访问应用：http://localhost:8080"
echo "2. 重新导入Excel文件（包含无理由退货字段）"
echo "3. 导出Excel文件，检查无理由退货列"
echo ""
echo "预期结果："
echo "- 导入: 7天无理由退货（一次性包装破损不支持）"
echo "- 导出: 1300030902  ✅"
echo ""
echo "如果问题仍然存在，查看日志："
echo "  tail -f meituan-backend/springboot-restart.log | grep '无理由退货'"
echo ""
