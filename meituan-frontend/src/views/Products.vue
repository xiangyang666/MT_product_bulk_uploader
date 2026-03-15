<template>
  <div class="products-container">
    <!-- 搜索栏和操作按钮 -->
    <div class="search-bar">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
        @change="handleDateChange"
        clearable
        style="width: 280px;"
      />
      <el-button 
        @click="selectToday" 
        :type="isActiveDateButton('today') ? 'primary' : ''"
        :style="isActiveDateButton('today') ? 'background-color: #FFD100; border-color: #FFD100; color: #333; margin-left: 8px;' : 'margin-left: 8px;'"
      >
        今日
      </el-button>
      <el-button 
        @click="selectYesterday"
        :type="isActiveDateButton('yesterday') ? 'primary' : ''"
        :style="isActiveDateButton('yesterday') ? 'background-color: #FFD100; border-color: #FFD100; color: #333;' : ''"
      >
        昨日
      </el-button>
      <el-input
        v-model="searchQuery"
        placeholder="搜索商品名称或类目ID"
        clearable
        @clear="handleSearch"
        style="flex: 1; margin-left: 8px;"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch" style="background-color: #FFD100; border-color: #FFD100; color: #333;">
        搜索
      </el-button>
      <el-button 
        type="danger" 
        :disabled="selectedIds.length === 0"
        @click="handleBatchDelete"
      >
        批量删除 ({{ selectedIds.length }})
      </el-button>
      <el-button
        type="danger"
        plain
        @click="handleClearAll"
      >
        清除本地数据
      </el-button>
      <el-button
        type="danger"
        plain
        @click="handleDeleteFromMeituan"
      >
        一键删除到美团
      </el-button>
      <el-button
        type="success"
        @click="handleExportImages"
        style="background-color: #67C23A; border-color: #67C23A;"
      >
        <el-icon><Download /></el-icon>
        导出商品图片
      </el-button>
    </div>

    <!-- 商品表格 -->
    <div class="table-card">
      <el-table
        :data="products"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        height="650"
        ref="tableRef"
      >
        <el-table-column type="selection" width="55" fixed="left" />
        <el-table-column type="index" label="序号" width="60" fixed="left" />
        
        <!-- 基础信息 -->
        <el-table-column prop="skuId" label="SKU ID" width="120" show-overflow-tooltip />
        <el-table-column prop="upcEan" label="条形码" width="130" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="商品类目名称" width="150" show-overflow-tooltip />
        <el-table-column prop="categoryId" label="商品类目ID" width="120" show-overflow-tooltip />
        <el-table-column prop="appSpuCode" label="APP SPU编码" width="130" show-overflow-tooltip />
        <el-table-column prop="productName" label="商品名称" width="200" show-overflow-tooltip />
        
        <!-- 图片视频 -->
        <el-table-column label="商品图片" width="150">
          <template #default="scope">
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-image 
                v-if="scope.row.productImage || scope.row.imageUrl"
                :src="scope.row.productImage || scope.row.imageUrl" 
                :preview-src-list="getProductImageList(scope.row)"
                fit="cover"
                style="width: 50px; height: 50px; border-radius: 4px; cursor: pointer;"
              />
              <el-button 
                type="primary" 
                size="small" 
                link
                @click="handleManageImages(scope.row)"
              >
                管理图片
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="coverVideo" label="封面视频" width="120" show-overflow-tooltip />
        <el-table-column prop="specImage" label="规格图" width="120" show-overflow-tooltip />
        
        <!-- 分类库存 -->
        <el-table-column prop="storeCategory" label="店内分类" width="120" show-overflow-tooltip />
        <el-table-column prop="storeCategoryCount" label="店内分类数量" width="120" />
        <el-table-column prop="specName" label="规格名称" width="120" show-overflow-tooltip />
        <el-table-column prop="storeCode" label="店内码/货号" width="120" show-overflow-tooltip />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="scope">
            <span style="color: #FFD100; font-weight: 600;">¥{{ scope.row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="saleStatus" label="售卖状态" width="100">
          <template #default="scope">
            <el-tag :type="getSaleStatusType(scope.row.saleStatus)" size="small">
              {{ scope.row.saleStatus || '在售' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="monthlySales" label="月售" width="80" />
        <el-table-column prop="weight" label="重量" width="80" />
        <el-table-column prop="weightUnit" label="重量单位" width="90" />
        <el-table-column prop="minPurchase" label="起购数" width="80" />
        <el-table-column prop="shelfCode" label="货架码/位置码" width="130" show-overflow-tooltip />
        
        <!-- 详情描述 -->
        <el-table-column prop="sellingPoint" label="商品卖点" width="150" show-overflow-tooltip />
        <el-table-column prop="sellingPointPeriod" label="卖点展示期" width="120" show-overflow-tooltip />
        <el-table-column prop="textDetail" label="文字详情" width="150" show-overflow-tooltip />
        
        <!-- 日期相关 -->
        <el-table-column prop="productionDate" label="生产日期" width="110" />
        <el-table-column prop="expiryDate" label="到期日期" width="110" />
        <el-table-column label="是否临期" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.isNearExpiry ? 'warning' : 'success'" size="small">
              {{ scope.row.isNearExpiry ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否过期" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.isExpired ? 'danger' : 'success'" size="small">
              {{ scope.row.isExpired ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <!-- 配送时间 -->
        <el-table-column prop="deliveryMode" label="发货模式" width="110" />
        <el-table-column prop="presaleDeliveryTime" label="预售配送时间" width="130" show-overflow-tooltip />
        <el-table-column prop="availableTime" label="可售时间" width="120" show-overflow-tooltip />
        
        <!-- 商品属性 -->
        <el-table-column label="力荐" width="70">
          <template #default="scope">
            <el-tag :type="scope.row.isRecommended ? 'success' : 'info'" size="small">
              {{ scope.row.isRecommended ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="无理由退货" width="110">
          <template #default="scope">
            <el-tag :type="scope.row.noReasonReturn ? 'success' : 'info'" size="small">
              {{ scope.row.noReasonReturn ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="组合商品" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isCombo ? 'warning' : 'info'" size="small">
              {{ scope.row.isCombo ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="四轮配送" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isFourWheelDelivery ? 'success' : 'info'" size="small">
              {{ scope.row.isFourWheelDelivery ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <!-- 合规审核 -->
        <el-table-column prop="complianceStatus" label="合规状态" width="100">
          <template #default="scope">
            <el-tag :type="getComplianceType(scope.row.complianceStatus)" size="small">
              {{ scope.row.complianceStatus || '合规' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="违规下架" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.violationOffline ? 'danger' : 'success'" size="small">
              {{ scope.row.violationOffline ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="必填信息缺失" width="130">
          <template #default="scope">
            <el-tag :type="scope.row.missingRequiredInfo ? 'warning' : 'success'" size="small">
              {{ scope.row.missingRequiredInfo ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="审核状态" width="100">
          <template #default="scope">
            <el-tag :type="getAuditStatusType(scope.row.auditStatus)" size="small">
              {{ scope.row.auditStatus || '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <!-- 系统字段 -->
        <el-table-column prop="createdTime" label="导入时间" width="160" fixed="right">
          <template #default="scope">
            <div :class="{ 'today-import': isToday(scope.row.createdTime) }">
              <div style="font-weight: 600;">{{ formatDate(scope.row.createdTime) }}</div>
              <div style="font-size: 12px; color: #909399;">{{ formatTime(scope.row.createdTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" fixed="right">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ scope.row.status || '待上传' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button 
              type="danger" 
              size="small" 
              link
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 加载更多提示 -->
      <div v-if="loadingMore" class="loading-more">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      <div v-else-if="hasMore" class="load-more-tip">
        滚动到底部加载更多
      </div>
      <div v-else-if="products.length > 0" class="no-more-tip">
        已加载全部数据
      </div>
    </div>

    <!-- 数据统计 -->
    <div class="data-stats">
      <span>已加载 {{ products.length }} 条</span>
      <span v-if="total > 0">/ 共 {{ total }} 条</span>
    </div>

    <!-- 图片管理对话框 -->
    <el-dialog
      v-model="imageDialogVisible"
      title="管理商品图片"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="image-manager">
        <div class="image-upload-area">
          <el-upload
            :action="''"
            :http-request="handleImageUpload"
            :show-file-list="false"
            :before-upload="beforeImageUpload"
            accept="image/*"
            :disabled="currentProductImages.length >= 5"
          >
            <el-button 
              type="primary" 
              :disabled="currentProductImages.length >= 5"
            >
              <el-icon><Upload /></el-icon>
              上传图片
            </el-button>
          </el-upload>
          <div class="upload-tip">
            最多上传5张图片，每张不超过10MB
            ({{ currentProductImages.length }}/5)
          </div>
        </div>

        <div class="image-list">
          <div 
            v-for="(imageUrl, index) in currentProductImages" 
            :key="index"
            class="image-item"
          >
            <el-image 
              :src="imageUrl" 
              :preview-src-list="currentProductImages"
              :initial-index="index"
              fit="cover"
              class="image-preview"
            />
            <div class="image-actions">
              <el-button 
                type="danger" 
                size="small"
                @click="handleDeleteImage(imageUrl)"
              >
                删除
              </el-button>
            </div>
          </div>
          <div v-if="currentProductImages.length === 0" class="no-images">
            暂无图片
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { Search, Loading, Download, Upload } from '@element-plus/icons-vue'
import request, { uploadProductImage, deleteProductImage, getProductImages, exportProductImages } from '@/api/index.js'

const products = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const searchQuery = ref('')
const dateRange = ref(null)
const activeDateButton = ref(null) // 'today', 'yesterday', or null
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const selectedIds = ref([])
const hasMore = ref(true)
const tableRef = ref(null)

// 图片管理相关
const imageDialogVisible = ref(false)
const currentProduct = ref(null)
const currentProductImages = ref([])

// 格式化日期（YYYY-MM-DD）
const formatDate = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 格式化时间（HH:mm:ss）
const formatTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${hours}:${minutes}:${seconds}`
}

// 判断是否为今天导入
const isToday = (dateTime) => {
  if (!dateTime) return false
  const date = new Date(dateTime)
  const today = new Date()
  return date.getFullYear() === today.getFullYear() &&
         date.getMonth() === today.getMonth() &&
         date.getDate() === today.getDate()
}

// 判断日期按钮是否激活
const isActiveDateButton = (buttonType) => {
  return activeDateButton.value === buttonType
}

// 日期选择器变化处理（手动选择日期时清除快捷按钮激活状态）
const handleDateChange = () => {
  activeDateButton.value = null
  handleSearch()
}

// 选择今日
const selectToday = () => {
  const today = new Date()
  const dateStr = formatDateToString(today)
  dateRange.value = [dateStr, dateStr]
  activeDateButton.value = 'today'
  handleSearch()
}

// 选择昨日
const selectYesterday = () => {
  const yesterday = new Date()
  yesterday.setDate(yesterday.getDate() - 1)
  const dateStr = formatDateToString(yesterday)
  dateRange.value = [dateStr, dateStr]
  activeDateButton.value = 'yesterday'
  handleSearch()
}

// 格式化日期为字符串（YYYY-MM-DD）
const formatDateToString = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const getStatusType = (status) => {
  const typeMap = {
    '已上传': 'success',
    '待上传': 'info',
    '上传失败': 'danger'
  }
  return typeMap[status] || 'info'
}

const getSaleStatusType = (status) => {
  const typeMap = {
    '在售': 'success',
    '停售': 'danger',
    '售罄': 'warning'
  }
  return typeMap[status] || 'success'
}

const getComplianceType = (status) => {
  const typeMap = {
    '合规': 'success',
    '不合规': 'danger',
    '待审核': 'warning'
  }
  return typeMap[status] || 'success'
}

const getAuditStatusType = (status) => {
  const typeMap = {
    '已通过': 'success',
    '待审核': 'warning',
    '未通过': 'danger'
  }
  return typeMap[status] || 'warning'
}

const fetchProducts = async (append = false) => {
  if (append) {
    loadingMore.value = true
  } else {
    loading.value = true
  }
  
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchQuery.value
    }
    
    // 添加日期范围参数
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    const response = await request.get('/products', { params })
    
    if (response.code === 200) {
      const newProducts = response.data.list || []
      
      if (append) {
        products.value = [...products.value, ...newProducts]
      } else {
        products.value = newProducts
      }
      
      total.value = response.data.total || 0
      hasMore.value = products.value.length < total.value
    }
  } catch (error) {
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  products.value = []
  hasMore.value = true
  fetchProducts()
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？删除后无法恢复！', '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request.delete(`/products/${row.id}`)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      fetchProducts()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要删除的商品')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedIds.value.length} 个商品吗？删除后无法恢复！`, 
      '批量删除确认', 
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await request.delete('/products/batch', {
      data: selectedIds.value
    })
    
    if (response.code === 200) {
      ElMessage.success(`成功删除 ${selectedIds.value.length} 个商品`)
      selectedIds.value = []
      fetchProducts()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const handleClearAll = async () => {
  try {
    // 第一次确认
    await ElMessageBox.confirm(
      '此操作将删除服务器数据库中的所有商品数据，且不可恢复！', 
      '危险操作警告', 
      {
        confirmButtonText: '我已了解，继续',
        cancelButtonText: '取消',
        type: 'error',
        distinguishCancelAndClose: true
      }
    )
    
    // 第二次确认 - 输入访问令牌
    const { value: accessToken } = await ElMessageBox.prompt(
      '请输入访问令牌以确认操作（默认令牌：admin123）', 
      '访问令牌验证', 
      {
        confirmButtonText: '确认清除',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入访问令牌',
        inputType: 'password',
        inputValidator: (value) => {
          if (!value || value.trim() === '') {
            return '访问令牌不能为空'
          }
          return true
        }
      }
    )
    
    // 显示loading
    const loading = ElLoading.service({
      lock: true,
      text: '正在清除所有商品，请稍候...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    try {
      const response = await request.delete('/products/clear', {
        data: {
          merchantId: 1,
          accessToken: accessToken
        }
      })
      
      if (response.code === 200) {
        ElMessage.success(`成功清除 ${response.data.deletedCount} 个商品`)
        // 刷新列表
        currentPage.value = 1
        products.value = []
        hasMore.value = true
        fetchProducts()
      }
    } finally {
      loading.close()
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      const errorMsg = error.response?.data?.message || '清除失败，请检查访问令牌'
      ElMessage.error(errorMsg)
    }
  }
}

// 一键删除到美团
const handleDeleteFromMeituan = async () => {
  try {
    // 第一次确认
    await ElMessageBox.confirm(
      '此操作将逐个删除美团平台上的所有商品，耗时较长（预计30-60分钟），且不可恢复！',
      '危险操作警告',
      {
        confirmButtonText: '我已了解，继续',
        cancelButtonText: '取消',
        type: 'error',
        distinguishCancelAndClose: true
      }
    )

    // 第二次确认 - 输入访问令牌
    const { value: accessToken } = await ElMessageBox.prompt(
      '请输入访问令牌以确认操作（默认令牌：admin123）',
      '访问令牌验证',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入访问令牌',
        inputType: 'password',
        inputValidator: (value) => {
          if (!value || value.trim() === '') {
            return '访问令牌不能为空'
          }
          return true
        }
      }
    )

    // 显示loading
    const loading = ElLoading.service({
      lock: true,
      text: '正在逐个删除美团商品，请耐心等待...（此操作耗时较长）',
      background: 'rgba(0, 0, 0, 0.7)'
    })

    try {
      const response = await request.delete('/products/delete-meituan', {
        data: {
          merchantId: 1,
          accessToken: accessToken
        }
      })

      if (response.code === 200) {
        const result = response.data
        const message = `删除完成：共 ${result.deletedCount || 0} 个，成功 ${result.meituanDeletedCount || 0} 个，失败 ${result.meituanFailedCount || 0} 个`

        if (result.meituanFailedCount > 0) {
          ElMessage.warning(message + '，部分商品删除失败')
        } else {
          ElMessage.success(message)
        }

        // 刷新列表
        fetchProducts()
      }
    } finally {
      loading.close()
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      const errorMsg = error.response?.data?.message || '删除失败，请检查访问令牌'
      ElMessage.error(errorMsg)
    }
  }
}

// ==================== 图片管理功能 ====================

// 获取商品图片列表（用于预览）
const getProductImageList = (row) => {
  try {
    if (row.productImages) {
      const images = JSON.parse(row.productImages)
      return Array.isArray(images) ? images : []
    }
    if (row.productImage || row.imageUrl) {
      return [row.productImage || row.imageUrl]
    }
  } catch (e) {
    if (row.productImage || row.imageUrl) {
      return [row.productImage || row.imageUrl]
    }
  }
  return []
}

// 打开图片管理对话框
const handleManageImages = async (row) => {
  currentProduct.value = row
  imageDialogVisible.value = true
  
  // 加载商品图片
  try {
    const response = await getProductImages(row.id)
    if (response.code === 200) {
      currentProductImages.value = response.data.imageUrls || []
    }
  } catch (error) {
    ElMessage.error('加载图片失败')
  }
}

// 图片上传前检查
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过10MB')
    return false
  }
  if (currentProductImages.value.length >= 5) {
    ElMessage.error('每个商品最多只能上传5张图片')
    return false
  }
  return true
}

// 处理图片上传
const handleImageUpload = async ({ file }) => {
  const loading = ElLoading.service({
    lock: true,
    text: '上传中...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  try {
    const response = await uploadProductImage(currentProduct.value.id, file, (progress) => {
      loading.setText(`上传中... ${progress}%`)
    })

    if (response.code === 200) {
      currentProductImages.value = response.data.imageUrls
      ElMessage.success('上传成功')
      
      // 刷新商品列表
      fetchProducts()
    }
  } catch (error) {
    ElMessage.error('上传失败')
  } finally {
    loading.close()
  }
}

// 删除图片
const handleDeleteImage = async (imageUrl) => {
  try {
    await ElMessageBox.confirm('确定要删除这张图片吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const loading = ElLoading.service({
      lock: true,
      text: '删除中...',
      background: 'rgba(0, 0, 0, 0.7)'
    })

    try {
      const response = await deleteProductImage(currentProduct.value.id, imageUrl)
      if (response.code === 200) {
        currentProductImages.value = response.data.imageUrls
        ElMessage.success('删除成功')
        
        // 刷新商品列表
        fetchProducts()
      }
    } finally {
      loading.close()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 导出商品图片
const handleExportImages = async () => {
  try {
    // 显示命名方式选择对话框
    const { value: namingType } = await ElMessageBox({
      title: '导出商品图片',
      message: '请选择图片命名方式',
      showInput: true,
      inputType: 'select',
      inputOptions: [
        { label: '商品名称', value: 'PRODUCT_NAME' },
        { label: '条形码', value: 'BARCODE' },
        { label: '店内码/货号', value: 'STORE_CODE' }
      ],
      inputValue: 'PRODUCT_NAME',
      confirmButtonText: '确认导出',
      cancelButtonText: '取消'
    })

    const loading = ElLoading.service({
      lock: true,
      text: '正在导出图片，请稍候...',
      background: 'rgba(0, 0, 0, 0.7)'
    })

    try {
      const blob = await exportProductImages({
        merchantId: 1,
        namingType: namingType || 'PRODUCT_NAME',
        productIds: selectedIds.value.length > 0 ? selectedIds.value : null
      })

      // 下载ZIP文件
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      const timestamp = new Date().toISOString().replace(/[-:]/g, '').replace('T', '_').split('.')[0]
      link.download = `商品图片_${timestamp}.zip`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)

      ElMessage.success('图片导出成功')
    } finally {
      loading.close()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('导出失败:', error)
      ElMessage.error('图片导出失败')
    }
  }
}

onMounted(async () => {
  selectToday()
  
  // 等待 DOM 渲染完成后监听表格内部滚动
  await nextTick()
  
  if (tableRef.value) {
    const tableBody = tableRef.value.$el.querySelector('.el-table__body-wrapper')
    if (tableBody) {
      tableBody.addEventListener('scroll', (event) => {
        const { scrollTop, scrollHeight, clientHeight } = event.target
        
        // 滚动到底部时加载更多（提前50px触发）
        if (scrollTop + clientHeight >= scrollHeight - 50 && hasMore.value && !loadingMore.value && !loading.value) {
          console.log('触发加载更多')
          currentPage.value++
          fetchProducts(true)
        }
      })
    }
  }
})
</script>

<style scoped>
.products-container {
  width: 100%;
  min-height: 100%;
  background-color: #f5f5f5;
  padding: 16px;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.search-bar .el-button--danger.is-plain {
  color: #ffffff !important;
  background-color: #f56c6c !important;
  border-color: #f56c6c !important;
}

.search-bar .el-button--danger.is-plain:hover {
  color: #ffffff !important;
  background-color: #f78989 !important;
  border-color: #f78989 !important;
}

.search-bar .el-button--danger.is-plain span {
  color: inherit !important;
}

.table-card {
  background-color: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 16px;
}

.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #409eff;
  gap: 8px;
}

.load-more-tip {
  text-align: center;
  padding: 16px;
  color: #909399;
  font-size: 14px;
}

.no-more-tip {
  text-align: center;
  padding: 16px;
  color: #c0c4cc;
  font-size: 14px;
}

.data-stats {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  padding: 16px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  font-size: 14px;
  color: #606266;
}

/* 今日导入高亮 */
.today-import {
  padding: 4px 8px;
  background-color: #fff7e6;
  border-radius: 4px;
  border-left: 3px solid #FFD100;
}

/* 图片管理对话框样式 */
.image-manager {
  padding: 16px 0;
}

.image-upload-area {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.image-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 16px;
  min-height: 150px;
}

.image-item {
  position: relative;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.image-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
}

.image-preview {
  width: 100%;
  height: 120px;
  cursor: pointer;
}

.image-actions {
  padding: 8px;
  background-color: #f5f7fa;
  text-align: center;
}

.no-images {
  grid-column: 1 / -1;
  text-align: center;
  padding: 40px;
  color: #909399;
  font-size: 14px;
}
</style>
