<template>
    <div class="page-container">
        <div class="content-wrapper">
            <el-card class="box-card">
                <template #header>
                    <div class="card-header">
                        <h1>个人中心</h1>
                    </div>
                </template>
                <div v-if="authStore.user">
                    <el-descriptions title="账户信息" border :column="2">
                        <template #extra>
                            <el-button type="primary" size="small" @click="handleEditProfile">编辑</el-button>
                        </template>
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
                                    <button class="button"
                                        @click="handleEditTitle(scope.row)"><span>修改标题</span></button>
                                    <button class="bin-button" @click="handleDelete(scope.row.id)">
                                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 39 7"
                                            class="bin-top">
                                            <line stroke-width="4" stroke="white" y2="5" x2="39" y1="5"></line>
                                            <line stroke-width="3" stroke="white" y2="1.5" x2="26.0357" y1="1.5"
                                                x1="12">
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

        <el-dialog v-model="editDialogVisible" title="编辑账户信息" width="400px">
            <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="80px">
                <el-form-item label="用户名" prop="username">
                    <el-input v-model="editForm.username" :prefix-icon="User" />
                </el-form-item>
                <el-form-item label="电子邮箱" prop="email">
                    <el-input v-model="editForm.email" :prefix-icon="Message" />
                </el-form-item>
                <el-form-item v-if="isEmailChanged" label="验证码" prop="verificationCode">
                    <div style="display: flex; gap: 10px; width: 100%;">
                        <el-input v-model="editForm.verificationCode" :prefix-icon="Key" placeholder="输入验证码" />
                        <el-button @click="sendVerificationCode" :loading="sending" :disabled="cooldown > 0"
                            size="small">
                            {{ cooldown > 0 ? `${cooldown}秒` : '发送' }}
                        </el-button>
                    </div>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="editDialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="submitEditProfile" :loading="savingProfile">
                        保存
                    </el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { ElNotification, ElMessageBox } from 'element-plus';
import { User, Message, Key } from '@element-plus/icons-vue';
import axios from 'axios';
import { API_URLS, getHeaders } from '@/config/api';

const authStore = useAuthStore();
const router = useRouter();
const resumes = ref([]);
const loadingResumes = ref(false);

const editDialogVisible = ref(false);
const editFormRef = ref(null);
const savingProfile = ref(false);

const editForm = reactive({
    username: '',
    email: '',
    verificationCode: ''
});

const editRules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
    ],
    email: [
        { required: true, message: '请输入电子邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入有效的电子邮箱地址', trigger: 'blur' }
    ],
    verificationCode: [
        { required: false, message: '请输入验证码', trigger: 'blur' }
    ]
};

const cooldown = ref(0);
const sending = ref(false);

const isEmailChanged = computed(() => {
    return editForm.email !== authStore.user.email;
});

const sendVerificationCode = async () => {
    if (!editForm.email) return;

    sending.value = true;
    try {
        await axios.post(API_URLS.auth.sendVerificationCode, null, {
            params: { email: editForm.email }
        });
        ElNotification({
            title: '成功',
            message: '验证码已发送，请查收',
            type: 'success',
        });

        cooldown.value = 60;
        const timer = setInterval(() => {
            cooldown.value--;
            if (cooldown.value <= 0) {
                clearInterval(timer);
            }
        }, 1000);
    } catch (error) {
        console.error('Error sending code:', error);
        ElNotification({
            title: '错误',
            message: error.response?.data || '发送失败，请稍后重试',
            type: 'error',
        });
    } finally {
        sending.value = false;
    }
};

const handleEditProfile = () => {
    editForm.username = authStore.user.username;
    editForm.email = authStore.user.email;
    editForm.verificationCode = '';
    editDialogVisible.value = true;
};

