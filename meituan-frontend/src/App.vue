<template>
  <div id="app" class="iphone-app">
    <router-view />
    
    <!-- 开发者工具密码输入对话框 -->
    <el-dialog
      v-model="showPasswordDialog"
      title="开发者工具"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <el-form @submit.prevent="verifyPassword">
        <el-form-item label="请输入密码">
          <el-input
            v-model="devToolsPassword"
            type="password"
            placeholder="请输入密码"
            show-password
            @keyup.enter="verifyPassword"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="verifyPassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { initDevToolsProtection } from '@/utils/devToolsProtection'

const showPasswordDialog = ref(false)
const devToolsPassword = ref('')

// 监听开发者工具密码请求
onMounted(() => {
  if (window.electronAPI) {
    // Electron 环境：监听主进程请求密码
    window.electronAPI.onRequestDevToolsPassword(() => {
      showPasswordDialog.value = true
      devToolsPassword.value = ''
    })
  } else {
    // Web 环境：初始化开发者工具保护
    initDevToolsProtection()
  }
})

// 验证密码
const verifyPassword = async () => {
  if (!devToolsPassword.value) {
    ElMessage.warning('请输入密码')
    return
  }
  
  if (window.electronAPI) {
    // Electron 环境：调用后端 API 验证密码
    try {
      const { verifyDevToolsPassword } = await import('@/api/devTools')
      const res = await verifyDevToolsPassword({ password: devToolsPassword.value })
      
      if (res.data?.valid) {
        ElMessage.success('密码正确，开发者工具已打开')
        showPasswordDialog.value = false
        devToolsPassword.value = ''
        
        // 通知 Electron 主进程打开开发者工具
        window.electronAPI.openDevToolsVerified()
      } else {
        ElMessage.error('密码错误')
        devToolsPassword.value = ''
      }
    } catch (error) {
      console.error('验证密码失败:', error)
      ElMessage.error('验证失败，请重试')
      devToolsPassword.value = ''
    }
  }
}
</script>

<style scoped>
.iphone-app {
  width: 100%;
  height: 100vh;
  background-color: var(--iphone-bg-secondary);
}
</style>

<style>
/* 全局样式：禁止文本选择和复制 */
* {
  -webkit-user-select: none !important;
  -moz-user-select: none !important;
  -ms-user-select: none !important;
  user-select: none !important;
}

/* 输入框允许选择 */
input,
textarea,
.el-input__inner,
.el-textarea__inner {
  -webkit-user-select: text !important;
  -moz-user-select: text !important;
  -ms-user-select: text !important;
  user-select: text !important;
}
</style>
