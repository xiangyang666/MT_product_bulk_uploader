#!/bin/bash

# MinIO 公共读取策略设置脚本
# 用途：将 meituan-products 存储桶设置为公共可读

MINIO_ENDPOINT="106.55.102.48:9000"
ACCESS_KEY="minio_cf4STY"
SECRET_KEY="minio_ZGBzK7"
BUCKET_NAME="meituan-products"

echo "================================================"
echo "MinIO 公共读取策略设置"
echo "================================================"
echo "Endpoint: $MINIO_ENDPOINT"
echo "Bucket: $BUCKET_NAME"
echo ""

# 下载 MinIO Client
echo "[1/3] 下载 MinIO Client..."
wget -q https://dl.min.io/client/mc/release/linux-amd64/mc -O /tmp/mc
chmod +x /tmp/mc
MC="/tmp/mc"

# 配置 MinIO 别名
echo "[2/3] 配置 MinIO 别名..."
$MC alias set myminio http://$MINIO_ENDPOINT $ACCESS_KEY $SECRET_KEY --api S3v4

# 设置存储桶为公共读取
echo "[3/3] 设置存储桶公共读取策略..."
$MC anonymous set download myminio/$BUCKET_NAME

# 验证策略
echo ""
echo "================================================"
echo "验证策略设置："
$MC anonymous list myminio/$BUCKET_NAME

echo ""
echo "================================================"
echo "✅ 完成！存储桶 $BUCKET_NAME 现在可以公共访问了"
echo "================================================"
echo ""
echo "测试访问："
echo "http://$MINIO_ENDPOINT/$BUCKET_NAME/products/1/your-image.png"
echo ""
