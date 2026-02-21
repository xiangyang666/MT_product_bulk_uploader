# JWT 认证系统 - 部署指南

## 📋 实现概述

已完成完整的 JWT (JSON Web Token) 认证系统实现，确保只有登录/注册接口公开，其他所有接口都需要认证。

### ✅ 已实现的功能

1. **JWT Token 生成与验证**
   - 使用 JJWT 0.12.3 库
   - HMAC-SHA256 签名算法
   - Token 有效期：7天
   - Token 包含：userId, username, role

2. **认证过滤器**
   - 拦截所有请求
   - 验证 Authorization 头中的 Bearer Token
   - 自动设置 Spring Security 上下文

3. **安全配置**
   - `/api/auth/**` - 公开访问（登录、注册）
   - `/api/**` - 需要认证
   - 无状态会话管理（Stateless）
   - CORS 已配置

4. **前端集成**
   - Axios 拦截器自动添加 Token
   - Token 存储在 localStorage
   - 自动处理 401 未授权错误

---

## 🔧 核心文件说明

### 后端文件

| 文件 | 说明 |
|------|------|
| `JwtUtil.java` | JWT 工具类，负责生成和验证 Token |
| `JwtAuthenticationFilter.java` | JWT 认证过滤器，拦截请求验证 Token |
| `SecurityConfig.java` | Spring Security 配置，定义访问规则 |
| `AuthController.java` | 认证控制器，处理登录/注册/退出 |
| `pom.xml` | 已添加 JJWT 依赖 |

### 前端文件

| 文件 | 说明 |
|------|------|
| `src/api/index.js` | Axios 配置，包含请求拦截器 |
| `test-api-auth.html` | JWT 认证测试工具 |

---

## 📦 部署步骤

### 1. 打包后端

在项目根目录执行：

```bash
cd meituan-backend
mvn clean package -Dmaven.test.skip=true
```

打包完成后，JAR 文件位置：
```
meituan-backend/target/app.jar
```

### 2. 上传到服务器

将 `app.jar` 上传到服务器（106.55.102.48）：

```bash
# 使用 SCP 或 FTP 工具上传
scp target/app.jar root@106.55.102.48:/opt/meituan/
```

### 3. 停止旧服务

```bash
# SSH 登录服务器
ssh root@106.55.102.48

# 查找并停止旧的 Java 进程
ps aux | grep app.jar
kill -9 <进程ID>
```

### 4. 启动新服务

```bash
# 进入应用目录
cd /opt/meituan/

# 启动服务（后台运行）
nohup java -jar app.jar > app.log 2>&1 &

# 查看日志
tail -f app.log
```

### 5. 验证部署

#### 方法 1：使用测试工具

1. 在浏览器中打开 `test-api-auth.html`
2. 点击"登录"按钮（默认用户名：admin，密码：admin123）
3. 测试各个功能：
   - ✅ 登录成功，获取 Token
   - ✅ 获取用户信息（需要认证）
   - ✅ 获取商品统计（需要认证）
   - ✅ 无 Token 访问被拦截（返回 403）

#### 方法 2：使用 curl 命令

```bash
# 1. 测试登录（应该成功）
curl -X POST http://106.55.102.48:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 响应示例：
# {
#   "code": 200,
#   "message": "登录成功",
#   "data": {
#     "token": "eyJhbGciOiJIUzI1NiJ9...",
#     "userInfo": {...}
#   }
# }

# 2. 保存返回的 token
TOKEN="<从上面响应中复制的 token>"

# 3. 测试需要认证的接口（应该成功）
curl -X GET "http://106.55.102.48:8080/api/products/stats?merchantId=1" \
  -H "Authorization: Bearer $TOKEN"

# 4. 测试无 Token 访问（应该返回 403）
curl -X GET "http://106.55.102.48:8080/api/products/stats?merchantId=1"
```

---

## 🔍 测试清单

### ✅ 必须通过的测试

