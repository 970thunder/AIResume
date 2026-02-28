<template>
    <div class="home-page" ref="homePageRef">
        <!-- Mouse Glow Effect -->
        <div class="mouse-glow" :style="mouseGlowStyle"></div>

        <!-- Hero Section -->
        <section class="hero-section">
            <div class="hero-content">
                <div class="badge">
                    <span class="badge-dot"></span>
                    <span class="badge-text">AI 驱动的简历生成引擎 v2.0</span>
                </div>
                <h1 class="hero-title">
                    用 <span class="gradient-text">智能算法</span> <br />
                    重塑您的职业生涯
                </h1>
                <p class="hero-subtitle">
                    不仅仅是简历工具，更是您的求职智能助手。深度分析、自动排版、精准匹配，让每一份简历都成为斩获 Offer 的利器。
                </p>
                <div class="hero-actions">
                    <router-link to="/generator" class="btn btn-primary" @click="addRipple">
                        <span class="btn-text">立即开始构建</span>
                        <div class="btn-glow"></div>
                        <el-icon>
                            <ArrowRight />
                        </el-icon>
                    </router-link>
                    <router-link to="/store" class="btn btn-secondary" @click="addRipple">
                        <el-icon>
                            <files />
                        </el-icon>
                        <span>浏览模板库</span>
                    </router-link>
                </div>
                <div class="hero-stats">
                    <div class="stat-item">
                        <span class="stat-value">{{ animatedStats.resumeCount }}<span class="stat-suffix">+</span></span>
                        <span class="stat-label">简历生成</span>
                    </div>
                    <div class="stat-divider"></div>
                    <div class="stat-item">
                        <span class="stat-value">{{ animatedStats.satisfaction }}<span class="stat-suffix">%</span></span>
                        <span class="stat-label">好评率</span>
                    </div>
                    <div class="stat-divider"></div>
                    <div class="stat-item">
                        <span class="stat-value">{{ animatedStats.templates }}<span class="stat-suffix">+</span></span>
                        <span class="stat-label">专业模板</span>
                    </div>
                </div>
            </div>

            <div class="hero-visual">
                <div class="visual-circle circle-1"></div>
                <div class="visual-circle circle-2"></div>
                <div class="visual-card">
                    <div class="card-header">
                        <div class="card-dots">
                            <span></span><span></span><span></span>
                        </div>
                    </div>
                    <div class="card-body">
                        <div class="skeleton-line w-75"></div>
                        <div class="skeleton-line w-50"></div>
                        <div class="skeleton-block"></div>
                        <div class="skeleton-row">
                            <div class="skeleton-col"></div>
                            <div class="skeleton-col"></div>
                        </div>
                    </div>
                    <div class="scan-line"></div>
                </div>
            </div>
        </section>

        <!-- Features Scroll Section -->
        <section class="features-section">
            <div class="section-header">
                <h2>全流程智能化求职体验</h2>
                <p>从创建到投递，每一个环节都为您精细打磨</p>
            </div>

            <div class="feature-strip">
                <div class="feature-item">
                    <div class="feature-icon-box">
                        <el-icon>
                            <MagicStick />
                        </el-icon>
                    </div>
                    <h3>智能生成</h3>
                    <p>输入经历，AI 自动润色并生成专业话术</p>
                </div>
                <div class="feature-item">
                    <div class="feature-icon-box">
                        <el-icon>
                            <DataAnalysis />
                        </el-icon>
                    </div>
                    <h3>简历评分</h3>
                    <p>多维度打分，精准定位简历薄弱点</p>
                </div>
                <div class="feature-item">
                    <div class="feature-icon-box">
                        <el-icon>
                            <Reading />
                        </el-icon>
                    </div>
                    <h3>模拟面试</h3>
                    <p>基于简历生成面试题，提前演练</p>
                </div>
            </div>
        </section>

        <!-- Workflow Section -->
        <section class="workflow-section">
            <div class="workflow-container">
                <div class="workflow-step">
                    <span class="step-number">01</span>
                    <h4>选择模板</h4>
                    <p>从精选库中挑选适合您行业的专业模板</p>
                </div>
                <div class="step-arrow">→</div>
                <div class="workflow-step">
                    <span class="step-number">02</span>
                    <h4>AI 填充</h4>
                    <p>上传旧简历或输入信息，AI 自动完善内容</p>
                </div>
                <div class="step-arrow">→</div>
                <div class="workflow-step">
                    <span class="step-number">03</span>
                    <h4>实时预览</h4>
                    <p>所见即所得，实时调整布局与配色</p>
                </div>
                <div class="step-arrow">→</div>
                <div class="workflow-step">
                    <span class="step-number">04</span>
                    <h4>一键导出</h4>
                    <p>生成高清 PDF，准备好您的求职敲门砖</p>
                </div>
            </div>
        </section>

        <!-- CTA Section -->
        <section class="cta-section">
            <div class="cta-content">
                <h2>准备好开始了吗？</h2>
                <p>立即加入，让 AI 为您的职业发展加速</p>
                <router-link to="/generator" class="btn btn-primary btn-lg">
                    免费创建简历
                </router-link>
            </div>
            <div class="cta-bg-glow"></div>
        </section>

        <footer class="home-footer">
            <div class="footer-content">
                <div class="footer-logo">Hyper 简生</div>
                <div class="footer-links">
                    <a href="#">关于我们</a>
                    <a href="#">使用条款</a>
                    <a href="#">隐私政策</a>
                    <a href="#">帮助中心</a>
                </div>
                <div class="footer-copyright">
                    © 2024 Hyper Resume. All rights reserved.
                </div>
            </div>
        </footer>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue';
