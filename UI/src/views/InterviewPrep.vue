<template>
  <div class="interview-prep-container">
    <div class="header-section">
      <h1 class="page-title">面经八股 AI 模拟面试</h1>
      <p class="page-subtitle">基于您的简历分析，为您定制高频面试题，助您斩获 Offer</p>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
      <div class="loading-text" v-if="!sessionActive">
        <el-icon class="is-loading">
          <Loading />
        </el-icon>
        <span>题目正在定制中，请稍候...</span>
      </div>
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
      <el-dialog v-model="showAnalysis" title="简历分析报告" width="70%" class="analysis-dialog">
        <div v-if="analysisData" class="analysis-report">
          <!-- Summary -->
          <div class="report-section">
            <h3 class="section-title">综合概览</h3>
            <div class="section-content summary-text">
              {{ analysisData.summary }}
            </div>
          </div>

          <!-- JD Analysis -->
          <div v-if="analysisData.jdAnalysis" class="report-section">
            <div class="flex-header">
              <h3 class="section-title">岗位匹配分析</h3>
              <el-tag :type="getScoreType(analysisData.jdAnalysis.matchScore)" effect="dark">
                匹配度: {{ analysisData.jdAnalysis.matchScore }}%
              </el-tag>
            </div>

            <div class="analysis-sub-section">
              <h4>匹配关键词</h4>
              <div class="tags-wrapper">
                <el-tag v-for="tag in analysisData.jdAnalysis.matchingKeywords" :key="tag" type="success"
                  class="mr-2 mb-2">
                  {{ tag }}
                </el-tag>
              </div>
            </div>

            <div class="analysis-sub-section" v-if="analysisData.jdAnalysis.missingKeywords?.length">
              <h4>缺失/需加强关键词</h4>
              <div class="tags-wrapper">
                <el-tag v-for="tag in analysisData.jdAnalysis.missingKeywords" :key="tag" type="info" class="mr-2 mb-2">
                  {{ tag }}
                </el-tag>
              </div>
            </div>

            <div class="analysis-sub-section bg-gray">
              <h4>差距分析</h4>
              <p>{{ analysisData.jdAnalysis.gapAnalysis }}</p>
            </div>
          </div>

          <!-- Advice -->
          <div v-if="analysisData.advice?.length" class="report-section">
            <h3 class="section-title">改进建议</h3>
            <ul class="advice-list">
              <li v-for="(item, index) in analysisData.advice" :key="index">
                {{ item }}
              </li>
            </ul>
          </div>
        </div>

        <div v-else-if="latestAnalysis" class="raw-json">
          <pre class="json-viewer">{{ formatAnalysis(latestAnalysis) }}</pre>
        </div>

        <el-empty v-else description="暂无分析记录，请先去简历分析页面进行分析" />
      </el-dialog>

      <!-- Interview Session -->
      <div class="interview-session">
        <div class="exit-btn-wrapper">
          <el-button @click="exitSession" type="danger" plain round class="exit-btn">
            <el-icon class="mr-1">
              <Close />
            </el-icon>
            退出面试
          </el-button>
        </div>

        <div class="session-header">
          <el-progress :percentage="progressPercentage" :format="progressFormat" class="session-progress" />
        </div>

        <div class="question-container" v-loading="submitting">
          <div class="question-header">
            <div class="tags-group">
              <el-tag effect="dark" class="category-tag">{{ currentQuestion.category || '综合' }}</el-tag>
              <el-tag :type="getDifficultyType(currentQuestion.difficulty)" effect="plain" class="ml-2">{{
                currentQuestion.difficulty
                }}</el-tag>
            </div>
            <span class="question-index">第 {{ currentQuestionIndex + 1 }} / {{ totalQuestions }} 题</span>
          </div>

          <div class="question-content">
            <h3 class="question-title">{{ currentQuestion.content }}</h3>

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
              <el-input v-model="userAnswer" :rows="8" type="textarea" placeholder="请输入您的回答..." resize="none" />
            </div>
          </div>

          <div class="question-footer">
            <el-button type="primary" size="large" @click="submitAndNext" :loading="submitting" class="submit-btn">
              {{ isLastQuestion ? '提交并结束' : '提交并下一题' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { EditPen, TrendCharts, VideoPlay, Warning, Document, Loading, Close } from '@element-plus/icons-vue';
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
const analysisData = computed(() => {
  if (!latestAnalysis.value) return null;
  try {
    return typeof latestAnalysis.value === 'string'
      ? JSON.parse(latestAnalysis.value)
      : latestAnalysis.value;
  } catch (e) {
    console.error('Parse analysis error:', e);
    return null;
  }
});

const getScoreType = (score) => {
  if (score >= 80) return 'success';
  if (score >= 60) return 'warning';
  return 'danger';
};

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
      axios.get(API_URLS.interview.latestAnalysis, { headers: getHeaders() }).catch(() => ({ data: null }))
    ]);

    stats.value = statsRes.data;
    latestAnalysis.value = analysisRes.data;

    // Check for pending session
    try {
      const pendingRes = await axios.get(API_URLS.interview.pending, { headers: getHeaders() });
      if (pendingRes.status === 200 && pendingRes.data) {
        currentSession.value = pendingRes.data;
        // If the backend returns currentQuestionIndex, use it, otherwise default to 0
        // Our backend update added this field
        currentQuestionIndex.value = currentSession.value.currentQuestionIndex || 0;

        // If all questions answered but status is still IN_PROGRESS (shouldn't happen ideally but just in case)
        if (currentQuestionIndex.value >= currentSession.value.questions.length && currentSession.value.questions.length > 0) {
          currentQuestionIndex.value = currentSession.value.questions.length - 1;
        }

        userAnswer.value = '';
        sessionActive.value = true;
        ElMessage.success('已恢复上次未完成的面试');
      }
    } catch (e) {
      // No pending session or error, ignore
      console.log('No pending session found');
    }

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
  // Check if answer is empty
  if (!userAnswer.value || !userAnswer.value.trim()) {
    try {
      await ElMessageBox.confirm(
        '当前题目未作答，确定跳过吗？跳过将记为0分。',
        '提示',
        {
          confirmButtonText: '确定跳过',
          cancelButtonText: '取消',
          type: 'warning',
        }
      );
    } catch {
      return;
    }
  }

  const payload = {
    sessionId: currentSession.value.id || currentSession.value.sessionId,
    questionId: currentQuestion.value.id,
    answer: userAnswer.value || ''
  };

  // If it's the last question, wait for completion to show finish dialog
  if (isLastQuestion.value) {
    submitting.value = true;
    try {
      await axios.post(API_URLS.interview.submit, payload, { headers: getHeaders() });
      finishSession();
    } catch (error) {
      console.error('Failed to submit answer:', error);
      ElMessage.error('提交失败，请重试');
    } finally {
      submitting.value = false;
    }
  } else {
    // Intermediate questions: Fire and forget (Async)
    // Don't set submitting=true to avoid UI freeze/loading spinner
    axios.post(API_URLS.interview.submit, payload, { headers: getHeaders() })
      .catch(error => {
        console.error('Background submission failed:', error);
        // Silent fail or small toast? Ideally retry queue, but for now just log
      });

    // Immediately switch to next question
    currentQuestionIndex.value++;
    userAnswer.value = '';
    window.scrollTo(0, 0);
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
    '确定要退出当前面试吗？退出后将无法恢复，未作答题目将不计分。',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    // Explicitly end session on backend
    if (currentSession.value && (currentSession.value.id || currentSession.value.sessionId)) {
      try {
        const sid = currentSession.value.id || currentSession.value.sessionId;
        await axios.post(API_URLS.interview.end(sid), {}, { headers: getHeaders() });
      } catch (e) {
        console.error('Failed to end session:', e);
      }
    }

    sessionActive.value = false;
    currentSession.value = null; // Clear session data
    fetchData(); // Refresh stats
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

const formatAnalysis = (val) => {
  if (!val) return '暂无数据';
  try {
    const obj = typeof val === 'string' ? JSON.parse(val) : val;
    return JSON.stringify(obj, null, 2);
  } catch (e) {
    console.error('Format analysis error:', e);
    return typeof val === 'string' ? val : JSON.stringify(val);
  }
};
</script>

<style scoped>
.interview-prep-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 0;
  min-height: 80vh;
}

