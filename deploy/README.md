# JiwuChat Docker 发布包 - 用户直接使用

本目录为 **仅拉取单镜像、不构建** 的部署包。用户无需源码与构建环境，只需 Docker，按下面步骤即可运行完整系统（单包内前端由 npx serve 提供，后端 Spring Boot）。

## 前置要求

- 已安装 [Docker](https://docs.docker.com/get-docker/) 与 Docker Compose（或 [OrbStack](https://orbstack.dev/)）
- 本目录内需包含：`docker-compose.yml`、`.env.example`、`start.sh`、`initdb.d/jiwu-chat-db.sql`

## 一键启动

```bash
# 1. 进入本目录（若从发布包解压，先 cd 到解压目录）
cd deploy

# 2. 复制环境配置并按需修改（必填：JIWU_CHAT_IMAGE）
cp .env.example .env

# 3. 启动
chmod +x start.sh
./start.sh
```

首次运行若未创建 `.env`，`start.sh` 会提示并自动从 `.env.example` 复制，编辑 `.env` 后再次执行 `./start.sh` 即可。

## .env 说明

| 变量 | 必填 | 说明 |
|------|------|------|
| `JIWU_CHAT_IMAGE` | 是 | 单包应用镜像地址，如 `ghcr.io/KiWi233333/jiwu-chat:latest` |
| `DOCKER_REGISTRY` | 否 | 镜像加速地址（如 `docker.1ms.run`），用于 MySQL/Redis/RabbitMQ |
| `WEB_PORT` | 否 | 前端端口（npx serve），默认 3000 |
| `API_HTTP_PORT` | 否 | 后端 HTTP 端口，默认 9090 |
| `API_WS_PORT` | 否 | WebSocket 端口，默认 9091 |
| `MYSQL_ROOT_PASSWORD` | 否 | MySQL root 密码，默认 password（**生产环境请务必修改**） |
| `RABBITMQ_DEFAULT_USER` / `RABBITMQ_DEFAULT_PASS` | 否 | RabbitMQ 账号，默认 guest / guest |

**所有以上配置均可通过 `.env` 自定义**：启动时 Docker Compose 会读取当前目录的 `.env`，将变量注入容器，因此数据库密码、端口、RabbitMQ 账号及可选的后端密钥（邮件、七牛、短信等）都使用您自己的值，保证服务按您的环境运行。

官方发布会在 `.env.example` 中写好可用的 `JIWU_CHAT_IMAGE`，用户复制为 `.env` 即可。**维护者**若需构建并发布自己的镜像（方便用户直接拉取），请参阅 [BUILD-IMAGE.md](BUILD-IMAGE.md)。

## 访问地址（默认）

- 前端：http://localhost:3000  
- 后端 API：http://localhost:9090  
- API 文档：http://localhost:9090/doc.html  
- WebSocket：ws://localhost:9091/ws  

默认体验账号：**ikun233** / **123456**（以数据库初始化脚本为准）。

## 常用命令

```bash
docker compose up -d      # 启动
docker compose down       # 停止
docker compose logs -f    # 查看日志
docker compose down -v    # 停止并删除数据卷（清空数据库等）
```

## 故障排查

- **拉取镜像失败**：在 `.env` 中设置 `DOCKER_REGISTRY=docker.1ms.run`（或其它可用镜像加速）后重试。
- **应用镜像不存在**：确认 `.env` 中 `JIWU_CHAT_IMAGE` 为已发布的地址；若官方未发布，请从源码仓库根目录使用带构建的部署方式（见主仓库 README / DOCKER.md）。
