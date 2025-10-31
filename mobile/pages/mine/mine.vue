<template>
  <view class="mine-container">
    <!-- 用户信息头部 -->
    <view class="user-header">
      <view class="user-info">
        <view class="user-avatar" v-if="!userInfo.avatar" @click="editProfile">
          <text class="avatar-text">{{ getAvatarText() }}</text>
        </view>
        <image class="user-avatar" v-else :src="userInfo.avatar" mode="aspectFill" @click="editProfile" />
        <view class="user-details">
          <text class="user-name">{{ userInfo.nick || '用户1234567801' }}</text>
          <text class="team-text">我的团队: {{ currentTeam.name }}</text>
        </view>
        <view class="switch-team-btn" @click="openTeamDialog">
          <text class="switch-team-text">切换团队</text>
        </view>
      </view>
    </view>

    <!-- 可提现区域 -->
    <view class="withdrawable-section">
      <text class="withdrawable-label">可提现(元)</text>
      <text class="withdrawable-amount">¥{{ formatNumber(earnings.withdrawable || 0) }}</text>
      <view class="withdraw-btn" @click="withdraw">
        <text class="btn-text">提现</text>
      </view>
    </view>

    <!-- 收益指标 -->
    <view class="earnings-section">
      <view class="earnings-header">
        <text class="earnings-title">我的收益</text>
        <text class="earnings-link" @click="goToFinanceFlow">财务流水 ></text>
      </view>
      <view class="earnings-indicators">
        <view class="indicator-item">
          <text class="indicator-title">今日收益</text>
          <text class="indicator-value">{{ formatNumber(earnings.todayEarnings || 0) }}</text>
        </view>
        <view class="indicator-item">
          <text class="indicator-title">总收益</text>
          <text class="indicator-value">{{ formatNumber(earnings.totalEarnings || 0) }}</text>
        </view>
        <view class="indicator-item">
          <text class="indicator-title">待提现</text>
          <text class="indicator-value">{{ formatNumber(earnings.pendingWithdraw || 0) }}</text>
        </view>
      </view>
    </view>

    <!-- 个人中心 -->
    <view class="personal-center">
      <text class="section-title">个人中心</text>
      <view class="no-permission-tip">
        <text class="tip-text">未开通带货权限，请点击<text class="link-text">如何带货?</text></text>
      </view>
      
      <view class="platform-auth">
        <!-- 使用v-for遍历平台列表 -->
        <view v-for="platform in platforms" :key="platform.id" class="platform-item" @click="handlePlatformAuth(platform)">
          <image class="platform-avatar" :src="platform.icon" mode="aspectFill" />
          <view class="platform-info">
            <text class="platform-name">{{ platform.name }}授权</text>
            <view class="badges">
              <text class="platform-badge" :class="{ 'unauthorized': !platform.isAuth }">
                {{ platform.isAuth ? '已授权' : '未授权' }}
              </text>
              <text v-if="platform.isAuth" class="sale-badge" :class="{ 'off': !platform.isSaleEnabled }">
                {{ platform.isSaleEnabled ? '已带货' : '未开通带货' }}
              </text>
            </view>
          </view>
          <view class="platform-user" v-if="platform.isAuth">
            <image class="user-small-avatar" :src="platform.userAvatar || '/static/images/default-avatar.png'" mode="aspectFill" />
            <text class="user-small-name">{{ platform.userName || '已授权用户' }}</text>
          </view>
          <text v-if="platform.status === 0" class="status-tag">未启用</text>
        </view>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-section">
      <view class="logout-btn" @click="logout">
        <text class="logout-text">退出登录</text>
      </view>
    </view>
  </view>
</template>

<script>
import api from '../../utils/api.js'

