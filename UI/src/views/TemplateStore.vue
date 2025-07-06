<template>
    <div class="page-container">
        <el-card>
            <template #header>
                <div class="card-header">
                    <h1>模板商城</h1>
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
                <el-row :gutter="20" v-if="templates.length > 0">
                    <el-col :xs="24" :sm="12" :md="8" v-for="template in templates" :key="template.id">
                        <el-card shadow="hover" class="template-card">
                            <div class="template-preview-wrapper">
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
                                        {{ template.type === 'free' ? '立即使用' : '购买使用' }}
                                    </el-button>
                                </div>
                            </div>
                        </el-card>
                    </el-col>
                </el-row>
                <el-empty v-else description="暂无可用模板" />
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { ElNotification } from 'element-plus';
import { API_URLS, getHeaders } from '@/config/api';

const router = useRouter();
const templates = ref([]);
const loading = ref(false);
const currentFilter = ref('all');

// 获取模板列表
const fetchTemplates = async () => {
    loading.value = true;
    try {
        const response = await axios.get(API_URLS.templates.all, {
            headers: getHeaders()
        });
        templates.value = response.data;

        // 加载模板HTML内容
        for (const template of templates.value) {
            if (template.templatePath) {
                try {
                    const response = await fetch(template.templatePath);
                    template.html = await response.text();
                } catch (error) {
                    console.error(`Error loading template ${template.templatePath}:`, error);
                    template.html = `<div style='text-align: center; padding: 20px; color: red;'>加载模板失败</div>`;
                }
            }
        }
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
}

.card-header h1 {
    margin: 0;
    font-size: 24px;
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
    margin-left: 20px;
}
</style>