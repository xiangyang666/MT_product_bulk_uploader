# Electron API URL 修复完成

## 问题描述

打包后的 Electron 应用登录时，请求地址变成了 `file:///D:/api/auth/login`，导致无法连接到后端服务器。

## 问题原因

在 `src/api/index.js` 中，axios 的 `baseURL` 使用了相对路径 `/api`。这在开发环境中可以通过 Vite 的代理工作，但在打包后的 Electron 应用中，由于使用 `file://` 协议加载本地文件，相对路径会被解析为 `file:///D:/api`。

## 解决方案

修改 `src/api/index.js`，让 axios 使用环境变量中的完整 HTTP URL：

```javascript
import { API_BASE_URL } from '@/config'

const request = axios.create({
  baseURL: `${API_BASE_URL}/api`,  // 使用完整 URL
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})
```

## 修改内容

### 文件：`meituan-frontend/src/api/index.js`

**修改前：**
```javascript
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',  // ❌ 相对路径
  // ...
})
```

**修改后：**
```javascript
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { API_BASE_URL } from '@/config'

const request = axios.create({
  baseURL: `${API_BASE_URL}/api`,  // ✅ 完整 URL
  // ...
})
```

## 环境配置

### 开发环境 (`.env.development`)
```
VITE_API_BASE_URL=http://127.0.0.1:8080
```

### 生产环境 (`.env.production`)
```
VITE_API_BASE_URL=http://106.55.102.48:8080
```

## 打包结果

✅ 前端构建成功
✅ Electron 打包成功

**安装包位置：**
```
meituan-frontend\dist-electron\美团商品上传工具-1.0.0-x64.exe
```

**文件大小：** 103.8 MB

## 测试说明

### 1. 开发环境测试
- API 请求地址：`http://127.0.0.1:8080/api/auth/login`
- 通过 Vite 代理访问后端

### 2. 生产环境测试
- API 请求地址：`http://106.55.102.48:8080/api/auth/login`
- 直接访问远程服务器

### 3. Electron 应用测试
- 运行打包后的应用：`美团商品上传工具-1.0.0-x64.exe`
- 测试登录功能
- 验证 API 请求使用完整的 HTTP URL

## 工作原理

1. **环境变量加载**：Vite 在构建时会将 `.env.production` 中的环境变量嵌入到代码中
2. **配置模块**：`src/config/index.js` 从 `import.meta.env` 读取 `VITE_API_BASE_URL`
3. **API 客户端**：`src/api/index.js` 使用配置模块中的 `API_BASE_URL` 构建完整的请求 URL
4. **请求发送**：所有 API 请求都会使用完整的 HTTP URL，无论在什么环境下运行

## 优势

✅ 开发和生产环境无缝切换
✅ 不需要修改代码，只需配置环境变量
✅ Electron 应用可以正确连接到远程服务器
✅ 保持了开发环境的代理功能

## 下一步

1. 安装并运行打包后的应用
2. 测试登录功能
3. 验证所有 API 请求都能正常工作
4. 如果有问题，检查浏览器控制台的网络请求

## 相关文件

- `.kiro/specs/electron-api-url-fix/requirements.md` - 需求文档
- `.kiro/specs/electron-api-url-fix/design.md` - 设计文档
- `.kiro/specs/electron-api-url-fix/tasks.md` - 任务列表
