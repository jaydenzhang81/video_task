// 平台SDK工具类
let sdkShortvideo = null

// 安全地获取原生插件
try {
  console.log('正在加载快手SDK插件...')
  sdkShortvideo = uni.requireNativePlugin('Shortvideoplatform-Sdk')
  
  if (sdkShortvideo) {
    console.log('快手SDK插件加载成功:', typeof sdkShortvideo)
    console.log('可用的方法:', Object.keys(sdkShortvideo))
  } else {
    console.error('快手SDK插件加载失败: 插件对象为空')
    sdkShortvideo = null
  }
} catch (error) {
  console.error('快手SDK插件加载失败:', error)
  sdkShortvideo = null
}

class PlatformSDK {
  constructor() {
    this.isAuthorized = false
    this.authInfo = null
  }

  // 检查SDK是否可用
  isSDKAvailable() {
    const available = sdkShortvideo !== null && typeof sdkShortvideo === 'object'
    console.log('SDK可用性检查:', available, 'sdkShortvideo类型:', typeof sdkShortvideo)
    return available
  }

  // 获取SDK状态信息
  getSDKStatus() {
    return {
      sdkLoaded: sdkShortvideo !== null,
      sdkType: typeof sdkShortvideo,
      availableMethods: sdkShortvideo ? Object.keys(sdkShortvideo) : [],
      hasAuthMethod: sdkShortvideo && typeof sdkShortvideo.onKwaiAuth === 'function',
      hasConfigMethod: sdkShortvideo && typeof sdkShortvideo.onSetKwaiOpenSdkConfig === 'function'
    }
  }

  // 初始化Android端快手SDK配置
  initAndroidKuaishouConfig() {
    console.log('开始初始化Android端快手SDK配置...')
    
    if (!this.isSDKAvailable()) {
      console.error('快手SDK插件未正确加载，无法初始化配置')
      return false
    }

    const systemInfo = uni.getSystemInfoSync()
    console.log('系统信息:', systemInfo.platform, systemInfo.system)

    if (systemInfo.platform === 'android') {
      try {
        if (typeof sdkShortvideo.onSetKwaiOpenSdkConfig !== 'function') {
          console.error('onSetKwaiOpenSdkConfig方法不存在')
          return false
        }

        // 快手端设置平台功能的配置选项（仅android有效）
        sdkShortvideo.onSetKwaiOpenSdkConfig({
          goToMargetAppNotInstall: true, // 应用未安装，是否自动跳转应用市场
          goToMargetAppVersionNotSupport: true, // 应用已安装但版本不支持，是否自动跳转应用市场
          setNewTaskFlag: true, // 设置启动功能页面是否使用新的页面栈
          setClearTaskFlag: true, // 设置启动功能页面是否清除当前页面栈，当isSetNewTaskFlag为true时生效
          showDefaultLoading: false, // 是否显示默认的loading页面作为功能启动的过渡
          scope: "user_info,user_video_publish,user_video_info"
        })
        console.log('Android端快手SDK配置初始化完成')
        return true
      } catch (error) {
        console.error('Android端快手SDK配置初始化失败:', error)
        return false
      }
    } else {
      console.log('当前不是Android平台，跳过Android配置初始化')
      return true // 非Android平台不算失败
    }
  }

  // 快手授权登录
  async kuaishouAuth() {
    return new Promise((resolve, reject) => {
      if (!this.isSDKAvailable()) {
        reject({
          success: false,
          error: '快手SDK插件未正确加载，请检查插件配置'
        })
        return
      }

      if (typeof sdkShortvideo.onKwaiAuth !== 'function') {
        reject({
          success: false,
          error: '快手授权方法不存在，请检查SDK版本'
        })
        return
      }

      try {
        sdkShortvideo.onKwaiAuth({
          scope: "user_info,user_video_publish,user_video_info", // 设置授权范围（ios），Android在加载插件地方设置
          state: "kuaishou_auth_" + Date.now() // STATE安全参数，标识和用户或者设备相关的授权请求
        }, (res) => {
          console.log("快手授权结果:", JSON.stringify(res))
          
          if (res.errorCode === 1) {
            // 授权成功
            this.isAuthorized = true
            this.authInfo = {
              code: res.code, // 临时授权码
              state: res.state, // 透传STATE安全参数
              grantedPermissions: res.grantedPermissions
            }
            resolve({
              success: true,
              data: this.authInfo
            })
          } else if (res.errorCode === -1) {
            // 取消授权
            this.isAuthorized = false
            this.authInfo = null
            reject({
              success: false,
              error: '用户取消授权',
              code: res.errorCode,
              state: res.state
            })
          } else {
            // 授权失败
            this.isAuthorized = false
            this.authInfo = null
            reject({
              success: false,
              error: res.errorString || '授权失败',
              code: res.errorCode,
              state: res.state
            })
          }
        })
      } catch (error) {
        console.error('快手授权调用失败:', error)
        reject({
          success: false,
          error: '快手授权调用失败: ' + error.message
        })
      }
    })
  }

