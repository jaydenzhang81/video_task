<template>
  <div class="main-layout">
    <!-- 左侧导航栏 -->
    <div class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="logo">
        <h2>Logo</h2>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        :collapse="isCollapsed"
        background-color="#1e3a8a"
        text-color="#ffffff"
        active-text-color="#3b82f6"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><House /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        
        <el-menu-item index="/video">
          <el-icon><VideoCamera /></el-icon>
          <span>视频管理</span>
        </el-menu-item>
        
        <el-menu-item index="/video-data">
          <el-icon><DataAnalysis /></el-icon>
          <span>视频数据</span>
        </el-menu-item>
        
        <el-menu-item index="/task">
          <el-icon><Document /></el-icon>
          <span>任务管理</span>
        </el-menu-item>
        
        <el-menu-item index="/finance">
          <el-icon><Money /></el-icon>
          <span>财务管理</span>
        </el-menu-item>
        
        <el-menu-item index="/influencer">
          <el-icon><User /></el-icon>
          <span>达人管理</span>
        </el-menu-item>
        
        <el-menu-item index="/ranking">
          <el-icon><Trophy /></el-icon>
          <span>榜单管理</span>
        </el-menu-item>
        
        <el-menu-item index="/system">
          <el-icon><Setting /></el-icon>
          <span>系统管理</span>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 顶部导航栏 -->
      <div class="top-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>一级菜单</el-breadcrumb-item>
            <el-breadcrumb-item>二级菜单</el-breadcrumb-item>
            <el-breadcrumb-item>三级菜单</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-input
            v-model="searchKeyword"
            placeholder="请输入客户名称"
            class="search-input"
            size="small"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          
          <el-button type="primary" size="small">
            <el-icon><User /></el-icon>
            邀请客户注册
          </el-button>
          
          <el-badge :value="2" class="notification-badge">
            <el-icon class="notification-icon"><Bell /></el-icon>
          </el-badge>
          
          <div class="user-info">
            <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
            <span class="username">小豆丁</span>
          </div>
        </div>
      </div>

      <!-- 标签页区域 -->
      <div class="tab-container">
        <div class="tab-description">
          每打开一个菜单在此展示，类似于浏览器的标签，支持快速切换
        </div>
        <div class="tabs">
          <el-tag
            v-for="tab in tabs"
            :key="tab.path"
            :closable="tab.path !== '/dashboard'"
            @close="closeTab(tab)"
            @click="switchTab(tab)"
            :class="{ active: activeTab === tab.path }"
          >
            {{ tab.title }}
          </el-tag>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="content-area">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  House,
  VideoCamera,
  Document,
  Money,
  User,
  Trophy,
  Setting,
  Search,
  Bell
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// 响应式数据
const isCollapsed = ref(false)
const searchKeyword = ref('')
const tabs = ref([
  { path: '/dashboard', title: '工作台' }
])
const activeTab = ref('/dashboard')

// 计算属性
const activeMenu = computed(() => route.path)

// 监听路由变化
watch(() => route.path, (newPath) => {
  addTab(newPath, route.meta.title || '菜单名称')
  activeTab.value = newPath
})

// 方法
const addTab = (path, title) => {
  const existingTab = tabs.value.find(tab => tab.path === path)
  if (!existingTab) {
    tabs.value.push({ path, title })
  }
}

const closeTab = (tab) => {
  const index = tabs.value.findIndex(t => t.path === tab.path)
  if (index > -1) {
    tabs.value.splice(index, 1)
    
    // 如果关闭的是当前标签，切换到前一个标签
    if (activeTab.value === tab.path) {
      const nextTab = tabs.value[index] || tabs.value[index - 1]
      if (nextTab) {
        router.push(nextTab.path)
      }
    }
  }
}

const switchTab = (tab) => {
  router.push(tab.path)
}
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 240px;
  background-color: #1e3a8a;
  transition: width 0.3s;
  display: flex;
  flex-direction: column;
}

.sidebar.collapsed {
  width: 64px;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  border-bottom: 1px solid #374151;
}

.logo h2 {
  margin: 0;
  font-size: 18px;
}

.sidebar-menu {
  flex: 1;
  border: none;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

.top-header {
  height: 60px;
  background-color: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-input {
  width: 200px;
}

.notification-badge {
  cursor: pointer;
}

.notification-icon {
  font-size: 20px;
  color: #6b7280;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
  color: #374151;
}

.tab-container {
  background-color: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  padding: 8px 20px;
}

.tab-description {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 8px;
}

.tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tabs .el-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.tabs .el-tag.active {
  background-color: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.content-area {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
</style>
