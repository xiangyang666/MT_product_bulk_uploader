<template>
  <div class="member-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>成员管理</span>
          <div class="header-actions">
            <el-button 
              v-if="isAdminPlus" 
              type="warning" 
              @click="handleDevToolsSettings"
              style="margin-right: 10px;"
            >
              <el-icon><Tools /></el-icon>
              开发者工具设置
            </el-button>
            <el-button type="primary" @click="handleCreate" v-if="canCreate">
              <el-icon><Plus /></el-icon>
              创建成员
            </el-button>
          </div>
        </div>
      </template>

      <!-- 筛选条件 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="用户名">
          <el-input
            v-model="queryForm.username"
            placeholder="请输入用户名"
            clearable
            @clear="handleQuery"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="queryForm.role"
            placeholder="请选择角色"
            clearable
            @clear="handleQuery"
          >
            <el-option label="超级管理员" value="SUPER_ADMIN" />
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryForm.status"
            placeholder="请选择状态"
            clearable
            @clear="handleQuery"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 成员列表 -->
      <el-table
        :data="memberList"
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="真实姓名" min-width="120" />
        <el-table-column prop="role" label="角色" width="140">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">
              {{ getRoleLabel(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="300">
          <template #default="{ row }">
            <template v-if="canOperateUser(row.username)">
              <el-button
                type="primary"
                size="small"
                @click="handleEdit(row)"
                link
              >
                编辑
              </el-button>
              <el-button
                type="warning"
                size="small"
                @click="handleChangePassword(row)"
                link
                v-if="canChangePassword"
              >
                改密码
              </el-button>
              <el-button
                :type="row.status === 1 ? 'warning' : 'success'"
                size="small"
                @click="handleToggleStatus(row)"
                link
                v-if="canChangeStatus"
              >
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="handleDelete(row)"
                link
              >
                删除
              </el-button>
            </template>
            <span v-else style="color: #999;">无权限操作</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="formData.username"
            placeholder="请输入用户名"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="请输入密码（至少8个字符）"
            show-password
          />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="formData.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="超级管理员" value="SUPER_ADMIN" v-if="isSuperAdmin" />
            <el-option label="管理员" value="ADMIN" v-if="isSuperAdmin" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="状态" prop="status" v-if="isEdit">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="400px"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码（至少8个字符）"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePasswordSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 开发者工具设置对话框 -->
    <el-dialog
      v-model="devToolsDialogVisible"
      title="开发者工具设置"
      width="500px"
    >
      <el-alert
        title="设置开发者工具访问密码"
        type="info"
        :closable="false"
        style="margin-bottom: 20px;"
      >
        <template #default>
          <p style="margin: 0;">此密码用于控制开发者工具的访问权限。</p>
          <p style="margin: 5px 0 0 0;">设置后，用户需要输入正确的密码才能打开开发者工具。</p>
        </template>
      </el-alert>

      <el-form
        ref="devToolsFormRef"
        :model="devToolsForm"
        :rules="devToolsRules"
        label-width="120px"
      >
        <el-form-item label="新密码" prop="password">
          <el-input
            v-model="devToolsForm.password"
            type="password"
            placeholder="请输入密码（至少6个字符）"
            show-password
            clearable
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="devToolsForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
            clearable
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="devToolsDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDevToolsSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Tools } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  getMemberList,
  createMember,
  updateMember,
  deleteMember,
  changeMemberPassword,
  changeMemberStatus
} from '@/api/member'
import { 
  setDevToolsPassword, 
  getDevToolsPasswordStatus 
} from '@/api/devTools'
import { refreshPasswordStatus } from '@/utils/devToolsProtection'

// 用户 store
const userStore = useUserStore()

// 当前用户信息
const currentUsername = computed(() => userStore.userInfo?.username || '')
const currentUserRole = computed(() => userStore.userInfo?.role || '')

// 权限计算
const isAdminPlus = computed(() => currentUsername.value === 'admin-plus')
const isSuperAdmin = computed(() => currentUserRole.value === 'SUPER_ADMIN')
const canCreate = computed(() => isSuperAdmin.value)
const canChangePassword = computed(() => isSuperAdmin.value)
const canChangeStatus = computed(() => isSuperAdmin.value)

// 判断是否可以操作某个用户
const canOperateUser = (username) => {
  // admin-plus 可以操作所有人
  if (isAdminPlus.value) {
    return true
  }
  // 超级管理员可以操作除了 admin-plus 之外的所有人
  if (isSuperAdmin.value && username !== 'admin-plus') {
    return true
  }
  // 其他情况不能操作
  return false
}

// 查询表单
const queryForm = reactive({
  username: '',
  role: '',
  status: null,
  page: 1,
  size: 10
})

// 列表数据
const memberList = ref([])
const total = ref(0)
const loading = ref(false)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)
const submitting = ref(false)

// 表单数据
const formData = reactive({
  id: null,
  username: '',
  password: '',
  role: 'USER',
  realName: '',
  email: '',
  phone: '',
  status: 1
})

// 表单验证规则
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在3-50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码长度至少8个字符', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 密码对话框
const passwordDialogVisible = ref(false)
const passwordFormRef = ref(null)
const passwordForm = reactive({
  id: null,
  newPassword: '',
  confirmPassword: ''
})

// 自定义验证器：确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '密码长度至少8个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 开发者工具设置对话框
const devToolsDialogVisible = ref(false)
const devToolsFormRef = ref(null)
const devToolsForm = reactive({
  password: '',
  confirmPassword: ''
})

// 自定义验证器：开发者工具密码确认
const validateDevToolsConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== devToolsForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const devToolsRules = {
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度必须在6-50个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateDevToolsConfirmPassword, trigger: 'blur' }
  ]
}

