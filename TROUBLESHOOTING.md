# 故障排除指南

## 🔍 常见问题及解决方案

### 1. 前端问题

#### 问题：窗口控制按钮不工作
**症状**：点击红黄绿三个圆点没有反应

**解决方案**：
1. 检查 `electron/preload.js` 是否使用 CommonJS 语法
   ```javascript
   // ✅ 正确
   const { contextBridge, ipcRenderer } = require('electron');
   
   // ❌ 错误
   import { contextBridge, ipcRenderer } from 'electron';
   ```

2. 检查 `electron/main.js` 中的 IPC 监听器
   ```javascript
   ipcMain.on('minimize-window', () => { ... });
   ipcMain.on('maximize-window', () => { ... });
   ipcMain.on('close-window', () => { ... });
   ```

3. 重启应用

#### 问题：Electron 应用无法启动
**症状**：运行 `pnpm run electron:dev` 报错

**解决方案**：
1. 检查 Node.js 版本（需要 16+）
   ```bash
   node --version
   ```

2. 重新安装依赖
   ```bash
   cd meituan-frontend
   rm -rf node_modules
   rm pnpm-lock.yaml
   pnpm install
   ```

3. 检查 Electron 是否正确安装
   ```bash
   pnpm list electron
   ```

4. 手动安装 Electron
   ```bash
   pnpm add -D electron
   node node_modules/electron/install.js
   ```

#### 问题：开发者工具快捷键不工作
**症状**：按 `Ctrl+Shift+D` 没有反应

**解决方案**：
1. 检查 `electron/main.js` 中的快捷键注册
   ```javascript
   globalShortcut.register('CommandOrControl+Shift+D', () => {
     mainWindow.webContents.toggleDevTools();
   });
   ```

2. 确保应用获得焦点
3. 尝试使用 `F12` 键（默认快捷键）

#### 问题：API 请求失败
**症状**：前端请求后端接口返回 404 或 500

**解决方案**：
1. 检查后端是否启动
   ```bash
   # 访问后端健康检查
   curl http://localhost:8080/actuator/health
   ```

2. 检查前端 API 配置
   ```javascript
   // src/api/index.js
   const baseURL = 'http://localhost:8080/api';
   ```

3. 检查跨域配置（后端）
   ```java
   @CrossOrigin(origins = "*")
   ```

4. 查看浏览器控制台错误信息

### 2. 后端问题

#### 问题：后端启动失败
**症状**：运行 `mvn spring-boot:run` 报错

**解决方案**：
1. 检查 Java 版本（需要 17+）
   ```bash
   java -version
   ```

2. 检查 Maven 版本（需要 3.6+）
   ```bash
   mvn -version
   ```

3. 清理并重新构建
   ```bash
   cd meituan-backend
   mvn clean install
   ```

4. 检查端口占用
   ```bash
   # Windows
   netstat -ano | findstr :8080
   
   # 杀死进程
   taskkill /PID <进程ID> /F
   ```

#### 问题：数据库连接失败
**症状**：启动时报 `Cannot create PoolableConnectionFactory`

**解决方案**：
1. 检查 MySQL 是否启动
   ```bash
   # Windows
   net start MySQL80
   
   # 或通过服务管理器启动
   ```

2. 检查数据库配置
   ```yaml
   # application.yml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/meituan_product?useUnicode=true&characterEncoding=utf8
       username: root
       password: your_password
   ```

3. 测试数据库连接
   ```bash
   mysql -u root -p
   USE meituan_product;
   SHOW TABLES;
   ```

4. 检查数据库是否存在
   ```sql
   SHOW DATABASES LIKE 'meituan_product';
   ```

5. 执行初始化脚本
   ```bash
   mysql -u root -p < database-init.sql
   ```

#### 问题：Excel 解析失败
**症状**：上传 Excel 文件后报错

**解决方案**：
1. 检查文件格式（必须是 .xlsx 或 .xls）
2. 检查文件大小（不超过 10MB）
3. 检查文件内容：
   - 第一行必须是表头
   - 必填字段不能为空
   - 类目ID必须是10位数字
   - 价格必须大于0

4. 查看详细错误信息
   ```java
   // 后端日志
   log.error("解析Excel失败", e);
   ```

5. 使用模板文件测试
   ```bash
   # 下载模板，填写数据后上传
   ```

#### 问题：MyBatis Mapper 找不到
**症状**：启动时报 `Invalid bound statement`

**解决方案**：
1. 检查 Mapper 接口和 XML 文件路径
   ```
   src/main/java/com/meituan/product/mapper/ProductMapper.java
   src/main/resources/mapper/ProductMapper.xml
   ```

2. 检查 application.yml 配置
   ```yaml
   mybatis-plus:
     mapper-locations: classpath:mapper/*.xml
   ```

3. 检查 Mapper 接口注解
   ```java
   @Mapper
   public interface ProductMapper extends BaseMapper<Product> {
   ```

4. 重新构建项目
   ```bash
   mvn clean install
   ```

### 3. 数据库问题

#### 问题：表不存在
**症状**：查询时报 `Table 'meituan_product.product' doesn't exist`