- [ ] 登录接口可以正常访问（无需 Token）
- [ ] 登录成功返回有效的 JWT Token
- [ ] Token 包含正确的用户信息（userId, username, role）
- [ ] 携带有效 Token 可以访问受保护接口
- [ ] 不携带 Token 访问受保护接口返回 403
- [ ] 携带无效 Token 访问受保护接口返回 403
- [ ] 前端登录后可以正常访问所有功能
- [ ] 前端退出登录后无法访问受保护接口

---

## 🛠️ 故障排查

### 问题 1：登录后仍然返回 403

**可能原因：**
- Token 未正确添加到请求头
- Token 格式错误（应该是 `Bearer <token>`）

**解决方法：**
```javascript
// 检查前端请求头
console.log(config.headers['Authorization']); // 应该是 "Bearer eyJhbGci..."
```

### 问题 2：Token 验证失败

**可能原因：**
- Token 已过期（7天有效期）
- 密钥不匹配

**解决方法：**
- 重新登录获取新 Token
- 检查 `JwtUtil.java` 中的 `SECRET_KEY` 是否一致

### 问题 3：CORS 错误

**可能原因：**
- 前端域名未在 CORS 配置中

**解决方法：**
- 检查 `WebConfig.java` 中的 `allowedOriginPatterns("*")`
- 确保前端使用正确的 API 地址

### 问题 4：所有接口都返回 403

**可能原因：**
- JWT 过滤器配置错误
- Security 配置过于严格

**解决方法：**
```bash
# 查看后端日志
tail -f app.log | grep JWT

# 检查是否有认证相关错误
```

---

## 📊 认证流程图

```
用户登录
   ↓
输入用户名密码
   ↓
POST /api/auth/login
   ↓
验证用户名密码
   ↓
生成 JWT Token (包含 userId, username, role)
   ↓
返回 Token 给前端
   ↓
前端保存 Token 到 localStorage
   ↓
后续请求自动添加 Authorization: Bearer <token>
   ↓
JwtAuthenticationFilter 拦截请求
   ↓
验证 Token 签名和有效期
   ↓
解析 Token 获取用户信息
   ↓
设置 Spring Security 上下文
   ↓
允许访问受保护接口
```

---

## 🔐 安全建议

### 生产环境配置

1. **修改 JWT 密钥**
   ```java
   // JwtUtil.java
   // 从环境变量或配置文件读取
   private static final String SECRET_KEY = System.getenv("JWT_SECRET_KEY");
   ```

2. **调整 Token 有效期**
   ```java
   // 根据业务需求调整（当前为 7 天）
   private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 1天
   ```

3. **启用 HTTPS**
   ```yaml
   # application.yml
   server:
     ssl:
       enabled: true
       key-store: classpath:keystore.p12
       key-store-password: your-password
   ```

4. **限制 CORS 来源**
   ```java
   // WebConfig.java
   .allowedOriginPatterns("https://yourdomain.com")
   ```

5. **添加 Token 刷新机制**
   - 实现 Refresh Token
   - Token 快过期时自动刷新

---

## 📝 API 接口说明

### 公开接口（无需认证）

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/auth/login` | POST | 用户登录 |
| `/api/auth/register` | POST | 用户注册 |

### 受保护接口（需要认证）

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/auth/userinfo` | GET | 获取当前用户信息 |
| `/api/auth/logout` | POST | 退出登录 |
| `/api/products/**` | ALL | 所有商品相关接口 |
| `/api/users/**` | ALL | 所有用户管理接口 |

---

## 🎯 下一步工作

部署完成后，建议进行以下工作：

1. ✅ 使用 `test-api-auth.html` 完整测试所有功能
2. ✅ 在前端应用中测试登录流程
3. ✅ 测试 Token 过期后的行为
4. ✅ 验证权限控制是否正确
5. ✅ 监控后端日志，确保没有异常

---

## 📞 联系信息

如有问题，请检查：
- 后端日志：`/opt/meituan/app.log`
- 数据库连接：106.55.102.48:3306
- MinIO 连接：106.55.102.48:9000
- 防火墙端口：8080 已开放

---

**部署时间：** 2026-02-16  
**版本：** v1.0.0  
**状态：** ✅ 就绪，可以部署
