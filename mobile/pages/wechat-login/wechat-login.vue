<template>
  <view class="wechat-login-container">
    <!-- 头部导航 -->
    <view class="header">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <text class="header-title">微信登录</text>
    </view>

    <!-- 微信登录状态 -->
    <view v-if="!isLoggedIn" class="login-section">
      <view class="wechat-info">
        <image class="wechat-avatar" :src="wechatUserInfo.headImgUrl || '/static/images/default-avatar.png'" mode="aspectFill" />
        <text class="wechat-nickname">{{ wechatUserInfo.nickName || '微信用户' }}</text>
        <text class="wechat-tip">检测到您是新用户，请绑定手机号完成注册</text>
      </view>

      <!-- 手机号绑定表单 -->
      <view class="bind-form">
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

    <!-- 登录成功状态 -->
    <view v-else class="success-section">
      <view class="success-icon">✅</view>
      <text class="success-title">登录成功</text>
      <text class="success-desc">欢迎回来，{{ userInfo.nick || userInfo.wechatNickName }}</text>
      
      <button class="continue-btn" @click="goToHome">
        进入应用
      </button>
    </view>

    <!-- 用户协议 -->
    <view class="agreement-section">
      <text class="agreement-text">
        绑定即表示同意
        <text class="agreement-link" @click="showAgreement">《用户协议》</text>
        和
        <text class="agreement-link" @click="showPrivacy">《隐私政策》</text>
      </text>
    </view>
  </view>
</template>

<script>
import api from '../../utils/api.js'

