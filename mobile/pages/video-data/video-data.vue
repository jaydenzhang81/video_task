<template>
  <view class="data-container">
    <!-- 数据统计 -->
    <view class="stats-section">
      <view class="section-header">
        <text class="section-title">互动数据</text>
        <text class="refresh-btn" @click="refreshData">🔄</text>
      </view>
      <view class="stats-grid">
        <!-- 播放量 -->
        <view class="stat-card">
          <view class="stat-value">{{ statsData.totalViews || '0' }}</view>
          <view class="stat-label">播放量</view>
          <view class="stat-trend" v-if="statsData.playCountTrend">
            <text class="trend-text" :class="statsData.playCountTrend > 0 ? 'up' : 'down'">
              {{ statsData.playCountTrend > 0 ? '↑' : '↓' }} {{ Math.abs(statsData.playCountTrend) }}%
            </text>
          </view>
          <view class="stat-icon">▶</view>
        </view>
        
        <!-- 点赞数 -->
        <view class="stat-card">
          <view class="stat-value">{{ statsData.totalLikes || '0' }}</view>
          <view class="stat-label">点赞数</view>
          <view class="stat-trend" v-if="statsData.likeCountTrend">
            <text class="trend-text" :class="statsData.likeCountTrend > 0 ? 'up' : 'down'">
              {{ statsData.likeCountTrend > 0 ? '↑' : '↓' }} {{ Math.abs(statsData.likeCountTrend) }}%
            </text>
          </view>
          <view class="stat-icon">👍</view>
        </view>
        
        <!-- 评论数 -->
        <view class="stat-card">
          <view class="stat-value">{{ statsData.totalComments || '0' }}</view>
          <view class="stat-label">评论数</view>
          <view class="stat-trend" v-if="statsData.commentCountTrend">
            <text class="trend-text" :class="statsData.commentCountTrend > 0 ? 'up' : 'down'">
              {{ statsData.commentCountTrend > 0 ? '↑' : '↓' }} {{ Math.abs(statsData.commentCountTrend) }}%
            </text>
          </view>
          <view class="stat-icon">💬</view>
        </view>
        
        <!-- 发布视频 -->
        <view class="stat-card">
          <view class="stat-value">{{ statsData.totalShares || '0' }}</view>
          <view class="stat-label">发布视频</view>
          <view class="stat-trend" v-if="statsData.shareCountTrend">
            <text class="trend-text" :class="statsData.shareCountTrend > 0 ? 'up' : 'down'">
              {{ statsData.shareCountTrend > 0 ? '↑' : '↓' }} {{ Math.abs(statsData.shareCountTrend) }}%
            </text>
          </view>
          <view class="stat-icon">📤</view>
        </view>
        
        <!-- 收藏数 -->
        <view class="stat-card">
          <view class="stat-value">{{ statsData.totalCollects || '0' }}</view>
          <view class="stat-label">收藏数</view>
          <view class="stat-trend" v-if="statsData.collectCountTrend">
            <text class="trend-text" :class="statsData.collectCountTrend > 0 ? 'up' : 'down'">
              {{ statsData.collectCountTrend > 0 ? '↑' : '↓' }} {{ Math.abs(statsData.collectCountTrend) }}%
            </text>
          </view>
          <view class="stat-icon">⭐</view>
        </view>
        
        <!-- 奖励金额 -->
        <view class="stat-card">
          <view class="stat-value">{{ formatReward(statsData.totalReward) }}</view>
          <view class="stat-label">奖励金额</view>
          <view class="stat-trend" v-if="statsData.rewardAmountTrend">
            <text class="trend-text" :class="statsData.rewardAmountTrend > 0 ? 'up' : 'down'">
              {{ statsData.rewardAmountTrend > 0 ? '↑' : '↓' }} {{ Math.abs(statsData.rewardAmountTrend) }}%
            </text>
          </view>
          <view class="stat-icon">💰</view>
        </view>
      </view>
    </view>
    
    <!-- 平台筛选 -->
    <view class="platform-filter">
      <scroll-view scroll-x="true" class="platform-scroll">
        <view 
          class="platform-item"
          :class="{ active: selectedPlatform === 'all' }"
          @click="selectPlatform('all')"
        >
          <view class="platform-icon-placeholder"></view>
          <text class="platform-name">全部</text>
        </view>
        <view 
          v-for="platform in platforms" 
          :key="platform.id"
          class="platform-item"
          :class="{ active: selectedPlatform === platform.id }"
          @click="selectPlatform(platform.id)"
        >
          <image class="platform-icon" :src="platform.imageUrl || getPlatformIcon(platform.id)" />
          <text class="platform-name">{{ platform.name }}</text>
        </view>
      </scroll-view>
    </view>
    
    <!-- 发布数据 -->
    <view class="publish-section">
      <view class="section-header">
        <text class="section-title">📹 发布数据</text>
        <text class="view-more" @click="viewMoreVideos">视频数量: {{ publishVideos.length }}</text>
      </view>
      
      <!-- 加载状态 -->
      <view class="loading-container" v-if="loading">
        <text class="loading-text">加载中...</text>
      </view>
      
      <!-- 视频列表 -->
      <view class="video-list" v-else>
        <view 
          v-for="video in publishVideos" 
          :key="video.id"
          class="video-item"
          @click="playVideo(video.videoId)"
        >
          <view class="video-thumbnail">
            <image class="thumbnail" :src="video.cover || defaultCover" mode="aspectFill" />
            <!-- 移除视频时长显示 -->
          </view>
          <view class="video-info">
            <view class="video-title">{{ video.title || '掉进樱花的世界' }}</view>
            <view class="video-stats">
              <view class="stat-item">
                <text class="stat-icon">👁</text>
                <text class="stat-value">{{ formatNumber(video.viewCount) || 0 }}</text>
              </view>
              <view class="stat-item">
                <text class="stat-icon">👍</text>
                <text class="stat-value">{{ formatNumber(video.likeCount) || 0 }}</text>
              </view>
              <view class="stat-item">
                <text class="stat-icon">💬</text>
                <text class="stat-value">{{ formatNumber(video.commentCount) || 0 }}</text>
              </view>
            </view>
            <view class="video-platform1">
              <text class="platform-label1">发布平台:</text>
              <text class="platform-name1">{{ getPlatformName(video.platformId) }}</text>
            </view>
            <!-- 数据状态 -->
            <view class="data-status" v-if="video.dataStatus === 0" style="color: #ff4d4f">
              回流数据异常
            </view>
          </view>
          <view class="video-status success" v-if="video.status === 1">
            发布成功
          </view>
		  <view class="video-status failed" v-if="video.status === 0">
		    发布中...
		  </view>
		  <view class="video-status failed" v-if="video.status === -1">
		    发布失败
		  </view>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view class="empty-state" v-if="!loading && publishVideos.length === 0">
        <text class="empty-icon">📊</text>
        <text class="empty-text">暂无发布数据</text>
        <text class="empty-subtext">快去发布一些视频吧</text>
      </view>
      
      <!-- 加载更多 -->
      <view class="load-more" v-if="hasMore && !loading">
        <text class="load-more-btn" @click="loadMoreVideos">加载更多</text>
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
      hasMore: true,
      page: 1,
      pageSize: 10,
      
      platforms: [],
      selectedPlatform: 'all',
      
      // 统计数据对象
      statsData: {
        totalViews: 0,
        totalLikes: 0,
        totalComments: 0,
        totalShares: 0,
        totalCollects: 0,
        totalReward: 0,
        playCountTrend: 0,
        likeCountTrend: 0,
        commentCountTrend: 0,
        shareCountTrend: 0,
        collectCountTrend: 0,
        rewardAmountTrend: 0
      },
      
      publishVideos: []
    }
  },
  
  onLoad() {
    this.loadData();
  },
  
  onPullDownRefresh() {
    this.refreshData().then(() => {
      uni.stopPullDownRefresh();
    });
  },
  
  onReachBottom() {
    if (this.hasMore && !this.loading) {
      this.loadMoreVideos();
    }
  },
  
  methods: {
    async loadData() {
      this.loading = true;
      try {
        // 并行加载数据
        await Promise.all([
          this.loadPlatforms(),
          this.loadStats(),
          this.loadPublishData()
        ]);
      } catch (error) {
        console.error('加载数据失败:', error);
        uni.showToast({
          title: '加载失败，请重试',
          icon: 'none'
        });
      } finally {
        this.loading = false;
      }
    },
    
    async loadPlatforms() {
      try {
        console.log('开始加载平台列表...');
        const res = await api.platform.list();
        console.log('平台列表接口返回:', res);
        
        if (res.code === 0 && res.data) {
          this.platforms = res.data;
          console.log('设置平台列表:', this.platforms);
        } else if (res.code === 10000 && res.data) {
          // 兼容其他成功状态码
          this.platforms = res.data;
          console.log('设置平台列表(10000):', this.platforms);
        } else {
          console.log('平台列表接口返回异常:', res);
          // 使用默认平台数据
          this.platforms = [
            { id: 1, name: '抖音', imageUrl: 'https://via.placeholder.com/60x60/FF0050/FFFFFF?text=抖音' },
            { id: 2, name: '快手', imageUrl: 'https://via.placeholder.com/60x60/FF6600/FFFFFF?text=快手' },
            { id: 3, name: '小红书', imageUrl: 'https://via.placeholder.com/60x60/FF2442/FFFFFF?text=小红书' }
          ];
        }
      } catch (error) {
        console.error('加载平台失败:', error);
        uni.showToast({
          title: '加载平台列表失败',
          icon: 'none'
        });
        // 使用默认平台数据
        this.platforms = [
          { id: 1, name: '抖音', imageUrl: 'https://via.placeholder.com/60x60/FF0050/FFFFFF?text=抖音' },
          { id: 2, name: '快手', imageUrl: 'https://via.placeholder.com/60x60/FF6600/FFFFFF?text=快手' },
          { id: 3, name: '小红书', imageUrl: 'https://via.placeholder.com/60x60/FF2442/FFFFFF?text=小红书' }
        ];
      }
    },
    
    async loadStats() {
      try {
        const params = {
          platformId: this.selectedPlatform === 'all' ? null : this.selectedPlatform
        };
        
        // 调用新的分享统计数据API
        const res = await api.video.shareStats(params);
        if (res.code === 10000 && res.data) {
          const data = res.data;
          console.log(data)
          // 直接更新统计数据对象
          this.statsData = {
            totalViews: data.totalViews || 0,
            totalLikes: data.totalLikes || 0,
            totalComments: data.totalComments || 0,
            totalShares: data.totalShares || 0,
            totalCollects: data.totalCollects || 0,
            totalReward: data.totalReward || 0,
            playCountTrend: data.playCount?.trend || 0,
            likeCountTrend: data.likeCount?.trend || 0,
            commentCountTrend: data.commentCount?.trend || 0,
            shareCountTrend: data.shareCount?.trend || 0,
            collectCountTrend: data.collectCount?.trend || 0,
            rewardAmountTrend: data.rewardAmount?.trend || 0
          };
        }
      } catch (error) {
        console.error('加载统计数据失败:', error);
        uni.showToast({
          title: '加载统计数据失败',
          icon: 'none'
        });
      }
    },
    
    async loadPublishData() {
      try {
        const params = {
          page: this.page,
          size: this.pageSize,
          platformId: this.selectedPlatform === 'all' ? null : this.selectedPlatform
        };
        
        const res = await api.video.publishList(params);
        console.log('发布列表接口返回:', res);
        
        if ((res.code === 0 || res.code === 10000) && res.data) {
          const videos = res.data.records || res.data || [];
          console.log('解析到的视频数据:', videos);
          
          if (this.page === 1) {
            this.publishVideos = videos;
          } else {
            this.publishVideos = [...this.publishVideos, ...videos];
          }
          
          this.hasMore = videos.length >= this.pageSize;
          console.log('设置视频列表:', this.publishVideos);
        } else {
          console.log('发布列表接口返回异常:', res);
        }
      } catch (error) {
        console.error('加载发布数据失败:', error);
        uni.showToast({
          title: '加载发布数据失败',
          icon: 'none',
          duration: 2000
        });
      }
    },
    
    async refreshData() {
      this.page = 1;
      this.hasMore = true;
      await this.loadData();
    },
    
    async loadMoreVideos() {
      if (this.loading || !this.hasMore) return;
      
      this.page++;
      await this.loadPublishData();
    },
    
    selectPlatform(platformId) {
      this.selectedPlatform = platformId;
      this.refreshData();
    },
    
    viewMoreVideos() {
      uni.navigateTo({
        url: '/pages/video-list/video-list'
      });
    },
    
    playVideo(videoId) {
      // 根据videoId播放视频
      uni.navigateTo({
        url: `/pages/video-player/video-player?videoId=${videoId}`
      });
    },
    
    // 工具方法
    formatReward(amount) {
      if (!amount) return '0 元';
      return `${amount.toLocaleString()} 元`;
    },
    
    formatNumber(num) {
      if (!num) return '0';
      const n = parseInt(num);
      if (n >= 10000) {
        return (n / 10000).toFixed(1) + 'w';
      }
      return n.toLocaleString();
    },
    
    formatTime(timestamp) {
      if (!timestamp) return '';
      const date = new Date(timestamp);
      const now = new Date();
      const diff = now - date;
      
      if (diff < 60000) return '刚刚';
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
      if (diff < 2592000000) return `${Math.floor(diff / 86400000)}天前`;
      
      return date.toLocaleDateString();
    },
    
    getPlatformIcon(platformId) {
      const icons = {
        1: 'https://via.placeholder.com/60x60/FF0050/FFFFFF?text=抖音',
        2: 'https://via.placeholder.com/60x60/FF6600/FFFFFF?text=快手',
        3: 'https://via.placeholder.com/60x60/FF2442/FFFFFF?text=小红书'
      };
      return icons[platformId] || 'https://via.placeholder.com/60x60/999999/FFFFFF?text=平台';
    },

    getPlatformName(platformId) {
      const platform = this.platforms.find(p => p.id === platformId);
      return platform ? platform.name : '未知平台';
    }
  }
}
</script>

