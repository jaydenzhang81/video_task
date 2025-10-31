<template>
  <view class="profile-container">
    <!-- 头部导航 -->
    <view class="header">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <text class="header-title">编辑资料</text>
    </view>

    <!-- 编辑表单 -->
    <view class="form-container">
      <!-- 头像 -->
      <view class="form-item" @click="chooseAvatar">
        <text class="form-label">头像</text>
        <view class="avatar-section">
          <image 
            v-if="formData.avatar" 
            class="avatar-image" 
            :src="formData.avatar" 
            mode="aspectFill"
          />
          <view v-else class="avatar-placeholder">
            <text class="avatar-text">{{ getAvatarText() }}</text>
          </view>
          <view class="edit-icon">
            <text class="edit-text">✏️</text>
          </view>
        </view>
      </view>

      <!-- 昵称 -->
      <view class="form-item" @click="editNickname">
        <text class="form-label">昵称</text>
        <view class="form-value">
          <text class="value-text">{{ formData.nick || '未设置' }}</text>
          <text class="arrow">></text>
        </view>
      </view>

      <!-- 性别 -->
      <view class="form-item" @click="chooseGender">
        <text class="form-label">性别</text>
        <view class="form-value">
          <text class="value-text">{{ getGenderText(formData.gender) }}</text>
          <text class="arrow">></text>
        </view>
      </view>

      <!-- 地区 -->
      <view class="form-item" @click="chooseRegion">
        <text class="form-label">地区</text>
        <view class="form-value">
          <text class="value-text">{{ formData.region || '未设置' }}</text>
          <text class="arrow">></text>
        </view>
      </view>
    </view>

    <!-- 保存按钮 -->
    <view class="save-section">
      <button class="save-btn" @click="saveProfile" :disabled="saving">
        {{ saving ? '保存中...' : '保存' }}
      </button>
    </view>

    <!-- 昵称编辑弹窗 -->
    <view v-if="showNicknameModal" class="modal-overlay" @click="closeNicknameModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">编辑昵称</text>
          <view class="modal-close" @click="closeNicknameModal">
            <text class="close-text">×</text>
          </view>
        </view>
        <view class="modal-body">
          <input 
            class="nickname-input" 
            v-model="tempNickname" 
            placeholder="请输入昵称"
            maxlength="20"
          />
        </view>
        <view class="modal-footer">
          <button class="modal-btn cancel-btn" @click="closeNicknameModal">取消</button>
          <button class="modal-btn confirm-btn" @click="confirmNickname">确定</button>
        </view>
      </view>
    </view>

    <!-- 性别选择弹窗 -->
    <view v-if="showGenderModal" class="modal-overlay" @click="closeGenderModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">选择性别</text>
          <view class="modal-close" @click="closeGenderModal">
            <text class="close-text">×</text>
          </view>
        </view>
        <view class="modal-body">
          <view class="gender-options">
            <view 
              class="gender-option" 
              :class="{ active: tempGender === 1 }"
              @click="tempGender = 1"
            >
              <text class="gender-text">男</text>
            </view>
            <view 
              class="gender-option" 
              :class="{ active: tempGender === 2 }"
              @click="tempGender = 2"
            >
              <text class="gender-text">女</text>
            </view>
            <view 
              class="gender-option" 
              :class="{ active: tempGender === 0 }"
              @click="tempGender = 0"
            >
              <text class="gender-text">保密</text>
            </view>
          </view>
        </view>
        <view class="modal-footer">
          <button class="modal-btn cancel-btn" @click="closeGenderModal">取消</button>
          <button class="modal-btn confirm-btn" @click="confirmGender">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import api from '../../utils/api.js'

