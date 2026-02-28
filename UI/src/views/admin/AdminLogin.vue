<template>
  <div class="admin-login-container" ref="loginRef">
    <!-- Animated Background -->
    <div class="bg-particles">
      <div v-for="n in 50" :key="n" class="bg-particle" :style="getParticleStyle(n)"></div>
    </div>

    <!-- Floating Orbs -->
    <div class="floating-orbs">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
    </div>

    <!-- Grid Pattern -->
    <div class="grid-pattern"></div>

    <!-- Login Card -->
    <div class="login-card">
      <div class="card-glow"></div>
      <div class="card-border"></div>

      <div class="card-content">
        <!-- Logo -->
        <div class="logo-section">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="url(#logoGrad)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 17L12 22L22 17" stroke="url(#logoGrad)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 12L12 17L22 12" stroke="url(#logoGrad)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <defs>
                <linearGradient id="logoGrad" x1="2" y1="2" x2="22" y2="22">
                  <stop stop-color="#0ea5e9"/>
                  <stop offset="1" stop-color="#8b5cf6"/>
                </linearGradient>
              </defs>
            </svg>
          </div>
          <h2 class="logo-text">Hyper Admin</h2>
        </div>

        <p class="subtitle">
          <span v-if="isInitMode">系统初始化 - 设置管理员账号</span>
          <span v-else>后台管理系统登录</span>
        </p>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
          <template v-if="isInitMode">
            <el-form-item label="管理员用户名" prop="username">
              <el-input v-model="form.username" placeholder="设置用户名">
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="管理员邮箱" prop="email">
              <el-input v-model="form.email" placeholder="设置邮箱">
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </template>

          <template v-else>
            <el-form-item label="用户名/邮箱" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名或邮箱">
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </template>

          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password>
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-button type="primary" class="submit-btn" :loading="loading" @click="handleSubmit">
            <span v-if="!loading">{{ isInitMode ? '初始化系统' : '登 录' }}</span>
            <span v-else>处理中...</span>
            <el-icon v-if="!loading" class="btn-icon"><ArrowRight /></el-icon>
          </el-button>
        </el-form>

        <div class="footer-links">
          <a href="/" class="back-link">
            <el-icon><Back /></el-icon>
            返回前台首页
          </a>
        </div>
      </div>
    </div>

    <!-- Mouse Glow -->
    <div class="mouse-glow" :style="glowStyle"></div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { API_URLS } from '@/config/api';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { User, Lock, Message, ArrowRight, Back } from '@element-plus/icons-vue';

const router = useRouter();
const authStore = useAuthStore();
const formRef = ref(null);
const loading = ref(false);
const isInitMode = ref(false);
const loginRef = ref(null);

const form = reactive({
  username: '',
  email: '',
  password: ''
});

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
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
  if (loginRef.value) {
    const rect = loginRef.value.getBoundingClientRect();
    glowStyle.value = {
      left: `${e.clientX - rect.left - 200}px`,
      top: `${e.clientY - rect.top - 200}px`,
      opacity: 0.15
    };
  }
};

onMounted(async () => {
  try {
    const res = await axios.get(API_URLS.admin.checkInit);
    isInitMode.value = !res.data;
  } catch (error) {
    console.error('Failed to check admin init status', error);
  }

  if (loginRef.value) {
    loginRef.value.addEventListener('mousemove', handleMouseMove);
  }
});

onUnmounted(() => {
  if (loginRef.value) {
    loginRef.value.removeEventListener('mousemove', handleMouseMove);
  }
});

