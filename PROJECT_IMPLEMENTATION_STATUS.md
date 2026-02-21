# 美团商品批量上传管理工具 - 实现状态

## 📋 项目概述

本项目是一个基于 Electron + Vue3 + Spring Boot 3 的桌面端商品批量上传管理工具，用于帮助商家批量管理和上传商品到美团平台。

## ✅ 已完成功能

### 1. 前端 (Electron + Vue3 + Vite)

#### 1.1 桌面应用框架
- ✅ Electron 桌面应用搭建
- ✅ macOS 风格窗口控制（红黄绿三个圆点）
  - 红色：关闭窗口
  - 黄色：最小化
  - 绿色：最大化/还原
- ✅ 无边框窗口 + 自定义标题栏
- ✅ 快捷键支持（Ctrl+Shift+D 打开开发者工具）

#### 1.2 用户界面
- ✅ 登录/注册页面
- ✅ 桌面端后台管理布局
  - 左侧深色菜单栏（200px，#001529）
  - 顶部导航栏（面包屑 + 用户信息）
  - 美团黄色主题（#FFD100, #FFA726）
- ✅ 菜单项：
  - 首页（统计数据展示）
  - 商品管理
  - 批量导入
  - 模板管理
  - 操作日志
  - 系统设置

#### 1.3 商品导入功能
- ✅ 三步导入流程：
  1. 选择文件（支持拖拽上传）
  2. 数据预览
  3. 导入完成（显示成功/失败统计）
- ✅ 下载导入模板
- ✅ Excel 文件验证（格式、大小限制）
- ✅ 数据预览表格
- ✅ 导入结果展示（成功/失败统计）
- ✅ 错误详情展示

### 2. 后端 (Spring Boot 3 + MyBatis Plus)

#### 2.1 核心功能
- ✅ Excel 文件解析（支持 .xlsx 和 .xls）
- ✅ 商品数据验证
  - 必填字段验证
  - 数据格式验证
  - 类目ID格式验证（10位数字）
  - 价格范围验证
- ✅ 商品数据库管理（CRUD）
- ✅ 批量导入商品
- ✅ 生成美团上传模板
- ✅ 批量上传到美团平台
- ✅ 一键清空商品

#### 2.2 API 接口
- ✅ `GET /api/products/stats` - 获取商品统计
- ✅ `GET /api/products/download-template` - 下载导入模板
- ✅ `POST /api/products/preview` - 预览Excel数据
- ✅ `POST /api/products/import` - 导入商品
- ✅ `GET /api/products` - 获取商品列表
- ✅ `GET /api/products/{id}` - 获取商品详情
- ✅ `POST /api/products/generate-template` - 生成美团模板
- ✅ `POST /api/products/upload` - 上传到美团
- ✅ `DELETE /api/products/clear` - 清空商品

#### 2.3 数据处理
- ✅ Excel 模板生成（带样式）
- ✅ 美团上传模板生成（根据配置文件）
- ✅ 分批上传（默认500条/批）
- ✅ 错误处理和重试机制
- ✅ 事务管理

### 3. 数据库设计
- ✅ 商品表（product）
  - 基本信息：商品名称、类目ID、价格、库存
  - 扩展信息：描述、图片URL
  - 状态管理：待上传、已上传、上传失败
  - 商家关联：merchant_id

### 4. 美团 API 集成
- ✅ API 客户端封装
- ✅ 批量上传商品接口
- ✅ 清空商品接口
- ✅ 访问令牌验证
- ✅ 重试机制（最多3次，指数退避）
- ✅ 错误处理

## 📁 项目结构

```
MT_product_bulk_uploader/
├── meituan-frontend/          # 前端项目
│   ├── electron/              # Electron 主进程
│   │   ├── main.js           # 主进程入口
│   │   └── preload.js        # 预加载脚本
│   ├── src/
│   │   ├── api/              # API 接口
│   │   ├── router/           # 路由配置
│   │   ├── stores/           # 状态管理
│   │   ├── styles/           # 样式文件
│   │   ├── views/            # 页面组件
│   │   │   ├── Login.vue     # 登录页
│   │   │   ├── Register.vue  # 注册页
│   │   │   ├── Layout.vue    # 主布局
│   │   │   ├── Home.vue      # 首页
│   │   │   └── Import.vue    # 批量导入
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   └── vite.config.js
│
├── meituan-backend/           # 后端项目
│   ├── src/main/java/com/meituan/product/
│   │   ├── client/           # API 客户端
│   │   │   └── MeituanApiClient.java
│   │   ├── config/           # 配置类
│   │   │   └── TemplateConfig.java
│   │   ├── controller/       # 控制器
│   │   │   └── ProductController.java
│   │   ├── dto/              # 数据传输对象
│   │   ├── entity/           # 实体类
│   │   │   └── Product.java
│   │   ├── exception/        # 异常类
│   │   ├── mapper/           # MyBatis Mapper
│   │   │   └── ProductMapper.java
│   │   └── service/          # 服务类
│   │       ├── ExcelService.java
│   │       └── ProductService.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── template-config.json
│   └── pom.xml
│
├── 资源/                      # 资源文件
│   ├── 商品的类目信息.csv
│   └── 批量创建模板_v1.3.6.csv
│
├── start-all.bat             # 一键启动脚本
├── start-backend.bat         # 启动后端
├── start-frontend.bat        # 启动前端
└── 需求.md                    # 需求文档
```

