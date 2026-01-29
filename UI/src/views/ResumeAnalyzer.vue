<template>
    <div class="resume-analyzer">
        <div class="analyzer-header">
            <h1 class="sci-fi-title">
                <span class="glitch-text" data-text="QUANTUM">QUANTUM</span>
                RESUME DIAGNOSTICS
            </h1>
            <p class="subtitle">AI 驱动的深度简历分析与岗位匹配系统</p>
        </div>

        <div class="analyzer-content">
            <!-- Upload & Config Section -->
            <transition name="fade" mode="out-in">
                <div v-if="!analysisResult && !loading" class="input-dashboard">
                    <div class="glass-panel main-panel">
                        <div class="panel-header">
                            <el-icon>
                                <Document />
                            </el-icon>
                            <span>简历输入源</span>
                        </div>
                        <div class="upload-area">
                            <el-upload class="upload-dragger" drag action="#" :auto-upload="false"
                                :on-change="handleFileChange" :show-file-list="false" accept=".pdf,.doc,.docx,.txt">
                                <div class="upload-content">
                                    <div class="icon-wrapper">
                                        <el-icon class="upload-icon">
                                            <UploadFilled />
                                        </el-icon>
                                        <div class="scan-ring"></div>
                                    </div>
                                    <div class="upload-text">
                                        <h3>点击或拖拽上传简历</h3>
                                        <p>支持 PDF, Word, TXT (Max 10MB)</p>
                                    </div>
                                </div>
                            </el-upload>
                            <div v-if="file" class="file-preview">
                                <el-icon>
                                    <DocumentChecked />
                                </el-icon>
                                <span>{{ file.name }}</span>
                                <el-button type="danger" link @click="clearFile"><el-icon>
                                        <Close />
                                    </el-icon></el-button>
                            </div>
                        </div>
                    </div>

                    <div class="glass-panel side-panel">
                        <div class="panel-header">
                            <el-icon>
                                <Aim />
                            </el-icon>
                            <span>目标岗位 (JD) - 可选</span>
                        </div>
                        <div class="jd-input-area">
                            <el-input v-model="jobDescription" type="textarea" :rows="8"
                                placeholder="粘贴目标岗位的职位描述 (JD)，AI 将自动分析简历与岗位的匹配度..." resize="none"
                                class="sci-fi-input" />
                        </div>
                        <div class="action-area">
                            <button class="sci-fi-btn" @click="startAnalysis" :disabled="!file">
                                <span class="btn-content">
                                    <el-icon class="btn-icon">
                                        <Cpu />
                                    </el-icon>
                                    启动神经元分析
                                </span>
                                <div class="btn-glitch"></div>
                            </button>
                        </div>
                    </div>
                </div>

                <!-- Loading Section -->
                <div v-else-if="loading" class="loading-container">
                    <div class="scanner-hud">
                        <div class="scanner-circle"></div>
                        <div class="scanner-line"></div>
                        <div class="scanner-text">
                            <div class="progress-percentage">{{ Math.floor(loadingProgress) }}%</div>
                            <span class="blink">SYSTEM ANALYZING...</span>
                            <div class="process-steps">
                                <div class="step" :class="{ active: loadingProgress > 10 }">解析文档结构</div>
                                <div class="step" :class="{ active: loadingProgress > 40 }">提取关键技能</div>
                                <div class="step" :class="{ active: loadingProgress > 70 }">计算岗位匹配度</div>
                                <div class="step" :class="{ active: loadingProgress > 90 }">生成优化策略</div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Result Section -->
                <div v-else class="result-dashboard">
                    <div class="dashboard-top-bar">
                        <div class="action-buttons">
                            <button class="sci-fi-btn small" @click="resetAnalysis">
                                <el-icon>
                                    <Back />
                                </el-icon> 返回
                            </button>
                            <button class="sci-fi-btn small download" @click="downloadReport">
                                <el-icon>
                                    <Download />
                                </el-icon> 下载报告 PDF
                            </button>
                        </div>
                        <div class="result-timestamp">ANALYSIS_ID: {{ new Date().getTime() }}</div>
                    </div>

                    <!-- Main Stats Row -->
                    <div class="stats-row">
                        <!-- Score Card -->
                        <div class="glass-card score-card hover-effect">
                            <div class="card-title">综合竞争力指数</div>
                            <div class="score-display">
                                <el-progress type="dashboard" :percentage="analysisResult.score" :width="180"
                                    :stroke-width="12" :color="scoreColor" stroke-linecap="butt">
                                    <template #default="{ percentage }">
                                        <div class="score-inner">
                                            <span class="big-score">{{ percentage }}</span>
                                            <span class="score-unit">PTS</span>
                                        </div>
                                    </template>
                                </el-progress>
                            </div>
                            <div class="score-verdict">{{ analysisResult.summary }}</div>
                        </div>

                        <!-- Radar Chart -->
                        <div class="glass-card radar-card hover-effect">
                            <div class="card-title">能力多维矩阵</div>
                            <div class="chart-box">
                                <v-chart ref="radarChartRef" class="chart" :option="radarOption" autoresize />
                            </div>
                        </div>

                        <!-- JD Match Card (Conditional) -->
                        <div v-if="analysisResult.jdAnalysis" class="glass-card jd-card hover-effect">
                            <div class="card-title">岗位匹配度分析</div>
                            <div class="jd-stats">
                                <div class="match-rate">
                                    <div class="label">匹配度</div>
                                    <div class="value"
                                        :style="{ color: getMatchColor(analysisResult.jdAnalysis.matchScore) }">
                                        {{ analysisResult.jdAnalysis.matchScore }}%
                                    </div>
                                </div>
                                <div class="keywords-analysis">
                                    <div class="keyword-group missing">
                                        <span class="group-label">缺失技能</span>
                                        <div class="tags">
                                            <span v-for="kw in analysisResult.jdAnalysis.missingKeywords" :key="kw"
                                                class="tech-tag missing">
                                                {{ kw }}
                                            </span>
                                            <span v-if="!analysisResult.jdAnalysis.missingKeywords?.length"
                                                class="empty-tag">无明显缺失</span>
                                        </div>
                                    </div>
                                    <div class="keyword-group matching">
                                        <span class="group-label">匹配技能</span>
                                        <div class="tags">
                                            <span v-for="kw in analysisResult.jdAnalysis.matchingKeywords" :key="kw"
                                                class="tech-tag matching">
                                                {{ kw }}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Detailed Analysis Grid -->
                    <div class="details-grid">
                        <div class="glass-panel detail-panel strength hover-effect">
                            <div class="panel-header">
                                <el-icon>
                                    <CircleCheckFilled />
                                </el-icon> 核心优势
                            </div>
                            <div class="panel-body">
                                <div v-for="(item, i) in analysisResult.strengths" :key="i" class="analysis-item">
                                    <span class="index">0{{ i + 1 }}</span>
                                    <p>{{ item }}</p>
                                </div>
                            </div>
                        </div>

                        <div class="glass-panel detail-panel weakness hover-effect">
                            <div class="panel-header">
                                <el-icon>
                                    <WarningFilled />
                                </el-icon> 潜在短板
                            </div>
                            <div class="panel-body">
                                <div v-for="(item, i) in analysisResult.weaknesses" :key="i" class="analysis-item">
                                    <span class="index">0{{ i + 1 }}</span>
                                    <p>{{ item }}</p>
                                </div>
                            </div>
                        </div>

                        <div class="glass-panel detail-panel advice hover-effect">
                            <div class="panel-header">
                                <el-icon>
                                    <TrendCharts />
                                </el-icon> 提升建议
                            </div>
                            <div class="panel-body">
                                <div v-for="(item, i) in analysisResult.advice" :key="i" class="analysis-item">
                                    <span class="index">0{{ i + 1 }}</span>
                                    <p>{{ item }}</p>
                                </div>
                            </div>
                        </div>

                        <!-- Gap Filling Resources Card -->
                        <div class="glass-panel detail-panel gap hover-effect">
                            <div class="panel-header">
                                <el-icon>
                                    <FirstAidKit />
                                </el-icon> 短板补足资源
                            </div>
                            <div class="panel-body">
                                <div v-for="(item, i) in analysisResult.gapFilling" :key="'g' + i"
                                    class="analysis-item resource-item">
                                    <div class="resource-info">
                                        <span class="index">0{{ i + 1 }}</span>
                                        <div class="resource-content">
                                            <a :href="item.link" target="_blank" class="resource-link">{{ item.title
                                                }}</a>
                                            <span class="resource-platform">{{ item.platform }}</span>
                                        </div>
                                    </div>
                                    <el-icon class="arrow-icon">
                                        <ArrowRight />
                                    </el-icon>
                                </div>
                                <div v-if="!analysisResult.gapFilling?.length" class="empty-state">
                                    暂无特定资源推荐
                                </div>
                            </div>
                        </div>

                        <!-- Learning Roadmap Card -->
                        <div class="glass-panel detail-panel roadmap hover-effect">
                            <div class="panel-header">
                                <el-icon>
                                    <Guide />
                                </el-icon> 智能学习路线
                            </div>
                            <div class="panel-body">
                                <div v-for="(item, i) in analysisResult.learningPaths" :key="'r' + i"
                                    class="analysis-item resource-item">
                                    <div class="resource-info">
                                        <span class="index">P{{ i + 1 }}</span>
                                        <div class="resource-content">
                                            <a :href="item.link" target="_blank" class="resource-link">{{ item.title
                                                }}</a>
                                            <span class="resource-platform">{{ item.platform }}</span>
                                        </div>
                                    </div>
                                    <el-icon class="arrow-icon">
                                        <ArrowRight />
                                    </el-icon>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </transition>
        </div>

        <!-- Hidden Print Template -->
        <div id="print-report-container" class="print-template">
            <!-- Header -->
            <div class="print-header">
                <h1>简历深度诊断报告</h1>
                <p>Generated by Quantum Resume Diagnostics</p>
            </div>

            <!-- Overview -->
            <div class="print-section">
                <h2 class="print-title">综合评估</h2>
                <div class="print-score-box">
                    <div class="print-score">{{ analysisResult?.score }} <span>PTS</span></div>
                    <p class="print-summary">{{ analysisResult?.summary }}</p>
                </div>
            </div>

            <!-- Chart Image -->
            <div v-if="radarChartImg" class="print-section center">
                <h2 class="print-title">能力矩阵</h2>
                <img :src="radarChartImg" class="print-chart-img" />
            </div>

            <!-- JD Match -->
            <div v-if="analysisResult?.jdAnalysis" class="print-section">
                <h2 class="print-title">岗位匹配分析</h2>
                <div class="print-card">
                    <div class="print-row"><strong>匹配度:</strong> <span
                            :style="{ color: getMatchColor(analysisResult.jdAnalysis.matchScore) }">{{
                                analysisResult.jdAnalysis.matchScore }}%</span></div>
                    <div class="print-row"><strong>匹配技能:</strong> {{
                        analysisResult.jdAnalysis.matchingKeywords.join(', ') }}
                    </div>
                    <div class="print-row"><strong>缺失技能:</strong> {{
                        analysisResult.jdAnalysis.missingKeywords?.join(', ') || '无'
                        }}</div>
                </div>
            </div>

            <!-- Detailed Analysis -->
            <div class="print-section">
                <h2 class="print-title">详细诊断</h2>

                <div class="print-detail-group">
                    <h3 class="print-subtitle strength">核心优势</h3>
                    <ul>
                        <li v-for="(item, i) in analysisResult?.strengths" :key="'s' + i">{{ item }}</li>
                    </ul>
                </div>

                <div class="print-detail-group">
                    <h3 class="print-subtitle weakness">潜在短板</h3>
                    <ul>
                        <li v-for="(item, i) in analysisResult?.weaknesses" :key="'w' + i">{{ item }}</li>
                    </ul>
                </div>

                <div class="print-detail-group">
                    <h3 class="print-subtitle advice">发展建议</h3>
                    <ul>
                        <li v-for="(item, i) in analysisResult?.advice" :key="'a' + i">{{ item }}</li>
                    </ul>
                </div>

                <div class="print-detail-group" v-if="analysisResult?.gapFilling?.length">
                    <h3 class="print-subtitle gap">短板补足资源</h3>
                    <ul>
                        <li v-for="(item, i) in analysisResult.gapFilling" :key="'g' + i">
                            [{{ item.platform }}] {{ item.title }}
                        </li>
                    </ul>
                </div>

                <div class="print-detail-group" v-if="analysisResult?.learningPaths?.length">
                    <h3 class="print-subtitle learning">进阶学习路线</h3>
                    <ul>
                        <li v-for="(item, i) in analysisResult.learningPaths" :key="'l' + i">
                            {{ item.title }}
                        </li>
                    </ul>
                </div>
            </div>

            <div class="print-footer">
                Generated by AI Resume Builder &copy; {{ new Date().getFullYear() }}
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onUnmounted, nextTick } from 'vue';
import { UploadFilled, Document, Back, CircleCheckFilled, WarningFilled, TrendCharts, Aim, Cpu, DocumentChecked, Close, Download, VideoPlay, ArrowRight, FirstAidKit, Guide } from '@element-plus/icons-vue';
import { ElMessage, ElLoading } from 'element-plus';
import axios from 'axios';
import html2pdf from 'html2pdf.js';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { RadarChart } from 'echarts/charts';
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import VChart from 'vue-echarts';
import { API_URLS } from '@/config/api';

