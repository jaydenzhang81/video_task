<template>
  <view class="login-container">
    <!-- 顶部logo和标题区域 -->
    <view class="header-section">
      <view class="header-content">
        <image class="logo-icon" src="/static/images/logo.png" mode="aspectFit" />
        <text class="page-title">账号密码登录</text>
      </view>
    </view>
    
    <!-- 输入表单区域 -->
    <view class="form-section">
      <!-- 账号输入 -->
      <view class="input-container">
        <input 
          class="input-field" 
          type="text" 
          placeholder="请输入账号" 
          v-model="form.phone"
        />
      </view>
      
      <!-- 密码输入 -->
      <view class="input-container">
        <input 
          class="input-field" 
          type="password" 
          placeholder="请输入密码" 
          v-model="form.password"
        />
      </view>
      
      <!-- 功能链接 -->
      <view class="function-links">
        <text class="link-text" @click="goToRegister">新用户注册</text>
        <text class="link-text forgot-link" @click="goToForgotPassword">忘记密码？</text>
      </view>
      
      <!-- 登录按钮 -->
      <button class="login-button" @click="handleLogin">登录</button>
      
      <!-- 验证码登录 -->
      <text class="verify-login-link" @click="handleSmsLogin">验证码登录</text>
    </view>
    
    <!-- 其他登录方式 -->
    <view class="other-login-section">
      <text class="other-login-title">其他登录方式</text>
      <view class="wechat-login" @click="handleWechatLogin">
        <image class="wechat-icon" src="/static/images/weixin.png" mode="aspectFit" />
      </view>
    </view>
    
    <!-- 底部用户协议 -->
    <view class="agreement-section">
      <label class="agreement-label">
        <checkbox-group @change="onAgreementChange">
          <checkbox class="agreement-checkbox" :checked="agreed" />
        </checkbox-group>
        <text class="agreement-text">
          我已阅读 
          <text class="agreement-link">用户协议</text> 
          及
          <text class="agreement-link">隐私协议</text> 
          并同意相关条款内容
        </text>
      </label>
    </view>
  </view>
</template>

<script>
import api from '../../utils/api.js'
import { saveToken, saveUserInfo } from '../../utils/auth.js'

