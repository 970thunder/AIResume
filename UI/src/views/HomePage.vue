<template>
    <div class="home-page">
        <div class="title-container">
            <h1>欢迎使用 AI 智能简历生成器</h1>
            <p class="subtitle">一站式解决您的简历烦恼，智能、高效、专业</p>
        </div>
        <DataPanel />
    </div>
</template>

<script setup>
import DataPanel from '@/components/DataPanel.vue';
</script>

<style scoped>
.home-page {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: space-between;
    height: 86vh;
    text-align: center;
}

.title-container h1 {
    font-size: 4em;
    color: #337ecc;
    font-weight: 600;
    margin-top: 25vh;
    margin-bottom: 20px;
}

.subtitle {
    font-size: 1.2em;
    color: #909399;
}
</style>