import request from '@/utils/request'

/**
 * 用户管理相关API（管理员端）
 */

// 分页查询用户列表
export function getUserList(params) {
  return request({
    url: '/api/admin/user/list',
    method: 'get',
    params
  })
}

// 获取用户详情
export function getUserDetail(userId) {
  return request({
    url: `/api/admin/user/${userId}`,
    method: 'get'
  })
}

// 处理用户违规
export function handleUserViolation(data) {
  return request({
    url: '/api/admin/user/violation',
    method: 'post',
    data
  })
}

