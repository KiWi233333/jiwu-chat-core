#!/usr/bin/env bash
# JiwuChat 一键 Docker 启动
# 在项目根目录执行：./scripts/docker-start.sh 或 bash scripts/docker-start.sh
set -e
cd "$(dirname "$0")/.."

# 严格检查：Docker 是否可用、daemon 是否在运行
if ! command -v docker >/dev/null 2>&1; then
  echo "错误：未找到 docker 命令，请先安装 Docker 或 OrbStack。"
  exit 1
fi
if ! docker info >/dev/null 2>&1; then
  echo "错误：无法连接 Docker daemon（Docker 未运行）。"
  echo "请先启动 Docker Desktop 或 OrbStack，再执行本脚本。"
  exit 1
fi

echo ">>> 构建并启动全部服务（MySQL / Redis / RabbitMQ / 后端 API / 前端 Web）..."
docker compose up -d --build
echo ""
echo ">>> 等待后端就绪（约 30–60 秒）..."
for i in $(seq 1 30); do
  if curl -sf http://localhost:9090/doc.html >/dev/null 2>&1; then
    echo ">>> 后端已就绪"
    break
  fi
  echo "    等待中... ($i/30)"
  sleep 3
done
echo ""
echo ">>> 访问地址："
echo "    前端（Web）：  http://localhost:3000"
echo "    后端 API：     http://localhost:9090"
echo "    API 文档：     http://localhost:9090/doc.html"
echo "    WebSocket：    ws://localhost:9091/ws"
echo ""
echo ">>> 停止：docker compose down"
