<template>
    <div class="auth-container">
        <form class="form">
            <div class="title">创建账户<br><span>加入我们，开始您的简历之旅</span></div>
            <el-form :model="registerForm" :rules="rules" ref="registerFormRef">
                <el-form-item prop="username">
                    <el-input v-model="registerForm.username" placeholder="请输入用户名" :prefix-icon="User" class="input" />
                </el-form-item>

                <el-form-item prop="email">
                    <el-input v-model="registerForm.email" placeholder="请输入电子邮箱" :prefix-icon="Message" class="input" />
                </el-form-item>

                <el-form-item prop="verificationCode" class="verification-group">
                    <el-input v-model="registerForm.verificationCode" placeholder="请输入验证码" :prefix-icon="Key"
                        class="input" />
                    <el-button @click="sendVerificationCode" :loading="sending"
                        :disabled="cooldown > 0 || !registerForm.email" class="verification-btn">
                        {{ cooldown > 0 ? `${cooldown}秒` : '获取验证码' }}
                    </el-button>
                </el-form-item>

                <el-form-item prop="password">
                    <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" :prefix-icon="Lock"
                        show-password class="input" />
                </el-form-item>

                <el-form-item prop="confirmPassword">
                    <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码"
                        :prefix-icon="Lock" show-password class="input" />
                </el-form-item>

                <el-button @click="handleRegister" :loading="loading" class="button-confirm">
                    {{ loading ? '注册中...' : '注册 →' }}
                </el-button>
            </el-form>
            <div class="switch-auth">
                已有账户？ <router-link to="/login">返回登录</router-link>
            </div>
        </form>
    </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { ElMessage } from 'element-plus';
import { User, Lock, Message, Key } from '@element-plus/icons-vue';
import axios from 'axios';

const router = useRouter();
const authStore = useAuthStore();
const registerFormRef = ref(null);
const loading = ref(false);
const sending = ref(false);
const cooldown = ref(0);

const registerForm = reactive({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    verificationCode: ''
});

const validateEmail = (rule, value, callback) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (value === '') {
        callback(new Error('请输入电子邮箱'));
    } else if (!emailRegex.test(value)) {
        callback(new Error('请输入有效的电子邮箱地址'));
    } else {
        callback();
    }
};

const validatePass = (rule, value, callback) => {
    if (value === '') {
        callback(new Error('请输入密码'));
    } else if (value.length < 6) {
        callback(new Error('密码长度不能小于6位'));
    } else {
        if (registerForm.confirmPassword !== '') {
            registerFormRef.value?.validateField('confirmPassword');
        }
        callback();
    }
};

const validatePass2 = (rule, value, callback) => {
    if (value === '') {
        callback(new Error('请再次输入密码'));
    } else if (value !== registerForm.password) {
        callback(new Error('两次输入的密码不一致'));
    } else {
        callback();
    }
};

const validateVerificationCode = (rule, value, callback) => {
    if (value === '') {
        callback(new Error('请输入验证码'));
    } else if (!/^\d{6}$/.test(value)) {
        callback(new Error('请输入6位数字验证码'));
    } else {
        callback();
    }
};

const rules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 2, max: 20, message: '用户名长度应在2-20个字符之间', trigger: 'blur' }
    ],
    email: [
        { validator: validateEmail, trigger: 'blur' }
    ],
    password: [
        { validator: validatePass, trigger: 'blur' }
    ],
    confirmPassword: [
        { validator: validatePass2, trigger: 'blur' }
    ],
    verificationCode: [
        { validator: validateVerificationCode, trigger: 'blur' }
    ]
};

const startCooldown = () => {
    cooldown.value = 60;
    const timer = setInterval(() => {
        cooldown.value--;
        if (cooldown.value <= 0) {
            clearInterval(timer);
        }
    }, 1000);
};

const sendVerificationCode = async () => {
    try {
        await registerFormRef.value.validateField('email');
        sending.value = true;

        // 检查邮箱是否已注册
        const checkResponse = await axios.get('/api/auth/check-email', {
            params: { email: registerForm.email }
        });

        if (checkResponse.data.exists) {
            ElMessage.error('该邮箱已被注册，请直接登录或使用其他邮箱');
            return;
        }

        // 开始倒计时
        startCooldown();

        await axios.post('/api/auth/send-verification-code', null, {
            params: { email: registerForm.email }
        });
        ElMessage.success('验证码已发送到您的邮箱，请注意查收');
    } catch (error) {
        if (error?.message) {
            ElMessage.error(error.message);
            cooldown.value = 0;
        } else {
            ElMessage.error(error.response?.data || '发送验证码失败，请稍后重试');
        }
    } finally {
        sending.value = false;
    }
};

const handleRegister = async () => {
    if (!registerFormRef.value) return;

    try {
        await registerFormRef.value.validate();
        loading.value = true;

        await authStore.register({
            username: registerForm.username,
            email: registerForm.email,
            password: registerForm.password,
            verificationCode: registerForm.verificationCode
        });

        ElMessage.success('注册成功！');
        router.push('/login');
    } catch (error) {
        ElMessage.error(error.response?.data || '注册失败，请稍后重试');
    } finally {
        loading.value = false;
    }
};
</script>

<style scoped>
.auth-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    background-color: #f5f5f5;
    padding: 0;
    /* 移除内边距 */
    overflow: hidden;
    /* 隐藏溢出内容 */
}

.form {
    --input-focus: #2d8cf0;
    --font-color: #060606;
    --font-color-sub: #383838;
    --bg-color: #fff;
    --main-color: #09465d;
    padding: 20px;
    margin-top: -40px;
    background: rgb(220, 244, 251);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 20px;
    border-radius: 5px;
    border: 2px solid var(--main-color);
    box-shadow: 4px 4px var(--main-color);
    width: 330px;
}

.title {
    color: var(--font-color);
    font-weight: 900;
    font-size: 20px;
    margin-bottom: 25px;
    text-align: center;
}

.title span {
    color: var(--font-color-sub);
    font-weight: 600;
    font-size: 17px;
    display: block;
    margin-top: 8px;
}

:deep(.el-form) {
    width: 100%;
}

:deep(.el-form-item) {
    margin-bottom: 15px;
}

:deep(.el-input__wrapper) {
    width: 100%;
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

.verification-group {
    display: flex;
    gap: 12px;
}

.verification-group :deep(.el-form-item__content) {
    display: flex;
    gap: 12px;
}

.verification-group :deep(.el-input) {
    width: 60%;
}

.verification-btn {
    height: 40px;
    border-radius: 5px;
    border: 2px solid var(--main-color);
    background-color: var(--bg-color);
    box-shadow: 4px 4px var(--main-color);
    font-size: 14px;
    font-weight: 600;
    color: var(--font-color);
    cursor: pointer;
    white-space: nowrap;
    min-width: 100px;
    padding: 0 12px;
    flex: 1;
}

.verification-btn:active {
    box-shadow: 0px 0px var(--main-color);
    transform: translate(3px, 3px);
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
    margin: 15px auto 0 auto;
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
    margin-top: 15px;
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

:deep(.el-form-item__error) {
    color: #ff4949;
    font-weight: 500;
}
</style>