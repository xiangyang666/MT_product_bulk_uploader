import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue')
      },
      {
        path: 'products',
        name: 'Products',
        component: () => import('@/views/Products.vue'),
        meta: { requiresRole: ['SUPER_ADMIN', 'ADMIN'] }
      },
      {
        path: 'import',
        name: 'Import',
        component: () => import('@/views/Import.vue'),
        meta: { requiresRole: ['SUPER_ADMIN', 'ADMIN'] }
      },
      {
        path: 'template',
        name: 'Template',
        component: () => import('@/views/Template.vue'),
        meta: { requiresRole: ['SUPER_ADMIN', 'ADMIN'] }
      },
      {
        path: 'upload',
        name: 'Upload',
        component: () => import('@/views/Upload.vue'),
        meta: { requiresRole: ['SUPER_ADMIN', 'ADMIN'] }
      },
      {
        path: 'logs',
        name: 'Logs',
        component: () => import('@/views/Logs.vue'),
        meta: { requiresRole: ['SUPER_ADMIN', 'ADMIN'] }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue'),
        meta: { requiresRole: ['SUPER_ADMIN', 'ADMIN'] }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/User.vue'),
        meta: { requiresRole: ['SUPER_ADMIN', 'ADMIN'] }
      },
      {
        path: 'members',
        name: 'Members',
        component: () => import('@/views/MemberManagement.vue'),
        meta: { requiresRole: ['SUPER_ADMIN'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/ProfileSettings.vue')
      },
      {
        path: 'redirect/:path(.*)',
        name: 'Redirect',
        component: () => import('@/views/Redirect.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const requiresRole = to.meta.requiresRole

  if (requiresAuth && !userStore.isLoggedIn) {
    // 需要登录但未登录，跳转到登录页
    next('/login')
  } else if (!requiresAuth && userStore.isLoggedIn && (to.path === '/login' || to.path === '/register')) {
    // 已登录用户访问登录/注册页，跳转到首页
    next('/')
  } else if (requiresRole && requiresRole.length > 0) {
    // 需要特定角色权限
    const userRole = userStore.userInfo?.role || 'USER'
    if (requiresRole.includes(userRole)) {
      next()
    } else {
      // 权限不足，跳转到首页
      next('/')
    }
  } else {
    next()
  }
})

export default router
