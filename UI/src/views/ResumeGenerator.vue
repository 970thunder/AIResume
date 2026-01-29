<script setup>
import { ref, computed, h, onMounted } from 'vue';
import { UploadFilled, Document, CircleCheck, CircleClose, Loading, Download } from '@element-plus/icons-vue'
import axios from 'axios';
import { ElNotification, ElLoading } from 'element-plus'
import html2pdf from 'html2pdf.js';
import { API_URLS, getHeaders } from '@/config/api';

const currentStep = ref(0);
const uploadedFiles = ref([]);
const selectedTemplate = ref(null);
const generatedResume = ref(null); // This will hold the AI analysis data for preview
const generatedResumeWithId = ref(null); // This will hold the final resume object with ID
const isLoading = ref(false);

// 修改为从数据库获取模板
const resumeTemplates = ref([]);

// 获取模板列表
const fetchTemplates = async () => {
    try {
        const response = await axios.get(API_URLS.templates.all, {
            headers: getHeaders()
        });
        resumeTemplates.value = response.data;
    } catch (error) {
        console.error('Error fetching templates:', error);
        ElNotification({
            title: '错误',
            message: '获取模板列表失败',
            type: 'error',
        });
    }
};

const downloadAsPdf = async () => {
    const elementToPrint = document.querySelector('.final-resume-container .resume-container');
    if (!elementToPrint) {
        ElNotification({
            title: '错误',
            message: '找不到可以下载的简历内容。',
            type: 'error',
        });
        return;
    }

    // Temporarily make the element visible to measure its full height
    elementToPrint.style.display = 'block';
    const contentHeight = elementToPrint.scrollHeight;
    elementToPrint.style.display = ''; // Revert style change

    const loadingInstance = ElLoading.service({
        lock: true,
        text: '正在生成高清PDF，请稍候...',
        background: 'rgba(0, 0, 0, 0.7)',
    })

    const fullName = generatedResume.value?.personalInfo?.fullName || 'resume';
    const fileName = `Resume_${fullName.replace(/\s/g, '_')}.pdf`;

    // Options for html2pdf.js
    const opt = {
        margin: 0,
        filename: fileName,
        image: { type: 'jpeg', quality: 0.98 },
        html2canvas: {
            scale: 2,
            useCORS: true,
            logging: false,
            onclone: (document) => {
                // Ensure the cloned element has no transformations applied
                const clonedElement = document.querySelector('.resume-container');
                if (clonedElement) {
                    clonedElement.style.transform = 'none';
                    clonedElement.style.zoom = '1';
                    clonedElement.style.width = '790px'; // Explicitly set width for PDF rendering
                    clonedElement.style.height = 'auto';
                }
            }
        },
        jsPDF: {
            unit: 'px',
            format: [794, contentHeight + 40], // 使用实际内容高度，添加一些边距
            orientation: 'portrait'
        }
    };

    html2pdf().from(elementToPrint).set(opt).save().then(() => {
        loadingInstance.close();
        ElNotification({
            title: '成功',
            message: 'PDF文件已开始下载。',
            type: 'success',
        });
    }).catch((err) => {
        loadingInstance.close();
        ElNotification({
            title: '错误',
            message: '生成PDF时发生未知错误。',
            type: 'error',
        });
        console.error('PDF generation error:', err);
    });
}

