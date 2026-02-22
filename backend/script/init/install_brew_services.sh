#!/bin/bash

#
# 一键安装开发环境脚本 (MySQL, Redis, RabbitMQ + 延迟队列插件)
# 适用于 macOS with Homebrew
#

# 为输出添加颜色，增强可读性
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# --- 1. 检查并更新 Homebrew ---
check_brew() {
  echo -e "${YELLOW}Checking for Homebrew...${NC}"
  if ! command -v brew &> /dev/null; then
    echo -e "${RED}Error: Homebrew is not installed. Please install it first from https://brew.sh/${NC}"
    exit 1
  fi
  echo -e "${GREEN}Homebrew found.${NC}"
  echo -e "${YELLOW}Updating Homebrew... This may take a moment.${NC}"
  brew update
  echo -e "${GREEN}Homebrew updated successfully.${NC}\n"
}

# --- 2. 安装核心服务 ---
install_services() {
  SERVICES=("mysql" "redis")
  RABBITMQ_FORMULA="rabbitmq"
  echo -e "${YELLOW}Starting to install services: ${SERVICES[*]} and ${RABBITMQ_FORMULA}...${NC}"
  
  # 安装通用服务
  for service in "${SERVICES[@]}"; do
    if brew list --formula | grep -q "^${service}$"; then
      echo -e "${GREEN}$service is already installed. Skipping installation.${NC}"
    else
      echo -e "Installing $service..."
      if brew install "$service"; then
        echo -e "${GREEN}$service installed successfully.${NC}"
      else
        echo -e "${RED}Failed to install $service. Please check the logs.${NC}"
        exit 1
      fi
    fi
  done

  # 安装指定版本的 RabbitMQ
  if brew list --formula | grep -q "^${RABBITMQ_FORMULA}$"; then
    echo -e "${GREEN}${RABBITMQ_FORMULA} is already installed. Skipping installation.${NC}"
  else
    echo -e "Installing ${RABBITMQ_FORMULA}..."
    if brew install "${RABBITMQ_FORMULA}"; then
      echo -e "${GREEN}${RABBITMQ_FORMULA} installed successfully.${NC}"
    else
      echo -e "${RED}Failed to install ${RABBITMQ_FORMULA}. Please check the logs.${NC}"
      echo -e "${YELLOW}Note: Homebrew might not have this specific version. You may need to install it from a specific tap or formula file.${NC}"
      exit 1
    fi
  fi

  echo -e "${GREEN}All core services are ready.${NC}\n"
}

