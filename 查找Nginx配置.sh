#!/bin/bash
# 查找 Nginx 配置文件位置

echo "=========================================="
echo "查找 Nginx 配置文件"
echo "=========================================="

# 1. 查找 nginx 可执行文件
echo "1. 查找 nginx 可执行文件..."
which nginx 2>/dev/null || find /usr -name nginx -type f 2>/dev/null | head -5

# 2. 查找 nginx 配置文件
echo ""
echo "2. 查找 nginx.conf 配置文件..."
find / -name "nginx.conf" -type f 2>/dev/null | head -10

# 3. 查找宝塔 Nginx 配置
echo ""
echo "3. 查找宝塔 Nginx 配置..."
if [ -d "/www/server/nginx" ]; then
    echo "✓ 找到宝塔 Nginx 目录"
    ls -la /www/server/nginx/conf/
fi

# 4. 查找站点配置文件
echo ""
echo "4. 查找站点配置文件..."
find /www -name "*106.55.102.48*.conf" -type f 2>/dev/null

# 5. 查找所有 vhost 配置
echo ""
echo "5. 查找 vhost 配置目录..."
find / -type d -name "vhost" 2>/dev/null | head -5

# 6. 检查 Nginx 进程
echo ""
echo "6. 检查 Nginx 进程..."
ps aux | grep nginx | grep -v grep

# 7. 查找 Nginx 配置中的 include 指令
echo ""
echo "7. 查找主配置文件..."
for conf in $(find / -name "nginx.conf" -type f 2>/dev/null); do
    echo "检查: $conf"
    grep -E "include.*vhost|include.*sites" "$conf" 2>/dev/null
done

echo ""
echo "=========================================="
echo "查找完成"
echo "=========================================="