// Register ECharts
use([CanvasRenderer, RadarChart, TitleComponent, TooltipComponent, LegendComponent]);

const loading = ref(false);
const loadingProgress = ref(0);
const analysisResult = ref(null);
const file = ref(null);
const jobDescription = ref('');
const radarChartRef = ref(null);
const radarChartImg = ref('');
let progressInterval = null;

const formatList = (list, fallback = '') => {
    return list?.join(', ') || fallback;
};

// Determine score color
const scoreColor = computed(() => {
    const score = analysisResult.value?.score || 0;
    return getMatchColor(score);
});

const getMatchColor = (score) => {
    if (score >= 80) return '#00f3ff';
    if (score >= 60) return '#ffd700';
    return '#ff0055';
};

// Radar Chart Option
const radarOption = computed(() => {
    if (!analysisResult.value) return {};

    return {
        backgroundColor: 'transparent',
        tooltip: {
            trigger: 'item'
        },
        radar: {
            indicator: analysisResult.value.radar,
            radius: '60%',
            splitNumber: 4,
            axisName: {
                color: '#8bacd1',
                fontSize: 12
            },
            splitLine: {
                lineStyle: {
                    color: 'rgba(0, 243, 255, 0.2)'
                }
            },
            splitArea: {
                show: false
            },
            axisLine: {
                lineStyle: {
                    color: 'rgba(0, 243, 255, 0.2)'
                }
            }
        },
        series: [{
            type: 'radar',
            data: [{
                value: analysisResult.value.radar.map(item => item.value),
                name: '能力维度',
                symbol: 'circle',
                symbolSize: 6,
                itemStyle: {
                    color: '#00f3ff'
                },
                lineStyle: {
                    width: 2,
                    color: '#00f3ff',
                    shadowColor: 'rgba(0, 243, 255, 0.5)',
                    shadowBlur: 10
                },
                areaStyle: {
                    color: {
                        type: 'linear',
                        x: 0, y: 0, x2: 0, y2: 1,
                        colorStops: [{
                            offset: 0, color: 'rgba(0, 243, 255, 0.5)'
                        }, {
                            offset: 1, color: 'rgba(0, 243, 255, 0.05)'
                        }]
                    }
                }
            }]
        }]
    };
});

