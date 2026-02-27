<template>
    <div class="page-container">
        <el-button @click="goBack" class="back-btn" :icon="ArrowLeft">返回列表</el-button>
        
        <div v-loading="loading" class="detail-container">
            <el-row :gutter="40" v-if="template">
                <!-- Left Column: Preview -->
                <el-col :xs="24" :md="14" :lg="16">
                    <div class="preview-wrapper">
                        <div class="preview-scroll-container" ref="scrollContainer">
                            <div class="preview-content-wrapper" :style="contentStyle">
                                <ShadowPreview v-if="template.html" class="template-preview" :content="template.html" />
                                <el-empty v-else description="预览加载中..." />
                            </div>
                        </div>
                    </div>
                </el-col>

                <!-- Right Column: Info -->
                <el-col :xs="24" :md="10" :lg="8">
                    <el-card shadow="never" class="info-card">
                        <h1 class="title">{{ template.name }}</h1>
                        
                        <div class="price-tag">
                            <el-tag :type="template.type === 'free' ? 'success' : 'warning'" size="large" effect="dark">
                                {{ template.type === 'free' ? '免费' : `¥${template.price}` }}
                            </el-tag>
                        </div>

                        <div class="description-section">
                            <h3>模板简介</h3>
                            <p class="description">{{ template.description || '暂无描述' }}</p>
                        </div>

                        <div class="meta-info">
                            <div class="meta-item">
                                <span class="label">作者:</span>
                                <span class="value">{{ template.author ? template.author.username : '未知' }}</span>
                            </div>
                            <div class="meta-item">
                                <span class="label">更新时间:</span>
                                <span class="value">{{ formatDate(template.updatedAt || template.createdAt) }}</span>
                            </div>
                        </div>

                        <div class="actions">
                            <el-button type="primary" size="large" class="action-btn" @click="handleAction">
                                {{ template.type === 'free' ? '立即使用' : '购买模板' }}
                            </el-button>
                        </div>
                    </el-card>
                </el-col>
            </el-row>
            <el-empty v-else-if="!loading" description="未找到模板信息" />
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import { ElNotification } from 'element-plus';
import { ArrowLeft } from '@element-plus/icons-vue';
import { API_URLS, getHeaders } from '@/config/api';
import ShadowPreview from '@/components/ShadowPreview.vue';

const route = useRoute();
const router = useRouter();
const template = ref(null);
const loading = ref(false);
const scrollContainer = ref(null);
const scale = ref(1);

const contentStyle = computed(() => ({
    transform: `scale(${scale.value})`,
    transformOrigin: 'top center',
    width: '210mm',
    minHeight: '297mm',
}));

const calculateScale = () => {
    if (!scrollContainer.value) return;
    const containerWidth = scrollContainer.value.clientWidth;
    // 210mm in pixels is approx 794px. Add some padding (e.g. 40px)
    const targetWidth = 794; 
    const padding = 40;
    const availableWidth = Math.max(containerWidth - padding, 300); // Minimum 300px
    
    // Calculate scale to fit width
    scale.value = Math.min(availableWidth / targetWidth, 1); // Max scale 1
};

const goBack = () => {
    router.push('/store');
};

const formatDate = (dateString) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString();
};

const fetchTemplateDetail = async () => {
    const id = route.params.id;
    if (!id) return;

    loading.value = true;
    try {
        // Use the defined API endpoint for getting template by ID
        const response = await axios.get(API_URLS.templates.byId(id), {
            headers: getHeaders()
        });
        
        template.value = response.data;
        
        // Process HTML content similar to TemplateStore
        if (template.value.htmlContent) {
             template.value.html = template.value.htmlContent;
        } else if (template.value.templatePath) {
             const res = await fetch(template.value.templatePath);
             template.value.html = await res.text();
        } else {
             template.value.html = `<div style='text-align: center; padding: 50px;'>预览不可用</div>`;
        }

    } catch (error) {
        console.error('Error fetching template detail:', error);
        ElNotification({
            title: '错误',
            message: '获取模板详情失败',
            type: 'error',
        });
    } finally {
        loading.value = false;
        // Recalculate scale after loading
        setTimeout(calculateScale, 100);
    }
};

const handleAction = () => {
    if (template.value.type === 'free') {
        router.push('/generator');
    } else {
        ElNotification({
            title: '提示',
            message: '付费功能即将上线，敬请期待！',
            type: 'info',
        });
    }
};

onMounted(() => {
    fetchTemplateDetail();
    window.addEventListener('resize', calculateScale);
    // Initial calculation
    setTimeout(calculateScale, 100);
});

onUnmounted(() => {
    window.removeEventListener('resize', calculateScale);
});
</script>

<style scoped>
.page-container {
    padding: 24px;
    max-width: 1200px;
    margin: 0 auto;
    background: radial-gradient(1200px 600px at 20% -20%, rgba(14, 165, 233, 0.08) 40%, var(--bg-primary) 100%);
    min-height: 100%;
}

