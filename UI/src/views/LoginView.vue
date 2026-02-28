<template>
  <div class="auth-container" ref="containerRef">
    <!-- Animated Background -->
    <div class="bg-effects">
      <!-- Gradient Orbs -->
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
      <div class="orb orb-4"></div>

      <!-- Grid Pattern -->
      <div class="bg-grid"></div>

      <!-- Floating Particles -->
      <div class="particles">
        <div class="particle" v-for="n in 40" :key="n" :style="getParticleStyle(n)"></div>
      </div>

      <!-- Scan Line -->
      <div class="scan-line"></div>

      <!-- Mouse Glow -->
      <div class="mouse-glow" :style="glowStyle"></div>
    </div>

    <!-- Login Card -->
    <div class="glass-card">
      <div class="card-glow"></div>
      <div class="card-border"></div>
      <div class="card-content">
        <!-- Logo -->
        <div class="logo-section">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="url(#loginGrad)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 17L12 22L22 17" stroke="url(#loginGrad)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 12L12 17L22 12" stroke="url(#loginGrad)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <defs>
                <linearGradient id="loginGrad" x1="2" y1="2" x2="22" y2="22">
                  <stop stop-color="#0ea5e9"/>
                  <stop offset="1" stop-color="#8b5cf6"/>
                </linearGradient>
              </defs>
            </svg>
          </div>
          <h1 class="main-title">AI 简历</h1>
        </div>
        <p class="sub-title">登录您的账户</p>

        <!-- Form -->
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef" @submit.prevent="handleLogin" class="custom-form">
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" placeholder="用户名 / 邮箱" size="large">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="密码" size="large" show-password>
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-button type="primary" @click="handleLogin" :loading="loading" class="login-btn">
            <span v-if="!loading">登 录</span>
            <span v-else>登录中...</span>
            <el-icon v-if="!loading" class="btn-icon"><ArrowRight /></el-icon>
          </el-button>
        </el-form>

        <!-- Footer -->
        <div class="footer">
          <p>还没有账户？ <router-link to="/register" class="link">立即注册</router-link></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElNotification } from 'element-plus';
import { User, Lock, ArrowRight } from '@element-plus/icons-vue';
import axios from 'axios';
import { API_URLS, getHeaders } from '@/config/api';

const router = useRouter();
const containerRef = ref(null);
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

