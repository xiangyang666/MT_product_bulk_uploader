<template>
  <div class="member-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>成员管理</span>
          <el-button type="primary" @click="handleCreate" v-if="canCreate">
            <el-icon><Plus /></el-icon>
            创建成员
          </el-button>
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
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="300">
          <template #default="{ row }">
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
            :disabled="isEdit"
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
            <el-option label="超级管理员" value="SUPER_ADMIN" v-if="canCreate" />
            <el-option label="管理员" value="ADMIN" v-if="canCreate" />
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getMemberList,
  createMember,
  updateMember,
  deleteMember,
  changeMemberPassword,
  changeMemberStatus
} from '@/api/member'

// 当前用户角色（临时硬编码，实际应从store获取）
const currentUserRole = ref('SUPER_ADMIN')

// 权限计算
const canCreate = computed(() => currentUserRole.value === 'SUPER_ADMIN')
const canChangePassword = computed(() => currentUserRole.value === 'SUPER_ADMIN')
const canChangeStatus = computed(() => currentUserRole.value === 'SUPER_ADMIN')

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

.query-form {
  margin-bottom: 20px;
}
</style>
