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
  router.replace({ path: '/' + (Array.isArray(path) ? path.join('/') : path) })
})
</script>
