<template>
  <view class="register-container">
    <!-- 顶部logo和标题区域 -->
    <view class="header-section">
      <view class="header-content">
        <image class="logo-icon" src="/static/images/logo.png" mode="aspectFit" />
        <text class="page-title">新用户注册</text>
      </view>
    </view>
    
    <!-- 输入表单区域 -->
    <view class="form-section">
    
      <!-- 手机号输入 -->
      <view class="input-container">
        <input 
          class="input-field" 
          :class="{ error: errors.phone }"
          type="text" 
          placeholder="请输入手机号" 
          v-model="form.phone"
          maxlength="11"
          @blur="validatePhone"
        />
        <text v-if="errors.phone" class="error-text">{{ errors.phone }}</text>
      </view>
    
      <!-- 验证码输入 -->
      <view class="input-container">
        <view class="code-input-group" :class="{ error: errors.code }">
          <input 
            class="input-field code-input" 
            :class="{ disabled: !codeInputEnabled }"
            type="text" 
            :placeholder="codeInputEnabled ? '请输入验证码' : '请先获取验证码'" 
            v-model="form.code"
            maxlength="6"
            :readonly="!codeInputEnabled"
            @blur="validateCode"
            @focus="onCodeFocus"
          />
          <button 
            class="code-button" 
            :class="{ disabled: countdown > 0 }"
            @click="sendCode"
          >
            {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
          </button>
        </view>
        <text v-if="errors.code" class="error-text">{{ errors.code }}</text>
      </view>
    
      <!-- 设置新密码 -->
      <view class="input-container">
        <input 
          class="input-field" 
          :class="{ error: errors.password }"
          type="password" 
          placeholder="请输入密码"
          v-model="form.password"
          @blur="validatePassword"
        />
        <view class="password-hint">6-12个字符,支持数字、大小写字母和符号至少包含一种</view>
        <text v-if="errors.password" class="error-text">{{ errors.password }}</text>
      </view>
      
      <!-- 昵称输入 -->
      <view class="input-container">
        <input 
          class="input-field" 
          type="text" 
          placeholder="请输入昵称（选填）" 
          v-model="form.nick"
        />
      </view>
      
      <!-- 注册按钮 -->
      <button class="register-button" @click="handleRegister">注册</button>
    </view>
    
    <!-- 底部用户协议 -->
    <view class="agreement-section">
      <label class="agreement-label">
        <checkbox-group @change="onAgreementChange">
          <checkbox class="agreement-checkbox" value="agree" :checked="agreed" />
        </checkbox-group>
        <text class="agreement-text">
          我已阅读 
          <text class="agreement-link">用户协议</text> 
          和
          <text class="agreement-link">隐私政策</text> 
          并同意相关条款内容
        </text>
      </label>
      <text class="return-link" @click="goToLogin">已有账号？立即登录</text>
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
        nick: ''
      },
      errors: {
        phone: '',
        code: '',
        password: ''
      },
      agreed: false,
      countdown: 0,
      countdownTimer: null,
      codeInputEnabled: false // 验证码输入框是否可用
    }
  },
  onLoad() {
    console.log('注册页面加载，agreed初始状态:', this.agreed);
    console.log('初始验证码输入框状态:', this.codeInputEnabled);
    this.checkCountdownStatus();
  },

  onUnload() {
    // 页面销毁时清理定时器
    if (this.countdownTimer) {
      clearTimeout(this.countdownTimer);
    }
  },
  methods: {
    // 验证手机号
    validatePhone() {
      if (!this.form.phone) {
        this.errors.phone = '请输入手机号';
        return false;
      }
      if (!/^1[3-9]\d{9}$/.test(this.form.phone)) {
        this.errors.phone = '请输入正确的手机号';
        return false;
      }
      this.errors.phone = '';
      return true;
    },

    // 验证码输入框获取焦点时处理
    onCodeFocus() {
      if (!this.codeInputEnabled) {
        uni.showToast({
          title: '请先获取验证码',
          icon: 'none'
        });
        // 移除焦点
        uni.hideKeyboard();
      }
    },

    // 验证验证码
    validateCode() {
      // 如果验证码输入框未启用，不进行验证
      if (!this.codeInputEnabled) {
        this.errors.code = '请先获取验证码';
        return false;
      }
      
      if (!this.form.code) {
        this.errors.code = '请输入验证码';
        return false;
      }
      if (this.form.code.length !== 6) {
        this.errors.code = '验证码应为6位数字';
        return false;
      }
      this.errors.code = '';
      return true;
    },

    // 验证密码
    validatePassword() {
      if (!this.form.password) {
        this.errors.password = '请输入密码';
        return false;
      }
      if (this.form.password.length < 6) {
        this.errors.password = '密码长度不能少于6位';
        return false;
      }
      if (this.form.password.length > 12) {
        this.errors.password = '密码长度不能超过12位';
        return false;
      }
      this.errors.password = '';
      return true;
    },

    // 验证所有字段
    validateAll() {
      const phoneValid = this.validatePhone();
      const codeValid = this.validateCode();
      const passwordValid = this.validatePassword();
      
      return phoneValid && codeValid && passwordValid;
    },

    // 检查倒计时状态（防止页面刷新后倒计时丢失）
    checkCountdownStatus() {
      const savedCountdown = uni.getStorageSync('register_countdown');
      const savedTime = uni.getStorageSync('register_countdown_time');
      const codeEnabled = uni.getStorageSync('register_code_enabled');
      
      // 恢复验证码输入框状态
      if (codeEnabled) {
        this.codeInputEnabled = true;
        console.log('从存储恢复验证码输入框状态:', this.codeInputEnabled);
      } else {
        console.log('验证码输入框保持禁用状态');
      }
      
      if (savedCountdown && savedTime) {
        const now = Date.now();
        const elapsed = Math.floor((now - savedTime) / 1000);
        const remaining = savedCountdown - elapsed;
        
        if (remaining > 0) {
          this.countdown = remaining;
          this.startCountdown();
        } else {
          // 清除过期的倒计时
          uni.removeStorageSync('register_countdown');
          uni.removeStorageSync('register_countdown_time');
        }
      }
    },

    async sendCode() {
      if (this.countdown > 0) {
        return; // 防止重复点击
      }

      // 验证手机号
      if (!this.validatePhone()) {
        return;
      }
      
      try {
        uni.showLoading({ title: '发送中...' });
        
        const res = await api.user.sendCode({
          phone: this.form.phone
        });
        
        uni.hideLoading();
        
        if (res.code === 10000) {
          uni.showToast({
            title: '验证码已发送',
            icon: 'success'
          });
          
          // 启用验证码输入框
          this.codeInputEnabled = true;
          console.log('验证码输入框已启用:', this.codeInputEnabled);
          
          // 开始倒计时并保存状态
          this.countdown = 60;
          uni.setStorageSync('register_countdown', this.countdown);
          uni.setStorageSync('register_countdown_time', Date.now());
          uni.setStorageSync('register_code_enabled', true);
          this.startCountdown();
        } else {
          uni.showToast({
            title: res.message || '发送失败',
            icon: 'none'
          });
        }
        
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
        const timer = setTimeout(() => {
          this.countdown--;
          // 更新存储的倒计时
          if (this.countdown > 0) {
            uni.setStorageSync('register_countdown', this.countdown);
          } else {
            // 倒计时结束，清除存储
            uni.removeStorageSync('register_countdown');
            uni.removeStorageSync('register_countdown_time');
          }
          this.startCountdown();
        }, 1000);
        
        // 保存timer引用，以便页面销毁时清除
        this.countdownTimer = timer;
      }
    },
    
    async handleRegister() {
      console.log('注册时agreed状态:', this.agreed);
      
      // 验证用户协议
      if (!this.agreed) {
        uni.showToast({
          title: '请先同意用户协议',
          icon: 'none'
        });
        return;
      }
      
      // 验证所有字段
      if (!this.validateAll()) {
        uni.showToast({
          title: '请检查输入信息',
          icon: 'none'
        });
        return;
      }
      
      try {
        uni.showLoading({ title: '注册中...' });
        
        const res = await api.user.register({
          phone: this.form.phone,
          code: this.form.code,
          password: this.form.password,
          nick: this.form.nick
        });
        
        uni.hideLoading();
        
        // 清除倒计时和验证码输入框状态
        uni.removeStorageSync('register_countdown');
        uni.removeStorageSync('register_countdown_time');
        uni.removeStorageSync('register_code_enabled');
        
        uni.showToast({
          title: '注册成功',
          icon: 'success'
        });
        
        // 注册成功后跳转到登录页
        setTimeout(() => {
          uni.navigateBack();
        }, 1500);
        
      } catch (error) {
        uni.hideLoading();
        uni.showToast({
          title: error.message || '注册失败',
          icon: 'none'
        });
      }
    },
    
    goToLogin() {
      uni.navigateBack();
    },

    onAgreementChange(e) {
      this.agreed = e.detail.value.length > 0;
      console.log('Agreement changed:', this.agreed, 'values:', e.detail.value);
    }
  }
}
</script>

