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
      tooltip: { trigger: 'item' },
      legend: { top: '5%', left: 'center' },
      series: [
        {
          name: '模板状态',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: { show: false, position: 'center' },
          emphasis: {
            label: { show: true, fontSize: 20, fontWeight: 'bold' }
          },
          labelLine: { show: false },
          data: [
            { value: stats.value.pendingTemplates, name: '待审核' },
            { value: stats.value.totalTemplates - stats.value.pendingTemplates, name: '已发布' } // Approximate
          ]
        }
      ]
    });
  }

  if (systemChartRef.value) {
    const chart = echarts.init(systemChartRef.value);
    chart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      xAxis: {
        type: 'category',
        data: ['用户', '模板', '题目', '今日面试']
      },
      yAxis: {
        type: 'value'
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
            color: 'rgba(180, 180, 180, 0.2)'
          },
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#83bff6' },
              { offset: 0.5, color: '#188df0' },
              { offset: 1, color: '#188df0' }
            ])
          }
        }
      ]
    });
  }
};
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-top: 10px;
}

.sub-value {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.mt-4 {
  margin-top: 24px;
}
</style>
