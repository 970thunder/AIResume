// 环境变量配置
const ENV = {
    development: {
        API_BASE_URL: 'http://localhost:9090/api',
        ALLOWED_ORIGINS: ['http://localhost:5173', 'http://localhost:8080']
    },
    production: {
        API_BASE_URL: '/api',
        ALLOWED_ORIGINS: ['https://www.hyper99.shop']
    }
};

const currentEnv = process.env.NODE_ENV || 'development';
const config = ENV[currentEnv];

export const API_BASE_URL = config.API_BASE_URL;
export const ALLOWED_ORIGINS = config.ALLOWED_ORIGINS;

export const API_URLS = {
    visits: {
        record: `${API_BASE_URL}/visits/record`,
        stats: `${API_BASE_URL}/visits/stats`,
    },
    templates: {
        all: `${API_BASE_URL}/templates`,
        free: `${API_BASE_URL}/templates/free`,
        premium: `${API_BASE_URL}/templates/premium`,
        byId: (id) => `${API_BASE_URL}/templates/${id}`,
        create: `${API_BASE_URL}/templates/create`,
    },
    auth: {
        login: `${API_BASE_URL}/auth/login`,
        register: `${API_BASE_URL}/auth/register`,
        sendVerificationCode: `${API_BASE_URL}/auth/send-verification-code`,
        checkEmail: `${API_BASE_URL}/auth/check-email`,
    },
    admin: {
        templates: `${API_BASE_URL}/admin/templates`,
        updateTemplateStatus: (id) => `${API_BASE_URL}/admin/templates/${id}/status`,
    },
    resume: {
        analyze: `${API_BASE_URL}/resume/analyze`,
        generate: `${API_BASE_URL}/resume/generate`,
        upload: `${API_BASE_URL}/files/upload`,
        history: `${API_BASE_URL}/resume/history`,
        byId: (id) => `${API_BASE_URL}/resume/${id}`,
        update: (id) => `${API_BASE_URL}/resume/${id}`,
        updateTitle: (id) => `${API_BASE_URL}/resume/${id}/title`,
        delete: (id) => `${API_BASE_URL}/resume/${id}`,
    }
};

// 请求头配置
export const getHeaders = () => {
    const headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    };
    const token = localStorage.getItem('token');
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    return headers;
};

// 文件上传请求头
export const getUploadHeaders = () => ({
    'Content-Type': 'multipart/form-data'
}); 