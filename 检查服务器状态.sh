#!/bin/bash
# 检查服务器状态脚本
# 使用方法：ssh root@106.55.102.48 'bash -s' < 检查服务器状态.sh

echo "=========================================="
echo "检查服务器状态"
echo "=========================================="

# 1. 检查后端服务（8080端口）
echo "1. 检查后端服务（8080端口）..."
if netstat -tlnp | grep :8080 > /dev/null; then
    echo "✓ 后端服务正在运行"
    netstat -tlnp | grep :8080
else
    echo "✗ 后端服务未运行！"
fi
echo ""

# 2. 检查 MinIO 服务（9000端口）
echo "2. 检查 MinIO 服务（9000端口）..."
if netstat -tlnp | grep :9000 > /dev/null; then
    echo "✓ MinIO 服务正在运行"
    netstat -tlnp | grep :9000
else
    echo "✗ MinIO 服务未运行！"
fi
echo ""

# 3. 检查 Nginx 配置
echo "3. 检查 Nginx 配置..."
echo "client_max_body_size 配置："
grep "client_max_body_size" /etc/nginx/sites-available/default || echo "✗ 未找到 client_max_body_size 配置"
echo ""
echo "反向代理配置："
grep -A 2 "location /api/" /etc/nginx/sites-available/default || echo "✗ 未找到反向代理配置"
echo ""

# 4. 检查磁盘空间
echo "4. 检查磁盘空间..."
df -h | grep -E "Filesystem|/$"
echo ""

# 5. 检查内存使用
echo "5. 检查内存使用..."
free -h
echo ""

# 6. 查看最近的 Nginx 错误日志
echo "6. 最近的 Nginx 错误日志（最后 20 行）..."
if [ -f /www/sites/106.55.102.48/log/error.log ]; then
    tail -20 /www/sites/106.55.102.48/log/error.log
else
    echo "未找到日志文件"
fi
echo ""

# 7. 查看后端日志（如果存在）
echo "7. 查找后端日志文件..."
find /www -name "*.log" -type f -mmin -60 2>/dev/null | head -5
echo ""

echo "=========================================="
echo "检查完成"
echo "=========================================="
