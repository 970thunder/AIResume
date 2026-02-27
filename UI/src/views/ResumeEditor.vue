<template>
    <div class="editor-layout">
        <!-- Left Side: Form -->
        <div class="editor-form-panel">
            <el-card shadow="never" class="form-card">
                <template #header>
                    <div class="card-header">
                        <span>编辑简历内容</span>
                        <div class="header-buttons">
                            <div class="buttons">
                                <button class="btn" @click="handleSave" :disabled="isSaving || !hasChanges"
                                    :class="{ 'saving': isSaving }">
                                    <span></span>
                                    <p data-start="Saving..." data-text="Saved!" data-title="保存"></p>
                                </button>
                            </div>
                            <el-button class="download-btn" @click="handleDownload"
                                :loading="isDownloading">下载PDF</el-button>
                        </div>
                    </div>
                </template>
                <el-form v-if="resumeData" :model="resumeData" label-position="top" class="resume-form">
                    <!-- Personal Info -->
                    <el-collapse v-model="activeCollapse">
                        <el-collapse-item title="基本信息" name="1">
                            <el-form-item label="姓名">
                                <el-input v-model="resumeData.personalInfo.fullName"></el-input>
                            </el-form-item>
                            <el-form-item label="求职意向">
                                <el-input v-model="resumeData.personalInfo.jobTitle"></el-input>
                            </el-form-item>
                            <el-form-item label="邮箱">
                                <el-input v-model="resumeData.personalInfo.email"></el-input>
                            </el-form-item>
                            <el-form-item label="电话">
                                <el-input v-model="resumeData.personalInfo.phone"></el-input>
                            </el-form-item>
                        </el-collapse-item>

                        <!-- Summary -->
                        <el-collapse-item title="个人简介" name="2">
                            <el-form-item label="个人简介">
                                <el-input type="textarea" :rows="5" v-model="resumeData.summary"></el-input>
                            </el-form-item>
                        </el-collapse-item>

                        <!-- Experience -->
                        <el-collapse-item title="工作经历" name="3">
                            <div v-for="(exp, index) in resumeData.experience" :key="index" class="form-section">
                                <el-form-item label="公司名称">
                                    <el-input v-model="exp.companyName"></el-input>
                                </el-form-item>
                                <el-form-item label="职位">
                                    <el-input v-model="exp.jobTitle"></el-input>
                                </el-form-item>
                                <el-form-item label="在职时间">
                                    <el-date-picker v-model="exp.startDate" type="month" placeholder="开始月份" />
                                    -
                                    <el-date-picker v-model="exp.endDate" type="month" placeholder="结束月份" />
                                </el-form-item>
                                <el-form-item label="工作职责">
                                    <el-input type="textarea" :rows="4" v-model="exp.responsibilities"
                                        placeholder="每行一个职责"></el-input>
                                </el-form-item>
                                <el-button type="danger" size="small"
                                    @click="resumeData.experience.splice(index, 1)">删除此经历</el-button>
                            </div>
                            <el-button @click="resumeData.experience.push({ responsibilities: '' })">添加工作经历</el-button>
                        </el-collapse-item>

                        <!-- Education -->
                        <el-collapse-item title="教育背景" name="4">
                            <div v-for="(edu, index) in resumeData.education" :key="index" class="form-section">
                                <el-form-item label="学校名称">
                                    <el-input v-model="edu.institutionName"></el-input>
                                </el-form-item>
                                <el-form-item label="学历">
                                    <el-input v-model="edu.degree"></el-input>
                                </el-form-item>
                                <el-form-item label="专业">
                                    <el-input v-model="edu.major"></el-input>
                                </el-form-item>
                                <el-form-item label="毕业时间">
                                    <el-date-picker v-model="edu.graduationDate" type="month" placeholder="毕业月份" />
                                </el-form-item>
                                <el-button type="danger" size="small"
                                    @click="resumeData.education.splice(index, 1)">删除此教育背景</el-button>
                            </div>
                            <el-button @click="resumeData.education.push({})">添加教育背景</el-button>
                        </el-collapse-item>

                        <!-- Projects -->
                        <el-collapse-item title="项目经历" name="5">
                            <div v-for="(proj, index) in resumeData.projects" :key="index" class="form-section">
                                <el-form-item label="项目名称">
                                    <el-input v-model="proj.projectName"></el-input>
                                </el-form-item>
                                <el-form-item label="项目描述">
                                    <el-input type="textarea" :rows="3" v-model="proj.description"></el-input>
                                </el-form-item>
                                <el-button type="danger" size="small"
                                    @click="resumeData.projects.splice(index, 1)">删除此项目</el-button>
                            </div>
                            <el-button @click="resumeData.projects.push({})">添加项目经历</el-button>
                        </el-collapse-item>

                        <!-- Skills -->
                        <el-collapse-item title="专业技能" name="6">
                            <el-form-item label="技能 (用逗号分隔)">
                                <el-input type="textarea" v-model="skillsInput" @input="updateSkillsArray"></el-input>
                            </el-form-item>
                        </el-collapse-item>
                    </el-collapse>
                </el-form>
                <el-skeleton v-else :rows="10" animated />
            </el-card>
        </div>

        <!-- Right Side: Preview -->
        <div class="editor-preview-panel">
            <el-card shadow="never" class="preview-card">

                <div v-if="templateHtml" class="preview-wrapper">
                    <iframe :srcdoc="processedHtml" class="preview-iframe"></iframe>
                </div>
                <el-skeleton v-else :rows="10" animated />
            </el-card>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import { ElNotification } from 'element-plus';
