<template>
  <div class="home-container">
    <!-- 欢迎区域 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <div class="welcome-icon">
          <svg class="meituan-logo" viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle class="logo-circle" cx="50" cy="50" r="45" fill="#FFD100" opacity="0.2"/>
            <circle class="logo-circle-2" cx="50" cy="50" r="35" fill="#FFD100" opacity="0.4"/>
            <path class="logo-check" d="M30 50L45 65L70 35" stroke="#1a1a1a" stroke-width="6" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="welcome-text">
          <h2>欢迎使用美团商品管理系统</h2>
          <p>高效管理您的商品数据，一键批量上传到美团平台</p>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-container">
      <el-row :gutter="16" class="stats-row">
        <el-col :span="12">
          <div class="stat-card">
            <div class="stat-icon-wrapper stat-icon-blue">
              <el-icon :size="24" class="stat-icon"><Goods /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalProducts }}</div>
              <div class="stat-label">商品总数</div>
            </div>
          </div>
        </el-col>

        <el-col :span="12">
          <div class="stat-card">
            <div class="stat-icon-wrapper stat-icon-green">
              <el-icon :size="24" class="stat-icon"><SuccessFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.uploadedProducts }}</div>
              <div class="stat-label">已上传</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 快捷操作 -->
    <div class="section-title">快捷操作</div>
    <div class="action-grid" :class="{ 'admin-grid': isAdmin, 'user-grid': !isAdmin }">
      <div class="action-card" @click="$router.push('/import')">
        <div class="action-icon" style="background-color: #fff7e6;">
          <el-icon :size="32" color="#FFD100"><Upload /></el-icon>
        </div>
        <div class="action-title">批量导入</div>
        <div class="action-desc">导入商品Excel</div>
      </div>

      <div class="action-card" @click="$router.push('/template')">
        <div class="action-icon" style="background-color: #f0f9ff;">
          <el-icon :size="32" color="#409eff"><Document /></el-icon>
        </div>
        <div class="action-title">生成模板</div>
        <div class="action-desc">美团上传模板</div>
      </div>

      <div class="action-card" @click="$router.push('/products')">
        <div class="action-icon" style="background-color: #f0f9ff;">
          <el-icon :size="32" color="#67c23a"><Goods /></el-icon>
        </div>
        <div class="action-title">商品管理</div>
        <div class="action-desc">查看商品列表</div>
      </div>

      <!-- 清空商品按钮 - 仅管理员可见 -->
      <div v-if="isAdmin" class="action-card" @click="handleClearProducts">
        <div class="action-icon" style="background-color: #fef0f0;">
          <el-icon :size="32" color="#f56c6c"><Delete /></el-icon>
        </div>
        <div class="action-title">清空商品</div>
        <div class="action-desc">清空后台数据</div>
      </div>
    </div>

    <!-- 最近操作 -->
    <div class="section-title">最近操作</div>
    <div class="log-list">
      <div v-for="log in recentLogs" :key="log.id" class="log-item">
        <div class="log-icon">
          <el-icon :size="20" color="#FFD100"><Clock /></el-icon>
        </div>
        <div class="log-content">
          <div class="log-title">{{ log.action }}</div>
          <div class="log-desc">{{ log.description }}</div>
          <div class="log-time">{{ log.time }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessageBox, ElMessage, ElNotification } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  Goods,
  Upload,
  Document,
  Delete,
  SuccessFilled,
  Clock
} from '@element-plus/icons-vue'
import request from '@/api/index.js'

const userStore = useUserStore()

const stats = ref({
  totalProducts: 0,
  uploadedProducts: 0,
  failedProducts: 0,
  pendingProducts: 0
})

const recentLogs = ref([
  {
    id: 1,
    action: '批量导入',
    description: '成功导入 100 条商品数据',
    time: '2024-02-09 10:30'
  },
  {
    id: 2,
    action: '生成模板',
    description: '生成美团上传模板',
    time: '2024-02-09 10:25'
  },
  {
    id: 3,
    action: '批量上传',
    description: '成功上传 50 条商品到美团',
    time: '2024-02-09 10:20'
  }
])

// 判断是否为管理员（SUPER_ADMIN 或 ADMIN）
const isAdmin = computed(() => {
  const role = userStore.userInfo?.role
  return role === 'SUPER_ADMIN' || role === 'ADMIN'
})

// 获取问候语
const getGreeting = () => {
  const hour = new Date().getHours()
  const username = userStore.userInfo?.username || '用户'
  
  if (hour >= 5 && hour < 12) {
    return {
      title: `早上好，${username}！`,
      message: '新的一天开始了，祝您工作顺利！'
    }
  } else if (hour >= 12 && hour < 18) {
    return {
      title: `下午好，${username}！`,
      message: '午后时光，继续加油！'
    }
  } else {
    return {
      title: `晚上好，${username}！`,
      message: '辛苦了一天，注意休息哦！'
    }
  }
}

// 显示欢迎通知
const showWelcomeNotification = () => {
  const greeting = getGreeting()
  ElNotification({
    title: greeting.title,
    message: greeting.message,
    type: 'success',
    duration: 4000,
    offset: 70
  })
}

