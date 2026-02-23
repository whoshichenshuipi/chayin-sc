import request from '@/utils/request'

/**
 * 获取商品列表
 * @param {Object} params - 查询参数
 * @param {Number} params.current - 当前页，默认1
 * @param {Number} params.size - 每页大小，默认10
 * @param {Number} params.merchantId - 商家ID（可选）
 * @param {Number} params.categoryId - 分类ID（可选）
 * @param {Number} params.status - 商品状态：0-下架 1-上架（可选）
 * @param {String} params.name - 商品名称（模糊查询，可选）
 * @returns {Promise} 分页数据，包含 records 数组
 */
export function getProductList(params = {}) {
  return request({
    url: '/api/product/list',
    method: 'get',
    params: {
      current: params.current || params.page || 1,
      size: params.size || 10,
      merchantId: params.merchantId,
      categoryId: params.categoryId,
      status: params.status,
      name: params.name || params.keyword
    }
  })
}

/**
 * 获取商品详情
 * @param {Number} productId - 商品ID
 * @returns {Promise} 商品详情
 */
export function getProductDetail(productId) {
  return request({
    url: `/api/product/detail/${productId}`,
    method: 'get'
  })
}

/**
 * 获取推荐商品（上架且销量高的商品）
 * @param {Number} limit - 返回数量，默认4
 * @returns {Promise} 商品列表
 */
export function getRecommendedProducts(limit = 4) {
  return request({
    url: '/api/product/list',
    method: 'get',
    params: {
      current: 1,
      size: limit,
      status: 1 // 只获取上架商品（按销量排序由后端处理）
    }
  }).then(res => {
    // MyBatis Plus 分页返回格式：{ records: [], total: 0, current: 1, size: 10 }
    return res?.records || res?.list || (Array.isArray(res) ? res : [])
  })
}

/**
 * 获取新品上市（最近创建的上架商品）
 * @param {Number} limit - 返回数量，默认4
 * @returns {Promise} 商品列表
 */
export function getNewProducts(limit = 4) {
  return request({
    url: '/api/product/list',
    method: 'get',
    params: {
      current: 1,
      size: limit,
      status: 1 // 只获取上架商品（按创建时间排序由后端处理）
    }
  }).then(res => {
    // MyBatis Plus 分页返回格式：{ records: [], total: 0, current: 1, size: 10 }
    return res?.records || res?.list || (Array.isArray(res) ? res : [])
  })
}

