<template>
    <el-container class="main-layout" ref="mainLayoutRef">
        <!-- Particle Background -->
        <ParticleBackground />

        <!-- Comet Trail Effect -->
        <div class="comet-container">
            <div v-for="(particle, index) in cometParticles" :key="index" class="comet-particle"
                :style="{
                    left: particle.x + 'px',
                    top: particle.y + 'px',
                    opacity: particle.opacity,
                    transform: `scale(${particle.scale})`,
                    background: particle.color
                }">
            </div>
        </div>

        <el-aside :width="isCollapsed ? '64px' : '260px'" class="sidebar">
            <div class="sidebar-header">
                <div v-if="!isCollapsed" class="logo-container">
                    <div class="logo-wrapper">
                        <GradientText :colors="['#0ea5e9', '#22d3ee', '#38bdf8', '#0ea5e9']" :animationSpeed="6"
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
                <!-- <div v-if="!isCollapsed" class="beian-info">
                    <a href="https://beian.miit.gov.cn/" target="_blank">桂ICP备2024034221号-2</a>
                </div> -->
                <div class="sidebar-toggle" @click="isCollapsed = !isCollapsed">
                    <el-icon v-if="!isCollapsed" class="toggle-icon">
                        <Fold />
                    </el-icon>
                    <el-icon v-else class="toggle-icon">
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
                            <el-avatar :size="32" class="user-avatar" :style="{ backgroundColor: '#0ea5e9' }">
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

        <!-- Wave Background -->
        <div class="wave-container">
            <div class="wave wave1"></div>
            <div class="wave wave2"></div>
            <div class="wave wave3"></div>
        </div>
    </el-container>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
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
const mainLayoutRef = ref(null);

// Comet trail effect
const cometParticles = ref([]);
const maxParticles = 20;
let particleId = 0;
const cometColors = [
    'radial-gradient(circle, #38bdf8 0%, #0ea5e9 50%, transparent 100%)',
    'radial-gradient(circle, #a5f3fc 0%, #67e8f9 50%, transparent 100%)',
    'radial-gradient(circle, #22d3ee 0%, #06b6d4 50%, transparent 100%)',
    'radial-gradient(circle, #818cf8 0%, #6366f1 50%, transparent 100%)'
];

const addCometParticle = (x, y) => {
    const colorIndex = Math.floor(Math.random() * cometColors.length);
    cometParticles.value.push({
        id: particleId++,
        x,
        y,
        opacity: 1,
        scale: 1 + Math.random() * 0.5,
        color: cometColors[colorIndex]
    });

    if (cometParticles.value.length > maxParticles) {
        cometParticles.value.shift();
    }
};

const updateCometParticles = () => {
    cometParticles.value = cometParticles.value.map(p => ({
        ...p,
        opacity: p.opacity - 0.05,
        scale: p.scale * 0.95
    })).filter(p => p.opacity > 0);
};

let animationFrameId = null;

const animateComet = () => {
    updateCometParticles();
    animationFrameId = requestAnimationFrame(animateComet);
};

// Mouse move handler
let lastCometTime = 0;
const handleMouseMove = (e) => {
    const now = Date.now();
    if (now - lastCometTime > 30) {
        addCometParticle(e.clientX, e.clientY);
        lastCometTime = now;
    }
};

const handleCommand = (command) => {
    if (command === 'logout') {
        authStore.logout();
        router.push('/login');
    } else if (command === 'profile') {
        router.push('/profile');
    }
};

onMounted(() => {
    animateComet();
    document.addEventListener('mousemove', handleMouseMove);
});

onUnmounted(() => {
    if (animationFrameId) {
        cancelAnimationFrame(animationFrameId);
    }
    document.removeEventListener('mousemove', handleMouseMove);
});
</script>

<style scoped>
.main-layout {
    height: 100vh;
    font-family: 'Plus Jakarta Sans', 'Manrope', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
    background: radial-gradient(1200px 600px at 20% -20%, #0b1220 40%, #0f172a 100%);
    position: relative;
}

/* Comet Trail Effect */
.comet-container {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    z-index: 9999;
    overflow: hidden;
}

.comet-particle {
    position: absolute;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    pointer-events: none;
    transform: translate(-50%, -50%);
    transition: opacity 0.1s ease-out, transform 0.1s ease-out;
    box-shadow:
        0 0 10px rgba(56, 189, 248, 0.5),
        0 0 20px rgba(56, 189, 248, 0.3),
        0 0 30px rgba(56, 189, 248, 0.2);
    filter: blur(1px);
}

.comet-particle::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 6px;
    height: 6px;
    background: white;
    border-radius: 50%;
    transform: translate(-50%, -50%);
    box-shadow: 0 0 8px white;
}

