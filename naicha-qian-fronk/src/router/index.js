import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue').catch((err) => {
          // 如果动态导入失败，记录错误并提示用户刷新页面
          console.error('动态导入 Home.vue 失败:', err)
          console.error('请尝试清除浏览器缓存并刷新页面 (Ctrl+Shift+R)')
          // 返回一个简单的错误组件或重试导入
          return import('@/views/Home.vue').catch(() => {
            // 如果再次失败，返回 NotFound 组件作为后备
            return import('@/views/NotFound.vue')
          })
        }),
        meta: { title: '首页', requireAuth: true }
      },
      {
        path: 'products',
        name: 'Products',
        component: () => import('@/views/Products.vue'),
        meta: { title: '商品浏览' }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/Cart.vue'),
        meta: { title: '购物车', requireAuth: true }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/Orders.vue'),
        meta: { title: '我的订单', requireAuth: true }
      },
      {
        path: 'activities',
        name: 'Activities',
        component: () => import('@/views/Activities.vue'),
        meta: { title: '营销活动' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', requireAuth: true }
      },
      {
        path: 'address',
        name: 'AddressManagement',
        component: () => import('@/views/AddressManagement.vue'),
        meta: { title: '收货地址', requireAuth: true }
      },
      {
        path: 'products/:id',
        name: 'ProductDetail',
        component: () => import('@/views/ProductDetail.vue'),
        meta: { title: '商品详情' }
      },
      {
        path: 'order-confirm',
        name: 'OrderConfirm',
        component: () => import('@/views/OrderConfirm.vue'),
        meta: { title: '订单确认', requireAuth: true }
      },
      {
        path: 'order-detail',
        name: 'OrderDetail',
        component: () => import('@/views/OrderDetail.vue'),
        meta: { title: '订单详情', requireAuth: true }
      },
      {
        path: 'order-tracking',
        name: 'OrderTracking',
        component: () => import('@/views/OrderTracking.vue'),
        meta: { title: '订单跟踪', requireAuth: true }
      },
      {
        path: 'after-sale',
        name: 'AfterSale',
        component: () => import('@/views/AfterSale.vue'),
        meta: { title: '申请售后', requireAuth: true }
      },
      {
        path: 'messages',
        name: 'MessageCenter',
        component: () => import('@/views/MessageCenter.vue'),
        meta: { title: '我的消息', requireAuth: true }
      },
      {
        path: 'help',
        name: 'HelpCenter',
        component: () => import('@/views/HelpCenter.vue'),
        meta: { title: '帮助中心' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：检查登录状态
let redirecting = false // 防止重复重定向的标志

router.beforeEach((to, from, next) => {
  // 如果正在重定向，直接返回，避免循环
  if (redirecting) {
    console.log('路由守卫：正在重定向中，跳过本次守卫')
    return
  }
  
  console.log('路由守卫：检查路由', to.path, '来源:', from.path)
  
  // 检查 URL 参数中是否有登录信息（从后台系统跳转过来时可能通过 URL 传递）
  const urlParams = new URLSearchParams(window.location.search)
  const tokenFromUrl = urlParams.get('token')
  const userInfoFromUrl = urlParams.get('userInfo')
  
  // 如果 URL 中有用户信息，保存到 localStorage（处理跨域跳转的情况）
  if (tokenFromUrl && userInfoFromUrl) {
    try {
      const decodedUserInfo = JSON.parse(decodeURIComponent(userInfoFromUrl))
      console.log('从 URL 参数中获取用户信息，保存到 localStorage', {
        userId: decodedUserInfo.id,
        userType: decodedUserInfo.userType
      })
      localStorage.setItem('token', tokenFromUrl)
      localStorage.setItem('userInfo', JSON.stringify(decodedUserInfo))
      console.log('URL 参数中的用户信息已保存到 localStorage')
      // 清除 URL 参数，避免暴露敏感信息
      const newUrl = window.location.pathname + window.location.hash
      window.history.replaceState({}, '', newUrl)
      console.log('URL 参数已清除')
    } catch (e) {
      console.error('解析 URL 中的用户信息失败:', e)
    }
  }
  
  const userStore = useUserStore()
  
  // 初始化用户信息（从 localStorage 恢复）
  userStore.initUser()
  
  // 如果 URL 中有用户信息，也需要更新 store
  if (tokenFromUrl && userInfoFromUrl) {
    try {
      const decodedUserInfo = JSON.parse(decodeURIComponent(userInfoFromUrl))
      if (decodedUserInfo.userType === 'user') {
        userStore.login(decodedUserInfo, tokenFromUrl)
        console.log('从 URL 参数更新 store 中的用户信息')
      }
    } catch (e) {
      console.error('从 URL 参数更新 store 失败:', e)
    }
  }
  
  // 如果路由需要登录，检查是否已登录
  if (to.meta.requireAuth) {
    // 重新从 localStorage 读取（因为可能刚刚从 URL 参数保存）
    const token = localStorage.getItem('token')
    const userInfoStr = localStorage.getItem('userInfo')
    
    // 如果 localStorage 中没有，但 URL 中有，使用 URL 中的
    let finalToken = token || tokenFromUrl
    let finalUserInfoStr = userInfoStr || userInfoFromUrl
    
    console.log('路由需要登录，检查登录状态:', { 
      hasToken: !!token, 
      hasUserInfo: !!userInfoStr,
      hasTokenFromUrl: !!tokenFromUrl,
      hasUserInfoFromUrl: !!userInfoFromUrl,
      finalToken: !!finalToken,
      finalUserInfoStr: !!finalUserInfoStr,
      isLoggedIn: userStore.isLoggedIn,
      hasUserInfoInStore: !!userStore.userInfo
    })
    
    // 如果仍然没有 token 或 userInfo，说明未登录
    if (!finalToken || !finalUserInfoStr) {
      console.log('未登录，准备跳转到登录页')
      // 设置重定向标志，避免重复跳转
      redirecting = true
      // 只有不是从外部跳转过来的时候才提示（避免从3001登录后跳转过来时提示）
      if (from.path && from.path !== '/login' && from.name !== 'Login' && !from.path.includes('localhost:3001')) {
        ElMessage.warning('请先登录')
      }
      setTimeout(() => {
        redirecting = false // 重置标志
        window.location.replace('http://localhost:3001/login')
      }, 100)
      return
    }
    
    // 如果 finalUserInfoStr 是从 URL 参数获取的，需要解码
    if (!userInfoStr && userInfoFromUrl) {
      try {
        finalUserInfoStr = decodeURIComponent(userInfoFromUrl)
        console.log('使用 URL 参数中的用户信息')
      } catch (e) {
        console.error('解码 URL 参数中的用户信息失败:', e)
        redirecting = true
        setTimeout(() => {
          redirecting = false
          window.location.replace('http://localhost:3001/login')
        }, 100)
        return
      }
    }
    
    // 解析用户信息并验证
    try {
      const userInfo = JSON.parse(finalUserInfoStr)
      console.log('解析用户信息成功:', {
        userType: userInfo.userType,
        userId: userInfo.id,
        username: userInfo.username
      })
      
      // 确保是普通用户
      if (userInfo.userType !== 'user') {
        // 不是普通用户，清除数据并跳转
        console.log('不是普通用户，类型:', userInfo.userType, '清除数据并跳转到后台')
        redirecting = true
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        setTimeout(() => {
          redirecting = false
          window.location.replace('http://localhost:3001/login')
        }, 100)
        return
      }
      
      // 确保 userStore 中有数据
      if (!userStore.isLoggedIn || !userStore.userInfo) {
        // 更新 store（使用 finalToken）
        userStore.login(userInfo, finalToken)
        console.log('用户登录状态已更新到store，允许访问')
      } else {
        console.log('用户已登录，store中已有数据，允许访问')
      }
    } catch (e) {
      console.error('解析用户信息失败:', e, '原始数据:', finalUserInfoStr)
      redirecting = true
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      setTimeout(() => {
        redirecting = false
        window.location.replace('http://localhost:3001/login')
      }, 100)
      return
    }
  } else {
    console.log('路由不需要登录，直接允许访问')
  }
  
  // 重置重定向标志
  redirecting = false
  next()
})

export default router
