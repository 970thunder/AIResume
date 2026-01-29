<template>
    <div class="home-page">
        <div class="content-container">
            <div class="hero-section">
                <h1 class="main-title">欢迎使用 AI 智能简历生成器</h1>
                <p class="subtitle">一站式解决您的简历烦恼，智能、高效、专业</p>
                <div class="action-buttons">
                    <router-link to="/generator" class="btn primary-btn">
                        开始制作简历 →
                    </router-link>
                    <router-link to="/store" class="btn secondary-btn">
                        浏览模板库
                    </router-link>
                </div>
            </div>

            <div class="features-grid">
                <div class="feature-card">
                    <div class="feature-icon-wrapper">
                        <el-icon class="feature-icon">
                            <MagicStick />
                        </el-icon>
                    </div>
                    <h3>AI 智能生成</h3>
                    <p>基于您的信息智能生成专业简历内容</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon-wrapper">
                        <el-icon class="feature-icon">
                            <Files />
                        </el-icon>
                    </div>
                    <h3>多种模板</h3>
                    <p>精选多种专业简历模板，满足不同需求</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon-wrapper">
                        <el-icon class="feature-icon">
                            <Lightning />
                        </el-icon>
                    </div>
                    <h3>快速生成</h3>
                    <p>几分钟内完成简历制作，高效便捷</p>
                </div>
            </div>
        </div>

        <DataPanel />
    </div>
</template>

<script setup>
import DataPanel from '@/components/DataPanel.vue';
import { MagicStick, Files, Lightning } from '@element-plus/icons-vue';
</script>

<style scoped>
.home-page {
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    position: relative;
    padding: 40px 20px;
    background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
    overflow-y: auto;
    box-sizing: border-box;
    scrollbar-width: none;
    /* Firefox */
    -ms-overflow-style: none;
    /* IE 10+ */
}

.home-page::-webkit-scrollbar {
    display: none;
    /* Chrome/Safari */
}

.content-container {
    max-width: 1200px;
    width: 100%;
    z-index: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.hero-section {
    text-align: center;
    margin-bottom: 60px;
    animation: fadeInDown 0.8s ease-out;
}

.main-title {
    font-size: 3rem;
    color: #2c3e50;
    font-weight: 800;
    margin-bottom: 20px;
    letter-spacing: -0.5px;
    background: linear-gradient(120deg, #2563eb, #3b82f6);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}

.subtitle {
    font-size: 1.25rem;
    color: #57606f;
    margin-bottom: 40px;
    font-weight: 400;
}

.action-buttons {
    display: flex;
    gap: 20px;
    justify-content: center;
}

.btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 14px 32px;
    border-radius: 50px;
    text-decoration: none;
    font-weight: 600;
    font-size: 16px;
    transition: all 0.3s ease;
    cursor: pointer;
    min-width: 160px;
}

.primary-btn {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    color: white;
    box-shadow: 0 4px 15px rgba(59, 130, 246, 0.4);
}

.primary-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(59, 130, 246, 0.5);
}

.secondary-btn {
    background: white;
    color: #57606f;
    border: 1px solid #e1e4e8;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.secondary-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 15px rgba(0, 0, 0, 0.1);
    color: #2c3e50;
    border-color: #d1d5db;
}

.features-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 30px;
    width: 100%;
    margin-bottom: 60px;
}

.feature-card {
    background: white;
    border-radius: 20px;
    padding: 40px 30px;
    text-align: center;
    transition: all 0.3s ease;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.5);
    backdrop-filter: blur(10px);
}

.feature-card:hover {
    transform: translateY(-10px);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.feature-icon-wrapper {
    width: 80px;
    height: 80px;
    background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 24px;
    color: white;
    font-size: 32px;
}

.feature-card:nth-child(2) .feature-icon-wrapper {
    background: linear-gradient(135deg, #93c5fd 0%, #60a5fa 100%);
}

.feature-card:nth-child(3) .feature-icon-wrapper {
    background: linear-gradient(135deg, #bfdbfe 0%, #93c5fd 100%);
}

.feature-card h3 {
    color: #2c3e50;
    font-size: 1.5rem;
    font-weight: 700;
    margin-bottom: 16px;
}

.feature-card p {
    color: #7f8c8d;
    font-size: 1rem;
    line-height: 1.6;
    margin: 0;
}

@keyframes fadeInDown {
    from {
        opacity: 0;
        transform: translateY(-20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

/* 响应式设计 */
@media (max-width: 768px) {
    .main-title {
        font-size: 2rem;
    }

    .subtitle {
        font-size: 1rem;
    }

    .action-buttons {
        flex-direction: column;
        width: 100%;
        max-width: 300px;
        margin: 0 auto;
    }

    .btn {
        width: 100%;
    }

    .features-grid {
        grid-template-columns: 1fr;
        gap: 20px;
    }
}
</style>