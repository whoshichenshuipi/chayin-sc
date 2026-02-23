import request from '@/utils/request'

/**
 * 商品管理相关API
 */

// 创建商品
export function createProduct(merchantId, data) {
  return request({
    url: `/api/product/create`,
    method: 'post',
    params: { merchantId },
    data
  })
}

// 更新商品
export function updateProduct(productId, data) {
  return request({
    url: `/api/product/update/${productId}`,
    method: 'put',
    data
  })
}

// 删除商品
export function deleteProduct(productId) {
  return request({
    url: `/api/product/delete/${productId}`,
    method: 'delete'
  })
}

// 查询商品详情
export function getProductDetail(productId) {
  return request({
    url: `/api/product/detail/${productId}`,
    method: 'get'
  })
}

// 分页查询商品列表
export function getProductList(params) {
  return request({
    url: `/api/product/list`,
    method: 'get',
    params
  })
}

// 上架商品
export function onSaleProduct(productId) {
  return request({
    url: `/api/product/on-sale/${productId}`,
    method: 'put'
  })
}

// 下架商品
export function offSaleProduct(productId) {
  return request({
    url: `/api/product/off-sale/${productId}`,
    method: 'put'
  })
}

// 调整库存
export function adjustStock(productId, stock) {
  return request({
    url: `/api/product/adjust-stock/${productId}`,
    method: 'put',
    params: { stock }
  })
}

// 获取商品分类列表
export function getProductCategories() {
  return request({
    url: `/api/category/tree`,
    method: 'get'
  })
}