.back-btn {
    margin-bottom: 20px;
    background: linear-gradient(145deg, rgba(30, 41, 59, 0.8), rgba(15, 23, 42, 0.9)) !important;
    border: 1px solid rgba(255, 255, 255, 0.12) !important;
    color: #f1f5f9 !important;
    font-weight: 600 !important;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
    border-radius: 10px !important;
}

.back-btn:hover {
    background: linear-gradient(145deg, rgba(30, 41, 59, 0.95), rgba(15, 23, 42, 1)) !important;
    border-color: rgba(14, 165, 233, 0.5) !important;
    color: #22d3ee !important;
    transform: translateX(-4px);
    box-shadow: 0 4px 16px rgba(14, 165, 233, 0.2) !important;
}

:deep(.back-btn .el-icon) {
    color: inherit !important;
}

.detail-container {
    min-height: 500px;
}

.preview-wrapper {
    background: linear-gradient(145deg, #1e293b 0%, #0f172a 100%);
    border-radius: 20px;
    padding: 24px;
    height: 800px;
    border: 1px solid rgba(255, 255, 255, 0.1);
    display: flex;
    justify-content: center;
    overflow: hidden;
    box-shadow: 0 25px 70px rgba(0, 0, 0, 0.4), inset 0 1px 0 rgba(255, 255, 255, 0.05);
    backdrop-filter: blur(16px);
}

.preview-scroll-container {
    width: 100%;
    height: 100%;
    overflow-y: auto;
    overflow-x: hidden;
    display: flex;
    justify-content: center;
    align-items: flex-start;
    padding-top: 20px;
}

.preview-scroll-container::-webkit-scrollbar {
    width: 8px;
}

.preview-scroll-container::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.03);
    border-radius: 4px;
}

.preview-scroll-container::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.12);
    border-radius: 4px;
}

.preview-scroll-container::-webkit-scrollbar-thumb:hover {
    background: rgba(14, 165, 233, 0.4);
}

.preview-content-wrapper {
    background: white;
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3);
    transition: transform 0.1s ease-out;
    border-radius: 4px;
    overflow: hidden;
}

.template-preview {
    /* Styles are now handled by wrapper and dynamic binding */
}

.info-card {
    position: sticky;
    top: 24px;
    background: linear-gradient(145deg, rgba(30, 41, 59, 0.8), rgba(15, 23, 42, 0.9)) !important;
    border: 1px solid rgba(255, 255, 255, 0.12) !important;
    border-radius: 20px !important;
    box-shadow: 0 20px 50px rgba(0, 0, 0, 0.4), inset 0 1px 0 rgba(255, 255, 255, 0.05) !important;
    backdrop-filter: blur(20px) !important;
    -webkit-backdrop-filter: blur(20px) !important;
}

:deep(.el-card__body) {
    padding: 24px !important;
}

.title {
    margin: 0 0 16px 0;
    font-size: 26px;
    color: #f8fafc;
    font-weight: 800;
    letter-spacing: -0.02em;
    text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.price-tag {
    margin-bottom: 24px;
}

:deep(.el-tag--success.el-tag--dark) {
    background: linear-gradient(135deg, #22c55e, #16a34a) !important;
    border-color: transparent !important;
    color: white !important;
    font-weight: 600;
}

:deep(.el-tag--warning.el-tag--dark) {
    background: linear-gradient(135deg, #f59e0b, #d97706) !important;
    border-color: transparent !important;
    color: white !important;
    font-weight: 600;
}

.description-section {
    margin-bottom: 24px;
    padding-bottom: 24px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.description-section h3 {
    margin: 0 0 12px 0;
    font-size: 16px;
    color: #e2e8f0;
    font-weight: 700;
    letter-spacing: 0.02em;
}

.description {
    color: #94a3b8;
    line-height: 1.7;
    margin: 0;
    font-size: 15px;
}

.meta-info {
    margin-bottom: 30px;
    background: rgba(255, 255, 255, 0.03);
    border-radius: 12px;
    padding: 16px;
}

.meta-item {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12px;
    font-size: 14px;
}

.meta-item:last-child {
    margin-bottom: 0;
}

.meta-item .label {
    color: #64748b;
    font-weight: 500;
}

.meta-item .value {
    color: #e2e8f0;
    font-weight: 500;
}

.action-btn {
    width: 100%;
    background: linear-gradient(135deg, #0ea5e9, #22d3ee) !important;
    border: none !important;
    color: #0f172a !important;
    font-weight: 700 !important;
    font-size: 16px !important;
    height: 52px !important;
    border-radius: 12px !important;
    box-shadow: 0 8px 24px rgba(14, 165, 233, 0.35) !important;
    transition: all 0.3s ease !important;
}

.action-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 32px rgba(14, 165, 233, 0.5) !important;
}

/* Loading state */
:deep(.el-loading-mask) {
    background: rgba(11, 18, 32, 0.85) !important;
}

:deep(.el-loading-spinner .el-loading-text) {
    color: #f1f5f9 !important;
}

:deep(.el-loading-spinner .path) {
    stroke: #0ea5e9 !important;
}

/* Empty state */
:deep(.el-empty__description p) {
    color: #94a3b8 !important;
}
</style>
