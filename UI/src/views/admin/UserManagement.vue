<template>
  <div class="user-management-container">
    <!-- Header -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">
          <el-icon><User /></el-icon>
          用户管理
        </h2>
        <p class="page-desc">管理系统用户和权限</p>
      </div>
      <div class="header-right">
        <el-button class="refresh-btn" @click="fetchUsers" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新列表
        </el-button>
      </div>
    </div>

    <!-- Stats Bar -->
    <div class="stats-bar">
      <div class="stat-item">
        <span class="stat-label">总用户数</span>
        <span class="stat-value">{{ users.length }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">管理员</span>
        <span class="stat-value admin-count">{{ adminCount }}</span>
      </div>
    </div>

    <!-- Table Card -->
    <div class="table-card">
      <el-table :data="users" style="width: 100%" v-loading="loading" class="custom-table">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" width="180">
          <template #default="scope">
            <div class="user-cell">
              <el-avatar :size="32" class="user-avatar">
                {{ scope.row.username?.charAt(0)?.toUpperCase() || 'U' }}
              </el-avatar>
              <span class="username">{{ scope.row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="220">
          <template #default="scope">
            <div class="email-cell">
              <el-icon><Message /></el-icon>
              <span>{{ scope.row.email || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="160">
          <template #default="scope">
            <div class="role-tags">
              <el-tag v-for="role in scope.row.roles" :key="role.id" :type="role.name === 'ROLE_ADMIN' ? 'danger' : ''" size="small" effect="dark">
                {{ role.name === 'ROLE_ADMIN' ? '管理员' : '用户' }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" align="center">
          <template #default="scope">
            <span class="time-text">{{ formatDate(scope.row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <div class="action-btns">
              <el-button size="small" class="action-btn edit" @click="handleEdit(scope.row)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button size="small" class="action-btn delete" @click="handleDelete(scope.row)" :disabled="isCurrentUser(scope.row)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Edit User Dialog -->
    <el-dialog v-model="editDialogVisible" title="编辑用户" width="440px" class="custom-dialog">
      <div class="edit-dialog-content">
        <div class="user-avatar-large">
          {{ editForm.username?.charAt(0)?.toUpperCase() || 'U' }}
        </div>
        <el-form :model="editForm" label-position="top" class="edit-form">
          <el-form-item label="用户名">
            <el-input v-model="editForm.username" disabled />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="editForm.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="角色">
            <el-radio-group v-model="editForm.role" class="role-radio">
              <el-radio label="ROLE_USER">
                <div class="radio-option">
                  <el-icon><User /></el-icon>
                  <span>普通用户</span>
                </div>
              </el-radio>
              <el-radio label="ROLE_ADMIN">
                <div class="radio-option">
                  <el-icon><UserFilled /></el-icon>
                  <span>管理员</span>
                </div>
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveUser">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import axios from 'axios';
import { API_URLS, getHeaders } from '@/config/api';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useAuthStore } from '@/stores/auth';
import { User, Refresh, Message, Edit, Delete, UserFilled } from '@element-plus/icons-vue';

const authStore = useAuthStore();
const users = ref([]);
const loading = ref(false);
const editDialogVisible = ref(false);
const editForm = reactive({
  id: null,
  username: '',
  email: '',
  role: 'ROLE_USER'
});

const adminCount = computed(() => {
  return users.value.filter(u => u.roles?.some(r => r.name === 'ROLE_ADMIN')).length;
});

onMounted(() => {
  fetchUsers();
});

const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const isCurrentUser = (row) => {
  return row.username === authStore.user?.username;
};

const fetchUsers = async () => {
  loading.value = true;
  try {
    const res = await axios.get(API_URLS.admin.users, { headers: getHeaders() });
    users.value = res.data;
  } catch (error) {
    ElMessage.error('获取用户列表失败');
  } finally {
    loading.value = false;
  }
};

const handleEdit = (row) => {
  editForm.id = row.id;
  editForm.username = row.username;
  editForm.email = row.email;
  const roles = row.roles ? row.roles.map(r => r.name) : [];
  editForm.role = roles.includes('ROLE_ADMIN') ? 'ROLE_ADMIN' : 'ROLE_USER';
  editDialogVisible.value = true;
};

const saveUser = async () => {
  try {
    const payload = {
      id: editForm.id,
      username: editForm.username,
      email: editForm.email,
      roles: [{ name: editForm.role }]
    };
    await axios.put(API_URLS.admin.user(editForm.id), payload, { headers: getHeaders() });
    ElMessage.success('更新成功');
    editDialogVisible.value = false;
    fetchUsers();
  } catch (error) {
    ElMessage.error('更新失败');
  }
};

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？此操作不可恢复。`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await axios.delete(API_URLS.admin.user(row.id), { headers: getHeaders() });
      ElMessage.success('删除成功');
      fetchUsers();
    } catch (error) {
      ElMessage.error('删除失败');
    }
  }).catch(() => {});
};
</script>

<style scoped>
.user-management-container {
  max-width: 1400px;
  margin: 0 auto;
}

/* Page Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #e2e8f0;
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.page-title .el-icon {
  color: #22d3ee;
  font-size: 24px;
}

.page-desc {
  color: #64748b;
  font-size: 14px;
  margin: 0;
}

.refresh-btn {
  background: rgba(14, 165, 233, 0.1);
  border: 1px solid rgba(14, 165, 233, 0.3);
  color: #22d3ee;
  font-weight: 500;
}

.refresh-btn:hover {
  background: rgba(14, 165, 233, 0.2);
  border-color: rgba(14, 165, 233, 0.5);
  color: #38bdf8;
}

/* Stats Bar */
.stats-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.1);
  border-radius: 10px;
  padding: 12px 20px;
}

.stat-label {
  color: #64748b;
  font-size: 13px;
}

.stat-value {
  color: #22d3ee;
  font-size: 18px;
  font-weight: 600;
}

.admin-count {
  color: #f87171;
}

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

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  background: linear-gradient(135deg, #0ea5e9, #8b5cf6);
  color: white;
  font-weight: 600;
  font-size: 14px;
}

.username {
  color: #e2e8f0;
  font-weight: 500;
}

.email-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #94a3b8;
}

.email-cell .el-icon {
  color: #64748b;
}

.role-tags {
  display: flex;
  gap: 6px;
}

.time-text {
  color: #64748b;
  font-size: 12px;
}

/* Action Buttons */
.action-btns {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.action-btn {
  padding: 6px 10px;
  font-size: 12px;
  border-radius: 6px;
}

.action-btn.edit {
  background: rgba(14, 165, 233, 0.1);
  border: 1px solid rgba(14, 165, 233, 0.3);
  color: #22d3ee;
}

.action-btn.edit:hover {
  background: rgba(14, 165, 233, 0.2);
}

.action-btn.delete {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  color: #f87171;
}

.action-btn.delete:hover:not(:disabled) {
  background: rgba(239, 68, 68, 0.2);
}

.action-btn.delete:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Dialog */
:deep(.el-dialog) {
  background: rgba(15, 23, 42, 0.95);
  border: 1px solid rgba(148, 163, 184, 0.1);
  border-radius: 16px;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid rgba(148, 163, 184, 0.1);
  padding: 20px 24px;
}

:deep(.el-dialog__title) {
  color: #e2e8f0;
  font-weight: 600;
}

:deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #64748b;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* Edit Dialog Content */
.edit-dialog-content {
  text-align: center;
}

.user-avatar-large {
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #0ea5e9, #8b5cf6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  font-size: 28px;
  font-weight: 700;
  color: white;
}

.edit-form {
  text-align: left;
}

.edit-form :deep(.el-form-item__label) {
  color: #94a3b8;
  font-weight: 500;
}

.edit-form :deep(.el-input__wrapper) {
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.2);
  color: #e2e8f0;
}

.edit-form :deep(.el-input__wrapper:hover) {
  border-color: rgba(14, 165, 233, 0.3);
}

.edit-form :deep(.el-input__wrapper.is-focus) {
  border-color: #0ea5e9;
}

.edit-form :deep(.el-input.is-disabled .el-input__wrapper) {
  background: rgba(30, 41, 59, 0.4);
}

.role-radio {
  display: flex;
  gap: 16px;
}

.role-radio :deep(.el-radio) {
  margin-right: 0;
}

.radio-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.radio-option .el-icon {
  font-size: 16px;
}

/* Responsive */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .stats-bar {
    flex-direction: column;
  }
}
</style>
