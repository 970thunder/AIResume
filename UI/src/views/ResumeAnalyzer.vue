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
                <div v-else-if="loading" class="loading-dashboard" :style="loadingContainerStyle">
                    <div class="quantum-loader">
                        <div class="circle-outer"></div>
                        <div class="circle-inner"></div>
                        <div class="particles">
                            <div class="particle" v-for="n in 12" :key="n" :style="{ '--i': n }"></div>
                        </div>
                        <div class="percentage">{{ Math.floor(loadingProgress) }}%</div>
                        <div class="status-text">SYSTEM ANALYZING...</div>
                    </div>
                    <div class="loading-steps">
                        <div class="step" :class="{ active: loadingProgress > 10 }">解析文档结构</div>
                        <div class="step" :class="{ active: loadingProgress > 40 }">提取关键技能</div>
                        <div class="step" :class="{ active: loadingProgress > 70 }">计算岗位匹配度</div>
                        <div class="step" :class="{ active: loadingProgress > 90 }">生成优化策略</div>
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

    </div>
</template>

<script setup>
import { ref, computed, onUnmounted, nextTick, onMounted } from 'vue';
import html2canvas from 'html2canvas';
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

// 鼠标位置跟踪
const mouseX = ref(0);
const mouseY = ref(0);
const windowWidth = ref(window.innerWidth);
const windowHeight = ref(window.innerHeight);

// 更新鼠标位置
const handleMouseMove = (event) => {
    mouseX.value = event.clientX;
    mouseY.value = event.clientY;
};

// 计算加载容器的样式
const loadingContainerStyle = computed(() => {
    // 计算鼠标相对于屏幕中心的偏移比例 (-0.5 到 0.5)
    const offsetX = (mouseX.value / windowWidth.value) - 0.5;
    const offsetY = (mouseY.value / windowHeight.value) - 0.5;

    // 最大旋转角度
    const maxRotate = 20;

    return {
        transform: `perspective(1000px) rotateY(${offsetX * maxRotate}deg) rotateX(${-offsetY * maxRotate}deg)`,
        transition: 'transform 0.1s ease-out'
    };
});

onMounted(() => {
    window.addEventListener('mousemove', handleMouseMove);
    window.addEventListener('resize', () => {
        windowWidth.value = window.innerWidth;
        windowHeight.value = window.innerHeight;
    });
});

