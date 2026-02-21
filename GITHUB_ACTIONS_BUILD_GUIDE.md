# GitHub Actions 自动打包指南

## 概述

使用 GitHub Actions 自动在云端打包 Windows 和 macOS 版本的 Electron 应用。

## 使用方法

### 方法 1: 通过 Git 标签触发 (推荐)

1. **提交代码到 GitHub**
   ```bash
   git add .
   git commit -m "准备发布 v1.0.0"
   git push origin main
   ```

2. **创建并推送版本标签**
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

3. **自动构建**
   - GitHub Actions 会自动检测到标签
   - 同时在 Windows 和 macOS 环境中构建
   - 构建完成后自动创建 Release
   - 安装包会附加到 Release 中

### 方法 2: 手动触发

1. 访问 GitHub 仓库页面
2. 点击 **Actions** 标签
3. 选择 **Build Electron App** 工作流
4. 点击 **Run workflow** 按钮
5. 选择分支并点击 **Run workflow**

## 查看构建结果

### 下载构建产物

1. 进入 **Actions** 标签
2. 点击最近的工作流运行
3. 在 **Artifacts** 部分下载:
   - `windows-installer` - Windows 安装包 (.exe)
   - `macos-installer` - macOS 安装包 (.dmg)

### 从 Release 下载

如果是通过标签触发的构建:
1. 进入 **Releases** 标签
2. 找到对应版本的 Release
3. 下载附件中的安装包

## 构建配置

### 当前配置

- **Windows**: 
  - 构建环境: `windows-latest`
  - 输出格式: NSIS 安装包 (.exe)
  - 架构: x64

- **macOS**: 
  - 构建环境: `macos-latest`
  - 输出格式: DMG 镜像 (.dmg)
  - 架构: x64 (Intel) 和 arm64 (Apple Silicon)

### 修改版本号

在 `meituan-frontend/package.json` 中修改:
```json
{
  "version": "1.0.0"
}
```

## macOS 代码签名 (可选)

如果需要对 macOS 应用进行代码签名:

1. **获取 Apple 开发者证书**
   - 需要 Apple Developer 账号
   - 导出 .p12 证书文件

2. **添加 GitHub Secrets**
   - 进入仓库 Settings > Secrets and variables > Actions
   - 添加以下 secrets:
     - `MAC_CERT`: 证书文件的 base64 编码
     - `MAC_CERT_PASSWORD`: 证书密码

3. **取消注释工作流中的代码签名配置**
   在 `.github/workflows/build-electron.yml` 中取消注释:
   ```yaml
   env:
     CSC_LINK: ${{ secrets.MAC_CERT }}
     CSC_KEY_PASSWORD: ${{ secrets.MAC_CERT_PASSWORD }}
   ```

## 故障排除

### 构建失败

1. 检查 Actions 日志查看错误信息
2. 确保 `meituan-frontend/package.json` 配置正确
3. 确保所有依赖都在 `package.json` 中声明

### 构建时间过长

- Windows 构建通常需要 5-10 分钟
- macOS 构建通常需要 10-15 分钟
- 如果超过 30 分钟,可能有问题

### 无法下载产物

- 构建产物保留 90 天
- Release 附件永久保留
- 建议使用标签触发构建以创建 Release

## 本地测试

在推送到 GitHub 之前,可以本地测试:

**Windows:**
```bash
cd meituan-frontend
npm install
npm run build
npm run electron:build:win
```

**macOS (需要 Mac 电脑):**
```bash
cd meituan-frontend
npm install
npm run build
npm run electron:build:mac
```

## 版本发布流程

1. 更新版本号 (`meituan-frontend/package.json`)
2. 更新 CHANGELOG (如果有)
3. 提交代码
4. 创建并推送标签
5. 等待 GitHub Actions 完成构建
6. 检查 Release 页面
7. 编辑 Release 添加更新说明
8. 发布 Release

## 成本

- GitHub Actions 对公开仓库免费
- 私有仓库每月有免费额度:
  - 2000 分钟 (Linux)
  - 1000 分钟 (Windows)
  - 200 分钟 (macOS)
- 超出后按分钟计费

## 注意事项

1. **首次使用**: 第一次构建可能需要更长时间(安装依赖)
2. **并行构建**: Windows 和 macOS 同时构建,节省时间
3. **缓存**: 使用 npm 缓存加速后续构建
4. **安全**: 不要在代码中硬编码敏感信息,使用 GitHub Secrets
5. **测试**: 下载构建产物后务必测试安装和运行

## 相关链接

- [GitHub Actions 文档](https://docs.github.com/en/actions)
- [electron-builder 文档](https://www.electron.build/)
- [代码签名指南](https://www.electron.build/code-signing)
