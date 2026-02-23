import request from '@/utils/request'
import { getProductDetail } from '@/api/product'

/**
 * 购物车相关API（用户端）
 * 注意：购物车数据可能存储在前端，这里提供验证和同步接口
 */

/**
 * 验证购物车商品状态（检查价格、库存、是否下架）
 * @param {Array} cartItems - 购物车商品列表，每个商品包含 productId
 * @returns {Promise} 验证结果，包含每个商品的最新状态
 */
export async function validateCartItems(cartItems) {
  // 如果没有商品，返回空数组
  if (!cartItems || cartItems.length === 0) {
    return []
  }
  
  // 批量获取商品详情来验证状态
  const validationPromises = cartItems.map(async (item) => {
    try {
      const productId = item.productId || item.id
      if (!productId) {
        console.warn('购物车商品缺少 productId:', item)
        return {
          cartItemId: item.cartItemId,
          productId: null,
          isValid: false,
          isDiscontinued: true,
          error: '缺少商品ID'
        }
      }
      
      const product = await getProductDetail(productId)
      
      // 处理商品图片格式
      let imageUrl = '/product-default.jpg'
      if (product.images) {
        if (Array.isArray(product.images) && product.images.length > 0) {
          imageUrl = product.images[0]
        } else if (typeof product.images === 'string') {
          imageUrl = product.images
        }
      }
      
      // 检查价格是否变动
      const oldPrice = item.originalPrice || item.price
      const newPrice = product.price
      const hasPriceChanged = oldPrice && Number(oldPrice) !== Number(newPrice)
      
      return {
        cartItemId: item.cartItemId,
        productId: product.id,
        isValid: product.status === 1 && product.stock > 0, // 商品上架且有库存
        currentPrice: product.price,
        originalPrice: product.originalPrice || product.price,
        stock: product.stock,
        status: product.status,
        hasPriceChanged: hasPriceChanged,
        isDiscontinued: product.status !== 1 || product.stock === 0,
        stockStatus: product.stock < 10 ? 'low' : 'sufficient',
        product: {
          ...product,
          image: imageUrl
        }
      }
    } catch (error) {
      console.error(`验证商品 ${item.productId || item.id} 失败:`, error)
      // 验证失败，标记为无效
      return {
        cartItemId: item.cartItemId,
        productId: item.productId || item.id,
        isValid: false,
        isDiscontinued: true,
        error: error.message || '商品验证失败'
      }
    }
  })
  
  return Promise.all(validationPromises)
}

/**
 * 批量获取商品详情（用于验证购物车）
 * @param {Array} productIds - 商品ID数组
 * @returns {Promise} 商品详情列表
 */
export async function batchGetProductDetails(productIds) {
  const promises = productIds.map(id => getProductDetail(id))
  return Promise.all(promises).catch(error => {
    console.error('批量获取商品详情失败:', error)
    return []
  })
}

