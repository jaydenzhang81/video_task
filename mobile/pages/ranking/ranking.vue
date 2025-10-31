<template>
  <view class="ranking-container">
    <!-- 页面标题 -->
    <view class="ranking-header">
      <text class="ranking-title">排行榜</text>
    </view>
    
    <!-- 榜单类型切换 -->
    <view class="ranking-tabs">
      <view 
        class="tab-item"
        :class="{ active: rankingType === 'weekly' }"
        @click="switchRankingType('weekly')"
      >
        <text class="tab-text">周榜</text>
      </view>
      <view 
        class="tab-item"
        :class="{ active: rankingType === 'monthly' }"
        @click="switchRankingType('monthly')"
      >
        <text class="tab-text">月榜</text>
      </view>
    </view>
    
    <!-- 排行榜统计 -->
    <view class="ranking-stats" v-if="rankingStats">
      <view class="stats-grid">
        <view class="stat-card">
          <text class="stat-value">{{ rankingStats.totalUsers }}</text>
          <text class="stat-label">参与人数</text>
        </view>
        <view class="stat-card">
          <text class="stat-value">¥{{ formatNumber(rankingStats.totalReward) }}</text>
          <text class="stat-label">总奖励</text>
        </view>
        <view class="stat-card">
          <text class="stat-value">{{ rankingStats.totalVideos }}</text>
          <text class="stat-label">总视频</text>
        </view>
      </view>
    </view>
    
    <!-- 排行榜列表 -->
    <view class="ranking-list">
      <view class="list-header">
        <text class="list-title">排行榜</text>
        <text class="refresh-btn" @click="refreshRanking">🔄</text>
      </view>
      
      <!-- 加载状态 -->
      <view class="loading-container" v-if="loading">
        <text class="loading-text">加载中...</text>
      </view>
      
      <!-- 排行榜内容 -->
      <view class="ranking-content" v-else>
        <!-- 前三名特殊展示 -->
        <view class="top-three" >
          <!-- 第二名 -->
          <view class="top-item second" v-if="rankingList[1]">
            <view class="rank-badge silver">2</view>
            <view class="user-avatar-container">
              <image v-if="rankingList[1].avatar" class="user-avatar" :src="rankingList[1].avatar" mode="aspectFill" />
              <view v-else class="user-avatar-placeholder">
                <text class="avatar-text">{{ rankingList[1].nick }}</text>
              </view>
            </view>
            <text class="user-name">{{ rankingList[1].nick }}</text>
            <text class="user-stats">发布: {{ rankingList[1].videoCount }}个视频</text>
            <text class="user-reward">¥{{ formatNumber(rankingList[1].rewardAmount) }}奖励</text>
          </view>
          
          <!-- 第一名 -->
          <view class="top-item first" v-if="rankingList[0]">
            <view class="rank-badge gold">1</view>
            <view class="user-avatar-container">
              <image v-if="rankingList[0].avatar" class="user-avatar" :src="rankingList[0].avatar" mode="aspectFill" />
              <view v-else class="user-avatar-placeholder">
                <text class="avatar-text">{{ rankingList[0].nick }}</text>
              </view>
            </view>
            <text class="user-name">{{ rankingList[0].nick }}</text>
            <text class="user-stats">发布: {{ rankingList[0].videoCount }}个视频</text>
            <text class="user-reward">¥{{ formatNumber(rankingList[0].rewardAmount) }}奖励</text>
          </view>
          
          <!-- 第三名 -->
          <view class="top-item third" v-if="rankingList[2]">
            <view class="rank-badge bronze">3</view>
            <view class="user-avatar-container">
              <image v-if="rankingList[2].avatar" class="user-avatar" :src="rankingList[2].avatar" mode="aspectFill" />
              <view v-else class="user-avatar-placeholder">
                <text class="avatar-text">{{ rankingList[2].nick }}</text>
              </view>
            </view>
            <text class="user-name">{{ rankingList[2].nick }}</text>
            <text class="user-stats">发布: {{ rankingList[2].videoCount }}个视频</text>
            <text class="user-reward">¥{{ formatNumber(rankingList[2].rewardAmount) }}奖励</text>
          </view>
        </view>
        
        <!-- 其他排名列表 -->
        <view class="other-ranks">
          <view 
            v-for="(item, index) in otherRankingList" 
            :key="item.userId"
            class="rank-item"
          >
            <view class="rank-number">{{ item.rank }}</view>
            <view class="user-avatar-container">
              <image v-if="item.avatar" class="user-avatar" :src="item.avatar" mode="aspectFill" />
              <view v-else class="user-avatar-placeholder">
                <text class="avatar-text">{{ getAvatarText(item.nick) }}</text>
              </view>
            </view>
            <view class="user-info">
              <text class="user-name">{{ item.nick }}</text>
              <text class="user-stats">发布: {{ item.videoCount }}个视频</text>
              <text class="user-reward">¥{{ formatNumber(item.rewardAmount) }}奖励</text>
            </view>
            <view class="rank-change" v-if="item.rankChange !== 0">
              <text class="change-text" :class="item.rankChange > 0 ? 'up' : 'down'">
                {{ item.rankChange > 0 ? '↑' : '↓' }} {{ Math.abs(item.rankChange) }}
              </text>
            </view>
            <view class="rank-change" v-else>
              <text class="change-text no-change">-</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view class="empty-state" v-if="!loading && rankingList.length === 0">
        <text class="empty-icon">🏆</text>
        <text class="empty-text">暂无排行榜数据</text>
        <text class="empty-subtext">快去分享视频参与排名吧</text>
      </view>
    </view>
  </view>
