<script setup>
import { ref } from 'vue';
import { House, Document, ShoppingCart, Fold, Expand, MagicStick } from '@element-plus/icons-vue';
import HomePage from './views/HomePage.vue';
import ResumeGenerator from './views/ResumeGenerator.vue';
import TemplateStore from './views/TemplateStore.vue';

const activeView = ref('home');
const isCollapsed = ref(false);

const viewComponents = {
  home: HomePage,
  generator: ResumeGenerator,
  store: TemplateStore,
};

const handleMenuSelect = (index) => {
  activeView.value = index;
};
</script>

<template>
  <el-container class="main-layout">
    <el-aside :width="isCollapsed ? '64px' : '200px'" class="sidebar">
      <div class="sidebar-header">
        <h2 v-if="!isCollapsed" class="sidebar-title">AI Resume</h2>
        <el-icon v-else :size="28" color="#337ecc"><magic-stick /></el-icon>
      </div>
      <el-menu default-active="home" @select="handleMenuSelect" :collapse="isCollapsed" class="sidebar-menu">
        <el-menu-item index="home">
          <el-icon>
            <house />
          </el-icon>
          <template #title>首页</template>
        </el-menu-item>
        <el-menu-item index="generator">
          <el-icon>
            <document />
          </el-icon>
          <template #title>AI 简历</template>
        </el-menu-item>
        <el-menu-item index="store">
          <el-icon><shopping-cart /></el-icon>
          <template #title>模板商城</template>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-toggle" @click="isCollapsed = !isCollapsed">
        <el-icon>
          <Fold v-if="!isCollapsed" />
          <Expand v-else />
        </el-icon>
      </div>
    </el-aside>
    <el-main class="content-area">
      <component :is="viewComponents[activeView]"></component>
    </el-main>
  </el-container>
</template>

<style scoped>
.main-layout {
  height: 100vh;
}

.sidebar {
  background-color: #f4f6f8;
  border-right: 1px solid #e0e0e0;
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5em;
  font-weight: 600;
  color: #337ecc;
  flex-shrink: 0;
  white-space: nowrap;
  overflow: hidden;
}

.sidebar-title {
  margin-top: 6vh;

}

.sidebar-menu {
  margin-top: 5vh;
  flex-grow: 1;
  border-right: none;
  background: transparent;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 100%;
}

.el-menu-item {
  font-size: 1em;
}

.el-menu-item.is-active {
  background-color: #e6f7ff;
  color: #337ecc;
  border-right: 3px solid #337ecc;
}

.content-area {
  padding: 20px;
  background-color: #fff;
  overflow-y: auto;
}

.sidebar-toggle {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background-color: #f0f2f5;
  color: #606266;
  flex-shrink: 0;
}

.sidebar-toggle:hover {
  background-color: #e9ebee;
}

.sidebar-toggle .el-icon {
  font-size: 20px;
}
</style>
