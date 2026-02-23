import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Layout from '@/components/Layout.vue'

// 管理员路由
const adminRoutes = [
  {
    path: '/admin',
    component: Layout,
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '管理员仪表盘', role: 'admin' }
      },
      {
        path: 'system/config',
        name: 'SystemConfig',
        component: () => import('@/views/admin/SystemConfig.vue'),
        meta: { title: '系统配置', role: 'admin' }
      },
      {
        path: 'system/logs',
        name: 'SystemLogs',
        component: () => import('@/views/admin/SystemLogs.vue'),
        meta: { title: '系统日志', role: 'admin' }
      },
      {
        path: 'merchant/list',
        name: 'MerchantList',
        component: () => import('@/views/admin/MerchantList.vue'),
        meta: { title: '商家列表', role: 'admin' }
      },
      {
        path: 'merchant/audit',
        name: 'MerchantAudit',
        component: () => import('@/views/admin/MerchantAudit.vue'),
        meta: { title: '商家审核', role: 'admin' }
      },
      {
        path: 'merchant/rating',
        name: 'MerchantRating',
        component: () => import('@/views/admin/MerchantRating.vue'),
        meta: { title: '商家评分', role: 'admin' }
      },
      {
        path: 'user/list',
        name: 'UserList',
        component: () => import('@/views/admin/UserList.vue'),
        meta: { title: '用户列表', role: 'admin' }
      },
      {
        path: 'user/feedback',
        name: 'UserFeedback',
        component: () => import('@/views/admin/UserFeedback.vue'),
        meta: { title: '用户反馈', role: 'admin' }
      },
      {
        path: 'data/overview',
        name: 'DataOverview',
        component: () => import('@/views/admin/DataOverview.vue'),
        meta: { title: '数据概览', role: 'admin' }
      },
      {
        path: 'data/reports',
        name: 'DataReports',
        component: () => import('@/views/admin/DataReports.vue'),
        meta: { title: '统计报表', role: 'admin' }
      },
      {
        path: 'notifications',
        name: 'NotificationCenter',
        component: () => import('@/views/admin/NotificationCenter.vue'),
        meta: { title: '消息通知', role: 'admin' }
      },
      {
        path: 'help',
        name: 'HelpCenter',
        component: () => import('@/views/admin/HelpCenter.vue'),
        meta: { title: '帮助中心', role: 'admin' }
      }
    ]
  }
]

// 商家路由
const merchantRoutes = [
  {
    path: '/merchant',
    component: Layout,
    children: [
      {
        path: 'dashboard',
        name: 'MerchantDashboard',
        component: () => import('@/views/merchant/Dashboard.vue'),
        meta: { title: '商家仪表盘', role: 'merchant' }
      },
      {
        path: 'shop/info',
        name: 'ShopInfo',
        component: () => import('@/views/merchant/ShopInfo.vue'),
        meta: { title: '店铺信息', role: 'merchant' }
      },
      {
        path: 'shop/rating',
        name: 'ShopRating',
        component: () => import('@/views/merchant/ShopRating.vue'),
        meta: { title: '评分评价', role: 'merchant' }
      },
      {
        path: 'product/list',
        name: 'ProductList',
        component: () => import('@/views/merchant/ProductList.vue'),
        meta: { title: '商品列表', role: 'merchant' }
      },
      {
        path: 'product/add',
        name: 'ProductAdd',
        component: () => import('@/views/merchant/ProductAdd.vue'),
        meta: { title: '添加商品', role: 'merchant' }
      },
      {
        path: 'product/category',
        name: 'ProductCategory',
        component: () => import('@/views/merchant/ProductCategory.vue'),
        meta: { title: '商品分类', role: 'merchant' }
      },
      {
        path: 'order/list',
        name: 'OrderList',
        component: () => import('@/views/merchant/OrderList.vue'),
        meta: { title: '订单列表', role: 'merchant' }
      },
      {
        path: 'order/process',
        name: 'OrderProcess',
        component: () => import('@/views/merchant/OrderProcess.vue'),
        meta: { title: '订单处理', role: 'merchant' }
      },
      {
        path: 'order/shipping',
        name: 'OrderShipping',
        component: () => import('@/views/merchant/OrderShipping.vue'),
        meta: { title: '发货跟踪', role: 'merchant' }
      },
      {
        path: 'order/refund',
        name: 'OrderRefund',
        component: () => import('@/views/merchant/OrderRefund.vue'),
        meta: { title: '退款处理', role: 'merchant' }
      },
      {
        path: 'order/management',
        name: 'OrderManagement',
        component: () => import('@/views/merchant/OrderManagement.vue'),
        meta: { title: '订单管理', role: 'merchant' }
      },
      {
        path: 'marketing/coupon',
        name: 'CouponManagement',
        component: () => import('@/views/merchant/CouponManagement.vue'),
        meta: { title: '优惠券管理', role: 'merchant' }
      },
      {
        path: 'marketing/promotion',
        name: 'PromotionManagement',
        component: () => import('@/views/merchant/PromotionManagement.vue'),
        meta: { title: '促销活动', role: 'merchant' }
      },
      {
        path: 'finance/income',
        name: 'FinanceIncome',
        component: () => import('@/views/merchant/FinanceIncome.vue'),
        meta: { title: '收入统计', role: 'merchant' }
      },
      {
        path: 'finance/withdraw',
        name: 'FinanceWithdraw',
        component: () => import('@/views/merchant/FinanceWithdraw.vue'),
        meta: { title: '提现管理', role: 'merchant' }
      },
      {
        path: 'analytics/sales',
        name: 'AnalyticsSales',
        component: () => import('@/views/merchant/AnalyticsSales.vue'),
        meta: { title: '销售统计', role: 'merchant' }
      },
      {
        path: 'analytics/customer',
        name: 'AnalyticsCustomer',
        component: () => import('@/views/merchant/AnalyticsCustomer.vue'),
        meta: { title: '客户分析', role: 'merchant' }
      },
      {
        path: 'notifications',
        name: 'MerchantNotificationCenter',
        component: () => import('@/views/merchant/NotificationCenter.vue'),
        meta: { title: '消息通知', role: 'merchant' }
      }
    ]
  }
]

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import(/* @vite-ignore */ '@/views/Login.vue'),
    meta: { title: '登录' }
  },
  ...adminRoutes,
  ...merchantRoutes,
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

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 恢复用户信息
  if (!userStore.isLoggedIn) {
    userStore.restoreUser()
  }
  
  // 如果访问登录页且已登录，重定向到对应仪表盘
  // 注意：普通用户登录后应该跳转到前端界面，不会进入这个路由守卫
  if (to.path === '/login' && userStore.isLoggedIn) {
    if (userStore.isAdmin) {
      next('/admin/dashboard')
    } else if (userStore.isMerchant) {
      next('/merchant/dashboard')
    } else {
      // 普通用户不应该在后台系统登录，如果已经登录应该跳转到前端界面
      // 但要避免循环重定向，检查来源
      if (from.path !== '/home' && from.path !== '/') {
        window.location.replace('http://localhost:3000/home')
      } else {
        next()
      }
      return
    }
    return
  }
  
  // 如果访问需要权限的页面
  if (to.meta.role) {
    if (!userStore.isLoggedIn) {
      next('/login')
      return
    }
    
    if (to.meta.role !== userStore.role) {
      // 权限不匹配，重定向到对应仪表盘
      if (userStore.isAdmin) {
        next('/admin/dashboard')
      } else if (userStore.isMerchant) {
        next('/merchant/dashboard')
      } else {
        next('/login')
      }
      return
    }
  }
  
  next()
})

export default router
