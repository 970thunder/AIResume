<template>
  <div class="admin-login-container">
    <div class="login-card">
      <div class="header">
        <h2>Hyper Admin</h2>
        <p v-if="isInitMode">系统初始化 - 设置管理员账号</p>
        <p v-else>后台管理系统登录</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
        <template v-if="isInitMode">
          <el-form-item label="管理员用户名" prop="username">
            <el-input v-model="form.username" placeholder="设置用户名" />
          </el-form-item>
          <el-form-item label="管理员邮箱" prop="email">
            <el-input v-model="form.email" placeholder="设置邮箱" />
          </el-form-item>
        </template>
        
        <template v-else>
           <el-form-item label="用户名/邮箱" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名或邮箱" />
          </el-form-item>
        </template>

        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>

        <el-button type="primary" class="submit-btn" :loading="loading" @click="handleSubmit">
          {{ isInitMode ? '初始化系统' : '登录' }}
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { API_URLS } from '@/config/api';
import axios from 'axios';
import { ElMessage } from 'element-plus';

const router = useRouter();
const authStore = useAuthStore();
const formRef = ref(null);
const loading = ref(false);
const isInitMode = ref(false);

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

onMounted(async () => {
  try {
    const res = await axios.get(API_URLS.admin.checkInit);
    isInitMode.value = !res.data;
  } catch (error) {
    console.error('Failed to check admin init status', error);
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
          // Clear form but keep username for convenience? 
          // Actually better to clear password
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
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: radial-gradient(1200px 600px at 20% -20%, rgba(14, 165, 233, 0.12) 40%, var(--bg-primary) 100%);
  background-image: radial-gradient(rgba(255, 255, 255, 0.06) 1px, transparent 1px);
  background-size: 20px 20px;
}

.login-card {
  width: 420px;
  background: var(--glass-bg);
  padding: 40px;
  border-radius: 20px;
  box-shadow: var(--glass-shadow);
  border: 1px solid var(--glass-border);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
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

.header {
  text-align: center;
  margin-bottom: 32px;
}

.header h2 {
  font-size: 28px;
  color: var(--fg-primary);
  margin-bottom: 10px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header p {
  color: var(--fg-muted);
  font-size: 14px;
}

/* Form Styles */
:deep(.el-form-item__label) {
  color: var(--fg-primary) !important;
  font-weight: 500;
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

.submit-btn {
  width: 100%;
  margin-top: 20px;
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

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(14, 165, 233, 0.45) !important;
}

.submit-btn:active {
  transform: translateY(1px);
}
</style>