import { API_URLS, getHeaders } from '@/config/api';
import html2pdf from 'html2pdf.js';

const props = defineProps({
    id: String
});

const route = useRoute();
const resumeData = ref(null);
const lastSavedResumeData = ref(null);
const templateHtml = ref('');
const activeCollapse = ref(['1']);
const isSaving = ref(false);
const isDownloading = ref(false);

const skillsInput = ref('');

const hasChanges = computed(() => {
    if (!resumeData.value || !lastSavedResumeData.value) {
        return false;
    }
    return JSON.stringify(resumeData.value) !== JSON.stringify(lastSavedResumeData.value);
});

// Fetch resume and template data on mount
onMounted(async () => {
    try {
        const resumeRes = await axios.get(API_URLS.resume.byId(props.id), { headers: getHeaders() });
        const parsedData = JSON.parse(resumeRes.data.aiAnalysisData);

        // Convert responsibilities array to newline-separated string for textarea v-model
        if (parsedData.experience) {
            parsedData.experience.forEach(exp => {
                if (Array.isArray(exp.responsibilities)) {
                    exp.responsibilities = exp.responsibilities.join('\n');
                }
            });
        }
        resumeData.value = parsedData;
        lastSavedResumeData.value = JSON.parse(JSON.stringify(parsedData)); // Create initial snapshot

        if (resumeData.value.skills && resumeData.value.skills.technicalSkills) {
            skillsInput.value = resumeData.value.skills.technicalSkills.join(', ');
        }

        const templateRes = await axios.get(API_URLS.templates.byId(resumeRes.data.templateId), { headers: getHeaders() });
        templateHtml.value = templateRes.data.htmlContent;
    } catch (error) {
        console.error("Failed to load data", error);
        ElNotification({ title: '错误', message: '加载简历数据失败', type: 'error' });
    }
});

const updateSkillsArray = () => {
    if (resumeData.value.skills) {
        resumeData.value.skills.technicalSkills = skillsInput.value.split(',').map(s => s.trim()).filter(s => s);
    }
}

const formatDateForDisplay = (date) => {
    if (!date) return '';
    const d = new Date(date);
    if (isNaN(d.getTime())) return '';
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    return `${year}-${month}`;
};

const generateSectionHtml = (items, template) => {
    if (!items || items.length === 0) return '';
    return items.map(item => template(item)).join('');
};