</template>

<script>
import api from '../../utils/api.js'

export default {
  data() {
    return {
      loading: false,
      rankingType: 'weekly', // weekly, monthly
      rankingList: [],
      rankingStats: null
    }
  },

  computed: {
    otherRankingList() {
      // 返回除前三名外的其他排名
      return this.rankingList.slice(3);
    }
  },

  onLoad() {
    this.loadData();
  },

  onPullDownRefresh() {
    this.refreshRanking().then(() => {
      uni.stopPullDownRefresh();
    });
  },

  methods: {
    async loadData() {
      this.loading = true;
      try {
        // 只加载排行榜列表，统计数据已包含在列表中
        await this.loadRankingList();
      } catch (error) {
        console.error('加载排行榜数据失败:', error);
        uni.showToast({
          title: '加载失败，请重试',
          icon: 'none'
        });
      } finally {
        this.loading = false;
      }
    },
    
    async loadRankingList() {
      try {
        const params = {
          type: this.rankingType
        };
        
        const res = await api.ranking.list(params);
        console.log('排行榜接口返回:', res); // 添加调试日志
        
        if (res.code === 10000 && res.data) {
          const data = res.data;
          this.rankingList = data.records || [];
          this.rankingStats = data.stats;
          console.log('排行榜数据:', this.rankingList); // 添加调试日志
          console.log('统计数据:', this.rankingStats); // 添加调试日志
        }
      } catch (error) {
        console.error('加载排行榜列表失败:', error);
        uni.showToast({
          title: '加载排行榜失败',
          icon: 'none'
        });
      }
    },

    async refreshRanking() {
      await this.loadData();
    },
    
    switchRankingType(type) {
      this.rankingType = type;
      this.refreshRanking();
    },
    
    // 工具方法
    formatNumber(num) {
      if (!num) return '0';
      const n = parseFloat(num);
      if (n >= 10000) {
        return (n / 10000).toFixed(1) + 'w';
      }
      return n.toLocaleString();
    },

    getAvatarText(nickname) {
      if (!nickname || nickname.length === 0) {
        return 'TA';
      }
      return nickname.charAt(0).toUpperCase();
    }
  }
}
</script>

