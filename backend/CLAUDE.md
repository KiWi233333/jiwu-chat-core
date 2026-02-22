# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Jiwu Hub (极物圈) 基于 Spring Boot 3.5.6 与 Java 21，提供用户、系统、资源管理与实时聊天（文本/图片/文件/语音/视频消息、群聊/单聊、撤回/删除/群通知）能力。

## Tech Stack

- **Java 21** + **Spring Boot 3.5.6** (Jakarta EE, not javax)
- **MyBatis Plus** 3.5.14 + MyBatis Plus Join 1.5.4
- **Redis** (Spring Data Redis) + **Redisson** 3.50.0 (distributed locks)
- **Caffeine** (local caching) + **RabbitMQ** (async messaging)
- **MySQL 8.4** + **Netty** 4.1.94 (WebSocket on port 9091)
- **Knife4j** 4.5.0 (OpenAPI 3/Swagger at `/doc.html`)
- **MapStruct** 1.5.5 + **Lombok** 1.18.30 + **Hutool** 5.8.40

## Multi-Module Project Structure

```
jiwu-chat-api/                         # Parent POM (packaging: pom)
├── jiwu-chat-common-core/             # Core utilities, annotations, AOP, configs, exceptions
├── jiwu-chat-common-data/             # Shared data layer (POJOs, Mappers, DAOs, DTOs, events)
├── jiwu-chat-module-chat/             # WebSocket chat (Netty-based)
├── jiwu-chat-module-res/              # Resource management (file upload, OSS)
├── jiwu-chat-module-sys/              # System management (roles, permissions, API keys)
├── jiwu-chat-module-user/             # User services (auth, profile, wallet)
├── jiwu-chat-module-admin/            # Admin management
└── jiwu-chat-starter/                 # Application entry point (main class, all configs)
```

### Module Dependencies

```
jiwu-chat-starter
    └── depends on: user, chat, res, sys, admin
        └── depend on jiwu-chat-common-data
            └── depends on jiwu-chat-common-core
```

### Module Internal Structure

Each business module follows:

```
jiwu-chat-module-xxx/src/main/java/com/jiwu/api/xxx/
├── controller/           # REST endpoints (admin/ for admin-only)
├── service/              # Service interfaces + impl/
├── common/
│   ├── dto/              # Module-specific request DTOs
│   ├── vo/               # Response View Objects
│   ├── enums/            # Module-specific enums
│   └── event/            # Domain events + listener/
└── config/               # Module configurations
```

## Development Commands

```bash
# Build entire project
mvn clean compile

# Run (dev profile is default)
mvn spring-boot:run -pl jiwu-chat-starter

# Run with specific profile
mvn spring-boot:run -pl jiwu-chat-starter -Dspring-boot.run.profiles=prod

# Package as JAR
mvn clean package -DskipTests

# Run packaged JAR
java -jar jiwu-chat-starter/target/jiwu-chat-api.jar

# Install to local repo (required after changes to common modules)
mvn clean install -DskipTests

# Run a single test
mvn test -Dtest=ClassName -pl jiwu-chat-starter
```

## Key Architecture Patterns

### 1. Response Format (`Result<T>`)

All APIs return `Result<T>`:

```java
return Result.ok(data);
return Result.ok("message", data);
return Result.fail("error message");
return Result.fail(ResultStatus.PARAM_ERR);
```

### 2. Exception Handling

```java
throw new BusinessException(ResultStatus.USER_NOT_FOUND);

// AssertUtil throws BusinessException on failure
AssertUtil.isTrue(condition, ResultStatus.PARAM_ERR, "msg");
AssertUtil.isFalse(condition, "msg");
AssertUtil.isEmpty(value, "msg");
AssertUtil.equal(expected, actual, "msg");
```

### 3. Custom Annotations

| Annotation                                              | Purpose                                         |
| ------------------------------------------------------- | ----------------------------------------------- |
| `@PortFlowControl(limit, time, timeUnit, errorMessage)` | Rate limiting (AOP via `FlowControlAspect`)     |
| `@RedissonLock`                                         | Distributed lock (AOP via `RedissonLockAspect`) |
| `@Auth`                                                 | Authentication required                         |
| `@IgnoreAuth`                                           | Skip auth interceptor                           |
| `@ReqPermission`                                        | Permission-based access                         |
| `@Password`, `@Phone`                                   | Custom JSR-303 validators                       |
| `@SecureInvoke`                                         | Guaranteed-once execution with DB record        |

### 4. User Context in Controllers

```java
// From request attribute (set by AuthenticationFilter)
String userId = request.getAttribute(UserConstant.USER_ID_KEY).toString();
// Or via thread-local holder
String userId = RequestHolderUtil.get().getId();
// Token from header
@RequestHeader(name = JwtConstant.HEADER_NAME) String token
```

