import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建 axios 实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 添加 token
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    
    // 添加商户ID（如果需要）
    if (config.needMerchantId && userStore.userInfo?.merchantId) {
      config.headers['X-Merchant-Id'] = userStore.userInfo.merchantId
    }
    
    // 添加用户ID（如果存在用户信息）
    if (userStore.userInfo?.id) {
      config.headers['X-User-Id'] = userStore.userInfo.id
    }
    
    // 合并自定义headers（如果传入customHeaders）
    if (config.customHeaders && typeof config.customHeaders === 'object') {
      Object.assign(config.headers, config.customHeaders)
    }
    
    // 添加用户类型（如果headers中指定了X-User-Type）
    if (config.headers && config.headers['X-User-Type']) {
      // 用户类型已在customHeaders中设置，这里不需要额外处理
    }
    
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    
    // 如果后端返回的数据格式是 Result 包装的
    if (res && typeof res === 'object' && res.code !== undefined) {
      if (res.code === 200 || res.success === true) {
        // 返回数据部分
        // 如果 data 存在，返回 data（可能是字符串、对象或数组）
        if (res.data !== undefined && res.data !== null) {
          return res.data
        }
        // 如果 data 不存在，记录警告并返回 null
        // 注意：某些接口可能只返回 message，没有 data
        console.warn('接口返回成功但没有 data 字段:', {
          code: res.code,
          message: res.message,
          fullResponse: res
        })
        return null
      } else {
        // 业务错误
        ElMessage.error(res.message || res.msg || '请求失败')
        return Promise.reject(new Error(res.message || res.msg || '请求失败'))
      }
    }
    
    // 直接返回数据（如果不是 Result 格式）
    return res
  },
  (error) => {
    console.error('响应错误:', error)
    
    if (error.response) {
      const { status, data } = error.response
      // 从 error.config 获取请求配置
      const config = error.config || {}
      
      switch (status) {
        case 401:
          ElMessage.error('未授权，请重新登录')
          const userStore = useUserStore()
          userStore.logout()
          router.push('/login')
          break
        case 403:
          ElMessage.error('拒绝访问')
          break
        case 404:
          // 如果配置了silent404，则不显示错误消息（让调用方自行处理）
          if (!config.silent404) {
            ElMessage.error('请求的资源不存在')
          }
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(data?.message || data?.msg || `请求失败: ${status}`)
      }
    } else if (error.request) {
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      ElMessage.error(error.message || '请求失败')
    }
    
    return Promise.reject(error)
  }
)

export default request

