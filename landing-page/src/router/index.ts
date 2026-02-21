import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import AppLayout from '@/layouts/AppLayout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: AppLayout,
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/pages/HomePage.vue'),
        meta: { title: '首页', showBreadcrumb: false }
      },
      {
        path: 'tutorial',
        name: 'Tutorial',
        component: () => import('@/pages/TutorialPage.vue'),
        meta: { title: '使用教程', showBreadcrumb: true }
      },
      {
        path: 'installation',
        name: 'Installation',
        component: () => import('@/pages/InstallationPage.vue'),
        meta: { title: '安装指南', showBreadcrumb: true }
      },
      {
        path: 'faq',
        name: 'FAQ',
        component: () => import('@/pages/FAQPage.vue'),
        meta: { title: '常见问题', showBreadcrumb: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, _from, savedPosition) {
    // 如果有 hash，滚动到对应元素
    if (to.hash) {
      return {
        el: to.hash,
        behavior: 'smooth'
      }
    }
    // 如果有保存的位置（浏览器前进/后退），恢复位置
    if (savedPosition) {
      return savedPosition
    }
    // 默认滚动到顶部
    return { top: 0, behavior: 'smooth' }
  }
})

// 更新页面标题
router.beforeEach((to, _from, next) => {
  const title = to.meta.title as string
  document.title = title ? `${title} - 美团商品批量上传系统` : '美团商品批量上传系统'
  next()
})

export default router
