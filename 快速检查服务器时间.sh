#!/bin/bash
# 快速检查服务器时间脚本

echo "=========================================="
echo "服务器时间检查"
echo "=========================================="

# 1. 显示当前系统时间
echo "1. 当前系统时间："
date "+%Y-%m-%d %H:%M:%S %Z"
echo ""

# 2. 显示时区设置
echo "2. 时区设置："
if command -v timedatectl &> /dev/null; then
    timedatectl | grep "Time zone"
else
    cat /etc/timezone 2>/dev/null || echo "无法获取时区信息"
fi
echo ""

# 3. 检查 NTP 同步状态
echo "3. NTP 时间同步状态："
if command -v timedatectl &> /dev/null; then
    timedatectl | grep "NTP"
else
    echo "timedatectl 命令不可用"
fi
echo ""

# 4. 检查年份是否正确
CURRENT_YEAR=$(date +%Y)
echo "4. 当前年份：$CURRENT_YEAR"
if [ "$CURRENT_YEAR" -gt 2025 ]; then
    echo "⚠️  警告：系统时间年份异常（$CURRENT_YEAR），应该是 2025 年"
elif [ "$CURRENT_YEAR" -lt 2025 ]; then
    echo "⚠️  警告：系统时间年份过旧（$CURRENT_YEAR）"
else
    echo "✓ 年份正确"
fi
echo ""

# 5. 检查数据库中的时间数据
echo "5. 检查数据库中的用户创建时间："
mysql -u root -p -e "
USE meituan_product;
SELECT 
    id, 
    username, 
    created_at,
    YEAR(created_at) as year
FROM users 
ORDER BY created_at DESC 
LIMIT 5;
" 2>/dev/null || echo "无法连接数据库（需要输入密码）"
echo ""

echo "=========================================="
echo "检查完成"
echo "=========================================="
echo ""
echo "如果发现时间错误，请参考 '检查并修复服务器时间.md' 文档进行修复"
