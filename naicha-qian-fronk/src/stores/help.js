import { defineStore } from 'pinia'
import { getFaqs, searchFaqs, likeFaq, reportFaq, customerServiceApi, feedbackApi } from '@/api/help'

export const useHelpStore = defineStore('help', {
  state: () => ({
    faqs: [],
    searchResults: [],
    chatMessages: [],
    isServiceOnline: true,
    feedbackList: [],
    loading: false
  }),

  getters: {
    // 按分类获取FAQ
    getFaqsByCategory: (state) => (category) => {
      return state.faqs.filter(faq => faq.category === category)
    },
    
    // 获取热门FAQ
    popularFaqs: (state) => {
      return state.faqs.filter(faq => faq.isPopular).slice(0, 5)
    },
    
    // 获取未读消息数量
    unreadMessageCount: (state) => {
      return state.chatMessages.filter(msg => !msg.isRead && !msg.isUser).length
    }
  },

  actions: {
    // 获取FAQ列表
    async fetchFaqs(params = {}) {
      this.loading = true
      try {
        const response = await getFaqs(params)
        this.faqs = response.data.faqs
        return response.data
      } catch (error) {
        console.error('获取FAQ列表失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 搜索FAQ
    async searchFaqs(keyword) {
      if (!keyword.trim()) {
        this.searchResults = []
        return
      }
      
      try {
        const response = await searchFaqs(keyword)
        this.searchResults = response.data.results
        return response.data
      } catch (error) {
        console.error('搜索FAQ失败:', error)
        throw error
      }
    },

    // 点赞FAQ
    async likeFaq(faqId) {
      try {
        await likeFaq(faqId)
        const faq = this.faqs.find(f => f.id === faqId)
        if (faq) {
          faq.isHelpful = !faq.isHelpful
          faq.likeCount += faq.isHelpful ? 1 : -1
        }
      } catch (error) {
        console.error('点赞FAQ失败:', error)
        throw error
      }
    },

    // 举报FAQ
    async reportFaq(faqId, reason) {
      try {
        await reportFaq(faqId, reason)
      } catch (error) {
        console.error('举报FAQ失败:', error)
        throw error
      }
    },

    // 获取客服状态
    async fetchServiceStatus() {
      try {
        const response = await customerServiceApi.getServiceStatus()
        this.isServiceOnline = response.data.isOnline
        return response.data
      } catch (error) {
        console.error('获取客服状态失败:', error)
        throw error
      }
    },

    // 获取聊天记录
    async fetchChatHistory() {
      try {
        const response = await customerServiceApi.getChatHistory()
        this.chatMessages = response.data.messages
        return response.data
      } catch (error) {
        console.error('获取聊天记录失败:', error)
        throw error
      }
    },

    // 发送消息
    async sendMessage(message) {
      try {
        const response = await customerServiceApi.sendMessage(message)
        const userMessage = {
          id: Date.now(),
          isUser: true,
          content: message,
          time: new Date().toLocaleTimeString().slice(0, 5),
          isRead: true
        }
        this.chatMessages.push(userMessage)
        return response.data
      } catch (error) {
        console.error('发送消息失败:', error)
        throw error
      }
    },

    // 提交离线留言
    async submitOfflineMessage(data) {
      try {
        await customerServiceApi.submitOfflineMessage(data)
      } catch (error) {
        console.error('提交离线留言失败:', error)
        throw error
      }
    },

    // 上传图片
    async uploadImage(file) {
      try {
        const response = await customerServiceApi.uploadImage(file)
        return response.data
      } catch (error) {
        console.error('上传图片失败:', error)
        throw error
      }
    },

    // 添加客服回复消息
    addServiceMessage(content) {
      const serviceMessage = {
        id: Date.now(),
        isUser: false,
        content,
        time: new Date().toLocaleTimeString().slice(0, 5),
        isRead: false
      }
      this.chatMessages.push(serviceMessage)
    },

    // 标记消息为已读
    markMessagesAsRead() {
      this.chatMessages.forEach(msg => {
        if (!msg.isUser) {
          msg.isRead = true
        }
      })
    },

    // 清空聊天记录
    clearChatMessages() {
      this.chatMessages = []
    },

    // 提交问题反馈
    async submitFeedback(data) {
      try {
        await feedbackApi.submitFeedback(data)
      } catch (error) {
        console.error('提交反馈失败:', error)
        throw error
      }
    },

    // 获取反馈列表
    async fetchFeedbacks(params = {}) {
      try {
        const response = await feedbackApi.getFeedbacks(params)
        this.feedbackList = response.data.feedbacks
        return response.data
      } catch (error) {
        console.error('获取反馈列表失败:', error)
        throw error
      }
    },

    // 获取反馈详情
    async fetchFeedbackDetail(feedbackId) {
      try {
        const response = await feedbackApi.getFeedbackDetail(feedbackId)
        return response.data
      } catch (error) {
        console.error('获取反馈详情失败:', error)
        throw error
      }
    },

    // 更新反馈状态
    async updateFeedbackStatus(feedbackId, status) {
      try {
        await feedbackApi.updateFeedbackStatus(feedbackId, status)
        const feedback = this.feedbackList.find(f => f.id === feedbackId)
        if (feedback) {
          feedback.status = status
        }
      } catch (error) {
        console.error('更新反馈状态失败:', error)
        throw error
      }
    },

    // 回复反馈
    async replyFeedback(feedbackId, reply) {
      try {
        await feedbackApi.replyFeedback(feedbackId, reply)
      } catch (error) {
        console.error('回复反馈失败:', error)
        throw error
      }
    },

    // 获取反馈统计
    async fetchFeedbackStats() {
      try {
        const response = await feedbackApi.getFeedbackStats()
        return response.data
      } catch (error) {
        console.error('获取反馈统计失败:', error)
        throw error
      }
    }
  }
})
