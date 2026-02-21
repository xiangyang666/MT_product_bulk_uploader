# 服务器配置说明

## 📋 服务器信息

本项目使用远程服务器提供MySQL和Redis服务。

### 服务器地址
- **IP地址**: 106.55.102.48

### MySQL配置
- **端口**: 3306
- **用户名**: root
- **密码**: mysql_G4EcQ6
- **数据库**: meituan_product

### Redis配置
- **端口**: 6379
- **密码**: redis_BNmX4z

### MinIO配置（远程服务器）
- **端口**: 9000（API）/ 9001（控制台）
- **服务器**: 106.55.102.48
- **用户名**: minio_cf4STY
- **密码**: minio_ZGBzK7
- **存储桶**: meituan-products

---

## 🔧 配置文件

### application.yml

配置文件位置：`meituan-backend/src/main/resources/application.yml`

```yaml
spring:
  application:
    name: meituan-product-upload
  
  # MySQL配置（远程服务器）
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://106.55.102.48:3306/meituan_product?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: mysql_G4EcQ6
  
  # Redis配置（远程服务器）
  redis:
    host: 106.55.102.48
    port: 6379
    password: redis_BNmX4z
    timeout: 3000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms

# MinIO配置（远程服务器）
minio:
  endpoint: http://106.55.102.48
  port: 9000
  access-key: minio_cf4STY
  secret-key: minio_ZGBzK7
  bucket-name: meituan-products
```

---

## 🚀 快速启动

### 1. 确保远程服务可访问

**测试MySQL连接**:
```bash
mysql -h 106.55.102.48 -P 3306 -u root -p
# 输入密码: mysql_G4EcQ6
```

**测试Redis连接**:
```bash
redis-cli -h 106.55.102.48 -p 6379 -a redis_BNmX4z
```

**测试MinIO连接**:
访问: http://106.55.102.48:9001
- 用户名: minio_cf4STY
- 密码: minio_ZGBzK7

### 2. 初始化数据库

```sql
-- 连接到MySQL
mysql -h 106.55.102.48 -P 3306 -u root -pmysql_G4EcQ6

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS meituan_product CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE meituan_product;

-- 导入表结构
SOURCE meituan-backend/src/main/resources/db/schema.sql;
```

### 3. 启动后端服务

MinIO已部署在远程服务器，无需本地启动。

```bash
start-backend.bat
```

### 4. 启动前端应用

```bash
start-frontend.bat
```

---

## 🌐 远程服务访问地址

- **MinIO控制台**: http://106.55.102.48:9001
- **MinIO API**: http://106.55.102.48:9000
- **后端API**: http://localhost:8080（本地）
- **前端应用**: Electron窗口（本地）

---

## 🔒 安全注意事项

### 1. 生产环境配置

在生产环境中，建议：
- 使用环境变量存储敏感信息
- 启用SSL/TLS加密连接
- 使用更强的密码
- 限制IP访问白名单

### 2. 环境变量配置

可以使用环境变量替代配置文件中的敏感信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:106.55.102.48}:${DB_PORT:3306}/meituan_product
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:mysql_G4EcQ6}
  
  redis:
    host: ${REDIS_HOST:106.55.102.48}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:redis_BNmX4z}
```

### 防火墙配置

确保服务器防火墙允许以下端口：
- MySQL: 3306
- Redis: 6379
- MinIO API: 9000
- MinIO控制台: 9001

---

## 📊 服务架构

```
┌─────────────────────────────────────────────────────────┐
│                    本地开发环境                          │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐                    │
│  │   前端应用    │  │   后端服务    │                    │
│  │  (Electron)  │  │ (Spring Boot)│                    │
│  └──────────────┘  └──────┬───────┘                    │
│                           │                              │
└───────────────────────────┼──────────────────────────────┘
                            │
                            │ 网络连接
                            ▼
