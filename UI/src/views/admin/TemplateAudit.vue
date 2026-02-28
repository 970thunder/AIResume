<template>
  <div class="template-audit-container">
    <!-- Header -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">
          <el-icon><Files /></el-icon>
          模板审核
        </h2>
        <p class="page-desc">审核用户提交的简历模板</p>
      </div>
      <div class="header-right">
        <el-button class="refresh-btn" @click="fetchTemplates" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新列表
        </el-button>
      </div>
    </div>

    <!-- Stats Bar -->
    <div class="stats-bar">
      <div class="stat-item">
        <span class="stat-label">待审核</span>
        <span class="stat-value">{{ templates.length }}</span>
      </div>
    </div>

    <!-- Table Card -->
    <div class="table-card">
      <el-table :data="templates" style="width: 100%" v-loading="loading" class="custom-table">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="模板名称" min-width="180">
          <template #default="scope">
            <div class="template-name">
              <el-icon class="template-icon"><Document /></el-icon>
              <span>{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="author.username" label="作者" width="140">
          <template #default="scope">
            <div class="author-cell">
              <el-avatar :size="28" class="author-avatar">
                {{ scope.row.author?.username?.charAt(0)?.toUpperCase() || 'U' }}
              </el-avatar>
              <span>{{ scope.row.author?.username || '未知' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.price > 0 ? 'warning' : 'success'" size="small" effect="dark">
              {{ scope.row.price > 0 ? '￥' + scope.row.price : '免费' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="180" align="center">
          <template #default="scope">
            <span class="time-text">{{ formatDate(scope.row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" align="center">
          <template #default="scope">
            <div class="action-btns">
              <el-button size="small" class="action-btn preview" @click="previewTemplate(scope.row)">
                <el-icon><View /></el-icon>
                预览
              </el-button>
              <el-button size="small" class="action-btn approve" @click="handleAudit(scope.row, true)">
                <el-icon><Check /></el-icon>
                通过
              </el-button>
              <el-button size="small" class="action-btn reject" @click="handleAudit(scope.row, false)">
                <el-icon><Close /></el-icon>
                驳回
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- Empty State -->
      <div v-if="!loading && templates.length === 0" class="empty-state">
        <el-icon class="empty-icon"><FolderOpened /></el-icon>
        <p>暂无待审核模板</p>
      </div>
    </div>

    <!-- Audit Dialog -->
    <el-dialog v-model="auditDialogVisible" :title="auditAction ? '确认通过' : '确认驳回'" width="440px" class="custom-dialog">
      <div class="dialog-content">
        <div class="dialog-icon" :class="auditAction ? 'success' : 'danger'">
          <el-icon v-if="auditAction"><CircleCheckFilled /></el-icon>
          <el-icon v-else><CircleCloseFilled /></el-icon>
        </div>
        <p class="dialog-title">
          {{ auditAction ? '通过' : '驳回' }}模板：{{ currentTemplate?.name }}
        </p>
        <el-input v-if="!auditAction" v-model="auditReason" type="textarea" :rows="3" placeholder="请输入驳回理由（必填）" class="reason-input" />
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="auditDialogVisible = false">取消</el-button>
          <el-button :type="auditAction ? 'success' : 'danger'" @click="confirmAudit" :disabled="!auditAction && !auditReason">
            确认{{ auditAction ? '通过' : '驳回' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Preview Dialog -->
    <el-dialog v-model="previewVisible" title="模板预览" width="85%" top="3vh" class="preview-dialog">
      <div v-if="currentTemplate" class="preview-container">
        <iframe :srcdoc="currentTemplate.htmlContent" frameborder="0" width="100%" height="650px"></iframe>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { API_URLS, getHeaders } from '@/config/api';
import { ElMessage } from 'element-plus';
import { Files, Refresh, Document, View, Check, Close, FolderOpened, CircleCheckFilled, CircleCloseFilled } from '@element-plus/icons-vue';

const templates = ref([]);
const loading = ref(false);
const auditDialogVisible = ref(false);
const previewVisible = ref(false);
const currentTemplate = ref(null);
const auditAction = ref(true);
const auditReason = ref('');

onMounted(() => {
  fetchTemplates();
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

const fetchTemplates = async () => {
  loading.value = true;
  try {
    const res = await axios.get(API_URLS.admin.pendingTemplates, { headers: getHeaders() });
    templates.value = res.data;
  } catch (error) {
    ElMessage.error('获取待审核模板失败');
  } finally {
    loading.value = false;
  }
};

const previewTemplate = (row) => {
  currentTemplate.value = row;
  previewVisible.value = true;
};

const handleAudit = (row, approved) => {
  currentTemplate.value = row;
  auditAction.value = approved;
  auditReason.value = '';
  auditDialogVisible.value = true;
};

const confirmAudit = async () => {
  if (!auditAction.value && !auditReason.value) {
    ElMessage.warning('请输入驳回理由');
    return;
  }
  try {
    await axios.post(API_URLS.admin.auditTemplate(currentTemplate.value.id), {
      approved: auditAction.value,
      reason: auditReason.value
    }, { headers: getHeaders() });

    ElMessage.success('审核操作成功');
    auditDialogVisible.value = false;
    fetchTemplates();
  } catch (error) {
    ElMessage.error('操作失败');
  }
};
</script>

<style scoped>
.template-audit-container {
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
  color: #f59e0b;
  font-size: 18px;
  font-weight: 600;
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

:deep(.el-table td.el-table__cell) {
  font-size: 13px;
}

.template-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.template-icon {
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
  font-size: 12px;
  font-weight: 600;
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
  padding: 6px 12px;
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

.action-btn.preview:hover {
  background: rgba(14, 165, 233, 0.2);
}

.action-btn.approve {
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.3);
  color: #34d399;
}

.action-btn.approve:hover {
  background: rgba(16, 185, 129, 0.2);
}

.action-btn.reject {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  color: #f87171;
}

.action-btn.reject:hover {
  background: rgba(239, 68, 68, 0.2);
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #64748b;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #475569;
}

/* Dialog */
:deep(.custom-dialog) {
  border-radius: 16px;
}

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

.dialog-content {
  text-align: center;
}

.dialog-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  font-size: 32px;
}

.dialog-icon.success {
  background: rgba(16, 185, 129, 0.15);
  color: #34d399;
}

.dialog-icon.danger {
  background: rgba(239, 68, 68, 0.15);
  color: #f87171;
}

.dialog-title {
  color: #e2e8f0;
  font-size: 15px;
  margin-bottom: 16px;
}

.reason-input :deep(.el-textarea__inner) {
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.2);
  color: #e2e8f0;
  border-radius: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* Preview Dialog */
:deep(.preview-dialog .el-dialog__body) {
  padding: 16px;
}

.preview-container {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

/* Responsive */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .action-btns {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