import { ArrowRight, Files, MagicStick, DataAnalysis, Reading, Download } from '@element-plus/icons-vue';

const homePageRef = ref(null);

// Stats animation
const stats = {
    resumeCount: 10000,
    satisfaction: 98,
    templates: 50
};

const animatedStats = reactive({
    resumeCount: 0,
    satisfaction: 0,
    templates: 0
});

// Mouse glow effect
const mouseGlow = reactive({
    x: 0,
    y: 0,
    visible: false
});

const mouseGlowStyle = computed(() => ({
    left: `${mouseGlow.x}px`,
    top: `${mouseGlow.y}px`,
    opacity: mouseGlow.visible ? 1 : 0
}));

// Animate stats counting
const animateStats = () => {
    const duration = 2000;
    const steps = 60;
    const interval = duration / steps;

    let step = 0;
    const timer = setInterval(() => {
        step++;
        const progress = step / steps;
        const easeOut = 1 - Math.pow(1 - progress, 3);

        animatedStats.resumeCount = Math.floor(stats.resumeCount * easeOut);
        animatedStats.satisfaction = Math.floor(stats.satisfaction * easeOut);
        animatedStats.templates = Math.floor(stats.templates * easeOut);

        if (step >= steps) {
            clearInterval(timer);
            animatedStats.resumeCount = stats.resumeCount;
            animatedStats.satisfaction = stats.satisfaction;
            animatedStats.templates = stats.templates;
        }
    }, interval);
};

// Ripple effect for buttons
const addRipple = (event) => {
    const button = event.currentTarget;
    const ripple = document.createElement('span');
    const rect = button.getBoundingClientRect();
    const size = Math.max(rect.width, rect.height);
    const x = event.clientX - rect.left - size / 2;
    const y = event.clientY - rect.top - size / 2;

    ripple.style.cssText = `
        position: absolute;
        width: ${size}px;
        height: ${size}px;
        left: ${x}px;
        top: ${y}px;
        background: rgba(255, 255, 255, 0.3);
        border-radius: 50%;
        transform: scale(0);
        animation: ripple 0.6s ease-out;
        pointer-events: none;
    `;
    button.appendChild(ripple);
    setTimeout(() => ripple.remove(), 600);
};

// Mouse move handler for glow effect
const handleMouseMove = (e) => {
    if (homePageRef.value) {
        const rect = homePageRef.value.getBoundingClientRect();
        mouseGlow.x = e.clientX - rect.left;
        mouseGlow.y = e.clientY - rect.top;
        mouseGlow.visible = true;
    }
};