// Particle styles
const getParticleStyle = (n) => {
  const size = Math.random() * 3 + 1;
  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${Math.random() * 100}%`,
    top: `${Math.random() * 100}%`,
    animationDelay: `${Math.random() * 20}s`,
    animationDuration: `${Math.random() * 30 + 20}s`
  };
};

// Mouse glow effect
const glowStyle = ref({ left: '-200px', top: '-200px', opacity: 0 });

const handleMouseMove = (e) => {
  if (containerRef.value) {
    const rect = containerRef.value.getBoundingClientRect();
    glowStyle.value = {
      left: `${e.clientX - rect.left - 200}px`,
      top: `${e.clientY - rect.top - 200}px`,
      opacity: 0.15
    };
  }
};

const handleMouseLeave = () => {
  glowStyle.value.opacity = 0;
};

onMounted(() => {
  if (containerRef.value) {
    containerRef.value.addEventListener('mousemove', handleMouseMove);
    containerRef.value.addEventListener('mouseleave', handleMouseLeave);
  }
});

onUnmounted(() => {
  if (containerRef.value) {
    containerRef.value.removeEventListener('mousemove', handleMouseMove);
    containerRef.value.removeEventListener('mouseleave', handleMouseLeave);
  }
});

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

        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(user));
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
/* Container */
.auth-container {
  position: relative;
  width: 100%;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: radial-gradient(1200px 600px at 20% -20%, #0b1220 40%, #0f172a 100%);
  overflow: hidden;
}

/* Background Effects */
.bg-effects {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

/* Floating Orbs */
.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  animation: floatOrb 20s ease-in-out infinite;
}

.orb-1 {
  width: 500px;
  height: 500px;
  background: rgba(14, 165, 233, 0.12);
  top: -150px;
  left: -150px;
}

.orb-2 {
  width: 400px;
  height: 400px;
  background: rgba(139, 92, 246, 0.1);
  bottom: -100px;
  right: -100px;
  animation-delay: -7s;
}

.orb-3 {
  width: 300px;
  height: 300px;
  background: rgba(34, 211, 238, 0.08);
  top: 40%;
  right: 10%;
  animation-delay: -14s;
}

.orb-4 {
  width: 250px;
  height: 250px;
  background: rgba(16, 185, 129, 0.08);
  bottom: 20%;
  left: 10%;
  animation-delay: -3s;
}

@keyframes floatOrb {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(40px, -40px) scale(1.1); }
  66% { transform: translate(-30px, 30px) scale(0.9); }
}

/* Grid Pattern */
.bg-grid {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image:
    linear-gradient(rgba(148, 163, 184, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(148, 163, 184, 0.03) 1px, transparent 1px);
  background-size: 60px 60px;
}

/* Particles */
.particles {
  position: absolute;
  width: 100%;
  height: 100%;
}

.particle {
  position: absolute;
  background: rgba(14, 165, 233, 0.6);
  border-radius: 50%;
  animation: floatParticle linear infinite;
}

@keyframes floatParticle {
  0%, 100% { transform: translateY(0) translateX(0); opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { transform: translateY(-100vh) translateX(30px); opacity: 0; }
}

/* Scan Line */
.scan-line {
  position: absolute;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(14, 165, 233, 0.3), transparent);
  animation: scan 8s linear infinite;
}

@keyframes scan {
  0% { top: -2px; opacity: 0; }
  5% { opacity: 1; }
  95% { opacity: 1; }
  100% { top: 100%; opacity: 0; }
}

/* Mouse Glow */
.mouse-glow {
  position: absolute;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(14, 165, 233, 0.2) 0%, transparent 70%);
  border-radius: 50%;
  transition: opacity 0.3s ease;
}

/* Glass Card */
.glass-card {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 420px;
  animation: cardEntry 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes cardEntry {
  0% { opacity: 0; transform: translateY(40px) scale(0.95); }
  100% { opacity: 1; transform: translateY(0) scale(1); }
}

.card-glow {
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.3), rgba(139, 92, 246, 0.3), rgba(34, 211, 238, 0.3));
  border-radius: 28px;
  filter: blur(20px);
  opacity: 0.4;
  animation: glowPulse 4s ease-in-out infinite;
}

@keyframes glowPulse {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.02); }
}

.card-border {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 26px;
  padding: 1px;
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.5), rgba(139, 92, 246, 0.3), rgba(34, 211, 238, 0.5));
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
}

.card-content {
  background: rgba(15, 23, 42, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 26px;
  padding: 48px 40px;
  position: relative;
}

/* Logo Section */
.logo-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  margin-bottom: 8px;
}

.logo-icon {
  width: 40px;
  height: 40px;
}

.logo-icon svg {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 4px 12px rgba(14, 165, 233, 0.4));
}

.main-title {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #0ea5e9 0%, #22d3ee 50%, #8b5cf6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  letter-spacing: -0.5px;
}

.sub-title {
  text-align: center;
  color: #64748b;
  font-size: 14px;
  margin-bottom: 36px;
}

/* Form */
.custom-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.custom-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.custom-form :deep(.el-form-item__error) {
  padding-top: 6px;
  color: #f87171;
  font-size: 12px;
}

.custom-form :deep(.el-input__wrapper) {
  background: rgba(30, 41, 59, 0.8) !important;
  border: 1px solid rgba(148, 163, 184, 0.1) !important;
  box-shadow: none !important;
  border-radius: 12px !important;
  padding: 4px 16px !important;
  height: 52px;
  transition: all 0.3s ease !important;
}

.custom-form :deep(.el-input__wrapper:hover) {
  border-color: rgba(14, 165, 233, 0.3) !important;
  background: rgba(30, 41, 59, 0.9) !important;
}

.custom-form :deep(.el-input__wrapper.is-focus) {
  border-color: #0ea5e9 !important;
  box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.15), inset 0 0 0 1px rgba(14, 165, 233, 0.1) !important;
}

.custom-form :deep(.el-input__inner) {
  color: #e2e8f0 !important;
  font-size: 15px;
}

.custom-form :deep(.el-input__inner::placeholder) {
  color: #475569 !important;
}

.custom-form :deep(.el-input__prefix) {
  color: #64748b;
}

.custom-form :deep(.el-input__suffix) {
  color: #64748b;
}

/* Login Button */
.login-btn {
  width: 100%;
  height: 52px;
  margin-top: 8px;
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 50%, #8b5cf6 100%) !important;
  border: none !important;
  color: #ffffff !important;
  font-weight: 600 !important;
  font-size: 16px !important;
  border-radius: 12px !important;
  box-shadow: 0 8px 32px rgba(14, 165, 233, 0.35) !important;
  transition: all 0.3s ease !important;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  position: relative;
  overflow: hidden;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(14, 165, 233, 0.45) !important;
}

.login-btn:active {
  transform: translateY(0);
}

.btn-icon {
  font-size: 16px;
  transition: transform 0.3s ease;
}

.login-btn:hover .btn-icon {
  transform: translateX(4px);
}

/* Footer */
.footer {
  text-align: center;
  margin-top: 28px;
  color: #64748b;
  font-size: 14px;
}

.link {
  color: #22d3ee;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.link:hover {
  color: #38bdf8;
  text-decoration: underline;
}

/* Responsive */
@media (max-width: 480px) {
  .glass-card {
    margin: 16px;
  }

  .card-content {
    padding: 36px 28px;
  }

  .main-title {
    font-size: 24px;
  }
}
</style>
