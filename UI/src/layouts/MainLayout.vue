<template>
    <el-container class="main-layout">
        <el-aside :width="isCollapsed ? '64px' : '260px'" class="sidebar">
            <div class="sidebar-header">
                <div v-if="!isCollapsed" class="logo-container">
                    <div class="logo-wrapper">
                        <GradientText :colors="['#3b82f6', '#06b6d4', '#0ea5e9', '#3b82f6']" :animationSpeed="6"
                            :showBorder="false" customClass="app-title">
                            Hyper 简生
                        </GradientText>
                    </div>
                </div>
                <div v-else class="collapsed-logo">
                    <img src="/favicon.png" alt="Logo" class="mini-logo" />
                </div>
            </div>

            <el-menu :default-active="$route.path" class="sidebar-menu" :collapse="isCollapsed" router>
                <el-menu-item index="/home">
                    <el-icon>
                        <HomeFilled />
                    </el-icon>
                    <template #title>
                        <span>首页</span>
                    </template>
                </el-menu-item>

                <el-menu-item index="/generator">
                    <el-icon>
                        <Document />
                    </el-icon>
                    <template #title>
                        <span>简历生成</span>
                    </template>
                </el-menu-item>

                <el-menu-item index="/analysis">
                    <el-icon>
                        <DataAnalysis />
                    </el-icon>
                    <template #title>
                        <span>简历分析</span>
                    </template>
                </el-menu-item>

                <el-menu-item index="/interview">
                    <el-icon>
                        <Reading />
                    </el-icon>
                    <template #title>
                        <span>面经八股</span>
                    </template>
                </el-menu-item>

                <el-menu-item index="/store">
                    <el-icon>
                        <ShoppingCart />
                    </el-icon>
                    <template #title>
                        <span>模板商城</span>
                    </template>
                </el-menu-item>
            </el-menu>

            <div class="sidebar-footer" :class="{ 'is-collapsed': isCollapsed }">
                <div class="footer-actions">
                    <InfoButton :is-collapsed="isCollapsed"></InfoButton>
                </div>
                <div v-if="!isCollapsed" class="beian-info">
                    <a href="https://beian.miit.gov.cn/" target="_blank">桂ICP备2024034221号-2</a>
                </div>
                <div class="sidebar-toggle" @click="isCollapsed = !isCollapsed">
                    <el-icon v-if="!isCollapsed">
                        <Fold />
                    </el-icon>
                    <el-icon v-else>
                        <Expand />
                    </el-icon>
                </div>
            </div>
        </el-aside>

        <el-container class="content-container" direction="vertical">
            <el-header class="main-header">
                <div class="user-info">
                    <el-dropdown @command="handleCommand">
                        <span class="el-dropdown-link">
                            <el-avatar :size="32" class="user-avatar" :style="{ backgroundColor: '#3b82f6' }">
                                {{ authStore.user?.username?.charAt(0)?.toUpperCase() || 'U' }}
                            </el-avatar>
                            <span class="username">欢迎, {{ authStore.user?.username }}</span>
                            <el-icon class="el-icon--right">
                                <ArrowDown />
                            </el-icon>
                        </span>
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                </div>
            </el-header>
            <el-main class="content-area">
                <router-view></router-view>
            </el-main>
        </el-container>
    </el-container>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import GradientText from '@/components/GradientText.vue';
import InfoButton from '@/components/InfoButton.vue';
import {
    HomeFilled,
    Document,
    ShoppingCart,
    Fold,
    Expand,
    ArrowDown,
    DataAnalysis,
    Reading
} from '@element-plus/icons-vue';

const isCollapsed = ref(false);
const authStore = useAuthStore();
const router = useRouter();

const handleCommand = (command) => {
    if (command === 'logout') {
        authStore.logout();
        router.push('/login');
    } else if (command === 'profile') {
        router.push('/profile');
    }
};
</script>

<style scoped>
.main-layout {
    height: 100vh;
    font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
    background-color: #f3f4f6;
}

.sidebar {
    background-color: #ffffff;
    border-right: 1px solid #e5e7eb;
    display: flex;
    flex-direction: column;
    transition: width 0.3s ease;
    z-index: 10;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.02);
    overflow-x: hidden;
}

.sidebar-header {
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-bottom: 1px solid #f3f4f6;
}

.logo-container {
    display: flex;
    align-items: center;
    gap: 12px;
}

.app-title {
    font-size: 20px;
    font-weight: 700;
    letter-spacing: -0.5px;
}

.collapsed-logo {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
}

.mini-logo {
    width: 32px;
    height: 32px;
}

.sidebar-menu {
    border-right: none;
    flex-grow: 1;
    padding: 16px 8px;
    background-color: transparent;
}

:deep(.el-menu-item) {
    border-radius: 8px;
    margin-bottom: 4px;
    height: 48px;
    line-height: 48px;
    color: #4b5563;
}

:deep(.el-menu-item.is-active) {
    background-color: #eff6ff;
    color: #3b82f6;
    font-weight: 600;
}

:deep(.el-menu-item:hover) {
    background-color: #f9fafb;
    color: #111827;
}

:deep(.el-menu-item.is-active:hover) {
    background-color: #eff6ff;
}

:deep(.el-icon) {
    font-size: 18px;
}

.sidebar-footer {
    padding: 16px;
    border-top: 1px solid #f3f4f6;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
}

.sidebar-footer.is-collapsed {
    padding: 12px 8px;
    gap: 8px;
}

.footer-actions {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
}

.beian-info {
    text-align: center;
}

.beian-info a {
    color: #9ca3af;
    text-decoration: none;
    font-size: 12px;
    transition: color 0.2s;
}

.beian-info a:hover {
    color: #6b7280;
}

.sidebar-toggle {
    width: 100%;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #6b7280;
    border-radius: 6px;
    transition: all 0.2s;
}

.sidebar-toggle:hover {
    background-color: #f3f4f6;
    color: #374151;
}

.content-container {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-width: 0;
    height: 100vh;
    overflow: hidden;
}

.main-header {
    height: 64px !important;
    width: 100% !important;
    background-color: #ffffff;
    border-bottom: 1px solid #e5e7eb;
    padding: 0 24px;
    display: flex !important;
    justify-content: flex-end !important;
    align-items: center;
    box-sizing: border-box;
    flex-shrink: 0;
    flex-direction: row !important;
}

.user-info {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    margin-left: auto !important;
}

.el-dropdown-link {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    color: #374151;
    font-weight: 500;
}

.user-avatar {
    font-weight: 600;
    color: #ffffff;
}

.username {
    font-size: 14px;
}

.content-area {
    padding: 0;
    overflow-y: auto;
    height: 100%;
    background: radial-gradient(circle at 50% 0%, #e0f2fe 0%, #f1f5f9 100%);
}

@media (max-width: 768px) {
    .main-header {
        padding: 0 16px;
    }

    .content-area {
        padding: 16px;
    }
}
</style>

<style>
.el-header.main-header {
    display: flex !important;
    justify-content: flex-end !important;
    align-items: center !important;
    flex-direction: row !important;
}
</style>