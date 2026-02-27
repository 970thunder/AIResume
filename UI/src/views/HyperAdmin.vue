<template>
    <div class="hyper-admin-container">
        <!-- Login Form -->
        <div v-if="!isLoggedInAsAdmin" class="login-wrapper">
            <el-card class="login-card" shadow="always">
                <template #header>
                    <div class="card-header">
                        <h2>Hyper Admin Access</h2>
                    </div>
                </template>
                <el-form :model="loginForm" @submit.prevent="handleLogin">
                    <el-form-item>
                        <el-input v-model="loginForm.username" placeholder="Username" :prefix-icon="User"
                            size="large"></el-input>
                    </el-form-item>
                    <el-form-item>
                        <el-input v-model="loginForm.password" type="password" placeholder="Password"
                            :prefix-icon="Lock" size="large" show-password></el-input>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="handleLogin" :loading="loading" long class="login-button">
                            {{ loading ? 'Signing In...' : 'Sign In' }}
                        </el-button>
                    </el-form-item>
                </el-form>
            </el-card>
        </div>

        <!-- Admin Panel -->
        <div v-else class="admin-panel">
            <el-card>
                <template #header>
                    <div class="card-header">
                        <h1>模板审核后台</h1>
                        <el-button type="danger" @click="handleLogout" :icon="SwitchButton">登出</el-button>
                    </div>
                </template>
                <el-table :data="templates" v-loading="loading" style="width: 100%" row-class-name="table-row-style">
                    <el-table-column prop="name" label="模板名称"></el-table-column>
                    <el-table-column prop="author.username" label="作者"></el-table-column>
                    <el-table-column prop="createdAt" label="提交时间" width="180">
                        <template #default="scope">{{ new Date(scope.row.createdAt).toLocaleString() }}</template>
                    </el-table-column>
                    <el-table-column prop="status" label="状态" width="120">
                        <template #default="scope">
                            <el-tag :type="getStatusTagType(scope.row.status)" effect="dark">{{ scope.row.status
                            }}</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="250">
                        <template #default="scope">
                            <el-button size="small" type="primary" @click="goToAuditor(scope.row.id)">预览/编辑</el-button>
                            <el-button v-if="scope.row.status === 'PENDING'" size="small" type="success"
                                @click="updateStatus(scope.row.id, 'APPROVED')">通过</el-button>
                            <el-button v-if="scope.row.status === 'PENDING' || scope.row.status === 'APPROVED'"
                                size="small" type="danger"
                                @click="updateStatus(scope.row.id, 'REJECTED')">拒绝/下架</el-button>
                            <el-button v-if="scope.row.status === 'REJECTED'" size="small" type="warning"
                                @click="updateStatus(scope.row.id, 'PENDING')">重新审核</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </el-card>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { ElNotification } from 'element-plus';
import { User, Lock, SwitchButton } from '@element-plus/icons-vue';
import axios from 'axios';
import { API_URLS, getHeaders } from '@/config/api';

const authStore = useAuthStore();
const router = useRouter();
const loading = ref(false);
const templates = ref([]);
const isLoggedInAsAdmin = ref(false);

const loginForm = reactive({ username: 'xnrHyper', password: '' });

const handleLogin = async () => {
    loading.value = true;
    try {
        const { isAdmin } = await authStore.login(loginForm);

        console.log('[DIAGNOSTIC] Login action returned isAdmin:', isAdmin);
        console.log('[DIAGNOSTIC] User object in store after login:', authStore.user ? JSON.parse(JSON.stringify(authStore.user)) : 'null');

        if (isAdmin) {
            ElNotification({ title: '成功', message: '管理员登录成功！', type: 'success' });
            isLoggedInAsAdmin.value = true;
            await fetchTemplates();
        } else {
            authStore.logout();
            isLoggedInAsAdmin.value = false;
            ElNotification({ title: '错误', message: '非管理员用户，禁止访问。', type: 'error' });
        }
    } catch (error) {
        ElNotification({ title: '错误', message: '登录失败，请检查凭证。', type: 'error' });
    } finally {
        loading.value = false;
    }
};