export default {
  data() {
    return {
      formData: {
        avatar: '',
        nick: '',
        gender: 0, // 0保密，1男，2女
        region: ''
      },
      saving: false,
      showNicknameModal: false,
      showGenderModal: false,
      tempNickname: '',
      tempGender: 0
    }
  },

  onLoad() {
    this.loadUserInfo()
  },

  methods: {
    // 加载用户信息
    async loadUserInfo() {
      try {
        const res = await api.user.info()
        if (res.data) {
          this.formData = {
            avatar: res.data.avatar || '',
            nick: res.data.nick || '',
            gender: res.data.gender || 0,
            region: res.data.region || ''
          }
        }
      } catch (error) {
        console.error('加载用户信息失败:', error)
        uni.showToast({
          title: '加载用户信息失败',
          icon: 'none'
        })
      }
    },

    // 返回上一页
    goBack() {
      uni.navigateBack()
    },

    // 选择头像
    chooseAvatar() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          const tempFilePath = res.tempFilePaths[0]
          this.uploadAvatar(tempFilePath)
        },
        fail: (error) => {
          console.error('选择图片失败:', error)
          uni.showToast({
            title: '选择图片失败',
            icon: 'none'
          })
        }
      })
    },

    // 上传头像
    async uploadAvatar(filePath) {
      try {
        uni.showLoading({ title: '上传中...' })
        
        // 这里需要实现文件上传接口
        // 暂时直接设置本地路径
        this.formData.avatar = filePath
        
        uni.hideLoading()
        uni.showToast({
          title: '头像更新成功',
          icon: 'success'
        })
      } catch (error) {
        uni.hideLoading()
        console.error('上传头像失败:', error)
        uni.showToast({
          title: '上传头像失败',
          icon: 'none'
        })
      }
    },

    // 编辑昵称
    editNickname() {
      this.tempNickname = this.formData.nick
      this.showNicknameModal = true
    },

    // 确认昵称
    confirmNickname() {
      if (!this.tempNickname.trim()) {
        uni.showToast({
          title: '昵称不能为空',
          icon: 'none'
        })
        return
      }
      this.formData.nick = this.tempNickname.trim()
      this.closeNicknameModal()
    },

    // 关闭昵称弹窗
    closeNicknameModal() {
      this.showNicknameModal = false
      this.tempNickname = ''
    },

    // 选择性别
    chooseGender() {
      this.tempGender = this.formData.gender
      this.showGenderModal = true
    },

    // 确认性别
    confirmGender() {
      this.formData.gender = this.tempGender
      this.closeGenderModal()
    },

    // 关闭性别弹窗
    closeGenderModal() {
      this.showGenderModal = false
    },

    // 选择地区
    chooseRegion() {
      // 这里可以集成地区选择组件
      uni.showToast({
        title: '地区选择功能待开发',
        icon: 'none'
      })
    },

    // 获取性别文本
    getGenderText(gender) {
      const genderMap = {
        0: '保密',
        1: '男',
        2: '女'
      }
      return genderMap[gender] || '保密'
    },

    // 获取头像文字
    getAvatarText() {
      if (this.formData.nick && this.formData.nick.length > 0) {
        return this.formData.nick.slice(-1)
      }
      return '用'
    },

    // 保存资料
    async saveProfile() {
      if (this.saving) return

      try {
        this.saving = true
        uni.showLoading({ title: '保存中...' })

        const updateData = {
          nick: this.formData.nick,
          gender: this.formData.gender,
          region: this.formData.region
        }

        // 如果有新头像，也一起上传
        if (this.formData.avatar && !this.formData.avatar.startsWith('http')) {
          updateData.avatar = this.formData.avatar
        }

        const res = await api.user.updateProfile(updateData)
        
        uni.hideLoading()
        
        if (res.code === 0 || res.code === 10000) {
          uni.showToast({
            title: '保存成功',
            icon: 'success'
          })
          
          // 延迟返回，让用户看到成功提示
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } else {
          uni.showToast({
            title: res.message || '保存失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('保存资料失败:', error)
        uni.showToast({
          title: '保存失败，请重试',
          icon: 'none'
        })
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style scoped>
.profile-container {
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

.form-container {
  background: white;
  margin-top: 20rpx;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 30rpx 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.form-item:last-child {
  border-bottom: none;
}

.form-label {
  font-size: 28rpx;
  color: #333;
  width: 120rpx;
}

.avatar-section {
  display: flex;
  align-items: center;
  position: relative;
}

.avatar-image {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
}

.avatar-placeholder {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  color: white;
  font-size: 32rpx;
  font-weight: bold;
}

.edit-icon {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 32rpx;
  height: 32rpx;
  background: #1890ff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid white;
}

.edit-text {
  font-size: 16rpx;
}

.form-value {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.value-text {
  font-size: 28rpx;
  color: #666;
}

.arrow {
  font-size: 24rpx;
  color: #ccc;
}

.save-section {
  padding: 40rpx 20rpx;
}

.save-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
  color: white;
  border: none;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: bold;
}

.save-btn:disabled {
  background: #ccc;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 600rpx;
  background: white;
  border-radius: 20rpx;
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.modal-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.modal-close {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-text {
  font-size: 40rpx;
  color: #999;
}

.modal-body {
  padding: 30rpx;
}

.nickname-input {
  width: 100%;
  height: 80rpx;
  border: 1rpx solid #ddd;
  border-radius: 10rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #333;
}

.gender-options {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.gender-option {
  padding: 20rpx;
  border: 1rpx solid #ddd;
  border-radius: 10rpx;
  text-align: center;
}

.gender-option.active {
  background: #e6f7ff;
  border-color: #1890ff;
}

.gender-text {
  font-size: 28rpx;
  color: #333;
}

.gender-option.active .gender-text {
  color: #1890ff;
  font-weight: bold;
}

.modal-footer {
  display: flex;
  border-top: 1rpx solid #f0f0f0;
}

.modal-btn {
  flex: 1;
  height: 88rpx;
  border: none;
  font-size: 28rpx;
  background: white;
}

.cancel-btn {
  color: #666;
  border-right: 1rpx solid #f0f0f0;
}

.confirm-btn {
  color: #1890ff;
  font-weight: bold;
}
</style>