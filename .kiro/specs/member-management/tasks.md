# Implementation Plan - Member Management Module

- [x] 1. 数据库迁移和初始化


  - 扩展 user 表，添加 role、status、created_by、updated_by 字段
  - 创建必要的索引（username, role, status）
  - 扩展 operation_log 表支持成员管理操作类型
  - _Requirements: 9.1, 10.1_





- [ ] 2. 后端核心实体和 DTO
  - [x] 2.1 扩展 User 实体类


    - 添加 role、status、createdBy、updatedBy 字段
    - 配置 MyBatis Plus 注解
    - _Requirements: 7.1, 9.1_
  
  - [ ] 2.2 创建成员管理相关 DTO
    - 实现 CreateMemberRequest（包含用户名、密码、角色验证）
    - 实现 UpdateMemberRequest
    - 实现 ChangePasswordRequest
    - 实现 ChangeStatusRequest




    - 实现 UserDTO（排除密码字段）
    - _Requirements: 2.2, 3.3_


  
  - [ ]* 2.3 编写 Property 测试：Member creation persistence
    - **Property 5: Member creation persistence**
    - **Validates: Requirements 2.4**

- [ ] 3. 密码安全和加密服务
  - [ ] 3.1 配置 BCrypt 密码编码器
    - 在 SecurityConfig 中配置 BCryptPasswordEncoder (strength=10)
    - _Requirements: 9.1_
  
  - [ ] 3.2 实现密码验证和加密逻辑
    - 创建密码加密方法
    - 创建密码验证方法
    - 确保密码最小长度验证（≥8字符）
    - _Requirements: 9.1, 9.2, 2.5_





  
  - [ ]* 3.3 编写 Property 测试：Passwords stored in BCrypt format
    - **Property 29: Passwords stored in BCrypt format**
    - **Validates: Requirements 9.1**
  
  - [ ]* 3.4 编写 Property 测试：BCrypt verification works correctly
    - **Property 30: BCrypt verification works correctly**
    - **Validates: Requirements 9.2**
  
  - [ ]* 3.5 编写 Property 测试：Plain text passwords not in logs or database
    - **Property 31: Plain text passwords not in logs or database**
    - **Validates: Requirements 9.3**

- [ ] 4. 权限服务实现
  - [ ] 4.1 创建 PermissionService
    - 实现 canViewMember 方法（检查查看权限）
    - 实现 canEditMember 方法（检查编辑权限）
    - 实现 canDeleteMember 方法（检查删除权限）
    - 实现 canChangeRole 方法（检查角色修改权限）
    - 实现 filterVisibleMembers 方法（根据角色过滤可见成员）
    - _Requirements: 5.1, 5.2, 5.4, 6.4, 6.5_
  
  - [ ]* 4.2 编写 Property 测试：Admin sees only regular users
    - **Property 13: Admin sees only regular users**




    - **Validates: Requirements 5.1**
  
  - [ ]* 4.3 编写 Property 测试：Admin cannot modify roles
    - **Property 14: Admin cannot modify roles**
    - **Validates: Requirements 5.2**
  
  - [ ]* 4.4 编写 Property 测试：Admin cannot access higher privilege users
    - **Property 16: Admin cannot access higher privilege users**
    - **Validates: Requirements 5.4**
  
  - [ ]* 4.5 编写 Property 测试：Regular user cannot access member management
    - **Property 19: Regular user cannot access member management**
    - **Validates: Requirements 6.4**
  
  - [ ]* 4.6 编写 Property 测试：Regular user cannot change own role
    - **Property 20: Regular user cannot change own role**
    - **Validates: Requirements 6.5**

- [ ] 5. 成员管理服务层
  - [ ] 5.1 创建 MemberService
    - 实现 listMembers 方法（支持筛选、分页、权限过滤）
    - 实现 createMember 方法（包含用户名唯一性验证、密码加密）
    - 实现 updateMember 方法（包含权限检查）
    - 实现 deleteMember 方法（包含自我删除保护）
    - 实现 changePassword 方法
    - 实现 changeStatus 方法
    - _Requirements: 1.1, 1.2, 1.3, 2.2, 2.4, 3.2, 3.3, 4.2, 4.3, 4.5_
  
  - [ ]* 5.2 编写 Property 测试：Member list contains required fields
    - **Property 1: Member list contains required fields**





    - **Validates: Requirements 1.1**
  
  - [ ]* 5.3 编写 Property 测试：Filter returns matching members only
    - **Property 2: Filter returns matching members only**
    - **Validates: Requirements 1.2**
  
  - [ ]* 5.4 编写 Property 测试：Pagination respects size parameter
    - **Property 3: Pagination respects size parameter**
    - **Validates: Requirements 1.3**
  
  - [ ]* 5.5 编写 Property 测试：Username uniqueness validation
    - **Property 4: Username uniqueness validation**
    - **Validates: Requirements 2.2**
  
  - [ ]* 5.6 编写 Property 测试：Member deletion removes data
    - **Property 11: Member deletion removes data**
    - **Validates: Requirements 4.2**
  
  - [x]* 5.7 编写 Property 测试：Status change effectiveness




    - **Property 12: Status change effectiveness**
    - **Validates: Requirements 4.3, 4.4**

