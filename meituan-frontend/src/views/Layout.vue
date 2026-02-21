<template>
  <div class="layout-container">
    <!-- macOS 风格标题栏 -->
    <div class="mac-titlebar">
      <div class="titlebar-left">
        <span class="app-name">美团商品管理系统</span>
      </div>
      <div class="titlebar-center">
        <span class="time">{{ currentTime }}</span>
      </div>
      <div class="traffic-lights">
        <div class="traffic-light minimize" @click="minimizeWindow"></div>
        <div class="traffic-light maximize" @click="maximizeWindow"></div>
        <div class="traffic-light close" @click="closeWindow"></div>
      </div>
    </div>

    <div class="layout-main">
      <!-- 左侧菜单栏 -->
      <div class="sidebar" :class="{ 'sidebar-collapsed': isCollapsed }">
        <!-- Logo 区域 -->
        <div class="sidebar-logo">
          <img src="/meituan.png" alt="美团" class="logo-image" />
          <span v-if="!isCollapsed" class="logo-text">美团管理</span>
        </div>

        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          background-color="transparent"
          text-color="rgba(255, 255, 255, 0.7)"
          active-text-color="#ffffff"
          :collapse="isCollapsed"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/" class="menu-item">
            <el-icon><HomeFilled /></el-icon>
            <span>主页</span>
          </el-menu-item>
          
          <el-menu-item index="/profile" class="menu-item" v-if="hasPermission(['USER'])">
            <el-icon><User /></el-icon>
            <span>个人资料</span>
          </el-menu-item>
          
          <el-menu-item index="/products" class="menu-item" v-if="hasPermission(['SUPER_ADMIN', 'ADMIN'])">
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
          
          <el-menu-item index="/import" class="menu-item" v-if="hasPermission(['SUPER_ADMIN', 'ADMIN'])">
            <el-icon><Upload /></el-icon>
            <span>批量导入</span>
          </el-menu-item>
          
          <el-menu-item index="/upload" class="menu-item" v-if="hasPermission(['SUPER_ADMIN', 'ADMIN'])">
            <el-icon><Promotion /></el-icon>
            <span>批量上传</span>
          </el-menu-item>
          
          <el-menu-item index="/template" class="menu-item" v-if="hasPermission(['SUPER_ADMIN', 'ADMIN'])">
            <el-icon><Document /></el-icon>
            <span>模板管理</span>
          </el-menu-item>
          
          <el-menu-item index="/logs" class="menu-item" v-if="hasPermission(['SUPER_ADMIN', 'ADMIN'])">
            <el-icon><List /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
          
          <el-menu-item index="/members" class="menu-item" v-if="hasPermission(['SUPER_ADMIN'])">
            <el-icon><User /></el-icon>
            <span>成员管理</span>
          </el-menu-item>
          
          <el-menu-item index="/settings" class="menu-item" v-if="hasPermission(['SUPER_ADMIN', 'ADMIN'])">
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 右侧内容区 -->
      <div class="content-wrapper">
        <!-- 顶部导航栏 -->
        <div class="navbar">
          <div class="navbar-left">
            <div class="navbar-actions">
              <el-button 
                :icon="Fold" 
                circle 
                @click="toggleSidebar"
                class="action-btn"
                title="折叠/展开菜单"
              />
              <el-button 
                :icon="Refresh" 
                circle 
                @click="handleRefresh"
                class="action-btn"
                title="刷新页面"
              />
            </div>
            <el-breadcrumb separator="/" class="breadcrumb">
              <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="breadcrumb">{{ breadcrumb }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          
          <div class="navbar-right">
            <el-dropdown @command="handleCommand" trigger="click">
              <div class="user-info">
                <el-avatar :size="36" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" class="user-avatar" />
                <div class="user-details">
                  <span class="username">{{ userStore.userInfo.username || '用户' }}</span>
                  <span class="user-role">管理员</span>
                </div>
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="custom-dropdown">
                  <el-dropdown-item command="profile" class="dropdown-item">
                    <div class="dropdown-item-content">
                      <el-icon class="item-icon"><User /></el-icon>
                      <span>个人资料</span>
                    </div>
                  </el-dropdown-item>
                  <el-dropdown-item command="settings" class="dropdown-item" v-if="hasPermission(['SUPER_ADMIN', 'ADMIN'])">
                    <div class="dropdown-item-content">
                      <el-icon class="item-icon"><Setting /></el-icon>
                      <span>系统设置</span>
                    </div>
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout" class="dropdown-item logout-item">
                    <div class="dropdown-item-content">
                      <el-icon class="item-icon"><SwitchButton /></el-icon>
                      <span>退出登录</span>
                    </div>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <!-- 主内容区 -->
        <div class="main-content">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  HomeFilled,
  Goods,
  Upload,
  Promotion,
  Document,
  List,
  Setting,
  User,
  SwitchButton,
  ArrowDown,
  Fold,
  Refresh
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentTime = ref('')
const isCollapsed = ref(false)

// 获取当前用户角色
const userRole = computed(() => userStore.userInfo?.role || 'USER')

// 检查是否有权限访问某个菜单
const hasPermission = (requiredRoles) => {
  if (!requiredRoles || requiredRoles.length === 0) return true
  return requiredRoles.includes(userRole.value)
}

// 菜单映射
const menuMap = {
  '/': '首页',
  '/products': '商品管理',
  '/import': '批量导入',
  '/upload': '批量上传',
  '/template': '模板管理',
  '/logs': '操作日志',
  '/members': '成员管理',
  '/profile': '个人设置',
  '/settings': '系统设置'
}

const activeMenu = computed(() => route.path)
const breadcrumb = computed(() => menuMap[route.path] || '')

const handleMenuSelect = (index) => {
  router.push(index)
}

// 折叠/展开侧边栏
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

// 刷新当前页面
const handleRefresh = () => {
  // 使用router的replace方法实现局部刷新
  const currentPath = route.path
  router.replace({ path: '/redirect' + currentPath })
}

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
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
      break
  }
}

