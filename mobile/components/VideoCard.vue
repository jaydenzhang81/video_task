<template>
  <view class="video-card" @click="handleClick">
    <view class="video-cover">
      <image :src="video.cover" mode="aspectFill" class="cover-image" />
      <view class="play-icon">
        <text class="iconfont icon-play"></text>
      </view>
    </view>
    
    <view class="video-info">
      <view class="video-title">{{ video.title }}</view>
      <view class="video-desc">{{ video.videoDesc }}</view>
      
      <view class="video-stats">
        <view class="stat-item" v-if="video.likeCount !== undefined">
          <text class="stat-label">点赞</text>
          <text class="stat-value">{{ video.likeCount }}</text>
        </view>
        <view class="stat-item" v-if="video.commentCount !== undefined">
          <text class="stat-label">评论</text>
          <text class="stat-value">{{ video.commentCount }}</text>
        </view>
        <view class="stat-item" v-if="video.viewCount !== undefined">
          <text class="stat-label">播放</text>
          <text class="stat-value">{{ video.viewCount }}</text>
        </view>
        <view class="stat-item reward-item" v-if="video.reward !== undefined && video.reward > 0">
          <text class="stat-label">奖励</text>
          <text class="stat-value reward-value">¥{{ video.reward }}</text>
        </view>
      </view>
      
      <view class="video-actions">
        <button 
          v-if="!video.isShared" 
          class="btn-publish" 
          @click.stop="handlePublish"
        >
          发布
        </button>
        <view v-else class="published-tag">
          <text class="tag-text">已发布</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'VideoCard',
  props: {
    video: {
      type: Object,
      required: true
    }
  },
  methods: {
    handleClick() {
      this.$emit('click', this.video)
    },
    
    handlePublish() {
      this.$emit('publish', this.video)
    }
  }
}
</script>

<style scoped>
.video-card {
  background: #fff;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.video-cover {
  position: relative;
  width: 100%;
  height: 300rpx;
}

.cover-image {
  width: 100%;
  height: 100%;
}

.play-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 80rpx;
  height: 80rpx;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.play-icon .iconfont {
  color: #fff;
  font-size: 40rpx;
}

.video-info {
  padding: 20rpx;
}

.video-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
  line-height: 1.4;
}

.video-desc {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 20rpx;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.video-stats {
  display: flex;
  margin-bottom: 20rpx;
}

.stat-item {
  display: flex;
  align-items: center;
  margin-right: 30rpx;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
  margin-right: 8rpx;
}

.stat-value {
  font-size: 24rpx;
  color: #333;
  font-weight: bold;
}

.reward-item {
  margin-left: auto;
}

.reward-value {
  color: #ff6b35 !important;
  font-weight: bold;
}

.video-actions {
  display: flex;
  justify-content: flex-end;
}

.btn-publish {
  background: #007aff;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  padding: 16rpx 32rpx;
  font-size: 28rpx;
}

.published-tag {
  background: #f0f0f0;
  border-radius: 8rpx;
  padding: 16rpx 32rpx;
}

.tag-text {
  font-size: 28rpx;
  color: #666;
}
</style>
