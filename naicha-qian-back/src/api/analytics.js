import request from '@/utils/request'

/**
 * 数据分析相关API
 */

// 获取核心数据概览
export function getCoreData() {
  return request({
    url: '/api/analytics/core-data',
    method: 'get'
  })
}

// 获取订单量趋势（近7天）
export function getOrderTrend(days = 7) {
  return request({
    url: '/api/analytics/order-trend',
    method: 'get',
    params: { days }
  })
}

// 获取销售额趋势（近30天）
export function getSalesTrend(days = 30) {
  return request({
    url: '/api/analytics/sales-trend',
    method: 'get',
    params: { days }
  })
}

// 获取商品销量排行
export function getProductRanking(params) {
  return request({
    url: '/api/analytics/product-ranking',
    method: 'post',
    data: params
  })
}

// 获取热门商品
export function getHotProducts(params) {
  return request({
    url: '/api/analytics/hot-products',
    method: 'post',
    data: params
  })
}

// 获取滞销商品
export function getSlowProducts(params) {
  return request({
    url: '/api/analytics/slow-products',
    method: 'post',
    data: params
  })
}

// 获取消费者画像数据
export function getCustomerProfile() {
  return request({
    url: '/api/analytics/customer-profile',
    method: 'get'
  })
}

// 获取消费频次分析
export function getConsumptionFrequency() {
  return request({
    url: '/api/analytics/consumption-frequency',
    method: 'get'
  })
}

// 获取消费偏好分析
export function getConsumptionPreference() {
  return request({
    url: '/api/analytics/consumption-preference',
    method: 'get'
  })
}

// 获取地域分布分析
export function getRegionDistribution() {
  return request({
    url: '/api/analytics/region-distribution',
    method: 'get'
  })
}

// 分页查询客户详情列表
export function getCustomerDetailList(params) {
  return request({
    url: '/api/analytics/customer-detail',
    method: 'post',
    data: params
  })
}

// 导出核心数据
export function exportCoreData(data) {
  return request({
    url: '/api/analytics/export/core-data',
    method: 'post',
    data,
    responseType: 'blob'
  })
}

// 导出商品数据
export function exportProductData(data) {
  return request({
    url: '/api/analytics/export/product-data',
    method: 'post',
    data,
    responseType: 'blob'
  })
}

// 导出消费者数据
export function exportCustomerData(data) {
  return request({
    url: '/api/analytics/export/customer-data',
    method: 'post',
    data,
    responseType: 'blob'
  })
}

// 导出所有经营数据
export function exportAllData(data) {
  return request({
    url: '/api/analytics/export/all-data',
    method: 'post',
    data,
    responseType: 'blob'
  })
}

