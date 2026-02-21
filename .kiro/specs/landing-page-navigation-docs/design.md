# Design Document

## Overview

本设计文档描述了为美团商品批量上传系统落地页添加导航栏和独立文档页面的技术方案。系统将从单页滚动设计扩展为多页面单页应用（SPA），使用 Vue Router 进行路由管理，并创建独立的使用教程、安装指南和常见问题页面。

设计目标：
- 保持现有的美团品牌视觉风格
- 实现流畅的页面切换体验
- 提供响应式的导航体验
- 创建结构化的文档内容

## Architecture

### 系统架构

```
┌─────────────────────────────────────────┐
│           Vue 3 Application             │
├─────────────────────────────────────────┤
│                                         │
│  ┌──────────────────────────────────┐  │
│  │       Vue Router (v4)            │  │
│  │  - Route Configuration           │  │
│  │  - Navigation Guards             │  │
│  │  - Scroll Behavior               │  │
│  └──────────────────────────────────┘  │
│                                         │
│  ┌──────────────────────────────────┐  │
│  │      Layout Components           │  │
│  │  - AppLayout (with Navigation)   │  │
│  │  - NavigationBar                 │  │
│  │  - Breadcrumb                    │  │
│  │  - Footer                        │  │
│  └──────────────────────────────────┘  │
│                                         │
│  ┌──────────────────────────────────┐  │
│  │         Page Components          │  │
│  │  - HomePage (existing content)   │  │
│  │  - TutorialPage                  │  │
│  │  - InstallationPage              │  │
│  │  - FAQPage                       │  │
│  └──────────────────────────────────┘  │
│                                         │
│  ┌──────────────────────────────────┐  │
│  │    Shared Components             │  │
│  │  - Existing animation components │  │
│  │  - Existing UI components        │  │
│  └──────────────────────────────────┘  │
│                                         │
└─────────────────────────────────────────┘
```

### 路由架构

```
/                    → HomePage (首页)
/tutorial            → TutorialPage (使用教程)
/installation        → InstallationPage (安装指南)
/faq                 → FAQPage (常见问题)
```

## Components and Interfaces

### 1. NavigationBar Component

**职责：** 提供顶部导航栏，支持桌面和移动端

**Props:**
```typescript
interface NavigationBarProps {
  // 无需 props，导航项在组件内部定义
}
```

**State:**
```typescript
interface NavigationBarState {
  isMobileMenuOpen: boolean  // 移动菜单是否打开
  isScrolled: boolean        // 是否已滚动（用于添加阴影）
}
```

**导航项配置:**
```typescript
interface NavItem {
  label: string      // 显示文本
  path: string       // 路由路径
  icon?: string      // 可选图标
}

const navItems: NavItem[] = [
  { label: '首页', path: '/' },
  { label: '使用教程', path: '/tutorial' },
  { label: '安装指南', path: '/installation' },
  { label: '常见问题', path: '/faq' },
  { label: '下载', path: '/#download' }  // 锚点链接
]
```

**关键功能:**
- 监听滚动事件，超过 50px 时添加阴影
- 响应式设计：桌面显示水平菜单，移动显示汉堡菜单
- 高亮当前活动路由
- 移动菜单打开时禁用 body 滚动

### 2. Breadcrumb Component

**职责：** 显示当前页面的面包屑导航

**Props:**
```typescript
interface BreadcrumbProps {
  // 自动从路由生成，无需 props
}
```

**路由到面包屑映射:**
```typescript
const breadcrumbMap: Record<string, string> = {
  '/': '首页',
  '/tutorial': '使用教程',
  '/installation': '安装指南',
  '/faq': '常见问题'
}
```

### 3. AppLayout Component

**职责：** 提供统一的页面布局结构

**结构:**
```vue
<template>
  <div class="app-layout">
    <NavigationBar />
    <main class="main-content">
      <Breadcrumb v-if="showBreadcrumb" />
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <Footer />
  </div>
</template>
```

### 4. Page Components

