<template>
  <view class="withdraw-container">
    <!-- 进度条 -->
    <view class="progress-bar">
      <view class="progress-step" :class="{ active: currentStep >= 1, completed: currentStep > 1 }">
        <view class="step-icon">
          <text v-if="currentStep > 1" class="check-icon">✓</text>
          <text v-else class="step-number">1</text>
        </view>
        <text class="step-text">选择账户</text>
      </view>
      <view class="progress-line" :class="{ active: currentStep >= 2 }"></view>
      <view class="progress-step" :class="{ active: currentStep >= 2, completed: currentStep > 2 }">
        <view class="step-icon">
          <text v-if="currentStep > 2" class="check-icon">✓</text>
          <text v-else class="step-number">2</text>
        </view>
        <text class="step-text">填写金额</text>
      </view>
      <view class="progress-line" :class="{ active: currentStep >= 3 }"></view>
      <view class="progress-step" :class="{ active: currentStep >= 3, completed: currentStep > 3 }">
        <view class="step-icon">
          <text v-if="currentStep > 3" class="check-icon">✓</text>
          <text v-else class="step-number">3</text>
        </view>
        <text class="step-text">完成</text>
      </view>
    </view>

    <!-- 步骤内容 -->
    <view class="step-content">
      <!-- 步骤1：选择账户 -->
      <view v-if="currentStep === 1" class="step-page">
        <text class="step-title">选择提现账户</text>
        <view class="account-selection">
          <view class="account-item" :class="{ selected: selectedAccount === 'wechat', disabled: !wechatAuthStatus }" @click="selectAccount('wechat')">
            <view class="account-info">
              <view class="account-icon wechat-icon">
                <text class="icon-text">微</text>
              </view>
              <view class="account-details">
                <text class="account-name">微信支付</text>
                <text class="account-desc" v-if="wechatAuthStatus">微***用户</text>
                <text class="account-desc unauthorized" v-else>未授权，将自动绑定</text>
              </view>
            </view>
            <view v-if="selectedAccount === 'wechat'" class="selected-icon">
              <text class="check-icon">✓</text>
            </view>
            <view v-if="!wechatAuthStatus" class="auth-status">
              <button class="authorize-btn" @click.stop="authorizeWechat">未授权去授权</button>
            </view>
          </view>
        </view>
      </view>

      <!-- 步骤2：填写金额 -->
      <view v-if="currentStep === 2" class="step-page">
        <text class="step-title">提现金额</text>
        <text class="amount-hint">请输入您要提现的金额</text>
        <view class="amount-input-container">
          <text class="currency-symbol">¥</text>
          <input 
            class="amount-input" 
            type="number" 
            v-model="withdrawAmount" 
            placeholder="请输入金额"
            @input="onAmountInput"
          />
        </view>
        <view class="amount-info">
          <text class="min-amount">最低提现金额: ¥10.00</text>
          <text class="available-balance">可用余额: <text class="balance-highlight">¥{{ formatNumber(availableBalance) }}</text></text>
        </view>
        <view class="quick-amounts">
          <view class="quick-amount-btn" @click="setAmount(50)">50</view>
          <view class="quick-amount-btn" @click="setAmount(100)">100</view>
          <view class="quick-amount-btn" @click="setAmount(200)">200</view>
        </view>
      </view>

      <!-- 步骤3：完成 -->
      <view v-if="currentStep === 3" class="step-page">
        <view class="success-container">
          <view class="success-icon">
            <text class="success-check">✓</text>
          </view>
          <text class="success-title">提现申请提交成功</text>
          <text class="success-message">我们将尽快处理您的提现请求，请耐心等待</text>
        </view>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="bottom-button">
      <view class="action-btn" :class="{ disabled: isNextStepDisabled() }" @click="handleNextStep">
        <text class="btn-text">{{ getButtonText() }}</text>
      </view>
    </view>
  </view>
</template>

<script>
import api from '../../utils/api.js'

