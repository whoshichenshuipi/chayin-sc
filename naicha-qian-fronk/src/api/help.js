import request from '@/utils/request'

/**
 * 帮助中心相关API（用户端）
 */

/**
 * 获取FAQ分类列表
 * @param {Object} params - 查询参数（可选）
 * @returns {Promise} FAQ分类列表
 */
export function getFaqCategories(params = {}) {
  return request({
    url: '/api/help/faq/categories',
    method: 'get',
    params,
    needMerchantId: false
  }).catch(error => {
    // 如果接口不存在，返回空列表
    if (error.response?.status === 404) {
      console.warn('后端FAQ分类接口不存在，请后端添加')
      return []
    }
    throw error
  })
}

/**
 * 获取FAQ列表
 * @param {Object} params - 查询参数
 * @param {String} params.categoryId - 分类ID（可选）
 * @param {String} params.keyword - 搜索关键词（可选）
 * @returns {Promise} FAQ列表
 */
export function getFaqList(params = {}) {
  return request({
    url: '/api/help/faq/list',
    method: 'get',
    params,
    needMerchantId: false
  }).catch(error => {
    // 如果接口不存在，返回空列表
    if (error.response?.status === 404) {
      console.warn('后端FAQ列表接口不存在，请后端添加')
      return []
    }
    throw error
  })
}

/**
 * 搜索FAQ
 * @param {String} keyword - 搜索关键词
 * @returns {Promise} 搜索结果
 */
export function searchFaq(keyword) {
  return request({
    url: '/api/help/faq/search',
    method: 'get',
    params: { keyword },
    needMerchantId: false
  }).catch(error => {
    // 如果接口不存在，返回空列表
    if (error.response?.status === 404) {
      console.warn('后端FAQ搜索接口不存在，请后端添加')
      return []
    }
    throw error
  })
}

/**
 * 提交用户反馈
 * @param {Object} data - 反馈数据
 * @param {String} data.type - 反馈类型：account-账号问题, order-订单问题, payment-支付问题, after_sale-售后问题, other-其他问题
 * @param {String} data.description - 问题描述
 * @param {String} data.contact - 联系方式（手机号或邮箱）
 * @param {Array} data.attachments - 附件列表（可选）
 * @returns {Promise} 提交结果
 */
export function submitFeedback(data) {
  // 映射前端反馈类型到后端类型
  const typeMap = {
    account: 'bug', // 账号问题归为功能问题
    order: 'bug',
    payment: 'bug',
    after_sale: 'bug',
    other: 'suggestion' // 其他问题归为建议类
  }
  
  const feedbackData = {
    type: typeMap[data.type] || 'suggestion',
    title: `[${data.type}] 用户反馈`,
    content: data.description,
    contact: data.contact,
    attachments: data.attachments || []
  }
  
  return request({
    url: '/api/user/feedback/submit',
    method: 'post',
    data: feedbackData,
    needMerchantId: false
  }).catch(error => {
    // 如果用户端接口不存在，尝试使用管理员接口
    if (error.response?.status === 404) {
      console.warn('后端用户反馈接口不存在，请后端添加')
      throw new Error('反馈提交功能暂未实现，请联系管理员')
    }
    throw error
  })
}

/**
 * 提交离线留言
 * @param {String} message - 留言内容
 * @returns {Promise} 提交结果
 */
export function submitOfflineMessage(message) {
  return request({
    url: '/api/user/feedback/offline-message',
    method: 'post',
    data: {
      content: message,
      type: 'suggestion'
    },
    needMerchantId: false
  }).catch(error => {
    // 如果接口不存在，提示后端需要实现
    if (error.response?.status === 404) {
      console.warn('后端离线留言接口不存在，请后端添加')
      throw new Error('留言功能暂未实现，请联系管理员')
    }
    throw error
  })
}

/**
 * 标记FAQ为有用/无用
 * @param {Number} faqId - FAQ ID
 * @param {Boolean} isHelpful - 是否有用
 * @returns {Promise} 操作结果
 */
export function markFaqHelpful(faqId, isHelpful) {
  return request({
    url: `/api/help/faq/${faqId}/helpful`,
    method: 'post',
    data: { isHelpful },
    needMerchantId: false
  }).catch(error => {
    // 如果接口不存在，忽略错误（不影响用户体验）
    if (error.response?.status === 404) {
      console.warn('后端FAQ点赞接口不存在')
      return { success: true }
    }
    throw error
  })
}

/**
 * 举报FAQ问题
 * @param {Number} faqId - FAQ ID
 * @param {String} reason - 举报原因（可选）
 * @returns {Promise} 操作结果
 */
export function reportFaq(faqId, reason) {
  return request({
    url: `/api/help/faq/${faqId}/report`,
    method: 'post',
    data: { reason },
    needMerchantId: false
  }).catch(error => {
    // 如果接口不存在，忽略错误（不影响用户体验）
    if (error.response?.status === 404) {
      console.warn('后端FAQ举报接口不存在')
      return { success: true }
    }
    throw error
  })
}

