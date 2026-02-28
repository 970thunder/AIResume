<template>
  <div class="dashboard-container">
    <!-- Stats Cards -->
    <div class="stats-grid">
      <div class="stat-card" v-for="(card, index) in statCards" :key="index" :style="{ animationDelay: `${index * 0.1}s` }">
        <div class="card-icon" :style="{ background: card.gradient }">
          <component :is="card.icon" />
        </div>
        <div class="card-info">
          <div class="card-label">{{ card.label }}</div>
          <div class="card-value">{{ card.value }}</div>
          <div class="card-sub" v-if="card.sub">{{ card.sub }}</div>
        </div>
        <div class="card-decoration">
          <div class="deco-line" :style="{ background: card.gradient }"></div>
        </div>
      </div>
    </div>

    <!-- Charts Section -->
    <div class="charts-grid">
      <div class="chart-card">
        <div class="chart-header">
          <div class="chart-title">
            <el-icon><PieChart /></el-icon>
            <span>模板状态分布</span>
          </div>
        </div>
        <div class="chart-body">
          <div ref="templateChartRef" class="chart-container"></div>
        </div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <div class="chart-title">
            <el-icon><DataAnalysis /></el-icon>
            <span>系统概览</span>
          </div>
        </div>
        <div class="chart-body">
          <div ref="systemChartRef" class="chart-container"></div>
        </div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="quick-actions">
      <div class="section-title">
        <el-icon><Operation /></el-icon>
        <span>快捷操作</span>
      </div>
      <div class="actions-grid">
        <router-link to="/admin/templates" class="action-btn">
          <el-icon><Files /></el-icon>
          <span>审核模板</span>
        </router-link>
        <router-link to="/admin/questions" class="action-btn">
          <el-icon><List /></el-icon>
          <span>管理题库</span>
        </router-link>
        <router-link to="/admin/users" class="action-btn">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </router-link>
        <router-link to="/" class="action-btn">
          <el-icon><HomeFilled /></el-icon>
          <span>访问前台</span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import { API_URLS, getHeaders } from '@/config/api';
import * as echarts from 'echarts';
import { User, Files, Document, ChatDotRound, PieChart, DataAnalysis, Operation, List, HomeFilled } from '@element-plus/icons-vue';

const stats = ref({
  totalUsers: 0,
  totalTemplates: 0,
  pendingTemplates: 0,
  totalQuestions: 0,
  activeSessionsToday: 0
});

const templateChartRef = ref(null);
const systemChartRef = ref(null);

const statCards = computed(() => [
  {
    label: '总用户数',
    value: stats.value.totalUsers,
    icon: User,
    gradient: 'linear-gradient(135deg, #0ea5e9, #22d3ee)',
    iconBg: 'rgba(14, 165, 233, 0.15)'
  },
  {
    label: '总模板数',
    value: stats.value.totalTemplates,
    sub: `待审核: ${stats.value.pendingTemplates}`,
    icon: Files,
    gradient: 'linear-gradient(135deg, #8b5cf6, #a78bfa)',
    iconBg: 'rgba(139, 92, 246, 0.15)'
  },
  {
    label: '题库总题数',
    value: stats.value.totalQuestions,
    icon: Document,
    gradient: 'linear-gradient(135deg, #f59e0b, #fbbf24)',
    iconBg: 'rgba(245, 158, 11, 0.15)'
  },
  {
    label: '今日面试会话',
    value: stats.value.activeSessionsToday,
    icon: ChatDotRound,
    gradient: 'linear-gradient(135deg, #10b981, #34d399)',
    iconBg: 'rgba(16, 185, 129, 0.15)'
  }
]);

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
        backgroundColor: 'rgba(15, 23, 42, 0.95)',
        borderColor: 'rgba(14, 165, 233, 0.3)',
        borderWidth: 1,
        textStyle: { color: '#e2e8f0' },
        borderRadius: 8,
        padding: [12, 16]
      },
      legend: {
        top: '5%',
        left: 'center',
        textStyle: { color: '#94a3b8', fontSize: 12 }
      },
      series: [
        {
          name: '模板状态',
          type: 'pie',
          radius: ['45%', '75%'],
          center: ['50%', '55%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 8,
            borderColor: 'rgba(15, 23, 42, 0.8)',
            borderWidth: 3
          },
          label: { show: false, position: 'center' },
          emphasis: {
            label: {
              show: true,
              fontSize: 18,
              fontWeight: 'bold',
              color: '#e2e8f0',
              formatter: '{b}\n{c}'
            },
            itemStyle: {
              shadowBlur: 20,
              shadowColor: 'rgba(14, 165, 233, 0.3)'
            }
          },
          labelLine: { show: false },
          data: [
            {
              value: stats.value.pendingTemplates,
              name: '待审核',
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
                  { offset: 0, color: '#f59e0b' },
                  { offset: 1, color: '#fbbf24' }
                ])
              }
            },
            {
              value: stats.value.totalTemplates - stats.value.pendingTemplates,
              name: '已发布',
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
                  { offset: 0, color: '#0ea5e9' },
                  { offset: 1, color: '#22d3ee' }
                ])
              }
            }
          ]
        }
      ]
    });

    window.addEventListener('resize', () => chart.resize());
  }

  if (systemChartRef.value) {
    const chart = echarts.init(systemChartRef.value);
    chart.setOption({
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow',
          shadowStyle: { color: 'rgba(14, 165, 233, 0.08)' }
        },
        backgroundColor: 'rgba(15, 23, 42, 0.95)',
        borderColor: 'rgba(14, 165, 233, 0.3)',
        borderWidth: 1,
        textStyle: { color: '#e2e8f0' },
        borderRadius: 8,
        padding: [12, 16]
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        top: '10%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: ['用户', '模板', '题目', '今日面试'],
        axisLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.2)' } },
        axisLabel: { color: '#94a3b8', fontSize: 12 },
        axisTick: { show: false }
      },
      yAxis: {
        type: 'value',
        axisLine: { show: false },
        axisLabel: { color: '#64748b', fontSize: 11 },
        splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.08)' } }
      },
      series: [
        {
          name: '数量',
          data: [
            {
              value: stats.value.totalUsers,
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#22d3ee' },
                  { offset: 1, color: '#0ea5e9' }
                ]),
                borderRadius: [6, 6, 0, 0]
              }
            },
            {
              value: stats.value.totalTemplates,
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#a78bfa' },
                  { offset: 1, color: '#8b5cf6' }
                ]),
                borderRadius: [6, 6, 0, 0]
              }
            },
            {
              value: stats.value.totalQuestions,
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#fbbf24' },
                  { offset: 1, color: '#f59e0b' }
                ]),
                borderRadius: [6, 6, 0, 0]
              }
            },
            {
              value: stats.value.activeSessionsToday,
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#34d399' },
                  { offset: 1, color: '#10b981' }
                ]),
                borderRadius: [6, 6, 0, 0]
              }
            }
          ],
          type: 'bar',
          barWidth: '50%',
          showBackground: true,
          backgroundStyle: {
            color: 'rgba(148, 163, 184, 0.05)',
            borderRadius: [6, 6, 0, 0]
          }
        }
      ]
    });

    window.addEventListener('resize', () => chart.resize());
  }
};
</script>

