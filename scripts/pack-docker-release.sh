#!/usr/bin/env bash
# 打 Docker 发布整合包：产出可发给用户直接解压使用的 zip（仅拉取镜像，无需构建）
# 在项目根目录执行：./scripts/pack-docker-release.sh
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
DEPLOY="$ROOT/deploy"
OUT="$ROOT/dist-docker"
NAME="jiwu-chat-core"
VERSION="${1:-latest}"

# 同步 Java 版本号到所有 pom.xml（跳过 latest）
if [ "$VERSION" != "latest" ]; then
  echo ">>> 同步 Java 版本号为 $VERSION ..."
  BACKEND="$ROOT/backend"
  # 父 POM 自身版本
  perl -i -0pe "s|(<groupId>com\.jiwu</groupId>\s*<artifactId>jiwu-chat-api</artifactId>\s*<version>)[^<]*(</version>)|\${1}${VERSION}\${2}|" "$BACKEND/pom.xml"
  # 所有子模块的 parent version
  for pom in "$BACKEND"/jiwu-chat-*/pom.xml; do
    perl -i -0pe "s|(<parent>\s*<groupId>com\.jiwu</groupId>\s*<artifactId>jiwu-chat-api</artifactId>\s*<version>)[^<]*(</version>)|\${1}${VERSION}\${2}|" "$pom"
  done
  echo "    已更新 $(ls "$BACKEND"/jiwu-chat-*/pom.xml | wc -l | tr -d ' ') 个子模块 + 父 POM"
fi

echo ">>> 准备发布包目录..."
rm -rf "$OUT"
mkdir -p "$OUT/$NAME"

echo ">>> 同步 deploy 内容并确保 initdb.d、docker 最新..."
cp -r "$DEPLOY"/docker-compose.yml "$DEPLOY"/.env.example "$DEPLOY"/start.sh "$DEPLOY"/README.md "$OUT/$NAME/"
mkdir -p "$OUT/$NAME/initdb.d" "$OUT/$NAME/docker"
cp "$ROOT/backend/docker-entrypoint-initdb.d/jiwu-chat-db.sql" "$OUT/$NAME/initdb.d/"
cp "$ROOT/docker/Dockerfile.rabbitmq" "$OUT/$NAME/docker/"

echo ">>> 打 zip..."
(cd "$OUT" && zip -r "$NAME-$VERSION.zip" "$NAME")
echo ">>> 已生成：$OUT/$NAME-$VERSION.zip"
ls -la "$OUT/$NAME-$VERSION.zip"
