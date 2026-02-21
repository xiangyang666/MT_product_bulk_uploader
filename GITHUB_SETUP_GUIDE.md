# GitHub 仓库设置指南

## 第一步: 创建 GitHub 仓库

### 1. 登录 GitHub
访问 https://github.com 并登录你的账号

### 2. 创建新仓库
1. 点击右上角的 **+** 号
2. 选择 **New repository**
3. 填写仓库信息:
   - **Repository name**: `meituan-product-uploader` (或你喜欢的名字)
   - **Description**: 美团商品批量上传管理工具
   - **Public/Private**: 选择 Public (免费使用 GitHub Actions)
   - **不要勾选** "Initialize this repository with a README"
   - **不要添加** .gitignore 或 license (我们已经有了)
4. 点击 **Create repository**

### 3. 记录仓库地址
创建后会显示仓库地址,类似:
```
https://github.com/你的用户名/meituan-product-uploader.git
```

## 第二步: 初始化本地 Git 仓库

打开命令行,在项目根目录执行:

```bash
# 1. 初始化 Git 仓库 (如果还没有)
git init

# 2. 添加所有文件
git add .

# 3. 创建第一次提交
git commit -m "Initial commit: 美团商品上传工具"

# 4. 设置主分支名称为 main
git branch -M main

# 5. 添加远程仓库 (替换成你的仓库地址)
git remote add origin https://github.com/你的用户名/meituan-product-uploader.git

# 6. 推送到 GitHub
git push -u origin main
```

## 第三步: 验证设置

1. 刷新 GitHub 仓库页面
2. 应该能看到所有代码文件
3. 检查 `.github/workflows/build-electron.yml` 是否存在

## 第四步: 测试自动构建

### 方法 1: 手动触发 (推荐首次测试)

1. 在 GitHub 仓库页面,点击 **Actions** 标签
2. 如果看到提示 "Workflows aren't being run on this repository"
   - 点击 **I understand my workflows, go ahead and enable them**
3. 点击左侧的 **Build Electron App**
4. 点击右侧的 **Run workflow** 按钮
5. 选择 `main` 分支
6. 点击绿色的 **Run workflow** 按钮
7. 等待构建完成 (约 10-20 分钟)

### 方法 2: 通过标签触发

```bash
# 创建版本标签
git tag v1.0.0

# 推送标签
git push origin v1.0.0
```

## 第五步: 下载构建产物

### 从 Actions 下载:
1. 进入 **Actions** 标签
2. 点击最近的工作流运行
3. 等待构建完成 (绿色勾号)
4. 在页面底部的 **Artifacts** 部分:
   - 下载 `windows-installer` (Windows .exe)
   - 下载 `macos-installer` (macOS .dmg)

### 从 Releases 下载 (如果通过标签触发):
1. 进入 **Releases** 标签
2. 找到对应版本
3. 在 **Assets** 部分下载安装包

## 常见问题

### Q: 推送代码时要求输入用户名密码?
**A:** GitHub 已不支持密码认证,需要使用 Personal Access Token:

1. 访问 https://github.com/settings/tokens
2. 点击 **Generate new token** > **Generate new token (classic)**
3. 设置:
   - Note: `meituan-uploader`
   - Expiration: 选择有效期
   - 勾选 `repo` (完整权限)
4. 点击 **Generate token**
5. **复制并保存** token (只显示一次!)
6. 推送时使用 token 作为密码

### Q: Actions 构建失败?
**A:** 检查以下几点:
1. 查看 Actions 日志中的错误信息
2. 确保 `meituan-frontend/package.json` 存在
3. 确保 `meituan-frontend/package-lock.json` 存在
4. 检查依赖是否正确安装

### Q: 构建时间太长?
**A:** 正常情况:
- Windows: 5-10 分钟
- macOS: 10-15 分钟
- 首次构建会更慢 (需要下载依赖)

### Q: 想要私有仓库?
**A:** 可以,但注意:
- 私有仓库有 Actions 分钟数限制
- 每月免费额度:
  - 2000 分钟 (Linux)
  - 1000 分钟 (Windows)  
  - 200 分钟 (macOS)
- macOS 构建消耗最快

### Q: 如何更新代码?
**A:** 
```bash
# 1. 修改代码
# 2. 提交更改
git add .
git commit -m "更新说明"
git push origin main

# 3. 如果要发布新版本
git tag v1.0.1
git push origin v1.0.1
```

## 使用 release.bat 快速发布

我已经创建了 `release.bat` 脚本,简化发布流程:

```bash
# 直接运行
release.bat

# 按提示操作:
# 1. 确认提交更改
# 2. 输入版本号 (如: 1.0.1)
# 3. 确认发布
# 4. 自动推送代码和标签
```

## 安全建议

1. **不要提交敏感信息**:
   - 数据库密码
   - API 密钥
   - 私钥文件

2. **使用 .gitignore**:
   - 已包含常见忽略规则
   - 检查是否需要添加其他文件

3. **使用 GitHub Secrets**:
   - 存储敏感配置
   - 在 Actions 中安全使用

## 下一步

1. ✅ 创建 GitHub 仓库
2. ✅ 推送代码
3. ✅ 启用 Actions
4. ✅ 测试构建
5. ✅ 下载安装包
6. ✅ 测试安装和运行
7. 🎉 发布到 landing-page!

## 需要帮助?

- GitHub Actions 文档: https://docs.github.com/en/actions
- Git 教程: https://git-scm.com/book/zh/v2
- 联系我获取更多帮助
