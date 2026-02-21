# 美团商品批量上传管理工具

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)
![Platform](https://img.shields.io/badge/platform-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey.svg)

一个基于 Electron + Vue3 + Spring Boot 3 的桌面端商品批量上传管理工具

[快速开始](#-快速开始) • [功能特性](#-功能特性) • [技术栈](#-技术栈) • [文档](#-文档)

</div>

---

## 📖 项目简介

本项目是一个专为美团商家设计的桌面端商品批量管理工具，旨在简化商品上传流程，提高工作效率。

### 核心功能
- 📥 **批量导入**：支持 Excel/CSV 文件批量导入商品信息
- 📄 **模板生成**：自动生成符合美团格式的上传模板
- 🚀 **批量上传**：一键批量上传商品到美团平台
- 🗑️ **一键清空**：快速清空美团后台所有商品
- 📊 **数据统计**：实时查看商品统计数据
- 🎨 **美观界面**：macOS 风格设计，美团主题色

### 适用场景
- 新店开业，需要批量上传大量商品
- 每日更新商品信息
- 季节性商品批量上下架
- 商品信息批量修改

## 🚀 快速开始

### 环境要求
- **Node.js**: 16.x 或更高版本
- **Java**: 17 或更高版本
- **Maven**: 3.6 或更高版本
- **MySQL**: 8.0 或更高版本
- **pnpm**: 8.x 或更高版本（推荐）

### 安装步骤

#### 1. 克隆项目
```bash
git clone https://github.com/yourusername/MT_product_bulk_uploader.git
cd MT_product_bulk_uploader
```

#### 2. 初始化数据库
```bash
# 连接到 MySQL
mysql -u root -p

# 执行初始化脚本
source database-init.sql
```

#### 3. 配置后端
编辑 `meituan-backend/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/meituan_product
    username: root
    password: your_password  # 修改为你的密码
```

#### 4. 安装前端依赖
```bash
cd meituan-frontend
pnpm install
```

#### 5. 启动项目

**方式一：使用启动脚本（推荐）**
```bash
# Windows
start-all.bat

# 或分别启动
start-backend.bat   # 启动后端
start-frontend.bat  # 启动前端
```

**方式二：手动启动**
```bash
# 启动后端（新终端）
cd meituan-backend
mvn spring-boot:run

# 启动前端（新终端）
cd meituan-frontend
pnpm run electron:dev
```

#### 6. 访问应用
- 前端应用会自动打开 Electron 窗口
- 后端 API: http://localhost:8080/api
- 默认登录账号：admin / 123456

## ✨ 功能特性

### 1. 商品批量导入
- ✅ 支持 Excel (.xlsx, .xls) 和 CSV 格式
- ✅ 拖拽上传，操作便捷
- ✅ 实时数据预览
- ✅ 完整的数据验证
- ✅ 详细的错误提示
- ✅ 导入结果统计

### 2. 美团模板生成
- ✅ 自动生成符合美团格式的 Excel 模板
- ✅ 支持自定义字段映射
- ✅ 批量选择商品生成
- ✅ 一键下载模板文件

### 3. 批量上传管理
- ✅ 分批上传（500条/批）
- ✅ 上传进度显示
- ✅ 失败重试机制（最多3次）
- ✅ 上传结果统计
- ✅ 错误详情展示

### 4. 商品管理
- ✅ 商品列表查看
- ✅ 商品状态管理（待上传、已上传、失败）
- ✅ 商品搜索和筛选
- ✅ 商品详情查看

### 5. 数据统计
- ✅ 商品总数统计
- ✅ 上传状态统计
- ✅ 实时数据更新
- ✅ 可视化图表展示

### 6. 界面设计
- ✅ macOS 风格窗口控制（红黄绿圆点）
- ✅ 美团黄色主题（#FFD100）
- ✅ 响应式布局
- ✅ 流畅的动画效果
- ✅ 深色侧边栏菜单

## 🛠 技术栈

### 前端技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Electron | 28+ | 桌面应用框架 |
| Vue 3 | 3.x | 渐进式 JavaScript 框架 |
| Vite | 5.x | 下一代前端构建工具 |
| Element Plus | 2.x | Vue 3 组件库 |
| Pinia | 2.x | Vue 状态管理 |
| Vue Router | 4.x | Vue 路由管理 |
| Axios | 1.x | HTTP 客户端 |

### 后端技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.x | Java 应用框架 |
| MyBatis Plus | 3.x | MyBatis 增强工具 |
| MySQL | 8.0+ | 关系型数据库 |
| Apache POI | 5.x | Excel 处理库 |
| Lombok | 1.x | Java 简化工具 |
| Spring Retry | 2.x | 重试机制 |

## 📁 项目结构

```
MT_product_bulk_uploader/
├── meituan-frontend/              # 前端项目
│   ├── electron/                  # Electron 主进程
│   │   ├── main.js               # 主进程入口
│   │   └── preload.js            # 预加载脚本
│   ├── src/
│   │   ├── api/                  # API 接口
│   │   ├── router/               # 路由配置
│   │   ├── stores/               # 状态管理
│   │   ├── styles/               # 样式文件
│   │   ├── views/                # 页面组件
│   │   ├── App.vue               # 根组件
│   │   └── main.js               # 入口文件
│   ├── package.json
│   └── vite.config.js
│
├── meituan-backend/               # 后端项目
│   ├── src/main/java/
│   │   └── com/meituan/product/
│   │       ├── client/           # API 客户端
│   │       ├── config/           # 配置类
│   │       ├── controller/       # 控制器
│   │       ├── dto/              # 数据传输对象
│   │       ├── entity/           # 实体类
│   │       ├── exception/        # 异常类
│   │       ├── mapper/           # MyBatis Mapper
│   │       └── service/          # 服务类
│   ├── src/main/resources/
│   │   ├── application.yml       # 应用配置
│   │   └── mapper/               # MyBatis XML
│   └── pom.xml
│
├── 资源/                          # 资源文件
│   ├── 商品的类目信息.csv
│   └── 批量创建模板_v1.3.6.csv
│
├── database-init.sql              # 数据库初始化脚本
├── start-all.bat                  # 一键启动脚本
├── start-backend.bat              # 启动后端
├── start-frontend.bat             # 启动前端
└── README.md                      # 项目说明
```

## 📚 文档

- [📋 项目实现状态](PROJECT_IMPLEMENTATION_STATUS.md) - 详细的功能实现清单
- [🔄 上下文转移总结](CONTEXT_TRANSFER_SUMMARY.md) - 项目开发历程
- [⚡ 快速参考指南](QUICK_REFERENCE.md) - 常用命令和配置
- [🐛 故障排除指南](TROUBLESHOOTING.md) - 常见问题解决方案
- [📝 需求文档](需求.md) - 原始需求说明

## 🔧 配置说明

### 后端配置
编辑 `meituan-backend/src/main/resources/application.yml`：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/meituan_product
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
编辑 `meituan-frontend/src/api/index.js`：

```javascript
const baseURL = 'http://localhost:8080/api';
```

## 📝 使用说明

### 1. 商品导入流程
1. 点击"批量导入"菜单
2. 下载导入模板（可选）
3. 填写商品信息
4. 上传 Excel 文件
5. 预览数据
6. 确认导入

### 2. 生成美团模板
1. 进入"商品管理"
2. 选择要上传的商品
3. 点击"生成美团模板"
4. 下载生成的 Excel 文件
5. 上传到美团后台

### 3. 批量上传
1. 选择要上传的商品
2. 输入美团 Access Token
3. 点击"批量上传"
4. 等待上传完成
5. 查看上传结果

### 4. 清空商品
1. 点击"一键清空商品"
2. 确认操作
3. 等待清空完成

## 🎯 开发计划

### 已完成 ✅
- [x] 基础框架搭建
- [x] 用户界面设计
- [x] Excel 导入导出
- [x] 商品数据管理
- [x] 美团 API 集成
- [x] 批量上传功能
- [x] 一键清空功能

### 进行中 🚧
- [ ] 集成真实美团 API
- [ ] 用户认证系统
- [ ] 商品管理页面完善

### 计划中 📅
- [ ] 模板管理功能
- [ ] 操作日志记录
- [ ] 系统设置页面
- [ ] 数据导出功能
- [ ] 多语言支持

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证

## 🙏 致谢

- 感谢 Element Plus 提供优秀的 UI 组件
- 感谢 Electron 提供跨平台桌面应用框架
- 感谢 Spring Boot 提供强大的后端框架

---

<div align="center">

**如果这个项目对你有帮助，请给一个 ⭐️ Star！**

Made with ❤️ by Development Team

</div>
