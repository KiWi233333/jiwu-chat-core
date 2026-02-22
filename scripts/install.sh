#!/usr/bin/env bash
# 一键部署 JiwuChat：基于发布的 Docker 包，先拉取镜像再部署
# 使用：curl -fsSL https://raw.githubusercontent.com/KiWi233333/jiwu-chat-core/main/scripts/install.sh | bash
set -e
REPO="KiWi233333/jiwu-chat-core"
INSTALL_DIR="${JIWU_CHAT_INSTALL_DIR:-$HOME/jiwu-chat-core}"
IMAGE="${JIWU_CHAT_IMAGE:-ghcr.io/kiwi233333/jiwu-chat-core:latest}"

if ! command -v docker >/dev/null 2>&1; then
  echo "错误：未找到 docker，请先安装 Docker。"
  exit 1
fi
if ! docker info >/dev/null 2>&1; then
  echo "错误：Docker 未运行，请先启动 Docker。"
  exit 1
fi

echo ">>> 安装目录: $INSTALL_DIR"
mkdir -p "$INSTALL_DIR"
cd "$INSTALL_DIR"

ZIP_URL=$(curl -sL "https://api.github.com/repos/$REPO/releases/latest" | grep -oE "https://[^\"]+jiwu-chat-core[^\"]+\.zip" | head -1)
if [ -z "$ZIP_URL" ]; then
  echo "无法获取发布包，请从 https://github.com/$REPO/releases 下载 jiwu-chat-core-*.zip 解压后执行 ./start.sh"
  exit 1
fi

echo ">>> 下载 Docker 发布包..."
curl -fsSL "$ZIP_URL" -o jiwu-chat-core.zip
unzip -o -q jiwu-chat-core.zip
rm -f jiwu-chat-core.zip
cd jiwu-chat-core

cp .env.example .env
if command -v sed >/dev/null 2>&1; then
  if sed --version 2>/dev/null | grep -q GNU; then
    sed -i "s|JIWU_CHAT_IMAGE=.*|JIWU_CHAT_IMAGE=$IMAGE|" .env
  else
    sed -i '' "s|JIWU_CHAT_IMAGE=.*|JIWU_CHAT_IMAGE=$IMAGE|" .env
  fi
fi

echo ">>> 1. 拉取镜像..."
docker compose pull

echo ">>> 2. 部署（启动服务）..."
docker compose up -d

echo ""
echo ">>> 部署完成。等待约 1–2 分钟（MySQL 初始化）后访问："
echo "    前端： http://localhost:3000"
echo "    API：  http://localhost:9090  文档： http://localhost:9090/doc.html"
echo "    默认账号：superAdmin / 123456"
echo ">>> 停止：cd $INSTALL_DIR/jiwu-chat-core && docker compose down"
