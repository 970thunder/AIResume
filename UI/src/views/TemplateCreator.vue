<template>
    <div class="template-creator-container">
        <div class="editor-pane">
            <div class="toolbar">
                <el-tag v-for="tag in availableTags" :key="tag.name" @click="insertTag(tag.value)" class="tag-button">
                    {{ tag.name }}
                </el-tag>
            </div>
            <codemirror v-model="htmlCode" placeholder="在这里输入你的HTML模板代码..."
                :style="{ flexGrow: 1, height: '100%', overflow: 'hidden' }" :autofocus="true" :indent-with-tab="true"
                :tab-size="2" :extensions="extensions" @ready="handleReady" @change="updatePreview" />
        </div>
        <div class="preview-pane">
            <div class="preview-toolbar">
                <el-button @click="testWithSampleData" type="primary">测试</el-button>
                <el-button @click="clearPreview" type="warning">清除</el-button>
                <el-button @click="promptPublishInfo" type="success">发布</el-button>
            </div>
            <div class="preview-wrapper">
                <iframe ref="previewFrame" class="preview-iframe"></iframe>
            </div>
        </div>
    </div>
    <el-dialog v-model="publishDialogVisible" title="发布模板" width="400px">
        <el-form :model="publishForm" label-position="top">
            <el-form-item label="模板名称" required>
                <el-input v-model="publishForm.name" placeholder="为你的模板起个名字"></el-input>
            </el-form-item>
            <el-form-item label="模板描述">
                <el-input v-model="publishForm.description" type="textarea" :rows="3"
                    placeholder="简单描述下模板的特点和适用场景"></el-input>
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="publishDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="publishTemplate" :loading="isPublishing">
                    确认发布
                </el-button>
            </span>
        </template>
    </el-dialog>
</template>

<script setup>
import { ref, reactive, shallowRef, onMounted } from 'vue';
import { ElButton, ElTag, ElDialog, ElForm, ElFormItem, ElInput, ElNotification } from 'element-plus';
import axios from 'axios';
import { API_URLS, getHeaders } from '@/config/api';
import { useAuthStore } from '@/stores/auth'; // Import auth store
import { Codemirror } from 'vue-codemirror';
import { html } from '@codemirror/lang-html';
import { oneDark } from '@codemirror/theme-one-dark';