// 更新时间
const updateTime = () => {
  const now = new Date()
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  currentTime.value = `${hours}:${minutes}`
}

// 窗口控制函数
const minimizeWindow = () => {
  console.log('minimizeWindow clicked', window.electronAPI)
  if (window.electronAPI) {
    window.electronAPI.minimizeWindow()
  } else {
    console.error('electronAPI not available')
  }
}

const maximizeWindow = () => {
  console.log('maximizeWindow clicked', window.electronAPI)
  if (window.electronAPI) {
    window.electronAPI.maximizeWindow()
  } else {
    console.error('electronAPI not available')
  }
}

const closeWindow = () => {
  console.log('closeWindow clicked', window.electronAPI)
  if (window.electronAPI) {
    window.electronAPI.closeWindow()
  } else {
    console.error('electronAPI not available')
  }
}

let timer = null
onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 60000) // 每分钟更新一次
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.layout-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #fafbfc;
}

/* macOS 风格标题栏 */
.mac-titlebar {
  height: 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 1000;
  -webkit-app-region: drag;
}

.titlebar-left,
.titlebar-center {
  -webkit-app-region: no-drag;
}

.app-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.time {
  font-size: 13px;
  font-weight: 500;
  color: #666;
}

/* macOS 红黄绿三个圆点 */
.traffic-lights {
  display: flex;
  gap: 8px;
  -webkit-app-region: no-drag;
}

.traffic-light {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.traffic-light:hover {
  transform: scale(1.15);
}

.traffic-light.close {
  background-color: #ff5f57;
}

.traffic-light.close:hover {
  background-color: #ff3b30;
}

.traffic-light.minimize {
  background-color: #ffbd2e;
}

.traffic-light.minimize:hover {
  background-color: #ff9500;
}

.traffic-light.maximize {
  background-color: #28c840;
}

.traffic-light.maximize:hover {
  background-color: #34c759;
}

/* 悬停时显示图标 */
.traffic-light:hover::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.traffic-light.close:hover::after {
  content: '×';
  font-size: 14px;
  color: white;
  font-weight: bold;
  line-height: 1;
}

.traffic-light.minimize:hover::after {
  content: '−';
  font-size: 14px;
  color: white;
  font-weight: bold;
  line-height: 1;
}

.traffic-light.maximize:hover::after {
  width: 8px;
  height: 8px;
  border: 2px solid white;
  border-radius: 2px;
}

/* 主布局 */
.layout-main {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 左侧菜单栏 */
.sidebar {
  width: 220px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  overflow-y: auto;
  transition: width 0.3s ease;
  border-right: 1px solid rgba(0, 0, 0, 0.08);
}

.sidebar.sidebar-collapsed {
  width: 64px;
}

.sidebar.sidebar-collapsed .sidebar-logo .logo-text {
  display: none;
}

/* Logo 区域 */
.sidebar-logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.5);
}

