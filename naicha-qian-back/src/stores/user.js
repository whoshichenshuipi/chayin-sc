import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    id: null,
    name: '',
    role: '', // 'admin' | 'merchant'
    token: '',
    permissions: [],
    merchantId: null // 商家ID（商家登录时，merchantId = id）
  }),
  
  getters: {
    isAdmin: (state) => state.role === 'admin',
    isMerchant: (state) => state.role === 'merchant',
    isLoggedIn: (state) => !!state.token,
    // 获取商家ID（如果是商家，返回id；否则返回null）
    getMerchantId: (state) => {
      if (state.role === 'merchant') {
        return state.merchantId || state.id
      }
      return null
    }
  },
  
  actions: {
    // 登录
    login(userInfo) {
      this.id = userInfo.id
      this.name = userInfo.name
      this.role = userInfo.role
      this.token = userInfo.token
      this.permissions = userInfo.permissions || []
      
      // 如果是商家，merchantId就是id
      if (userInfo.role === 'merchant') {
        this.merchantId = userInfo.merchantId || userInfo.id
      } else {
        this.merchantId = null
      }
      
      // 保存到本地存储
      localStorage.setItem('user', JSON.stringify({
        id: this.id,
        name: this.name,
        role: this.role,
        token: this.token,
        permissions: this.permissions,
        merchantId: this.merchantId
      }))
    },
    
    // 退出登录
    logout() {
      this.id = null
      this.name = ''
      this.role = ''
      this.token = ''
      this.permissions = []
      this.merchantId = null
      
      localStorage.removeItem('user')
    },
    
    // 从本地存储恢复用户信息
    restoreUser() {
      const userStr = localStorage.getItem('user')
      if (userStr) {
        const user = JSON.parse(userStr)
        this.id = user.id
        this.name = user.name
        this.role = user.role
        this.token = user.token
        this.permissions = user.permissions || []
        // 兼容旧数据：如果是商家但没有merchantId，使用id作为merchantId
        if (user.role === 'merchant') {
          this.merchantId = user.merchantId || user.id
        } else {
          this.merchantId = user.merchantId || null
        }
      }
    },
    
    // 检查权限
    hasPermission(permission) {
      return this.permissions.includes(permission)
    }
  }
})