const submitEditProfile = async () => {
    if (!editFormRef.value) return;

    await editFormRef.value.validate(async (valid) => {
        if (valid) {
            // 如果邮箱变更，必须填写验证码
            if (isEmailChanged.value && !editForm.verificationCode) {
                ElNotification({
                    title: '提示',
                    message: '修改邮箱需要输入验证码',
                    type: 'warning',
                });
                return;
            }

            savingProfile.value = true;
            try {
                const payload = {
                    username: editForm.username
                };

                if (isEmailChanged.value) {
                    payload.email = editForm.email;
                    payload.verificationCode = editForm.verificationCode;
                }

                const response = await axios.put(API_URLS.auth.updateProfile, payload, { headers: getHeaders() });

                // Update store with new token and user info
                authStore.setToken(response.data.token);
                authStore.setUser(response.data.user);

                ElNotification({
                    title: '成功',
                    message: '账户信息更新成功',
                    type: 'success',
                });
                editDialogVisible.value = false;
            } catch (error) {
                console.error('Error updating profile:', error);
                const msg = error.response?.data?.message || '更新失败';
                ElNotification({
                    title: '错误',
                    message: msg,
                    type: 'error',
                });
            } finally {
                savingProfile.value = false;
            }
        }
    });
};

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
    width: 100%;
    height: 100%;
    overflow-y: auto;
    box-sizing: border-box;
    background: radial-gradient(1200px 600px at 20% -20%, rgba(14, 165, 233, 0.08) 40%, #0b1220 100%);
}

.content-wrapper {
    padding: 32px;
    max-width: 1100px;
    margin: 0 auto;
}

/* Card Styles */
.box-card {
    background: linear-gradient(145deg, rgba(30, 41, 59, 0.7), rgba(15, 23, 42, 0.85)) !important;
    border: 1px solid rgba(255, 255, 255, 0.1) !important;
    border-radius: 20px !important;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.05) !important;
    backdrop-filter: blur(20px) !important;
    -webkit-backdrop-filter: blur(20px) !important;
}

:deep(.el-card__header) {
    border-bottom: 1px solid rgba(255, 255, 255, 0.08) !important;
    padding: 24px 28px !important;
    background: rgba(255, 255, 255, 0.02);
}

:deep(.el-card__body) {
    padding: 28px !important;
}

.card-header h1,
.card-header h2 {
    margin: 0;
    font-size: 24px;
    color: #f8fafc;
    font-weight: 800;
    letter-spacing: -0.02em;
}

/* Descriptions */
:deep(.el-descriptions) {
    --el-descriptions-table-border: 1px solid rgba(255, 255, 255, 0.1) !important;
}

:deep(.el-descriptions__title) {
    color: #f1f5f9 !important;
    font-weight: 700 !important;
    font-size: 18px !important;
}

:deep(.el-descriptions__label.el-descriptions__cell) {
    color: #cbd5e1 !important;
    background: #1e293b !important;
    font-weight: 600 !important;
}

:deep(.el-descriptions__content.el-descriptions__cell) {
    color: #ffffff !important;
    background: #334155 !important;
    font-weight: 500 !important;
}

:deep(.el-descriptions__body .el-descriptions__table .el-descriptions__cell) {
    border-color: rgba(255, 255, 255, 0.1) !important;
}

:deep(.el-descriptions__table) {
    background: transparent !important;
}

:deep(.el-descriptions__body) {
    background: transparent !important;
}

:deep(.el-descriptions__table td),
:deep(.el-descriptions__table th) {
    background: transparent !important;
}