#### HomePage
- 重构现有的 App.vue 内容
- 保持所有现有的 sections 和组件
- 移除顶层布局，只保留内容

#### TutorialPage

**内容结构:**
```typescript
interface TutorialStep {
  id: number
  title: string
  description: string
  details: string[]
  image?: string
}

const tutorialSteps: TutorialStep[] = [
  {
    id: 1,
    title: '准备商品数据',
    description: '准备您的商品信息，可以使用 Excel 或 CSV 格式',
    details: [
      '下载系统提供的模板文件',
      '按照模板格式填写商品信息',
      '确保必填字段完整（商品名称、价格、类目等）'
    ]
  },
  {
    id: 2,
    title: '导入商品文件',
    description: '将准备好的商品文件导入系统',
    details: [
      '点击"导入商品"按钮',
      '选择您的 Excel 或 CSV 文件',
      '系统会自动识别美团格式'
    ]
  },
  {
    id: 3,
    title: '预览和编辑',
    description: '查看导入的商品数据，进行必要的调整',
    details: [
      '在商品列表中查看所有导入的商品',
      '可以编辑、删除或添加商品',
      '检查商品图片是否正确'
    ]
  },
  {
    id: 4,
    title: '批量上传',
    description: '一键将商品批量上传到美团平台',
    details: [
      '点击"批量上传"按钮',
      '系统会自动上传所有商品',
      '查看上传进度和结果'
    ]
  }
]
```

#### InstallationPage

**内容结构:**
```typescript
interface SystemRequirement {
  category: string
  items: string[]
}

interface InstallationStep {
  id: number
  title: string
  description: string
  commands?: string[]
}

const systemRequirements: SystemRequirement[] = [
  {
    category: '操作系统',
    items: ['Windows 10 或更高版本', 'Windows 11 (推荐)']
  },
  {
    category: '硬件要求',
    items: ['至少 4GB 内存', '至少 500MB 可用磁盘空间']
  },
  {
    category: '软件依赖',
    items: ['无需额外安装，所有依赖已打包']
  }
]

const installationSteps: InstallationStep[] = [
  {
    id: 1,
    title: '下载安装包',
    description: '从官网下载最新版本的安装包'
  },
  {
    id: 2,
    title: '运行安装程序',
    description: '双击下载的安装包，按照向导完成安装'
  },
  {
    id: 3,
    title: '首次配置',
    description: '启动应用后，进行基本配置'
  },
  {
    id: 4,
    title: '开始使用',
    description: '配置完成后即可开始使用系统'
  }
]
```

#### FAQPage

**内容结构:**
```typescript
interface FAQItem {
  id: number
  category: string
  question: string
  answer: string
}

const faqCategories = ['安装问题', '使用问题', '错误排查', '其他']

const faqItems: FAQItem[] = [
  {
    id: 1,
    category: '安装问题',
    question: '安装时提示缺少依赖怎么办？',
    answer: '本系统已打包所有依赖，无需额外安装。如果仍有问题，请确保您的 Windows 系统已更新到最新版本。'
  },
  {
    id: 2,
    category: '使用问题',
    question: '支持哪些文件格式？',
    answer: '系统支持 Excel (.xlsx, .xls) 和 CSV (.csv) 格式。推荐使用 Excel 格式以获得最佳兼容性。'
  },
  {
    id: 3,
    category: '使用问题',
    question: '如何批量上传商品？',
    answer: '首先导入商品文件，系统会自动识别格式。然后在商品列表页面点击"批量上传"按钮即可。'
  },
  {
    id: 4,
    category: '错误排查',
    question: '上传失败怎么办？',
    answer: '请检查：1) 网络连接是否正常 2) 美团 API 配置是否正确 3) 商品数据是否符合要求。详细错误信息可在日志页面查看。'
  },
  {
    id: 5,
    category: '错误排查',
    question: '导入的商品数据不显示？',
    answer: '请确保文件格式正确，必填字段完整。可以下载系统提供的模板文件作为参考。'
  },
  {
    id: 6,
    category: '其他',
    question: '系统是否收费？',
    answer: '本系统完全免费，无需注册，下载即可使用。'
  }
]
```