<style scoped>
.register-container {
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

/* 表单区域 */
.form-section {
  flex: 1;
  background: white;
  padding: 60rpx 40rpx;
}

.input-container {
  margin-bottom: 40rpx;
}

.page-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
  margin: 0;
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

.input-field.error {
  background: #fff5f5;
  border: 1rpx solid #ff4d4f;
}

.input-field.disabled {
  color: #ccc !important;
  background-color: #f5f5f5 !important;
  cursor: not-allowed;
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

.code-input-group.error {
  background: #fff5f5;
  border: 1rpx solid #ff4d4f;
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

.register-button {
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

.register-button:active {
  transform: scale(0.98);
}

/* 底部协议 */
.agreement-section {
  background: white;
  padding: 30rpx 40rpx 50rpx;
  border-top: 1rpx solid #f0f0f0;
}

.agreement-label {
  display: flex;
  align-items: flex-start;
  margin-bottom: 30rpx;
}

.agreement-checkbox {
  margin-right: 20rpx;
  margin-top: 4rpx;
}

.agreement-text {
  font-size: 24rpx;
  color: #999;
  line-height: 1.6;
  flex: 1;
}

.agreement-link {
  color: #ff6b35;
}

.return-link {
  display: block;
  text-align: center;
  font-size: 30rpx;
  color: #ff6b35;
  margin-top: 10rpx;
}

.error-text {
  font-size: 24rpx;
  color: #ff4d4f;
  margin-top: 10rpx;
  line-height: 1.4;
}
</style>
