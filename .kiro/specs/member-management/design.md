# Design Document - Member Management Module

## Overview

成员管理模块为美团商品管理系统提供完整的用户账号管理和基于角色的访问控制（RBAC）功能。该模块支持三种用户角色（超级管理员、管理员、普通用户），实现细粒度的权限控制，确保系统安全性和可管理性。

核心功能包括：
- 用户账号的增删改查操作
- 基于角色的权限验证
- 密码安全管理（BCrypt加密）
- 操作日志审计
- 初始化默认超级管理员

## Architecture

### 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                        Frontend Layer                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Member Mgmt  │  │ Profile Page │  │ Login/Auth   │      │
│  │    Page      │  │              │  │   Component  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ HTTP/REST API
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                       Backend Layer                          │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Security Filter Chain                    │   │
│  │  (JWT Authentication + Role-Based Authorization)     │   │
│  └──────────────────────────────────────────────────────┘   │
│                            │                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Member     │  │     Auth     │  │     Log      │      │
│  │  Controller  │  │  Controller  │  │  Controller  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         │                  │                  │              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Member     │  │     Auth     │  │     Log      │      │
│  │   Service    │  │   Service    │  │   Service    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         │                  │                  │              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │    User      │  │   Password   │  │ Operation    │      │
│  │   Mapper     │  │   Encoder    │  │ Log Mapper   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      Database Layer                          │
│  ┌──────────────┐  ┌──────────────┐                         │
│  │  user table  │  │operation_log │                         │
│  │              │  │    table     │                         │
│  └──────────────┘  └──────────────┘                         │
└─────────────────────────────────────────────────────────────┘
```

### 技术栈

- **Backend**: Spring Boot, Spring Security, MyBatis Plus
- **Frontend**: Vue 3, Element Plus
- **Database**: MySQL
- **Security**: JWT, BCrypt
- **API**: RESTful

## Components and Interfaces

### Backend Components

#### 1. User Entity (扩展现有)
```java
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;  // BCrypt encrypted
    private String role;      // SUPER_ADMIN, ADMIN, USER
    private Integer status;   // 0: disabled, 1: active
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
```

#### 2. MemberController
```java
@RestController
@RequestMapping("/api/members")
public class MemberController {
    // 查询成员列表（带权限过滤）
    @GetMapping
    ApiResponse<Page<UserDTO>> listMembers(
        @RequestParam(required = false) String username,
        @RequestParam(required = false) String role,
        @RequestParam(required = false) Integer status,
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "10") Integer size
    );
    
    // 创建成员（仅超级管理员）
    @PostMapping
    ApiResponse<UserDTO> createMember(@RequestBody CreateMemberRequest request);
    
    // 更新成员信息
    @PutMapping("/{id}")
    ApiResponse<UserDTO> updateMember(
        @PathVariable Long id,
        @RequestBody UpdateMemberRequest request
    );
    
    // 删除成员
    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteMember(@PathVariable Long id);
    
    // 修改成员密码
    @PutMapping("/{id}/password")
    ApiResponse<Void> changePassword(
        @PathVariable Long id,
        @RequestBody ChangePasswordRequest request
    );
    
    // 修改成员状态（启用/禁用）
    @PutMapping("/{id}/status")
    ApiResponse<Void> changeStatus(
        @PathVariable Long id,
        @RequestBody ChangeStatusRequest request
    );
}
```

#### 3. MemberService
```java
@Service
public class MemberService {
    Page<User> listMembers(String username, String role, Integer status, 
                          Integer page, Integer size, User currentUser);
    User createMember(CreateMemberRequest request, User currentUser);
    User updateMember(Long id, UpdateMemberRequest request, User currentUser);
    void deleteMember(Long id, User currentUser);
    void changePassword(Long id, String newPassword, User currentUser);
    void changeStatus(Long id, Integer status, User currentUser);
    void initDefaultSuperAdmin();
}
```

#### 4. PermissionService
```java
@Service
public class PermissionService {
    boolean canViewMember(User currentUser, User targetUser);
    boolean canEditMember(User currentUser, User targetUser);
    boolean canDeleteMember(User currentUser, User targetUser);
    boolean canChangeRole(User currentUser, User targetUser);
    List<User> filterVisibleMembers(List<User> members, User currentUser);
}
```

#### 5. Security Configuration
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // JWT filter for authentication
    // Role-based authorization rules
    // Password encoder (BCrypt)
}
```

