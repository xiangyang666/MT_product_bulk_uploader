# Upload.vue 文件创建问题 - 解决方案

## 🐛 问题描述

Upload.vue 文件在创建后变成 0 字节，导致 Vite 报错：
```
[plugin:vite:vue] At least one <template> or <script> is required in a single file component.
```

## 🔍 问题原因

文件系统或 IDE 工具在写入大文件时出现问题，导致文件内容丢失。

## ✅ 解决方案

### 方案 1：手动创建文件（推荐）

1. **在 VS Code 或其他编辑器中打开**：
   ```
   meituan-frontend/src/views/Upload.vue
   ```

2. **复制以下完整代码并粘贴**：

```vue
<template>
  <div class="upload-container">
    <!-- 统计卡片区域 -->
    <div class="stats-section">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <el-icon class="stat-icon total"><Box /></el-icon>
          <div class="stat-info">
            <div class="stat-label">商品总数</div>
            <div class="stat-value">{{ stats.total || 0 }}</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <el-icon class="stat-icon pending"><Clock /></el-icon>
          <div class="stat-info">
            <div class="stat-label">待上传</div>
            <div class="stat-value">{{ stats.pending || 0 }}</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <el-icon class="stat-icon uploaded"><CircleCheck /></el-icon>
          <div class="stat-info">
            <div class="stat-label">已上传</div>
            <div class="stat-value">{{ stats.uploaded || 0 }}</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <el-icon class="stat-icon failed"><CircleClose /></el-icon>
          <div class="stat-info">
            <div class="stat-label">上传失败</div>
            <div class="stat-value">{{ stats.failed || 0 }}</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 主操作区域 -->
    <el-card class="action-section" shadow="hover">
      <div class="action-header">
        <h3>批量上传全部商品</h3>
        <p class="action-desc">一键生成包含所有商品的美团上传模板</p>
      </div>

      <div class="action-body">
        <el-button
          type="primary"
          size="large"
          class="generate-btn"
          :loading="generating"
          :disabled="stats.total === 0"
          @click="handleGenerateTemplate"
        >
          <el-icon v-if="!generating"><Download /></el-icon>
          {{ generating ? '生成中...' : '生成全部商品模板' }}
        </el-button>

        <div v-if="stats.total === 0" class="empty-tip">
          <el-icon><InfoFilled /></el-icon>
          <span>暂无商品数据，请先导入商品</span>
        </div>
      </div>

      <!-- 商品预览 -->
      <div v-if="recentProducts.length > 0" class="preview-section">
        <div class="preview-header">
          <span class="preview-title">最近导入的商品</span>
          <el-link type="primary" @click="goToProducts">查看全部商品 →</el-link>
        </div>

        <div class="preview-list">
          <div v-for="product in recentProducts" :key="product.id" class="preview-item">
            <div class="product-info">
              <span class="product-name">{{ product.productName }}</span>
              <span class="product-category">{{ product.categoryName }}</span>
            </div>
            <div class="product-meta">
              <span class="product-price">¥{{ product.price }}</span>
              <el-tag :type="getStatusType(product.status)" size="small">
                {{ getStatusText(product.status) }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 操作历史区域 -->
    <el-card class="history-section" shadow="hover">
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
          <div class="history-icon">
            <el-icon v-if="log.status === 'SUCCESS'" class="success-icon">
              <CircleCheck />
            </el-icon>
            <el-icon v-else class="failed-icon">
              <CircleClose />
            </el-icon>
          </div>

          <div class="history-content">
            <div class="history-title">
              <span>{{ getOperationText(log.operationType) }}</span>
              <span class="history-count">{{ log.productCount }} 个商品</span>
            </div>
            <div class="history-time">{{ formatTime(log.operationTime) }}</div>
          </div>

          <el-tag :type="log.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
            {{ log.status === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import { Box, Clock, CircleCheck, CircleClose, Download, InfoFilled, Document } from '@element-plus/icons-vue'
import { getProductStats, getRecentProducts, generateAllTemplate, getRecentOperations } from '@/api/index.js'

const router = useRouter()
const stats = ref({ total: 0, pending: 0, uploaded: 0, failed: 0 })
const recentProducts = ref([])
const recentLogs = ref([])
const loading = ref(false)
const generating = ref(false)

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

const loadPageData = async () => {
  loading.value = true
  try {
    const [statsRes, productsRes, logsRes] = await Promise.all([
      getProductStats(),
      getRecentProducts(1, 10),
      getRecentOperations(1, null, 3)
    ])
    stats.value = statsRes.data || { total: 0, pending: 0, uploaded: 0, failed: 0 }
    recentProducts.value = productsRes.data || []
    recentLogs.value = logsRes.data || []
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败，请刷新页面重试')
  } finally {
    loading.value = false
  }
}

const handleGenerateTemplate = async () => {
  if (stats.value.total === 0) {
    ElMessage.warning('暂无商品数据，无法生成模板')
    return
  }
  generating.value = true
  const loadingInstance = ElLoading.service({
    lock: true,
    text: `正在生成 ${stats.value.total} 个商品的模板，请稍候...`,
    background: 'rgba(0, 0, 0, 0.7)',
  })
  try {
    const blob = await generateAllTemplate()
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
    ElMessage.success(`成功生成包含 ${stats.value.total} 个商品的模板：${filename}`)
    setTimeout(() => { loadPageData() }, 1000)
  } catch (error) {
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
  } finally {
    generating.value = false
    loadingInstance.close()
  }
}

const goToProducts = () => {
  router.push('/products')
}

onMounted(() => {
  loadPageData()
})
</script>

<style scoped>
.upload-container {
  width: 100%;
  min-height: 100%;
  background-color: #f5f5f5;
  padding: 20px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  grid-template-areas: "stats stats stats" "action action history";
}
.stats-section {
  grid-area: stats;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.stat-card {
  border-radius: 12px;
  transition: transform 0.2s;
}
.stat-card:hover {
  transform: translateY(-4px);
}
.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-icon {
  font-size: 48px;
  flex-shrink: 0;
}
.stat-icon.total { color: #409eff; }
.stat-icon.pending { color: #e6a23c; }
.stat-icon.uploaded { color: #67c23a; }
.stat-icon.failed { color: #f56c6c; }
.stat-info { flex: 1; }
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: #303133;
}
.action-section {
  grid-area: action;
  border-radius: 12px;
}
.action-header {
  text-align: center;
  margin-bottom: 32px;
}
.action-header h3 {
  font-size: 24px;
  color: #303133;
  margin: 0 0 8px 0;
}
.action-desc {
  font-size: 14px;
  color: #909399;
  margin: 0;
}
.action-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
}
.generate-btn {
  width: 100%;
  max-width: 400px;
  height: 56px;
  font-size: 18px;
  font-weight: 600;
  background: linear-gradient(135deg, #FFD100 0%, #FFA500 100%);
  border: none;
  color: #333;
  transition: all 0.3s;
}
.generate-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(255, 209, 0, 0.3);
}
.generate-btn:active:not(:disabled) { transform: translateY(0); }
.generate-btn:disabled {
  background: #f5f5f5;
  color: #c0c4cc;
}
.empty-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
}
.preview-section {
  border-top: 1px solid #ebeef5;
  padding-top: 24px;
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
  color: #303133;
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
  padding: 12px;
  background-color: #f9fafb;
  border-radius: 8px;
  transition: background-color 0.2s;
}
.preview-item:hover { background-color: #f0f2f5; }
.product-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}
.product-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
.product-category {
  font-size: 12px;
  color: #909399;
}
.product-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}
.product-price {
  font-size: 16px;
  font-weight: 600;
  color: #FFD100;
}
.history-section {
  grid-area: history;
  border-radius: 12px;
}
.history-header { margin-bottom: 24px; }
.history-header h3 {
  font-size: 18px;
  color: #303133;
  margin: 0 0 4px 0;
}
.history-subtitle {
  font-size: 12px;
  color: #909399;
}
.empty-history {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #c0c4cc;
  gap: 12px;
}
.empty-history .el-icon { font-size: 48px; }
.history-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.history-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background-color: #f9fafb;
  border-radius: 8px;
  transition: background-color 0.2s;
}
.history-item:hover { background-color: #f0f2f5; }
.history-icon { flex-shrink: 0; }
.history-icon .el-icon { font-size: 24px; }
.success-icon { color: #67c23a; }
.failed-icon { color: #f56c6c; }
.history-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.history-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
.history-count {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}
.history-time {
  font-size: 12px;
  color: #909399;
}
@media (max-width: 1200px) {
  .upload-container {
    grid-template-columns: 1fr;
    grid-template-areas: "stats" "action" "history";
  }
  .stats-section { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .upload-container {
    padding: 12px;
    gap: 12px;
  }
  .stats-section {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  .stat-value { font-size: 24px; }
  .action-header h3 { font-size: 20px; }
  .generate-btn {
    height: 48px;
    font-size: 16px;
  }
}
</style>
```

3. **保存文件**（Ctrl + S）

4. **重启 Vite 开发服务器**：
   ```bash
   # 停止服务器（Ctrl + C）
   cd meituan-frontend
   npm run dev
   ```

## 📋 验证

1. 检查文件大小：
   ```bash
   dir meituan-frontend\src\views\Upload.vue
   ```
   应该显示约 8-10 KB

2. 访问页面：
   ```
   http://localhost:5173/upload
   ```

## 🎯 预期结果

页面应该显示：
- 4 个统计卡片（商品总数、待上传、已上传、失败）
- 大号黄色"生成全部商品模板"按钮
- 商品预览列表（如果有商品）
- 操作历史记录（如果有操作）

---

**问题类型**：文件系统写入问题  
**解决方案**：手动在编辑器中创建文件  
**状态**：需要手动操作
