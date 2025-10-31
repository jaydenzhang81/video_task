<template>
  <view class="platform-list">
    <view
        v-for="platform in platforms"
        :key="platform.id"
        class="platform-item"
        :class="{ active: selectedPlatform === platform.id }"
        @click="handlePlatformClick(platform)">
      <image class="platform-icon" :src="platform.icon"/>
      <text class="platform-name">{{ platform.name }}</text>
      <text class="auth-status" :class="{ authorized: platform.isAuth }">
        {{ platform.isAuth ? '已授权' : '未授权' }}
      </text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'PlatformList',
  props: {
    // 平台列表数据
    platforms: {
      type: Array,
      default: () => [],
      required: true
    },
    // 当前选中的平台ID
    selectedPlatform: {
      type: [Number, String],
      default: null
    }
  },
  methods: {
    /**
     * 处理平台点击事件
     * @param {Object} platform - 被点击的平台对象
     */
    handlePlatformClick(platform) {
      // 检查平台是否已授权
      if (!platform.isAuth) {
        // 发送事件通知父组件平台未授权
        this.$emit('platform-unauthorized', platform)
        return
      }
      
      // 发送事件通知父组件平台被点击
      this.$emit('platform-click', platform)
    }
  }
}
</script>

<style scoped>
.platform-list {
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.platform-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx;
  border-radius: 15rpx;
  transition: all 0.3s;
  cursor: pointer;
}

.platform-item.active {
  background: #e3f2fd;
  transform: scale(1.05);
}

.platform-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-bottom: 10rpx;
  display: block;
}

.platform-name {
  font-size: 24rpx;
  color: #333;
  text-align: center;
  margin-bottom: 10rpx;
}

.auth-status {
  font-size: 20rpx;
  color: #999;
}

.auth-status.authorized {
  color: #4CAF50; /* 绿色表示已授权 */
}
</style>