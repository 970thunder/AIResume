import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

import MainLayout from '@/layouts/MainLayout.vue';
import HomePage from '../views/HomePage.vue';
import ResumeGenerator from '../views/ResumeGenerator.vue';
import LoginView from '../views/LoginView.vue';
import RegisterView from '../views/RegisterView.vue';
import TemplateStore from '../views/TemplateStore.vue';
import UserProfile from '../views/UserProfile.vue';
import ResumeEditor from '../views/ResumeEditor.vue';

const routes = [
    {
        path: '/',
        component: MainLayout,
        meta: { requiresAuth: true },
        children: [
            { path: '', redirect: '/home' },
            { path: 'home', name: 'Home', component: HomePage },
            { path: 'generator', name: 'Generator', component: ResumeGenerator },
            { path: 'store', name: 'Store', component: TemplateStore },
            { path: 'profile', name: 'Profile', component: UserProfile },
            { path: 'editor/:id', name: 'Editor', component: ResumeEditor, props: true },
        ],
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

    if (requiresAuth && !authStore.isAuthenticated) {
        next({ name: 'Login' });
    } else {
        next();
    }
});

export default router; 