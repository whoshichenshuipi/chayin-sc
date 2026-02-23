/**
 * 图片工具函数
 */

// API 基础 URL
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081'

/**
 * 处理图片 URL，确保是完整的 URL
 * @param {String} imageUrl - 图片 URL（可能是相对路径或完整 URL）
 * @returns {String} 完整的图片 URL
 */
export function getImageUrl(imageUrl) {
  if (!imageUrl) {
    return null
  }
  
  // 确保是字符串类型
  if (typeof imageUrl !== 'string') {
    console.warn('getImageUrl 接收到非字符串类型:', typeof imageUrl, imageUrl)
    return null
  }
  
  // 如果包含中文或其他无效字符（可能是错误消息），返回 null
  if (/[\u4e00-\u9fa5]/.test(imageUrl) && !imageUrl.startsWith('data:')) {
    console.warn('URL 包含中文字符，可能是错误消息:', imageUrl)
    return null
  }
  
  // 如果已经是完整的 URL（http:// 或 https:// 开头），直接返回
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl
  }
  
  // 如果是 data URI（base64），直接返回
  if (imageUrl.startsWith('data:')) {
    return imageUrl
  }
  
  // 如果是 Vite 处理的资源路径（/assets/ 或 /src/assets/），直接返回
  // Vite 会自动处理这些路径
  if (imageUrl.startsWith('/assets/') || imageUrl.startsWith('/src/assets/')) {
    return imageUrl
  }
  
  // 如果是相对路径（以 / 开头但不是 assets），拼接基础 URL
  if (imageUrl.startsWith('/')) {
    const path = imageUrl.substring(1)
    return `${API_BASE_URL}/${path}`
  }
  
  // 如果是相对路径（不以 / 开头），拼接基础 URL
  return `${API_BASE_URL}/${imageUrl}`
}

// 默认头像（SVG base64）
const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHZpZXdCb3g9IjAgMCA0MCA0MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPGNpcmNsZSBjeD0iMjAiIGN5PSIyMCIgcj0iMjAiIGZpbGw9IiNFNjM5NDYiLz4KPHBhdGggZD0iTTIwIDEyQzE2LjY4NjMgMTIgMTQgMTQuNjg2MyAxNCAxOEMxNCAyMS4zMTM3IDE2LjY4NjMgMjQgMjAgMjRDMjMuMzEzNyAyNCAyNiAyMS4zMTM3IDI2IDE4QzI2IDE0LjY4NjMgMjMuMzEzNyAxMiAyMCAxMloiIGZpbGw9IndoaXRlIi8+CjxwYXRoIGQ9Ik0xMCAzMEMxMCAyNS41ODE3IDEzLjU4MTcgMjIgMTggMjJIMjJDMjYuNDE4MyAyMiAzMCAyNS41ODE3IDMwIDMwVjM0SDEwVjMwWiIgZmlsbD0id2hpdGUiLz4KPC9zdmc+'

// 默认商品图片（SVG base64）
export const defaultProductImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgdmlld0JveD0iMCAwIDIwMCAyMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIiBmaWxsPSIjRjVGNUY1Ii8+CjxwYXRoIGQ9Ik0xMDAgNzBDODQuNjg2MyA3MCA3MiA4Mi42ODYzIDcyIDk4QzcyIDExMy4zMTQgODQuNjg2MyAxMjYgMTAwIDEyNkMxMTUuMzE0IDEyNiAxMjggMTEzLjMxNCAxMjggOThDMTI4IDgyLjY4NjMgMTE1LjMxNCA3MCAxMDAgNzBaIiBmaWxsPSIjQ0NDQ0NDIi8+CjxwYXRoIGQ9Ik01MCAxNDBDNTAgMTI1LjU4MiA2My41ODE3IDExMiA4MCAxMTJIMTIwQzEzNi40MTggMTEyIDE1MCAxMjUuNTgyIDE1MCAxNDBWMTYwSDUwVjE0MFoiIGZpbGw9IiNDQ0NDQ0MiLz4KPC9zdmc+'

/**
 * 处理图片加载错误，返回默认图片
 * @param {Event} event - 图片加载错误事件
 * @param {String} defaultImage - 默认图片 URL（可选）
 * @returns {String} 默认图片 URL
 */
export function handleImageError(event, defaultImage = null) {
  if (event && event.target) {
    const target = event.target
    const currentSrc = target.src
    
    // 避免无限循环
    if (defaultImage && currentSrc === defaultImage) {
      return defaultImage
    }
    
    // 如果当前已经是默认图片，不再替换
    if (currentSrc === defaultAvatar || currentSrc === defaultProductImage) {
      return currentSrc
    }
    
    // 记录错误日志（仅在开发环境）
    if (import.meta.env.DEV) {
      console.warn('图片加载失败，使用默认图片:', currentSrc)
    }
    
    // 使用自定义默认图片，如果没有则使用默认头像
    const fallbackImage = defaultImage || defaultAvatar
    target.src = fallbackImage
    
    return fallbackImage
  }
  
  return defaultImage || defaultAvatar
}

/**
 * 验证图片 URL 是否有效
 * @param {String} imageUrl - 图片 URL
 * @returns {Boolean} 是否为有效的图片 URL
 */
export function isValidImageUrl(imageUrl) {
  if (!imageUrl) return false
  
  // 检查是否是 data URI
  if (imageUrl.startsWith('data:')) return true
  
  // 检查是否是完整的 URL
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) return true
  
  // 检查是否是相对路径
  if (imageUrl.startsWith('/') || imageUrl.includes('.')) return true
  
  return false
}