// Process HTML for preview
const processedHtml = computed(() => {
    if (!templateHtml.value || !resumeData.value) return '';
    let html = templateHtml.value;
    const data = resumeData.value;

    // Simple placeholder replacement
    html = html.replace(/{{fullName}}/g, data.personalInfo?.fullName || '');
    html = html.replace(/{{jobTitle}}/g, data.personalInfo?.jobTitle || '');
    html = html.replace(/{{email}}/g, data.personalInfo?.email || '');
    html = html.replace(/{{phone}}/g, data.personalInfo?.phone || '');
    html = html.replace(/{{address}}/g, data.personalInfo?.address?.city || '');
    html = html.replace(/{{summary}}/g, data.summary || '');

    // Experience
    const expTemplate = (exp) => `
        <div class="item">
            <div class="item-header">
                <div class="title">${exp.companyName || ''} - ${exp.jobTitle || ''}</div>
                <div class="date">${formatDateForDisplay(exp.startDate)} - ${exp.endDate ? formatDateForDisplay(exp.endDate) : '至今'}</div>
            </div>
            <div class="item-content">
                <ul>${(exp.responsibilities || '').split('\n').map(line => `<li>${line}</li>`).join('')}</ul>
            </div>
        </div>`;
    html = html.replace(/<!-- {{experience}} -->/g, generateSectionHtml(data.experience, expTemplate));

    // Education
    const eduTemplate = (edu) => `
        <div class="item">
            <div class="item-header">
                <div class="title">${edu.institutionName || ''}</div>
                <div class="date">${formatDateForDisplay(edu.graduationDate)}</div>
            </div>
            <div class="item-subheader">${edu.degree || ''}, ${edu.major || ''}</div>
        </div>`;
    html = html.replace(/<!-- {{education}} -->/g, generateSectionHtml(data.education, eduTemplate));

    // Projects
    const projTemplate = (proj) => `
         <div class="item">
            <div class="item-header">
                <div class="title">${proj.projectName || ''}</div>
            </div>
            <div class="item-content">
                 <ul><li>${(proj.description || '').replace(/\n/g, '</li><li>')}</li></ul>
            </div>
        </div>`;
    html = html.replace(/<!-- {{projects}} -->/g, generateSectionHtml(data.projects, projTemplate));

    // Skills
    const skillsHtml = (data.skills?.technicalSkills || []).map(skill => `<span class="skill-tag">${skill}</span>`).join(' ');
    html = html.replace(/<!-- {{skills}} -->/g, skillsHtml);

    return html;
});

// Save handler
const handleSave = async () => {
    if (isSaving.value || !hasChanges.value) return;
    isSaving.value = true;

    try {
        const dataToSave = JSON.parse(JSON.stringify(resumeData.value));
        if (dataToSave.experience) {
            dataToSave.experience.forEach(exp => {
                if (typeof exp.responsibilities === 'string') {
                    exp.responsibilities = exp.responsibilities.split('\n').filter(line => line.trim() !== '');
                }
            });
        }

        const payload = {
            aiAnalysisData: JSON.stringify(dataToSave),
            templateId: (await axios.get(API_URLS.resume.byId(props.id), { headers: getHeaders() })).data.templateId
        };

        const savePromise = axios.put(API_URLS.resume.update(props.id), payload, { headers: getHeaders() });
        const minDurationPromise = new Promise(resolve => setTimeout(resolve, 500));

        await Promise.all([savePromise, minDurationPromise]);

        lastSavedResumeData.value = JSON.parse(JSON.stringify(resumeData.value));
        ElNotification({ title: '成功', message: '简历已保存', type: 'success' });
    } catch (error) {
        console.error("Failed to save resume", error);
        ElNotification({ title: '错误', message: '保存失败', type: 'error' });
    } finally {
        isSaving.value = false;
    }
};

// Download handler
const handleDownload = () => {
    isDownloading.value = true;
    const element = document.createElement('div');
    element.innerHTML = processedHtml.value;

    const opt = {
        margin: 0,
        filename: `${resumeData.value.personalInfo.fullName}_resume.pdf`,
        image: { type: 'jpeg', quality: 0.98 },
        html2canvas: { scale: 2, useCORS: true },
        jsPDF: { unit: 'in', format: 'letter', orientation: 'portrait' }
    };

    html2pdf().from(element).set(opt).save().then(() => {
        isDownloading.value = false;
    });
};

watch(resumeData, () => {
    // This will trigger re-computation of processedHtml whenever resumeData changes
}, { deep: true });

</script>

<style scoped>
.editor-layout {
    display: flex;
    height: 100%;
    background: radial-gradient(1200px 600px at 20% -20%, rgba(14, 165, 233, 0.08) 40%, var(--bg-primary) 100%);
}

.editor-form-panel {
    flex: 1;
    overflow-y: auto;
    height: 100%;
    padding: 16px;
    box-sizing: border-box;
}

.editor-preview-panel {
    flex: 2;
    overflow-y: auto;
    height: 100%;
    background: linear-gradient(135deg, rgba(15, 23, 42, 0.95) 0%, rgba(11, 18, 32, 0.98) 100%);
    padding: 24px;
    box-sizing: border-box;
    display: flex;
    justify-content: center;
    align-items: flex-start;
    border-left: 1px solid var(--glass-border);
}

