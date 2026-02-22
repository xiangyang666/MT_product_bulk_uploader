<template>
  <div class="version-list">
    <el-table
      v-loading="loading"
      :data="versions"
      style="width: 100%"
      empty-text="暂无版本记录"
    >
      <el-table-column prop="version" label="版本号" width="120">
        <template #default="{ row }">
          <div class="version-cell">
            <span class="version-number">v{{ row.version }}</span>
            <el-tag v-if="row.isLatest" type="success" size="small" class="latest-tag">
              官网展示
            </el-tag>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="platform" label="平台" width="100">
        <template #default="{ row }">
          <el-tag :type="row.platform === 'Windows' ? 'primary' : 'success'">
            {{ row.platform }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />

      <el-table-column prop="fileSizeFormatted" label="文件大小" width="100" />

      <el-table-column prop="downloadCount" label="下载次数" width="100" align="center">
        <template #default="{ row }">
          <el-text type="info">{{ row.downloadCount || 0 }}</el-text>
        </template>
      </el-table-column>

      <el-table-column prop="uploadedBy" label="上传者" width="100" />

      <el-table-column prop="createdAt" label="上传时间" width="160">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="!row.isLatest"
            type="primary"
            size="small"
            link
            @click="handleSetLatest(row)"
          >
            设为官网展示
          </el-button>
          <el-tag v-else type="success" size="small">
            当前展示版本
          </el-tag>
          <el-button
            type="warning"
            size="small"
            link
            @click="handleEdit(row)"
          >
            编辑
          </el-button>
          <el-button
            type="success"
            size="small"
            link
            @click="handleDownload(row)"
          >
            下载
          </el-button>
          <el-button
            type="danger"
            size="small"
            link
            @click="handleDelete(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>

      <el-table-column type="expand">
        <template #default="{ row }">
          <div class="expand-content" v-if="row.releaseNotes">
            <div class="expand-label">发布说明：</div>
            <div class="expand-text">{{ row.releaseNotes }}</div>
          </div>
          <div class="expand-content" v-else>
            <el-text type="info">暂无发布说明</el-text>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > 0"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handlePageChange"
      class="pagination"
    />

    <el-empty v-if="!loading && versions.length === 0" description="暂无版本记录">
      <el-button type="primary" @click="$emit('upload')">上传第一个版本</el-button>
    </el-empty>

    <!-- 编辑版本对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑版本信息"
      width="600px"
      @close="handleEditDialogClose"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
      >
        <el-form-item label="版本号" prop="version">
          <el-input v-model="editForm.version" placeholder="例如：1.0.0" />
        </el-form-item>
        <el-form-item label="平台">
          <el-input v-model="editForm.platform" disabled />
        </el-form-item>
        <el-form-item label="文件名">
          <el-input v-model="editForm.fileName" disabled />
        </el-form-item>
        <el-form-item label="发布说明" prop="releaseNotes">
          <el-input
            v-model="editForm.releaseNotes"
            type="textarea"
            :rows="6"
            placeholder="请输入版本更新说明..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit" :loading="editSubmitting">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getVersionList, setLatestVersion, deleteVersion, getDownloadUrl } from '@/api'
import request from '@/api/index.js'

const props = defineProps({
  platform: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['upload', 'refresh'])

const loading = ref(false)
const versions = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 编辑相关
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const editSubmitting = ref(false)
const editForm = ref({
  id: null,
  version: '',
  platform: '',
  fileName: '',
  releaseNotes: ''
})

const editRules = {
  version: [
    { required: true, message: '请输入版本号', trigger: 'blur' },
    { pattern: /^\d+\.\d+\.\d+$/, message: '版本号格式应为 x.x.x', trigger: 'blur' }
  ]
}

// 加载版本列表
const loadVersions = async () => {
  loading.value = true
  try {
    const result = await getVersionList(props.platform, currentPage.value, pageSize.value)
    versions.value = result.data.records || []
    total.value = result.data.total || 0
  } catch (error) {
    console.error('加载版本列表失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 设置为官网展示版本
const handleSetLatest = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定将版本 ${row.version} 设置为官网展示版本吗？设置后，用户在官网将看到并下载此版本。`,
      '设置官网展示版本',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await setLatestVersion(row.id)
    ElMessage.success('设置成功，官网将展示此版本')
    loadVersions()
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('设置展示版本失败:', error)
      ElMessage.error('设置失败')
    }
  }
}

// 编辑版本
const handleEdit = (row) => {
  editForm.value = {
    id: row.id,
    version: row.version,
    platform: row.platform,
    fileName: row.fileName,
    releaseNotes: row.releaseNotes || ''
  }
  editDialogVisible.value = true
}

// 提交编辑
const handleEditSubmit = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    editSubmitting.value = true
    try {
      await request.put(`/app-versions/${editForm.value.id}`, {
        version: editForm.value.version,
        releaseNotes: editForm.value.releaseNotes
      })
      
      ElMessage.success('更新成功')
      editDialogVisible.value = false
      loadVersions()
      emit('refresh')
    } catch (error) {
      console.error('更新版本信息失败:', error)
      ElMessage.error('更新失败')
    } finally {
      editSubmitting.value = false
    }
  })
}

// 关闭编辑对话框
const handleEditDialogClose = () => {
  editFormRef.value?.resetFields()
}

// 下载版本
const handleDownload = async (row) => {
  try {
    const result = await getDownloadUrl(row.id)
    const url = result.data
    
    // 在新窗口打开下载链接
    window.open(url, '_blank')
    
    ElMessage.success('开始下载')
    
    // 刷新列表以更新下载次数
    setTimeout(() => {
      loadVersions()
    }, 1000)
  } catch (error) {
    console.error('获取下载链接失败:', error)
    ElMessage.error('下载失败')
  }
}

// 删除版本
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除版本 ${row.version} 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteVersion(row.id)
    ElMessage.success('删除成功')
    loadVersions()
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除版本失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 分页变化
const handlePageChange = (page) => {
  currentPage.value = page
  loadVersions()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadVersions()
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 监听平台变化
watch(() => props.platform, () => {
  currentPage.value = 1
  loadVersions()
})

// 组件挂载时加载数据
onMounted(() => {
  loadVersions()
})

// 暴露刷新方法
defineExpose({
  refresh: loadVersions
})
</script>

<style scoped>
.version-list {
  margin-top: 20px;
}

.version-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.version-number {
  font-weight: 600;
  color: #303133;
}

.latest-tag {
  margin-left: 4px;
}

.expand-content {
  padding: 16px 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin: 8px 0;
}

.expand-label {
  font-weight: 600;
  color: #606266;
  margin-bottom: 8px;
}

.expand-text {
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
