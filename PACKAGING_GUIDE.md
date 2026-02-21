# 美团商品上传工具 - 打包部署指南

## 📦 项目打包配置完成

项目已配置好 Windows 和 Mac 双平台打包支持。

## 🚀 快速开始

### Windows 打包

**方式一：使用脚本（推荐）**
```bash
# 双击运行
build-windows.bat
```

**方式二：手动命令**
```bash
cd meituan-frontend
pnpm install
pnpm run electron:build:win
```

### Mac 打包

**方式一：使用脚本（推荐）**
```bash
cd meituan-frontend
chmod +x build-mac.sh
./build-mac.sh
```

**方式二：手动命令**
```bash
cd meituan-frontend
pnpm install
pnpm run electron:build:mac
```

## 📋 打包前准备

### 环境要求
- Node.js 16 或更高版本
- pnpm（推荐）或 npm
- 足够的磁盘空间（至少 2GB）

### 安装 pnpm（如果还没安装）
```bash
npm install -g pnpm
```

## 📁 输出文件

打包完成后，安装包会生成在 `meituan-frontend/dist-electron/` 目录：

### Windows
```
dist-electron/
  └── 美团商品上传工具-1.0.0-x64.exe    (约 150-200MB)
```

### Mac
```
dist-electron/
  ├── 美团商品上传工具-1.0.0-x64.dmg     (Intel Mac)
  └── 美团商品上传工具-1.0.0-arm64.dmg   (Apple Silicon)
```

## ⚙️ 打包配置说明

### 应用信息
- **应用名称**：美团商品上传工具
- **应用 ID**：com.meituan.product.upload
- **版本号**：1.0.0
- **主程序**：electron/main.js

### Windows 配置
- **安装方式**：NSIS 安装程序
- **架构支持**：x64
- **安装选项**：
  - ✅ 允许选择安装目录
  - ✅ 创建桌面快捷方式
  - ✅ 创建开始菜单快捷方式

### Mac 配置
- **安装方式**：DMG 磁盘镜像
- **架构支持**：x64 (Intel) + arm64 (Apple Silicon)
- **应用分类**：商业工具

## 🛠️ 可用的打包命令

```bash
# 仅打包 Windows
pnpm run electron:build:win

# 仅打包 Mac
pnpm run electron:build:mac

# 打包所有平台（需要在对应平台上运行）
pnpm run electron:build:all

# 开发模式运行
pnpm run electron:dev
```

## 🔧 常见问题

### 1. 打包失败：找不到 pnpm
**解决方案：**
```bash
npm install -g pnpm
```

### 2. 打包失败：依赖安装错误
**解决方案：**
```bash
# 清理并重新安装
rm -rf node_modules pnpm-lock.yaml
pnpm install
```

### 3. Windows 上打包 Mac 版本失败
**原因：** 跨平台打包需要额外配置

**解决方案：**
- 在 Mac 上打包 Mac 版本
- 或使用 CI/CD 服务（如 GitHub Actions）

### 4. Mac 打包提示权限错误
**解决方案：**
```bash
chmod +x build-mac.sh
```

### 5. 打包后应用无法启动
**检查项：**
- 确保后端服务已启动
- 检查 API 地址配置
- 查看应用日志

## 📝 修改版本号

编辑 `meituan-frontend/package.json`：
```json
{
  "version": "1.0.1"
}
```

## 🎨 自定义应用图标

当前使用 `public/meituan.png` 作为应用图标。

如需更换：
1. 准备图标文件（建议 512x512 PNG）
2. 替换 `public/meituan.png`
3. 重新打包

## 📦 打包流程说明

1. **安装依赖** - 安装所有 npm 包
2. **构建前端** - 使用 Vite 构建 Vue 应用
3. **打包应用** - 使用 electron-builder 打包成安装程序

整个过程大约需要 5-10 分钟（取决于网络和机器性能）。

## 🚢 分发安装包

### 内部分发
1. 将生成的安装包上传到公司服务器
2. 分享下载链接给用户

### 用户安装

**Windows：**
1. 下载 `.exe` 文件
2. 双击运行安装程序
3. 按照向导完成安装

**Mac：**
1. 下载 `.dmg` 文件
2. 双击打开
3. 拖拽应用到 Applications 文件夹

## 📚 相关文档

- `BUILD.md` - 详细打包说明
- `QUICK_BUILD.md` - 快速打包指南
- `README.md` - 项目说明

## ✅ 打包检查清单

打包前：
- [ ] 代码已提交
- [ ] 版本号已更新
- [ ] 依赖已安装
- [ ] 本地测试通过

打包后：
- [ ] 安装包已生成
- [ ] 安装测试通过
- [ ] 功能测试通过
- [ ] 准备分发

## 🎯 下一步

打包完成后，建议：
1. 在干净的系统上测试安装
2. 测试所有核心功能
3. 准备用户使用文档
4. 规划版本更新机制

---

**需要帮助？** 查看详细文档或联系开发团队。

