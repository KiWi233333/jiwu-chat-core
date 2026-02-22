# 数据库说明

项目使用**单一建表脚本**，包含用户、系统、聊天、账单、资源等所需表结构。

## 使用方式

1. **一键初始化**：执行 `jiwu-chat-db.sql` 即可完成建库、建表与基础数据初始化。

   ```bash
   mysql -u root -p < docker-entrypoint-initdb.d/jiwu-chat-db.sql
   ```

   脚本会创建数据库 `jiwu-chat-db`，并创建所有表及初始化角色/菜单/权限、默认管理员账号（用户名 `admin`）、聊天示例数据等。

2. **敏感数据**：脚本中管理员密码、盐、手机、邮箱等已置空。部署后请在系统中修改管理员密码或通过「忘记密码」流程设置。

## 表列表

- **用户与系统**：`sys_user`, `sys_user_address`, `sys_user_role`, `sys_user_salt`, `sys_menu`, `sys_permission`, `sys_role`, `sys_role_menu`, `sys_role_permission`, `sys_secure_invoke_record`
- **聊天**：`chat_contact`, `chat_message`, `chat_message_reaction`, `chat_room`, `chat_room_group`, `chat_room_self`, `chat_group_member`, `chat_user_apply`, `chat_user_friend`
- **用户侧**：`user_bills`, `user_wallet`, `user_recharge_combo`
- **资源**：（无独立资源表）

## 配置与启动

- 各 profile 的 `spring.datasource.url` 指向上述数据库（默认库名 `jiwu-chat-db`，可按需修改）。
- dev 环境不包含预置敏感数据；prod 环境敏感配置通过环境变量注入且默认置空，便于安全部署。
