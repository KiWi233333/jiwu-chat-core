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

echo ">>> 构建并启动全部服务（MySQL / Redis / RabbitMQ / 应用单包）..."
if ! docker compose up -d --build; then
  echo ""
  echo "错误：拉取或启动镜像失败。若报错含 registry-1.docker.io 或 EOF，多为网络无法访问 Docker Hub。"
  echo "建议：在 OrbStack/Docker 中配置镜像加速（如 https://docker.1ms.run 等），或使用代理后重试。"
  exit 1
fi
echo ""
echo ">>> 等待应用就绪（约 40–90 秒）..."
for i in $(seq 1 35); do
  if curl -sf http://localhost:9090/doc.html >/dev/null 2>&1; then
    echo ">>> 应用已就绪"
    break
  fi
  echo "    等待中... ($i/35)"
  sleep 3
done
echo ""
echo ">>> 访问地址："
echo "    前端（npx serve）： http://localhost:3000"
echo "    后端 API：         http://localhost:9090"
echo "    API 文档：         http://localhost:9090/doc.html"
echo "    WebSocket：        ws://localhost:9091/ws"
echo ""
echo ">>> 停止：docker compose down"