export default {
  data() {
    return {
      userInfo: {
        avatar: null,
        nick: '',
        teamName: ''
      },
      platformSaleState: {},
      earnings: {
        withdrawable: 0,
        todayEarnings: 0,
        totalEarnings: 0,
        pendingWithdraw: 0
      },
      platforms: [],
      // 团队相关数据
      currentTeam: { id: 0, name: '无团队' },
      showTeamDialog: false,
      teamList: []
    }
  },

  onLoad() {
    // 页面加载时获取数据
    this.loadData();
    this.loadTeam();
  },

  onShow() {
    // 页面显示时也获取数据，确保数据是最新的
    this.loadData();
  },

  methods: {
    // 加载所有数据
    async loadData() {
      try {
        // 显示加载中
        uni.showLoading({ title: '加载中...' });
        
        // 并行请求数据
        const [userInfoRes, earningsRes, platformListRes] = await Promise.all([
          this.loadUserInfo(),
          this.loadEarnings(),
          this.loadPlatformList()
        ]);
        
        // 处理用户信息
        if (userInfoRes && userInfoRes.code === 10000 && userInfoRes.data) {
          this.userInfo = { ...this.userInfo, ...userInfoRes.data };
          this.platformSaleState = userInfoRes.data.platformSaleState || {};
        }
        
        // 处理收益信息
        if (earningsRes && earningsRes.code === 10000 && earningsRes.data) {
          this.earnings = { ...this.earnings, ...earningsRes.data };
        }
        
        // 处理平台授权信息
        if (platformListRes && platformListRes.data && Array.isArray(platformListRes.data)) {
          this.platforms = platformListRes.data.map(platform => ({
            id: platform.id,
            name: platform.name,
            icon: platform.imageUrl || this.getPlatformIcon(platform.id),
            isAuth: platform.isAuth || false,
            status: platform.status,
            userName: platform.userName || '',
            userAvatar: platform.userAvatar || null,
            isSaleEnabled: (this.platformSaleState && this.platformSaleState[platform.id]) === 1
          }));
        }
      } catch (error) {
        console.error('加载数据失败:', error);
        // 加载失败时使用默认平台数据
        this.loadDefaultPlatforms();
      } finally {
        // 隐藏加载中
        uni.hideLoading();
      }
    },

    // 加载用户信息
    async loadUserInfo() {
      try {
        const res = await api.user.info();
        return res;
      } catch (error) {
        console.error('加载用户信息失败:', error);
        return null;
      }
    },

    // 加载收益信息
    async loadEarnings() {
      try {
        const res = await api.user.earnings();
        return res;
      } catch (error) {
        console.error('加载收益信息失败:', error);
        return null;
      }
    },

    // 加载平台列表 - 使用首页相同的接口
    async loadPlatformList() {
      try {
        const res = await api.get('/platform/list');
        return res;
      } catch (error) {
        console.error('加载平台列表失败:', error);
        return null;
      }
    },

    // 根据平台ID返回对应的图标URL
    getPlatformIcon(platformId) {
      // 根据平台ID返回对应的图标URL
      const iconMap = {
        1: '/static/images/douyin-icon.png', // 抖音
        2: '/static/images/kuaishou-icon.png', // 快手
        3: '/static/images/xiaohongshu-icon.png'  // 小红书
      }
      return iconMap[platformId] || '/static/images/default-platform-icon.png'
    },

    // 加载默认平台数据
    loadDefaultPlatforms() {
      // 默认平台数据，确保页面正常显示
      this.platforms = [
        {
          id: 1,
          name: '抖音',
          icon: '/static/images/douyin-icon.png',
          isAuth: false,
          status: 1,
          userName: '',
          userAvatar: null
        },
        {
          id: 2,
          name: '快手',
          icon: '/static/images/kuaishou-icon.png',
          isAuth: false,
          status: 1,
          userName: '',
          userAvatar: null
        },
        {
          id: 3,
          name: '小红书',
          icon: '/static/images/xiaohongshu-icon.png',
          isAuth: false,
          status: 1,
          userName: '',
          userAvatar: null
        }
      ];
    },

    // 加载团队信息
    async loadTeam() {
      try {
        const res = await api.team.myTeam();
        if (res && res.code === 10000 && res.data) {
          this.currentTeam = res.data;
        }
      } catch (e) {
        console.error('获取团队失败:', e);
        uni.showToast({ title: '获取团队信息失败', icon: 'none' });
      }
    },

    // 打开团队选择对话框
    async openTeamDialog() {
      try {
        const res = await api.team.list();
        if (res && res.code === 10000 && res.data) {
          this.teamList = res.data;
          // 显示团队选择弹窗
          uni.showActionSheet({
            itemList: this.teamList.map(team => team.name),
            success: (res) => {
              const selectedTeam = this.teamList[res.tapIndex];
              this.selectTeam(selectedTeam);
            }
          });
        }
      } catch (e) {
        console.error('获取团队列表失败:', e);
        uni.showToast({ title: '获取团队列表失败', icon: 'none' });
      }
    },

    // 选择团队
    async selectTeam(team) {
      try {
        const res = await api.team.changeTeam({ teamId: team.id });
        if (res && res.code === 10000) {
          this.currentTeam = team;
          uni.showToast({ title: '修改团队成功' });
        } else {
          uni.showToast({ title: res ? res.message : '修改团队失败', icon: 'none' });
        }
      } catch (e) {
        console.error('修改团队失败:', e);
        uni.showToast({ title: '修改团队失败', icon: 'none' });
      }
    },

    withdraw() {
      uni.navigateTo({ url: '/pages/withdraw/withdraw' });
    },

    handlePlatformAuth(platform) {
      // 检查平台是否未启用
      if (platform.status === 0) {
        uni.showToast({ title: '该平台未启用', icon: 'none' });
        return;
      }
      
      // 检查平台是否已授权
      if (platform.isAuth) {
        uni.showToast({ title: `${platform.name}已授权`, icon: 'none' });
        return;
      }

      uni.showModal({
        title: '平台授权',
        content: `确认授权${platform.name}吗？`,
        success: (res) => {
          if (res.confirm) {
            uni.navigateTo({ url: `/pages/platform-auth/platform-auth?platform=${platform.id}` });
          }
        }
      });
    },

    logout() {
      uni.showModal({
        title: '退出确认',
        content: '确认退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            uni.removeStorageSync('token');
            uni.removeStorageSync('userInfo');
            uni.reLaunch({ url: '/pages/login/login' });
          }
        }
      });
    },

    // 格式化数字
    formatNumber(num) {
      if (num === null || num === undefined) return '0.00';
      const n = parseFloat(num);
      return n.toLocaleString('zh-CN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      });
    },
    
    // 获取头像文字
    getAvatarText() {
      if (this.userInfo.phone && this.userInfo.phone.length >= 4) {
        return this.userInfo.phone.slice(-4);
      }
      return '用户';
    },

    // 编辑资料
    editProfile() {
      uni.navigateTo({
        url: '/pages/profile/profile'
      });
    },

    // 跳转到财务流水
    goToFinanceFlow() {
      uni.navigateTo({
        url: '/pages/finance-flow/finance-flow'
      });
    }
  }
}
</script>