const handleMouseLeave = () => {
    mouseGlow.visible = false;
};

onMounted(() => {
    animateStats();
    if (homePageRef.value) {
        homePageRef.value.addEventListener('mousemove', handleMouseMove);
        homePageRef.value.addEventListener('mouseleave', handleMouseLeave);
    }
});

onUnmounted(() => {
    if (homePageRef.value) {
        homePageRef.value.removeEventListener('mousemove', handleMouseMove);
        homePageRef.value.removeEventListener('mouseleave', handleMouseLeave);
    }
});
</script>

<style scoped>
.home-page {
    width: 100%;
    min-height: 100%;
    overflow-x: hidden;
    /* Main Background managed by Layout, but we add overlay */
    background: transparent;
    position: relative;
}

/* Mouse Glow Effect */
.mouse-glow {
    position: absolute;
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, rgba(56, 189, 248, 0.15) 0%, transparent 70%);
    border-radius: 50%;
    pointer-events: none;
    transform: translate(-50%, -50%);
    transition: opacity 0.3s ease;
    z-index: 0;
}

/* Typography & Variables locally mapped if needed, but using globals */
h1,
h2,
h3,
h4 {
    margin: 0;
    line-height: 1.2;
    color: #ffffff;
    /* Force white for all headings */
}

/* Hero Section */
.hero-section {
    min-height: 90vh;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 8%;
    position: relative;
    overflow: hidden;
    color: #ffffff;
    /* Default text color for hero */
}

.hero-content {
    flex: 1;
    max-width: 600px;
    z-index: 2;
    animation: slideUp 0.8s ease-out;
}

.badge {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 6px 12px;
    background: rgba(14, 165, 233, 0.1);
    border: 1px solid rgba(14, 165, 233, 0.2);
    border-radius: 20px;
    margin-bottom: 24px;
    font-size: 13px;
    color: #38bdf8;
    /* Light blue instead of accent variable */
}

.badge-dot {
    width: 6px;
    height: 6px;
    background: #38bdf8;
    border-radius: 50%;
    box-shadow: 0 0 8px #38bdf8;
}

.hero-title {
    font-size: 3.5rem;
    font-weight: 800;
    color: #ffffff;
    margin-bottom: 24px;
    letter-spacing: -1px;
}

.gradient-text {
    background: linear-gradient(135deg, #38bdf8 0%, #a5f3fc 100%);
    /* Brighter gradient */
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}

.hero-subtitle {
    font-size: 1.1rem;
    color: #cbd5e1;
    /* Light grey instead of muted variable */
    margin-bottom: 40px;
    line-height: 1.6;
}

.hero-actions {
    display: flex;
    gap: 16px;
    margin-bottom: 48px;
}

.btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 12px 28px;
    border-radius: 12px;
    font-weight: 600;
    font-size: 16px;
    text-decoration: none;
    transition: all 0.3s ease;
    cursor: pointer;
    position: relative;
    overflow: hidden;
}

.btn-primary {
    background: #38bdf8;
    color: #000;
    box-shadow: 0 0 20px rgba(14, 165, 233, 0.4);
}

.btn-primary:hover {
    transform: translateY(-2px);
    box-shadow: 0 0 30px rgba(14, 165, 233, 0.6);
}

.btn-glow {
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.4) 0%, transparent 60%);
    opacity: 0;
    transform: scale(0.5);
    transition: opacity 0.3s, transform 0.3s;
}

.btn-primary:hover .btn-glow {
    opacity: 1;
    transform: scale(1);
}

.btn-secondary {
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    color: #ffffff;
}

.btn-secondary:hover {
    background: rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.2);
}

.hero-stats {
    display: flex;
    align-items: center;
    gap: 24px;
}

.stat-item {
    display: flex;
    flex-direction: column;
}

.stat-value {
    font-size: 1.5rem;
    font-weight: 700;
    color: #ffffff;
}

.stat-label {
    font-size: 0.85rem;
    color: var(--muted);
}