## Data Models

### Router Configuration

```typescript
import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

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
  scrollBehavior(to, from, savedPosition) {
    if (to.hash) {
      return { el: to.hash, behavior: 'smooth' }
    }
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0, behavior: 'smooth' }
  }
})

// 更新页面标题
router.beforeEach((to, from, next) => {
  const title = to.meta.title as string
  document.title = title ? `${title} - 美团商品批量上传系统` : '美团商品批量上传系统'
  next()
})

export default router
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Navigation Link Active State
*For any* route path, when the user navigates to that path, the corresponding navigation link should be highlighted with the active state styling using Meituan yellow (#FFD100).
**Validates: Requirements 2.3, 2.5**

### Property 2: Navigation Sticky Position
*For any* scroll position, the navigation bar should remain fixed at the top of the viewport.
**Validates: Requirements 1.2**

### Property 3: Navigation Shadow on Scroll
*For any* scroll position greater than 50px, the navigation bar should display a shadow effect; for positions less than or equal to 50px, no shadow should be present.
**Validates: Requirements 1.5**

### Property 4: Mobile Menu Toggle Behavior
*For any* mobile menu state (open or closed), when the user clicks the hamburger menu icon, the menu state should toggle to the opposite state, and when a menu link is clicked, the menu should close and navigate to the target page.
**Validates: Requirements 3.2, 3.4**

### Property 5: Mobile Menu Body Scroll Lock
*For any* mobile menu state, when the menu is open, body scrolling should be disabled; when closed, body scrolling should be enabled.
**Validates: Requirements 3.3**

### Property 6: Page Transition and Scroll Reset
*For any* page navigation (excluding hash navigation), when the route changes, the system should apply a fade transition effect and reset the scroll position to the top of the page.
**Validates: Requirements 10.1, 10.3**

### Property 7: Transition Duration Constraint
*For any* page transition animation, the animation duration should be between 300ms and 500ms.
**Validates: Requirements 10.2**

### Property 8: Breadcrumb Navigation Accuracy
*For any* documentation page route, the breadcrumb should display the correct path from home to the current page, and clicking any breadcrumb link should navigate to the corresponding page.
**Validates: Requirements 8.2, 8.3**

### Property 9: Route Title Synchronization
*For any* route navigation, the browser document title should update to reflect the current page title in the format "{Page Title} - 美团商品批量上传系统".
**Validates: Requirements 9.3**

### Property 10: Hash Navigation Behavior
*For any* hash link (e.g., /#download), when clicked, the router should navigate to the home page and smoothly scroll to the corresponding element.
**Validates: Requirements 9.4**

### Property 11: Client-Side Routing
*For any* navigation between pages, the system should use client-side routing without triggering a full page refresh.
**Validates: Requirements 9.2**

### Property 12: Browser History Management
*For any* route change, the system should correctly update the browser's URL and history, and the browser's back/forward buttons should navigate to the correct pages.
**Validates: Requirements 9.3, 9.4**

### Property 13: Documentation Page Consistency
*For any* documentation page (tutorial, installation, FAQ), the page should use the same layout structure, consistent styling for headings and content, the same color scheme, persistent navigation bar, and unified footer.
**Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5**

### Property 14: Tutorial Step Structure
*For any* tutorial step displayed on the tutorial page, the step should have a number, title, and detailed description.
**Validates: Requirements 4.3**

### Property 15: FAQ Accordion Behavior
*For any* FAQ item, when the user clicks the question, the answer should expand or collapse with a smooth transition animation.
**Validates: Requirements 6.2, 6.3, 6.4**

### Property 16: Navigation Link Hover Feedback
*For any* navigation link, when the mouse hovers over it, the link should display visual feedback through color change or underline.
**Validates: Requirements 2.4**

### Property 17: Contrast Ratio Compliance
*For any* text displayed on the navigation bar against its background, the contrast ratio should meet WCAG AA standards (at least 4.5:1 for normal text).
**Validates: Requirements 1.4**

## Error Handling

### Router Error Handling

1. **404 Not Found**
   - 捕获未匹配的路由
   - 重定向到首页或显示 404 页面
   - 记录错误日志

2. **Navigation Failures**
   - 处理导航取消或中断
   - 提供用户友好的错误提示
   - 允许用户重试

### Component Error Handling

1. **Lazy Loading Failures**
   - 捕获组件加载失败
   - 显示加载错误提示
   - 提供重新加载选项

2. **Scroll Behavior Errors**
   - 处理无效的 hash 目标
   - 优雅降级到默认滚动行为

## Testing Strategy

### Unit Tests

使用 Vitest 进行单元测试：

1. **NavigationBar Component**
   - 测试导航项渲染
   - 测试移动菜单切换
   - 测试滚动监听和阴影效果
   - 测试活动链接高亮

2. **Breadcrumb Component**
   - 测试面包屑生成逻辑
   - 测试路径映射正确性

3. **Router Configuration**
   - 测试路由定义正确性
   - 测试 scrollBehavior 函数
   - 测试 beforeEach 导航守卫

### Property-Based Tests

使用 fast-check 库进行属性测试，每个测试运行至少 100 次迭代。每个属性测试必须使用注释标记对应的设计文档属性：

1. **Property 1: Navigation Link Active State**
   - 生成随机路由路径（从定义的路由列表中）
   - 验证对应的导航链接被正确高亮且使用 #FFD100 颜色
   - **Feature: landing-page-navigation-docs, Property 1: Navigation Link Active State**

2. **Property 2: Navigation Sticky Position**
   - 生成随机的滚动位置
   - 验证导航栏的 position 样式始终为 fixed
   - **Feature: landing-page-navigation-docs, Property 2: Navigation Sticky Position**

3. **Property 3: Navigation Shadow on Scroll**
   - 生成随机的滚动位置（0-1000px）
   - 验证 > 50px 时有阴影，<= 50px 时无阴影
   - **Feature: landing-page-navigation-docs, Property 3: Navigation Shadow on Scroll**

4. **Property 4: Mobile Menu Toggle Behavior**
   - 生成随机的菜单操作序列（打开、关闭、点击链接）
   - 验证状态切换正确且点击链接后菜单关闭
   - **Feature: landing-page-navigation-docs, Property 4: Mobile Menu Toggle Behavior**

5. **Property 5: Mobile Menu Body Scroll Lock**
   - 生成随机的菜单状态
   - 验证菜单打开时 body overflow 为 hidden，关闭时为 auto
   - **Feature: landing-page-navigation-docs, Property 5: Mobile Menu Body Scroll Lock**

6. **Property 6: Page Transition and Scroll Reset**
   - 生成随机的非 hash 路由导航序列
   - 验证每次导航后滚动位置为 0 且有过渡动画
   - **Feature: landing-page-navigation-docs, Property 6: Page Transition and Scroll Reset**

7. **Property 7: Transition Duration Constraint**
   - 检查所有页面过渡动画的 CSS
   - 验证 transition-duration 在 300-500ms 范围内
   - **Feature: landing-page-navigation-docs, Property 7: Transition Duration Constraint**

8. **Property 8: Breadcrumb Navigation Accuracy**
   - 生成随机的文档页面路由
   - 验证面包屑路径正确且点击可导航
   - **Feature: landing-page-navigation-docs, Property 8: Breadcrumb Navigation Accuracy**

9. **Property 9: Route Title Synchronization**
   - 生成随机的路由导航
   - 验证 document.title 格式正确
   - **Feature: landing-page-navigation-docs, Property 9: Route Title Synchronization**

10. **Property 10: Hash Navigation Behavior**
    - 生成随机的 hash 链接
    - 验证导航到首页且滚动到目标元素
    - **Feature: landing-page-navigation-docs, Property 10: Hash Navigation Behavior**

11. **Property 11: Client-Side Routing**
    - 生成随机的页面导航序列
    - 验证没有触发页面刷新（通过监听 beforeunload 事件）
    - **Feature: landing-page-navigation-docs, Property 11: Client-Side Routing**

12. **Property 12: Browser History Management**
    - 生成随机的导航序列和前进/后退操作
    - 验证 URL 和历史记录正确更新
    - **Feature: landing-page-navigation-docs, Property 12: Browser History Management**

13. **Property 13: Documentation Page Consistency**
    - 遍历所有文档页面
    - 验证布局、样式、导航栏、页脚的一致性
    - **Feature: landing-page-navigation-docs, Property 13: Documentation Page Consistency**

14. **Property 14: Tutorial Step Structure**
    - 生成随机的教程步骤数据
    - 验证每个步骤都有编号、标题和描述
    - **Feature: landing-page-navigation-docs, Property 14: Tutorial Step Structure**

15. **Property 15: FAQ Accordion Behavior**
    - 生成随机的 FAQ 项点击序列
    - 验证展开/折叠行为和过渡动画
    - **Feature: landing-page-navigation-docs, Property 15: FAQ Accordion Behavior**

16. **Property 16: Navigation Link Hover Feedback**
    - 遍历所有导航链接
    - 验证悬停时有视觉反馈（样式变化）
    - **Feature: landing-page-navigation-docs, Property 16: Navigation Link Hover Feedback**

17. **Property 17: Contrast Ratio Compliance**
    - 检查导航栏所有文本元素
    - 验证对比度 >= 4.5:1
    - **Feature: landing-page-navigation-docs, Property 17: Contrast Ratio Compliance**

### Integration Tests

1. **完整导航流程**
   - 测试从首页到各文档页面的导航
   - 验证页面内容正确加载
   - 验证面包屑和标题正确显示

2. **响应式行为**
   - 测试不同视口尺寸下的导航行为
   - 验证移动菜单在小屏幕上正确显示

3. **Hash 导航**
   - 测试首页内的锚点导航
   - 验证平滑滚动到目标元素

## Implementation Notes

### 技术栈

- **Vue 3**: 使用 Composition API
- **Vue Router 4**: 路由管理
- **TypeScript**: 类型安全
- **Vite**: 构建工具
- **CSS Variables**: 主题管理

### 样式指南

1. **导航栏样式**
   - 背景色: `rgba(255, 255, 255, 0.95)` (半透明白色)
   - 高度: `64px`
   - 阴影: `0 2px 8px rgba(0, 0, 0, 0.1)` (滚动时)
   - 活动链接: 美团黄 `#FFD100`

