# 美团商品批量上传管理工具 - 启动指南

## 📋 前置准备

### 1. 安装必要软件

- **Java 17+**: [下载地址](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.6+**: [下载地址](https://maven.apache.org/download.cgi)
- **MySQL 8.0+**: [下载地址](https://dev.mysql.com/downloads/mysql/)
- **Node.js 16+**: [下载地址](https://nodejs.org/)
- **MinIO**: [下载地址](https://min.io/download)

### 2. MinIO服务

**注意：MinIO已部署在远程服务器上，无需本地安装和启动**

- 控制台地址: http://106.55.102.48:9001
- API地址: http://106.55.102.48:9000
- 用户名: `minio_cf4STY`
- 密码: `minio_ZGBzK7`

应用启动时会自动创建存储桶 `meituan-products`

### 3. 配置数据库

**注意：本项目使用远程MySQL和Redis服务器**

数据库已配置为远程服务器：
- **MySQL**: 106.55.102.48:3306
- **Redis**: 106.55.102.48:6379

配置文件位置：`meituan-backend/src/main/resources/application.yml`

详细配置说明请查看 [SERVER_CONFIG.md](SERVER_CONFIG.md)

#### 初始化数据库

```bash
# 连接到远程MySQL
mysql -h 106.55.102.48 -P 3306 -u root -pmysql_G4EcQ6

# 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS meituan_product CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 使用数据库
USE meituan_product;

# 导入表结构
SOURCE meituan-backend/src/main/resources/db/schema.sql;
```

## 🚀 快速启动

### 方式一：使用启动脚本（推荐）

#### 1. 启动后端

双击运行 `start-backend.bat`

或在命令行中执行：
```bash
start-backend.bat
```

#### 2. 启动前端

双击运行 `start-frontend.bat`

或在命令行中执行：
```bash
start-frontend.bat
```

### 方式二：手动启动

#### 1. 启动后端

```bash
cd meituan-backend
mvn clean install
mvn spring-boot:run
```

后端服务启动成功后，访问: http://localhost:8080

#### 2. 启动前端

```bash
cd meituan-frontend
npm install
npm run electron:dev
```

## 🧪 测试MinIO图片上传

### 使用curl测试

```bash
# 上传图片
curl -X POST http://localhost:8080/api/images/upload \
  -F "file=@test-image.jpg" \
  -F "merchantId=1"

# 响应示例
{
  "code": 200,
  "message": "图片上传成功",
  "data": {
    "imageUrl": "http://localhost:9000/meituan-products/products/1/xxx.jpg?...",
    "fileName": "test-image.jpg"
  },
  "timestamp": 1234567890
}
```

### 使用Postman测试

1. 创建POST请求: `http://localhost:8080/api/images/upload`
2. 选择 Body -> form-data
3. 添加字段:
   - `file`: 选择文件类型，上传图片
   - `merchantId`: 输入商家ID（可选）
4. 点击 Send

## 📊 MinIO配置说明

### 配置文件位置

`meituan-backend/src/main/resources/application.yml`

### MinIO配置项

```yaml
minio:
  endpoint: http://localhost      # MinIO服务地址
  port: 9000                      # MinIO API端口
  access-key: minio_cf4STY        # 访问密钥
  secret-key: minio_ZGBzK7        # 密钥
  bucket-name: meituan-products   # 存储桶名称
```

### 修改MinIO配置

如果你的MinIO配置不同，请修改上述配置项。

## 🔧 常见问题

### 1. MinIO连接失败

**问题**: `Unable to connect to MinIO`

**解决方案**:
- 检查MinIO服务是否启动
- 检查端口9000是否被占用
- 检查访问密钥和密钥是否正确
- 检查防火墙设置

### 2. 存储桶创建失败

**问题**: `Bucket creation failed`

**解决方案**:
- 手动登录MinIO控制台创建存储桶
- 检查MinIO用户权限
- 应用会在启动时自动尝试创建存储桶

### 3. 图片上传失败

**问题**: `File upload failed`

**解决方案**:
- 检查文件大小（最大5MB）
- 检查文件类型（仅支持图片）
- 检查MinIO服务状态
- 查看后端日志获取详细错误信息

### 4. 图片URL无法访问

**问题**: 上传成功但URL无法访问

**解决方案**:
- 检查MinIO桶的访问策略
- URL包含预签名参数，有效期为7天
- 确保MinIO服务正在运行

## 📝 API文档

### 图片上传API

#### 上传图片

```
POST /api/images/upload
Content-Type: multipart/form-data

参数:
- file: 图片文件（必填）
- merchantId: 商家ID（可选）

响应:
{
  "code": 200,
  "message": "图片上传成功",
  "data": {
    "imageUrl": "图片访问URL",
    "fileName": "原始文件名"
  },
  "timestamp": 1234567890
}
```

#### 删除图片

```
DELETE /api/images/delete?objectName=xxx
Content-Type: application/json

参数:
- objectName: 对象名称（必填）

响应:
{
  "code": 200,
  "message": "图片删除成功",
  "data": null,
  "timestamp": 1234567890
}
```

## 🎯 下一步

1. **测试图片上传功能**
   - 使用Postman或curl测试图片上传
   - 验证图片URL可以正常访问

2. **集成到商品管理**
   - 在商品导入时支持图片上传
   - 在生成美团模板时包含图片URL

3. **前端界面开发**
   - 添加图片上传组件
   - 实现图片预览功能
   - 支持批量图片上传

## 📞 获取帮助

- 查看 [README.md](README.md) 了解项目详情
- 查看 [QUICK_START.md](QUICK_START.md) 了解快速启动
- 查看 [PROJECT_STATUS.md](PROJECT_STATUS.md) 了解开发进度

---

**祝你使用愉快！** 🎉
