<template>
  <div class="home-container">
    <!-- 搜索栏 -->
    <section class="search-section">
      <div class="container">
        <div class="search-bar">
          <el-input
            v-model="searchQuery"
            placeholder="搜索商品名称、商家名称或关键词"
            size="large"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        </div>
      </div>
    </section>

    <!-- 分类导航 -->
    <section class="category-nav-section">
      <div class="container">
        <div class="category-nav">
          <div class="nav-tabs">
            <div 
              v-for="category in categoryTabs" 
              :key="category.id"
              class="nav-tab"
              :class="{ active: activeCategory === category.id }"
              @click="switchCategory(category.id)"
            >
              <el-icon><component :is="category.icon" /></el-icon>
              <span>{{ category.name }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 限时促销活动轮播图 -->
    <section class="hero-section">
      <div class="container">
        <h2 class="section-title">限时促销活动</h2>
        <el-carousel height="300px" indicator-position="outside" arrow="hover">
          <el-carousel-item v-for="(banner, index) in promotionBanners" :key="index">
            <div 
              class="banner-item" 
              :style="{ backgroundImage: `url(${getBannerImageUrl(banner.image)})` }" 
              @click="goToActivity(banner)"
            >
              <div class="banner-content">
                <div class="banner-badge">{{ banner.type }}</div>
                <h3>{{ banner.title }}</h3>
                <p>{{ banner.description }}</p>
                <div class="banner-time">
                  <el-icon><Clock /></el-icon>
                  <span>活动截止：{{ banner.endTime }}</span>
                </div>
                <el-button type="primary" size="large" @click.stop="goToActivity(banner)">
                  立即参与
                </el-button>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </section>

    <!-- 附近热门商家 -->
    <section class="nearby-merchants-section">
      <div class="container">
        <div class="section-header">
          <h2>附近热门商家</h2>
          <p>按距离排序，快速找到您身边的优质商家</p>
        </div>
        
        <el-row :gutter="20">
          <el-col :span="8" v-for="merchant in nearbyMerchants" :key="merchant.id">
            <el-card class="merchant-card" shadow="hover" @click="goToMerchant(merchant)">
              <div class="merchant-header">
                <div class="merchant-info">
                  <h3>{{ merchant.name }}</h3>
                  <div class="merchant-rating">
                    <el-rate v-model="merchant.rating" disabled show-score />
                    <span class="rating-text">{{ merchant.rating }}分</span>
                  </div>
                </div>
                <div class="merchant-distance">{{ merchant.distance }}km</div>
              </div>
              <div class="merchant-details">
                <div class="detail-item">
                  <el-icon><Clock /></el-icon>
                  <span>配送时间：{{ merchant.deliveryTime }}分钟</span>
                </div>
                <div class="detail-item">
                  <el-icon><Money /></el-icon>
                  <span>起送价：¥{{ merchant.minOrder }}</span>
                </div>
                <div class="detail-item">
                  <el-icon><Van /></el-icon>
                  <span>配送费：¥{{ merchant.deliveryFee }}</span>
                </div>
              </div>
              <div class="merchant-tags">
                <el-tag v-for="tag in merchant.tags" :key="tag" size="small" type="info">
                  {{ tag }}
                </el-tag>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </section>

    <!-- 为你推荐商品 -->
    <section class="recommended-section">
      <div class="container">
        <div class="section-header">
          <h2>为你推荐</h2>
          <p>根据您的历史消费偏好和浏览记录推荐</p>
        </div>
        
        <el-row :gutter="20">
          <el-col :span="6" v-for="product in recommendedProducts" :key="product.id">
            <el-card class="product-card" shadow="hover" @click="goToProductDetail(product)">
              <div class="product-image">
                <img 
                  :src="getProductImageUrl(product.image)" 
                  :alt="product.name"
                  @error="handleProductImageError"
                  loading="lazy"
                />
                <div class="product-badge" v-if="product.isHot">热销</div>
                <div class="product-badge new" v-if="product.isNew">新品</div>
                <!-- 促销标签 -->
                <div class="product-badge promotion" v-if="productPromotions[product.id]">
                  {{ getPromotionBadgeText(productPromotions[product.id]) }}
                </div>
              </div>
              <div class="product-info">
                <h3>{{ product.name }}</h3>
                <p class="product-desc">{{ product.description }}</p>
                <div class="product-price">
                  <span class="current-price">¥{{ getProductDisplayPrice(product).toFixed(2) }}</span>
                  <span class="original-price" v-if="getProductOriginalPrice(product) > getProductDisplayPrice(product)">
                    ¥{{ getProductOriginalPrice(product).toFixed(2) }}
                  </span>
                </div>
                <div class="product-actions">
                  <el-button type="primary" @click.stop="addToCart(product)">
                    加入购物车
                  </el-button>
                  <el-button @click.stop="toggleFavorite(product)">
                    <el-icon>
                      <component :is="product.isFavorite ? 'StarFilled' : 'Star'" />
                    </el-icon>
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </section>

    <!-- 新品上市 -->
    <section class="new-products-section">
      <div class="container">
        <div class="section-header">
          <h2>新品上市</h2>
          <p>最新推出的产品，标注"新品"标签</p>
        </div>
        
        <el-row :gutter="20">
          <el-col :span="6" v-for="product in newProducts" :key="product.id">
            <el-card class="product-card" shadow="hover" @click="goToProductDetail(product)">
              <div class="product-image">
                <img 
                  :src="getProductImageUrl(product.image)" 
                  :alt="product.name"
                  @error="handleProductImageError"
                  loading="lazy"
                />
                <div class="product-badge new">新品</div>
                <!-- 促销标签 -->
                <div class="product-badge promotion" v-if="productPromotions[product.id]">
                  {{ getPromotionBadgeText(productPromotions[product.id]) }}
                </div>
              </div>
              <div class="product-info">
                <h3>{{ product.name }}</h3>
                <p class="product-desc">{{ product.description }}</p>
                <div class="product-price">
                  <span class="current-price">¥{{ getProductDisplayPrice(product).toFixed(2) }}</span>
                  <span class="original-price" v-if="getProductOriginalPrice(product) > getProductDisplayPrice(product)">
                    ¥{{ getProductOriginalPrice(product).toFixed(2) }}
                  </span>
                </div>
                <div class="product-actions">
                  <el-button type="primary" @click.stop="addToCart(product)">
                    加入购物车
                  </el-button>
                  <el-button @click.stop="toggleFavorite(product)">
                    <el-icon>
                      <component :is="product.isFavorite ? 'StarFilled' : 'Star'" />
                    </el-icon>
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </section>

    <!-- 分类导航 -->
    <section class="category-section">
      <div class="container">
        <div class="section-header">
          <h2>商品分类</h2>
          <p>多种口味，满足不同需求</p>
        </div>
        
        <el-row :gutter="20">
          <el-col :span="6" v-for="category in categories" :key="category.id">
            <div class="category-item" @click="goToCategory(category)">
              <div class="category-icon">
                <el-icon size="40">
                  <component :is="category.icon" />
                </el-icon>
              </div>
              <h3>{{ category.name }}</h3>
              <p>{{ category.description }}</p>
            </div>
          </el-col>
        </el-row>
      </div>
    </section>

    <!-- 最新活动 -->
    <section class="activity-section">
      <div class="container">
        <div class="section-header">
          <h2>最新活动</h2>
          <p>精彩活动，不容错过</p>
        </div>
        
        <el-row :gutter="20">
          <el-col :span="8" v-for="activity in activities" :key="activity.id">
            <el-card class="activity-card" shadow="hover">
              <div class="activity-image">
                <img 
                  :src="getActivityImageUrl(activity.image)" 
                  :alt="activity.title"
                  @error="handleActivityImageError"
                  loading="lazy"
                />
                <div class="activity-badge">{{ activity.type }}</div>
              </div>
              <div class="activity-content">
                <h3>{{ activity.title }}</h3>
                <p>{{ activity.description }}</p>
                <div class="activity-time">
                  <el-icon><Clock /></el-icon>
                  <span>{{ activity.endTime }}</span>
                </div>
                <el-button type="primary" @click="goToActivity(activity)">
                  立即参与
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </section>

    <!-- 功能导航 -->
    <section class="features-section">
      <div class="container">
        <div class="section-header">
          <h2>全部功能</h2>
          <p>浏览所有可用功能，快速访问</p>
        </div>
        
        <el-row :gutter="20">
          <el-col 
            :span="6" 
            :xs="12" 
            :sm="8" 
            :md="6" 
            v-for="feature in features" 
            :key="feature.id"
          >
            <el-card 
              class="feature-card" 
              shadow="hover" 
              @click="goToFeature(feature)"
            >
              <div class="feature-icon">
                <el-icon :size="40">
                  <component :is="feature.icon" />
                </el-icon>
              </div>
              <h3>{{ feature.name }}</h3>
              <p>{{ feature.description }}</p>
              <el-tag 
                v-if="feature.requireAuth" 
                size="small" 
                type="warning"
                class="auth-tag"
              >
                需登录
              </el-tag>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { getImagesByRange } from '@/utils/imageLoader'
import { getImageUrl, handleImageError } from '@/utils/imageUtils'
import { 
  Coffee, 
  IceCream, 
  Apple, 
  Orange,
  Clock,
  Search,
  Star,
  StarFilled,
  Money,
  Van,
  ShoppingCart,
  List,
  Document,
  Location,
  Service,
  User,
  Message,
  QuestionFilled,
  Promotion,
  Ticket,
  Box
} from '@element-plus/icons-vue'
// 导入 API
import { getProductList, getRecommendedProducts, getNewProducts } from '@/api/product'
import { getCategoryTree } from '@/api/category'
import { getNearbyMerchants } from '@/api/merchant'
import { getActivePromotions, getPromotionPage, checkUserParticipatedPromotion } from '@/api/promotion'
import { calculatePromotionPrice } from '@/utils/priceCalculator'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

// 搜索相关
const searchQuery = ref('')
const activeCategory = ref(1)

// 加载状态
const loading = ref({
  categories: false,
  promotions: false,
  merchants: false,
  recommended: false,
  newProducts: false,
  activities: false
})

// 分类导航（从 API 获取）
const categoryTabs = ref([])
const categories = ref([])

// 促销活动轮播图
const bannerImages = getImagesByRange(0, 3)
const promotionBanners = ref([])

// 附近热门商家
const nearbyMerchants = ref([])

// 推荐商品
const recommendedProducts = ref([])

// 新品上市
const newProducts = ref([])

// 最新活动（使用促销活动数据）
const activities = ref([])

// 商品促销活动映射（商品ID -> 促销活动）
const productPromotions = ref({})
// 促销活动用户参与状态映射（促销活动ID -> 是否已参与）
const promotionParticipatedStatus = ref({})

// 功能列表
const features = ref([
  {
    id: 1,
    name: '商品浏览',
    description: '浏览所有商品',
    path: '/products',
    icon: 'Box',
    requireAuth: false
  },
  {
    id: 2,
    name: '商品详情',
    description: '查看商品详细信息',
    path: '/products',
    icon: 'Document',
    requireAuth: false,
    note: '需要商品ID'
  },
  {
    id: 3,
    name: '购物车',
    description: '管理购物车商品',
    path: '/cart',
    icon: 'ShoppingCart',
    requireAuth: true
  },
  {
    id: 4,
    name: '我的订单',
    description: '查看所有订单',
    path: '/orders',
    icon: 'List',
    requireAuth: true
  },
  {
    id: 5,
    name: '订单详情',
    description: '查看订单详细信息',
    path: '/orders',
    icon: 'Document',
    requireAuth: true,
    note: '需要订单ID'
  },
  {
    id: 6,
    name: '订单跟踪',
    description: '跟踪订单配送状态',
    path: '/orders',
    icon: 'Location',
    requireAuth: true,
    note: '需要订单ID'
  },
  {
    id: 7,
    name: '订单确认',
    description: '确认订单并支付',
    path: '/cart',
    icon: 'Ticket',
    requireAuth: true,
    note: '从购物车进入'
  },
  {
    id: 8,
    name: '申请售后',
    description: '申请退款或售后服务',
    path: '/after-sale',
    icon: 'Service',
    requireAuth: true
  },
  {
    id: 9,
    name: '收货地址',
    description: '管理收货地址',
    path: '/address',
    icon: 'Location',
    requireAuth: true
  },
  {
    id: 10,
    name: '营销活动',
    description: '查看最新促销活动',
    path: '/activities',
    icon: 'Promotion',
    requireAuth: false
  },
  {
    id: 11,
    name: '我的消息',
    description: '查看系统消息和通知',
    path: '/messages',
    icon: 'Message',
    requireAuth: true
  },
  {
    id: 12,
    name: '帮助中心',
    description: '常见问题和使用帮助',
    path: '/help',
    icon: 'QuestionFilled',
    requireAuth: false
  },
  {
    id: 13,
    name: '个人中心',
    description: '个人信息和设置',
    path: '/profile',
    icon: 'User',
    requireAuth: true
  }
])

// 数据转换：将分类转换为分类标签格式
const convertCategoriesToTabs = (categories) => {
  const iconMap = {
    '经典奶茶': 'Coffee',
    '果茶': 'Apple',
    '奶盖茶': 'Coffee',
    '冰沙': 'IceCream',
    '热饮': 'Coffee',
    '冷饮': 'IceCream',
    '咖啡': 'Coffee'
  }
  
  // 获取一级分类，最多7个
  const topCategories = categories
    .filter(cat => !cat.parentId || cat.level === 1)
    .slice(0, 7)
  
  return topCategories.map((cat, index) => ({
    id: cat.id,
    name: cat.name,
    icon: iconMap[cat.name] || 'Coffee'
  }))
}

// 数据转换：将促销活动转换为轮播图格式
const convertPromotionsToBanners = (promotions) => {
  const typeMap = {
    'discount': '优惠',
    'full_reduce': '满减',
    'buy_one_get_one': '买一送一',
    'second_half_price': '第二件半价'
  }
  
  return promotions.map((promo, index) => ({
    id: promo.id,
    title: promo.name,
    description: promo.description || '',
    type: typeMap[promo.type] || '活动',
    image: bannerImages[index % bannerImages.length] || '/banner1.jpg',
    endTime: promo.endTime ? new Date(promo.endTime).toLocaleDateString() : '长期有效',
    promotion: promo
  }))
}

// 数据转换：将商品转换为前端格式
const convertProducts = (products) => {
  const productImages = getImagesByRange(3, products.length)
  
  return products.map((product, index) => {
    // 获取第一张图片
    const image = product.images && product.images.length > 0 
      ? product.images[0] 
      : productImages[index % productImages.length]
    
    return {
      id: product.id,
      name: product.name,
      description: product.description || '',
      price: product.price ? parseFloat(product.price) : 0,
      originalPrice: product.originalPrice ? parseFloat(product.originalPrice) : null,
      image: image,
      isHot: product.sales > 100, // 销量大于100视为热销
      isNew: false, // 新品由单独接口获取
      isFavorite: false,
      merchantId: product.merchantId,
      categoryId: product.categoryId
    }
  })
}

// 数据转换：将促销活动转换为活动卡片格式
const convertPromotionsToActivities = (promotions) => {
  const typeMap = {
    'discount': '折扣',
    'full_reduce': '满减',
    'buy_one_get_one': '买一送一',
    'second_half_price': '第二件半价'
  }
  
  const activityImages = getImagesByRange(11, promotions.length)
  
  return promotions.map((promo, index) => ({
    id: promo.id,
    title: promo.name,
    description: promo.description || '',
    type: typeMap[promo.type] || '活动',
    image: activityImages[index % activityImages.length] || '/activity1.jpg',
    endTime: promo.endTime ? new Date(promo.endTime).toLocaleDateString() : '长期有效'
  }))
}

// 加载分类数据
const loadCategories = async () => {
  try {
    loading.value.categories = true
    const categoryList = await getCategoryTree()
    
    if (categoryList && categoryList.length > 0) {
      // 过滤启用状态的分类
      const enabledCategories = categoryList.filter(cat => cat.status === 1)
      categories.value = enabledCategories
      categoryTabs.value = convertCategoriesToTabs(enabledCategories)
      
      if (categoryTabs.value.length > 0) {
        activeCategory.value = categoryTabs.value[0].id
      }
    }
  } catch (error) {
    console.error('加载分类失败:', error)
    // 使用默认分类
    categoryTabs.value = [
  { id: 1, name: '经典奶茶', icon: 'Coffee' },
  { id: 2, name: '果茶', icon: 'Apple' },
  { id: 3, name: '奶盖茶', icon: 'Coffee' },
      { id: 4, name: '冰沙', icon: 'IceCream' }
    ]
  } finally {
    loading.value.categories = false
  }
}

// 加载促销活动
const loadPromotions = async () => {
  try {
    loading.value.promotions = true
    const promotions = await getActivePromotions(3)
    
    if (promotions && promotions.length > 0) {
      promotionBanners.value = convertPromotionsToBanners(promotions)
      activities.value = convertPromotionsToActivities(promotions)
    } else {
      // 使用默认数据
      promotionBanners.value = [
  {
    id: 1,
    title: '夏日限定新品',
    description: '杨枝甘露、柠檬蜂蜜茶等新品上市',
    type: '新品',
    image: bannerImages[0] || '/banner1.jpg',
    endTime: '2024-12-31'
        }
      ]
    }
  } catch (error) {
    console.error('加载促销活动失败:', error)
  } finally {
    loading.value.promotions = false
  }
}

// 加载附近商家
const loadMerchants = async () => {
  try {
    loading.value.merchants = true
    const merchants = await getNearbyMerchants(3)
    
    if (merchants && merchants.length > 0) {
      nearbyMerchants.value = merchants.map(merchant => ({
        ...merchant,
        name: merchant.shopName || merchant.name || '未知商家',
        rating: merchant.rating ? parseFloat(merchant.rating) : 4.5
      }))
    } else {
      // 使用默认数据
      nearbyMerchants.value = []
    }
  } catch (error) {
    console.error('加载商家失败:', error)
  } finally {
    loading.value.merchants = false
  }
}

// 获取商品显示价格（含促销）
const getProductDisplayPrice = (product) => {
  const basePrice = product.price || 0
  const promotion = productPromotions.value[product.id]
  
  // 只有在用户参与了促销活动时才应用价格折扣
  if (promotion && promotionParticipatedStatus.value[promotion.id]) {
    const promotionPrice = calculatePromotionPrice(basePrice, promotion, 1)
    return promotionPrice.price
  }
  
  return basePrice
}

// 获取商品原价（用于显示）
const getProductOriginalPrice = (product) => {
  const promotion = productPromotions.value[product.id]
  
  // 如果有促销活动，显示促销前的价格
  if (promotion) {
    return product.price || 0
  }
  
  // 否则显示商品的原价
  return product.originalPrice || product.price || 0
}

// 获取促销活动标签文本
const getPromotionBadgeText = (promotion) => {
  if (!promotion) return ''
  
  const typeMap = {
    'discount': `${promotion.discountRate || ''}折`,
    'second_half_price': '第二件半价',
    'buy_one_get_one': '买一送一',
    'full_reduce': '满减'
  }
  
  return typeMap[promotion.type] || '促销'
}

// 加载商品促销活动
const loadProductPromotions = async (productList) => {
  try {
    // 获取所有商户ID
    const merchantIds = [...new Set(productList.map(p => p.merchantId).filter(id => id))]
    
    // 为每个商户加载促销活动
    for (const merchantId of merchantIds) {
      try {
        const response = await getPromotionPage({
          pageNum: 1,
          pageSize: 50,
          status: 'active',
          merchantId: merchantId
        })
        
        const promotions = response?.records || response?.list || []
        
        // 为每个商品匹配促销活动
        productList.forEach(product => {
          if (product.merchantId === merchantId) {
            const activePromotion = promotions.find(p => {
              if (p.productIds && Array.isArray(p.productIds) && p.productIds.includes(product.id)) {
                // 检查活动类型是否影响价格
                return ['discount', 'second_half_price', 'buy_one_get_one'].includes(p.type)
              }
              return false
            })
            
            if (activePromotion) {
              productPromotions.value[product.id] = activePromotion
              
              // 检查用户是否参与了该促销活动（仅当用户已登录时）
              if (userStore.isLoggedIn && activePromotion.id) {
                checkPromotionParticipatedStatus(activePromotion.id)
              }
            }
          }
        })
      } catch (error) {
        console.error(`加载商户 ${merchantId} 的促销活动失败:`, error)
        // 继续处理其他商户
      }
    }
  } catch (error) {
    console.error('加载商品促销活动失败:', error)
    // 不显示错误，静默失败
  }
}

// 检查用户是否参与了促销活动
const checkPromotionParticipatedStatus = async (promotionId) => {
  // 如果已经检查过，直接返回
  if (promotionParticipatedStatus.value[promotionId] !== undefined) {
    return
  }
  
  try {
    const hasParticipated = await checkUserParticipatedPromotion(promotionId)
    promotionParticipatedStatus.value[promotionId] = hasParticipated
  } catch (error) {
    console.warn(`检查促销活动 ${promotionId} 参与状态失败:`, error)
    // 检查失败时，默认设为未参与
    promotionParticipatedStatus.value[promotionId] = false
  }
}

// 加载推荐商品
const loadRecommendedProducts = async () => {
  try {
    loading.value.recommended = true
    const products = await getRecommendedProducts(4)
    
    if (products && products.length > 0) {
      const convertedProducts = convertProducts(products)
      recommendedProducts.value = convertedProducts
      // 加载商品促销活动
      await loadProductPromotions(convertedProducts)
    } else {
      recommendedProducts.value = []
    }
  } catch (error) {
    console.error('加载推荐商品失败:', error)
  } finally {
    loading.value.recommended = false
  }
}

// 加载新品
const loadNewProducts = async () => {
  try {
    loading.value.newProducts = true
    const products = await getNewProducts(4)
    
    if (products && products.length > 0) {
      const convertedProducts = convertProducts(products).map(product => ({
        ...product,
        isNew: true
      }))
      newProducts.value = convertedProducts
      // 加载商品促销活动
      await loadProductPromotions(convertedProducts)
    } else {
      newProducts.value = []
    }
  } catch (error) {
    console.error('加载新品失败:', error)
  } finally {
    loading.value.newProducts = false
  }
}

// 初始化数据
onMounted(async () => {
  // 并行加载所有数据
  await Promise.all([
    loadCategories(),
    loadPromotions(),
    loadMerchants(),
    loadRecommendedProducts(),
    loadNewProducts()
  ])
})

// 搜索功能
const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({
      path: '/products',
      query: { search: searchQuery.value.trim() }
    })
  } else {
    ElMessage.warning('请输入搜索关键词')
  }
}

