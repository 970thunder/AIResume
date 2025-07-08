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
    background: linear-gradient(to right, #6dd5ed, #2193b0);
}

.login-wrapper {
    width: 380px;
}

.login-card {
    border-radius: 15px;
}

.card-header h2 {
    text-align: center;
    color: #2193b0;
}

.login-button {
    width: 100%;
}

.admin-panel {
    width: 90%;
    max-width: 1400px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
</style>