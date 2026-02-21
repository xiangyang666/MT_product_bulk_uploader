# 启动脚本使用指南

本项目提供了多个便捷的启动脚本，帮助你快速启动各个服务。

## 📜 可用脚本

### 1. start-minio.bat
启动MinIO对象存储服务

**功能**:
- 自动设置MinIO用户名和密码
- 创建数据存储目录
- 启动MinIO服务器
- 开启控制台（端口9001）

**使用方法**:
```bash
# 双击运行
start-minio.bat

# 或在命令行中
.\start-minio.bat
```

**服务地址**:
- API: http://localhost:9000
- 控制台: http://localhost:9001
- 用户名: `minio_cf4STY`
- 密码: `minio_ZGBzK7`

---

### 2. start-backend.bat
启动后端Spring Boot服务

**功能**:
- 检查Java环境
- 检查Maven环境
- 启动后端服务

**使用方法**:
```bash
# 双击运行
start-backend.bat

# 或在命令行中
.\start-backend.bat
```

**服务地址**:
- 后端API: http://localhost:8080

**前置要求**:
- Java 17+
- Maven 3.6+
- MySQL 8.0+（已创建数据库）
- MinIO服务已启动

---

### 3. start-frontend.bat
启动前端Electron应用

**功能**:
- 检查Node.js环境
- 自动安装依赖（首次运行）
- 启动Electron开发模式

**使用方法**:
```bash
# 双击运行
start-frontend.bat

# 或在命令行中
.\start-frontend.bat
```

**前置要求**:
- Node.js 16+
- 后端服务已启动

---

### 4. start-all.bat ⭐ 推荐
一键启动所有服务

**功能**:
- 依次启动MinIO、后端、前端
- 自动等待服务启动完成
- 在独立窗口中运行各服务

**使用方法**:
```bash
# 双击运行
start-all.bat

# 或在命令行中
.\start-all.bat
```

**启动顺序**:
1. MinIO服务（等待5秒）
2. 后端服务（等待30秒）
3. 前端应用

**注意事项**:
- 首次运行需要较长时间（Maven下载依赖、npm安装依赖）
- 确保所有前置要求已满足
- 各服务在独立的命令行窗口中运行

---

## 🚀 快速开始流程

### 首次启动

1. **准备环境**
   ```bash
   # 确保已安装：
   - Java 17+
   - Maven 3.6+
   - MySQL 8.0+
   - Node.js 16+
   - MinIO
   ```

2. **创建数据库**
   ```sql
   mysql -u root -p
   CREATE DATABASE meituan_product CHARACTER SET utf8mb4;
   USE meituan_product;
   SOURCE meituan-backend/src/main/resources/db/schema.sql;
   ```

3. **配置数据库连接**
   ```yaml
   # 编辑 meituan-backend/src/main/resources/application.yml
   spring:
     datasource:
       username: root
       password: your_password  # 修改为你的密码
   ```

4. **一键启动**
   ```bash
   start-all.bat
   ```

### 日常启动

如果环境已配置好，直接运行：
```bash
start-all.bat
```

或分别启动各服务：
```bash
# 终端1
start-minio.bat

# 终端2
start-backend.bat

# 终端3
start-frontend.bat
```

---

## 🔍 验证服务状态

### 1. 验证MinIO
访问: http://localhost:9001
- 能否登录控制台
- 是否存在 `meituan-products` 存储桶

### 2. 验证后端
访问: http://localhost:8080/api/products
- 应返回JSON响应（可能是空数组）

### 3. 验证前端
- Electron窗口是否打开
- 界面是否正常显示

### 4. 测试图片上传
打开浏览器访问项目根目录的 `test-image-upload.html`

---

## ⚠️ 常见问题

### 问题1: "Java不是内部或外部命令"

**原因**: 未安装Java或未配置环境变量

**解决**:
1. 下载安装 Java 17+
2. 配置JAVA_HOME环境变量
3. 将 %JAVA_HOME%\bin 添加到PATH

### 问题2: "Maven不是内部或外部命令"

**原因**: 未安装Maven或未配置环境变量

**解决**:
1. 下载安装 Maven 3.6+
2. 配置MAVEN_HOME环境变量
3. 将 %MAVEN_HOME%\bin 添加到PATH

### 问题3: "端口被占用"

**错误**: `Port 8080 is already in use`

**解决**:
```bash
# 查找占用端口的进程
netstat -ano | findstr :8080

# 结束进程
taskkill /PID <进程ID> /F
```

### 问题4: "数据库连接失败"

**错误**: `Communications link failure`

**解决**:
1. 确保MySQL服务已启动
2. 检查数据库名称、用户名、密码
3. 检查application.yml配置

### 问题5: "MinIO连接失败"

**错误**: `Unable to connect to MinIO`

**解决**:
1. 确保MinIO服务已启动
2. 检查端口9000是否被占用
3. 检查application.yml中的MinIO配置

### 问题6: "npm install失败"

**原因**: 网络问题或npm源慢

**解决**:
```bash
# 使用淘宝镜像
npm config set registry https://registry.npmmirror.com

# 重新安装
cd meituan-frontend
npm install
```

---

## 🛑 停止服务

### 方法1: 关闭窗口
直接关闭各服务的命令行窗口

### 方法2: Ctrl+C
在各服务的命令行窗口中按 `Ctrl+C`

### 方法3: 任务管理器
打开任务管理器，结束相关进程：
- java.exe (后端)
- node.exe (前端)
- minio.exe (MinIO)

---

## 📝 日志查看

### 后端日志
在后端启动窗口中实时查看，或查看：
```
meituan-backend/logs/
```

### 前端日志
在前端启动窗口中查看，或打开开发者工具（F12）

### MinIO日志
在MinIO启动窗口中查看

---

## 🔧 自定义配置

### 修改端口

**后端端口**:
```yaml
# meituan-backend/src/main/resources/application.yml
server:
  port: 8081  # 改为其他端口
```

**MinIO端口**:
```bash
# 修改 start-minio.bat
minio.exe server minio-data --console-address ":9002"
```

### 修改MinIO凭证

```yaml
# meituan-backend/src/main/resources/application.yml
minio:
  access-key: your_access_key
  secret-key: your_secret_key
```

同时修改 `start-minio.bat`:
```bash
set MINIO_ROOT_USER=your_access_key
set MINIO_ROOT_PASSWORD=your_secret_key
```

---

## 📚 相关文档

- [START_GUIDE.md](START_GUIDE.md) - 详细启动指南
- [QUICK_START.md](QUICK_START.md) - 快速开始
- [MINIO_INTEGRATION.md](MINIO_INTEGRATION.md) - MinIO集成文档
- [README.md](README.md) - 项目说明

---

## 💡 提示

1. **首次启动较慢**: Maven和npm需要下载依赖，请耐心等待
2. **保持窗口打开**: 关闭窗口会停止服务
3. **按顺序启动**: 建议按 MinIO → 后端 → 前端 的顺序启动
4. **检查日志**: 遇到问题先查看各服务的日志输出
5. **使用start-all.bat**: 推荐使用一键启动脚本，自动处理启动顺序

---

**祝你使用愉快！** 🎉
