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
      <el-button
        type="primary"
        @click="handleCreateProduct"
        style="background-color: #FFD100; border-color: #FFD100; color: #333;"
      >
        <el-icon><Plus /></el-icon>
        创建商品
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
                v-if="getFirstProductImage(scope.row)"
                :src="getFirstProductImage(scope.row)" 
                :preview-src-list="getProductImageList(scope.row)"
                fit="cover"
                style="width: 50px; height: 50px; border-radius: 4px; cursor: pointer;"
              >
                <template #error>
                  <div style="width: 50px; height: 50px; display: flex; align-items: center; justify-content: center; background: #f5f7fa; color: #909399; font-size: 12px;">
                    无图片
                  </div>
                </template>
              </el-image>
              <span v-else style="color: #909399; font-size: 12px;">无图片</span>
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
      
      <!-- 分页器 -->
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          background
        />
      </div>
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
            multiple
            :disabled="currentProductImages.length >= 5"
          >
            <el-button 
              type="primary" 
              :disabled="currentProductImages.length >= 5"
            >
              <el-icon><Upload /></el-icon>
              上传图片（可多选）
            </el-button>
          </el-upload>
          <div class="upload-tip">
            最多上传5张图片，每张不超过10MB，可一次选择多张
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

    <!-- 创建商品对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建商品"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="productFormRef"
        :model="productForm"
        :rules="productFormRules"
        label-width="120px"
      >
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="productForm.productName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品类目" prop="categoryName">
          <el-input v-model="productForm.categoryName" placeholder="请输入商品类目名称" />
        </el-form-item>
        <el-form-item label="类目ID" prop="categoryId">
          <el-input v-model="productForm.categoryId" placeholder="请输入类目ID" />
        </el-form-item>
        <el-form-item label="条形码" prop="upcEan">
          <el-input v-model="productForm.upcEan" placeholder="请输入条形码" />
        </el-form-item>
        <el-form-item label="店内码/货号" prop="storeCode">
          <el-input v-model="productForm.storeCode" placeholder="请输入店内码/货号" />
        </el-form-item>
        <el-form-item label="规格名称" prop="specName">
          <el-input v-model="productForm.specName" placeholder="请输入规格名称" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="productForm.price" :precision="2" :min="0" :step="0.1" placeholder="请输入价格" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="productForm.stock" :min="0" placeholder="请输入库存" />
        </el-form-item>
        <el-form-item label="重量" prop="weight">
          <el-input v-model="productForm.weight" placeholder="请输入重量" />
        </el-form-item>
        <el-form-item label="重量单位" prop="weightUnit">
          <el-select v-model="productForm.weightUnit" placeholder="请选择重量单位" style="width: 100%;">
            <el-option label="克(g)" value="克(g)" />
            <el-option label="千克(kg)" value="千克(kg)" />
            <el-option label="毫升(ml)" value="毫升(ml)" />
            <el-option label="升(L)" value="升(L)" />
            <el-option label="个" value="个" />
            <el-option label="件" value="件" />
          </el-select>
        </el-form-item>
        <el-form-item label="品牌" prop="brand">
          <el-input v-model="productForm.brand" placeholder="请输入品牌" />
        </el-form-item>
        <el-form-item label="起购数" prop="minPurchase">
          <el-input-number v-model="productForm.minPurchase" :min="1" :step="1" placeholder="请输入起购数" />
        </el-form-item>
        <el-form-item label="售卖状态" prop="saleStatus">
          <el-select v-model="productForm.saleStatus" placeholder="请选择售卖状态" style="width: 100%;">
            <el-option label="在售" value="售卖中" />
            <el-option label="停售" value="停售" />
            <el-option label="售罄" value="售罄" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="productForm.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
        </el-form-item>
        <el-form-item label="商品卖点" prop="sellingPoint">
          <el-input v-model="productForm.sellingPoint" placeholder="请输入商品卖点" />
        </el-form-item>
        <el-form-item label="店内分类" prop="storeCategory">
          <el-input v-model="productForm.storeCategory" placeholder="请输入店内分类，格式：分类1>分类2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitProduct" :loading="submitting">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { Search, Loading, Download, Upload, Plus } from '@element-plus/icons-vue'
import request, { uploadProductImage, deleteProductImage, getProductImages, exportProductImages, createProduct } from '@/api/index.js'

const products = ref([])
const loading = ref(false)
const searchQuery = ref('')
const dateRange = ref(null)
const activeDateButton = ref(null) // 'today', 'yesterday', or null
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const selectedIds = ref([])
const tableRef = ref(null)

// 图片管理相关
const imageDialogVisible = ref(false)
const currentProduct = ref(null)
const currentProductImages = ref([])
const uploadQueue = ref([]) // 上传队列
const isUploading = ref(false) // 是否正在上传
let uploadTimer = null // 上传延迟定时器

// 创建商品相关
const createDialogVisible = ref(false)
const submitting = ref(false)
const productFormRef = ref(null)
const productForm = ref({
  productName: '',
  categoryName: '',
  categoryId: '',
  upcEan: '',
  storeCode: '',
  specName: '',
  price: 0.01,
  stock: 100,
  weight: 0,
  weightUnit: '克(g)',
  brand: '',
  minPurchase: 1,
  saleStatus: '售卖中',
  description: '',
  sellingPoint: '',
  storeCategory: '',
  merchantId: 1
})

// 表单验证规则
const productFormRules = {
  productName: [
    { required: true, message: '请输入商品名称', trigger: 'blur' }
  ],
  categoryName: [
    { required: true, message: '请输入商品类目', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请输入类目ID', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' }
  ]
}

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

