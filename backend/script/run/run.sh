#!/usr/bin/env bash
set -euo pipefail

# 主聚合脚本 - 提供统一的脚本执行入口
# 支持菜单选择单个脚本运行，也支持直接运行单个脚本

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 日志函数
log_info() { echo -e "${CYAN}ℹ️  [INFO]${NC} $*"; }
log_warn() { echo -e "${YELLOW}⚠️  [WARN]${NC} $*" >&2; }
log_error() { echo -e "${RED}❌ [ERROR]${NC} $*" >&2; }
log_success() { echo -e "${GREEN}✅ [SUCCESS]${NC} $*"; }

# 显示帮助信息
show_help() {
  cat <<EOF
${CYAN}JiwuChat 脚本管理工具${NC}

${GREEN}用法:${NC}
  ./script/run.sh [选项] [脚本名称]

${GREEN}选项:${NC}
  -h, --help     显示此帮助信息
  -l, --list     列出所有可用脚本
  -i, --interactive  交互式菜单模式（默认）

${GREEN}直接运行脚本:${NC}
  ./script/run.sh <脚本名称>

${GREEN}可用脚本:${NC}
  1) init_db.sh              - 数据库初始化（Docker/容器环境）
  2) init_db_local.sh        - 本地数据库初始化（交互式）
  3) dev-run.sh              - 运行开发环境应用
  4) install_brew_services.sh - 安装开发环境服务（MySQL, Redis, RabbitMQ）
  5) server_brew.sh          - 管理 brew 服务（启动/停止）

${GREEN}示例:${NC}
  ./script/run.sh                    # 交互式菜单
  ./script/run.sh init_db_local.sh    # 直接运行指定脚本
  ./script/run.sh --list              # 列出所有脚本
EOF
}

# 列出所有可用脚本
list_scripts() {
  echo -e "${CYAN}📋 可用脚本列表:${NC}\n"
  local scripts=(
    "init_db.sh:数据库初始化（Docker/容器环境）"
    "init_db_local.sh:本地数据库初始化（交互式）"
    "dev-run.sh:运行开发环境应用"
    "install_brew_services.sh:安装开发环境服务"
    "server_brew.sh:管理 brew 服务"
  )
  
  for i in "${!scripts[@]}"; do
    IFS=':' read -r name desc <<< "${scripts[$i]}"
    printf "  ${GREEN}%d)${NC} ${CYAN}%-25s${NC} - %s\n" $((i+1)) "$name" "$desc"
  done
  echo ""
}

# 显示交互式菜单
show_menu() {
  clear
  echo -e "${GREEN}        ╔════════════════════════════╗${NC}"
  echo -e "${GREEN}        ║     JiwuChat 脚本管理工具   ║${NC}"
  echo -e "${GREEN}        ╚════════════════════════════╝${NC}"
  echo ""
  echo -e "${CYAN}请选择要执行的脚本:${NC}"
  echo ""
  echo -e "  ${GREEN}1)${NC} ${CYAN}init_db.sh${NC}              - 数据库初始化（Docker/容器环境）"
  echo -e "  ${GREEN}2)${NC} ${CYAN}init_db_local.sh${NC}        - 本地数据库初始化（交互式）"
  echo -e "  ${GREEN}3)${NC} ${CYAN}dev-run.sh${NC}              - 运行开发环境应用"
  echo -e "  ${GREEN}4)${NC} ${CYAN}install_brew_services.sh${NC} - 安装开发环境服务"
  echo -e "  ${GREEN}5)${NC} ${CYAN}server_brew.sh${NC}           - 管理 brew 服务"
  echo ""
  echo -e "  ${YELLOW}0)${NC} 退出"
  echo ""
  read -p "请输入选项 [0-5]: " choice
}

# 执行脚本
run_script() {
  local script_name="$1"
  shift  # 移除第一个参数（脚本名称），保留其他参数
  local script_path="${SCRIPT_DIR}/${script_name}"
  
  if [[ ! -f "${script_path}" ]]; then
    log_error "脚本不存在: ${script_name}"
    return 1
  fi
  
  if [[ ! -x "${script_path}" ]]; then
    log_warn "脚本没有执行权限，正在添加执行权限..."
    chmod +x "${script_path}"
  fi
  
  log_info "正在执行: ${script_name}"
  echo ""
  
  # 切换到脚本目录的父目录（项目根目录）
  cd "${SCRIPT_DIR}/.."
  
  # 执行脚本，只传递剩余的参数（不包括脚本名称）
  if bash "${script_path}" "$@"; then
    echo ""
    log_success "脚本执行完成: ${script_name}"
    return 0
  else
    echo ""
    log_error "脚本执行失败: ${script_name}"
    return 1
  fi
}

# 根据选择执行对应脚本
execute_choice() {
  local choice="$1"
  shift 2>/dev/null || true
  
  case "${choice}" in
    1)
      run_script "init_db.sh" "$@"
      ;;
    2)
      run_script "init_db_local.sh" "$@"
      ;;
    3)
      run_script "dev-run.sh" "$@"
      ;;
    4)
      run_script "install_brew_services.sh" "$@"
      ;;
    5)
      run_script "server_brew.sh" "$@"
      ;;
    0)
      log_info "退出脚本管理工具"
      exit 0
      ;;
    *)
      log_error "无效的选项: ${choice}"
      return 1
      ;;
  esac
}

# 主函数
main() {
  # 解析命令行参数
  if [[ $# -eq 0 ]]; then
    # 无参数，进入交互式模式
    while true; do
      show_menu
      execute_choice "${choice}"
      echo ""
      read -p "按 Enter 键继续..." -r
    done
  else
    case "$1" in
      -h|--help)
        show_help
        exit 0
        ;;
      -l|--list)
        list_scripts
        exit 0
        ;;
      -i|--interactive)
        while true; do
          show_menu
          execute_choice "${choice}"
          echo ""
          read -p "按 Enter 键继续..." -r
        done
        ;;
      *)
        # 尝试作为脚本名称执行
        local script_name="$1"
        shift 2>/dev/null || true
        run_script "${script_name}" "$@"
        exit $?
        ;;
    esac
  fi
}

# 运行主函数
main "$@"

