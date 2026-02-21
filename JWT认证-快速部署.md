# JWT 认证系统 - 快速部署卡片

## 🚀 一键部署流程

### 步骤 1：打包后端
```bash
# Windows 环境
build-backend.bat

# 或手动执行
cd meituan-backend
mvn clean package -Dmaven.test.skip=true
```

**输出文件：** `meituan-backend/target/app.jar`

---

### 步骤 2：上传到服务器
```bash
scp meituan-backend/target/app.jar root@106.55.102.48:/opt/meituan/
```

---

### 步骤 3：重启服务
```bash
# SSH 登录服务器
ssh root@106.55.102.48

# 停止旧服务
ps aux | grep app.jar
kill -9 <进程ID>

# 启动新服务
cd /opt/meituan/
nohup java -jar app.jar > app.log 2>&1 &

# 查看日志
tail -f app.log
```

---

### 步骤 4：测试验证

#### 快速测试（浏览器）
打开 `test-api-auth.html`，依次测试：
1. ✅ 登录（admin/admin123）
2. ✅ 获取用户信息
3. ✅ 获取商品统计
4. ✅ 无 Token 访问（应该失败）

#### 命令行测试
```bash
# 1. 登录获取 Token
curl -X POST http://106.55.102.48:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 2. 使用 Token 访问（替换 <TOKEN>）
curl -X GET "http://106.55.102.48:8080/api/products/stats?merchantId=1" \
  -H "Authorization: Bearer <TOKEN>"

# 3. 无 Token 访问（应该返回 403）
curl -X GET "http://106.55.102.48:8080/api/products/stats?merchantId=1"
```

---

## ✅ 验证清单

- [ ] 后端服务启动成功（查看日志无错误）
- [ ] 登录接口返回 Token
- [ ] Token 可以解析出用户信息
- [ ] 携带 Token 可以访问受保护接口
- [ ] 不携带 Token 返回 403 错误
- [ ] 前端登录功能正常
- [ ] 前端可以访问所有业务功能

---

## 🔧 核心配置

### 数据库（远程）
- 地址：106.55.102.48:3306
- 数据库：meituan_product
- 用户：root
- 密码：mysql_G4EcQ6

### MinIO（远程）
- 地址：106.55.102.48:9000
- Access Key：minio_cf4STY
- Secret Key：minio_ZGBzK7

### JWT 配置
- 算法：HMAC-SHA256
- 有效期：7天
- Token 格式：`Bearer <token>`

---

## 🐛 常见问题

### 问题：登录后仍返回 403
**解决：** 检查前端是否正确添加 `Authorization: Bearer <token>` 头

### 问题：Token 验证失败
**解决：** Token 可能已过期，重新登录获取新 Token

### 问题：连接被拒绝
**解决：** 确保端口 8080 已在防火墙开放

### 问题：数据库连接失败
**解决：** 检查 application.yml 中的数据库配置

---

## 📁 关键文件

| 文件 | 说明 |
|------|------|
| `JwtUtil.java` | JWT 生成和验证 |
| `JwtAuthenticationFilter.java` | 请求拦截和认证 |
| `SecurityConfig.java` | 安全配置 |
| `AuthController.java` | 登录/注册接口 |
| `test-api-auth.html` | 测试工具 |
| `JWT认证部署指南.md` | 完整部署文档 |

---

## 🎯 认证规则

### 公开接口（无需 Token）
- `POST /api/auth/login` - 登录
- `POST /api/auth/register` - 注册

### 受保护接口（需要 Token）
- `GET /api/auth/userinfo` - 用户信息
- `POST /api/auth/logout` - 退出登录
- `ALL /api/products/**` - 所有商品接口
- `ALL /api/users/**` - 所有用户接口

---

## 📞 服务器信息

- **IP：** 106.55.102.48
- **端口：** 8080
- **应用目录：** /opt/meituan/
- **日志文件：** /opt/meituan/app.log

---

**准备就绪！现在可以开始部署了。** 🚀