onMounted(async () => {
    // 获取模板列表
    await fetchTemplates();

    // 加载模板HTML内容
    for (const template of resumeTemplates.value) {
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
            ${get(proj, 'repositoryUrl') ? ` | <a href="${get(proj, 'repositoryUrl')}" target="_blank">${get(proj, 'repositoryUrl')}</a>` : ''}
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
        const response = await axios.post(API_URLS.resume.upload, formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });

        const analysisResponse = await axios.post(API_URLS.resume.analyze, {
            sessionId: response.data.sessionId,
            extractedContent: response.data.extractedContent,
        }, {
            headers: getHeaders()
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
    // No other logic needed here, just set the selection.
};

const generateAndPreview = async () => {
    isLoading.value = true;
    try {
        const response = await axios.post(API_URLS.resume.generate, {
            templateId: selectedTemplate.value.id,
            aiAnalysis: generatedResume.value
        }, {
            headers: getHeaders()
        });

        if (response.data) {
            const newResume = response.data.resume || response.data;
            generatedResumeWithId.value = newResume;
            generatedResume.value = newResume.resumeData || newResume;

            goToStep(3);
            ElNotification({
                title: '成功',
                dangerouslyUseHTMLString: true,
                message: '<strong>简历已生成！</strong>',
                type: 'success',
                icon: CircleCheck,
            });
        } else {
            throw new Error(response.data?.message || '生成失败');
        }
    } catch (error) {
        console.error('Error generating resume:', error);
        if (error.response) {
            console.error('Backend Error Response:', error.response.data);
        }
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
    <div class="pixel-generator" v-loading="isLoading" element-loading-background="rgba(255, 255, 255, 0.8)"
        element-loading-text="AI分析中，请稍候...">

        <!-- 像素化背景装饰 -->
        <div class="pixel-bg-decoration"></div>

        <div class="pixel-header" v-if="currentStep < 1">
            <h1 class="pixel-main-title">AI 智能简历生成器</h1>
            <p class="pixel-header-subtitle">让AI帮您打造完美简历</p>
        </div>

        <div class="pixel-main-card">
            <!-- 像素风格步骤指示器 -->
            <div class="pixel-steps">
                <div class="step-item" :class="{ active: currentStep >= 0, completed: currentStep > 0 }">
                    <div class="step-number">1</div>
                    <div class="step-label">上传资料</div>
                </div>
                <div class="step-connector" :class="{ active: currentStep >= 1 }"></div>
                <div class="step-item" :class="{ active: currentStep >= 1, completed: currentStep > 1 }">
                    <div class="step-number">2</div>
                    <div class="step-label">AI 分析</div>
                </div>
                <div class="step-connector" :class="{ active: currentStep >= 2 }"></div>
                <div class="step-item" :class="{ active: currentStep >= 2, completed: currentStep > 2 }">
                    <div class="step-number">3</div>
                    <div class="step-label">选择模板</div>
                </div>
                <div class="step-connector" :class="{ active: currentStep >= 3 }"></div>
                <div class="step-item" :class="{ active: currentStep >= 3, completed: currentStep > 3 }">
                    <div class="step-number">4</div>
                    <div class="step-label">生成预览</div>
                </div>
            </div>

            <!-- Step 1: Upload -->
            <div v-if="currentStep === 0" class="pixel-step-content">
                <div class="pixel-content-card">
                    <h2 class="pixel-step-title">📁 上传您的个人资料</h2>
                    <p class="pixel-subtitle">支持PDF, Word, TXT等格式。AI将自动提取关键信息。</p>

                    <div class="pixel-upload-area">
                        <el-upload drag multiple action="#" :auto-upload="false" :on-change="handleFileChange"
                            :on-remove="handleFileRemove" class="pixel-upload">
                            <div class="upload-content">
                                <div class="upload-icon">📎</div>
                                <div class="upload-text">
                                    <div class="primary-text">将文件拖到此处</div>
                                    <div class="secondary-text">或 <em>点击上传</em></div>
                                </div>
                            </div>
                        </el-upload>
                    </div>

                    <div class="pixel-button-group">
                        <button class="pixel-btn primary" :disabled="uploadedFiles.length === 0" @click="goToStep(1)">
                            下一步 →
                        </button>
                    </div>
                </div>
            </div>

            <!-- Step 2: AI Process -->
            <div v-if="currentStep === 1" class="pixel-step-content">
                <div class="pixel-content-card">
                    <h2 class="pixel-step-title">🤖 AI 智能分析</h2>
                    <p class="pixel-subtitle">我们即将使用 DeepSeek AI 分析您的资料，请确认开始。</p>

                    <div class="pixel-file-list">
                        <h4 class="file-list-title">📋 待处理文件:</h4>
                        <div class="file-tags">
                            <div v-for="file in uploadedFiles" :key="file.uid" class="pixel-file-tag">
                                <span class="file-icon">📄</span>
                                <span class="file-name">{{ file.name }}</span>
                            </div>
                        </div>
                    </div>

                    <div class="pixel-button-group">
                        <button class="pixel-btn primary" @click="processWithAI">
                            <span class="btn-icon">⚡</span>
                            开始AI分析
                        </button>
                        <button class="pixel-btn secondary" @click="goToStep(0)">
                            ← 返回
                        </button>
                    </div>
                </div>
            </div>

            <!-- Step 3: Template Selection -->
            <div v-if="currentStep === 2" class="pixel-step-content">
                <div class="pixel-content-card">
                    <h2 class="pixel-step-title">🎨 选择简历模板</h2>
                    <p class="pixel-subtitle">选择一个最适合您的模板设计。</p>

                    <div class="pixel-templates-grid">
                        <div v-for="template in resumeTemplates" :key="template.id" class="pixel-template-card"
                            :class="{ selected: selectedTemplate && selectedTemplate.id === template.id }"
                            @click="selectTemplate(template)">
                            <div class="template-preview-container">
                                <div v-if="template.html" class="template-preview-wrapper">
                                    <div class="template-preview"
                                        :style="{ transform: 'scale(0.25)', transformOrigin: 'top left' }"
                                        v-html="template.html"></div>
                                </div>
                                <div class="template-overlay">
                                    <div class="select-indicator">✓</div>
                                </div>
                            </div>
                            <div class="template-info">
                                <div class="template-name">{{ template.name }}</div>
                                <div class="template-desc">{{ template.description }}</div>
                                <div class="template-price">
                                    <span class="price-tag" :class="template.type">
                                        {{ template.type === 'free' ? '免费' : `¥${template.price}` }}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="pixel-button-group">
                        <button class="pixel-btn primary" :disabled="!selectedTemplate" @click="generateAndPreview">
                            生成并预览 →
                        </button>
                        <button class="pixel-btn secondary" @click="goToStep(1)">
                            ← 返回
                        </button>
                    </div>
                </div>
            </div>

            <!-- Step 4: Preview -->
            <div v-if="currentStep === 3 && generatedResume" class="pixel-step-content preview-step">
                <div class="pixel-content-card preview-card">
                    <h2 class="pixel-step-title">👀 简历预览</h2>
                    <p class="pixel-subtitle">这是根据您的AI分析和所选模板生成的最终简历预览。</p>

                    <div class="pixel-preview-container">
                        <div class="final-resume-container" v-html="finalResumeHtml"></div>
                    </div>

                    <div class="pixel-button-group">
                        <button class="pixel-btn success" @click="downloadAsPdf">
                            <span class="btn-icon">📥</span>
                            下载为 PDF
                        </button>
                        <button class="pixel-btn secondary" @click="goToStep(2)">
                            ← 返回
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.pixel-generator {
    --main-color: #09465d;
    --bg-color: #fff;
    --font-color: #060606;
    --font-color-sub: #383838;
    --input-focus: #2d8cf0;
    --card-bg: rgb(220, 244, 251);
    --success-color: #67c23a;

    height: 100%;
    background: radial-gradient(circle at 50% 0%, #e0f2fe 0%, #f1f5f9 100%);
    padding: 10px;
    position: relative;
    overflow-y: auto;
    overflow-x: hidden;
    box-sizing: border-box;
    scrollbar-width: none;
    /* Firefox */
    -ms-overflow-style: none;
    /* IE 10+ */
}

.pixel-generator::-webkit-scrollbar {
    display: none;
    /* Chrome/Safari */
}

/* 像素化背景装饰 */
.pixel-bg-decoration {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-image:
        radial-gradient(circle at 15% 25%, var(--main-color) 2px, transparent 2px),
        radial-gradient(circle at 85% 15%, var(--input-focus) 1px, transparent 1px),
        radial-gradient(circle at 70% 75%, var(--main-color) 1px, transparent 1px),
        radial-gradient(circle at 30% 85%, var(--input-focus) 1.5px, transparent 1.5px);
    background-size: 120px 120px, 180px 180px, 100px 100px, 140px 140px;
    opacity: 0.08;
    pointer-events: none;
    z-index: 0;
}

.pixel-header {
    text-align: center;
    margin-bottom: 10px;
    z-index: 1;
    position: relative;
}

.pixel-main-title {
    font-size: 2em;
    color: var(--main-color);
    font-weight: 900;
    margin: 0 0 5px 0;
    text-shadow:
        2px 2px 0 #fff,
        4px 4px 0 rgba(9, 70, 93, 0.3);
    letter-spacing: -1px;
}

.pixel-header-subtitle {
    font-size: 1em;
    color: var(--font-color-sub);
    margin: 0;
    font-weight: 600;
}

.pixel-main-card {
    max-width: 1200px;
    margin: 0 auto;
    background: var(--card-bg);
    border: 2px solid var(--main-color);
    border-radius: 12px;
    box-shadow: 6px 6px 0 var(--main-color);
    padding: 10px;
    position: relative;
    z-index: 1;
}

/* 像素风格步骤指示器 */
.pixel-steps {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 10px;
    flex-wrap: wrap;
    gap: 10px;
}

.step-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
}

.step-number {
    width: 32px;
    height: 32px;
    border: 2px solid var(--font-color-sub);
    background: var(--bg-color);
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 14px;
    color: var(--font-color-sub);
    box-shadow: 2px 2px 0 rgba(9, 70, 93, 0.2);
    transition: all 0.3s ease;
}

.step-item.active .step-number {
    border-color: var(--input-focus);
    background: var(--input-focus);
    color: white;
    box-shadow: 2px 2px 0 rgba(45, 140, 240, 0.3);
}

.step-item.completed .step-number {
    border-color: var(--success-color);
    background: var(--success-color);
    color: white;
    box-shadow: 2px 2px 0 rgba(103, 194, 58, 0.3);
}

.step-label {
    font-size: 14px;
    font-weight: 600;
    color: var(--font-color-sub);
    text-align: center;
}

.step-item.active .step-label {
    color: var(--input-focus);
}

.step-item.completed .step-label {
    color: var(--success-color);
}

.step-connector {
    width: 60px;
    height: 4px;
    background: var(--font-color-sub);
    opacity: 0.3;
    margin: 0 10px;
    border-radius: 2px;
    transition: all 0.3s ease;
}

.step-connector.active {
    background: var(--input-focus);
    opacity: 1;
    box-shadow: 0 2px 4px rgba(45, 140, 240, 0.3);
}

.pixel-step-content {
    display: flex;
    justify-content: center;
}

.pixel-content-card {
    background: var(--bg-color);
    border: 2px solid var(--main-color);
    border-radius: 8px;
    box-shadow: 4px 4px 0 var(--main-color);
    padding: 20px;
    max-width: 800px;
    width: 100%;
    text-align: center;
}

.pixel-content-card.preview-card {
    max-width: 1000px;
}

.pixel-step-title {
    font-size: 1.5em;
    color: var(--main-color);
    font-weight: 700;
    margin: 0 0 10px 0;
}

.pixel-subtitle {
    color: var(--font-color-sub);
    font-size: 1em;
    margin-bottom: 20px;
    font-weight: 500;
}

/* 上传区域 */
.pixel-upload-area {
    margin: 20px 0;
}

:deep(.pixel-upload .el-upload-dragger) {
    border: 3px dashed var(--main-color) !important;
    border-radius: 8px !important;
    background: var(--bg-color) !important;
    box-shadow: 4px 4px 0 rgba(9, 70, 93, 0.2) !important;
    transition: all 0.3s ease !important;
    padding: 30px !important;
}

:deep(.pixel-upload .el-upload-dragger:hover) {
    border-color: var(--input-focus) !important;
    background: rgba(220, 244, 251, 0.5) !important;
    transform: translate(-2px, -2px) !important;
    box-shadow: 6px 6px 0 rgba(9, 70, 93, 0.3) !important;
}

.upload-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 15px;
}

.upload-icon {
    font-size: 3em;
    opacity: 0.7;
}

.upload-text .primary-text {
    font-size: 18px;
    font-weight: 600;
    color: var(--font-color);
    margin-bottom: 5px;
}

.upload-text .secondary-text {
    font-size: 14px;
    color: var(--font-color-sub);
}

.upload-text em {
    color: var(--input-focus);
    font-weight: 600;
}

/* 文件列表 */
.pixel-file-list {
    background: rgba(220, 244, 251, 0.5);
    border: 2px solid rgba(9, 70, 93, 0.2);
    border-radius: 6px;
    padding: 20px;
    margin: 30px 0;
    text-align: left;
}

.file-list-title {
    color: var(--main-color);
    font-weight: 700;
    margin: 0 0 15px 0;
    font-size: 1.1em;
}

.file-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
}

.pixel-file-tag {
    display: flex;
    align-items: center;
    gap: 8px;
    background: var(--bg-color);
    border: 2px solid var(--main-color);
    border-radius: 4px;
    padding: 8px 12px;
    box-shadow: 2px 2px 0 var(--main-color);
    font-weight: 600;
    color: var(--font-color);
}

.file-icon {
    font-size: 1.2em;
}

.file-name {
    font-size: 14px;
}

/* 模板网格 */
.pixel-templates-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 20px;
    margin: 20px 0;
}

.pixel-template-card {
    background: var(--bg-color);
    border: 2px solid var(--main-color);
    border-radius: 8px;
    box-shadow: 4px 4px 0 var(--main-color);
    cursor: pointer;
    transition: all 0.3s ease;
    overflow: hidden;
}

.pixel-template-card:hover {
    transform: translate(-2px, -2px);
    box-shadow: 6px 6px 0 var(--main-color);
}

.pixel-template-card.selected {
    border-color: var(--input-focus);
    box-shadow: 4px 4px 0 var(--input-focus);
}

.pixel-template-card.selected:hover {
    box-shadow: 6px 6px 0 var(--input-focus);
}

.template-preview-container {
    position: relative;
    height: 200px;
    overflow: hidden;
    background: #f9f9f9;
    border-bottom: 2px solid var(--main-color);
}

.template-preview-wrapper {
    height: 100%;
    overflow: hidden;
}

.template-preview {
    width: 840px;
    height: 1188px;
    transform-origin: top left;
}

.template-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(45, 140, 240, 0.1);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.pixel-template-card.selected .template-overlay {
    opacity: 1;
}

.select-indicator {
    width: 50px;
    height: 50px;
    background: var(--input-focus);
    border: 3px solid white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 24px;
    font-weight: bold;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.template-info {
    padding: 20px;
    text-align: center;
}

.template-name {
    font-weight: 700;
    font-size: 1.1em;
    color: var(--main-color);
    margin-bottom: 8px;
}

.template-desc {
    font-size: 14px;
    color: var(--font-color-sub);
    margin-bottom: 12px;
    line-height: 1.4;
    min-height: 40px;
}

.template-price {
    display: flex;
    justify-content: center;
}

.price-tag {
    padding: 4px 12px;
    border-radius: 4px;
    font-weight: 600;
    font-size: 12px;
    border: 2px solid;
}

.price-tag.free {
    background: var(--success-color);
    color: white;
    border-color: var(--success-color);
}

.price-tag.premium {
    background: #f56c6c;
    color: white;
    border-color: #f56c6c;
}

/* 预览容器 */
.pixel-preview-container {
    background: #f9f9f9;
    border: 2px solid var(--main-color);
    border-radius: 6px;
    padding: 20px;
    margin: 30px 0;
    overflow: auto;
    max-height: 600px;
}

.final-resume-container {
    background: white;
    border-radius: 4px;
    overflow: visible;
}

.final-resume-container :deep(.resume-container) {
    margin: 0 auto !important;
    border: none !important;
    box-shadow: none !important;
    transform: none;
}

/* 按钮组 */
.pixel-button-group {
    display: flex;
    gap: 15px;
    justify-content: center;
    flex-wrap: wrap;
    margin-top: 20px;
}

.pixel-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 12px 20px;
    border: 2px solid var(--main-color);
    border-radius: 6px;
    font-weight: 700;
    font-size: 15px;
    cursor: pointer;
    transition: all 0.1s ease;
    text-decoration: none;
    background: var(--bg-color);
    color: var(--main-color);
    box-shadow: 4px 4px 0 var(--main-color);
    min-width: 140px;
}

.pixel-btn.primary {
    background: var(--main-color);
    color: white;
}

.pixel-btn.secondary {
    background: var(--bg-color);
    color: var(--main-color);
}

.pixel-btn.success {
    background: var(--success-color);
    color: white;
    border-color: var(--success-color);
    box-shadow: 4px 4px 0 var(--success-color);
}

.pixel-btn:hover:not(:disabled) {
    transform: translate(1px, 1px);
}

.pixel-btn.primary:hover:not(:disabled) {
    box-shadow: 3px 3px 0 var(--main-color);
}

.pixel-btn.secondary:hover:not(:disabled) {
    box-shadow: 3px 3px 0 var(--main-color);
}

.pixel-btn.success:hover:not(:disabled) {
    box-shadow: 3px 3px 0 var(--success-color);
}

.pixel-btn:active:not(:disabled) {
    transform: translate(4px, 4px);
    box-shadow: 0 0 0;
}

.pixel-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
    background: #ccc;
    border-color: #999;
    color: #666;
    box-shadow: 4px 4px 0 #999;
}

.btn-icon {
    font-size: 1.2em;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .pixel-generator {
        padding: 15px;
    }

    .pixel-main-title {
        font-size: 2.2em;
    }

    .pixel-main-card {
        padding: 25px;
    }

    .pixel-content-card {
        padding: 25px;
    }

    .pixel-steps {
        flex-direction: column;
        gap: 15px;
    }

    .step-connector {
        width: 4px;
        height: 30px;
        margin: 5px 0;
    }

    .pixel-templates-grid {
        grid-template-columns: 1fr;
        gap: 20px;
    }

    .pixel-button-group {
        flex-direction: column;
        align-items: center;
    }

    .pixel-btn {
        width: 100%;
        max-width: 280px;
    }
}

@media (max-width: 480px) {
    .pixel-main-title {
        font-size: 1.8em;
    }

    .pixel-main-card {
        padding: 20px;
    }

    .pixel-content-card {
        padding: 20px;
    }

    .step-number {
        width: 35px;
        height: 35px;
        font-size: 14px;
    }

    .step-label {
        font-size: 12px;
    }
}

/* Loading 样式覆盖 */
:deep(.el-loading-spinner .el-loading-text) {
    color: var(--main-color) !important;
    font-size: 16px !important;
    font-weight: 600 !important;
    margin-top: 10px !important;
}

:deep(.el-loading-spinner .circular) {
    width: 50px !important;
    height: 50px !important;
}

:deep(.el-loading-spinner .path) {
    stroke: var(--input-focus) !important;
    stroke-width: 3 !important;
}
</style>
