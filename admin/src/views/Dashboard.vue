<template>
  <div class="dashboard">
    <!-- 数据速览 -->
    <div class="data-overview">
      <div class="section-header">
        <h2>数据速览</h2>
        <div class="date-filters">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="onDateRangeChange"
            size="small"
          />
          <el-select v-model="timeFilter" @change="onTimeFilterChange" size="small" style="width: 100px;">
            <el-option label="今日" value="today" />
            <el-option label="本周" value="week" />
            <el-option label="本月" value="month" />
            <el-option label="本年" value="year" />
          </el-select>
        </div>
        <div class="platform-filters">
          <span 
            v-for="platform in platforms" 
            :key="platform.id"
            :class="{ active: selectedPlatform === platform.id }"
            @click="onPlatformChange(platform.id)"
          >
            {{ platform.name }}
          </span>
        </div>
      </div>

      <div class="data-cards" v-loading="statsLoading">
        <div class="card-row">
          <div class="data-card">
            <div class="card-header">
              <h3>账号数</h3>
              <div class="chart-container">
                <v-chart :option="getAccountChartOption" />
              </div>
            </div>
            <div class="card-content">
              <div class="main-value">{{ dashboardStats?.accountStats?.total || 0 }}</div>
              <div class="sub-values">
                <span>在线: {{ dashboardStats?.accountStats?.online || 0 }}</span>
                <span>过期: {{ dashboardStats?.accountStats?.expired || 0 }}</span>
              </div>
            </div>
          </div>

          <div class="data-card">
            <div class="card-header">
              <h3>已提现</h3>
              <div class="chart-container">
                <v-chart :option="getWithdrawnChartOption" />
              </div>
            </div>
            <div class="card-content">
              <div class="main-value">{{ formatCurrency(dashboardStats?.withdrawStats?.withdrawn) }}</div>
              <div class="comparison">
                <span :class="dashboardStats?.withdrawStats?.monthlyChange >= 0 ? 'increase' : 'decrease'">
                  较上月: {{ formatPercentage(dashboardStats?.withdrawStats?.monthlyChange) }}
                </span>
                <el-icon v-if="dashboardStats?.withdrawStats?.monthlyChange >= 0"><ArrowUp /></el-icon>
                <el-icon v-else><ArrowDown /></el-icon>
              </div>
            </div>
          </div>

          <div class="data-card">
            <div class="card-header">
              <h3>播放量</h3>
              <div class="chart-container">
                <v-chart :option="getPlayCountChartOption" />
              </div>
            </div>
            <div class="card-content">
              <div class="main-value">{{ formatNumber(dashboardStats?.playStats?.totalPlays) }}</div>
              <div class="comparison">
                <span :class="dashboardStats?.playStats?.monthlyChange >= 0 ? 'increase' : 'decrease'">
                  较上月: {{ formatPercentage(dashboardStats?.playStats?.monthlyChange) }}
                </span>
                <el-icon v-if="dashboardStats?.playStats?.monthlyChange >= 0"><ArrowUp /></el-icon>
                <el-icon v-else><ArrowDown /></el-icon>
              </div>
            </div>
          </div>

          <div class="data-card">
            <div class="card-header">
              <h3>发布成功</h3>
              <div class="chart-container">
                <v-chart :option="getSuccessPostChartOption" />
              </div>
            </div>
            <div class="card-content">
              <div class="main-value">{{ dashboardStats?.publishStats?.successCount || 0 }}</div>
              <div class="sub-values">
                <span>发布总数: {{ dashboardStats?.publishStats?.totalCount || 0 }}</span>
              </div>
            </div>
          </div>

          <div class="data-card">
            <div class="card-header">
              <h3>评论数</h3>
              <div class="chart-container">
                <v-chart :option="getCommentChartOption" />
              </div>
            </div>
            <div class="card-content">
              <div class="main-value">{{ formatNumber(dashboardStats?.commentStats?.totalComments) }}</div>
              <div class="comparison">
                <span :class="dashboardStats?.commentStats?.monthlyChange >= 0 ? 'increase' : 'decrease'">
                  较上月: {{ formatPercentage(dashboardStats?.commentStats?.monthlyChange) }}
                </span>
                <el-icon v-if="dashboardStats?.commentStats?.monthlyChange >= 0"><ArrowUp /></el-icon>
                <el-icon v-else><ArrowDown /></el-icon>
              </div>
            </div>
          </div>
        </div>

        <div class="card-row">
          <div class="data-card">
            <div class="card-header">
              <h3>视频总收益</h3>
              <div class="chart-container">
                <v-chart :option="getRevenueChartOption" />
              </div>
            </div>
            <div class="card-content">
              <div class="main-value">{{ formatCurrency(dashboardStats?.revenueStats?.totalRevenue) }}</div>
              <div class="comparison">
                <span :class="dashboardStats?.revenueStats?.monthlyChange >= 0 ? 'increase' : 'decrease'">
                  较上月: {{ formatPercentage(dashboardStats?.revenueStats?.monthlyChange) }}
                </span>
                <el-icon v-if="dashboardStats?.revenueStats?.monthlyChange >= 0"><ArrowUp /></el-icon>
                <el-icon v-else><ArrowDown /></el-icon>
              </div>
            </div>
          </div>

          <div class="data-card">
            <div class="card-header">
              <h3>未提现</h3>
              <div class="chart-container">
                <v-chart :option="getUnwithdrawnChartOption" />
              </div>
            </div>
            <div class="card-content">
              <div class="main-value">{{ formatCurrency(dashboardStats?.withdrawStats?.unwithdrawn) }}</div>
              <div class="comparison">
                <span :class="dashboardStats?.withdrawStats?.monthlyChange >= 0 ? 'increase' : 'decrease'">
                  较上月: {{ formatPercentage(dashboardStats?.withdrawStats?.monthlyChange) }}
                </span>
                <el-icon v-if="dashboardStats?.withdrawStats?.monthlyChange >= 0"><ArrowUp /></el-icon>
                <el-icon v-else><ArrowDown /></el-icon>
              </div>
            </div>
          </div>

          <div class="data-card">
            <div class="card-header">
              <h3>点赞数</h3>
              <div class="chart-container">
                <v-chart :option="getComment2ChartOption" />
              </div>
            </div>
            <div class="card-content">
              <div class="main-value">{{ formatNumber(dashboardStats?.commentStats?.totalComments) }}</div>
              <div class="comparison">
                <span :class="dashboardStats?.commentStats?.monthlyChange >= 0 ? 'increase' : 'decrease'">
                  较上月: {{ formatPercentage(dashboardStats?.commentStats?.monthlyChange) }}
                </span>
                <el-icon v-if="dashboardStats?.commentStats?.monthlyChange >= 0"><ArrowUp /></el-icon>
                <el-icon v-else><ArrowDown /></el-icon>
              </div>
            </div>
          </div>

          <div class="data-card">
            <div class="card-header">
              <h3>发布失败</h3>
              <div class="chart-container">
                <v-chart :option="getFailedPostChartOption" />
              </div>
            </div>
            <div class="card-content">
              <div class="main-value">{{ dashboardStats?.publishStats?.failedCount || 0 }}</div>
              <div class="sub-values">
                <span>发布总数: {{ dashboardStats?.publishStats?.totalCount || 0 }}</span>
              </div>
            </div>
          </div>

          <div class="data-card">
            <div class="card-header">
              <h3>收藏数</h3>
              <div class="chart-container">
                <v-chart :option="getCollectionChartOption" />
              </div>
            </div>
            <div class="card-content">
              <div class="main-value">{{ formatNumber(dashboardStats?.collectionStats?.totalCollections) }}</div>
              <div class="comparison">
                <span :class="dashboardStats?.collectionStats?.monthlyChange >= 0 ? 'increase' : 'decrease'">
                  较上月: {{ formatPercentage(dashboardStats?.collectionStats?.monthlyChange) }}
                </span>
                <el-icon v-if="dashboardStats?.collectionStats?.monthlyChange >= 0"><ArrowUp /></el-icon>
                <el-icon v-else><ArrowDown /></el-icon>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 达人排行榜 -->
    <div class="influencer-ranking">
      <div class="section-header">
        <h2>达人排行榜</h2>
        <div class="ranking-controls">
          <el-radio-group v-model="rankingType" @change="onRankingTypeChange" size="small">
            <el-radio-button label="total">总榜单</el-radio-button>
            <el-radio-button label="monthly">月榜单</el-radio-button>
            <el-radio-button label="weekly">周榜单</el-radio-button>
          </el-radio-group>
          <el-button type="primary" size="small" @click="exportRankingData">数据导出</el-button>
        </div>
      </div>

      <div class="ranking-content">
        <div class="ranking-table">
          <el-table 
            :data="rankingData" 
            v-loading="rankingLoading"
            style="width: 100%"
            :default-sort="{ prop: 'rank', order: 'ascending' }"
          >
            <el-table-column prop="rank" label="排名" width="80" align="center">
              <template #default="{ row }">
                <div class="rank-cell">
                  <span v-if="row.rank <= 3" class="rank-medal" :class="`rank-${row.rank}`">
                    {{ row.rank }}
                  </span>
                  <span v-else>{{ row.rank }}</span>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column prop="influencerName" label="达人信息" min-width="150">
              <template #default="{ row }">
                <div class="influencer-info">
                  <el-avatar :src="row.avatar" :size="32">
                    <span>{{ row.influencerName?.charAt(0) }}</span>
                  </el-avatar>
                  <div class="info-text">
                    <div class="name">{{ row.influencerName }}</div>
                    <div class="phone">{{ row.phone }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column prop="team" label="所在团队" width="120" />
            
            <el-table-column prop="rewardAmount" label="奖励金额" width="120" align="right">
              <template #default="{ row }">
                <span class="amount">{{ formatCurrency(row.rewardAmount) }}</span>
              </template>
            </el-table-column>
            
            <el-table-column prop="videosPosted" label="发布视频数" width="120" align="center" />
            
            <el-table-column label="互动数据" min-width="200">
              <template #default="{ row }">
                <div class="interaction-data">
                  <div class="data-item">
                    <span class="label">播放:</span>
                    <span class="value">{{ formatNumber(row.totalViews) }}</span>
                  </div>
                  <div class="data-item">
                    <span class="label">点赞:</span>
                    <span class="value">{{ formatNumber(row.totalLikes) }}</span>
                  </div>
                  <div class="data-item">
                    <span class="label">评论:</span>
                    <span class="value">{{ formatNumber(row.totalComments) }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="totalRankings"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="onPageSizeChange"
            @current-change="onCurrentPageChange"
            style="margin-top: 20px; justify-content: center;"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { Calendar, ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  LineChart,
  BarChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

// 响应式数据
const selectedPlatform = ref(null) // null表示全部平台
const rankingType = ref('total')
const dateRange = ref([])
const timeFilter = ref('week')

const platforms = ref([
  { id: null, name: '全部' },
  { id: 1, name: '抖音' },
  { id: 2, name: '快手' },
  { id: 3, name: '小红书' }
])

// 数据状态
const statsLoading = ref(false)
const rankingLoading = ref(false)
const dashboardStats = ref(null)
const rankingData = ref([])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(20)
const totalRankings = ref(0)

// API 方法
const fetchDashboardStats = async () => {
  try {
    statsLoading.value = true
    const params = {
      timeFilter: timeFilter.value,
      platformId: selectedPlatform.value,
    }
    
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    const response = await api.get('/admin/dashboard/stats', params)
    if (response.code === 10000) {
      dashboardStats.value = response.data
    } else {
      ElMessage.error(response.message || '获取统计数据失败')
    }
  } catch (error) {
    ElMessage.error('获取统计数据失败')
    console.error('fetchDashboardStats error:', error)
  } finally {
    statsLoading.value = false
  }
}

const fetchRankingData = async () => {
  try {
    rankingLoading.value = true
    const params = {
      type: rankingType.value,
      page: currentPage.value,
      size: pageSize.value,
      platformId: selectedPlatform.value
    }
    
    const response = await api.get('/admin/influencer/ranking', params)
    if (response.code === 10000) {
      rankingData.value = response.data.rankings || []
      totalRankings.value = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取排行榜数据失败')
    }
  } catch (error) {
    ElMessage.error('获取排行榜数据失败')
    console.error('fetchRankingData error:', error)
  } finally {
    rankingLoading.value = false
  }
}

// 事件处理
const onPlatformChange = (platformId) => {
  selectedPlatform.value = platformId
  fetchDashboardStats()
  fetchRankingData()
}

const onTimeFilterChange = () => {
  dateRange.value = [] // 清空日期范围选择
  fetchDashboardStats()
}

const onDateRangeChange = () => {
  timeFilter.value = '' // 清空快捷时间选择
  fetchDashboardStats()
}

const onRankingTypeChange = () => {
  currentPage.value = 1 // 重置页码
  fetchRankingData()
}

const onPageSizeChange = () => {
  currentPage.value = 1
  fetchRankingData()
}

const onCurrentPageChange = () => {
  fetchRankingData()
}

const exportRankingData = async () => {
  try {
    const params = {
      type: 'ranking',
      timeFilter: timeFilter.value,
      platformId: selectedPlatform.value
    }
    
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    const response = await api.post('/admin/export', params)
    if (response.code === 10000) {
      ElMessage.success('导出成功')
    } else {
      ElMessage.error(response.message || '导出失败')
    }
  } catch (error) {
    ElMessage.error('导出失败')
    console.error('exportRankingData error:', error)
  }
}

// 格式化方法
const formatCurrency = (value) => {
  if (!value) return '¥0.00'
  return `¥${Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

const formatNumber = (value) => {
  if (!value) return '0'
  return Number(value).toLocaleString('zh-CN')
}

const formatPercentage = (value) => {
  if (!value) return '0%'
  const sign = value >= 0 ? '+' : ''
  return `${sign}${value.toFixed(1)}%`
}

// 图表配置
const getAccountChartOption = computed(() => ({
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    data: [
      { value: dashboardStats.value?.accountStats?.online || 0, name: '在线' },
      { value: dashboardStats.value?.accountStats?.expired || 0, name: '过期' }
    ],
    label: { show: false },
    emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } },
    color: ['#3b82f6', '#e5e7eb']
  }]
}))

const getWithdrawnChartOption = computed(() => {
  const chartData = dashboardStats.value?.withdrawStats?.chartData || []
  return {
    grid: { top: 5, right: 5, bottom: 5, left: 5 },
    xAxis: { 
      type: 'category', 
      data: chartData.map(item => item.date), 
      show: false 
    },
    yAxis: { type: 'value', show: false },
    series: [{
      data: chartData.map(item => item.value),
      type: 'line',
      smooth: true,
      lineStyle: { color: '#3b82f6', width: 2 },
      areaStyle: { 
        color: { 
          type: 'linear', 
          x: 0, y: 0, x2: 0, y2: 1, 
          colorStops: [
            { offset: 0, color: 'rgba(59, 130, 246, 0.3)' }, 
            { offset: 1, color: 'rgba(59, 130, 246, 0.1)' }
          ] 
        } 
      }
    }]
  }
})

// 通用图表配置生成函数
const createLineChartOption = (chartData, color = '#3b82f6') => ({
  grid: { top: 5, right: 5, bottom: 5, left: 5 },
  xAxis: { type: 'category', data: chartData.map(item => item.date), show: false },
  yAxis: { type: 'value', show: false },
  series: [{
    data: chartData.map(item => item.value),
    type: 'line',
    smooth: true,
    lineStyle: { color, width: 2 },
    areaStyle: {
      color: {
        type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: color + '4D' }, // 30% opacity
          { offset: 1, color: color + '1A' }  // 10% opacity
        ]
      }
    }
  }]
})

const createBarChartOption = (chartData, color = '#3b82f6') => ({
  grid: { top: 5, right: 5, bottom: 5, left: 5 },
  xAxis: { type: 'category', data: chartData.map(item => item.date), show: false },
  yAxis: { type: 'value', show: false },
  series: [{
    data: chartData.map(item => item.value),
    type: 'bar',
    itemStyle: { color }
  }]
})

const getPlayCountChartOption = computed(() => 
  createLineChartOption(dashboardStats.value?.playStats?.chartData || [])
)

const getSuccessPostChartOption = computed(() => 
  createLineChartOption(dashboardStats.value?.publishStats?.chartData || [], '#10b981')
)

const getCommentChartOption = computed(() => 
  createBarChartOption(dashboardStats.value?.commentStats?.chartData || [])
)

const getRevenueChartOption = computed(() => 
  createLineChartOption(dashboardStats.value?.revenueStats?.chartData || [])
)

const getUnwithdrawnChartOption = computed(() => 
  createLineChartOption(dashboardStats.value?.withdrawStats?.chartData || [])
)

const getComment2ChartOption = computed(() => ({
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    data: [
      { value: dashboardStats.value?.commentStats?.totalComments || 0, name: '评论' }
    ],
    label: { show: false },
    emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } },
    color: ['#3b82f6']
  }]
}))

const getFailedPostChartOption = computed(() => 
  createLineChartOption(dashboardStats.value?.publishStats?.chartData || [], '#ef4444')
)

const getCollectionChartOption = computed(() => 
  createBarChartOption(dashboardStats.value?.collectionStats?.chartData || [])
)

// 生命周期
onMounted(() => {
  fetchDashboardStats()
  fetchRankingData()
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.date-filters {
  display: flex;
  gap: 16px;
  align-items: center;
}

.date-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #6b7280;
}

.date-format-note {
  font-size: 12px;
  color: #9ca3af;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #6b7280;
}

.platform-filters {
  display: flex;
  gap: 16px;
}

.platform-filters span {
  padding: 4px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.platform-filters span:hover {
  background-color: #f3f4f6;
}

.platform-filters span.active {
  background-color: #3b82f6;
  color: white;
}

.data-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.data-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.data-card:hover {
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.card-header h3 {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

.chart-container {
  width: 60px;
  height: 40px;
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.main-value {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.sub-values {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #6b7280;
}

.comparison {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

.comparison .increase {
  color: #10b981;
}

.comparison .decrease {
  color: #ef4444;
}

.ranking-controls {
  display: flex;
  gap: 16px;
  align-items: center;
}

.ranking-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.ranking-table {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.ranking-chart {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  min-height: 300px;
}

/* 排名奖牌样式 */
.rank-medal {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  color: white;
  font-weight: bold;
  font-size: 12px;
}

.rank-medal.rank-1 {
  background: linear-gradient(45deg, #ffd700, #ffed4e);
}

.rank-medal.rank-2 {
  background: linear-gradient(45deg, #c0c0c0, #e5e5e5);
}

.rank-medal.rank-3 {
  background: linear-gradient(45deg, #cd7f32, #daa520);
}

/* 达人信息样式 */
.influencer-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-text .name {
  font-weight: 500;
  color: #1f2937;
}

.info-text .phone {
  font-size: 12px;
  color: #6b7280;
}

/* 金额样式 */
.amount {
  font-weight: 600;
  color: #059669;
}

/* 互动数据样式 */
.interaction-data {
  display: flex;
  gap: 12px;
}

.data-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.data-item .label {
  font-size: 12px;
  color: #6b7280;
}

.data-item .value {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

/* 排名单元格样式 */
.rank-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .card-row {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .card-row {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .date-filters {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .platform-filters {
    flex-wrap: wrap;
  }
  
  .ranking-content {
    grid-template-columns: 1fr;
  }
  
  .interaction-data {
    flex-direction: column;
    gap: 4px;
  }
  
  .data-item {
    flex-direction: row;
    justify-content: space-between;
  }
}
</style> 