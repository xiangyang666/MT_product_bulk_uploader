<template>
  <div class="images-container">
    <div class="page-header">
      <h2 class="page-title">批量上传图片</h2>
      <p class="page-desc">支持批量上传商品图片，最多支持100张</p>
    </div>

    <!-- 上传区域 -->
    <div class="upload-section">
      <el-card shadow="never">
        <el-upload
          ref="uploadRef"
          v-model:file-list="fileList"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :on-success="handleSuccess"
          :on-error="handleError"
          :on-progress="handleProgress"
          :before-upload="beforeUpload"
          :on-change="handleChange"
          :auto-upload="false"
          multiple
          :limit="100"
          drag
          class="upload-area"
        >
          <el-icon class="upload-icon"><UploadFilled /></el-icon>
          <div class="upload-text">
            <p class="upload-main-text">拖拽文件到此处，或<em>点击上传</em></p>
            <p class="upload-sub-text">支持 jpg、png、gif 格式，单个文件不超过 5MB</p>
          </div>
        </el-upload>

        <div class="upload-actions">
          <el-button
            type="primary"
            :loading="uploading"
            @click="handleUpload"
            :disabled="fileList.length === 0"
            style="background-color: #FFD100; border-color: #FFD100; color: #333;"
          >
            {{ uploading ? '上传中...' : `开始上传 (${fileList.length} 张)` }}
          </el-button>
          <el-button @click="handleClear" :disabled="uploading">
            清空列表
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 上传结果 -->
    <div v-if="uploadedImages.length > 0 || errorFiles.length > 0" class="result-section">
      <!-- 成功上传的图片 -->
      <el-card v-if="uploadedImages.length > 0" shadow="never" class="result-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">✓ 成功上传 ({{ uploadedImages.length }} 张)</span>
            <el-button
              type="primary"
              size="small"
              @click="handleCopyAllUrls"
              style="background-color: #FFD100; border-color: #FFD100; color: #333;"
            >
              复制所有URL
            </el-button>
          </div>
        </template>

        <div class="image-grid">
          <div
            v-for="(image, index) in uploadedImages"
            :key="index"
            class="image-item"
          >
            <el-image
              :src="image.imageUrl"
              fit="cover"
              class="uploaded-image"
              :preview-src-list="[image.imageUrl]"
            />
            <div class="image-info">
              <div class="image-name" :title="image.fileName">{{ image.fileName }}</div>
              <div class="image-size">{{ image.size }}</div>
            </div>
            <el-input
              v-model="image.imageUrl"
              readonly
              size="small"
              class="image-url-input"
            >
              <template #append>
                <el-button
                  :icon="DocumentCopy"
                  @click="copyUrl(image.imageUrl)"
                />
              </template>
            </el-input>
          </div>
        </div>
      </el-card>

      <!-- 上传失败的图片 -->
      <el-card v-if="errorFiles.length > 0" shadow="never" class="result-card error-card">
        <template #header>
          <span class="card-title">✗ 上传失败 ({{ errorFiles.length }} 张)</span>
        </template>
        <div class="error-list">
          <div
            v-for="(error, index) in errorFiles"
            :key="index"
            class="error-item"
          >
            <el-icon class="error-icon"><CircleClose /></el-icon>
            <span>{{ error }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 使用说明 -->
    <el-card shadow="never" class="tips-card">
      <template #header>
        <span class="card-title">使用说明</span>
      </template>
      <div class="tips-content">
        <h4>如何使用批量上传的图片：</h4>
        <ol>
          <li>上传图片后，复制图片URL</li>
          <li>在Excel表格的"图片"或"图片URL"列中粘贴URL</li>
          <li>导入Excel时系统会自动关联图片</li>
        </ol>
        <h4>注意事项：</h4>
        <ul>
          <li>最多支持同时上传 100 张图片</li>
          <li>单个图片文件大小不能超过 5MB</li>
          <li>支持格式：JPG、PNG、GIF</li>
          <li>图片上传后可通过URL直接访问</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import { UploadFilled, DocumentCopy, CircleClose } from '@element-plus/icons-vue'
import request from '@/api/index.js'

const uploadRef = ref(null)
const fileList = ref([])
const uploading = ref(false)
const uploadedImages = ref([])
const errorFiles = ref([])

// 获取上传地址和请求头
const token = localStorage.getItem('token')
const uploadUrl = `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/images/batch-upload?merchantId=1`
const uploadHeaders = {
  'Authorization': `Bearer ${token}`
}

// 文件选择变化
const handleChange = (file, fileList) => {
  // 过滤非图片文件
  const isImage = file.raw.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
}

// 上传前验证
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  return true
}

