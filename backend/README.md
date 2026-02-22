<p align="center">
  <img src="./.doc/logo.png" width="160" alt="极物圈" />
</p>

<p align="center">
  <strong>Jiwu Hub API</strong>
</p>

<p align="center">
  <a href="https://github.com/KiWi233333/JiwuChat">JiwuChat 客户端</a> ·
  <a href="https://github.com/Kiwi233333/front-jiwuquan-mall-pc">前台</a> ·
  <a href="https://github.com/Kiwi233333/front-jiwuquan-mall-back">后台</a> ·
  <a href="./LICENSE">GPL-3.0</a>
</p>

---

## 介绍

极物圈（Jiwu Hub）是一套基于 **Spring Boot 3.5** 与 **Java 21** 的后端 API，提供用户与系统管理、资源管理、账单钱包以及基于 **Netty WebSocket** 的实时聊天能力，采用前后端分离架构，便于扩展与二次开发。可与 [JiwuChat](https://github.com/KiWi233333/JiwuChat) 等客户端配合使用。✨

**主要能力：**

- **用户与系统**：注册/登录、个人资料、地址、角色权限、菜单、API Key、安全调用记录
- **资源**：文件上传（七牛 OSS）
- **聊天**：文本/图片/文件/语音/视频消息，单聊与群聊，消息撤回/删除/群通知、在线状态与历史
- **账单与钱包**：用户侧账单、钱包、充值套餐

---

## 系统功能

| 模块       | 子模块     | 功能描述                                               | 是否达成 |
| ---------- | ---------- | ------------------------------------------------------ | -------- |
| 用户模块   | 账户管理   | 用户注册、登录、JWT 认证、个人资料、地址管理           | ✅       |
|            | 账号安全   | 密码加密、敏感词过滤、XSS 防护、安全调用记录           | ✅       |
| 系统模块   | 权限管理   | 角色、权限、菜单、API Key、请求鉴权                    | ✅       |
| 消息模块   | 基础聊天   | 文本/图片/文件/语音/视频消息、消息撤回、删除、已读状态 | ✅       |
|            | 数据同步   | 多设备消息同步、阅读状态同步、历史消息游标分页         | ✅       |
|            | 高级聊天   | 消息引用回复、@提及、群公告、撤回后重新编辑            | ✅       |
| 会话模块   | 会话管理   | 会话列表、置顶、隐藏、未读数统计、会话排序             | ✅       |
| 群聊模块   | 群聊操作   | 创建群聊、退出群聊、群详情                             | ✅       |
|            | 群成员管理 | 群成员管理、管理员设置、@列表                          | ✅       |
| 联系人模块 | 好友操作   | 好友申请、搜索、列表、拒绝/删除好友                    | ✅       |
| 资源模块   | 文件与 OSS | 文件上传、七牛云 OSS                                   | ✅       |
| 其他       | 账单钱包   | 用户账单、钱包、充值套餐                               | ✅       |
|            | 文档与规范 | Knife4j OpenAPI 3、统一响应 `Result<T>`、全局异常处理  | ✅       |

---

## 技术栈

| 类别       | 技术/组件                                                       | 说明                   |
| ---------- | --------------------------------------------------------------- | ---------------------- |
| 语言与框架 | Java 21、Spring Boot 3.5.6                                      | Jakarta EE             |
| 数据       | MySQL 8.4、MyBatis Plus 3.5.14、MyBatis Plus Join 1.5.4         | 持久化与多表关联       |
| 缓存与消息 | Redis、Caffeine、RabbitMQ、Redisson                             | 分布式缓存、锁与异步   |
| 实时通讯   | Netty 4.1.94                                                    | WebSocket（9091 端口） |
| 安全与工具 | JWT(Auth0)、Mica-XSS、Sensitive-Word、Hutool、MapStruct、Lombok | 认证与工具链           |
| 文档       | Knife4j 4.5.0                                                   | OpenAPI 3 / Swagger    |

---

## 项目结构

```
jiwu-chat-api/
├── jiwu-chat-common-core/     # 公共核心（注解、异常、工具、配置）
├── jiwu-chat-common-data/     # 公共数据层（实体、Mapper、DAO、事件）
├── jiwu-chat-module-user/     # 用户服务（注册登录、资料、地址、钱包）
├── jiwu-chat-module-sys/      # 系统管理（角色、权限、菜单、API Key）
├── jiwu-chat-module-res/      # 资源管理（上传、OSS）
├── jiwu-chat-module-chat/     # 实时聊天（WebSocket、消息、群组）
├── jiwu-chat-module-admin/    # 管理后台 API
└── jiwu-chat-starter/         # 启动模块（主类与配置）
```

---

## 快速开始

### 环境要求

- **JDK 21+**
- **Maven 3.8+**
- **MySQL 8.0+**
- **Redis 6.0+**
- **RabbitMQ 3.11+**（可选，按需启用）

### 1. 克隆与编译

```bash
git clone <仓库地址>
cd jiwu-chat-api-1
mvn clean compile
```

### 2. 初始化数据库

使用项目提供的**单一建表脚本**完成建库、建表与基础数据（角色/菜单/权限、默认管理员、示例聊天数据）：

```bash
mysql -u root -p < docker-entrypoint-initdb.d/jiwu-chat-db.sql
```

脚本会创建数据库 `jiwu-chat-db`。更多说明见 [docker-entrypoint-initdb.d/README-DB.md](docker-entrypoint-initdb.d/README-DB.md)。

### 3. 配置与启动

- 开发环境默认使用 `dev` profile，数据源、Redis、RabbitMQ 等可通过环境变量覆盖（见 `jiwu-chat-starter/src/main/resources/application-dev.yml`）。
- 若使用上述脚本，请保证 `spring.datasource.url` 指向 `jiwu-chat-db`（或与脚本中库名一致）。

```bash
# 使用默认 dev 配置启动
mvn spring-boot:run -pl jiwu-chat-starter

# 指定 profile
mvn spring-boot:run -pl jiwu-chat-starter -Dspring-boot.run.profiles=test
```

### 4. 访问

| 服务      | 地址                           |
| --------- | ------------------------------ |
| HTTP API  | http://localhost:9090          |
| API 文档  | http://localhost:9090/doc.html |
| WebSocket | ws://localhost:9091/ws         |

默认管理员账号见数据库脚本说明（用户名 `admin`，首次部署后请修改密码）。

---

## API 与响应约定

- 文档地址：启动后访问 **http://localhost:9090/doc.html**（Knife4j / OpenAPI 3）。
- 统一响应体 `Result<T>`：
  - 成功：`Result.ok(data)` / `Result.ok("message", data)`
  - 失败：`Result.fail("message")` / `Result.fail(ResultStatus.xxx)`

---

## 配置与部署

### 环境变量一览

生产或 Docker 部署时，敏感配置均通过环境变量注入。常用变量如下：

| 类别     | 变量名                                                                     | 说明                | 必填     |
| -------- | -------------------------------------------------------------------------- | ------------------- | -------- |
| 数据源   | `SPRING_DATASOURCE_URL` / `USERNAME` / `PASSWORD`                          | JDBC 与账号密码     | 是       |
| Redis    | `SPRING_REDIS_HOST` / `PORT` / `DATABASE`                                  | Redis 连接          | 是       |
| RabbitMQ | `SPRING_RABBITMQ_*`                                                        | 消息队列（按需）    | 按需     |
| 七牛 OSS | `QINIU_ACCESS_KEY` / `QINIU_SECRET_KEY` / `QINIU_BUCKET` / `QINIU_HOST` 等 | 对象存储            | 需上传时 |
| UniSMS   | `UNISMS_ACCESS_KEY` / `UNISMS_SECRET_KEY` / `UNISMS_TEMPLATE_ID` 等        | 短信与 Webhook      | 需短信时 |
| 邮件     | `MAIL_HOST` / `MAIL_PORT` / `MAIL_USERNAME` / `MAIL_PASSWORD`              | SMTP 发信           | 需发信时 |
| 腾讯翻译 | `TENCENT_SECRET_ID` / `TENCENT_SECRET_KEY`                                 | 翻译 API            | 需翻译时 |
| 服务     | `SERVER_PORT`、`profiles.active`                                           | HTTP 端口、运行环境 | 否       |

更多项见 `jiwu-chat-starter/src/main/resources/application.yml` 及各 `application-{profile}.yml`。

### Docker 部署

```bash
mvn clean package -DskipTests
docker compose build
docker compose up -d
```

- 应用端口 9090、WebSocket 9091 已映射；MySQL/Redis/RabbitMQ 在 `docker-compose.yml` 中配置。
- **生产环境务必修改默认密码**（如 `MYSQL_ROOT_PASSWORD`、`SPRING_DATASOURCE_PASSWORD` 等）。

### Nginx 反向代理

- HTTP API（9090）与 WebSocket（9091）需分别代理。
- WebSocket 需设置：`Upgrade`、`Connection "upgrade"`。
- 参考示例见项目内 `nginx.conf`。

### 其他说明

- **数据库**：脚本创建 `jiwu-chat-db`；表列表与说明见 [docker-entrypoint-initdb.d/README-DB.md](docker-entrypoint-initdb.d/README-DB.md)。
- **环境**：`dev`（默认）、`test`、`pre`、`prod`，对应 `application-{profile}.yml`。
- **打包运行**：`mvn clean package -DskipTests`；`java -jar jiwu-chat-starter/target/jiwu-chat-starter-*.jar --spring.profiles.active=prod`

---

## 开发约定

- **命名**：Controller/Service/DAO/Mapper 后缀；查询 `get*`/`find*`/`list*`，变更 `add*`/`update*`/`delete*`。
- **注入**：优先 `@Resource`；写操作使用 `@Transactional(rollbackFor = Exception.class)`。
- **接口**：使用 `@Tag`、`@Operation` 等 Knife4j 注解；参数校验使用 `@Valid` 与 JSR-303。
- **异常**：业务异常使用 `BusinessException(ResultStatus)`，由全局异常处理并返回统一格式。

---

## 常见问题 | FAQ

- **启动报错找不到数据库 / 表**  
  请先执行 `docker-entrypoint-initdb.d/jiwu-chat-db.sql` 创建库表及基础数据，并确认 `spring.datasource.url` 指向 `jiwu-chat-db`。

- **WebSocket 连接失败**  
  确认 Netty 服务在 9091 端口启动；若经 Nginx 代理，需配置 WebSocket 的 `Upgrade` 与 `Connection` 头。

- **文件上传 / 短信 / 邮件不生效**  
  检查对应环境变量（七牛 OSS、UniSMS、SMTP）是否已在当前 profile 下配置。

- 如有其他问题，欢迎在 [Issues](https://github.com/KiWi233333/jiwu-chat-api-1/issues) 区留言或通过下方联系方式反馈。

---

## 许可证与致谢

- 许可证：[GPL-3.0](./LICENSE)
- 致谢：Spring Boot、MyBatis Plus、Hutool、Knife4j、Netty 等

---

## 联系方式

- 邮箱：[kiwi2333@qq.com](mailto:kiwi2333@qq.com)
- QQ: [1329634286](https://wpa.qq.com/msgrd?v=3&uin=1329634286&site=qqq&menu=yes)
- QQ 群: [939204073](https://qm.qq.com/q/iSaETNVdKw)

---

<p align="center">极物圈 · 后端 API</p>