┌─────────────────────────────────────────────────────────┐
│              远程服务器 (106.55.102.48)                  │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │    MySQL     │  │    Redis     │  │    MinIO     │  │
│  │   (3306)     │  │   (6379)     │  │ (9000/9001)  │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

## 🧪 连接测试

### 测试MySQL连接

**使用命令行**:
```bash
mysql -h 106.55.102.48 -P 3306 -u root -pmysql_G4EcQ6 -e "SELECT VERSION();"
```

**使用Java代码**:
```java
String url = "jdbc:mysql://106.55.102.48:3306/meituan_product";
String username = "root";
String password = "mysql_G4EcQ6";

try (Connection conn = DriverManager.getConnection(url, username, password)) {
    System.out.println("MySQL连接成功！");
} catch (SQLException e) {
    System.err.println("MySQL连接失败：" + e.getMessage());
}
```

### 测试Redis连接

**使用命令行**:
```bash
redis-cli -h 106.55.102.48 -p 6379 -a redis_BNmX4z PING
```

**使用Java代码**:
```java
@Autowired
private RedisTemplate<String, Object> redisTemplate;

public void testRedis() {
    redisTemplate.opsForValue().set("test", "Hello Redis!");
    String value = (String) redisTemplate.opsForValue().get("test");
    System.out.println("Redis测试成功：" + value);
}
```

---

## 🔧 故障排查

### 问题1: 无法连接MySQL

**错误信息**:
```
Communications link failure
```

**解决方案**:
1. 检查网络连接
2. 确认服务器IP和端口正确
3. 检查防火墙设置
4. 验证用户名和密码
5. 确认MySQL服务正在运行

### 问题2: 无法连接Redis

**错误信息**:
```
Unable to connect to Redis
```

**解决方案**:
1. 检查网络连接
2. 确认服务器IP和端口正确
3. 验证Redis密码
4. 检查防火墙设置
5. 确认Redis服务正在运行

### 问题3: 连接超时

**错误信息**:
```
Connection timeout
```

**解决方案**:
1. 增加连接超时时间
2. 检查网络延迟
3. 确认服务器负载正常
4. 检查是否有网络限制

---

## 📝 数据库管理

### 查看数据库

```sql
-- 连接到MySQL
mysql -h 106.55.102.48 -P 3306 -u root -pmysql_G4EcQ6

-- 查看所有数据库
SHOW DATABASES;

-- 使用数据库
USE meituan_product;

-- 查看所有表
SHOW TABLES;

-- 查看表结构
DESC t_product;
```

### 备份数据库

```bash
# 备份整个数据库
mysqldump -h 106.55.102.48 -P 3306 -u root -pmysql_G4EcQ6 meituan_product > backup.sql

# 恢复数据库
mysql -h 106.55.102.48 -P 3306 -u root -pmysql_G4EcQ6 meituan_product < backup.sql
```

---

## 🔄 Redis缓存使用

### 缓存商品数据

```java
@Service
public class ProductService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String PRODUCT_CACHE_KEY = "product:";
    
    public Product getProduct(Long id) {
        // 先从缓存获取
        String key = PRODUCT_CACHE_KEY + id;
        Product product = (Product) redisTemplate.opsForValue().get(key);
        
        if (product == null) {
            // 缓存未命中，从数据库查询
            product = productRepository.selectById(id);
            
            if (product != null) {
                // 存入缓存，设置过期时间1小时
                redisTemplate.opsForValue().set(key, product, 1, TimeUnit.HOURS);
            }
        }
        
        return product;
    }
}
```

---

## 📚 相关文档

- [START_GUIDE.md](START_GUIDE.md) - 启动指南
- [MINIO_INTEGRATION.md](MINIO_INTEGRATION.md) - MinIO集成
- [README.md](README.md) - 项目说明

---

**配置完成！** 🎉

现在你的应用将连接到远程MySQL和Redis服务器，同时使用本地MinIO存储图片。
