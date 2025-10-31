import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const api = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 添加token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    const { data } = response
    
    // 如果返回的状态码为200，说明接口请求成功，可以正常拿到数据
    if (response.status === 200) {
      return data
    }
    
    // 否则的话抛出错误
    return Promise.reject(new Error(data.message || 'Error'))
  },
  error => {
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          ElMessage.error('未授权，请重新登录')
          // 清除token并跳转登录页面
          localStorage.removeItem('token')
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('拒绝访问')
          break
        case 404:
          ElMessage.error('请求错误，未找到该资源')
          break
        case 405:
          ElMessage.error('请求方法未允许')
          break
        case 408:
          ElMessage.error('请求超时')
          break
        case 500:
          ElMessage.error('服务器端出错')
          break
        case 501:
          ElMessage.error('网络未实现')
          break
        case 502:
          ElMessage.error('网络错误')
          break
        case 503:
          ElMessage.error('服务不可用')
          break
        case 504:
          ElMessage.error('网络超时')
          break
        case 505:
          ElMessage.error('http版本不支持该请求')
          break
        default:
          ElMessage.error(data.message || '连接错误')
      }
    } else {
      ElMessage.error('连接到服务器失败')
    }
    
    return Promise.reject(error)
  }
)

// 封装GET请求
api.get = (url, params) => {
  return api.request({
    method: 'GET',
    url,
    params
  })
}

// 封装POST请求
api.post = (url, data) => {
  return api.request({
    method: 'POST',
    url,
    data
  })
}

// 封装PUT请求
api.put = (url, data) => {
  return api.request({
    method: 'PUT',
    url,
    data
  })
}

// 封装DELETE请求
api.delete = (url) => {
  return api.request({
    method: 'DELETE',
    url
  })
}

export default api 