const handleLogout = () => {
    authStore.logout();
    isLoggedInAsAdmin.value = false;
    ElNotification({ title: '提示', message: '您已成功登出。', type: 'info' });
};

const fetchTemplates = async () => {
    loading.value = true;
    try {
        const response = await axios.get(API_URLS.admin.templates, { headers: getHeaders() });
        templates.value = response.data;
    } catch (error) {
        ElNotification({ title: '错误', message: '获取模板列表失败', type: 'error' });
    } finally {
        loading.value = false;
    }
};

const updateStatus = async (id, status) => {
    try {
        await axios.put(API_URLS.admin.updateTemplateStatus(id), { status }, { headers: getHeaders() });
        ElNotification({ title: '成功', message: `模板状态已更新`, type: 'success' });
        await fetchTemplates();
    } catch (error) {
        ElNotification({ title: '错误', message: '更新状态失败', type: 'error' });
    }
};

const goToAuditor = (id) => {
    router.push({ name: 'HyperTemplateAuditor', params: { id } });
};

const getStatusTagType = (status) => {
    if (status === 'APPROVED') return 'success';
    if (status === 'PENDING') return 'warning';
    if (status === 'REJECTED') return 'danger';
    return 'info';
};

onMounted(() => {
    authStore.initializeAuth();

    console.log('[DIAGNOSTIC] Component did mount.');
    console.log('[DIAGNOSTIC] User object from store on mount:', authStore.user ? JSON.parse(JSON.stringify(authStore.user)) : 'null');
    console.log('[DIAGNOSTIC] `isAdmin` getter from store on mount:', authStore.isAdmin);

    isLoggedInAsAdmin.value = authStore.isAdmin;
    if (isLoggedInAsAdmin.value) {
        fetchTemplates();
    }
});
</script>

<style scoped>
.hyper-admin-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    background: radial-gradient(1200px 600px at 20% -20%, rgba(14, 165, 233, 0.15) 40%, var(--bg-primary) 100%);
    padding: 24px;
    box-sizing: border-box;
}

.login-wrapper {
    width: 400px;
    animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(30px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.login-card {
    border-radius: 20px !important;
    background: var(--glass-bg) !important;
    border: 1px solid var(--glass-border) !important;
    box-shadow: var(--glass-shadow) !important;
    backdrop-filter: var(--glass-blur) !important;
    -webkit-backdrop-filter: var(--glass-blur) !important;
}

:deep(.el-card__header) {
    border-bottom: 1px solid var(--glass-border) !important;
    padding: 24px !important;
}

:deep(.el-card__body) {
    padding: 32px 24px !important;
}

.card-header h2 {
    text-align: center;
    color: var(--fg-primary);
    font-weight: 700;
    margin: 0;
}

.card-header h1 {
    color: var(--fg-primary);
    font-weight: 700;
    margin: 0;
}

/* Form Styles */
:deep(.el-form-item) {
    margin-bottom: 20px;
}

:deep(.el-input__wrapper) {
    background: rgba(15, 23, 42, 0.6) !important;
    border: 1px solid var(--glass-border) !important;
    box-shadow: none !important;
    border-radius: 12px !important;
    transition: all 0.3s ease !important;
}

:deep(.el-input__wrapper:hover) {
    border-color: rgba(255, 255, 255, 0.2) !important;
}

:deep(.el-input__wrapper.is-focus) {
    border-color: var(--accent-primary) !important;
    box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.15) !important;
}

:deep(.el-input__inner) {
    color: var(--fg-primary) !important;
}

:deep(.el-input__inner::placeholder) {
    color: var(--fg-muted) !important;
}

:deep(.el-input__prefix) {
    color: var(--fg-muted) !important;
}

.login-button {
    width: 100%;
    background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary)) !important;
    border: none !important;
    color: var(--bg-primary) !important;
    font-weight: 600 !important;
    border-radius: 12px !important;
    height: 48px !important;
    font-size: 16px !important;
    box-shadow: 0 8px 24px rgba(14, 165, 233, 0.35) !important;
    transition: all 0.3s ease !important;
}

