#!/bin/bash

#
# RabbitMQ 延迟队列插件安装脚本
# 根据已安装的 RabbitMQ 版本自动匹配并安装对应的插件版本
# 适用于 macOS with Homebrew
#

set -euo pipefail

# 颜色输出
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 插件名称
PLUGIN_NAME="rabbitmq_delayed_message_exchange"
REPO="rabbitmq/rabbitmq-delayed-message-exchange"

# 检查 Homebrew 是否安装
if ! command -v brew &> /dev/null; then
    echo -e "${RED}Error: Homebrew is not installed. Please install it first from https://brew.sh/${NC}"
    exit 1
fi

# 检查 RabbitMQ 是否已安装
if ! brew list --formula | grep -q "^rabbitmq$"; then
    echo -e "${RED}Error: RabbitMQ is not installed via Homebrew${NC}"
    echo -e "${YELLOW}Please install RabbitMQ first: brew install rabbitmq${NC}"
    exit 1
fi

# 获取 RabbitMQ 版本和路径
RABBITMQ_VERSION=$(brew list --versions rabbitmq | awk '{print $2}')
RABBITMQ_PREFIX=$(brew --prefix rabbitmq)

if [ -z "$RABBITMQ_VERSION" ]; then
    echo -e "${RED}Error: Could not determine RabbitMQ version${NC}"
    exit 1
fi

echo -e "${BLUE}===============================================${NC}"
echo -e "${BLUE}  RabbitMQ Delayed Message Exchange Plugin   ${NC}"
echo -e "${BLUE}===============================================${NC}\n"
echo -e "${YELLOW}Detected RabbitMQ version: ${RABBITMQ_VERSION}${NC}"
echo -e "${YELLOW}RabbitMQ prefix: ${RABBITMQ_PREFIX}${NC}\n"

# 确定插件目录
PLUGIN_DIR="${RABBITMQ_PREFIX}/plugins"
if [ ! -d "$PLUGIN_DIR" ]; then
    # 尝试旧版本路径
    OLD_PLUGIN_DIR="${RABBITMQ_PREFIX}/lib/rabbitmq/lib/rabbitmq_server-${RABBITMQ_VERSION}/plugins"
    if [ -d "$OLD_PLUGIN_DIR" ]; then
        PLUGIN_DIR="$OLD_PLUGIN_DIR"
    else
        echo -e "${RED}Error: Could not find RabbitMQ plugins directory${NC}"
        echo -e "${YELLOW}Tried:${NC}"
        echo -e "  - ${RABBITMQ_PREFIX}/plugins"
        echo -e "  - ${OLD_PLUGIN_DIR}"
        exit 1
    fi
fi

echo -e "${GREEN}Plugin directory: ${PLUGIN_DIR}${NC}\n"

# 检查插件是否已安装
if "${RABBITMQ_PREFIX}/sbin/rabbitmq-plugins" list | grep -q "^\[E\].*${PLUGIN_NAME}"; then
    echo -e "${GREEN}Plugin ${PLUGIN_NAME} is already enabled.${NC}"
    echo -e "${YELLOW}Current plugin status:${NC}"
    "${RABBITMQ_PREFIX}/sbin/rabbitmq-plugins" list | grep delayed || true
    exit 0
fi

# 解析版本号
MAJOR_VERSION=$(echo "$RABBITMQ_VERSION" | cut -d. -f1)
MINOR_VERSION=$(echo "$RABBITMQ_VERSION" | cut -d. -f2)
PATCH_VERSION=$(echo "$RABBITMQ_VERSION" | cut -d. -f3)

# 如果没有补丁版本，默认为 0
if [ -z "$PATCH_VERSION" ]; then
    PATCH_VERSION="0"
fi

# 尝试下载插件的版本列表（从精确匹配到降级）
VERSIONS_TO_TRY=(
    "${MAJOR_VERSION}.${MINOR_VERSION}.${PATCH_VERSION}"
    "${MAJOR_VERSION}.${MINOR_VERSION}.0"
    "${MAJOR_VERSION}.$((MINOR_VERSION - 1)).0"
    "${MAJOR_VERSION}.$((MINOR_VERSION - 2)).0"
)

