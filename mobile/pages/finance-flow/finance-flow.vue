<template>
  <view class="finance-flow-container">
    <!-- 头部导航 -->
    <view class="header">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <text class="header-title">财务流水</text>
    </view>

    <!-- 筛选条件 -->
    <view class="filter-section">
      <view class="filter-tabs">
        <view 
          class="filter-tab" 
          :class="{ active: currentType === 'all' }"
          @click="changeType('all')"
        >
          <text class="tab-text">全部</text>
        </view>
        <view 
          class="filter-tab" 
          :class="{ active: currentType === 'income' }"
          @click="changeType('income')"
        >
          <text class="tab-text">收入</text>
        </view>
        <view 
          class="filter-tab" 
          :class="{ active: currentType === 'expense' }"
          @click="changeType('expense')"
        >
          <text class="tab-text">支出</text>
        </view>
      </view>
      
      <view class="date-filter" @click="showDatePicker">
        <text class="date-text">{{ dateRangeText }}</text>
        <text class="date-icon">📅</text>
      </view>
    </view>

    <!-- 统计信息 -->
    <view class="stats-section">
      <view class="stats-card">
        <view class="stat-item">
          <text class="stat-label">总收入</text>
          <text class="stat-value income">+{{ formatMoney(totalIncome) }}</text>
        </view>
        <view class="stat-item">
          <text class="stat-label">总支出</text>
          <text class="stat-value expense">-{{ formatMoney(totalExpense) }}</text>
        </view>
        <view class="stat-item">
          <text class="stat-label">净收入</text>
          <text class="stat-value" :class="netIncome >= 0 ? 'income' : 'expense'">
            {{ netIncome >= 0 ? '+' : '' }}{{ formatMoney(netIncome) }}
          </text>
        </view>
      </view>
    </view>

    <!-- 流水列表 -->
    <view class="flow-list">
      <view v-if="loading" class="loading-section">
        <text class="loading-text">加载中...</text>
      </view>
      
      <view v-else-if="flowList.length === 0" class="empty-section">
        <text class="empty-text">暂无财务流水</text>
      </view>
      
      <view v-else>
        <view 
          v-for="(item, index) in flowList" 
          :key="item.id"
          class="flow-item"
        >
          <view class="flow-left">
            <view class="flow-icon" :class="getFlowIconClass(item.type)">
              <text class="icon-text">{{ getFlowIcon(item.type) }}</text>
            </view>
            <view class="flow-info">
              <text class="flow-title">{{ item.title }}</text>
              <text class="flow-desc">{{ item.description }}</text>
              <text class="flow-time">{{ formatTime(item.createTime) }}</text>
            </view>
          </view>
          <view class="flow-right">
            <text 
              class="flow-amount" 
              :class="item.type === 'income' ? 'income' : 'expense'"
            >
              {{ item.type === 'income' ? '+' : '-' }}{{ formatMoney(item.amount) }}
            </text>
            <text class="flow-status" :class="getStatusClass(item.status)">
              {{ getStatusText(item.status) }}
            </text>
          </view>
        </view>
      </view>
    </view>

    <!-- 日期选择器 -->
    <view v-if="showDateModal" class="modal-overlay" @click="closeDateModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">选择时间范围</text>
          <view class="modal-close" @click="closeDateModal">
            <text class="close-text">×</text>
          </view>
        </view>
        <view class="modal-body">
          <view class="date-inputs">
            <view class="date-input-item">
              <text class="date-label">开始时间</text>
              <picker 
                mode="date" 
                :value="startDate" 
                @change="onStartDateChange"
                class="date-picker"
              >
                <view class="picker-text">{{ startDate }}</view>
              </picker>
            </view>
            <view class="date-input-item">
              <text class="date-label">结束时间</text>
              <picker 
                mode="date" 
                :value="endDate" 
                @change="onEndDateChange"
                class="date-picker"
              >
                <view class="picker-text">{{ endDate }}</view>
              </picker>
            </view>
          </view>
        </view>
        <view class="modal-footer">
          <button class="modal-btn cancel-btn" @click="closeDateModal">取消</button>
          <button class="modal-btn confirm-btn" @click="confirmDateRange">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import api from '../../utils/api.js'

