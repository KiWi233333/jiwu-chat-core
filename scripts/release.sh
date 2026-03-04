#!/usr/bin/env bash
# ============================================================
# JiwuChat 统一版本发布脚本
# 同步更新前端（package.json / Cargo.toml）与后端（Maven POM）版本号，
# 提交、打 tag，推送后自动触发 GitHub Actions 构建多架构 Docker 镜像。
#
# 用法：
#   ./scripts/release.sh patch          # 1.8.3 → 1.8.4
#   ./scripts/release.sh minor          # 1.8.3 → 1.9.0
#   ./scripts/release.sh major          # 1.8.3 → 2.0.0
#   ./scripts/release.sh 2.0.0          # 指定版本号
#   ./scripts/release.sh patch --dry-run  # 预览变更，不实际修改
#   ./scripts/release.sh patch --no-push  # 只提交和打 tag，不推送
#   ./scripts/release.sh patch --pack     # 发布后自动打发布整合包 zip
# ============================================================
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# ─── 颜色输出 ───────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

info()  { echo -e "${CYAN}[INFO]${NC} $*"; }
ok()    { echo -e "${GREEN}[OK]${NC} $*"; }
warn()  { echo -e "${YELLOW}[WARN]${NC} $*"; }
error() { echo -e "${RED}[ERROR]${NC} $*" >&2; exit 1; }

# ─── 参数解析 ───────────────────────────────────────────────
BUMP=""
DRY_RUN=false
NO_PUSH=false
PACK=false

for arg in "$@"; do
  case "$arg" in
    --dry-run)  DRY_RUN=true ;;
    --no-push)  NO_PUSH=true ;;
    --pack)     PACK=true ;;
    --help|-h)
      echo "用法: $0 <patch|minor|major|x.y.z> [--dry-run] [--no-push] [--pack]"
      echo ""
      echo "选项:"
      echo "  patch|minor|major   按语义化版本递增"
      echo "  x.y.z              指定精确版本号"
      echo "  --dry-run          预览变更，不实际修改任何文件"
      echo "  --no-push          提交并打 tag，但不推送到远程"
      echo "  --pack             发布后自动运行 pack-docker-release.sh 打发布整合包"
      exit 0
      ;;
    -*)
      error "未知选项: $arg（使用 --help 查看用法）"
      ;;
    *)
      [ -z "$BUMP" ] && BUMP="$arg" || error "多余参数: $arg"
      ;;
  esac
done

[ -z "$BUMP" ] && error "请指定版本递增类型或版本号（使用 --help 查看用法）"

# ─── 版本号文件路径 ─────────────────────────────────────────
PKG_JSON="$ROOT/frontend/package.json"
CARGO_TOML="$ROOT/frontend/src-tauri/Cargo.toml"
PARENT_POM="$ROOT/backend/pom.xml"
CHILD_POMS=(
  "$ROOT/backend/jiwu-chat-common-core/pom.xml"
  "$ROOT/backend/jiwu-chat-common-data/pom.xml"
  "$ROOT/backend/jiwu-chat-module-user/pom.xml"
  "$ROOT/backend/jiwu-chat-module-chat/pom.xml"
  "$ROOT/backend/jiwu-chat-module-res/pom.xml"
  "$ROOT/backend/jiwu-chat-module-sys/pom.xml"
  "$ROOT/backend/jiwu-chat-module-admin/pom.xml"
  "$ROOT/backend/jiwu-chat-starter/pom.xml"
)

# ─── 读取当前版本 ───────────────────────────────────────────
CURRENT_VERSION=$(grep '"version"' "$PKG_JSON" | head -1 | sed 's/.*"\([0-9]*\.[0-9]*\.[0-9]*\)".*/\1/')
[ -z "$CURRENT_VERSION" ] && error "无法从 $PKG_JSON 读取当前版本号"

IFS='.' read -r CUR_MAJOR CUR_MINOR CUR_PATCH <<< "$CURRENT_VERSION"