// --- State ---
const boilerplateHtml = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>基础简历模板代码</title>
</head>
<body>
    <div class="resume-container">
        <style>
            body { font-family: 'Helvetica Neue', Helvetica, Arial, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif; line-height: 1.6; color: #333; background-color: #fff; }
            .resume-container { width: 790px; margin: 20px auto; padding: 40px; border: 1px solid #eee; box-shadow: 0 0 10px rgba(0, 0, 0, 0.05); }
            .header { display: flex; justify-content: space-between; align-items: flex-start; padding-bottom: 20px; margin-bottom: 30px; }
            .user-info { flex-grow: 1; }
            .full-name { font-size: 32px; font-weight: 600; margin: 0 0 5px 0; color: #000; }
            .job-title { font-size: 18px; margin: 0 0 15px 0; color: #555; }
            .contact-info { list-style: none; padding: 0; margin: 0; display: flex; flex-wrap: wrap; gap: 15px; font-size: 14px; color: #444; }
            .contact-info li a { color: #00b38a; text-decoration: none; }
            .contact-info li a:hover { text-decoration: underline; }
            .avatar-placeholder { width: 100px; height: 130px; background-color: #f0f0f0; border: 1px solid #ddd; border-radius: 6px; margin-left: 40px; flex-shrink: 0; }
            .section, .item { page-break-inside: avoid; }
            .section { margin-bottom: 25px; text-align: left; }
            .section-title { font-size: 20px; font-weight: bold; color: #333; padding-bottom: 10px; border-bottom: 2px solid #eee; display: flex; align-items: center; margin-bottom: 15px; }
            .section-title::before { content: ''; display: inline-block; width: 12px; height: 12px; background-color: #00b38a; border-radius: 50%; margin-right: 12px; }
            .section-content { font-size: 15px; color: #555; }
        </style>
        <div class="header">
            <div class="user-info">
                <h1 class="full-name">{{fullName}}</h1>
                <p class="job-title">{{jobTitle}}</p>
                <ul class="contact-info">
                    <li>{{phone}}</li>
                    <li>{{email}}</li>
                    <li>{{address}}</li>
                    {{links}} 
                </ul>
            </div>
            <div class="avatar-placeholder"></div>
        </div>
        <div class="section"><h2 class="section-title">个人简介</h2><div class="section-content">{{summary}}</div></div>
        <div class="section"><h2 class="section-title">工作经历</h2><div class="section-content">{{experience}}</div></div>
        <div class="section"><h2 class="section-title">教育背景</h2><div class="section-content">{{education}}</div></div>
        <div class="section"><h2 class="section-title">项目经历</h2><div class="section-content">{{projects}}</div></div>
        <div class="section"><h2 class="section-title">专业技能</h2><div class="section-content">{{skills}}</div></div>
    </div>
</body>
</html>`;

const htmlCode = ref(boilerplateHtml);
const previewFrame = ref(null);
const view = shallowRef();
const extensions = [html(), oneDark];

const availableTags = ref([
    { name: '姓名', value: '{{fullName}}' }, { name: '求职意向', value: '{{jobTitle}}' },
    { name: '电话', value: '{{phone}}' }, { name: '邮箱', value: '{{email}}' },
    { name: '地址', value: '{{address}}' }, { name: '个人链接', value: '{{links}}' },
    { name: '个人简介', value: '{{summary}}' }, { name: '工作经历', value: '{{experience}}' },
    { name: '教育背景', value: '{{education}}' }, { name: '项目经历', value: '{{projects}}' },
    { name: '专业技能', value: '{{skills}}' },
]);

const sampleData = {
    fullName: '张三', jobTitle: '前端开发工程师', phone: '138-8888-8888',
    email: 'zhangsan@example.com', address: '中国，上海市',
    links: '<li><a href="https://github.com/zhangsan" target="_blank">GitHub</a></li>',
    summary: '<p>一位充满激情的前端开发者，拥有3年以上的Web应用开发经验...</p>',
    experience: '<div>一段工作经历的示例HTML</div>',
    education: '<div>一段教育背景的示例HTML</div>',
    projects: '<div>一段项目经历的示例HTML</div>',
    skills: '<p>熟练掌握 Vue、React、Node.js...</p>',
};

const publishDialogVisible = ref(false);
const isPublishing = ref(false);
const publishForm = reactive({ name: '', description: '' });
const authStore = useAuthStore(); // Initialize auth store

// --- Methods ---
const handleReady = (payload) => { view.value = payload.view; };
const updatePreview = () => {
    if (previewFrame.value) {
        const doc = previewFrame.value.contentWindow.document;
        doc.open();
        doc.write(htmlCode.value);
        doc.close();
    }
};

const insertTag = (tag) => {
    if (view.value) {
        view.value.dispatch(view.value.state.replaceSelection(tag));
    }
};

const testWithSampleData = () => {
    let populatedHtml = htmlCode.value;
    for (const key in sampleData) {
        populatedHtml = populatedHtml.replace(new RegExp(`\\{\\{${key}\\}\\}`, 'g'), sampleData[key]);
    }
    const doc = previewFrame.value.contentWindow.document;
    doc.open(); doc.write(populatedHtml); doc.close();
};

const clearPreview = () => { updatePreview(); };

const promptPublishInfo = () => {
    publishForm.name = '';
    publishForm.description = '';
    publishDialogVisible.value = true;
};

const publishTemplate = async () => {
    if (!publishForm.name) {
        ElNotification({ title: '提示', message: '请输入模板名称', type: 'warning' });
        return;
    }

    if (!authStore.isAuthenticated || !authStore.token) {
        ElNotification({ title: '错误', message: '请先登录再发布模板', type: 'error' });
        return;
    }

    isPublishing.value = true;
    try {
        const payload = { ...publishForm, htmlContent: htmlCode.value };

        const headers = {
            ...getHeaders(),
            Authorization: `Bearer ${authStore.token}`
        };

        // 诊断日志：打印将要发送的请求头
        console.log('即将发送的请求头:', headers);

        await axios.post(API_URLS.templates.create, payload, { headers });
        ElNotification({ title: '成功', message: '模板已成功提交审核！', type: 'success' });
        publishDialogVisible.value = false;
    } catch (error) {
        ElNotification({ title: '错误', message: error.response?.data?.message || '发布失败，请检查网络或联系管理员', type: 'error' });
    } finally {
        isPublishing.value = false;
    }
};

onMounted(() => { updatePreview(); });
</script>

<style scoped>
.template-creator-container {
    display: flex;
    height: calc(100vh - 60px);
}

.editor-pane {
    flex: 1;
    display: flex;
    flex-direction: column;
    padding: 10px;
    border-right: 1px solid #dcdfe6;
    overflow-y: auto;
}

.preview-pane {
    flex: 1;
    overflow-y: auto;
    height: 100%;
    background-color: #f0f2f5;
    padding: 20px;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    align-items: center;
}

/* Hide scrollbar for all panes */
.editor-pane::-webkit-scrollbar,
.preview-pane::-webkit-scrollbar {
    display: none;
}

.editor-pane,
.preview-pane {
    -ms-overflow-style: none;
    scrollbar-width: none;
}

.toolbar {
    margin-bottom: 10px;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    flex-shrink: 0;
}

.tag-button {
    cursor: pointer;
    transition: all 0.2s;
}

.tag-button:hover {
    transform: scale(1.1);
}

.preview-toolbar {
    margin-bottom: 20px;
    display: flex;
    gap: 10px;
    flex-shrink: 0;
}

.preview-wrapper {
    width: 840px;
    height: 1188px;
    transform: scale(0.74);
    transform-origin: top center;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
    background-color: white;
    flex-shrink: 0;
}

.preview-iframe {
    width: 100%;
    height: 100%;
    border: none;
}
</style>