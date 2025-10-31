<template>
  <view class="platform-selector">
    <view class="selector-header">
      <text class="header-title">选择发布平台</text>
    </view>
    
    <view class="platform-list">
      <view 
        v-for="platform in platforms" 
        :key="platform.id"
        class="platform-item"
        :class="{ active: selectedPlatform && selectedPlatform.id === platform.id }"
        @click="selectPlatform(platform)"
      >
        <view class="platform-icon">
          <image :src="platform.imageUrl" mode="aspectFit" class="icon-image" />
        </view>
        <view class="platform-info">
          <text class="platform-name">{{ platform.name }}</text>
          <text class="platform-status" :class="{ authorized: platform.isAuth }">
            {{ platform.isAuth ? '已授权' : '未授权' }}
          </text>
        </view>
        <view class="platform-check">
          <text v-if="selectedPlatform && selectedPlatform.id === platform.id" class="check-icon">✓</text>
        </view>
      </view>
    </view>
    
    <view class="selector-actions">
      <button class="btn-cancel" @click="handleCancel">取消</button>
      <button 
        class="btn-confirm" 
        :disabled="!selectedPlatform"
        @click="handleConfirm"
      >
        确定
      </button>
    </view>
  </view>
</template>

<script>
export default {
  name: 'PlatformSelector',
  props: {
    platforms: {
      type: Array,
      default: () => []
    },
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      selectedPlatform: null
    }
  },
  methods: {
    selectPlatform(platform) {
      this.selectedPlatform = platform
    },
    
    handleCancel() {
      this.selectedPlatform = null
      this.$emit('cancel')
    },
    
    handleConfirm() {
      if (this.selectedPlatform) {
        this.$emit('confirm', this.selectedPlatform)
        this.selectedPlatform = null
      }
    }
  }
}
</script>

<style scoped>
.platform-selector {
  background: #fff;
  border-radius: 20rpx 20rpx 0 0;
  padding: 40rpx 20rpx 20rpx;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  transform: translateY(100%);
  transition: transform 0.3s ease;
}

.platform-selector.show {
  transform: translateY(0);
}

.selector-header {
  text-align: center;
  margin-bottom: 30rpx;
}

.header-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.platform-list {
  max-height: 600rpx;
  overflow-y: auto;
}

.platform-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  border-radius: 12rpx;
  margin-bottom: 10rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s ease;
}

.platform-item.active {
  background: #f0f8ff;
  border-color: #007aff;
}

.platform-icon {
  width: 80rpx;
  height: 80rpx;
  margin-right: 20rpx;
}

.icon-image {
  width: 100%;
  height: 100%;
}

.platform-info {
  flex: 1;
}

.platform-name {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
  display: block;
  margin-bottom: 8rpx;
}

.platform-status {
  font-size: 24rpx;
  color: #999;
}

.platform-status.authorized {
  color: #4cd964;
}

.platform-check {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.check-icon {
  color: #007aff;
  font-size: 32rpx;
  font-weight: bold;
}

.selector-actions {
  display: flex;
  margin-top: 30rpx;
  gap: 20rpx;
}

.btn-cancel {
  flex: 1;
  background: #f0f0f0;
  color: #666;
  border: none;
  border-radius: 8rpx;
  padding: 20rpx;
  font-size: 28rpx;
}

.btn-confirm {
  flex: 1;
  background: #007aff;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  padding: 20rpx;
  font-size: 28rpx;
}

.btn-confirm:disabled {
  background: #ccc;
  color: #999;
}
</style>
