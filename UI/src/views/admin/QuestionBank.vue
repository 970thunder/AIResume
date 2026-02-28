<template>
  <div class="question-bank-container">
    <!-- Header -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">
          <el-icon><List /></el-icon>
          题库管理
        </h2>
        <p class="page-desc">管理面试题目，支持 AI 智能生成</p>
      </div>
      <div class="header-right">
        <el-button class="ai-btn" @click="handleAiGenerate">
          <el-icon><MagicStick /></el-icon>
          AI 生成
        </el-button>
        <el-button class="add-btn" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增题目
        </el-button>
      </div>
    </div>

    <!-- Stats Bar -->
    <div class="stats-bar">
      <div class="stat-item">
        <span class="stat-label">总题目数</span>
        <span class="stat-value">{{ questions.length }}</span>
      </div>
    </div>

    <!-- Table Card -->
    <div class="table-card">
      <el-table :data="questions" style="width: 100%" v-loading="loading" class="custom-table" height="calc(100vh - 280px)">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="content" label="题目内容" min-width="280" show-overflow-tooltip>
          <template #default="scope">
            <div class="question-content">
              {{ scope.row.content }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.type === 'MULTIPLE_CHOICE' ? '' : 'success'" size="small" effect="dark">
              {{ scope.row.type === 'MULTIPLE_CHOICE' ? '选择题' : '问答题' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="90" align="center">
          <template #default="scope">
            <span class="difficulty-badge" :class="getDifficultyClass(scope.row.difficulty)">
              {{ getDifficultyLabel(scope.row.difficulty) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120">
          <template #default="scope">
            <el-tag size="small" effect="plain">{{ scope.row.category || '未分类' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="正确率" width="140" align="center">
          <template #default="scope">
            <div class="accuracy-cell">
              <span class="accuracy-text">{{ scope.row.correctCount || 0 }}/{{ scope.row.totalAttempts || 0 }}</span>
              <el-progress :percentage="calculateAccuracy(scope.row)" :status="getAccuracyStatus(scope.row)" :stroke-width="4" :show-text="false" />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <div class="action-btns">
              <el-button size="small" class="action-btn edit" @click="handleEdit(scope.row)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button size="small" class="action-btn delete" @click="handleDelete(scope.row)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- AI Generate Dialog -->
    <el-dialog v-model="aiDialogVisible" title="AI 批量生成题目" width="480px" class="custom-dialog">
      <div class="dialog-content">
        <div class="ai-icon">
          <el-icon><MagicStick /></el-icon>
        </div>
        <p class="ai-desc">AI 将根据指定的技术栈自动生成面试题目</p>
        <el-form :model="aiForm" label-position="top" class="ai-form">
          <el-form-item label="生成数量">
            <el-input-number v-model="aiForm.count" :min="1" :max="20" style="width: 100%" />
          </el-form-item>
          <el-form-item label="技术/分类">
            <el-input v-model="aiForm.category" placeholder="例如：Java, Redis, Spring Boot" />
          </el-form-item>
        </el-form>
        <div class="ai-tip">
          <el-icon><InfoFilled /></el-icon>
          <span>AI 生成可能需要较长时间，请耐心等待</span>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="aiDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="aiLoading" @click="confirmAiGenerate">开始生成</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Edit/Add Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑题目' : '新增题目'" width="560px" class="custom-dialog">
      <el-form :model="form" label-position="top" class="edit-form">
        <el-form-item label="题目内容" required>
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入题目内容" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="类型">
              <el-select v-model="form.type" style="width: 100%">
                <el-option label="问答题" value="OPEN_ENDED" />
                <el-option label="选择题" value="MULTIPLE_CHOICE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度">
              <el-select v-model="form.difficulty" style="width: 100%">
                <el-option label="简单" value="EASY" />
                <el-option label="中等" value="MEDIUM" />
                <el-option label="困难" value="HARD" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类">
              <el-input v-model="form.category" placeholder="例如：Java" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标签">
              <el-input v-model="form.tags" placeholder="逗号分隔" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="form.type === 'MULTIPLE_CHOICE'" label="选项 (JSON 格式)">
          <el-input v-model="optionsJson" type="textarea" :rows="3" placeholder='["选项A", "选项B", "选项C", "选项D"]' />
        </el-form-item>
        <el-form-item label="参考答案">
          <el-input v-model="form.referenceAnswer" type="textarea" :rows="4" placeholder="请输入参考答案" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveQuestion">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import axios from 'axios';
import { API_URLS, getHeaders } from '@/config/api';
import { ElMessage, ElMessageBox } from 'element-plus';
import { List, Plus, MagicStick, Edit, Delete, InfoFilled } from '@element-plus/icons-vue';

const questions = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const isEdit = ref(false);
const optionsJson = ref('[]');

const aiDialogVisible = ref(false);
const aiLoading = ref(false);
const aiForm = reactive({
  count: 5,
  category: ''
});

const form = reactive({
  id: null,
  content: '',
  type: 'OPEN_ENDED',
  options: '[]',
  referenceAnswer: '',
  difficulty: 'MEDIUM',
  category: '',
  tags: ''
});

watch(optionsJson, (val) => {
  form.options = val;
});

onMounted(() => {
  fetchQuestions();
});

const fetchQuestions = async () => {
  loading.value = true;
  try {
    const res = await axios.get(API_URLS.admin.questions, { headers: getHeaders() });
    questions.value = res.data;
  } catch (error) {
    ElMessage.error('获取题目列表失败');
  } finally {
    loading.value = false;
  }
};

const handleAiGenerate = () => {
  aiForm.count = 5;
  aiForm.category = '';
  aiDialogVisible.value = true;
};

const confirmAiGenerate = async () => {
  if (!aiForm.category) {
    ElMessage.warning('请输入分类/技术栈');
    return;
  }
  aiLoading.value = true;
  try {
    await axios.post(API_URLS.admin.generateQuestions, aiForm, { headers: getHeaders() });
    ElMessage.success('AI 生成题目成功');
    aiDialogVisible.value = false;
    fetchQuestions();
  } catch (error) {
    ElMessage.error('生成失败，请稍后重试');
  } finally {
    aiLoading.value = false;
  }
};

const calculateAccuracy = (row) => {
  if (!row.totalAttempts || row.totalAttempts === 0) return 0;
  return Math.round((row.correctCount / row.totalAttempts) * 100);
};

const getAccuracyStatus = (row) => {
  const acc = calculateAccuracy(row);
  if (acc >= 80) return 'success';
  if (acc >= 60) return 'warning';
  return 'exception';
};

const getDifficultyClass = (diff) => {
  switch (diff) {
    case 'EASY': return 'easy';
    case 'MEDIUM': return 'medium';
    case 'HARD': return 'hard';
    default: return '';
  }
};

const getDifficultyLabel = (diff) => {
  switch (diff) {
    case 'EASY': return '简单';
    case 'MEDIUM': return '中等';
    case 'HARD': return '困难';
    default: return diff;
  }
};

const handleAdd = () => {
  isEdit.value = false;
  resetForm();
  dialogVisible.value = true;
};

const handleEdit = (row) => {
  isEdit.value = true;
  Object.assign(form, row);
  optionsJson.value = row.options || '[]';
  dialogVisible.value = true;
};

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该题目吗？', '警告', { type: 'warning' }).then(async () => {
    try {
      await axios.delete(API_URLS.admin.question(row.id), { headers: getHeaders() });
      ElMessage.success('删除成功');
      fetchQuestions();
    } catch (error) {
      ElMessage.error('删除失败');
    }
  });
};

const saveQuestion = async () => {
  if (!form.content) {
    ElMessage.warning('请输入题目内容');
    return;
  }
  if (form.type === 'MULTIPLE_CHOICE') {
    try {
      JSON.parse(optionsJson.value);
    } catch (e) {
      ElMessage.error('选项必须是有效的 JSON 格式');
      return;
    }
  }
  try {
    if (isEdit.value) {
      await axios.put(API_URLS.admin.question(form.id), form, { headers: getHeaders() });
    } else {
      await axios.post(API_URLS.admin.questions, form, { headers: getHeaders() });
    }
    ElMessage.success('保存成功');
    dialogVisible.value = false;
    fetchQuestions();
  } catch (error) {
    ElMessage.error('保存失败');
  }
};

const resetForm = () => {
  form.id = null;
  form.content = '';
  form.type = 'OPEN_ENDED';
  form.options = '[]';
  optionsJson.value = '[]';
  form.referenceAnswer = '';
  form.difficulty = 'MEDIUM';
  form.category = '';
  form.tags = '';
};
</script>

<style scoped>
.question-bank-container {
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

.header-right {
  display: flex;
  gap: 12px;
}

.ai-btn {
  background: rgba(139, 92, 246, 0.1);
  border: 1px solid rgba(139, 92, 246, 0.3);
  color: #a78bfa;
  font-weight: 500;
}

.ai-btn:hover {
  background: rgba(139, 92, 246, 0.2);
  color: #c4b5fd;
}

.add-btn {
  background: rgba(14, 165, 233, 0.1);
  border: 1px solid rgba(14, 165, 233, 0.3);
  color: #22d3ee;
  font-weight: 500;
}

.add-btn:hover {
  background: rgba(14, 165, 233, 0.2);
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

.question-content {
  color: #e2e8f0;
  line-height: 1.5;
}

.difficulty-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.difficulty-badge.easy {
  background: rgba(16, 185, 129, 0.15);
  color: #34d399;
}

.difficulty-badge.medium {
  background: rgba(245, 158, 11, 0.15);
  color: #fbbf24;
}

.difficulty-badge.hard {
  background: rgba(239, 68, 68, 0.15);
  color: #f87171;
}

.accuracy-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.accuracy-text {
  font-size: 12px;
  color: #64748b;
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

.action-btn.delete:hover {
  background: rgba(239, 68, 68, 0.2);
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

/* AI Dialog */
.ai-icon {
  width: 56px;
  height: 56px;
  background: rgba(139, 92, 246, 0.15);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  font-size: 28px;
  color: #a78bfa;
}

.ai-desc {
  text-align: center;
  color: #94a3b8;
  font-size: 14px;
  margin-bottom: 20px;
}

.ai-form :deep(.el-form-item__label) {
  color: #94a3b8;
}

.ai-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(14, 165, 233, 0.1);
  border-radius: 8px;
  color: #64748b;
  font-size: 13px;
}

.ai-tip .el-icon {
  color: #22d3ee;
}

/* Edit Form */
.edit-form :deep(.el-form-item__label) {
  color: #94a3b8;
  font-weight: 500;
}

.edit-form :deep(.el-input__wrapper),
.edit-form :deep(.el-textarea__inner) {
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.2);
  color: #e2e8f0;
}

.edit-form :deep(.el-input__wrapper:hover),
.edit-form :deep(.el-textarea__inner:hover) {
  border-color: rgba(14, 165, 233, 0.3);
}

.edit-form :deep(.el-input__wrapper.is-focus),
.edit-form :deep(.el-textarea__inner:focus) {
  border-color: #0ea5e9;
}
</style>
