import { defineStore } from 'pinia'
import { getImageUrl } from '@/utils/imageUtils'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    isLoggedIn: false,
    token: null
  }),

  getters: {
    getUserName: (state) => state.userInfo?.username || '游客',
    getUserAvatar: (state) => {
      const avatar = state.userInfo?.avatar
      if (!avatar) {
        return 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHZpZXdCb3g9IjAgMCA0MCA0MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPGNpcmNsZSBjeD0iMjAiIGN5PSIyMCIgcj0iMjAiIGZpbGw9IiNFNjM5NDYiLz4KPHBhdGggZD0iTTIwIDEyQzE2LjY4NjMgMTIgMTQgMTQuNjg2MyAxNCAxOEMxNCAyMS4zMTM3IDE2LjY4NjMgMjQgMjAgMjRDMjMuMzEzNyAyNCAyNiAyMS4zMTM3IDI2IDE4QzI2IDE0LjY4NjMgMjMuMzEzNyAxMiAyMCAxMloiIGZpbGw9IndoaXRlIi8+CjxwYXRoIGQ9Ik0xMCAzMEMxMCAyNS41ODE3IDEzLjU4MTcgMjIgMTggMjJIMjJDMjYuNDE4MyAyMiAzMCAyNS41ODE3IDMwIDMwVjM0SDEwVjMwWiIgZmlsbD0id2hpdGUiLz4KPC9zdmc+'
      }
      // 如果已经是 data URI，直接返回
      if (avatar.startsWith('data:')) {
        return avatar
      }
      // 否则处理为完整的 URL
      return getImageUrl(avatar)
    }
  },

  actions: {
    initUser() {
      // 从本地存储恢复用户信息
      const token = localStorage.getItem('token')
      const userInfoStr = localStorage.getItem('userInfo')
      
      if (token && userInfoStr) {
        try {
          const userInfo = JSON.parse(userInfoStr)
          // 确保是普通用户（不是管理员或商家）
          if (userInfo.userType === 'user') {
            this.token = token
            this.userInfo = userInfo
            this.isLoggedIn = true
          } else {
            // 如果不是普通用户，清除数据
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
            this.token = null
            this.userInfo = null
            this.isLoggedIn = false
          }
        } catch (e) {
          console.error('解析用户信息失败:', e)
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          this.token = null
          this.userInfo = null
          this.isLoggedIn = false
        }
      } else {
        // 如果没有数据，重置状态
        this.token = null
        this.userInfo = null
        this.isLoggedIn = false
      }
    },

    login(userInfo, token) {
      this.userInfo = userInfo
      this.token = token
      this.isLoggedIn = true
      
      // 保存到本地存储
      localStorage.setItem('token', token)
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },

    logout() {
      this.userInfo = null
      this.token = null
      this.isLoggedIn = false
      
      // 清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },

    updateUserInfo(userInfo) {
      this.userInfo = { ...this.userInfo, ...userInfo }
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    }
  }
})