export default {
  data() {
    return {
      isLoggedIn: false,
      phone: '',
      code: '',
      codeSending: false,
      countdown: 0,
      binding: false,
      wechatUserInfo: {
        unionid: '',
        openId: '',
        nickName: '',
        headImgUrl: '',
        sex: 0,
        country: '',
        province: '',
        city: ''
      },
      userInfo: {}
    }
  },

  onLoad(options) {
    console.log('微信登录页面参数:', options);
    
    // 检查是否从登录页面跳转过来
    if (options.fromLogin === 'true') {
      // 直接进行微信登录
      this.performWechatLogin()
    } else {
      // 从页面参数获取微信用户信息
      if (options.code) {
        this.wechatUserInfo.code = decodeURIComponent(options.code);
      }
      if (options.unionid) {
        this.wechatUserInfo.unionid = decodeURIComponent(options.unionid);
      }
      if (options.openId) {
        this.wechatUserInfo.openId = decodeURIComponent(options.openId);
      }
      if (options.nickName) {
        this.wechatUserInfo.nickName = decodeURIComponent(options.nickName);
      }
      if (options.headImgUrl) {
        this.wechatUserInfo.headImgUrl = decodeURIComponent(options.headImgUrl);
      }
      if (options.sex) {
        this.wechatUserInfo.sex = parseInt(options.sex);
      }
      if (options.country) {
        this.wechatUserInfo.country = decodeURIComponent(options.country);
      }
      if (options.province) {
        this.wechatUserInfo.province = decodeURIComponent(options.province);
      }
      if (options.city) {
        this.wechatUserInfo.city = decodeURIComponent(options.city);
      }

      console.log('解析后的微信用户信息:', this.wechatUserInfo);

      // 尝试微信登录
      this.attemptWechatLogin()
    }
  },

  methods: {
    // 返回上一页
    goBack() {
      uni.navigateBack()
    },

    // 执行微信登录（App端）
    performWechatLogin() {
      // #ifdef APP-PLUS
      this.wechatAppLogin()
      // #endif
      
      // #ifdef MP-WEIXIN
      this.wechatMiniProgramLogin()
      // #endif
      
      // #ifndef APP-PLUS || MP-WEIXIN
      uni.showModal({
        title: '提示',
        content: '微信登录功能仅在App和微信小程序中可用',
        showCancel: false
      })
      // #endif
    },

    // App端微信登录
    wechatAppLogin() {
      uni.login({
        provider: 'weixin',
        onlyAuthorize: true, // 仅请求授权认证
        success: (loginRes) => {
          console.log('微信登录成功:', loginRes)
          
          // 发送code到后端进行登录
          this.loginWithCode(loginRes.code)
        },
        fail: (err) => {
          console.error('微信登录失败:', err)
          uni.showToast({
            title: '微信登录失败',
            icon: 'none'
          })
        }
      })
    },

    // 小程序端微信登录
    wechatMiniProgramLogin() {
      uni.getUserProfile({
        desc: '用于完善用户资料',
        success: (res) => {
          console.log('获取用户信息成功:', res)
          this.processWechatLogin(res.userInfo)
        },
        fail: (err) => {
          console.error('获取用户信息失败:', err)
          uni.showToast({
            title: '获取用户信息失败',
            icon: 'none'
          })
        }
      })
    },

    // 使用code进行登录
    async loginWithCode(code) {
      try {
        uni.showLoading({ title: '登录中...' })
        
        const res = await api.wechat.loginByCode({ code })
        
        uni.hideLoading()
        
        if (res.code === 0 || res.code === 10000) {
          if (res.data.isNewUser && res.data.needBindPhone) {
            // 新用户需要绑定手机号
            this.isLoggedIn = false
            this.wechatUserInfo = {
              ...this.wechatUserInfo,
              unionid: res.data.unionid || '',
              openId: res.data.openId || ''
            }
          } else {
            // 登录成功
            this.isLoggedIn = true
            this.userInfo = res.data.user
            
            // 保存token
            uni.setStorageSync('token', res.data.token)
            
            uni.showToast({
              title: '登录成功',
              icon: 'success'
            })
          }
        } else {
          uni.showToast({
            title: res.message || '登录失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('登录失败:', error)
        uni.showToast({
          title: '登录失败，请重试',
          icon: 'none'
        })
      }
    },

    // 尝试微信登录
    async attemptWechatLogin() {
      try {
        if (!this.wechatUserInfo.code) {
          uni.showToast({
            title: '微信授权码不完整',
            icon: 'none'
          })
          return
        }

        uni.showLoading({ title: '登录中...' });

        // 调用新的微信登录接口
        const response = await api.user.wechatLogin({
          code: this.wechatUserInfo.code,
          unionid: this.wechatUserInfo.unionid,
          openId: this.wechatUserInfo.openId,
          nickName: this.wechatUserInfo.nickName,
          headImgUrl: this.wechatUserInfo.headImgUrl,
          sex: this.wechatUserInfo.sex,
          country: this.wechatUserInfo.country,
          province: this.wechatUserInfo.province,
          city: this.wechatUserInfo.city
        });

        uni.hideLoading();

        if (response.code === 10000) {
          if (response.data.isNewUser) {
            // 新用户，显示绑定手机号界面
            this.isLoggedIn = false;
          } else {
            // 老用户，直接登录成功
            this.handleLoginSuccess(response.data);
          }
        } else {
          // 登录失败，显示绑定手机号界面
          this.isLoggedIn = false;
          uni.showToast({
            title: response.message || '微信登录失败',
            icon: 'none'
          });
        }
        
      } catch (error) {
        uni.hideLoading();
        console.error('微信登录失败:', error);
        this.isLoggedIn = false;
        uni.showToast({
          title: '微信登录失败，请绑定手机号',
          icon: 'none'
        });
      }
    },

    // 处理登录成功
    handleLoginSuccess(data) {
      // 保存用户信息和token
      if (data.token) {
        uni.setStorageSync('token', data.token);
      }
      if (data.user) {
        uni.setStorageSync('userInfo', JSON.stringify(data.user));
      }
      
      this.isLoggedIn = true;
      
      uni.showToast({
        title: '登录成功',
        icon: 'success'
      });
      
      // 延迟跳转到首页
      setTimeout(() => {
        uni.switchTab({
          url: '/pages/index/index',
          fail: () => {
            uni.reLaunch({
              url: '/pages/index/index'
            });
          }
        });
      }, 1500);
    },

    // 旧的登录逻辑（保留兼容）
    async oldAttemptWechatLogin() {
      try {
        if (!this.wechatUserInfo.unionid) {
          uni.showToast({
            title: '微信授权信息不完整',
            icon: 'none'
          })
          return
        }

        const loginData = {
          unionid: this.wechatUserInfo.unionid,
          openId: this.wechatUserInfo.openId,
          nickName: this.wechatUserInfo.nickName,
          headImgUrl: this.wechatUserInfo.headImgUrl,
          sex: this.wechatUserInfo.sex,
          country: this.wechatUserInfo.country,
          province: this.wechatUserInfo.province,
          city: this.wechatUserInfo.city
        }

        const res = await api.wechat.login(loginData)
        
        if (res.code === 0 || res.code === 10000) {
          if (res.data.isNewUser && res.data.needBindPhone) {
            // 新用户需要绑定手机号
            this.isLoggedIn = false
          } else {
            // 登录成功
            this.isLoggedIn = true
            this.userInfo = res.data.user
            
            // 保存token
            uni.setStorageSync('token', res.data.token)
            
            uni.showToast({
              title: '登录成功',
              icon: 'success'
            })
          }
        } else {
          uni.showToast({
            title: res.message || '登录失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('微信登录失败:', error)
        uni.showToast({
          title: '登录失败，请重试',
          icon: 'none'
        })
      }
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
          
          // 开始倒计时
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
      if (!this.phone) {
        uni.showToast({
          title: '请输入手机号',
          icon: 'none'
        })
        return
      }

      if (!this.code) {
        uni.showToast({
          title: '请输入验证码',
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
        this.binding = true
        
        const bindData = {
          unionid: this.wechatUserInfo.unionid,
          phone: this.phone,
          code: this.code
        }

        const res = await api.wechat.bindPhone(bindData)
        
        if (res.code === 0 || res.code === 10000) {
          this.isLoggedIn = true
          this.userInfo = res.data.user
          
          // 保存token
          uni.setStorageSync('token', res.data.token)
          
          uni.showToast({
            title: '绑定成功',
            icon: 'success'
          })
        } else {
          uni.showToast({
            title: res.message || '绑定失败',
            icon: 'none'
          })
        }
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

    // 进入应用
    goToHome() {
      uni.switchTab({
        url: '/pages/index/index'
      })
    },

    // 显示用户协议
    showAgreement() {
      uni.showModal({
        title: '用户协议',
        content: '这里是用户协议的内容...',
        showCancel: false
      })
    },

    // 显示隐私政策
    showPrivacy() {
      uni.showModal({
        title: '隐私政策',
        content: '这里是隐私政策的内容...',
        showCancel: false
      })
    }
  }
}
</script>

<style scoped>
.wechat-login-container {
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

.login-section {
  padding: 40rpx 20rpx;
}

.wechat-info {
  text-align: center;
  margin-bottom: 60rpx;
}

.wechat-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  margin-bottom: 20rpx;
}

.wechat-nickname {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}

.wechat-tip {
  display: block;
  font-size: 24rpx;
  color: #666;
}

.bind-form {
  background: white;
  border-radius: 20rpx;
  padding: 40rpx;
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
  background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
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

.success-section {
  text-align: center;
  padding: 100rpx 40rpx;
}

.success-icon {
  font-size: 120rpx;
  margin-bottom: 30rpx;
}

.success-title {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 15rpx;
}

.success-desc {
  display: block;
  font-size: 28rpx;
  color: #666;
  margin-bottom: 60rpx;
}

.continue-btn {
  width: 400rpx;
  height: 88rpx;
  background: linear-gradient(135deg, #52c41a, #73d13d);
  color: white;
  border: none;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: bold;
}

.agreement-section {
  position: fixed;
  bottom: 40rpx;
  left: 40rpx;
  right: 40rpx;
  text-align: center;
}

.agreement-text {
  font-size: 22rpx;
  color: #999;
  line-height: 1.5;
}

.agreement-link {
  color: #1890ff;
}
</style>
