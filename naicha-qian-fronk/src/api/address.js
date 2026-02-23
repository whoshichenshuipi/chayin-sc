import request from '@/utils/request'

/**
 * 地址管理相关API（用户端）
 */

/**
 * 获取用户地址列表
 * @returns {Promise} 地址列表
 */
export function getAddressList() {
  return request({
    url: '/api/address/list',
    method: 'get',
    needMerchantId: false,
    silent404: true // 404时不显示错误消息，由调用方处理
  }).catch(error => {
    // 如果接口不存在，返回空列表
    if (error.response?.status === 404) {
      console.warn('地址列表接口不存在，请后端添加')
      return []
    }
    throw error
  })
}

/**
 * 获取地址详情
 * @param {Number} addressId - 地址ID
 * @returns {Promise} 地址详情
 */
export function getAddressDetail(addressId) {
  return request({
    url: `/api/address/detail/${addressId}`,
    method: 'get',
    needMerchantId: false
  })
}

/**
 * 创建地址
 * @param {Object} addressData - 地址数据
 * @returns {Promise} 创建结果
 */
export function createAddress(addressData) {
  return request({
    url: '/api/address/create',
    method: 'post',
    data: addressData,
    needMerchantId: false,
    silent404: true // 404时不显示错误消息，由调用方处理
  })
}

/**
 * 更新地址
 * @param {Number} addressId - 地址ID
 * @param {Object} addressData - 地址数据
 * @returns {Promise} 更新结果
 */
export function updateAddress(addressId, addressData) {
  return request({
    url: `/api/address/update/${addressId}`,
    method: 'put',
    data: addressData,
    needMerchantId: false,
    silent404: true // 404时不显示错误消息，由调用方处理
  })
}

/**
 * 删除地址
 * @param {Number} addressId - 地址ID
 * @returns {Promise} 删除结果
 */
export function deleteAddress(addressId) {
  return request({
    url: `/api/address/delete/${addressId}`,
    method: 'delete',
    needMerchantId: false,
    silent404: true // 404时不显示错误消息，由调用方处理
  })
}

/**
 * 设置默认地址
 * @param {Number} addressId - 地址ID
 * @returns {Promise} 设置结果
 */
export function setDefaultAddress(addressId) {
  return request({
    url: `/api/address/set-default/${addressId}`,
    method: 'put',
    needMerchantId: false,
    silent404: true // 404时不显示错误消息，由调用方处理
  })
}

