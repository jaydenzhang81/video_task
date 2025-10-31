<template>
  <view class="container">
    
    <!-- 未登录状态显示微信登录 -->
    <view v-if="!isLoggedIn" class="login-prompt">
      <view class="login-content">
        <image class="login-logo" src="/static/images/index.png" mode="aspectFill"></image>
        <text class="login-title">欢迎使用精彩视频</text>
        <text class="login-subtitle">登录后查看更多精彩内容</text>
        
        <view class="login-buttons">
          <button class="wechat-login-btn" @click="handleWechatLogin">
            <text class="wechat-icon">🔗</text>
            微信一键登录
          </button>
          <button class="account-login-btn" @click="goToLogin">
            账号密码登录
          </button>
        </view>
      </view>
    </view>
    
    <!-- 已登录状态显示正常内容 -->
    <view v-if="isLoggedIn">
      <!-- Banner区域 -->
      <view class="banner-container">
        <image class="banner-image" src="/static/images/index.png" mode="aspectFill"></image>
        <view class="banner-content">
          <text class="banner-title">精彩视频推荐</text>
          <text class="banner-subtitle">发现更多优质内容</text>
        </view>
      </view>

    <!-- 平台筛选区域 -->
    <scroll-view class="platform-scroll" scroll-x="true" show-scrollbar="false">
      <view class="platform-list">
        <!-- 平台选项 -->
        <view
            v-for="platform in platforms"
            :key="platform.id"
            class="platform-item"
            :class="{ 
              active: selectedPlatform === platform.id, 
              disabled: platform.status === 0 
            }"
            @click="handlePlatformClick(platform)"
        >
          <image class="platform-icon" :src="platform.icon"/>
          <text class="platform-name">{{ platform.name }}</text>
          <text v-if="platform.status === 0" class="status-tag">未启用</text>
        </view>
      </view>
    </scroll-view>

    <!-- 视频分类区域 - 改为横向滚动 -->
    <scroll-view class="category-scroll" scroll-x="true" show-scrollbar="false">
      <view class="category-list">
        <view
            v-for="type in videoTypes"
            :key="type.id"
            class="category-item"
            :class="{ active: activeCategory === type.id }"
            @click="activeCategory = type.id"
        >
          <text class="category-text">{{ type.name }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- 视频网格列表 -->
    <view class="video-grid">
      <view
          v-for="video in filteredVideos"
          :key="video.id"
          class="video-card"
      >
        <view class="video-thumbnail" @click="playVideo(video)">
          <image class="thumbnail-image" :src="video.cover"/>
          <view class="play-button">
            <text class="play-icon">▶</text>
          </view>
          <view class="video-badge">{{ video.reward }}￥</view>
        </view>

        <view class="video-info">
          <text class="video-title">{{ video.title }}</text>

          <view class="video-stats">
            <view class="stat-item">
              <text class="stat-icon">👁</text>
              <text class="stat-value">{{ video.viewCount }}</text>
            </view>
            <view class="stat-item">
              <text class="stat-icon">👍</text>
              <text class="stat-value">{{ video.likeCount }}</text>
            </view>
            <view class="stat-item">
              <text class="stat-icon">💬</text>
              <text class="stat-value">{{ video.commentCount }}</text>
            </view>
          </view>

          <!-- 发布按钮/无权限提示区域：外层不再限制canShowShareButton，内部分别控制按钮或提示显示 -->
          <view class="share-buttons" v-if="!video.isShared && !video.isPending && selectedPlatform">
            <button
                v-if="canShowShareButton(video)"
                class="share-btn"
                @click.stop="shareVideo(video)"
            >
              <text class="btn-icon">💰</text>
              发布视频
            </button>

            <!-- 分享中状态 -->
            <view v-if="video.isPending" class="pending-status">
              <text class="pending-icon">⏳</text>
              <text class="pending-text">发布中...</text>
            </view>

            <!-- 已发布状态 -->
            <view v-if="video.isShared" class="shared-status">
              <text class="shared-icon">✅</text>
              <text class="shared-text">发布成功</text>
            </view>

            <!-- 平台无带货发布权限提示 - 仅带货视频显示 -->
            <view v-if="isBusinessVideo(video) && !canPublishBusinessOnPlatform(selectedPlatform)" class="non-business-tip">
              <text class="tip-icon">🔒</text>
              <text class="tip-text">请升级账号</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 加载状态 -->
    <view class="loading-section" v-if="loading && videos.length === 0">
      <text>加载中...</text>
    </view>

    <!-- 加载更多 -->
    <view class="load-more" v-if="hasMore && videos.length > 0">
      <button class="load-more-btn" @click="loadMore" :disabled="loading">
        {{ loading ? '加载中...' : '加载更多' }}
      </button>
    </view>

    <!-- 无更多数据 -->
    <view class="no-more" v-if="!hasMore && videos.length > 0">
      <text class="no-more-text">没有更多数据了</text>
    </view>
    
    </view><!-- 已登录状态结束 -->

  </view>
</template>

<script>
import api from '../../utils/api.js'
import {checkLoginStatus} from '../../utils/auth.js'
import platformSDK from '../../utils/platform-sdk.js'

export default {
  data() {
    return {
      platforms: [],
      videoTypes: [],
      videos: [],
      selectedPlatform: null,
      loading: false,
      hasMore: true,
      page: 1,
      pageSize: 10,
      userInfo: null, // 用户信息
      isBusinessUser: false, // 是否为带货用户（废弃：改为按平台判断）
      platformSaleState: {}, // 平台带货发布状态：{ [platformId]: status }
      activeCategory: '', // 当前选中的分类，初始为空
      isLoggedIn: false // 登录状态
    }
  },
  computed: {
    // 根据选中的分类筛选视频
    filteredVideos() {
      if (!this.activeCategory) {
        return this.videos
      } else {
        return this.videos.filter(video => video.videoType === this.activeCategory)
      }
    }
  },

  onLoad() {
    this.checkLoginStatus()
  },

  onShow() {
    // 每次显示页面时检查登录状态
    this.checkLoginStatus()
  },

  onPullDownRefresh() {
    this.loadData().then(() => {
      uni.stopPullDownRefresh()
    })
  },

  // 触底加载更多
  onReachBottom() {
    if (this.hasMore && !this.loading) {
      this.loadMore()
    } else if (!this.hasMore && !this.loading) {
      // 没有更多数据时显示提示
      uni.showToast({
        title: '没有更多数据了',
        icon: 'none'
      })
    }
  },

  methods: {
    checkLoginStatus() {
      // 使用统一的登录状态检查函数
      this.isLoggedIn = checkLoginStatus();
      if (this.isLoggedIn) {
        this.loadData()
        return true;
      }
      return false;
    },

    async loadData() {
      this.loading = true
      this.page = 1 // 重置页码
      this.hasMore = true // 重置hasMore状态

      try {
        // 加载平台列表
        const platformRes = await api.get('/platform/list')

        if (platformRes.data && Array.isArray(platformRes.data)) {
          // 转换平台数据格式
          this.platforms = platformRes.data.map(platform => ({
            id: platform.id,
            name: platform.name,
            icon: platform.imageUrl || this.getPlatformIcon(platform.id), // 优先使用接口返回的图片URL
            isAuth: platform.isAuth || false,
            status: platform.status // 保留status字段
          }))

          // 默认选中第一个平台
          if (this.platforms.length > 0) {
            this.selectedPlatform = this.platforms[0].id
          }
        } else {
          this.platforms = []
        }

        // 加载视频类型
        const typeRes = await api.get('/video/types')

        if (typeRes.data && Array.isArray(typeRes.data)) {
          this.videoTypes = typeRes.data
          // 如果有视频类型，设置第一个为默认选中
          if (this.videoTypes.length > 0) {
            this.activeCategory = this.videoTypes[0].id
          }
        } else {
          this.videoTypes = []
        }

        // 加载用户信息
        await this.loadUserInfo()

        // 加载视频列表 - 默认加载所有视频
        await this.loadVideos()

      } catch (error) {
        console.error('加载数据失败:', error)

        // 加载失败，显示错误信息
        uni.showToast({
          title: '加载失败，请检查网络连接',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },

    // 加载更多视频
    async loadMore() {
      if (this.loading || !this.hasMore) return

      this.page++
      await this.loadVideos()
    },

    async loadUserInfo() {
      try {
        const userRes = await api.user.info()
        if (userRes.data) {
          this.userInfo = userRes.data
          // 保存平台带货发布状态
          this.platformSaleState = userRes.data.platformSaleState || {}
          // 兼容旧逻辑：保留布尔但不再使用
          this.isBusinessUser = userRes.data.userType === 1
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    },

    loadDefaultData() {
      // 空数据，正式开发模式
      this.platforms = []
      this.videoTypes = []
      this.videos = []
    },

    async loadVideos() {
      if (this.loading) return

      this.loading = true

      // 重置分页状态
      this.page = 1;
      this.hasMore = true;

      try {
        // 如果有选中平台，只加载该平台的视频；否则加载所有平台的视频
        if (this.selectedPlatform) {
          // 加载选中平台的视频
          await this.loadVideosByPlatform();
        } else {
          // 加载所有平台的视频
          await this.loadAllVideos();
        }
      } catch (error) {
        console.error('加载视频失败:', error)
        uni.showToast({
          title: '加载视频失败，请重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },

    async loadVideosByPlatform() {
      // 为每个视频类型分别加载视频
      const allVideos = []
      let hasMoreData = false; // 用于判断是否还有更多数据

      for (const type of this.videoTypes) {
        const params = {
          num: this.pageSize,
          page: this.page, // 添加分页参数
          videoType: type.id, // 使用新的按分类获取功能
          platformId: this.selectedPlatform // 添加平台参数
        }

        console.log(`加载${type.name}分类视频，参数:`, params)

        // 使用正确的API调用方式
        const res = await api.get('/video/list', params)

        if (res.data && Array.isArray(res.data)) {
          // 转换后端数据格式为前端期望的格式
          const typeVideos = res.data.map(video => ({
            id: video.id,
            title: video.title || '未知标题',
            videoType: video.videoType,
            cover: video.cover || '/static/images/default-video-cover.jpg',
            author: video.author || '系统用户',
            authorAvatar: video.authorAvatar || '/static/images/default-avatar.png',
            viewCount: video.viewCount ? video.viewCount.toString() : '0',
            likeCount: video.likeCount ? video.likeCount.toString() : '0',
            commentCount: video.commentCount ? video.commentCount.toString() : '0',
            shareCount: video.shareCount ? video.shareCount.toString() : '0',
            favoriteCount: video.favoriteCount ? video.favoriteCount.toString() : '0',
            badge: video.badge || '1x',
            duration: video.duration || '00:30',
            isShared: video.isShared || false,
            isPending: video.isPending || false,
            // 添加后端特有的字段
            videoDesc: video.videoDesc,
            url: video.url,
            status: video.status,
            reward: video.reward || 0,
            ctime: video.ctime,
            utime: video.utime
          }))

          if (this.page === 1) {
            allVideos.push(...typeVideos)
          } else {
            // 对于分页加载，需要将新数据追加到现有视频列表中
            // 保存现有视频数据
            const existingVideos = [...this.videos];
            // 追加新数据
            this.videos = [...existingVideos, ...typeVideos];
          }

          // 只要有一个分类还有数据，就认为还有更多数据
          if (typeVideos.length >= this.pageSize) {
            hasMoreData = true;
          }
        }
      }

      if (this.page === 1) {
        this.videos = allVideos
      }

      // 更新hasMore状态
      this.hasMore = hasMoreData;
    },

    async loadAllVideos() {
      // 为每个视频类型分别加载视频
      const allVideos = []
      let hasMoreData = false; // 用于判断是否还有更多数据

      for (const type of this.videoTypes) {
        const params = {
          num: this.pageSize,
          page: this.page, // 添加分页参数
          videoType: type.id // 使用新的按分类获取功能
        }

        console.log(`加载${type.name}分类视频，参数:`, params)

        // 使用正确的API调用方式
        const res = await api.get('/video/list', params)

        if (res.data && Array.isArray(res.data)) {
          // 转换后端数据格式为前端期望的格式
          const typeVideos = res.data.map(video => ({
            id: video.id,
            title: video.title || '未知标题',
            videoType: video.videoType,
            cover: video.cover || '/static/images/default-video-cover.jpg',
            author: video.author || '系统用户',
            authorAvatar: video.authorAvatar || '/static/images/default-avatar.png',
            viewCount: video.viewCount ? video.viewCount.toString() : '0',
            likeCount: video.likeCount ? video.likeCount.toString() : '0',
            commentCount: video.commentCount ? video.commentCount.toString() : '0',
            shareCount: video.shareCount ? video.shareCount.toString() : '0',
            favoriteCount: video.favoriteCount ? video.favoriteCount.toString() : '0',
            badge: video.badge || '1x',
            duration: video.duration || '00:30',
            isShared: video.isShared || false,
            isPending: video.isPending || false,
            // 添加后端特有的字段
            videoDesc: video.videoDesc,
            url: video.url,
            status: video.status,
            reward: video.reward || 0,
            ctime: video.ctime,
            utime: video.utime
          }))

          allVideos.push(...typeVideos);

          // 只要有一个分类还有数据，就认为还有更多数据
          if (typeVideos.length >= this.pageSize) {
            hasMoreData = true;
          }
        }
      }

      if (this.page === 1) {
        this.videos = allVideos;
      } else {
        // 对于分页加载，需要将新数据追加到现有视频列表中
        this.videos = [...this.videos, ...allVideos];
      }

      // 更新hasMore状态
      this.hasMore = hasMoreData;
    },

    handlePlatformClick(platform) {
      // 处理"全部"选项
      if (platform === null) {
        if (this.selectedPlatform !== null) {
          this.selectedPlatform = null
          this.loadVideos()
        }
        return
      }

      // 检查平台是否未启用
      if (platform.status === 0) {
        uni.showToast({
          title: '该平台未启用',
          icon: 'none'
        })
        return
      }

      // 处理具体平台选项
      if (!platform.isAuth) {
        uni.showModal({
          title: '未授权',
          content: `检测到您未授权${platform.name}，是否前往授权？`,
          confirmText: '去授权',
          cancelText: '取消',
          success: (res) => {
            if (res.confirm) {
              this.goToPlatformAuth()
            }
          }
        })
        return
      }

      // 如果之前选择的平台不同，重新加载视频
      if (this.selectedPlatform !== platform.id) {
        this.selectedPlatform = platform.id
        this.loadVideos()
      }
    },

    selectPlatform(platformId) {
      this.selectedPlatform = platformId
      this.loadVideos()
    },


    getPlatformIcon(platformId) {
      // 根据平台ID返回对应的图标URL
      const iconMap = {
        1: '/static/images/douyin-icon.png', // 抖音
        2: '/static/images/kuaishou-icon.png', // 快手
        3: '/static/images/xiaohongshu-icon.png'  // 小红书
      }
      return iconMap[platformId] || '/static/images/default-platform-icon.png'
    },

    /**
     * 判断是否为带货视频
     * @param {Object} video 视频对象
     * @returns {boolean} 是否为带货视频
     */
    isBusinessVideo(video) {
      // 根据视频类型判断是否为带货视频
      // 这里假设视频类型ID为1的是带货视频，您可以根据实际情况调整
      return video.videoType === 1
    },

    /**
     * 判断当前平台是否允许发布带货视频
     */
    canPublishBusinessOnPlatform(platformId) {
      if (!platformId) return false
      const state = this.platformSaleState && this.platformSaleState[platformId]
      return state === 1
    },

    /**
     * 判断是否显示发布按钮
     */
    canShowShareButton(video) {
      if (this.isBusinessVideo(video)) {
        return this.canPublishBusinessOnPlatform(this.selectedPlatform)
      }
      return true
    },

    playVideo(video) {
      // 跳转到视频播放页面
      uni.navigateTo({
        url: `/pages/video-player/video-player?id=${video.id}`
      })
    },

    async shareVideo(video) {
      // 带货视频需校验当前平台权限
      if (this.isBusinessVideo(video) && !this.canPublishBusinessOnPlatform(this.selectedPlatform)) {
        uni.showModal({
          title: '权限不足',
          content: '您不是带货用户，无法发布带货视频。请联系客服升级为带货用户。',
          showCancel: false,
          confirmText: '我知道了'
        })
        return
      }

      if (!this.selectedPlatform) {
        uni.showToast({
          title: '请先选择平台',
          icon: 'none'
        })
        return
      }

      // 检查平台授权状态
      const platform = this.platforms.find(p => p.id === this.selectedPlatform)
      if (!platform || !platform.isAuth) {
        uni.showModal({
          title: '未授权',
          content: `请先授权${platform ? platform.name : '该平台'}`,
          confirmText: '去授权',
          cancelText: '取消',
          success: (res) => {
            if (res.confirm) {
              this.goToPlatformAuth()
            }
          }
        })
        return
      }

      // 检查发布限制
      try {
        const limitResponse = await api.video.checkPublishLimit({
          platformId: this.selectedPlatform
        })
        
        if (!limitResponse.data || !limitResponse.data.canPublish) {
          const publishLimit = limitResponse.data && limitResponse.data.publishNum ? limitResponse.data.publishNum : 5
          uni.showModal({
            title: '发布失败',
            content: `今日已达该平台发布上限（${publishLimit}个），请明天再试`,
            showCancel: false
          })
          return
        }
      } catch (error) {
        console.error('检查发布限制失败:', error)
        // 检查失败时，显示错误信息，但允许继续发布（降级处理）
        if (error.message) {
          uni.showToast({
            title: error.message,
            icon: 'none'
          })
        }
      }

      // 根据平台选择发布方式
      if (this.selectedPlatform === 2) {
        // 快手平台 - 使用后端队列发布
        await this.shareWithBackend(video)
      } else if (this.selectedPlatform === 1) {
        // 抖音平台 - 使用前端SDK发布
        await this.shareWithSDK(video)
      } else {
        uni.showToast({
          title: '暂不支持该平台发布',
          icon: 'none'
        })
      }
    },

    // 使用后端发布视频（快手平台）
    async shareWithBackend(video) {
      uni.showLoading({title: '准备发布...'})

      try {
        // 调用后端发布接口
        const response = await api.video.share({
          platformId: this.selectedPlatform,
          videoId: video.id
        })

        uni.hideLoading()

        if (response.code === 0 || response.code === 10000) {
          // 发布成功，但状态为0（分享中）
          uni.showToast({
            title: '已提交发布任务',
            icon: 'success'
          })

          // 更新视频状态为分享中
          this.updateVideoShareStatus(video.id, 'pending')

          // 重新加载视频列表
          this.loadVideos()

        } else {
          // 发布失败
          uni.showModal({
            title: '发布失败',
            content: response.message || '发布失败，请重试',
            showCancel: false
          })
        }

      } catch (error) {
        uni.hideLoading()
        console.error('快手后端发布错误:', error)

        let errorMessage = '发布失败，请重试'
        if (error.message) {
          errorMessage = error.message
        } else if (error.code) {
          errorMessage = `错误码: ${error.code}`
        }

        uni.showModal({
          title: '发布失败',
          content: errorMessage,
          showCancel: false
        })
      }
    },


    // 更新视频分享状态
    updateVideoShareStatus(videoId, status) {
      const video = this.videos.find(v => v.id === videoId)
      if (video) {
        if (status === 'pending') {
          video.isPending = true
          video.isShared = false
        } else if (status === true) {
          video.isShared = true
          video.isPending = false
        } else {
          video.isShared = false
          video.isPending = false
        }
      }
    },


    goToPlatformAuth() {
      uni.navigateTo({
        url: '/pages/platform-auth/platform-auth'
      })
    },

    /**
     * 处理未授权平台点击事件
     * @param {Object} platform - 未授权的平台对象
     */




    // 使用前端SDK直接发布（仅抖音平台）
    async shareWithSDK(video) {
      uni.showLoading({title: '准备发布...'})

      try {
        const platform = this.platforms.find(p => p.id === this.selectedPlatform)

        if (this.selectedPlatform === 1) {
          // 抖音平台 - 使用前端SDK发布
          const shareResult = await platformSDK.douyinShareVideo(video, {
            shareToPublish: true,
            hashtags: ['精彩视频']
          })

          uni.hideLoading()

          if (shareResult.success) {
            uni.showToast({
              title: `${platform.name}发布成功`,
              icon: 'success'
            })

            // 更新视频状态
            this.updateVideoShareStatus(video.id, true)
          } else {
            throw new Error(shareResult.error || '发布失败')
          }
        } else {
          throw new Error('当前平台不支持前端SDK发布')
        }

      } catch (error) {
        uni.hideLoading()
        console.error('前端SDK发布失败:', error)

        let errorMessage = '发布失败'
        if (error.error) {
          errorMessage = error.error
        } else if (error.message) {
          errorMessage = error.message
        } else if (typeof error === 'string') {
          errorMessage = error
        }

        uni.showModal({
          title: '发布失败',
          content: errorMessage,
          showCancel: false
        })
      }
    },

    // 微信登录处理
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
      uni.showLoading({ title: '微信登录中...' });
      
      // 先获取微信用户信息
      uni.getUserInfo({
        provider: 'weixin',
        success: (infoRes) => {
          console.log('获取微信用户信息成功:', infoRes);
          this.processWechatLogin(infoRes.userInfo);
        },
        fail: (err) => {
          uni.hideLoading();
          console.error('获取微信用户信息失败:', err);
          
          // 如果获取用户信息失败，直接进行登录
          this.processWechatLogin({});
        }
      });
    },

    // 小程序端微信登录
    wechatMiniProgramLogin() {
      uni.showLoading({ title: '微信登录中...' });
      
      // 小程序端直接进行登录
      this.processWechatLogin({});
    },

    // 处理微信登录
    processWechatLogin(userInfo) {
      // 获取微信登录code
      uni.login({
        provider: 'weixin',
        success: (loginRes) => {
          console.log('微信登录成功:', loginRes);
          uni.hideLoading();
          
          // 构建微信用户信息
          const wechatData = {
            code: loginRes.code,
            unionid: loginRes.unionid || '',
            openId: loginRes.openid || '',
            nickName: userInfo.nickName || '',
            headImgUrl: userInfo.avatarUrl || '',
            sex: userInfo.gender || 0,
            country: userInfo.country || '',
            province: userInfo.province || '',
            city: userInfo.city || ''
          };
          
          // 跳转到微信登录页面处理后续逻辑
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
          uni.hideLoading();
          console.error('微信登录失败:', err);
          uni.showToast({
            title: '微信登录失败',
            icon: 'none'
          });
        }
      });
    },

    // 跳转到账号登录页面
    goToLogin() {
      uni.navigateTo({
        url: '/pages/login/login'
      });
    }

  }
}
</script>

<style scoped>
.container {
  padding: 0;
  background-color: #f5f5f5;
}

/* 登录提示页面样式 */
.login-prompt {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}

.login-content {
  background: white;
  border-radius: 20rpx;
  padding: 60rpx 40rpx;
  text-align: center;
  box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 500rpx;
}

.login-logo {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  margin-bottom: 40rpx;
}

.login-title {
  font-size: 48rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 20rpx;
}

.login-subtitle {
  font-size: 28rpx;
  color: #666;
  display: block;
  margin-bottom: 60rpx;
}

.login-buttons {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.wechat-login-btn {
  background: linear-gradient(135deg, #07c160 0%, #00d976 100%);
  color: white;
  border: none;
  border-radius: 50rpx;
  height: 80rpx;
  font-size: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6rpx 20rpx rgba(7, 193, 96, 0.3);
}

.wechat-login-btn:active {
  transform: scale(0.95);
}

.wechat-icon {
  margin-right: 15rpx;
  font-size: 36rpx;
}

.account-login-btn {
  background: transparent;
  color: #667eea;
  border: 2rpx solid #667eea;
  border-radius: 50rpx;
  height: 80rpx;
  font-size: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.account-login-btn:active {
  background: rgba(102, 126, 234, 0.1);
}

/* Banner样式 */
.banner-container {
  position: relative;
  width: 100%;
  height: 200rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
}

.banner-image {
  width: 100%;
  height: 100%;
}

.banner-content {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 20rpx;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
  color: white;
}

.banner-title {
  font-size: 36rpx;
  font-weight: bold;
  display: block;
}

.banner-subtitle {
  font-size: 24rpx;
  opacity: 0.9;
  display: block;
  margin-top: 8rpx;
}

/* 顶部标题区域 */
.top-header {
  text-align: center;
  padding: 30rpx 0;
  background-color: #fff;
  border-bottom: 1rpx solid #eee;
}

.page-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

/* 平台筛选区域样式 */
.platform-scroll {
  background-color: #fff;
  padding: 20rpx 0;
  margin-bottom: 10rpx;
}

.platform-list {
  display: flex;
  padding: 0 20rpx;
  justify-content: center;
}

.platform-item {
  display: flex;
  align-items: center;
  padding: 15rpx 30rpx;
  margin: 0 10rpx;
  border-radius: 30rpx;
  transition: all 0.3s;
  cursor: pointer;
  background-color: #fff;
  border: 1rpx solid #eee;
  position: relative;
}

.platform-item.disabled {
  opacity: 0.5;
  background-color: #f5f5f5;
}

.status-tag {
  position: absolute;
  top: -5rpx;
  right: -10rpx;
  background-color: #ff9800;
  color: white;
  font-size: 16rpx;
  padding: 2rpx 8rpx;
  border-radius: 10rpx;
  transform: scale(0.8);
}

.platform-item.active {
  background-color: #FF6B6B;
  color: white;
  border-color: #FF6B6B;
}

.platform-icon {
  width: 32rpx;
  height: 32rpx;
  margin-right: 10rpx;
  display: block;
  border-radius: 50%;
}

.platform-name {
  font-size: 28rpx;
  color: #333;
  white-space: nowrap;
}

.platform-item.active .platform-name {
  color: white;
}

/* 分类滚动区域 */
.category-scroll {
  background-color: #fff;
  padding: 15rpx 20rpx;
  margin-bottom: 20rpx;
}

.category-list {
  display: flex;
  justify-content: center;
  min-width: 100%;
}

.category-item {
  padding: 10rpx 25rpx;
  margin: 0 10rpx;
  position: relative;
  transition: all 0.3s;
}

.category-item.active {
  color: #FF6B6B;
}

.category-text {
  font-size: 30rpx;
  color: #666;
  font-weight: 500;
}

.category-item.active .category-text {
  color: #FF6B6B;
  font-weight: 600;
}

/* 选中状态的下划线 */
.category-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40rpx;
  height: 6rpx;
  background-color: #FF6B6B;
  border-radius: 3rpx;
}

/* 视频网格布局 */
.video-grid {
  padding: 20rpx;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  box-sizing: border-box;
}

.video-card {
  width: calc(50% - 10rpx);
  margin-bottom: 20rpx;
  background-color: #fff;
  border-radius: 10rpx;
  padding: 20rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  box-sizing: border-box;
}

.video-thumbnail {
  position: relative;
  width: 100%;
  padding-top: 100%;
  border-radius: 10rpx;
  overflow: hidden;
  height: 140rpx
}

.thumbnail-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.play-button {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 48rpx;
  height: 48rpx;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.play-icon {
  color: white;
  font-size: 30rpx;
}

.video-badge {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  background: rgba(255, 0, 0, 0.8);
  color: white;
  padding: 5rpx 10rpx;
  border-radius: 15rpx;
  font-size: 24rpx;
}

.video-duration {
  position: absolute;
  bottom: 10rpx;
  right: 10rpx;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 5rpx 10rpx;
  border-radius: 5rpx;
  font-size: 20rpx;
}

.video-info {
  margin-top: 15rpx;
}

.video-title {
  font-size: 28rpx;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 10rpx;
  line-height: 1.4;
}

.video-author {
  display: flex;
  align-items: center;
  margin-bottom: 15rpx;
}

.author-avatar {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  margin-right: 10rpx;
}

.author-name {
  font-size: 24rpx;
  color: #666;
}

.video-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10rpx;
}

.stat-item {
  display: flex;
  align-items: center;
}

.stat-icon {
  font-size: 18rpx;
  margin-right: 3rpx;
}

.stat-value {
  font-size: 18rpx;
  color: #666;
}


.kuaishou-btn {
  background: linear-gradient(135deg, #FF6600, #FF8C00);
  color: white;
}

.douyin-btn {
  background: linear-gradient(135deg, #FF0050, #FF1E90);
  color: white;
}

.default-btn {
  background: #667eea;
  color: white;
}

.btn-icon {
  margin-right: 8rpx;
  font-size: 20rpx;
}

.pending-status {
  align-items: center;
  justify-content: center;
  margin-top: 10rpx;
  padding: 8rpx 16rpx;
  background: #fff3e0;
  border: 1px solid #ffcc80;
}

.pending-icon {
  margin-right: 8rpx;
  font-size: 20rpx;
}

.pending-text {
  font-size: 22rpx;
  color: #ff9800;
}

.shared-status {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 10rpx;
  padding: 8rpx 16rpx;
  background: #f0f9ff;
  border-radius: 20rpx;
  border: 1px solid #e0f2fe;
}

.shared-icon {
  margin-right: 8rpx;
  font-size: 20rpx;
}

.shared-text {
  font-size: 22rpx;
  color: #4CAF50;
}

.non-business-tip {
  display: flex;
  width: 200rpx;
  height: 50rpx;
  align-items: center;
  justify-content: center;
  margin-top: 10rpx;
  padding: 8rpx 16rpx;
  background: #fff8e1;
  border-radius: 20rpx;
  border: 1px solid #ffcc02;
}

.tip-icon {
  margin-right: 8rpx;
  font-size: 20rpx;
}

.tip-text {
  font-size: 22rpx;
  color: #ff9800;
}

.loading-section {
  text-align: center;
  padding: 40rpx;
  color: #666;
}


/* 发布按钮样式 */
.share-buttons {
  margin-top: 20rpx;
}

.share-btn {
  width: 200rpx;
  height: 50rpx;
  background: linear-gradient(135deg, #FF6B6B, #FF8E8E);
  color: white;
  border: none;
  border-radius: 35rpx;
  font-size: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.share-btn:active {
  transform: scale(0.95);
}

.btn-icon {
  margin-right: 8rpx;
  font-size: 20rpx;
}

/* 状态样式 */
.pending-status,
.shared-status,
.non-business-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 180rpx;
  height: 36rpx;
  border-radius: 35rpx;
  font-size: 28rpx;
  margin: auto;
}

.pending-status {
  background: #fff3e0;
  border: 1px solid #ffcc80;
}

.pending-icon {
  margin-right: 8rpx;
  font-size: 20rpx;
}

.pending-text {
  font-size: 24rpx;
  color: #ff9800;
}

.shared-status {
  background: #f0f9ff;
  border: 1px solid #e0f2fe;
}

.shared-icon {
  margin-right: 8rpx;
  font-size: 20rpx;
}

.shared-text {
  font-size: 24rpx;
  color: #4CAF50;
}

.non-business-tip {
  background: #fff8e1;
  border: 1px solid #ffcc02;
}

.tip-icon {
  margin-right: 8rpx;
  font-size: 20rpx;
}

.tip-text {
  font-size: 24rpx;
  color: #ff9800;
}

/* 加载状态 */
.loading-section {
  text-align: center;
  padding: 40rpx;
  color: #666;
}

/* 加载更多相关样式 */
.load-more {
  display: flex;
  justify-content: center;
  margin: 40rpx 0;
}

.load-more-btn {
  width: 50%;
  height: 50rpx;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 25rpx;
  font-size: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.load-more-btn:disabled {
  background: #ccc;
}

.no-more {
  text-align: center;
  padding: 40rpx;
  color: #666;
}
</style>