.dashboard-layout {
  position: relative;
  height: 600px;
  display: flex;
  justify-content: center;
  align-items: center;
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
  width: 250px;
  height: 200px;
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
  top: 5%;
  left: 10%;
  border-radius: 40% 60% 70% 30% / 40% 50% 60% 50%;
  background: linear-gradient(135deg, #fff 0%, #f0f9ff 100%);
}

.card-top-right {
  top: 5%;
  right: 10%;
  border-radius: 60% 40% 30% 70% / 50% 40% 50% 60%;
  background: linear-gradient(135deg, #fff 0%, #f0fff4 100%);
}

.card-bottom-left {
  bottom: 25%;
  left: 10%;
  border-radius: 70% 30% 50% 50% / 60% 40% 60% 40%;
  background: linear-gradient(135deg, #fff 0%, #fff0f0 100%);
}

.card-bottom-right {
  bottom: 25%;
  right: 10%;
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
.analysis-report {
  padding: 10px;
}

.report-section {
  margin-bottom: 24px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #ebeef5;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 16px;
  color: #303133;
  border-left: 4px solid #409eff;
  padding-left: 10px;
}

.summary-text {
  line-height: 1.6;
  color: #606266;
  font-size: 15px;
}

.flex-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.flex-header .section-title {
  margin-bottom: 0;
}

.analysis-sub-section {
  margin-bottom: 16px;
}

.analysis-sub-section h4 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #303133;
}

.analysis-sub-section.bg-gray {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
}

.analysis-sub-section.bg-gray p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.advice-list {
  padding-left: 20px;
  margin: 0;
}

.advice-list li {
  margin-bottom: 8px;
  line-height: 1.5;
  color: #606266;
  list-style-type: disc;
}

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
  position: relative;
  /* Make space for header/progress */
}

.exit-btn-wrapper {
  position: fixed;
  top: 20px;
  /* Adjust based on navbar height, assuming ~80px */
  right: 20px;
  z-index: 100;
}

.exit-btn {
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
  transition: all 0.3s;
}

.exit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(245, 108, 108, 0.4);
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

.question-content {}

/* Question Styles */
.question-container {
  margin-top: 24px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.question-index {
  color: #909399;
  font-size: 14px;
}

.question-title {
  font-size: 24px;
  color: #303133;
  line-height: 1.6;
  font-weight: 600;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.radio-group-vertical {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  width: 100%;
}

/* Gradient Capsule Option Styles */
.option-item {
  margin: 0 !important;
  width: 100%;
  padding: 16px 24px;
  border-radius: 50px;
  /* Capsule shape */
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #fff;
  border: 1px solid #e4e7ed;
  font-size: 16px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.option-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.option-item.is-checked {
  background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%);
  border-color: transparent;
  box-shadow: 0 4px 16px 0 rgba(64, 158, 255, 0.3);
}

.option-item.is-checked :deep(.el-radio__label) {
  color: #fff !important;
  font-weight: 500;
}

.option-item :deep(.el-radio__input.is-checked .el-radio__inner) {
  border-color: #fff;
  background: #fff;
}

.option-item :deep(.el-radio__input.is-checked .el-radio__inner::after) {
  background-color: #409eff;
}

/* Text Area Styles */
.text-answer {
  margin-top: 16px;
}

.text-answer :deep(.el-textarea__inner) {
  background: #f8fafc;
  border: 1px solid transparent;
  padding: 20px;
  border-radius: 16px;
  font-size: 16px;
  line-height: 1.6;
  transition: all 0.3s;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.02);
}

.text-answer :deep(.el-textarea__inner):focus {
  background: #fff;
  border-color: #409eff;
  box-shadow: 0 0 0 1px #409eff, 0 4px 12px rgba(64, 158, 255, 0.1);
}

.question-footer {
  margin-top: 16px;
  text-align: right;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  padding-top: 16px;
}

.submit-btn {
  padding: 12px 36px;
  border-radius: 50px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

/* Loading */
.loading-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.loading-text {
  margin-top: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 16px;
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