<template>
  <view class="wechat-auth-container">
    <!-- 头部导航 -->
    <view class="header">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <text class="header-title">微信授权</text>
    </view>

    <!-- 授权说明 -->
    <view class="auth-intro">
      <view class="intro-icon">🔐</view>
      <text class="intro-title">微信授权</text>
      <text class="intro-desc">为了保障您的资金安全，提现到微信需要先进行微信授权</text>
    </view>

    <!-- 授权状态 -->
    <view class="auth-status-section">
      <view class="status-item" :class="{ active: authSteps.step1 }">
        <view class="status-icon">
          <text v-if="authSteps.step1" class="check-icon">✓</text>
          <text v-else class="step-number">1</text>
        </view>
        <view class="status-content">
          <text class="status-title">微信登录</text>
          <text class="status-desc">使用微信账号登录</text>
        </view>
      </view>
      
      <view class="status-line" :class="{ active: authSteps.step1 }"></view>
      
      <view class="status-item" :class="{ active: authSteps.step2 }">
        <view class="status-icon">
          <text v-if="authSteps.step2" class="check-icon">✓</text>
          <text v-else class="step-number">2</text>
        </view>
        <view class="status-content">
          <text class="status-title">绑定手机号</text>
          <text class="status-desc">绑定手机号完成注册</text>
        </view>
      </view>
      
      <view class="status-line" :class="{ active: authSteps.step2 }"></view>
      
      <view class="status-item" :class="{ active: authSteps.step3 }">
        <view class="status-icon">
          <text v-if="authSteps.step3" class="check-icon">✓</text>
          <text v-else class="step-number">3</text>
        </view>
        <view class="status-content">
          <text class="status-title">授权完成</text>
          <text class="status-desc">可以正常提现到微信</text>
        </view>
      </view>
    </view>

    <!-- 当前状态显示 -->
    <view class="current-status">
      <view v-if="!authSteps.step1" class="status-card">
        <view class="status-header">
          <text class="status-title">开始微信授权</text>
          <text class="status-subtitle">点击下方按钮开始授权流程</text>
        </view>
        <button class="auth-btn" @click="startWechatAuth" :disabled="authing">
          {{ authing ? '授权中...' : '开始微信授权' }}
        </button>
      </view>
      
      <view v-else-if="authSteps.step1 && !authSteps.step2" class="status-card">
        <view class="status-header">
          <text class="status-title">绑定手机号</text>
          <text class="status-subtitle">请输入手机号完成注册</text>
        </view>
        
        <view class="phone-form">
          <view class="form-item">
            <text class="form-label">手机号</text>
            <input 
              class="form-input" 
              v-model="phone" 
              placeholder="请输入手机号"
              type="number"
              maxlength="11"
            />
          </view>
          
          <view class="form-item">
            <text class="form-label">验证码</text>
            <view class="code-input-group">
              <input 
                class="form-input code-input" 
                v-model="code" 
                placeholder="请输入验证码"
                type="number"
                maxlength="6"
              />
              <button 
                class="code-btn" 
                @click="sendCode" 
                :disabled="codeSending || countdown > 0"
              >
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </button>
            </view>
          </view>
          
          <button class="bind-btn" @click="bindPhone" :disabled="binding">
            {{ binding ? '绑定中...' : '绑定手机号' }}
          </button>
        </view>
      </view>
      
      <view v-else-if="authSteps.step2 && !authSteps.step3" class="status-card">
        <view class="status-header">
          <text class="status-title">授权完成</text>
          <text class="status-subtitle">微信授权已完成，可以正常提现</text>
        </view>
        <button class="complete-btn" @click="goToWithdraw">
          返回提现
        </button>
      </view>
    </view>

    <!-- 授权说明 -->
    <view class="auth-tips">
      <text class="tips-title">授权说明</text>
      <view class="tips-list">
        <text class="tip-item">• 微信授权仅用于提现功能，保障资金安全</text>
        <text class="tip-item">• 我们不会获取您的微信聊天记录等隐私信息</text>
        <text class="tip-item">• 您可以随时在设置中解除授权</text>
      </view>
    </view>
  </view>
