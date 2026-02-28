<template>
  <router-view v-slot="{ Component, route }">
    <transition :name="route.meta.transition || 'page-fade'" mode="out-in">
      <component :is="Component" :key="route.path" />
    </transition>
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

/* Page Transition Animations - Enhanced */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.98);
  filter: blur(2px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-20px) scale(0.98);
  filter: blur(2px);
}

/* Slide transition */
.page-slide-enter-active,
.page-slide-leave-active {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-slide-enter-from {
  opacity: 0;
  transform: translateX(100px);
}

.page-slide-leave-to {
  opacity: 0;
  transform: translateX(-100px);
}

/* Zoom transition */
.page-zoom-enter-active,
.page-zoom-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-zoom-enter-from {
  opacity: 0;
  transform: scale(0.9);
}

.page-zoom-leave-to {
  opacity: 0;
  transform: scale(1.1);
}
</style>
