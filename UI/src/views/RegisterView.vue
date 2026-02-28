<template>
    <div class="auth-container">
        <!-- Enhanced Background Animation -->
        <div class="bg-grid"></div>
        <div class="circles">
            <div class="circle circle-1"></div>
            <div class="circle circle-2"></div>
            <div class="circle circle-3"></div>
        </div>
        <!-- Floating Particles -->
        <div class="particles">
            <div class="particle" v-for="n in 20" :key="n" :style="{ '--delay': n * 0.5 + 's', '--x': Math.random() * 100 + '%' }"></div>
        </div>
        <!-- Scan Line Effect -->
        <div class="scan-line"></div>

        <div class="glass-card">
            <div class="card-content">
                <div class="title-section">
                    <p class="sub-title">加入我们，开启智能简历之旅</p>
                </div>
                
                <el-form :model="registerForm" :rules="rules" ref="registerFormRef" class="custom-form">
                    <el-form-item prop="username">
                        <div class="input-wrapper">
                            <el-input 
                                v-model="registerForm.username" 
                                placeholder="用户名" 
                                :prefix-icon="User" 
                                class="custom-input" 
                            />
                        </div>
                    </el-form-item>

                    <el-form-item prop="email">
                        <div class="input-wrapper">
                            <el-input 
                                v-model="registerForm.email" 
                                placeholder="电子邮箱" 
                                :prefix-icon="Message" 
                                class="custom-input" 
                            />
                        </div>
                    </el-form-item>

                    <el-form-item prop="verificationCode" class="verification-group">
                        <div class="verify-row">
                            <div class="input-wrapper">
                                <el-input 
                                    v-model="registerForm.verificationCode" 
                                    placeholder="验证码" 
                                    :prefix-icon="Key"
                                    class="custom-input" 
                                />
                            </div>
                            <el-button 
                                @click="sendVerificationCode" 
                                :loading="sending"
                                :disabled="cooldown > 0 || !registerForm.email" 
                                class="verify-btn"
                            >
                                {{ cooldown > 0 ? `${cooldown}秒` : '获取验证码' }}
                            </el-button>
                        </div>
                    </el-form-item>

                    <el-form-item prop="password">
                        <div class="input-wrapper">
                            <el-input 
                                v-model="registerForm.password" 
                                type="password" 
                                placeholder="密码" 
                                :prefix-icon="Lock"
                                show-password 
                                class="custom-input" 
                            />
                        </div>
                    </el-form-item>

                    <el-form-item prop="confirmPassword">
                        <div class="input-wrapper">
                            <el-input 
                                v-model="registerForm.confirmPassword" 
                                type="password" 
                                placeholder="确认密码"
                                :prefix-icon="Lock" 
                                show-password 
                                class="custom-input" 
                            />
                        </div>
                    </el-form-item>

                    <div class="actions">
                        <el-button @click="handleRegister" :loading="loading" class="register-btn">
                            {{ loading ? '注册中...' : '注 册' }}
                            <span class="btn-glow"></span>
                        </el-button>
                    </div>
                </el-form>

                <div class="footer">
                    <p>已有账户？ <router-link to="/login" class="link">立即登录</router-link></p>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { ElMessage } from 'element-plus';