.form-card {
    width: 100%;
    height: fit-content;
    display: flex;
    flex-direction: column;
    background: var(--glass-bg) !important;
    border: 1px solid var(--glass-border) !important;
    border-radius: 16px !important;
    box-shadow: var(--glass-shadow) !important;
    backdrop-filter: var(--glass-blur) !important;
    -webkit-backdrop-filter: var(--glass-blur) !important;
}

.preview-card {
    width: 100%;
    height: fit-content;
    display: flex;
    flex-direction: column;
    background: transparent !important;
    border: none !important;
    box-shadow: none !important;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    color: var(--fg-primary);
}

.card-header span {
    font-weight: 600;
    font-size: 1.1rem;
}

.resume-form {
    padding: 0 10px;
}

/* Element Plus Collapse 深色主题覆盖 */
:deep(.el-collapse) {
    border-color: var(--glass-border) !important;
}

:deep(.el-collapse-item__header) {
    background: transparent !important;
    color: var(--fg-primary) !important;
    border-color: var(--glass-border) !important;
    font-weight: 600;
    font-size: 15px;
    transition: all 0.3s ease;
}

:deep(.el-collapse-item__header:hover) {
    color: var(--accent-primary) !important;
}

:deep(.el-collapse-item__wrap) {
    background: transparent !important;
    border-color: var(--glass-border) !important;
}

:deep(.el-collapse-item__content) {
    color: var(--fg-secondary) !important;
    padding-bottom: 20px;
}

/* Form Items */
:deep(.el-form-item__label) {
    color: var(--fg-secondary) !important;
    font-weight: 500;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
    background: rgba(15, 23, 42, 0.6) !important;
    border: 1px solid var(--glass-border) !important;
    box-shadow: none !important;
    border-radius: 10px !important;
    transition: all 0.3s ease !important;
}

:deep(.el-input__wrapper:hover),
:deep(.el-textarea__inner:hover) {
    border-color: rgba(255, 255, 255, 0.2) !important;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-textarea__inner:focus) {
    border-color: var(--accent-primary) !important;
    box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.15) !important;
}

:deep(.el-input__inner),
:deep(.el-textarea__inner) {
    color: var(--fg-primary) !important;
}

:deep(.el-input__inner::placeholder),
:deep(.el-textarea__inner::placeholder) {
    color: var(--fg-muted) !important;
}

/* Date Picker */
:deep(.el-date-editor) {
    --el-date-editor-width: 130px;
}

.preview-wrapper {
    width: 840px;
    height: 1188px;
    transform: scale(0.75);
    transform-origin: top center;
    box-shadow: 0 25px 80px rgba(0, 0, 0, 0.5);
    background-color: white;
    border-radius: 8px;
    overflow: hidden;
}

.preview-iframe {
    width: 100%;
    height: 100%;
    border: none;
}

.form-section {
    border: 1px solid var(--glass-border);
    padding: 16px;
    margin-bottom: 16px;
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.02);
    backdrop-filter: blur(8px);
    transition: all 0.3s ease;
}

.form-section:hover {
    background: rgba(255, 255, 255, 0.04);
    border-color: rgba(255, 255, 255, 0.15);
}

/* Hide scrollbar for Chrome, Safari and Opera */
.editor-form-panel::-webkit-scrollbar,
.editor-preview-panel::-webkit-scrollbar {
    display: none;
}

/* Hide scrollbar for IE, Edge and Firefox */
.editor-form-panel,
.editor-preview-panel {
    -ms-overflow-style: none;
    scrollbar-width: none;
}

.header-buttons {
    display: flex;
    align-items: center;
    gap: 16px;
}

.download-btn {
    background: linear-gradient(135deg, var(--accent-primary), var(--accent-secondary)) !important;
    color: var(--bg-primary) !important;
    border: none !important;
    cursor: pointer;
    border-radius: 10px !important;
    width: 100px !important;
    height: 42px !important;
    font-weight: 600 !important;
    transition: all 0.3s ease !important;
    box-shadow: 0 8px 24px rgba(14, 165, 233, 0.35) !important;
}

.download-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 32px rgba(14, 165, 233, 0.45) !important;
}

/* Save Button Styles - Dark Theme */
.buttons {
    display: flex;
    justify-content: space-around;
}

.buttons button {
    width: 100px;
    height: 42px;
    background: rgba(255, 255, 255, 0.06);
    margin: 0;
    color: var(--accent-primary);
    position: relative;
    overflow: hidden;
    font-size: 14px;
    letter-spacing: 1px;
    font-weight: 600;
    text-transform: uppercase;
    transition: all 0.3s ease;
    cursor: pointer;
    border: 1px solid var(--glass-border);
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
}