### Frontend Components

#### 1. MemberManagement.vue
成员管理主页面，包含：
- 成员列表表格（支持筛选、分页）
- 创建成员按钮和对话框
- 编辑成员对话框
- 删除确认对话框
- 权限控制（根据当前用户角色显示不同操作）

#### 2. MemberForm.vue
成员创建/编辑表单组件：
- 用户名输入
- 密码输入（创建时必填，编辑时可选）
- 角色选择（根据权限显示可选角色）
- 状态切换

#### 3. ProfileSettings.vue
个人设置页面：
- 显示当前用户信息
- 修改密码功能（需要验证当前密码）

### API Endpoints

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| GET | /api/members | 获取成员列表 | ADMIN, SUPER_ADMIN |
| POST | /api/members | 创建成员 | SUPER_ADMIN |
| GET | /api/members/{id} | 获取成员详情 | ADMIN, SUPER_ADMIN |
| PUT | /api/members/{id} | 更新成员信息 | ADMIN, SUPER_ADMIN |
| DELETE | /api/members/{id} | 删除成员 | ADMIN, SUPER_ADMIN |
| PUT | /api/members/{id}/password | 修改密码 | SUPER_ADMIN |
| PUT | /api/members/{id}/status | 修改状态 | SUPER_ADMIN |
| GET | /api/profile | 获取当前用户信息 | USER, ADMIN, SUPER_ADMIN |
| PUT | /api/profile | 更新个人信息 | USER, ADMIN, SUPER_ADMIN |
| PUT | /api/profile/password | 修改个人密码 | USER, ADMIN, SUPER_ADMIN |

## Data Models

### User Table Schema
```sql
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt encrypted',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'SUPER_ADMIN, ADMIN, USER',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0: disabled, 1: active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Operation Log Enhancement
扩展现有的 operation_log 表以支持成员管理操作：
```sql
-- 新增操作类型
-- MEMBER_CREATE, MEMBER_UPDATE, MEMBER_DELETE, 
-- MEMBER_PASSWORD_CHANGE, MEMBER_STATUS_CHANGE
```

### DTOs

#### CreateMemberRequest
```java
public class CreateMemberRequest {
    @NotBlank
    private String username;
    
    @NotBlank
    @Size(min = 8)
    private String password;
    
    @NotBlank
    private String role;  // SUPER_ADMIN, ADMIN, USER
}
```

#### UpdateMemberRequest
```java
public class UpdateMemberRequest {
    private String username;
    private String role;
    private Integer status;
}
```

#### ChangePasswordRequest
```java
public class ChangePasswordRequest {
    private String currentPassword;  // Required for self password change
    