const fetchProducts = async () => {
  loading.value = true
  
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
      products.value = response.data.list || []
      total.value = response.data.total || 0
      
      // 滚动到表格顶部
      scrollToTop()
    }
  } catch (error) {
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

// 滚动到表格顶部
const scrollToTop = () => {
  if (tableRef.value) {
    const tableBody = tableRef.value.$el.querySelector('.el-table__body-wrapper')
    if (tableBody) {
      tableBody.scrollTop = 0
    }
  }
}

// 处理页码变化
const handlePageChange = (page) => {
  currentPage.value = page
  fetchProducts()
}

// 处理每页数量变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchProducts()
}

const handleSearch = () => {
  currentPage.value = 1
  fetchProducts()
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 打开创建商品对话框
const handleCreateProduct = () => {
  // 重置表单
  productForm.value = {
    productName: '',
    categoryName: '',
    categoryId: '',
    upcEan: '',
    storeCode: '',
    specName: '',
    price: 0.01,
    stock: 100,
    weight: 0,
    weightUnit: '克(g)',
    brand: '',
    minPurchase: 1,
    saleStatus: '售卖中',
    description: '',
    sellingPoint: '',
    storeCategory: '',
    merchantId: 1
  }
  createDialogVisible.value = true
}

// 提交创建商品
const handleSubmitProduct = async () => {
  try {
    // 表单验证
    await productFormRef.value.validate()

    submitting.value = true

    const response = await createProduct(productForm.value)

    if (response.code === 200) {
      ElMessage.success('创建成功')
      createDialogVisible.value = false
      // 刷新商品列表
      fetchProducts()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('创建商品失败:', error)
    }
  } finally {
    submitting.value = false
  }
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

// 获取商品的第一张图片（用于列表显示）
const getFirstProductImage = (row) => {
  try {
    // 优先使用 productImages 字段（JSON数组）
    if (row.productImages) {
      const images = JSON.parse(row.productImages)
      if (Array.isArray(images) && images.length > 0) {
        return images[0]
      }
    }
  } catch (e) {
    console.error('解析productImages失败:', e)
  }
  
  // 降级使用旧字段
  return row.productImage || row.imageUrl || null
}

// 获取商品图片列表（用于预览）
const getProductImageList = (row) => {
  try {
    // 优先使用 productImages 字段（JSON数组）
    if (row.productImages) {
      const images = JSON.parse(row.productImages)
      if (Array.isArray(images) && images.length > 0) {
        return images
      }
    }
  } catch (e) {
    console.error('解析productImages失败:', e)
  }
  
  // 降级使用旧字段
  if (row.productImage || row.imageUrl) {
    return [row.productImage || row.imageUrl]
  }
  
  return []
}

// 打开图片管理对话框
const handleManageImages = async (row) => {
  currentProduct.value = row
  // 立即清空图片列表，避免显示上一个商品的图片
  currentProductImages.value = []
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
  
  // 检查总数量限制（当前已有 + 队列中的 + 新文件）
  const totalCount = currentProductImages.value.length + uploadQueue.value.length + 1
  if (totalCount > 5) {
    ElMessage.error(`每个商品最多只能上传5张图片，当前已有${currentProductImages.value.length}张，最多还能上传${5 - currentProductImages.value.length}张`)
    return false
  }
  
  return true
}

// 处理图片上传（Element Plus会为每个文件单独调用此函数）
const handleImageUpload = async ({ file }) => {
  // 将文件添加到队列
  uploadQueue.value.push(file)
  
  // 清除之前的定时器
  if (uploadTimer) {
    clearTimeout(uploadTimer)
  }
  
  // 延迟500ms后批量上传（等待所有文件都添加到队列）
  uploadTimer = setTimeout(async () => {
    if (!isUploading.value && uploadQueue.value.length > 0) {
      await processBatchUpload()
    }
  }, 500)
}

// 批量处理上传队列
const processBatchUpload = async () => {
  if (uploadQueue.value.length === 0 || isUploading.value) {
    return
  }
  
  isUploading.value = true
  const filesToUpload = [...uploadQueue.value]
  uploadQueue.value = []
  
  const loading = ElLoading.service({
    lock: true,
    text: `准备上传${filesToUpload.length}张图片...`,
    background: 'rgba(0, 0, 0, 0.7)'
  })

  try {
    let successCount = 0
    let failCount = 0
    
    // 逐个上传文件
    for (let i = 0; i < filesToUpload.length; i++) {
      const file = filesToUpload[i]
      loading.setText(`正在上传第${i + 1}/${filesToUpload.length}张图片...`)
      
      try {
        const response = await uploadProductImage(currentProduct.value.id, file, (progress) => {
          loading.setText(`正在上传第${i + 1}/${filesToUpload.length}张图片... ${progress}%`)
        })

        if (response.code === 200) {
          currentProductImages.value = response.data.imageUrls
          successCount++
        } else {
          failCount++
        }
      } catch (error) {
        console.error('上传失败:', error)
        failCount++
      }
    }
    
    // 显示结果
    if (failCount === 0) {
      ElMessage.success(`成功上传${successCount}张图片`)
    } else if (successCount === 0) {
      ElMessage.error(`上传失败，请重试`)
    } else {
      ElMessage.warning(`成功上传${successCount}张，失败${failCount}张`)
    }
    
    // 刷新商品列表
    await fetchProducts()
  } catch (error) {
    ElMessage.error('上传失败')
  } finally {
    loading.close()
    isUploading.value = false
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

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
  margin-top: 16px;
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
