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
      <div v-if="sessionActive" class="interview-session">
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
                <div
                  v-for="(option, idx) in currentQuestion.options"
                  :key="idx"
                  class="option-card"
                  :class="{ 'is-selected': userAnswer === option }"
                  @click="userAnswer = option"
                >
                  <div class="option-indicator">{{ String.fromCharCode(65 + idx) }}</div>
                  <el-radio :label="option" class="option-radio-hidden">
                    <span class="option-text">{{ option }}</span>
                  </el-radio>
                </div>
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
/* Dark Glass Theme - With explicit fallback colors */
.interview-prep-container {
  --accent-primary: #0ea5e9;
  --accent-secondary: #22d3ee;
  --fg-primary: #e6edf3;
  --fg-secondary: #cbd5e1;
  --fg-muted: #94a3b8;
  --bg-primary: #0b1220;
  --glass-bg: rgba(255, 255, 255, 0.06);
  --glass-bg-hover: rgba(255, 255, 255, 0.1);
  --glass-border: rgba(255, 255, 255, 0.12);
  --glass-blur: blur(14px);
  --glass-shadow: 0 10px 40px rgba(0, 0, 0, 0.35);
  --success: #22c55e;
  --danger: #ef4444;
  --warning: #f59e0b;
  --font-sans: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  --font-mono: 'JetBrains Mono', 'Fira Code', monospace;

  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 0;
  min-height: 80vh;
  animation: fadeIn 0.5s ease-out;
}

.header-section {
  text-align: center;
  margin-bottom: 40px;
  animation: fadeInDown 0.6s ease-out;
}

.page-title {
  font-size: 2.5em;
  font-weight: 800;
  background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.page-subtitle {
  color: var(--fg-muted);
  font-size: 1.1em;
}

.dashboard-layout {
  position: relative;
  height: 600px;
  display: flex;
  justify-content: center;
  align-items: center;
  animation: fadeInUp 0.6s ease-out;
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
  background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary));
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: var(--bg-primary);
  box-shadow: 0 0 40px rgba(14, 165, 233, 0.4);
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
  border: 2px solid var(--accent-primary);
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

/* Orbit Cards - Glass Style */
.orbit-card {
  position: absolute;
  width: 250px;
  height: 200px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  box-shadow: var(--glass-shadow);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  transition: all 0.3s ease;
}

.orbit-card:hover {
  transform: translateY(-5px);
  background: var(--glass-bg-hover);
  border-color: var(--accent-primary);
  box-shadow: 0 15px 40px rgba(14, 165, 233, 0.2);
  z-index: 5;
}

.clickable {
  cursor: pointer;
}

.clickable:hover {
  border-color: var(--accent-primary);
}

/* Irregular Shapes */
.card-top-left {
  top: 5%;
  left: 10%;
  border-radius: 40% 60% 70% 30% / 40% 50% 60% 50%;
  animation: fadeInUp 0.5s ease-out 0.1s backwards;
}

.card-top-right {
  top: 5%;
  right: 10%;
  border-radius: 60% 40% 30% 70% / 50% 40% 50% 60%;
  animation: fadeInUp 0.5s ease-out 0.2s backwards;
}

.card-bottom-left {
  bottom: 25%;
  left: 10%;
  border-radius: 70% 30% 50% 50% / 60% 40% 60% 40%;
  animation: fadeInUp 0.5s ease-out 0.3s backwards;
}

.card-bottom-right {
  bottom: 25%;
  right: 10%;
  border-radius: 30% 70% 60% 40% / 40% 60% 50% 50%;
  animation: fadeInUp 0.5s ease-out 0.4s backwards;
}

.card-content {
  text-align: center;
}

.card-icon {
  font-size: 24px;
  margin-bottom: 8px;
  color: var(--accent-primary);
}

.card-label {
  font-size: 14px;
  color: var(--fg-muted);
  margin-bottom: 4px;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: var(--fg-primary);
}

.card-hint {
  font-size: 12px;
  color: var(--accent-primary);
  margin-top: 4px;
}

/* Dialog Styles */
.analysis-report {
  padding: 10px;
}

.report-section {
  margin-bottom: 24px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: 12px;
  padding: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 16px;
  color: var(--fg-primary);
  border-left: 4px solid var(--accent-primary);
  padding-left: 10px;
}

.summary-text {
  line-height: 1.6;
  color: var(--fg-secondary);
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
  color: var(--fg-primary);
}

.analysis-sub-section.bg-gray {
  background: rgba(255, 255, 255, 0.03);
  padding: 12px;
  border-radius: 8px;
}

.analysis-sub-section.bg-gray p {
  margin: 0;
  color: var(--fg-secondary);
  font-size: 14px;
}

.advice-list {
  padding-left: 20px;
  margin: 0;
}

.advice-list li {
  margin-bottom: 8px;
  line-height: 1.5;
  color: var(--fg-secondary);
  list-style-type: disc;
}

