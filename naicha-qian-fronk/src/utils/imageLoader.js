/**
 * 图片资源加载工具
 * 动态导入 assets 目录下的所有图片
 */

// 导入所有图片资源
const imageModules = import.meta.glob('@/assets/*.webp', { eager: true })

// 获取所有图片路径
export const getAllImages = () => {
  const images = []
  for (const path in imageModules) {
    images.push(imageModules[path].default || path)
  }
  return images
}

// 获取随机图片
export const getRandomImage = () => {
  const images = getAllImages()
  if (images.length === 0) return null
  return images[Math.floor(Math.random() * images.length)]
}

// 获取指定索引的图片（循环使用）
export const getImageByIndex = (index) => {
  const images = getAllImages()
  if (images.length === 0) return null
  return images[index % images.length]
}

// 获取多个不重复的随机图片
export const getRandomImages = (count) => {
  const images = getAllImages()
  if (images.length === 0) return []
  
  const shuffled = [...images].sort(() => Math.random() - 0.5)
  return shuffled.slice(0, Math.min(count, images.length))
}

// 获取循环使用的图片列表
export const getImagesForCount = (count) => {
  const images = getAllImages()
  if (images.length === 0) return []
  
  const result = []
  for (let i = 0; i < count; i++) {
    result.push(images[i % images.length])
  }
  return result
}

// 获取指定范围的图片（如果超出范围则循环使用）
export const getImagesByRange = (start, count) => {
  const images = getAllImages()
  if (images.length === 0) return []
  
  const result = []
  for (let i = 0; i < count; i++) {
    result.push(images[(start + i) % images.length])
  }
  return result
}

