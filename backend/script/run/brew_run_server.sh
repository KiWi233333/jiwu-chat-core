#!/bin/bash

# 定义服务名称
SERVICES=("mysql" "redis" "rabbitmq")

# 函数：启动服务
start_services() {
  echo "启动服务..."
  for service in "${SERVICES[@]}"; do
    echo "启动 $service..."
    brew services start "$service"
    if [ $? -eq 0 ]; then
      echo "$service 启动成功"
    else
      echo "$service 启动失败"
    fi
  done
  echo "所有服务启动完成。"
}

# 函数：停止服务
stop_services() {
  echo "停止服务..."
  for service in "${SERVICES[@]}"; do
    echo "停止 $service..."
    brew services stop "$service"
    if [ $? -eq 0 ]; then
      echo "$service 停止成功"
    else
      echo "$service 停止失败"
    fi
  done
  echo "所有服务停止完成。"
}

# 菜单选择
show_menu() {
  echo "请选择操作："
  echo "1. 启动所有服务"
  echo "2. 停止所有服务"
  echo "3. 退出"
  read -p "请输入选项 (1/2/3): " choice
}

# 主程序
main() {
  while true; do
    show_menu
    case "$choice" in
    1)
      start_services
      ;;
    2)
      stop_services
      ;;
    3)
      echo "退出脚本。"
      exit 0
      ;;
    *)
      echo "无效的选项，请重新选择。"
      ;;
    esac
  done
}

# 运行主程序
main
