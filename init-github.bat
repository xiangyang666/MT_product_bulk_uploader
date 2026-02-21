@echo off
setlocal enabledelayedexpansion

echo ========================================
echo GitHub 仓库初始化助手
echo ========================================
echo.

REM 检查 Git 是否安装
git --version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未安装 Git
    echo 请访问 https://git-scm.com/download/win 下载安装
    pause
    exit /b 1
)

echo Git 版本:
git --version
echo.

REM 检查是否已经是 Git 仓库
if exist ".git" (
    echo 检测到已存在 Git 仓库
    set /p reinit="是否重新初始化? (y/n): "
    if /i not "!reinit!"=="y" (
        echo 已取消
        pause
        exit /b 0
    )
)

echo.
echo ========================================
echo 第一步: 配置 Git 用户信息
echo ========================================
echo.

REM 检查 Git 用户配置
git config user.name >nul 2>&1
if errorlevel 1 (
    set /p username="请输入你的 Git 用户名: "
    git config --global user.name "!username!"
)

git config user.email >nul 2>&1
if errorlevel 1 (
    set /p email="请输入你的 Git 邮箱: "
    git config --global user.email "!email!"
)

echo 当前 Git 配置:
echo 用户名: 
git config user.name
echo 邮箱: 
git config user.email
echo.

echo ========================================
echo 第二步: 输入 GitHub 仓库信息
echo ========================================
echo.
echo 请先在 GitHub 上创建仓库:
echo 1. 访问 https://github.com/new
echo 2. 填写仓库名称 (如: meituan-product-uploader)
echo 3. 选择 Public
echo 4. 不要勾选任何初始化选项
echo 5. 点击 Create repository
echo.
set /p repo_url="请输入仓库地址 (如: https://github.com/username/repo.git): "

if "!repo_url!"=="" (
    echo 错误: 仓库地址不能为空
    pause
    exit /b 1
)

echo.
echo ========================================
echo 第三步: 初始化本地仓库
echo ========================================
echo.

echo [1/6] 初始化 Git 仓库...
if not exist ".git" (
    git init
    if errorlevel 1 (
        echo 初始化失败
        pause
        exit /b 1
    )
    echo ✓ 初始化成功
) else (
    echo ✓ 仓库已存在
)

echo.
echo [2/6] 设置主分支为 main...
git branch -M main
if errorlevel 1 (
    echo 设置失败
    pause
    exit /b 1
)
echo ✓ 设置成功

echo.
echo [3/6] 添加所有文件...
git add .
if errorlevel 1 (
    echo 添加文件失败
    pause
    exit /b 1
)
echo ✓ 文件已添加

echo.
echo [4/6] 创建初始提交...
git commit -m "Initial commit: 美团商品批量上传工具"
if errorlevel 1 (
    echo 提交失败
    pause
    exit /b 1
)
echo ✓ 提交成功

echo.
echo [5/6] 添加远程仓库...
git remote remove origin >nul 2>&1
git remote add origin !repo_url!
if errorlevel 1 (
    echo 添加远程仓库失败
    pause
    exit /b 1
)
echo ✓ 远程仓库已添加

echo.
echo [6/6] 推送到 GitHub...
echo.
echo 注意: 如果要求输入密码,请使用 Personal Access Token
echo 获取 Token: https://github.com/settings/tokens
echo.
git push -u origin main
if errorlevel 1 (
    echo.
    echo 推送失败!
    echo.
    echo 可能的原因:
    echo 1. 网络连接问题
    echo 2. 需要 Personal Access Token (不能使用密码)
    echo 3. 仓库地址错误
    echo.
    echo 获取 Token 步骤:
    echo 1. 访问 https://github.com/settings/tokens
    echo 2. 点击 Generate new token (classic)
    echo 3. 勾选 repo 权限
    echo 4. 生成并复制 token
    echo 5. 推送时使用 token 作为密码
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo 初始化完成! 🎉
echo ========================================
echo.
echo 仓库地址: !repo_url!
echo.
echo 接下来:
echo 1. 访问你的 GitHub 仓库查看代码
echo 2. 进入 Actions 标签启用工作流
echo 3. 运行 release.bat 发布第一个版本
echo.
echo 或者手动触发构建:
echo 1. 访问 Actions 标签
echo 2. 选择 Build Electron App
echo 3. 点击 Run workflow
echo.

pause
