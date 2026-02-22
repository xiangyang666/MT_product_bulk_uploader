<template>
  <div></div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

onMounted(() => {
  const { params } = route
  const { path } = params
  const targetPath = '/' + (Array.isArray(path) ? path.join('/') : path)
  
  // 使用 nextTick 确保 DOM 更新完成后再跳转
  router.replace({ path: targetPath }).catch(err => {
    // 忽略导航重复错误
    if (err.name !== 'NavigationDuplicated') {
      console.error('重定向失败:', err)
      // 如果重定向失败，回到首页
      router.replace('/')
    }
  })
})
</script>
