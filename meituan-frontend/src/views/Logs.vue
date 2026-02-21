<template>
  <div class="logs-container">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="操作类型">
          <el-select v-model="filterForm.operationType" placeholder="全部" clearable style="width: 150px">
            <el-option label="导入商品" value="IMPORT" />
            <el-option label="生成模板" value="GENERATE" />
            <el-option label="批量上传" value="UPLOAD" />
            <el-option label="清空商品" value="CLEAR" />
            <el-option label="更新设置" value="SETTINGS_UPDATE" />
            <el-option label="上传模板" value="TEMPLATE_UPLOAD" />
            <el-option label="删除模板" value="TEMPLATE_DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 380px"
          />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="filterForm.keyword"
            placeholder="搜索操作详情"
            clearable
            style="width: 200px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" style="background-color: #FFD100; border-color: #FFD100; color: #333;">
            查询
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 日志表格 -->
    <div class="table-card">
      <el-table
        :data="logs"
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="operationType" label="操作类型" width="120">
          <template #default="scope">
            <el-tag :type="getOperationTypeTag(scope.row.operationType)" size="small">
              {{ getOperationTypeLabel(scope.row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationDetail" label="操作详情" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="successCount" label="成功数" width="80" />
        <el-table-column prop="failedCount" label="失败数" width="80" />
        <el-table-column prop="duration" label="耗时" width="100">
          <template #default="scope">
            {{ scope.row.duration ? scope.row.duration + 'ms' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="日志详情"
      width="600px"
    >
      <div v-if="currentLog" class="log-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="操作类型">
            <el-tag :type="getOperationTypeTag(currentLog.operationType)" size="small">
              {{ getOperationTypeLabel(currentLog.operationType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作详情">{{ currentLog.operationDetail }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentLog.status)" size="small">
              {{ getStatusLabel(currentLog.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="成功数量">{{ currentLog.successCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="失败数量">{{ currentLog.failedCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="耗时">{{ currentLog.duration ? currentLog.duration + 'ms' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentLog.createdTime) }}</el-descriptions-item>
          <el-descriptions-item label="错误信息" v-if="currentLog.errorMessage">
            <div class="error-message">{{ currentLog.errorMessage }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, View } from '@element-plus/icons-vue'
import request from '@/api/index.js'

const logs = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const detailDialogVisible = ref(false)
const currentLog = ref(null)

const filterForm = ref({
  operationType: '',
  dateRange: null,
  keyword: ''
})

const getOperationTypeLabel = (type) => {
  const labels = {
    'IMPORT': '导入商品',
    'GENERATE': '生成模板',
    'UPLOAD': '批量上传',
    'CLEAR': '清空商品',
    'SETTINGS_UPDATE': '更新设置',
    'TEMPLATE_UPLOAD': '上传模板',
    'TEMPLATE_DELETE': '删除模板'
  }
  return labels[type] || type
}

const getOperationTypeTag = (type) => {
  const tags = {
    'IMPORT': 'success',
    'GENERATE': 'primary',
    'UPLOAD': 'warning',
    'CLEAR': 'danger',
    'SETTINGS_UPDATE': 'info',
    'TEMPLATE_UPLOAD': 'success',
    'TEMPLATE_DELETE': 'danger'
  }
  return tags[type] || 'info'
}

const getStatusLabel = (status) => {
  const labels = {
    0: '进行中',
    1: '成功',
    2: '失败'
  }
  return labels[status] || '未知'
}

const getStatusType = (status) => {
  const types = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return types[status] || 'info'
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return dateTime.replace('T', ' ')
}

const fetchLogs = async () => {
  loading.value = true
  try {
    const params = {
      merchantId: 1,
      page: currentPage.value,
      size: pageSize.value
    }
    
    if (filterForm.value.operationType) {
      params.operationType = filterForm.value.operationType
    }
    
    if (filterForm.value.dateRange && filterForm.value.dateRange.length === 2) {
      params.startTime = filterForm.value.dateRange[0]
      params.endTime = filterForm.value.dateRange[1]
    }
    
    if (filterForm.value.keyword) {
      params.keyword = filterForm.value.keyword
    }
    
    const response = await request.get('/logs', { params })
    
    if (response.code === 200) {
      logs.value = response.data.list || []
      total.value = response.data.total || 0
    }
  } catch (error) {
    ElMessage.error('获取日志列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchLogs()
}

const handleReset = () => {
  filterForm.value = {
    operationType: '',
    dateRange: null,
    keyword: ''
  }
  currentPage.value = 1
  fetchLogs()
}

const handleSizeChange = () => {
  fetchLogs()
}

const handleCurrentChange = () => {
  fetchLogs()
}

const handleViewDetail = (row) => {
  currentLog.value = row
  detailDialogVisible.value = true
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped>
.logs-container {
  width: 100%;
  min-height: 100%;
  background-color: #f5f5f5;
  padding: 16px;
}

.filter-bar {
  background-color: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 16px;
}

.filter-form {
  margin: 0;
}

.table-card {
  background-color: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 16px;
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 16px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.log-detail {
  padding: 16px 0;
}

.error-message {
  color: #f56c6c;
  word-break: break-all;
}
</style>