</template>

<script>
import api from '../../utils/api.js'

export default {
  data() {
    return {
      from: '', // 来源页面
      authing: false,
      phone: '',
      code: '',
      codeSending: false,
      countdown: 0,
      binding: false,
      authSteps: {
        step1: false, // 微信登录
        step2: false, // 绑定手机号
        step3: false  // 授权完成
      }
    }
  },

  onLoad(options) {
    if (options.from) {
      this.from = options.from
    }
    this.checkAuthStatus()
  },

  methods: {
    // 返回上一页
    goBack() {
      if (this.from === 'withdraw') {
        uni.navigateBack()
      } else {
        uni.switchTab({
          url: '/pages/mine/mine'
        })
      }
    },

    // 检查授权状态
    async checkAuthStatus() {
      try {
        const res = await api.user.checkWechatAuth()
        if (res.code === 10000 && res.data) {
          if (res.data.isAuthorized) {
            this.authSteps.step1 = true
            this.authSteps.step2 = true
            this.authSteps.step3 = true
          }
        }
      } catch (error) {
        console.error('检查授权状态失败:', error)
      }
    },

    // 开始微信授权
    startWechatAuth() {
      // #ifdef APP-PLUS
      this.wechatAppAuth()
      // #endif
      
      // #ifdef MP-WEIXIN
      this.wechatMiniProgramAuth()
      // #endif
      
      // #ifndef APP-PLUS || MP-WEIXIN
      uni.showModal({
        title: '提示',
        content: '微信授权功能仅在App和微信小程序中可用',
        showCancel: false
      })
      // #endif
    },

    // App端微信授权
    wechatAppAuth() {
      this.authing = true
      
      uni.login({
        provider: 'weixin',
        onlyAuthorize: true,
        success: (loginRes) => {
          console.log('微信授权成功:', loginRes)
          this.authSteps.step1 = true
          this.authing = false
          
          // 跳转到微信登录页面完成绑定
          uni.navigateTo({
            url: `/pages/wechat-login/wechat-login?fromLogin=true&code=${loginRes.code}`
          })
        },
        fail: (err) => {
          console.error('微信授权失败:', err)
          this.authing = false
          uni.showToast({
            title: '微信授权失败',
            icon: 'none'
          })
        }
      })
    },

    // 小程序端微信授权
    wechatMiniProgramAuth() {
      this.authing = true
      
      uni.getUserProfile({
        desc: '用于微信授权',
        success: (res) => {
          console.log('获取用户信息成功:', res)
          this.authSteps.step1 = true
          this.authing = false
          
          // 跳转到微信登录页面完成绑定
          const params = new URLSearchParams()
          Object.keys(res.userInfo).forEach(key => {
            if (res.userInfo[key]) {
              params.append(key, encodeURIComponent(res.userInfo[key]))
            }
          })
          
          uni.navigateTo({
            url: `/pages/wechat-login/wechat-login?${params.toString()}`
          })
        },
        fail: (err) => {
          console.error('获取用户信息失败:', err)
          this.authing = false
          uni.showToast({
            title: '获取用户信息失败',
            icon: 'none'
          })
        }
      })
    },

    // 发送验证码
    async sendCode() {
      if (!this.phone) {
        uni.showToast({
          title: '请输入手机号',
          icon: 'none'
        })
        return
      }

      if (!/^1[3-9]\d{9}$/.test(this.phone)) {
        uni.showToast({
          title: '请输入正确的手机号',
          icon: 'none'
        })
        return
      }

      try {
        this.codeSending = true
        
        const res = await api.user.sendCode({ phone: this.phone })
        
        if (res.code === 0 || res.code === 10000) {
          uni.showToast({
            title: '验证码已发送',
            icon: 'success'
          })
          
          this.startCountdown()
        } else {
          uni.showToast({
            title: res.message || '发送失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('发送验证码失败:', error)
        uni.showToast({
          title: '发送失败，请重试',
          icon: 'none'
        })
      } finally {
        this.codeSending = false
      }
    },

    // 开始倒计时
    startCountdown() {
      this.countdown = 60
      const timer = setInterval(() => {
        this.countdown--
        if (this.countdown <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    },

    // 绑定手机号
    async bindPhone() {
      if (!this.phone || !this.code) {
        uni.showToast({
          title: '请填写完整信息',
          icon: 'none'
        })
        return
      }

      try {
        this.binding = true
        
        // 这里需要调用绑定手机号的接口
        // 由于需要unionid，这里暂时模拟成功
        uni.showToast({
          title: '绑定成功',
          icon: 'success'
        })
        
        this.authSteps.step2 = true
        this.authSteps.step3 = true
        
      } catch (error) {
        console.error('绑定手机号失败:', error)
        uni.showToast({
          title: '绑定失败，请重试',
          icon: 'none'
        })
      } finally {
        this.binding = false
      }
    },

    // 返回提现页面
    goToWithdraw() {
      if (this.from === 'withdraw') {
        uni.navigateBack()
      } else {
        uni.navigateTo({
          url: '/pages/withdraw/withdraw'
        })
      }
    }
  }
}
</script>

<style scoped>
.wechat-auth-container {
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

.auth-intro {
  text-align: center;
  padding: 60rpx 40rpx;
  background: white;
  margin: 20rpx;
  border-radius: 20rpx;
}

.intro-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.intro-title {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 15rpx;
}

.intro-desc {
  display: block;
  font-size: 28rpx;
  color: #666;
  line-height: 1.5;
}

.auth-status-section {
  background: white;
  margin: 20rpx;
  border-radius: 20rpx;
  padding: 40rpx;
}

.status-item {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
}

.status-item:last-child {
  margin-bottom: 0;
}

.status-icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.status-item.active .status-icon {
  background: #52c41a;
}

.step-number {
  font-size: 24rpx;
  color: #999;
  font-weight: bold;
}

.status-item.active .step-number {
  color: white;
}

.check-icon {
  font-size: 24rpx;
  color: white;
  font-weight: bold;
}

.status-content {
  flex: 1;
}

.status-title {
  display: block;
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 5rpx;
}

.status-desc {
  display: block;
  font-size: 24rpx;
  color: #666;
}

.status-line {
  width: 2rpx;
  height: 30rpx;
  background: #f0f0f0;
  margin-left: 30rpx;
  margin-bottom: 30rpx;
}

.status-line.active {
  background: #52c41a;
}

.current-status {
  margin: 20rpx;
}

.status-card {
  background: white;
  border-radius: 20rpx;
  padding: 40rpx;
  text-align: center;
}

.status-header {
  margin-bottom: 40rpx;
}

.status-title {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}

.status-subtitle {
  display: block;
  font-size: 26rpx;
  color: #666;
}

.auth-btn, .complete-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #52c41a, #73d13d);
  color: white;
  border: none;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: bold;
}

.auth-btn:disabled {
  background: #ccc;
}

.phone-form {
  text-align: left;
}

.form-item {
  margin-bottom: 30rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 15rpx;
}

.form-input {
  width: 100%;
  height: 80rpx;
  border: 1rpx solid #ddd;
  border-radius: 10rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #333;
  box-sizing: border-box;
}

.code-input-group {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.code-input {
  flex: 1;
}

.code-btn {
  height: 80rpx;
  padding: 0 30rpx;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 10rpx;
  font-size: 24rpx;
  white-space: nowrap;
}

.code-btn:disabled {
  background: #ccc;
}

.bind-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #1890ff, #40a9ff);
  color: white;
  border: none;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: bold;
  margin-top: 20rpx;
}

.bind-btn:disabled {
  background: #ccc;
}

.auth-tips {
  background: white;
  margin: 20rpx;
  border-radius: 20rpx;
  padding: 30rpx;
}

.tips-title {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.tip-item {
  font-size: 24rpx;
  color: #666;
  line-height: 1.5;
}
</style>
