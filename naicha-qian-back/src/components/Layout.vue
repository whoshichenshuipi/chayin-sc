<template>
  <div class="admin-layout">
    <!-- 左侧菜单栏 -->
    <div class="sidebar">
      <div class="logo">
        <h1>奶茶商城后台</h1>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        background-color="#001529"
        text-color="rgba(255, 255, 255, 0.65)"
        active-text-color="#fff"
        router
      >
        <!-- 管理员菜单 -->
        <template v-if="userRole === 'admin'">
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataBoard /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          
          <el-sub-menu index="system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统配置管理</span>
            </template>
            <el-menu-item index="/admin/system/config">系统配置</el-menu-item>
            <el-menu-item index="/admin/system/logs">系统日志</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="merchant">
            <template #title>
              <el-icon><Shop /></el-icon>
              <span>商家管理</span>
            </template>
            <el-menu-item index="/admin/merchant/list">商家列表</el-menu-item>
            <el-menu-item index="/admin/merchant/audit">商家审核</el-menu-item>
            <el-menu-item index="/admin/merchant/rating">商家评分</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="user">
            <template #title>
              <el-icon><User /></el-icon>
              <span>消费者管理</span>
            </template>
            <el-menu-item index="/admin/user/list">用户列表</el-menu-item>
            <el-menu-item index="/admin/user/feedback">用户反馈</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="data">
            <template #title>
              <el-icon><TrendCharts /></el-icon>
              <span>数据统计</span>
            </template>
            <el-menu-item index="/admin/data/overview">数据概览</el-menu-item>
            <el-menu-item index="/admin/data/reports">统计报表</el-menu-item>
          </el-sub-menu>
        </template>
        
        <!-- 商家菜单 -->
        <template v-else-if="userRole === 'merchant'">
          <el-menu-item index="/merchant/dashboard">
            <el-icon><DataBoard /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          
          <el-sub-menu index="shop">
            <template #title>
              <el-icon><Shop /></el-icon>
              <span>店铺信息管理</span>
            </template>
            <el-menu-item index="/merchant/shop/info">店铺信息</el-menu-item>
            <el-menu-item index="/merchant/shop/rating">评分评价</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="product">
            <template #title>
              <el-icon><Goods /></el-icon>
              <span>商品管理</span>
            </template>
            <el-menu-item index="/merchant/product/list">商品列表</el-menu-item>
            <el-menu-item index="/merchant/product/add">添加商品</el-menu-item>
            <el-menu-item index="/merchant/product/category">商品分类</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="order">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>订单管理</span>
            </template>
            <el-menu-item index="/merchant/order/list">订单列表</el-menu-item>
            <el-menu-item index="/merchant/order/process">订单处理</el-menu-item>
            <el-menu-item index="/merchant/order/shipping">发货跟踪</el-menu-item>
            <el-menu-item index="/merchant/order/refund">退款处理</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="marketing">
            <template #title>
              <el-icon><Promotion /></el-icon>
              <span>营销活动管理</span>
            </template>
            <el-menu-item index="/merchant/marketing/coupon">优惠券管理</el-menu-item>
            <el-menu-item index="/merchant/marketing/promotion">促销活动</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="finance">
            <template #title>
              <el-icon><Money /></el-icon>
              <span>财务管理</span>
            </template>
            <el-menu-item index="/merchant/finance/income">收入统计</el-menu-item>
            <el-menu-item index="/merchant/finance/withdraw">提现管理</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="analytics">
            <template #title>
              <el-icon><TrendCharts /></el-icon>
              <span>经营数据</span>
            </template>
            <el-menu-item index="/merchant/analytics/sales">销售统计</el-menu-item>
            <el-menu-item index="/merchant/analytics/customer">客户分析</el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </div>
    
    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 顶部导航栏 -->
      <div class="header">
        <div class="header-left">
          <el-button 
            type="text" 
            @click="toggleCollapse"
            :icon="isCollapse ? 'Expand' : 'Fold'"
          >
            {{ isCollapse ? '展开' : '收起' }}
          </el-button>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-icon><User /></el-icon>
              {{ userName }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      
      <!-- 页面内容 -->
      <div class="content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const activeMenu = computed(() => router.currentRoute.value.path)

const userRole = computed(() => userStore.role)
const userName = computed(() => userStore.name)

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const logout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #666;
  
  .el-icon {
    margin: 0 4px;
  }
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}
</style>
