import { UserStorage } from './storage.js'

// 请求配置
const config = {
  baseURL: '',
  timeout: 10000,
  header: {
    'Content-Type': 'application/json'
  }
}

// 请求拦截器
const requestInterceptor = (options) => {
  // 添加token
  const token = UserStorage.getToken()
  if (token) {
    options.header = {
      ...options.header,
      'Authorization': `Bearer ${token}`
    }
  }
  
  // 添加baseURL
  if (!options.url.startsWith('http')) {
    options.url = config.baseURL + options.url
  }
  
  // 添加超时时间
  if (!options.timeout) {
    options.timeout = config.timeout
  }
  
  return options
}

// 响应拦截器
const responseInterceptor = (response) => {
  const { statusCode, data } = response
  
  // 请求成功
  if (statusCode === 200) {
    // 业务成功
    if (data.code === 10000) {
      return data
    }
    
    // 业务失败
    const error = new Error(data.message || '请求失败')
    error.code = data.code
    error.data = data
    throw error
  }
  
  // HTTP错误
  const error = new Error(`HTTP ${statusCode}`)
  error.statusCode = statusCode
  error.data = data
  throw error
}

// 错误处理
const errorHandler = (error) => {
  console.error('Request error:', error)
  
  // 401 未授权，跳转登录
  if (error.statusCode === 401 || error.code === 10002) {
    UserStorage.clearUserData()
    uni.reLaunch({
      url: '/pages/login/login'
    })
    return
  }
  
  // 网络错误
  if (error.errMsg && error.errMsg.includes('request:fail')) {
    uni.showToast({
      title: '网络连接失败',
      icon: 'none'
    })
    return
  }
  
  // 其他错误
  uni.showToast({
    title: error.message || '请求失败',
    icon: 'none'
  })
}

// 请求方法
const request = (options) => {
  return new Promise((resolve, reject) => {
    // 请求拦截
    const interceptedOptions = requestInterceptor(options)
    
    uni.request({
      ...interceptedOptions,
      success: (response) => {
        try {
          const result = responseInterceptor(response)
          resolve(result)
        } catch (error) {
          errorHandler(error)
          reject(error)
        }
      },
      fail: (error) => {
        errorHandler(error)
        reject(error)
      }
    })
  })
}

// 封装常用请求方法
export const http = {
  // GET请求
  get(url, data = {}, options = {}) {
    return request({
      url,
      method: 'GET',
      data,
      ...options
    })
  },
  
  // POST请求
  post(url, data = {}, options = {}) {
    return request({
      url,
      method: 'POST',
      data,
      ...options
    })
  },
  
  // PUT请求
  put(url, data = {}, options = {}) {
    return request({
      url,
      method: 'PUT',
      data,
      ...options
    })
  },
  
  // DELETE请求
  delete(url, data = {}, options = {}) {
    return request({
      url,
      method: 'DELETE',
      data,
      ...options
    })
  },
  
  // 文件上传
  upload(url, filePath, name = 'file', formData = {}, options = {}) {
    return new Promise((resolve, reject) => {
      const token = UserStorage.getToken()
      const header = {
        'Authorization': token ? `Bearer ${token}` : ''
      }
      
      uni.uploadFile({
        url: config.baseURL + url,
        filePath,
        name,
        formData,
        header,
        success: (response) => {
          try {
            const data = JSON.parse(response.data)
            if (data.code === 10000) {
              resolve(data)
            } else {
              const error = new Error(data.message || '上传失败')
              error.code = data.code
              reject(error)
            }
          } catch (e) {
            reject(new Error('响应解析失败'))
          }
        },
        fail: (error) => {
          reject(error)
        }
      })
    })
  }
}

export default http
