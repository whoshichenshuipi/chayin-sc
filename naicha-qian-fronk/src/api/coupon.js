import request from '@/utils/request'

/**
 * 优惠券相关API（用户端）
 */

/**
 * 分页查询优惠券列表（可领取的优惠券）
 * @param {Object} params - 查询参数
 * @param {Number} params.current - 当前页，默认1
 * @param {Number} params.size - 每页大小，默认10
 * @param {String} params.status - 状态：active-进行中（可选）
 * @returns {Promise} 分页数据，包含 records 数组
 */
export function getCouponPage(params = {}) {
  return request({
    url: '/api/coupon/page',
    method: 'post',
    data: {
      pageNum: params.pageNum || params.current || params.page || 1,
      pageSize: params.pageSize || params.size || 10,
      status: params.status,
      name: params.name
    },
    needMerchantId: false, // 用户端不需要商户ID
    silent404: true // 404时不显示错误消息
  }).catch(error => {
    // 如果接口不存在，返回空列表
    if (error.response?.status === 404) {
      return { records: [], total: 0, current: params.pageNum || params.current || 1, size: params.pageSize || params.size || 10 }
    }
    throw error
  })
}

/**
 * 获取优惠券详情
 * @param {Number} couponId - 优惠券ID
 * @returns {Promise} 优惠券详情
 */
export function getCouponDetail(couponId) {
  return request({
    url: `/api/coupon/detail/${couponId}`,
    method: 'get',
    silent404: true // 404时不显示错误消息
  }).catch(error => {
    // 如果接口不存在，返回null
    if (error.response?.status === 404) {
      return null
    }
    throw error
  })
}

/**
 * 领取优惠券
 * 注意：此接口可能尚未实现，如后端不支持，请后端添加
 * @param {Number} couponId - 优惠券ID
 * @returns {Promise} 领取结果
 */
export function receiveCoupon(couponId) {
  return request({
    url: `/api/coupon/receive/${couponId}`,
    method: 'post',
    needMerchantId: false // 用户端不需要商户ID
  })
}

/**
 * 获取我的优惠券列表
 * 注意：此接口可能尚未实现，如后端不支持，请后端添加
 * @param {Object} params - 查询参数
 * @param {Number} params.current - 当前页，默认1
 * @param {Number} params.size - 每页大小，默认10
 * @param {String} params.status - 状态：unused-未使用, used-已使用, expired-已过期（可选）
 * @returns {Promise} 分页数据，包含 records 数组
 */
export function getMyCoupons(params = {}) {
  return request({
    url: '/api/coupon/my',
    method: 'post',
    data: {
      pageNum: params.pageNum || params.current || params.page || 1,
      pageSize: params.pageSize || params.size || 10,
      status: params.status
    },
    needMerchantId: false,
    silent404: true // 404时不显示错误消息
  }).catch(error => {
    // 如果接口不存在，返回空列表
    if (error.response?.status === 404) {
      return { records: [], total: 0, current: params.pageNum || params.current || 1, size: params.pageSize || params.size || 10 }
    }
    throw error
  })
}

