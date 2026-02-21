# 成员管理模块 - 部署指南

## 📋 概述

本文档提供成员管理模块的完整部署指南，包括数据库迁移、配置更新和初始化步骤。

## ✅ 已完成的功能

### 后端功能
- ✅ 用户实体扩展（role, status, created_by, updated_by字段）
- ✅ 5个DTO类（CreateMemberRequest, UpdateMemberRequest, ChangePasswordRequest, ChangeStatusRequest, UserDTO）
- ✅ BCrypt密码加密服务
- ✅ 基于角色的权限控制（RBAC）
- ✅ 成员管理服务（增删改查）
- ✅ 成员管理API（7个端点）
- ✅ 个人资料管理API（3个端点）
- ✅ 默认超级管理员自动初始化
- ✅ 操作日志记录

### 核心文件
```
后端：
├── entity/User.java                    # 用户实体（已扩展）
├── dto/                                # DTO类
│   ├── CreateMemberRequest.java
│   ├── UpdateMemberRequest.java
│   ├── ChangePasswordRequest.java
│   ├── ChangeStatusRequest.java
│   └── UserDTO.java
├── service/
│   ├── PasswordService.java            # 密码加密服务
│   ├── PermissionService.java          # 权限控制服务
│   ├── MemberService.java              # 成员管理服务
│   ├── SystemInitService.java          # 系统初始化服务
│   └── OperationLogService.java        # 操作日志服务（已扩展）
├── controller/
│   ├── MemberController.java           # 成员管理API
│   └── ProfileController.java          # 个人资料API
└── config/
    ├── SecurityConfig.java             # Security配置（BCrypt）
    └── MemberManagementConfig.java     # 成员管理配置

数据库：
└── database-migration-member-management.sql  # 数据库迁移脚本
```

## 🚀 部署步骤

### 1. 数据库迁移

#### 1.1 备份现有数据（重要！）
```bash
# 备份整个数据库
mysqldump -u root -p meituan_product > backup_$(date +%Y%m%d_%H%M%S).sql
```

#### 1.2 执行迁移脚本
```bash
# 方式1：命令行执行
mysql -u root -p meituan_product < database-migration-member-management.sql

# 方式2：MySQL客户端执行
mysql -u root -p
USE meituan_product;
SOURCE database-migration-member-management.sql;
```

#### 1.3 验证迁移结果
```sql
-- 检查user表结构
SHOW CREATE TABLE user;

-- 检查字段是否添加成功
DESC user;

-- 检查索引
SHOW INDEX FROM user;

-- 检查是否创建了默认超级管理员
SELECT id, username, role, status, created_at, created_by 
FROM user 
WHERE role = 'SUPER_ADMIN';
```

### 2. 配置更新

#### 2.1 application.yml配置
配置文件已更新，包含以下新配置：

```yaml
# 成员管理配置
member-management:
  default-admin:
    username: admin                    # 默认管理员用户名
    password: Admin@123456             # 默认管理员密码
    real-name: 系统管理员
    email: admin@example.com
  password:
    min-length: 8                      # 密码最小长度
    bcrypt-strength: 10                # BCrypt加密强度
```

#### 2.2 修改默认密码（生产环境必须）
```yaml
# 生产环境建议修改为强密码
member-management:
  default-admin:
    password: YourStrongPassword@2024
```

### 3. 启动应用

#### 3.1 编译项目
```bash
cd meituan-backend
mvn clean package -DskipTests
```

#### 3.2 启动后端服务
```bash
# 开发环境
mvn spring-boot:run

# 生产环境
java -jar target/meituan-product-upload-1.0.0.jar
```

#### 3.3 检查启动日志
查看日志确认初始化成功：
```
开始系统初始化...
检查默认超级管理员...
默认超级管理员创建成功: username=admin, id=1
⚠️ 请在首次登录后立即修改默认密码！
系统初始化完成
```

### 4. 验证部署

#### 4.1 测试API端点

**获取成员列表**
```bash
curl -X GET "http://localhost:8080/api/members?page=1&size=10"
```

**获取个人信息**
```bash
curl -X GET "http://localhost:8080/api/profile"
```