## 🚀 如何启动项目

### 方式一：使用启动脚本（推荐）

```bash
# Windows 系统
start-all.bat          # 同时启动前后端

# 或分别启动
start-backend.bat      # 仅启动后端
start-frontend.bat     # 仅启动前端
```

### 方式二：手动启动

#### 启动后端
```bash
cd meituan-backend
mvn spring-boot:run
```

#### 启动前端
```bash
cd meituan-frontend
pnpm install           # 首次运行需要安装依赖
pnpm run electron:dev  # 启动 Electron 应用
```

## 📝 使用说明

### 1. 商品导入流程

1. **下载模板**
   - 点击"下载导入模板"按钮
   - 获取带示例数据的 Excel 模板

2. **填写商品信息**
   - 按照模板格式填写商品数据
   - 必填字段：商品名称、类目ID（10位数字）、价格
   - 可选字段：库存、商品描述、图片URL

3. **上传文件**
   - 拖拽或点击上传 Excel 文件
   - 支持 .xlsx 和 .xls 格式
   - 文件大小限制：10MB

4. **预览数据**
   - 系统自动解析并展示商品数据
   - 检查数据是否正确

5. **确认导入**
   - 点击"确认导入"按钮
   - 查看导入结果（成功/失败统计）

### 2. 生成美团模板

1. 在商品管理页面选择要上传的商品
2. 点击"生成美团模板"按钮
3. 系统自动生成符合美团格式的 Excel 文件
4. 下载文件后可直接上传到美团后台

### 3. 批量上传到美团

1. 选择要上传的商品
2. 输入美团访问令牌（Access Token）
3. 点击"批量上传"按钮
4. 系统自动分批上传（500条/批）
5. 查看上传结果

### 4. 清空商品

1. 点击"一键清空商品"按钮
2. 确认操作
3. 系统同时清空美团后台和本地数据库的商品

## ⚙️ 配置说明

### 后端配置 (application.yml)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/meituan_product?useUnicode=true&characterEncoding=utf8
    username: root
    password: your_password

meituan:
  api:
    base-url: https://api.meituan.com
    timeout: 30000
  upload:
    batch-size: 500
```

### 前端配置

- API 基础地址：`http://localhost:8080/api`
- 开发者工具快捷键：`Ctrl+Shift+D`

## 🎨 界面特点

1. **macOS 风格窗口控制**
   - 红黄绿三个圆点，悬停时显示图标
   - 圆点尺寸：16px，悬停放大 1.15 倍

2. **美团主题色**
   - 主色：#FFD100（美团黄）
   - 辅助色：#FFA726
   - 深色菜单：#001529

3. **响应式设计**
   - 适配不同屏幕尺寸
   - 流畅的动画效果

## 🔧 技术栈

### 前端
- Electron 28+
- Vue 3
- Vite 5
- Element Plus
- Pinia (状态管理)
- Vue Router
- Axios

### 后端
- Spring Boot 3
- MyBatis Plus
- MySQL 8
- Apache POI (Excel 处理)
- Lombok
- Spring Retry

## 📊 数据库表结构

### product 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| merchant_id | BIGINT | 商家ID |
| product_name | VARCHAR(255) | 商品名称 |
| category_id | VARCHAR(20) | 类目ID（10位数字） |
| price | DECIMAL(10,2) | 价格 |
| stock | INT | 库存 |
| description | TEXT | 商品描述 |
| image_url | VARCHAR(500) | 图片URL |
| status | TINYINT | 状态（0:待上传 1:已上传 2:失败） |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

## 🐛 已知问题

1. **美团 API 集成**
   - 当前使用的是模拟 API 地址
   - 需要替换为真实的美团开放平台 API
   - 需要申请美团开放平台账号和 API 权限

2. **用户认证**
   - 登录/注册功能为前端演示
   - 需要实现真实的用户认证系统

## 📌 下一步工作

### 必须完成
1. **集成真实美团 API**
   - 申请美团开放平台账号
   - 获取 API 文档
   - 实现真实的 API 调用

2. **用户认证系统**
   - 实现用户注册/登录
   - JWT Token 管理
   - 权限控制

3. **数据库初始化**
   - 创建数据库表
   - 初始化数据

### 可选优化
1. **商品管理页面**
   - 商品列表展示
   - 商品编辑/删除
   - 商品搜索/筛选

2. **模板管理**
   - 自定义模板配置
   - 模板版本管理

3. **操作日志**
   - 记录所有操作
   - 日志查询和导出

4. **系统设置**
   - 美团 API 配置
   - 上传参数配置
   - 主题切换

5. **错误处理优化**
   - 更详细的错误提示
   - 错误日志记录
   - 异常恢复机制

## 📞 技术支持

如有问题，请查看：
- 需求文档：`需求.md`
- 快速启动指南：`QUICK_START.md`
- 项目状态：`PROJECT_STATUS.md`

---

**最后更新时间：** 2026年2月9日
**项目版本：** v1.0.0
**开发状态：** 核心功能已完成，待集成真实美团API
