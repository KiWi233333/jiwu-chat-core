# 创建并发布自己的完整镜像

本文说明如何从源码构建 **JiwuChat 应用镜像**（前后端单包），推送到你自己的镜像仓库，并打包成「用户解压即用」的 Docker 发布包。用户只需配置 `.env` 并执行 `./start.sh`，无需构建。

---

## 一、前置要求

- 已安装 [Docker](https://docs.docker.com/get-docker/) 与 Docker Compose
- 本仓库代码（`git clone` 或已拉取最新）
- 目标镜像仓库账号（如 Docker Hub、GitHub Container Registry、阿里云 ACR 等）

---

## 二、构建应用镜像

在**项目根目录**执行（会读取根目录 `.env` 中的构建参数，若无可复制 `.env.example`）：

```bash
cd <项目根目录>

# 可选：配置构建时的前端 API 地址（用户访问你服务的域名时必改）
# cp .env.example .env
# 编辑 .env：VITE_API_BASE_URL、VITE_API_WS_BASE_URL 改为你的域名，如 https://api.example.com/ 、 wss://api.example.com/ws

# 仅构建应用镜像（不启动其他服务）
docker compose build jiwu-chat
```

或直接使用 `docker build`（需在项目根目录执行）：

```bash
docker build -f docker/Dockerfile \
  --build-arg DOCKER_REGISTRY=${DOCKER_REGISTRY:-docker.io} \
  --build-arg VITE_API_BASE_URL=${VITE_API_BASE_URL:-http://localhost:9090/} \
  --build-arg VITE_API_WS_BASE_URL=${VITE_API_WS_BASE_URL:-ws://localhost:9091/ws} \
  -t jiwu-chat:latest .
```

构建完成后本地会有镜像 `jiwu-chat:latest`。

---

## 三、打标签并推送到你的镜像仓库

将本地镜像打上你的仓库地址与版本号，并推送。

### 3.1 Docker Hub

```bash
# 登录（首次）
docker login

# 打标签（将 your-username 改为你的 Docker Hub 用户名）
docker tag jiwu-chat:latest your-username/jiwu-chat:v1.0.0
# 可选：同时保留 latest
docker tag jiwu-chat:latest your-username/jiwu-chat:latest

# 推送
docker push your-username/jiwu-chat:v1.0.0
docker push your-username/jiwu-chat:latest
```

用户侧镜像地址示例：`your-username/jiwu-chat:v1.0.0`。

### 3.2 GitHub Container Registry (GHCR)

```bash
# 登录（将 YOUR_GITHUB_TOKEN 换为 Personal Access Token，需勾选 read:packages、write:packages）
echo "YOUR_GITHUB_TOKEN" | docker login ghcr.io -u YOUR_GITHUB_USERNAME --password-stdin

# 打标签（将 YOUR_GITHUB_USERNAME 改为你的 GitHub 用户名或组织名）
docker tag jiwu-chat:latest ghcr.io/YOUR_GITHUB_USERNAME/jiwu-chat:v1.0.0
docker tag jiwu-chat:latest ghcr.io/YOUR_GITHUB_USERNAME/jiwu-chat:latest

# 推送
docker push ghcr.io/YOUR_GITHUB_USERNAME/jiwu-chat:v1.0.0
docker push ghcr.io/YOUR_GITHUB_USERNAME/jiwu-chat:latest
```

用户侧镜像地址示例：`ghcr.io/YOUR_GITHUB_USERNAME/jiwu-chat:v1.0.0`。

### 3.3 阿里云 / 腾讯云 / 其他私有仓库

```bash
# 以阿里云 ACR 为例（地域与命名空间按控制台实际填写）
docker login --username=你的阿里云账号 registry.cn-hangzhou.aliyuncs.com

docker tag jiwu-chat:latest registry.cn-hangzhou.aliyuncs.com/你的命名空间/jiwu-chat:v1.0.0
docker push registry.cn-hangzhou.aliyuncs.com/你的命名空间/jiwu-chat:v1.0.0
```

用户侧镜像地址示例：`registry.cn-hangzhou.aliyuncs.com/你的命名空间/jiwu-chat:v1.0.0`。

---

## 四、更新发布包中的镜像地址并打 zip

1. 编辑 **deploy/.env.example**，将 `JIWU_CHAT_IMAGE` 改为你刚推送的地址：

   ```bash
   # 例如
   JIWU_CHAT_IMAGE=ghcr.io/YOUR_GITHUB_USERNAME/jiwu-chat:v1.0.0
   ```

2. 在项目根目录打发布包：

   ```bash
   ./scripts/pack-docker-release.sh 1.0.0
   ```

   会在 `dist-docker/` 下生成 **jiwu-chat-docker-1.0.0.zip**。

3. 将 zip 分发给用户（如 GitHub Release 附件、网盘等）。

---

## 五、用户如何使用你的镜像

给用户的说明（可写在 Release 或你的文档里）：

1. 解压 **jiwu-chat-docker-1.0.0.zip**
2. 进入解压目录：`cd jiwu-chat-docker`
3. 复制环境配置：`cp .env.example .env`
4. （可选）编辑 `.env`：修改 `MYSQL_ROOT_PASSWORD`、端口等，保证服务用自己的配置
5. 启动：`chmod +x start.sh && ./start.sh`
6. 浏览器访问：前端 http://localhost:3000 ，API 文档 http://localhost:9090/doc.html

`.env` 中的 `JIWU_CHAT_IMAGE` 已指向你的镜像，用户无需构建，直接拉取你发布的镜像即可。

---

## 六、可选：对外部署时前端地址

若用户通过 **Nginx 反代** 或 **域名** 访问（非 localhost），需要在**你构建镜像时**就把前端请求的 API 地址写进镜像：

- 在项目根目录 `.env` 中设置：
  - `VITE_API_BASE_URL=https://api.你的域名/`
  - `VITE_API_WS_BASE_URL=wss://api.你的域名/ws`
- 再执行 `docker compose build jiwu-chat` 并重新打标签、推送。

这样用户用你的镜像部署到自己的域名时，前端会正确请求该域名下的 API 与 WebSocket。若用户始终用 localhost 访问，可省略此步。

---

## 七、可选：RabbitMQ 也使用预构建镜像（用户零构建）

当前发布包内首次运行会**本地构建一次** RabbitMQ 镜像（含延迟插件）。若希望用户**完全无需构建**，你可预先构建并推送 RabbitMQ 镜像，再修改 deploy 的 compose 使用 `image:` 拉取该镜像。

1. 在项目根目录构建并推送 RabbitMQ 镜像：

   ```bash
   docker compose build rabbitmq
   docker tag jiwu-rabbitmq:latest ghcr.io/YOUR_USERNAME/jiwu-rabbitmq:latest
   docker push ghcr.io/YOUR_USERNAME/jiwu-rabbitmq:latest
   ```

2. 修改 **deploy/docker-compose.yml** 中 `rabbitmq` 服务：去掉 `build` 块，改为使用你的镜像，例如：

   ```yaml
   rabbitmq:
     image: ghcr.io/YOUR_USERNAME/jiwu-rabbitmq:latest
     # 删除 build: ... 整块
     container_name: jiwu-rabbitmq
     ...
   ```

3. 打发布包时不再需要把 `docker/Dockerfile.rabbitmq` 打进 zip（可选：从 `pack-docker-release.sh` 中去掉拷贝该文件的步骤）。

这样用户解压后 `./start.sh` 将只拉取镜像，无需任何本地构建。

---

## 快速命令汇总（以 GHCR 为例）

```bash
cd <项目根目录>
docker compose build jiwu-chat
docker tag jiwu-chat:latest ghcr.io/YOUR_USERNAME/jiwu-chat:v1.0.0
echo "YOUR_GITHUB_TOKEN" | docker login ghcr.io -u YOUR_USERNAME --password-stdin
docker push ghcr.io/YOUR_USERNAME/jiwu-chat:v1.0.0

# 更新 deploy/.env.example 中 JIWU_CHAT_IMAGE=ghcr.io/YOUR_USERNAME/jiwu-chat:v1.0.0
./scripts/pack-docker-release.sh 1.0.0
# 将 dist-docker/jiwu-chat-docker-1.0.0.zip 发给用户
```

完成以上步骤后，你就拥有了自己的完整镜像与发布包，用户即可用 `.env` 配置并启动自己的服务。
