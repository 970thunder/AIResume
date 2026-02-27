<template>
    <div class="page-container">
        <el-card shadow="never" class="store-card">
            <template #header>
                <div class="card-header">
                    <div class="header-left">
                        <div class="search-container">
                            <el-input v-model="searchQuery" placeholder="搜索模板..." class="search-input"
                                :prefix-icon="Search" clearable @input="handleSearch" />
                        </div>
                        <router-link to="/template/create">
                            <el-button type="primary" class="create-btn">
                                <el-icon class="el-icon--left">
                                    <Plus />
                                </el-icon>
                                创建模板
                            </el-button>
                        </router-link>
                    </div>
                    <div class="filter-buttons">
                        <el-radio-group v-model="currentFilter" @change="filterTemplates">
                            <el-radio-button value="all" label="all">全部</el-radio-button>
                            <el-radio-button value="free" label="free">免费</el-radio-button>
                            <el-radio-button value="premium" label="premium">付费</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
            </template>

            <div v-loading="loading" class="templates-container">
                <el-row :gutter="24" v-if="filteredTemplates.length > 0">
                    <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="template in filteredTemplates" :key="template.id">
                        <el-card shadow="hover" class="template-card" :body-style="{ padding: '0px' }">
                            <div class="template-preview-wrapper" v-loading="templateLoadingStates[template.id]">
                                <div class="preview-container">
                                    <ShadowPreview v-if="template.html" class="template-html-content"
                                        :content="template.html" />
                                    <el-empty v-else description="预览加载中..." :image-size="60" />
                                </div>
                                <div class="preview-overlay">
                                    <el-button type="primary" @click="viewTemplateDetail(template)">
                                        查看详情
                                    </el-button>
                                </div>
                            </div>
                            <div class="template-info">
                                <div class="info-header">
                                    <h3 class="template-name" :title="template.name">{{ template.name }}</h3>
                                    <el-tag :type="template.type === 'free' ? 'success' : 'warning'" size="small"
                                        effect="plain">
                                        {{ template.type === 'free' ? '免费' : `¥${template.price}` }}
                                    </el-tag>
                                </div>
                                <p class="description" :title="template.description">{{ template.description || '暂无描述'
                                    }}</p>
                            </div>
                        </el-card>
                    </el-col>
                </el-row>
                <el-empty v-else description="未找到匹配的模板" />
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { ElNotification } from 'element-plus';
import { Search, Plus } from '@element-plus/icons-vue';
import { API_URLS, getHeaders } from '@/config/api';
import ShadowPreview from '@/components/ShadowPreview.vue';

const router = useRouter();
const templates = ref([]);
const loading = ref(false);
const currentFilter = ref('all');
const templateLoadingStates = ref({});
const searchQuery = ref('');

// 搜索处理函数
const handleSearch = () => {
    // 这里不需要特别的处理，因为我们使用计算属性来过滤模板
};

// 过滤后的模板列表
const filteredTemplates = computed(() => {
    return templates.value.filter(template => {
        const searchLower = searchQuery.value.toLowerCase();
        return (
            template.name.toLowerCase().includes(searchLower) ||
            template.description?.toLowerCase().includes(searchLower)
        );
    });
});

// 设置模板加载状态
const setTemplateLoading = (templateId, isLoading) => {
    templateLoadingStates.value[templateId] = isLoading;
};

// 加载模板HTML内容
const loadTemplateHtml = async (templates) => {
    // 初始化所有模板的加载状态为true
    templates.forEach(template => {
        setTemplateLoading(template.id, true);
    });

    // 并行加载所有模板，而不是串行
    const loadPromises = templates.map(async (template) => {
        try {
            if (template.htmlContent) {
                // 优先使用数据库中的 htmlContent
                template.html = template.htmlContent;
            } else if (template.templatePath) {
                // Fallback 到旧的 templatePath 方式
                const response = await fetch(template.templatePath);
                template.html = await response.text();
            } else {
                template.html = `<div style='text-align: center; padding: 20px; color: #909399;'>预览不可用</div>`;
            }
        } catch (error) {
            console.error(`Error loading template ${template.templatePath || `ID: ${template.id}`}:`, error);
            template.html = `<div style='text-align: center; padding: 20px; color: #f56c6c;'>加载模板失败</div>`;
        } finally {
            setTemplateLoading(template.id, false);
        }
    });

    await Promise.all(loadPromises);
};