- [ ] 6. 成员管理控制器
  - [ ] 6.1 创建 MemberController
    - 实现 GET /api/members（列表查询，支持筛选和分页）
    - 实现 POST /api/members（创建成员，仅超级管理员）
    - 实现 GET /api/members/{id}（获取成员详情）
    - 实现 PUT /api/members/{id}（更新成员信息）
    - 实现 DELETE /api/members/{id}（删除成员）
    - 实现 PUT /api/members/{id}/password（修改密码）
    - 实现 PUT /api/members/{id}/status（修改状态）
    - 添加参数验证注解（@Valid）
    - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 3.1, 3.3, 4.1, 4.2_
  
  - [ ]* 6.2 编写 Property 测试：Member details completeness
    - **Property 6: Member details completeness**
    - **Validates: Requirements 3.1**
  
  - [ ]* 6.3 编写 Property 测试：Role update effectiveness
    - **Property 7: Role update effectiveness**
    - **Validates: Requirements 3.2**
  
  - [ ]* 6.4 编写 Property 测试：Password update with validation
    - **Property 8: Password update with validation**
    - **Validates: Requirements 3.3**

- [ ] 7. 个人资料管理
  - [ ] 7.1 创建 ProfileController
    - 实现 GET /api/profile（获取当前用户信息）
    - 实现 PUT /api/profile（更新个人信息）
    - 实现 PUT /api/profile/password（修改个人密码，需验证当前密码）
    - _Requirements: 6.1, 6.2, 6.3_
  
  - [ ]* 7.2 编写 Property 测试：Profile query returns current user data
    - **Property 17: Profile query returns current user data**
    - **Validates: Requirements 6.1**
  
  - [ ]* 7.3 编写 Property 测试：Self password change requires current password
    - **Property 18: Self password change requires current password**
    - **Validates: Requirements 6.2**

- [ ] 8. 操作日志集成
  - [ ] 8.1 扩展 OperationLogService
    - 添加成员管理操作类型常量（MEMBER_CREATE, MEMBER_UPDATE, MEMBER_DELETE, MEMBER_PASSWORD_CHANGE, MEMBER_STATUS_CHANGE）
    - 在所有成员管理操作中记录日志
    - _Requirements: 3.5, 8.1, 8.4_
  
  - [ ]* 8.2 编写 Property 测试：Operation logging completeness
    - **Property 10: Operation logging completeness**
    - **Validates: Requirements 3.5, 8.1**
  
  - [ ]* 8.3 编写 Property 测试：Log query returns all operations
    - **Property 26: Log query returns all operations**
    - **Validates: Requirements 8.2**
  
  - [ ]* 8.4 编写 Property 测试：Log filtering works correctly
    - **Property 27: Log filtering works correctly**
    - **Validates: Requirements 8.3**
  
  - [ ]* 8.5 编写 Property 测试：Failed operations are logged
    - **Property 28: Failed operations are logged**
    - **Validates: Requirements 8.4**

- [ ] 9. Spring Security 配置和权限控制
  - [ ] 9.1 更新 SecurityConfig
    - 配置成员管理端点的角色权限（@PreAuthorize）
    - 配置 JWT 过滤器包含角色信息
    - 添加权限不足时的异常处理（返回403）




    - _Requirements: 7.1, 7.2, 7.3_
  
  - [ ] 9.2 更新 AuthService
    - 在登录成功后加载用户角色到 JWT token
    - 实现会话过期处理




    - _Requirements: 7.1, 7.4_
  
  - [ ]* 9.3 编写 Property 测试：Login loads role information
    - **Property 21: Login loads role information**
    - **Validates: Requirements 7.1**
  
  - [x]* 9.4 编写 Property 测试：Protected endpoints verify permissions

    - **Property 22: Protected endpoints verify permissions**
    - **Validates: Requirements 7.2**
  
  - [ ]* 9.5 编写 Property 测试：Insufficient permission returns 403
    - **Property 23: Insufficient permission returns 403**
    - **Validates: Requirements 7.3**
  
  - [x]* 9.6 编写 Property 测试：Session expiry clears permissions

    - **Property 24: Session expiry clears permissions**
    - **Validates: Requirements 7.4**
  




  - [ ]* 9.7 编写 Property 测试：Role change applies on next request
    - **Property 25: Role change applies on next request**
    - **Validates: Requirements 7.5**






- [ ] 10. 默认超级管理员初始化
  - [x] 10.1 实现系统初始化逻辑


    - 在 MemberService 中实现 initDefaultSuperAdmin 方法
    - 检查数据库是否为空
    - 从配置文件读取默认管理员凭证
    - 创建默认超级管理员账号
    - 在应用启动时调用初始化方法（使用 @PostConstruct 或 ApplicationRunner）
    - _Requirements: 10.1, 10.2, 10.4_
  
  - [ ]* 10.2 编写 Property 测试：Default admin uses configured credentials
    - **Property 32: Default admin uses configured credentials**
    - **Validates: Requirements 10.2**

