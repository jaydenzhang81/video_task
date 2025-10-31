<template>
  <view class="forgot-container">
    <!-- 顶部logo和标题区域 -->
    <view class="header-section">
      <view class="header-content">
        <image class="logo-icon" src="/static/images/logo.png" mode="aspectFit" />
        <text class="page-title">找回密码</text>
      </view>
    </view>
    
    <!-- 输入表单区域 -->
    <view class="form-section">
      <!-- 手机号输入 -->
      <view class="input-container">
        <input 
          class="input-field" 
          type="text" 
          placeholder="请输入手机号" 
          v-model="form.phone"
          maxlength="11"
        />
      </view>
      
      <!-- 验证码输入 -->
      <view class="input-container">
        <view class="code-input-group">
          <input 
            class="input-field code-input" 
            type="text" 
            placeholder="请输入验证码" 
            v-model="form.code"
            maxlength="6"
          />
          <button 
            class="code-button" 
            :class="{ disabled: countdown > 0 }"
            @click="sendCode"
          >
            {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
          </button>
        </view>
      </view>
      
      <!-- 新密码输入 -->
      <view class="input-container">
        <input 
          class="input-field" 
          type="password" 
          placeholder="请设置新密码" 
          v-model="form.password"
        />
        <view class="password-hint">6-12个字符,支持数字、大小写字母和符号至少包含一种</view>
      </view>
      
      <!-- 确认密码输入 -->
      <view class="input-container">
        <input 
          class="input-field" 
          type="password" 
          placeholder="请确认新密码" 
          v-model="form.confirmPassword"
        />
      </view>
      
      <!-- 重置按钮 -->
      <button class="reset-button" @click="handleReset">重置密码</button>
    </view>
    
    <!-- 底部链接 -->
    <view class="footer-section">
      <text class="back-login-link" @click="goToLogin">想起密码了？立即登录</text>
    </view>
  </view>
</template>

<script>
import api from '../../utils/api.js'

export default {
  data() {
    return {
      form: {
        phone: '',
        code: '',
        password: '',
        confirmPassword: ''
      },
      countdown: 0
    }
  },
  methods: {
    async sendCode() {
      if (!this.form.phone) {
        uni.showToast({
          title: '请输入手机号',
          icon: 'none'
        });
        return;
      }
      
      if (!/^1[3-9]\d{9}$/.test(this.form.phone)) {
        uni.showToast({
          title: '请输入正确的手机号',
          icon: 'none'
        });
        return;
      }
      
      try {
        uni.showLoading({ title: '发送中...' });
        
        await api.post('/user/sendResetCode', {
          phone: this.form.phone
        });
        
        uni.hideLoading();
        uni.showToast({
          title: '验证码已发送',
          icon: 'success'
        });
        
        // 开始倒计时
        this.countdown = 60;
        this.startCountdown();
        
      } catch (error) {
        uni.hideLoading();
        uni.showToast({
          title: error.message || '发送失败',
          icon: 'none'
        });
      }
    },
    
    startCountdown() {
      if (this.countdown > 0) {
        setTimeout(() => {
          this.countdown--;
          this.startCountdown();
        }, 1000);
      }
    },
    
    async handleReset() {
      if (!this.form.phone || !this.form.code || !this.form.password || !this.form.confirmPassword) {
        uni.showToast({
          title: '请填写完整信息',
          icon: 'none'
        });
        return;
      }
      
      if (!/^1[3-9]\d{9}$/.test(this.form.phone)) {
        uni.showToast({
          title: '请输入正确的手机号',
          icon: 'none'
        });
        return;
      }
      
      if (this.form.password !== this.form.confirmPassword) {
        uni.showToast({
          title: '两次密码不一致',
          icon: 'none'
        });
        return;
      }
      
      if (this.form.password.length < 6) {
        uni.showToast({
          title: '密码长度不能少于6位',
          icon: 'none'
        });
        return;
      }
      
      try {
        uni.showLoading({ title: '重置中...' });
        
        await api.post('/user/resetPassword', {
          phone: this.form.phone,
          code: this.form.code,
          password: this.form.password
        });
        
        uni.hideLoading();
        uni.showToast({
          title: '密码重置成功',
          icon: 'success'
        });
        
        // 重置成功后跳转到登录页
        setTimeout(() => {
          uni.navigateBack();
        }, 1500);
        
      } catch (error) {
        uni.hideLoading();
        uni.showToast({
          title: error.message || '重置失败',
          icon: 'none'
        });
      }
    },
    
    goToLogin() {
      uni.navigateBack();
    }
  }
}
</script>

<style scoped>
.forgot-container {
  min-height: 100vh;
  background: #f8f9fa;
  display: flex;
  flex-direction: column;
  padding: 0;
}

/* 顶部区域 */
.header-section {
  background: white;
  padding: 80rpx 40rpx 60rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20rpx;
}

.logo-icon {
  width: 50rpx;
  height: 50rpx;
  border-radius: 40rpx;
  flex-shrink: 0;
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.1);
}

.page-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
  margin: 0;
}

/* 表单区域 */
.form-section {
  flex: 1;
  background: white;
  padding: 60rpx 40rpx;
}

.input-container {
  margin-bottom: 40rpx;
}

.input-field {
  width: 100%;
  height: 96rpx;
  background: #f5f5f5;
  border: none;
  border-radius: 12rpx;
  font-size: 32rpx;
  padding: 0 32rpx;
  box-sizing: border-box;
  color: #333;
}

.input-field:focus {
  background: #f0f0f0;
  outline: none;
}

.input-field::placeholder {
  color: #999;
}

/* 验证码输入组合 */
.code-input-group {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  padding: 0 32rpx;
}

.code-input {
  flex: 1;
  height: 96rpx;
  background: transparent;
  border: none;
  padding: 0;
}

.code-button {
  background: #ff6b35;
  color: white;
  border: none;
  border-radius: 24rpx;
  padding: 12rpx 24rpx;
  font-size: 26rpx;
  white-space: nowrap;
}

.code-button.disabled {
  background: #ccc;
  cursor: not-allowed;
}

.password-hint {
  font-size: 24rpx;
  color: #999;
  margin-top: 12rpx;
  line-height: 1.4;
}

.reset-button {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  color: white;
  border: none;
  border-radius: 48rpx;
  font-size: 34rpx;
  font-weight: 600;
  margin-top: 40rpx;
  box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.3);
}

.reset-button:active {
  transform: scale(0.98);
}

/* 底部链接 */
.footer-section {
  background: white;
  padding: 30rpx 40rpx 50rpx;
  border-top: 1rpx solid #f0f0f0;
}

.back-login-link {
  display: block;
  text-align: center;
  font-size: 30rpx;
  color: #ff6b35;
}
</style>