    @NotBlank
    @Size(min = 8)
    private String newPassword;
}
```

#### UserDTO
```java
public class UserDTO {
    private Long id;
    private String username;
    private String role;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    // password field excluded for security
}
```

## 
Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Member list contains required fields
*For any* member returned in the list API, the response should include username, role, createdAt, and status fields
**Validates: Requirements 1.1**

### Property 2: Filter returns matching members only
*For any* filter criteria (username, role, or status), all members returned by the list API should match the specified filter condition
**Validates: Requirements 1.2**

### Property 3: Pagination respects size parameter
*For any* valid page size parameter, the number of members returned should not exceed the specified page size
**Validates: Requirements 1.3**

### Property 4: Username uniqueness validation
*For any* create member request, if the username already exists in the database, the system should reject the request with an appropriate error
**Validates: Requirements 2.2**

### Property 5: Member creation persistence
*For any* valid member creation request, after successful creation, querying the database should return a member with the same username and role
**Validates: Requirements 2.4**

### Property 6: Member details completeness
*For any* existing member, retrieving their details should return all fields including username, role, and configuration items
**Validates: Requirements 3.1**

### Property 7: Role update effectiveness
*For any* member, after updating their role, subsequent queries should reflect the new role value
**Validates: Requirements 3.2**

### Property 8: Password update with validation
*For any* password change request with a valid new password (≥8 characters), the password should be updated and stored in BCrypt format
**Validates: Requirements 3.3**

### Property 9: Configuration update persistence
*For any* member configuration update, subsequent queries should return the updated configuration values
**Validates: Requirements 3.4**

### Property 10: Operation logging completeness
*For any* member management operation (create, update, delete), the operation log should contain operationType, operator, target, timestamp, and result
**Validates: Requirements 3.5, 8.1**

### Property 11: Member deletion removes data
*For any* member, after successful deletion, querying the database for that member should return no results
**Validates: Requirements 4.2**

### Property 12: Status change effectiveness
*For any* member, after changing their status to disabled, the status field should be updated and login attempts should be rejected
**Validates: Requirements 4.3, 4.4**

### Property 13: Admin sees only regular users
*For any* member list query by an admin user, the returned list should contain only members with role "USER" and exclude "ADMIN" and "SUPER_ADMIN" roles
**Validates: Requirements 5.1**

### Property 14: Admin cannot modify roles
*For any* update request from an admin user, if the request attempts to change a member's role, the system should reject the request with a permission error
**Validates: Requirements 5.2**

### Property 15: Admin can delete regular users
*For any* regular user, an admin should be able to successfully delete them and the operation should be logged
**Validates: Requirements 5.3**

### Property 16: Admin cannot access higher privilege users
*For any* member with role "ADMIN" or "SUPER_ADMIN", when an admin user attempts to access their information, the system should return a 403 permission error
**Validates: Requirements 5.4**

### Property 17: Profile query returns current user data
*For any* authenticated user, querying their profile should return their own user information with correct username and role
**Validates: Requirements 6.1**

### Property 18: Self password change requires current password
*For any* regular user attempting to change their own password, the request must include the current password for verification
**Validates: Requirements 6.2**

### Property 19: Regular user cannot access member management
*For any* regular user, attempting to access the member management endpoints should return a 403 permission error
**Validates: Requirements 6.4**

### Property 20: Regular user cannot change own role
*For any* regular user, attempting to update their own role should be rejected with a permission error
**Validates: Requirements 6.5**

### Property 21: Login loads role information
*For any* successful login, the session or JWT token should contain the user's role information
**Validates: Requirements 7.1**

### Property 22: Protected endpoints verify permissions
*For any* protected endpoint access, the system should verify the user's role matches the required permission level
**Validates: Requirements 7.2**

### Property 23: Insufficient permission returns 403
*For any* request where the user's role does not meet the endpoint's requirement, the system should return HTTP 403 status
**Validates: Requirements 7.3**

### Property 24: Session expiry clears permissions
*For any* expired session, subsequent requests should fail authentication and require re-login
**Validates: Requirements 7.4**

### Property 25: Role change applies on next request
*For any* user whose role has been modified, their next authenticated request should use the updated role for permission checks
**Validates: Requirements 7.5**

### Property 26: Log query returns all operations
*For any* time period with member management operations, querying the operation log should return all operations that occurred in that period
**Validates: Requirements 8.2**

### Property 27: Log filtering works correctly
*For any* log filter criteria (operation type, operator, or time range), all returned log entries should match the specified criteria
**Validates: Requirements 8.3**

### Property 28: Failed operations are logged
*For any* failed member management operation, the operation log should contain an entry with the failure reason and error message
**Validates: Requirements 8.4**

### Property 29: Passwords stored in BCrypt format
*For any* user in the database, the password field should be a valid BCrypt hash (starting with "$2a$", "$2b$", or "$2y$")
**Validates: Requirements 9.1**

### Property 30: BCrypt verification works correctly
*For any* user, verifying the correct password should succeed and verifying an incorrect password should fail
**Validates: Requirements 9.2**

### Property 31: Plain text passwords not in logs or database
*For any* password-related operation, searching logs and database records should not reveal any plain text passwords
**Validates: Requirements 9.3**

### Property 32: Default admin uses configured credentials
*For any* system initialization with empty user table, the created default super admin should have the username and password specified in the configuration file
**Validates: Requirements 10.2**

## Error Handling

### Error Categories

#### 1. Validation Errors (400 Bad Request)
- 用户名为空或格式不正确
- 密码长度不足（< 8字符）
- 角色值无效（不是 SUPER_ADMIN, ADMIN, USER 之一）
- 必填字段缺失

#### 2. Authentication Errors (401 Unauthorized)
- JWT token 无效或过期
- 登录凭证错误
- 会话已过期

#### 3. Authorization Errors (403 Forbidden)
- 权限不足，无法访问资源
- 尝试修改不允许修改的字段（如管理员修改角色）
- 尝试删除自己的账号
- 普通用户访问管理功能

#### 4. Resource Errors (404 Not Found)
- 用户ID不存在
- 请求的资源未找到

#### 5. Conflict Errors (409 Conflict)
- 用户名已存在
- 尝试创建重复的资源

#### 6. Server Errors (500 Internal Server Error)
- 数据库连接失败
- 密码加密失败
- 未预期的系统错误

### Error Response Format
```json
{
    "success": false,
    "message": "错误描述信息",
    "code": "ERROR_CODE",
    "timestamp": "2024-01-01T12:00:00Z"
}
```

### Error Handling Strategy

1. **输入验证**: 在 Controller 层使用 @Valid 注解进行参数验证
2. **业务验证**: 在 Service 层进行业务逻辑验证（如用户名唯一性）
3. **权限验证**: 在 Security Filter 和 Service 层进行权限检查
4. **异常捕获**: 使用 GlobalExceptionHandler 统一处理异常
5. **日志记录**: 所有错误都应记录到日志系统，包含上下文信息
6. **用户友好**: 返回给前端的错误信息应该清晰、可操作

## Testing Strategy

### Unit Testing

使用 JUnit 5 和 Mockito 进行单元测试，覆盖以下场景：

#### Backend Unit Tests
- **MemberService**: 测试业务逻辑方法
  - 创建成员时的验证逻辑
  - 权限检查逻辑
  - 密码加密和验证
  
- **PermissionService**: 测试权限判断方法
  - 不同角色的权限边界
  - 特殊情况（如自我删除保护）

- **UserMapper**: 测试数据访问层
  - CRUD 操作
  - 查询过滤和分页

#### Frontend Unit Tests
- **MemberManagement.vue**: 测试组件行为
  - 列表渲染
  - 筛选和分页交互
  - 按钮权限显示

- **MemberForm.vue**: 测试表单验证
  - 必填字段验证
  - 密码强度验证
  - 角色选择逻辑

### Property-Based Testing

使用 JUnit-Quickcheck 进行基于属性的测试，验证系统的通用正确性属性。

#### Configuration
```xml
<dependency>
    <groupId>com.pholser</groupId>
    <artifactId>junit-quickcheck-core</artifactId>
    <version>1.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.pholser</groupId>
    <artifactId>junit-quickcheck-generators</artifactId>
    <version>1.0</version>
    <scope>test</scope>