const handleSubmit = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        if (isInitMode.value) {
          await axios.post(API_URLS.admin.init, form);
          ElMessage.success('系统初始化成功，请登录');
          isInitMode.value = false;
          form.password = '';
        } else {
          await authStore.login({
            username: form.username,
            password: form.password
          });
          if (authStore.isAdmin) {
            ElMessage.success('登录成功');
            router.push('/admin/dashboard');
          } else {
            ElMessage.error('该账号没有管理员权限');
            authStore.logout();
          }
        }
      } catch (error) {
        ElMessage.error(error.response?.data?.message || (isInitMode.value ? '初始化失败' : '登录失败'));
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.admin-login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: radial-gradient(1200px 600px at 20% -20%, #0b1220 40%, #0f172a 100%);
  position: relative;
  overflow: hidden;
}

/* Background Particles */
.bg-particles {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.bg-particle {
  position: absolute;
  background: rgba(14, 165, 233, 0.6);
  border-radius: 50%;
  animation: floatParticle linear infinite;
}

@keyframes floatParticle {
  0%, 100% {
    transform: translateY(0) translateX(0);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100vh) translateX(50px);
    opacity: 0;
  }
}

/* Floating Orbs */
.floating-orbs {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  animation: floatOrb 20s ease-in-out infinite;
}

.orb-1 {
  width: 400px;
  height: 400px;
  background: rgba(14, 165, 233, 0.15);
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.orb-2 {
  width: 300px;
  height: 300px;
  background: rgba(139, 92, 246, 0.12);
  bottom: -50px;
  right: -50px;
  animation-delay: -7s;
}

.orb-3 {
  width: 250px;
  height: 250px;
  background: rgba(34, 211, 238, 0.1);
  top: 50%;
  right: 20%;
  animation-delay: -14s;
}

@keyframes floatOrb {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -30px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
}

/* Grid Pattern */
.grid-pattern {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image:
    linear-gradient(rgba(148, 163, 184, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(148, 163, 184, 0.03) 1px, transparent 1px);
  background-size: 60px 60px;
  pointer-events: none;
  z-index: 0;
}

/* Mouse Glow */
.mouse-glow {
  position: fixed;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(14, 165, 233, 0.2) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
  z-index: 1;
  transition: opacity 0.3s ease;
}

/* Login Card */
.login-card {
  width: 420px;
  position: relative;
  z-index: 10;
  animation: cardEntry 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes cardEntry {
  0% {
    opacity: 0;
    transform: translateY(40px) scale(0.95);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.card-glow {
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.3), rgba(139, 92, 246, 0.3), rgba(34, 211, 238, 0.3));
  border-radius: 24px;
  filter: blur(20px);
  opacity: 0.5;
  animation: glowPulse 4s ease-in-out infinite;
}

@keyframes glowPulse {
  0%, 100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.02);
  }
}

.card-border {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 22px;
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
  border-radius: 22px;
  padding: 48px 40px;
  position: relative;
}

/* Logo Section */
.logo-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  margin-bottom: 12px;
}

.logo-icon {
  width: 42px;
  height: 42px;
}

.logo-icon svg {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 4px 12px rgba(14, 165, 233, 0.4));
}

.logo-text {
  font-size: 26px;
  font-weight: 700;
  background: linear-gradient(135deg, #0ea5e9 0%, #22d3ee 50%, #8b5cf6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  letter-spacing: -0.5px;
}

.subtitle {
  text-align: center;
  color: #64748b;
  font-size: 14px;
  margin-bottom: 32px;
}

/* Form Styles */
:deep(.el-form-item__label) {
  color: #94a3b8 !important;
  font-weight: 500;
  font-size: 13px;
  padding-bottom: 8px;
}

:deep(.el-input__wrapper) {
  background: rgba(30, 41, 59, 0.8) !important;
  border: 1px solid rgba(148, 163, 184, 0.1) !important;
  box-shadow: none !important;
  border-radius: 12px !important;
  transition: all 0.3s ease !important;
  padding: 4px 16px !important;
  height: 48px;
}

:deep(.el-input__wrapper:hover) {
  border-color: rgba(14, 165, 233, 0.3) !important;
  background: rgba(30, 41, 59, 0.9) !important;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #0ea5e9 !important;
  box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.15), inset 0 0 0 1px rgba(14, 165, 233, 0.1) !important;
}

:deep(.el-input__inner) {
  color: #e2e8f0 !important;
  font-size: 14px;
}

:deep(.el-input__inner::placeholder) {
  color: #475569 !important;
}

:deep(.el-input__prefix) {
  color: #64748b;
}

:deep(.el-input__suffix) {
  color: #64748b;
}

/* Submit Button */
.submit-btn {
  width: 100%;
  margin-top: 24px;
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 50%, #8b5cf6 100%) !important;
  border: none !important;
  color: white !important;
  font-weight: 600 !important;
  border-radius: 12px !important;
  height: 50px !important;
  font-size: 15px !important;
  box-shadow: 0 8px 32px rgba(14, 165, 233, 0.35) !important;
  transition: all 0.3s ease !important;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(14, 165, 233, 0.45) !important;
}

.submit-btn:active {
  transform: translateY(0);
}

.btn-icon {
  font-size: 16px;
  transition: transform 0.3s ease;
}

.submit-btn:hover .btn-icon {
  transform: translateX(4px);
}

/* Footer Links */
.footer-links {
  margin-top: 24px;
  text-align: center;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  text-decoration: none;
  font-size: 13px;
  transition: all 0.3s ease;
  padding: 8px 16px;
  border-radius: 8px;
}

.back-link:hover {
  color: #22d3ee;
  background: rgba(14, 165, 233, 0.1);
}

/* Responsive */
@media (max-width: 480px) {
  .login-card {
    width: calc(100% - 32px);
    margin: 16px;
  }

  .card-content {
    padding: 32px 24px;
  }

  .logo-text {
    font-size: 22px;
  }
}
</style>
