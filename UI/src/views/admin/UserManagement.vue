<template>
  <div class="user-management-container">
    <el-card>
      <template #header>
        <div class="header-actions">
          <h3>用户管理</h3>
          <el-button type="primary" @click="fetchUsers">刷新列表</el-button>
        </div>
      </template>

      <el-table :data="users" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column label="角色" width="200">
          <template #default="scope">
            <el-tag v-for="role in scope.row.roles" :key="role.id" class="mr-1">
              {{ role.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <!-- Placeholder for future actions -->
        <el-table-column label="操作" min-width="120">
          <template #default="scope">
             <!-- e.g., Disable user -->
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { API_URLS, getHeaders } from '@/config/api';
import { ElMessage } from 'element-plus';

const users = ref([]);
const loading = ref(false);

onMounted(() => {
  fetchUsers();
});

const fetchUsers = async () => {
  loading.value = true;
  try {
    const res = await axios.get(API_URLS.admin.users, { headers: getHeaders() });
    users.value = res.data;
  } catch (error) {
    ElMessage.error('获取用户列表失败');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.mr-1 {
  margin-right: 4px;
}
</style>