onUnmounted(() => {
    window.removeEventListener('mousemove', handleMouseMove);
});

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
            trigger: 'item',
            backgroundColor: 'rgba(15, 23, 42, 0.9)',
            borderColor: 'rgba(255, 255, 255, 0.12)',
            textStyle: {
                color: '#e6edf3'
            }
        },
        radar: {
            indicator: analysisResult.value.radar,
            radius: '60%',
            splitNumber: 4,
            axisName: {
                color: '#94a3b8',
                fontSize: 12
            },
            splitLine: {
                lineStyle: {
                    color: 'rgba(14, 165, 233, 0.2)'
                }
            },
            splitArea: {
                show: false
            },
            axisLine: {
                lineStyle: {
                    color: 'rgba(14, 165, 233, 0.2)'
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
                    color: '#0ea5e9'
                },
                lineStyle: {
                    width: 2,
                    color: '#0ea5e9',
                    shadowColor: 'rgba(14, 165, 233, 0.5)',
                    shadowBlur: 10
                },
                areaStyle: {
                    color: {
                        type: 'linear',
                        x: 0, y: 0, x2: 0, y2: 1,
                        colorStops: [{
                            offset: 0, color: 'rgba(14, 165, 233, 0.5)'
                        }, {
                            offset: 1, color: 'rgba(14, 165, 233, 0.05)'
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
/* Dark Glass Theme Variables - With explicit fallback colors */
.resume-analyzer {
    --sci-blue: #0ea5e9;
    --sci-blue-dim: rgba(14, 165, 233, 0.1);
    --dark-bg: #0b1220;
    --glass-bg: rgba(255, 255, 255, 0.06);
    --glass-bg-hover: rgba(255, 255, 255, 0.1);
    --glass-border: rgba(255, 255, 255, 0.12);
    --glass-border-hover: rgba(255, 255, 255, 0.2);
    --text-main: #e6edf3;
    --text-sub: #94a3b8;
    --glass-blur: blur(14px);
    --accent-primary: #0ea5e9;
    --accent-secondary: #22d3ee;
    --fg-primary: #e6edf3;
    --fg-secondary: #cbd5e1;
    --fg-muted: #94a3b8;
    --bg-primary: #0b1220;
    --success: #22c55e;
    --danger: #ef4444;
    --warning: #f59e0b;
    --info: #3b82f6;
    --font-sans: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
    --font-mono: 'JetBrains Mono', 'Fira Code', monospace;

    position: relative;
    z-index: 1;
    min-height: 100vh;
    overflow-y: auto;
    scrollbar-width: none;
    -ms-overflow-style: none;
    padding: 20px;
    background: radial-gradient(circle at 50% 0%, rgba(14, 165, 233, 0.1) 0%, var(--bg-primary) 100%);
    font-family: var(--font-sans);
    animation: fadeIn 0.5s ease-out;
}

.resume-analyzer::-webkit-scrollbar {
    display: none;
}

.analyzer-header {
    text-align: center;
    margin-bottom: 40px;
    position: relative;
    animation: fadeInDown 0.6s ease-out;
}

.sci-fi-title {
    font-size: 3em;
    font-weight: 900;
    color: var(--fg-primary);
    letter-spacing: 4px;
    margin-bottom: 5px;
    text-transform: uppercase;
    text-shadow: 0 2px 20px rgba(14, 165, 233, 0.3);
}

.glitch-text {
    background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary));
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    position: relative;
    display: inline-block;
}

.subtitle {
    color: var(--fg-muted);
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
    animation: fadeInUp 0.6s ease-out;
}

.glass-panel {
    background: var(--glass-bg);
    border: 1px solid var(--glass-border);
    border-radius: 16px;
    padding: 25px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.35);
    backdrop-filter: var(--glass-blur);
    -webkit-backdrop-filter: var(--glass-blur);
    transition: all 0.3s ease;
}

.glass-panel:hover {
    background: var(--glass-bg-hover);
    border-color: var(--glass-border-hover);
}

.panel-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 20px;
    font-weight: 700;
    color: var(--fg-primary);
    text-transform: uppercase;
    letter-spacing: 1px;
    border-bottom: 1px solid var(--glass-border);
    padding-bottom: 10px;
}

/* Upload Area Styling */
.upload-dragger :deep(.el-upload-dragger) {
    background: rgba(15, 23, 42, 0.4);
    border: 2px dashed var(--glass-border);
    border-radius: 12px;
    height: 300px;
    display: flex;
    justify-content: center;
    align-items: center;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.upload-dragger :deep(.el-upload-dragger:hover) {
    border-color: var(--accent-primary);
    background: rgba(15, 23, 42, 0.6);
    box-shadow: 0 0 30px rgba(14, 165, 233, 0.2);
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
    color: var(--fg-muted);
    z-index: 2;
}

.scan-ring {
    position: absolute;
    width: 100%;
    height: 100%;
    border: 2px solid transparent;
    border-top-color: var(--accent-primary);
    border-radius: 50%;
    animation: spin 3s linear infinite;
}

.file-preview {
    margin-top: 15px;
    display: flex;
    align-items: center;
    gap: 10px;
    background: rgba(14, 165, 233, 0.15);
    padding: 8px 15px;
    border-radius: 8px;
    color: var(--accent-primary);
    font-weight: 600;
    border: 1px solid rgba(14, 165, 233, 0.3);
}

/* Sci-Fi Input & Button */
.sci-fi-input :deep(.el-textarea__inner) {
    background: rgba(15, 23, 42, 0.6);
    border: 1px solid var(--glass-border);
    color: var(--fg-primary);
    font-family: var(--font-mono);
    padding: 15px;
    border-radius: 12px;
    transition: all 0.3s;
}

.sci-fi-input :deep(.el-textarea__inner::placeholder) {
    color: var(--fg-muted);
}

.sci-fi-input :deep(.el-textarea__inner:focus) {
    background: rgba(15, 23, 42, 0.8);
    border-color: var(--accent-primary);
    box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.2);
}

.sci-fi-btn {
    position: relative;
    width: 100%;
    padding: 18px;
    margin-top: 20px;
    background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary));
    border: none;
    border-radius: 12px;
    color: var(--bg-primary);
    font-weight: 800;
    letter-spacing: 2px;
    cursor: pointer;
    overflow: hidden;
    transition: all 0.3s;
    text-transform: uppercase;
    box-shadow: 0 0 30px rgba(14, 165, 233, 0.3);
}