.stat-divider {
    width: 1px;
    height: 30px;
    background: rgba(255, 255, 255, 0.1);
}

/* Hero Visual */
.hero-visual {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
    height: 500px;
    perspective: 1000px;
}

.visual-circle {
    position: absolute;
    border-radius: 50%;
    filter: blur(60px);
    z-index: 0;
}

.circle-1 {
    width: 300px;
    height: 300px;
    background: rgba(14, 165, 233, 0.2);
    top: 10%;
    right: 10%;
    animation: float 6s ease-in-out infinite;
}

.circle-2 {
    width: 200px;
    height: 200px;
    background: rgba(34, 211, 238, 0.15);
    bottom: 10%;
    left: 10%;
    animation: float 8s ease-in-out infinite reverse;
}

.visual-card {
    width: 340px;
    height: 460px;
    background: rgba(15, 23, 42, 0.6);
    border: 2px solid rgba(255, 255, 255, 0.2);
    border-radius: 20px;
    backdrop-filter: blur(20px);
    position: relative;
    z-index: 1;
    transform: rotateY(-10deg) rotateX(5deg);
    box-shadow: 20px 20px 60px rgba(0, 0, 0, 0.4);
    display: flex;
    flex-direction: column;
    overflow: hidden;
    transition: transform 0.5s ease;
}

.visual-card:hover {
    transform: rotateY(0) rotateX(0);
}

.card-header {
    height: 40px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    display: flex;
    align-items: center;
    padding: 0 16px;
}

.card-dots {
    display: flex;
    gap: 6px;
}

.card-dots span {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
}

