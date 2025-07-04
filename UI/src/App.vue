<script setup>
import { ref, computed, h } from 'vue';
import { UploadFilled, Document, CircleCheck, CircleClose, Loading, Download } from '@element-plus/icons-vue'
import axios from 'axios';
import { ElNotification, ElLoading } from 'element-plus'

const currentStep = ref(0);
const uploadedFiles = ref([]);
const selectedTemplate = ref(null);
const generatedResume = ref(null); // This will hold the AI analysis data for preview
const generatedResumeWithId = ref(null); // This will hold the final resume object with ID
const isLoading = ref(false);

const resumeTemplates = [
  { id: 1, name: "经典商务", type: "free", description: "适合传统行业和商务场合" },
  { id: 2, name: "现代简约", type: "free", description: "简洁现代，适合各种职位" },
  { id: 3, name: "创意设计", type: "premium", price: "¥29", description: "适合设计师和创意工作者" },
  { id: 4, name: "技术专业", type: "premium", price: "¥35", description: "专为技术人员优化" },
  { id: 5, name: "高端商务", type: "premium", price: "¥45", description: "高级管理层专用模板" },
  { id: 6, name: "学术研究", type: "free", description: "适合学术界和研究人员" }
];

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
      personalInfo: { name: "张三 (模拟)", email: "zhangsan-mock@example.com", phone: "138-0000-0000", address: "模拟数据地址" },
      summary: "这是模拟的个人简介...",
      experience: [{ company: "模拟公司", position: "软件工程师", duration: "2022-2024", description: "负责模拟项目开发..." }],
      education: [{ school: "模拟大学", degree: "计算机科学", duration: "2018-2022" }],
      skills: ["Vue", "Spring Boot", "Element Plus"],
      sessionId: "mock-session-id-123" // Add mock session ID for testing
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
  <div id="app-container">
    <el-container v-loading="isLoading" element-loading-background="rgba(255, 255, 255, 0.8)"
      element-loading-text="AI分析中，请稍候...">
      <el-header class="app-header">
        <h1>AI 智能简历生成器</h1>
      </el-header>
      <el-main>
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
            <el-button type="primary" :disabled="uploadedFiles.length === 0" @click="goToStep(1)" class="step-button">
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
                  :class="{ selected: selectedTemplate?.id === template.id }" @click="selectTemplate(template)">
                  <div class="template-name">{{ template.name }}</div>
                  <div class="template-desc">{{ template.description }}</div>
                  <el-tag :type="template.type === 'free' ? 'success' : 'warning'" size="small">{{ template.type ===
                    'free' ? '免费' : template.price }}</el-tag>
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
            <p class="subtitle">这是根据您的AI分析生成的简历内容预览。</p>
            <div class="resume-preview-container">
              <div class="resume-preview">
                <h3>个人信息</h3>
                <p><strong>姓名:</strong> {{ generatedResume.personalInfo.name }}</p>
                <p><strong>邮箱:</strong> {{ generatedResume.personalInfo.email }}</p>
                <p><strong>电话:</strong> {{ generatedResume.personalInfo.phone }}</p>
                <p><strong>地址:</strong> {{ generatedResume.personalInfo.address }}</p>

                <h3>个人简介</h3>
                <p>{{ generatedResume.summary }}</p>

                <h3>工作经历</h3>
                <div v-for="exp in generatedResume.experience" :key="exp.company" class="experience-item">
                  <p><strong>{{ exp.position }}</strong> at {{ exp.company }} ({{ exp.duration }})</p>
                  <p>{{ exp.description }}</p>
                </div>

                <h3>教育背景</h3>
                <div v-for="edu in generatedResume.education" :key="edu.school" class="education-item">
                  <p><strong>{{ edu.degree }}</strong>, {{ edu.school }} ({{ edu.duration }})</p>
                </div>

                <h3>技能</h3>
                <el-tag v-for="skill in generatedResume.skills" :key="skill" type="primary" class="skill-tag">{{ skill
                  }}</el-tag>
              </div>
            </div>
            <el-button @click="goToStep(2)" class="step-button">返回</el-button>
          </div>

        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<style scoped>
:deep(.el-loading-spinner .el-loading-text) {
  color: #337ecc;
  font-size: 16px;
  margin-top: 10px;
}

#app-container {
  padding: 20px;
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
  text-align: center;
  max-width: 700px;
  margin: 40px auto;
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
