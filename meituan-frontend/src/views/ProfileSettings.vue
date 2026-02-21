<template>
  <div class="profile-settings">
    <el-row :gutter="20">
      <!-- 个人信息 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>个人信息</span>
          </template>
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
          >
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
            <el-form-item label="角色">
              <el-tag :type="getRoleType(profileForm.role)">
                {{ getRoleLabel(profileForm.role) }}
              </el-tag>
            </el-form-item>
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="状态">
              <el-tag :type="profileForm.status === 1 ? 'success' : 'danger'">
                {{ profileForm.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </el-form-item>
            <el-form-item label="注册时间">
              <span>{{ formatDate(profileForm.createdAt) }}</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdateProfile" :loading="updating">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 修改密码 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>修改密码</span>
          </template>
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
          >
            <el-form-item label="当前密码" prop="currentPassword">
              <el-input
                v-model="passwordForm.currentPassword"
                type="password"
                placeholder="请输入当前密码"
                show-password
              />
            </el-form-item>
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
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">
                修改密码
              </el-button>
              <el-button @click="handleResetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile, changePassword } from '@/api/member'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 个人信息表单
const profileFormRef = ref(null)
const profileForm = reactive({
  id: null,
  username: '',
  role: '',
  realName: '',
  email: '',
  phone: '',
  status: 1,
  createdAt: ''
})

const profileRules = {
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 密码表单
const passwordFormRef = ref(null)
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (_rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '密码长度至少8个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const updating = ref(false)
const changingPassword = ref(false)

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

// 格式化日期：本年只显示月日，否则显示年月日
const formatDate = (dateString) => {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  const now = new Date()
  const currentYear = now.getFullYear()
  const dateYear = date.getFullYear()
  
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  
  // 如果是本年，只显示月日
  if (dateYear === currentYear) {
    return `${month}-${day}`
  }
  
  // 否则显示年月日
  return `${dateYear}-${month}-${day}`
}

// 加载个人信息
const loadProfile = async () => {
  try {
    // 从userStore获取当前登录用户的用户名
    const username = userStore.userInfo?.username
    if (!username) {
      ElMessage.error('未获取到用户信息，请重新登录')
      return
    }
    
    const res = await getProfile(username)
    Object.assign(profileForm, res.data)
  } catch (error) {
    console.error('获取个人信息失败:', error)
    ElMessage.error('获取个人信息失败')
  }
}

// 更新个人信息
const handleUpdateProfile = async () => {
  if (!profileFormRef.value) return
  
  await profileFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    updating.value = true
    try {
      await updateProfile({
        realName: profileForm.realName,
        email: profileForm.email,
        phone: profileForm.phone
      })
      ElMessage.success('个人信息更新成功')
      loadProfile()
    } catch (error) {
      console.error('更新个人信息失败:', error)
    } finally {
      updating.value = false
    }
  })
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    changingPassword.value = true
    try {
      const username = userStore.userInfo?.username
      if (!username) {
        ElMessage.error('未获取到用户信息，请重新登录')
        return
      }
      
      await changePassword({
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword
      }, username)
      
      ElMessage.success('密码修改成功，请重新登录')
      handleResetPasswordForm()
      
      // 清除用户信息并跳转到登录页
      await userStore.logout()
      router.push('/login')
    } catch (error) {
      console.error('修改密码失败:', error)
    } finally {
      changingPassword.value = false
    }
  })
}

// 重置密码表单
const handleResetPasswordForm = () => {
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

// 初始化
onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-settings {
  padding: 20px;
}
</style>
