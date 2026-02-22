# 配置文件变更说明

本项目配置文件已由多个`.properties`文件转换为`.yml`格式，并按照环境进行拆分。

## 变更内容

原有配置文件已转换为以下YAML格式文件：

1. `application.yml` - 基础公共配置
2. `application-dev.yml` - 开发环境配置
3. `application-test.yml` - 测试环境配置
4. `application-pre.yml` - 预发布环境配置
5. `application-prod.yml` - 生产环境配置

## 配置结构说明

- `application.yml` 包含所有环境共用的基础配置
- 各环境特定的配置（如数据库连接、Redis、消息队列等）放置在对应的环境配置文件中
- 不同环境间的配置完全隔离，便于维护和切换

## 环境激活方式

配置激活方式不变，仍使用：

```yml
spring.profiles.active=${profiles.active:dev}
```

在POM文件中通过profiles配置各环境的激活参数：

```xml
<profiles>
    <profile>
        <id>dev</id>
        <properties>
            <profile.active>dev</profile.active>
        </properties>
    </profile>
    <profile>
        <id>prod</id>
        <properties>
            <profile.active>prod</profile.active>
        </properties>
    </profile>
    <!-- 其他环境配置 -->
</profiles>
```

## 迁移优势

1. YAML格式结构更清晰，层次分明
2. 环境配置分离，管理和维护更加方便
3. 避免重复配置，减少冗余
4. 符合Spring Boot推荐的配置方式

如需恢复旧配置，原配置文件已备份在`config_backup`目录下。

## 代码适配

1. 所有使用`@PropertySource`引用特定properties文件的类都已修改为直接使用`@Value`注解从application.yml中读取配置
2. 以下类已进行修改:
   - `FileOSSConfig`
   - `TranslationUtil`
   - `DeepSeekAiUtils`
   - `KimiAiUtils`
   - `SiliconFlowAiUtil`
   - `UniSmsUtil`
   - `XunFeiR1Util`
   - `XunFeiHttpUtil`
   - `XunFeiAiUtil`

## Maven配置调整

1. 更新了maven-resources-plugin的版本从3.2.0到3.3.1，解决了资源文件编码问题
2. 添加了更多非过滤文件扩展名，确保二进制文件不被破坏