- [ ] 11. 前端成员管理页面
  - [ ] 11.1 创建 MemberManagement.vue
    - 实现成员列表表格（显示用户名、角色、状态、创建时间）
    - 实现筛选功能（用户名、角色、状态）
    - 实现分页功能
    - 添加创建成员按钮（仅超级管理员可见）
    - 添加编辑、删除、启用/禁用按钮（根据权限显示）




    - 实现空状态提示
    - _Requirements: 1.1, 1.2, 1.3, 1.4_
  
  - [ ] 11.2 创建 MemberForm.vue 组件
    - 实现成员创建/编辑表单
    - 用户名输入（带唯一性验证）
    - 密码输入（创建时必填，编辑时可选，最小8字符）
    - 角色选择下拉框（根据当前用户权限显示可选角色）
    - 状态切换开关
    - 表单验证逻辑
    - _Requirements: 2.1, 2.2, 2.5, 3.1_
  
  - [ ] 11.3 实现删除确认对话框
    - 显示删除警告信息
    - 防止删除当前登录用户
    - _Requirements: 4.1, 4.5_

- [ ] 12. 前端个人设置页面
  - [ ] 12.1 创建或更新 ProfileSettings.vue
    - 显示当前用户信息（用户名、角色）
    - 实现修改密码功能（需要输入当前密码）
    - 密码强度验证
    - _Requirements: 6.1, 6.2, 6.3_

- [ ] 13. 前端路由和权限控制
  - [ ] 13.1 添加成员管理路由
    - 添加 /members 路由到 MemberManagement.vue
    - 配置路由守卫，仅管理员和超级管理员可访问
    - _Requirements: 6.4_




  
  - [ ] 13.2 更新导航菜单
    - 在侧边栏添加"成员管理"菜单项
    - 根据用户角色显示/隐藏菜单项
    - _Requirements: 6.4_

- [ ] 14. 前端 API 集成
  - [ ] 14.1 创建成员管理 API 模块
    - 实现 listMembers API 调用
    - 实现 createMember API 调用
    - 实现 updateMember API 调用
    - 实现 deleteMember API 调用
    - 实现 changePassword API 调用
    - 实现 changeStatus API 调用
    - 实现 getProfile API 调用
    - 实现 updateProfile API 调用
    - 统一错误处理和提示
    - _Requirements: 1.1, 2.2, 2.3, 3.2, 3.3, 4.2, 4.3, 4.4, 6.1, 6.2, 6.3_

- [ ] 15. 错误处理和用户反馈
  - [x] 15.1 实现全局错误处理




    - 在 GlobalExceptionHandler 中添加成员管理相关异常处理
    - 处理验证错误（400）
    - 处理认证错误（401）
    - 处理授权错误（403）
    - 处理资源不存在错误（404）


    - 处理冲突错误（409，如用户名重复）
    - 返回统一的错误响应格式
    - _Requirements: 2.3, 4.5, 5.4, 6.3, 6.4, 6.5, 7.3_
  
  - [ ] 15.2 前端错误提示优化
    - 实现友好的错误消息显示
    - 针对不同错误类型显示不同的提示
    - _Requirements: 2.3, 4.4, 6.3_

- [ ] 16. 配置文件更新
  - [ ] 16.1 更新 application.yml
    - 添加成员管理配置节
    - 配置默认超级管理员用户名和密码
    - 配置密码策略（最小长度、BCrypt强度）
    - 配置 JWT 相关参数
    - _Requirements: 10.2_

- [ ] 17. Checkpoint - 确保所有测试通过
  - 运行所有单元测试和属性测试
  - 确保所有测试通过，如有问题请询问用户

- [ ] 18. 集成测试和端到端测试
  - [ ]* 18.1 编写成员管理 API 集成测试
    - 测试完整的认证和授权流程
    - 测试 CRUD 操作的端到端流程
    - 测试权限边界情况
    - _Requirements: 7.2, 7.3_
  
  - [ ]* 18.2 编写前端组件集成测试
    - 测试 MemberManagement.vue 的交互流程
    - 测试 MemberForm.vue 的表单验证
    - 测试权限控制的 UI 表现
    - _Requirements: 1.1, 2.1, 6.4_

- [ ] 19. 文档和部署准备
  - [ ] 19.1 创建数据库迁移脚本
    - 编写完整的 SQL 迁移脚本
    - 包含表结构修改和索引创建
    - _Requirements: 9.1_
  
  - [ ] 19.2 编写部署文档
    - 记录配置步骤
    - 记录初始化流程
    - 记录首次登录和密码修改步骤
    - _Requirements: 10.1, 10.3_

- [ ] 20. 最终 Checkpoint - 完整功能验证
  - 确保所有测试通过，如有问题请询问用户
