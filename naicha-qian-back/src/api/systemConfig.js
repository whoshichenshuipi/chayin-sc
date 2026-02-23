import request from '@/utils/request'

/**
 * 系统配置API
 */

/**
 * 获取基础参数配置
 * @returns {Promise} 基础参数配置
 */
export function getBasicConfig() {
  return request({
    url: '/api/admin/system-config/basic',
    method: 'get'
  })
}

/**
 * 保存基础参数配置
 * @param {Object} config 基础参数配置
 * @returns {Promise} 保存结果
 */
export function saveBasicConfig(config) {
  return request({
    url: '/api/admin/system-config/basic',
    method: 'post',
    data: config
  })
}

/**
 * 获取支付方式配置
 * @returns {Promise} 支付方式配置
 */
export function getPaymentConfig() {
  return request({
    url: '/api/admin/system-config/payment',
    method: 'get'
  })
}

/**
 * 保存支付方式配置
 * @param {Object} config 支付方式配置
 * @returns {Promise} 保存结果
 */
export function savePaymentConfig(config) {
  return request({
    url: '/api/admin/system-config/payment',
    method: 'post',
    data: config
  })
}

/**
 * 获取配送规则配置
 * @returns {Promise} 配送规则配置
 */
export function getDeliveryConfig() {
  return request({
    url: '/api/admin/system-config/delivery',
    method: 'get'
  })
}

/**
 * 保存配送规则配置
 * @param {Object} config 配送规则配置
 * @returns {Promise} 保存结果
 */
export function saveDeliveryConfig(config) {
  return request({
    url: '/api/admin/system-config/delivery',
    method: 'post',
    data: config
  })
}

/**
 * 测试支付连接
 * @returns {Promise} 测试结果
 */
export function testPaymentConnection() {
  return request({
    url: '/api/admin/system-config/payment/test',
    method: 'post'
  })
}