// 获取模板列表
const fetchTemplates = async () => {
    loading.value = true;
    try {
        const response = await axios.get(API_URLS.templates.all, {
            headers: getHeaders()
        });
        templates.value = response.data;
        await loadTemplateHtml(templates.value);
    } catch (error) {
        console.error('Error fetching templates:', error);
        ElNotification({
            title: '错误',
            message: '获取模板列表失败',
            type: 'error',
        });
    } finally {
        loading.value = false;
    }
};

// 筛选模板
const filterTemplates = async (filter) => {
    loading.value = true;
    try {
        const url = filter === 'free'
            ? API_URLS.templates.free
            : filter === 'premium'
                ? API_URLS.templates.premium
                : API_URLS.templates.all;

        const response = await axios.get(url, {
            headers: getHeaders()
        });
        templates.value = response.data;
        await loadTemplateHtml(templates.value);
    } catch (error) {
        console.error('Error filtering templates:', error);
        ElNotification({
            title: '错误',
            message: '筛选模板失败',
            type: 'error',
        });
    } finally {
        loading.value = false;
    }
};

// 使用模板
const viewTemplateDetail = (template) => {
    router.push(`/template/${template.id}`);
};

onMounted(() => {
    fetchTemplates();
});
</script>

<style scoped>
.page-container {
    padding: 24px;
    width: 100%;
    box-sizing: border-box;
    max-width: none;
    background: radial-gradient(1200px 600px at 20% -20%, rgba(14, 165, 233, 0.08) 40%, var(--bg-primary) 100%);
    min-height: 100%;
}

.store-card {
    border-radius: 16px !important;
    border: 1px solid rgba(255, 255, 255, 0.1) !important;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3) !important;
    background: linear-gradient(145deg, rgba(30, 41, 59, 0.6), rgba(15, 23, 42, 0.8)) !important;
    backdrop-filter: blur(16px) !important;
    -webkit-backdrop-filter: blur(16px) !important;
}

:deep(.el-card__header) {
    border-bottom: 1px solid rgba(255, 255, 255, 0.08) !important;
    padding: 20px 24px !important;
}

:deep(.el-card__body) {
    padding: 24px !important;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 16px;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    min-width: 0;
}

.search-container {
    width: 300px;
}

:deep(.search-input .el-input__wrapper) {
    background: rgba(15, 23, 42, 0.8) !important;
    border: 1px solid rgba(255, 255, 255, 0.12) !important;
    box-shadow: none !important;
    border-radius: 10px !important;
}

:deep(.search-input .el-input__wrapper:hover) {
    border-color: rgba(255, 255, 255, 0.25) !important;
}

:deep(.search-input .el-input__wrapper.is-focus) {
    border-color: #0ea5e9 !important;
    box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.2) !important;
}

:deep(.search-input .el-input__inner) {
    color: #f1f5f9 !important;
}

:deep(.search-input .el-input__inner::placeholder) {
    color: #64748b !important;
}

:deep(.search-input .el-input__prefix) {
    color: #64748b !important;
}

.create-btn {
    font-weight: 600 !important;
    background: linear-gradient(135deg, #0ea5e9, #22d3ee) !important;
    border: none !important;
    color: #0f172a !important;
    box-shadow: 0 8px 24px rgba(14, 165, 233, 0.35) !important;
    transition: all 0.3s ease !important;
}

.create-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 32px rgba(14, 165, 233, 0.45) !important;
}

/* Radio Button Group */
:deep(.el-radio-group) {
    display: flex;
    gap: 0;
}

:deep(.el-radio-button__inner) {
    background: rgba(30, 41, 59, 0.8) !important;
    border: 1px solid rgba(255, 255, 255, 0.12) !important;
    color: #e2e8f0 !important;
    font-weight: 600 !important;
    transition: all 0.3s ease !important;
}

:deep(.el-radio-button:first-child .el-radio-button__inner) {
    border-radius: 10px 0 0 10px !important;
}

