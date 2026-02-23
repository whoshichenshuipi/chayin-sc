<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

onMounted(() => {
  // 初始化用户状态
  userStore.initUser()
})
</script>

<style>
#app {
  min-height: 100vh;
  background-color: var(--color-bg-white);
}
</style>
