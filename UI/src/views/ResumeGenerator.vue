<script setup>
import { ref, computed, h, onMounted } from 'vue';
import { UploadFilled, Document, CircleCheck, CircleClose, Loading, Download } from '@element-plus/icons-vue'
import axios from 'axios';
import { ElNotification, ElLoading } from 'element-plus'

const currentStep = ref(0);
const uploadedFiles = ref([]);
const selectedTemplate = ref(null);
const generatedResume = ref(null); // This will hold the AI analysis data for preview
const generatedResumeWithId = ref(null); // This will hold the final resume object with ID
const isLoading = ref(false);

const resumeTemplates = ref([
    { id: 1, name: "经典商务", type: "free", description: "适合传统行业和商务场合", path: '/src/templates/classic.html', html: '' },
    { id: 2, name: "现代简约", type: "free", description: "简洁现代，适合各种职位", path: '/src/templates/modern.html', html: '' },
    { id: 3, name: "创意设计", type: "premium", price: "¥29", description: "适合设计师和创意工作者" },
    { id: 4, name: "技术专业", type: "premium", price: "¥35", description: "专为技术人员优化" },
    { id: 5, "name": "高端商务", type: "premium", price: "¥45", description: "高级管理层专用模板" },
    { id: 6, name: "学术研究", type: "free", description: "适合学术界和研究人员" }
]);

onMounted(async () => {
    for (const template of resumeTemplates.value) {
        if (template.path) {
            try {
                const response = await fetch(template.path);
                template.html = await response.text();
            } catch (error) {
                console.error(`Error loading template ${template.path}:`, error);
                // Provide fallback content if the template fails to load
                template.html = `<div style='text-align: center; padding: 20px; color: red;'>加载模板失败</div>`;
            }
        }
    }
});

const normalizedResume = computed(() => {
    const rawData = generatedResume.value;
    if (!rawData) return null;

    // Deep copy to avoid mutating the original state
    const data = JSON.parse(JSON.stringify(rawData));

    // Normalize Personal Info
    if (data.personalInfo) {
        data.personalInfo.fullName = data.personalInfo.fullName || data.personalInfo.name;
        if (typeof data.personalInfo.address === 'string') {
            data.personalInfo.address = { city: data.personalInfo.address };
        }
    }

    // Normalize Experience
    if (data.experience) {
        data.experience.forEach(exp => {
            exp.companyName = exp.companyName || exp.company;
            exp.jobTitle = exp.jobTitle || exp.position;
            if (exp.duration && !exp.startDate) {
                const parts = exp.duration.split(/–|-/).map(p => p.trim());
                exp.startDate = parts[0] || '';
                exp.endDate = parts[1] || 'Present';
            }
            if (exp.description && !Array.isArray(exp.responsibilities)) {
                exp.responsibilities = exp.description.split(/[。；\n]/).filter(s => s.trim());
            }
        });
    }

    // Normalize Education
    if (data.education) {
        data.education.forEach(edu => {
            edu.institutionName = edu.institutionName || edu.school;
            if (edu.duration && !edu.graduationDate) {
                const parts = edu.duration.split(/–|-/).map(p => p.trim());
                edu.graduationDate = parts[1] || parts[0] || '';
            }
            if (edu.degree && !edu.major) {
                const parts = edu.degree.split('|').map(p => p.trim());
                edu.degree = parts[0];
                edu.major = parts[1] || '';
            }
        });
    }

    // Normalize Skills
    if (Array.isArray(data.skills)) {
        data.skills = { technicalSkills: data.skills, softSkills: [], tools: [] };
    }

    return data;
});

