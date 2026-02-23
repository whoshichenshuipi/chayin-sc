import request from '@/utils/request'

/**
 * 订单配送相关API
 */

// 分页查询订单配送列表
export function getShipmentPage(data) {
  return request({
    url: '/api/order/shipment/page',
    method: 'post',
    data
  })
}

// 根据订单ID查询配送信息
export function getShipmentDetail(orderId) {
  return request({
    url: `/api/order/shipment/detail/${orderId}`,
    method: 'get'
  })
}

// 根据订单号查询配送信息
export function getShipmentByOrderNo(orderNo) {
  return request({
    url: `/api/order/shipment/orderNo/${orderNo}`,
    method: 'get'
  })
}

// 订单发货
export function shipOrder(data) {
  return request({
    url: '/api/order/shipment/ship',
    method: 'post',
    data
  })
}

// 批量发货
export function batchShipOrder(data) {
  return request({
    url: '/api/order/shipment/batch-ship',
    method: 'post',
    data
  })
}

// 更新配送进度
export function updateShippingProgress(data) {
  return request({
    url: '/api/order/shipment/progress',
    method: 'post',
    data
  })
}

// 完成配送
export function completeDelivery(orderId) {
  return request({
    url: `/api/order/shipment/complete/${orderId}`,
    method: 'post'
  })
}
