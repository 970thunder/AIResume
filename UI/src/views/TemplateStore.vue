<template>
    <div class="store-page">
        <div class="placeholder">
            <el-icon :size="50" color="#c0c4cc"><shopping-cart-full /></el-icon>
            <h1>模板商城即将上线</h1>
            <p>敬请期待更多精美、专业的简历模板！</p>
        </div>
    </div>
</template>

<script setup>
import { ShoppingCartFull } from '@element-plus/icons-vue';
</script>

<style scoped>
.store-page {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
    text-align: center;
    color: #909399;
}

.placeholder h1 {
    font-size: 2em;
    color: #606266;
    margin: 20px 0 10px 0;
}
</style>