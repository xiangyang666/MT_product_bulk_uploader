<template>
  <div class="user-container">
    <!-- 用户信息卡片 -->
    <div class="user-header">
      <el-avatar :size="80" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
      <div class="user-info">
        <div class="user-name">{{ userStore.userInfo.username || '用户' }}</div>
        <div class="user-email">{{ userStore.userInfo.email || 'user@example.com' }}</div>
      </div>
    </div>

    <!-- 功能列表 -->
    <div class="menu-section">
      <div class="menu-item" @click="$router.push('/logs')">
        <div class="menu-left">
          <el-icon :size="20" color="#FFD100"><List /></el-icon>
          <span>操作日志</span>
        </div>
        <el-icon><ArrowRight /></el-icon>
      </div>

      <div class="menu-item" @click="$router.push('/settings')">
        <div class="menu-left">
          <el-icon :size="20" color="#FFD100"><Setting /></el-icon>
          <span>系统设置</span>
        </div>
        <el-icon><ArrowRight /></el-icon>
      </div>

      <div class="menu-item" @click="handleLogout">
        <div class="menu-left">
          <el-icon :size="20" color="#f56c6c"><SwitchButton /></el-icon>
          <span style="color: #f56c6c">退出登录</span>
        </div>
        <el-icon><ArrowRight /></el-icon>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { List, Setting, SwitchButton, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await userStore.logout()
    ElMessage.success('退出成功')
    router.push('/login')
  } catch (error) {
    // 用户取消
  }
}
</script>

<style scoped>
.user-container {
  width: 100%;
  min-height: 100%;
  background-color: #f5f5f5;
  padding: 20px;
}

.user-header {
  background: linear-gradient(135deg, #FFD100 0%, #FFA726 100%);
  border-radius: 16px;
  padding: 32px 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(255, 209, 0, 0.2);
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 24px;
  font-weight: 600;
  color: white;
  margin-bottom: 8px;
}

.user-email {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
}

.menu-section {
  background-color: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  cursor: pointer;
  transition: background-color 0.3s;
  border-bottom: 1px solid #f5f5f5;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:active {
  background-color: #f5f5f5;
}

.menu-left {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  color: #333;
}
</style>