.action-buttons {
    display: flex;
    gap: 15px;
}

.sci-fi-btn.download {
    background: linear-gradient(135deg, var(--success), #4ade80);
    width: auto;
    margin-top: 0;
    padding: 8px 20px;
    font-size: 14px;
    display: flex;
    align-items: center;
    gap: 8px;
    color: white;
}

.sci-fi-btn.download:hover {
    box-shadow: 0 0 20px rgba(34, 197, 94, 0.4);
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
    box-shadow: 0 0 40px rgba(14, 165, 233, 0.5);
}

.sci-fi-btn:disabled {
    background: var(--glass-bg);
    border: 1px solid var(--glass-border);
    color: var(--fg-muted);
    cursor: not-allowed;
    opacity: 0.7;
    box-shadow: none;
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
    animation: fadeInUp 0.6s ease-out;
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
    font-family: var(--font-mono);
    color: var(--fg-muted);
}

.stats-row {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr;
    gap: 20px;
    margin-bottom: 20px;
}

.glass-card {
    background: var(--glass-bg);
    border: 1px solid var(--glass-border);
    border-radius: 16px;
    padding: 20px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.35);
    backdrop-filter: var(--glass-blur);
    -webkit-backdrop-filter: var(--glass-blur);
    position: relative;
    overflow: hidden;
    transition: all 0.3s ease;
}

/* Hover Effects */
.hover-effect:hover {
    transform: translateY(-5px);
    background: var(--glass-bg-hover);
    border-color: var(--accent-primary);
    box-shadow: 0 15px 40px rgba(14, 165, 233, 0.2);
}

.card-title {
    font-size: 0.9em;
    color: var(--fg-muted);
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
    color: var(--fg-primary);
}

.score-unit {
    font-size: 0.8em;
    color: var(--fg-muted);
    font-weight: 700;
}

.score-verdict {
    text-align: center;
    color: var(--fg-secondary);
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
    text-shadow: 0 0 20px rgba(14, 165, 233, 0.3);
}

.keyword-group {
    margin-bottom: 10px;
}

.group-label {
    display: block;
    font-size: 0.8em;
    color: var(--fg-muted);
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
    border-radius: 6px;
    font-weight: 600;
}

.tech-tag.missing {
    background: rgba(239, 68, 68, 0.15);
    color: var(--danger);
    border: 1px solid rgba(239, 68, 68, 0.3);
}

.tech-tag.matching {
    background: rgba(34, 197, 94, 0.15);
    color: var(--success);
    border: 1px solid rgba(34, 197, 94, 0.3);
}

/* Detailed Grid */
.details-grid {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr;
    gap: 20px;
}

.detail-panel {
    border-top: 4px solid var(--glass-border);
}

.detail-panel.strength {
    border-color: var(--success);
}

.detail-panel.weakness {
    border-color: var(--danger);
}