const handleFileChange = (uploadFile) => {
    const isLt10M = uploadFile.size / 1024 / 1024 < 10;
    if (!isLt10M) {
        ElMessage.error('上传文件大小不能超过 10MB!');
        return;
    }
    file.value = uploadFile.raw;
};

const clearFile = (e) => {
    e.stopPropagation();
    file.value = null;
};

const startAnalysis = async () => {
    if (!file.value) return;

    loading.value = true;
    loadingProgress.value = 0;

    // Simulate progress
    progressInterval = setInterval(() => {
        if (loadingProgress.value < 90) {
            loadingProgress.value += Math.random() * 5;
        }
    }, 500);

    const formData = new FormData();
    formData.append('file', file.value);
    if (jobDescription.value) {
        formData.append('jobDescription', jobDescription.value);
    }

    try {
        const token = localStorage.getItem('token');
        const response = await axios.post(API_URLS.analysis.career, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
                'Authorization': `Bearer ${token}`
            }
        });

        loadingProgress.value = 100;
        setTimeout(() => {
            const data = response.data;

            // Mock enrichment for learning paths if not present
            if (!data.learningPaths) {
                // Generate gap filling resources based on weaknesses (Mock logic)
                const weaknessKeywords = data.weaknesses ? data.weaknesses.join(' ') : '';
                const gapResources = [];

                // Algorithm & Data Structure
                if (weaknessKeywords.includes('算法') || weaknessKeywords.includes('数据结构') || weaknessKeywords.includes('基础')) {
                    gapResources.push({ title: 'Hello 算法 - 动画图解', platform: 'Hello-Algo', link: 'https://www.hello-algo.com/', type: 'gap' });
                    gapResources.push({ title: 'LeetCode 热题 100', platform: 'LeetCode', link: 'https://leetcode.cn/problem-list/2cktkvj/', type: 'gap' });
                }

                // Framework Deep Dive
                if (weaknessKeywords.includes('Vue') || weaknessKeywords.includes('源码') || weaknessKeywords.includes('原理') || weaknessKeywords.includes('React')) {
                    gapResources.push({ title: 'Vue.js 设计与实现', platform: '微信读书', link: 'https://weread.qq.com/', type: 'gap' });
                    gapResources.push({ title: 'React 技术揭秘', platform: 'Web', link: 'https://react.iamkasong.com/', type: 'gap' });
                }

                // Engineering & Performance
                if (weaknessKeywords.includes('工程化') || weaknessKeywords.includes('性能') || weaknessKeywords.includes('构建') || weaknessKeywords.includes('部署')) {
                    gapResources.push({ title: '前端工程化体系设计与实践', platform: '掘金小册', link: 'https://juejin.cn/book/6844733759942590472', type: 'gap' });
                    gapResources.push({ title: 'Web 性能优化权威指南', platform: 'MDN', link: 'https://developer.mozilla.org/zh-CN/docs/Web/Performance', type: 'gap' });
                }

                // Fallback / General defaults if few matches (Ensure at least 3 items)
                if (gapResources.length < 3) {
                    gapResources.push({ title: '现代 JavaScript 教程', platform: 'JavaScript.info', link: 'https://zh.javascript.info/', type: 'gap' });
                    gapResources.push({ title: '前端开发者路线图 2024', platform: 'Roadmap.sh', link: 'https://roadmap.sh/frontend', type: 'gap' });
                    gapResources.push({ title: 'GitHub Awesome Frontend', platform: 'GitHub', link: 'https://github.com/sindresorhus/awesome', type: 'gap' });
                }

                // Deduplicate and limit
                const uniqueResources = [];
                const seenLinks = new Set();
                for (const res of gapResources) {
                    if (!seenLinks.has(res.link)) {
                        seenLinks.add(res.link);
                        uniqueResources.push(res);
                    }
                }

                data.gapFilling = uniqueResources.slice(0, 4);

                data.learningPaths = [
                    { title: '阶段一: 夯实基础 (HTML5/CSS3/ES6+)', platform: 'MDN & FreeCodeCamp', link: 'https://developer.mozilla.org/', type: 'roadmap' },
                    { title: '阶段二: 框架进阶 (Vue3 全家桶/React)', platform: '官方文档', link: 'https://vuejs.org/', type: 'roadmap' },
                    { title: '阶段三: 工程化与性能优化 (Vite/Webpack)', platform: 'Bilibili', link: 'https://www.bilibili.com/', type: 'roadmap' },
                    { title: '阶段四: 架构设计与服务端 (Node.js/Micro-frontend)', platform: '极客时间', link: 'https://time.geekbang.org/', type: 'roadmap' }
                ];
            }

            analysisResult.value = data;
            loading.value = false;
        }, 500);

        ElMessage.success('神经元分析完成！');
    } catch (error) {
        console.error('Analysis failed:', error);
        ElMessage.error('分析失败，请稍后重试');
        loading.value = false;
    } finally {
        clearInterval(progressInterval);
    }
};

