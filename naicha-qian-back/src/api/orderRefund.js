import request from '@/utils/request'

/**
 * 订单退款相关API
 */

// 分页查询订单退款申请列表
export function getRefundPage(data) {
  return request({
    url: '/api/order/refund/page',
    method: 'post',
    data
  })
}

// 根据ID查询订单退款申请详情
export function getRefundDetail(id) {
  return request({
    url: `/api/order/refund/detail/${id}`,
    method: 'get'
  })
}

// 根据订单ID查询订单退款申请详情
export function getRefundByOrderId(orderId) {
  return request({
    url: `/api/order/refund/order/${orderId}`,
    method: 'get'
  })
}

// 提交订单退款申请
export function submitRefundRequest(data) {
  return request({
    url: '/api/order/refund/submit',
    method: 'post',
    data
  })
}

// 处理订单退款申请
export function processRefundRequest(data) {
  return request({
    url: '/api/order/refund/process',
    method: 'post',
    data
  })
}

// 批量处理订单退款申请
export function batchProcessRefundRequest(data) {
  return request({
    url: '/api/order/refund/batch-process',
    method: 'post',
    data
  })
}

// 获取退款统计数据
export function getRefundStats() {
  return request({
    url: '/api/order/refund/stats',
    method: 'get'
  })
}

// 完成退款
export function completeRefund(refundId) {
  return request({
    url: `/api/order/refund/complete/${refundId}`,
    method: 'post'
  })
}