</dependency>
```

#### Property Test Requirements
- 每个属性测试应运行至少 100 次迭代
- 每个测试必须使用注释标记对应的设计文档属性
- 注释格式: `// Feature: member-management, Property {number}: {property_text}`
- 每个正确性属性应该由一个单独的属性测试实现

#### Property Test Examples

```java
// Feature: member-management, Property 1: Member list contains required fields
@Property(trials = 100)
public void memberListContainsRequiredFields(@From(UserGenerator.class) List<User> users) {
    // Setup: save users to database
    // Execute: query member list
    // Verify: each member has username, role, createdAt, status
}

// Feature: member-management, Property 2: Filter returns matching members only
@Property(trials = 100)
public void filterReturnsMatchingMembersOnly(
    @From(UserGenerator.class) List<User> users,
    @From(RoleGenerator.class) String role) {
    // Setup: save users to database
    // Execute: query with role filter
    // Verify: all returned members have the specified role
}

// Feature: member-management, Property 29: Passwords stored in BCrypt format
@Property(trials = 100)
public void passwordsStoredInBCryptFormat(
    @From(UsernameGenerator.class) String username,
    @From(PasswordGenerator.class) String password) {
    // Execute: create user with password
    // Verify: password in database matches BCrypt pattern
}
```

### Integration Testing