// 分类切换
const switchCategory = (categoryId) => {
  activeCategory.value = categoryId
  const category = categoryTabs.value.find(cat => cat.id === categoryId)
  if (category) {
    router.push({
      path: '/products',
      query: { category: category.name }
    })
  }
}

// 跳转到商家页面
const goToMerchant = (merchant) => {
  router.push({
    path: '/products',
    query: { merchant: merchant.id }
  })
}

const goToProducts = () => {
  router.push('/products')
}

const goToProductDetail = (product) => {
  router.push(`/products/${product.id}`)
}

const goToCategory = (category) => {
  router.push({
    path: '/products',
    query: { category: category.name }
  })
}

const goToActivity = (activity) => {
  router.push('/activities')
}

const addToCart = (product) => {
  cartStore.addToCart(product)
  ElMessage.success('已添加到购物车')
}

// 收藏功能
const toggleFavorite = (product) => {
  product.isFavorite = !product.isFavorite
  ElMessage.success(product.isFavorite ? '已添加到收藏' : '已取消收藏')
}

// 跳转到功能页面
const goToFeature = (feature) => {
  if (feature.note) {
    // 如果有备注说明，先跳转到相关列表页
    ElMessage.info(`${feature.note}，请从${feature.description}进入`)
    router.push(feature.path)
  } else {
    router.push(feature.path)
  }
}

