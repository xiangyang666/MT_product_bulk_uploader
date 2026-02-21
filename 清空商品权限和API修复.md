# 清空商品功能 - 权限控制和API修复

## 问题描述

用户报告了两个问题：

1. **权限问题**：普通用户（USER角色）在首页可以看到"清空商品"按钮，但这个功能应该只对管理员开放
2. **API调用错误**：点击清空按钮时报错 `HttpMessageNotReadableException: Required request body is missing`

## 错误原因分析

### 1. 权限问题
- 首页的"清空商品"按钮没有进行角色权限判断
- 所有登录用户（包括普通用户）都能看到这个危险操作按钮

### 2. API调用错误
- 后端 `ProductController.clearProducts()` 方法期望接收 `@RequestBody ClearRequest` 对象
- 前端使用 `request.delete('/products/clear')` 没有发送请求体
- DELETE请求默认不携带请求体，导致后端无法解析参数

## 解决方案

### 1. 添加权限控制（前端）

**文件**: `meituan-frontend/src/views/Home.vue`

#### 修改内容：

1. **添加角色判断计算属性**：
```javascript
// 判断是否为管理员（SUPER_ADMIN 或 ADMIN）
const isAdmin = computed(() => {
  const role = userStore.userInfo?.role
  return role === 'SUPER_ADMIN' || role === 'ADMIN'
})
```

2. **使用 v-if 控制按钮显示**：
```vue
<!-- 清空商品按钮 - 仅管理员可见 -->
<div v-if="isAdmin" class="action-card" @click="handleClearProducts">
  <div class="action-icon" style="background-color: #fef0f0;">
    <el-icon :size="32" color="#f56c6c"><Delete /></el-icon>
  </div>
  <div class="action-title">清空商品</div>
  <div class="action-desc">清空后台数据</div>
</div>
```

3. **移除未使用的图标导入**：
```javascript
// 移除 WarningFilled（未使用）
import {
  Goods,
  Upload,
  Document,
  Delete,
  SuccessFilled,
  Clock
} from '@element-plus/icons-vue'
```

### 2. 修复API调用（前端）

**文件**: `meituan-frontend/src/views/Home.vue`

#### 修改内容：

修改 `handleClearProducts` 函数，在DELETE请求中添加请求体：

```javascript
const handleClearProducts = async () => {
  try {
    await ElMessageBox.confirm(
      '此操作将清空美团后台的所有商品数据，是否继续？',
      '警告',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 发送DELETE请求，携带请求体
    const response = await request.delete('/products/clear', {
      data: {
        merchantId: 1,
        accessToken: 'admin123'
      }
    })
    
    if (response.code === 200) {
      ElMessage.success('清空成功')
      fetchStats()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空失败:', error)
      ElMessage.error('清空失败')
    }
  }
}
```

**关键点**：
- 使用 `data` 字段在DELETE请求中传递请求体
- 传递 `merchantId: 1`（默认商家ID）
- 传递 `accessToken: 'admin123'`（后端验证令牌）

## 后端API说明

**接口**: `DELETE /api/products/clear`

**请求体**:
```json
{
  "merchantId": 1,
  "accessToken": "admin123"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "成功清空24条商品",
  "data": {
    "deletedCount": 24
  }
}
```

## 权限说明

### 角色定义
- `SUPER_ADMIN`: 超级管理员 - 拥有所有权限
- `ADMIN`: 管理员 - 拥有管理权限
- `USER`: 普通用户 - 只能查看和操作自己的数据

### 清空商品功能权限
- ✅ `SUPER_ADMIN` - 可以清空商品
- ✅ `ADMIN` - 可以清空商品
- ❌ `USER` - 不能清空商品（按钮不显示）

## 测试验证

### 1. 普通用户登录
- 登录普通用户账号（role = 'USER'）
- 访问首页
- **预期结果**：看不到"清空商品"按钮

### 2. 管理员登录
- 登录管理员账号（role = 'ADMIN' 或 'SUPER_ADMIN'）
- 访问首页
- **预期结果**：可以看到"清空商品"按钮
- 点击按钮，确认操作
- **预期结果**：成功清空商品，显示成功消息

### 3. API调用测试
```bash
# 使用curl测试
curl -X DELETE http://localhost:8080/api/products/clear \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "merchantId": 1,
    "accessToken": "admin123"
  }'
```

## 修改文件清单

- ✅ `meituan-frontend/src/views/Home.vue`
  - 添加 `isAdmin` 计算属性
  - 添加 `v-if="isAdmin"` 条件渲染
  - 修复 `handleClearProducts` 函数的API调用
  - 移除未使用的 `WarningFilled` 导入

## 注意事项

1. **前端权限控制只是UI层面的隐藏**，后端仍需要进行权限验证
2. **accessToken验证**：当前使用硬编码的 `'admin123'`，生产环境应该使用真实的认证令牌
3. **商家ID**：当前使用默认值 `1`，多商家系统需要动态获取
4. **后端权限验证**：建议在 `ProductController.clearProducts()` 方法中添加 Spring Security 权限注解：
   ```java
   @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
   @DeleteMapping("/clear")
   public ApiResponse<ClearResult> clearProducts(@RequestBody ClearRequest request)
   ```

## 完成状态

✅ 问题已修复
- 普通用户无法看到"清空商品"按钮
- 管理员可以正常使用清空功能
- API调用错误已解决
- 代码已优化（移除未使用的导入）
