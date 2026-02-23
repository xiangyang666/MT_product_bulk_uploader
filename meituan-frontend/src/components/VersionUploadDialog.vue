<template>
  <el-dialog
    v-model="dialogVisible"
    title="上传新版本"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="安装包文件" prop="files" required>
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :limit="2"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          :accept="'.exe,.dmg'"
          :file-list="fileList"
          drag
          multiple
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            拖拽文件到此处或 <em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持同时上传 Windows (.exe) 和 macOS (.dmg) 文件，单个文件最大 500MB
            </div>
          </template>
        </el-upload>
      </el-form-item>

      <el-form-item label="检测平台" v-if="detectedPlatforms.length > 0">
        <el-tag 
          v-for="platform in detectedPlatforms" 
          :key="platform"
          :type="platform === 'Windows' ? 'primary' : 'success'"
          style="margin-right: 8px;"
        >
          {{ platform }}
        </el-tag>
      </el-form-item>

      <el-form-item label="版本号" prop="version" required>
        <el-input
          v-model="form.version"
          placeholder="例如: 1.0.0"
          clearable
          :loading="loadingVersion"
        >
          <template #prepend>v</template>
          <template #suffix v-if="loadingVersion">
            <el-icon class="is-loading"><Loading /></el-icon>
          </template>
        </el-input>
        <div class="form-tip">
          格式: X.Y.Z (例如: 1.0.0)，所有文件使用相同版本号
          <span v-if="loadingVersion" style="color: #409eff;"> · 正在获取最新版本...</span>
        </div>
      </el-form-item>

      <el-form-item label="发布说明" prop="releaseNotes">
        <el-input
          v-model="form.releaseNotes"
          type="textarea"
          :rows="4"
          placeholder="描述此版本的更新内容..."
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <!-- 上传进度列表 -->
    <div v-if="uploadTasks.length > 0" class="upload-tasks">
      <div class="tasks-header">
        <span>上传进度</span>
        <span class="tasks-summary">{{ completedCount }}/{{ uploadTasks.length }} 完成</span>
      </div>
      
      <div 
        v-for="task in uploadTasks" 
        :key="task.id" 
        class="upload-task"
        :class="{ 'task-error': task.status === 'error' }"
      >
        <div class="task-header">
          <div class="task-info">
            <el-icon 
              :class="{
                'task-icon-uploading': task.status === 'uploading',
                'task-icon-success': task.status === 'success',
                'task-icon-error': task.status === 'error',
                'task-icon-waiting': task.status === 'waiting'
              }"
            >
              <Loading v-if="task.status === 'uploading'" class="is-loading" />
              <CircleCheck v-else-if="task.status === 'success'" />
              <CircleClose v-else-if="task.status === 'error'" />
              <Clock v-else />
            </el-icon>
            <span class="task-name">{{ task.fileName }}</span>
            <el-tag size="small" :type="task.platform === 'Windows' ? 'primary' : 'success'">
              {{ task.platform }}
            </el-tag>
          </div>
          <div class="task-actions">
            <span class="task-progress-text">{{ task.progress }}%</span>
            <el-button 
              v-if="task.status === 'error'" 
              type="primary" 
              size="small" 
              link
              @click="retryUpload(task)"
            >
              重试
            </el-button>
          </div>
        </div>
        
        <el-progress 
          :percentage="task.progress" 
          :stroke-width="8"
          :color="getProgressColor(task)"
          :show-text="false"
          :status="task.status === 'error' ? 'exception' : undefined"
        />
        
        <div v-if="task.error" class="task-error-msg" :class="{ 'task-info-msg': task.progress === 100 && task.status === 'uploading' }">
          <el-icon><WarningFilled v-if="task.status === 'error'" /><Loading v-else class="is-loading" /></el-icon>
          <span>{{ task.error }}</span>
        </div>
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose" :disabled="isUploading">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleSubmit" 
          :loading="isUploading"
          :disabled="uploadTasks.length > 0 && completedCount === uploadTasks.length"
        >
          {{ getSubmitButtonText() }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Loading, CircleCheck, CircleClose, Clock, WarningFilled } from '@element-plus/icons-vue'