// 图片处理函数
const getBannerImageUrl = (imageUrl) => {
  if (!imageUrl) return ''
  // 如果是完整的 URL，直接返回
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl
  }
  // 如果是 data URI，直接返回
  if (imageUrl.startsWith('data:')) {
    return imageUrl
  }
  // 否则使用 getImageUrl 处理
  return getImageUrl(imageUrl) || imageUrl
}

const getProductImageUrl = (imageUrl) => {
  if (!imageUrl) return ''
  // 如果是完整的 URL，直接返回
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl
  }
  // 如果是 data URI，直接返回
  if (imageUrl.startsWith('data:')) {
    return imageUrl
  }
  // 否则使用 getImageUrl 处理
  return getImageUrl(imageUrl) || imageUrl
}

const getActivityImageUrl = (imageUrl) => {
  if (!imageUrl) return ''
  // 如果是完整的 URL，直接返回
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl
  }
  // 如果是 data URI，直接返回
  if (imageUrl.startsWith('data:')) {
    return imageUrl
  }
  // 否则使用 getImageUrl 处理
  return getImageUrl(imageUrl) || imageUrl
}

// 图片加载错误处理
const handleProductImageError = (event) => {
  handleImageError(event)
}

const handleActivityImageError = (event) => {
  handleImageError(event)
}
</script>