**创建成员**
```bash
curl -X POST "http://localhost:8080/api/members" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test@123456",
    "role": "USER",
    "realName": "测试用户",
    "email": "test@example.com"
  }'
```

#### 4.2 验证权限控制
- 超级管理员可以查看所有用户
- 管理员只能查看普通用户
- 普通用户只能查看自己

#### 4.3 验证密码加密
```sql
-- 检查密码是否为BCrypt格式（以$2a$, $2b$或$2y$开头）
SELECT id, username, password 
FROM user 
LIMIT 1;
```

## 🔐 安全建议

### 1. 首次登录后立即修改密码
```bash
# 使用默认账号登录后，立即修改密码
curl -X PUT "http://localhost:8080/api/profile/password" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "Admin@123456",
    "newPassword": "YourNewStrongPassword@2024"
  }'
```

### 2. 密码策略
- 最小长度：8个字符
- 建议包含：大小写字母、数字、特殊字符
- 定期更换密码

### 3. 权限管理
- 遵循最小权限原则
- 定期审查用户权限
- 及时禁用或删除不再使用的账号

### 4. 操作审计
- 所有成员管理操作都会记录日志
- 定期检查操作日志
- 关注异常操作

## 📊 API文档

### 成员管理API

| 方法 | 端点 | 描述 | 权限 |
|------|------|------|------|
| GET | /api/members | 获取成员列表 | ADMIN, SUPER_ADMIN |
| GET | /api/members/{id} | 获取成员详情 | ADMIN, SUPER_ADMIN |
| POST | /api/members | 创建成员 | SUPER_ADMIN |
| PUT | /api/members/{id} | 更新成员信息 | ADMIN, SUPER_ADMIN |
| DELETE | /api/members/{id} | 删除成员 | ADMIN, SUPER_ADMIN |
| PUT | /api/members/{id}/password | 修改成员密码 | SUPER_ADMIN |
| PUT | /api/members/{id}/status | 修改成员状态 | SUPER_ADMIN |

### 个人资料API

| 方法 | 端点 | 描述 | 权限 |
|------|------|------|------|
| GET | /api/profile | 获取当前用户信息 | ALL |
| PUT | /api/profile | 更新个人信息 | ALL |
| PUT | /api/profile/password | 修改个人密码 | ALL |

## 🔧 故障排查

### 问题1：默认管理员未创建
**症状**：启动后无法使用admin账号登录

**解决方案**：
1. 检查数据库连接是否正常
2. 查看启动日志是否有错误
3. 手动执行SQL创建管理员：
```sql
INSERT INTO user (username, password, real_name, email, role, status, created_at, updated_at, created_by, updated_by)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 
        '系统管理员', 'admin@example.com', 'SUPER_ADMIN', 1, NOW(), NOW(), 'system', 'system');
```

### 问题2：密码验证失败
**症状**：修改密码时提示"当前密码错误"

**解决方案**：
1. 确认输入的密码正确
2. 检查密码是否为BCrypt格式
3. 查看日志中的详细错误信息

### 问题3：权限不足
**症状**：操作时提示"权限不足"

**解决方案**：
1. 检查当前用户的角色
2. 确认操作需要的权限级别
3. 联系超级管理员提升权限

## 📝 后续开发

### 待实现功能
- [ ] JWT认证集成
- [ ] 前端成员管理页面
- [ ] 前端个人设置页面
- [ ] 操作日志查询页面
- [ ] 密码强度策略配置
- [ ] 用户活动监控

### 技术债务
- [ ] 完善单元测试
- [ ] 添加集成测试
- [ ] 实现真实的认证上下文
- [ ] 添加API文档（Swagger）

## 📞 支持

如有问题，请查看：
1. 启动日志：`logs/application.log`
2. 数据库日志：检查MySQL错误日志
3. API响应：查看返回的错误信息

## 📄 相关文档

- [需求文档](.kiro/specs/member-management/requirements.md)
- [设计文档](.kiro/specs/member-management/design.md)
- [任务列表](.kiro/specs/member-management/tasks.md)
- [数据库迁移脚本](database-migration-member-management.sql)