PLUGIN_VERSION=""
TEMP_FILE=""

echo -e "${YELLOW}Attempting to download compatible plugin version...${NC}"

for VERSION in "${VERSIONS_TO_TRY[@]}"; do
    PLUGIN_URL="https://github.com/${REPO}/releases/download/v${VERSION}/rabbitmq_delayed_message_exchange-${VERSION}.ez"
    TEMP_FILE="/tmp/rabbitmq_delayed_message_exchange-${VERSION}.ez"
    
    echo -e "${BLUE}Trying version ${VERSION}...${NC}"
    
    # 下载插件
    if curl -L -f -s -o "$TEMP_FILE" "$PLUGIN_URL" 2>/dev/null; then
        # 验证下载的文件是否有效（.ez 文件本质是 zip）
        if unzip -t "$TEMP_FILE" &> /dev/null; then
            PLUGIN_VERSION="$VERSION"
            echo -e "${GREEN}Successfully downloaded plugin version: ${PLUGIN_VERSION}${NC}\n"
            break
        else
            echo -e "${YELLOW}Downloaded file is not valid, trying next version...${NC}"
            rm -f "$TEMP_FILE"
        fi
    else
        echo -e "${YELLOW}Version ${VERSION} not found, trying next version...${NC}"
        rm -f "$TEMP_FILE"
    fi
done

if [ -z "$PLUGIN_VERSION" ] || [ ! -f "$TEMP_FILE" ]; then
    echo -e "${RED}Error: Failed to download compatible plugin version${NC}"
    echo -e "${YELLOW}Tried versions: ${VERSIONS_TO_TRY[*]}${NC}"
    echo -e "${YELLOW}Please check:${NC}"
    echo -e "  1. Your network connection"
    echo -e "  2. GitHub releases: https://github.com/${REPO}/releases"
    echo -e "  3. RabbitMQ version compatibility"
    exit 1
fi

# 移动插件到插件目录
PLUGIN_FILE="${PLUGIN_DIR}/rabbitmq_delayed_message_exchange-${PLUGIN_VERSION}.ez"

echo -e "${YELLOW}Installing plugin to: ${PLUGIN_DIR}${NC}"
if mv "$TEMP_FILE" "$PLUGIN_FILE"; then
    echo -e "${GREEN}Plugin file installed successfully${NC}\n"
else
    echo -e "${RED}Error: Failed to move plugin file${NC}"
    echo -e "${YELLOW}You may need to run with sudo or check directory permissions${NC}"
    rm -f "$TEMP_FILE"
    exit 1
fi

# 启用插件
echo -e "${YELLOW}Enabling ${PLUGIN_NAME} plugin...${NC}"
if "${RABBITMQ_PREFIX}/sbin/rabbitmq-plugins" enable "$PLUGIN_NAME"; then
    echo -e "${GREEN}Plugin enabled successfully${NC}\n"
else
    echo -e "${RED}Error: Failed to enable plugin${NC}"
    echo -e "${YELLOW}Please ensure RabbitMQ is running and check the logs${NC}"
    exit 1
fi

# 验证插件是否已启用
echo -e "${YELLOW}Verifying plugin installation...${NC}"
if "${RABBITMQ_PREFIX}/sbin/rabbitmq-plugins" list | grep -q "^\[E\].*${PLUGIN_NAME}"; then
    echo -e "${GREEN}Plugin verification successful!${NC}"
    echo -e "\n${YELLOW}Plugin status:${NC}"
    "${RABBITMQ_PREFIX}/sbin/rabbitmq-plugins" list | grep delayed || true
    echo -e "\n${GREEN}===============================================${NC}"
    echo -e "${GREEN}  Plugin installation completed!              ${NC}"
    echo -e "${GREEN}===============================================${NC}"
    echo -e "${YELLOW}Note: You may need to restart RabbitMQ for the plugin to take full effect:${NC}"
    echo -e "  brew services restart rabbitmq"
else
    echo -e "${YELLOW}Warning: Plugin may not be fully enabled. Please check manually:${NC}"
    echo -e "  ${RABBITMQ_PREFIX}/sbin/rabbitmq-plugins list"
fi