// 处理上传
const handleUpload = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请先选择要上传的图片')
    return
  }

  uploading.value = true
  uploadedImages.value = []
  errorFiles.value = []

  try {
    // 创建 FormData
    const formData = new FormData()
    fileList.value.forEach(file => {
      formData.append('files', file.raw)
    })
    formData.append('merchantId', 1)

    // 发送请求
    const response = await request.post('/images/batch-upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      timeout: 600000 // 10分钟超时
    })

    if (response.code === 200) {
      uploadedImages.value = response.data.uploadedImages || []
      errorFiles.value = response.data.errorFiles || []

      // 显示上传结果通知
      ElNotification({
        title: '上传完成',
        message: `成功 ${response.data.successCount} 张，失败 ${response.data.errorCount} 张`,
        type: errorFiles.value.length === 0 ? 'success' : 'warning',
        duration: 5000
      })

      // 清空文件列表
      fileList.value = []
    }
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error(error.response?.data?.message || '上传失败，请重试')
  } finally {
    uploading.value = false
  }
}

// 处理进度
const handleProgress = (event, file, fileList) => {
  // 可以在这里显示上传进度
}

// 处理成功
const handleSuccess = (response, file, fileList) => {
  // 批量上传时不会调用这个方法，因为我们不使用 el-upload 的自动上传
}

// 处理错误
const handleError = (error, file, fileList) => {
  ElMessage.error(`${file.name} 上传失败`)
}

// 清空列表
const handleClear = () => {
  fileList.value = []
  uploadedImages.value = []
  errorFiles.value = []
}

// 复制URL
const copyUrl = async (url) => {
  try {
    await navigator.clipboard.writeText(url)
    ElMessage.success('URL已复制到剪贴板')
  } catch (error) {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = url
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    ElMessage.success('URL已复制到剪贴板')
  }
}

// 复制所有URL
const handleCopyAllUrls = async () => {
  const urls = uploadedImages.value.map(img => img.imageUrl).join('\n')
  try {
    await navigator.clipboard.writeText(urls)
    ElMessage.success(`已复制 ${uploadedImages.value.length} 个URL`)
  } catch (error) {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = urls
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    ElMessage.success(`已复制 ${uploadedImages.value.length} 个URL`)
  }
}
</script>

<style scoped>
.images-container {
  padding: 20px;
  min-height: calc(100vh - 40px);
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.page-desc {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.upload-section {
  margin-bottom: 20px;
}

.upload-area {
  margin-bottom: 20px;
}

.upload-icon {
  font-size: 67px;
  color: #FFD100;
}

.upload-text {
  margin-top: 16px;
}

.upload-main-text {
  font-size: 16px;
  color: #333;
  margin: 0 0 8px 0;
}

.upload-main-text em {
  color: #FFD100;
  font-style: normal;
}

.upload-sub-text {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.upload-actions {
  display: flex;
  gap: 12px;
}

.result-section {
  margin-bottom: 20px;
}

.result-card {
  margin-bottom: 20px;
}

.result-card:last-child {
  margin-bottom: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.error-card .card-title {
  color: #f56c6c;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.image-item {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 12px;
  background-color: #fff;
}

.uploaded-image {
  width: 100%;
  height: 150px;
  border-radius: 4px;
  margin-bottom: 8px;
}

.image-info {
  margin-bottom: 8px;
}

.image-name {
  font-size: 13px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.image-size {
  font-size: 12px;
  color: #999;
}

.image-url-input {
  font-size: 12px;
}

.error-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.error-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background-color: #fef0f0;
  border-radius: 4px;
  font-size: 13px;
  color: #f56c6c;
}

.error-icon {
  font-size: 16px;
}

.tips-card {
  background-color: #f8f9fa;
}

.tips-content h4 {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 16px 0 8px 0;
}

.tips-content h4:first-child {
  margin-top: 0;
}

.tips-content ol,
.tips-content ul {
  margin: 0;
  padding-left: 20px;
}

.tips-content li {
  font-size: 13px;
  color: #666;
  margin: 4px 0;
}
</style>