// 获取角色标签
const getRoleLabel = (role) => {
  const roleMap = {
    'SUPER_ADMIN': '超级管理员',
    'ADMIN': '管理员',
    'USER': '普通用户'
  }
  return roleMap[role] || role
}

// 获取角色类型
const getRoleType = (role) => {
  const typeMap = {
    'SUPER_ADMIN': 'danger',
    'ADMIN': 'warning',
    'USER': 'info'
  }
  return typeMap[role] || 'info'
}

// 格式化日期 - 将 UTC 时间转换为本地时间显示（始终显示年份）
const formatDate = (dateString) => {
  if (!dateString) return ''
  
  // 解析 UTC 时间并转换为本地时间
  const date = new Date(dateString)
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  
  // 始终显示 年-月-日 时:分
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 查询成员列表
const loadMemberList = async () => {
  loading.value = true
  try {
    const res = await getMemberList(queryForm)
    memberList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('获取成员列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryForm.page = 1
  loadMemberList()
}

// 重置
const handleReset = () => {
  queryForm.username = ''
  queryForm.role = ''
  queryForm.status = null
  queryForm.page = 1
  loadMemberList()
}

// 创建成员
const handleCreate = () => {
  isEdit.value = false
  dialogTitle.value = '创建成员'
  Object.assign(formData, {
    id: null,
    username: '',
    password: '',
    role: 'USER',
    realName: '',
    email: '',
    phone: '',
    status: 1
  })
  dialogVisible.value = true
}

// 编辑成员
const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑成员'
  Object.assign(formData, {
    id: row.id,
    username: row.username,
    password: '',
    role: row.role,
    realName: row.realName || '',
    email: row.email || '',
    phone: row.phone || '',
    status: row.status
  })
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEdit.value) {
        await updateMember(formData.id, {
          username: formData.username,
          role: formData.role,
          realName: formData.realName,
          email: formData.email,
          phone: formData.phone,
          status: formData.status
        })
        ElMessage.success('更新成功')
        
        // 如果修改的是当前登录用户，刷新用户信息
        if (formData.id === userStore.userInfo.id) {
          await userStore.getUserInfo()
        }
      } else {
        await createMember(formData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadMemberList()
    } catch (error) {
      console.error('操作失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

// 对话框关闭
const handleDialogClose = () => {
  formRef.value?.resetFields()
}

// 修改密码
const handleChangePassword = (row) => {
  passwordForm.id = row.id
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordDialogVisible.value = true
}

// 提交密码修改
const handlePasswordSubmit = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      await changeMemberPassword(passwordForm.id, {
        newPassword: passwordForm.newPassword
      })
      ElMessage.success('密码修改成功')
      passwordDialogVisible.value = false
    } catch (error) {
      console.error('密码修改失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

// 切换状态
const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(
      `确定要${statusText}用户 ${row.username} 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await changeMemberStatus(row.id, { status: newStatus })
    ElMessage.success(`${statusText}成功`)
    loadMemberList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('状态修改失败:', error)
    }
  }
}

// 删除成员
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 ${row.username} 吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    await deleteMember(row.id)
    ElMessage.success('删除成功')
    loadMemberList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 开发者工具设置
const handleDevToolsSettings = () => {
  devToolsForm.password = ''
  devToolsForm.confirmPassword = ''
  devToolsDialogVisible.value = true
}

// 提交开发者工具密码设置
const handleDevToolsSubmit = async () => {
  if (!devToolsFormRef.value) return
  
  await devToolsFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      await setDevToolsPassword({ password: devToolsForm.password })
      ElMessage.success('开发者工具密码设置成功')
      devToolsDialogVisible.value = false
      
      // 刷新密码状态，启用保护
      await refreshPasswordStatus()
    } catch (error) {
      console.error('设置开发者工具密码失败:', error)
      ElMessage.error(error.response?.data?.message || '设置失败，请重试')
    } finally {
      submitting.value = false
    }
  })
}

// 初始化
onMounted(() => {
  loadMemberList()
})
</script>

<style scoped>
.member-management {
  width: 100%;
  height: 100%;
  padding: 20px;
  box-sizing: border-box;
}

.member-management :deep(.el-card) {
  width: 100%;
}

.member-management :deep(.el-table) {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.query-form {
  margin-bottom: 20px;
}
</style>
