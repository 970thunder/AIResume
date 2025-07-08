<template>
    <div class="auditor-container">
        <div class="editor-pane">
            <codemirror v-model="htmlCode" placeholder="加载模板代码中..."
                :style="{ flexGrow: 1, height: '100%', overflow: 'hidden' }" :autofocus="true" :indent-with-tab="true"
                :tab-size="2" :extensions="extensions" />
        </div>
        <div class="preview-pane">
            <div class="preview-toolbar">
                <h3>审核操作</h3>
                <el-button @click="approveTemplate" type="success" :icon="Check">通过审核</el-button>
                <el-button @click="rejectTemplate" type="danger" :icon="Close">拒绝模板</el-button>
                <el-divider />
                <el-button @click="goBack" :icon="Back">返回列表</el-button>
            </div>
            <div class="preview-wrapper">
                <iframe class="preview-iframe" :srcdoc="htmlCode"></iframe>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Codemirror } from 'vue-codemirror';
import { html } from '@codemirror/lang-html';
import { oneDark } from '@codemirror/theme-one-dark';
import { Check, Close, Back } from '@element-plus/icons-vue';
import axios from 'axios';
import { ElNotification } from 'element-plus';
import { API_URLS, getHeaders } from '@/config/api';

const route = useRoute();
const router = useRouter();
const templateId = ref(route.params.id);
const htmlCode = ref('');
const extensions = [html(), oneDark];

const fetchTemplateDetails = async () => {
    try {
        const response = await axios.get(API_URLS.templates.byId(templateId.value), { headers: getHeaders() });
        const template = response.data;

        if (template.templatePath) {
            const htmlResponse = await fetch(template.templatePath);
            htmlCode.value = await htmlResponse.text();
        } else {
            htmlCode.value = template.htmlContent || '<!-- 无法加载模板内容 -->';
        }

    } catch (error) {
        ElNotification({ title: '错误', message: '加载模板详情失败', type: 'error' });
    }
};

const updateStatus = async (status) => {
    try {
        await axios.put(API_URLS.admin.updateTemplateStatus(templateId.value), { status }, { headers: getHeaders() });
        ElNotification({ title: '成功', message: `操作成功`, type: 'success' });
        router.push('/hyper');
    } catch (error) {
        ElNotification({ title: '错误', message: '操作失败', type: 'error' });
    }
};

const approveTemplate = () => updateStatus('APPROVED');
const rejectTemplate = () => updateStatus('REJECTED');
const goBack = () => router.push('/hyper');

onMounted(fetchTemplateDetails);
</script>

<style scoped>
.auditor-container {
    display: flex;
    height: 100vh;
    background-color: #f0f2f5;
}

.editor-pane {
    flex: 1;
    display: flex;
    flex-direction: column;
    height: 100vh;
}

.preview-pane {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 20px;
    overflow: auto;
}

.preview-toolbar {
    width: 100%;
    max-width: 840px;
    margin-bottom: 20px;
    text-align: center;
    flex-shrink: 0;
}

.preview-wrapper {
    width: 100%;
    max-width: 840px;
    height: auto;
    aspect-ratio: 840 / 1188;
    background-color: white;
    box-shadow: 0 0 15px rgba(0, 0, 0, 0.1);
    border: 1px solid #e0e0e0;
    transform-origin: top center;
}

.preview-iframe {
    width: 100%;
    height: 100%;
    border: none;
    transform: scale(1);
    transform-origin: top left;
}
</style>
