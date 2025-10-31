import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layout/MainLayout.vue'

const routes = [
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '工作台' }
      },
      {
        path: '/video',
        name: 'VideoManagement',
        component: () => import('@/views/VideoManagement.vue'),
        meta: { title: '视频管理' }
      },
      {
        path: '/video-data',
        name: 'VideoData',
        component: () => import('@/views/VideoData.vue'),
        meta: { title: '视频数据' }
      },
      {
        path: '/task',
        name: 'TaskManagement',
        component: () => import('@/views/TaskManagement.vue'),
        meta: { title: '任务管理' }
      },
      {
        path: '/finance',
        name: 'FinanceManagement',
        component: () => import('@/views/FinanceManagement.vue'),
        meta: { title: '财务管理' }
      },
      {
        path: '/influencer',
        name: 'InfluencerManagement',
        component: () => import('@/views/InfluencerManagement.vue'),
        meta: { title: '达人管理' }
      },
      {
        path: '/ranking',
        name: 'RankingManagement',
        component: () => import('@/views/RankingManagement.vue'),
        meta: { title: '榜单管理' }
      },
      {
        path: '/system',
        name: 'SystemManagement',
        component: () => import('@/views/SystemManagement.vue'),
        meta: { title: '系统管理' }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router 