2. **页面过渡**
   - 淡入淡出: `opacity` 从 0 到 1
   - 持续时间: `300ms`
   - 缓动函数: `ease-in-out`

3. **移动菜单**
   - 全屏覆盖
   - 背景: `rgba(0, 0, 0, 0.95)`
   - 滑入动画: `transform: translateX(-100%)` 到 `translateX(0)`

### 性能优化

1. **路由懒加载**
   - 所有页面组件使用动态导入
   - 减少初始包大小

2. **滚动监听优化**
   - 使用 `throttle` 限制滚动事件频率
   - 使用 `passive` 事件监听器

3. **移动菜单优化**
   - 使用 CSS `transform` 而非 `left/right`
   - 启用硬件加速

### 可访问性

1. **键盘导航**
   - 所有导航链接可通过 Tab 键访问
   - 移动菜单可通过 Escape 键关闭

2. **ARIA 属性**
   - 导航栏: `role="navigation"`
   - 移动菜单按钮: `aria-label="打开菜单"`, `aria-expanded`
   - 面包屑: `aria-label="面包屑导航"`

3. **焦点管理**
   - 页面切换后焦点移到主内容区
   - 移动菜单打开时焦点移到菜单内

### 浏览器兼容性

- 现代浏览器 (Chrome, Firefox, Safari, Edge)
- 不支持 IE11
- 使用 Vite 的默认 polyfill 配置
