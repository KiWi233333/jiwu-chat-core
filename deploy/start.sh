#!/usr/bin/env bash
# JiwuChat 发布包 - 一键启动（仅拉取单镜像，不构建）
set -e
cd "$(dirname "$0")"

if ! command -v docker >/dev/null 2>&1; then
  echo "错误：未找到 docker，请先安装 Docker 或 OrbStack。"
  exit 1
fi
if ! docker info >/dev/null 2>&1; then
  echo "错误：Docker 未运行，请先启动 Docker Desktop 或 OrbStack。"
  exit 1
fi

if [ ! -f .env ]; then
  echo "未找到 .env，已从 .env.example 复制，请按需修改后重新执行。"
  cp .env.example .env
  echo "已创建 .env，请编辑 JIWU_CHAT_IMAGE 后执行：./start.sh"
  exit 0
fi

echo ">>> 拉取并启动服务..."
if ! docker compose up -d; then
  echo ""
  echo "若拉取失败，可设置 .env 中 DOCKER_REGISTRY=镜像加速地址（如 docker.1ms.run）后重试。"
  exit 1
fi

echo ""
echo ">>> 等待应用就绪（约 30–60 秒）..."
for i in $(seq 1 30); do
  if curl -sf http://localhost:${API_HTTP_PORT:-9090}/doc.html >/dev/null 2>&1; then
    echo ">>> 服务已就绪"
    break
  fi
  echo "    等待中... ($i/30)"
  sleep 3
done

echo ""
echo ">>> 访问地址："
echo "    前端（npx serve）： http://localhost:${WEB_PORT:-3000}"
echo "    后端 API：         http://localhost:${API_HTTP_PORT:-9090}"
echo "    API 文档：        http://localhost:${API_HTTP_PORT:-9090}/doc.html"
echo "    WebSocket：       ws://localhost:${API_WS_PORT:-9091}/ws"
echo ""
echo ">>> 停止：docker compose down"