.card-body {
    padding: 24px;
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.skeleton-line {
    height: 8px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 4px;
}

.skeleton-block {
    height: 100px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 8px;
    margin-top: 10px;
}

.skeleton-row {
    display: flex;
    gap: 12px;
}

.skeleton-col {
    flex: 1;
    height: 60px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 8px;
}

.w-75 {
    width: 75%;
}

.w-50 {
    width: 50%;
}

.scan-line {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 4px;
    background: linear-gradient(90deg, transparent, var(--accent), transparent);
    animation: scan 3s linear infinite;
    opacity: 0.5;
}

/* Features Section */
.features-section {
    padding: 100px 8%;
    background: transparent;
    /* Remove dark overlay if it clashes */
    text-align: center;
}

.section-header h2 {
    font-size: 2.5rem;
    color: #ffffff !important;
    margin-bottom: 16px;
}

.section-header p {
    color: #cbd5e1 !important;
    /* Light grey */
    margin-bottom: 60px;
    font-size: 1.1rem;
}

.feature-strip {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 32px;
}

.feature-item {
    background: rgba(255, 255, 255, 0.05);
    /* Slightly lighter background */
    border: 1px solid rgba(255, 255, 255, 0.1);
    padding: 32px;
    border-radius: 16px;
    transition: all 0.3s ease;
}

.feature-item:hover {
    background: rgba(255, 255, 255, 0.1);
    transform: translateY(-5px);
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.feature-icon-box {
    width: 56px;
    height: 56px;
    background: rgba(56, 189, 248, 0.15);
    /* Brighter blue bg */
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: #38bdf8;
    /* Brighter blue icon */
    margin: 0 auto 20px;
}

.feature-item h3 {
    color: #ffffff !important;
    font-size: 1.25rem;
    margin-bottom: 12px;
}

.feature-item p {
    color: #cbd5e1 !important;
    /* Light grey */
    font-size: 0.95rem;
    line-height: 1.5;
}

/* Workflow Section */
.workflow-section {
    padding: 100px 8%;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    background: rgba(0, 0, 0, 0.2);
}

.workflow-container {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 20px;
    max-width: 1200px;
    margin: 0 auto;
}

.workflow-step {
    flex: 1;
    text-align: center;
}

.step-number {
    font-size: 3rem;
    font-weight: 800;
    color: rgba(255, 255, 255, 0.15);
    /* More visible number */
    display: block;
    margin-bottom: 8px;
}

.workflow-step h4 {
    font-size: 1.2rem;
    color: #ffffff !important;
    margin-bottom: 8px;
}

.workflow-step p {
    font-size: 0.9rem;
    color: #cbd5e1 !important;
    /* Light grey */
}

.step-arrow {
    font-size: 24px;
    color: rgba(255, 255, 255, 0.3);
    /* More visible arrow */
}

/* CTA Section */
.cta-section {
    padding: 120px 8%;
    text-align: center;
    position: relative;
    overflow: hidden;
}

.cta-content {
    position: relative;
    z-index: 2;
    max-width: 600px;
    margin: 0 auto;
}

.cta-content h2 {
    font-size: 3rem;
    color: #ffffff !important;
    margin-bottom: 24px;
}

.cta-content p {
    color: #cbd5e1 !important;
    /* Light grey */
    margin-bottom: 40px;
    font-size: 1.1rem;
}

.btn-lg {
    padding: 16px 48px;
    font-size: 18px;
}

.cta-bg-glow {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 600px;
    height: 600px;
    background: radial-gradient(circle, rgba(14, 165, 233, 0.15) 0%, transparent 70%);
    z-index: 1;
    pointer-events: none;
}

/* Footer */
.home-footer {
    padding: 60px 8%;
    background: rgba(0, 0, 0, 0.2);
    border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.footer-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.footer-logo {
    font-size: 1.5rem;
    font-weight: 800;
    color: #ffffff;
}

.footer-links {
    display: flex;
    gap: 24px;
}

.footer-links a {
    color: #cbd5e1 !important;
    /* Force light grey */
    text-decoration: none;
    font-size: 0.9rem;
    transition: color 0.2s;
}

.footer-links a:hover {
    color: #ffffff !important;
    /* Force white on hover */
    text-shadow: 0 0 8px rgba(255, 255, 255, 0.5);
}

.footer-copyright {
    color: rgba(255, 255, 255, 0.4) !important;
    /* Ensure copyright is visible */
    font-size: 0.85rem;
}

/* Animations */
@keyframes slideUp {
    from {
        opacity: 0;
        transform: translateY(30px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes ripple {
    to {
        transform: scale(4);
        opacity: 0;
    }
}

@keyframes float {

    0%,
    100% {
        transform: translateY(0);
    }

    50% {
        transform: translateY(-20px);
    }
}

@keyframes scan {
    0% {
        top: 0;
        opacity: 0;
    }

    50% {
        opacity: 1;
    }

    100% {
        top: 100%;
        opacity: 0;
    }
}

/* Responsive */
@media (max-width: 1024px) {
    .hero-section {
        flex-direction: column;
        text-align: center;
        padding-top: 120px;
    }

    .hero-content {
        margin-bottom: 60px;
    }

    .hero-actions {
        justify-content: center;
    }

    .hero-stats {
        justify-content: center;
    }

    .workflow-container {
        flex-direction: column;
    }

    .step-arrow {
        transform: rotate(90deg);
        margin: 20px 0;
    }

    .footer-content {
        flex-direction: column;
        gap: 30px;
    }
}
</style>

<style>
/* Global Custom Scrollbar */
::-webkit-scrollbar {
    width: 8px;
    height: 8px;
}

::-webkit-scrollbar-track {
    background: rgba(15, 23, 42, 0.6);
    border-radius: 4px;
}

::-webkit-scrollbar-thumb {
    background: linear-gradient(180deg, #38bdf8 0%, #0ea5e9 50%, #0284c7 100%);
    border-radius: 4px;
    border: 2px solid rgba(15, 23, 42, 0.8);
    transition: all 0.3s ease;
}

::-webkit-scrollbar-thumb:hover {
    background: linear-gradient(180deg, #7dd3fc 0%, #38bdf8 50%, #0ea5e9 100%);
    box-shadow: 0 0 10px rgba(56, 189, 248, 0.5);
}

::-webkit-scrollbar-corner {
    background: rgba(15, 23, 42, 0.6);
}

/* Firefox Scrollbar */
* {
    scrollbar-width: thin;
    scrollbar-color: #38bdf8 rgba(15, 23, 42, 0.6);
}
</style>