  // 快手分享视频
  async kuaishouShareVideo(videoInfo) {
    if (!this.isAuthorized) {
      throw new Error('请先进行快手授权')
    }

    return new Promise((resolve, reject) => {
      // 转换视频路径
      debugger
      const videoPath = plus.io.convertLocalFileSystemURL(videoInfo.url)
      
      sdkShortvideo.onKwaiShareMedia({
        transaction: "SingleVideoPublish", // android: SingleVideoPublish
        mediaFeature: 3, // ios: 3表示视频发布功能
        isChooseImage: false, // 不打开相册选择
        mediaObject: {
          videoAssets: [videoPath] // 视频列表
        },
        tags: [
          videoInfo.title || "精彩视频"
        ], // 话题(可选)
        plcBindInfo: {
          plcMpAppId: "appid", // 小程序id
          plcTitle: videoInfo.title || "分享视频", // 标题
          plcMpPath: "/index/xxx" // 小程序路径
        } // 挂载小程序(可选)
      }, (res) => {
        console.log("快手分享结果:", JSON.stringify(res))
        
        if (res.errorCode === 1) {
          // 成功
          resolve({
            success: true,
            data: res
          })
        } else if (res.errorCode === -1) {
          // 取消
          reject({
            success: false,
            error: '用户取消分享',
            code: res.errorCode
          })
        } else {
          // 失败
          reject({
            success: false,
            error: res.errorString || '分享失败',
            code: res.errorCode
          })
        }
      })
    })
  }

  // 快手分享图片
  async kuaishouShareImage(imageInfo) {
    if (!this.isAuthorized) {
      throw new Error('请先进行快手授权')
    }

    return new Promise((resolve, reject) => {
      // 转换图片路径
      const imagePath = plus.io.convertLocalFileSystemURL(imageInfo.url)
      
      sdkShortvideo.onKwaiShareMedia({
        transaction: "SinglePicturePublish", // android: SinglePicturePublish
        mediaFeature: 2, // ios: 2表示图片编辑功能
        isChooseImage: false, // 不打开相册选择
        mediaObject: {
          imageAssets: [imagePath] // 图片列表
        },
        tags: [
          imageInfo.title || "精彩图片"
        ], // 话题(可选)
        plcBindInfo: {
          plcMpAppId: "appid", // 小程序id
          plcTitle: imageInfo.title || "分享图片", // 标题
          plcMpPath: "/index/xxx" // 小程序路径
        } // 挂载小程序(可选)
      }, (res) => {
        console.log("快手分享图片结果:", JSON.stringify(res))
        
        if (res.errorCode === 1) {
          // 成功
          resolve({
            success: true,
            data: res
          })
        } else if (res.errorCode === -1) {
          // 取消
          reject({
            success: false,
            error: '用户取消分享',
            code: res.errorCode
          })
        } else {
          // 失败
          reject({
            success: false,
            error: res.errorString || '分享失败',
            code: res.errorCode
          })
        }
      })
    })
  }

  // 快手分享多个媒体文件
  async kuaishouShareMultiMedia(mediaInfo) {
    if (!this.isAuthorized) {
      throw new Error('请先进行快手授权')
    }

    return new Promise((resolve, reject) => {
      // 转换媒体文件路径
      const mediaPaths = mediaInfo.urls.map(url => plus.io.convertLocalFileSystemURL(url))
      
      sdkShortvideo.onKwaiShareMedia({
        transaction: "MultiMediaClip", // android: MultiMediaClip
        mediaFeature: 4, // ios: 4表示智能裁剪功能
        isChooseImage: false, // 不打开相册选择
        mediaObject: {
          videoAssets: mediaPaths.filter(path => path.includes('.mp4')), // 视频列表
          imageAssets: mediaPaths.filter(path => !path.includes('.mp4')) // 图片列表
        },
        tags: [
          mediaInfo.title || "精彩内容"
        ], // 话题(可选)
        plcBindInfo: {
          plcMpAppId: "appid", // 小程序id
          plcTitle: mediaInfo.title || "分享内容", // 标题
          plcMpPath: "/index/xxx" // 小程序路径
        } // 挂载小程序(可选)
      }, (res) => {
        console.log("快手分享多个媒体文件结果:", JSON.stringify(res))
        
        if (res.errorCode === 1) {
          // 成功
          resolve({
            success: true,
            data: res
          })
        } else if (res.errorCode === -1) {
          // 取消
          reject({
            success: false,
            error: '用户取消分享',
            code: res.errorCode
          })
        } else {
          // 失败
          reject({
            success: false,
            error: res.errorString || '分享失败',
            code: res.errorCode
          })
        }
      })
    })
  }

