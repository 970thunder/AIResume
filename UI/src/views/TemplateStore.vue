<template>
    <div class="page-container">
        <el-card>
            <template #header>
                <div class="card-header">
                    <h1>模板商城</h1>
                </div>
            </template>
            <p>这里将来会展示所有可用的精美简历模板，敬请期待！</p>
            <el-empty description="模板正在加速上架中..." />
        </el-card>
    </div>
</template>

<script setup>
// 未来将在这里添加模板商城的逻辑，例如从后端获取模板列表
</script>

<style scoped>
.page-container {
    padding: 20px;
}

.card-header h1 {
    margin: 0;
    font-size: 24px;
}
</style>