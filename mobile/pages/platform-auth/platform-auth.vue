<template>
  <view class="platform-auth-container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <text class="nav-title">平台授权</text>
      <view class="debug-btn" @click="toggleDebugInfo">
        <text class="debug-icon">🔧</text>
      </view>
    </view>

    <!-- 平台列表 -->
    <view class="platform-list">
      <view 
        v-for="platform in platforms" 
        :key="platform.id"
        class="platform-item"
        @click="handlePlatformAuth(platform)"
      >
        <view class="platform-info">
          <image class="platform-icon" :src="platform.icon" />
          <view class="platform-details">
            <text class="platform-name">{{ platform.name }}</text>
            <text class="platform-desc">{{ platform.description || `发布视频到${platform.name}平台` }}</text>
          </view>
        </view>
        
        <view class="auth-status">
          <text 
            class="status-text" 
            :class="{ authorized: platform.isAuth }"
          >
            {{ platform.isAuth ? '已授权' : '未授权' }}
          </text>
          <text class="auth-btn" :class="{ authorized: platform.isAuth }">
            {{ platform.isAuth ? '重新授权' : '立即授权' }}
          </text>
        </view>
      </view>
    </view>

    <!-- 授权说明 -->
    <view class="auth-tips">
      <text class="tips-title">授权说明</text>
      <text class="tips-content">
        1. 授权后可以发布视频到对应平台\n
        2. 授权信息会安全保存在本地\n
        3. 可以随时取消授权\n
        4. 授权有效期为30天
      </text>
    </view>

    <!-- 调试信息区域 -->
    <view class="debug-info" v-if="showDebugInfo">
      <text class="debug-title">SDK调试信息</text>
      <text class="debug-content">
        SDK加载状态: {{ sdkStatus.sdkLoaded ? '成功' : '失败' }}\n
        SDK类型: {{ sdkStatus.sdkType }}\n
        可用方法: {{ sdkStatus.availableMethods.join(', ') }}\n
        授权方法: {{ sdkStatus.hasAuthMethod ? '存在' : '不存在' }}\n
        配置方法: {{ sdkStatus.hasConfigMethod ? '存在' : '不存在' }}
      </text>
    </view>
  </view>
</template>

<script>
import api from '../../utils/api.js'
import platformSDK from '../../utils/platform-sdk.js'