import { uploadVersion, getLatestVersion } from '@/api'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'success'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const formRef = ref(null)
const uploadRef = ref(null)
const selectedFiles = ref([])
const fileList = ref([])
const detectedPlatforms = ref([])
const uploadTasks = ref([])
const isUploading = ref(false)
const loadingVersion = ref(false)

// 计算完成数量
const completedCount = computed(() => {
  return uploadTasks.value.filter(task => task.status === 'success').length
})

const form = reactive({
  files: [],
  version: '',
  releaseNotes: ''
})

const rules = {
  files: [
    { required: true, message: '请选择要上传的文件', trigger: 'change' }
  ],
  version: [
    { required: true, message: '请输入版本号', trigger: 'blur' },
    { 
      pattern: /^\d+\.\d+\.\d+$/, 
      message: '版本号格式错误，应为 X.Y.Z', 
      trigger: 'blur' 
    }
  ]
}

// 递增版本号
const incrementVersion = (version) => {
  if (!version) return '1.0.0'
  
  const parts = version.split('.')
  if (parts.length !== 3) return '1.0.0'
  
  const [major, minor, patch] = parts.map(Number)
  
  // 递增补丁版本号
  return `${major}.${minor}.${patch + 1}`
}

// 获取并设置默认版本号
const fetchAndSetDefaultVersion = async () => {
  if (!detectedPlatforms.value.length) return
  
  loadingVersion.value = true
  
  try {
    // 优先获取 Windows 平台的最新版本，如果没有则获取 macOS
    const platform = detectedPlatforms.value.includes('Windows') ? 'Windows' : 'macOS'
    
    // 使用静默模式，不显示错误提示
    const response = await getLatestVersion(platform, true)
    
    if (response.code === 200 && response.data && response.data.version) {
      // 递增版本号
      const newVersion = incrementVersion(response.data.version)
      form.version = newVersion
      console.log(`自动设置版本号: ${newVersion} (基于 ${platform} 平台)`)
    } else {
      // 如果没有历史版本，使用默认值
      form.version = '1.0.0'
      console.log('没有历史版本，使用默认版本号: 1.0.0')
    }
  } catch (error) {
    // 静默处理错误，不显示给用户
    console.log('获取最新版本失败，尝试备用方案')
    
    // 如果获取失败，尝试另一个平台
    if (detectedPlatforms.value.length > 1) {
      try {
        const alternatePlatform = detectedPlatforms.value.find(p => 
          p !== (detectedPlatforms.value.includes('Windows') ? 'Windows' : 'macOS')
        )
        if (alternatePlatform) {
          console.log(`尝试获取 ${alternatePlatform} 平台的版本`)
          const response = await getLatestVersion(alternatePlatform, true)
          if (response.code === 200 && response.data && response.data.version) {
            const newVersion = incrementVersion(response.data.version)
            form.version = newVersion
            console.log(`自动设置版本号: ${newVersion} (基于 ${alternatePlatform} 平台)`)
            return
          }
        }
      } catch (err) {
        console.log('获取备用平台版本失败')
      }
    }
    // 都失败了，使用默认值
    form.version = '1.0.0'
    console.log('使用默认版本号: 1.0.0')
  } finally {
    loadingVersion.value = false
  }
}

// 监听平台变化，自动获取版本号
watch(detectedPlatforms, (newPlatforms) => {
  if (newPlatforms.length > 0 && !form.version) {
    fetchAndSetDefaultVersion()
  }
}, { deep: true })

// 监听对话框打开，重置版本号
watch(dialogVisible, (newVal) => {
  if (newVal && detectedPlatforms.value.length > 0) {
    fetchAndSetDefaultVersion()
  }
})

// 获取进度条颜色
const getProgressColor = (task) => {
  if (task.status === 'error') return '#f56c6c'
  if (task.status === 'success') return '#67c23a'
  if (task.progress < 30) return '#409eff'
  if (task.progress < 70) return '#409eff'
  return '#67c23a'
}

// 获取提交按钮文本
const getSubmitButtonText = () => {
  if (isUploading.value) {
    return `上传中... (${completedCount.value}/${uploadTasks.value.length})`
  }
  if (uploadTasks.value.length > 0 && completedCount.value === uploadTasks.value.length) {
    return '全部完成'
  }
  return '开始上传'
}