export default {
  data() {
    return {
      currentStep: 1,
      selectedAccount: 'wechat',
      withdrawAmount: '',
      availableBalance: 12568.90,
      minWithdrawAmount: 10.00,
      wechatAuthStatus: false // 微信授权状态
    }
  },

  onLoad() {
    this.loadUserBalance();
    this.checkWechatAuthStatus();
  },

  methods: {
    async authorizeWechat() {
      try {
        // 使用uni-app标准微信登录API获取授权code
        uni.login({
          provider: 'weixin',
          onlyAuthorize: true, // 微信登录仅请求授权认证
          success: async (event) => {
            const { code } = event;
            // 将code发送给后端进行验证和绑定
            try {
              const res = await api.user.wechatAuthorize({ code });
              if (res.code === 10000) {
                this.wechatAuthStatus = true;
                uni.showToast({ title: '授权成功', icon: 'success' });
              } else {
                uni.showToast({ title: res.message || '授权失败', icon: 'none' });
              }
            } catch (error) {
              uni.showToast({ title: '授权失败，请重试', icon: 'none' });
            }
          },
          fail: (err) => {
            console.error('微信登录授权失败:', err);
            uni.showToast({ title: '微信授权失败', icon: 'none' });
          }
        });
      } catch (error) {
        uni.showToast({ title: '授权失败，请重试', icon: 'none' });
      }
    },
    async loadUserBalance() {
      try {
        const res = await api.user.earnings();
        if (res.code === 10000 && res.data) {
          this.availableBalance = res.data.withdrawable || 0;
        }
      } catch (error) {
        console.error('加载余额失败:', error);
      }
    },

    goBack() {
      if (this.currentStep > 1) {
        this.currentStep--;
      } else {
        uni.navigateBack();
      }
    },

    async selectAccount(account) {
      // 直接选择账户，不再弹出授权页面；未授权将由后端自动绑定
      this.selectedAccount = account;
    },

    onAmountInput(e) {
      this.withdrawAmount = e.detail.value;
    },

    setAmount(amount) {
      this.withdrawAmount = amount.toString();
    },

    // 检查微信授权状态
    async checkWechatAuth() {
      try {
        const res = await api.user.checkWechatAuth();
        return res.code === 10000 && res.data && res.data.isAuthorized;
      } catch (error) {
        console.error('检查微信授权失败:', error);
        return false;
      }
    },

    // 检查微信授权状态并更新UI
    async checkWechatAuthStatus() {
      try {
        const res = await api.user.checkWechatAuth();
        this.wechatAuthStatus = res.code === 10000 && res.data && res.data.isAuthorized;
      } catch (error) {
        console.error('检查微信授权状态失败:', error);
        this.wechatAuthStatus = false;
      }
    },

    // 显示微信授权模态框
    showWechatAuthModal() {
      uni.showModal({
        title: '需要微信授权',
        content: '提现到微信需要先进行微信授权，是否立即授权？',
        confirmText: '立即授权',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            this.goToWechatAuth();
          }
        }
      });
    },

    // 跳转到微信授权页面
    goToWechatAuth() {
      uni.navigateTo({
        url: '/pages/wechat-auth/wechat-auth?from=withdraw'
      });
    },

    async handleNextStep() {
      if (this.currentStep === 1) {
        // 验证账户选择
        if (!this.selectedAccount) {
          uni.showToast({ title: '请选择提现账户', icon: 'none' });
          return;
        }
        
        // 如果选择微信支付但未授权，阻止进入下一步
        if (this.selectedAccount === 'wechat' && !this.wechatAuthStatus) {
          uni.showToast({ title: '请先完成微信授权', icon: 'none' });
          return;
        }
        
        this.currentStep = 2;
      } else if (this.currentStep === 2) {
        // 验证金额
        const amount = parseFloat(this.withdrawAmount);
        if (!amount || amount < this.minWithdrawAmount) {
          uni.showToast({ title: `最低提现金额为¥${this.minWithdrawAmount}`, icon: 'none' });
          return;
        }
        if (amount > this.availableBalance) {
          uni.showToast({ title: '余额不足', icon: 'none' });
          return;
        }

        // 前端不再拦截微信授权，后端将自动绑定

        // 提交提现申请
        try {
          const res = await api.user.withdraw({ 
            amount: amount,
            accountType: this.selectedAccount
          });
          if (res.code === 10000) {
            this.currentStep = 3;
          } else {
            uni.showToast({ title: res.message || '提现失败', icon: 'none' });
          }
        } catch (error) {
          console.error('提现失败:', error);
          uni.showToast({ title: '提现失败，请重试', icon: 'none' });
        }
      } else if (this.currentStep === 3) {
        // 完成提现流程
        uni.navigateBack();
      }
    },

    getButtonText() {
      switch (this.currentStep) {
        case 1: return '下一步';
        case 2: return '确认提现';
        case 3: return '完成';
        default: return '下一步';
      }
    },

    // 判断下一步按钮是否应该被禁用
    isNextStepDisabled() {
      if (this.currentStep === 1) {
        return this.selectedAccount === 'wechat' && !this.wechatAuthStatus;
      }
      return false;
    },

    formatNumber(num) {
      if (!num) return '0.00';
      const n = parseFloat(num);
      return n.toLocaleString('zh-CN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      });
    }
  }
}
</script>

