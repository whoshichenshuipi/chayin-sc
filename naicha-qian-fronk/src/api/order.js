import request from '@/utils/request'

/**
 * 订单相关API（用户端）
 */

/**
 * 获取订单详情
 * @param {Number} orderId - 订单ID
 * @returns {Promise} 订单详情
 */
export function getOrderDetail(orderId) {
  return request({
    url: `/api/order/detail/${orderId}`,
    method: 'get',
    needMerchantId: false
  })
}

/**
 * 根据订单号查询订单
 * @param {String} orderNo - 订单号
 * @returns {Promise} 订单详情
 */
export function getOrderByOrderNo(orderNo) {
  return request({
    url: `/api/order/search/${orderNo}`,
    method: 'get',
    needMerchantId: false
  })
}

/**
 * 分页查询订单列表
 * @param {Object} params - 查询参数
 * @param {Number} params.pageNum - 当前页
 * @param {Number} params.pageSize - 每页大小
 * @param {Number} params.status - 订单状态（可选）
 * @param {String} params.orderNo - 订单号（可选）
 * @param {String} params.startTime - 开始时间（可选，格式：YYYY-MM-DD HH:mm:ss）
 * @param {String} params.endTime - 结束时间（可选，格式：YYYY-MM-DD HH:mm:ss）
 * @returns {Promise} 分页数据，包含 records 数组和 total
 */
export function getOrderPage(params = {}) {
  const queryData = {
    pageNum: params.pageNum || params.current || 1,
    pageSize: params.pageSize || params.size || 10
  }
  
  // 添加可选参数
  if (params.status !== undefined && params.status !== null) {
    queryData.status = params.status
  }
  if (params.orderNo) {
    queryData.orderNo = params.orderNo
  }
  if (params.startTime) {
    queryData.startTime = params.startTime
  }
  if (params.endTime) {
    queryData.endTime = params.endTime
  }
  
  return request({
    url: '/api/order/page',
    method: 'post',
    data: queryData,
    needMerchantId: false,
    silent404: true // 404时不显示错误消息
  }).catch(error => {
    // 如果接口不存在，返回空列表
    if (error.response?.status === 404) {
      console.warn('订单列表接口不存在，请后端添加')
      return { records: [], total: 0, current: 1, size: queryData.pageSize }
    }
    throw error
  })
}

/**
 * 创建订单
 * @param {Object} orderData - 订单数据
 * @param {Number} orderData.addressId - 地址ID
 * @param {String} orderData.payMethod - 支付方式：wechat/alipay/wallet
 * @param {Number} orderData.couponId - 优惠券ID（可选）
 * @param {Array} orderData.items - 订单商品列表
 * @param {String} orderData.remark - 订单备注（可选）
 * @returns {Promise} 订单ID或订单详情
 */
export function createOrder(orderData) {
  return request({
    url: '/api/order/create',
    method: 'post',
    data: orderData,
    needMerchantId: false,
    silent404: true // 404时不显示错误消息，由调用方处理
  }).catch(error => {
    // 如果接口不存在，返回模拟成功（便于前端开发）
    if (error.response?.status === 404) {
      console.warn('创建订单接口不存在，请后端添加')
      return {
        id: Date.now(),
        orderNo: 'ORD' + Date.now(),
        status: 0,
        message: '订单创建成功（模拟）'
      }
    }
    throw error
  })
}

/**
 * 取消订单
 * @param {Number} orderId - 订单ID
 * @returns {Promise} 操作结果
 */
export function cancelOrder(orderId) {
  return request({
    url: `/api/order/cancel/${orderId}`,
    method: 'put',
    needMerchantId: false
  })
}

/**
 * 完成订单（确认收货）
 * @param {Number} orderId - 订单ID
 * @returns {Promise} 操作结果
 */
export function completeOrder(orderId) {
  return request({
    url: `/api/order/complete/${orderId}`,
    method: 'put',
    needMerchantId: false
  })
}

/**
 * 获取订单配送信息
 * @param {Number} orderId - 订单ID
 * @returns {Promise} 配送信息
 */
export function getOrderShipment(orderId) {
  return request({
    url: `/api/order/shipment/detail/${orderId}`,
    method: 'get',
    needMerchantId: false,
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
 * 根据订单号获取配送信息
 * @param {String} orderNo - 订单号
 * @returns {Promise} 配送信息
 */
export function getOrderShipmentByOrderNo(orderNo) {
  return request({
    url: `/api/order/shipment/orderNo/${orderNo}`,
    method: 'get',
    needMerchantId: false,
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
 * 订单支付
 * @param {Number} orderId - 订单ID
 * @param {String} payMethod - 支付方式：wechat/alipay/wallet
 * @returns {Promise} 支付结果（返回订单详情）
 */
export function payOrder(orderId, payMethod = 'wechat') {
  return request({
    url: `/api/order/pay/${orderId}`,
    method: 'post',
    data: { payMethod },
    needMerchantId: false
  })
}

/**
 * 获取订单统计信息（用户端）
 * 注意：后端可能有用户端的统计接口，如果没有则返回null
 * @returns {Promise} 订单统计信息
 */
export function getOrderStatistics() {
  return request({
    url: '/api/order/statistics',
    method: 'get',
    needMerchantId: false,
    silent404: true
  }).catch(error => {
    // 如果接口不存在，返回null（前端可以自己计算）
    if (error.response?.status === 404) {
      return null
    }
    throw error
  })
}

