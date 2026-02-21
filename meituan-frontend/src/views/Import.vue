<template>
  <div class="import-container">
    <div class="import-card">
      <el-steps :active="currentStep" finish-status="success" simple>
        <el-step title="选择文件" />
        <el-step title="数据预览" />
        <el-step title="导入完成" />
      </el-steps>

      <!-- 步骤1: 选择文件 -->
      <div v-if="currentStep === 0" class="step-content">
        <div class="template-download">
          <el-button 
            type="primary" 
            plain
            @click="handleDownloadTemplate"
            :loading="downloading"
          >
            <el-icon><Download /></el-icon>
            {{ downloading ? '下载中...' : '下载导入模板' }}
          </el-button>
          <span class="template-tip">首次使用？先下载模板了解格式</span>
        </div>

        <el-upload
          ref="uploadRef"
          drag
          :auto-upload="false"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          :limit="1"
          accept=".xlsx,.xls"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            将 Excel 文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持 .xlsx 和 .xls 格式，文件大小不超过 10MB
            </div>
          </template>
        </el-upload>

        <div v-if="selectedFile" class="file-info">
          <div class="file-selected">
            <el-icon :size="24" color="#FFD100"><Document /></el-icon>
            <div class="file-details">
              <div class="file-name">{{ selectedFile.name }}</div>
              <div class="file-size">{{ formatFileSize(selectedFile.size) }}</div>
            </div>
          </div>
          <el-button 
            type="primary" 
            :loading="previewing"
            @click="handlePreview"
            style="background-color: #FFD100; border-color: #FFD100; color: #333; margin-top: 16px;"
          >
            {{ previewing ? '解析中...' : '下一步：预览数据' }}
          </el-button>
        </div>

        <el-alert
          title="导入说明"
          type="info"
          :closable="false"
          style="margin-top: 20px"
        >
          <div>
            <p>1. Excel 文件第一行为表头，从第二行开始为数据</p>
            <p>2. 必填字段：商品名称、类目ID、价格</p>
            <p>3. 可选字段：库存、商品描述、图片URL</p>
            <p>4. 类目ID必须是10位数字</p>
          </div>
        </el-alert>
      </div>

      <!-- 步骤2: 数据预览 -->
      <div v-if="currentStep === 1" class="step-content">
        <!-- 格式识别信息 -->
        <el-alert
          :title="previewResult.formatMessage || '数据预览'"
          :type="previewResult.formatType === 'MEITUAN' ? 'success' : 'info'"
          :closable="false"
          style="margin-bottom: 16px"
        >
          <div class="preview-stats">
            <span>总计：{{ previewResult.totalCount }} 行</span>
            <span class="success-text">预计成功：{{ previewResult.successCount }} 行</span>
            <span v-if="previewResult.failedCount > 0" class="error-text">
              存在错误：{{ previewResult.failedCount }} 行
            </span>
            <span v-if="previewResult.hasMoreData" class="info-text">
              （仅显示前20行）
            </span>
          </div>
        </el-alert>

        <!-- 错误详情（如果有） -->
        <el-alert
          v-if="previewResult.errors && previewResult.errors.length > 0"
          title="数据验证错误"
          type="warning"
          :closable="false"
          style="margin-bottom: 16px"
        >
          <div class="error-list">
            <div v-for="(error, index) in previewResult.errors.slice(0, 5)" :key="index" class="error-item">
              第{{ error.rowNum }}行 - {{ error.fieldName }}: {{ error.message }}
              <span v-if="error.originalValue" class="error-value">（值：{{ error.originalValue }}）</span>
            </div>
            <div v-if="previewResult.errors.length > 5" class="more-errors">
              还有 {{ previewResult.errors.length - 5 }} 条错误...
            </div>
            <div v-if="previewResult.hasMoreErrors" class="more-errors">
              共 {{ previewResult.remainingErrorCount }} 条错误未显示
            </div>
          </div>
        </el-alert>

        <el-table 
          :data="previewData" 
          style="width: 100%" 
          max-height="500"
          border
        >
          <el-table-column type="index" label="序号" width="60" fixed="left" />
          
          <!-- 基础信息 -->
          <el-table-column prop="skuId" label="SKU ID" width="120" show-overflow-tooltip />
          <el-table-column prop="upcEan" label="条形码" width="130" show-overflow-tooltip />
          <el-table-column prop="categoryName" label="商品类目名称" width="150" show-overflow-tooltip />
          <el-table-column prop="categoryId" label="商品类目ID" width="120" show-overflow-tooltip />
          <el-table-column prop="appSpuCode" label="APP SPU编码" width="130" show-overflow-tooltip />
          <el-table-column prop="productName" label="商品名称" width="200" show-overflow-tooltip />
          
          <!-- 图片视频 -->
          <el-table-column label="商品图片" width="100">
            <template #default="{ row }">
              <span v-if="row.productImage || row.imageUrl" style="color: #67c23a;">✓ 有</span>
              <span v-else style="color: #f56c6c;">✗ 无</span>
            </template>
          </el-table-column>
          <el-table-column prop="coverVideo" label="封面视频" width="100" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="row.coverVideo" style="color: #67c23a;">✓ 有</span>
              <span v-else style="color: #f56c6c;">✗ 无</span>
            </template>
          </el-table-column>
          <el-table-column prop="specImage" label="规格图" width="100" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="row.specImage" style="color: #67c23a;">✓ 有</span>
              <span v-else style="color: #f56c6c;">✗ 无</span>
            </template>
          </el-table-column>
          
          <!-- 分类库存 -->
          <el-table-column prop="storeCategory" label="店内分类" width="120" show-overflow-tooltip />
          <el-table-column prop="storeCategoryCount" label="店内分类数量" width="120" />
          <el-table-column prop="specName" label="规格名称" width="120" show-overflow-tooltip />
          <el-table-column prop="storeCode" label="店内码/货号" width="120" show-overflow-tooltip />
          <el-table-column prop="price" label="价格(元)" width="100">
            <template #default="{ row }">
              <span style="color: #FFD100; font-weight: 600;">¥{{ row.price || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="stock" label="库存" width="80" />
          <el-table-column prop="saleStatus" label="售卖状态" width="100" />
          <el-table-column prop="monthlySales" label="月售" width="80" />
          <el-table-column prop="weight" label="重量" width="80" />
          <el-table-column prop="weightUnit" label="重量单位" width="90" />
          <el-table-column prop="minPurchase" label="起购数" width="80" />
          <el-table-column prop="shelfCode" label="货架码/位置码" width="130" show-overflow-tooltip />
          
          <!-- 详情描述 -->
          <el-table-column prop="sellingPoint" label="商品卖点" width="150" show-overflow-tooltip />
          <el-table-column prop="sellingPointPeriod" label="卖点展示期" width="120" show-overflow-tooltip />
          <el-table-column prop="textDetail" label="文字详情" width="150" show-overflow-tooltip />
          <el-table-column prop="description" label="商品描述" width="150" show-overflow-tooltip />
          
          <!-- 日期相关 -->
          <el-table-column prop="productionDate" label="生产日期" width="110" />
          <el-table-column prop="expiryDate" label="到期日期" width="110" />
          <el-table-column label="是否临期" width="90">
            <template #default="{ row }">
              <span v-if="row.isNearExpiry" style="color: #e6a23c;">是</span>
              <span v-else style="color: #67c23a;">否</span>
            </template>
          </el-table-column>
          <el-table-column label="是否过期" width="90">
            <template #default="{ row }">
              <span v-if="row.isExpired" style="color: #f56c6c;">是</span>
              <span v-else style="color: #67c23a;">否</span>
            </template>
          </el-table-column>
          
          <!-- 配送时间 -->
          <el-table-column prop="deliveryMode" label="发货模式" width="110" />
          <el-table-column prop="presaleDeliveryTime" label="预售配送时间" width="130" show-overflow-tooltip />
          <el-table-column prop="availableTime" label="可售时间" width="120" show-overflow-tooltip />
          
          <!-- 商品属性 -->
          <el-table-column label="力荐" width="70">
            <template #default="{ row }">
              <span v-if="row.isRecommended" style="color: #67c23a;">✓</span>
              <span v-else style="color: #909399;">-</span>
            </template>
          </el-table-column>
          <el-table-column label="无理由退货" width="110">
            <template #default="{ row }">
              <span v-if="row.noReasonReturn" style="color: #67c23a;">✓</span>
              <span v-else style="color: #909399;">-</span>
            </template>
          </el-table-column>
          <el-table-column label="组合商品" width="100">
            <template #default="{ row }">
              <span v-if="row.isCombo" style="color: #e6a23c;">✓</span>
              <span v-else style="color: #909399;">-</span>
            </template>
          </el-table-column>
          <el-table-column label="四轮配送" width="100">
            <template #default="{ row }">
              <span v-if="row.isFourWheelDelivery" style="color: #67c23a;">✓</span>
              <span v-else style="color: #909399;">-</span>
            </template>
          </el-table-column>
          
          <!-- 合规审核 -->
          <el-table-column prop="complianceStatus" label="合规状态" width="100" />
          <el-table-column label="违规下架" width="100">
            <template #default="{ row }">
              <span v-if="row.violationOffline" style="color: #f56c6c;">是</span>
              <span v-else style="color: #67c23a;">否</span>
            </template>
          </el-table-column>
          <el-table-column label="必填信息缺失" width="130">
            <template #default="{ row }">
              <span v-if="row.missingRequiredInfo" style="color: #e6a23c;">是</span>
              <span v-else style="color: #67c23a;">否</span>
            </template>
          </el-table-column>
          <el-table-column prop="auditStatus" label="审核状态" width="100" fixed="right" />
        </el-table>

        <div class="step-actions">
          <el-button @click="handleBack" :disabled="importing">上一步</el-button>
          <el-button 
            type="primary" 
            :loading="importing"
            :disabled="importing"
            @click="handleImport"
            style="background-color: #FFD100; border-color: #FFD100; color: #333;"
          >
            {{ importing ? '导入中...' : '确认导入' }}
          </el-button>
        </div>
      </div>

      <!-- 步骤3: 导入完成 -->
      <div v-if="currentStep === 2" class="step-content">
        <el-result
          :icon="importResult.failedCount > 0 ? 'warning' : 'success'"
          :title="importResult.failedCount > 0 ? '导入完成（部分失败）' : '导入成功'"
        >
          <template #sub-title>
            <div class="import-summary">
              <div v-if="importResult.formatType" class="summary-item format">
                <span class="label">格式：</span>
                <span class="value">{{ formatTypeText(importResult.formatType) }}</span>
              </div>
              <div class="summary-item success">
                <span class="label">成功：</span>
                <span class="value">{{ importResult.successCount }} 条</span>
              </div>
              <div v-if="importResult.failedCount > 0" class="summary-item failed">
                <span class="label">失败：</span>
                <span class="value">{{ importResult.failedCount }} 条</span>
              </div>
              <div class="summary-item">
                <span class="label">总计：</span>
                <span class="value">{{ importResult.totalCount }} 条</span>
              </div>
              <div v-if="importResult.duration" class="summary-item">
                <span class="label">耗时：</span>
                <span class="value">{{ (importResult.duration / 1000).toFixed(2) }}s</span>
              </div>
            </div>
          </template>
          <template #extra>
            <el-button 
              type="primary" 
              @click="handleReset"
              style="background-color: #FFD100; border-color: #FFD100; color: #333;"
            >
              继续导入
            </el-button>
            <el-button @click="$router.push('/products')">查看商品列表</el-button>
          </template>
        </el-result>

        <!-- 错误详情 -->
        <div v-if="importResult.errorDetails && importResult.errorDetails.length > 0" class="error-details">
          <el-collapse>
            <el-collapse-item name="1">
              <template #title>
                <span>查看失败详情（{{ importResult.errorDetails.length }} 条）</span>
                <span v-if="importResult.hasMoreErrors" class="more-errors-hint">
                  还有 {{ importResult.remainingErrorCount }} 条错误未显示
                </span>
              </template>
              <el-table :data="importResult.errorDetails" style="width: 100%" max-height="400">
                <el-table-column prop="rowNum" label="行号" width="80" />
                <el-table-column prop="fieldName" label="字段" width="150" />
                <el-table-column prop="message" label="错误信息" min-width="200" />
                <el-table-column prop="originalValue" label="原始值" width="150" show-overflow-tooltip />
              </el-table>
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled, Document, Download } from '@element-plus/icons-vue'
import request from '@/api/index.js'

const uploadRef = ref(null)
const currentStep = ref(0)
const selectedFile = ref(null)
const previewData = ref([])
const previewResult = ref({
  formatType: null,
  formatMessage: '',
  totalCount: 0,
  successCount: 0,
  failedCount: 0,
  previewCount: 0,
  hasMoreData: false,
  errors: [],
  hasMoreErrors: false,
  remainingErrorCount: 0
})
const previewing = ref(false)
const importing = ref(false)
const downloading = ref(false)
const importResult = ref({
  successCount: 0,
  failedCount: 0,
  totalCount: 0,
  formatType: null,
  errorDetails: [],
  hasMoreErrors: false,
  remainingErrorCount: 0,
  duration: 0
})

// 格式类型文本转换
const formatTypeText = (formatType) => {
  const typeMap = {
    'MEITUAN': '美团格式',
    'STANDARD': '标准格式',
    'UNKNOWN': '未知格式'
  }
  return typeMap[formatType] || formatType
}

// 下载模板
const handleDownloadTemplate = async () => {
  downloading.value = true
  try {
    const response = await request.get('/products/download-template', {
      responseType: 'blob'
    })
    
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([response]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', '商品导入模板.xlsx')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('模板下载成功')
  } catch (error) {
    console.error('下载模板失败:', error)
    ElMessage.error('下载模板失败，请稍后重试')
  } finally {
    downloading.value = false
  }
}

// 文件选择
const handleFileChange = (file) => {
  // 验证文件大小（10MB）
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 10MB')
    uploadRef.value.clearFiles()
    return
  }

  // 验证文件类型
  const fileName = file.name
  const fileExtension = fileName.substring(fileName.lastIndexOf('.')).toLowerCase()
  if (!['.xlsx', '.xls'].includes(fileExtension)) {
    ElMessage.error('只支持 .xlsx 和 .xls 格式的文件')
    uploadRef.value.clearFiles()
    return
  }

  selectedFile.value = file
}

// 文件移除
const handleFileRemove = () => {
  selectedFile.value = null
  previewData.value = []
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 预览数据
const handlePreview = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  previewing.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value.raw)
    formData.append('merchantId', 1) // 默认商家ID

    const response = await request.post('/products/preview', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    if (response.code === 200) {
      // 新的响应格式包含格式识别信息
      previewResult.value = response.data
      previewData.value = response.data.products || []
      
      if (previewData.value.length === 0) {
        ElMessage.warning('文件中没有检测到有效的商品数据')
        return
      }
      
      currentStep.value = 1
      
      // 显示格式识别信息
      const formatText = formatTypeText(previewResult.value.formatType)
      ElMessage.success(`${response.message || '预览成功'} - ${formatText}`)
    }
  } catch (error) {
    console.error('预览失败:', error)
    ElMessage.error(error.response?.data?.message || '文件解析失败，请检查文件格式')
  } finally {
    previewing.value = false
  }
}