.json-viewer {
  background: rgba(15, 23, 42, 0.6);
  padding: 16px;
  border-radius: 12px;
  overflow: auto;
  max-height: 500px;
  font-family: var(--font-mono);
  color: var(--fg-secondary);
  border: 1px solid var(--glass-border);
}

/* Session Styles */
.interview-session {
  max-width: 800px;
  margin: 0 auto;
  position: relative;
  animation: fadeInUp 0.5s ease-out;
}

.exit-btn-wrapper {
  position: absolute;
  top: -60px;
  right: 0;
  z-index: 100;
}

.exit-btn {
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
  transition: all 0.3s;
}

.exit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(239, 68, 68, 0.4);
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
  color: var(--fg-muted);
  font-size: 14px;
}

/* Question Styles */
.question-container {
  margin-top: 24px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  padding: 24px;
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  box-shadow: var(--glass-shadow);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.question-index {
  color: var(--fg-muted);
  font-size: 14px;
}

.question-title {
  font-size: 24px;
  color: var(--fg-primary);
  line-height: 1.6;
  font-weight: 600;
  margin-bottom: 20px;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.radio-group-vertical {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  width: 100%;
}

/* Modern Option Card Styles */
.option-card {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(15, 23, 42, 0.5);
  border: 1px solid var(--glass-border);
  position: relative;
  overflow: hidden;
}

.option-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.1), rgba(34, 211, 238, 0.1));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.option-card:hover {
  transform: translateX(8px);
  border-color: rgba(14, 165, 233, 0.5);
  box-shadow: 0 4px 20px rgba(14, 165, 233, 0.15);
}

.option-card:hover::before {
  opacity: 1;
}

.option-card.is-selected {
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.2), rgba(34, 211, 238, 0.15));
  border-color: var(--accent-primary);
  box-shadow: 0 4px 24px rgba(14, 165, 233, 0.3), inset 0 0 0 1px rgba(14, 165, 233, 0.3);
}

.option-card.is-selected::before {
  opacity: 1;
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.15), rgba(34, 211, 238, 0.1));
}

/* Option Indicator (A, B, C, D) */
.option-indicator {
  width: 36px;
  height: 36px;
  min-width: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 15px;
  margin-right: 16px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: var(--fg-muted);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.option-card:hover .option-indicator {
  background: rgba(14, 165, 233, 0.15);
  border-color: rgba(14, 165, 233, 0.4);
  color: var(--accent-primary);
}

.option-card.is-selected .option-indicator {
  background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary));
  border-color: transparent;
  color: var(--bg-primary);
  box-shadow: 0 4px 12px rgba(14, 165, 233, 0.4);
  transform: scale(1.05);
}

/* Hidden Radio Styling */
.option-radio-hidden {
  margin: 0 !important;
  flex: 1;
}

.option-radio-hidden :deep(.el-radio__input) {
  display: none;
}

.option-radio-hidden :deep(.el-radio__label) {
  padding-left: 0;
  color: var(--fg-primary);
  font-size: 15px;
  line-height: 1.6;
}

.option-text {
  display: block;
  position: relative;
  z-index: 1;
}

.option-card.is-selected .option-radio-hidden :deep(.el-radio__label) {
  color: var(--fg-primary);
  font-weight: 500;
}

/* Text Area Styles */
.text-answer {
  margin-top: 16px;
}

.text-answer :deep(.el-textarea__inner) {
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid var(--glass-border);
  padding: 20px;
  border-radius: 16px;
  font-size: 16px;
  line-height: 1.6;
  transition: all 0.3s;
  color: var(--fg-primary);
}

.text-answer :deep(.el-textarea__inner::placeholder) {
  color: var(--fg-muted);
}

.text-answer :deep(.el-textarea__inner):focus {
  background: rgba(15, 23, 42, 0.8);
  border-color: var(--accent-primary);
  box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.2);
}

.question-footer {
  margin-top: 24px;
  text-align: right;
  border-top: 1px solid var(--glass-border);
  padding-top: 16px;
}

.submit-btn {
  padding: 12px 36px;
  border-radius: 50px;
  font-weight: 600;
  box-shadow: 0 0 20px rgba(14, 165, 233, 0.3);
  transition: all 0.3s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 0 30px rgba(14, 165, 233, 0.5);
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
  color: var(--fg-muted);
  font-size: 16px;
}

.wrong-question-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.wrong-question-detail {
  padding: 16px;
  background: rgba(245, 158, 11, 0.1);
  border-radius: 8px;
  border: 1px solid rgba(245, 158, 11, 0.2);
}

.wrong-question-detail p {
  color: var(--fg-secondary);
  margin: 0;
}

.ai-feedback {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed var(--glass-border);
}

.ai-feedback p {
  color: var(--fg-secondary);
}

.truncate-text {
  display: inline-block;
  max-width: 400px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
  color: var(--fg-primary);
}

/* Tags */
.tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.mr-2 {
  margin-right: 8px;
}

.mb-2 {
  margin-bottom: 8px;
}

.ml-2 {
  margin-left: 8px;
}

.mr-1 {
  margin-right: 4px;
}

/* Utility classes */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>