import request from '@/utils/request'

/**
 * 上传图片
 * @param {File} file - 图片文件
 * @param {String} folder - 文件夹名称（可选）
 * @returns {Promise<String>} 图片URL字符串
 */
export function uploadImage(file, folder = 'avatars') {
  const formData = new FormData()
  formData.append('file', file)
  if (folder) {
    formData.append('folder', folder)
  }
  
  return request({
    url: '/api/upload/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(result => {
    console.log('上传接口原始返回:', result, typeof result)
    
    // 响应拦截器应该已经提取了 res.data，所以这里 result 应该是字符串 URL
    if (typeof result === 'string') {
      // 验证是否是有效的 URL
      if (result && result.length > 0) {
        // 检查是否包含中文（可能是错误消息）
        if (/[\u4e00-\u9fa5]/.test(result)) {
          console.error('上传返回包含中文，可能是错误消息:', result)
          throw new Error('上传接口返回格式错误：' + result)
        }
        return result
      } else {
        console.error('上传返回空字符串')
        throw new Error('上传接口返回空数据')
      }
    } else if (result && typeof result === 'object') {
      // 如果响应拦截器没有正确处理，返回了整个对象
      console.warn('响应拦截器可能未正确处理，收到对象:', result)
      // 尝试从对象中提取 URL
      const url = result.data || result.url || result.imageUrl
      if (typeof url === 'string' && url.length > 0 && !/[\u4e00-\u9fa5]/.test(url)) {
        return url
      }
      // 如果提取失败，抛出错误
      console.error('无法从返回对象中提取有效 URL:', result)
      throw new Error('上传接口返回格式错误：无法提取图片 URL')
    } else if (result === null || result === undefined) {
      console.error('上传返回 null 或 undefined')
      throw new Error('上传接口返回空数据')
    } else {
      console.error('上传接口返回未知格式:', typeof result, result)
      throw new Error('上传接口返回格式错误')
    }
  }).catch(error => {
    console.error('上传图片失败:', error)
    throw error
  })
}

/**
 * 批量上传图片
 * @param {File[]} files - 图片文件数组
 * @param {String} folder - 文件夹名称（可选）
 * @returns {Promise} 图片URL数组
 */
export function uploadImages(files, folder = 'images') {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  if (folder) {
    formData.append('folder', folder)
  }
  
  return request({
    url: '/api/upload/images/batch',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除文件
 * @param {String} fileUrl - 文件URL
 * @returns {Promise} 删除结果
 */
export function deleteFile(fileUrl) {
  return request({
    url: '/api/upload/file',
    method: 'delete',
    params: { fileUrl }
  })
}