const resetAnalysis = () => {
    analysisResult.value = null;
    file.value = null;
    jobDescription.value = '';
    radarChartImg.value = '';
};

const downloadReport = async () => {
    if (!analysisResult.value) return;

    // Use an opaque background to hide the print template flickering
    const loadingInstance = ElLoading.service({
        lock: true,
        text: '正在生成诊断报告...',
        background: '#ffffff',
    });

    const element = document.getElementById('print-report-container');

    try {
        // Capture chart image
        if (radarChartRef.value) {
            radarChartImg.value = radarChartRef.value.getDataURL({
                type: 'png',
                pixelRatio: 2,
                backgroundColor: 'transparent'
            });
        }

        await nextTick(); // Wait for image to render in hidden div

        // Temporarily make it visible for html2pdf to capture
        // It will be covered by the full-screen loading mask
        element.style.display = 'block';
        element.style.zIndex = '1000'; // High enough to cover content, lower than Loading mask
        element.style.left = '0';

        const opt = {
            margin: 10,
            filename: `Resume_Analysis_${new Date().getTime()}.pdf`,
            image: { type: 'jpeg', quality: 0.98 },
            html2canvas: { scale: 2, useCORS: true, logging: true },
            jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
        };

        await html2pdf().from(element).set(opt).save();
        ElMessage.success('报告下载成功');
    } catch (error) {
        console.error('PDF generation error:', error);
        ElMessage.error('生成报告失败');
    } finally {
        // Hide it again
        if (element) {
            element.style.display = 'none';
        }
        loadingInstance.close();
    }
};

