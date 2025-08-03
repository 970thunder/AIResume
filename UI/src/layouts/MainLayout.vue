<template>
    <el-container class="main-layout">
        <el-aside :width="isCollapsed ? '80px' : '280px'" class="sidebar pixel-sidebar">
            <!-- 像素风格装饰边框 -->
            <div class="pixel-border-top"></div>
            <div class="pixel-border-bottom"></div>

            <div class="sidebar-header">
                <div v-if="!isCollapsed" class="logo-container">
                    <div class="pixel-logo">
                        <div class="pixel-icon">
                            <div class="pixel-grid">
                                <span class="pixel pixel-1"></span>
                                <span class="pixel pixel-2"></span>
                                <span class="pixel pixel-3"></span>
                                <span class="pixel pixel-4"></span>
                                <span class="pixel pixel-5"></span>
                                <span class="pixel pixel-6"></span>
                                <span class="pixel pixel-7"></span>
                                <span class="pixel pixel-8"></span>
                                <span class="pixel pixel-9"></span>
                            </div>
                        </div>
                        <GradientText :colors="['#3b82f6', '#06b6d4', '#0ea5e9', '#3b82f6']" :animationSpeed="6"
                            :showBorder="false" customClass="pixel-title">
                            Hyper 简生
                        </GradientText>
                    </div>
                </div>
                <div v-else class="collapsed-logo">
                    <div class="pixel-icon-small">
                        <span class="pixel-dot"></span>
                        <span class="pixel-dot"></span>
                        <span class="pixel-dot"></span>
                        <span class="pixel-dot"></span>
                    </div>
                </div>
            </div>

            <!-- 像素风格分割线 -->
            <div class="pixel-divider"></div>

            <el-menu :default-active="$route.path" class="sidebar-menu pixel-menu" :collapse="isCollapsed" router>
                <el-menu-item index="/home" class="pixel-menu-item">
                    <div class="menu-icon-wrapper">
                        <div class="pixel-home-icon"></div>
                    </div>
                    <template #title>
                        <span class="pixel-text">首页</span>
                    </template>
                </el-menu-item>

                <el-menu-item index="/generator" class="pixel-menu-item">
                    <div class="menu-icon-wrapper">
                        <div class="pixel-doc-icon"></div>
                    </div>
                    <template #title>
                        <span class="pixel-text">简历生成</span>
                    </template>
                </el-menu-item>

                <el-menu-item index="/store" class="pixel-menu-item">
                    <div class="menu-icon-wrapper">
                        <div class="pixel-shop-icon"></div>
                    </div>
                    <template #title>
                        <span class="pixel-text">模板商城</span>
                    </template>
                </el-menu-item>
            </el-menu>

            <!-- 像素风格装饰元素 -->
            <div class="pixel-decoration">
                <div class="floating-pixels">
                    <span class="float-pixel pixel-float-1"></span>
                    <span class="float-pixel pixel-float-2"></span>
                    <span class="float-pixel pixel-float-3"></span>
                </div>
            </div>

            <div class="sidebar-bottom-content">
                <InfoButton :is-collapsed="isCollapsed"></InfoButton>
                <div v-if="!isCollapsed" class="beian-info pixel-beian">
                    <div class="pixel-link-border">
                        <a href="https://beian.miit.gov.cn/" target="_blank">桂ICP备2024034221号-2</a>
                    </div>
                </div>
            </div>

            <div class="sidebar-toggle pixel-toggle" @click="isCollapsed = !isCollapsed">
                <div class="toggle-pixel-icon">
                    <span v-if="!isCollapsed" class="pixel-arrow-left"></span>
                    <span v-else class="pixel-arrow-right"></span>
                </div>
            </div>
        </el-aside>

        <el-container>
            <el-header class="main-header pixel-header">
                <div class="header-content">
                    <div class="breadcrumb">
                        <!-- Breadcrumb can be added here if needed -->
                    </div>
                    <div class="user-info">
                        <el-dropdown @command="handleCommand">
                            <span class="el-dropdown-link pixel-dropdown">
                                <span class="pixel-user-icon"></span>
                                欢迎, {{ authStore.user?.username }}
                                <el-icon class="el-icon--right">
                                    <ArrowDown />
                                </el-icon>
                            </span>
                            <template #dropdown>
                                <el-dropdown-menu class="pixel-dropdown-menu">
                                    <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                                    <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                                </el-dropdown-menu>
                            </template>
                        </el-dropdown>
                    </div>
                </div>
            </el-header>
            <el-main class="content-area pixel-content">
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
    MagicStick,
    ArrowDown
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

    font-family: 'Courier New', monospace;
}

.pixel-sidebar {
    background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
    border-right: 2px solid #adcbfc;
    border-image:
        repeating-linear-gradient(0deg,
            #3b82f6 0px,
            #3b82f6 2px,
            transparent 2px,
            transparent 4px) 4;
    transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    display: flex;
    flex-direction: column;
    overflow: visible !important;
    position: relative;
    box-shadow:
        inset -2px 0 0 rgba(59, 130, 246, 0.2),
        4px 0 20px rgba(59, 130, 246, 0.1);
}

