import request from '@/utils/request'

/**
 * 通知相关API（用户端）
 */

/**
 * 获取自定义请求头（包含用户类型）
 * 注意：用户ID由request拦截器自动添加
 */
const getCustomHeaders = () => {
  return {
    'X-User-Type': 'user'
  }
}

/**
 * 分页查询通知列表
 * @param {Object} params - 查询参数
 * @param {Number} params.pageNum - 当前页，默认1
 * @param {Number} params.pageSize - 每页大小，默认20
 * @param {String} params.type - 通知类型（可选）
 * @param {Number} params.isRead - 是否已读：0-未读，1-已读，null-全部（可选）
 * @param {String} params.keyword - 搜索关键词（可选）
 * @returns {Promise} 分页数据，包含 records 数组
 */
export function getNotificationPage(params = {}) {
  return request({
    url: '/api/notifications/page',
    method: 'post',
    data: {
      pageNum: params.pageNum || params.current || params.page || 1,
      pageSize: params.pageSize || params.size || 20,
      type: params.type,
      isRead: params.isRead,
      recipientType: 'user' // 用户端固定为user
    },
    needMerchantId: false,
    customHeaders: getCustomHeaders(),
    silent404: true // 404时不显示错误消息，由调用方处理
  })
}

/**
 * 获取通知详情
 * @param {Number} notificationId - 通知ID
 * @returns {Promise} 通知详情
 */
export function getNotificationDetail(notificationId) {
  return request({
    url: `/api/notifications/${notificationId}`,
    method: 'get',
    needMerchantId: false,
    customHeaders: getCustomHeaders()
  })
}

/**
 * 标记通知为已读
 * @param {Number} notificationId - 通知ID
 * @returns {Promise} 操作结果
 */
export function markAsRead(notificationId) {
  return request({
    url: `/api/notifications/${notificationId}/read`,
    method: 'put',
    needMerchantId: false,
    customHeaders: getCustomHeaders()
  })
}

/**
 * 批量标记为已读
 * @param {Array} notificationIds - 通知ID列表
 * @returns {Promise} 操作结果
 */
export function batchMarkAsRead(notificationIds) {
  return request({
    url: '/api/notifications/batch-read',
    method: 'put',
    data: notificationIds,
    needMerchantId: false,
    customHeaders: getCustomHeaders()
  })
}

/**
 * 标记所有为已读
 * @returns {Promise} 操作结果
 */
export function markAllAsRead() {
  return request({
    url: '/api/notifications/mark-all-read',
    method: 'put',
    needMerchantId: false,
    customHeaders: getCustomHeaders()
  })
}

/**
 * 删除通知
 * @param {Number} notificationId - 通知ID
 * @returns {Promise} 操作结果
 */
export function deleteNotification(notificationId) {
  return request({
    url: `/api/notifications/${notificationId}`,
    method: 'delete',
    needMerchantId: false,
    customHeaders: getCustomHeaders()
  })
}

/**
 * 批量删除通知
 * @param {Array} notificationIds - 通知ID列表
 * @returns {Promise} 操作结果
 */
export function batchDeleteNotifications(notificationIds) {
  return request({
    url: '/api/notifications/batch-delete',
    method: 'delete',
    data: notificationIds,
    needMerchantId: false,
    customHeaders: getCustomHeaders()
  })
}

/**
 * 获取未读通知数量
 * @returns {Promise} 未读数量
 */
export function getUnreadCount() {
  return request({
    url: '/api/notifications/unread-count',
    method: 'get',
    needMerchantId: false,
    customHeaders: getCustomHeaders()
  }).catch(error => {
    // 如果接口不存在，返回0
    if (error.response?.status === 404) {
      return 0
    }
    throw error
  })
}

/**
 * 获取通知统计
 * @returns {Promise} 统计数据
 */
export function getNotificationStats() {
  return request({
    url: '/api/notifications/stats',
    method: 'get',
    needMerchantId: false,
    customHeaders: getCustomHeaders()
  }).catch(error => {
    // 如果接口不存在，返回默认统计
    if (error.response?.status === 404) {
      return {
        totalCount: 0,
        unreadCount: 0,
        readCount: 0
      }
    }
    throw error
  })
}

