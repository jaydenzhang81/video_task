// 本地存储工具类
class Storage {
  // 设置存储
  static set(key, value) {
    try {
      uni.setStorageSync(key, value)
    } catch (e) {
      console.error('Storage set error:', e)
    }
  }
  
  // 获取存储
  static get(key, defaultValue = null) {
    try {
      const value = uni.getStorageSync(key)
      return value || defaultValue
    } catch (e) {
      console.error('Storage get error:', e)
      return defaultValue
    }
  }
  
  // 移除存储
  static remove(key) {
    try {
      uni.removeStorageSync(key)
    } catch (e) {
      console.error('Storage remove error:', e)
    }
  }
  
  // 清空所有存储
  static clear() {
    try {
      uni.clearStorageSync()
    } catch (e) {
      console.error('Storage clear error:', e)
    }
  }
  
  // 检查是否有某个key
  static has(key) {
    try {
      const value = uni.getStorageSync(key)
      return value !== '' && value !== null && value !== undefined
    } catch (e) {
      return false
    }
  }
  
  // 获取存储大小
  static getInfo() {
    try {
      return uni.getStorageInfoSync()
    } catch (e) {
      console.error('Storage getInfo error:', e)
      return {}
    }
  }
}

// 用户相关存储
export const UserStorage = {
  // 保存用户信息
  setUserInfo(userInfo) {
    Storage.set('userInfo', userInfo)
  },
  
  // 获取用户信息
  getUserInfo() {
    return Storage.get('userInfo', {})
  },
  
  // 保存token
  setToken(token) {
    Storage.set('token', token)
  },
  
  // 获取token
  getToken() {
    return Storage.get('token', '')
  },
  
  // 检查是否已登录
  isLoggedIn() {
    return Storage.has('token')
  },
  
  // 清除用户数据
  clearUserData() {
    Storage.remove('userInfo')
    Storage.remove('token')
  }
}

// 应用设置存储
export const AppStorage = {
  // 保存应用设置
  setSettings(settings) {
    Storage.set('appSettings', settings)
  },
  
  // 获取应用设置
  getSettings() {
    return Storage.get('appSettings', {
      theme: 'light',
      language: 'zh-CN',
      autoPlay: true
    })
  },
  
  // 保存平台选择
  setSelectedPlatform(platform) {
    Storage.set('selectedPlatform', platform)
  },
  
  // 获取平台选择
  getSelectedPlatform() {
    return Storage.get('selectedPlatform', null)
  }
}

// 缓存相关存储
export const CacheStorage = {
  // 保存缓存数据
  setCache(key, data, expireTime = 3600000) { // 默认1小时过期
    const cacheData = {
      data,
      expireTime: Date.now() + expireTime
    }
    Storage.set(`cache_${key}`, cacheData)
  },
  
  // 获取缓存数据
  getCache(key) {
    const cacheData = Storage.get(`cache_${key}`)
    if (!cacheData) return null
    
    if (Date.now() > cacheData.expireTime) {
      Storage.remove(`cache_${key}`)
      return null
    }
    
    return cacheData.data
  },
  
  // 清除缓存
  clearCache(key) {
    if (key) {
      Storage.remove(`cache_${key}`)
    } else {
      // 清除所有缓存
      const info = Storage.getInfo()
      if (info.keys) {
        info.keys.forEach(k => {
          if (k.startsWith('cache_')) {
            Storage.remove(k)
          }
        })
      }
    }
  }
}

export default Storage