/* 像素风格装饰边框 */
.pixel-border-top,
.pixel-border-bottom {
    position: absolute;
    left: 0;
    right: 0;
    height: 8px;
    background: repeating-linear-gradient(90deg,
            #669efa 0px,
            #6a9ef3 8px,
            #06b6d4 8px,
            #06b6d4 16px,
            #0ea5e9 16px,
            #0ea5e9 24px,
            #dff2f9 24px,
            hsl(194, 78%, 69%) 32px);
    z-index: 10;
}

.pixel-border-top {
    top: 0;
    border-top: 2px solid #ffffff;
}

.pixel-border-bottom {
    bottom: 0;
    border-bottom: 2px solid #ffffff;
}

.sidebar-header {
    height: 80px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    background: linear-gradient(45deg, #f8fafc, #ffffff);
    border-bottom: 2px solid #3b82f6;
    margin-top: 8px;
    box-shadow: 0 2px 8px rgba(59, 130, 246, 0.1);
}

.logo-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
}

.pixel-logo {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
}

.pixel-icon {
    width: 36px;
    height: 36px;
    position: relative;
}

.pixel-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    grid-template-rows: repeat(3, 1fr);
    width: 100%;
    height: 100%;
    gap: 2px;
}

.pixel {
    width: 100%;
    height: 100%;
    border-radius: 1px;
    animation: pixelGlow 2s infinite alternate;
}

.pixel-1,
.pixel-5,
.pixel-9 {
    background: #3b82f6;
    animation-delay: 0s;
}

.pixel-2,
.pixel-4,
.pixel-6,
.pixel-8 {
    background: #06b6d4;
    animation-delay: 0.3s;
}

.pixel-3,
.pixel-7 {
    background: #0ea5e9;
    animation-delay: 0.6s;
}

.collapsed-logo {
    display: flex;
    align-items: center;
    justify-content: center;
}

.pixel-icon-small {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: repeat(2, 1fr);
    width: 24px;
    height: 24px;
    gap: 2px;
}

.pixel-dot {
    width: 100%;
    height: 100%;
    background: #3b82f6;
    border-radius: 1px;
    animation: pixelPulse 1.5s infinite;
}

.pixel-dot:nth-child(2) {
    animation-delay: 0.2s;
}

.pixel-dot:nth-child(3) {
    animation-delay: 0.4s;
}

.pixel-dot:nth-child(4) {
    animation-delay: 0.6s;
}

.pixel-title {
    font-size: 18px;
    font-weight: bold;
    text-shadow:
        1px 1px 0 rgba(255, 255, 255, 0.8),
        0 0 10px rgba(59, 130, 246, 0.3);
    letter-spacing: 1px;
}

.pixel-divider {
    height: 4px;
    background: repeating-linear-gradient(90deg,
            #3b82f6 0px,
            #3b82f6 4px,
            #e0f2fe 4px,
            #e0f2fe 8px);
    margin: 10px 20px;
    border-radius: 2px;
}

.pixel-menu {
    flex-grow: 1;
    border-right: none;
    background: transparent;
    padding: 0 10px;
}

.pixel-menu-item {
    font-weight: 600;
    margin: 8px 0;
    border-radius: 8px;
    background: linear-gradient(45deg, rgba(248, 250, 252, 0.9), rgba(241, 245, 249, 0.9));
    border: 2px solid transparent;
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;
    color: #475569;
}

.pixel-menu-item::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(59, 130, 246, 0.2), transparent);
    transition: left 0.5s ease;
}

.pixel-menu-item:hover::before {
    left: 100%;
}