:deep(.el-descriptions__extra .el-button--primary) {
    background: linear-gradient(135deg, #0ea5e9, #22d3ee) !important;
    border: none !important;
    color: #0f172a !important;
    font-weight: 600 !important;
    border-radius: 8px !important;
}

:deep(.el-descriptions__extra .el-button--primary:hover) {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(14, 165, 233, 0.4) !important;
}

/* Table Styles */
:deep(.el-table) {
    background: transparent !important;
    --el-table-bg-color: transparent !important;
    --el-table-tr-bg-color: transparent !important;
    --el-table-header-bg-color: rgba(255, 255, 255, 0.06) !important;
    --el-table-header-text-color: #f1f5f9 !important;
    --el-table-text-color: #cbd5e1 !important;
    --el-table-row-hover-bg-color: rgba(14, 165, 233, 0.08) !important;
    --el-table-border-color: rgba(255, 255, 255, 0.08) !important;
}

:deep(.el-table th.el-table__cell) {
    background: rgba(255, 255, 255, 0.06) !important;
    color: #f1f5f9 !important;
    font-weight: 700 !important;
    font-size: 14px !important;
}

:deep(.el-table td.el-table__cell) {
    color: #cbd5e1 !important;
    font-size: 14px !important;
}

:deep(.el-table--enable-row-hover .el-table__body tr:hover > td.el-table__cell) {
    background: rgba(14, 165, 233, 0.1) !important;
}

:deep(.el-table__inner-wrapper::before) {
    background: rgba(255, 255, 255, 0.08) !important;
}

/* Empty State */
:deep(.el-empty__description p) {
    color: #94a3b8 !important;
}

/* Dialog Styles */
:deep(.el-dialog) {
    background: linear-gradient(145deg, rgba(30, 41, 59, 0.95), rgba(15, 23, 42, 0.98)) !important;
    border: 1px solid rgba(255, 255, 255, 0.1) !important;
    border-radius: 20px !important;
    box-shadow: 0 25px 80px rgba(0, 0, 0, 0.5) !important;
    backdrop-filter: blur(20px) !important;
}

:deep(.el-dialog__header) {
    border-bottom: 1px solid rgba(255, 255, 255, 0.08) !important;
    padding: 20px 24px !important;
}

:deep(.el-dialog__title) {
    color: #f8fafc !important;
    font-weight: 700 !important;
    font-size: 18px !important;
}

:deep(.el-dialog__headerbtn .el-dialog__close) {
    color: #64748b !important;
}

:deep(.el-dialog__headerbtn:hover .el-dialog__close) {
    color: #22d3ee !important;
}

:deep(.el-dialog__body) {
    padding: 24px !important;
    color: #cbd5e1 !important;
}

:deep(.el-dialog__footer) {
    border-top: 1px solid rgba(255, 255, 255, 0.08) !important;
    padding: 16px 24px !important;
}

/* Form Styles */
:deep(.el-form-item__label) {
    color: #e2e8f0 !important;
    font-weight: 600 !important;
}

:deep(.el-input__wrapper) {
    background: rgba(15, 23, 42, 0.8) !important;
    border: 1px solid rgba(255, 255, 255, 0.1) !important;
    box-shadow: none !important;
    border-radius: 10px !important;
}

:deep(.el-input__wrapper:hover) {
    border-color: rgba(255, 255, 255, 0.2) !important;
}

:deep(.el-input__wrapper.is-focus) {
    border-color: #0ea5e9 !important;
    box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.2) !important;
}

:deep(.el-input__inner) {
    color: #f1f5f9 !important;
}

:deep(.el-input__inner::placeholder) {
    color: #64748b !important;
}

:deep(.el-input__prefix) {
    color: #64748b !important;
}

/* Dialog Footer Buttons */
:deep(.dialog-footer .el-button:not(.el-button--primary)) {
    background: rgba(255, 255, 255, 0.08) !important;
    border: 1px solid rgba(255, 255, 255, 0.12) !important;
    color: #e2e8f0 !important;
    font-weight: 600 !important;
    border-radius: 8px !important;
}

:deep(.dialog-footer .el-button:not(.el-button--primary):hover) {
    background: rgba(255, 255, 255, 0.12) !important;
    border-color: rgba(255, 255, 255, 0.2) !important;
}

