#!/bin/bash
# 快速修复 Nginx 配置脚本
# 使用方法：ssh root@106.55.102.48 'bash -s' < 快速修复-Nginx配置.sh

echo "=========================================="
echo "开始修复 Nginx 配置"
echo "=========================================="

# 1. 备份当前配置
echo "1. 备份当前配置..."
cp /etc/nginx/sites-available/default /etc/nginx/sites-available/default.backup.$(date +%Y%m%d_%H%M%S)
echo "✓ 备份完成"

# 2. 创建新配置
echo "2. 创建新配置..."
cat > /etc/nginx/sites-available/default << 'EOF'
server {
    listen 80;
    server_name 106.55.102.48;
    
    # ==================== 大文件上传优化配置 ====================
    # 客户端请求体最大大小（上传文件大小限制）
    client_max_body_size 500M;
    
    # 客户端请求体缓冲区大小
    client_body_buffer_size 128k;
    
    # 客户端请求体超时时间
    client_body_timeout 600s;
    
    # 发送超时时间
    send_timeout 600s;
    # ==================== 大文件上传优化配置结束 ====================
    
    index index.php index.html index.htm default.php default.htm default.html;
    access_log /www/sites/106.55.102.48/log/access.log main;
    error_log /www/sites/106.55.102.48/log/error.log;
    
    # ==================== 后端 API 反向代理配置 ====================
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 代理超时配置（重要！）
        proxy_connect_timeout 600s;
        proxy_send_timeout 600s;
        proxy_read_timeout 600s;
        
        # 代理缓冲区配置
        proxy_buffer_size 64k;
        proxy_buffers 4 64k;
        proxy_busy_buffers_size 128k;
        
        # 临时文件写入大小
        proxy_temp_file_write_size 128k;
        
        # 禁用代理缓冲（对于大文件上传非常重要！）
        proxy_request_buffering off;
    }
    # ==================== 后端 API 反向代理配置结束 ====================
    
    location ~ ^/(\.user.ini|\.htaccess|\.git|\.env|\.svn|\.project|LICENSE|README.md) {
        return 404;
    }
    
    location ^~ /.well-known/acme-challenge {
        allow all;
        root /usr/share/nginx/html;
    }
    
    if ( $uri ~ "^/\.well-known/.*\.(php|jsp|py|js|css|lua|ts|go|zip|tar\.gz|rar|7z|sql|bak)$" ) {
        return 403;
    }
    
    root /www/sites/106.55.102.48/index;
    error_page 404 /404.html;
}
EOF
echo "✓ 配置文件已创建"

# 3. 测试配置
echo "3. 测试 Nginx 配置..."
nginx -t
if [ $? -eq 0 ]; then
    echo "✓ 配置测试通过"
else
    echo "✗ 配置测试失败，恢复备份..."
    cp /etc/nginx/sites-available/default.backup.$(date +%Y%m%d)* /etc/nginx/sites-available/default
    exit 1
fi

# 4. 重启 Nginx
echo "4. 重启 Nginx..."
systemctl restart nginx
if [ $? -eq 0 ]; then
    echo "✓ Nginx 重启成功"
else
    echo "✗ Nginx 重启失败"
    systemctl status nginx
    exit 1
fi

# 5. 验证配置
echo "5. 验证配置..."
echo "检查 client_max_body_size:"
grep "client_max_body_size" /etc/nginx/sites-available/default
echo ""
echo "检查反向代理配置:"
grep -A 2 "location /api/" /etc/nginx/sites-available/default

echo ""
echo "=========================================="
echo "✓ Nginx 配置修复完成！"
echo "=========================================="
echo ""
echo "现在可以重新测试文件上传功能了"
