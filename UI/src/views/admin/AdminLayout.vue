<template>
  <div class="admin-layout">
    <el-container class="h-screen">
      <el-aside width="240px" class="admin-sidebar">
        <div class="logo">
          <h2>Hyper Admin</h2>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical"
          router
          background-color="#001529"
          text-color="#a6adb4"
          active-text-color="#1890ff"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataLine /></el-icon>
            <span>数据大屏</span>
          </el-menu-item>
          <el-menu-item index="/admin/templates">
            <el-icon><Files /></el-icon>
            <span>模板审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/questions">
            <el-icon><List /></el-icon>
            <span>题库管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-container>
        <el-header class="admin-header">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentRouteName }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="el-dropdown-link">
                {{ user?.username }}
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="home">返回前台</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <el-main class="admin-main">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { DataLine, Files, List, ArrowDown } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const user = computed(() => authStore.user);
const activeMenu = computed(() => route.path);
const currentRouteName = computed(() => {
  switch (route.path) {
    case '/admin/dashboard': return '数据大屏';
    case '/admin/templates': return '模板审核';
    case '/admin/questions': return '题库管理';
    default: return '管理后台';
  }
});

const handleCommand = (command) => {
  if (command === 'logout') {
    authStore.logout();
    router.push('/admin/login');
  } else if (command === 'home') {
    router.push('/');
  }
};
</script>

<style scoped>
.h-screen {
  height: 100vh;
}

.admin-sidebar {
  background-color: #001529;
  color: white;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #002140;
}

.logo h2 {
  color: white;
  margin: 0;
  font-size: 18px;
}

.el-menu-vertical {
  border-right: none;
  flex: 1;
}

.admin-header {
  background-color: white;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #606266;
}

.admin-main {
  background-color: #f0f2f5;
  padding: 24px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