:deep(.dialog-footer .el-button--primary) {
    background: linear-gradient(135deg, #0ea5e9, #22d3ee) !important;
    border: none !important;
    color: #0f172a !important;
    font-weight: 700 !important;
    border-radius: 8px !important;
}

/* Actions Container */
.actions-container {
    display: flex;
    align-items: center;
    gap: 12px;
}

/* Fancy Button - Modern Style */
.fancy {
    background: rgba(255, 255, 255, 0.06);
    border: 1px solid rgba(255, 255, 255, 0.12);
    border-radius: 10px;
    box-sizing: border-box;
    color: #f1f5f9;
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    letter-spacing: 0.02em;
    margin: 0;
    outline: none;
    overflow: visible;
    padding: 12px 24px;
    position: relative;
    text-align: center;
    text-decoration: none;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    user-select: none;
    font-size: 13px;
    min-width: 130px;
}

.fancy::before {
    content: " ";
    width: 4px;
    height: 16px;
    background: linear-gradient(180deg, #0ea5e9, #22d3ee);
    top: 50%;
    left: 12px;
    position: absolute;
    transform: translateY(-50%);
    border-radius: 2px;
    transition: all 0.3s ease;
}

.fancy .text {
    font-size: 13px;
    padding-left: 12px;
    display: block;
    text-align: left;
    transition: all 0.3s ease-in-out;
    color: #f1f5f9;
}

.fancy .top-key,
.fancy .bottom-key-1,
.fancy .bottom-key-2 {
    display: none;
}

.fancy:hover {
    color: #0f172a;
    background: linear-gradient(135deg, #0ea5e9, #22d3ee);
    border-color: transparent;
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(14, 165, 233, 0.35);
}

.fancy:hover::before {
    background: #0f172a;
    width: 4px;
}

.fancy:hover .text {
    color: #0f172a;
}

/* Bin Button - Modern Style */
.bin-button {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 42px;
    height: 42px;
    border-radius: 12px;
    background: rgba(239, 68, 68, 0.15);
    cursor: pointer;
    border: 1px solid rgba(239, 68, 68, 0.3);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
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
    background: rgba(239, 68, 68, 0.25);
    border-color: rgba(239, 68, 68, 0.5);
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(239, 68, 68, 0.25);
}

.bin-button:active {
    transform: scale(0.95);
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

/* Modify Title Button - Modern Style */
.button {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
    border: none;
    background: linear-gradient(135deg, #0ea5e9, #22d3ee);
    color: #0f172a;
    font-family: inherit;
    text-align: center;
    font-size: 13px;
    box-shadow: 0 6px 20px rgba(14, 165, 233, 0.3);
    min-width: 110px;
    padding: 12px 20px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    cursor: pointer;
    font-weight: 600;
}

.button span {
    cursor: pointer;
    display: inline-block;
    position: relative;
    transition: 0.3s;
}

.button span:after {
    content: '';
    position: absolute;
    opacity: 0;
    top: 0;
    right: -20px;
    transition: 0.5s;
}

.button:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 30px rgba(14, 165, 233, 0.4);
}

.button:hover span {
    padding-right: 0;
}

.button:hover span:after {
    opacity: 1;
    right: 0;
}

/* Loading State */
:deep(.el-loading-mask) {
    background: rgba(11, 18, 32, 0.85) !important;
    backdrop-filter: blur(8px);
}

:deep(.el-loading-spinner .el-loading-text) {
    color: #f1f5f9 !important;
}

:deep(.el-loading-spinner .path) {
    stroke: #0ea5e9 !important;
}

/* MessageBox */
:deep(.el-message-box) {
    background: linear-gradient(145deg, rgba(30, 41, 59, 0.95), rgba(15, 23, 42, 0.98)) !important;
    border: 1px solid rgba(255, 255, 255, 0.1) !important;
    border-radius: 20px !important;
    backdrop-filter: blur(20px) !important;
}

:deep(.el-message-box__title) {
    color: #f8fafc !important;
    font-weight: 700 !important;
}

:deep(.el-message-box__content) {
    color: #cbd5e1 !important;
}

:deep(.el-message-box__headerbtn .el-message-box__close) {
    color: #64748b !important;
}

:deep(.el-message-box__headerbtn:hover .el-message-box__close) {
    color: #22d3ee !important;
}

:deep(.el-message-box__btns .el-button--primary) {
    background: linear-gradient(135deg, #0ea5e9, #22d3ee) !important;
    border: none !important;
    color: #0f172a !important;
    font-weight: 600 !important;
    border-radius: 8px !important;
}

:deep(.el-message-box__btns .el-button:not(.el-button--primary)) {
    background: rgba(255, 255, 255, 0.08) !important;
    border: 1px solid rgba(255, 255, 255, 0.12) !important;
    color: #e2e8f0 !important;
    font-weight: 600 !important;
    border-radius: 8px !important;
}

/* Responsive */
@media (max-width: 768px) {
    .content-wrapper {
        padding: 16px;
    }

    .actions-container {
        flex-wrap: wrap;
        gap: 8px;
    }

    .fancy {
        min-width: 110px;
        padding: 10px 16px;
        font-size: 12px;
    }

    .button {
        min-width: 90px;
        padding: 10px 16px;
        font-size: 12px;
    }
}
</style>