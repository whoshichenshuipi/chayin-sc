import request from '@/utils/request'

/**
 * 优惠券管理相关API
 */

// 创建优惠券
export function createCoupon(data) {
  return request({
    url: '/api/coupon/create',
    method: 'post',
    data
  })
}

// 更新优惠券
export function updateCoupon(couponId, data) {
  return request({
    url: `/api/coupon/update/${couponId}`,
    method: 'put',
    data
  })
}

// 删除优惠券
export function deleteCoupon(couponId) {
  return request({
    url: `/api/coupon/delete/${couponId}`,
    method: 'delete'
  })
}

// 查询优惠券详情
export function getCouponDetail(couponId) {
  return request({
    url: `/api/coupon/detail/${couponId}`,
    method: 'get'
  })
}

// 分页查询优惠券列表
export function getCouponList(params) {
  return request({
    url: '/api/coupon/page',
    method: 'post',
    data: params
  })
}

// 暂停/恢复优惠券
export function togglePauseCoupon(couponId) {
  return request({
    url: `/api/coupon/toggle-pause/${couponId}`,
    method: 'put'
  })
}

