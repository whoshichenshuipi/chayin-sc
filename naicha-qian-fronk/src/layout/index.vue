<template>
  <div class="layout">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="container">
        <div class="header-content">
          <!-- Logo -->
          <div class="logo" @click="$router.push('/home')">
            <el-icon size="28" :style="{ color: 'var(--color-primary)' }">
              <Coffee />
            </el-icon>
            <span class="logo-text">奶茶商城</span>
          </div>

          <!-- 主导航 -->
          <nav class="main-nav">
            <el-menu
              :default-active="activeMenuIndex"
              mode="horizontal"
              router
              class="nav-menu"
            >
              <el-menu-item index="/home">
                <el-icon><House /></el-icon>
                <span>首页</span>
              </el-menu-item>
              
              <!-- 商品菜单 -->
              <el-sub-menu index="products-menu">
                <template #title>
                  <el-icon><Shop /></el-icon>
                  <span>商品</span>
                </template>
                <el-menu-item index="/products">
                  <el-icon><Box /></el-icon>
                  <span>商品浏览</span>
                </el-menu-item>
                <el-menu-item index="/activities">
                  <el-icon><Promotion /></el-icon>
                  <span>营销活动</span>
                </el-menu-item>
              </el-sub-menu>

              <!-- 订单菜单 -->
              <el-sub-menu index="orders-menu" v-if="userStore.isLoggedIn">
                <template #title>
                  <el-icon><List /></el-icon>
                  <span>我的订单</span>
                </template>
                <el-menu-item index="/orders">
                  <el-icon><Document /></el-icon>
                  <span>订单列表</span>
                </el-menu-item>
                <el-menu-item index="/after-sale">
                  <el-icon><Service /></el-icon>
                  <span>申请售后</span>
                </el-menu-item>
              </el-sub-menu>
              <el-menu-item index="/orders" v-else>
                <el-icon><List /></el-icon>
                <span>我的订单</span>
              </el-menu-item>

              <!-- 个人中心菜单 -->
              <el-sub-menu index="user-menu" v-if="userStore.isLoggedIn">
                <template #title>
                  <el-icon><User /></el-icon>
                  <span>个人中心</span>
                </template>
                <el-menu-item index="/address">
                  <el-icon><Location /></el-icon>
                  <span>收货地址</span>
                </el-menu-item>
                <el-menu-item index="/messages">
                  <el-icon><Message /></el-icon>
                  <span>我的消息</span>
                </el-menu-item>
              </el-sub-menu>

              <el-menu-item index="/help">
                <el-icon><QuestionFilled /></el-icon>
                <span>帮助中心</span>
              </el-menu-item>
            </el-menu>
          </nav>

          <!-- 右侧操作区 -->
          <div class="header-actions">
            <!-- 搜索框 -->
            <el-input
              v-model="searchKeyword"
              placeholder="搜索商品..."
              class="search-input"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>

            <!-- 购物车 -->
            <el-badge :value="cartStore.getTotalCount" class="cart-badge">
              <el-button
                type="primary"
                :icon="ShoppingCart"
                circle
                @click="$router.push('/cart')"
              />
            </el-badge>

            <!-- 用户菜单 -->
            <el-dropdown v-if="userStore.isLoggedIn" @command="handleUserCommand" trigger="click">
              <el-avatar :src="userStore.getUserAvatar" class="user-avatar">
                {{ userStore.getUserName.charAt(0) }}
              </el-avatar>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    <span>个人中心</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="orders">
                    <el-icon><List /></el-icon>
                    <span>我的订单</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="address">
                    <el-icon><Location /></el-icon>
                    <span>收货地址</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="messages">
                    <el-icon><Message /></el-icon>
                    <span>我的消息</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="cart" divided>
                    <el-icon><ShoppingCart /></el-icon>
                    <span>购物车</span>
                    <el-badge :value="cartStore.getTotalCount" class="cart-badge-inline" />
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <el-icon><SwitchButton /></el-icon>
                    <span>退出登录</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <!-- 登录按钮 -->
            <el-button
              v-else
              type="primary"
              @click="$router.push('/login')"
            >
              登录
            </el-button>
          </div>
        </div>
      </div>
    </header>

    <!-- 主要内容区 -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- 底部 -->
    <footer class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-section">
            <h4>关于我们</h4>
            <p>专业的奶茶销售系统</p>
          </div>
          <div class="footer-section">
            <h4>联系方式</h4>
            <p>客服热线：400-123-4567</p>
          </div>
          <div class="footer-section">
            <h4>营业时间</h4>
            <p>周一至周日 9:00-22:00</p>
          </div>
        </div>
        <div class="footer-bottom">
          <p>&copy; 2024 奶茶销售系统. All rights reserved.</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { 
  Coffee, 
  House, 
  Shop, 
  Trophy, 
  Search, 
  ShoppingCart,
  Box,
  Promotion,
  List,
  Document,
  Service,
  User,
  Location,
  Message,
  QuestionFilled,
  SwitchButton
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

const searchKeyword = ref('')

// 计算当前激活的菜单项
const activeMenuIndex = computed(() => {
  const path = route.path
  // 如果路径匹配子菜单项，返回完整路径
  if (path === '/products' || path === '/activities') {
    return path
  }
  if (path === '/orders' || path === '/after-sale') {
    return path
  }
  if (path === '/address' || path === '/messages') {
    return path
  }
  // 其他情况返回路径
  return path
})

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({
      path: '/products',
      query: { search: searchKeyword.value }
    })
  }
}

