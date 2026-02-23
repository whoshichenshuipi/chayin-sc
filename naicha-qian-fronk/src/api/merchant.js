import request from '@/utils/request'

/**
 * 获取商家列表
 * @param {Object} params - 查询参数
 * @param {Number} params.current - 当前页，默认1
 * @param {Number} params.size - 每页大小，默认10
 * @param {Number} params.status - 状态：0-待审核 1-已审核（可选）
 * @param {Number} params.businessStatus - 营业状态：1-营业中（可选）
 * @param {String} params.keyword - 搜索关键词（可选）
 * @returns {Promise} 分页数据，包含 records 数组
 */
export function getMerchantList(params = {}) {
  return request({
    url: '/api/admin/merchant/list',
    method: 'get',
    params: {
      current: params.current || params.page || 1,
      size: params.size || 10,
      status: params.status || 1, // 默认只获取已审核的商家
      businessStatus: params.businessStatus || 1, // 默认只获取营业中的商家
      keyword: params.keyword
    }
  })
}

/**
 * 获取附近商家
 * @param {Number} limit - 返回数量，默认3
 * @returns {Promise} 商家列表
 */
export function getNearbyMerchants(limit = 3) {
  return request({
    url: '/api/admin/merchant/list',
    method: 'get',
    params: {
      current: 1,
      size: limit,
      status: 1, // 只获取已审核的商家
      businessStatus: 1 // 只获取营业中的商家
    }
  }).then(res => {
    // MyBatis Plus 分页返回格式：{ records: [], total: 0, current: 1, size: 10 }
    const merchants = res?.records || res?.list || (Array.isArray(res) ? res : [])
    // 返回列表数据，并计算距离（模拟）
    return merchants.map((merchant, index) => ({
      ...merchant,
      distance: (index + 1) * 0.5, // 模拟距离
      deliveryTime: 20 + index * 5, // 模拟配送时间
      minOrder: 20 + index * 5, // 模拟起送价
      deliveryFee: 3 + index, // 模拟配送费
      rating: merchant.rating ? parseFloat(merchant.rating) : 4.5 + Math.random() * 0.5, // 评分
      tags: merchant.tags || ['热销', '新品'] // 标签
    }))
  })
}

/**
 * 根据ID获取商家详情
 * @param {Number} merchantId - 商家ID
 * @returns {Promise} 商家详情
 */
export function getMerchantDetail(merchantId) {
  return request({
    url: `/api/admin/merchant/${merchantId}`,
    method: 'get'
  })
}

