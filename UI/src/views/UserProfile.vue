<template>
    <div class="page-container">
        <el-card class="box-card">
            <template #header>
                <div class="card-header">
                    <h1>个人中心</h1>
                </div>
            </template>
            <div v-if="authStore.user">
                <el-descriptions title="账户信息" border :column="2">
                    <el-descriptions-item label="用户名">{{ authStore.user.username }}</el-descriptions-item>
                    <el-descriptions-item label="电子邮箱">{{ authStore.user.email }}</el-descriptions-item>
                    <el-descriptions-item label="注册时间">{{ formattedDate(authStore.user.createdAt)
                        }}</el-descriptions-item>
                </el-descriptions>
            </div>
            <el-empty v-else description="无法加载用户信息" />
        </el-card>

        <el-card class="box-card" style="margin-top: 20px;">
            <template #header>
                <div class="card-header">
                    <h2>简历历史记录</h2>
                </div>
            </template>
            <div v-loading="loadingResumes">
                <el-table :data="resumes" style="width: 100%" v-if="resumes.length > 0">
                    <el-table-column prop="title" label="简历标题" width="250" />
                    <el-table-column prop="createdAt" label="创建时间">
                        <template #default="scope">
                            {{ formattedDate(scope.row.createdAt) }}
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="410">
                        <template #default="scope">
                            <div class="actions-container">
                                <a class="fancy" @click.prevent="handleReEdit(scope.row)">
                                    <span class="top-key"></span>
                                    <span class="text">重新编辑</span>
                                    <span class="bottom-key-1"></span>
                                    <span class="bottom-key-2"></span>
                                </a>
                                <button class="button" @click="handleEditTitle(scope.row)"><span>修改标题</span></button>
                                <button class="bin-button" @click="handleDelete(scope.row.id)">
                                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 39 7"
                                        class="bin-top">
                                        <line stroke-width="4" stroke="white" y2="5" x2="39" y1="5"></line>
                                        <line stroke-width="3" stroke="white" y2="1.5" x2="26.0357" y1="1.5" x1="12">
                                        </line>
                                    </svg>
                                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 33 39"
                                        class="bin-bottom">
                                        <mask fill="white" :id="`path-1-inside-1_8_19_${scope.row.id}`">
                                            <path
                                                d="M0 0H33V35C33 37.2091 31.2091 39 29 39H4C1.79086 39 0 37.2091 0 35V0Z">
                                            </path>
                                        </mask>
                                        <path :mask="`url(#path-1-inside-1_8_19_${scope.row.id})`" fill="white"
                                            d="M0 0H33H0ZM37 35C37 39.4183 33.4183 43 29 43H4C-0.418278 43 -4 39.4183 -4 35H4H29H37ZM4 43C-0.418278 43 -4 39.4183 -4 35V0H4V35V43ZM37 0V35C37 39.4183 33.4183 43 29 43V35V0H37Z">
                                        </path>
                                        <path stroke-width="4" stroke="white" d="M12 6L12 29"></path>
                                        <path stroke-width="4" stroke="white" d="M21 6V29"></path>
                                    </svg>
                                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 89 80"
                                        class="garbage">
                                        <path fill="white"
                                            d="M20.5 10.5L37.5 15.5L42.5 11.5L51.5 12.5L68.75 0L72 11.5L79.5 12.5H88.5L87 22L68.75 31.5L75.5066 25L86 26L87 35.5L77.5 48L70.5 49.5L80 50L77.5 71.5L63.5 58.5L53.5 68.5L65.5 70.5L45.5 73L35.5 79.5L28 67L16 63L12 51.5L0 48L16 25L22.5 17L20.5 10.5Z">
                                        </path>
                                    </svg>
                                </button>
                            </div>
                        </template>
                    </el-table-column>
                </el-table>
                <el-empty v-else description="暂无简历记录，快去生成一份吧！" />
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { ElNotification, ElMessageBox } from 'element-plus';
import axios from 'axios';
import { API_URLS, getHeaders } from '@/config/api';

const authStore = useAuthStore();
const router = useRouter();
const resumes = ref([]);
const loadingResumes = ref(false);

const fetchResumes = async () => {
    loadingResumes.value = true;
    try {
        const response = await axios.get(API_URLS.resume.history, { headers: getHeaders() });
        resumes.value = response.data;
    } catch (error) {
        console.error('Error fetching resume history:', error);
        ElNotification({
            title: '错误',
            message: '获取简历历史记录失败',
            type: 'error',
        });
    } finally {
        loadingResumes.value = false;
    }
};

const handleReEdit = (resume) => {
    router.push({ name: 'Editor', params: { id: resume.id } });
};

const handleEditTitle = async (resume) => {
    try {
        const { value } = await ElMessageBox.prompt('请输入新的简历标题', '修改标题', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            inputValue: resume.title,
            inputValidator: (val) => val && val.trim() !== '',
            inputErrorMessage: '标题不能为空',
        });

        const response = await axios.put(API_URLS.resume.updateTitle(resume.id), { title: value }, { headers: getHeaders() });
        const updatedResume = response.data;
        const index = resumes.value.findIndex(r => r.id === updatedResume.id);
        if (index !== -1) {
            resumes.value[index].title = updatedResume.title;
        }
        ElNotification({ title: '成功', message: '简历标题已更新', type: 'success' });
    } catch (error) {
        if (error !== 'cancel') {
            console.error('Error updating title:', error);
            ElNotification({ title: '错误', message: '标题更新失败', type: 'error' });
        }
    }
};

