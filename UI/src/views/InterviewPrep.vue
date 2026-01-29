<template>
  <div class="interview-prep-container">
    <div class="header-section">
      <h1 class="page-title">面经八股 AI 模拟面试</h1>
      <p class="page-subtitle">基于您的简历分析，为您定制高频面试题，助您斩获 Offer</p>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- Main Content -->
    <div v-else class="content-wrapper">

      <!-- Stats Dashboard -->
      <div v-if="!sessionActive" class="dashboard-layout">

        <!-- Center Start Button -->
        <div class="center-action">
          <div class="start-btn-wrapper" @click="startInterview">
            <div class="pulse-ring"></div>
            <div class="start-btn">
              <el-icon :size="40">
                <VideoPlay />
              </el-icon>
              <span>开始面试</span>
            </div>
          </div>
        </div>

        <!-- Surrounding Cards -->
        <!-- Top Left: Total Questions -->
        <div class="orbit-card card-top-left">
          <div class="card-content">
            <div class="card-icon">
              <EditPen />
            </div>
            <div class="card-label">总答题数</div>
            <div class="card-value">{{ stats.totalQuestionsAnswered || 0 }}</div>
          </div>
        </div>

        <!-- Top Right: Accuracy -->
        <div class="orbit-card card-top-right">
          <div class="card-content">
            <div class="card-icon">
              <TrendCharts />
            </div>
            <div class="card-label">正确率</div>
            <div class="card-value">{{ (stats.accuracy || 0).toFixed(1) }}%</div>
          </div>
        </div>

        <!-- Bottom Left: Wrong Questions -->
        <div class="orbit-card card-bottom-left clickable" @click="showWrongQuestions = true">
          <div class="card-content">
            <div class="card-icon">
              <Warning />
            </div>
            <div class="card-label">错题集</div>
            <div class="card-value">{{ stats.wrongRecords ? stats.wrongRecords.length : 0 }}</div>
            <div class="card-hint">点击查看详情</div>
          </div>
        </div>

        <!-- Bottom Right: Resume Analysis -->
        <div class="orbit-card card-bottom-right clickable" @click="showAnalysis = true">
          <div class="card-content">
            <div class="card-icon">
              <Document />
            </div>
            <div class="card-label">简历分析</div>
            <div class="card-value">查看</div>
            <div class="card-hint">点击回顾分析</div>
          </div>
        </div>

      </div>

      <!-- Wrong Questions Dialog -->
      <el-dialog v-model="showWrongQuestions" title="错题集 / 待加强知识点" width="70%">
        <div v-if="stats.wrongRecords && stats.wrongRecords.length > 0">
          <el-collapse accordion>
            <el-collapse-item v-for="(record, index) in stats.wrongRecords" :key="record.id" :name="index">
              <template #title>
                <div class="wrong-question-title">
                  <el-tag type="danger" size="small" class="mr-2">错题</el-tag>
                  <span class="truncate-text">{{ record.questionContent }}</span>
                </div>
              </template>
              <div class="wrong-question-detail">
                <p><strong>您的回答：</strong> {{ record.userAnswer }}</p>
                <div class="ai-feedback">
                  <p><strong>AI 点评：</strong></p>
                  <div v-html="formatText(record.aiEvaluation)"></div>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
        <el-empty v-else description="暂无错题记录，继续保持！" />
      </el-dialog>

      <!-- Resume Analysis Dialog -->
      <el-dialog v-model="showAnalysis" title="简历分析报告" width="70%">
        <div v-if="latestAnalysis" class="analysis-content">
          <pre class="json-viewer">{{ formatAnalysis(latestAnalysis) }}</pre>
        </div>
        <el-empty v-else description="暂无分析记录，请先去简历分析页面进行分析" />
      </el-dialog>

      <!-- Interview Session -->
      <div v-if="sessionActive" class="interview-session">
        <div class="session-header">
          <el-button @click="exitSession" size="small">退出面试</el-button>
          <el-progress :percentage="progressPercentage" :format="progressFormat" class="session-progress" />
        </div>

        <el-card class="question-card" v-loading="submitting">
          <template #header>
            <div class="question-header">
              <el-tag>{{ currentQuestion.category || '综合' }}</el-tag>
              <el-tag :type="getDifficultyType(currentQuestion.difficulty)" class="ml-2">{{ currentQuestion.difficulty
                }}</el-tag>
              <span class="question-index">第 {{ currentQuestionIndex + 1 }} / {{ totalQuestions }} 题</span>
            </div>
          </template>

          <div class="question-content">
            <h3>{{ currentQuestion.content }}</h3>

            <!-- Multiple Choice Options -->
            <div v-if="currentQuestion.type === 'MULTIPLE_CHOICE'" class="options-list">
              <el-radio-group v-model="userAnswer" class="radio-group-vertical">
                <el-radio v-for="(option, idx) in currentQuestion.options" :key="idx" :label="option" border
                  class="option-item">
                  {{ option }}
                </el-radio>
              </el-radio-group>
            </div>

            <!-- Open Ended Input -->
            <div v-else class="text-answer">
              <el-input v-model="userAnswer" :rows="6" type="textarea" placeholder="请输入您的回答..." />
            </div>
          </div>

          <div class="question-footer">
            <el-button type="primary" @click="submitAndNext" :disabled="!userAnswer" :loading="submitting">
              {{ isLastQuestion ? '提交并结束' : '提交并下一题' }}
            </el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { EditPen, TrendCharts, VideoPlay, Warning, Document } from '@element-plus/icons-vue';
