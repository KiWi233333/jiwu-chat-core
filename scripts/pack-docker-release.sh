#!/usr/bin/env bash
# 打 Docker 发布整合包：产出可发给用户直接解压使用的 zip（仅拉取镜像，无需构建）
# 在项目根目录执行：./scripts/pack-docker-release.sh
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
DEPLOY="$ROOT/deploy"
OUT="$ROOT/dist-docker"
NAME="jiwu-chat-docker"
VERSION="${1:-latest}"

echo ">>> 准备发布包目录..."
rm -rf "$OUT"
mkdir -p "$OUT/$NAME"

echo ">>> 同步 deploy 内容并确保 initdb.d、docker 最新..."
cp -r "$DEPLOY"/docker-compose.yml "$DEPLOY"/.env.example "$DEPLOY"/start.sh "$DEPLOY"/README.md "$OUT/$NAME/"
mkdir -p "$OUT/$NAME/initdb.d" "$OUT/$NAME/docker"
cp "$ROOT/backend/docker-entrypoint-initdb.d/jiwu-chat-db.sql" "$OUT/$NAME/initdb.d/"
cp "$ROOT/docker/Dockerfile.rabbitmq" "$OUT/$NAME/docker/"

echo ">>> 打 zip..."
(cd "$OUT" && zip -r "$NAME-$VERSION.zip" "$NAME")
echo ">>> 已生成：$OUT/$NAME-$VERSION.zip"
ls -la "$OUT/$NAME-$VERSION.zip"