const handleDelete = async (resumeId) => {
    try {
        await ElMessageBox.confirm('确定要删除这份简历吗？此操作不可撤销。', '确认删除', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
        });

        await axios.delete(API_URLS.resume.delete(resumeId), { headers: getHeaders() });
        resumes.value = resumes.value.filter(r => r.id !== resumeId);
        ElNotification({ title: '成功', message: '简历已删除', type: 'success' });
    } catch (error) {
        if (error !== 'cancel') {
            console.error('Error deleting resume:', error);
            ElNotification({ title: '错误', message: '删除失败', type: 'error' });
        }
    }
};


const formattedDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    });
};

onMounted(() => {
    if (authStore.user) {
        fetchResumes();
    }
});
</script>

<style scoped>
.page-container {
    padding: 20px;
    max-width: 1000px;
    margin: 0 auto;
    height: 100%;
    overflow-y: auto;
    box-sizing: border-box;
}

.card-header h1,
.card-header h2 {
    margin: 0;
    font-size: 24px;
}

.actions-container {
    display: flex;
    align-items: center;
    gap: 12px;
}

.fancy {
    background-color: transparent;
    border: 2px solid #000;
    border-radius: 0;
    box-sizing: border-box;
    color: #fff;
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    letter-spacing: 0.05em;
    margin: 0;
    outline: none;
    overflow: visible;
    padding: 1.25em 2em;
    position: relative;
    text-align: center;
    text-decoration: none;
    text-transform: none;
    transition: all 0.3s ease-in-out;
    user-select: none;
    font-size: 11px;
    width: 160px;
}

.fancy::before {
    content: " ";
    width: 1.25rem;
    height: 2px;
    background: black;
    top: 50%;
    left: 1.5em;
    position: absolute;
    transform: translateY(-50%);
    transform-origin: center;
    transition: background 0.3s linear, width 0.3s linear;
}

.fancy .text {
    font-size: 1.125em;
    line-height: 1.33333em;
    padding-left: 2em;
    display: block;
    text-align: left;
    transition: all 0.3s ease-in-out;
    text-transform: uppercase;
    text-decoration: none;
    color: black;
}

.fancy .top-key {
    height: 2px;
    width: 1.25rem;
    top: -2px;
    left: 0.5rem;
    position: absolute;
    background: #e8e8e8;
    transition: width 0.5s ease-out, left 0.3s ease-out;
}

.fancy .bottom-key-1 {
    height: 2px;
    width: 1.25rem;
    right: 1.5rem;
    bottom: -2px;
    position: absolute;
    background: #e8e8e8;
    transition: width 0.5s ease-out, right 0.3s ease-out;
}

.fancy .bottom-key-2 {
    height: 2px;
    width: 0.5rem;
    right: 0.5rem;
    bottom: -2px;
    position: absolute;
    background: #e8e8e8;
    transition: width 0.5s ease-out, right 0.3s ease-out;
}

.fancy:hover {
    color: white;
    background: rgb(113, 183, 235);
}

.fancy:hover::before {
    width: 0.75rem;
    background: white;
}

.fancy:hover .text {
    color: rgb(255, 255, 255);
    padding-left: 1.5em;
}

.fancy:hover .top-key {
    left: -2px;
    width: 0px;
}

.fancy:hover .bottom-key-1,
.fancy:hover .bottom-key-2 {
    right: 0;
    width: 0;
}

.bin-button {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 44px;
    height: 44px;
    border-radius: 50%;
    background-color: rgb(255, 95, 95);
    cursor: pointer;
    border: 2px solid rgb(255, 201, 201);
    transition-duration: 0.3s;
    position: relative;
    overflow: hidden;
}

.bin-bottom {
    width: 12px;
    z-index: 2;
}

.bin-top {
    width: 14px;
    transform-origin: right;
    transition-duration: 0.3s;
    z-index: 2;
}

.bin-button:hover .bin-top {
    transform: rotate(45deg);
}

.bin-button:hover {
    background-color: rgb(255, 0, 0);
}

.bin-button:active {
    transform: scale(0.9);
}

.garbage {
    position: absolute;
    width: 11px;
    height: auto;
    z-index: 1;
    opacity: 0;
    transition: all 0.3s;
}

.bin-button:hover .garbage {
    animation: throw 0.4s linear;
}

@keyframes throw {
    from {
        transform: translate(-400%, -700%);
        opacity: 0;
    }

    to {
        transform: translate(0%, 0%);
        opacity: 1;
    }
}

.button {
    display: inline-block;
    border-radius: 7px;
    border: none;
    background: #589ef4c4;
    color: white;
    font-family: inherit;
    text-align: center;
    font-size: 13px;
    box-shadow: 0px 14px 56px -11px #1875FF;
    width: 10em;
    padding: 1em;
    transition: all 0.4s;
    cursor: pointer;
}

.button span {
    cursor: pointer;
    display: inline-block;
    position: relative;
    transition: 0.4s;
}

.button span:after {
    content: 'Title';
    position: absolute;
    opacity: 0;
    top: 0;
    right: -20px;
    transition: 0.7s;
}

.button:hover span {
    padding-right: 2.5em;
}

.button:hover span:after {
    opacity: 1;
    right: 0;
}
</style>