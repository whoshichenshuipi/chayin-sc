import request from '@/utils/request'

/**
 * 管理员相关API
 */

// 获取管理员仪表盘数据
export function getDashboardData(days = 30) {
  return request({
    url: '/api/admin/dashboard/data',
    method: 'get',
    params: { days }
  })
}

/**
 * 数据概览相关API
 */

// 获取数据概览
export function getDataOverview(period, startDate, endDate) {
  return request({
    url: '/api/admin/data-overview/overview',
    method: 'get',
    params: { period, startDate, endDate }
  })
}

/**
 * 数据报表相关API
 */

// 生成数据报表
export function generateDataReport(reportType, period, startDate, endDate) {
  return request({
    url: '/api/admin/data-report/generate',
    method: 'get',
    params: { type: reportType, period, startDate, endDate }
  })
}

/**
 * 商家管理相关API
 */

// 分页查询商家列表
export function getMerchantList(params) {
  return request({
    url: '/api/admin/merchant/list',
    method: 'get',
    params
  })
}

// 获取商家详情
export function getMerchantDetail(merchantId) {
  return request({
    url: `/api/admin/merchant/${merchantId}`,
    method: 'get'
  })
}

// 更新商家状态
export function updateMerchantStatus(merchantId, status) {
  return request({
    url: `/api/admin/merchant/${merchantId}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 商家审核相关API
 */

// 分页查询待审核商家列表
export function getPendingAuditMerchants(params) {
  return request({
    url: '/api/admin/merchant-audit/pending/list',
    method: 'get',
    params
  })
}

// 分页查询审核历史
export function getAuditHistory(params) {
  return request({
    url: '/api/admin/merchant-audit/history/list',
    method: 'get',
    params
  })
}

// 审核商家
export function auditMerchant(data) {
  return request({
    url: '/api/admin/merchant-audit/audit',
    method: 'post',
    data
  })
}

// 快速审核通过
export function approveMerchant(merchantId) {
  return request({
    url: `/api/admin/merchant-audit/approve/${merchantId}`,
    method: 'post'
  })
}

// 快速审核拒绝
export function rejectMerchant(merchantId, reason) {
  return request({
    url: `/api/admin/merchant-audit/reject/${merchantId}`,
    method: 'post',
    params: { reason }
  })
}

/**
 * 商家评分相关API
 */

// 分页查询商家评分列表
export function getMerchantRatingList(params) {
  return request({
    url: '/api/admin/merchant-rating/list',
    method: 'get',
    params
  })
}

// 获取商家评分统计列表
export function getMerchantRatingStatsList(merchantIds = null) {
  // Spring Boot的@RequestParam List会自动处理多个同名参数
  // axios会将数组转换为 ?merchantIds=1&merchantIds=2 格式
  return request({
    url: '/api/admin/merchant-rating/stats/list',
    method: 'get',
    params: merchantIds && merchantIds.length > 0 ? { merchantIds } : {}
  })
}

// 根据商家ID获取评分统计
export function getMerchantRatingStats(merchantId) {
  return request({
    url: `/api/admin/merchant-rating/stats/${merchantId}`,
    method: 'get'
  })
}

/**
 * 用户反馈相关API
 */

// 分页查询用户反馈列表
export function getUserFeedbackList(params) {
  return request({
    url: '/api/admin/user-feedback/list',
    method: 'get',
    params
  })
}

// 获取反馈详情
export function getUserFeedbackDetail(feedbackId) {
  return request({
    url: `/api/admin/user-feedback/${feedbackId}`,
    method: 'get'
  })
}

// 分配反馈处理
export function assignUserFeedback(data) {
  return request({
    url: '/api/admin/user-feedback/assign',
    method: 'post',
    data
  })
}

// 完成反馈处理
export function completeUserFeedback(data) {
  return request({
    url: '/api/admin/user-feedback/complete',
    method: 'post',
    data
  })
}

