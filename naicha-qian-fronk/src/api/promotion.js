import request from '@/utils/request'

/**
 * 分页查询促销活动列表
 * @param {Object} params - 查询参数
 * @param {Number} params.current - 当前页，默认1
 * @param {Number} params.size - 每页大小，默认10
 * @param {Number} params.merchantId - 商家ID（可选）
 * @param {String} params.status - 状态：active-进行中（可选）
 * @returns {Promise} 分页数据，包含 records 数组
 */
export function getPromotionPage(params = {}) {
  return request({
    url: '/api/promotion/page',
    method: 'post',
    data: {
      pageNum: params.pageNum || params.current || params.page || 1,
      pageSize: params.pageSize || params.size || 10,
      merchantId: params.merchantId,
      status: params.status,
      type: params.type,
      name: params.name
    },
    needMerchantId: false // 用户端不需要商户ID
  })
}

/**
 * 获取进行中的促销活动（用于轮播图）
 * @param {Number} limit - 返回数量，默认3
 * @returns {Promise} 活动列表
 */
export function getActivePromotions(limit = 3) {
  return request({
    url: '/api/promotion/page',
    method: 'post',
    data: {
      pageNum: 1,
      pageSize: limit,
      status: 'active' // 只获取进行中的活动
    },
    needMerchantId: false
  }).then(res => {
    // MyBatis Plus 分页返回格式：{ records: [], total: 0, current: 1, size: 10 }
    return res?.records || res?.list || (Array.isArray(res) ? res : [])
  })
}

/**
 * 获取促销活动详情
 * @param {Number} promotionId - 活动ID
 * @returns {Promise} 活动详情
 */
export function getPromotionDetail(promotionId) {
  return request({
    url: `/api/promotion/detail/${promotionId}`,
    method: 'get'
  })
}

/**
 * 参与促销活动（用于统计参与人数）
 * @param {Number} promotionId - 活动ID
 * @returns {Promise} 参与结果
 */
export function participatePromotion(promotionId) {
  return request({
    url: `/api/promotion/participate/${promotionId}`,
    method: 'post',
    needMerchantId: false // 用户端不需要商户ID
  })
}

/**
 * 检查用户是否已参与营销活动
 * @param {Number} promotionId - 营销活动ID
 * @returns {Promise<Boolean>} 是否已参与（true/false）
 */
export function checkUserParticipatedPromotion(promotionId) {
  return request({
    url: `/api/promotion/user-cart/check/${promotionId}`,
    method: 'get',
    needMerchantId: false
  }).then(res => {
    // request 拦截器已经返回了 res.data，所以 res 就是布尔值
    return res === true || res === false ? res : false
  })
}

/**
 * 保存用户营销活动参与记录（绑定用户ID、营销活动ID和购物车商品ID）
 * @param {Object} data - 参与记录数据
 * @param {Number} data.promotionId - 营销活动ID
 * @param {Number} data.productId - 商品ID
 * @param {String} data.cartItemId - 购物车项ID
 * @param {Number} data.quantity - 商品数量
 * @param {Number} data.originalPrice - 商品原价
 * @param {Number} data.promotionPrice - 促销价格（可选）
 * @param {Number} data.discountAmount - 优惠金额（可选）
 * @returns {Promise} 保存结果
 */
export function savePromotionUserCart(data) {
  return request({
    url: '/api/promotion/user-cart',
    method: 'post',
    data,
    needMerchantId: false
  })
}

