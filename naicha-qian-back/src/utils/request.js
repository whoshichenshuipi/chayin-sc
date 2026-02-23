import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const request = axios.create({
  baseURL: 'http://localhost:8081', // 后端API地址
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const user = localStorage.getItem('user')
    if (user) {
      const userInfo = JSON.parse(user)
      if (userInfo.token) {
        config.headers.Authorization = `Bearer ${userInfo.token}`
      }
      
      // 添加商家ID到请求头
      // 如果是商家角色，使用merchantId或id作为X-Merchant-Id
      if (userInfo.role === 'merchant') {
        const merchantId = userInfo.merchantId || userInfo.id
        if (merchantId) {
          config.headers['X-Merchant-Id'] = merchantId
        }
      } else if (userInfo.merchantId) {
        // 如果不是商家角色但有merchantId字段，也添加
        config.headers['X-Merchant-Id'] = userInfo.merchantId
      }
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 检查响应是否是 HTML（说明接口路径错误）
    const contentType = response.headers['content-type'] || ''
    if (contentType.includes('text/html')) {
      console.error('接口返回了 HTML 而不是 JSON，可能是路径错误:', response.config.url)
      ElMessage.error('接口路径错误，请检查后端服务是否正常运行')
      return Promise.reject(new Error('接口返回了 HTML 而不是 JSON'))
    }
    
    const res = response.data
    
    // 检查是否是 Result 格式 { code, message, data, timestamp }
    if (res && typeof res === 'object' && 'code' in res) {
      // 如果返回的状态码不是200，说明接口调用失败
      if (res.code !== 200) {
        ElMessage.error(res.message || '请求失败')
        return Promise.reject(new Error(res.message || '请求失败'))
      }
      // 成功时返回整个 Result 对象，前端可以从 res.data 获取实际数据
      return res
    }
    
    // 如果直接返回数据（没有 Result 包装），直接返回
    return res
  },
  error => {
    console.error('响应错误:', error)
    
    let message = '网络错误'
    
    if (error.response) {
      const { status, data } = error.response
      
      // 检查是否是 HTML 响应
      const contentType = error.response.headers['content-type'] || ''
      if (contentType.includes('text/html')) {
        message = '接口路径错误，请检查后端服务是否正常运行'
        console.error('接口返回了 HTML:', error.config.url)
      } else {
        switch (status) {
          case 400:
            message = data?.message || '请求参数错误'
            break
          case 401:
            message = '未登录或登录已过期'
            // 清除本地存储的用户信息
            localStorage.removeItem('user')
            // 跳转到登录页
            if (window.location.pathname !== '/login') {
              window.location.href = '/login'
            }
            break
          case 403:
            message = '没有权限访问'
            break
          case 404:
            message = '请求的资源不存在'
            break
          case 500:
            message = '服务器内部错误'
            break
          default:
            message = data?.message || `请求失败 (${status})`
        }
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时'
    } else if (error.message.includes('Network Error')) {
      message = '网络连接失败'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
