<template>
    <el-container class="main-layout">
        <el-aside :width="isCollapsed ? '64px' : '200px'" class="sidebar">
            <div class="sidebar-header">
                <h2 v-if="!isCollapsed" class="sidebar-title">AI Resume</h2>
                <el-icon v-else :size="28" color="#337ecc"><magic-stick /></el-icon>
            </div>
            <el-menu :default-active="$route.path" class="sidebar-menu" :collapse="isCollapsed" router>
                <el-menu-item index="/home">
                    <el-icon>
                        <house />
                    </el-icon>
                    <template #title>首页</template>
                </el-menu-item>
                <el-menu-item index="/generator">
                    <el-icon>
                        <document />
                    </el-icon>
                    <template #title>AI 简历</template>
                </el-menu-item>
                <el-menu-item index="/store">
                    <el-icon><shopping-cart /></el-icon>
                    <template #title>模板商城</template>
                </el-menu-item>
            </el-menu>
            <div class="sidebar-toggle" @click="isCollapsed = !isCollapsed">
                <el-icon>
                    <Fold v-if="!isCollapsed" />
                    <Expand v-else />
                </el-icon>
            </div>
        </el-aside>

        <el-container>
            <el-header class="main-header">
                <div class="header-content">
                    <div class="breadcrumb">
                        <!-- Optional: Breadcrumb can be added here -->
                    </div>
                    <div class="user-info">
                        <template v-if="auth.isAuthenticated">
                            <el-dropdown>
                                <span class="el-dropdown-link">
                                    欢迎, {{ auth.user?.username }}
                                    <el-icon class="el-icon--right"><arrow-down /></el-icon>
                                </span>
                                <template #dropdown>
                                    <el-dropdown-menu>
                                        <el-dropdown-item @click="auth.logout">退出登录</el-dropdown-item>
                                    </el-dropdown-menu>
                                </template>
                            </el-dropdown>
                        </template>
                        <template v-else>
                            <router-link to="/login">登录</router-link>
                            <el-divider direction="vertical"></el-divider>
                            <router-link to="/register">注册</router-link>
                        </template>
                    </div>
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
import { useAuthStore } from '@/stores/auth';
import { House, Document, ShoppingCart, Fold, Expand, MagicStick, ArrowDown } from '@element-plus/icons-vue';

const isCollapsed = ref(false);
const auth = useAuthStore();
</script>

<style scoped>
.main-layout {
    height: 100vh;
}

.sidebar {
    background-color: #f4f6f8;
    border-right: 1px solid #e0e0e0;
    transition: width 0.3s ease;
    display: flex;
    flex-direction: column;
}

.sidebar-header {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.5em;
    font-weight: 600;
    color: #337ecc;
    flex-shrink: 0;
    white-space: nowrap;
    overflow: hidden;
}

.sidebar-title {
    margin-top: 6vh;
}

.sidebar-menu {
    margin-top: 5vh;
    flex-grow: 1;
    border-right: none;
    background: transparent;
}

.sidebar-menu:not(.el-menu--collapse) {
    width: 100%;
}

.el-menu-item {
    font-size: 1em;
    transition: all 0.2s ease-in-out;
}

.el-menu-item:hover:not(.is-active) {
    background-color: #f0f2f5;
}

.el-menu-item.is-active {
    background-color: #e6f7ff;
    color: #337ecc;
    border-right: 3px solid #337ecc;
    transform: translateX(5px) scale(1.02);
    box-shadow: 0 4px 12px rgba(51, 126, 204, 0.2);
}

.sidebar-toggle {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    background-color: #f0f2f5;
    color: #606266;
    flex-shrink: 0;
}

.sidebar-toggle:hover {
    background-color: #e9ebee;
}

.sidebar-toggle .el-icon {
    font-size: 20px;
}

.main-header {
    border-bottom: 1px solid #e0e0e0;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.header-content {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.user-info {
    display: flex;
    align-items: center;
}

.user-info .el-dropdown-link {
    cursor: pointer;
    display: flex;
    align-items: center;
}

.content-area {
    padding: 20px;
    background-color: #fff;
    overflow-y: auto;
}
</style>