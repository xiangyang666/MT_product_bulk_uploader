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
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const showPasswordDialog = ref(false)
const devToolsPassword = ref('')

// 监听开发者工具密码请求
onMounted(() => {
  if (window.electronAPI) {
    // 监听主进程请求密码
    window.electronAPI.onRequestDevToolsPassword(() => {
      showPasswordDialog.value = true
      devToolsPassword.value = ''
    })
    
    // 监听密码验证结果
    window.electronAPI.onDevToolsPasswordResult((result) => {
      if (result.success) {
        ElMessage.success('密码正确，开发者工具已打开')
        showPasswordDialog.value = false
        devToolsPassword.value = ''
      } else {
        ElMessage.error(result.message || '密码错误')
        devToolsPassword.value = ''
      }
    })
  }
})

// 验证密码
const verifyPassword = () => {
  if (!devToolsPassword.value) {
    ElMessage.warning('请输入密码')
    return
  }
  
  if (window.electronAPI) {
    window.electronAPI.verifyDevToolsPassword(devToolsPassword.value)
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