onUnmounted(() => {
    if (progressInterval) clearInterval(progressInterval);
});
</script>

<style scoped>
/* Theme Variables */
.resume-analyzer {
    --sci-blue: #00f3ff;
    --sci-blue-dim: rgba(0, 243, 255, 0.1);
    --dark-bg: #f8fafc;
    --glass-bg: rgba(255, 255, 255, 0.9);
    --glass-border: rgba(255, 255, 255, 0.6);
    --text-main: #1e293b;
    --text-sub: #64748b;

    position: relative;
    z-index: 1;
    height: 100%;
    overflow-y: auto;
    scrollbar-width: none;
    -ms-overflow-style: none;
    padding: 20px;
    background: radial-gradient(circle at 50% 0%, #e0f2fe 0%, #f1f5f9 100%);
    font-family: 'Rajdhani', 'Inter', sans-serif;
}

.resume-analyzer::-webkit-scrollbar {
    display: none;
}

.analyzer-header {
    text-align: center;
    margin-bottom: 40px;
    position: relative;
}

.sci-fi-title {
    font-size: 3em;
    font-weight: 900;
    color: #0f172a;
    letter-spacing: 4px;
    margin-bottom: 5px;
    text-transform: uppercase;
    text-shadow: 0 2px 10px rgba(15, 23, 42, 0.1);
}

.glitch-text {
    color: #3b82f6;
    position: relative;
    display: inline-block;
}

.subtitle {
    color: #64748b;
    font-size: 1.1em;
    letter-spacing: 2px;
}

/* Input Dashboard */
.input-dashboard {
    display: grid;
    grid-template-columns: 1.2fr 0.8fr;
    gap: 30px;
    max-width: 1200px;
    margin: 0 auto;
}

.glass-panel {
    background: var(--glass-bg);
    border: 1px solid white;
    border-radius: 16px;
    padding: 25px;
    box-shadow:
        0 10px 30px -5px rgba(0, 0, 0, 0.05),
        0 0 0 1px rgba(255, 255, 255, 0.5) inset;
    backdrop-filter: blur(20px);
    transition: all 0.3s ease;
}

.panel-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 20px;
    font-weight: 700;
    color: #334155;
    text-transform: uppercase;
    letter-spacing: 1px;
    border-bottom: 2px solid #f1f5f9;
    padding-bottom: 10px;
}

