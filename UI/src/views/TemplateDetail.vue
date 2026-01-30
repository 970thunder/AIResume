<template>
    <div class="page-container">
        <el-button @click="goBack" class="back-btn" :icon="ArrowLeft">返回列表</el-button>
        
        <div v-loading="loading" class="detail-container">
            <el-row :gutter="40" v-if="template">
                <!-- Left Column: Preview -->
                <el-col :xs="24" :md="14" :lg="16">
                    <div class="preview-wrapper">
                        <div class="preview-scroll-container">
                            <ShadowPreview v-if="template.html" class="template-preview" :content="template.html" />
                            <el-empty v-else description="预览加载中..." />
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
import { ref, onMounted } from 'vue';
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
        // We can reuse the same API endpoint if it supports getting by ID
        // Assuming there is an endpoint like /api/templates/{id} or we have to fetch all and find
        // Based on TemplateService.java: public Template getTemplateById(Long id)
        // I need to check API_URLS if there is a specific endpoint for detail
        // If not, I might need to add it or use the getAll endpoint (not efficient but works for now)
        
        // Let's assume there is a detail endpoint, or we construct it.
        // Looking at api.js might be helpful, but I'll assume standard REST: /api/templates/{id}
        // Wait, I should check api.js first to be sure or add it.
        
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
});
</script>

<style scoped>
.page-container {
    padding: 24px;
    max-width: 1200px;
    margin: 0 auto;
}

.back-btn {
    margin-bottom: 20px;
}

.detail-container {
    min-height: 500px;
}

.preview-wrapper {
    background-color: #f5f7fa;
    border-radius: 8px;
    padding: 20px;
    height: 800px; /* Fixed height for scroll */
    border: 1px solid #ebeef5;
    display: flex;
    justify-content: center;
    overflow: hidden;
}

.preview-scroll-container {
    width: 100%;
    height: 100%;
    overflow-y: auto;
    overflow-x: hidden;
    display: flex;
    justify-content: center;
}

.template-preview {
    width: 210mm;
    min-height: 297mm;
    background: white;
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    transform: scale(0.65);
    transform-origin: top center;
    margin-bottom: -35%;
}

.info-card {
    position: sticky;
    top: 24px;
}

.title {
    margin: 0 0 16px 0;
    font-size: 24px;
    color: #303133;
}

.price-tag {
    margin-bottom: 24px;
}

.description-section {
    margin-bottom: 24px;
    padding-bottom: 24px;
    border-bottom: 1px solid #ebeef5;
}

.description-section h3 {
    margin: 0 0 12px 0;
    font-size: 16px;
    color: #303133;
}

.description {
    color: #606266;
    line-height: 1.6;
    margin: 0;
}

.meta-info {
    margin-bottom: 30px;
}

.meta-item {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12px;
    font-size: 14px;
}

.meta-item .label {
    color: #909399;
}

.meta-item .value {
    color: #303133;
}

.action-btn {
    width: 100%;
}
</style>