<style scoped lang="css">
.home-container {
  min-height: calc(100vh - 120px);
}

/* 搜索栏样式 */
.search-section {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-secondary) 100%);
  padding: var(--spacing-xl) 0;
  margin-bottom: var(--spacing-lg);
  box-shadow: var(--shadow-lg);
}

.search-bar {
  max-width: 600px;
  margin: 0 auto;
}

.search-bar :deep(.el-input__wrapper) {
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
}

/* 分类导航样式 */
.category-nav-section {
  background: white;
  padding: var(--spacing-lg) 0;
  margin-bottom: var(--spacing-lg);
  box-shadow: var(--shadow-card);
}

.nav-tabs {
  display: flex;
  gap: 20px;
  overflow-x: auto;
  padding: 10px 0;
}

.nav-tab {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-base);
  min-width: 100px;
  white-space: nowrap;
}

.nav-tab:hover {
  background: var(--color-primary-lighter);
  transform: translateY(-2px);
}

.nav-tab.active {
  background: var(--color-primary);
  color: white;
  box-shadow: var(--shadow-md);
}

.nav-tab .el-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.nav-tab span {
  font-size: 14px;
  font-weight: 500;
}

/* 轮播图样式 */
.hero-section {
  margin-bottom: 40px;
}

.section-title {
  font-size: var(--font-size-2xl);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-lg);
  text-align: center;
  font-weight: var(--font-weight-bold);
}

