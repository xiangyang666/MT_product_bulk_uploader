<template>
  <div class="upload-container">
    <!-- 统计卡片区域 - iPhone风格 -->
    <div class="stats-section">
      <div class="stat-card total">
        <div class="stat-icon-wrapper">
          <el-icon class="stat-icon"><Box /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalCount || 0 }}</div>
          <div class="stat-label">商品总数</div>
        </div>
      </div>

      <div class="stat-card pending">
        <div class="stat-icon-wrapper">
          <el-icon class="stat-icon"><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.pendingCount || 0 }}</div>
          <div class="stat-label">待上传</div>
        </div>
      </div>

      <div class="stat-card uploaded">
        <div class="stat-icon-wrapper">
          <el-icon class="stat-icon"><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.uploadedCount || 0 }}</div>
          <div class="stat-label">已上传</div>
        </div>
      </div>

      <div class="stat-card failed">
        <div class="stat-icon-wrapper">
          <el-icon class="stat-icon"><CircleClose /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.failedCount || 0 }}</div>
          <div class="stat-label">上传失败</div>
        </div>
      </div>
    </div>

    <!-- 主操作区域 - iPhone风格 -->
    <div class="action-section">
      <div class="action-card">
        <div class="action-header">
          <h3>批量上传全部商品</h3>
          <p class="action-desc">一键生成包含所有商品的美团上传模板</p>
        </div>

        <div class="action-body">
          <button
            class="generate-btn"
            :class="{ 'is-loading': generating, 'is-disabled': stats.totalCount === 0 }"
            :disabled="stats.totalCount === 0 || generating"
            @click="handleGenerateTemplate"
          >
            <span v-if="generating" class="btn-loading">
              <span class="loading-spinner"></span>
              生成中...
            </span>
            <span v-else class="btn-content">
              <el-icon><Download /></el-icon>
              生成全部商品模板
            </span>
          </button>

          <!-- 炫酷进度条 -->
          <div v-if="generating" class="progress-container">
            <div class="progress-info">
              <span class="progress-text">{{ progressText }}</span>
              <span class="progress-percent">{{ progress }}%</span>
            </div>
            <el-progress 
              :percentage="progress" 
              :stroke-width="8"
              :show-text="false"
              :color="progressColors"
              class="custom-progress"
            />
            <div class="progress-stats">
              <span>正在处理 {{ stats.totalCount }} 个商品</span>
            </div>
          </div>

          <div v-if="stats.totalCount === 0" class="tip-box empty">
            <el-icon><InfoFilled /></el-icon>
            <span>暂无商品数据，请先导入商品</span>
          </div>
          
          <div v-else-if="!generating" class="tip-box success">
            <el-icon><InfoFilled /></el-icon>
            <span>将使用您在模板管理中上传的美团模板</span>
          </div>
        </div>

        <!-- 商品预览 -->
        <div v-if="recentProducts.length > 0" class="preview-section">
          <div class="preview-header">
            <span class="preview-title">最近导入的商品</span>
            <span class="preview-link" @click="goToProducts">查看全部 →</span>
          </div>

          <div class="preview-list">
            <div v-for="product in recentProducts" :key="product.id" class="preview-item">
              <div class="product-info">
                <span class="product-name">{{ product.productName }}</span>
                <span class="product-category">{{ product.categoryName }}</span>
              </div>
              <div class="product-meta">
                <span class="product-price">¥{{ product.price }}</span>
                <span class="product-status" :class="`status-${product.status}`">
                  {{ getStatusText(product.status) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作历史区域 - iPhone风格 -->
    <div class="history-section">
      <div class="history-card">
        <div class="history-header">
          <h3>操作历史</h3>
          <span class="history-subtitle">最近3次操作记录</span>
        </div>

        <div v-if="recentLogs.length === 0" class="empty-history">
          <el-icon><Document /></el-icon>
          <span>暂无操作记录</span>
        </div>

        <div v-else class="history-list">
          <div v-for="log in recentLogs" :key="log.id" class="history-item">
            <div class="history-icon" :class="log.status === 'SUCCESS' ? 'success' : 'failed'">
              <el-icon v-if="log.status === 'SUCCESS'"><CircleCheck /></el-icon>
              <el-icon v-else><CircleClose /></el-icon>
            </div>

            <div class="history-content">
              <div class="history-title">
                <span>{{ getOperationText(log.operationType) }}</span>
                <span class="history-count">{{ log.productCount }} 个商品</span>
              </div>
              <div class="history-time">{{ formatTime(log.operationTime) }}</div>
            </div>

            <span class="history-status" :class="log.status === 'SUCCESS' ? 'success' : 'failed'">
              {{ log.status === 'SUCCESS' ? '成功' : '失败' }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 历史文件区域 -->
    <div class="history-files-section">
      <div class="history-files-card">
        <div class="history-files-header">
          <h3>历史文件</h3>
          <span class="history-files-subtitle">最近生成的模板文件</span>
        </div>

        <div v-if="historyFiles.length === 0" class="empty-files">
          <el-icon><Document /></el-icon>
          <span>暂无历史文件</span>
        </div>

        <div v-else class="files-list">
          <div v-for="file in historyFiles" :key="file.id" class="file-item">
            <div class="file-icon">
              <el-icon><Document /></el-icon>
            </div>

            <div class="file-content">
              <div class="file-name">{{ file.fileName }}</div>
              <div class="file-meta">
                <span>{{ file.fileSizeFormatted }}</span>
                <span>{{ file.productCount }} 个商品</span>
                <span>{{ formatTime(file.createdAt) }}</span>
                <span>下载 {{ file.downloadCount }} 次</span>
              </div>
            </div>

            <el-button 
              type="primary" 
              size="small"
              @click="handleDownloadFile(file)"
              style="background-color: #FFD100; border-color: #FFD100; color: #333;"
            >
              <el-icon><Download /></el-icon>
              下载
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Box, Clock, CircleCheck, CircleClose, Download, InfoFilled, Document } from '@element-plus/icons-vue'
import { getProductStats, getRecentProducts, generateAllTemplate, getRecentOperations, getRecentGeneratedFiles, downloadGeneratedFile } from '@/api/index.js'

const router = useRouter()
const stats = ref({ totalCount: 0, pendingCount: 0, uploadedCount: 0, failedCount: 0 })
const recentProducts = ref([])
const recentLogs = ref([])
const historyFiles = ref([])
const loading = ref(false)
const generating = ref(false)

// 进度条相关
const progress = ref(0)
const progressText = ref('准备生成模板...')
const progressColors = [
  { color: '#FFD100', percentage: 30 },
  { color: '#FFA500', percentage: 70 },
  { color: '#FF8C00', percentage: 100 }
]

const getStatusType = (status) => {
  const typeMap = { 0: 'info', 1: 'success', 2: 'danger' }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = { 0: '待上传', 1: '已上传', 2: '失败' }
  return textMap[status] || '待上传'
}

const getOperationText = (type) => {
  const textMap = {
    'GENERATE_ALL': '生成全部商品模板',
    'GENERATE_TEMPLATE': '生成商品模板',
    'IMPORT': '导入商品',
    'UPLOAD': '上传商品'
  }
  return textMap[type] || type
}

const formatTime = (timeString) => {
  if (!timeString) return '-'
  const date = new Date(timeString)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  return date.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

// 模拟进度更新
const simulateProgress = () => {
  progress.value = 0
  progressText.value = '正在读取商品数据...'
  
  const stages = [
    { percent: 20, text: '正在读取商品数据...' },
    { percent: 40, text: '正在加载模板文件...' },
    { percent: 60, text: '正在填充商品信息...' },
    { percent: 80, text: '正在生成Excel文件...' },
    { percent: 95, text: '正在压缩文件...' },
    { percent: 100, text: '生成完成！' }
  ]
  
  let currentStage = 0
  const interval = setInterval(() => {
    if (currentStage < stages.length && generating.value) {
      progress.value = stages[currentStage].percent
      progressText.value = stages[currentStage].text
      currentStage++
    } else {
      clearInterval(interval)
    }
  }, 500)
  
  return interval
}

const loadPageData = async () => {
  loading.value = true
  try {
    const [statsRes, productsRes, logsRes, filesRes] = await Promise.all([
      getProductStats(),
      getRecentProducts(1, 10),
      getRecentOperations(1, null, 3),
      getRecentGeneratedFiles(1, 10)
    ])
    
    stats.value = statsRes.data || { totalCount: 0, pendingCount: 0, uploadedCount: 0, failedCount: 0 }
    recentProducts.value = productsRes.data || []
    recentLogs.value = logsRes.data || []
    historyFiles.value = filesRes.data || []
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败，请刷新页面重试')
  } finally {
    loading.value = false
  }
}

const handleGenerateTemplate = async () => {
  if (stats.value.totalCount === 0) {
    ElMessage.warning('暂无商品数据，无法生成模板')
    return
  }
  
  generating.value = true
  const progressInterval = simulateProgress()
  
  try {
    const blob = await generateAllTemplate()
    
    // 确保进度条到100%
    progress.value = 100
    progressText.value = '生成完成！'
    
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    const timestamp = new Date().toISOString().replace(/[-:]/g, '').replace('T', '_').split('.')[0]
    const filename = `meituan_all_products_${timestamp}.xlsx`
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success(`成功生成包含 ${stats.value.totalCount} 个商品的模板：${filename}`)
    
    // 延迟关闭进度条，让用户看到100%
    setTimeout(() => {
      generating.value = false
      clearInterval(progressInterval)
      loadPageData()
    }, 800)
  } catch (error) {
    clearInterval(progressInterval)
    console.error('生成模板失败:', error)
    if (error.response?.status === 500) {
      ElMessage.error('服务器错误，请稍后重试')
    } else if (error.response?.status === 400) {
      ElMessage.error('请求参数错误，请刷新页面重试')
    } else if (error.message?.includes('timeout')) {
      ElMessage.error('生成超时，商品数量过多，请联系管理员')
    } else {
      ElMessage.error('生成模板失败，请重试')
    }
    generating.value = false
  }
}

const goToProducts = () => {
  router.push('/products')
}

const handleDownloadFile = async (file) => {
  try {
    const blob = await downloadGeneratedFile(file.id)
    
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = file.fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('文件下载成功')
    
    // 刷新列表（更新下载次数）
    loadPageData()
  } catch (error) {
    console.error('下载文件失败:', error)
    ElMessage.error('下载文件失败')
  }
}

onMounted(() => {
  loadPageData()
})
</script>

<style scoped>
/* 与首页一致的风格 */
.upload-container {
  width: 100%;
  min-height: 100vh;
  background-color: #fafbfc;
  padding: 0;
}

/* 统计卡片 */
.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin: 24px 24px 32px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(0, 0, 0, 0.06);
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.08);
}

.stat-icon-wrapper {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-card.total .stat-icon-wrapper {
  background: rgba(59, 130, 246, 0.1);
}

.stat-card.total .stat-icon {
  font-size: 24px;
  color: #3b82f6;
}

.stat-card.pending .stat-icon-wrapper {
  background: rgba(245, 158, 11, 0.1);
}

.stat-card.pending .stat-icon {
  font-size: 24px;
  color: #f59e0b;
}

.stat-card.uploaded .stat-icon-wrapper {
  background: rgba(34, 197, 94, 0.1);
}

.stat-card.uploaded .stat-icon {
  font-size: 24px;
  color: #22c55e;
}

.stat-card.failed .stat-icon-wrapper {
  background: rgba(239, 68, 68, 0.1);
}

.stat-card.failed .stat-icon {
  font-size: 24px;
  color: #ef4444;
}

.stat-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
}

/* 主操作区域 */
.action-section {
  margin: 0 24px 32px;
}

.action-card {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.action-header {
  text-align: center;
  margin-bottom: 32px;
}

.action-header h3 {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.action-desc {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  font-weight: 500;
}

.action-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
}

/* 按钮样式 */
.generate-btn {
  width: 100%;
  max-width: 400px;
  height: 52px;
  border-radius: 12px;
  border: none;
  background: #FFD100;
  color: #1a1a1a;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(255, 209, 0, 0.2);
}

.generate-btn:hover:not(.is-disabled):not(.is-loading) {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(255, 209, 0, 0.3);
}

.generate-btn:active:not(.is-disabled):not(.is-loading) {
  transform: translateY(0);
}

.generate-btn.is-disabled {
  background: #f3f4f6;
  color: #9ca3af;
  box-shadow: none;
  cursor: not-allowed;
}

.generate-btn.is-loading {
  background: #FFD100;
  opacity: 0.8;
  cursor: wait;
}

.btn-content, .btn-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(26, 26, 26, 0.3);
  border-top-color: #1a1a1a;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 提示框 */
.tip-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
}

.tip-box.empty {
  background: #f3f4f6;
  color: #6b7280;
}

.tip-box.success {
  background: rgba(34, 197, 94, 0.1);
  color: #16a34a;
}

/* 炫酷进度条 */
.progress-container {
  width: 100%;
  max-width: 500px;
  padding: 24px;
  background: linear-gradient(135deg, #fff9e6 0%, #fff3d6 100%);
  border-radius: 16px;
  border: 2px solid #FFD100;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.progress-text {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
}

.progress-percent {
  font-size: 18px;
  font-weight: 700;
  color: #FFD100;
  font-family: 'Courier New', monospace;
}

.custom-progress {
  margin-bottom: 12px;
}

.custom-progress :deep(.el-progress-bar__outer) {
  background-color: rgba(255, 209, 0, 0.2);
  border-radius: 10px;
  overflow: hidden;
}

.custom-progress :deep(.el-progress-bar__inner) {
  border-radius: 10px;
  transition: width 0.3s ease;
  background: linear-gradient(90deg, #FFD100 0%, #FFA500 50%, #FF8C00 100%);
  background-size: 200% 100%;
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.progress-stats {
  text-align: center;
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
}

/* 商品预览 */
.preview-section {
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  padding-top: 24px;
  margin-top: 8px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.preview-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.preview-link {
  font-size: 14px;
  color: #3b82f6;
  cursor: pointer;
  font-weight: 500;
  transition: opacity 0.2s;
}

.preview-link:hover {
  opacity: 0.7;
}

.preview-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.preview-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #fafbfc;
  border-radius: 10px;
  transition: all 0.2s;
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.preview-item:hover {
  background: #f3f4f6;
  transform: translateX(4px);
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.product-name {
  font-size: 15px;
  color: #1a1a1a;
  font-weight: 600;
}

.product-category {
  font-size: 13px;
  color: #6b7280;
}

.product-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-price {
  font-size: 16px;
  font-weight: 700;
  color: #FFD100;
}

.product-status {
  padding: 4px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
}

.product-status.status-0 {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.product-status.status-1 {
  background: rgba(34, 197, 94, 0.1);
  color: #22c55e;
}

.product-status.status-2 {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

/* 操作历史 */
.history-section {
  margin: 0 24px 24px;
}

.history-card {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.history-header {
  margin-bottom: 24px;
}

.history-header h3 {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 4px 0;
}

.history-subtitle {
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
}

.empty-history {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #9ca3af;
  gap: 12px;
}

.empty-history .el-icon {
  font-size: 48px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  transition: all 0.2s;
}

.history-item:hover {
  background-color: rgba(0, 0, 0, 0.02);
}

.history-item:last-child {
  border-bottom: none;
}

.history-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.history-icon.success {
  background: rgba(34, 197, 94, 0.1);
}

.history-icon.success .el-icon {
  color: #22c55e;
  font-size: 20px;
}

.history-icon.failed {
  background: rgba(239, 68, 68, 0.1);
}

.history-icon.failed .el-icon {
  color: #ef4444;
  font-size: 20px;
}

.history-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.history-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  color: #1a1a1a;
  font-weight: 600;
}

.history-count {
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
}

.history-time {
  font-size: 12px;
  color: #9ca3af;
}

.history-status {
  padding: 4px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.history-status.success {
  background: rgba(34, 197, 94, 0.1);
  color: #22c55e;
}

.history-status.failed {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .upload-container {
    grid-template-columns: 1fr;
    grid-template-areas: "stats" "action" "history";
  }
  .stats-section {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-section {
    grid-template-columns: 1fr;
    gap: 12px;
    margin: 16px 16px 24px;
  }
  .stat-card {
    padding: 16px;
  }
  .stat-value {
    font-size: 24px;
  }
  .action-section, .history-section {
    margin: 0 16px 24px;
  }
  .action-card, .history-card {
    padding: 24px;
  }
  .action-header h3 {
    font-size: 20px;
  }
  .generate-btn {
    height: 48px;
    font-size: 15px;
  }
}

/* 历史文件区域 */
.history-files-section {
  margin: 0 24px 24px;
}

.history-files-card {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.history-files-header {
  margin-bottom: 24px;
}

.history-files-header h3 {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 4px 0;
}

.history-files-subtitle {
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
}

.empty-files {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #9ca3af;
  gap: 12px;
}

.empty-files .el-icon {
  font-size: 48px;
}

.files-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #fafbfc;
  border-radius: 10px;
  transition: all 0.2s;
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.file-item:hover {
  background: #f3f4f6;
  transform: translateX(4px);
}

.file-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(255, 209, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.file-icon .el-icon {
  font-size: 24px;
  color: #FFD100;
}

.file-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.file-name {
  font-size: 15px;
  color: #1a1a1a;
  font-weight: 600;
}

.file-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #6b7280;
}
</style>