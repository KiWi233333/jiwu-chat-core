# 维护者：发布 Docker 整合包说明

本目录用于产出 **用户直接使用的 Docker 发布包**：用户无需克隆源码、无需构建，解压后配置 `.env` 并执行 `./start.sh` 即可运行（前后端单包，无 nginx）。

**完整步骤（构建镜像 → 打标签 → 推送 → 打 zip）** 见 **[BUILD-IMAGE.md](BUILD-IMAGE.md)**（含 Docker Hub / GHCR / 阿里云等示例）。下面为简要流程。
## 发布前准备

### 1. 构建并推送单包镜像

在仓库根目录构建并推送到可公开拉取的仓库（如 Docker Hub、GitHub Container Registry）：

```bash
cd <仓库根目录>
docker compose build jiwu-chat
# 打标签为你的镜像地址，例如：
docker tag jiwu-chat:latest ghcr.io/KiWi233333/jiwu-chat:v1.0.0
docker push ghcr.io/KiWi233333/jiwu-chat:v1.0.0
```

（若使用 Docker Hub：`docker tag jiwu-chat:latest username/jiwu-chat:v1.0.0` 并 `docker push username/jiwu-chat:v1.0.0`。）

### 2. 更新 deploy/.env.example 中的镜像地址

将 `JIWU_CHAT_IMAGE` 改为刚推送的地址与标签，例如：

```
JIWU_CHAT_IMAGE=ghcr.io/KiWi233333/jiwu-chat:v1.0.0
```

## 打发布包

在项目根目录执行：

```bash
./scripts/pack-docker-release.sh [版本号]
# 例如：./scripts/pack-docker-release.sh 1.0.0
```

会在 `dist-docker/` 下生成 `jiwu-chat-docker-<版本号>.zip`（未传版本号则为 `latest`）。

发布包内容：

- `docker-compose.yml`（jiwu-chat 仅拉取单镜像；MySQL/Redis 拉取官方镜像；RabbitMQ 需本地构建一次，见下）
- `.env.example`（含本次发布的镜像地址）
- `start.sh`、`README.md`
- `initdb.d/jiwu-chat-db.sql`
- `docker/Dockerfile.rabbitmq`（首次 `docker compose up -d` 时会构建 RabbitMQ 镜像，之后复用）

## 发布给用户

1. 将 `dist-docker/jiwu-chat-docker-<版本>.zip` 作为 GitHub Release 附件或其它渠道分发。
2. 用户说明（可写在 Release 说明或主站）：
   - 解压 zip
   - `cp .env.example .env`（如需改端口或镜像加速再编辑 .env）
   - `chmod +x start.sh && ./start.sh`
   - 浏览器打开 http://localhost:9090

## 可选：CI 自动构建并打包

可在 GitHub Actions 中：

1. 在 tag 推送时构建 `jiwu-chat` 并推送到 GHCR。
2. 将 `deploy/` 目录与 `scripts/pack-docker-release.sh` 产出物打 zip，上传为 Release 附件。

这样每次发布新版本时，用户都能拿到「解压即用」的 Docker 整合包。
