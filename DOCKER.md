# JiwuChat Docker 一键启动说明

本方案通过 Docker Compose 一键启动完整系统：MySQL、Redis、RabbitMQ、**前后端单包应用**（前端 SSG 由 `npx serve .output/public` 提供，后端 Spring Boot），无需在本地安装除 Docker 以外的依赖。

## 两种使用方式

| 方式 | 适用 | 说明 |
|------|------|------|
| **发布包（用户直接使用）** | 已发布镜像、不想构建 | 使用 [deploy/](deploy/) 目录或发布后的 zip：解压 → `cp .env.example .env` → `./start.sh`，仅拉取单镜像。详见 [deploy/README.md](deploy/README.md)。 |
| **源码构建（单镜像）** | 开发或未发布镜像 | 在项目根目录执行 `./scripts/docker-start.sh` 或 `docker compose up -d --build`，会构建一个应用镜像（前后端合一）。 |

维护者打发布包：在根目录执行 `./scripts/pack-docker-release.sh [版本号]`，产出 `dist-docker/jiwu-chat-docker-<版本>.zip`；发布前需先构建并推送镜像，并更新 `deploy/.env.example` 中的 `JIWU_CHAT_IMAGE`。**如何构建并发布自己的完整镜像**（Docker Hub / GHCR / 自建仓库）见 [deploy/BUILD-IMAGE.md](deploy/BUILD-IMAGE.md)；发布流程概览见 [deploy/RELEASE.md](deploy/RELEASE.md)。

## 前置要求

- [Docker](https://docs.docker.com/get-docker/) 与 [Docker Compose](https://docs.docker.com/compose/install/)（或 Docker Desktop 自带 Compose）
- 磁盘空间：建议至少 5GB 可用（镜像与数据卷）

## 一键启动

在项目根目录执行：

```bash
# 方式一：使用脚本（推荐，会等待应用就绪并打印访问地址）
chmod +x scripts/docker-start.sh
./scripts/docker-start.sh

# 方式二：直接使用 docker compose
docker compose up -d --build
```

- 首次运行会构建**单包镜像**（后端 + 前端），耗时约 5–15 分钟，视机器性能而定。
- 之后仅启动容器时，执行 `docker compose up -d` 即可，约 1 分钟内可访问。

## 访问地址

| 服务 | 地址 |
|------|------|
| 前端（npx serve） | http://localhost:3000 |
| 后端 API | http://localhost:9090 |
| API 文档 | http://localhost:9090/doc.html |
| WebSocket | ws://localhost:9091/ws |
| RabbitMQ 管理 | 未暴露端口，需可进入容器或自行映射 |

默认体验账号：**ikun233** / **123456**（由数据库初始化脚本创建）。

## 服务说明

| 容器名 | 说明 | 端口映射 |
|--------|------|----------|
| jiwu-mysql | MySQL 8.0，库 `jiwu-chat-db` | 仅内网（3306） |
| jiwu-redis | Redis 6.2 | 仅内网（6379） |
| jiwu-rabbitmq | RabbitMQ 3.13 | 仅内网（5672/15672） |
| jiwu-chat | 前后端单包（npx serve 前端 + Spring Boot API + WebSocket） | 3000（前端）、9090（HTTP）、9091（WS） |

数据库与中间件不对外暴露端口，仅应用对外。

## 常用命令

```bash
# 启动（后台）
docker compose up -d

# 查看日志
docker compose logs -f
docker compose logs -f jiwu-chat

# 停止并删除容器（保留数据卷）
docker compose down

# 停止并删除容器及数据卷（清空数据库等）
docker compose down -v

# 仅重新构建并启动应用
docker compose up -d --build jiwu-chat
```

## 配置说明

- **前后端统一配置**：在项目根目录使用 `.env` 配置（可复制 `.env.example`）。前端构建时读取 `VITE_*`，后端运行时读取 `SPRING_*`、`MAIL_*`、`QINIU_*` 等。详见 [CONFIG.md](CONFIG.md)。
- **直接指定镜像地址**：若拉取 Docker Hub 较慢或失败，可在项目根目录创建 `.env`，设置 `DOCKER_REGISTRY=镜像地址`（如 `DOCKER_REGISTRY=docker.1ms.run`）。该变量会作用于：① Compose 中的 MySQL/Redis/RabbitMQ 镜像；② `docker/Dockerfile` 构建时的基础镜像（Maven、Eclipse Temurin、Node）。可参考根目录 `.env.example`。
- **数据库**：由 `backend/docker-entrypoint-initdb.d/jiwu-chat-db.sql` 在 MySQL 首次启动时自动建库建表并写入基础数据。库名为 `jiwu-chat-db`。
- **应用环境变量**：在根目录 `docker-compose.yml` 中配置，如数据库密码、Redis/RabbitMQ 连接等。生产部署请修改默认密码。

## 故障排查

- **拉取镜像失败（报错含 registry-1.docker.io 或 EOF）**：在项目根目录创建 `.env`，设置 `DOCKER_REGISTRY=docker.1ms.run`（或其它可用镜像），然后执行 `docker compose up -d --build`。
- **应用启动失败**：查看 `docker compose logs jiwu-chat`。常见原因：MySQL 未就绪（可多等 1–2 分钟）、数据库名/账号密码与 `SPRING_DATASOURCE_*` 不一致。
- **前端白屏或接口 404**：确认应用已就绪（访问 http://localhost:9090/doc.html 可打开）。
- **WebSocket 连接失败**：确认 9091 端口未被占用。

如需重置环境，可执行 `docker compose down -v` 后再次 `docker compose up -d --build`（会重新初始化数据库）。