// 文件选择变化
const handleFileChange = (file, files) => {
  // 验证文件类型
  const fileName = file.name
  if (!fileName.endsWith('.exe') && !fileName.endsWith('.dmg')) {
    ElMessage.error('仅支持 .exe 和 .dmg 文件')
    uploadRef.value.handleRemove(file)
    return
  }

  // 验证文件大小 (500MB)
  const maxSize = 500 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error(`文件 ${fileName} 大小超过限制（最大 500MB）`)
    uploadRef.value.handleRemove(file)
    return
  }

  // 检查是否已有相同平台的文件
  const platform = fileName.endsWith('.exe') ? 'Windows' : 'macOS'
  const existingFile = selectedFiles.value.find(f => {
    const existingPlatform = f.name.endsWith('.exe') ? 'Windows' : 'macOS'
    return existingPlatform === platform
  })

  if (existingFile) {
    ElMessage.warning(`已存在 ${platform} 平台的文件，将替换为新文件`)
    // 移除旧文件
    const index = selectedFiles.value.findIndex(f => f === existingFile)
    if (index > -1) {
      selectedFiles.value.splice(index, 1)
    }
  }

  // 添加新文件
  selectedFiles.value.push(file.raw || file)
  form.files = selectedFiles.value
  fileList.value = files

  // 更新检测到的平台
  updateDetectedPlatforms()
}

// 文件移除
const handleFileRemove = (file) => {
  const index = selectedFiles.value.findIndex(f => f.name === file.name)
  if (index > -1) {
    selectedFiles.value.splice(index, 1)
  }
  form.files = selectedFiles.value
  updateDetectedPlatforms()
}

// 更新检测到的平台
const updateDetectedPlatforms = () => {
  const platforms = new Set()
  selectedFiles.value.forEach(file => {
    if (file.name.endsWith('.exe')) {
      platforms.add('Windows')
    } else if (file.name.endsWith('.dmg')) {
      platforms.add('macOS')
    }
  })
  detectedPlatforms.value = Array.from(platforms)
}

// 创建上传任务
const createUploadTasks = () => {
  console.log('selectedFiles:', selectedFiles.value.map(f => ({ name: f.name, size: f.size })))
  
  uploadTasks.value = selectedFiles.value.map((file, index) => {
    const platform = file.name.endsWith('.exe') ? 'Windows' : 'macOS'
    const task = {
      id: `task-${Date.now()}-${index}`,
      file: file,
      fileName: file.name,
      platform: platform,
      progress: 0,
      status: 'waiting', // waiting, uploading, success, error
      error: null
    }
    console.log(`创建任务 ${index + 1}:`, { fileName: task.fileName, platform: task.platform })
    return task
  })
  
  console.log('uploadTasks 创建完成，共', uploadTasks.value.length, '个任务')
}

// 上传单个文件
const uploadSingleFile = async (task) => {
  console.log(`开始上传文件: ${task.fileName}`)
  task.status = 'uploading'
  task.progress = 0
  task.error = null

  try {
    const result = await uploadVersion(
      task.file,
      {
        version: form.version,
        releaseNotes: form.releaseNotes
      },
      (percent) => {
        task.progress = percent
        console.log(`${task.fileName} 上传进度: ${percent}%`)
        
        // 当进度达到 100% 时，显示处理中状态
        if (percent === 100) {
          task.error = '文件已上传，服务器处理中...'
        }
      }
    )

    console.log(`${task.fileName} 上传成功:`, result)
    task.status = 'success'
    task.progress = 100
    task.error = null // 清除"处理中"提示
    
    return true
  } catch (error) {
    console.error(`上传 ${task.fileName} 失败:`, error)
    console.error('错误详情:', {
      message: error.message,
      response: error.response,
      code: error.code
    })
    
    task.status = 'error'
    
    // 更详细的错误信息
    if (error.response) {
      task.error = error.response.data?.message || error.message || '上传失败，请重试'
    } else if (error.code === 'ECONNABORTED' || error.code === 'ERR_NETWORK') {
      task.error = '网络连接中断，可能是文件过大或网络不稳定，请重试'
    } else if (error.message) {
      task.error = error.message
    } else {
      task.error = '上传失败，请重试'
    }
    
    return false
  }
}