.banner-item {
  height: 300px;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  cursor: pointer;
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.banner-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(230, 57, 70, 0.7) 0%, rgba(42, 157, 143, 0.7) 100%);
  transition: opacity var(--transition-base);
}

.banner-item:hover::before {
  opacity: 0.8;
}

.banner-content {
  text-align: center;
  color: white;
  z-index: 1;
  position: relative;
}

.banner-badge {
  position: absolute;
  top: 20px;
  left: 20px;
  background: var(--color-primary);
  color: white;
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  box-shadow: var(--shadow-md);
  z-index: 2;
}

.banner-content h3 {
  font-size: 28px;
  margin-bottom: 12px;
}

.banner-content p {
  font-size: 16px;
  margin-bottom: 16px;
}

.banner-time {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 20px;
  font-size: 14px;
  opacity: 0.9;
}

/* 商家卡片样式 */
.nearby-merchants-section,
.recommended-section,
.new-products-section {
  padding: 40px 0;
}

.nearby-merchants-section {
  background: var(--color-bg-white);
}

.merchant-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s;
}

.merchant-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-elevated);
}

.merchant-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.merchant-info h3 {
  margin-bottom: var(--spacing-sm);
  color: var(--color-text-primary);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
}

.merchant-rating {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.rating-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.merchant-distance {
  background: var(--color-primary);
  color: white;
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-lg);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  box-shadow: var(--shadow-sm);
}

