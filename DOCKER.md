# JiwuChat Docker 一键启动说明

本方案通过 Docker Compose 一键启动完整系统：MySQL、Redis、RabbitMQ、后端 API、前端 Web，无需在本地安装除 Docker 以外的依赖。

## 前置要求

- [Docker](https://docs.docker.com/get-docker/) 与 [Docker Compose](https://docs.docker.com/compose/install/)（或 Docker Desktop 自带 Compose）
- 磁盘空间：建议至少 5GB 可用（镜像与数据卷）

## 一键启动

在项目根目录执行：

```bash
# 方式一：使用脚本（推荐，会等待后端就绪并打印访问地址）
chmod +x scripts/docker-start.sh
./scripts/docker-start.sh

# 方式二：直接使用 docker compose
docker compose up -d --build
```

- 首次运行会构建后端（Maven）与前端（Nuxt）镜像，耗时约 5–15 分钟，视机器性能而定。
- 之后仅启动容器时，执行 `docker compose up -d` 即可，约 1 分钟内可访问。

## 访问地址

| 服务         | 地址                             |
| ------------ | -------------------------------- |
| 前端（Web）  | http://localhost:3000            |
| 后端 API     | http://localhost:9090            |
| API 文档     | http://localhost:9090/doc.html   |
| WebSocket    | ws://localhost:9091/ws           |
| RabbitMQ 管理| 未暴露端口，需可进入容器或自行映射 |

默认体验账号：**ikun233** / **123456**（由数据库初始化脚本创建）。

## 服务说明

| 容器名        | 说明                     | 端口映射              |
| ------------- | ------------------------ | --------------------- |
| jiwu-mysql    | MySQL 8.0，库 `jiwu-chat-db` | 仅内网（3306）        |
| jiwu-redis    | Redis 6.2                | 仅内网（6379）        |
| jiwu-rabbitmq | RabbitMQ 3.13            | 仅内网（5672/15672）  |
| jiwu-chat-api  | 后端 Spring Boot API + WebSocket | 9090（HTTP）、9091（WS） |
| jiwu-chat-web | 前端 Nuxt 静态资源（Nginx） | 3000 → 80             |

数据库与中间件不对外暴露端口，仅后端与前端对外，减少本机端口占用与安全面。

## 常用命令

```bash
# 启动（后台）
docker compose up -d

# 查看日志（全部或指定服务）
docker compose logs -f
docker compose logs -f jiwu-chat-api

# 停止并删除容器（保留数据卷）
docker compose down

# 停止并删除容器及数据卷（清空数据库等）
docker compose down -v

# 仅重新构建并启动后端
docker compose up -d --build jiwu-chat-api

# 仅重新构建并启动前端
docker compose up -d --build jiwu-chat-web
```

## 配置说明

- **数据库**：由 `backend/docker-entrypoint-initdb.d/jiwu-chat-db.sql` 在 MySQL 首次启动时自动建库建表并写入基础数据（角色、菜单、默认管理员等）。库名为 `jiwu-chat-db`。
- **后端环境变量**：在根目录 `docker-compose.yml` 中配置，如数据库密码、Redis/RabbitMQ 连接等。生产部署请修改默认密码（如 `jiwu_root_pwd`、`jiwu_pwd`）。
- **前端 API 地址**：构建时通过 build args 注入，默认为 `http://localhost:9090/` 与 `ws://localhost:9091/ws`，以便浏览器访问同机后端。若通过 Nginx 反向代理统一域名，可修改 `docker-compose.yml` 中 `jiwu-chat-web` 的 `args` 后重新构建前端镜像。

## 故障排查

- **后端启动失败**：查看 `docker compose logs jiwu-chat-api`。常见原因：MySQL 未就绪（可多等 1–2 分钟）、数据库名/账号密码与 `SPRING_DATASOURCE_*` 不一致。
- **前端白屏或接口 404**：确认后端已就绪（访问 http://localhost:9090/doc.html 可打开）；浏览器控制台查看请求是否指向 `http://localhost:9090`。
- **WebSocket 连接失败**：确认 9091 端口未被占用，且前端使用的 `VITE_API_WS_BASE_URL` 为 `ws://localhost:9091/ws`（与后端 Netty 端口一致）。

如需重置环境，可执行 `docker compose down -v` 后再次 `docker compose up -d --build`（会重新初始化数据库）。
