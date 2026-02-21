# MinIO对象存储集成文档

## 📋 概述

本文档描述了MinIO对象存储在美团商品批量上传管理工具中的集成实现。

## 🎯 功能特性

### 1. 图片存储
- 支持商品图片上传到MinIO对象存储
- 自动生成唯一文件名（UUID）
- 支持按商家ID分类存储
- 文件类型验证（仅支持图片）
- 文件大小限制（最大5MB）

### 2. 图片管理
- 图片上传
- 图片删除
- 图片URL生成（预签名URL，7天有效期）
- 文件存在性检查
- 文件下载

### 3. 自动化
- 应用启动时自动创建存储桶
- 自动处理MinIO连接
- 异常自动处理和日志记录

## 🏗️ 架构设计

### 组件结构

```
MinIO集成
├── 配置层
│   ├── MinioConfig.java          # MinIO客户端配置
│   └── MinioInitializer.java     # 启动时初始化
├── 服务层
│   └── MinioService.java         # 文件操作服务
└── 控制层
    └── ImageController.java      # 图片上传API
```

### 数据流

```
客户端 → ImageController → MinioService → MinIO服务器
   ↓                                           ↓
预览图片 ← 返回URL ← 生成预签名URL ← 存储成功
```

## 📝 配置说明

### application.yml配置

```yaml
minio:
  endpoint: http://localhost      # MinIO服务地址
  port: 9000                      # MinIO API端口
  access-key: minio_cf4STY        # 访问密钥
  secret-key: minio_ZGBzK7        # 密钥
  bucket-name: meituan-products   # 存储桶名称
```

### Maven依赖

```xml
<dependency>
    <groupId>io.minio</groupId>
    <artifactId>minio</artifactId>
    <version>8.5.7</version>
</dependency>
```

## 🔌 API接口

### 1. 上传图片

**请求**
```http
POST /api/images/upload
Content-Type: multipart/form-data

参数:
- file: 图片文件（必填）
- merchantId: 商家ID（可选，用于分类存储）
```

**响应**
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

**curl示例**
```bash
curl -X POST http://localhost:8080/api/images/upload \
  -F "file=@test-image.jpg" \
  -F "merchantId=1"
```

### 2. 删除图片

**请求**
```http
DELETE /api/images/delete?objectName=products/1/xxx.jpg
```

**响应**
```json
{
  "code": 200,
  "message": "图片删除成功",
  "data": null,
  "timestamp": 1234567890
}
```

**curl示例**
```bash
curl -X DELETE "http://localhost:8080/api/images/delete?objectName=products/1/xxx.jpg"
```

## 💻 代码示例

### 上传图片（Java）

```java
@Autowired
private MinioService minioService;

public String uploadProductImage(MultipartFile file, Long merchantId) {
    String folder = "products/" + merchantId;
    String imageUrl = minioService.uploadFile(file, folder);
    return imageUrl;
}
```

### 上传图片（JavaScript）

```javascript
async function uploadImage(file, merchantId) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('merchantId', merchantId);
    
    const response = await fetch('http://localhost:8080/api/images/upload', {
        method: 'POST',
        body: formData
    });
    
    const data = await response.json();
    return data.data.imageUrl;
}
```

## 🚀 快速开始

### 1. 启动MinIO服务

```bash
# Windows
start-minio.bat

# 或手动启动
set MINIO_ROOT_USER=minio_cf4STY
set MINIO_ROOT_PASSWORD=minio_ZGBzK7
minio.exe server minio-data --console-address ":9001"
```

### 2. 访问MinIO控制台

- URL: http://localhost:9001
- 用户名: `minio_cf4STY`
- 密码: `minio_ZGBzK7`

### 3. 启动后端服务

```bash
cd meituan-backend
mvn spring-boot:run
```

应用启动时会自动创建 `meituan-products` 存储桶。

### 4. 测试图片上传

打开浏览器访问：`test-image-upload.html`

