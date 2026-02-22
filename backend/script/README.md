# JiwuChat 脚本使用说明

本目录包含 JiwuChat 项目的各种管理脚本。

## 🚀 快速开始

### 方式一：使用聚合脚本（推荐）

使用 `run.sh` 作为统一入口，支持交互式菜单和直接运行：

```bash
# 交互式菜单模式（默认）
./script/run.sh

# 直接运行指定脚本
./script/run.sh init_db_local.sh

# 查看帮助
./script/run.sh --help

# 列出所有可用脚本
./script/run.sh --list
```

### 方式二：直接运行单个脚本

所有脚本都可以独立运行，功能保持不变：

```bash
# 数据库初始化（Docker/容器环境）
./script/init_db.sh

# 本地数据库初始化（交互式）
./script/init_db_local.sh

# 运行开发环境应用
./script/dev-run.sh

# 安装开发环境服务
./script/install_brew_services.sh

# 管理 brew 服务
./script/server_brew.sh

# 单独安装 RabbitMQ 延迟队列插件
./script/install-rabbitmq-delayed-plugin.sh
```

## 📋 脚本说明

### 1. `run.sh` - 聚合脚本管理器

**功能：**

- 提供统一的脚本执行入口
- 支持交互式菜单选择
- 支持直接运行指定脚本
- 自动处理脚本执行权限

**使用示例：**

```bash
# 交互式菜单
./script/run.sh

# 直接运行脚本
./script/run.sh init_db_local.sh --env dev

# 查看帮助
./script/run.sh --help
```

### 2. `init_db.sh` - 数据库初始化（Docker/容器环境）

**功能：**

- 在 Docker 容器或环境中初始化数据库
- 根据环境变量自动选择数据库
- 自动导入 SQL 文件

**环境变量：**

- `APP_ENV` 或 `ENVIRONMENT`: 环境名称（dev/test/prod，默认：prod）
- `MYSQL_HOST`: MySQL 主机（默认：127.0.0.1）
- `MYSQL_PORT`: MySQL 端口（默认：3306）
- `MYSQL_USER`: MySQL 用户（默认：root）
- `MYSQL_PASSWORD` 或 `MYSQL_ROOT_PASSWORD`: MySQL 密码
- `MYSQL_DATABASE`: 目标数据库名称

**使用示例：**

```bash
# 使用默认配置（prod 环境）
./script/init_db.sh

# 指定环境
APP_ENV=dev ./script/init_db.sh
```

### 3. `init_db_local.sh` - 本地数据库初始化（交互式）

**功能：**

- 本地环境数据库初始化
- 交互式选择环境（dev/test/prod）
- 交互式输入 MySQL 连接信息
- 自动导入 SQL 文件

**使用示例：**

```bash
# 交互式模式
./script/init_db_local.sh

# 指定环境（跳过部分交互）
./script/init_db_local.sh --env dev

# 查看帮助
./script/init_db_local.sh --help
```

**选项：**

- `-e, --env <dev|test|prod>`: 指定目标环境
- `-h, --help`: 显示帮助信息

### 4. `dev-run.sh` - 运行开发环境应用

**功能：**

- 运行开发环境的 Java 应用
- 使用 dev profile

**使用示例：**

```bash
./script/dev-run.sh
```

**注意：** 需要先编译项目：

```bash
mvn clean package
```

### 5. `install_brew_services.sh` - 安装开发环境服务

**功能：**

- 一键安装开发环境所需服务（MySQL, Redis, RabbitMQ）
- 自动安装 RabbitMQ 延迟队列插件（使用智能版本匹配）
- 自动配置 MySQL root 密码（123456）
- 自动启动所有服务

**使用示例：**

```bash
./script/install_brew_services.sh
```

**插件安装策略：**

1. **优先策略**：根据已安装的 RabbitMQ 版本匹配对应的插件版本（更安全、更兼容）
2. **回退策略**：如果版本匹配失败，使用 GitHub API 获取最新版本

**注意：**

- 仅适用于 macOS with Homebrew
- 需要管理员权限
- MySQL root 密码将设置为 `123456`
- RabbitMQ 插件安装已集成，无需单独运行 `install-rabbitmq-delayed-plugin.sh`

### 6. `install-rabbitmq-delayed-plugin.sh` - 安装 RabbitMQ 延迟队列插件

**功能：**

- 根据已安装的 RabbitMQ 版本自动匹配并安装对应的插件版本
- 智能版本匹配：从精确版本到降级版本自动尝试
- 自动验证插件文件有效性
- 自动启用插件并验证安装结果

**使用示例：**

```bash
# 直接运行（需要先安装 RabbitMQ）
./script/install-rabbitmq-delayed-plugin.sh
```

**特性：**

- ✅ 自动检测 RabbitMQ 版本
- ✅ 智能版本匹配（支持多个降级策略）
- ✅ 文件完整性验证
- ✅ 已安装检查（避免重复安装）
- ✅ 彩色输出，清晰的错误提示

**注意：**

- 仅适用于 macOS with Homebrew
- 需要先安装 RabbitMQ：`brew install rabbitmq`
- 如果版本匹配失败，脚本会提供详细的错误信息

### 7. `server_brew.sh` - 管理 brew 服务

**功能：**

- 管理 brew 服务的启动和停止
- 支持 MySQL, Redis, RabbitMQ 服务

**使用示例：**

```bash
# 交互式菜单
./script/server_brew.sh
```

**功能选项：**

1. 启动所有服务
2. 停止所有服务
3. 退出

## 🔧 脚本执行权限

所有脚本都已设置执行权限。如果遇到权限问题，可以手动添加：

```bash
chmod +x script/*.sh
```

## 📝 注意事项

1. **数据库脚本**：`init_db.sh` 和 `init_db_local.sh` 功能类似，但使用场景不同：
   - `init_db.sh`: 适用于 Docker 容器或 CI/CD 环境
   - `init_db_local.sh`: 适用于本地开发环境，提供更多交互

2. **RabbitMQ 插件安装**：
   - `install_brew_services.sh` 已集成插件安装功能，推荐使用
   - `install-rabbitmq-delayed-plugin.sh` 可单独使用，适合仅安装插件或更新插件

3. **环境变量**：脚本支持通过环境变量配置，优先级高于默认值

4. **错误处理**：所有脚本都使用 `set -euo pipefail` 确保错误时立即退出

5. **日志输出**：脚本使用统一的日志格式和颜色输出，便于识别信息、警告和错误

## 🆘 常见问题

**Q: 脚本提示 "command not found"**
A: 确保脚本有执行权限：`chmod +x script/run.sh`

**Q: MySQL 连接失败**
A: 检查 MySQL 服务是否运行，以及连接信息是否正确

**Q: 如何查看脚本帮助**
A: 使用 `./script/run.sh --help` 或 `./script/<脚本名> --help`（如果支持）

## 📚 相关文档

- 项目主文档：`../README.md`
- 数据库初始化文档：`../docker-entrypoint-initdb.d/README.md`（如果存在）
