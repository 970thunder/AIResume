import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

import MainLayout from '@/layouts/MainLayout.vue';
import HomePage from '../views/HomePage.vue';
import ResumeGenerator from '../views/ResumeGenerator.vue';
import ResumeAnalyzer from '../views/ResumeAnalyzer.vue';
import LoginView from '../views/LoginView.vue';
import RegisterView from '../views/RegisterView.vue';
import TemplateStore from '../views/TemplateStore.vue';
import UserProfile from '../views/UserProfile.vue';
import ResumeEditor from '../views/ResumeEditor.vue';
import TemplateCreator from '../views/TemplateCreator.vue';
import InterviewPrep from '../views/InterviewPrep.vue';

// Admin Views
import AdminLayout from '../views/admin/AdminLayout.vue';
import AdminLogin from '../views/admin/AdminLogin.vue';
import AdminDashboard from '../views/admin/Dashboard.vue';
import TemplateAudit from '../views/admin/TemplateAudit.vue';
import QuestionBank from '../views/admin/QuestionBank.vue';
import UserManagement from '../views/admin/UserManagement.vue';

const routes = [
    {
        path: '/',
        component: MainLayout,
        meta: { requiresAuth: true },
        children: [
            { path: '', redirect: '/home' },
            { path: 'home', name: 'Home', component: HomePage },
            { path: 'generator', name: 'Generator', component: ResumeGenerator },
            { path: 'analysis', name: 'Analysis', component: ResumeAnalyzer },
            { path: 'interview', name: 'Interview', component: InterviewPrep },
            { path: 'store', name: 'Store', component: TemplateStore },
            { path: 'profile', name: 'Profile', component: UserProfile },
            { path: 'editor/:id', name: 'Editor', component: ResumeEditor, props: true },
            { path: 'template/create', name: 'TemplateCreator', component: TemplateCreator },
        ],
    },
    {
        path: '/admin',
        component: AdminLayout,
        meta: { requiresAdmin: true },
        children: [
            { path: '', redirect: '/admin/dashboard' },
            { path: 'dashboard', name: 'AdminDashboard', component: AdminDashboard },
            { path: 'templates', name: 'TemplateAudit', component: TemplateAudit },
            { path: 'questions', name: 'QuestionBank', component: QuestionBank },
            { path: 'users', name: 'UserManagement', component: UserManagement },
        ]
    },
    {
        path: '/admin/login',
        name: 'AdminLogin',
        component: AdminLogin
    },
    {
        path: '/login',
        name: 'Login',
        component: LoginView,
    },
    {
        path: '/register',
        name: 'Register',
        component: RegisterView,
    },
];

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
});

router.beforeEach((to, from, next) => {
    const authStore = useAuthStore();

    // Initialize auth store on first navigation
    if (!authStore.token && localStorage.getItem('token')) {
        authStore.initializeAuth();
    }

    const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
    const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin);

    if (requiresAuth && !authStore.isAuthenticated) {
        next({ name: 'Login' });
    } else if (requiresAdmin && !authStore.isAdmin) {
        // if trying to access admin page without being an admin, redirect to admin login
        next({ name: 'AdminLogin' });
    }
    else {
        next();
    }
});

export default router;
