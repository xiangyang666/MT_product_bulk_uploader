# 快速打包指南

## 最简单的打包方式

### Windows 用户

1. 双击运行 `build-windows.bat`
2. 等待打包完成
3. 在 `dist-electron` 文件夹中找到安装包

### Mac 用户

1. 打开终端，进入项目目录
2. 运行：`chmod +x build-mac.sh && ./build-mac.sh`
3. 等待打包完成
4. 在 `dist-electron` 文件夹中找到安装包

## 手动打包命令

### 仅打包当前平台

```bash
# Windows
pnpm run electron:build:win

# Mac
pnpm run electron:build:mac
```

### 打包所有平台（不推荐在 Windows 上使用）

```bash
pnpm run electron:build:all
```

## 打包前检查清单

- [ ] 已安装 Node.js 16+
- [ ] 已安装 pnpm（或 npm）
- [ ] 已进入 meituan-frontend 目录
- [ ] 网络连接正常

## 常见问题

### 问题1：找不到 pnpm 命令
**解决方案：**
```bash
npm install -g pnpm
```

### 问题2：打包失败
**解决方案：**
1. 删除 `node_modules` 文件夹
2. 删除 `dist` 文件夹
3. 重新运行打包脚本

### 问题3：Mac 打包提示权限错误
**解决方案：**
```bash
chmod +x build-mac.sh
```

## 输出文件说明

打包成功后，在 `dist-electron` 目录下会生成：

### Windows
- `美团商品上传工具-1.0.0-x64.exe` - 安装程序（约 100-200MB）

### Mac
- `美团商品上传工具-1.0.0-x64.dmg` - Intel Mac 安装包
- `美团商品上传工具-1.0.0-arm64.dmg` - Apple Silicon 安装包

## 下一步

打包完成后，你可以：
1. 直接安装测试
2. 分发给其他用户
3. 上传到服务器

详细说明请查看 `BUILD.md`

