<template>
  <div class="hyper-admin-container" ref="containerRef">
    <!-- Background Effects -->
    <div class="bg-effects">
      <div class="bg-gradient"></div>
      <div class="bg-grid"></div>
      <div class="bg-particles">
        <div v-for="n in 30" :key="n" class="particle" :style="getParticleStyle(n)"></div>
      </div>
    </div>

    <!-- Login Form -->
    <div v-if="!isLoggedInAsAdmin" class="login-wrapper">
      <div class="login-card">
        <div class="card-glow"></div>
        <div class="card-border"></div>
        <div class="card-content">
          <div class="logo-section">
            <div class="logo-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="url(#grad1)" stroke-width="2"/>
                <path d="M2 17L12 22L22 17" stroke="url(#grad1)" stroke-width="2"/>
                <path d="M2 12L12 17L22 12" stroke="url(#grad1)" stroke-width="2"/>
                <defs>
                  <linearGradient id="grad1" x1="2" y1="2" x2="22" y2="22">
                    <stop stop-color="#0ea5e9"/>
                    <stop offset="1" stop-color="#8b5cf6"/>
                  </linearGradient>
                </defs>
              </svg>
            </div>
            <h2 class="logo-text">Hyper Admin</h2>
          </div>
          <p class="login-subtitle">超级管理员访问入口</p>

          <el-form :model="loginForm" @submit.prevent="handleLogin" class="login-form">
            <el-form-item>
              <el-input v-model="loginForm.username" placeholder="Username" size="large">
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-input v-model="loginForm.password" type="password" placeholder="Password" size="large" show-password>
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleLogin" :loading="loading" class="login-button">
                <span v-if="!loading">Sign In</span>
                <span v-else>Signing In...</span>
                <el-icon v-if="!loading" class="btn-icon"><ArrowRight /></el-icon>
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>

    <!-- Admin Panel -->
    <div v-else class="admin-panel">
      <div class="panel-header">
        <div class="header-left">
          <h1 class="panel-title">
            <el-icon><Setting /></el-icon>
            模板审核后台
          </h1>
          <p class="panel-desc">超级管理员面板</p>
        </div>
        <div class="header-right">
          <el-button class="refresh-btn" @click="fetchTemplates" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
          <el-button class="logout-btn" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            登出
          </el-button>
        </div>
      </div>

      <!-- Stats -->
      <div class="stats-row">
        <div class="stat-card">
          <span class="stat-label">总模板</span>
          <span class="stat-value">{{ templates.length }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">待审核</span>
          <span class="stat-value pending">{{ pendingCount }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">已通过</span>
          <span class="stat-value approved">{{ approvedCount }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">已拒绝</span>
          <span class="stat-value rejected">{{ rejectedCount }}</span>
        </div>
      </div>

      <!-- Table -->
      <div class="table-card">
        <el-table :data="templates" v-loading="loading" style="width: 100%" class="custom-table">
          <el-table-column prop="id" label="ID" width="80" align="center" />
          <el-table-column prop="name" label="模板名称" min-width="180">
            <template #default="scope">
              <div class="template-name">
                <el-icon><Document /></el-icon>
                <span>{{ scope.row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="author.username" label="作者" width="140">
            <template #default="scope">
              <div class="author-cell">
                <el-avatar :size="26" class="author-avatar">
                  {{ scope.row.author?.username?.charAt(0)?.toUpperCase() || 'U' }}
                </el-avatar>
                <span>{{ scope.row.author?.username || '-' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="提交时间" width="180" align="center">
            <template #default="scope">
              <span class="time-text">{{ formatDate(scope.row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="scope">
              <span class="status-badge" :class="getStatusClass(scope.row.status)">
                {{ getStatusLabel(scope.row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280" align="center">
            <template #default="scope">
              <div class="action-btns">
                <el-button size="small" class="action-btn preview" @click="goToAuditor(scope.row.id)">
                  <el-icon><View /></el-icon>
                  预览
                </el-button>
                <el-button v-if="scope.row.status === 'PENDING'" size="small" class="action-btn approve" @click="updateStatus(scope.row.id, 'APPROVED')">
                  <el-icon><Check /></el-icon>
                  通过
                </el-button>
                <el-button v-if="scope.row.status === 'PENDING' || scope.row.status === 'APPROVED'" size="small" class="action-btn reject" @click="updateStatus(scope.row.id, 'REJECTED')">
                  <el-icon><Close /></el-icon>
                  拒绝
                </el-button>
                <el-button v-if="scope.row.status === 'REJECTED'" size="small" class="action-btn reapprove" @click="updateStatus(scope.row.id, 'PENDING')">
                  <el-icon><RefreshRight /></el-icon>
                  重审
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { ElNotification } from 'element-plus';
import { User, Lock, SwitchButton, ArrowRight, Setting, Refresh, RefreshRight, Document, View, Check, Close } from '@element-plus/icons-vue';
import axios from 'axios';
import { API_URLS, getHeaders } from '@/config/api';

const authStore = useAuthStore();
const router = useRouter();
const loading = ref(false);
const templates = ref([]);
const isLoggedInAsAdmin = ref(false);
const containerRef = ref(null);

const loginForm = reactive({ username: 'xnrHyper', password: '' });

const pendingCount = computed(() => templates.value.filter(t => t.status === 'PENDING').length);
const approvedCount = computed(() => templates.value.filter(t => t.status === 'APPROVED').length);
const rejectedCount = computed(() => templates.value.filter(t => t.status === 'REJECTED').length);

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

const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const getStatusClass = (status) => {
  switch (status) {
    case 'PENDING': return 'pending';
    case 'APPROVED': return 'approved';
    case 'REJECTED': return 'rejected';
    default: return '';
  }
};

const getStatusLabel = (status) => {
  switch (status) {
    case 'PENDING': return '待审核';
    case 'APPROVED': return '已通过';
    case 'REJECTED': return '已拒绝';
    default: return status;
  }
};

const handleLogin = async () => {
  loading.value = true;
  try {
    const res = await axios.post(API_URLS.auth.login, loginForm);
    if (res.data.user?.roles?.some(r => r.name === 'ROLE_ADMIN')) {
      isLoggedInAsAdmin.value = true;
      authStore.setAuth(res.data.token, res.data.user);
      fetchTemplates();
      ElNotification({ title: '成功', message: '登录成功', type: 'success' });
    } else {
      ElNotification({ title: '错误', message: '非管理员账号', type: 'error' });
    }
  } catch (error) {
    ElNotification({ title: '错误', message: '登录失败', type: 'error' });
  } finally {
    loading.value = false;
  }
};

const handleLogout = () => {
  isLoggedInAsAdmin.value = false;
  authStore.logout();
  templates.value = [];
};

const fetchTemplates = async () => {
  loading.value = true;
  try {
    const res = await axios.get(API_URLS.admin.pendingTemplates, { headers: getHeaders() });
    templates.value = res.data;
  } catch (error) {
    ElNotification({ title: '错误', message: '获取数据失败', type: 'error' });
  } finally {
    loading.value = false;
  }
};

const goToAuditor = (id) => {
  router.push(`/admin/templates?id=${id}`);
};

const updateStatus = async (id, status) => {
  try {
    await axios.post(API_URLS.admin.auditTemplate(id), { approved: status === 'APPROVED' }, { headers: getHeaders() });
    ElNotification({ title: '成功', message: '状态已更新', type: 'success' });
    fetchTemplates();
  } catch (error) {
    ElNotification({ title: '错误', message: '操作失败', type: 'error' });
  }
};

onMounted(() => {
  if (authStore.isAdmin) {
    isLoggedInAsAdmin.value = true;
    fetchTemplates();
  }
});
</script>

<style scoped>
.hyper-admin-container {
  min-height: 100vh;
  background: radial-gradient(1200px 600px at 20% -20%, #0b1220 40%, #0f172a 100%);
  position: relative;
  overflow-x: hidden;
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

.bg-gradient {
  position: absolute;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 30% 20%, rgba(14, 165, 233, 0.08) 0%, transparent 50%);
}

.bg-grid {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image:
    linear-gradient(rgba(148, 163, 184, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(148, 163, 184, 0.03) 1px, transparent 1px);
  background-size: 60px 60px;
}

.bg-particles {
  position: absolute;
  width: 100%;
  height: 100%;
}

.particle {
  position: absolute;
  background: rgba(14, 165, 233, 0.5);
  border-radius: 50%;
  animation: floatParticle linear infinite;
}

@keyframes floatParticle {
  0%, 100% { transform: translateY(0); opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { transform: translateY(-100vh); opacity: 0; }
}

/* Login Wrapper */
.login-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  position: relative;
  z-index: 1;
}

.login-card {
  width: 400px;
  position: relative;
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
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.3), rgba(139, 92, 246, 0.3));
  border-radius: 24px;
  filter: blur(20px);
  opacity: 0.4;
  animation: glowPulse 4s ease-in-out infinite;
}

@keyframes glowPulse {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.5; }
}

.card-border {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 22px;
  padding: 1px;
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.5), rgba(139, 92, 246, 0.3));
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
}

.card-content {
  background: rgba(15, 23, 42, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 22px;
  padding: 40px;
  position: relative;
}

.logo-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 8px;
}

.logo-icon {
  width: 36px;
  height: 36px;
}

.logo-icon svg {
  width: 100%;
  height: 100%;
}

.logo-text {
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(135deg, #0ea5e9, #22d3ee, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.login-subtitle {
  text-align: center;
  color: #64748b;
  font-size: 13px;
  margin-bottom: 32px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.el-input__wrapper) {
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.1);
  box-shadow: none;
  border-radius: 10px;
  height: 48px;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: rgba(14, 165, 233, 0.3);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: #0ea5e9;
  box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.1);
}

.login-form :deep(.el-input__inner) {
  color: #e2e8f0;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: #475569;
}

.login-button {
  width: 100%;
  background: linear-gradient(135deg, #0ea5e9, #8b5cf6) !important;
  border: none !important;
  color: white !important;
  font-weight: 600 !important;
  border-radius: 10px !important;
  height: 48px !important;
  font-size: 15px !important;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(14, 165, 233, 0.3);
}

.btn-icon {
  transition: transform 0.3s ease;
}

.login-button:hover .btn-icon {
  transform: translateX(4px);
}

/* Admin Panel */
.admin-panel {
  padding: 32px;
  max-width: 1400px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #e2e8f0;
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.panel-title .el-icon {
  color: #22d3ee;
}

.panel-desc {
  color: #64748b;
  font-size: 14px;
  margin: 0;
}

.header-right {
  display: flex;
  gap: 12px;
}

.refresh-btn {
  background: rgba(14, 165, 233, 0.1);
  border: 1px solid rgba(14, 165, 233, 0.3);
  color: #22d3ee;
}

.refresh-btn:hover {
  background: rgba(14, 165, 233, 0.2);
}

.logout-btn {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  color: #f87171;
}

.logout-btn:hover {
  background: rgba(239, 68, 68, 0.2);
}

/* Stats Row */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.1);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-label {
  color: #64748b;
  font-size: 13px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #e2e8f0;
}

.stat-value.pending { color: #fbbf24; }
.stat-value.approved { color: #34d399; }
.stat-value.rejected { color: #f87171; }

/* Table Card */
.table-card {
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.1);
  border-radius: 16px;
  padding: 20px;
  backdrop-filter: blur(12px);
}

/* Custom Table */
:deep(.custom-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(30, 41, 59, 0.5);
  --el-table-row-hover-bg-color: rgba(14, 165, 233, 0.08);
  --el-table-border-color: rgba(148, 163, 184, 0.1);
  --el-table-text-color: #94a3b8;
  --el-table-header-text-color: #e2e8f0;
}

:deep(.el-table th.el-table__cell) {
  font-weight: 600;
  font-size: 13px;
}

.template-name {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #e2e8f0;
}

.template-name .el-icon {
  color: #22d3ee;
}

.author-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-avatar {
  background: linear-gradient(135deg, #0ea5e9, #8b5cf6);
  color: white;
  font-size: 11px;
  font-weight: 600;
}

.time-text {
  color: #64748b;
  font-size: 12px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.pending {
  background: rgba(245, 158, 11, 0.15);
  color: #fbbf24;
}

.status-badge.approved {
  background: rgba(16, 185, 129, 0.15);
  color: #34d399;
}

.status-badge.rejected {
  background: rgba(239, 68, 68, 0.15);
  color: #f87171;
}

/* Action Buttons */
.action-btns {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: wrap;
}

.action-btn {
  padding: 5px 10px;
  font-size: 12px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-btn.preview {
  background: rgba(14, 165, 233, 0.1);
  border: 1px solid rgba(14, 165, 233, 0.3);
  color: #22d3ee;
}

.action-btn.approve {
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.3);
  color: #34d399;
}

.action-btn.reject {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  color: #f87171;
}

.action-btn.reapprove {
  background: rgba(245, 158, 11, 0.1);
  border: 1px solid rgba(245, 158, 11, 0.3);
  color: #fbbf24;
}

.action-btn:hover {
  filter: brightness(1.2);
}

/* Responsive */
@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .panel-header {
    flex-direction: column;
    gap: 16px;
  }

  .action-btns {
    flex-direction: column;
  }
}
</style>
