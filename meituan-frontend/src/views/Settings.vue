<template>
  <div class="settings-container">
    <el-tabs v-model="activeTab" class="settings-tabs">
      <!-- 系统设置标签页 -->
      <el-tab-pane label="系统设置" name="system">
        <el-row :gutter="20">
          <el-col :span="16">
            <!-- 设置表单 -->
            <div class="settings-card">
              <div class="card-header">
                <h3>商家配置</h3>
              </div>
              <el-form
                ref="settingsFormRef"
                :model="settingsForm"
                :rules="settingsRules"
                label-width="140px"
                v-loading="loading"
              >
                <el-form-item label="商家名称" prop="merchantName">
                  <el-input v-model="settingsForm.merchantName" placeholder="请输入商家名称" />
                  <div class="form-tip">
                    用于标识不同商家的商品数据
                  </div>
                </el-form-item>
                
                <el-form-item label="默认类目ID" prop="templateConfig">
                  <el-input
                    v-model="settingsForm.templateConfig"
                    placeholder="例如：2000010001"
                  />
                  <div class="form-tip">
                    设置默认的商品类目ID，导入时如果没有类目ID将使用此默认值
                  </div>
                </el-form-item>
                
                <el-form-item>
                  <el-button type="primary" @click="handleSave" :loading="saving" style="background-color: #FFD100; border-color: #FFD100; color: #333;">
                    <el-icon><Check /></el-icon>
                    保存设置
                  </el-button>
                  <el-button @click="handleReset">
                    <el-icon><RefreshLeft /></el-icon>
                    重置为默认值
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-col>

          <el-col :span="8">
            <!-- 配置信息卡片 -->
            <div class="info-card">
              <div class="card-header">
                <h3>配置信息</h3>
              </div>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="商家ID">{{ settingsForm.merchantId || '-' }}</el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ formatDateTime(settingsForm.createdTime) }}</el-descriptions-item>
                <el-descriptions-item label="更新时间">{{ formatDateTime(settingsForm.updatedTime) }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </el-col>
        </el-row>

        <!-- 应用版本管理 -->
        <div class="version-management-card" :class="{ 'restricted': !isAdmin }">
          <!-- 权限遮罩层 -->
          <div v-if="!isAdmin" class="permission-overlay">
            <div class="permission-notice">
              <el-icon class="notice-icon"><Lock /></el-icon>
              <div class="notice-title">需要admin-plus管理员权限</div>
              <div class="notice-description">应用版本管理功能仅对admin-plus管理员开放</div>
            </div>
          </div>
          
          <!-- 版本管理内容 -->
          <div class="version-content" :class="{ 'blurred': !isAdmin }">
            <div class="card-header">
              <h3>应用版本管理</h3>
              <el-button 
                v-if="isAdmin"
                type="primary" 
                @click="showUploadDialog" 
                data-testid="upload-version-button"
                style="background-color: #FFD100; border-color: #FFD100; color: #333;"
              >
                <el-icon><Upload /></el-icon>
                上传新版本
              </el-button>
            </div>
            
            <el-tabs v-model="versionTab" @tab-change="handleVersionTabChange">
              <el-tab-pane label="Windows" name="Windows">
                <version-list ref="windowsListRef" platform="Windows" @upload="showUploadDialog" @refresh="handleVersionRefresh" />
              </el-tab-pane>
              <el-tab-pane label="macOS" name="macOS">
                <version-list ref="macosListRef" platform="macOS" @upload="showUploadDialog" @refresh="handleVersionRefresh" />
              </el-tab-pane>
            </el-tabs>
          </div>
        </div>
      </el-tab-pane>

      <!-- 个人资料标签页 -->
      <el-tab-pane label="个人资料" name="profile">
        <el-row :gutter="20">
          <!-- 个人信息 -->
          <el-col :span="12">
            <div class="settings-card">
              <div class="card-header">
                <h3>个人信息</h3>
              </div>
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
                  <el-button type="primary" @click="handleUpdateProfile" :loading="updatingProfile" style="background-color: #FFD100; border-color: #FFD100; color: #333;">
                    <el-icon><Check /></el-icon>
                    保存修改
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-col>

          <!-- 修改密码 -->
          <el-col :span="12">
            <div class="settings-card">
              <div class="card-header">
                <h3>修改密码</h3>
              </div>
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
                  <el-button type="primary" @click="handleChangePassword" :loading="changingPassword" style="background-color: #FFD100; border-color: #FFD100; color: #333;">
                    <el-icon><Check /></el-icon>
                    修改密码
                  </el-button>
                  <el-button @click="handleResetPasswordForm">
                    <el-icon><RefreshLeft /></el-icon>
                    重置
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>

    <!-- 上传对话框 -->
    <version-upload-dialog
      v-model:visible="uploadDialogVisible"
      @success="handleUploadSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, RefreshLeft, Upload, Lock } from '@element-plus/icons-vue'
import request from '@/api/index.js'
import { getProfile, updateProfile, changePassword } from '@/api/member'
import { useUserStore } from '@/stores/user'
import VersionList from '@/components/VersionList.vue'
import VersionUploadDialog from '@/components/VersionUploadDialog.vue'

const router = useRouter()
const userStore = useUserStore()

// 权限检查 - 只有admin-plus账号可以管理应用版本
const isAdmin = computed(() => {
  const username = userStore.userInfo?.username
  return username === 'admin-plus'
})

const loading = ref(false)
const saving = ref(false)
const settingsFormRef = ref(null)
const windowsListRef = ref(null)
const macosListRef = ref(null)
const uploadDialogVisible = ref(false)
const activeTab = ref('system')
const versionTab = ref('Windows')

// 个人资料相关
const profileFormRef = ref(null)
const passwordFormRef = ref(null)
const updatingProfile = ref(false)
const changingPassword = ref(false)

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

// 系统设置相关
const settingsForm = ref({
  id: null,
  merchantId: 1,
  merchantName: '',
  templateConfig: '',
  createdTime: null,
  updatedTime: null
})

const settingsRules = {
  merchantName: [
    { max: 100, message: '商家名称长度不能超过100个字符', trigger: 'blur' }
  ],
  templateConfig: [
    { max: 100, message: '默认类目ID长度不能超过100个字符', trigger: 'blur' }
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

// 格式化日期 - 将 UTC 时间转换为本地时间显示
const formatDate = (dateString) => {
  if (!dateString) return ''
  
  // 解析 UTC 时间并转换为本地时间
  const date = new Date(dateString)
  const now = new Date()
  const currentYear = now.getFullYear()
  const dateYear = date.getFullYear()
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  
  // 如果是今年，显示 月-日 时:分
  if (dateYear === currentYear) {
    return `${month}-${day} ${hours}:${minutes}`
  }
  
  // 否则显示 年-月-日 时:分
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ')
}

// 加载个人信息
const loadProfile = async () => {
  try {
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
    
    updatingProfile.value = true
    try {
      await updateProfile({
        realName: profileForm.realName,
        email: profileForm.email,
        phone: profileForm.phone
      })
      ElMessage.success('个人信息更新成功')
      
      // 刷新用户信息
      await userStore.getUserInfo()
      await loadProfile()
    } catch (error) {
      console.error('更新个人信息失败:', error)
    } finally {
      updatingProfile.value = false
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

// 获取系统设置
const fetchSettings = async () => {
  loading.value = true
  try {
    const response = await request.get('/settings', {
      params: { merchantId: 1 }
    })
    
    if (response.code === 200 && response.data) {
      settingsForm.value = {
        id: response.data.id,
        merchantId: response.data.merchantId,
        merchantName: response.data.merchantName || '',
        templateConfig: response.data.templateConfig || '',
        createdTime: response.data.createdTime,
        updatedTime: response.data.updatedTime
      }
    }
  } catch (error) {
    ElMessage.error('获取设置失败')
  } finally {
    loading.value = false
  }
}

// 保存系统设置
const handleSave = async () => {
  if (!settingsFormRef.value) return
  
  await settingsFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    saving.value = true
    
    try {
      const response = await request.put('/settings', settingsForm.value)
      
      if (response.code === 200) {
        ElMessage.success('设置保存成功')
        fetchSettings()
      }
    } catch (error) {
      ElMessage.error('设置保存失败')
    } finally {
      saving.value = false
    }
  })
}

// 重置系统设置
const handleReset = async () => {
  try {
    await ElMessageBox.confirm('确定要重置为默认设置吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    loading.value = true
    
    const response = await request.post('/settings/reset', null, {
      params: { merchantId: 1 }
    })
    
    if (response.code === 200) {
      ElMessage.success('已重置为默认设置')
      fetchSettings()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重置失败')
    }
  } finally {
    loading.value = false
  }
}

// 版本管理相关方法
const showUploadDialog = () => {
  uploadDialogVisible.value = true
}

const handleUploadSuccess = () => {
  if (versionTab.value === 'Windows' && windowsListRef.value) {
    windowsListRef.value.refresh()
  } else if (versionTab.value === 'macOS' && macosListRef.value) {
    macosListRef.value.refresh()
  }
}

const handleVersionRefresh = () => {
  if (versionTab.value === 'Windows' && windowsListRef.value) {
    windowsListRef.value.refresh()
  } else if (versionTab.value === 'macOS' && macosListRef.value) {
    macosListRef.value.refresh()
  }
}

const handleVersionTabChange = (tabName) => {
  versionTab.value = tabName
}

onMounted(() => {
  loadProfile()
  fetchSettings()
})
</script>

<style scoped>
.settings-container {
  width: 100%;
  min-height: 100%;
  background-color: #f5f5f5;
  padding: 16px;
}

.settings-tabs {
  background-color: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.settings-tabs :deep(.el-tabs__content) {
  padding-top: 20px;
}

.settings-card,
.info-card {
  background-color: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
}

.version-management-card {
  position: relative;
  background-color: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-top: 20px;
}

.version-management-card.restricted {
  overflow: hidden;
}

/* 模糊内容层 */
.version-content.blurred {
  filter: blur(4px);
  pointer-events: none;
  user-select: none;
}

/* 权限遮罩层 */
.permission-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  border-radius: 12px;
}

/* 权限提示框 */
.permission-notice {
  text-align: center;
  padding: 32px;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  max-width: 400px;
}

.notice-icon {
  font-size: 48px;
  color: #909399;
  margin-bottom: 16px;
}

.notice-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.notice-description {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

/* 浏览器兼容性回退 */
@supports not (backdrop-filter: blur(2px)) {
  .permission-overlay {
    background-color: rgba(255, 255, 255, 0.85);
  }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .permission-notice {
    padding: 24px;
    max-width: 90%;
  }
  
  .notice-icon {
    font-size: 36px;
  }
  
  .notice-title {
    font-size: 16px;
  }
  
  .notice-description {
    font-size: 13px;
  }
}

.card-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

@media (max-width: 1200px) {
  .settings-container {
    padding: 12px;
  }
}
</style>
