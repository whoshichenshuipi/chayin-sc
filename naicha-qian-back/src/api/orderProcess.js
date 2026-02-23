import request from '@/utils/request'

/**
 * 订单处理相关API
 */

// 分页查询待处理订单列表
export function getPendingOrderPage(data) {
  return request({
    url: '/api/order/process/page',
    method: 'post',
    data
  })
}

// 接单处理
export function acceptOrder(data) {
  return request({
    url: '/api/order/process/accept',
    method: 'post',
    data
  })
}

// 开始制作
export function startMaking(data) {
  return request({
    url: '/api/order/process/start-making',
    method: 'post',
    data
  })
}

// 制作完成
export function finishMaking(data) {
  return request({
    url: '/api/order/process/finish-making',
    method: 'post',
    data
  })
}

// 批量处理订单
export function batchProcessOrders(orderIds, processType) {
  return request({
    url: '/api/order/process/batch',
    method: 'post',
    params: { processType },
    data: orderIds
  })
}

// 获取订单处理统计
export function getOrderProcessStatistics() {
  return request({
    url: '/api/order/process/statistics',
    method: 'get'
  })
}

// 根据订单ID查询订单详情
export function getOrderDetail(orderId) {
  return request({
    url: `/api/order/detail/${orderId}`,
    method: 'get'
  })
}
