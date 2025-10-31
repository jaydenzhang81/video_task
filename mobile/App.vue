<script>
import platformSDK from './utils/platform-sdk.js'

export default {
  onLaunch: function() {
    console.log('App Launch')
    // 初始化Android端快手SDK配置
    platformSDK.initAndroidKuaishouConfig()
    // 检查登录状态
    const token = uni.getStorageSync('token')
    if (!token) {
      // 延迟执行，避免与页面跳转冲突
      setTimeout(() => {
        const currentPages = getCurrentPages()
        const currentPage = currentPages[currentPages.length - 1]
        // 如果当前不在登录页面，才跳转到登录页
        if (currentPage && currentPage.route !== 'pages/login/login') {
          uni.reLaunch({
            url: '/pages/login/login'
          })
        }
      }, 100)
    }
  },
  onShow: function() {
    console.log('App Show')
  },
  onHide: function() {
    console.log('App Hide')
  }
}
</script>

<style>
/* 全局样式 */
page {
  background-color: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
}

/* 通用样式类 */
.container {
  padding: 20rpx;
}

.card {
  background: #fff;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.btn-primary {
  background: #007aff;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  padding: 20rpx 40rpx;
  font-size: 28rpx;
}

.btn-secondary {
  background: #f0f0f0;
  color: #333;
  border: 1rpx solid #ddd;
  border-radius: 8rpx;
  padding: 20rpx 40rpx;
  font-size: 28rpx;
}

.text-center {
  text-align: center;
}

.text-primary {
  color: #007aff;
}

.text-secondary {
  color: #666;
}

.text-muted {
  color: #999;
}

.flex {
  display: flex;
}

.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.flex-column {
  display: flex;
  flex-direction: column;
}

.mt-10 {
  margin-top: 10rpx;
}

.mt-20 {
  margin-top: 20rpx;
}

.mb-10 {
  margin-bottom: 10rpx;
}

.mb-20 {
  margin-bottom: 20rpx;
}

.p-20 {
  padding: 20rpx;
}
</style>
