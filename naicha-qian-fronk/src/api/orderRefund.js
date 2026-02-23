import request from '@/utils/request'

/**
 * 订单退款相关API（用户端）
 */

/**
 * 根据订单ID查询订单退款申请详情
 * @param {Number} orderId - 订单ID
 * @returns {Promise} 退款申请详情
 */
export function getRefundByOrderId(orderId) {
  return request({
    url: `/api/order/refund/order/${orderId}`,
    method: 'get',
    needMerchantId: false
  })
}

/**
 * 提交订单退款申请
 * @param {Object} refundData - 退款申请数据
 * @param {Number} refundData.orderId - 订单ID
 * @param {String} refundData.processType - 处理类型：cancel-取消申请，refund-退款申请
 * @param {String} refundData.reason - 申请原因
 * @param {String} refundData.detailReason - 详细说明
 * @param {Number} refundData.refundAmount - 退款金额
 * @param {String} refundData.refundType - 退款类型：full-全额退款，partial-部分退款
 * @param {String} refundData.refundReason - 退款原因
 * @param {Array} refundData.attachments - 附件列表
 * @returns {Promise} 提交结果
 */
export function submitRefundRequest(refundData) {
  return request({
    url: '/api/order/refund/submit',
    method: 'post',
    data: refundData,
    needMerchantId: false
  })
}

/**
 * 根据ID查询订单退款申请详情
 * @param {Number} refundId - 退款申请ID
 * @returns {Promise} 退款申请详情
 */
export function getRefundDetail(refundId) {
  return request({
    url: `/api/order/refund/detail/${refundId}`,
    method: 'get',
    needMerchantId: false
  })
}