.sidebar {
    background: var(--card);
    border-right: 1px solid var(--border);
    display: flex;
    flex-direction: column;
    transition: width 0.3s ease;
    z-index: 10;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.35);
    overflow-x: hidden;
    backdrop-filter: blur(14px);
}

.sidebar-header {
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-bottom: 1px solid var(--border);
}

.logo-container {
    display: flex;
    align-items: center;
    gap: 12px;
}

.app-title {
    font-size: 20px;
    font-weight: 800;
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

:deep(.el-menu) {
    background: transparent;
    border-right: none;
}

:deep(.el-menu-item) {
    border-radius: 12px;
    margin-bottom: 6px;
    height: 48px;
    line-height: 48px;
    color: #e2e8f0 !important;
    /* Force light color for visibility */
    transition: all 0.3s ease;
    background: transparent;
}

:deep(.el-menu-item.is-active) {
    background: linear-gradient(135deg, rgba(14, 165, 233, 0.2), rgba(34, 211, 238, 0.15));
    color: #ffffff !important;
    font-weight: 700;
    box-shadow: 0 4px 12px rgba(14, 165, 233, 0.2);
    border: 1px solid rgba(14, 165, 233, 0.3);
}

:deep(.el-menu-item:hover) {
    background: rgba(255, 255, 255, 0.1);
    color: #ffffff !important;
}

:deep(.el-menu-item .el-icon) {
    color: #cbd5e1 !important;
    /* Light grey icon */
}

:deep(.el-menu-item.is-active .el-icon) {
    color: #38bdf8 !important;
    /* Active icon blue */
}

:deep(.el-icon) {
    font-size: 18px;
}

.sidebar-footer {
    padding: 16px;
    border-top: 1px solid var(--border);
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
    color: var(--muted);
    text-decoration: none;
    font-size: 12px;
    transition: color 0.2s;
}

.beian-info a:hover {
    color: var(--fg);
}

.sidebar-toggle {
    width: 100%;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: var(--muted);
    border-radius: 8px;
    transition: all 0.2s;
}

.sidebar-toggle:hover {
    background-color: rgba(243, 232, 232, 0.2);
    color: var(--fg);
}

.toggle-icon{
    color: #b0b1b1;
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
    background: var(--card);
    border-bottom: 1px solid var(--border);
    padding: 0 24px;
    display: flex !important;
    justify-content: flex-end !important;
    align-items: center;
    box-sizing: border-box;
    flex-shrink: 0;
    flex-direction: row !important;
    backdrop-filter: blur(12px);
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.35);
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
    color: var(--fg);
    font-weight: 600;
}

.user-avatar {
    font-weight: 700;
    color: #0b1220;
}

.username {
    font-size: 14px;
}

.content-area {
    padding: 0;
    overflow-y: auto;
    height: 100%;
    background: transparent;
    position: relative;
    z-index: 1;
}

/* Wave Background */
.wave-container {
    position: fixed;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 200px;
    overflow: hidden;
    z-index: 2;
    pointer-events: none;
}

.wave {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 200%;
    height: 100%;
    background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='%2338bdf8' fill-opacity='0.1' d='M0,192L48,197.3C96,203,192,213,288,229.3C384,245,480,267,576,250.7C672,235,768,181,864,181.3C960,181,1056,235,1152,234.7C1248,235,1344,181,1392,154.7L1440,128L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E") repeat-x;
    background-size: 50% 100%;
    animation: wave 15s linear infinite;
}

.wave1 {
    animation-delay: 0s;
    opacity: 0.5;
    bottom: 0;
}

.wave2 {
    animation-delay: -5s;
    opacity: 0.3;
    bottom: 10px;
}

.wave3 {
    animation-delay: -10s;
    opacity: 0.2;
    bottom: 20px;
}

@keyframes wave {
    0% { transform: translateX(0); }
    100% { transform: translateX(-50%); }
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
