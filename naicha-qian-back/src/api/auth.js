import request from '@/utils/request'

/**
 * 认证相关API
 */

// 用户登录
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 用户注册
export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

// 检查用户名是否存在
export function checkUsername(username, userType) {
  return request({
    url: '/auth/check-username',
    method: 'get',
    params: {
      username,
      userType
    }
  })
}

// 检查邮箱是否存在
export function checkEmail(email) {
  return request({
    url: '/auth/check-email',
    method: 'get',
    params: {
      email
    }
  })
}

// 检查手机号是否存在
export function checkPhone(phone) {
  return request({
    url: '/auth/check-phone',
    method: 'get',
    params: {
      phone
    }
  })
}

// 检查店铺名称是否存在
export function checkShopName(shopName) {
  return request({
    url: '/auth/check-shop-name',
    method: 'get',
    params: {
      shopName
    }
  })
}
