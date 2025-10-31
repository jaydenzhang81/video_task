import { handleLoginExpired } from './auth.js';

const BASE_URL = 'https://shiwu.felix88.cn/api';

// 请求拦截器
const request = (url, options = {}) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token');
    
    uni.request({
      url: BASE_URL + url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'swuser': token ? `${token}` : '',
        ...options.header
      },
      success: (res) => {
        console.log('API请求成功:', url, res.data);
        if (res.statusCode === 200) {
          if (res.data.code === 0 || res.data.code === 10000) {
            console.log('API请求成功，状态码:', res.data.code);
            resolve(res.data);
          } else if (res.data.code === 10401) {
            // 用户未登录，使用统一的处理函数
            console.log('用户未登录，跳转到登录页');
            handleLoginExpired('请先登录');
            reject(res.data);
          } else {
            console.log('API返回错误:', res.data);
            reject(res.data);
          }
        } else {
          console.log('HTTP状态码错误:', res.statusCode);
          reject({
            code: res.statusCode,
            message: '网络请求失败'
          });
        }
      },
      fail: (err) => {
        reject({
          code: -1,
          message: '网络连接失败'
        });
      }
    });
  });
};

// API方法
export const api = {
  // 通用请求方法
  get: (url, params) => request(url, { data: params }),
  post: (url, data) => request(url, { method: 'POST', data }),
  put: (url, data) => request(url, { method: 'PUT', data }),
  delete: (url) => request(url, { method: 'DELETE' }),
  
  // 团队相关
  team: {
    myTeam: () => request('/team/my-team'),
    list: () => request('/team/list'),
    changeTeam: (data) => request('/team/change', { method: 'POST', data })
  },
  
  // 用户相关
  user: {
    register: (data) => request('/user/register', { method: 'POST', data }),
    login: (data) => request('/user/login', { method: 'POST', data }),
    sendCode: (data) => request('/user/sendCode', { method: 'POST', data }),
    sendResetCode: (data) => request('/user/sendResetCode', { method: 'POST', data }),
    resetPassword: (data) => request('/user/resetPassword', { method: 'POST', data }),
    profile: () => request('/user/profile'),
    updateProfile: (data) => request('/user/updateProfile', { method: 'PUT', data }),
    info: () => request('/user/info'),
    earnings: () => request('/user/earnings'),
    withdraw: (data) => request('/user/withdraw', { method: 'POST', data }),
    financeFlow: (params) => request('/user/financeFlow', { data: params }),
    checkWechatAuth: () => request('/user/checkWechatAuth'),
    wechatAuthorize: (data) => request('/user/wechat/authorize', { method: 'POST', data }),
    wechatLogin: (data) => request('/user/wechat/login', { method: 'POST', data })
  },
  
  // 平台相关
  platform: {
    list: () => request('/platform/list')
  },
  
  // 视频相关
  video: {
    types: () => request('/video/types'),
    list: (params) => request('/video/list', { data: params }),
    share: (data) => request('/video/share', { method: 'POST', data }),
    publish: (data) => request('/video/publish', { method: 'POST', data }),
    // 视频数据相关
    stats: (params) => request('/video/stats', { data: params }),
    shareStats: (params) => request('/video/share-stats', { data: params }),
    publishList: (params) => request('/video/publish-list', { data: params }),
    chartData: (params) => request('/video/chart-data', { data: params }),
    // 检查发布限制
    checkPublishLimit: (data) => request('/video/check-publish-limit', { method: 'POST', data })
  },
  
  // 同步相关
  sync: {
    list: () => request('/sync/list'),
    execute: (data) => request('/sync/execute', { method: 'POST', data })
  },
  
  // 奖励相关
  reward: {
    account: () => request('/reward/account'),
    accountLogs: () => request('/reward/account/logs'),
    addBalance: (data) => request('/reward/add-balance', { method: 'POST', data })
  },
  
  // 视频数据同步相关
  videoSync: {
    syncByPhotoId: (photoId) => request('/video-sync/sync-by-photo-id', { method: 'POST', data: { photoId } }),
    syncByUserId: () => request('/video-sync/sync-by-user-id', { method: 'POST' }),
    syncKuaishou: () => request('/video-sync/sync-kuaishou', { method: 'POST' })
  },
  
  // 排行榜相关
  ranking: {
    list: (params) => request('/ranking/list', { data: params }),
    myRank: (params) => request('/ranking/my-rank', { data: params }),
    stats: (params) => request('/ranking/stats', { data: params })
  },
  
  // 微信登录相关
  wechat: {
    login: (data) => request('/wechat/login', { method: 'POST', data }),
    bindPhone: (data) => request('/wechat/bindPhone', { method: 'POST', data }),
    checkUnionid: (params) => request('/wechat/checkUnionid', { data: params }),
    loginByCode: (data) => request('/wechat/loginByCode', { method: 'POST', data })
  }
};

export default api;