import { API_URLS, getHeaders } from '@/config/api';
import axios from 'axios';

// State
const loading = ref(true);
const stats = ref({});
const latestAnalysis = ref(null);
const sessionActive = ref(false);
const submitting = ref(false);
const showWrongQuestions = ref(false);
const showAnalysis = ref(false);

// Session State
const currentSession = ref(null);
const currentQuestionIndex = ref(0);
const userAnswer = ref('');

// Computed
const currentQuestion = computed(() => {
  if (!currentSession.value || !currentSession.value.questions) return {};
  return currentSession.value.questions[currentQuestionIndex.value];
});

const totalQuestions = computed(() => {
  return currentSession.value?.questions?.length || 10;
});

const isLastQuestion = computed(() => {
  return currentQuestionIndex.value >= totalQuestions.value - 1;
});

const progressPercentage = computed(() => {
  return ((currentQuestionIndex.value) / totalQuestions.value) * 100;
});

const progressFormat = () => {
  return `${currentQuestionIndex.value + 1}/${totalQuestions.value}`;
};

// Lifecycle
onMounted(() => {
  fetchData();
});

// Methods
const fetchData = async () => {
  loading.value = true;
  try {
    const [statsRes, analysisRes] = await Promise.all([
      axios.get(API_URLS.interview.stats, { headers: getHeaders() }),
      axios.get('/api/interview/analysis/latest', { headers: getHeaders() }).catch(() => ({ data: null }))
    ]);

    stats.value = statsRes.data;
    latestAnalysis.value = analysisRes.data;
  } catch (error) {
    console.error('Failed to fetch data:', error);
    ElMessage.error('获取数据失败');
  } finally {
    loading.value = false;
  }
};

const startInterview = async () => {
  loading.value = true;
  try {
    const response = await axios.post(API_URLS.interview.start, {}, { headers: getHeaders() });
    currentSession.value = response.data;
    currentQuestionIndex.value = 0;
    userAnswer.value = '';
    sessionActive.value = true;
  } catch (error) {
    console.error('Failed to start interview:', error);
    ElMessage.error('启动面试失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const submitAndNext = async () => {
  if (!userAnswer.value) return;

  const payload = {
    sessionId: currentSession.value.id,
    questionId: currentQuestion.value.id,
    answer: userAnswer.value
  };

  submitting.value = true;
  try {
    await axios.post(API_URLS.interview.submit, payload, { headers: getHeaders() });

    if (isLastQuestion.value) {
      finishSession();
    } else {
      currentQuestionIndex.value++;
      userAnswer.value = '';
      window.scrollTo(0, 0);
    }
  } catch (error) {
    console.error('Failed to submit answer:', error);
    ElMessage.error('提交失败，请重试');
  } finally {
    submitting.value = false;
  }
};

const finishSession = () => {
  ElMessageBox.alert('所有题目已提交！AI正在后台进行评分和分析，请稍后查看统计数据。', '面试完成', {
    confirmButtonText: '返回概览',
    callback: () => {
      sessionActive.value = false;
      fetchData(); // Refresh stats
    },
  });
};

const exitSession = () => {
  ElMessageBox.confirm(
    '确定要退出当前面试吗？进度将不会保存。',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    sessionActive.value = false;
    fetchData();
  }).catch(() => { });
};

const getDifficultyType = (difficulty) => {
  switch (difficulty) {
    case 'EASY': return 'success';
    case 'MEDIUM': return 'warning';
    case 'HARD': return 'danger';
    default: return 'info';
  }
};

const formatText = (text) => {
  if (!text) return '';
  return text.replace(/\n/g, '<br>');
};

const formatAnalysis = (jsonString) => {
  try {
    const obj = JSON.parse(jsonString);
    return JSON.stringify(obj, null, 2);
  } catch (e) {
    return jsonString;
  }
};
</script>

<style scoped>
.interview-prep-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  min-height: 80vh;
}

