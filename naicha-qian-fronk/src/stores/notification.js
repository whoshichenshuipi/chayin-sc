import { defineStore } from 'pinia'
import { userNotificationApi } from '@/api/notification'

export const useNotificationStore = defineStore('notification', {
  state: () => ({
    notifications: [],
    unreadCount: 0,
    loading: false,
    settings: {
      types: ['order', 'marketing', 'system'],
      methods: ['popup', 'sms'],
      marketingMuted: false
    }
  }),

  getters: {
    // 获取未读通知
    unreadNotifications: (state) => state.notifications.filter(n => !n.isRead),
    
    // 按类型获取通知
    getNotificationsByType: (state) => (type) => {
      return state.notifications.filter(n => n.type === type)
    },
    
    // 获取营销通知
    marketingNotifications: (state) => state.notifications.filter(n => n.type === 'marketing'),
    
    // 获取订单通知
    orderNotifications: (state) => state.notifications.filter(n => n.type === 'order'),
    
    // 获取系统通知
    systemNotifications: (state) => state.notifications.filter(n => n.type === 'system')
  },

  actions: {
    // 获取通知列表
    async fetchNotifications(params = {}) {
      this.loading = true
      try {
        const response = await userNotificationApi.getNotifications(params)
        this.notifications = response.data.notifications
        this.unreadCount = response.data.unreadCount
        return response.data
      } catch (error) {
        console.error('获取通知列表失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 获取未读数量
    async fetchUnreadCount() {
      try {
        const response = await userNotificationApi.getUnreadCount()
        this.unreadCount = response.data.count
        return response.data
      } catch (error) {
        console.error('获取未读数量失败:', error)
        throw error
      }
    },

    // 标记为已读
    async markAsRead(notificationId) {
      try {
        await userNotificationApi.markAsRead(notificationId)
        const notification = this.notifications.find(n => n.id === notificationId)
        if (notification) {
          notification.isRead = true
          this.unreadCount = Math.max(0, this.unreadCount - 1)
        }
      } catch (error) {
        console.error('标记已读失败:', error)
        throw error
      }
    },

    // 批量标记为已读
    async markAllAsRead() {
      try {
        await userNotificationApi.markAllAsRead()
        this.notifications.forEach(notification => {
          notification.isRead = true
        })
        this.unreadCount = 0
      } catch (error) {
        console.error('批量标记已读失败:', error)
        throw error
      }
    },

    // 删除通知
    async deleteNotification(notificationId) {
      try {
        await userNotificationApi.deleteNotification(notificationId)
        const index = this.notifications.findIndex(n => n.id === notificationId)
        if (index > -1) {
          const notification = this.notifications[index]
          this.notifications.splice(index, 1)
          if (!notification.isRead) {
            this.unreadCount = Math.max(0, this.unreadCount - 1)
          }
        }
      } catch (error) {
        console.error('删除通知失败:', error)
        throw error
      }
    },

    // 批量删除通知
    async deleteNotifications(notificationIds) {
      try {
        await userNotificationApi.deleteNotifications(notificationIds)
        const deletedCount = notificationIds.length
        this.notifications = this.notifications.filter(n => !notificationIds.includes(n.id))
        // 重新计算未读数量
        this.unreadCount = this.notifications.filter(n => !n.isRead).length
        return deletedCount
      } catch (error) {
        console.error('批量删除通知失败:', error)
        throw error
      }
    },

    // 设置营销通知免打扰
    async setMarketingMute(isMuted) {
      try {
        await userNotificationApi.setMarketingMute(isMuted)
        this.settings.marketingMuted = isMuted
      } catch (error) {
        console.error('设置免打扰失败:', error)
        throw error
      }
    },

    // 添加新通知（用于实时推送）
    addNotification(notification) {
      this.notifications.unshift(notification)
      if (!notification.isRead) {
        this.unreadCount++
      }
    },

    // 更新通知
    updateNotification(notificationId, updates) {
      const notification = this.notifications.find(n => n.id === notificationId)
      if (notification) {
        Object.assign(notification, updates)
        // 如果标记为已读，更新未读数量
        if (updates.isRead && !notification.isRead) {
          this.unreadCount = Math.max(0, this.unreadCount - 1)
        }
      }
    },

    // 清空通知
    clearNotifications() {
      this.notifications = []
      this.unreadCount = 0
    },

    // 获取通知设置
    async fetchSettings() {
      try {
        const response = await userNotificationApi.getNotificationSettings()
        this.settings = response.data.settings
        return response.data
      } catch (error) {
        console.error('获取通知设置失败:', error)
        throw error
      }
    },

    // 更新通知设置
    async updateSettings(settings) {
      try {
        await userNotificationApi.updateNotificationSettings(settings)
        this.settings = { ...this.settings, ...settings }
      } catch (error) {
        console.error('更新通知设置失败:', error)
        throw error
      }
    }
  }
})
