<template>
  <div>
    <nav class="navigation-bar" :class="{ 'is-scrolled': isScrolled, 'is-hidden': !isVisible }" role="navigation">
      <div class="nav-container">
        <router-link to="/" class="nav-logo">
          <img src="/images/meituan.png" alt="美团" class="logo-image" />
          <span class="logo-text">美团商品批量上传系统</span>
        </router-link>
        
        <!-- 桌面端导航 -->
        <div class="nav-links desktop-nav">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="nav-link"
            :class="{ 'is-active': isActiveLink(item.path) }"
          >
            {{ item.label }}
          </router-link>
          <span class="nav-indicator" :style="indicatorStyle"></span>
        </div>
        
        <!-- 移动端汉堡菜单按钮 -->
        <button
          class="hamburger-button mobile-only"
          :class="{ 'is-open': isMobileMenuOpen }"
          @click.stop="toggleMobileMenu"
          :aria-label="isMobileMenuOpen ? '关闭菜单' : '打开菜单'"
          :aria-expanded="isMobileMenuOpen"
        >
          <span class="hamburger-line"></span>
          <span class="hamburger-line"></span>
          <span class="hamburger-line"></span>
        </button>
      </div>
    </nav>
    
    <!-- 移动端菜单 - 独立于导航栏 -->
    <transition name="mobile-menu">
      <div v-if="isMobileMenuOpen" class="mobile-menu-overlay" @click="closeMobileMenu">
        <div class="mobile-menu" @click.stop>
          <div class="mobile-menu-content">
            <router-link
              v-for="item in navItems"
              :key="item.path"
              :to="item.path"
              class="mobile-nav-link"
              :class="{ 'is-active': isActiveLink(item.path) }"
              @click="closeMobileMenu"
            >
              {{ item.label }}
            </router-link>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'

interface NavItem {
  label: string
  path: string
}

const navItems: NavItem[] = [
  { label: '首页', path: '/' },
  { label: '使用教程', path: '/tutorial' },
  { label: '安装指南', path: '/installation' },
  { label: '常见问题', path: '/faq' },
  { label: '下载', path: '/#download' }
]

const route = useRoute()
const isScrolled = ref(false)
const isVisible = ref(false)
const isMobileMenuOpen = ref(false)
const indicatorStyle = ref({
  left: '0px',
  width: '0px',
  opacity: '0'
})

const handleScroll = () => {
  const scrollY = window.scrollY
  isScrolled.value = scrollY > 50
  
  // 在移动端，只有首页才需要滑动展示，其他页面始终显示
  if (window.innerWidth <= 768) {
    if (route.path === '/') {
      // 首页：滚动超过10px才显示导航栏（但如果菜单打开，则始终显示）
      isVisible.value = scrollY > 10 || isMobileMenuOpen.value
    } else {
      // 其他页面：始终显示
      isVisible.value = true
    }
  } else {
    // 桌面端始终显示
    isVisible.value = true
  }
}

const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
  
  // 当菜单打开时，确保导航栏可见
  if (isMobileMenuOpen.value && window.innerWidth <= 768) {
    isVisible.value = true
  }
  
  // 锁定/解锁 body 滚动
  if (isMobileMenuOpen.value) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
    // 关闭菜单后，根据页面和滚动位置决定是否显示导航栏
    if (window.innerWidth <= 768) {
      if (route.path === '/') {
        isVisible.value = window.scrollY > 10
      } else {
        isVisible.value = true
      }
    }
  }
}

const closeMobileMenu = () => {
  isMobileMenuOpen.value = false
  document.body.style.overflow = ''
  // 关闭菜单后，根据页面和滚动位置决定是否显示导航栏
  if (window.innerWidth <= 768) {
    if (route.path === '/') {
      isVisible.value = window.scrollY > 10
    } else {
      isVisible.value = true
    }
  }
}

const isActiveLink = (path: string) => {
  // 处理 hash 链接 - 只有在实际滚动到该区域时才激活
  if (path.includes('#download')) {
    return false // 下载链接不显示为激活状态
  }
  // 精确匹配首页
  if (path === '/') {
    return route.path === '/'
  }
  // 其他页面匹配
  return route.path === path
}

const updateIndicator = () => {
  nextTick(() => {
    const activeLink = document.querySelector('.nav-link.is-active') as HTMLElement
    if (activeLink) {
      const navLinks = document.querySelector('.nav-links') as HTMLElement
      if (navLinks) {
        const navLinksRect = navLinks.getBoundingClientRect()
        const activeLinkRect = activeLink.getBoundingClientRect()
        
        indicatorStyle.value = {
          left: `${activeLinkRect.left - navLinksRect.left}px`,
          width: `${activeLinkRect.width}px`,
          opacity: '1'
        }
      }
    } else {
      indicatorStyle.value = {
        ...indicatorStyle.value,
        opacity: '0'
      }
    }
  })
}

