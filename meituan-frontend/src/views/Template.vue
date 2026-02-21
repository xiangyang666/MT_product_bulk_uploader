<template>
  <div class="template-container">
    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="showUploadDialog" style="background-color: #FFD100; border-color: #FFD100; color: #333;">
        <el-icon><Upload /></el-icon>
        上传模板
      </el-button>
    </div>

    <!-- 模板表格 -->
    <div class="table-card">
      <el-table
        :data="templates"
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="templateName" label="模板名称" show-overflow-tooltip />
        <el-table-column prop="templateType" label="模板类型" width="120">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.templateType)" size="small">
              {{ getTypeLabel(scope.row.templateType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileSize" label="文件大小" width="120">
          <template #default="scope">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handlePreview(scope.row)">
              <el-icon><View /></el-icon>
              预览
            </el-button>
            <el-button link type="primary" size="small" @click="handleDownload(scope.row)">
              <el-icon><Download /></el-icon>
              下载
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(scope.row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 上传对话框 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传模板"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="uploadForm" :rules="uploadRules" ref="uploadFormRef" label-width="100px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="uploadForm.templateName" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板类型" prop="templateType">
          <el-select v-model="uploadForm.templateType" placeholder="请选择模板类型" style="width: 100%">
            <el-option label="导入模板" value="IMPORT" />
            <el-option label="导出模板" value="EXPORT" />
            <el-option label="美团模板" value="MEITUAN" />
          </el-select>
        </el-form-item>
        <el-form-item label="模板文件" prop="file">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-exceed="handleExceed"
            accept=".xlsx,.xls,.csv"
            drag
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              拖拽文件到此处或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 .xlsx, .xls, .csv 格式，文件大小不超过 10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpload" :loading="uploading" style="background-color: #FFD100; border-color: #FFD100; color: #333;">
          确定上传
        </el-button>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="模板预览"
      width="700px"
    >
      <div v-if="previewData" class="preview-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="模板名称">{{ previewData.templateName }}</el-descriptions-item>
          <el-descriptions-item label="模板类型">{{ getTypeLabel(previewData.templateType) }}</el-descriptions-item>
          <el-descriptions-item label="列数">{{ previewData.columnCount }}</el-descriptions-item>
          <el-descriptions-item label="数据行数">{{ previewData.dataRowCount }}</el-descriptions-item>
        </el-descriptions>
        <div class="preview-headers" v-if="previewData.headers && previewData.headers.length > 0">
          <h4>表头信息</h4>
          <el-tag v-for="(header, index) in previewData.headers" :key="index" style="margin: 4px;">
            {{ header }}
          </el-tag>
        </div>
      </div>
      <template #footer>
        <el-button @click="previewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Download, Delete, View, UploadFilled } from '@element-plus/icons-vue'
import request from '@/api/index.js'

const templates = ref([])
const loading = ref(false)
const uploadDialogVisible = ref(false)
const previewDialogVisible = ref(false)
const uploading = ref(false)
const uploadFormRef = ref(null)
const uploadRef = ref(null)
const previewData = ref(null)

const uploadForm = ref({
  templateName: '',
  templateType: '',
  file: null
})

const uploadRules = {
  templateName: [
    { required: true, message: '请输入模板名称', trigger: 'blur' }
  ],
  templateType: [
    { required: true, message: '请选择模板类型', trigger: 'change' }
  ],
  file: [
    { required: true, message: '请选择文件', trigger: 'change' }
  ]
}

const getTypeLabel = (type) => {
  const labels = {
    'IMPORT': '导入模板',
    'EXPORT': '导出模板',
    'MEITUAN': '美团模板'
  }
  return labels[type] || type
}

const getTypeTagType = (type) => {
  const types = {
    'IMPORT': 'success',
    'EXPORT': 'warning',
    'MEITUAN': 'primary'
  }
  return types[type] || 'info'
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return dateTime.replace('T', ' ')
}

const fetchTemplates = async () => {
  loading.value = true
  try {
    const response = await request.get('/templates', {
      params: { merchantId: 1 }
    })
    
    if (response.code === 200) {
      templates.value = response.data || []
    }
  } catch (error) {
    ElMessage.error('获取模板列表失败')
  } finally {
    loading.value = false
  }
}

const showUploadDialog = () => {
  uploadForm.value = {
    templateName: '',
    templateType: '',
    file: null
  }
  uploadDialogVisible.value = true
}

const handleFileChange = (file) => {
  uploadForm.value.file = file.raw
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

const handleUpload = async () => {
  if (!uploadFormRef.value) return
  
  await uploadFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    if (!uploadForm.value.file) {
      ElMessage.warning('请选择文件')
      return
    }
    
    uploading.value = true
    
    try {
      const formData = new FormData()
      formData.append('file', uploadForm.value.file)
      formData.append('templateName', uploadForm.value.templateName)
      formData.append('templateType', uploadForm.value.templateType)
      formData.append('merchantId', 1)
      
      const response = await request.post('/templates/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      
      if (response.code === 200) {
        ElMessage.success('模板上传成功')
        uploadDialogVisible.value = false
        fetchTemplates()
      }
    } catch (error) {
      ElMessage.error('模板上传失败')
    } finally {
      uploading.value = false
    }
  })
}

const handlePreview = async (row) => {
  try {
    const response = await request.get(`/templates/${row.id}/preview`)
    
    if (response.code === 200) {
      previewData.value = response.data
      previewDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('预览失败')
  }
}

const handleDownload = async (row) => {
  try {
    const response = await request.get(`/templates/${row.id}/download`, {
      responseType: 'blob'
    })
    
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([response]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', row.templateName + '.xlsx')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('下载成功')
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该模板吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request.delete(`/templates/${row.id}`, {
      params: { merchantId: 1 }
    })
    
    if (response.code === 200) {
      ElMessage.success('删除成功')
      fetchTemplates()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchTemplates()
})
</script>

<style scoped>
.template-container {
  width: 100%;
  min-height: 100%;
  background-color: #f5f5f5;
  padding: 16px;
}

.action-bar {
  margin-bottom: 16px;
}

.table-card {
  background-color: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.preview-content {
  padding: 16px 0;
}

.preview-headers {
  margin-top: 20px;
}

.preview-headers h4 {
  margin-bottom: 12px;
  color: #333;
}

:deep(.el-upload-dragger) {
  padding: 40px;
}
</style>