# --- 3. 安装 RabbitMQ 延迟队列插件 ---
install_rabbitmq_plugin() {
  PLUGIN_NAME="rabbitmq_delayed_message_exchange"
  REPO="rabbitmq/rabbitmq-delayed-message-exchange"
  RABBITMQ_PREFIX=$(brew --prefix rabbitmq)
  
  # 确定插件目录
  RABBITMQ_PLUGINS_DIR="${RABBITMQ_PREFIX}/plugins"
  if [ ! -d "$RABBITMQ_PLUGINS_DIR" ]; then
    # 尝试旧版本路径
    RABBITMQ_VERSION=$(brew list --versions rabbitmq | awk '{print $2}')
    OLD_PLUGIN_DIR="${RABBITMQ_PREFIX}/lib/rabbitmq/lib/rabbitmq_server-${RABBITMQ_VERSION}/plugins"
    if [ -d "$OLD_PLUGIN_DIR" ]; then
      RABBITMQ_PLUGINS_DIR="$OLD_PLUGIN_DIR"
    else
      echo -e "${RED}Error: Could not find RabbitMQ plugins directory${NC}"
      exit 1
    fi
  fi

  echo -e "${YELLOW}Setting up RabbitMQ delayed message plugin...${NC}"
  
  # 检查插件是否已启用
  if "${RABBITMQ_PREFIX}/sbin/rabbitmq-plugins" list | grep -q "^\[E\].*${PLUGIN_NAME}"; then
    echo -e "${GREEN}Plugin ${PLUGIN_NAME} is already enabled. Skipping installation.${NC}\n"
    return 0
  fi

  # 策略1: 根据 RabbitMQ 版本匹配插件版本（推荐，更安全）
  RABBITMQ_VERSION=$(brew list --versions rabbitmq | awk '{print $2}')
  if [ -n "$RABBITMQ_VERSION" ]; then
    MAJOR_VERSION=$(echo "$RABBITMQ_VERSION" | cut -d. -f1)
    MINOR_VERSION=$(echo "$RABBITMQ_VERSION" | cut -d. -f2)
    PATCH_VERSION=$(echo "$RABBITMQ_VERSION" | cut -d. -f3)
    
    if [ -z "$PATCH_VERSION" ]; then
      PATCH_VERSION="0"
    fi

    # 尝试的版本列表（从精确匹配到降级）
    VERSIONS_TO_TRY=(
      "${MAJOR_VERSION}.${MINOR_VERSION}.${PATCH_VERSION}"
      "${MAJOR_VERSION}.${MINOR_VERSION}.0"
      "${MAJOR_VERSION}.$((MINOR_VERSION - 1)).0"
      "${MAJOR_VERSION}.$((MINOR_VERSION - 2)).0"
    )

    echo -e "${YELLOW}Attempting to download compatible plugin version for RabbitMQ ${RABBITMQ_VERSION}...${NC}"
    
    PLUGIN_VERSION=""
    TEMP_FILE=""
    
    for VERSION in "${VERSIONS_TO_TRY[@]}"; do
      PLUGIN_URL="https://github.com/${REPO}/releases/download/v${VERSION}/rabbitmq_delayed_message_exchange-${VERSION}.ez"
      TEMP_FILE="/tmp/rabbitmq_delayed_message_exchange-${VERSION}.ez"
      
      if curl -L -f -s -o "$TEMP_FILE" "$PLUGIN_URL" 2>/dev/null; then
        # 验证下载的文件是否有效
        if unzip -t "$TEMP_FILE" &> /dev/null; then
          PLUGIN_VERSION="$VERSION"
          echo -e "${GREEN}Found compatible plugin version: ${PLUGIN_VERSION}${NC}"
          break
        else
          rm -f "$TEMP_FILE"
        fi
      else
        rm -f "$TEMP_FILE"
      fi
    done

    if [ -n "$PLUGIN_VERSION" ] && [ -f "$TEMP_FILE" ]; then
      PLUGIN_FILE="${RABBITMQ_PLUGINS_DIR}/rabbitmq_delayed_message_exchange-${PLUGIN_VERSION}.ez"
      mv "$TEMP_FILE" "$PLUGIN_FILE"
      echo -e "${GREEN}Plugin downloaded successfully.${NC}"
    else
      echo -e "${YELLOW}Version matching failed, trying latest release...${NC}"
      PLUGIN_VERSION=""
    fi
  fi

  # 策略2: 如果版本匹配失败，使用 GitHub API 获取最新版本
  if [ -z "$PLUGIN_VERSION" ]; then
    echo -e "${YELLOW}Fetching latest plugin release from GitHub...${NC}"
    
    # 检查 jq 是否安装
    if ! command -v jq &> /dev/null; then
      echo -e "${YELLOW}jq is not installed. Attempting to install with Homebrew...${NC}"
      if ! brew install jq; then
        echo -e "${RED}Failed to install jq. Please install it manually to proceed.${NC}"
        exit 1
      fi
    fi
    
    PLUGIN_URL=$(curl -s "https://api.github.com/repos/${REPO}/releases/latest" | jq -r '.assets[] | select(.name | endswith(".ez")) | .browser_download_url')

    if [ -z "$PLUGIN_URL" ] || [ "$PLUGIN_URL" == "null" ]; then
      echo -e "${RED}Could not find the latest plugin download URL. Please check the repository or your network connection.${NC}"
      exit 1
    fi

    PLUGIN_FILE=$(basename "$PLUGIN_URL")
    TEMP_FILE="/tmp/${PLUGIN_FILE}"
    PLUGIN_PATH="${RABBITMQ_PLUGINS_DIR}/${PLUGIN_FILE}"

    # 如果插件已存在，则跳过
    if [ -f "$PLUGIN_PATH" ]; then
      echo -e "${GREEN}Plugin file already exists. Skipping download.${NC}"
    else
      echo "Downloading plugin from $PLUGIN_URL..."
      if curl -L -s -o "$TEMP_FILE" "$PLUGIN_URL"; then
        # 验证下载的文件是否是一个有效的 zip 归档
        if ! unzip -t "$TEMP_FILE" &> /dev/null; then
          echo -e "${RED}Downloaded file is not a valid plugin. It might be a 404 page or corrupted.${NC}"
          rm -f "$TEMP_FILE"
          exit 1
        fi
        echo -e "${GREEN}Plugin downloaded successfully.${NC}"
        mv "$TEMP_FILE" "$PLUGIN_PATH"
      else
        echo -e "${RED}Failed to download plugin. Please check your network or the URL.${NC}"
        rm -f "$TEMP_FILE"
        exit 1
      fi
    fi
  fi

  # 启用插件
  echo -e "${YELLOW}Enabling ${PLUGIN_NAME} plugin...${NC}"
  if "${RABBITMQ_PREFIX}/sbin/rabbitmq-plugins" enable "$PLUGIN_NAME"; then
    echo -e "${GREEN}Plugin enabled successfully.${NC}\n"
  else
    echo -e "${RED}Failed to enable plugin.${NC}"
    echo -e "${YELLOW}Please ensure RabbitMQ is running and check the logs${NC}"
    exit 1
  fi
}

