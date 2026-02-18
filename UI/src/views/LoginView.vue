<template>
    <div class="auth-container">
        <!-- Background Animation Elements -->
        <div class="circles">
            <div class="circle circle-1"></div>
            <div class="circle circle-2"></div>
            <div class="circle circle-3"></div>
        </div>

        <div class="glass-card">
            <div class="card-content">
                <div class="title-section">
                    <h1 class="main-title">欢迎回来</h1>
                    <p class="sub-title">登录您的 AI 简历账户</p>
                </div>

                <el-form :model="loginForm" :rules="rules" ref="loginFormRef" @submit.prevent="handleLogin"
                    class="custom-form">
                    <el-form-item prop="username">
                        <div class="input-wrapper">
                            <el-input v-model="loginForm.username" placeholder="用户名 / 邮箱" :prefix-icon="User"
                                class="custom-input" />
                            <div class="input-highlight"></div>
                        </div>
                    </el-form-item>

                    <el-form-item prop="password">
                        <div class="input-wrapper">
                            <el-input v-model="loginForm.password" type="password" placeholder="密码" :prefix-icon="Lock"
                                show-password class="custom-input" />
                            <div class="input-highlight"></div>
                        </div>
                    </el-form-item>

                    <div class="actions">
                        <el-button @click="handleLogin" :loading="loading" class="login-btn">
                            {{ loading ? '登录中...' : '登 录' }}
                            <span class="btn-glow"></span>
                        </el-button>
                    </div>
                </el-form>

                <div class="footer">
                    <p>还没有账户？ <router-link to="/register" class="link">立即注册</router-link></p>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElNotification } from 'element-plus';
import { User, Lock } from '@element-plus/icons-vue';
import Loading from '@/components/loading.vue';
import axios from 'axios';
import { API_URLS, getHeaders } from '@/config/api';

const router = useRouter();
const loginFormRef = ref(null);
const loading = ref(false);

const loginForm = reactive({
    username: '',
    password: '',
});

const rules = {
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

const handleLogin = async () => {
    if (!loginFormRef.value) return;
    await loginFormRef.value.validate(async (valid) => {
        if (valid) {
            loading.value = true;
            try {
                const response = await axios.post(API_URLS.auth.login, loginForm, {
                    headers: getHeaders()
                });
                const token = response.data.token;
                const user = response.data.user;

                // Store token and user info
                localStorage.setItem('token', token);
                localStorage.setItem('user', JSON.stringify(user));

                // Set Authorization header for subsequent requests
                axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;

                ElNotification({
                    title: '成功',
                    message: '登录成功！',
                    type: 'success',
                    duration: 3000
                });
                router.push('/');
            } catch (error) {
                ElNotification({
                    title: '错误',
                    message: error.response?.data?.message || '登录失败，请检查您的凭证。',
                    type: 'error',
                });
            } finally {
                loading.value = false;
            }
        }
    });
};
</script>

<style scoped>
/* Container & Background */
.auth-container {
    position: relative;
    width: 100%;
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    background: #0f172a;
    /* Dark slate background */
    overflow: hidden;
    font-family: 'Inter', sans-serif;
}

/* Animated Background Circles */
.circles {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 1;
    overflow: hidden;
}

.circle {
    position: absolute;
    border-radius: 50%;
    filter: blur(80px);
    opacity: 0.6;
    animation: float 20s infinite ease-in-out;
}

.circle-1 {
    width: 500px;
    height: 500px;
    background: #4f46e5;
    /* Indigo */
    top: -100px;
    left: -100px;
    animation-delay: 0s;
}

.circle-2 {
    width: 400px;
    height: 400px;
    background: #ec4899;
    /* Pink */
    bottom: -50px;
    right: -50px;
    animation-delay: -5s;
}

.circle-3 {
    width: 300px;
    height: 300px;
    background: #06b6d4;
    /* Cyan */
    bottom: 20%;
    left: 20%;
    animation-delay: -10s;
}

@keyframes float {

    0%,
    100% {
        transform: translate(0, 0);
    }

    33% {
        transform: translate(30px, -50px) scale(1.1);
    }

    66% {
        transform: translate(-20px, 20px) scale(0.9);
    }
}

/* Glass Card */
.glass-card {
    position: relative;
    z-index: 10;
    width: 100%;
    max-width: 420px;
    padding: 3rem;
    background: rgba(255, 255, 255, 0.03);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 24px;
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
    animation: cardEntrance 0.8s cubic-bezier(0.2, 0.8, 0.2, 1);
}

@keyframes cardEntrance {
    from {
        opacity: 0;
        transform: translateY(30px) scale(0.95);
    }

    to {
        opacity: 1;
        transform: translateY(0) scale(1);
    }
}

/* Content Styling */
.title-section {
    text-align: center;
    margin-bottom: 2.5rem;
}

.main-title {
    font-size: 2rem;
    font-weight: 700;
    color: #fff;
    margin-bottom: 0.5rem;
    letter-spacing: -0.5px;
    background: linear-gradient(to right, #fff, #cbd5e1);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}

.sub-title {
    color: #94a3b8;
    font-size: 0.95rem;
}

/* Form Styling */
.custom-form {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.input-wrapper {
    position: relative;
    width: 100%;
}

:deep(.custom-input .el-input__wrapper) {
    background: rgba(15, 23, 42, 0.6) !important;
    border: 1px solid rgba(255, 255, 255, 0.1) !important;
    box-shadow: none !important;
    border-radius: 12px;
    padding: 8px 16px;
    transition: all 0.3s ease;
    height: 50px;
}

:deep(.custom-input .el-input__wrapper:hover) {
    border-color: rgba(255, 255, 255, 0.2) !important;
    background: rgba(15, 23, 42, 0.8) !important;
}

:deep(.custom-input .el-input__wrapper.is-focus) {
    border-color: #6366f1 !important;
    background: rgba(15, 23, 42, 0.9) !important;
    box-shadow: 0 0 0 1px rgba(99, 102, 241, 0.5) !important;
}

:deep(.custom-input .el-input__inner) {
    color: #fff !important;
    font-size: 1rem;
    height: 100%;
}

:deep(.custom-input .el-input__prefix) {
    color: #94a3b8;
    margin-right: 10px;
}

/* Button Styling */
.actions {
    margin-top: 1rem;
}

.login-btn {
    width: 100%;
    height: 54px;
    background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
    border: none;
    border-radius: 12px;
    color: #fff;
    font-size: 1rem;
    font-weight: 600;
    letter-spacing: 0.5px;
    position: relative;
    overflow: hidden;
    transition: all 0.3s ease;
    z-index: 1;
}

.login-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 20px -10px rgba(79, 70, 229, 0.5);
}

.login-btn:active {
    transform: translateY(1px);
}

.btn-glow {
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg,
            transparent,
            rgba(255, 255, 255, 0.2),
            transparent);
    transition: 0.5s;
}

.login-btn:hover .btn-glow {
    left: 100%;
    transition: 0.5s;
}

/* Footer Styling */
.footer {
    text-align: center;
    margin-top: 2rem;
    color: #94a3b8;
    font-size: 0.9rem;
}

.link {
    color: #818cf8;
    text-decoration: none;
    font-weight: 500;
    transition: color 0.2s;
}

.link:hover {
    color: #a5b4fc;
    text-decoration: underline;
}

/* Element Plus Overrides */
:deep(.el-form-item) {
    margin-bottom: 0;
}

:deep(.el-form-item__error) {
    padding-top: 6px;
    color: #f87171;
    font-size: 0.85rem;
}
</style>