<template>
    <div class="home-page">
        <div class="pixel-container">
            <div class="title-card">
                <h1 class="pixel-title">欢迎使用 AI 智能简历生成器</h1>
                <p class="pixel-subtitle">一站式解决您的简历烦恼，智能、高效、专业</p>
                <div class="pixel-buttons">
                    <router-link to="/generator" class="pixel-btn primary">
                        开始制作简历 →
                    </router-link>
                    <router-link to="/store" class="pixel-btn secondary">
                        浏览模板库
                    </router-link>
                </div>
            </div>

            <div class="features-grid">
                <div class="feature-card">
                    <div class="feature-icon">🤖</div>
                    <h3>AI 智能生成</h3>
                    <p>基于您的信息智能生成专业简历内容</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon">📋</div>
                    <h3>多种模板</h3>
                    <p>精选多种专业简历模板，满足不同需求</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon">⚡</div>
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
</script>

<style scoped>
.home-page {
    --main-color: #09465d;
    --bg-color: #fff;
    --font-color: #060606;
    --font-color-sub: #383838;
    --input-focus: #2d8cf0;
    --card-bg: rgb(220, 244, 251);



    display: flex;
    flex-direction: column;
    align-items: center;
    position: relative;
}

/* 添加像素化背景点缀 */
.home-page::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image:
        radial-gradient(circle at 20% 30%, var(--main-color) 2px, transparent 2px),
        radial-gradient(circle at 80% 20%, var(--input-focus) 1px, transparent 1px),
        radial-gradient(circle at 60% 80%, var(--main-color) 1px, transparent 1px);
    background-size: 100px 100px, 150px 150px, 80px 80px;
    opacity: 0.1;
    pointer-events: none;
}

.pixel-container {
    max-width: 1200px;
    width: 100%;
    z-index: 1;
}

.title-card {
    background: var(--card-bg);
    border: 3px solid var(--main-color);
    border-radius: 8px;
    box-shadow: 6px 6px 0 var(--main-color);
    padding: 40px;
    text-align: center;
    margin-bottom: 40px;
    transform: perspective(1000px) rotateX(2deg);
}

.pixel-title {
    font-size: 3.5em;
    color: var(--main-color);
    font-weight: 900;
    margin: 0 0 20px 0;
    text-shadow:
        2px 2px 0 #fff,
        4px 4px 0 rgba(9, 70, 93, 0.3);
    letter-spacing: -1px;
}

.pixel-subtitle {
    font-size: 1.4em;
    color: var(--font-color-sub);
    margin: 0 0 30px 0;
    font-weight: 600;
}

.pixel-buttons {
    display: flex;
    gap: 20px;
    justify-content: center;
    flex-wrap: wrap;
}

.pixel-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 15px 25px;
    border-radius: 6px;
    border: 2px solid var(--main-color);
    box-shadow: 4px 4px 0 var(--main-color);
    text-decoration: none;
    font-weight: 700;
    font-size: 16px;
    transition: all 0.1s ease;
    cursor: pointer;
    min-width: 180px;
}

.pixel-btn.primary {
    background: var(--main-color);
    color: white;
}

.pixel-btn.secondary {
    background: var(--bg-color);
    color: var(--main-color);
}

.pixel-btn:hover {
    transform: translate(1px, 1px);
    box-shadow: 3px 3px 0 var(--main-color);
}

.pixel-btn:active {
    transform: translate(4px, 4px);
    box-shadow: 0 0 0 var(--main-color);
}

.features-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 30px;
    margin-bottom: 50px;
}

.feature-card {
    background: var(--bg-color);
    border: 2px solid var(--main-color);
    border-radius: 6px;
    box-shadow: 4px 4px 0 var(--main-color);
    padding: 30px 25px;
    text-align: center;
    transition: all 0.2s ease;
}

.feature-card:hover {
    transform: translate(-2px, -2px);
    box-shadow: 6px 6px 0 var(--main-color);
}

.feature-icon {
    font-size: 3em;
    margin-bottom: 15px;
    display: block;
}

.feature-card h3 {
    color: var(--main-color);
    font-size: 1.4em;
    font-weight: 700;
    margin: 0 0 15px 0;
}

.feature-card p {
    color: var(--font-color-sub);
    font-size: 1em;
    line-height: 1.5;
    margin: 0;
    font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .home-page {
        padding: 20px 15px;
    }

    .title-card {
        padding: 30px 20px;
        transform: none;
    }

    .pixel-title {
        font-size: 2.5em;
    }

    .pixel-subtitle {
        font-size: 1.2em;
    }

    .pixel-buttons {
        flex-direction: column;
        align-items: center;
    }

    .pixel-btn {
        width: 100%;
        max-width: 280px;
    }

    .features-grid {
        grid-template-columns: 1fr;
        gap: 20px;
    }
}

@media (max-width: 480px) {
    .pixel-title {
        font-size: 2em;
    }

    .feature-card {
        padding: 25px 20px;
    }

    .feature-icon {
        font-size: 2.5em;
    }
}
</style>