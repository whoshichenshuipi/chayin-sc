import request from '@/utils/request'

/**
 * 系统日志相关API
 */

/**
 * 分页查询日志列表
 * @param {Object} params 查询参数
 * @param {string} params.type 日志类型：operation-操作日志，system-系统日志
 * @param {string} params.level 日志级别：info, warn, error, debug
 * @param {string} params.module 操作模块
 * @param {string} params.user 操作用户
 * @param {string} params.startTime 开始时间
 * @param {string} params.endTime 结束时间
 * @param {number} params.current 当前页
 * @param {number} params.size 每页大小
 */
export function getLogList(params) {
  return request({
    url: '/api/admin/system-log/list',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取日志详情
 * @param {number} id 日志ID
 */
export function getLogById(id) {
  return request({
    url: `/api/admin/system-log/${id}`,
    method: 'get'
  })
}

/**
 * 手动备份日志
 */
export function backupLogs() {
  return request({
    url: '/api/admin/system-log/backup',
    method: 'post'
  })
}

/**
 * 导出日志
 * @param {Object} queryDTO 查询条件
 */
export function exportLogs(queryDTO) {
  return request({
    url: '/api/admin/system-log/export',
    method: 'post',
    data: queryDTO
  })
}

/**
 * 获取自动备份配置
 */
export function getBackupConfig() {
  return request({
    url: '/api/admin/system-log/backup/config',
    method: 'get'
  })
}

/**
 * 保存自动备份配置
 * @param {Object} configDTO 备份配置
 */
export function saveBackupConfig(configDTO) {
  return request({
    url: '/api/admin/system-log/backup/config',
    method: 'post',
    data: configDTO
  })
}

/**
 * 清理过期日志
 * @param {number} retentionDays 保留天数
 */
export function cleanLogs(retentionDays) {
  return request({
    url: '/api/admin/system-log/clean',
    method: 'post',
    params: { retentionDays }
  })
}