<style scoped>
.withdraw-container {
  background: #f5f5f5;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: white;
  padding: 20rpx 30rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1rpx solid #f0f0f0;
}

.nav-back {
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
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.header-time {
  font-size: 24rpx;
  color: #666;
}

.progress-bar {
  background: white;
  padding: 40rpx 30rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.step-icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 15rpx;
}

.step-icon.active {
  background: #52c41a;
}

.step-number {
  font-size: 24rpx;
  color: #999;
  font-weight: bold;
}

.step-icon.active .step-number {
  color: white;
}

.check-icon {
  font-size: 24rpx;
  color: white;
  font-weight: bold;
}

.step-text {
  font-size: 24rpx;
  color: #999;
}

.progress-step.active .step-text {
  color: #52c41a;
}

.progress-step.completed .step-text {
  color: #52c41a;
}

.progress-line {
  flex: 1;
  height: 2rpx;
  background: #f0f0f0;
  margin: 0 20rpx;
  margin-bottom: 30rpx;
}

.progress-line.active {
  background: #52c41a;
}

.step-content {
  flex: 1;
  padding: 40rpx 30rpx;
}

.step-page {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.step-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 40rpx;
  text-align: center;
}

.account-selection {
  width: 100%;
  max-width: 600rpx;
}

.account-item {
  background: white;
  border-radius: 15rpx;
  padding: 30rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 2rpx solid #f0f0f0;
  cursor: pointer;
}

.account-item.selected {
  border-color: #52c41a;
  background: #f6ffed;
}

.account-item.disabled {
  opacity: 0.6;
  background: #f5f5f5;
}

.account-info {
  display: flex;
  align-items: center;
}

.account-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.wechat-icon {
  background: #07c160;
}

.icon-text {
  color: white;
  font-size: 24rpx;
  font-weight: bold;
}

.account-details {
  display: flex;
  flex-direction: column;
}

.account-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 5rpx;
}

.account-desc {
  font-size: 24rpx;
  color: #666;
}

.account-desc.unauthorized {
  color: #ff4d4f;
}

.auth-status {
  background: #fff2f0;
  color: #ff4d4f;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
}

.auth-text {
  color: #ff4d4f;
}

.authorize-btn {
  background: #07c160;
  color: white;
  border: none;
  border-radius: 20rpx;
  padding: 8rpx 16rpx;
  font-size: 22rpx;
  cursor: pointer;
}

.authorize-btn:active {
  background: #06ad56;
}

.selected-icon {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: #52c41a;
  display: flex;
  align-items: center;
  justify-content: center;
}

.amount-hint {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 30rpx;
  text-align: center;
}

.amount-input-container {
  background: white;
  border-radius: 15rpx;
  padding: 30rpx;
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
  width: 100%;
  max-width: 600rpx;
}

.currency-symbol {
  font-size: 36rpx;
  color: #333;
  margin-right: 20rpx;
  font-weight: bold;
}

.amount-input {
  flex: 1;
  font-size: 36rpx;
  color: #333;
  border: none;
  outline: none;
}

.amount-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30rpx;
}

.min-amount {
  font-size: 24rpx;
  color: #666;
  margin-bottom: 10rpx;
}

.available-balance {
  font-size: 24rpx;
  color: #666;
}

.balance-highlight {
  color: #1890ff;
  font-weight: bold;
}

.quick-amounts {
  display: flex;
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.quick-amount-btn {
  background: white;
  border: 2rpx solid #f0f0f0;
  border-radius: 10rpx;
  padding: 20rpx 30rpx;
  font-size: 28rpx;
  color: #333;
  cursor: pointer;
}

.quick-amount-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.success-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.success-icon {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: #f6ffed;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30rpx;
}

.success-check {
  font-size: 60rpx;
  color: #52c41a;
  font-weight: bold;
}

.success-title {
  font-size: 32rpx;
  color: #333;
  font-weight: bold;
  margin-bottom: 20rpx;
}

.success-message {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
  max-width: 500rpx;
}

.bottom-button {
  padding: 30rpx;
  background: white;
  border-top: 1rpx solid #f0f0f0;
}

.action-btn {
  background: #1890ff;
  color: white;
  padding: 30rpx;
  border-radius: 15rpx;
  text-align: center;
  cursor: pointer;
}

.action-btn.disabled {
  background: #d9d9d9;
  color: #999;
  cursor: not-allowed;
}

.btn-text {
  font-size: 32rpx;
  font-weight: 500;
}
</style>
