# API密钥管理模块

## 概述

本模块提供了完整的API密钥管理功能，包括创建、更新、删除、启用/停用、刷新等操作，支持权限控制和过期时间管理。

## 功能特性

### 1. 基础CRUD操作
- **创建API密钥**: 生成唯一的API密钥，支持自定义名称、过期时间和备注
- **更新API密钥**: 修改密钥名称、过期时间和备注信息
- **删除API密钥**: 安全删除API密钥及其权限关联
- **查询API密钥**: 支持分页查询、条件筛选和详情查看

### 2. 状态管理
- **启用/停用**: 动态控制API密钥的可用状态
- **过期管理**: 自动检测过期状态，支持设置永不过期
- **状态枚举**: 1-启用，0-停用，2-已过期

### 3. 安全特性
- **密钥刷新**: 支持重新生成API密钥，旧密钥自动失效
- **权限控制**: 支持为API密钥分配特定权限
- **使用记录**: 记录最近使用时间，便于监控和审计
- **脱敏显示**: 在界面上脱敏显示API密钥，保护安全性

### 4. 权限管理
- **权限分配**: 支持为API密钥分配多个权限
- **权限验证**: 提供权限检查接口，支持第三方系统集成
- **权限关联**: 通过中间表管理API密钥与权限的关系

## 数据库设计

### 主要表结构

#### sys_api_key (API密钥表)
- `id`: 主键ID
- `user_id`: 创建用户ID
- `key_name`: Key名称
- `api_key`: API密钥
- `status`: 状态（1:启用 0:停用 2:已过期）
- `expire_time`: 过期时间
- `remark`: 备注
- `last_used_time`: 最近使用时间
- `create_time`: 创建时间
- `update_time`: 更新时间

#### sys_api_key_permission (API密钥权限关联表)
- `id`: 关联ID
- `api_key_id`: API密钥ID
- `permission_id`: 权限ID
- `creator_id`: 创建者ID
- `create_time`: 创建时间
- `update_time`: 更新时间

## API接口

### 基础操作
- `POST /sys/api-key` - 创建API密钥
- `PUT /sys/api-key` - 更新API密钥
- `DELETE /sys/api-key/{id}` - 删除API密钥
- `GET /sys/api-key/{id}` - 获取API密钥详情

### 状态管理
- `PUT /sys/api-key/{id}/enable` - 启用API密钥
- `PUT /sys/api-key/{id}/disable` - 停用API密钥
- `PUT /sys/api-key/{id}/refresh` - 刷新API密钥

### 查询接口
- `GET /sys/api-key/page` - 分页查询API密钥
- `GET /sys/api-key/user` - 获取当前用户的API密钥列表
- `GET /sys/api-key/validate/{apiKey}` - 验证API密钥

## 使用示例

### 创建API密钥
```json
POST /sys/api-key
{
    "keyName": "第三方系统集成",
    "expireTime": "2026-08-16 23:59:59",
    "remark": "用于第三方系统API调用",
    "permissionIds": ["perm1", "perm2"]
}
```

### 查询API密钥
```json
GET /sys/api-key/page?pageNum=1&pageSize=10&keyName=集成&status=1
```

### 启用API密钥
```json
PUT /sys/api-key/{id}/enable
```

## 安全考虑

1. **密钥生成**: 使用安全的随机数生成器，支持自定义长度和前缀
2. **权限验证**: 每次API调用都需要验证密钥有效性和权限
3. **过期管理**: 自动检测过期状态，防止过期密钥被使用
4. **脱敏显示**: 在界面上不显示完整密钥，保护安全性
5. **审计日志**: 记录所有操作，便于安全审计

## 扩展功能

1. **批量操作**: 支持批量启用/停用/删除
2. **使用统计**: 统计API密钥的使用频率和调用次数
3. **告警机制**: 密钥即将过期时发送告警通知
4. **限流控制**: 支持API调用频率限制
5. **黑白名单**: 支持IP白名单和黑名单管理

## 技术架构

- **Controller层**: 处理HTTP请求，参数验证和响应封装
- **Service层**: 业务逻辑处理，事务管理和权限控制
- **Mapper层**: 数据访问层，基于MyBatis-Plus
- **DTO/VO**: 数据传输对象和视图对象，支持参数验证
- **枚举类**: 状态枚举和常量定义
- **工具类**: 密钥生成、验证和脱敏处理

## 注意事项

1. API密钥创建后需要妥善保管，建议在安全环境下生成
2. 定期刷新API密钥，提高安全性
3. 合理设置过期时间，避免长期使用同一密钥
4. 及时清理不再使用的API密钥
5. 监控API密钥的使用情况，发现异常及时处理 