.merchant-details {
  margin-bottom: 16px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.merchant-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.section-header {
  text-align: center;
  margin-bottom: var(--spacing-2xl);
}

.section-header p {
  color: var(--color-text-secondary);
  font-size: var(--font-size-base);
}

.section-header h2 {
  font-size: var(--font-size-3xl);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-sm);
  font-weight: var(--font-weight-bold);
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-secondary) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.section-header p {
  color: #666;
  font-size: 16px;
}

.featured-section,
.category-section,
.activity-section,
.features-section {
  padding: 60px 0;
}

.category-section {
  background: #f8f9fa;
}

.product-card {
  cursor: pointer;
  transition: transform 0.3s;
  margin-bottom: 20px;
}

.product-card:hover {
  transform: translateY(-5px);
}

.product-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: var(--color-primary);
  color: white;
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  box-shadow: var(--shadow-sm);
}

.product-info {
  padding: var(--spacing-md);
}

.product-info h3 {
  margin-bottom: var(--spacing-sm);
  color: var(--color-text-primary);
  font-weight: var(--font-weight-semibold);
}

.product-desc {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  margin-bottom: var(--spacing-sm);
  line-height: var(--line-height-normal);
}

.product-price {
  margin-bottom: var(--spacing-sm);
}

.current-price {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
  margin-left: 8px;
}

