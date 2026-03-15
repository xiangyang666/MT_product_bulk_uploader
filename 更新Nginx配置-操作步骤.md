# 更新 Nginx 配置 - 操作步骤

## 问题分析

当前 Nginx 配置存在以下问题：
1. ❌ 缺少 `client_max_body_size` - 默认只能上传 1MB 文件
2. ❌ 缺少超时配置 - 大文件上传会超时断开
3. ❌ 缺少反向代理配置 - 没有将 `/api/` 请求转发到后端 8080 端口
4. ❌ 缺少 `proxy_request_buffering off` - 大文件上传会卡住

## 解决方案

已生成优化后的配置文件：`nginx-config-optimized.conf`

### 关键改动说明

#### 1. 大文件上传支持
```nginx
client_max_body_size 500M;          # 允许上传 500MB 文件
client_body_buffer_size 128k;       # 缓冲区大小
client_body_timeout 600s;           # 客户端超时 10 分钟
send_timeout 600s;                  # 发送超时 10 分钟
```

#### 2. 反向代理配置（新增）
```nginx
location /api/ {
    proxy_pass http://localhost:8080/api/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    
    # 代理超时配置
    proxy_connect_timeout 600s;     # 连接超时
    proxy_send_timeout 600s;        # 发送超时
    proxy_read_timeout 600s;        # 读取超时（最重要！）
    
    # 禁用代理缓冲（大文件上传必须！）
    proxy_request_buffering off;
}
```

## 操作步骤

### 方法 1：直接替换配置文件（推荐）

```bash
# 1. SSH 登录服务器
ssh root@106.55.102.48

# 2. 备份当前配置
cp /etc/nginx/sites-available/default /etc/nginx/sites-available/default.backup

# 3. 编辑配置文件
vi /etc/nginx/sites-available/default

# 4. 将 nginx-config-optimized.conf 的内容完整复制进去
# （按 i 进入编辑模式，粘贴内容，按 ESC 然后输入 :wq 保存退出）

# 5. 测试配置是否正确
nginx -t

# 6. 如果测试通过，重启 Nginx
systemctl restart nginx

# 7. 查看 Nginx 状态
systemctl status nginx
```

### 方法 2：使用宝塔面板（如果安装了）

```bash
# 1. 登录宝塔面板
# 访问：http://106.55.102.48:8888

# 2. 进入"网站" -> 找到 106.55.102.48 站点 -> 点击"设置"

# 3. 点击"配置文件"标签

# 4. 将 nginx-config-optimized.conf 的内容完整复制进去

# 5. 点击"保存"按钮（宝塔会自动测试并重启 Nginx）
```

### 方法 3：手动添加配置（如果不想完全替换）

如果你不想完全替换配置，可以只添加关键部分：

```bash
# 1. SSH 登录服务器
ssh root@106.55.102.48

# 2. 编辑配置文件
vi /etc/nginx/sites-available/default

# 3. 在 server { 块的开头（listen 80; 之后）添加：
client_max_body_size 500M;
client_body_buffer_size 128k;
client_body_timeout 600s;
send_timeout 600s;

# 4. 在 server { 块中添加反向代理配置（在 location ~ ^/... 之前）：
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

# 5. 保存并测试
nginx -t

# 6. 重启 Nginx
systemctl restart nginx
```

## 验证配置是否生效

### 1. 检查 Nginx 配置
```bash
# 查看当前配置
cat /etc/nginx/sites-available/default | grep -A 5 "client_max_body_size"

# 查看反向代理配置
cat /etc/nginx/sites-available/default | grep -A 10 "location /api/"
```

### 2. 测试上传功能
1. 打开应用的"系统设置" -> "应用版本管理"
2. 点击"上传新版本"
3. 拖入一个大文件（如 exe 或 dmg）
4. 观察上传进度是否正常

### 3. 查看日志
```bash
# 查看 Nginx 错误日志
tail -f /www/sites/106.55.102.48/log/error.log

# 查看 Nginx 访问日志
tail -f /www/sites/106.55.102.48/log/access.log
```

## 预期效果

配置完成后：
- ✅ 支持上传 500MB 以内的文件
- ✅ 上传过程不会超时断开
- ✅ exe 文件可以完整上传
- ✅ dmg 文件可以完整上传
- ✅ 上传到 100% 后服务器正常响应
- ✅ 多个文件可以顺序上传成功

## 常见问题

### Q1: nginx -t 报错怎么办？
```bash
# 查看详细错误信息
nginx -t

# 如果配置有语法错误，恢复备份
cp /etc/nginx/sites-available/default.backup /etc/nginx/sites-available/default
```

### Q2: 重启 Nginx 失败怎么办？
```bash
# 查看 Nginx 状态
systemctl status nginx

# 查看错误日志
journalctl -xe | grep nginx

# 强制重启
systemctl stop nginx
systemctl start nginx
```

### Q3: 配置后还是上传失败怎么办？
```bash
# 1. 确认后端服务是否运行
netstat -tlnp | grep 8080

# 2. 确认 MinIO 是否运行
netstat -tlnp | grep 9000

# 3. 查看后端日志
# 找到你的 Spring Boot 应用日志文件并查看
```

## 注意事项

1. **备份配置**：修改前一定要备份原配置文件
2. **测试配置**：修改后必须执行 `nginx -t` 测试
3. **重启服务**：配置生效需要重启 Nginx
4. **检查端口**：确保 8080 端口的后端服务正在运行
5. **磁盘空间**：确保服务器有足够空间存储上传的文件

## 服务器信息

- 服务器 IP: `106.55.102.48`
- Nginx 配置路径: `/etc/nginx/sites-available/default`
- 后端端口: `8080`
- MinIO 端口: `9000`
- 日志路径: `/www/sites/106.55.102.48/log/`
