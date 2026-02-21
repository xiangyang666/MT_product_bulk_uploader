@echo off
setlocal enabledelayedexpansion

echo ========================================
echo 美团商品上传工具 - 版本发布助手
echo ========================================
echo.

REM 检查是否在 git 仓库中
git rev-parse --git-dir >nul 2>&1
if errorlevel 1 (
    echo 错误: 当前目录不是 Git 仓库
    echo 请先初始化 Git 仓库: git init
    pause
    exit /b 1
)

REM 检查是否有未提交的更改
git diff-index --quiet HEAD --
if errorlevel 1 (
    echo 警告: 有未提交的更改
    echo.
    git status --short
    echo.
    set /p commit="是否提交这些更改? (y/n): "
    if /i "!commit!"=="y" (
        set /p message="请输入提交信息: "
        git add .
        git commit -m "!message!"
        if errorlevel 1 (
            echo 提交失败
            pause
            exit /b 1
        )
    ) else (
        echo 请先提交更改后再发布版本
        pause
        exit /b 1
    )
)

echo.
echo 当前版本信息:
type meituan-frontend\package.json | findstr "version"
echo.

set /p version="请输入新版本号 (例如: 1.0.0): "
if "!version!"=="" (
    echo 错误: 版本号不能为空
    pause
    exit /b 1
)

echo.
echo 即将执行以下操作:
echo 1. 推送代码到 GitHub
echo 2. 创建标签: v!version!
echo 3. 推送标签触发自动构建
echo.
set /p confirm="确认继续? (y/n): "
if /i not "!confirm!"=="y" (
    echo 已取消
    pause
    exit /b 0
)

echo.
echo [1/3] 推送代码到 GitHub...
git push origin main
if errorlevel 1 (
    echo 推送失败,请检查网络连接和 GitHub 权限
    pause
    exit /b 1
)

echo.
echo [2/3] 创建标签 v!version!...
git tag v!version!
if errorlevel 1 (
    echo 创建标签失败,可能标签已存在
    pause
    exit /b 1
)

echo.
echo [3/3] 推送标签...
git push origin v!version!
if errorlevel 1 (
    echo 推送标签失败
    pause
    exit /b 1
)

echo.
echo ========================================
echo 发布成功!
echo ========================================
echo.
echo 版本: v!version!
echo.
echo 接下来:
echo 1. 访问 GitHub Actions 查看构建进度
echo 2. 构建完成后在 Releases 页面查看
echo 3. 下载并测试安装包
echo.
echo GitHub Actions 地址:
echo https://github.com/你的用户名/你的仓库名/actions
echo.

pause