.login-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 32px rgba(14, 165, 233, 0.45) !important;
}

.admin-panel {
    width: 90%;
    max-width: 1400px;
    animation: fadeInUp 0.6s ease-out;
}

.admin-panel :deep(.el-card) {
    background: var(--glass-bg) !important;
    border: 1px solid var(--glass-border) !important;
    border-radius: 16px !important;
    box-shadow: var(--glass-shadow) !important;
    backdrop-filter: var(--glass-blur) !important;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.card-header :deep(.el-button--danger) {
    background: rgba(239, 68, 68, 0.15) !important;
    border: 1px solid rgba(239, 68, 68, 0.3) !important;
    color: #f87171 !important;
}

.card-header :deep(.el-button--danger:hover) {
    background: rgba(239, 68, 68, 0.25) !important;
    border-color: rgba(239, 68, 68, 0.5) !important;
}

/* Table Styles */
:deep(.el-table) {
    background: transparent !important;
    --el-table-bg-color: transparent !important;
    --el-table-tr-bg-color: transparent !important;
    --el-table-header-bg-color: rgba(255, 255, 255, 0.04) !important;
    --el-table-header-text-color: var(--fg-primary) !important;
    --el-table-text-color: var(--fg-secondary) !important;
    --el-table-row-hover-bg-color: rgba(255, 255, 255, 0.06) !important;
    --el-table-border-color: var(--glass-border) !important;
}

:deep(.el-table th.el-table__cell) {
    background: rgba(255, 255, 255, 0.04) !important;
    color: var(--fg-primary) !important;
    font-weight: 600 !important;
}

:deep(.el-table td.el-table__cell) {
    color: var(--fg-secondary) !important;
}

:deep(.el-table--enable-row-hover .el-table__body tr:hover > td.el-table__cell) {
    background: rgba(255, 255, 255, 0.06) !important;
}

/* Table Buttons */
:deep(.el-table .el-button--primary) {
    background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary)) !important;
    border: none !important;
    color: var(--bg-primary) !important;
}

:deep(.el-table .el-button--success) {
    background: rgba(34, 197, 94, 0.15) !important;
    border: 1px solid rgba(34, 197, 94, 0.3) !important;
    color: #4ade80 !important;
}

:deep(.el-table .el-button--danger) {
    background: rgba(239, 68, 68, 0.15) !important;
    border: 1px solid rgba(239, 68, 68, 0.3) !important;
    color: #f87171 !important;
}

:deep(.el-table .el-button--warning) {
    background: rgba(245, 158, 11, 0.15) !important;
    border: 1px solid rgba(245, 158, 11, 0.3) !important;
    color: #fbbf24 !important;
}

/* Tags */
:deep(.el-tag--success.el-tag--dark) {
    background: rgba(34, 197, 94, 0.2) !important;
    border-color: rgba(34, 197, 94, 0.3) !important;
    color: #4ade80 !important;
}

:deep(.el-tag--warning.el-tag--dark) {
    background: rgba(245, 158, 11, 0.2) !important;
    border-color: rgba(245, 158, 11, 0.3) !important;
    color: #fbbf24 !important;
}

:deep(.el-tag--danger.el-tag--dark) {
    background: rgba(239, 68, 68, 0.2) !important;
    border-color: rgba(239, 68, 68, 0.3) !important;
    color: #f87171 !important;
}

:deep(.el-tag--info.el-tag--dark) {
    background: rgba(255, 255, 255, 0.1) !important;
    border-color: var(--glass-border) !important;
    color: var(--fg-secondary) !important;
}

/* Loading */
:deep(.el-loading-mask) {
    background: rgba(11, 18, 32, 0.8) !important;
}

:deep(.el-loading-spinner .el-loading-text) {
    color: var(--fg-primary) !important;
}

:deep(.el-loading-spinner .path) {
    stroke: var(--accent-primary) !important;
}
</style>