或使用curl：
```bash
curl -X POST http://localhost:8080/api/images/upload \
  -F "file=@test-image.jpg" \
  -F "merchantId=1"
```

## 📂 文件存储结构

```
meituan-products/              # 存储桶
├── products/                  # 商品图片目录
│   ├── 1/                     # 商家ID 1
│   │   ├── uuid1.jpg
│   │   ├── uuid2.png
│   │   └── ...
│   ├── 2/                     # 商家ID 2
│   │   └── ...
│   └── uuid3.jpg              # 未指定商家ID
└── ...
```

## 🔒 安全特性

### 1. 文件验证
- 文件类型验证（仅允许图片）
- 文件大小限制（最大5MB）
- 文件名UUID化，防止冲突

### 2. 访问控制
- 使用预签名URL访问
- URL有效期7天
- 支持按商家ID隔离存储

### 3. 错误处理
- 完整的异常捕获和处理
- 详细的错误日志记录
- 友好的错误提示信息

## 🛠️ 故障排查

### 问题1：MinIO连接失败

**错误信息**
```
Unable to connect to MinIO
```

**解决方案**
1. 检查MinIO服务是否启动
2. 检查端口9000是否被占用
3. 检查配置文件中的endpoint和port
4. 检查访问密钥是否正确

### 问题2：存储桶创建失败

**错误信息**
```
Bucket creation failed
```

**解决方案**
1. 手动登录MinIO控制台创建存储桶
2. 检查MinIO用户权限
3. 查看后端日志获取详细错误

### 问题3：图片上传失败

**错误信息**
```
File upload failed
```

**解决方案**
1. 检查文件大小（不超过5MB）
2. 检查文件类型（必须是图片）
3. 检查MinIO服务状态
4. 查看后端日志

### 问题4：图片URL无法访问

**原因**
- MinIO服务未运行
- URL已过期（超过7天）
- 网络问题

**解决方案**
1. 确保MinIO服务正在运行
2. 重新生成URL
3. 检查网络连接

## 📊 性能优化

### 1. 文件上传优化
- 使用流式上传，支持大文件
- 分片上传（10MB per part）
- 异步上传处理

### 2. URL生成优化
- 预签名URL缓存
- 批量URL生成
- URL有效期可配置

### 3. 存储优化
- 按商家ID分类存储
- 定期清理过期文件
- 压缩图片（可选）

## 🔄 集成到商品管理

### 在商品导入时上传图片

```java
@Service
public class ProductService {
    
    @Autowired
    private MinioService minioService;
    
    public ImportResult importProducts(MultipartFile excelFile, 
                                      List<MultipartFile> images,
                                      Long merchantId) {
        // 1. 解析Excel
        List<Product> products = excelService.parseExcel(excelFile);
        
        // 2. 上传图片
        for (int i = 0; i < images.size(); i++) {
            String imageUrl = minioService.uploadFile(
                images.get(i), 
                "products/" + merchantId
            );
            products.get(i).setImageUrl(imageUrl);
        }
        
        // 3. 保存到数据库
        productRepository.batchInsert(products);
        
        return ImportResult.success(products);
    }
}
```

## 📚 参考资料

- [MinIO官方文档](https://min.io/docs/minio/linux/index.html)
- [MinIO Java SDK](https://min.io/docs/minio/linux/developers/java/minio-java.html)
- [Spring Boot集成MinIO](https://docs.spring.io/spring-boot/docs/current/reference/html/)

## 🎉 总结

MinIO对象存储已成功集成到美团商品批量上传管理工具中，提供了完整的图片存储和管理功能。主要特性包括：

✅ 自动化配置和初始化
✅ 完整的文件操作API
✅ 安全的访问控制
✅ 友好的错误处理
✅ 详细的日志记录
✅ 便捷的测试工具

---

**版本**: 1.0.0
**更新时间**: 2024-02-09
**作者**: Kiro AI Assistant
