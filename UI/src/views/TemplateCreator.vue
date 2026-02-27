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
                <div class="zoom-controls">
                    <el-button circle size="small" @click="adjustZoom(0.1)">+</el-button>
                    <span class="zoom-text">{{ Math.round(scale * 100) }}%</span>
                    <el-button circle size="small" @click="adjustZoom(-0.1)">-</el-button>
                    <el-button size="small" @click="resetView">复位</el-button>
                </div>
            </div>
            <div class="preview-viewport" @wheel.prevent="handleWheel" @mousedown="startDrag" @mousemove="onDrag"
                @mouseup="stopDrag" @mouseleave="stopDrag">
                <div class="preview-content" :style="previewStyle">
                    <div class="preview-wrapper">
                        <iframe ref="previewFrame" class="preview-iframe"></iframe>
                        <!-- 遮罩层，用于捕获鼠标事件以支持拖拽 -->
                        <div class="iframe-overlay"></div>
                    </div>
                </div>
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
            <el-form-item label="价格 (元)" required>
                <el-input-number v-model="publishForm.price" :min="0" :precision="2" :step="1"
                    placeholder="0.00"></el-input-number>
                <div style="font-size: 12px; color: #909399; margin-top: 5px;">
                    输入 0 表示免费模板
                </div>
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
import { ref, reactive, shallowRef, onMounted, onUnmounted, computed } from 'vue';
import { ElButton, ElTag, ElDialog, ElForm, ElFormItem, ElInput, ElInputNumber, ElNotification } from 'element-plus';