export default {
  data() {
    return {
      currentType: 'all', // all, income, expense
      startDate: '',
      endDate: '',
      showDateModal: false,
      loading: false,
      flowList: [],
      totalIncome: 0,
      totalExpense: 0,
      netIncome: 0
    }
  },

  computed: {
    dateRangeText() {
      if (this.startDate && this.endDate) {
        return `${this.startDate} 至 ${this.endDate}`
      }
      return '选择时间范围'
    }
  },

  onLoad() {
    this.initDateRange()
    this.loadFinanceFlow()
  },

  onPullDownRefresh() {
    this.loadFinanceFlow()
    setTimeout(() => {
      uni.stopPullDownRefresh()
    }, 1000)
  },

  methods: {
    // 初始化日期范围（默认最近30天）
    initDateRange() {
      const endDate = new Date()
      const startDate = new Date()
      startDate.setDate(startDate.getDate() - 30)
      
      this.endDate = this.formatDate(endDate)
      this.startDate = this.formatDate(startDate)
    },

    // 格式化日期
    formatDate(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },

    // 返回上一页
    goBack() {
      uni.navigateBack()
    },

    // 切换类型
    changeType(type) {
      this.currentType = type
      this.loadFinanceFlow()
    },

    // 显示日期选择器
    showDatePicker() {
      this.showDateModal = true
    },

    // 关闭日期选择器
    closeDateModal() {
      this.showDateModal = false
    },

    // 开始日期变化
    onStartDateChange(e) {
      this.startDate = e.detail.value
    },

    // 结束日期变化
    onEndDateChange(e) {
      this.endDate = e.detail.value
    },

    // 确认日期范围
    confirmDateRange() {
      if (this.startDate && this.endDate && this.startDate > this.endDate) {
        uni.showToast({
          title: '开始时间不能晚于结束时间',
          icon: 'none'
        })
        return
      }
      this.closeDateModal()
      this.loadFinanceFlow()
    },

    // 加载财务流水
    async loadFinanceFlow() {
      try {
        this.loading = true
        
        const params = {
          type: this.currentType === 'all' ? null : this.currentType,
          startDate: this.startDate,
          endDate: this.endDate
        }

        const res = await api.user.financeFlow(params)
        
        if (res.code === 0 || res.code === 10000) {
          this.flowList = res.data.list || []
          this.totalIncome = res.data.totalIncome || 0
          this.totalExpense = res.data.totalExpense || 0
          this.netIncome = this.totalIncome - this.totalExpense
        } else {
          uni.showToast({
            title: res.message || '加载失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('加载财务流水失败:', error)
        uni.showToast({
          title: '加载失败，请重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },

    // 获取流水图标
    getFlowIcon(type) {
      const iconMap = {
        'income': '💰',
        'expense': '💸',
        'withdraw': '💳',
        'reward': '🎁',
        'refund': '↩️'
      }
      return iconMap[type] || '📊'
    },

    // 获取流水图标样式
    getFlowIconClass(type) {
      return `icon-${type}`
    },

    // 获取状态样式
    getStatusClass(status) {
      const statusMap = {
        0: 'pending',
        1: 'success',
        2: 'failed'
      }
      return statusMap[status] || 'pending'
    },

    // 获取状态文本
    getStatusText(status) {
      const statusMap = {
        0: '处理中',
        1: '已完成',
        2: '失败'
      }
      return statusMap[status] || '处理中'
    },

    // 格式化金额
    formatMoney(amount) {
      return Number(amount).toFixed(2)
    },

    // 格式化时间
    formatTime(timestamp) {
      const date = new Date(timestamp)
      const now = new Date()
      const diff = now - date
      
      // 小于1分钟
      if (diff < 60000) {
        return '刚刚'
      }
      
      // 小于1小时
      if (diff < 3600000) {
        return `${Math.floor(diff / 60000)}分钟前`
      }
      
      // 小于1天
      if (diff < 86400000) {
        return `${Math.floor(diff / 3600000)}小时前`
      }
      
      // 超过1天，显示具体日期
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      
      return `${month}-${day} ${hours}:${minutes}`
    }
  }
}
</script>

<style scoped>
.finance-flow-container {
  background: #f5f5f5;
  min-height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: white;
  border-bottom: 1rpx solid #eee;
}

.back-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 36rpx;
  color: #333;
}

.header-title {
  flex: 1;
  text-align: center;
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-right: 60rpx;
}

.filter-section {
  background: white;
  padding: 20rpx;
  border-bottom: 1rpx solid #eee;
}

.filter-tabs {
  display: flex;
  margin-bottom: 20rpx;
}

.filter-tab {
  flex: 1;
  text-align: center;
  padding: 20rpx 0;
  border-radius: 10rpx;
  margin: 0 10rpx;
  background: #f5f5f5;
}

.filter-tab.active {
  background: #1890ff;
}

.tab-text {
  font-size: 28rpx;
  color: #666;
}

.filter-tab.active .tab-text {
  color: white;
  font-weight: bold;
}

.date-filter {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx;
  background: #f8f9fa;
  border-radius: 10rpx;
}

.date-text {
  font-size: 28rpx;
  color: #333;
  margin-right: 10rpx;
}

.date-icon {
  font-size: 24rpx;
}

.stats-section {
  padding: 20rpx;
}

.stats-card {
  background: white;
  border-radius: 20rpx;
  padding: 30rpx;
  display: flex;
  justify-content: space-around;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 24rpx;
  color: #666;
  margin-bottom: 10rpx;
}

.stat-value {
  font-size: 32rpx;
  font-weight: bold;
}

.stat-value.income {
  color: #52c41a;
}

.stat-value.expense {
  color: #ff4d4f;
}

.flow-list {
  padding: 0 20rpx;
}

.loading-section,
.empty-section {
  text-align: center;
  padding: 100rpx 0;
}

.loading-text,
.empty-text {
  font-size: 28rpx;
  color: #999;
}

.flow-item {
  background: white;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.flow-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.flow-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.icon-income {
  background: #e6f7ff;
}

.icon-expense {
  background: #fff2f0;
}

.icon-withdraw {
  background: #f6ffed;
}

.icon-reward {
  background: #fff7e6;
}

.icon-refund {
  background: #f0f0f0;
}

.icon-text {
  font-size: 32rpx;
}

.flow-info {
  flex: 1;
}

.flow-title {
  display: block;
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.flow-desc {
  display: block;
  font-size: 24rpx;
  color: #666;
  margin-bottom: 8rpx;
}

.flow-time {
  display: block;
  font-size: 22rpx;
  color: #999;
}

.flow-right {
  text-align: right;
}

.flow-amount {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.flow-amount.income {
  color: #52c41a;
}

.flow-amount.expense {
  color: #ff4d4f;
}

.flow-status {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
}

.flow-status.pending {
  background: #fff7e6;
  color: #fa8c16;
}

.flow-status.success {
  background: #f6ffed;
  color: #52c41a;
}

.flow-status.failed {
  background: #fff2f0;
  color: #ff4d4f;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 600rpx;
  background: white;
  border-radius: 20rpx;
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.modal-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.modal-close {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-text {
  font-size: 40rpx;
  color: #999;
}

.modal-body {
  padding: 30rpx;
}

.date-inputs {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.date-input-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.date-label {
  font-size: 28rpx;
  color: #333;
  width: 120rpx;
}

.date-picker {
  flex: 1;
}

.picker-text {
  padding: 20rpx;
  border: 1rpx solid #ddd;
  border-radius: 10rpx;
  font-size: 28rpx;
  color: #333;
  text-align: center;
}

.modal-footer {
  display: flex;
  border-top: 1rpx solid #f0f0f0;
}

.modal-btn {
  flex: 1;
  height: 88rpx;
  border: none;
  font-size: 28rpx;
  background: white;
}

.cancel-btn {
  color: #666;
  border-right: 1rpx solid #f0f0f0;
}

.confirm-btn {
  color: #1890ff;
  font-weight: bold;
}
</style>