// 重试上传
const retryUpload = async (task) => {
  await uploadSingleFile(task)
  
  // 检查是否全部完成
  checkAllCompleted()
}

// 检查是否全部完成
const checkAllCompleted = () => {
  const allSuccess = uploadTasks.value.every(task => task.status === 'success')
  const hasError = uploadTasks.value.some(task => task.status === 'error')
  
  if (allSuccess) {
    ElMessage.success('所有文件上传成功')
    emit('success')
    isUploading.value = false
  } else if (!isUploading.value && hasError) {
    ElMessage.warning('部分文件上传失败，请点击重试按钮继续上传')
  }
}

// 提交表单 - 流式上传
const handleSubmit = async () => {
  if (selectedFiles.value.length === 0) {
    ElMessage.error('请选择要上传的文件')
    return
  }

  try {
    await formRef.value.validate()
    
    // 如果已有任务且有失败的，只上传失败的
    if (uploadTasks.value.length > 0) {
      const failedTasks = uploadTasks.value.filter(task => task.status === 'error')
      if (failedTasks.length > 0) {
        isUploading.value = true
        
        console.log('重试上传失败的任务:', failedTasks.map(t => t.fileName))
        
        // 流式上传失败的任务
        for (const task of failedTasks) {
          await uploadSingleFile(task)
        }
        
        checkAllCompleted()
        return
      }
    }
    
    // 创建新的上传任务
    createUploadTasks()
    console.log('创建上传任务:', uploadTasks.value.map(t => ({ name: t.fileName, platform: t.platform })))
    
    isUploading.value = true

    // 流式上传所有文件 - 即使某个失败也继续下一个
    for (let i = 0; i < uploadTasks.value.length; i++) {
      const task = uploadTasks.value[i]
      console.log(`开始上传第 ${i + 1}/${uploadTasks.value.length} 个文件: ${task.fileName}`)
      
      try {
        await uploadSingleFile(task)
      } catch (error) {
        console.error(`任务 ${task.fileName} 上传异常:`, error)
        // 继续下一个文件
      }
    }

    console.log('所有上传任务完成')
    checkAllCompleted()

  } catch (error) {
    console.error('表单验证失败:', error)
    isUploading.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  if (isUploading.value) {
    ElMessage.warning('文件正在上传中，请等待完成')
    return
  }
  
  formRef.value?.resetFields()
  uploadRef.value?.clearFiles()
  selectedFiles.value = []
  fileList.value = []
  form.files = []
  form.version = ''
  detectedPlatforms.value = []
  uploadTasks.value = []
  dialogVisible.value = false
}
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.el-upload__tip {
  font-size: 12px;
  color: #909399;
}

:deep(.el-upload-dragger) {
  padding: 20px;
}

/* 上传任务列表 */
.upload-tasks {
  margin-top: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  max-height: 400px;
  overflow-y: auto;
}

.tasks-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.tasks-summary {
  font-size: 13px;
  color: #909399;
  font-weight: normal;
}

.upload-task {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  transition: all 0.3s;
}

.upload-task:last-child {
  margin-bottom: 0;
}

.upload-task.task-error {
  border: 1px solid #f56c6c;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.task-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.task-icon-uploading {
  color: #409eff;
  font-size: 18px;
}

.task-icon-success {
  color: #67c23a;
  font-size: 18px;
}

.task-icon-error {
  color: #f56c6c;
  font-size: 18px;
}

.task-icon-waiting {
  color: #909399;
  font-size: 18px;
}

.task-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.task-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.task-progress-text {
  font-size: 14px;
  color: #409eff;
  font-weight: 600;
  min-width: 45px;
  text-align: right;
}

.task-error-msg {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  padding: 8px 12px;
  background: #fef0f0;
  border-radius: 4px;
  font-size: 12px;
  color: #f56c6c;
}

.task-error-msg.task-info-msg {
  background: #f0f9ff;
  color: #409eff;
}

.task-error-msg .el-icon {
  font-size: 14px;
}
</style>
