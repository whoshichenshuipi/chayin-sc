import request from '@/utils/request'

// 获取通知列表
export function getNotifications(params) {
  return request({
    url: '/api/notifications',
    method: 'get',
    params
  })
}

// 获取未读通知数量
export function getUnreadCount() {
  return request({
    url: '/api/notifications/unread-count',
    method: 'get'
  })
}

// 标记通知为已读
export function markAsRead(notificationId) {
  return request({
    url: `/api/notifications/${notificationId}/read`,
    method: 'put'
  })
}

// 批量标记为已读
export function markAllAsRead() {
  return request({
    url: '/api/notifications/mark-all-read',
    method: 'put'
  })
}

// 删除通知
export function deleteNotification(notificationId) {
  return request({
    url: `/api/notifications/${notificationId}`,
    method: 'delete'
  })
}

// 批量删除通知
export function deleteNotifications(notificationIds) {
  return request({
    url: '/api/notifications/batch-delete',
    method: 'delete',
    data: { notificationIds }
  })
}

// 设置通知免打扰
export function setMute(notificationId, isMuted) {
  return request({
    url: `/api/notifications/${notificationId}/mute`,
    method: 'put',
    data: { isMuted }
  })
}

// 获取通知设置
export function getNotificationSettings() {
  return request({
    url: '/api/notifications/settings',
    method: 'get'
  })
}

// 更新通知设置
export function updateNotificationSettings(settings) {
  return request({
    url: '/api/notifications/settings',
    method: 'put',
    data: settings
  })
}

// 发送通知
export function sendNotification(data) {
  return request({
    url: '/api/notifications/send',
    method: 'post',
    data
  })
}

// 获取通知统计
export function getNotificationStats() {
  return request({
    url: '/api/notifications/stats',
    method: 'get'
  })
}

// 前台用户通知相关
export const userNotificationApi = {
  // 获取用户通知列表
  getNotifications(params) {
    return request({
      url: '/api/user/notifications',
      method: 'get',
      params
    })
  },

  // 获取用户未读通知数量
  getUnreadCount() {
    return request({
      url: '/api/user/notifications/unread-count',
      method: 'get'
    })
  },

  // 标记用户通知为已读
  markAsRead(notificationId) {
    return request({
      url: `/api/user/notifications/${notificationId}/read`,
      method: 'put'
    })
  },

  // 用户批量标记为已读
  markAllAsRead() {
    return request({
      url: '/api/user/notifications/mark-all-read',
      method: 'put'
    })
  },

  // 删除用户通知
  deleteNotification(notificationId) {
    return request({
      url: `/api/user/notifications/${notificationId}`,
      method: 'delete'
    })
  },

  // 设置营销通知免打扰
  setMarketingMute(isMuted) {
    return request({
      url: '/api/user/notifications/marketing-mute',
      method: 'put',
      data: { isMuted }
    })
  }
}

// 管理员通知相关
export const adminNotificationApi = {
  // 获取管理员通知列表
  getNotifications(params) {
    return request({
      url: '/api/admin/notifications',
      method: 'get',
      params
    })
  },

  // 获取管理员通知统计
  getStats() {
    return request({
      url: '/api/admin/notifications/stats',
      method: 'get'
    })
  },

  // 处理通知操作
  handleNotificationAction(notificationId, actionId) {
    return request({
      url: `/api/admin/notifications/${notificationId}/action/${actionId}`,
      method: 'post'
    })
  }
}

// 商家通知相关
export const merchantNotificationApi = {
  // 获取商家通知列表
  getNotifications(params) {
    return request({
      url: '/api/merchant/notifications',
      method: 'get',
      params
    })
  },

  // 获取商家通知统计
  getStats() {
    return request({
      url: '/api/merchant/notifications/stats',
      method: 'get'
    })
  },

  // 处理商家通知操作
  handleNotificationAction(notificationId, actionId) {
    return request({
      url: `/api/merchant/notifications/${notificationId}/action/${actionId}`,
      method: 'post'
    })
  }
}
