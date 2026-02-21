#!/bin/bash

echo "========================================"
echo "美团商品上传工具 - Mac 打包脚本"
echo "========================================"
echo ""

echo "[1/3] 检查依赖..."
if [ ! -d "node_modules" ]; then
    echo "未找到 node_modules，正在安装依赖..."
    pnpm install
    if [ $? -ne 0 ]; then
        echo "依赖安装失败，请检查网络连接"
        exit 1
    fi
else
    echo "依赖已安装"
fi
echo ""

echo "[2/3] 构建前端代码..."
pnpm run build
if [ $? -ne 0 ]; then
    echo "前端构建失败"
    exit 1
fi
echo ""

echo "[3/3] 打包 Mac 应用..."
pnpm run electron:build:mac
if [ $? -ne 0 ]; then
    echo "打包失败"
    exit 1
fi
echo ""

echo "========================================"
echo "打包完成！"
echo "安装包位置: dist-electron/"
echo "========================================"

