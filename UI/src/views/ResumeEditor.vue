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
    height: calc(100vh - 60px);
    /* Adjust based on your nav height */
}

.editor-form-panel {
    flex: 1;
    /* Width ratio 1 */
    overflow-y: auto;
    height: 100%;
    padding: 10px;
    box-sizing: border-box;
}

.editor-preview-panel {
    flex: 2;
    /* Width ratio 2 */
    overflow-y: auto;
    height: 100%;
    background-color: #f0f2f5;
    padding: 20px;
    box-sizing: border-box;
    display: flex;
    justify-content: center;
    align-items: flex-start;
}

.form-card,
.preview-card {
    width: 100%;
    height: fit-content;
    /* Let card grow with content */
    display: flex;
    flex-direction: column;
}

.preview-card {
    background: none;
    border: none;
    box-shadow: none;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.resume-form {
    padding: 0 10px;
}

.preview-wrapper {
    width: 840px;
    /* A4-like width */
    height: 1188px;
    /* A4-like height */
    transform: scale(0.75);
    transform-origin: top center;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
    background-color: white;
}

.preview-iframe {
    width: 100%;
    height: 100%;
    border: none;
}

.form-section {
    border: 1px solid #eee;
    padding: 15px;
    margin-bottom: 15px;
    border-radius: 4px;
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
    /* IE and Edge */
    scrollbar-width: none;
    /* Firefox */
}

.header-buttons {
    display: flex;
    align-items: center;
    gap: 20px;
}

.download-btn {
    background-color: #e5eefc;
    color: #3b82f6;
    border: none;
    cursor: pointer;
    border-radius: 8px;
    width: 100px;
    height: 45px;
    transition: 0.3s;
}

.download-btn:hover {
    background-color: #3b82f6;
    box-shadow: 0 0 0 5px #3b83f65f;
    color: #fff;
}

/* New Save Button Styles */
.buttons {
    display: flex;
    justify-content: space-around;
}

.buttons button {
    width: 100px;
    height: 45px;
    background-color: white;
    margin: 0;
    color: #3699c0;
    position: relative;
    overflow: hidden;
    font-size: 14px;
    letter-spacing: 1px;
    font-weight: 500;
    text-transform: uppercase;
    transition: all 0.3s ease;
    cursor: pointer;
    border: 1px solid #e0e0e0;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
}

.buttons button:before,
.buttons button:after {
    content: "";
    position: absolute;
    width: 0;
    height: 2px;
    background-color: #44d8a4;
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
    background-color: #44d8a4;
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
}

.buttons button p:after {
    content: attr(data-text);
    top: 150%;
    color: #44d8a4;
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
}

.buttons button.saving {
    background-color: #f0f0f0;
}

.buttons button.saving p:after {
    content: attr(data-start);
    top: 50%;
    transform: translateY(-50%);
    color: #a0a0a0;
}

.buttons button.saving:hover:before,
.buttons button.saving:hover:after,
.buttons button.saving:hover span:before,
.buttons button.saving:hover span:after {
    width: 0;
    height: 0;
}
</style>