#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
用法: ./scripts/init_db_local.sh [选项]

选项:
  -e, --env <dev|test|prod>  目标环境（默认为 dev）
  -h, --help                 显示此帮助信息

环境变量（覆盖默认值）:
  APP_ENV
  MYSQL_HOST, MYSQL_PORT, MYSQL_USER, MYSQL_PASSWORD, MYSQL_DATABASE

交互式模式:
  如果不提供选项，脚本将提示输入。
EOF
}

# Print with emoji icons
log_info() { echo "ℹ️  [INFO] $*"; }
log_warn() { echo "⚠️  [WARN] $*" >&2; }
log_error() { echo "❌ [ERROR] $*" >&2; }
log_success() { echo "✅ [SUCCESS] $*"; }

ENV_NAME="${APP_ENV:-}"

while [[ $# -gt 0 ]]; do
  case "$1" in
    -e|--env)
      shift
      [[ $# -gt 0 ]] || { log_error "--env 选项缺少值"; exit 1; }
      ENV_NAME="$1"
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      log_error "未知选项: $1"
      usage
      exit 1
      ;;
  esac
done

# Interactive mode: prompt for environment if not provided
if [[ -z "${ENV_NAME}" ]]; then
  echo "🗄️  ==================================="
  echo "   数据库初始化脚本"
  echo "   ==================================="
  echo ""
  echo "📋 请选择目标环境:"
  echo "   1) dev   - 💻 开发环境"
  echo "   2) test  - 🧪 测试环境"
  echo "   3) prod  - 🚀 生产环境"
  echo ""
  read -p "请输入选项 [1-3] (默认: 1): " env_choice
  
  case "${env_choice:-1}" in
    1) ENV_NAME="dev" ;;
    2) ENV_NAME="test" ;;
    3) ENV_NAME="prod" ;;
    *)
      log_error "无效的选择。请输入 1、2 或 3。"
      exit 1
      ;;
  esac
fi

case "${ENV_NAME}" in
  dev)   DEFAULT_DB="jiwu-chat-db-dev" ;;
  test)  DEFAULT_DB="jiwu-chat-db-test" ;;
  prod)  DEFAULT_DB="jiwu-chat-db-prod" ;;
  *)
    log_error "不支持的环境: ${ENV_NAME}。请使用 dev、test 或 prod。"
    exit 1
    ;;
esac

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
SQL_DIR="${REPO_ROOT}/docker-entrypoint-initdb.d"

MYSQL_HOST="${MYSQL_HOST:-}"
MYSQL_PORT="${MYSQL_PORT:-}"
MYSQL_USER="${MYSQL_USER:-}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-${MYSQL_ROOT_PASSWORD:-}}"
TARGET_DB="${MYSQL_DATABASE:-${DEFAULT_DB}}"

# Interactive prompts for MySQL connection details
if [[ -z "${MYSQL_HOST}" ]]; then
  read -p "MySQL 主机地址 (默认: 127.0.0.1): " MYSQL_HOST
  MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
fi

if [[ -z "${MYSQL_PORT}" ]]; then
  read -p "MySQL 端口 (默认: 3306): " MYSQL_PORT
  MYSQL_PORT="${MYSQL_PORT:-3306}"
fi

if [[ -z "${MYSQL_USER}" ]]; then
  read -p "MySQL 用户名 (默认: root): " MYSQL_USER
  MYSQL_USER="${MYSQL_USER:-root}"
fi

if [[ -z "${MYSQL_PASSWORD}" ]]; then
  read -s -p "MySQL 密码: " MYSQL_PASSWORD
  echo ""
fi

if [[ -z "${TARGET_DB}" || "${TARGET_DB}" == "${DEFAULT_DB}" ]]; then
  read -p "目标数据库 (默认: ${DEFAULT_DB}): " user_db
  TARGET_DB="${user_db:-${DEFAULT_DB}}"
fi

if ! command -v mysql >/dev/null 2>&1; then
  log_error "mysql 客户端未安装或不在 PATH 中"
  exit 1
fi

if [[ ! -d "${SQL_DIR}" ]]; then
  log_error "SQL 目录未找到: ${SQL_DIR}"
  exit 1
fi

[[ -n "${MYSQL_PASSWORD}" ]] && export MYSQL_PWD="${MYSQL_PASSWORD}"

log_info "使用环境: ${ENV_NAME}"
log_info "目标数据库: ${TARGET_DB}"
log_info "MySQL 主机: ${MYSQL_HOST}:${MYSQL_PORT}"
log_info "MySQL 用户: ${MYSQL_USER}"
echo ""
read -p "是否继续使用这些设置? [Y/n]: " confirm
confirm="${confirm:-Y}"

if [[ ! "${confirm}" =~ ^[Yy]$ ]]; then
  log_info "操作已由用户取消。"
  exit 0
fi

mysql_common_args=(
  --protocol=tcp
  --host="${MYSQL_HOST}"
  --port="${MYSQL_PORT}"
  --user="${MYSQL_USER}"
  --default-character-set=utf8mb4
)

log_info "确保数据库 \`${TARGET_DB}\` 存在..."
mysql "${mysql_common_args[@]}" --execute="CREATE DATABASE IF NOT EXISTS \`${TARGET_DB}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"

# Collect SQL files with proper ordering
SQL_FILES=()

# 1. First, add base schema files (jiwu_chat_db.sql or jiwu-chat-db.sql)
while IFS= read -r file; do
  SQL_FILES+=("${file}")
done < <(find "${SQL_DIR}" -maxdepth 1 -type f \
    \( -name "jiwu_chat_db.sql" -o -name "jiwu-chat-db.sql" \) | sort)

# 2. Then, add environment-specific files
while IFS= read -r file; do
  SQL_FILES+=("${file}")
done < <(find "${SQL_DIR}" -maxdepth 1 -type f -name "*-${ENV_NAME}.sql" | sort)

# 3. Finally, add changelog files (sorted by date)
while IFS= read -r file; do
  SQL_FILES+=("${file}")
done < <(find "${SQL_DIR}" -maxdepth 1 -type f -name "*.sql" \
    ! -name "create_db.sql" \
    ! -name "jiwu_chat_db.sql" \
    ! -name "jiwu-chat-db.sql" \
    ! -name "*-dev.sql" \
    ! -name "*-test.sql" \
    ! -name "*-prod.sql" | sort)

if [[ ${#SQL_FILES[@]} -eq 0 ]]; then
  log_warn "在 ${SQL_DIR} 中未找到 SQL 文件"
  exit 0
fi

echo ""
log_info "导入顺序:"
for sql_file in "${SQL_FILES[@]}"; do
  printf '   📄 %s\n' "$(basename "${sql_file}")"
done
echo ""

for sql_file in "${SQL_FILES[@]}"; do
  log_info "正在导入 $(basename "${sql_file}") 到 ${TARGET_DB}..."
  mysql "${mysql_common_args[@]}" --database="${TARGET_DB}" < "${sql_file}"
done

echo ""
log_success "数据库初始化完成! 🎉"
