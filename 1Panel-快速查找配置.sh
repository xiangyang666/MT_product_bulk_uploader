#!/bin/bash
# 1Panel 快速查找配置脚本

echo "=========================================="
echo "1Panel Nginx 配置查找"
echo "=========================================="

# 1. 查找 1Panel 端口
echo "1. 1Panel 管理端口："
if [ -f /usr/local/1panel/1panel.conf ]; then
    cat /usr/local/1panel/1panel.conf | grep port
else
    echo "未找到 1Panel 配置文件"
fi
echo ""

# 2. 查找站点配置文件
echo "2. 站点配置文件位置："
find /opt/1panel -name "*106.55.102.48*.conf" -type f 2>/dev/null
echo ""

# 3. 查找所有 vhost 配置目录
echo "3. vhost 配置目录："
find /opt/1panel -type d -name "vhost" -o -name "conf.d" 2>/dev/null | head -5
echo ""

# 4. 查看 OpenResty/Nginx 状态
echo "4. Nginx 服务状态："
if systemctl status openresty >/dev/null 2>&1; then
    echo "✓ OpenResty 正在运行"
    systemctl status openresty | grep Active
elif systemctl status nginx >/dev/null 2>&1; then
    echo "✓ Nginx 正在运行"
    systemctl status nginx | grep Active
else
    echo "✗ Nginx 服务未运行"
fi
echo ""

# 5. 查看当前站点配置内容
echo "5. 当前站点配置内容："
CONFIG_FILE=$(find /opt/1panel -name "*106.55.102.48*.conf" -type f 2>/dev/null | head -1)
if [ -n "$CONFIG_FILE" ]; then
    echo "配置文件: $CONFIG_FILE"
    echo "---"
    cat "$CONFIG_FILE"
else
    echo "未找到站点配置文件"
fi
echo ""

# 6. 检查是否已有大文件配置
echo "6. 检查大文件上传配置："
if [ -n "$CONFIG_FILE" ]; then
    if grep -q "client_max_body_size" "$CONFIG_FILE"; then
        echo "✓ 已配置 client_max_body_size："
        grep "client_max_body_size" "$CONFIG_FILE"
    else
        echo "✗ 未配置 client_max_body_size（需要添加）"
    fi
    
    if grep -q "location /api/" "$CONFIG_FILE"; then
        echo "✓ 已配置反向代理 /api/"
    else
        echo "✗ 未配置反向代理 /api/（需要添加）"
    fi
else
    echo "无法检查，未找到配置文件"
fi
echo ""

# 7. 查看后端服务状态
echo "7. 后端服务状态（8080端口）："
if netstat -tlnp 2>/dev/null | grep :8080 >/dev/null; then
    echo "✓ 后端服务正在运行"
    netstat -tlnp | grep :8080
else
    echo "✗ 后端服务未运行"
fi
echo ""

# 8. 查看 MinIO 服务状态
echo "8. MinIO 服务状态（9000端口）："
if netstat -tlnp 2>/dev/null | grep :9000 >/dev/null; then
    echo "✓ MinIO 服务正在运行"
    netstat -tlnp | grep :9000
else
    echo "✗ MinIO 服务未运行"
fi
echo ""

echo "=========================================="
echo "查找完成"
echo "=========================================="
echo ""
echo "下一步操作："
echo "1. 登录 1Panel: http://106.55.102.48:端口号"
echo "2. 进入 '网站' -> 找到 106.55.102.48 站点"
echo "3. 点击 '配置' 或 '编辑'"
echo "4. 添加大文件上传和反向代理配置"
echo "5. 保存并重启"
