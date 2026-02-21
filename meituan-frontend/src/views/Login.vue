<template>
  <div class="login-container">
    <!-- macOS 风格窗口控制栏 -->
    <div class="mac-titlebar">
      <div class="traffic-lights">
        <div class="traffic-light minimize" @click="minimizeWindow"></div>
        <div class="traffic-light maximize" @click="maximizeWindow"></div>
        <div class="traffic-light close" @click="closeWindow"></div>
      </div>
    </div>

    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <div class="login-content">
      <!-- 左侧品牌区 -->
      <div class="brand-section animate-slide-in-left">
        <div class="brand-logo">
          <img src="/meituan.png" alt="美团" />
        </div>
        <h1 class="brand-title">美团商品管理系统</h1>
        <p class="brand-desc">高效管理商品数据，一键批量上传到美团平台</p>
        
        <div class="features">
          <div class="feature-item">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M9 12L11 14L15 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/>
              </svg>
            </div>
            <span>批量导入商品</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M9 12L11 14L15 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/>
              </svg>
            </div>
            <span>一键生成模板</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M9 12L11 14L15 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/>
              </svg>
            </div>
            <span>智能数据管理</span>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="form-section animate-slide-in-right">
        <div class="form-box">
          <div class="form-header">
            <h2>欢迎回来</h2>
            <p>登录您的账户以继续</p>
          </div>
          
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            @keyup.enter="handleLogin"
          >
            <el-form-item prop="username">
              <div class="input-wrapper">
                <div class="input-icon">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="2"/>
                    <path d="M6 21C6 17.134 8.686 14 12 14C15.314 14 18 17.134 18 21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                </div>
                <el-input
                  v-model="loginForm.username"
                  placeholder="请输入用户名或手机号"
                  size="large"
                />
              </div>
            </el-form-item>
            
            <el-form-item prop="password">
              <div class="input-wrapper">
                <div class="input-icon">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="5" y="11" width="14" height="10" rx="2" stroke="currentColor" stroke-width="2"/>
                    <path d="M8 11V7C8 4.79086 9.79086 3 12 3C14.2091 3 16 4.79086 16 7V11" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                </div>
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  size="large"
                  show-password
                />
              </div>
            </el-form-item>
            
            <div class="form-options">
              <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
            </div>
            
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                @click="handleLogin"
                class="login-btn"
              >
                {{ loading ? '登录中...' : '登录' }}
              </el-button>
            </el-form-item>
            
            <div class="form-footer">
              <span>还没有账号？</span>
              <el-link type="primary" @click="goToRegister" :underline="false">立即注册</el-link>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

