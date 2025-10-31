<template>
  <view class="video-player-container">
    <!-- 返回按钮 -->
    <view class="back-btn" @click="goBack">
      <text class="back-icon">←</text>
    </view>
    

    
    <!-- 视频播放区域 -->
    <view class="video-section">
      <video 
        :src="videoInfo.url" 
        :poster="videoInfo.cover"
        class="video-player"
        :controls="false"
        :autoplay="false"
        :loop="true"
        :muted="false"
        @play="onVideoPlay"
        @pause="onVideoPause"
        @ended="onVideoEnded"
        @error="onVideoError"
      >
        <cover-view class="video-placeholder" v-if="!videoInfo.url">
          <cover-image class="placeholder-image" :src="videoInfo.cover" />
          <cover-view class="play-button" @click="playVideo">
            <cover-text class="play-icon">▶</cover-text>
          </cover-view>
        </cover-view>
      </video>
    </view>
    
    <!-- 右侧交互按钮栏 -->
    <view class="interaction-bar" style="border: 2rpx solid red;">
      <!-- 用户头像 -->
      <view class="user-avatar-section" @click="goToUserProfile">
        <image class="user-avatar" :src="videoInfo.authorAvatar" />
        <view class="follow-btn">
          <text class="follow-icon">+</text>
        </view>
      </view>
      
      <!-- 点赞按钮 -->
      <view class="action-item" @click="toggleLike">
        <view class="action-icon" :class="{ active: isLiked }">
          <text class="icon-text">{{ isLiked ? '❤️' : '🤍' }}</text>
        </view>
        <text class="action-count">{{ videoInfo.likeCount }}</text>
      </view>
      
      <!-- 评论按钮 -->
      <view class="action-item" @click="showComments">
        <view class="action-icon">
          <text class="icon-text">💬</text>
        </view>
        <text class="action-count">{{ videoInfo.commentCount }}</text>
      </view>
      
      <!-- 收藏按钮 -->
      <view class="action-item" @click="toggleFavorite">
        <view class="action-icon" :class="{ active: isFavorited }">
          <text class="icon-text">{{ isFavorited ? '⭐' : '☆' }}</text>
        </view>
        <text class="action-count">{{ favoriteCount }}</text>
      </view>
      
      <!-- 分享按钮 -->
      <view class="action-item" @click="showShareOptions">
        <view class="action-icon">
          <text class="icon-text">📤</text>
        </view>
        <text class="action-count">{{ shareCount }}</text>
      </view>
    </view>
    
    <!-- 底部信息区域 -->
    <view class="bottom-info">
      <!-- 用户信息 -->
      <view class="user-info">
        <text class="username">@{{ videoInfo.author }}</text>
        <text class="user-emoji">🥔</text>
      </view>
      
      <!-- 视频描述 -->
      <view class="video-description">
        <text class="description-text">{{ videoInfo.videoDesc || '显示视频账号、点赞量、转发量、评论、浏览量' }}</text>
      </view>
      
      <!-- 发布按钮 -->
      <view class="publish-section" v-if="!videoInfo.isShared">
        <text class="publish-title">发布到平台</text>
        <view class="platform-list">
          <view 
            v-for="platform in platforms" 
            :key="platform.id"
            class="platform-item"
            @click="publishToPlatform(platform)"
          >
            <image class="platform-icon" :src="platform.icon" />
            <text class="platform-name">{{ platform.name }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 评论弹窗 -->
    <uni-popup ref="commentPopup" type="bottom">
      <view class="comment-popup">
        <view class="popup-header">
          <text class="popup-title">评论</text>
          <text class="close-btn" @click="closeComments">✕</text>
        </view>
        <view class="comment-list">
          <view class="comment-item" v-for="comment in comments" :key="comment.id">
            <image class="comment-avatar" :src="comment.avatar" />
            <view class="comment-content">
              <text class="comment-author">{{ comment.author }}</text>
              <text class="comment-text">{{ comment.content }}</text>
              <text class="comment-time">{{ comment.time }}</text>
            </view>
          </view>
        </view>
        <view class="comment-input">
          <input 
            class="input-field" 
            placeholder="说点什么..." 
            v-model="commentText"
            @confirm="submitComment"
          />
          <button class="submit-btn" @click="submitComment">发送</button>
        </view>
      </view>
    </uni-popup>
    
    <!-- 分享弹窗 -->
    <uni-popup ref="sharePopup" type="bottom">
      <view class="share-popup">
        <view class="popup-header">
          <text class="popup-title">分享到</text>
          <text class="close-btn" @click="closeShare">✕</text>
        </view>
        <view class="share-options">
          <view class="share-item" @click="shareToWechat">
            <text class="share-icon">💬</text>
            <text class="share-name">微信</text>
          </view>
          <view class="share-item" @click="shareToQQ">
            <text class="share-icon">🐧</text>
            <text class="share-name">QQ</text>
          </view>
          <view class="share-item" @click="shareToWeibo">
            <text class="share-icon">📱</text>
            <text class="share-name">微博</text>
          </view>
          <view class="share-item" @click="copyLink">
            <text class="share-icon">🔗</text>
            <text class="share-name">复制链接</text>
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
import api from '../../utils/api.js'
import platformSDK from '../../utils/platform-sdk.js' // 引入平台SDK

export default {
  data() {
    return {
      videoInfo: {
        id: null,
        title: '',
        url: '',
        cover: '',
        author: '',
        authorAvatar: '',
        viewCount: '0',
        likeCount: '0',
        commentCount: '0',
        videoDesc: '',
        isShared: false
      },
      platforms: [],
      comments: [],
      commentText: '',
      isLiked: false,
      isFavorited: false,
      isPlaying: false,
      favoriteCount: '7',
      shareCount: '1'
    }
  },
  
  onLoad(options) {
    console.log('视频播放页面加载，参数:', options)
    
    // 初始化Android端快手SDK配置
    platformSDK.initAndroidKuaishouConfig()
    
    // 获取视频ID
    const videoId = options.id || options.videoId
    console.log('视频ID:', videoId)
    
    if (videoId) {
      this.loadVideoInfo(videoId)
    } else {
      // 如果没有视频ID，使用默认数据
      console.log('使用默认视频数据')
      this.loadDefaultVideo()
    }
    
    // 加载平台列表
    this.loadPlatforms()
    
    // 加载评论
    this.loadComments()
  },
  
  methods: {
    async loadVideoInfo(videoId) {
      try {
        // 调用后端API获取视频详情
        const res = await api.get(`/video/detail/${videoId}`)
        
        if (res.data) {
          // 使用接口返回的数据
          this.videoInfo = {
            id: res.data.id || videoId,
            title: res.data.title || '未知标题',
            url: res.data.url || '', // 使用接口返回的视频URL
            cover: res.data.cover || 'https://via.placeholder.com/400x800/FF6B35/FFFFFF?text=视频封面',
            author: res.data.author || '系统用户',
            authorAvatar: res.data.authorAvatar || 'https://via.placeholder.com/80x80/FF6B35/FFFFFF?text=头像',
            viewCount: res.data.viewCount ? res.data.viewCount.toString() : '0',
            likeCount: res.data.likeCount ? res.data.likeCount.toString() : '0',
            commentCount: res.data.commentCount ? res.data.commentCount.toString() : '0',
            videoDesc: res.data.videoDesc || '显示视频账号、点赞量、转发量、评论、浏览量',
            isShared: res.data.isShared || false
          }
        } else {
          // 如果接口没有返回数据，使用默认数据
          this.loadDefaultVideo()
        }
        
      } catch (error) {
        console.error('加载视频信息失败:', error)
        
        // 如果接口调用失败，使用默认数据
        this.loadDefaultVideo()
        
        uni.showToast({
          title: '加载视频失败，使用默认数据',
          icon: 'none'
        })
      }
    },
    
    loadDefaultVideo() {
      this.videoInfo = {
        id: 1,
        title: '快!看!美不美~',
        url: 'https://www.w3schools.com/html/mov_bbb.mp4',
        cover: 'https://via.placeholder.com/400x800/FF6B35/FFFFFF?text=日落视频',
        author: '悲伤烤土豆',
        authorAvatar: 'https://via.placeholder.com/80x80/FF6B35/FFFFFF?text=头像',
        viewCount: '3,342',
        likeCount: '61',
        commentCount: '2',
        videoDesc: '显示视频账号、点赞量、转发量、评论、浏览量',
        isShared: false
      }
      console.log('默认视频数据已加载:', this.videoInfo)
    },
    
    async loadPlatforms() {
      try {
        const res = await api.get('/platform/list')
        if (res.data && Array.isArray(res.data)) {
          this.platforms = res.data.map(platform => ({
            id: platform.id,
            name: platform.name,
            icon: platform.imageUrl || this.getPlatformIcon(platform.id)
          }))
        }
      } catch (error) {
        console.error('加载平台列表失败:', error)
        // 使用默认平台数据
        this.platforms = [
          { id: 1, name: '抖音', icon: 'https://via.placeholder.com/60x60/FF0050/FFFFFF?text=抖音' },
          { id: 2, name: '快手', icon: 'https://via.placeholder.com/60x60/FF6600/FFFFFF?text=快手' },
          { id: 3, name: '小红书', icon: 'https://via.placeholder.com/60x60/FF2442/FFFFFF?text=小红书' }
        ]
      }
    },
    
    getPlatformIcon(platformId) {
      const iconMap = {
        1: 'https://via.placeholder.com/60x60/FF0050/FFFFFF?text=抖音', // 抖音
        2: 'https://via.placeholder.com/60x60/FF6600/FFFFFF?text=快手', // 快手
        3: 'https://via.placeholder.com/60x60/FF2442/FFFFFF?text=小红书'  // 小红书
      }
      return iconMap[platformId] || 'https://via.placeholder.com/60x60/999999/FFFFFF?text=平台'
    },
    
    loadComments() {
      // TODO: 加载真实评论数据
      this.comments = []
    },
    
    // 视频播放控制
    playVideo() {
      this.isPlaying = true
    },
    
    onVideoPlay() {
      this.isPlaying = true
    },
    
    onVideoPause() {
      this.isPlaying = false
    },
    
    onVideoEnded() {
      this.isPlaying = false
    },
    
    onVideoError(e) {
      console.error('视频播放错误:', e)
    },
    
    // 交互功能
    toggleLike() {
      console.log('点击点赞按钮')
      this.isLiked = !this.isLiked
      if (this.isLiked) {
        this.videoInfo.likeCount = (parseInt(this.videoInfo.likeCount.replace(',', '')) + 1).toLocaleString()
      } else {
        this.videoInfo.likeCount = (parseInt(this.videoInfo.likeCount.replace(',', '')) - 1).toLocaleString()
      }
      console.log('点赞状态:', this.isLiked, '点赞数:', this.videoInfo.likeCount)
    },
    
    toggleFavorite() {
      this.isFavorited = !this.isFavorited
      if (this.isFavorited) {
        this.favoriteCount = (parseInt(this.favoriteCount) + 1).toString()
      } else {
        this.favoriteCount = (parseInt(this.favoriteCount) - 1).toString()
      }
      uni.showToast({
        title: this.isFavorited ? '已收藏' : '已取消收藏',
        icon: 'success'
      })
    },
    
    showComments() {
      this.$refs.commentPopup.open()
    },
    
    closeComments() {
      this.$refs.commentPopup.close()
    },
    
    submitComment() {
      if (!this.commentText.trim()) {
        uni.showToast({
          title: '请输入评论内容',
          icon: 'none'
        })
        return
      }
      
      // 添加新评论
      const newComment = {
        id: Date.now(),
        author: '我',
        avatar: 'https://via.placeholder.com/60x60/2196F3/FFFFFF?text=我',
        content: this.commentText,
        time: '刚刚'
      }
      
      this.comments.unshift(newComment)
      this.commentText = ''
      
      // 更新评论数
      this.videoInfo.commentCount = (parseInt(this.videoInfo.commentCount) + 1).toString()
      
      uni.showToast({
        title: '评论成功',
        icon: 'success'
      })
    },
    
    showShareOptions() {
      this.$refs.sharePopup.open()
    },
    
    closeShare() {
      this.$refs.sharePopup.close()
    },
    
    shareToWechat() {
      uni.showToast({
        title: '分享到微信',
        icon: 'success'
      })
      this.closeShare()
    },
    
    shareToQQ() {
      uni.showToast({
        title: '分享到QQ',
        icon: 'success'
      })
      this.closeShare()
    },
    
    shareToWeibo() {
      uni.showToast({
        title: '分享到微博',
        icon: 'success'
      })
      this.closeShare()
    },
    
    copyLink() {
      uni.setClipboardData({
        data: `https://example.com/video/${this.videoInfo.id}`,
        success: () => {
          uni.showToast({
            title: '链接已复制',
            icon: 'success'
          })
        }
      })
      this.closeShare()
    },
    
    async publishToPlatform(platform) {
      uni.showLoading({ title: '准备发布...' })
      
      try {
        if (platform.id === 2) {
          // 快手平台 - 使用后端队列发布
          const response = await api.post('/video/share', {
            platformId: platform.id,
            videoId: this.videoInfo.id
          })
          
          uni.hideLoading()
          
          if (response.code === 0 || response.code === 10000) {
            uni.showToast({
              title: `已加入快手发布队列`,
              icon: 'success'
            })
            
            // 更新视频状态
            this.videoInfo.isShared = true
          } else {
            throw new Error(response.message || '发布失败')
          }
          
        } else if (platform.id === 1) {
          // 抖音平台 - 使用前端SDK发布
          const shareResult = await platformSDK.douyinShareVideo(this.videoInfo, {
            shareToPublish: true,
            hashtags: ['精彩视频']
          })
          
          uni.hideLoading()
          
          if (shareResult.success) {
            // 调用后端保存分享记录
            await api.post('/video/share', {
              platformId: platform.id,
              videoId: this.videoInfo.id
            })
            
            uni.showToast({
              title: `已发布到${platform.name}`,
              icon: 'success'
            })
            
            // 更新视频状态
            this.videoInfo.isShared = true
          } else {
            throw new Error(shareResult.error || '发布失败')
          }
        } else {
          throw new Error('不支持的平台')
        }
        
      } catch (error) {
        uni.hideLoading()
        console.error('发布失败:', error)
        
        uni.showToast({
          title: error.error || error.message || '发布失败',
          icon: 'none'
        })
      }
    },
    
    goToUserProfile() {
      uni.showToast({
        title: '跳转到用户主页',
        icon: 'none'
      })
    },
    
    goBack() {
      uni.navigateBack()
    },
    

  }
}
</script>

<style scoped>
.video-player-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  background: #000000;
  overflow: hidden;
}

