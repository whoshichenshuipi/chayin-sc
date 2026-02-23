import request from '@/utils/request'

/**
 * 财务管理相关API
 */

// 获取财务概览
export function getFinanceSummary(startTime, endTime) {
  return request({
    url: '/api/finance/summary',
    method: 'get',
    params: {
      startTime: startTime ? startTime : undefined,
      endTime: endTime ? endTime : undefined
    }
  })
}

// 分页查询收入记录
export function getIncomeRecords(data) {
  return request({
    url: '/api/finance/income/records',
    method: 'post',
    data
  })
}

// 分页查询支出记录
export function getExpenseRecords(data) {
  return request({
    url: '/api/finance/expense/records',
    method: 'post',
    data
  })
}

// 导出收入明细
export function exportIncomeData(data) {
  return request({
    url: '/api/finance/income/export',
    method: 'post',
    data,
    responseType: 'blob'
  })
}

// 导出支出明细
export function exportExpenseData(data) {
  return request({
    url: '/api/finance/expense/export',
    method: 'post',
    data,
    responseType: 'blob'
  })
}

