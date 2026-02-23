import request from '@/utils/request'

/**
 * 商家管理相关API
 */

// 获取商家信息
export function getMerchantInfo(merchantId) {
  return request({
    url: `/merchant/info`,
    method: 'get',
    params: { merchantId }
  })
}

// 更新店铺信息
export function updateShopInfo(merchantId, data) {
  return request({
    url: `/merchant/shop/info`,
    method: 'put',
    params: { merchantId },
    data
  })
}

// 获取营业时间设置
export function getBusinessHours(merchantId) {
  return request({
    url: `/merchant/business-hours`,
    method: 'get',
    params: { merchantId }
  })
}

// 更新营业时间设置
export function updateBusinessHours(merchantId, data) {
  return request({
    url: `/merchant/business-hours`,
    method: 'put',
    params: { merchantId },
    data
  })
}

// 根据地址获取经纬度
export function getLocationByAddress(address) {
  return request({
    url: `/merchant/location`,
    method: 'get',
    params: { address }
  })
}