export default {
  data() {
    return {
      platforms: [],
      sdkStatus: { // 新增SDK状态数据
        sdkLoaded: false,
        sdkType: '未知',
        availableMethods: [],
        hasAuthMethod: false,
        hasConfigMethod: false
      },
      showDebugInfo: false // 新增调试信息显示控制
    }
  },

  onLoad() {
    // 初始化Android端快手SDK配置
    const initResult = platformSDK.initAndroidKuaishouConfig()
    if (!initResult) {
      console.warn('快手SDK初始化失败，可能影响授权功能')
    }
    
    // 输出SDK状态信息用于调试
    this.sdkStatus = platformSDK.getSDKStatus()
    console.log('SDK状态信息:', this.sdkStatus)
    
    this.loadPlatforms()
  },

  methods: {
    // 加载平台列表
    async loadPlatforms() {
      try {
        const res = await api.get('/platform/list')
        if (res.data && Array.isArray(res.data)) {
          // 转换平台数据格式，添加默认描述
          this.platforms = res.data.map(platform => ({
            id: platform.id,
            name: platform.name,
            icon: platform.imageUrl || this.getPlatformIcon(platform.id),
            description: platform.description || `发布视频到${platform.name}平台`,
            isAuth: platform.isAuth || false
          }))
        } else {
          // 如果接口失败，使用默认数据
          this.loadDefaultPlatforms()
        }
      } catch (error) {
        console.error('加载平台列表失败:', error)
        // 加载失败时使用默认数据
        this.loadDefaultPlatforms()
      }
    },

    // 加载默认平台数据
    loadDefaultPlatforms() {
      this.platforms = [
        {
          id: 2,
          name: '快手',
          icon: 'https://via.placeholder.com/80x80/FF6600/FFFFFF?text=快手',
          description: '发布视频到快手平台',
          isAuth: false
        },
        {
          id: 1,
          name: '抖音',
          icon: 'https://via.placeholder.com/80x80/FF0050/FFFFFF?text=抖音',
          description: '发布视频到抖音平台',
          isAuth: false
        }
      ]
    },

    // 获取平台图标
    getPlatformIcon(platformId) {
      const iconMap = {
        1: 'https://via.placeholder.com/80x80/FF0050/FFFFFF?text=抖音', // 抖音
        2: 'https://via.placeholder.com/80x80/FF6600/FFFFFF?text=快手', // 快手
        3: 'https://via.placeholder.com/80x80/FF2442/FFFFFF?text=小红书'  // 小红书
      }
      return iconMap[platformId] || 'https://via.placeholder.com/80x80/999999/FFFFFF?text=平台'
    },

    // 处理平台授权
    async handlePlatformAuth(platform) {
      try {
        uni.showLoading({ title: '授权中...' })

        let authResult
        if (platform.id === 2) {
          // 快手授权 - 检查SDK是否可用
          if (!platformSDK.isSDKAvailable()) {
            const sdkStatus = platformSDK.getSDKStatus()
            console.error('SDK状态:', sdkStatus)
            throw new Error('快手SDK插件未正确加载，请检查插件配置或重新安装应用')
          }
          
          // 先调用SDK获取授权码
          console.log('开始调用快手授权...')
          const sdkAuthResult = await platformSDK.kuaishouAuth()
          console.log('快手授权结果:', sdkAuthResult)
          
          if (sdkAuthResult.success) {
            // 调用后端接口保存授权信息
            authResult = await this.saveKuaishouAuthToBackend(sdkAuthResult.data)
          } else {
            throw new Error(sdkAuthResult.error || '快手授权失败')
          }
        } else if (platform.id === 1) {
          // 抖音授权：先SDK获取code，再回传后端换取access_token
          console.log('开始调用抖音授权...')
          const sdkAuthResult = await platformSDK.douyinAuth()
          console.log('抖音授权结果:', sdkAuthResult)
          if (sdkAuthResult && sdkAuthResult.success) {
            authResult = await this.saveDouyinAuthToBackend(sdkAuthResult.data)
          } else {
            throw new Error((sdkAuthResult && sdkAuthResult.error) || '抖音授权失败')
          }
        } else {
          throw new Error('不支持的平台')
        }

        if (authResult.success) {
          // 保存授权信息到本地
          platformSDK.saveAuthInfo(platform.id, authResult.data)
          
          // 更新本地状态
          platform.isAuth = true
          
          uni.hideLoading()
          uni.showToast({
            title: `${platform.name}授权成功`,
            icon: 'success'
          })

          // 重新加载平台列表以获取最新状态
          setTimeout(() => {
            this.loadPlatforms()
          }, 1000)
        }
      } catch (error) {
        uni.hideLoading()
        console.error('授权失败:', error)
        
        // 显示更详细的错误信息
        let errorMessage = '授权失败'
        if (error.error) {
          errorMessage = error.error
        } else if (error.message) {
          errorMessage = error.message
        } else if (typeof error === 'string') {
          errorMessage = error
        }
        
        uni.showToast({
          title: errorMessage,
          icon: 'none',
          duration: 3000
        })
      }
    },

    // 保存快手授权信息到后端
    async saveKuaishouAuthToBackend(authInfo) {
      try {
        const res = await api.post('/platform/kuaishou/auth', {
          authCode: authInfo.code, // 使用code而不是authCode
          state: authInfo.state
        })
        if (res.code === 10000) {
          return { success: true, data: res.data }
        } else {
          throw new Error(res.message || '保存授权信息失败')
        }
      } catch (error) {
        console.error('保存快手授权信息失败:', error)
        throw error
      }
    },

    // 保存抖音授权信息到后端（换取access_token）
    async saveDouyinAuthToBackend(authInfo) {
      try {
        const res = await api.post('/platform/douyin/auth', {
          authCode: authInfo.authCode,
          state: authInfo.state
        })
        if (res.code === 10000) {
          return { success: true, data: res.data }
        } else {
          throw new Error(res.message || '保存抖音授权信息失败')
        }
      } catch (error) {
        console.error('保存抖音授权信息失败:', error)
        throw error
      }
    },

    goBack() {
      uni.navigateBack()
    },

    // 切换调试信息显示
    toggleDebugInfo() {
      this.showDebugInfo = !this.showDebugInfo
    }
  }
}
</script>

<style scoped>
.platform-auth-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 40rpx;
}

.nav-header {
  position: relative;
  height: 88rpx;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1rpx solid #f0f0f0;
}

.back-btn {
  position: absolute;
  left: 30rpx;
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

.nav-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.debug-btn {
  position: absolute;
  right: 30rpx;
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.debug-icon {
  font-size: 36rpx;
  color: #333;
}

.platform-list {
  margin: 20rpx;
}

.platform-item {
  background: white;
  border-radius: 20rpx;
  padding: 40rpx;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.platform-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.platform-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 30rpx;
}

.platform-details {
  flex: 1;
}

.platform-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 10rpx;
}

.platform-desc {
  font-size: 24rpx;
  color: #666;
}

.auth-status {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.status-text {
  font-size: 24rpx;
  color: #ff4757;
  margin-bottom: 10rpx;
}

.status-text.authorized {
  color: #2ed573;
}

.auth-btn {
  font-size: 24rpx;
  color: #667eea;
  padding: 10rpx 20rpx;
  border: 1rpx solid #667eea;
  border-radius: 20rpx;
}

.auth-btn.authorized {
  color: #2ed573;
  border-color: #2ed573;
}

.auth-tips {
  margin: 40rpx 20rpx;
  background: white;
  border-radius: 20rpx;
  padding: 40rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.tips-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 20rpx;
}

.tips-content {
  font-size: 24rpx;
  color: #666;
  line-height: 1.6;
}

/* 新增调试信息样式 */
.debug-info {
  margin: 20rpx;
  background: white;
  border-radius: 20rpx;
  padding: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.debug-title {
  font-size: 24rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 10rpx;
}

.debug-content {
  font-size: 20rpx;
  color: #666;
  line-height: 1.4;
}
</style>
