#!/usr/bin/env bash
# 一键创建部署目录并拉取 docker-compose、.env、initdb、RabbitMQ Dockerfile
# 用法：curl -fsSL https://raw.githubusercontent.com/KiWi233333/jiwu-chat-core/main/deploy/install.sh | bash -s [镜像标签，默认 latest]
set -e
BASE_URL="${JIWU_INSTALL_BASE_URL:-https://raw.githubusercontent.com/KiWi233333/jiwu-chat-core/main}"
TAG="${1:-latest}"
DIR="jiwu-chat-core"

mkdir -p "$DIR/initdb.d" "$DIR/docker"
curl -fsSL -o "$DIR/docker-compose.yml" "$BASE_URL/deploy/docker-compose.yml"
curl -fsSL -o "$DIR/.env" "$BASE_URL/deploy/.env.example"
curl -fsSL -o "$DIR/initdb.d/jiwu-chat-db.sql" "$BASE_URL/backend/docker-entrypoint-initdb.d/jiwu-chat-db.sql"
curl -fsSL -o "$DIR/docker/Dockerfile.rabbitmq" "$BASE_URL/docker/Dockerfile.rabbitmq"
(echo "JIWU_CHAT_IMAGE=ghcr.io/kiwi233333/jiwu-chat-core:$TAG"; grep -v '^JIWU_CHAT_IMAGE=' "$DIR/.env") > "$DIR/.env.tmp" && mv "$DIR/.env.tmp" "$DIR/.env"
echo ">>> 配置已写入 $DIR/，执行：cd $DIR && docker compose up -d"