/* 返回按钮 */
.back-btn {
  position: absolute;
  top: 60rpx;
  left: 30rpx;
  z-index: 100;
  width: 80rpx;
  height: 80rpx;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  color: white;
  font-size: 40rpx;
  font-weight: bold;
}

/* 视频播放区域 */
.video-section {
  position: relative;
  width: 100%;
  height: 100%;
  background: #000000;
}

.video-player {
  width: 100%;
  height: 100%;
  background: #000000;
}

.video-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.play-button {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 120rpx;
  height: 120rpx;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.play-icon {
  color: #333;
  font-size: 48rpx;
}

/* 右侧交互按钮栏 */
.interaction-bar {
  right: 24rpx;
  bottom: 320rpx;
  gap: 32rpx;
}

.user-avatar {
  width: 88rpx;
  height: 88rpx;
  border-width: 2rpx;
}

.follow-btn {
  width: 40rpx;
  height: 40rpx;
  background: rgba(255, 255, 255, 0.2);
}

/* 底部信息区域 */
.bottom-info {
  padding: 32rpx 24rpx 48rpx;
}

.username {
  font-size: 36rpx;
}

.video-description {
  margin-bottom: 24rpx;
}

.description-text {
  font-size: 32rpx;
  line-height: 1.6;
}

/* 平台发布按钮 */
.platform-list {
  gap: 24rpx;
}

.platform-item {
  padding: 16rpx;
  background: rgba(255, 255, 255, 0.15);
}

.platform-icon {
  width: 72rpx;
  height: 72rpx;
}

/* 弹窗样式统一 */
.popup-header {
  padding: 24rpx 32rpx;
}

.popup-title {
  font-size: 36rpx;
}

.close-btn {
  font-size: 32rpx;
  color: #999;
  padding: 10rpx;
}

.comment-list {
  max-height: 400rpx;
  overflow-y: auto;
}

.comment-item {
  display: flex;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.comment-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
}

.comment-content {
  flex: 1;
}

.comment-author {
  font-size: 24rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 8rpx;
}

.comment-text {
  font-size: 24rpx;
  color: #333;
  line-height: 1.4;
  margin-bottom: 8rpx;
}

.comment-time {
  font-size: 20rpx;
  color: #999;
}

.comment-input {
  display: flex;
  gap: 20rpx;
  margin-top: 30rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
}

.input-field {
  flex: 1;
  height: 60rpx;
  border: 1rpx solid #e0e0e0;
  border-radius: 30rpx;
  padding: 0 20rpx;
  font-size: 24rpx;
}

.submit-btn {
  width: 120rpx;
  height: 60rpx;
  background: #FF0050;
  color: white;
  border: none;
  border-radius: 30rpx;
  font-size: 24rpx;
}

.share-options {
  display: flex;
  justify-content: space-around;
  padding: 30rpx 0;
}

.share-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  padding: 20rpx;
  border-radius: 15rpx;
  transition: all 0.3s;
}

.share-item:active {
  background: #f0f0f0;
  transform: scale(0.95);
}

.share-icon {
  font-size: 48rpx;
}

.share-name {
  font-size: 24rpx;
  color: #666;
}
</style>