import { User, Lock, Message, Key } from '@element-plus/icons-vue';
import axios from 'axios';
import { API_URLS, getHeaders } from '@/config/api';

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

        const checkResponse = await axios.get(API_URLS.auth.checkEmail, {
            params: { email: registerForm.email },
            headers: getHeaders()
        });

        if (checkResponse.data.exists) {
            ElMessage.error('该邮箱已被注册，请直接登录或使用其他邮箱');
            return;
        }

        startCooldown();

        await axios.post(API_URLS.auth.sendVerificationCode, null, {
            params: { email: registerForm.email },
            headers: getHeaders()
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
/* Container & Background - Shared with Login */
.auth-container {
    position: relative;
    width: 100%;
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    background: #0f172a;
    overflow: hidden;
    font-family: 'Inter', sans-serif;
}

.circles {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 1;
    overflow: hidden;
    pointer-events: none;
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
    background: var(--accent-primary);
    top: -100px;
    right: -100px;
    animation-delay: 0s;
}

.circle-2 {
    width: 400px;
    height: 400px;
    background: var(--accent-secondary);
    bottom: -50px;
    left: -50px;
    animation-delay: -5s;
}

.circle-3 {
    width: 300px;
    height: 300px;
    background: #6366f1;
    top: 40%;
    left: 40%;
    animation-delay: -10s;
}

@keyframes float {
    0%, 100% { transform: translate(0, 0); }
    33% { transform: translate(30px, -50px) scale(1.1); }
    66% { transform: translate(-20px, 20px) scale(0.9); }
}

/* Glass Card */
.glass-card {
    position: relative;
    z-index: 10;
    width: 100%;
    max-width: 440px;
    padding: 1.75rem;
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

.title-section {
    text-align: center;
    margin-bottom: 1.25rem;
}

.main-title {
    font-size: 2rem;
    font-weight: 700;
    color: #fff;
    margin-bottom: 0.5rem;
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
    gap: 1rem;
}

.input-wrapper {
    position: relative;
    width: 100%;
}

:deep(.verification-group .verify-row) {
    display: flex;
    align-items: center;
    gap: 12px;
    width: 100%;
}

:deep(.verification-group .verify-row .input-wrapper) {
    flex: 1;
}

:deep(.custom-input .el-input__wrapper) {
    background: rgba(15, 23, 42, 0.6) !important;
    border: 1px solid rgba(255, 255, 255, 0.1) !important;
    box-shadow: none !important;
    border-radius: 12px;
    padding: 8px 16px;
    transition: all 0.3s ease;
    height: 42px;
}

:deep(.custom-input .el-input__wrapper:hover) {
    border-color: rgba(255, 255, 255, 0.2) !important;
    background: rgba(15, 23, 42, 0.8) !important;
}

:deep(.custom-input .el-input__wrapper.is-focus) {
    border-color: var(--accent-primary) !important;
    background: rgba(15, 23, 42, 0.9) !important;
    box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.2) !important;
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

/* Verification Button */
.verify-btn {
    height: 42px;
    padding: 0 16px;
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.1);
    color: #fff;
    font-weight: 600;
    transition: all 0.3s ease;
    white-space: nowrap;
    min-width: 100px;
}

.verify-btn:hover:not(:disabled) {
    background: rgba(255, 255, 255, 0.2);
    border-color: rgba(255, 255, 255, 0.3);
}

.verify-btn:disabled {
    background: rgba(255, 255, 255, 0.05);
    color: #64748b;
    border-color: rgba(255, 255, 255, 0.05);
}

/* Register Button */
.actions {
    margin-top: 0.5rem;
}

.register-btn {
    width: 100%;
    height: 48px;
    background: linear-gradient(135deg, var(--accent-primary) 0%, var(--accent-secondary) 100%);
    border: none;
    border-radius: 12px;
    color: var(--bg-primary);
    font-size: 1rem;
    font-weight: 600;
    letter-spacing: 0.5px;
    position: relative;
    overflow: hidden;
    transition: all 0.3s ease;
    z-index: 1;
    box-shadow: 0 0 20px rgba(14, 165, 233, 0.3);
}

.register-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 0 30px rgba(14, 165, 233, 0.5);
}

.register-btn:active {
    transform: translateY(1px);
}

.btn-glow {
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(
        90deg,
        transparent,
        rgba(255, 255, 255, 0.2),
        transparent
    );
    transition: 0.5s;
}

.register-btn:hover .btn-glow {
    left: 100%;
    transition: 0.5s;
}

/* Footer */
.footer {
    text-align: center;
    margin-top: 1.5rem;
    color: #94a3b8;
    font-size: 0.9rem;
}

.link {
    color: var(--accent-primary);
    text-decoration: none;
    font-weight: 500;
    transition: color 0.2s;
}

.link:hover {
    color: var(--accent-secondary);
    text-decoration: underline;
}

/* Element Plus Overrides */
:deep(.el-form-item) {
    margin-bottom: 0;
}

:deep(.el-form-item__error) {
    padding-top: 4px;
    color: #f87171;
    font-size: 0.85rem;
}
</style>
