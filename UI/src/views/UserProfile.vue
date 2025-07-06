<template>
    <div class="page-container">
        <el-card>
            <template #header>
                <div class="card-header">
                    <h1>个人中心</h1>
                </div>
            </template>
            <div v-if="authStore.user">
                <el-descriptions title="账户信息" border :column="2">
                    <el-descriptions-item label="用户名">{{ authStore.user.username }}</el-descriptions-item>
                    <el-descriptions-item label="电子邮箱">{{ authStore.user.email }}</el-descriptions-item>
                    <el-descriptions-item label="注册时间">{{ formattedDate(authStore.user.createdAt)
                        }}</el-descriptions-item>
                </el-descriptions>
            </div>
            <el-empty v-else description="无法加载用户信息" />
        </el-card>
    </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();

const formattedDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    });
};
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