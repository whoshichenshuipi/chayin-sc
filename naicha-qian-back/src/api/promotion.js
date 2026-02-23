import request from '@/utils/request'

/**
 * 促销活动管理相关API
 */

// 创建促销活动
export function createPromotion(data) {
  return request({
    url: '/api/promotion/create',
    method: 'post',
    data
  })
}

// 更新促销活动
export function updatePromotion(promotionId, data) {
  return request({
    url: `/api/promotion/update/${promotionId}`,
    method: 'put',
    data
  })
}

// 删除促销活动
export function deletePromotion(promotionId) {
  return request({
    url: `/api/promotion/delete/${promotionId}`,
    method: 'delete'
  })
}

// 查询促销活动详情
export function getPromotionDetail(promotionId) {
  return request({
    url: `/api/promotion/detail/${promotionId}`,
    method: 'get'
  })
}

// 分页查询促销活动列表
export function getPromotionList(params) {
  return request({
    url: '/api/promotion/page',
    method: 'post',
    data: params
  })
}

// 暂停/恢复促销活动
export function togglePausePromotion(promotionId) {
  return request({
    url: `/api/promotion/toggle-pause/${promotionId}`,
    method: 'put'
  })
}

// 结束促销活动
export function endPromotion(promotionId) {
  return request({
    url: `/api/promotion/end/${promotionId}`,
    method: 'put'
  })
}

