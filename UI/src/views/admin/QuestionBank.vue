<template>
  <div class="question-bank-container">
    <el-card>
      <template #header>
        <div class="header-actions">
          <h3>题库管理</h3>
          <div>
            <el-button type="success" @click="handleAiGenerate">AI 生成题目</el-button>
            <el-button type="primary" @click="handleAdd">新增题目</el-button>
          </div>
        </div>
      </template>

      <!-- Filters? For now just simple table -->
      <el-table :data="questions" style="width: 100%" v-loading="loading" height="calc(100vh - 200px)">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="content" label="题目内容" show-overflow-tooltip min-width="300" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.type === 'MULTIPLE_CHOICE' ? 'info' : 'success'">
              {{ scope.row.type === 'MULTIPLE_CHOICE' ? '选择题' : '问答题' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="100">
          <template #default="scope">
            <el-tag :type="getDifficultyType(scope.row.difficulty)">{{ scope.row.difficulty }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column label="统计 (正确/总数)" width="150">
          <template #default="scope">
            <span>{{ scope.row.correctCount || 0 }} / {{ scope.row.totalAttempts || 0 }}</span>
            <el-progress 
              :percentage="calculateAccuracy(scope.row)" 
              :status="getAccuracyStatus(scope.row)"
              :stroke-width="6"
              :show-text="false"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- AI Generate Dialog -->
    <el-dialog v-model="aiDialogVisible" title="AI 批量生成题目" width="500px">
      <el-form :model="aiForm" label-width="100px">
        <el-form-item label="生成数量">
          <el-input-number v-model="aiForm.count" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="技术/分类">
          <el-input v-model="aiForm.category" placeholder="例如：Java, Redis, Spring Boot" />
        </el-form-item>
      </el-form>
      <div class="ai-tip">
        <el-alert title="AI 生成可能需要较长时间，请耐心等待。" type="info" show-icon :closable="false" />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="aiDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="aiLoading" @click="confirmAiGenerate">开始生成</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Edit/Add Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑题目' : '新增题目'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="题目内容">
          <el-input v-model="form.content" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type">
            <el-option label="问答题" value="OPEN_ENDED" />
            <el-option label="选择题" value="MULTIPLE_CHOICE" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="form.difficulty">
            <el-option label="简单" value="EASY" />
            <el-option label="中等" value="MEDIUM" />
            <el-option label="困难" value="HARD" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="form.category" placeholder="例如：Java, Redis" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="逗号分隔" />
        </el-form-item>
        
        <template v-if="form.type === 'MULTIPLE_CHOICE'">
          <el-form-item label="选项">
            <el-input v-model="optionsJson" type="textarea" :rows="4" placeholder='JSON array: ["Option A", "Option B"]' />
            <span class="text-gray-400 text-xs">请输入 JSON 数组格式的选项</span>
          </el-form-item>
        </template>

        <el-form-item label="参考答案">
          <el-input v-model="form.referenceAnswer" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveQuestion">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import axios from 'axios';
import { API_URLS, getHeaders } from '@/config/api';
import { ElMessage, ElMessageBox } from 'element-plus';

const questions = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const isEdit = ref(false);
const optionsJson = ref('[]');

// AI Generate
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
  ElMessageBox.confirm('确定要删除该题目吗？', '警告', {
    type: 'warning'
  }).then(async () => {
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
  try {
    // Validate JSON if multiple choice
    if (form.type === 'MULTIPLE_CHOICE') {
      try {
        JSON.parse(optionsJson.value);
      } catch (e) {
        ElMessage.error('选项必须是有效的 JSON 格式');
        return;
      }
    }

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

const getDifficultyType = (diff) => {
  switch (diff) {
    case 'EASY': return 'success';
    case 'MEDIUM': return 'warning';
    case 'HARD': return 'danger';
    default: return 'info';
  }
};
</script>

<style scoped>
.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.ai-tip {
  margin-bottom: 20px;
}
</style>