/* Upload Area Styling */
.upload-dragger :deep(.el-upload-dragger) {
    background: linear-gradient(145deg, #f8fafc, #fff);
    border: 2px dashed #cbd5e1;
    border-radius: 12px;
    height: 300px;
    display: flex;
    justify-content: center;
    align-items: center;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.upload-dragger :deep(.el-upload-dragger:hover) {
    border-color: #3b82f6;
    transform: translateY(-2px);
    box-shadow: 0 10px 20px rgba(59, 130, 246, 0.1);
}

.upload-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
}

.icon-wrapper {
    position: relative;
    width: 80px;
    height: 80px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.upload-icon {
    font-size: 40px;
    color: #94a3b8;
    z-index: 2;
}

.scan-ring {
    position: absolute;
    width: 100%;
    height: 100%;
    border: 2px solid transparent;
    border-top-color: #3b82f6;
    border-radius: 50%;
    animation: spin 3s linear infinite;
}

.file-preview {
    margin-top: 15px;
    display: flex;
    align-items: center;
    gap: 10px;
    background: #eff6ff;
    padding: 8px 15px;
    border-radius: 6px;
    color: #3b82f6;
    font-weight: 600;
}

/* Sci-Fi Input & Button */
.sci-fi-input :deep(.el-textarea__inner) {
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    color: #334155;
    font-family: monospace;
    padding: 15px;
    border-radius: 8px;
    transition: all 0.3s;
}

.sci-fi-input :deep(.el-textarea__inner:focus) {
    background: #fff;
    border-color: #3b82f6;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.sci-fi-btn {
    position: relative;
    width: 100%;
    padding: 18px;
    margin-top: 20px;
    background: linear-gradient(135deg, #3b82f6, #2563eb);
    border: none;
    border-radius: 8px;
    color: white;
    font-weight: 800;
    letter-spacing: 2px;
    cursor: pointer;
    overflow: hidden;
    transition: all 0.3s;
    text-transform: uppercase;
}

.action-buttons {
    display: flex;
    gap: 15px;
}

.sci-fi-btn.download {
    background: linear-gradient(135deg, #10b981, #059669);
    width: auto;
    margin-top: 0;
    padding: 8px 20px;
    font-size: 14px;
    display: flex;
    align-items: center;
    gap: 8px;
}

.sci-fi-btn.download:hover {
    box-shadow: 0 0 15px rgba(16, 185, 129, 0.4);
}

/* Print Template Styles - Hidden from screen but accessible to html2pdf */
.print-template {
    display: none;
    /* Hidden by default */
    position: fixed;
    left: 0;
    top: 0;
    width: 794px;
    /* A4 width */
    background: white;
    color: #333;
    padding: 40px;
    font-family: 'Inter', sans-serif;
    box-sizing: border-box;
    z-index: -100;
}

.print-header {
    text-align: center;
    border-bottom: 2px solid #333;
    padding-bottom: 20px;
    margin-bottom: 30px;
}

.print-header h1 {
    font-size: 24px;
    margin: 0;
    color: #000;
}

.print-header p {
    color: #666;
    margin-top: 10px;
    font-size: 14px;
}

.print-section {
    margin-bottom: 30px;
}

.print-section.center {
    text-align: center;
}

.print-title {
    font-size: 18px;
    border-left: 4px solid #3b82f6;
    padding-left: 10px;
    margin-bottom: 15px;
    color: #1e293b;
    text-align: left;
}

.print-score-box {
    display: flex;
    gap: 20px;
    align-items: center;
    background: #f8fafc;
    padding: 20px;
    border-radius: 8px;
}

.print-score {
    font-size: 48px;
    font-weight: bold;
    color: #3b82f6;
    white-space: nowrap;
}

.print-score span {
    font-size: 16px;
    color: #666;
    font-weight: normal;
}

.print-summary {
    flex: 1;
    line-height: 1.6;
    margin: 0;
    color: #334155;
}

.print-chart-img {
    width: 400px;
    max-width: 100%;
}

.print-card {
    background: #f8fafc;
    padding: 15px;
    border-radius: 8px;
    border: 1px solid #e2e8f0;
}

.print-row {
    margin-bottom: 10px;
    line-height: 1.5;
}

.print-detail-group {
    margin-bottom: 20px;
}

.print-subtitle {
    font-size: 16px;
    margin-bottom: 10px;
}

.print-subtitle.strength {
    color: #10b981;
}

.print-subtitle.weakness {
    color: #f59e0b;
}

.print-subtitle.advice {
    color: #3b82f6;
}

.print-subtitle.gap {
    color: #f43f5e;
}

.print-subtitle.learning {
    color: #8b5cf6;
}

.print-detail-group ul {
    padding-left: 20px;
    margin: 0;
}

.print-detail-group li {
    margin-bottom: 5px;
    line-height: 1.5;
}

.print-footer {
    margin-top: 50px;
    text-align: center;
    color: #999;
    font-size: 12px;
    border-top: 1px solid #eee;
    padding-top: 20px;
}

.sci-fi-btn:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 10px 25px rgba(37, 99, 235, 0.4);
}

.sci-fi-btn:disabled {
    background: #cbd5e1;
    cursor: not-allowed;
    opacity: 0.7;
}

.btn-content {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    position: relative;
    z-index: 2;
}

/* Result Dashboard Styling */
.result-dashboard {
    max-width: 1200px;
    margin: 0 auto;
}

.dashboard-top-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.sci-fi-btn.small {
    width: auto;
    padding: 8px 20px;
    margin: 0;
    font-size: 0.9em;
}

.result-timestamp {
    font-family: monospace;
    color: #94a3b8;
}

.stats-row {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr;
    gap: 20px;
    margin-bottom: 20px;
}

.glass-card {
    background: white;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
    position: relative;
    overflow: hidden;
    transition: all 0.3s ease;
}

/* Hover Effects */
.hover-effect:hover {
    transform: translateY(-5px);
    box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
    border-color: #3b82f6;
}

.card-title {
    font-size: 0.9em;
    color: #64748b;
    text-transform: uppercase;
    letter-spacing: 1px;
    margin-bottom: 15px;
    font-weight: 700;
}

.score-display {
    display: flex;
    justify-content: center;
    padding: 20px 0;
}

.score-inner {
    display: flex;
    flex-direction: column;
    align-items: center;
    line-height: 1;
}

.big-score {
    font-size: 3.5em;
    font-weight: 900;
    color: #0f172a;
}

.score-unit {
    font-size: 0.8em;
    color: #94a3b8;
    font-weight: 700;
}

.score-verdict {
    text-align: center;
    color: #334155;
    font-size: 0.9em;
    line-height: 1.5;
}

.chart-box {
    height: 250px;
}

/* JD Stats */
.jd-stats {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.match-rate {
    text-align: center;
}

.match-rate .value {
    font-size: 3em;
    font-weight: 900;
    text-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.keyword-group {
    margin-bottom: 10px;
}

.group-label {
    display: block;
    font-size: 0.8em;
    color: #94a3b8;
    margin-bottom: 5px;
}

.tags {
    display: flex;
    flex-wrap: wrap;
    gap: 5px;
}

.tech-tag {
    font-size: 0.75em;
    padding: 3px 8px;
    border-radius: 4px;
    font-weight: 600;
}

.tech-tag.missing {
    background: #fef2f2;
    color: #ef4444;
    border: 1px solid #fee2e2;
}

.tech-tag.matching {
    background: #ecfdf5;
    color: #10b981;
    border: 1px solid #d1fae5;
}

/* Detailed Grid */
.details-grid {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr;
    gap: 20px;
}

.detail-panel {
    border-top: 4px solid #e2e8f0;
}

.detail-panel.strength {
    border-color: #10b981;
}

.detail-panel.weakness {
    border-color: #ef4444;
}

.detail-panel.advice {
    border-color: #3b82f6;
}

.detail-panel.gap {
    border-color: #f43f5e;
}

.detail-panel.roadmap {
    border-color: #0ea5e9;
}

.detail-panel.learning {
    border-color: #8b5cf6;
}

.analysis-item {
    display: flex;
    gap: 12px;
    margin-bottom: 15px;
    padding-bottom: 15px;
    border-bottom: 1px dashed #f1f5f9;
}

.analysis-item:last-child {
    border-bottom: none;
    margin-bottom: 0;
}

.index {
    font-family: monospace;
    color: #cbd5e1;
    font-weight: 700;
}

.analysis-item p {
    margin: 0;
    font-size: 0.9em;
    color: #475569;
    line-height: 1.6;
}

/* Resource Item Styling */
.resource-item {
    align-items: center;
    justify-content: space-between;
}

.resource-info {
    display: flex;
    gap: 12px;
    align-items: flex-start;
}

.resource-content {
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.resource-link {
    color: #2563eb;
    text-decoration: none;
    font-weight: 600;
    font-size: 0.95em;
    transition: color 0.2s;
}

.resource-link:hover {
    color: #1e40af;
    text-decoration: underline;
}

.resource-platform {
    font-size: 0.75em;
    color: #94a3b8;
    background: #f1f5f9;
    padding: 2px 6px;
    border-radius: 4px;
    display: inline-block;
    width: fit-content;
}

.arrow-icon {
    color: #cbd5e1;
}

.empty-state {
    text-align: center;
    color: #94a3b8;
    font-size: 0.9em;
    padding: 20px 0;
}

/* Loading Animation */
.loading-container {
    height: 500px;
    display: flex;
    justify-content: center;
    align-items: center;
}

.scanner-hud {
    position: relative;
    width: 300px;
    height: 300px;
    display: flex;
    justify-content: center;
    align-items: center;
}

.scanner-circle {
    position: absolute;
    width: 100%;
    height: 100%;
    border: 2px solid #e2e8f0;
    border-radius: 50%;
    border-top-color: #3b82f6;
    border-bottom-color: #3b82f6;
    animation: spin 2s linear infinite;
}

.scanner-text {
    text-align: center;
    z-index: 2;
}

.progress-percentage {
    font-size: 3.5em;
    font-weight: 900;
    color: #3b82f6;
    font-family: monospace;
    margin-bottom: 10px;
    text-shadow: 0 0 20px rgba(59, 130, 246, 0.4);
}

.blink {
    font-family: monospace;
    font-weight: 700;
    color: #3b82f6;
    animation: blink 1s infinite;
}

.process-steps {
    margin-top: 20px;
    display: flex;
    flex-direction: column;
    gap: 5px;
}

.step {
    font-size: 0.8em;
    color: #94a3b8;
    transition: all 0.3s;
}

.step.active {
    color: #0f172a;
    font-weight: 700;
}

@keyframes spin {
    100% {
        transform: rotate(360deg);
    }
}

@keyframes blink {
    50% {
        opacity: 0.5;
    }
}

@media (max-width: 1024px) {

    .input-dashboard,
    .stats-row,
    .details-grid {
        grid-template-columns: 1fr;
    }
}
</style>