<template>
  <div class="template-audit-container">
    <el-card>
      <template #header>
        <div class="flex justify-between items-center">
          <h3>模板审核</h3>
          <el-button type="primary" @click="fetchTemplates">刷新列表</el-button>
        </div>
      </template>

      <el-table :data="templates" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="模板名称" width="200" />
        <el-table-column prop="author.username" label="作者" width="150" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="scope">
            {{ scope.row.price > 0 ? '￥' + scope.row.price : '免费' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="180" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" @click="previewTemplate(scope.row)">预览</el-button>
            <el-button size="small" type="success" @click="handleAudit(scope.row, true)">通过</el-button>
            <el-button size="small" type="danger" @click="handleAudit(scope.row, false)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Audit Dialog -->
    <el-dialog v-model="auditDialogVisible" title="审核处理" width="400px">
      <p>正在{{ auditAction ? '通过' : '驳回' }}模板：{{ currentTemplate?.name }}</p>
      <el-input
        v-if="!auditAction"
        v-model="auditReason"
        type="textarea"
        placeholder="请输入驳回理由"
        class="mt-2"
      />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="auditDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAudit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Preview Dialog -->
    <el-dialog v-model="previewVisible" title="模板预览" width="80%" top="5vh">
      <div v-if="currentTemplate" class="preview-container">
        <iframe :srcdoc="currentTemplate.htmlContent" frameborder="0" width="100%" height="600px"></iframe>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { API_URLS, getHeaders } from '@/config/api';
import { ElMessage } from 'element-plus';

const templates = ref([]);
const loading = ref(false);
const auditDialogVisible = ref(false);
const previewVisible = ref(false);
const currentTemplate = ref(null);
const auditAction = ref(true); // true = approve, false = reject
const auditReason = ref('');

onMounted(() => {
  fetchTemplates();
});

const fetchTemplates = async () => {
  loading.value = true;
  try {
    const res = await axios.get(API_URLS.admin.pendingTemplates, { headers: getHeaders() });
    templates.value = res.data;
  } catch (error) {
    ElMessage.error('获取待审核模板失败');
  } finally {
    loading.value = false;
  }
};

const previewTemplate = (row) => {
  currentTemplate.value = row;
  previewVisible.value = true;
};

const handleAudit = (row, approved) => {
  currentTemplate.value = row;
  auditAction.value = approved;
  auditReason.value = '';
  auditDialogVisible.value = true;
};

const confirmAudit = async () => {
  try {
    await axios.post(API_URLS.admin.auditTemplate(currentTemplate.value.id), {
      approved: auditAction.value,
      reason: auditReason.value
    }, { headers: getHeaders() });
    
    ElMessage.success('审核操作成功');
    auditDialogVisible.value = false;
    fetchTemplates();
  } catch (error) {
    ElMessage.error('操作失败');
  }
};
</script>

<style scoped>
.mt-2 {
  margin-top: 8px;
}
</style>