.product-badge.new {
  background: var(--color-secondary);
}

.product-badge.promotion {
  background: #ff6b6b;
  top: 10px;
  left: 10px;
  right: auto;
}

.product-actions {
  display: flex;
  gap: 8px;
}

.product-actions .el-button {
  flex: 1;
}

.product-actions .el-button:last-child {
  flex: 0 0 auto;
  width: 40px;
  padding: 0;
}

.category-item {
  text-align: center;
  padding: var(--spacing-xl) var(--spacing-lg);
  background: white;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-base);
  box-shadow: var(--shadow-card);
}

.category-item:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-elevated);
}

.category-icon {
  color: var(--color-primary);
  margin-bottom: var(--spacing-md);
  transition: transform var(--transition-fast);
}

.category-item:hover .category-icon {
  transform: scale(1.1);
}

.category-item h3 {
  margin-bottom: var(--spacing-sm);
  color: var(--color-text-primary);
  font-weight: var(--font-weight-semibold);
}

.category-item p {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.activity-card {
  margin-bottom: 20px;
}

.activity-image {
  position: relative;
  height: 150px;
  overflow: hidden;
}

.activity-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.activity-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background: var(--color-primary);
  color: white;
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  box-shadow: var(--shadow-sm);
  z-index: 2;
}

