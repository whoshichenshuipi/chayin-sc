import request from '@/utils/request'

/**
 * 文件上传API
 */
export const uploadApi = {
  /**
   * 上传图片
   * @param {File} file 图片文件
   * @param {string} folder 文件夹名称
   * @returns {Promise} 上传结果
   */
  uploadImage(file, folder = 'images') {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('folder', folder)
    
    return request.post('/api/upload/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 上传文件
   * @param {File} file 文件
   * @param {string} folder 文件夹名称
   * @returns {Promise} 上传结果
   */
  uploadFile(file, folder = 'files') {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('folder', folder)
    
    return request.post('/api/upload/file', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 批量上传图片
   * @param {File[]} files 图片文件数组
   * @param {string} folder 文件夹名称
   * @returns {Promise} 上传结果
   */
  uploadImages(files, folder = 'images') {
    const formData = new FormData()
    files.forEach(file => {
      formData.append('files', file)
    })
    formData.append('folder', folder)
    
    return request.post('/api/upload/images/batch', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 删除文件
   * @param {string} fileUrl 文件URL
   * @returns {Promise} 删除结果
   */
  deleteFile(fileUrl) {
    return request.delete('/api/upload/file', {
      params: { fileUrl }
    })
  }
}
