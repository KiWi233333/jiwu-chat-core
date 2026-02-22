#!/bin/sh
set -e
# 前端：SSG 打包后由 npx serve 提供；后端：Spring Boot
# 先启动后端（后台），再启动前端静态服务（前台）
java -jar /app/jiwu-chat-api.jar &
# 等待后端就绪后再起前端（可选，便于健康检查通过）
for i in 1 2 3 4 5 6 7 8 9 10; do
  if wget -q -O- http://127.0.0.1:9090/actuator/health >/dev/null 2>&1; then
    break
  fi
  sleep 3
done
exec npx --yes serve /app/frontend -p 3000 -l 3000 --no-clipboard