# --- 4. 启动所有服务 ---
start_all_services() {
  SERVICES=("mysql" "redis" "rabbitmq")
  echo -e "${YELLOW}Starting all services...${NC}"
  for service in "${SERVICES[@]}"; do
    # 使用 stop 和 start 代替 restart，以更稳健地处理服务状态
    echo "Stopping $service..."
    brew services stop "$service" >/dev/null 2>&1
    echo "Starting $service..."
    if brew services start "$service"; then
      echo -e "${GREEN}$service started successfully.${NC}"
    else
      echo -e "${RED}Failed to start $service. Please run 'brew services list' and check for issues.${NC}"
      # 如果是 MySQL 启动失败，则直接退出
      if [ "$service" == "mysql" ]; then
        exit 1
      fi
    fi
  done
}

# --- 5. 配置 MySQL root 密码 ---
configure_mysql_password() {
  echo -e "${YELLOW}Configuring MySQL root password...${NC}"

  # 确保 MySQL 服务正在运行
  if ! brew services list | grep -q "mysql.*started"; then
    echo -e "${RED}MySQL service is not running. Cannot configure password. Aborting.${NC}"
    exit 1
  fi

  # 等待服务稳定
  sleep 5

  # 为 root@localhost 设置密码。新版 MySQL 可能需要先设置一个临时密码或无密码登录。
  # Homebrew 安装后，root 用户默认无密码
  echo "Setting MySQL root password to '123456'..."
  if mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '123456';"; then
    echo -e "${GREEN}MySQL root password set successfully.${NC}"
  else
    echo -e "${RED}Failed to set MySQL root password. It might be already set or another issue occurred.${NC}"
    echo -e "${YELLOW}Attempting to set password with mysqladmin...${NC}"
    if mysqladmin -u root password '123456'; then
        echo -e "${GREEN}MySQL root password set successfully with mysqladmin.${NC}"
    else
        echo -e "${RED}Failed to set password with mysqladmin as well. Please check MySQL status and logs.${NC}"
    fi
  fi
}

# --- 主函数 ---
main() {
  echo -e "${GREEN}===============================================${NC}"
  echo -e "${GREEN}  Starting Development Environment Setup       ${NC}"
  echo -e "${GREEN}===============================================${NC}\n"
  
  check_brew
  install_services
  install_rabbitmq_plugin
  start_all_services
  configure_mysql_password
  
  echo -e "\n${GREEN}===============================================${NC}"
  echo -e "${GREEN}      Setup Complete! All services are up.     ${NC}"
  echo -e "${GREEN}===============================================${NC}"
  
  # 显示服务状态
  echo -e "\n${YELLOW}Current services status:${NC}"
  brew services list
}

# 运行主程序
main