  // 抖音授权登录
  async douyinAuth() {
    return new Promise((resolve, reject) => {
      if (!this.isSDKAvailable()) {
        reject({
          success: false,
          error: '抖音SDK插件未正确加载，请检查插件配置'
        })
        return
      }

      if (typeof sdkShortvideo.onAuthorize !== 'function') {
        reject({
          success: false,
          error: '抖音授权方法不存在，请检查SDK版本'
        })
        return
      }

      try {
        sdkShortvideo.onAuthorize({
          scope: "user_info",
          state: "douyin_auth_" + Date.now()
        }, (res) => {
          console.log("抖音授权结果:", JSON.stringify(res))

          if (res.errCode === 0) {
            resolve({
              success: true,
              data: {
                authCode: res.authCode,
                state: res.state,
                grantedPermissions: res.grantedPermissions
              }
            })
          } else if (res.errCode === -2) {
            // 用户手动取消
            reject({
              success: false,
              error: '用户取消授权',
              code: res.errCode,
              state: res.state
            })
          } else {
            reject({
              success: false,
              error: res.errString || '授权失败',
              code: res.errCode,
              state: res.state
            })
          }
        })
      } catch (error) {
        console.error('抖音授权调用失败:', error)
        reject({
          success: false,
          error: '抖音授权调用失败: ' + error.message
        })
      }
    })
  }

  // 抖音分享视频
  async douyinShareVideo(videoInfo, options = {}) {
    return new Promise((resolve, reject) => {
      if (!this.isSDKAvailable()) {
        reject({ success: false, error: '抖音SDK插件未正确加载，请检查插件配置' })
        return
      }
      if (typeof sdkShortvideo.onShare !== 'function') {
        reject({ success: false, error: '抖音分享方法不存在，请检查SDK版本' })
        return
      }

      try {
        const convertPath = (p) => {
          try {
            if (typeof plus !== 'undefined' && plus.io && typeof plus.io.convertLocalFileSystemURL === 'function') {
              return plus.io.convertLocalFileSystemURL(p)
            }
          } catch (e) {}
          return p
        }

        const videoPath = videoInfo && videoInfo.url ? convertPath(videoInfo.url) : ''
        const coverPath = videoInfo && videoInfo.cover ? convertPath(videoInfo.cover) : ''

        const payload = {
          isChooseImage: false,
          shareMediaType: 'ShareMediaTypeVideo',
          shareToPublish: options.shareToPublish === undefined ? true : !!options.shareToPublish,
          state: options.state || ('douyin_share_' + Date.now()),
          hashtags: options.hashtags || [],
          microAppInfo: options.microAppInfo,
          extras: options.extras,
          shareToType: options.shareToType,
          feature: options.feature,
          shareParam: Object.assign({}, options.shareParam || {}, {
            // 如果未提供 titleObject，则兜底使用简单 title/desc
            title: (options.shareParam && options.shareParam.title) || (videoInfo && videoInfo.title) || '分享视频',
            desc: (options.shareParam && options.shareParam.desc) || (videoInfo && videoInfo.videoDesc) || '精彩视频分享',
            videoPath,
            coverPath
          })
        }

        sdkShortvideo.onShare(payload, (res) => {
          console.log('抖音分享结果:', JSON.stringify(res))
          if (res && res.errorCode === 0) {
            resolve({ success: true, data: res })
          } else {
            reject({
              success: false,
              error: (res && (res.errorString || res.errorMsg)) || '分享失败',
              code: res && (res.errorCode || res.subErrorCode)
            })
          }
        })
      } catch (error) {
        reject({ success: false, error: '抖音分享调用失败: ' + error.message })
      }
    })
  }

  // 抖音分享图片/图集
  async douyinShareImages(imagePaths = [], options = {}) {
    return new Promise((resolve, reject) => {
      if (!this.isSDKAvailable()) {
        reject({ success: false, error: '抖音SDK插件未正确加载，请检查插件配置' })
        return
      }
      if (typeof sdkShortvideo.onShare !== 'function') {
        reject({ success: false, error: '抖音分享方法不存在，请检查SDK版本' })
        return
      }

      try {
        const convertPath = (p) => {
          try {
            if (typeof plus !== 'undefined' && plus.io && typeof plus.io.convertLocalFileSystemURL === 'function') {
              return plus.io.convertLocalFileSystemURL(p)
            }
          } catch (e) {}
          return p
        }
        const converted = Array.isArray(imagePaths) ? imagePaths.map(p => convertPath(p)) : []

        const payload = {
          isChooseImage: !!options.isChooseImage, // 仅 iOS 生效
          maxCount: options.maxCount || 12,
          shareMediaType: 'ShareMediaTypeImage',
          imageAlbumMode: options.imageAlbumMode === undefined ? true : !!options.imageAlbumMode,
          state: options.state || ('douyin_share_images_' + Date.now()),
          hashtags: options.hashtags || [],
          microAppInfo: options.microAppInfo,
          extras: options.extras,
          feature: options.feature || 'note',
          shareParam: Object.assign({
            imagePaths: converted
          }, options.shareParam || {})
        }

        sdkShortvideo.onShare(payload, (res) => {
          console.log('抖音图片/图集分享结果:', JSON.stringify(res))
          if (res && res.errorCode === 0) {
            resolve({ success: true, data: res })
          } else {
            reject({
              success: false,
              error: (res && (res.errorString || res.errorMsg)) || '分享失败',
              code: res && (res.errorCode || res.subErrorCode)
            })
          }
        })
      } catch (error) {
        reject({ success: false, error: '抖音分享调用失败: ' + error.message })
      }
    })
  }