export default {
  data() {
    return {
      form: {
        phone: '',
        password: ''
      },
      agreed: false
    }
  },
  methods: {
    async handleLogin() {
      if (!this.agreed) {
        uni.showToast({
          title: '请先同意用户协议',
          icon: 'none'
        });
        return;
      }
      
      if (!this.form.phone || !this.form.password) {
        uni.showToast({
          title: '请输入账号和密码',
          icon: 'none'
        });
        return;
      }
      
      try {
        uni.showLoading({ title: '登录中...' });
        
        const res = await api.post('/user/login', {
          phone: this.form.phone,
          password: this.form.password
        });
        
        uni.hideLoading();
        
        // 保存token和用户信息
        console.log('登录响应:', res);
        
        // 从响应中提取token和用户信息
        const token = res.data?.token || res.data?.data?.token || res.token;
        const userInfo = res.data?.userInfo || res.data?.data?.userInfo;
        
        if (token) {
          saveToken(token);
          console.log('保存的token:', token);
        } else {
          console.error('未找到token:', res);
          throw new Error('登录响应中未找到token');
        }
        
        // 保存用户信息
        if (userInfo) {
          saveUserInfo(userInfo);
        }
        
        uni.showToast({
          title: '登录成功',
          icon: 'success'
        });
        
        // 跳转到首页
        setTimeout(() => {
          try {
            uni.switchTab({
              url: '/pages/index/index',
              success: () => {
                console.log('跳转到首页成功');
              },
              fail: (err) => {
                console.error('跳转到首页失败:', err);
                // 如果switchTab失败，尝试使用reLaunch
                uni.reLaunch({
                  url: '/pages/index/index'
                });
              }
            });
          } catch (error) {
            console.error('跳转异常:', error);
            uni.reLaunch({
              url: '/pages/index/index'
            });
          }
        }, 1500);
        
      } catch (error) {
        uni.hideLoading();
        uni.showToast({
          title: error.message || '登录失败',
          icon: 'none'
        });
      }
    },
    
    handleSmsLogin() {
      uni.showToast({
        title: '短信登录功能开发中',
        icon: 'none'
      });
    },
    
    handleWechatLogin() {
      // 检查是否支持微信登录
      // #ifdef APP-PLUS
      this.wechatAppLogin();
      // #endif
      
      // #ifdef MP-WEIXIN
      this.wechatMiniProgramLogin();
      // #endif
      
      // #ifndef APP-PLUS || MP-WEIXIN
      uni.showModal({
        title: '提示',
        content: '微信登录功能仅在App和微信小程序中可用',
        showCancel: false
      });
      // #endif
    },
    
    // App端微信登录
    wechatAppLogin() {
      uni.login({
        provider: 'weixin',
        onlyAuthorize: true, // 仅请求授权认证
        success: (loginRes) => {
          console.log('微信登录成功:', loginRes);
          
          // 跳转到微信登录页面处理
          uni.navigateTo({
            url: `/pages/wechat-login/wechat-login?fromLogin=true`
          });
        },
        fail: (err) => {
          console.error('微信登录失败:', err);
          uni.showToast({
            title: '微信登录失败',
            icon: 'none'
          });
        }
      });
    },

    // 小程序端微信登录
    wechatMiniProgramLogin() {
      uni.getUserProfile({
        desc: '用于完善用户资料',
        success: (res) => {
          console.log('获取用户信息成功:', res);
          this.processWechatLogin(res.userInfo);
        },
        fail: (err) => {
          console.error('获取用户信息失败:', err);
          uni.showToast({
            title: '获取用户信息失败',
            icon: 'none'
          });
        }
      });
    },
    
    // 处理微信登录
    processWechatLogin(userInfo) {
      // 获取微信登录code
      uni.login({
        provider: 'weixin',
        success: (loginRes) => {
          console.log('微信登录成功:', loginRes);
          
          // 构建微信用户信息
          const wechatData = {
            code: loginRes.code,
            unionid: '', // 这里需要从后端获取
            openId: loginRes.openid || '',
            nickName: userInfo.nickName,
            headImgUrl: userInfo.avatarUrl,
            sex: userInfo.gender,
            country: userInfo.country,
            province: userInfo.province,
            city: userInfo.city
          };
          
          // 跳转到微信登录页面
          const params = new URLSearchParams();
          Object.keys(wechatData).forEach(key => {
            if (wechatData[key]) {
              params.append(key, encodeURIComponent(wechatData[key]));
            }
          });
          
          uni.navigateTo({
            url: `/pages/wechat-login/wechat-login?${params.toString()}`
          });
        },
        fail: (err) => {
          console.error('微信登录失败:', err);
          uni.showToast({
            title: '微信登录失败',
            icon: 'none'
          });
        }
      });
    },
    
    goToRegister() {
      uni.navigateTo({ url: '/pages/register/register' });
    },
    
    goToForgotPassword() {
      uni.navigateTo({ url: '/pages/forgot-password/forgot-password' });
    },

    onAgreementChange(e) {
      this.agreed = e.detail.value;
    },


  }
}
</script>

<style scoped>
.login-container {
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

.function-links {
  display: flex;
  justify-content: space-between;
  margin: 40rpx 0 60rpx;
}

.link-text {
  font-size: 28rpx;
  color: #666;
}

.forgot-link {
  color: #ff6b35;
}

.login-button {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  color: white;
  border: none;
  border-radius: 48rpx;
  font-size: 34rpx;
  font-weight: 600;
  margin-bottom: 40rpx;
  box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.3);
}

.login-button:active {
  transform: scale(0.98);
}

.verify-login-link {
  display: block;
  text-align: center;
  font-size: 30rpx;
  color: #666;
  margin-top: 20rpx;
}

/* 其他登录方式 */
.other-login-section {
  background: white;
  padding: 40rpx;
  text-align: center;
  border-top: 1rpx solid #f0f0f0;
}

.other-login-title {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 40rpx;
  display: block;
}

.wechat-login {
  display: inline-block;
}

.wechat-icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  box-shadow: 0 3rpx 12rpx rgba(0, 0, 0, 0.1);
}

.wechat-login:active .wechat-icon {
  transform: scale(0.95);
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
</style> 