<style scoped>
.mine-container {
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 40rpx;
}

.user-header {
  background: white;
  padding: 30rpx 20rpx;
  margin-bottom: 20rpx;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  color: white;
  font-size: 24rpx;
  font-weight: bold;
}

.user-details {
  flex: 1;
}

.user-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 10rpx;
}

.team-text {
  font-size: 26rpx;
  color: #666;
}

.switch-team-btn {
  background: #f0f0f0;
  padding: 10rpx 20rpx;
  border-radius: 20rpx;
}

.switch-team-text {
  font-size: 24rpx;
  color: #666;
}

.withdrawable-section {
  background: linear-gradient(135deg, #ff4d4f, #ff7a45);
  margin: 0 20rpx 20rpx;
  border-radius: 15rpx;
  padding: 30rpx;
  position: relative;
}

.withdrawable-label {
  color: rgba(255, 255, 255, 0.9);
  font-size: 28rpx;
  display: block;
  margin-bottom: 10rpx;
}

.withdrawable-amount {
  color: white;
  font-size: 48rpx;
  font-weight: bold;
  display: block;
}

.withdraw-btn {
  position: absolute;
  right: 30rpx;
  top: 50%;
  transform: translateY(-50%);
  background: white;
  color: #ff4d4f;
  padding: 15rpx 40rpx;
  border-radius: 30rpx;
  text-align: center;
  cursor: pointer;
}

.btn-text {
  font-size: 28rpx;
  font-weight: bold;
}

.earnings-section {
  background: white;
  margin: 20rpx;
  border-radius: 20rpx;
  padding: 30rpx;
}

.earnings-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.earnings-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.earnings-link {
  font-size: 28rpx;
  color: #1890ff;
}

.earnings-indicators {
  display: flex;
  justify-content: space-between;
}

.indicator-item {
  flex: 1;
  text-align: center;
}

.indicator-title {
  font-size: 26rpx;
  color: #666;
  display: block;
  margin-bottom: 15rpx;
}

.indicator-value {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.personal-center {
  background: white;
  margin: 0 20rpx 20rpx;
  border-radius: 15rpx;
  padding: 30rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 20rpx;
}

.no-permission-tip {
  background: #fff7e6;
  padding: 20rpx;
  border-radius: 10rpx;
  margin-bottom: 20rpx;
}

.tip-text {
  font-size: 26rpx;
  color: #fa8c16;
}

.link-text {
  color: #1890ff;
}

.platform-auth {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.platform-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #f8f9fa;
  border-radius: 10rpx;
  cursor: pointer;
}

.platform-info {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.platform-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  margin-right: 20rpx;
}

.platform-name {
  font-size: 28rpx;
  color: #333;
  margin-right: 20rpx;
}

.badges {
  display: flex;
  gap: 12rpx;
  align-items: center;
}

.platform-badge {
  background: #f6ffed;
  color: #52c41a;
  padding: 5rpx 20rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
}

.sale-badge {
  background: #e6f7ff;
  color: #1890ff;
  padding: 5rpx 20rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
}

.sale-badge.off {
  background: #fff7e6;
  color: #fa8c16;
}

.platform-badge.unauthorized {
  background: #fff2f0;
  color: #ff4d4f;
}

.user-small-avatar {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  margin-right: 10rpx;
}

.user-small-name {
  font-size: 24rpx;
  color: #666;
}

.logout-section {
  padding: 0 20rpx;
}

.logout-btn {
  background: #ff4d4f;
  color: white;
  padding: 30rpx;
  border-radius: 15rpx;
  text-align: center;
  cursor: pointer;
}

.logout-text {
  font-size: 32rpx;
  font-weight: 500;
}
</style>
