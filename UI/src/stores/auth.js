import { defineStore } from 'pinia';
import axios from 'axios';
import { API_URLS, getHeaders } from '@/config/api';

const useAuthStore = defineStore('auth', {
    state: () => ({
        token: localStorage.getItem('token') || null,
        user: JSON.parse(localStorage.getItem('user')) || null,
    }),
    getters: {
        isAuthenticated: (state) => !!state.token,
    },
    actions: {
        async login(credentials) {
            const response = await axios.post(API_URLS.auth.login, credentials, {
                headers: getHeaders()
            });
            this.setToken(response.data.token);
            this.setUser(response.data.user);
            return response.data;
        },
        async register(userInfo) {
            const response = await axios.post(API_URLS.auth.register, userInfo, {
                headers: getHeaders()
            });
            this.setToken(response.data.token);
            this.setUser(response.data.user);
            return response.data;
        },
        logout() {
            this.setToken(null);
            this.setUser(null);
        },
        setToken(token) {
            this.token = token;
            if (token) {
                localStorage.setItem('token', token);
                axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
            } else {
                localStorage.removeItem('token');
                delete axios.defaults.headers.common['Authorization'];
            }
        },
        setUser(user) {
            this.user = user;
            if (user) {
                localStorage.setItem('user', JSON.stringify(user));
            } else {
                localStorage.removeItem('user');
            }
        },
        // Initialize authentication from localStorage
        initializeAuth() {
            const token = localStorage.getItem('token');
            const user = localStorage.getItem('user');
            if (token && user) {
                this.setToken(token);
                this.setUser(JSON.parse(user));
            }
        }
    },
});

export { useAuthStore }; 