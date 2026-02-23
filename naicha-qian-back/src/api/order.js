import request from '@/utils/request'

/**
 * 订单相关API
 */

// 分页查询订单列表
export function getOrderPage(data) {
  return request({
    url: '/api/order/page',
    method: 'post',
    data
  })
}

// 根据订单ID查询订单详情
export function getOrderDetail(orderId) {
  return request({
    url: `/api/order/detail/${orderId}`,
    method: 'get'
  })
}

// 根据订单号查询订单
export function getOrderByOrderNo(orderNo) {
  return request({
    url: `/api/order/search/${orderNo}`,
    method: 'get'
  })
}

// 更新订单状态
export function updateOrderStatus(data) {
  return request({
    url: '/api/order/status',
    method: 'put',
    data
  })
}

// 处理订单
export function processOrder(orderId) {
  return request({
    url: `/api/order/process/${orderId}`,
    method: 'put'
  })
}

// 发货订单
export function shipOrder(orderId) {
  return request({
    url: `/api/order/ship/${orderId}`,
    method: 'put'
  })
}

// 完成订单
export function completeOrder(orderId) {
  return request({
    url: `/api/order/complete/${orderId}`,
    method: 'put'
  })
}

// 取消订单
export function cancelOrder(orderId) {
  return request({
    url: `/api/order/cancel/${orderId}`,
    method: 'put'
  })
}

// 获取订单统计信息
export function getOrderStatistics() {
  return request({
    url: '/api/order/statistics',
    method: 'get'
  })
}