### 5. Data Access (MyBatis Plus)

```java
// Mapper: extends MPJBaseMapper for join support
@Mapper
public interface UserMapper extends MPJBaseMapper<User> {}

// DAO: extends MPJBaseServiceImpl (used directly in services)
@Service
public class UserDAO extends MPJBaseServiceImpl<UserMapper, User> {
    // Fluent query
    public User getByUsername(String name) {
        return lambdaQuery().eq(User::getUsername, name).one();
    }

    // Join query
    public UserVO getWithDetails(String id) {
        return selectJoinOne(UserVO.class,
            new MPJLambdaWrapper<User>()
                .eq(User::getId, id)
                .leftJoin(UserDetail.class, UserDetail::getUserId, User::getId));
    }
}
```

### 6. Cache Abstraction

```java
// Redis string cache - extend and implement 3 methods
public class UserCache extends AbstractRedisStringCache<String, UserVO> {
    @Override
    protected String getKey(String userId) { return "user:" + userId; }
    @Override
    protected Long getExpireSeconds() { return 3600L; }
    @Override
    protected Map<String, UserVO> load(List<String> userIds) { /* load from DB */ }
}
```

Existing caches in `jiwu-chat-common-data/cache/`: `UserCache`, `UserInfoCache`, `ChatRoomCache`, `ChatMsgCache`, `ChatGroupMemberCache`, etc.

### 7. Cursor-Based Pagination

For high-volume paginated lists (messages, contacts):

```java
CursorPageBaseVO<T> result = CursorUtils.getCursorPageByMysql(dao, req, wrapper, cursorField);
```

### 8. Event-Driven Architecture

Spring events published via `ApplicationEventPublisher`; RabbitMQ for cross-service async.

- Events defined in `jiwu-chat-common-data/event/` (e.g., `UserRegisterEvent`, `UserLogoutEvent`, `OAuthUserAvatarEvent`)
- Listeners in `jiwu-chat-module-chat/common/event/listener/`
- MQ producer: `MqProducer` with `@SecureInvoke` for guaranteed delivery

### 9. Chat Message Type Strategy

Each message type has a handler extending `AbstractMsgHandler<T>`:

```
MsgHandlerFactory → AbstractMsgHandler implementations:
  TextMsgHandler, ImgMsgHandler, FileMsgHandler,
  SoundMsgHandler, VideoMsgHandler, RecallMsgHandler,
  DelMsgHandler, GroupNoticeMsgHandler, SystemMsgHandler
```

### 10. WebSocket (Netty)

- Server starts on port 9091 (`NettyWebSocketServer`)
- Pipeline: `HttpServerCodec → HttpObjectAggregator → WebSocketServerProtocolHandler → IdleStateHandler → NettyWebSocketServerHandler`
- Token auth on handshake via `WSAuthorize`
- Push notifications via `PushService` → `WSPushTypeEnum` (channels: single user, group room, all users)

## Coding Standards

### Naming Conventions

| Type                     | Pattern                             |
| ------------------------ | ----------------------------------- |
| Controller               | `*Controller`                       |
| Service interface / impl | `*Service` / `*ServiceImpl`         |
| DAO                      | `*DAO`                              |
| Mapper                   | `*Mapper`                           |
| Entity                   | Domain noun (`User`, `ChatMessage`) |
| Request DTO              | `*DTO`                              |
| Response                 | `*VO`                               |

### Code Style

```java
@Slf4j
@RestController
@RequestMapping("/user")
@Tag(name = "User Module")               // Knife4j
public class UserController {
    @Resource                             // Use @Resource, not @Autowired
    private UserService userService;

    @Operation(summary = "Get user")
    @GetMapping("/{id}")
    public Result<UserVO> getUser(@PathVariable String id) {
        return Result.ok(userService.getUser(id));
    }
}

@Service
public class UserServiceImpl implements UserService {
    @Transactional(rollbackFor = Exception.class)  // Writes
    public void createUser(User user) {}

    @Transactional(readOnly = true)                // Reads
    public User getUser(String id) {}
}
```

## Configuration

- Profiles: `dev` (default), `test`, `pre`, `prod`
- Config files: `jiwu-chat-starter/src/main/resources/application-{profile}.yml`
- API docs: `http://localhost:9090/doc.html`
- WebSocket: `ws://localhost:9091/ws`
- Database init scripts: `docker-entrypoint-initdb.d/`

## External Services

| Service      | Purpose              |
| ------------ | -------------------- |
| Qiniu Cloud  | Object storage (OSS) |
| UniSMS/SMS4J | SMS                  |
| Tencent TMT  | Translation          |
