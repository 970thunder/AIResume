<template>
    <div class="page-container">
        <el-card>
            <template #header>
                <div class="card-header">
                    <div class="header-left">
                        <div class="brutalist-container">
                            <input v-model="searchQuery" @input="handleSearch" placeholder="搜索-支持搜索标题、描述..."
                                class="brutalist-input smooth-type" type="text" />
                            <label class="brutalist-label">搜索模板</label>
                        </div>
                    </div>
                    <div class="filter-buttons">
                        <el-radio-group v-model="currentFilter" @change="filterTemplates">
                            <el-radio-button label="all">全部</el-radio-button>
                            <el-radio-button label="free">免费模板</el-radio-button>
                            <el-radio-button label="premium">付费模板</el-radio-button>
                        </el-radio-group>
                    </div>
                </div>
            </template>

            <div v-loading="loading" class="templates-container">
                <el-row :gutter="20" v-if="filteredTemplates.length > 0">
                    <el-col :xs="24" :sm="12" :md="8" v-for="template in filteredTemplates" :key="template.id">
                        <el-card shadow="hover" class="template-card">
                            <div class="template-preview-wrapper" v-loading="templateLoadingStates[template.id]">
                                <div v-if="template.html" class="template-preview"
                                    :style="{ transform: 'scale(0.3)', transformOrigin: 'top left' }"
                                    v-html="template.html">
                                </div>
                                <el-empty v-else description="预览加载中..." />
                            </div>
                            <div class="template-info">
                                <h3>{{ template.name }}</h3>
                                <p class="description">{{ template.description }}</p>
                                <div class="template-footer">
                                    <el-tag :type="template.type === 'free' ? 'success' : 'warning'" size="small">
                                        {{ template.type === 'free' ? '免费' : `¥${template.price}` }}
                                    </el-tag>
                                    <el-button type="primary" size="small" @click="useTemplate(template)">
                                        查看详情
                                    </el-button>
                                </div>
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
import { API_URLS, getHeaders } from '@/config/api';

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
            template.description.toLowerCase().includes(searchLower)
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

    for (const template of templates) {
        if (template.templatePath) {
            try {
                const response = await fetch(template.templatePath);
                template.html = await response.text();
                // 添加1秒延迟
                await new Promise(resolve => setTimeout(resolve, 700));
            } catch (error) {
                console.error(`Error loading template ${template.templatePath}:`, error);
                template.html = `<div style='text-align: center; padding: 20px; color: red;'>加载模板失败</div>`;
            } finally {
                setTemplateLoading(template.id, false);
            }
        }
    }
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
    padding: 20px;
    max-width: 1200px;
    margin: 0 auto;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 20px;
}

.card-header h1 {
    margin: 0;
    font-size: 24px;
    white-space: nowrap;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 20px;
}

.brutalist-container {
    position: relative;
    width: 400px;
    font-family: monospace;
}

.brutalist-input {
    width: 100%;
    padding: 8px 12px;
    font-size: 15px;
    font-weight: bold;
    color: #5192ed;
    background-color: #fff;
    border: 3px solid #003898;
    position: relative;
    overflow: hidden;
    border-radius: 0;
    outline: none;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    box-shadow: 4px 4px 0 #8ec9ed, 8px 8px 0 #4a90e2;
}

.brutalist-input:focus {
    transform: translate(-4px, -4px);
    box-shadow: 8px 8px 0 #000, 12px 12px 0 #4a90e2;
}

.brutalist-input:not(:focus) {
    transform: translate(0, 0);
}

.brutalist-input:focus {
    animation: none;
}

.brutalist-label {
    position: absolute;
    left: -10px;
    top: -15px;
    font-size: 10px;
    font-weight: bold;
    color: rgb(255, 255, 255);
    background-color: #bcddff;
    padding: 3px 8px;
    transform: rotate(-1deg);
    z-index: 1;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.smooth-type {
    position: relative;
    overflow: hidden;
}

.smooth-type::before {
    content: "";
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    background: linear-gradient(90deg, #fff 0%, rgba(255, 255, 255, 0) 100%);
    z-index: 1;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.smooth-type:focus::before {
    opacity: 1;
    animation: type-gradient 2s linear infinite;
}

@keyframes type-gradient {
    0% {
        background-position: 300px 0;
    }

    100% {
        background-position: 0 0;
    }
}

.brutalist-input::placeholder {
    color: #888;
    transition: color 0.3s ease;
}

.brutalist-input:focus::placeholder {
    color: transparent;
}

.templates-container {
    margin-top: 20px;
}

.template-card {
    margin-bottom: 20px;
    transition: all 0.3s ease;
}

.template-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.template-preview-wrapper {
    height: 280px;
    overflow: hidden;
    border-bottom: 1px solid #ebeef5;
    background-color: #f8f9fa;
    position: relative;
}

.template-preview {
    width: 840px;
    height: 1188px;
}

.template-info {
    padding: 15px;
}

.template-info h3 {
    margin: 0 0 10px 0;
    font-size: 18px;
    color: #303133;
}

.description {
    color: #606266;
    font-size: 14px;
    margin-bottom: 15px;
    min-height: 40px;
}

.template-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.filter-buttons {
    margin-left: auto;
}

/* 自定义加载动画样式 */
:deep(.el-loading-mask) {
    background-color: rgba(255, 255, 255, 0.8);
}

:deep(.el-loading-spinner) {
    top: 40%;
}

:deep(.el-loading-spinner .circular) {
    width: 30px;
    height: 30px;
}
</style>