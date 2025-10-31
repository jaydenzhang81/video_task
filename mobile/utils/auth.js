/**
 * 登录状态检查工具函数
 */

/**
 * 检查用户是否已登录
 * @returns {boolean} 是否已登录
 */
export function isLoggedIn() {
  const token = uni.getStorageSync('token');
  return !!token;
}

/**
 * 检查登录状态，如果未登录则跳转到登录页
 * @returns {boolean} 是否已登录
 */
export function checkLoginStatus() {
  if (!isLoggedIn()) {
    uni.reLaunch({
      url: '/pages/login/login'
    });
    return false;
  }
  return true;
}

/**
 * 清除用户登录信息
 */
export function clearLoginInfo() {
  uni.removeStorageSync('token');
  uni.removeStorageSync('userInfo');
}

/**
 * 处理登录失效
 * @param {string} message 提示信息
 */
export function handleLoginExpired(message = '登录已过期，请重新登录') {
  clearLoginInfo();
  
  uni.showToast({
    title: message,
    icon: 'none',
    duration: 2000
  });
  
  setTimeout(() => {
    uni.reLaunch({
      url: '/pages/login/login'
    });
  }, 1000);
}

/**
 * 获取用户信息
 * @returns {Object|null} 用户信息
 */
export function getUserInfo() {
  return uni.getStorageSync('userInfo') || null;
}

/**
 * 保存用户信息
 * @param {Object} userInfo 用户信息
 */
export function saveUserInfo(userInfo) {
  uni.setStorageSync('userInfo', userInfo);
}

/**
 * 保存登录token
 * @param {string} token 登录token
 */
export function saveToken(token) {
  uni.setStorageSync('token', token);
}
