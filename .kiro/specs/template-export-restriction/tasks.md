# 实施计划 - 模板导出限制

## 任务列表

- [x] 1. 创建异常类和DTO



  - 创建`TemplateNotFoundException`异常类
  - 创建`TemplateFileException`异常类
  - 创建`TemplateStatusDTO`数据传输对象



  - _Requirements: 1.2, 2.2, 5.1, 6.1_

- [x] 2. 实现模板状态查询功能



  - 在`TemplateService`中添加`getTemplateStatus()`方法
  - 在`TemplateService`中添加`findLatestMeituanTemplate()`辅助方法
  - 在`TemplateController`中添加`GET /api/template/status`接口
  - _Requirements: 6.1, 6.2, 6.3, 6.4_

- [x] 3. 修改ExcelService移除默认模板回退



  - 修改`generateMeituanTemplateFromUserTemplate()`方法
  - 移除所有`return generateMeituanTemplate(products)`回退调用
  - 在模板未找到时抛出`TemplateNotFoundException`



  - 在文件下载失败时抛出`TemplateFileException("模板文件丢失")`
  - 在文件读取失败时抛出`TemplateFileException("模板文件损坏")`



  - 添加详细的错误日志记录
  - _Requirements: 2.2, 2.3, 2.4, 5.1, 5.2, 5.3, 5.4_



- [x] 4. 更新全局异常处理器
  - 在`GlobalExceptionHandler`中添加`TemplateNotFoundException`处理方法
  - 在`GlobalExceptionHandler`中添加`TemplateFileException`处理方法
  - 确保返回HTTP 400状态码和明确的错误消息
  - _Requirements: 1.3, 4.1, 5.5_


- [x] 5. 添加数据库索引优化查询性能
  - 在`template`表上创建复合索引`idx_merchant_template_type_time`
  - 验证索引效果
  - _Requirements: 性能要求_

- [x] 6. 前端：添加模板状态API调用

  - 在`src/api/template.js`中添加`getTemplateStatus()`方法
  - 配置API请求路径和参数
  - _Requirements: 6.1_

- [x] 7. 前端：修改Upload.vue页面状态管理
  - 添加`loading`状态变量
  - 添加`templateStatus`状态变量
  - 在`onMounted`钩子中并行调用`fetchTemplateStatus()`和`fetchProductStats()`

  - 实现`fetchTemplateStatus()`方法调用模板状态API
  - 添加`isButtonDisabled`计算属性
  - 添加`tipText`计算属性
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 8.1, 8.2, 8.3, 8.4_

- [x] 8. 前端：添加loading骨架屏UI
  - 添加`.skeleton-button`样式组件


  - 添加shimmer动画效果
  - 在页面加载时显示骨架屏
  - 在API返回后隐藏骨架屏
  - _Requirements: 8.1, 8.5_

- [x] 9. 前端：更新按钮禁用逻辑和提示
  - 根据`isButtonDisabled`控制按钮禁用状态
  - 添加按钮title提示"请先上传美团模板"
  - 更新提示文本根据模板状态显示不同内容
  - 添加`.tip-box.warning`样式用于无模板警告
  - 添加"前往模板管理"链接
  - _Requirements: 3.4, 3.5, 3.6, 3.9_


- [x] 10. 前端：优化错误处理和用户引导
  - 在`handleGenerateTemplate()`中添加模板检查
  - 无模板时显示确认对话框，提供"前往模板管理"选项
  - 导出失败时检查错误类型，针对性提示
  - 模板被删除时刷新模板状态并引导用户
  - 添加API调用超时处理（>5秒）
  - _Requirements: 3.7, 3.8, 3.10, 4.1, 4.2, 4.3, 4.4, 8.6_

- [x] 11. 前端：添加错误提示样式
  - 添加`.tip-box.warning`警告样式
  - 添加`.tip-link`链接样式
  - 确保视觉层次清晰
  - _Requirements: 4.2, 4.3_

- [ ] 12. 后端：添加单元测试
  - 测试`TemplateService.getTemplateStatus()`有模板场景
  - 测试`TemplateService.getTemplateStatus()`无模板场景
  - 测试`ExcelService`无模板时抛出异常
  - 测试`ExcelService`文件不存在时抛出异常
  - 测试`ExcelService`文件损坏时抛出异常
  - 验证不再调用默认模板方法
  - _Requirements: 1.2, 2.2, 5.1, 5.2, 6.2, 6.3_

- [ ] 13. 后端：添加集成测试
  - 测试无模板时导出返回400错误
  - 测试有模板时导出成功
  - 测试模板状态API返回正确格式
  - 测试删除模板后状态更新
  - _Requirements: 1.2, 2.5, 6.1_

- [ ] 14. 前端：添加组件测试
  - 测试无模板时按钮禁用
  - 测试有模板时按钮启用
  - 测试页面加载时显示loading
  - 测试API失败时的错误处理
  - _Requirements: 3.4, 3.5, 8.1, 3.10_

- [ ] 15. 添加配置开关支持降级
  - 在`application.yml`中添加`meituan.template.strict-mode`配置
  - 在`ExcelService`中读取配置
  - 当`strict-mode=false`时恢复默认模板回退
  - 添加配置说明文档
  - _Requirements: 风险缓解_

- [ ] 16. 性能优化和监控
  - 验证数据库索引效果
  - 添加模板状态API响应时间日志
  - 添加导出操作性能监控
  - 前端并行请求优化验证
  - _Requirements: 性能要求, 6.7_

- [ ] 17. 编写部署文档
  - 编写数据库迁移脚本
  - 编写后端部署步骤
  - 编写前端部署步骤
  - 编写验证清单
  - 编写回滚方案
  - _Requirements: 部署和回滚_

- [ ] 18. 最终验收测试
  - 删除模板后验证无法导出
  - 上传模板后验证可以导出
  - 验证错误提示清晰明确
  - 验证页面loading体验
  - 验证"前往模板管理"链接正常
  - 验证多模板场景选择最新模板
  - 验证性能指标达标
  - _Requirements: 所有需求_

- [ ] 19. Checkpoint - 确保所有测试通过
  - 确保所有测试通过，如有问题请询问用户

## 任务说明

### 实施顺序
1. 先完成后端核心功能（任务1-5）
2. 再实现前端UI和交互（任务6-11）
3. 然后添加测试（任务12-14）
4. 最后优化和部署（任务15-18）

### 关键依赖
- 任务2依赖任务1（需要DTO）
- 任务3依赖任务1（需要异常类）
- 任务4依赖任务1（需要异常类）
- 任务7依赖任务6（需要API方法）
- 任务9依赖任务7（需要状态变量）
- 任务10依赖任务7（需要状态变量）

### 测试策略
- 任务12-14为测试任务，验证核心功能正确性
- 单元测试覆盖关键业务逻辑
- 集成测试验证API端到端流程
- 前端测试确保UI交互正确

### 可选任务
- 任务15（降级开关）为可选，用于风险缓解
- 如果系统稳定可以跳过

### 性能目标
- 模板状态查询 < 100ms
- 页面加载 < 2s
- 导出生成 < 30s（3000商品）
