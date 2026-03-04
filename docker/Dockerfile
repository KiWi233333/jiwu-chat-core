# 前后端单包：前端 SSG 用 npx serve .output/public，后端 Spring Boot
# 构建上下文：项目根目录，例：docker build -f docker/Dockerfile .
ARG DOCKER_REGISTRY=docker.1ms.run

# 阶段一：后端
FROM ${DOCKER_REGISTRY}/library/maven:3.9-eclipse-temurin-21-alpine AS backend
WORKDIR /build
COPY backend/ backend/
RUN cd backend && mvn clean package -DskipTests -pl jiwu-chat-starter -am -q

# 阶段二：前端 SSG（pnpm build:nuxt → .output/public）
# 构建时使用固定占位默认值；实际 API 地址在部署时由 serve-with-config.js 通过环境变量注入
FROM ${DOCKER_REGISTRY}/library/node:22-alpine AS frontend
RUN corepack enable && corepack prepare pnpm@latest --activate
WORKDIR /app
COPY frontend/ frontend/
WORKDIR /app/frontend
ENV VITE_API_BASE_URL=http://localhost:9090/
ENV VITE_API_WS_BASE_URL=ws://localhost:9091/
ENV VITE_BASE_OSS_PATH=
ENV NUXT_PUBLIC_NODE_ENV=production
ENV NUXT_PUBLIC_SPA=false
ENV SKIP_CHECK_ENV=true
ENV CI=true
RUN pnpm install --frozen-lockfile || pnpm install
RUN pnpm run build:nuxt

# 阶段三：运行（Node 跑 npx serve + JRE 跑 jar）
FROM ${DOCKER_REGISTRY}/library/node:22-alpine
RUN apk add --no-cache openjdk21-jre-headless wget
WORKDIR /app

COPY --from=backend /build/backend/target/jiwu-chat-api.jar /app/jiwu-chat-api.jar
COPY --from=frontend /app/frontend/.output/public /app/frontend
COPY docker/entrypoint.sh /entrypoint.sh
COPY docker/serve-with-config.js /app/serve-with-config.js
RUN chmod +x /entrypoint.sh

EXPOSE 9090 9091 3000

ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=9090
ENV SPRING_DATASOURCE_URL=""
ENV SPRING_DATASOURCE_USERNAME=""
ENV SPRING_DATASOURCE_PASSWORD=""
ENV SPRING_REDIS_HOST=""
ENV SPRING_REDIS_PORT=6379
ENV SPRING_REDIS_DATABASE=0
ENV SPRING_RABBITMQ_HOST=""
ENV SPRING_RABBITMQ_PORT=5672
ENV SPRING_RABBITMQ_USERNAME=""
ENV SPRING_RABBITMQ_PASSWORD=""
# 前端部署时可覆盖，无需在构建时写死（见 serve-with-config.js）
ENV VITE_API_BASE_URL=http://localhost:9090/
ENV VITE_API_WS_BASE_URL=ws://localhost:9091/
ENV VITE_BASE_OSS_PATH=""

ENTRYPOINT ["/entrypoint.sh"]
