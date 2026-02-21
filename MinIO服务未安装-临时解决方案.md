# MinIO 服务未安装 - 临时解决方案

## 问题描述
后端启动时报错：`Failed to connect to localhost:9000`

## 根本原因
MinIO 对象存储服务未安装或未启动

## 已实施的临时解决方案

### ✅ 禁用 MinIO 初始化
已注释掉 `MinioInitializer.java` 中的 `@Component` 注解，这样后端可以正常启动，不会因为 MinIO 连接失败而中断。

**修改的文件：**
```
meituan-backend/src/main/java/com/meituan/product/config/MinioInitializer.java
```

**影响：**
- ✅ 后端可以正常启动
- ⚠️ 图片上传功能暂时不可用
- ✅ 其他功能（商品管理、导入等）正常工作

---

## 完整解决方案（可选）

如果你需要使用图片上传功能，需要安装 MinIO：

### 步骤 1: 下载 MinIO

访问 MinIO 官网下载 Windows 版本：
https://min.io/download

或者直接下载：
```
https://dl.min.io/server/minio/release/windows-amd64/minio.exe
```

### 步骤 2: 安装 MinIO

1. 将下载的 `minio.exe` 放到项目根目录
2. 确保 `start-minio.bat` 脚本存在

### 步骤 3: 启动 MinIO

```bash
# 双击运行或命令行执行
start-minio.bat
```

### 步骤 4: 恢复 MinIO 初始化

编辑 `meituan-backend/src/main/java/com/meituan/product/config/MinioInitializer.java`

将：
```java
// @Component  // 临时禁用MinIO初始化，避免启动失败
```

改回：
```java
@Component
```

### 步骤 5: 重启后端

```bash
cd meituan-backend
mvn spring-boot:run
```

---

## 验证 MinIO 是否正常

### 检查 MinIO 服务
```bash
# 访问 MinIO 控制台
http://localhost:9001

# 登录凭证（在 application.yml 中）
用户名: minioadmin
密码: minioadmin
```

### 检查后端日志
启动后端后，应该看到：
```
开始初始化MinIO存储桶...
MinIO存储桶初始化完成
```

---

## 当前状态

### ✅ 可以正常使用的功能
- 商品列表查看
- 商品导入（Excel）
- 商品编辑
- 商品删除
- 模板生成
- 批量上传

### ⚠️ 暂时不可用的功能
- 图片上传到 MinIO
- 图片管理

---

## 下一步

1. **现在**：重启后端服务，应该可以正常启动了
2. **稍后**：如果需要图片功能，按照"完整解决方案"安装 MinIO

---

## 快速启动命令

```bash
# 1. 重启后端（MinIO已禁用）
cd meituan-backend
mvn spring-boot:run

# 2. 启动前端
cd meituan-frontend
npm run dev
```

---

**修改时间**: 2026-02-16
**状态**: ✅ 临时解决方案已实施
**影响**: 后端可以正常启动，图片功能暂时不可用
