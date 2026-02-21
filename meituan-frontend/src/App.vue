<template>
  <div id="app" class="iphone-app">
    <router-view />
  </div>
</template>

<script setup>
// iPhone风格的主应用组件
</script>

<style scoped>
.iphone-app {
  width: 100%;
  height: 100vh;
  background-color: var(--iphone-bg-secondary);
}
</style>
