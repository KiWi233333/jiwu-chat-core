<p align="center">
  <img src="frontend/.doc/jiwuchat-tauri.png" width="120" alt="JiwuChat" />
</p>

<h3 align="center">
  <strong>JiwuChat - 极物聊天</strong>
</h3>

<p align="center">
  <a href="./frontend/README.md">前端文档</a> ·
  <a href="./backend/README.md">后端文档</a> ·
  <a href="./frontend/Run.md">运行说明</a> ·
  <a href="https://blog.jiwuchat.top/">官网</a> 
</p>

<p align="center">
  <a href="https://github.com/KiWi233333/JiwuChat" target="_blank"><img src="https://img.shields.io/badge/Github-项目地址-blueviolet?style=flat" alt="项目地址" /></a>
  <a href="https://github.com/KiWi233333/JiwuChat/stargazers" target="_blank"><img src="https://img.shields.io/github/stars/KiWi233333/JiwuChat?style=social" alt="Stars" /></a>
  <a href="https://qm.qq.com/q/iSaETNVdKw" target="_blank"><img src="https://img.shields.io/badge/QQ群-939204073-blue?logo=tencentqq&logoColor=white" alt="QQ群" /></a>
</p>

**多语言**：简体中文（本文） | [English](frontend/.doc/README.en.md) | [Deutsch](frontend/.doc/README.de.md) | [Español](frontend/.doc/README.es.md) | [Français](frontend/.doc/README.fr.md) | [日本語](frontend/.doc/README.ja.md) | [한국어](frontend/.doc/README.ko.md) | [Português](frontend/.doc/README.pt.md) | [Русский](frontend/.doc/README.ru.md)

---

## 介绍

本仓库为 **Jiwu Chat Core** 单体仓库（Monorepo），包含极物聊天客户端与极物圈后端 API，前后端同仓便于本地联调与统一维护。

- **前端 [JiwuChat](frontend/)**：基于 **Tauri 2** 与 **Nuxt 4** 的轻量（约 10MB）多平台聊天应用，支持桌面 / Web / 移动端；具备实时消息、深浅色主题等。
- **后端 [Jiwu Hub API](backend/)**：基于 **Spring Boot 3.5** 与 **Java 21** 的后端服务，提供用户与系统管理、资源管理、账单钱包，以及基于 **Netty WebSocket** 的实时聊天能力，可与本仓库前端或其他客户端配合使用。

一套代码多端适配，前后端分离便于扩展与二次开发。

---

## 一套代码，多端适配

<p align="center">
  <img src="frontend/.doc/previews.png" alt="多端适配" width="800" />
</p>

---

## 默认账号

| 项目 | 说明         |
| ---- | ------------ |
| 账号 | `superAdmin` |
| 密码 | `123456`     |

---

## 仓库结构

```
jiwu-chat-core/
├── frontend/          # JiwuChat 客户端（Nuxt 4 + Tauri 2）
│   ├── app/            # Nuxt 应用源码（页面、组件、composables）
│   ├── src-tauri/      # Tauri 2 桌面/移动端 Rust 层
│   ├── .doc/           # 截图、多语言 README
│   ├── README.md       # 前端详细说明与截图
│   └── Run.md          # 前端运行与调试说明
├── backend/            # 极物圈 API（Spring Boot 3.5）
│   ├── jiwu-chat-common-core/   # 公共核心（注解、异常、工具、配置）
│   ├── jiwu-chat-common-data/  # 公共数据层（实体、Mapper、缓存、事件）
│   ├── jiwu-chat-module-user/  # 用户服务（注册登录、资料、地址、钱包）
│   ├── jiwu-chat-module-sys/   # 系统管理（角色、权限、菜单、API Key）
│   ├── jiwu-chat-module-res/   # 资源管理（上传、OSS）
│   ├── jiwu-chat-module-chat/  # 实时聊天（Netty WebSocket、消息、群组）
│   ├── jiwu-chat-module-admin/ # 管理后台 API
│   ├── jiwu-chat-starter/      # 启动模块（主类与配置）
│   ├── docker-entrypoint-initdb.d/   # 数据库初始化脚本
│   └── README.md       # 后端模块说明、API、部署
└── README.md           # 本文件（主项目说明）
```

