import request from '@/utils/request'

/**
 * 商品分类相关API
 */

// 获取分类树
export function getCategoryTree() {
  return request({
    url: `/api/category/tree`,
    method: 'get'
  })
}

// 查询分类详情
export function getCategoryById(id) {
  return request({
    url: `/api/category/${id}`,
    method: 'get'
  })
}

// 创建分类
export function createCategory(data) {
  return request({
    url: `/api/category`,
    method: 'post',
    data
  })
}

// 更新分类
export function updateCategory(id, data) {
  return request({
    url: `/api/category/${id}`,
    method: 'put',
    data
  })
}

// 删除分类
export function deleteCategory(id) {
  return request({
    url: `/api/category/${id}`,
    method: 'delete'
  })
}

// 更新分类状态
export function updateCategoryStatus(id, status) {
  return request({
    url: `/api/category/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 更新分类排序
export function updateCategorySort(ids) {
  return request({
    url: `/api/category/sort`,
    method: 'put',
    data: ids
  })
}

// 获取分类下的商品数量
export function getCategoryProductCount(id) {
  return request({
    url: `/api/category/${id}/product-count`,
    method: 'get'
  })
}