.dashboard-layout {
  position: relative;
  height: 600px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 40px;
}

/* Center Start Button */
.center-action {
  position: relative;
  z-index: 10;
}

.start-btn-wrapper {
  position: relative;
  width: 160px;
  height: 160px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.start-btn-wrapper:hover {
  transform: scale(1.05);
}

.start-btn {
  width: 140px;
  height: 140px;
  background: linear-gradient(135deg, #409eff, #3a8ee6);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  box-shadow: 0 10px 20px rgba(64, 158, 255, 0.4);
  position: relative;
  z-index: 2;
}

.start-btn span {
  margin-top: 8px;
  font-weight: bold;
  font-size: 16px;
}

.pulse-ring {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 2px solid #409eff;
  animation: pulse 2s infinite;
  z-index: 1;
}

@keyframes pulse {
  0% {
    transform: scale(0.9);
    opacity: 1;
  }

  100% {
    transform: scale(1.4);
    opacity: 0;
  }
}

/* Orbit Cards */
.orbit-card {
  position: absolute;
  width: 180px;
  height: 140px;
  background: white;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  transition: all 0.3s ease;
  border: 1px solid #ebeef5;
}

.orbit-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
  z-index: 5;
}

.clickable {
  cursor: pointer;
}

.clickable:hover {
  border-color: #409eff;
}

/* Irregular Shapes */
.card-top-left {
  top: 10%;
  left: 15%;
  border-radius: 40% 60% 70% 30% / 40% 50% 60% 50%;
  background: linear-gradient(135deg, #fff 0%, #f0f9ff 100%);
}

.card-top-right {
  top: 10%;
  right: 15%;
  border-radius: 60% 40% 30% 70% / 50% 40% 50% 60%;
  background: linear-gradient(135deg, #fff 0%, #f0fff4 100%);
}

.card-bottom-left {
  bottom: 10%;
  left: 15%;
  border-radius: 70% 30% 50% 50% / 60% 40% 60% 40%;
  background: linear-gradient(135deg, #fff 0%, #fff0f0 100%);
}

.card-bottom-right {
  bottom: 10%;
  right: 15%;
  border-radius: 30% 70% 60% 40% / 40% 60% 50% 50%;
  background: linear-gradient(135deg, #fff 0%, #fff9f0 100%);
}

.card-content {
  text-align: center;
}

.card-icon {
  font-size: 24px;
  margin-bottom: 8px;
  color: #606266;
}

.card-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.card-hint {
  font-size: 12px;
  color: #409eff;
  margin-top: 4px;
}

/* Dialog Styles */
.json-viewer {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  overflow: auto;
  max-height: 500px;
  font-family: monospace;
}

/* Session Styles */
.interview-session {
  max-width: 800px;
  margin: 0 auto;
}

.question-card {
  margin-top: 24px;
}

.question-header {
  display: flex;
  align-items: center;
}

.question-index {
  margin-left: auto;
  color: #909399;
  font-size: 14px;
}

.question-content {
  padding: 20px 0;
}

.question-content h3 {
  margin-bottom: 24px;
  line-height: 1.6;
  font-size: 18px;
}

.option-item {
  width: 100%;
  margin-bottom: 12px;
  margin-right: 0 !important;
}

.question-footer {
  margin-top: 24px;
  text-align: right;
}

.wrong-question-detail {
  padding: 12px;
  background: #fdf6ec;
  border-radius: 4px;
}

.ai-feedback {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #dcdfe6;
}

.truncate-text {
  display: inline-block;
  max-width: 400px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}
</style>