// Debug check
console.log('TemplateCreator loaded. computed type:', typeof computed);
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
            html { overflow-x: hidden; }
            body { font-family: 'Helvetica Neue', Helvetica, Arial, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif; line-height: 1.6; color: #333; background-color: #fff; margin: 0; padding: 0; }
            .resume-container { width: 100%; min-height: 100%; box-sizing: border-box; margin: 0 auto; padding: 40px; overflow-wrap: break-word; word-wrap: break-word; }
            img, video, table, .section-content { max-width: 100%; }
            .header { display: flex; justify-content: space-between; align-items: flex-start; padding-bottom: 20px; margin-bottom: 30px; flex-wrap: wrap; }
            .user-info { flex-grow: 1; min-width: 200px; }
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
const previewViewport = ref(null);
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
const publishForm = reactive({ name: '', description: '', price: 0 });
const authStore = useAuthStore(); // Initialize auth store

// --- Zoom & Pan State ---
const scale = ref(1);
const translateX = ref(0);
const translateY = ref(0);
const isDragging = ref(false);
const startX = ref(0);
const startY = ref(0);

const previewStyle = computed(() => ({
    transform: `translate(${translateX.value}px, ${translateY.value}px) scale(${scale.value})`,
    transformOrigin: 'center center',
    transition: isDragging.value ? 'none' : 'transform 0.1s ease-out'
}));

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

const handleWheel = (e) => {
    if (e.ctrlKey || e.metaKey || true) { // Always zoom on wheel as requested
        const delta = e.deltaY > 0 ? -0.1 : 0.1;
        const newScale = Math.max(0.1, Math.min(5, scale.value + delta));
        scale.value = newScale;
    }
};

const startDrag = (e) => {
    // Only drag if clicking on the preview content or background, not buttons
    if (e.target.closest('button') || e.target.closest('.tag-button')) return;

    isDragging.value = true;
    startX.value = e.clientX - translateX.value;
    startY.value = e.clientY - translateY.value;

    // Change cursor style
    document.body.style.cursor = 'grabbing';
};

const onDrag = (e) => {
    if (!isDragging.value) return;
    e.preventDefault();
    translateX.value = e.clientX - startX.value;
    translateY.value = e.clientY - startY.value;
};

const stopDrag = () => {
    isDragging.value = false;
    document.body.style.cursor = '';
};

const adjustZoom = (delta) => {
    const newScale = Math.max(0.1, Math.min(5, scale.value + delta));
    scale.value = newScale;
};

const resetView = () => {
    scale.value = 1;
    translateX.value = 0;
    translateY.value = 0;
};

const autoFit = () => {
    if (!previewViewport.value) return;

    const { clientWidth, clientHeight } = previewViewport.value;
    const padding = 40; // padding for better look

    // A4 dimensions in pixels (96 DPI approx)
    // 210mm = 793.7px, 297mm = 1122.5px
    const paperWidth = 794;
    const paperHeight = 1123;

    const availableWidth = Math.max(100, clientWidth - padding);
    const availableHeight = Math.max(100, clientHeight - padding);

    const scaleX = availableWidth / paperWidth;
    const scaleY = availableHeight / paperHeight;

    // Use the smaller scale to ensure full fit
    scale.value = Math.min(scaleX, scaleY, 1);

    // Center it
    translateX.value = 0;
    translateY.value = 0;
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
    doc.open();
    doc.write(populatedHtml);
    doc.close();
};

const clearPreview = () => {
    updatePreview();
};

const promptPublishInfo = () => {
    publishForm.name = '';
    publishForm.description = '';
    publishForm.price = 0;
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

onMounted(() => {
    updatePreview();
    // Delay auto-fit slightly to ensure DOM is ready
    setTimeout(() => {
        autoFit();
    }, 100);

    window.addEventListener('resize', autoFit);
});

// 在组件卸载时移除事件监听
onUnmounted(() => {
    window.removeEventListener('resize', autoFit);
    document.body.style.cursor = '';
});
</script>

<style scoped>
.template-creator-container {
    display: flex;
    height: 100%;
    overflow: hidden;
    background: radial-gradient(1200px 600px at 20% -20%, rgba(14, 165, 233, 0.08) 40%, var(--bg-primary) 100%);
}

.editor-pane {
    flex: 1;
    display: flex;
    flex-direction: column;
    padding: 16px;
    border-right: 1px solid var(--glass-border);
    overflow-y: auto;
    min-width: 0;
    background: rgba(255, 255, 255, 0.02);
}

.preview-pane {
    flex: 1;
    overflow: hidden;
    height: 100%;
    background: linear-gradient(135deg, rgba(15, 23, 42, 0.95) 0%, rgba(11, 18, 32, 0.98) 100%);
    padding: 16px;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    align-items: center;
    min-width: 0;
}

.preview-viewport {
    flex: 1;
    width: 100%;
    overflow: hidden;
    position: relative;
    display: flex;
    justify-content: center;
    align-items: center;
    background-image: radial-gradient(rgba(255, 255, 255, 0.08) 1px, transparent 1px);
    background-size: 20px 20px;
    border: 1px solid var(--glass-border);
    border-radius: 12px;
    box-shadow: inset 0 0 30px rgba(0, 0, 0, 0.3);
}

.preview-content {
    will-change: transform;
}

.preview-wrapper {
    width: 210mm;
    height: 297mm;
    background-color: white;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
    position: relative;
    overflow: hidden;
    border-radius: 4px;
}

.preview-iframe {
    width: 100%;
    height: 100%;
    border: none;
    display: block;
}

.iframe-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 10;
    background: transparent;
}

.zoom-controls {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-left: auto;
}

.zoom-text {
    font-size: 13px;
    color: var(--fg-secondary);
    min-width: 45px;
    text-align: center;
    font-weight: 500;
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
    margin-bottom: 12px;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    flex-shrink: 0;
}

.tag-button {
    cursor: pointer;
    transition: all 0.2s;
    background: rgba(255, 255, 255, 0.06) !important;
    border: 1px solid var(--glass-border) !important;
    color: var(--fg-primary) !important;
}

.tag-button:hover {
    transform: scale(1.08);
    background: rgba(14, 165, 233, 0.15) !important;
    border-color: var(--accent-primary) !important;
    color: var(--accent-primary) !important;
}

.preview-toolbar {
    margin-bottom: 16px;
    display: flex;
    gap: 10px;
    flex-shrink: 0;
    width: 100%;
    justify-content: center;
}

/* Button Styles */
.preview-toolbar :deep(.el-button--primary) {
    background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary)) !important;
    border: none !important;
    color: var(--bg-primary) !important;
    font-weight: 600 !important;
    box-shadow: 0 6px 20px rgba(14, 165, 233, 0.35) !important;
    transition: all 0.3s ease !important;
}

.preview-toolbar :deep(.el-button--primary:hover) {
    transform: translateY(-2px);
    box-shadow: 0 10px 28px rgba(14, 165, 233, 0.45) !important;
}

.preview-toolbar :deep(.el-button--warning) {
    background: rgba(245, 158, 11, 0.15) !important;
    border: 1px solid rgba(245, 158, 11, 0.3) !important;
    color: #fbbf24 !important;
    font-weight: 600 !important;
}

.preview-toolbar :deep(.el-button--warning:hover) {
    background: rgba(245, 158, 11, 0.25) !important;
    border-color: rgba(245, 158, 11, 0.5) !important;
}

.preview-toolbar :deep(.el-button--success) {
    background: rgba(34, 197, 94, 0.15) !important;
    border: 1px solid rgba(34, 197, 94, 0.3) !important;
    color: #4ade80 !important;
    font-weight: 600 !important;
}

.preview-toolbar :deep(.el-button--success:hover) {
    background: rgba(34, 197, 94, 0.25) !important;
    border-color: rgba(34, 197, 94, 0.5) !important;
}

.preview-toolbar :deep(.el-button.is-circle) {
    background: rgba(255, 255, 255, 0.06) !important;
    border: 1px solid var(--glass-border) !important;
    color: var(--fg-primary) !important;
}

