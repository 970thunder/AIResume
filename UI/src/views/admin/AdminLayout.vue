<template>
  <div class="admin-layout" ref="adminLayoutRef">
    <!-- Particle Background -->
    <div class="particles-bg">
      <div v-for="n in 30" :key="n" class="particle" :style="getParticleStyle(n)"></div>
    </div>

    <el-container class="h-screen">
      <el-aside :width="isCollapsed ? '64px' : '240px'" class="admin-sidebar">
        <div class="sidebar-header">
          <div v-if="!isCollapsed" class="logo-container">
            <div class="logo-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="url(#gradient1)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M2 17L12 22L22 17" stroke="url(#gradient1)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M2 12L12 17L22 12" stroke="url(#gradient1)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <defs>
                  <linearGradient id="gradient1" x1="2" y1="2" x2="22" y2="22">
                    <stop stop-color="#0ea5e9"/>
                    <stop offset="1" stop-color="#8b5cf6"/>
                  </linearGradient>
                </defs>
              </svg>
            </div>
            <span class="logo-text">Hyper Admin</span>
          </div>
          <div v-else class="collapsed-logo">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="mini-icon">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="#0ea5e9" stroke-width="2"/>
              <path d="M2 17L12 22L22 17" stroke="#8b5cf6" stroke-width="2"/>
              <path d="M2 12L12 17L22 12" stroke="#22d3ee" stroke-width="2"/>
            </svg>
          </div>
        </div>

        <el-menu :default-active="activeMenu" class="admin-menu" :collapse="isCollapsed" router>
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataLine /></el-icon>
            <template #title>数据大屏</template>
          </el-menu-item>
          <el-menu-item index="/admin/templates">
            <el-icon><Files /></el-icon>
            <template #title>模板审核</template>
          </el-menu-item>
          <el-menu-item index="/admin/questions">
            <el-icon><List /></el-icon>
            <template #title>题库管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
        </el-menu>

        <div class="sidebar-footer" :class="{ 'is-collapsed': isCollapsed }">
          <div class="sidebar-toggle" @click="isCollapsed = !isCollapsed">
            <el-icon v-if="!isCollapsed"><Fold /></el-icon>
            <el-icon v-else><Expand /></el-icon>
          </div>
        </div>
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
              <div class="user-dropdown">
                <el-avatar :size="32" class="user-avatar">
                  {{ user?.username?.charAt(0)?.toUpperCase() || 'A' }}
                </el-avatar>
                <span class="username">{{ user?.username }}</span>
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="home">
                    <el-icon><HomeFilled /></el-icon>
                    返回前台
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <el-main class="admin-main">
          <router-view v-slot="{ Component }">
            <component :is="Component" />
          </router-view>
        </el-main>
      </el-container>
    </el-container>

    <!-- Glow Effect -->
    <div class="glow-effect" :style="glowStyle"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { DataLine, Files, List, ArrowDown, User, Fold, Expand, HomeFilled, SwitchButton } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const adminLayoutRef = ref(null);
const isCollapsed = ref(false);

const user = computed(() => authStore.user);
const activeMenu = computed(() => route.path);
const currentRouteName = computed(() => {
  switch (route.path) {
    case '/admin/dashboard': return '数据大屏';
    case '/admin/templates': return '模板审核';
    case '/admin/questions': return '题库管理';
    case '/admin/users': return '用户管理';
    default: return '管理后台';
  }
});

// Particle background
const getParticleStyle = (n) => {
  const size = Math.random() * 4 + 2;
  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${Math.random() * 100}%`,
    top: `${Math.random() * 100}%`,
    animationDelay: `${Math.random() * 20}s`,
    animationDuration: `${Math.random() * 20 + 20}s`,
    opacity: Math.random() * 0.5 + 0.2
  };
};

// Mouse glow effect
const glowStyle = ref({ left: '-100px', top: '-100px', opacity: 0 });

const handleMouseMove = (e) => {
  if (adminLayoutRef.value) {
    const rect = adminLayoutRef.value.getBoundingClientRect();
    glowStyle.value = {
      left: `${e.clientX - rect.left - 150}px`,
      top: `${e.clientY - rect.top - 150}px`,
      opacity: 0.15
    };
  }
};

const handleMouseLeave = () => {
  glowStyle.value.opacity = 0;
};

const handleCommand = (command) => {
  if (command === 'logout') {
    authStore.logout();
    router.push('/admin/login');
  } else if (command === 'home') {
    router.push('/');
  }
};

onMounted(() => {
  if (adminLayoutRef.value) {
    adminLayoutRef.value.addEventListener('mousemove', handleMouseMove);
    adminLayoutRef.value.addEventListener('mouseleave', handleMouseLeave);
  }
});

onUnmounted(() => {
  if (adminLayoutRef.value) {
    adminLayoutRef.value.removeEventListener('mousemove', handleMouseMove);
    adminLayoutRef.value.removeEventListener('mouseleave', handleMouseLeave);
  }
});
</script>

<style scoped>
.admin-layout {
  background: radial-gradient(1200px 600px at 20% -20%, #0b1220 40%, #0f172a 100%);
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

.h-screen {
  height: 100vh;
  position: relative;
  z-index: 1;
}

/* Particle Background */
.particles-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.particle {
  position: absolute;
  background: radial-gradient(circle, rgba(14, 165, 233, 0.8) 0%, transparent 70%);
  border-radius: 50%;
  animation: float linear infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) translateX(0);
  }
  25% {
    transform: translateY(-30px) translateX(15px);
  }
  50% {
    transform: translateY(-15px) translateX(-10px);
  }
  75% {
    transform: translateY(-40px) translateX(20px);
  }
}

/* Glow Effect */
.glow-effect {
  position: fixed;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(14, 165, 233, 0.3) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;
  transition: opacity 0.3s ease;
}

/* Sidebar */
.admin-sidebar {
  background: rgba(15, 23, 42, 0.8);
  border-right: 1px solid rgba(148, 163, 184, 0.1);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  z-index: 10;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.3);
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(148, 163, 184, 0.1);
  padding: 0 16px;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 32px;
  height: 32px;
}

.logo-icon svg {
  width: 100%;
  height: 100%;
}

.logo-text {
  background: linear-gradient(135deg, #0ea5e9 0%, #22d3ee 50%, #8b5cf6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.collapsed-logo {
  display: flex;
  align-items: center;
  justify-content: center;
}

.mini-icon {
  width: 28px;
  height: 28px;
}

/* Menu */
.admin-menu {
  border-right: none;
  flex: 1;
  background: transparent !important;
  padding: 12px 8px;
}

:deep(.el-menu-item) {
  border-radius: 10px;
  margin-bottom: 4px;
  height: 44px;
  line-height: 44px;
  color: #94a3b8 !important;
  transition: all 0.3s ease;
  background: transparent;
}

:deep(.el-menu-item:hover) {
  background: rgba(14, 165, 233, 0.1);
  color: #e2e8f0 !important;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.2), rgba(139, 92, 246, 0.15));
  color: #ffffff !important;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(14, 165, 233, 0.2);
  border: 1px solid rgba(14, 165, 233, 0.3);
}

:deep(.el-menu-item .el-icon) {
  color: #64748b;
  font-size: 18px;
  transition: color 0.3s ease;
}

:deep(.el-menu-item:hover .el-icon),
:deep(.el-menu-item.is-active .el-icon) {
  color: #22d3ee;
}

/* Sidebar Footer */
.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(148, 163, 184, 0.1);
}

.sidebar-footer.is-collapsed {
  padding: 12px 8px;
}

.sidebar-toggle {
  width: 100%;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #64748b;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.sidebar-toggle:hover {
  background: rgba(14, 165, 233, 0.1);
  color: #22d3ee;
}

/* Header */
.admin-header {
  background: rgba(15, 23, 42, 0.6);
  border-bottom: 1px solid rgba(148, 163, 184, 0.1);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.header-left :deep(.el-breadcrumb__item) {
  color: #64748b;
}

.header-left :deep(.el-breadcrumb__inner) {
  color: #94a3b8;
  font-weight: 500;
  transition: color 0.2s;
}

.header-left :deep(.el-breadcrumb__inner:hover) {
  color: #22d3ee;
}

.header-left :deep(.el-breadcrumb__separator) {
  color: #475569;
}

/* User Dropdown */
.user-dropdown {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 20px;
  transition: all 0.3s ease;
}

.user-dropdown:hover {
  background: rgba(14, 165, 233, 0.1);
}

.user-avatar {
  background: linear-gradient(135deg, #0ea5e9, #8b5cf6);
  color: white;
  font-weight: 600;
  font-size: 14px;
}

.username {
  color: #e2e8f0;
  font-weight: 500;
  font-size: 14px;
}

.dropdown-icon {
  color: #64748b;
  transition: transform 0.3s ease;
}

.user-dropdown:hover .dropdown-icon {
  color: #22d3ee;
}

/* Dropdown Menu */
:deep(.el-dropdown-menu) {
  background: rgba(15, 23, 42, 0.95);
  border: 1px solid rgba(148, 163, 184, 0.1);
  backdrop-filter: blur(12px);
  border-radius: 12px;
  padding: 8px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.4);
}

:deep(.el-dropdown-menu__item) {
  color: #94a3b8;
  border-radius: 8px;
  padding: 10px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

:deep(.el-dropdown-menu__item:hover) {
  background: rgba(14, 165, 233, 0.15);
  color: #e2e8f0;
}

:deep(.el-dropdown-menu__item .el-icon) {
  color: #64748b;
}

:deep(.el-dropdown-menu__item:hover .el-icon) {
  color: #22d3ee;
}

/* Main Content */
.admin-main {
  background: transparent;
  padding: 24px;
  overflow-y: auto;
}

/* Responsive */
@media (max-width: 768px) {
  .admin-header {
    padding: 0 16px;
  }

  .admin-main {
    padding: 16px;
  }

  .username {
    display: none;
  }
}
</style>