<style scoped>
.ranking-container {
  padding: 20rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.ranking-header {
  text-align: center;
  margin-bottom: 30rpx;
}

.ranking-title {
  font-size: 48rpx;
  font-weight: bold;
  color: #333;
}

/* 榜单类型切换 */
.ranking-tabs {
  display: flex;
  background: white;
  border-radius: 15rpx;
  padding: 10rpx;
  margin-bottom: 20rpx;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 20rpx;
  border-radius: 10rpx;
  transition: all 0.3s;
}

.tab-item.active {
  background: #667eea;
}

.tab-text {
  font-size: 28rpx;
  font-weight: 500;
  color: #666;
}

.tab-item.active .tab-text {
  color: white;
}

/* 排行榜统计 */
.ranking-stats {
  margin-bottom: 20rpx;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 15rpx;
}

.stat-card {
  background: white;
  border-radius: 15rpx;
  padding: 30rpx;
  text-align: center;
}

.stat-card .stat-value {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 10rpx;
}

.stat-card .stat-label {
  font-size: 24rpx;
  color: #666;
  display: block;
}

/* 排行榜列表 */
.ranking-list {
  background: white;
  border-radius: 15rpx;
  padding: 30rpx;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.list-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.refresh-btn {
  font-size: 32rpx;
  color: #667eea;
  cursor: pointer;
}

.loading-container {
  padding: 60rpx;
  text-align: center;
}

.loading-text {
  font-size: 28rpx;
  color: #999;
}

/* 前三名特殊展示 */
.top-three {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  gap: 20rpx;
  margin-bottom: 40rpx;
  padding: 20rpx 0;
}

.top-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.top-item.first {
  order: 2;
}

.top-item.second {
  order: 1;
}

.top-item.third {
  order: 3;
}

.rank-badge {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20rpx;
  font-weight: bold;
  color: white;
}

.rank-badge.gold {
  background: linear-gradient(135deg, #FFD700, #FFA500);
}

.rank-badge.silver {
  background: linear-gradient(135deg, #C0C0C0, #A0A0A0);
}

.rank-badge.bronze {
  background: linear-gradient(135deg, #CD7F32, #B8860B);
}

.user-avatar-container {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  overflow: hidden;
  margin-bottom: 15rpx;
  border: 4rpx solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-avatar-placeholder {
  width: 100%;
  height: 100%;
  background-color: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.avatar-text {
  font-size: 40rpx;
  font-weight: bold;
  color: #666;
}

.top-item.first .user-avatar-container {
  width: 140rpx;
  height: 140rpx;
  border-color: #FFD700;
}

.top-item.second .user-avatar-container {
  width: 120rpx;
  height: 120rpx;
  border-color: #C0C0C0;
}

.top-item.third .user-avatar-container {
  width: 100rpx;
  height: 100rpx;
  border-color: #CD7F32;
}

.top-item.first .avatar-text {
  font-size: 30rpx;
}

.top-item.second .avatar-text {
  font-size: 25rpx;
}

.top-item.third .avatar-text {
  font-size: 20rpx;
}

.user-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
  text-align: center;
}

.user-stats {
  font-size: 22rpx;
  color: #666;
  margin-bottom: 5rpx;
  text-align: center;
}

.user-reward {
  font-size: 24rpx;
  color: #52c41a;
  font-weight: 500;
  text-align: center;
}

/* 其他排名列表 */
.other-ranks {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.rank-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #f8f9fa;
  border-radius: 15rpx;
  transition: all 0.3s;
}

.rank-number {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: #667eea;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: bold;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-info .user-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-info .user-stats {
  font-size: 22rpx;
  color: #666;
  margin-bottom: 5rpx;
}

.user-info .user-reward {
  font-size: 24rpx;
  color: #52c41a;
  font-weight: 500;
}

.rank-change {
  padding: 8rpx 16rpx;
  border-radius: 15rpx;
  background: #f0f0f0;
  flex-shrink: 0;
}

.rank-change .change-text {
  font-size: 22rpx;
  font-weight: 500;
}

.rank-change .change-text.up {
  color: #52c41a;
}

.rank-change .change-text.down {
  color: #ff4d4f;
}

.rank-change .change-text.no-change {
  color: #999;
}

.other-ranks .user-avatar-container {
  width: 80rpx;
  height: 80rpx;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.other-ranks .avatar-text {
  font-size: 30rpx;
}

/* 空状态 */
.empty-state {
  padding: 100rpx 40rpx;
  text-align: center;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
  display: block;
}

.empty-text {
  font-size: 32rpx;
  color: #333;
  margin-bottom: 10rpx;
  display: block;
}

.empty-subtext {
  font-size: 26rpx;
  color: #999;
  display: block;
}
</style> 