const finalResumeHtml = computed(() => {
    if (!normalizedResume.value || !selectedTemplate.value || !selectedTemplate.value.html) {
        return '<p>请先完成AI分析并选择一个模板。</p>';
    }

    let html = selectedTemplate.value.html;
    const data = normalizedResume.value;

    // --- Helper function to prevent errors on missing data ---
    const get = (obj, path, defaultValue = '') => {
        const keys = Array.isArray(path) ? path : path.split('.');
        let result = obj;
        for (const key of keys) {
            if (result === null || result === undefined) return defaultValue;
            result = result[key];
        }
        return result === undefined ? defaultValue : result;
    };

    // 1. Populate Personal Info
    html = html.replace(/{{fullName}}/g, get(data, 'personalInfo.fullName'));
    html = html.replace(/{{jobTitle}}/g, get(data, 'personalInfo.jobTitle'));
    html = html.replace(/{{phone}}/g, get(data, 'personalInfo.phone'));
    html = html.replace(/{{email}}/g, get(data, 'personalInfo.email'));
    const address = [get(data, 'personalInfo.address.city'), get(data, 'personalInfo.address.state'), get(data, 'personalInfo.address.country')].filter(Boolean).join(', ');
    html = html.replace(/{{address}}/g, address);

    const links = get(data, 'personalInfo.links', {});
    const linksHtml = Object.entries(links).map(([key, value]) => {
        if (value) return `<li><a href="${value}" target="_blank">${key.replace('Url', '')}</a></li>`;
        return '';
    }).join('');
    html = html.replace('<!-- {{links}} -->', linksHtml);
    html = html.replace('{{links}}', linksHtml);


    // 2. Populate Summary
    html = html.replace(/{{summary}}/g, get(data, 'summary'));

    // 3. Populate Education
    const educationHtml = get(data, 'education', []).map(edu => `
    <div class="item">
      <div class="item-header">
        <span class="title">${get(edu, 'institutionName')} - ${get(edu, 'major')}</span>
        <span class="date">${get(edu, 'graduationDate')}</span>
      </div>
      <div class="item-content">
        <p><strong>${get(edu, 'degree')}</strong> ${get(edu, 'honors') ? `(${get(edu, 'honors')})` : ''}</p>
        ${get(edu, 'gpa') ? `<p>GPA: ${get(edu, 'gpa')}</p>` : ''}
      </div>
    </div>
  `).join('');
    html = html.replace('<!-- {{education}} -->', educationHtml);
    html = html.replace('{{education}}', educationHtml);

    // 4. Populate Experience
    const experienceHtml = get(data, 'experience', []).map(exp => `
    <div class="item">
      <div class="item-header">
        <span class="title">${get(exp, 'jobTitle')} <span class="at-company">at ${get(exp, 'companyName')}</span></span>
        <span class="date">${get(exp, 'startDate')} - ${get(exp, 'endDate')}</span>
      </div>
       <div class="item-subheader">${get(exp, 'location')}</div>
      <ul class="item-content">
        ${get(exp, 'responsibilities', []).map(r => `<li>${r}</li>`).join('')}
      </ul>
    </div>
  `).join('');
    html = html.replace('<!-- {{experience}} -->', experienceHtml);
    html = html.replace('{{experience}}', experienceHtml);

    // 5. Populate Skills
    const skillsData = get(data, 'skills', {});
    const skillsHtml = Object.entries(skillsData).map(([category, skillList]) => {
        if (Array.isArray(skillList) && skillList.length > 0) {
            return `<strong>${category.replace('Skills', '')}:</strong> ` + skillList.map(skill => `<span class="skill-tag">${skill}</span>`).join(' ');
        }
        return '';
    }).filter(Boolean).join('<br>');
    html = html.replace('<!-- {{skills}} -->', `<div class="skills-section">${skillsHtml}</div>`);
    html = html.replace('{{skills}}', `<div class="skills-section">${skillsHtml}</div>`);

    // 6. Populate Projects
    const projectsHtml = get(data, 'projects', []).map(proj => `
    <div class="item">
      <div class="item-header">
        <span class="title">${get(proj, 'projectName')}</span>
        <span class="date">
            ${get(proj, 'projectUrl') ? `<a href="${get(proj, 'projectUrl')}" target="_blank">Live Demo</a>` : ''}
            ${get(proj, 'repositoryUrl') ? ` | <a href="${get(proj, 'repositoryUrl')}" target="_blank">Source Code</a>` : ''}
        </span>
      </div>
      <div class="item-content">
        <p>${get(proj, 'description')}</p>
        <p><strong>Technologies:</strong> ${get(proj, 'technologiesUsed', []).join(', ')}</p>
      </div>
    </div>
  `).join('');
    html = html.replace('<!-- {{projects}} -->', projectsHtml);
    html = html.replace('{{projects}}', projectsHtml);

    // 7. Populate Certifications
    const certsHtml = get(data, 'certifications', []).map(cert => `
    <div class="item">
        <div class="item-header">
            <span class="title">${get(cert, 'name')}</span>
            <span class="date">${get(cert, 'issueDate')}</span>
        </div>
        <div class="item-content">
            <p>Issued by: ${get(cert, 'issuingOrganization')}</p>
        </div>
    </div>
  `).join('');
    html = html.replace('<!-- {{certifications}} -->', certsHtml);
    html = html.replace('{{certifications}}', certsHtml);

    // 8. Populate Languages
    const langsHtml = get(data, 'languages', []).map(lang => `
    <span class="language-item"><strong>${get(lang, 'language')}:</strong> ${get(lang, 'proficiency')}</span>
  `).join(' | ');
    html = html.replace('<!-- {{languages}} -->', langsHtml);
    html = html.replace('{{languages}}', langsHtml);

    return html;
});


