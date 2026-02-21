# 环境变量配置说明

## 环境变量文件

项目使用 Vite 的环境变量系统,支持以下环境文件:

- `.env.development` - 开发环境配置 (npm run dev)
- `.env.production` - 生产环境配置 (npm run build)
- `.env.local` - 本地覆盖配置 (优先级最高,不提交到git)

## 配置项

### VITE_API_BASE_URL

API服务器的基础URL

**开发环境默认值**: `http://localhost:8080/api`
**生产环境默认值**: `http://106.55.102.48:8080/api`

## 使用方法

### 开发环境

```bash
# 使用默认开发配置
npm run dev

# 或创建 .env.local 文件自定义配置
cp .env.local.example .env.local
# 编辑 .env.local 修改 VITE_API_BASE_URL
```

### 生产环境

```bash
# 使用默认生产配置
npm run build

# 或设置环境变量
VITE_API_BASE_URL=http://your-api-server.com/api npm run build
```

## 在代码中使用

```typescript
// 环境变量会在构建时被替换
const apiUrl = import.meta.env.VITE_API_BASE_URL

// 示例: src/utils/api.ts
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://106.55.102.48:8080/api'
```

## 注意事项

1. 环境变量必须以 `VITE_` 开头才能在客户端代码中访问
2. 修改环境变量后需要重启开发服务器
3. `.env.local` 文件不会提交到git,适合存放个人配置
4. 生产构建时会使用 `.env.production` 中的配置
