# 打包说明文档

## 环境要求

### Windows 打包
- Node.js 16+
- pnpm 或 npm
- Windows 10/11

### Mac 打包
- Node.js 16+
- pnpm 或 npm
- macOS 10.13+
- Xcode Command Line Tools

## 打包步骤

### 1. 安装依赖
```bash
cd meituan-frontend
pnpm install
# 或
npm install
```

### 2. 打包命令

#### 仅打包 Windows 版本
```bash
pnpm run electron:build:win
# 或
npm run electron:build:win
```

#### 仅打包 Mac 版本（需要在 Mac 系统上运行）
```bash
pnpm run electron:build:mac
# 或
npm run electron:build:mac
```

#### 同时打包 Windows 和 Mac 版本
```bash
pnpm run electron:build:all
# 或
npm run electron:build:all
```

注意：在 Windows 上打包 Mac 版本需要额外配置，建议在对应平台上打包。

### 3. 输出文件

打包完成后，安装包会生成在 `dist-electron` 目录下：

#### Windows
- `美团商品上传工具-1.0.0-x64.exe` - Windows 安装程序

#### Mac
- `美团商品上传工具-1.0.0-x64.dmg` - Intel Mac 安装包
- `美团商品上传工具-1.0.0-arm64.dmg` - Apple Silicon (M1/M2) 安装包

## 打包配置说明

### 应用信息
- 应用名称：美团商品上传工具
- 应用 ID：com.meituan.product.upload
- 版本号：1.0.0

### Windows 配置
- 安装方式：NSIS 安装程序
- 支持架构：x64
- 安装选项：
  - 允许用户选择安装目录
  - 创建桌面快捷方式
  - 创建开始菜单快捷方式

### Mac 配置
- 安装方式：DMG 磁盘镜像
- 支持架构：x64 (Intel) 和 arm64 (Apple Silicon)
- 应用分类：商业工具

## 常见问题

### 1. 打包失败
- 确保已安装所有依赖：`pnpm install`
- 确保已构建前端代码：`pnpm run build`
- 检查 Node.js 版本是否符合要求

### 2. Mac 打包需要代码签名
如果需要发布到 Mac App Store 或进行公证，需要：
- Apple Developer 账号
- 配置代码签名证书
- 在 package.json 中添加签名配置

### 3. 跨平台打包
- 在 Windows 上打包 Mac 版本需要额外配置
- 建议在目标平台上进行打包
- 或使用 CI/CD 服务（如 GitHub Actions）进行多平台打包

## 测试打包结果

### Windows
1. 运行生成的 .exe 安装程序
2. 按照安装向导完成安装
3. 启动应用测试功能

### Mac
1. 打开生成的 .dmg 文件
2. 将应用拖拽到 Applications 文件夹
3. 启动应用测试功能

## 更新版本号

修改 `package.json` 中的 `version` 字段：
```json
{
  "version": "1.0.1"
}
```

## 自定义图标

如果需要使用自定义图标：
1. 准备图标文件：
   - Windows: 256x256 PNG 或 ICO 格式
   - Mac: 512x512 PNG 或 ICNS 格式
2. 将图标文件放在 `build` 目录下
3. 更新 package.json 中的图标路径

## 发布

打包完成后，可以通过以下方式发布：
1. 直接分发安装包文件
2. 上传到公司内部服务器
3. 使用自动更新服务（需要额外配置）