# ─── 计算新版本号 ───────────────────────────────────────────
case "$BUMP" in
  patch) NEW_VERSION="$CUR_MAJOR.$CUR_MINOR.$((CUR_PATCH + 1))" ;;
  minor) NEW_VERSION="$CUR_MAJOR.$((CUR_MINOR + 1)).0" ;;
  major) NEW_VERSION="$((CUR_MAJOR + 1)).0.0" ;;
  *)
    # 验证自定义版本号格式
    if [[ ! "$BUMP" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
      error "无效的版本号格式: $BUMP（需要 x.y.z）"
    fi
    NEW_VERSION="$BUMP"
    ;;
esac

TAG="v$NEW_VERSION"

# ─── 前置检查 ───────────────────────────────────────────────
# 检查是否有未提交的变更
if ! $DRY_RUN; then
  if ! git diff --quiet HEAD 2>/dev/null; then
    warn "工作区有未提交的变更，建议先提交或暂存（git stash）"
    read -r -p "是否继续？[y/N] " confirm
    [[ "$confirm" =~ ^[Yy]$ ]] || exit 0
  fi

  # 检查 tag 是否已存在
  if git tag -l "$TAG" | grep -q "$TAG"; then
    error "Tag $TAG 已存在，请使用其他版本号"
  fi
fi

# ─── 打印计划 ───────────────────────────────────────────────
echo ""
echo -e "${BOLD}═══════════════════════════════════════════════${NC}"
echo -e "${BOLD}  JiwuChat 版本发布${NC}"
echo -e "${BOLD}═══════════════════════════════════════════════${NC}"
echo ""
echo -e "  当前版本:  ${RED}$CURRENT_VERSION${NC}"
echo -e "  新版本:    ${GREEN}$NEW_VERSION${NC}"
echo -e "  Git Tag:   ${CYAN}$TAG${NC}"
echo ""
echo -e "${BOLD}  将更新以下文件:${NC}"
echo -e "  ${CYAN}前端:${NC}"
echo "    - frontend/package.json"
echo "    - frontend/src-tauri/Cargo.toml"
echo -e "  ${CYAN}后端:${NC}"
echo "    - backend/pom.xml (parent)"
for pom in "${CHILD_POMS[@]}"; do
  echo "    - ${pom#$ROOT/}"
done
echo ""

if $DRY_RUN; then
  echo -e "${YELLOW}[DRY-RUN] 预览模式，不会修改任何文件${NC}"
  echo ""
  exit 0
fi

read -r -p "确认发布？[y/N] " confirm
[[ "$confirm" =~ ^[Yy]$ ]] || { echo "已取消"; exit 0; }
echo ""

# ─── 更新 frontend/package.json ─────────────────────────────
info "更新 frontend/package.json ..."
# 使用 node 精确修改 JSON，避免 sed 破坏格式
node -e "
const fs = require('fs');
const path = '$PKG_JSON';
const pkg = JSON.parse(fs.readFileSync(path, 'utf8'));
pkg.version = '$NEW_VERSION';
fs.writeFileSync(path, JSON.stringify(pkg, null, 2) + '\n');
"
ok "frontend/package.json → $NEW_VERSION"

# ─── 更新 frontend/src-tauri/Cargo.toml ─────────────────────
info "更新 frontend/src-tauri/Cargo.toml ..."
sed -i '' "s/^version = \"$CURRENT_VERSION\"/version = \"$NEW_VERSION\"/" "$CARGO_TOML"
ok "Cargo.toml → $NEW_VERSION"

# ─── 更新 backend/pom.xml（parent） ─────────────────────────
CURRENT_POM_VERSION=$(grep -m1 '<version>' "$PARENT_POM" | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | tr -d ' ')
info "更新 backend/pom.xml (parent: $CURRENT_POM_VERSION → $NEW_VERSION) ..."

# 替换 parent pom 自身版本（第一个 <version>）
sed -i '' "0,/<version>$CURRENT_POM_VERSION<\/version>/s/<version>$CURRENT_POM_VERSION<\/version>/<version>$NEW_VERSION<\/version>/" "$PARENT_POM"
ok "backend/pom.xml → $NEW_VERSION"

# ─── 更新子模块 pom.xml 中的 parent version ──────────────────
for pom in "${CHILD_POMS[@]}"; do
  module_name="${pom#$ROOT/}"
  info "更新 $module_name ..."
  # 精准替换 <parent> 块内的 <version>
  sed -i '' "/<parent>/,/<\/parent>/s/<version>$CURRENT_POM_VERSION<\/version>/<version>$NEW_VERSION<\/version>/" "$pom"
  ok "$module_name → $NEW_VERSION"
done

# ─── Git 提交与打 Tag ────────────────────────────────────────
echo ""
info "Git 提交变更 ..."

git add \
  "$PKG_JSON" \
  "$CARGO_TOML" \
  "$PARENT_POM" \
  "${CHILD_POMS[@]}"

git commit -m "$(cat <<EOF
chore(release): 发布 v$NEW_VERSION

- frontend/package.json: $CURRENT_VERSION → $NEW_VERSION
- frontend/src-tauri/Cargo.toml: $CURRENT_VERSION → $NEW_VERSION
- backend/pom.xml: $CURRENT_POM_VERSION → $NEW_VERSION (parent + 8 modules)
EOF
)"

ok "已提交: chore(release): 发布 v$NEW_VERSION"

info "创建 Tag: $TAG ..."
git tag -a "$TAG" -m "Release $NEW_VERSION"
ok "已创建 Tag: $TAG"

# ─── 推送 ───────────────────────────────────────────────────
if $NO_PUSH; then
  warn "跳过推送（--no-push）。手动推送："
  echo "  git push && git push origin $TAG"
else
  info "推送到远程 ..."
  git push
  git push origin "$TAG"
  ok "已推送提交和 Tag"
  echo ""
  echo -e "${GREEN}GitHub Actions 将自动构建 Docker 镜像:${NC}"
  echo -e "  ghcr.io/kiwi233333/jiwu-chat-core:$NEW_VERSION"
  echo -e "  ghcr.io/kiwi233333/jiwu-chat-core:latest"
fi

# ─── 可选：打发布整合包 ─────────────────────────────────────
if $PACK; then
  echo ""
  info "打发布整合包 ..."
  bash "$ROOT/scripts/pack-docker-release.sh" "$NEW_VERSION"
fi

# ─── 完成 ───────────────────────────────────────────────────
echo ""
echo -e "${BOLD}═══════════════════════════════════════════════${NC}"
echo -e "${GREEN}  发布 v$NEW_VERSION 完成！${NC}"
echo -e "${BOLD}═══════════════════════════════════════════════${NC}"
echo ""
echo "  后续操作："
echo "    1. 等待 GitHub Actions 构建完成"
echo "    2. 到 GitHub Releases 创建正式 Release（可选）"
if ! $PACK; then
  echo "    3. 打发布整合包: ./scripts/pack-docker-release.sh $NEW_VERSION"
fi
echo ""
