<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>总用户数</span>
              <el-tag type="success">User</el-tag>
            </div>
          </template>
          <div class="card-value">{{ stats.totalUsers }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>总模板数</span>
              <el-tag type="primary">Template</el-tag>
            </div>
          </template>
          <div class="card-value">{{ stats.totalTemplates }}</div>
          <div class="sub-value">待审核: {{ stats.pendingTemplates }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>题库总题数</span>
              <el-tag type="warning">Question</el-tag>
            </div>
          </template>
          <div class="card-value">{{ stats.totalQuestions }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>今日面试会话</span>
              <el-tag type="danger">Interview</el-tag>
            </div>
          </template>
          <div class="card-value">{{ stats.activeSessionsToday }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-4">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>模板状态分布</span>
          </template>
          <div ref="templateChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>系统概览</span>
          </template>
          <div ref="systemChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { API_URLS, getHeaders } from '@/config/api';
import * as echarts from 'echarts';

const stats = ref({
  totalUsers: 0,
  totalTemplates: 0,
  pendingTemplates: 0,
  totalQuestions: 0,
  activeSessionsToday: 0
});

const templateChartRef = ref(null);
const systemChartRef = ref(null);

onMounted(async () => {
  await fetchStats();
  initCharts();
});

const fetchStats = async () => {
  try {
    const res = await axios.get(API_URLS.admin.stats, { headers: getHeaders() });
    stats.value = res.data;
  } catch (error) {
    console.error('Failed to fetch stats', error);
  }
};

const initCharts = () => {
  if (templateChartRef.value) {
    const chart = echarts.init(templateChartRef.value);
    chart.setOption({
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'item',
        backgroundColor: 'rgba(15, 23, 42, 0.9)',
        borderColor: 'rgba(255, 255, 255, 0.12)',
        textStyle: { color: '#e6edf3' }
      },
      legend: {
        top: '5%',
        left: 'center',
        textStyle: { color: '#94a3b8' }
      },
      series: [
        {
          name: '模板状态',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: 'rgba(255, 255, 255, 0.1)',
            borderWidth: 2
          },
          label: { show: false, position: 'center' },
          emphasis: {
            label: { show: true, fontSize: 20, fontWeight: 'bold', color: '#e6edf3' }
          },
          labelLine: { show: false },
          data: [
            { value: stats.value.pendingTemplates, name: '待审核', itemStyle: { color: '#f59e0b' } },
            { value: stats.value.totalTemplates - stats.value.pendingTemplates, name: '已发布', itemStyle: { color: '#0ea5e9' } }
          ]
        }
      ]
    });
  }

  if (systemChartRef.value) {
    const chart = echarts.init(systemChartRef.value);
    chart.setOption({
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        backgroundColor: 'rgba(15, 23, 42, 0.9)',
        borderColor: 'rgba(255, 255, 255, 0.12)',
        textStyle: { color: '#e6edf3' }
      },
      xAxis: {
        type: 'category',
        data: ['用户', '模板', '题目', '今日面试'],
        axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.12)' } },
        axisLabel: { color: '#94a3b8' }
      },
      yAxis: {
        type: 'value',
        axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.12)' } },
        axisLabel: { color: '#94a3b8' },
        splitLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.06)' } }
      },
      series: [
        {
          name: '数量',
          data: [
            stats.value.totalUsers,
            stats.value.totalTemplates,
            stats.value.totalQuestions,
            stats.value.activeSessionsToday
          ],
          type: 'bar',
          showBackground: true,
          backgroundStyle: {
            color: 'rgba(255, 255, 255, 0.05)'
          },
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#22d3ee' },
              { offset: 0.5, color: '#0ea5e9' },
              { offset: 1, color: '#0284c7' }
            ])
          }
        }
      ]
    });
  }
};
</script>

<style scoped>
.dashboard-container {
  animation: fadeInUp 0.5s ease-out;
}

/* Stat Card Enhancement */
.stat-card {
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, transparent, rgba(14, 165, 233, 0.05));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 40px rgba(14, 165, 233, 0.2);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-value {
  font-size: 28px;
  font-weight: bold;
  color: var(--fg-primary);
  margin-top: 10px;
  transition: all 0.3s ease;
}

.stat-card:hover .card-value {
  color: var(--accent-primary);
  text-shadow: 0 0 20px rgba(14, 165, 233, 0.3);
}

.sub-value {
  font-size: 14px;
  color: var(--fg-muted);
  margin-top: 5px;
}

.mt-4 {
  margin-top: 24px;
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

/* Element Plus Card Deep Theme */
:deep(.el-card) {
  background: var(--glass-bg) !important;
  border: 1px solid var(--glass-border) !important;
  border-radius: 16px !important;
}

:deep(.el-card__header) {
  border-bottom: 1px solid var(--glass-border) !important;
  color: var(--fg-primary) !important;
  font-weight: 600;
}

:deep(.el-card__body) {
  color: var(--fg-secondary) !important;
}
</style>
