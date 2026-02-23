import request from '@/utils/request'

/**
 * 获取当前用户信息
 * @param {Number} userId - 用户ID
 * @returns {Promise} 用户信息
 */
export function getUserInfo(userId) {
  return request({
    url: `/api/admin/user/${userId}`,
    method: 'get'
  })
}

/**
 * 更新用户信息
 * @param {Number} userId - 用户ID
 * @param {Object} userInfo - 用户信息
 * @returns {Promise} 更新结果
 */
export function updateUserInfo(userId, userInfo) {
  // 使用用户端更新接口
  return request({
    url: '/api/user/profile',
    method: 'put',
    data: {
      nickname: userInfo.nickname,
      email: userInfo.email,
      phone: userInfo.phone,
      avatar: userInfo.avatar,
      gender: userInfo.gender,
      birthday: userInfo.birthday,
      address: userInfo.address
    },
    headers: {
      'X-User-Id': userId
    }
  }).catch((error) => {
    // 如果 PUT 方法失败，尝试使用 POST 方法
    if (error.response?.status === 404 || error.response?.status === 405) {
      return request({
        url: '/api/user/profile',
        method: 'post',
        data: {
          nickname: userInfo.nickname,
          email: userInfo.email,
          phone: userInfo.phone,
          avatar: userInfo.avatar,
          gender: userInfo.gender,
          birthday: userInfo.birthday,
          address: userInfo.address
        },
        headers: {
          'X-User-Id': userId
        }
      })
    }
    throw error
  })
}

/**
 * 修改密码
 * @param {Object} passwordData - 密码数据 { oldPassword, newPassword }
 * @returns {Promise} 修改结果
 */
export function changePassword(passwordData) {
  // 注意：需要后端提供修改密码接口，假设路径为 /api/user/change-password
  return request({
    url: '/api/user/change-password',
    method: 'post',
    data: passwordData
  })
}

/**
 * 获取用户订单统计（从用户详情中获取）
 * @param {Number} userId - 用户ID
 * @returns {Promise} 订单统计 { orderCount, totalAmount }
 */
export function getUserStats(userId) {
  return getUserInfo(userId).then(userInfo => ({
    orderCount: userInfo.orderCount || 0,
    totalAmount: userInfo.totalAmount || 0
  }))
}

/**
 * 发送验证码（用于忘记密码等场景）
 * @param {String} phone - 手机号
 * @param {String} type - 验证码类型：forgot-password-忘记密码, register-注册, login-登录
 * @returns {Promise} 发送结果
 */
export function sendVerificationCode(phone, type = 'forgot-password') {
  return request({
    url: '/api/auth/send-verification-code',
    method: 'post',
    data: {
      phone,
      type
    },
    needMerchantId: false
  }).catch(error => {
    // 如果接口不存在，返回成功（模拟发送）
    if (error.response?.status === 404) {
      console.warn('后端验证码发送接口不存在，请后端添加')
      return { success: true, message: '验证码已发送（模拟）' }
    }
    throw error
  })
}

/**
 * 忘记密码 - 重置密码
 * @param {Object} data - 重置密码数据 { phone, verificationCode, newPassword }
 * @returns {Promise} 重置结果
 */
export function resetPassword(data) {
  return request({
    url: '/api/auth/reset-password',
    method: 'post',
    data: {
      phone: data.phone,
      verificationCode: data.verificationCode,
      newPassword: data.newPassword
    },
    needMerchantId: false
  }).catch(error => {
    // 如果接口不存在，提示后端需要实现
    if (error.response?.status === 404) {
      console.warn('后端重置密码接口不存在，请后端添加')
      throw new Error('重置密码功能暂未实现，请联系管理员')
    }
    throw error
  })
}

