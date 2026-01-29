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
                                    <el-button type="primary" @click="useTemplate(template)">
                                        立即使用
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
const useTemplate = (template) => {
    if (template.type === 'free') {
        router.push('/resume-generator');
    } else {
        ElNotification({
            title: '提示',
            message: '付费功能即将上线，敬请期待！',
            type: 'info',
        });
    }
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
}

.store-card {
    border-radius: 12px;
    border: 1px solid #e4e7ed;
    box-shadow: 5px 5px 10px 5px rgba(0, 0, 0, 0.1);
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

.create-btn {
    font-weight: 500;
}

.templates-container {
    min-height: 400px;
}

.template-card {
    margin-bottom: 24px;
    transition: all 0.3s ease;
    border: none;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    border-radius: 8px;
    overflow: hidden;
}

.template-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.template-preview-wrapper {
    height: 360px;
    overflow: hidden;
    background-color: #f5f7fa;
    position: relative;
    border-bottom: 1px solid #ebeef5;
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
    background: rgba(0, 0, 0, 0.3);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.template-card:hover .preview-overlay {
    opacity: 1;
}

.template-info {
    padding: 16px;
}

.info-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
}

.template-name {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 70%;
}

.description {
    color: #909399;
    font-size: 13px;
    margin: 0;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    height: 40px;
}

/* Custom Scrollbar for the page */
::-webkit-scrollbar {
    width: 8px;
    height: 8px;
}

::-webkit-scrollbar-track {
    background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
}
</style>