const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'orders':
      router.push('/orders')
      break
    case 'address':
      router.push('/address')
      break
    case 'messages':
      router.push('/messages')
      break
    case 'cart':
      router.push('/cart')
      break
    case 'logout':
      userStore.logout()
      router.push('/home')
      break
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  box-shadow: var(--shadow-md);
  position: sticky;
  top: 0;
  z-index: var(--z-index-sticky);
  border-bottom: 1px solid var(--color-bg-gray-light);
  backdrop-filter: blur(10px);
}

/* 确保头部没有意外的背景色块 */
.header::before,
.header::after {
  display: none;
}

.container {
  max-width: var(--container-max-width);
  margin: 0 auto;
  padding: 0 var(--container-padding);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
}

.logo {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  cursor: pointer;
  transition: transform var(--transition-fast);
}

.logo:hover {
  transform: scale(1.05);
}

.logo-text {
  color: var(--color-text-primary);
  font-weight: var(--font-weight-bold);
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-secondary) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.main-nav {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-menu {
  border-bottom: none;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-input {
  width: 200px;
}

.cart-badge {
  cursor: pointer;
}

.cart-badge-inline {
  margin-left: 8px;
}

.user-avatar {
  cursor: pointer;
  transition: transform 0.3s;
}

.user-avatar:hover {
  transform: scale(1.1);
}

.nav-menu {
  border-bottom: none;
  background: transparent !important;
}

/* Element Plus 菜单样式修复 - 移除默认的背景色块 */
.nav-menu .el-menu-item,
.nav-menu .el-sub-menu__title {
  padding: 0 20px;
  background: transparent !important;
  border-bottom: none !important;
}

.nav-menu .el-menu-item:hover,
.nav-menu .el-sub-menu__title:hover {
  background: rgba(230, 57, 70, 0.08) !important;
}

.nav-menu .el-menu-item.is-active {
  background: transparent !important;
  color: var(--color-primary) !important;
  border-bottom: 2px solid var(--color-primary) !important;
}

.nav-menu .el-sub-menu.is-active .el-sub-menu__title {
  background: transparent !important;
  color: var(--color-primary) !important;
}

/* 下拉菜单图标间距 */
.el-dropdown-menu__item .el-icon {
  margin-right: 8px;
}

.el-dropdown-menu__item {
  display: flex;
  align-items: center;
}

.main-content {
  flex: 1;
  min-height: calc(100vh - 120px);
}

.footer {
  background: var(--color-gray-dark);
  color: var(--color-text-inverse);
  padding: var(--spacing-2xl) 0 var(--spacing-lg);
  margin-top: var(--spacing-2xl);
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 30px;
  margin-bottom: 20px;
}

.footer-section h4 {
  margin-bottom: var(--spacing-sm);
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
}

.footer-section p {
  color: var(--color-text-tertiary);
  line-height: var(--line-height-normal);
}

.footer-bottom {
  text-align: center;
  padding-top: var(--spacing-lg);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  color: var(--color-text-tertiary);
}

@media (max-width: 768px) {
  .header-content {
    flex-wrap: wrap;
    height: auto;
    padding: 10px 0;
  }
  
  .logo {
    order: 1;
    flex: 0 0 auto;
  }
  
  .main-nav {
    order: 3;
    width: 100%;
    margin-top: 10px;
  }
  
  .header-actions {
    order: 2;
    flex: 0 0 auto;
    margin-left: auto;
  }
  
  .search-input {
    width: 120px;
  }

  .nav-menu {
    font-size: 14px;
  }

  .nav-menu .el-menu-item,
  .nav-menu .el-sub-menu__title {
    padding: 0 10px;
  }
}

@media (max-width: 480px) {
  .logo-text {
    display: none;
  }

  .search-input {
    width: 100px;
  }

  .nav-menu .el-menu-item span,
  .nav-menu .el-sub-menu__title span {
    display: none;
  }
}
</style>