// 返回上一步
const handleBack = () => {
  currentStep.value = 0
}

// 导入数据
const handleImport = async () => {
  try {
    await ElMessageBox.confirm(
      `确认导入 ${previewResult.value.totalCount} 条商品数据吗？${previewResult.value.hasMoreData ? '（预览仅显示前20行，实际将导入全部数据）' : ''}`,
      '确认导入',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    importing.value = true
    const formData = new FormData()
    formData.append('file', selectedFile.value.raw)
    formData.append('merchantId', 1) // 默认商家ID

    const response = await request.post('/products/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    if (response.code === 200) {
      importResult.value = response.data
      currentStep.value = 2
      
      if (importResult.value.failedCount > 0) {
        ElMessage.warning(`导入完成，成功 ${importResult.value.successCount} 条，失败 ${importResult.value.failedCount} 条`)
      } else {
        ElMessage.success(`成功导入 ${importResult.value.successCount} 条商品数据`)
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('导入失败:', error)
      ElMessage.error(error.response?.data?.message || '导入失败，请稍后重试')
    }
  } finally {
    importing.value = false
  }
}

// 重置
const handleReset = () => {
  currentStep.value = 0
  selectedFile.value = null
  previewData.value = []
  previewResult.value = {
    formatType: null,
    formatMessage: '',
    totalCount: 0,
    successCount: 0,
    failedCount: 0,
    previewCount: 0,
    hasMoreData: false,
    errors: [],
    hasMoreErrors: false,
    remainingErrorCount: 0
  }
  importResult.value = {
    successCount: 0,
    failedCount: 0,
    totalCount: 0,
    formatType: null,
    errorDetails: [],
    hasMoreErrors: false,
    remainingErrorCount: 0,
    duration: 0
  }
  uploadRef.value.clearFiles()
}
</script>

<style scoped>
.import-container {
  width: 100%;
  min-height: 100%;
  padding: 24px;
}

.import-card {
  background-color: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.step-content {
  margin-top: 40px;
}

.template-download {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 24px;
  padding: 20px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 12px;
}

.template-tip {
  font-size: 14px;
  color: #666;
}

.file-info {
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.file-selected {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #fff7e6 0%, #fffbf0 100%);
  border-radius: 12px;
  border: 2px dashed #FFD100;
  width: 100%;
  max-width: 500px;
}

.file-details {
  flex: 1;
}

.file-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.file-size {
  font-size: 14px;
  color: #999;
}

.step-actions {
  margin-top: 32px;
  display: flex;
  justify-content: center;
  gap: 16px;
}

.import-summary {
  display: flex;
  gap: 32px;
  justify-content: center;
  margin-top: 16px;
  font-size: 16px;
  flex-wrap: wrap;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.summary-item .label {
  color: #666;
}

.summary-item .value {
  font-weight: 600;
  font-size: 18px;
}

.summary-item.success .value {
  color: #67c23a;
}

.summary-item.failed .value {
  color: #f56c6c;
}

.summary-item.format .value {
  color: #409eff;
}

.preview-stats {
  display: flex;
  gap: 24px;
  font-size: 14px;
  margin-top: 8px;
}

.preview-stats .success-text {
  color: #67c23a;
  font-weight: 600;
}

.preview-stats .error-text {
  color: #f56c6c;
  font-weight: 600;
}

.preview-stats .info-text {
  color: #909399;
  font-style: italic;
}

.error-list {
  margin-top: 8px;
}

.error-item {
  padding: 4px 0;
  font-size: 13px;
  color: #e6a23c;
}

.error-value {
  color: #909399;
  font-size: 12px;
}

.more-errors {
  padding: 4px 0;
  font-size: 13px;
  color: #909399;
  font-style: italic;
}

.more-errors-hint {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
  font-style: italic;
}

.error-details {
  margin-top: 24px;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

:deep(.el-upload-dragger) {
  padding: 40px;
}

:deep(.el-icon--upload) {
  font-size: 67px;
  color: #FFD100;
  margin-bottom: 16px;
}

:deep(.el-upload__text) {
  font-size: 16px;
  color: #666;
}

:deep(.el-upload__text em) {
  color: #FFD100;
  font-style: normal;
}

:deep(.el-alert p) {
  margin: 4px 0;
  line-height: 1.6;
}
</style>