const fetchStats = async () => {
  try {
    const response = await request.get('/products/stats')
    if (response.code === 200) {
      // 映射接口返回的字段名到组件使用的字段名
      stats.value = {
        totalProducts: response.data.totalCount || 0,
        uploadedProducts: response.data.uploadedCount || 0,
        failedProducts: response.data.failedCount || 0,
        pendingProducts: response.data.pendingCount || 0
      }
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const handleClearProducts = async () => {
  try {
    await ElMessageBox.confirm(
      '此操作将清空美团后台的所有商品数据，是否继续？',
      '警告',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 发送DELETE请求，携带请求体
    const response = await request.delete('/products/clear', {
      data: {
        merchantId: 1,
        accessToken: 'admin123'
      }
    })
    
    if (response.code === 200) {
      ElMessage.success('清空成功')
      fetchStats()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空失败:', error)
      ElMessage.error('清空失败')
    }
  }
}

onMounted(() => {
  fetchStats()
  // 显示欢迎通知
  showWelcomeNotification()
})
</script>

<style scoped>
.home-container {
  width: 100%;
  min-height: 100%;
  background-color: #fafbfc;
  padding: 0;
}

/* 欢迎区域 */
.welcome-banner {
  background: #FFD100;
  border-radius: 20px;
  padding: 40px;
  margin: 24px 24px 24px;
  box-shadow: 0 4px 20px rgba(255, 209, 0, 0.15);
  position: relative;
  overflow: hidden;
}

.welcome-banner::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.welcome-content {
  display: flex;
  align-items: center;
  gap: 24px;
  position: relative;
  z-index: 1;
}

.welcome-icon {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
}

.meituan-logo {
  width: 100%;
  height: 100%;
}

/* Logo 动画 */
@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.2;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.4;
  }
}

@keyframes pulse-2 {
  0%, 100% {
    transform: scale(1);
    opacity: 0.4;
  }
  50% {
    transform: scale(1.15);
    opacity: 0.6;
  }
}

@keyframes draw-check {
  0% {
    stroke-dashoffset: 100;
  }
  100% {
    stroke-dashoffset: 0;
  }
}

.logo-circle {
  animation: pulse 2s ease-in-out infinite;
  transform-origin: center;
}

.logo-circle-2 {
  animation: pulse-2 2s ease-in-out infinite;
  transform-origin: center;
}

.logo-check {
  stroke-dasharray: 100;
  stroke-dashoffset: 100;
  animation: draw-check 1.5s ease-in-out infinite;
}

.welcome-text h2 {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px 0;
  letter-spacing: 0.5px;
}

.welcome-text p {
  font-size: 15px;
  color: rgba(0, 0, 0, 0.65);
  margin: 0;
  font-weight: 500;
}

/* 统计卡片容器 */
.stats-container {
  margin: 0 24px;
  padding: 0;
}

.stats-row {
  margin-bottom: 24px;
  padding: 0;
}

.stat-card {
  background-color: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(0, 0, 0, 0.06);
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

.stat-icon-blue {
  background: rgba(59, 130, 246, 0.1);
}

.stat-icon-blue .stat-icon {
  color: #3b82f6;
}

.stat-icon-green {
  background: rgba(34, 197, 94, 0.1);
}

.stat-icon-green .stat-icon {
  color: #22c55e;
}

.stat-icon-orange {
  background: rgba(239, 68, 68, 0.1);
}

.stat-icon-orange .stat-icon {
  color: #ef4444;
}

.stat-icon-yellow {
  background: rgba(245, 158, 11, 0.1);
}

.stat-icon-yellow .stat-icon {
  color: #f59e0b;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 4px;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
}

/* 分区标题 */
.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 24px 16px;
  padding: 0;
}

/* 快捷操作 - 动态列数布局 */
.action-grid {
  display: grid;
  gap: 16px;
  margin: 0 24px 32px;
  padding: 0;
}

/* 管理员：2列布局（4个按钮分两行） */
.action-grid.admin-grid {
  grid-template-columns: repeat(2, 1fr);
}

/* 普通用户：3列布局（3个按钮一行展示） */
.action-grid.user-grid {
  grid-template-columns: repeat(3, 1fr);
}

.action-card {
  background-color: white;
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.08);
  border-color: #FFD100;
}

.action-card:active {
  transform: translateY(0);
}

.action-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  transition: all 0.3s;
}

.action-card:hover .action-icon {
  transform: scale(1.05);
}

.action-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.action-desc {
  font-size: 13px;
  color: #6b7280;
}

/* 日志列表 */
.log-list {
  background-color: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.06);
  margin: 0 24px 24px;
  padding: 0 40px;
}

.log-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  transition: background-color 0.3s;
}

.log-item:hover {
  background-color: rgba(0, 0, 0, 0.02);
}

.log-item:last-child {
  border-bottom: none;
}

.log-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(255, 209, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.log-icon .el-icon {
  color: #FFD100;
}

.log-content {
  flex: 1;
}

.log-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 6px;
}

.log-desc {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 6px;
}

.log-time {
  font-size: 12px;
  color: #9ca3af;
}
</style>
