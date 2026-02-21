# 美团商品批量上传管理工具 - 快速启动指南

## 🚀 5分钟快速启动

### 前置要求

- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Node.js 16+
- npm 或 yarn
- MinIO（用于图片存储）

## 📦 MinIO安装和启动

### 1. 下载MinIO

访问 [MinIO官网](https://min.io/download) 下载Windows版本

### 2. 启动MinIO服务

使用提供的启动脚本：
```bash
start-minio.bat
```

或手动启动：
```bash
# 设置环境变量
set MINIO_ROOT_USER=minio_cf4STY
set MINIO_ROOT_PASSWORD=minio_ZGBzK7

# 启动服务
minio.exe server minio-data --console-address ":9001"
```

### 3. 访问MinIO控制台

- 控制台地址：http://localhost:9001
- API地址：http://localhost:9000
- 用户名：`minio_cf4STY`
- 密码：`minio_ZGBzK7`

### 4. 创建存储桶（可选）

应用启动时会自动创建名为 `meituan-products` 的存储桶。

如需手动创建：
1. 登录MinIO控制台
2. 点击 "Buckets" -> "Create Bucket"
3. 输入桶名称：`meituan-products`
4. 点击 "Create Bucket"

## 📦 后端启动

### 1. 创建数据库

```bash
# 登录MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE meituan_product CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 使用数据库
USE meituan_product;

# 导入表结构
SOURCE meituan-backend/src/main/resources/db/schema.sql;
```

### 2. 配置数据库连接

编辑 `meituan-backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/meituan_product?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password  # 修改为你的MySQL密码
```

### 3. 启动后端服务

```bash
cd meituan-backend
mvn clean install
mvn spring-boot:run
```

✅ 后端服务启动成功！访问：http://localhost:8080

## 🎨 前端启动

### 1. 安装依赖

```bash
cd meituan-frontend
npm install
```

### 2. 启动开发模式

```bash
npm run electron:dev
```

✅ Electron应用启动成功！

## 🧪 测试API

### 使用curl测试

#### 0. 上传商品图片

```bash
curl -X POST http://localhost:8080/api/images/upload \
  -F "file=@test-image.jpg" \
  -F "merchantId=1"
```

响应示例：
```json
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

#### 1. 导入商品（需要准备Excel文件）

```bash
curl -X POST http://localhost:8080/api/products/import \
  -F "file=@test.xlsx" \
  -F "merchantId=1"
```

#### 2. 获取商品列表

```bash
curl -X GET "http://localhost:8080/api/products?merchantId=1"
```

#### 3. 生成美团模板

```bash
curl -X POST http://localhost:8080/api/products/generate-template \
  -H "Content-Type: application/json" \
  -d '{
    "productIds": [1, 2, 3],
    "merchantId": 1
  }' \
  --output template.xlsx
```

#### 4. 批量上传到美团

```bash
curl -X POST http://localhost:8080/api/products/upload \
  -H "Content-Type: application/json" \
  -d '{
    "productIds": [1, 2, 3],
    "merchantId": 1,
    "accessToken": "your_access_token"
  }'
```

#### 5. 清空商品

```bash
curl -X DELETE http://localhost:8080/api/products/clear \
  -H "Content-Type: application/json" \
  -d '{
    "merchantId": 1,
    "accessToken": "your_access_token"
  }'
```

## 📝 准备测试数据

### Excel文件格式

创建一个Excel文件（test.xlsx），包含以下列：

| 商品名称 | 类目ID | 价格 | 库存 | 商品描述 | 商品图片URL |
|---------|--------|------|------|---------|------------|
| 测试商品1 | 1234567890 | 99.99 | 100 | 这是测试商品 | http://example.com/image1.jpg |
| 测试商品2 | 1234567890 | 199.99 | 50 | 这是测试商品2 | http://example.com/image2.jpg |

**注意：**
- 商品名称：必填，最大255字符
- 类目ID：必填，必须是10位数字
- 价格：必填，大于0，最大99999.99
- 库存：可选，默认0
- 商品描述：可选，最大1000字符
- 商品图片URL：可选，最大500字符

## 🔧 常见问题

### 1. 数据库连接失败

**问题：** `Communications link failure`

**解决：**
- 检查MySQL是否启动
- 检查用户名密码是否正确
- 检查数据库名称是否正确

### 2. 端口被占用

**问题：** `Port 8080 is already in use`

**解决：**
```yaml
# 修改 application.yml
server:
  port: 8081  # 改为其他端口
```

### 3. Excel文件格式错误

**问题：** `不支持的文件格式`

**解决：**
- 确保文件是 .xlsx 或 .xls 格式
- 确保文件不为空
- 确保文件大小不超过10MB

### 4. 数据验证失败

**问题：** `数据验证失败`

**解决：**
- 检查类目ID是否为10位数字
- 检查价格是否大于0
- 检查必填字段是否为空

## 📊 API文档

### 基础URL

```
http://localhost:8080/api
```

### 接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /products/import | 导入商品 |
| GET | /products | 获取商品列表 |
| GET | /products/{id} | 获取商品详情 |
| POST | /products/generate-template | 生成美团模板 |
| POST | /products/upload | 批量上传到美团 |
| DELETE | /products/clear | 清空商品 |

### 响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1234567890
}
```

## 🎯 下一步

1. **配置美团API**
   - 在 `application.yml` 中配置真实的美团API地址
   - 获取美团开放平台的访问令牌

2. **完善前端界面**
   - 实现文件上传组件
   - 实现商品列表展示
   - 实现批量操作功能

3. **测试完整流程**
   - 导入Excel → 查看商品 → 生成模板 → 上传到美团

## 📞 获取帮助

- 查看 [README.md](README.md) 了解项目详情
- 查看 [PROJECT_STATUS.md](PROJECT_STATUS.md) 了解开发进度
- 查看 [需求文档](.kiro/specs/meituan-product-batch-upload/requirements.md)
- 查看 [设计文档](.kiro/specs/meituan-product-batch-upload/design.md)

---

**祝你使用愉快！** 🎉