// 从路由参数中获取用户名（注册后跳转）
onMounted(() => {
  if (route.query.username) {
    loginForm.username = route.query.username
  }
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.login(loginForm)
        ElMessage.success('登录成功')
        router.push('/')
      } catch (error) {
        // 错误消息已经在 API 拦截器中显示，这里不需要再显示
        console.error('登录失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

const goToRegister = () => {
  router.push('/register')
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
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #fafbfc 0%, #e8eef5 100%);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255, 209, 0, 0.1) 0%, rgba(255, 167, 38, 0.1) 100%);
  animation: float 20s infinite ease-in-out;
}

.circle-1 {
  width: 400px;
  height: 400px;
  top: -200px;
  left: -200px;
  animation-delay: 0s;
}

.circle-2 {
  width: 300px;
  height: 300px;
  bottom: -150px;
  right: -150px;
  animation-delay: 5s;
}

.circle-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  right: 10%;
  animation-delay: 10s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -30px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
}

/* macOS 风格标题栏 */
.mac-titlebar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 40px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  -webkit-app-region: drag;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 16px;
  z-index: 1000;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.traffic-lights {
  display: flex;
  gap: 8px;
  -webkit-app-region: no-drag;
}

.traffic-light {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s;
}

.traffic-light:hover {
  transform: scale(1.2);
}

.traffic-light.close {
  background-color: #ff5f57;
}

.traffic-light.minimize {
  background-color: #ffbd2e;
}

.traffic-light.maximize {
  background-color: #28c840;
}

/* 主内容区 */
.login-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 40px 40px;
  gap: 80px;
  position: relative;
  z-index: 1;
}

/* 进入动画 */
@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-60px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(60px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.animate-slide-in-left {
  animation: slideInLeft 0.8s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

.animate-slide-in-right {
  animation: slideInRight 0.8s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

/* 左侧品牌区 */
.brand-section {
  flex: 1;
  max-width: 500px;
}

.brand-logo {
  width: 80px;
  height: 80px;
  margin-bottom: 32px;
  animation: bounce 2s infinite;
}

.brand-logo img {
  width: 100%;
  height: 100%;
  border-radius: 20px;
  box-shadow: 0 8px 24px rgba(255, 209, 0, 0.3);
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.brand-title {
  font-size: 42px;
  font-weight: 800;
  color: #1a1a1a;
  margin-bottom: 16px;
  line-height: 1.2;
  letter-spacing: -0.5px;
}

.brand-desc {
  font-size: 18px;
  color: #6b7280;
  margin-bottom: 48px;
  line-height: 1.6;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
}

.feature-item:hover {
  transform: translateX(8px);
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.feature-icon {
  width: 40px;
  height: 40px;
  background: #FFD100;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.feature-icon svg {
  width: 20px;
  height: 20px;
  color: #1a1a1a;
}

.feature-item span {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

/* 右侧表单区 */
.form-section {
  flex: 0 0 460px;
}

.form-box {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 48px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.form-header {
  margin-bottom: 40px;
}

.form-header h2 {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.form-header p {
  font-size: 15px;
  color: #6b7280;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.input-wrapper {
  position: relative;
  width: 100%;
}

.input-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  color: #9ca3af;
  z-index: 1;
  pointer-events: none;
}

.input-icon svg {
  width: 100%;
  height: 100%;
}

.login-form :deep(.el-input) {
  width: 100%;
}

.login-form :deep(.el-input__wrapper) {
  width: 100%;
  padding-left: 48px;
  border-radius: 12px;
  box-shadow: none;
  border: 1px solid rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: #FFD100;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: #FFD100;
  box-shadow: 0 0 0 3px rgba(255, 209, 0, 0.1);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.form-options :deep(.el-checkbox__label) {
  color: #6b7280;
  font-size: 14px;
}

/* 自定义复选框样式 - 黄色对号 */
.form-options :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #FFD100;
  border-color: #FFD100;
}

.form-options :deep(.el-checkbox__input.is-checked .el-checkbox__inner::after) {
  border-color: #1a1a1a;
}

.form-options :deep(.el-checkbox__input .el-checkbox__inner:hover) {
  border-color: #FFD100;
}

.form-options :deep(.el-checkbox__input.is-focus .el-checkbox__inner) {
  border-color: #FFD100;
}

.form-options :deep(.el-link) {
  font-size: 14px;
  color: #FFD100;
  font-weight: 500;
}

.login-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #FFD100 0%, #FFA726 100%);
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  transition: all 0.3s;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(255, 209, 0, 0.4);
}

.login-btn:active {
  transform: translateY(0);
}

.form-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #6b7280;
}

.form-footer span {
  margin-right: 8px;
}

.form-footer :deep(.el-link) {
  color: #FFD100;
  font-weight: 600;
}

/* 响应式 */
@media (max-width: 1200px) {
  .login-content {
    gap: 40px;
  }
  
  .brand-section {
    max-width: 400px;
  }
  
  .brand-title {
    font-size: 36px;
  }
}

@media (max-width: 900px) {
  .brand-section {
    display: none;
  }
  
  .form-section {
    flex: 0 0 auto;
    width: 100%;
    max-width: 460px;
  }
}
</style>
