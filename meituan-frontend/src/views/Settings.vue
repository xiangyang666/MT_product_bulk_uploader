<template>
  <div class="settings-container">
    <!-- 设置表单 -->
    <div class="settings-card">
      <div class="card-header">
        <h3>系统设置</h3>
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

    <!-- 应用版本管理 -->
    <div class="version-management-card">
      <div class="card-header">
        <h3>应用版本管理</h3>
        <el-button type="primary" @click="showUploadDialog" style="background-color: #FFD100; border-color: #FFD100; color: #333;">
          <el-icon><Upload /></el-icon>
          上传新版本
        </el-button>
      </div>
      
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="Windows" name="Windows">
          <version-list ref="windowsListRef" platform="Windows" @upload="showUploadDialog" @refresh="handleVersionRefresh" />
        </el-tab-pane>
        <el-tab-pane label="macOS" name="macOS">
          <version-list ref="macosListRef" platform="macOS" @upload="showUploadDialog" @refresh="handleVersionRefresh" />
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 上传对话框 -->
    <version-upload-dialog
      v-model:visible="uploadDialogVisible"
      @success="handleUploadSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, RefreshLeft, Upload } from '@element-plus/icons-vue'
import request from '@/api/index.js'
import VersionList from '@/components/VersionList.vue'
import VersionUploadDialog from '@/components/VersionUploadDialog.vue'

const loading = ref(false)
const saving = ref(false)
const settingsFormRef = ref(null)
const windowsListRef = ref(null)
const macosListRef = ref(null)
const uploadDialogVisible = ref(false)
const activeTab = ref('Windows')

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

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ')
}

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
  // 刷新当前标签页的版本列表
  if (activeTab.value === 'Windows' && windowsListRef.value) {
    windowsListRef.value.refresh()
  } else if (activeTab.value === 'macOS' && macosListRef.value) {
    macosListRef.value.refresh()
  }
}

const handleVersionRefresh = () => {
  // 版本操作后刷新列表
  if (activeTab.value === 'Windows' && windowsListRef.value) {
    windowsListRef.value.refresh()
  } else if (activeTab.value === 'macOS' && macosListRef.value) {
    macosListRef.value.refresh()
  }
}

const handleTabChange = (tabName) => {
  activeTab.value = tabName
}

onMounted(() => {
  fetchSettings()
})
</script>

<style scoped>
.settings-container {
  width: 100%;
  min-height: 100%;
  background-color: #f5f5f5;
  padding: 16px;
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 16px;
}

.settings-card,
.info-card {
  background-color: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.version-management-card {
  grid-column: 1 / -1;
  background-color: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
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
    grid-template-columns: 1fr;
  }
  
  .version-management-card {
    grid-column: 1;
  }
}
</style>