  // 抖音通用分享（直接透传payload）
  async douyinShare(payload) {
    return new Promise((resolve, reject) => {
      if (!this.isSDKAvailable()) {
        reject({ success: false, error: '抖音SDK插件未正确加载，请检查插件配置' })
        return
      }
      if (typeof sdkShortvideo.onShare !== 'function') {
        reject({ success: false, error: '抖音分享方法不存在，请检查SDK版本' })
        return
      }
      try {
        sdkShortvideo.onShare(payload, (res) => {
          console.log('抖音通用分享结果:', JSON.stringify(res))
          if (res && res.errorCode === 0) {
            resolve({ success: true, data: res })
          } else {
            reject({
              success: false,
              error: (res && (res.errorString || res.errorMsg)) || '分享失败',
              code: res && (res.errorCode || res.subErrorCode)
            })
          }
        })
      } catch (error) {
        reject({ success: false, error: '抖音分享调用失败: ' + error.message })
      }
    })
  }

  // 保存视频到相册（iOS端使用）
  async saveVideoToPhotosAlbum(videoPath) {
    return new Promise((resolve, reject) => {
      sdkShortvideo.onSaveImageToPhotosAlbum({
        filePath: plus.io.convertLocalFileSystemURL(videoPath),
        type: 'video' // 保存视频
      }, (res) => {
        if (res.path) {
          console.log('保存视频成功：' + res.path)
          resolve({
            success: true,
            path: res.path
          })
        } else {
          console.log('保存视频失败')
          reject({
            success: false,
            error: '保存视频失败'
          })
        }
      })
    })
  }

  // 保存图片到相册（iOS端使用）
  async saveImageToPhotosAlbum(imagePath) {
    return new Promise((resolve, reject) => {
      sdkShortvideo.onSaveImageToPhotosAlbum({
        filePath: plus.io.convertLocalFileSystemURL(imagePath),
        type: 'image' // 保存图片
      }, (res) => {
        if (res.path) {
          console.log('保存图片成功：' + res.path)
          resolve({
            success: true,
            path: res.path
          })
        } else {
          console.log('保存图片失败')
          reject({
            success: false,
            error: '保存图片失败'
          })
        }
      })
    })
  }

  // 保存多个媒体文件到相册（iOS端使用）
  async saveMultiMediaToPhotosAlbum(filePaths) {
    return new Promise((resolve, reject) => {
      const convertedPaths = filePaths.map(path => plus.io.convertLocalFileSystemURL(path))
      
      sdkShortvideo.onSaveMultiMediaToPhotosAlbum({
        filePaths: convertedPaths
      }, (res) => {
        if (res.paths) {
          console.log('保存多个媒体文件成功：' + res.paths)
          resolve({
            success: true,
            paths: res.paths
          })
        } else {
          console.log('保存多个媒体文件失败')
          reject({
            success: false,
            error: '保存多个媒体文件失败'
          })
        }
      })
    })
  }

  // 检查媒体文件是否存在于相册中（iOS端使用）
  checkMultiMediaInPhotosAlbum(localIdentifier) {
    return sdkShortvideo.checkMultiMediaInPhotosAlbum(localIdentifier)
  }

  // 检查平台授权状态
  checkAuthStatus(platformId) {
    // 这里可以根据实际需求检查本地存储的授权信息
    const authKey = `auth_${platformId}`
    const authInfo = uni.getStorageSync(authKey)
    return authInfo && authInfo.isAuthorized
  }

  // 保存授权信息
  saveAuthInfo(platformId, authInfo) {
    const authKey = `auth_${platformId}`
    uni.setStorageSync(authKey, {
      isAuthorized: true,
      authInfo: authInfo,
      timestamp: Date.now()
    })
  }

  // 清除授权信息
  clearAuthInfo(platformId) {
    const authKey = `auth_${platformId}`
    uni.removeStorageSync(authKey)
  }
}

export default new PlatformSDK()