.logo-image {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  border-radius: 10px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  letter-spacing: 0.5px;
}

.sidebar-menu {
  border-right: none;
  height: calc(100% - 64px);
  padding: 12px 0;
  background: transparent;
}

.sidebar-menu .menu-item {
  height: 44px;
  line-height: 44px;
  margin: 4px 12px;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #6b7280;
  font-weight: 500;
}

.sidebar-menu .menu-item:hover {
  background-color: rgba(0, 0, 0, 0.05) !important;
  color: #1a1a1a;
}

.sidebar-menu .menu-item.is-active {
  background: #FFD100 !important;
  box-shadow: 0 2px 8px rgba(255, 209, 0, 0.25);
  font-weight: 600;
  color: #1a1a1a !important;
}

.sidebar-menu .menu-item.is-active .el-icon {
  color: #1a1a1a;
}

.sidebar-menu .menu-item .el-icon {
  font-size: 18px;
  color: inherit;
}

/* 右侧内容区 */
.content-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 顶部导航栏 */
.navbar {
  height: 64px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  z-index: 10;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}

.navbar-left {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 20px;
}

.navbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.navbar-actions .el-button + .el-button {
  margin-left: 0;
}

.action-btn {
  width: 40px;
  height: 40px;
  border: none;
  background-color: rgba(0, 0, 0, 0.05);
  color: #6b7280;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 10px;
}

.action-btn:hover {
  background: #FFD100;
  border-color: transparent;
  color: #1a1a1a;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 209, 0, 0.25);
}

.breadcrumb {
  flex: 1;
  font-weight: 500;
}

.breadcrumb :deep(.el-breadcrumb__item) {
  font-size: 14px;
}

.breadcrumb :deep(.el-breadcrumb__inner) {
  color: #6b7280;
  font-weight: 500;
}

.breadcrumb :deep(.el-breadcrumb__inner:hover) {
  color: #1a1a1a;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background-color: rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.user-info:hover {
  background: rgba(255, 209, 0, 0.1);
  border-color: #FFD100;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 209, 0, 0.15);
}

.user-avatar {
  flex-shrink: 0;
  border: 2px solid rgba(255, 209, 0, 0.3);
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 80px;
}

.username {
  font-size: 14px;
  color: #1a1a1a;
  font-weight: 600;
  line-height: 1.2;
}

.user-role {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.2;
}

.dropdown-icon {
  font-size: 14px;
  color: #6b7280;
  transition: transform 0.3s, color 0.3s;
}

.user-info:hover .dropdown-icon {
  color: #FFD100;
  transform: rotate(180deg);
}

/* 下拉菜单样式 */
.custom-dropdown {
  min-width: 200px;
  padding: 8px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.dropdown-item {
  border-radius: 8px;
  margin: 4px 0;
  padding: 0;
  transition: all 0.3s;
}

.dropdown-item-content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
}

.item-icon {
  font-size: 18px;
  color: #6b7280;
  transition: color 0.3s;
}

.dropdown-item:hover {
  background: rgba(255, 209, 0, 0.1);
}

.dropdown-item:hover .item-icon {
  color: #FFD100;
}

.dropdown-item:hover span {
  color: #1a1a1a;
}

.logout-item:hover {
  background: rgba(239, 68, 68, 0.1);
}

.logout-item:hover .item-icon {
  color: #ef4444;
}

.logout-item:hover span {
  color: #ef4444;
}

/* 主内容区 */
.main-content {
  flex: 1;
  overflow-y: auto;
  background-color: #fafbfc;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