<style scoped>
.data-container {
  padding: 20rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

/* 平台筛选样式 */
.platform-filter {
  background: white;
  border-radius: 15rpx;
  padding: 20rpx 30rpx;
  margin-bottom: 20rpx;
}

.platform-scroll {
  white-space: nowrap;
}

.platform-item {
  display: inline-flex;
  flex-direction: row;
  align-items: center;
  padding: 15rpx 20rpx;
  margin-right: 20rpx;
  border-radius: 12rpx;
  transition: all 0.3s;
  min-width: 120rpx;
  cursor: pointer;
  background: #f8f9fa;
  justify-content: center; /* 新增居中属性 */
}

.platform-item.active {
  background: #667eea;
  color: white;
}

.platform-icon {
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
  margin-right: 10rpx;
  flex-shrink: 0;
}

.platform-icon-placeholder {
  width: 0rpx;
  height: 32rpx;
  margin-right: 10rpx;
  flex-shrink: 0;
}

.platform-name {
  font-size: 24rpx;
  font-weight: 500;
  white-space: nowrap;
}

.stats-section {
  background: white;
  border-radius: 15rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.refresh-btn {
  font-size: 32rpx;
  color: #667eea;
  cursor: pointer;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
}

.stat-card {
  background: #f8f9fa;
  border-radius: 15rpx;
  padding: 30rpx;
  position: relative;
}

.stat-value {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}

.stat-label {
  font-size: 24rpx;
  color: #666;
  margin-bottom: 15rpx;
}

.stat-trend {
  margin-bottom: 10rpx;
}

.trend-text {
  font-size: 20rpx;
  padding: 5rpx 10rpx;
  border-radius: 10rpx;
}

.trend-text.up {
  background: #e8f5e8;
  color: #52c41a;
}

.trend-text.down {
  background: #fff2f0;
  color: #ff4d4f;
}

.stat-icon {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  font-size: 40rpx;
  opacity: 0.3;
}

.publish-section {
  background: white;
  border-radius: 15rpx;
  padding: 30rpx;
}

.view-more {
  font-size: 26rpx;
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

.video-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.video-item {
  display: flex;
  align-items: flex-start;
  padding: 20rpx;
  background: #f8f9fa;
  border-radius: 15rpx;
  transition: all 0.3s;
  position: relative;
}

.video-item:active {
  transform: scale(0.98);
}

.video-thumbnail {
  position: relative;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.thumbnail {
  width: 120rpx;
  height: 120rpx;
  border-radius: 10rpx;
  object-fit: cover;
}

.video-duration-overlay {
  position: absolute;
  bottom: 8rpx;
  left: 8rpx;
  font-size: 20rpx;
  color: white;
  background: rgba(0, 0, 0, 0.7);
  padding: 4rpx 8rpx;
  border-radius: 6rpx;
}

.data-status {
  margin-top: 10rpx;
  font-size: 22rpx;
  font-weight: 500;
  color: #666;
  display: flex;
  align-items: center;
}

.data-status[style*="#ff4d4f"] {
  color: #ff4d4f;
}

.video-info {
  flex: 1;
  min-width: 0;
}

.video-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-platform1 {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #666;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.platform-label1 {
  font-weight: 500;
  color: #333;
}

.platform-name1 {
  font-weight: 500;
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  font-size: 22rpx;
}

.video-stats {
  display: flex;
  gap: 30rpx;
  margin-bottom: 10rpx;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5rpx;
}

.stat-item .stat-icon {
  font-size: 20rpx;
  color: #999;
  position: static;
  opacity: 1;
}

.stat-item .stat-value {
  font-size: 20rpx;
  color: #666;
  position: static;
  font-weight: normal;
}

.video-status {
  position: absolute;
  top: 50%;
  right: 20rpx;
  transform: translateY(-50%);
  padding: 6rpx 12rpx;
  border-radius: 12rpx;
  font-size: 20rpx;
  font-weight: 500;
}

.video-status.success {
  background: #e8f5e8;
  color: #52c41a;
}

.video-status.failed {
  background: #fff2f0;
  color: #ff4d4f;
}

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

.load-more {
  padding: 40rpx;
  text-align: center;
}

.load-more-btn {
  display: inline-block;
  padding: 20rpx 40rpx;
  background: #667eea;
  color: white;
  border-radius: 25rpx;
  font-size: 28rpx;
  cursor: pointer;
}
</style>
