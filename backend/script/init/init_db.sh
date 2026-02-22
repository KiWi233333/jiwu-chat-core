#!/usr/bin/env bash
set -euo pipefail

SQL_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

ENV_NAME="${APP_ENV:-${ENVIRONMENT:-prod}}"

case "${ENV_NAME}" in
  dev)
    DEFAULT_DB="jiwu-chat-db-dev"
    ;;
  test)
    DEFAULT_DB="jiwu-chat-db-test"
    ;;
  prod|production)
    DEFAULT_DB="jiwu-chat-db-prod"
    ENV_NAME="prod"
    ;;
  *)
    echo "[WARN] Unknown APP_ENV=${ENV_NAME}, defaulting to prod" >&2
    ENV_NAME="prod"
    DEFAULT_DB="jiwu-chat-db-prod"
    ;;
esac

MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-${MYSQL_ROOT_PASSWORD:-}}"
TARGET_DB="${MYSQL_DATABASE:-${DEFAULT_DB}}"

if ! command -v mysql >/dev/null 2>&1; then
  echo "[ERROR] mysql client not found" >&2
  exit 1
fi

if [[ -n "${MYSQL_PASSWORD}" ]]; then
  export MYSQL_PWD="${MYSQL_PASSWORD}"
fi

mysql_common_args=(
  --protocol=tcp
  --host="${MYSQL_HOST}"
  --port="${MYSQL_PORT}"
  --user="${MYSQL_USER}"
  --default-character-set=utf8mb4
)

echo "[INFO] Using environment: ${ENV_NAME}"
echo "[INFO] Creating database \`${TARGET_DB}\` if it does not exist..."
mysql "${mysql_common_args[@]}" --execute="CREATE DATABASE IF NOT EXISTS \\`${TARGET_DB}\\` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"

SQL_FILES=()
while IFS= read -r file; do
  SQL_FILES+=("${file}")
done < <(find "${SQL_DIR}" -maxdepth 1 -type f -name "*-${ENV_NAME}.sql" | sort)

while IFS= read -r file; do
  SQL_FILES+=("${file}")
done < <(find "${SQL_DIR}" -maxdepth 1 -type f -name "*.sql" \
    ! -name "create_db.sql" \
    ! -name "*-dev.sql" \
    ! -name "*-test.sql" \
    ! -name "*-prod.sql" | sort)

if [[ ${#SQL_FILES[@]} -eq 0 ]]; then
  echo "[WARN] No SQL files found in ${SQL_DIR}" >&2
  exit 0
fi

echo "[INFO] Import order:" >&2
for sql_file in "${SQL_FILES[@]}"; do
  printf '  - %s\n' "$(basename "${sql_file}")" >&2
done

for sql_file in "${SQL_FILES[@]}"; do
  echo "[INFO] Importing $(basename "${sql_file}") into ${TARGET_DB}..."
  mysql "${mysql_common_args[@]}" --database="${TARGET_DB}" < "${sql_file}"
done

echo "[INFO] MySQL initialization finished."