**解决方案**：
1. 执行初始化脚本
   ```bash
   mysql -u root -p < database-init.sql
   ```

2. 手动创建表
   ```sql
   USE meituan_product;
   SOURCE database-init.sql;
   ```

3. 检查表是否存在
   ```sql
   SHOW TABLES;
   DESC product;
   ```

#### 问题：字符编码问题
**症状**：中文显示乱码

**解决方案**：
1. 检查数据库字符集
   ```sql
   SHOW VARIABLES LIKE 'character%';
   ```

2. 修改数据库字符集
   ```sql
   ALTER DATABASE meituan_product CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. 修改表字符集
   ```sql
   ALTER TABLE product CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

4. 检查连接字符集
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/meituan_product?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
   ```

### 4. 美团 API 问题

#### 问题：API 调用失败
**症状**：上传商品时报 `MeituanApiException`

**解决方案**：
1. 检查 API 地址配置
   ```yaml
   meituan:
     api:
       base-url: https://api.meituan.com
   ```

2. 检查 Access Token 是否有效
   ```java
   // 验证 Token
   boolean valid = meituanApiClient.validateAccessToken(token);
   ```

3. 检查网络连接
   ```bash
   ping api.meituan.com
   ```

4. 查看详细错误信息
   ```java
   log.error("调用美团API失败", e);
   ```

5. 检查 API 文档，确认接口地址和参数

#### 问题：Token 过期
**症状**：API 返回 `401 Unauthorized`

**解决方案**：
1. 重新获取 Access Token
2. 更新数据库中的 Token
   ```sql
   UPDATE merchant SET access_token = 'new_token', token_expires_at = '2026-12-31 23:59:59' WHERE id = 1;
   ```

3. 实现 Token 自动刷新机制

### 5. 性能问题

#### 问题：导入大文件很慢
**症状**：上传大 Excel 文件时响应缓慢

**解决方案**：
1. 增加文件上传大小限制
   ```yaml
   spring:
     servlet:
       multipart:
         max-file-size: 50MB
         max-request-size: 50MB
   ```

2. 使用分批处理
   ```java
   // 已实现，每批500条
   @Value("${meituan.upload.batch-size:500}")
   private Integer batchSize;
   ```

3. 优化数据库批量插入
   ```java
   productMapper.batchInsert(products);
   ```

4. 增加数据库连接池大小
   ```yaml
   spring:
     datasource:
       hikari:
         maximum-pool-size: 20
   ```

#### 问题：内存溢出
**症状**：处理大文件时报 `OutOfMemoryError`

**解决方案**：
1. 增加 JVM 内存
   ```bash
   # 启动时指定
   java -Xmx2g -Xms1g -jar app.jar
   ```

2. 使用流式处理
   ```java
   // 避免一次性加载所有数据到内存
   ```

3. 分批处理数据

### 6. 打包部署问题

#### 问题：前端打包失败
**症状**：运行 `pnpm run electron:build` 报错

**解决方案**：
1. 清理缓存
   ```bash
   rm -rf node_modules
   rm -rf dist
   pnpm install
   ```

2. 检查 package.json 配置
   ```json
   {
     "build": {
       "appId": "com.meituan.product",
       "productName": "美团商品管理工具"
     }
   }
   ```

3. 检查网络连接（下载 Electron 二进制文件）

#### 问题：后端打包失败
**症状**：运行 `mvn clean package` 报错

**解决方案**：
1. 跳过测试
   ```bash
   mvn clean package -DskipTests
   ```

2. 检查依赖
   ```bash
   mvn dependency:tree
   ```

3. 清理本地仓库
   ```bash
   rm -rf ~/.m2/repository
   mvn clean install
   ```

## 🔧 调试技巧

### 前端调试
1. 打开开发者工具：`Ctrl+Shift+D` 或 `F12`
2. 查看 Console 标签的错误信息
3. 查看 Network 标签的网络请求
4. 使用 Vue DevTools 调试组件状态

### 后端调试
1. 查看控制台日志
2. 使用 IDE 断点调试
3. 查看日志文件
4. 使用 Postman 测试 API

### 数据库调试
1. 使用 MySQL Workbench 或 Navicat
2. 查看慢查询日志
3. 使用 EXPLAIN 分析查询
4. 检查索引使用情况

## 📞 获取帮助

如果以上方法都无法解决问题：

1. **查看日志**
   - 前端：开发者工具 Console
   - 后端：控制台输出
   - 数据库：MySQL 错误日志

2. **搜索错误信息**
   - Google 搜索错误信息
   - Stack Overflow
   - GitHub Issues

3. **联系开发人员**
   - 提供详细的错误信息
   - 提供复现步骤
   - 提供环境信息（操作系统、版本等）

## 📝 日志位置

### 前端日志
- 开发模式：开发者工具 Console
- 生产模式：`%APPDATA%/meituan-product/logs/`

### 后端日志
- 控制台输出
- 日志文件：`logs/spring.log`

### 数据库日志
- MySQL 错误日志：`/var/log/mysql/error.log`
- 慢查询日志：`/var/log/mysql/slow.log`

---

**最后更新**：2026年2月9日
**版本**：v1.0
