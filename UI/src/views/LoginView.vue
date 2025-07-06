<template>
    <div class="auth-container">
        <form class="form">
            <div class="title">欢迎回来<br><span>登录您的账户</span></div>
            <el-form :model="loginForm" :rules="rules" ref="loginFormRef" @submit.prevent="handleLogin">
                <el-form-item prop="username">
                    <el-input v-model="loginForm.username" placeholder="用户名/邮箱" :prefix-icon="User"
                        class="input"></el-input>
                </el-form-item>
                <el-form-item prop="password">
                    <el-input v-model="loginForm.password" type="password" placeholder="密码" :prefix-icon="Lock"
                        show-password class="input" style="margin-top: 2vh;"></el-input>
                </el-form-item>

                <el-button @click="handleLogin" :loading="loading" class="button-confirm">
                    {{ loading ? '登录中...' : '登录 →' }}
                </el-button>
            </el-form>
            <div class="switch-auth">
                没有账户？ <router-link to="/register">立即注册</router-link>
            </div>
        </form>
    </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { ElNotification } from 'element-plus';
import { User, Lock } from '@element-plus/icons-vue';
import Loading from '@/component/loading.vue';

const router = useRouter();
const authStore = useAuthStore();
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
                await authStore.login(loginForm);
                ElNotification({
                    title: '成功',
                    message: '登录成功，欢迎回来！',
                    closeIcon: Loading
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
.auth-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    background-color: #f5f5f5;
}

.form {
    --input-focus: #2d8cf0;
    --font-color: #000000;
    --font-color-sub: #394056;
    --bg-color: #fff;
    --main-color: #09465d;
    padding: 20px;
    background: rgb(220, 244, 251);
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    justify-content: center;
    gap: 20px;
    border-radius: 5px;
    border: 2px solid var(--main-color);
    box-shadow: 4px 4px var(--main-color);
    min-width: 320px;
}

.title {
    color: var(--font-color);
    font-weight: 900;
    font-size: 20px;
    margin-bottom: 25px;
}

.title span {
    color: var(--font-color-sub);
    font-weight: 600;
    font-size: 17px;
}

:deep(.el-input__wrapper) {
    width: 250px;
    height: 40px;
    border-radius: 5px !important;
    border: 2px solid var(--main-color) !important;
    background-color: var(--bg-color) !important;
    box-shadow: 4px 4px var(--main-color) !important;
    font-size: 15px;
    font-weight: 600;
    color: var(--font-color);
    padding: 5px 10px;
    outline: none;
}

:deep(.el-input__wrapper.is-focus) {
    border: 2px solid var(--input-focus) !important;
}

.login-with {
    display: flex;
    gap: 20px;
    margin: 20px 0;
}

.button-log {
    cursor: pointer;
    width: 40px;
    height: 40px;
    border-radius: 100%;
    border: 2px solid var(--main-color);
    background-color: var(--bg-color);
    box-shadow: 4px 4px var(--main-color);
    color: var(--font-color);
    font-size: 25px;
    display: flex;
    justify-content: center;
    align-items: center;
}

.icon {
    width: 24px;
    height: 24px;
    fill: var(--main-color);
}

.button-log:active {
    box-shadow: 0px 0px var(--main-color);
    transform: translate(3px, 3px);
}

.button-confirm {
    margin: 20px auto 0 auto;
    width: 120px;
    height: 40px;
    border-radius: 5px;
    border: 2px solid var(--main-color);
    background-color: var(--bg-color);
    box-shadow: 4px 4px var(--main-color);
    font-size: 17px;
    font-weight: 600;
    color: var(--font-color);
    cursor: pointer;
}

.button-confirm:active {
    box-shadow: 0px 0px var(--main-color);
    transform: translate(3px, 3px);
}

.switch-auth {
    text-align: center;
    margin-top: 16px;
    color: var(--font-color-sub);
}

.switch-auth a {
    color: var(--input-focus);
    text-decoration: none;
    font-weight: 600;
}

.switch-auth a:hover {
    opacity: 0.8;
}

:deep(.el-form-item) {
    margin-bottom: 0;
}

:deep(.el-form-item__error) {
    color: #ff4949;
    font-weight: 500;
}

/* 添加全局样式 */
:deep(.custom-notification) {
    background: rgb(220, 244, 251);
    border: 2px solid var(--main-color);
    box-shadow: 4px 4px var(--main-color);
    padding: 10px 15px;
}

:deep(.custom-notification .el-notification__icon) {
    font-size: 24px;
    width: 24px;
    height: 24px;
    margin-right: 10px;
}

:deep(.custom-notification .el-notification__title) {
    color: var(--font-color);
    font-weight: 600;
}

:deep(.custom-notification .el-notification__content) {
    color: var(--font-color-sub);
}

:deep(.loader) {
    width: 24px;
    height: 24px;
    margin-right: 10px;
}
</style>