:deep(.el-radio-button:last-child .el-radio-button__inner) {
    border-radius: 0 10px 10px 0 !important;
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
    background: linear-gradient(135deg, #0ea5e9, #22d3ee) !important;
    border-color: #0ea5e9 !important;
    color: #0f172a !important;
    box-shadow: 0 4px 12px rgba(14, 165, 233, 0.3) !important;
}

:deep(.el-radio-button__inner:hover) {
    color: #22d3ee !important;
    background: rgba(30, 41, 59, 0.95) !important;
}

.templates-container {
    min-height: 400px;
}

.template-card {
    margin-bottom: 24px;
    transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
    border: 1px solid rgba(255, 255, 255, 0.12) !important;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3) !important;
    border-radius: 16px !important;
    overflow: hidden;
    background: linear-gradient(145deg, rgba(30, 41, 59, 0.7), rgba(15, 23, 42, 0.8)) !important;
    backdrop-filter: blur(16px) !important;
}

.template-card:hover {
    transform: translateY(-8px) scale(1.02);
    box-shadow: 0 20px 50px rgba(14, 165, 233, 0.25), 0 0 0 1px rgba(14, 165, 233, 0.3) !important;
    border-color: rgba(14, 165, 233, 0.4) !important;
}

:deep(.el-card__body) {
    padding: 0 !important;
}

.template-preview-wrapper {
    height: 360px;
    overflow: hidden;
    background: linear-gradient(145deg, #1e293b 0%, #0f172a 100%);
    position: relative;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
    width: 100%;
    max-width: 100%;
    min-width: 0;
}

.preview-container {
    width: 100%;
    height: 100%;
    overflow: hidden;
    position: relative;
    max-width: 100%;
    min-width: 0;
    contain: layout style paint;
}

.template-html-content {
    width: 210mm;
    height: 297mm;
    background: white;
    transform: translateX(-50%) scale(0.25);
    transform-origin: top center;
    overflow: hidden;
    pointer-events: none;
    padding: 20px;
    box-sizing: border-box;
    position: absolute;
    left: 50%;
    top: 0;
    min-width: 0;
    max-width: 100vw;
}

.preview-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(15, 23, 42, 0.75);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: all 0.35s ease;
    backdrop-filter: blur(6px);
}

.template-card:hover .preview-overlay {
    opacity: 1;
}

.preview-overlay :deep(.el-button--primary) {
    background: linear-gradient(135deg, #0ea5e9, #22d3ee) !important;
    border: none !important;
    color: #0f172a !important;
    font-weight: 600 !important;
    box-shadow: 0 8px 24px rgba(14, 165, 233, 0.4) !important;
}

.preview-overlay :deep(.el-button--primary:hover) {
    transform: translateY(-2px);
    box-shadow: 0 12px 32px rgba(14, 165, 233, 0.5) !important;
}

.template-info {
    padding: 18px 20px;
    background: linear-gradient(180deg, rgba(15, 23, 42, 0.6), rgba(15, 23, 42, 0.9));
    border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.info-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
}

.template-name {
    margin: 0;
    font-size: 17px;
    font-weight: 700;
    color: #f1f5f9;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 70%;
    letter-spacing: 0.02em;
}

.description {
    color: #94a3b8;
    font-size: 14px;
    margin: 0;
    line-height: 1.6;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    min-height: 44px;
}

/* Tag styles */
:deep(.el-tag--success) {
    background: linear-gradient(135deg, rgba(34, 197, 94, 0.25), rgba(22, 163, 74, 0.2)) !important;
    border-color: rgba(34, 197, 94, 0.4) !important;
    color: #4ade80 !important;
    font-weight: 600;
}

:deep(.el-tag--warning) {
    background: linear-gradient(135deg, rgba(245, 158, 11, 0.25), rgba(217, 119, 6, 0.2)) !important;
    border-color: rgba(245, 158, 11, 0.4) !important;
    color: #fbbf24 !important;
    font-weight: 600;
}

/* Empty state */
:deep(.el-empty__description p) {
    color: #94a3b8 !important;
}

/* Loading overlay */
:deep(.el-loading-mask) {
    background: rgba(11, 18, 32, 0.85) !important;
}

:deep(.el-loading-spinner .el-loading-text) {
    color: #f1f5f9 !important;
}

:deep(.el-loading-spinner .path) {
    stroke: #0ea5e9 !important;
}
</style>