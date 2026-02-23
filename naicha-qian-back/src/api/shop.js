import request from '@/utils/request'

/**
 * 店铺信息管理API
 */
export const shopApi = {
  /**
   * 获取店铺信息
   */
  getShopInfo() {
    return request.get('/api/shop/info/info')
  },

  /**
   * 更新店铺基础信息
   */
  updateShopInfo(data) {
    return request.post('/api/shop/info/update', data)
  },

  /**
   * 更新店铺营业状态
   */
  updateShopStatus(data) {
    return request.post('/api/shop/info/status/update', data)
  },

  /**
   * 获取店铺评分统计
   */
  getRatingStats() {
    return request.get('/api/shop/info/rating/stats')
  },

  /**
   * 获取评价列表
   */
  getReviewList(rating = null) {
    const params = {}
    if (rating !== null) {
      params.rating = rating
    }
    return request.get('/api/shop/info/reviews', { params })
  },

  /**
   * 回复评价
   */
  replyReview(data) {
    return request.post('/api/shop/info/review/reply', data)
  }
}