<style scoped>
.dashboard-container {
  max-width: 1400px;
  margin: 0 auto;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.1);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  animation: cardFadeIn 0.5s ease forwards;
  opacity: 0;
  backdrop-filter: blur(12px);
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-card:hover {
  transform: translateY(-4px);
  border-color: rgba(14, 165, 233, 0.3);
  box-shadow: 0 12px 32px rgba(14, 165, 233, 0.15);
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.card-icon .el-icon {
  font-size: 24px;
  color: white;
}

.card-info {
  flex: 1;
  min-width: 0;
}

.card-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 8px;
  font-weight: 500;
}

.card-value {
  font-size: 28px;
  font-weight: 700;
  color: #f1f5f9;
  line-height: 1.2;
  transition: all 0.3s ease;
}

.stat-card:hover .card-value {
  color: #22d3ee;
  text-shadow: 0 0 20px rgba(34, 211, 238, 0.3);
}

.card-sub {
  font-size: 12px;
  color: #f59e0b;
  margin-top: 6px;
  font-weight: 500;
}

.card-decoration {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.stat-card:hover .card-decoration {
  opacity: 1;
}

.deco-line {
  height: 100%;
  width: 60%;
  margin: 0 auto;
  border-radius: 3px 3px 0 0;
}

/* Charts Grid */
.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.chart-card {
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.1);
  border-radius: 16px;
  overflow: hidden;
  backdrop-filter: blur(12px);
  transition: all 0.3s ease;
}

.chart-card:hover {
  border-color: rgba(14, 165, 233, 0.2);
}

.chart-header {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.1);
}

.chart-title {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #e2e8f0;
  font-weight: 600;
  font-size: 15px;
}

.chart-title .el-icon {
  font-size: 18px;
  color: #22d3ee;
}

.chart-body {
  padding: 16px;
}

.chart-container {
  height: 300px;
}

/* Quick Actions */
.quick-actions {
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.1);
  border-radius: 16px;
  padding: 24px;
  backdrop-filter: blur(12px);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #e2e8f0;
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 20px;
}

.section-title .el-icon {
  font-size: 18px;
  color: #22d3ee;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px;
  background: rgba(30, 41, 59, 0.5);
  border: 1px solid rgba(148, 163, 184, 0.1);
  border-radius: 12px;
  color: #94a3b8;
  text-decoration: none;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: rgba(14, 165, 233, 0.1);
  border-color: rgba(14, 165, 233, 0.3);
  color: #22d3ee;
  transform: translateY(-2px);
}

.action-btn .el-icon {
  font-size: 24px;
}

.action-btn span {
  font-size: 13px;
  font-weight: 500;
}

/* Responsive */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .charts-grid {
    grid-template-columns: 1fr;
  }

  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .chart-container {
    height: 250px;
  }
}
</style>
