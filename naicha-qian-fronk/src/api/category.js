import request from '@/utils/request'

/**
 * 获取分类树
 * @returns {Promise} 分类树列表
 */
export function getCategoryTree() {
  return request({
    url: '/api/category/tree',
    method: 'get'
  })
}

/**
 * 根据ID获取分类详情
 * @param {Number} categoryId - 分类ID
 * @returns {Promise} 分类详情
 */
export function getCategoryById(categoryId) {
  return request({
    url: `/api/category/${categoryId}`,
    method: 'get'
  })
}