- 前端详细说明（技术栈、开发、截图、多语言）：[frontend/README.md](frontend/README.md)
- 后端详细说明（模块、接口、环境变量、部署）：[backend/README.md](backend/README.md)
- 前端运行与调试：[frontend/Run.md](frontend/Run.md)
- **致谢**：[MallChat](https://github.com/zongzibinbin/MallChat)、Tauri、Nuxt、Vue、Spring Boot、MyBatis Plus、Netty、Knife4j 等开源项目。

---

## 快速开始

> **🛠 在线配置器**：可视化填写环境变量、自动生成 `.env` 文件与完整启动命令 →
> **[https://kiwi233333.github.io/jiwu-chat-core/](https://kiwi233333.github.io/jiwu-chat-core/)**

### 一、使用预构建镜像（推荐新用户）

需已安装 [Docker](https://docs.docker.com/get-docker/) 与 Docker Compose。镜像见 [GitHub Packages](https://github.com/KiWi233333/jiwu-chat-core/pkgs/container/jiwu-chat-core)。

**1. 拉取镜像**

```bash
docker pull ghcr.io/kiwi233333/jiwu-chat-core:latest
```

**2. 创建配置（含 .env）**

```bash
curl -fsSL https://raw.githubusercontent.com/KiWi233333/jiwu-chat-core/main/deploy/install.sh | bash -s latest
```

**3. 部署**

```bash
cd jiwu-chat-core && docker compose up -d
```

等待约 1–2 分钟（MySQL 初始化）后访问：前端 http://localhost:3000 · API http://localhost:9090 · 文档 http://localhost:9090/doc.html。默认账号 `superAdmin` / `123456`。

### 二、Docker 一键启动（从源码构建，推荐开发）

无需本地安装 MySQL / Redis / RabbitMQ / JDK / Node，在项目根目录从源码构建并启动完整系统：

```bash
# 在项目根目录执行（首次会构建镜像，约 5–15 分钟）
./scripts/docker-start.sh

# 或直接使用 docker compose
docker compose up -d --build
```

启动后可访问：

| 服务      | 地址                                              |
| --------- | ------------------------------------------------- |
| 前端      | http://localhost:3000（npx serve .output/public） |
| 后端 API  | http://localhost:9090                             |
| API 文档  | http://localhost:9090/doc.html                    |
| WebSocket | ws://localhost:9091/ws                            |

默认体验账号：`ikun233` / `123456`。停止服务：`docker compose down`。详细说明见 [DOCKER.md](DOCKER.md)；前后端可配置项见 [CONFIG.md](CONFIG.md)（根目录 `.env`）。

### 三、环境要求概览（本地开发）

| 端   | 要求                                                                                          |
| ---- | --------------------------------------------------------------------------------------------- |
| 前端 | Node.js ≥20、pnpm ≥10、Rust（[Tauri 前置文档](https://tauri.app/zh-cn/start/prerequisites/)） |
| 后端 | JDK 21+、Maven 3.8+、MySQL 8.0+、Redis 6.0+、RabbitMQ 3.11+（可选）                           |

### 1. 克隆仓库

```bash
git clone <本仓库地址>
cd jiwu-chat-core
```

### 2. 启动后端

```bash
cd backend
# 首次需初始化数据库，见 backend/docker-entrypoint-initdb.d/README-DB.md
mysql -u root -p < docker-entrypoint-initdb.d/jiwu-chat-db.sql

mvn clean compile
mvn spring-boot:run -pl jiwu-chat-starter
```

| 服务      | 地址                           |
| --------- | ------------------------------ |
| HTTP API  | http://localhost:9090          |
| API 文档  | http://localhost:9090/doc.html |
| WebSocket | ws://localhost:9091/ws         |

### 3. 启动前端

```bash
cd frontend
pnpm install
pnpm run dev:nuxt    # Web 开发
pnpm run dev:tauri   # 桌面开发
```

更多命令（构建、移动端、调试）见 [frontend/README.md](frontend/README.md) 与 [frontend/Run.md](frontend/Run.md)。

---

## 技术栈概览

| 端   | 类别       | 技术                                                       |
| ---- | ---------- | ---------------------------------------------------------- |
| 前端 | 框架       | Nuxt 4、Tauri 2、Vue 3、TypeScript                         |
| 前端 | UI / 状态  | Element Plus、Pinia、UnoCSS                                |
| 前端 | 构建       | Vite (rolldown-vite)、Nuxi                                 |
| 后端 | 语言与框架 | Java 21、Spring Boot 3.5                                   |
| 后端 | 数据与缓存 | MySQL 8、MyBatis Plus、Redis、Caffeine、RabbitMQ、Redisson |
| 后端 | 实时通讯   | Netty WebSocket                                            |
| 后端 | 文档       | Knife4j (OpenAPI 3)                                        |

更细版本与说明见 [frontend/README.md](frontend/README.md) 与 [backend/README.md](backend/README.md)。

---

## 项目截图

- **桌面端 / 移动端 / Web 端** 更多截图与说明见 [frontend/README.md#项目截图](frontend/README.md#项目截图)。

<p align="center">
  <img src="frontend/.doc/desktop/login.png" width="280" alt="登录" />
  <img src="frontend/.doc/desktop/home.png" width="280" alt="首页" />
  <img src="frontend/.doc/desktop/ai.png" width="280" alt="AI群聊" />
</p>

---

## 系统功能

<p align="center">
  <img src="frontend/.doc/JiwuChat%20功能导图.png" alt="功能导图" width="700" />
</p>

<details>
  <summary><strong>功能表格（展开/折叠）</strong></summary>

| 模块       | 子模块     | 功能描述                                                       | 是否达成 |
| ---------- | ---------- | -------------------------------------------------------------- | -------- |
| 用户模块   | 账户管理   | 用户注册、登录、历史登录账号选择                               | ✅       |
|            | 账号安全   | 邮箱/手机号绑定提醒、设备管理、账号安全验证                    | ✅       |
| 消息模块   | 基础聊天   | 文本消息、图片消息、视频消息、文件上传、消息撤回、消息已读状态 | ✅       |
|            | 数据同步   | 多设备消息同步、阅读状态同步                                   | ✅       |
|            | 高级聊天   | 消息引用回复、@提及功能、公告、撤回消息重新编辑                | ✅       |
| 会话模块   | 会话管理   | 会话列表、置顶会话、隐藏会话、会话未读数统计、会话排序         | ✅       |
| 群聊模块   | 群聊操作   | 创建群聊、退出群聊、查看群聊详情                               | ✅       |
|            | 群成员管理 | 群成员管理、设置管理员、撤销管理员、获取@列表                  | ✅       |
| 联系人模块 | 好友操作   | 好友申请、好友搜索、好友列表、拒绝好友申请、删除好友           | ✅       |
|            | 资料与通知 | 好友详情查看、申请未读数统计                                   | ✅       |
| 通知系统   | 消息通知   | 桌面通知、系统托盘提醒、铃声设置、消息免打扰                   | ✅       |
| 扩展功能   | 综合集成   | 博客集成                                                       | ✅       |
| 其他模块   | 其他功能   | 聊天社交功能、文件下载管理、翻译工具（AI 翻译/腾讯翻译）       | ✅       |
|            | 文件与播放 | 图片预览器、视频播放器、文件下载、批量图片上传                 | ✅       |
|            | 主题配置   | 深浅色主题切换、系统主题跟随、字体设置、自适应布局             | ✅       |
|            | 平台兼容   | Windows、macOS、Linux、Android、Web 端适配                     | ✅       |

</details>

---

## 常见问题

- **前端**：安装与运行、Tauri 构建、多端调试等见 [frontend/README.md](frontend/README.md) 的「常见问题」与 [frontend/Run.md](frontend/Run.md)。
- **后端**：数据库/表未创建、WebSocket 连不上、上传/短信/邮件配置等见 [backend/README.md](backend/README.md) 的「常见问题」与 [backend/使用说明.md](backend/使用说明.md)（如有）。

<details>
  <summary><strong>macOS 安装提示「已损坏」或证书问题</strong></summary>

macOS 安装时若提示“安装包已损坏”或证书相关，可按以下处理：

1. 系统设置 → 安全性与隐私 → 允许“任何来源”的 App（若可见）。
2. **安装前**在终端执行（将 `JiwuChat_1.8.0_aarch64.dmg` 换成实际文件名）：
   ```bash
   sudo xattr -rd com.apple.quarantine JiwuChat_1.8.0_aarch64.dmg
   ```
3. **已安装**则执行：
   ```bash
   sudo xattr -r -d com.apple.quarantine /Applications/JiwuChat.app
   ```

</details>

- 其它问题欢迎在仓库 [Issues](https://github.com/KiWi233333/JiwuChat/issues) 留言或通过下方联系方式反馈。

---

## 免责声明

- 本仓库仅供学习与交流使用，软件按「原样」提供，不提供任何明示或暗示的保证。
- 作者及贡献者不对因使用本软件而产生的任何直接、间接、附带或后果性损害承担责任。
- 使用者应自行确保其使用行为符合所在地区法律法规，因违规使用产生的法律责任由使用者自行承担。
- 赞助行为为自愿支持，不构成任何服务或承诺。

---

## ☕ 赞助支持

如果您觉得 JiwuChat 对您有帮助，欢迎赞助支持，您的支持是我们不断前进的动力！

<p align="center">
  <img src="assets/wechat.png" width="200" alt="微信支付" />
  <img src="assets/alipay.png" width="200" alt="支付宝" />
</p>

---

## 联系方式

- 邮箱：[kiwi2333@qq.com](mailto:kiwi2333@qq.com)
- QQ：[1329634286](https://wpa.qq.com/msgrd?v=3&uin=1329634286&site=qqq&menu=yes)
- QQ 群：[939204073](https://qm.qq.com/q/iSaETNVdKw)