const handleFileChange = (file, fileList) => {
    uploadedFiles.value = fileList;
};

const handleFileRemove = (file, fileList) => {
    uploadedFiles.value = fileList;
};

const goToStep = (step) => {
    currentStep.value = step;
};

const processWithAI = async () => {
    isLoading.value = true;
    const formData = new FormData();
    uploadedFiles.value.forEach(file => {
        formData.append('files', file.raw);
    });

    try {
        const response = await axios.post('http://localhost:8080/api/files/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });

        const analysisResponse = await axios.post('http://localhost:8080/api/resume/analyze', {
            sessionId: response.data.sessionId,
            extractedContent: response.data.extractedContent,
        });

        generatedResume.value = analysisResponse.data.analysis;
        generatedResume.value.sessionId = response.data.sessionId;
        goToStep(2);
        ElNotification({
            title: '成功',
            dangerouslyUseHTMLString: true,
            message: '<strong>AI分析完成！</strong>',
            type: 'success',
            icon: CircleCheck,
        });
    } catch (error) {
        console.error('Error during AI processing:', error);
        ElNotification({
            title: '错误',
            dangerouslyUseHTMLString: true,
            message: '<strong>AI处理失败，请检查后端服务和API配置。</strong>',
            type: 'error',
            icon: CircleClose,
        });
        // Fallback for UI testing
        generatedResume.value = {
            personalInfo: {
                fullName: "张三 (模拟)",
                jobTitle: "高级软件工程师",
                email: "zhangsan-mock@example.com",
                phone: "138-0000-0000",
                address: { city: "深圳", state: "广东", country: "中国" },
                links: {
                    linkedInUrl: "https://www.linkedin.com/in/mock",
                    githubUrl: "https://github.com/mock",
                    portfolioUrl: "https://portfolio.mock.com"
                }
            },
            summary: "一位充满激情、注重细节的软件工程师，在设计、开发和维护高性能Web应用方面拥有超过5年的经验。擅长使用Vue和Spring Boot技术栈，并致力于编写整洁、可扩展的代码。",
            experience: [{
                companyName: "模拟科技公司",
                jobTitle: "软件工程师",
                location: "深圳, 广东",
                startDate: "2022-01",
                endDate: "至今",
                responsibilities: [
                    "领导了旗舰产品的重构项目，将前端性能提升了40%。",
                    "设计并实现了一个新的RESTful API网关，处理超过100万次/天的请求。",
                    "指导了两位初级工程师，并主导了团队的代码审查流程。"
                ]
            }],
            education: [{
                institutionName: "模拟大学",
                location: "北京",
                degree: "计算机科学学士",
                major: "计算机科学",
                gpa: "3.8/4.0",
                graduationDate: "2022-06",
                honors: "优秀毕业生"
            }],
            skills: {
                technicalSkills: ["Java", "Spring Boot", "Vue.js", "JavaScript", "SQL"],
                softSkills: ["团队协作", "解决问题", "沟通能力"],
                tools: ["Git", "Docker", "Jira", "IntelliJ IDEA"]
            },
            projects: [{
                projectName: "AI简历生成器",
                description: "一个智能Web应用，可以分析用户上传的简历文件，并使用AI生成结构化的、专业的简历。",
                technologiesUsed: ["Vue.js", "Element Plus", "Spring Boot", "DeepSeek API"],
                repositoryUrl: "https://github.com/mock/ai-resume"
            }],
            certifications: [{
                name: "Oracle认证Java程序员 (OCP)",
                issuingOrganization: "Oracle",
                issueDate: "2023-08"
            }],
            languages: [
                { language: "中文", proficiency: "母语" },
                { language: "English", proficiency: "专业工作能力" }
            ],
            sessionId: "mock-session-id-123"
        };
        goToStep(2);
    } finally {
        isLoading.value = false;
    }
};

const selectTemplate = (template) => {
    selectedTemplate.value = template;
    if (template.type === 'premium') {
        ElNotification({
            title: '提示',
            dangerouslyUseHTMLString: true,
            message: `<strong>选择了付费模板: ${template.name} - ${template.price}</strong>`,
            type: 'info',
        });
    }
};

const generateAndPreview = async () => {
    isLoading.value = true;
    try {
        const response = await axios.post('http://localhost:8080/api/resume/generate', {
            sessionId: generatedResume.value.sessionId,
            templateId: selectedTemplate.value.id,
        });
        if (response.data.success) {
            generatedResumeWithId.value = response.data.resume;
            goToStep(3);
            ElNotification({
                title: '成功',
                dangerouslyUseHTMLString: true,
                message: '<strong>简历已生成！</strong>',
                type: 'success',
                icon: CircleCheck,
            });
        } else {
            throw new Error(response.data.message || '生成失败');
        }
    } catch (error) {
        console.error('Error generating resume:', error);
        ElNotification({
            title: '错误',
            dangerouslyUseHTMLString: true,
            message: `<strong>${error.message || '生成简历时发生错误。'}</strong>`,
            type: 'error',
            icon: CircleClose,
        });
    } finally {
        isLoading.value = false;
    }
}

</script>

<template>
    <div v-loading="isLoading" element-loading-background="rgba(255, 255, 255, 0.8)"
        element-loading-text="AI分析中，请稍候...">
        <div class="app-header">
            <h1>AI 智能简历生成器</h1>
        </div>
        <el-card class="main-card">
            <el-steps :active="currentStep" finish-status="success" align-center>
                <el-step title="上传资料" />
                <el-step title="AI 分析" />
                <el-step title="选择模板" />
                <el-step title="生成预览" />
            </el-steps>

            <!-- Step 1: Upload -->
            <div v-if="currentStep === 0" class="step-content">
                <h2>上传您的个人资料</h2>
                <p class="subtitle">支持PDF, Word, TXT等格式。AI将自动提取关键信息。</p>
                <el-upload drag multiple action="#" :auto-upload="false" :on-change="handleFileChange"
                    :on-remove="handleFileRemove" class="upload-area">
                    <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                    <div class="el-upload__text">
                        将文件拖到此处，或<em>点击上传</em>
                    </div>
                </el-upload>
                <el-button type="primary" :disabled="uploadedFiles.length === 0" @click="goToStep(1)"
                    class="step-button">
                    下一步
                </el-button>
            </div>

            <!-- Step 2: AI Process -->
            <div v-if="currentStep === 1" class="step-content">
                <h2>AI 智能分析</h2>
                <p class="subtitle">我们即将使用 DeepSeek AI 分析您的资料，请确认开始。</p>
                <div class="file-list">
                    <h4>待处理文件:</h4>
                    <el-tag v-for="file in uploadedFiles" :key="file.uid" type="info" class="file-tag">
                        <el-icon>
                            <Document />
                        </el-icon> {{ file.name }}
                    </el-tag>
                </div>
                <el-button type="primary" @click="processWithAI" :icon="Loading" class="step-button">
                    开始AI分析
                </el-button>
                <el-button @click="goToStep(0)" class="step-button">返回</el-button>
            </div>

            <!-- Step 3: Template Selection -->
            <div v-if="currentStep === 2" class="step-content">
                <h2>选择简历模板</h2>
                <p class="subtitle">选择一个最适合您的模板设计。</p>
                <el-row :gutter="20">
                    <el-col :span="8" v-for="template in resumeTemplates" :key="template.id">
                        <el-card shadow="hover" class="template-card"
                            :class="{ selected: selectedTemplate?.id === template.id }"
                            @click="selectTemplate(template)">
                            <div v-if="template.html" class="template-preview-wrapper">
                                <div class="template-preview"
                                    :style="{ transform: 'scale(0.3)', transformOrigin: 'top left' }"
                                    v-html="template.html"></div>
                            </div>
                            <div class="template-info">
                                <div class="template-name">{{ template.name }}</div>
                                <div class="template-desc">{{ template.description }}</div>
                                <el-tag :type="template.type === 'free' ? 'success' : 'warning'" size="small">{{
                                    template.type ===
                                        'free' ? '免费' : template.price }}</el-tag>
                            </div>
                        </el-card>
                    </el-col>
                </el-row>
                <el-button type="primary" :disabled="!selectedTemplate" @click="generateAndPreview" class="step-button">
                    生成并预览
                </el-button>
                <el-button @click="goToStep(1)" class="step-button">返回</el-button>
            </div>

            <!-- Step 4: Preview -->
            <div v-if="currentStep === 3 && generatedResume" class="step-content">
                <h2>简历预览</h2>
                <p class="subtitle">这是根据您的AI分析和所选模板生成的最终简历预览。</p>
                <div class="final-resume-container" v-html="finalResumeHtml"></div>
                <el-button @click="goToStep(2)" class="step-button">返回</el-button>
            </div>

        </el-card>
    </div>
</template>

<style scoped>
:deep(.el-loading-spinner .el-loading-text) {
    color: #337ecc;
    font-size: 16px;
    margin-top: 10px;
}

.app-header {
    text-align: center;
    margin-bottom: 20px;
}

.app-header h1 {
    color: #337ecc;
    font-weight: 600;
}

.main-card {
    max-width: 960px;
    margin: 0 auto;
}

.el-steps {
    margin-bottom: 40px;
}

.step-content {
    max-width: 700px;
    margin: 40px auto;
}

.step-content:not(:has(.final-resume-container)) {
    text-align: center;
}

.step-content h2 {
    font-size: 1.5em;
    margin-bottom: 10px;
    color: #303133;
}

.step-content .subtitle {
    color: #909399;
    margin-bottom: 30px;
}

.upload-area {
    margin-bottom: 20px;
}

.step-button {
    margin-top: 20px;
}

.file-list {
    margin-bottom: 20px;
    text-align: left;
    padding: 10px;
    background-color: #f9fafc;
    border-radius: 4px;
}

.file-tag {
    margin-right: 10px;
    margin-top: 5px;
}

.template-card {
    cursor: pointer;
    border: 2px solid transparent;
    transition: all 0.3s ease;
}

.template-card.selected {
    border-color: #409eff;
    box-shadow: 0 0 15px rgba(64, 158, 255, 0.3);
}

.template-card:hover {
    transform: translateY(-5px);
}

.template-info {
    padding: 14px;
    text-align: center;
}

.template-name {
    font-weight: bold;
    margin-bottom: 5px;
}

.template-desc {
    font-size: 14px;
    color: #606266;
    margin-bottom: 10px;
    min-height: 40px;
}

.template-preview-wrapper {
    height: 280px;
    overflow: hidden;
    border-bottom: 1px solid #ebeef5;
}

.template-preview {
    width: 840px;
    /* Match the content width to keep proportions */
    height: 1188px;
    /* Approximate A4 paper ratio */
}

.final-resume-container {
    border: 1px solid #ebeef5;
    background-color: #f9f9f9;
    padding: 20px;
    overflow: hidden;
}

.final-resume-container :deep(.resume-container) {
    margin: 0 auto !important;
    zoom: 0.75;
    /* Using zoom to scale layout space */
    border: none !important;
    box-shadow: none !important;
    transform: scale(0.85);
    transform-origin: top;
    height: 1100px;
}

.resume-preview-container {
    border: 1px solid #ebeef5;
    border-radius: 4px;
    padding: 20px;
    min-height: 400px;
    background-color: #fafafa;
    text-align: left;
}

.resume-preview h3 {
    font-size: 1.2em;
    color: #337ecc;
    margin: 20px 0 10px 0;
    border-left: 4px solid #337ecc;
    padding-left: 10px;
}

.experience-item,
.education-item {
    margin-bottom: 15px;
    padding-left: 15px;
}

.skill-tag {
    margin-right: 10px;
    margin-bottom: 10px;
}
</style>