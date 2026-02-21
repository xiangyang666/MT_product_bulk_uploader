<template>
  <el-dialog
    v-model="dialogVisible"
    title="上传新版本"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="安装包文件" prop="file" required>
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :limit="1"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          :accept="'.exe,.dmg'"
          drag
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            拖拽文件到此处或 <em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              仅支持 .exe 和 .dmg 文件，最大 500MB
            </div>
          </template>
        </el-upload>
      </el-form-item>

      <el-form-item label="检测平台" v-if="detectedPlatform">
        <el-tag :type="detectedPlatform === 'Windows' ? 'primary' : 'success'">
          {{ detectedPlatform }}
        </el-tag>
      </el-form-item>

      <el-form-item label="版本号" prop="version" required>
        <el-input
          v-model="form.version"
          placeholder="例如: 1.0.0"
          clearable
        >
          <template #prepend>v</template>
        </el-input>
        <div class="form-tip">格式: X.Y.Z (例如: 1.0.0)</div>
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

    <!-- 上传进度 -->
    <div v-if="uploading" class="upload-progress">
      <div class="progress-info">
        <span class="progress-text">正在上传...</span>
        <span class="progress-percent">{{ uploadProgress }}%</span>
      </div>
      <el-progress 
        :percentage="uploadProgress" 
        :stroke-width="12"
        :color="progressColor"
        :show-text="false"
      />
      <div class="progress-tip">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>请勿关闭窗口,大文件上传可能需要几分钟</span>
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose" :disabled="uploading">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="uploading">
          {{ uploading ? '上传中...' : '确定上传' }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Loading } from '@element-plus/icons-vue'
import { uploadVersion } from '@/api'

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
const uploading = ref(false)
const uploadProgress = ref(0)
const selectedFile = ref(null)
const detectedPlatform = ref('')

// 进度条颜色
const progressColor = computed(() => {
  if (uploadProgress.value < 30) return '#409eff'
  if (uploadProgress.value < 70) return '#67c23a'
  return '#67c23a'
})

const form = reactive({
  file: null,
  version: '',
  releaseNotes: ''
})

const rules = {
  file: [
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

// 文件选择变化
const handleFileChange = (file) => {
  // 验证文件类型
  const fileName = file.name
  if (!fileName.endsWith('.exe') && !fileName.endsWith('.dmg')) {
    ElMessage.error('仅支持 .exe 和 .dmg 文件')
    uploadRef.value.clearFiles()
    return
  }

  // 验证文件大小 (500MB)
  const maxSize = 500 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小超过限制（最大 500MB）')
    uploadRef.value.clearFiles()
    return
  }

  // Element Plus upload 组件返回的 file 对象，raw 属性是原始 File 对象
  selectedFile.value = file.raw || file
  form.file = selectedFile.value  // 更新 form.file 以通过验证
  
  // 自动检测平台
  if (fileName.endsWith('.exe')) {
    detectedPlatform.value = 'Windows'
  } else if (fileName.endsWith('.dmg')) {
    detectedPlatform.value = 'macOS'
  }
}

// 文件移除
const handleFileRemove = () => {
  selectedFile.value = null
  form.file = null  // 清空 form.file
  detectedPlatform.value = ''
}

// 提交表单
const handleSubmit = async () => {
  if (!selectedFile.value) {
    ElMessage.error('请选择要上传的文件')
    return
  }

  try {
    await formRef.value.validate()
    
    uploading.value = true
    uploadProgress.value = 0

    const result = await uploadVersion(
      selectedFile.value, 
      {
        version: form.version,
        releaseNotes: form.releaseNotes
      },
      (percent) => {
        // 真实上传进度回调
        uploadProgress.value = percent
      }
    )

    uploadProgress.value = 100

    ElMessage.success('版本上传成功')
    emit('success', result.data)
    
    // 延迟关闭,让用户看到100%
    setTimeout(() => {
      handleClose()
    }, 500)

  } catch (error) {
    console.error('上传失败:', error)
    if (error.code === 'ECONNABORTED') {
      ElMessage.error('上传超时,请检查网络或稍后重试')
    } else {
      ElMessage.error(error.message || '上传失败')
    }
  } finally {
    uploading.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  if (!uploading.value) {
    formRef.value?.resetFields()
    uploadRef.value?.clearFiles()
    selectedFile.value = null
    form.file = null
    detectedPlatform.value = ''
    uploadProgress.value = 0
    dialogVisible.value = false
  }
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

.upload-progress {
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-top: 20px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.progress-text {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.progress-percent {
  font-size: 18px;
  color: #409eff;
  font-weight: 600;
}

.progress-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
}

.progress-tip .el-icon {
  font-size: 14px;
  color: #409eff;
}
</style>