watch(() => route.path, () => {
  updateIndicator()
  // 路由变化时更新导航栏可见性
  if (window.innerWidth <= 768) {
    if (route.path === '/') {
      isVisible.value = window.scrollY > 10
    } else {
      isVisible.value = true
    }
  }
})

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
  window.addEventListener('resize', updateIndicator)
  // 初始化可见性状态
  if (window.innerWidth <= 768) {
    if (route.path === '/') {
      isVisible.value = window.scrollY > 10
    } else {
      isVisible.value = true
    }
  } else {
    isVisible.value = true
  }
  updateIndicator()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('resize', updateIndicator)
  document.body.style.overflow = ''
})
</script>

<style scoped>
.navigation-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  transition: all 0.3s ease;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.navigation-bar.is-scrolled {
  background-color: rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.08);
  border-bottom-color: rgba(0, 0, 0, 0.1);
}

/* 移动端隐藏状态 */
@media (max-width: 768px) {
  .navigation-bar.is-hidden {
    transform: translateY(-100%);
    opacity: 0;
  }
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--spacing-md);
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-logo {
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.logo-image {
  height: 32px;
  width: auto;
  object-fit: contain;
}

.logo-text {
  font-size: 17px;
  font-weight: 600;
  color: #1d1d1f;
  transition: color 0.2s ease;
  letter-spacing: -0.3px;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', sans-serif;
}

.nav-logo:hover .logo-text {
  color: #FFD100;
}

.desktop-nav {
  display: flex;
  gap: var(--spacing-md);
  align-items: center;
  position: relative;
}

.nav-link {
  text-decoration: none;
  color: #6e6e73;
  font-size: 15px;
  font-weight: 500;
  padding: 0.5rem 0.75rem;
  border-radius: 8px;
  transition: all 0.2s ease;
  position: relative;
  letter-spacing: -0.2px;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', sans-serif;
}

.nav-link:hover {
  color: #1d1d1f;
  background-color: rgba(0, 0, 0, 0.04);
}

.nav-link.is-active {
  color: #1d1d1f;
  font-weight: 600;
  background-color: rgba(255, 209, 0, 0.15);
}

/* 滑动指示器 */
.nav-indicator {
  position: absolute;
  bottom: -2px;
  height: 2px;
  background: linear-gradient(90deg, #FFD100 0%, #FFC300 100%);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  pointer-events: none;
  border-radius: 2px;
}

/* 汉堡菜单按钮 */
.hamburger-button {
  display: none;
  flex-direction: column;
  justify-content: space-around;
  width: 32px;
  height: 32px;
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 4px;
  z-index: 1001;
  position: relative;
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
}

.hamburger-line {
  width: 100%;
  height: 3px;
  background-color: #1d1d1f;
  border-radius: 2px;
  transition: all 0.3s ease;
}

.hamburger-button.is-open .hamburger-line {
  background-color: #1d1d1f;
}

.hamburger-button.is-open .hamburger-line:nth-child(1) {
  transform: rotate(45deg) translateY(10px);
}

.hamburger-button.is-open .hamburger-line:nth-child(2) {
  opacity: 0;
}

.hamburger-button.is-open .hamburger-line:nth-child(3) {
  transform: rotate(-45deg) translateY(-10px);
}

/* 移动端菜单遮罩层 */
.mobile-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 1002;
  backdrop-filter: blur(2px);
  -webkit-backdrop-filter: blur(2px);
}

/* 移动端菜单 */
.mobile-menu {
  position: absolute;
  top: 64px;
  left: 0;
  right: 0;
  height: auto;
  max-height: 50vh;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  overflow-y: auto;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

.mobile-menu-content {
  padding: 8px 16px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.mobile-nav-link {
  text-decoration: none;
  color: #007AFF;
  font-size: 17px;
  font-weight: 400;
  padding: 14px 16px;
  transition: all 0.2s ease;
  text-align: center;
  display: block;
  background: transparent;
  border-radius: 10px;
  letter-spacing: -0.4px;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text', sans-serif;
}

.mobile-nav-link:hover {
  background: rgba(0, 122, 255, 0.08);
}

.mobile-nav-link.is-active {
  background: rgba(0, 122, 255, 0.12);
  color: #007AFF;
  font-weight: 600;
}

/* 移动菜单过渡动画 - iPhone 风格 */
.mobile-menu-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.mobile-menu-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 1, 1);
}

.mobile-menu-enter-from {
  opacity: 0;
}

.mobile-menu-enter-from .mobile-menu {
  transform: translateY(-10px);
}

.mobile-menu-leave-to {
  opacity: 0;
}

.mobile-menu-leave-to .mobile-menu {
  transform: translateY(-10px);
}

/* 响应式 */
@media (max-width: 768px) {
  .desktop-nav {
    display: none;
  }
  
  .hamburger-button {
    display: flex;
  }
  
  .mobile-only {
    display: flex;
  }
  
  .logo-image {
    height: 28px;
  }
  
  .logo-text {
    font-size: var(--font-size-base);
  }
  
  .nav-container {
    padding: 0 var(--spacing-sm);
  }
}

@media (min-width: 769px) {
  .mobile-only {
    display: none;
  }
  
  .mobile-menu {
    display: none;
  }
}
</style>
