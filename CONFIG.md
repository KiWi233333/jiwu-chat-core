# Docker 下前后端配置说明

起 Docker 服务时，用户通过**项目根目录的 `.env`** 统一配置前后端，无需改仓库内的 `application*.yml` 或 `frontend/.env.production`。

## 使用方式

1. 在项目根目录复制示例并按需修改：
   ```bash
   cp .env.example .env
   # 编辑 .env，填写 API 地址、七牛/短信/邮件等
   ```
2. 构建并启动（会读取 `.env`）：
   ```bash
   docker compose up -d --build
   ```

- **前端**：`.env` 中的 `VITE_*` 在 **部署时（运行时）** 通过 environment 传入容器，由镜像内 Node 服务在响应时注入，无需在构建时写死；对应前端 [useBaseUrl](frontend/app/composables/utils/useBaseUrl.ts) 读取的 `window.__APP_CONFIG__`。
- **后端**：`.env` 中的 `SPRING_*`、`MAIL_*`、`QINIU_*` 等在 **运行时** 通过 environment 传入容器，对应 [application.yml](backend/jiwu-chat-starter/src/main/resources/application.yml) 与 [application-prod.yml](backend/jiwu-chat-starter/src/main/resources/application-prod.yml)。

## 配置项与对应关系

### 前端（部署时生效，无需重新构建）

| .env 变量 | 说明 | 默认 |
|-----------|------|------|
| `VITE_API_BASE_URL` | 后端 API 根地址（浏览器访问） | `http://localhost:9090/` |
| `VITE_API_WS_BASE_URL` | WebSocket 地址 | `ws://localhost:9091/` |
| `VITE_BASE_OSS_PATH` | 七牛 OSS 等静态资源域名 | - |

修改前端相关变量后**重启容器**即可生效：`docker compose up -d`（与后端一致，无需重新 build）。

### 后端（运行时，对应 application.yml / application-prod.yml）

| .env 变量 | 说明 | 对应配置 |
|-----------|------|----------|
| `SPRING_DATASOURCE_*` | 数据源（一般由 compose 自动填） | application-prod.yml `spring.datasource` |
| `SPRING_REDIS_*` / `SPRING_RABBITMQ_*` | Redis / RabbitMQ（compose 默认与中间件一致） | application-prod.yml |
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码（compose 默认：password） | 与官方镜像常用默认一致 |
| `RABBITMQ_DEFAULT_USER` / `RABBITMQ_DEFAULT_PASS` | RabbitMQ 默认用户（compose 默认：guest/guest） | 与官方镜像一致 |
| `MAIL_HOST/PORT/USERNAME/PASSWORD` | SMTP 邮件 | application.yml `spring.mail` |
| `QINIU_ACCESS_KEY` 等 | 七牛云 OSS | application.yml `qi-niu-cloud` |
| `UNISMS_*` / `UNI_SMS_WEBHOOK_*` | UniSMS 短信 | application.yml `uni-sms` |
| `TENCENT_SECRET_ID/SECRET_KEY` | 腾讯翻译 | application.yml `tencent` |
| `RES_TRANSLATION_*` | AI 翻译（SiliconFlow 等） | application.yml `res.translation` |
| `MCP_ALLOWED_KEY` | MCP 鉴权 | application.yml `mcp.server` |

修改后端相关变量后**重启容器**即可：`docker compose up -d`（无需重新 build）。

### Docker 与端口

| .env 变量 | 说明 |
|-----------|------|
| `DOCKER_REGISTRY` | 镜像拉取地址（如 `docker.1ms.run`） |

## 常见场景

- **本地/内网**：保持默认即可，前端 `http://localhost:9090/`、`ws://localhost:9091/`。
- **对外部署（Nginx 反代）**：在 `.env` 中设 `VITE_API_BASE_URL=https://api.你的域名/`、`VITE_API_WS_BASE_URL=wss://api.你的域名/`，然后执行 `docker compose up -d` 重启容器即可。
- **启用七牛/短信/邮件**：在 `.env` 中填写对应变量，重启：`docker compose up -d`。

## 发布包（deploy/）用户

使用 [deploy/](deploy/) 或发布 zip 时，配置方式相同：在解压目录下创建 `.env`（可参考 deploy 内 `.env.example`），再执行 `./start.sh` 或 `docker compose up -d`。前后端环境变量均由 compose 在**运行时**传入容器；同一镜像可在不同环境通过 `.env` 配置 API 地址等，无需重新构建。