.buttons button:before,
.buttons button:after {
    content: "";
    position: absolute;
    width: 0;
    height: 2px;
    background: linear-gradient(90deg, var(--accent-primary), var(--accent-secondary));
    transition: all 0.3s cubic-bezier(0.35, 0.1, 0.25, 1);
}

.buttons button:before {
    right: 0;
    top: 0;
    transition: all 0.5s cubic-bezier(0.35, 0.1, 0.25, 1);
}

.buttons button:after {
    left: 0;
    bottom: 0;
}

.buttons button span {
    width: 100%;
    height: 100%;
    position: absolute;
    left: 0;
    top: 0;
    margin: 0;
    padding: 0;
    z-index: 1;
}

.buttons button span:before,
.buttons button span:after {
    content: "";
    position: absolute;
    width: 2px;
    height: 0;
    background: linear-gradient(180deg, var(--accent-primary), var(--accent-secondary));
    transition: all 0.3s cubic-bezier(0.35, 0.1, 0.25, 1);
}

.buttons button span:before {
    right: 0;
    top: 0;
    transition: all 0.5s cubic-bezier(0.35, 0.1, 0.25, 1);
}

.buttons button span:after {
    left: 0;
    bottom: 0;
}

.buttons button p {
    padding: 0;
    margin: 0;
    transition: all 0.4s cubic-bezier(0.35, 0.1, 0.25, 1);
    position: absolute;
    width: 100%;
    height: 100%;
}

.buttons button p:before,
.buttons button p:after {
    position: absolute;
    width: 100%;
    transition: all 0.4s cubic-bezier(0.35, 0.1, 0.25, 1);
    z-index: 1;
    left: 0;
    display: flex;
    align-items: center;
    justify-content: center;
}

.buttons button p:before {
    content: attr(data-title);
    top: 50%;
    transform: translateY(-50%);
    color: var(--fg-primary);
}

.buttons button p:after {
    content: attr(data-text);
    top: 150%;
    color: var(--accent-secondary);
}

.buttons button:hover {
    background: rgba(255, 255, 255, 0.1);
    border-color: rgba(14, 165, 233, 0.4);
}

.buttons button:hover:before,
.buttons button:hover:after {
    width: 100%;
}

.buttons button:hover span {
    z-index: 1;
}

.buttons button:hover span:before,
.buttons button:hover span:after {
    height: 100%;
}

.buttons button:hover p:before {
    top: -50%;
    transform: rotate(5deg);
}

.buttons button:hover p:after {
    top: 50%;
    transform: translateY(-50%);
}

.buttons button:disabled {
    cursor: not-allowed;
    opacity: 0.5;
}

.buttons button.saving {
    background: rgba(255, 255, 255, 0.03);
}

.buttons button.saving p:after {
    content: attr(data-start);
    top: 50%;
    transform: translateY(-50%);
    color: var(--fg-muted);
}

.buttons button.saving:hover:before,
.buttons button.saving:hover:after,
.buttons button.saving:hover span:before,
.buttons button.saving:hover span:after {
    width: 0;
    height: 0;
}

/* Element Plus 按钮深色主题 */
:deep(.el-button--danger) {
    background: rgba(239, 68, 68, 0.15) !important;
    border-color: rgba(239, 68, 68, 0.3) !important;
    color: #f87171 !important;
}

:deep(.el-button--danger:hover) {
    background: rgba(239, 68, 68, 0.25) !important;
    border-color: rgba(239, 68, 68, 0.5) !important;
}

:deep(.el-button:not(.el-button--danger):not(.download-btn)) {
    background: rgba(255, 255, 255, 0.06) !important;
    border: 1px solid var(--glass-border) !important;
    color: var(--fg-primary) !important;
    transition: all 0.3s ease !important;
}

:deep(.el-button:not(.el-button--danger):not(.download-btn):hover) {
    background: rgba(255, 255, 255, 0.1) !important;
    border-color: var(--accent-primary) !important;
    color: var(--accent-primary) !important;
}

/* Skeleton 深色主题 */
:deep(.el-skeleton__item) {
    background: linear-gradient(90deg, rgba(255, 255, 255, 0.06) 25%, rgba(255, 255, 255, 0.1) 37%, rgba(255, 255, 255, 0.06) 63%) !important;
}
</style>