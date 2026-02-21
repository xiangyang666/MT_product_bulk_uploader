<template>
  <div class="register-container">
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

    <div class="register-content">
      <!-- 左侧注册表单 -->
      <div class="form-section animate-slide-in-left">
        <div class="form-box">
          <div class="form-header">
            <h2>创建账户</h2>
            <p>填写以下信息完成注册</p>
          </div>
          
          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            class="register-form"
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
                  v-model="registerForm.username"
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
                  v-model="registerForm.password"
                  type="password"
                  placeholder="请输入密码"
                  size="large"
                  show-password
                />
              </div>
            </el-form-item>
            
            <el-form-item prop="confirmPassword">
              <div class="input-wrapper">
                <div class="input-icon">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="5" y="11" width="14" height="10" rx="2" stroke="currentColor" stroke-width="2"/>
                    <path d="M8 11V7C8 4.79086 9.79086 3 12 3C14.2091 3 16 4.79086 16 7V11" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    <circle cx="12" cy="16" r="1" fill="currentColor"/>
                  </svg>
                </div>
                <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="请确认密码"
                  size="large"
                  show-password
                />
              </div>
            </el-form-item>
            
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                @click="handleRegister"
                class="register-btn"
              >
                {{ loading ? '注册中...' : '立即注册' }}
              </el-button>
            </el-form-item>
            
            <div class="form-footer">
              <span>已有账号？</span>
              <el-link type="primary" @click="goToLogin" :underline="false">立即登录</el-link>
            </div>
          </el-form>
        </div>
      </div>

      <!-- 右侧品牌区 -->
      <div class="brand-section animate-slide-in-right">
        <div class="brand-logo">
          <img src="/meituan.png" alt="美团" />
        </div>
        <h1 class="brand-title">开始您的旅程</h1>
        <p class="brand-desc">注册账号，立即体验强大的商品管理功能</p>
        
        <div class="benefits">
          <div class="benefit-item">
            <div class="benefit-number">01</div>
            <div class="benefit-content">
              <h3>快速上手</h3>
              <p>简单直观的操作界面，无需培训即可使用</p>
            </div>
          </div>
          <div class="benefit-item">
            <div class="benefit-number">02</div>
            <div class="benefit-content">
              <h3>高效管理</h3>
              <p>批量处理商品数据，节省大量时间</p>
            </div>
          </div>
          <div class="benefit-item">
            <div class="benefit-number">03</div>
            <div class="benefit-content">
              <h3>安全可靠</h3>
              <p>数据加密存储，保障信息安全</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.register(registerForm)
        ElMessage.success('注册成功，请登录')
        router.push({
          path: '/login',
          query: { username: registerForm.username }
        })
      } catch (error) {
        ElMessage.error(error.message || '注册失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const goToLogin = () => {
  router.push('/login')
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
.register-container {
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
  right: -200px;
  animation-delay: 0s;
}

.circle-2 {
  width: 300px;
  height: 300px;
  bottom: -150px;
  left: -150px;
  animation-delay: 5s;
}

.circle-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  left: 10%;
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
.register-content {
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

.benefits {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.benefit-item {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
}

.benefit-item:hover {
  transform: translateX(8px);
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.benefit-number {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #FFD100 0%, #FFA726 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  flex-shrink: 0;
}

.benefit-content h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.benefit-content p {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  line-height: 1.5;
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

.register-form :deep(.el-form-item) {
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

.register-form :deep(.el-input) {
  width: 100%;
}

.register-form :deep(.el-input__wrapper) {
  width: 100%;
  padding-left: 48px;
  border-radius: 12px;
  box-shadow: none;
  border: 1px solid rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.register-form :deep(.el-input__wrapper:hover) {
  border-color: #FFD100;
}

.register-form :deep(.el-input__wrapper.is-focus) {
  border-color: #FFD100;
  box-shadow: 0 0 0 3px rgba(255, 209, 0, 0.1);
}

.register-btn {
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

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(255, 209, 0, 0.4);
}

.register-btn:active {
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
  .register-content {
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