.detail-panel.advice {
    border-color: var(--accent-primary);
}

.detail-panel.gap {
    border-color: var(--warning);
}

.detail-panel.roadmap {
    border-color: var(--info);
}

.detail-panel.learning {
    border-color: #8b5cf6;
}

.analysis-item {
    display: flex;
    gap: 12px;
    margin-bottom: 15px;
    padding-bottom: 15px;
    border-bottom: 1px dashed var(--glass-border);
}

.analysis-item:last-child {
    border-bottom: none;
    margin-bottom: 0;
}

.index {
    font-family: var(--font-mono);
    color: var(--fg-muted);
    font-weight: 700;
}

.analysis-item p {
    margin: 0;
    font-size: 0.9em;
    color: var(--fg-secondary);
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
    color: var(--accent-primary);
    text-decoration: none;
    font-weight: 600;
    font-size: 0.95em;
    transition: color 0.2s;
}

.resource-link:hover {
    color: var(--accent-secondary);
    text-decoration: underline;
}

.resource-platform {
    font-size: 0.75em;
    color: var(--fg-muted);
    background: var(--glass-bg);
    padding: 2px 6px;
    border-radius: 4px;
    display: inline-block;
    width: fit-content;
    border: 1px solid var(--glass-border);
}

.arrow-icon {
    color: var(--fg-muted);
}

.empty-state {
    text-align: center;
    color: var(--fg-muted);
    font-size: 0.9em;
    padding: 20px 0;
}

/* Loading Animation */
.loading-dashboard {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 600px;
    transform-style: preserve-3d;
}

.quantum-loader {
    position: relative;
    width: 200px;
    height: 200px;
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
    margin-bottom: 40px;
}

.circle-outer {
    position: absolute;
    width: 100%;
    height: 100%;
    border-radius: 50%;
    border: 4px solid rgba(14, 165, 233, 0.1);
    border-top-color: var(--accent-primary);
    animation: spin 2s linear infinite;
    box-shadow: 0 0 30px rgba(14, 165, 233, 0.3);
}

.circle-inner {
    position: absolute;
    width: 70%;
    height: 70%;
    border-radius: 50%;
    border: 4px solid rgba(14, 165, 233, 0.1);
    border-bottom-color: var(--accent-secondary);
    animation: spin 1.5s linear infinite reverse;
}

.particles {
    position: absolute;
    width: 100%;
    height: 100%;
    animation: spin 4s linear infinite;
}

.particle {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 6px;
    height: 6px;
    background: var(--accent-primary);
    border-radius: 50%;
    transform: translate(-50%, -50%) rotate(calc(30deg * var(--i))) translateY(-110px);
    box-shadow: 0 0 15px var(--accent-primary);
}

.percentage {
    font-size: 36px;
    font-weight: bold;
    color: var(--accent-primary);
    z-index: 10;
    text-shadow: 0 0 20px rgba(14, 165, 233, 0.5);
}

.status-text {
    margin-top: 10px;
    font-size: 14px;
    color: var(--accent-primary);
    letter-spacing: 2px;
    z-index: 10;
}

.loading-steps {
    display: flex;
    flex-direction: column;
    gap: 15px;
    width: 300px;
}

.step {
    padding: 10px 15px;
    background: var(--glass-bg);
    border-radius: 8px;
    color: var(--fg-muted);
    font-size: 14px;
    transition: all 0.3s;
    border: 1px solid var(--glass-border);
}

.step.active {
    background: rgba(14, 165, 233, 0.15);
    color: var(--accent-primary);
    border-color: rgba(14, 165, 233, 0.3);
    font-weight: 600;
    transform: translateX(10px);
    box-shadow: 0 4px 15px rgba(14, 165, 233, 0.2);
}

@media (max-width: 1024px) {

    .input-dashboard,
    .stats-row,
    .details-grid {
        grid-template-columns: 1fr;
    }
}
</style>