.pixel-menu-item:hover {
    border-color: #3b82f6;
    box-shadow:
        0 0 20px rgba(59, 130, 246, 0.2),
        inset 0 0 20px rgba(59, 130, 246, 0.05);
    transform: translateX(4px);
    background: linear-gradient(45deg, #ffffff, #f8fafc);
    color: #1e293b;
}

.pixel-menu-item.is-active {
    background: linear-gradient(45deg, #3b82f6, #06b6d4);
    color: #ffffff;
    border-color: #0ea5e9;
    box-shadow: 0 0 25px rgba(59, 130, 246, 0.4);
}

.menu-icon-wrapper {
    width: 20px;
    height: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 8px;
}

/* 像素风格图标 */
.pixel-home-icon {
    width: 16px;
    height: 16px;
    background: currentColor;
    position: relative;
    clip-path: polygon(50% 0%, 0% 100%, 100% 100%);
}

.pixel-home-icon::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 25%;
    width: 50%;
    height: 60%;
    background: #f8fafc;
}

.pixel-doc-icon {
    width: 14px;
    height: 16px;
    background: currentColor;
    position: relative;
}

.pixel-doc-icon::before {
    content: '';
    position: absolute;
    top: 2px;
    left: 2px;
    right: 2px;
    height: 2px;
    background: #f8fafc;
    box-shadow:
        0 4px 0 #f8fafc,
        0 8px 0 #f8fafc;
}

.pixel-shop-icon {
    width: 16px;
    height: 14px;
    background: currentColor;
    position: relative;
    border-radius: 2px 2px 0 0;
}

.pixel-shop-icon::before {
    content: '';
    position: absolute;
    bottom: -4px;
    left: 2px;
    right: 2px;
    height: 4px;
    background: currentColor;
}

.pixel-text {
    font-size: 14px;
    font-weight: 600;
    text-shadow: 1px 1px 0 rgba(0, 0, 0, 0.5);
}

.pixel-decoration {
    position: absolute;
    top: 50%;
    right: 10px;
    transform: translateY(-50%);
}

.floating-pixels {
    position: relative;
    width: 20px;
    height: 60px;
}

.float-pixel {
    position: absolute;
    width: 4px;
    height: 4px;
    background: #3b82f6;
    border-radius: 1px;
    animation: float 3s infinite ease-in-out;
}

.pixel-float-1 {
    top: 0;
    left: 0;
    animation-delay: 0s;
}

.pixel-float-2 {
    top: 20px;
    left: 8px;
    animation-delay: 1s;
    background: #06b6d4;
}

.pixel-float-3 {
    top: 40px;
    left: 4px;
    animation-delay: 2s;
    background: #0ea5e9;
}

.sidebar-bottom-content {
    flex-shrink: 0;
    transition: all 0.3s ease;
    padding: 10px;
}

.pixel-beian {
    text-align: center;
    margin-top: 10px;
}

.pixel-link-border {
    padding: 8px;
    border: 2px solid #3b82f6;
    border-radius: 4px;
    background: rgba(59, 130, 246, 0.05);
    transition: all 0.3s ease;
}

.pixel-link-border:hover {
    background: rgba(59, 130, 246, 0.1);
    box-shadow: 0 0 15px rgba(59, 130, 246, 0.2);
}

.pixel-beian a {
    color: #3b82f6;
    text-decoration: none;
    font-size: 11px;
    font-weight: 600;
    text-shadow: none;
}

.pixel-toggle {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    background: linear-gradient(45deg, #3b82f6, #06b6d4);
    color: #ffffff;
    flex-shrink: 0;
    border-top: 2px solid #0ea5e9;
    transition: all 0.3s ease;
    margin-bottom: 8px;
}

.pixel-toggle:hover {
    background: linear-gradient(45deg, #06b6d4, #0ea5e9);
    box-shadow: 0 0 20px rgba(59, 130, 246, 0.4);
}

.toggle-pixel-icon {
    font-size: 18px;
    font-weight: bold;
}

.pixel-arrow-left::after {
    content: '◀';
}

.pixel-arrow-right::after {
    content: '▶';
}

.pixel-header {
    background: linear-gradient(90deg, #ffffff, #f8fafc);
    border-bottom: 3px solid #3b82f6;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    padding: 0 30px;
    box-shadow: 0 2px 10px rgba(59, 130, 246, 0.1);
}

.pixel-dropdown {
    color: #3b82f6;
    font-weight: 600;
    text-shadow: none;
    display: flex;
    align-items: center;
    gap: 8px;
}

.pixel-user-icon {
    width: 20px;
    height: 20px;
    background: #3b82f6;
    border-radius: 50%;
    position: relative;
    display: inline-block;
}

.pixel-user-icon::before {
    content: '';
    position: absolute;
    top: 4px;
    left: 50%;
    transform: translateX(-50%);
    width: 8px;
    height: 8px;
    background: #ffffff;
    border-radius: 50%;
}

.pixel-user-icon::after {
    content: '';
    position: absolute;
    bottom: 2px;
    left: 50%;
    transform: translateX(-50%);
    width: 12px;
    height: 6px;
    background: #ffffff;
    border-radius: 6px 6px 0 0;
}

.pixel-content {
    padding: 30px;
}

/* 动画效果 */
@keyframes pixelGlow {
    0% {
        box-shadow: 0 0 5px currentColor;
        opacity: 0.8;
    }

    100% {
        box-shadow: 0 0 15px currentColor, 0 0 25px currentColor;
        opacity: 1;
    }
}

@keyframes pixelPulse {

    0%,
    100% {
        opacity: 0.6;
        transform: scale(1);
    }

    50% {
        opacity: 1;
        transform: scale(1.1);
    }
}

@keyframes float {

    0%,
    100% {
        transform: translateY(0px) rotate(0deg);
        opacity: 0.7;
    }

    50% {
        transform: translateY(-10px) rotate(180deg);
        opacity: 1;
    }
}

/* 响应式适配 */
@media (max-width: 768px) {
    .pixel-sidebar {
        width: 64px !important;
    }

    .sidebar-header {
        height: 60px;
    }

    .pixel-title {
        font-size: 14px;
    }
}

/* Element Plus 组件样式覆盖 */
.pixel-menu .el-menu-item {
    color: #475569 !important;
    background: transparent !important;
}

.pixel-menu .el-menu-item.is-active {
    color: #ffffff !important;
    background: linear-gradient(45deg, #3b82f6, #06b6d4) !important;
}

.pixel-menu .el-menu-item:hover {
    color: #1e293b !important;
}
</style>