# 1Panel 修改 Nginx 配置 - 操作指南

## 方法 1：通过 1Panel Web 界面修改（推荐）

### 步骤 1：登录 1Panel
1. 浏览器打开：`http://106.55.102.48:端口号`
   - 默认端口通常是：`8090`、`8888`、`9999` 或其他
   - 如果不确定端口，可以在服务器执行：`cat /usr/local/1panel/1panel.conf | grep port`

2. 输入用户名和密码登录

### 步骤 2：找到网站配置
1. 点击左侧菜单 **"网站"**
2. 找到 `106.55.102.48` 站点
3. 点击右侧的 **"配置"** 或 **"编辑"** 按钮

### 步骤 3：修改 Nginx 配置
1. 在配置编辑器中，找到 `server {` 块
2. 在 `server_name 106.55.102.48;` 这行之后，添加以下配置：

```nginx
# 大文件上传优化配置
client_max_body_size 500M;
client_body_buffer_size 128k;
client_body_timeout 600s;
send_timeout 600s;
```

3. 找到或添加 `location /api/` 块（如果没有就添加）：

```nginx
location /api/ {
    proxy_pass http://localhost:8080/api/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    
    # 代理超时配置
    proxy_connect_timeout 600s;
    proxy_send_timeout 600s;
    proxy_read_timeout 600s;
    
    # 代理缓冲区配置
    proxy_buffer_size 64k;
    proxy_buffers 4 64k;
    proxy_busy_buffers_size 128k;
    proxy_temp_file_write_size 128k;
    
    # 禁用代理缓冲（重要！）
    proxy_request_buffering off;
}
```

4. 点击 **"保存"** 按钮
5. 1Panel 会自动测试配置并重启 Nginx

### 完整配置示例

如果你想完全替换配置，可以使用以下完整配置：

```nginx
server {
    listen 80;
    server_name 106.55.102.48;
    
    # 大文件上传优化配置
    client_max_body_size 500M;
    client_body_buffer_size 128k;
    client_body_timeout 600s;
    send_timeout 600s;
    
    index index.php index.html index.htm default.php default.htm default.html;
    access_log /www/sites/106.55.102.48/log/access.log;
    error_log /www/sites/106.55.102.48/log/error.log;
    
    # 后端 API 反向代理配置
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 600s;
        proxy_send_timeout 600s;
        proxy_read_timeout 600s;
        proxy_buffer_size 64k;
        proxy_buffers 4 64k;
        proxy_busy_buffers_size 128k;
        proxy_temp_file_write_size 128k;
        proxy_request_buffering off;
    }
    
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
```

---

## 方法 2：通过命令行修改（如果 Web 界面不可用）

### 步骤 1：找到 1Panel Nginx 配置文件

```bash
# 查找 1Panel Nginx 配置目录
ls -la /opt/1panel/apps/openresty/*/conf/conf.d/
# 或
ls -la /www/server/nginx/conf/conf.d/
# 或
find /opt/1panel -name "*.conf" | grep 106.55.102.48
```

### 步骤 2：编辑配置文件

```bash
# 找到配置文件后，编辑它（替换为实际路径）
vi /opt/1panel/apps/openresty/*/conf/conf.d/106.55.102.48.conf
```

### 步骤 3：添加配置

在 `server {` 块中添加上面的配置内容

### 步骤 4：重启 Nginx

```bash
# 通过 1Panel 命令重启
1pctl restart

# 或者直接重启 OpenResty/Nginx
systemctl restart openresty
# 或
systemctl restart nginx
```

---

## 方法 3：查找 1Panel 管理的 Nginx 配置

在服务器上执行以下命令：

```bash
# 1. 查找 1Panel 安装目录
ls -la /opt/1panel/

# 2. 查找 OpenResty 配置（1Panel 通常使用 OpenResty）
find /opt/1panel -name "nginx.conf" -type f

# 3. 查找站点配置文件
find /opt/1panel -name "*106.55.102.48*.conf" -type f

# 4. 查看 1Panel 配置
cat /usr/local/1panel/1panel.conf

# 5. 查找 vhost 配置目录
find /opt/1panel -type d -name "vhost" -o -name "conf.d"
```

---

## 验证配置是否生效

### 方法 1：通过 1Panel 界面
1. 登录 1Panel
2. 进入 "网站" -> 找到站点 -> 点击 "配置"
3. 查看配置中是否有 `client_max_body_size 500M`

### 方法 2：通过命令行
```bash
# 查找并查看配置文件
find /opt/1panel -name "*106.55.102.48*.conf" -exec grep "client_max_body_size" {} \;

# 查看 Nginx 进程
ps aux | grep nginx

# 测试配置
nginx -t
# 或
openresty -t
```

---

## 常见问题

### Q1: 找不到 1Panel 登录地址
```bash
# 查看 1Panel 端口
cat /usr/local/1panel/1panel.conf | grep port

# 或查看进程
netstat -tlnp | grep 1panel
```

### Q2: 忘记 1Panel 密码
```bash
# 重置密码
1pctl user-info

# 或重置管理员密码
1pctl reset-password
```

### Q3: 配置保存后不生效
1. 检查 1Panel 是否显示配置错误
2. 查看 Nginx 错误日志：
```bash
tail -f /www/sites/106.55.102.48/log/error.log
```

### Q4: 找不到站点配置
```bash
# 列出所有 1Panel 管理的站点
find /opt/1panel -name "*.conf" -type f | grep -v "nginx.conf"
```

---

## 快速命令（在服务器上执行）

```bash
# 1. 查找 1Panel 端口
echo "1Panel 端口："
cat /usr/local/1panel/1panel.conf 2>/dev/null | grep port || echo "未找到配置文件"

# 2. 查找站点配置文件
echo ""
echo "站点配置文件："
find /opt/1panel -name "*106.55.102.48*.conf" -type f 2>/dev/null

# 3. 查看 OpenResty/Nginx 状态
echo ""
echo "Nginx 状态："
systemctl status openresty 2>/dev/null || systemctl status nginx 2>/dev/null || echo "未找到服务"

# 4. 查看当前配置
echo ""
echo "当前配置："
find /opt/1panel -name "*106.55.102.48*.conf" -type f -exec cat {} \; 2>/dev/null
```

---

## 推荐操作流程

1. **登录 1Panel Web 界面**（最简单）
2. 找到 `106.55.102.48` 站点
3. 编辑配置，添加大文件上传和反向代理配置
4. 保存并等待 1Panel 自动重启 Nginx
5. 刷新前端页面，重新测试上传

---

## 需要的配置关键点

无论使用哪种方法，确保添加以下关键配置：

1. ✅ `client_max_body_size 500M;` - 允许上传 500MB
2. ✅ `client_body_timeout 600s;` - 客户端超时 10 分钟
3. ✅ `proxy_read_timeout 600s;` - 代理读取超时 10 分钟
4. ✅ `proxy_request_buffering off;` - 禁用代理缓冲
5. ✅ `location /api/` 反向代理配置

---

## 下一步

1. 登录 1Panel Web 界面
2. 修改 Nginx 配置
3. 保存并重启
4. 重新测试文件上传

如果遇到问题，请告诉我具体的错误信息！
