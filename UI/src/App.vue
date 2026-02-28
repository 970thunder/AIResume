<template>
  <router-view v-slot="{ Component, route }">
    <component :is="Component" :key="route.path" />
  </router-view>
</template>

<script setup>
import { onMounted } from 'vue';
import { useAuthStore } from './stores/auth';

// Try to initialize auth state from localStorage on app startup
onMounted(() => {
  const authStore = useAuthStore();
  authStore.initializeAuth();
});
</script>

<style>
/* Reset some default styles */
body,
#app {
  margin: 0;
  padding: 0;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}
</style>