测试完整的请求-响应流程：

- **API Integration Tests**: 使用 MockMvc 测试完整的 HTTP 请求流程
  - 认证和授权流程
  - 端到端的 CRUD 操作
  - 错误处理和响应格式

- **Database Integration Tests**: 使用 H2 内存数据库测试数据持久化
  - 事务管理
  - 数据一致性
  - 并发操作

### Test Data Generators

为属性测试创建自定义生成器：

```java
public class UserGenerator extends Generator<User> {
    @Override
    public User generate(SourceOfRandomness random, GenerationStatus status) {
        User user = new User();
        user.setUsername(generateUsername(random));
        user.setPassword(generatePassword(random));
        user.setRole(generateRole(random));
        user.setStatus(random.nextInt(0, 2));
        return user;
    }
}

public class RoleGenerator extends Generator<String> {
    private static final String[] ROLES = {"SUPER_ADMIN", "ADMIN", "USER"};
    
    @Override
    public String generate(SourceOfRandomness random, GenerationStatus status) {
        return random.choose(Arrays.asList(ROLES));
    }
}
```

### Test Coverage Goals

- 单元测试代码覆盖率: ≥ 80%
- 属性测试覆盖所有正确性属性: 100%
- 集成测试覆盖所有 API 端点: 100%
- 关键业务逻辑分支覆盖率: ≥ 90%

## Security Considerations

### Authentication
- 使用 JWT (JSON Web Token) 进行无状态认证
- Token 有效期: 24小时
- Refresh token 机制用于延长会话

### Authorization
- 基于角色的访问控制 (RBAC)
- 三级权限: SUPER_ADMIN > ADMIN > USER
- 方法级权限注解: @PreAuthorize

### Password Security
- BCrypt 加密算法 (strength = 10)
- 密码最小长度: 8 字符
- 强制首次登录修改默认密码
- 密码不在日志或响应中暴露

### API Security
- CORS 配置限制允许的来源
- CSRF 保护（对于非 JWT 的会话）
- Rate limiting 防止暴力破解
- SQL 注入防护（使用 MyBatis 参数化查询）

### Audit Trail
- 所有敏感操作记录日志
- 日志包含: 操作者、操作类型、目标、时间戳、结果
- 日志不可篡改，仅追加

## Performance Considerations

### Database Optimization
- 在 username, role, status 字段上创建索引
- 使用分页查询避免大量数据加载
- 连接池配置优化

### Caching Strategy
- 用户角色信息缓存（Redis）
- 缓存失效策略: 角色修改时清除对应缓存
- TTL: 1小时

### API Response Time Targets
- 列表查询: < 200ms
- 单个用户操作: < 100ms
- 批量操作: < 500ms

## Deployment Considerations

### Database Migration
```sql
-- Migration script for member management
ALTER TABLE user ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';
ALTER TABLE user ADD COLUMN status TINYINT NOT NULL DEFAULT 1;
ALTER TABLE user ADD COLUMN created_by VARCHAR(50);
ALTER TABLE user ADD COLUMN updated_by VARCHAR(50);

CREATE INDEX idx_role ON user(role);
CREATE INDEX idx_status ON user(status);
```

### Configuration
```yaml
# application.yml
member-management:
  default-admin:
    username: admin
    password: Admin@123456
  password:
    min-length: 8
    bcrypt-strength: 10
  jwt:
    secret: ${JWT_SECRET}
    expiration: 86400000  # 24 hours
```

### Initial Setup
1. 运行数据库迁移脚本
2. 配置默认超级管理员凭证
3. 启动应用，自动创建默认管理员
4. 首次登录后修改默认密码

## Future Enhancements

### Phase 2 Features
- 细粒度权限控制（功能级权限）
- 用户组和团队管理
- 多因素认证 (MFA)
- 密码策略配置（复杂度要求、过期时间）
- 用户活动监控和异常检测

### Scalability
- 支持 LDAP/AD 集成
- SSO (Single Sign-On) 支持
- 分布式会话管理
- 审计日志归档策略