.preview-toolbar :deep(.el-button.is-circle:hover) {
    background: rgba(255, 255, 255, 0.12) !important;
    border-color: var(--accent-primary) !important;
    color: var(--accent-primary) !important;
}

.preview-toolbar :deep(.el-button:not(.el-button--primary):not(.el-button--warning):not(.el-button--success):not(.is-circle)) {
    background: rgba(255, 255, 255, 0.06) !important;
    border: 1px solid var(--glass-border) !important;
    color: var(--fg-primary) !important;
}

/* Dialog Styles */
:deep(.el-dialog) {
    background: var(--glass-bg) !important;
    border: 1px solid var(--glass-border) !important;
    border-radius: 16px !important;
    box-shadow: 0 25px 80px rgba(0, 0, 0, 0.5) !important;
    backdrop-filter: var(--glass-blur) !important;
}

:deep(.el-dialog__header) {
    border-bottom: 1px solid var(--glass-border) !important;
    padding: 20px 24px !important;
}

:deep(.el-dialog__title) {
    color: var(--fg-primary) !important;
    font-weight: 600 !important;
}

:deep(.el-dialog__headerbtn .el-dialog__close) {
    color: var(--fg-muted) !important;
}

:deep(.el-dialog__headerbtn:hover .el-dialog__close) {
    color: var(--accent-primary) !important;
}

:deep(.el-dialog__body) {
    padding: 24px !important;
    color: var(--fg-secondary) !important;
}

:deep(.el-dialog__footer) {
    border-top: 1px solid var(--glass-border) !important;
    padding: 16px 24px !important;
}

:deep(.el-form-item__label) {
    color: var(--fg-primary) !important;
    font-weight: 500;
}

:deep(.el-input__wrapper) {
    background: rgba(15, 23, 42, 0.6) !important;
    border: 1px solid var(--glass-border) !important;
    box-shadow: none !important;
    border-radius: 10px !important;
}

:deep(.el-input__wrapper:hover) {
    border-color: rgba(255, 255, 255, 0.2) !important;
}

:deep(.el-input__wrapper.is-focus) {
    border-color: var(--accent-primary) !important;
    box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.15) !important;
}

:deep(.el-input__inner) {
    color: var(--fg-primary) !important;
}

:deep(.el-input__inner::placeholder) {
    color: var(--fg-muted) !important;
}

:deep(.el-textarea__inner) {
    background: rgba(15, 23, 42, 0.6) !important;
    border: 1px solid var(--glass-border) !important;
    border-radius: 10px !important;
    color: var(--fg-primary) !important;
}

:deep(.el-textarea__inner:hover) {
    border-color: rgba(255, 255, 255, 0.2) !important;
}

:deep(.el-textarea__inner:focus) {
    border-color: var(--accent-primary) !important;
    box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.15) !important;
}

:deep(.el-input-number) {
    background: rgba(15, 23, 42, 0.6) !important;
}

:deep(.el-input-number .el-input__wrapper) {
    background: transparent !important;
}

:deep(.el-input-number__decrease),
:deep(.el-input-number__increase) {
    background: rgba(255, 255, 255, 0.06) !important;
    border-color: var(--glass-border) !important;
    color: var(--fg-secondary) !important;
}

:deep(.el-input-number__decrease:hover),
:deep(.el-input-number__increase:hover) {
    color: var(--accent-primary) !important;
}

/* Dialog Footer Buttons */
:deep(.dialog-footer .el-button:not(.el-button--primary)) {
    background: rgba(255, 255, 255, 0.06) !important;
    border: 1px solid var(--glass-border) !important;
    color: var(--fg-primary) !important;
}

:deep(.dialog-footer .el-button--primary) {
    background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary)) !important;
    border: none !important;
    color: var(--bg-primary) !important;
    font-weight: 600 !important;
}

/* CodeMirror Editor Container */
:deep(.cm-editor) {
    background: rgba(15, 23, 42, 0.8) !important;
    border-radius: 12px;
    border: 1px solid var(--glass-border);
    overflow: hidden;
}

:deep(.cm-scroller) {
    background: transparent !important;
}

/* 响应式调整 */
@media (max-width: 1200px) {
    .template-creator-container {
        flex-direction: column;
        height: auto;
        min-height: calc(100vh - 60px);
    }

    .editor-pane {
        flex: none;
        height: 45vh;
        border-right: none;
        border-bottom: 1px solid var(--glass-border);
    }

    .preview-pane {
        flex: none;
        height: 55vh;
        padding: 12px;
    }
}

@media (max-width: 768px) {
    .template-creator-container {
        padding: 8px;
    }

    .preview-pane {
        padding: 8px;
    }

    .toolbar {
        gap: 6px;
    }

    .tag-button {
        font-size: 12px;
        padding: 2px 8px;
    }

    .preview-toolbar {
        flex-wrap: wrap;
    }
}
</style>