.activity-content {
  padding: var(--spacing-md);
}

.activity-content h3 {
  margin-bottom: var(--spacing-sm);
  color: var(--color-text-primary);
  font-weight: var(--font-weight-semibold);
}

.activity-content p {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  margin-bottom: var(--spacing-sm);
  line-height: var(--line-height-normal);
}

.activity-time {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  color: var(--color-text-tertiary);
  font-size: var(--font-size-xs);
  margin-bottom: var(--spacing-md);
}

/* 功能导航样式 */
.features-section {
  background: var(--color-bg-white);
  padding: var(--spacing-2xl) 0;
}

.feature-card {
  text-align: center;
  cursor: pointer;
  transition: all var(--transition-base);
  margin-bottom: var(--spacing-lg);
  padding: var(--spacing-lg);
  height: 100%;
  border-radius: var(--radius-lg);
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-elevated);
}

.feature-icon {
  color: var(--color-primary);
  margin-bottom: var(--spacing-md);
  transition: transform var(--transition-fast);
}

.feature-card:hover .feature-icon {
  transform: scale(1.1);
  color: var(--color-primary-dark);
}

.feature-card h3 {
  margin-bottom: var(--spacing-sm);
  color: var(--color-text-primary);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
}

.feature-card p {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  margin-bottom: var(--spacing-sm);
  min-height: 40px;
  line-height: var(--line-height-normal);
}

.auth-tag {
  margin-top: 8px;
}

/* ========== 响应式适配 ========== */
@media (max-width: 768px) {
  .search-section {
    padding: var(--spacing-lg) 0;
  }
  
  .search-bar {
    max-width: 100%;
  }
  
  .nav-tabs {
    gap: var(--spacing-sm);
    padding: var(--spacing-sm) 0;
  }
  
  .nav-tab {
    padding: var(--spacing-sm) var(--spacing-md);
    min-width: 80px;
  }
  
  .banner-item {
    height: 200px;
  }
  
  .banner-content h3 {
    font-size: var(--font-size-xl);
  }
  
  .banner-content p {
    font-size: var(--font-size-sm);
  }
  
  .section-header h2 {
    font-size: var(--font-size-2xl);
  }
  
  .nearby-merchants-section,
  .recommended-section,
  .new-products-section {
    padding: var(--spacing-lg) 0;
  }
  
  .merchant-card {
    margin-bottom: var(--spacing-md);
  }
  
  .feature-card {
    padding: var(--spacing-md);
  }
}

@media (max-width: 480px) {
  .banner-item {
    height: 180px;
  }
  
  .banner-content h3 {
    font-size: var(--font-size-lg);
  }
  
  .nav-tab span {
    font-size: var(--font-size-xs);
  }
  
  .category-item,
  .feature-card {
    padding: var(--spacing-md);
  }

  .featured-section .el-col,
  .category-section .el-col,
  .activity-section .el-col,
  .features-section .el-col {
    margin-bottom: 20px;
  }

  .feature-card {
    padding: 16px;
  }

  .feature-card h3 {
    font-size: 16px;
  }

  .feature-card p {
    font-size: 12px;
    min-height: 32px;
  }

  .feature-icon .el-icon {
    font-size: 32px !important;
  }
}
</style>
