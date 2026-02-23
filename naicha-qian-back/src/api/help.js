import request from '@/utils/request'

// 获取FAQ列表
export function getFaqs(params) {
  return request({
    url: '/api/help/faqs',
    method: 'get',
    params
  })
}

// 搜索FAQ
export function searchFaqs(keyword) {
  return request({
    url: '/api/help/faqs/search',
    method: 'get',
    params: { keyword }
  })
}

// 获取FAQ详情
export function getFaqDetail(faqId) {
  return request({
    url: `/api/help/faqs/${faqId}`,
    method: 'get'
  })
}

// 点赞FAQ
export function likeFaq(faqId) {
  return request({
    url: `/api/help/faqs/${faqId}/like`,
    method: 'post'
  })
}

// 举报FAQ
export function reportFaq(faqId, reason) {
  return request({
    url: `/api/help/faqs/${faqId}/report`,
    method: 'post',
    data: { reason }
  })
}

// 获取操作指南列表
export function getGuides(params) {
  return request({
    url: '/api/help/guides',
    method: 'get',
    params
  })
}

// 获取指南详情
export function getGuideDetail(guideId) {
  return request({
    url: `/api/help/guides/${guideId}`,
    method: 'get'
  })
}

// 下载指南文档
export function downloadGuide(guideId) {
  return request({
    url: `/api/help/guides/${guideId}/download`,
    method: 'get',
    responseType: 'blob'
  })
}

// 在线客服相关
export const customerServiceApi = {
  // 获取客服状态
  getServiceStatus() {
    return request({
      url: '/api/customer-service/status',
      method: 'get'
    })
  },

  // 获取聊天记录
  getChatHistory() {
    return request({
      url: '/api/customer-service/chat-history',
      method: 'get'
    })
  },

  // 发送消息
  sendMessage(message) {
    return request({
      url: '/api/customer-service/send-message',
      method: 'post',
      data: { message }
    })
  },

  // 提交离线留言
  submitOfflineMessage(data) {
    return request({
      url: '/api/customer-service/offline-message',
      method: 'post',
      data
    })
  },

  // 上传图片
  uploadImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/api/customer-service/upload-image',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}

// 问题反馈相关
export const feedbackApi = {
  // 提交问题反馈
  submitFeedback(data) {
    return request({
      url: '/api/feedback',
      method: 'post',
      data
    })
  },

  // 获取反馈列表
  getFeedbacks(params) {
    return request({
      url: '/api/feedback',
      method: 'get',
      params
    })
  },

  // 获取反馈详情
  getFeedbackDetail(feedbackId) {
    return request({
      url: `/api/feedback/${feedbackId}`,
      method: 'get'
    })
  },

  // 更新反馈状态
  updateFeedbackStatus(feedbackId, status) {
    return request({
      url: `/api/feedback/${feedbackId}/status`,
      method: 'put',
      data: { status }
    })
  },

  // 分配反馈处理人
  assignFeedback(feedbackId, assigneeId) {
    return request({
      url: `/api/feedback/${feedbackId}/assign`,
      method: 'put',
      data: { assigneeId }
    })
  },

  // 回复反馈
  replyFeedback(feedbackId, reply) {
    return request({
      url: `/api/feedback/${feedbackId}/reply`,
      method: 'post',
      data: { reply }
    })
  },

  // 获取反馈统计
  getFeedbackStats() {
    return request({
      url: '/api/feedback/stats',
      method: 'get'
    })
  }
}

// 管理员帮助中心相关
export const adminHelpApi = {
  // 获取操作指南分类
  getGuideCategories() {
    return request({
      url: '/api/admin/help/guide-categories',
      method: 'get'
    })
  },

  // 创建操作指南
  createGuide(data) {
    return request({
      url: '/api/admin/help/guides',
      method: 'post',
      data
    })
  },

  // 更新操作指南
  updateGuide(guideId, data) {
    return request({
      url: `/api/admin/help/guides/${guideId}`,
      method: 'put',
      data
    })
  },

  // 删除操作指南
  deleteGuide(guideId) {
    return request({
      url: `/api/admin/help/guides/${guideId}`,
      method: 'delete'
    })
  },

  // 获取FAQ管理列表
  getFaqs(params) {
    return request({
      url: '/api/admin/help/faqs',
      method: 'get',
      params
    })
  },

  // 创建FAQ
  createFaq(data) {
    return request({
      url: '/api/admin/help/faqs',
      method: 'post',
      data
    })
  },

  // 更新FAQ
  updateFaq(faqId, data) {
    return request({
      url: `/api/admin/help/faqs/${faqId}`,
      method: 'put',
      data
    })
  },

  // 删除FAQ
  deleteFaq(faqId) {
    return request({
      url: `/api/admin/help/faqs/${faqId}`,
      method: 'delete'
    })
  },

  // 获取所有反馈
  getAllFeedbacks(params) {
    return request({
      url: '/api/admin/feedback',
      method: 'get',
      params
    })
  },

  // 处理反馈
  processFeedback(feedbackId, data) {
    return request({
      url: `/api/admin/feedback/${feedbackId}/process`,
      method: 'put',
      data
    })
  }
}

// 商家帮助中心相关
export const merchantHelpApi = {
  // 获取商家操作指南
  getMerchantGuides(params) {
    return request({
      url: '/api/merchant/help/guides',
      method: 'get',
      params
    })
  },

  // 获取商家FAQ
  getMerchantFaqs(params) {
    return request({
      url: '/api/merchant/help/faqs',
      method: 'get',
      params
    })
  },

  // 提交商家反馈
  submitMerchantFeedback(data) {
    return request({
      url: '/api/merchant/feedback',
      method: 'post',
      data
    })
  },

  // 获取商家反馈列表
  getMerchantFeedbacks(params) {
    return request({
      url: '/api/merchant/feedback',
      method: 'get',
      params
    })
  }
}
