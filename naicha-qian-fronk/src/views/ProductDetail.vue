<template>
  <div class="product-detail-container" v-loading="loading">
    <div class="container">
      <!-- 面包屑导航 -->
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/products' }">商品列表</el-breadcrumb-item>
        <el-breadcrumb-item>{{ product.name || '商品详情' }}</el-breadcrumb-item>
      </el-breadcrumb>

      <el-row :gutter="40">
        <!-- 商品图片轮播 -->
        <el-col :span="12">
          <div class="product-images">
            <el-carousel height="400px" indicator-position="outside" arrow="hover">
              <el-carousel-item v-for="(image, index) in product.images" :key="index">
                <div class="image-item">
                  <SmartImage 
                    :src="image" 
                    :alt="product.name"
                    image-class="product-detail-image"
                    loading="lazy"
                  />
                </div>
              </el-carousel-item>
            </el-carousel>
          </div>
        </el-col>

        <!-- 商品信息 -->
        <el-col :span="12">
          <div class="product-info">
            <h1 class="product-title">{{ product.name }}</h1>
            <p class="product-desc">{{ product.description }}</p>
            
            <!-- 价格信息 -->
            <div class="price-section">
              <div class="current-price">¥{{ currentPrice.toFixed(2) }}</div>
              <div class="original-price" v-if="displayOriginalPrice > currentPrice">
                ¥{{ displayOriginalPrice.toFixed(2) }}
              </div>
              <div class="discount-badge" v-if="discountInfo">
                {{ discountInfo.discountText }} 省¥{{ discountInfo.discount.toFixed(2) }}
              </div>
            </div>

            <!-- 库存状态 -->
            <div class="stock-status">
              <el-tag v-if="stockStatus === 'sufficient'" type="success">库存充足</el-tag>
              <el-tag v-else-if="stockStatus === 'low'" type="warning">仅剩 {{ product.stock }} 杯</el-tag>
              <el-tag v-else type="danger">已售罄</el-tag>
            </div>

            <!-- 规格选择 -->
            <div class="specifications">
              <!-- 甜度选择 -->
              <div class="spec-group">
                <label>甜度：</label>
                <el-radio-group v-model="selectedSpecs.sweetness">
                  <el-radio v-for="sweet in product.sweetness" :key="sweet" :label="sweet">
                    {{ sweet }}
                  </el-radio>
                </el-radio-group>
              </div>

              <!-- 温度选择 -->
              <div class="spec-group">
                <label>温度：</label>
                <el-radio-group v-model="selectedSpecs.temperature">
                  <el-radio v-for="temp in product.temperature" :key="temp" :label="temp">
                    {{ temp }}
                  </el-radio>
                </el-radio-group>
              </div>

              <!-- 规格选择 -->
              <div class="spec-group">
                <label>规格：</label>
                <el-radio-group v-model="selectedSpecs.size">
                  <el-radio v-for="size in product.size" :key="size" :label="size">
                    {{ size }}
                  </el-radio>
                </el-radio-group>
              </div>

              <!-- 加料选择 -->
              <div class="spec-group" v-if="product.toppings && product.toppings.length > 0">
                <label>加料：</label>
                <el-checkbox-group v-model="selectedSpecs.toppings">
                  <el-checkbox v-for="topping in product.toppings" :key="topping.name" :label="topping.name">
                    {{ topping.name }} (+¥{{ topping.price }})
                  </el-checkbox>
                </el-checkbox-group>
              </div>
            </div>

            <!-- 数量选择 -->
            <div class="quantity-section">
              <label>数量：</label>
              <el-input-number 
                v-model="quantity" 
                :min="1" 
                :max="product.stock"
                :disabled="stockStatus === 'soldout'"
              />
            </div>

            <!-- 操作按钮 -->
            <div class="action-buttons">
              <el-button 
                type="primary" 
                size="large" 
                @click="addToCart"
                :disabled="stockStatus === 'soldout'"
              >
                <el-icon><ShoppingCart /></el-icon>
                加入购物车
              </el-button>
              <el-button 
                type="success" 
                size="large" 
                @click="buyNow"
                :disabled="stockStatus === 'soldout'"
              >
                立即购买
              </el-button>
              <el-button @click="toggleFavorite">
                <el-icon>
                  <component :is="product.isFavorite ? 'StarFilled' : 'Star'" />
                </el-icon>
                {{ product.isFavorite ? '已收藏' : '收藏' }}
              </el-button>
              <el-button @click="shareProduct">
                <el-icon><Share /></el-icon>
                分享
              </el-button>
            </div>

            <!-- 商家信息 -->
            <div class="merchant-info">
              <h3>商家信息</h3>
              <div class="merchant-details">
                <div class="merchant-item">
                  <span class="label">店铺名称：</span>
                  <span class="value">{{ product.merchant.name }}</span>
                </div>
                <div class="merchant-item">
                  <span class="label">评分：</span>
                  <el-rate v-model="product.merchant.rating" disabled show-score />
                </div>
                <div class="merchant-item">
                  <span class="label">配送时间：</span>
                  <span class="value">{{ product.merchant.deliveryTime }}分钟</span>
                </div>
                <div class="merchant-item">
                  <span class="label">配送费：</span>
                  <span class="value">¥{{ product.merchant.deliveryFee }}</span>
                </div>
                <div class="merchant-item">
                  <span class="label">起送价：</span>
                  <span class="value">¥{{ product.merchant.minOrder }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 商品详情标签页 -->
      <div class="product-tabs">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="商品详情" name="detail">
            <div class="detail-content">
              <h3>商品介绍</h3>
              <p>{{ product.detailDescription }}</p>
              
              <h3>营养成分</h3>
              <el-table :data="product.nutrition" border>
                <el-table-column prop="name" label="营养成分" />
                <el-table-column prop="value" label="含量" />
                <el-table-column prop="unit" label="单位" />
              </el-table>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="用户评价" name="reviews">
            <div class="reviews-content">
              <div class="review-summary">
                <div class="rating-overview">
                  <div class="rating-score">{{ product.rating }}</div>
                  <div class="rating-stars">
                    <el-rate v-model="product.rating" disabled />
                  </div>
                  <div class="rating-count">共{{ product.reviewCount }}条评价</div>
                </div>
                <div class="rating-breakdown">
                  <div v-for="(count, index) in product.ratingBreakdown" :key="index" class="rating-item">
                    <span>{{ 5 - index }}星</span>
                    <el-progress :percentage="(count / product.reviewCount) * 100" :show-text="false" />
                    <span>{{ count }}</span>
                  </div>
                </div>
              </div>
              
              <div class="review-filter">
                <el-radio-group v-model="reviewFilter">
                  <el-radio label="all">全部</el-radio>
                  <el-radio label="good">好评</el-radio>
                  <el-radio label="medium">中评</el-radio>
                  <el-radio label="bad">差评</el-radio>
                  <el-radio label="image">有图</el-radio>
                </el-radio-group>
              </div>
              
              <div class="review-list">
                <div v-for="review in filteredReviews" :key="review.id" class="review-item">
                  <div class="review-header">
                    <div class="user-info">
                      <el-avatar :src="review.userAvatar" />
                      <span class="username">{{ review.username }}</span>
                    </div>
                    <div class="review-rating">
                      <el-rate v-model="review.rating" disabled />
                      <span class="review-date">{{ review.date }}</span>
                    </div>
                  </div>
                  <div class="review-content">
                    <p>{{ review.content }}</p>
                    <div v-if="review.images && review.images.length > 0" class="review-images">
                      <SmartImage 
                        v-for="img in review.images" 
                        :key="img" 
                        :src="img" 
                        image-class="review-image"
                        @click="previewImage(img)"
                      />
                    </div>
                  </div>
                  <div class="review-actions">
                    <el-button type="text" @click="likeReview(review)">
                      <el-icon><Good /></el-icon>
                      {{ review.likes }}
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { getProductDetail } from '@/api/product'
import { getPromotionPage, checkUserParticipatedPromotion } from '@/api/promotion'
import { getImagesByRange } from '@/utils/imageLoader'
import { calculatePromotionPrice } from '@/utils/priceCalculator'
import { 
  ShoppingCart, 
  Star, 
  StarFilled, 
  Share,
  Good
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const loading = ref(false)
const defaultImages = getImagesByRange(14, 5) // 默认图片

const product = ref({
  id: null,
  name: '',
  description: '',
  detailDescription: '',
  price: 0,
  originalPrice: null,
  images: [],
  category: '',
  tags: [],
  isHot: false,
  isNew: false,
  isFavorite: false,
  sales: 0,
  rating: 4.5,
  reviewCount: 0,
  stock: 0,
  hasDiscount: false,
  sweetness: ['全糖', '七分糖', '五分糖', '三分糖', '无糖'],
  temperature: ['热饮', '常温', '冷饮'],
  size: ['中杯', '大杯', '超大杯'],
  toppings: [],
  merchant: {
    id: null,
    name: '',
    rating: 0,
    deliveryTime: 0,
    deliveryFee: 0,
    minOrder: 0
  },
  nutrition: [],
  ratingBreakdown: [0, 0, 0, 0, 0],
  reviews: []
})

// 转换商品图片路径
const convertImagePath = (imagePath) => {
  if (!imagePath) return null
  if (imagePath.startsWith('http')) return imagePath
  if (imagePath.startsWith('/')) return imagePath
  return `/api/files/${imagePath}`
}

// 转换商品数据
const convertProduct = (data) => {
  // 处理图片
  let images = []
  if (data.images && Array.isArray(data.images) && data.images.length > 0) {
    images = data.images.map(img => convertImagePath(img)).filter(img => img)
  }
  
  // 如果没有图片，使用默认图片
  if (images.length === 0) {
    images = defaultImages.slice(0, 3)
  }
  
  return {
    id: data.id,
    name: data.name || '',
    description: data.description || '',
    detailDescription: data.detailDescription || data.description || '',
    price: data.price ? parseFloat(data.price) : 0,
    originalPrice: data.originalPrice ? parseFloat(data.originalPrice) : null,
    images: images,
    category: data.categoryName || data.category || '',
    tags: data.promotionTypes || data.tags || [],
    isHot: (data.sales || 0) > 100,
    isNew: false,
    isFavorite: false,
    sales: data.sales || 0,
    rating: data.rating || 4.5,
    reviewCount: data.reviewCount || 0,
    stock: data.stock || 0,
    hasDiscount: !!data.originalPrice || data.isPromotion,
    sweetness: data.sweetnessOptions || ['全糖', '七分糖', '五分糖', '三分糖', '无糖'],
    temperature: data.temperatureOptions || ['热饮', '常温', '冷饮'],
    size: data.sizes ? data.sizes.map(s => s.name || s) : ['中杯', '大杯', '超大杯'],
    toppings: data.toppings || [],
    merchant: data.merchant || {
      id: data.merchantId,
      name: data.merchantName || '',
      rating: 4.5,
      deliveryTime: 25,
      deliveryFee: 3,
      minOrder: 20
    },
    nutrition: data.nutrition || [],
    ratingBreakdown: data.ratingBreakdown || [0, 0, 0, 0, 0],
    reviews: data.reviews || []
  }
}

const selectedSpecs = ref({
  sweetness: '全糖',
  temperature: '常温',
  size: '中杯',
  toppings: []
})

const quantity = ref(1)
const activeTab = ref('detail')
const reviewFilter = ref('all')
const productPromotion = ref(null) // 商品参与的促销活动
const promotionParticipatedStatus = ref(false) // 用户是否参与了促销活动

// 计算基础价格（不含促销）
const basePrice = computed(() => {
  let price = product.value.price || 0
  // 根据规格调整价格
  if (selectedSpecs.value.size === '大杯') {
    price += 3
  } else if (selectedSpecs.value.size === '超大杯') {
    price += 6
  }
  
  // 加料价格
  selectedSpecs.value.toppings.forEach(toppingName => {
    const topping = product.value.toppings.find(t => t.name === toppingName)
    if (topping) {
      price += topping.price || 0
    }
  })
  
  return price
})

// 计算当前价格（含促销）
const currentPrice = computed(() => {
  const base = basePrice.value
  
  // 只有在用户参与了促销活动时才应用价格折扣
  if (productPromotion.value && promotionParticipatedStatus.value) {
    const promotionPrice = calculatePromotionPrice(base, productPromotion.value, quantity.value)
    return promotionPrice.price
  }
  
  return base
})

// 计算原价（用于显示）
const displayOriginalPrice = computed(() => {
  // 如果有促销活动，显示促销前的价格
  if (productPromotion.value) {
    return basePrice.value
  }
  // 否则显示商品的原价
  return product.value.originalPrice || basePrice.value
})

// 计算折扣信息
const discountInfo = computed(() => {
  if (!productPromotion.value) {
    // 没有促销活动，检查是否有原价
    if (product.value.originalPrice && product.value.originalPrice > product.value.price) {
      return {
        discount: product.value.originalPrice - product.value.price,
        discountText: '优惠'
      }
    }
    return null
  }
  
  const promotionPrice = calculatePromotionPrice(basePrice.value, productPromotion.value, quantity.value)
  return {
    discount: promotionPrice.discount,
    discountText: promotionPrice.discountText || '促销'
  }
})

const stockStatus = computed(() => {
  if (product.value.stock === 0) return 'soldout'
  if (product.value.stock < 10) return 'low'
  return 'sufficient'
})

const filteredReviews = computed(() => {
  let reviews = product.value.reviews
  
  switch (reviewFilter.value) {
    case 'good':
      reviews = reviews.filter(r => r.rating >= 4)
      break
    case 'medium':
      reviews = reviews.filter(r => r.rating === 3)
      break
    case 'bad':
      reviews = reviews.filter(r => r.rating <= 2)
      break
    case 'image':
      reviews = reviews.filter(r => r.images && r.images.length > 0)
      break
  }
  
  return reviews
})

const addToCart = async () => {
  try {
    await cartStore.addToCart(product.value, quantity.value, selectedSpecs.value, productPromotion.value)
    ElMessage.success('已添加到购物车')
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const buyNow = () => {
  addToCart()
  router.push('/cart')
}

const toggleFavorite = () => {
  product.value.isFavorite = !product.value.isFavorite
  ElMessage.success(product.value.isFavorite ? '已添加到收藏' : '已取消收藏')
}

const shareProduct = () => {
  ElMessageBox.confirm(
    '选择分享方式',
    '分享商品',
    {
      distinguishCancelAndClose: true,
      confirmButtonText: '微信好友',
      cancelButtonText: '朋友圈',
      type: 'info',
    }
  ).then(() => {
    ElMessage.success('分享到微信好友')
  }).catch((action) => {
    if (action === 'cancel') {
      ElMessage.success('分享到朋友圈')
    }
  })
}

const likeReview = (review) => {
  review.likes++
  ElMessage.success('点赞成功')
}

const previewImage = (image) => {
  // 图片预览功能
  ElMessage.info('图片预览功能')
}

// 加载商品详情
const loadProductDetail = async () => {
  const productId = route.params.id
  if (!productId) {
    ElMessage.error('商品ID不存在')
    router.push('/products')
    return
  }
  
  try {
    loading.value = true
    const result = await getProductDetail(productId)
    
    // 处理返回数据
    let productData = result
    if (result && result.data) {
      productData = result.data
    }
    
    if (productData) {
      product.value = convertProduct(productData)
      
      // 加载商品参与的促销活动
      if (productData.merchantId) {
        await loadProductPromotion(parseInt(productId), productData.merchantId)
      }
    } else {
      ElMessage.error('商品不存在')
      router.push('/products')
    }
  } catch (error) {
    console.error('加载商品详情失败:', error)
    ElMessage.error('加载商品详情失败，请稍后重试')
    router.push('/products')
  } finally {
    loading.value = false
  }
}

// 加载商品参与的促销活动
const loadProductPromotion = async (productId, merchantId) => {
  try {
    const response = await getPromotionPage({
      pageNum: 1,
      pageSize: 10,
      status: 'active',
      merchantId: merchantId
    })
    
    const promotions = response?.records || response?.list || []
    
    // 查找包含该商品的促销活动（限时折扣、第二件半价、买一送一）
    const activePromotion = promotions.find(p => {
      if (p.productIds && Array.isArray(p.productIds) && p.productIds.includes(productId)) {
        // 检查活动类型是否影响价格
        return ['discount', 'second_half_price', 'buy_one_get_one'].includes(p.type)
      }
      return false
    })
    
    if (activePromotion) {
      productPromotion.value = activePromotion
      
      // 检查用户是否参与了该促销活动（仅当用户已登录时）
      if (userStore.isLoggedIn && activePromotion.id) {
        try {
          const hasParticipated = await checkUserParticipatedPromotion(activePromotion.id)
          promotionParticipatedStatus.value = hasParticipated
        } catch (error) {
          console.warn(`检查促销活动 ${activePromotion.id} 参与状态失败:`, error)
          promotionParticipatedStatus.value = false
        }
      } else {
        promotionParticipatedStatus.value = false
      }
    } else {
      promotionParticipatedStatus.value = false
    }
  } catch (error) {
    console.error('加载商品促销活动失败:', error)
    // 不显示错误，静默失败
    promotionParticipatedStatus.value = false
  }
}

// 图片加载失败处理
const handleImageError = (event, imagePath) => {
  console.warn('商品图片加载失败:', imagePath)
  // 使用默认图片
  const defaultImage = defaultImages[0]
  if (defaultImage && event.target.src !== defaultImage) {
    event.target.src = defaultImage
  } else {
    // 如果还是没有，隐藏图片或显示占位图
    event.target.style.display = 'none'
    event.target.onerror = null // 防止无限循环
  }
}

onMounted(() => {
  loadProductDetail()
})
</script>

<style scoped>
.product-detail-container {
  padding: 20px 0;
  min-height: calc(100vh - 120px);
}

.breadcrumb {
  margin-bottom: 20px;
}

.product-images {
  margin-bottom: 20px;
}

.image-item {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.image-item img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.product-info {
  padding: 20px;
}

.product-title {
  font-size: 24px;
  color: #333;
  margin-bottom: 12px;
}

.product-desc {
  color: #666;
  font-size: 16px;
  margin-bottom: 20px;
  line-height: 1.6;
}

.price-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.current-price {
  font-size: 28px;
  font-weight: bold;
  color: #ff6b6b;
}

.original-price {
  font-size: 18px;
  color: #999;
  text-decoration: line-through;
}

.discount-badge {
  background: #ff6b6b;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.stock-status {
  margin-bottom: 24px;
}

.specifications {
  margin-bottom: 24px;
}

.spec-group {
  margin-bottom: 16px;
}

.spec-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.quantity-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.quantity-section label {
  font-weight: 500;
  color: #333;
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-bottom: 32px;
  flex-wrap: wrap;
}

.merchant-info {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
}

.merchant-info h3 {
  margin-bottom: 16px;
  color: #333;
}

.merchant-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.merchant-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.merchant-item .label {
  font-weight: 500;
  color: #666;
  min-width: 80px;
}

.merchant-item .value {
  color: #333;
}

.product-tabs {
  margin-top: 40px;
}

.detail-content h3 {
  margin-bottom: 12px;
  color: #333;
}

.detail-content p {
  color: #666;
  line-height: 1.6;
  margin-bottom: 20px;
}

.review-summary {
  display: flex;
  gap: 40px;
  margin-bottom: 24px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.rating-overview {
  text-align: center;
}

.rating-score {
  font-size: 36px;
  font-weight: bold;
  color: #ff6b6b;
  margin-bottom: 8px;
}

.rating-count {
  color: #666;
  font-size: 14px;
}

.rating-breakdown {
  flex: 1;
}

.rating-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.rating-item span:first-child {
  min-width: 30px;
  font-size: 14px;
  color: #666;
}

.rating-item span:last-child {
  min-width: 20px;
  font-size: 14px;
  color: #666;
}

.review-filter {
  margin-bottom: 24px;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 16px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  font-weight: 500;
  color: #333;
}

.review-rating {
  display: flex;
  align-items: center;
  gap: 8px;
}

.review-date {
  color: #999;
  font-size: 12px;
}

.review-content p {
  color: #333;
  line-height: 1.6;
  margin-bottom: 12px;
}

.review-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.review-images img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
}

.review-actions {
  display: flex;
  gap: 16px;
}

@media (max-width: 768px) {
  .product-detail-container .el-col {
    margin-bottom: 20px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .review-summary {
    flex-direction: column;
    gap: 20px;
  }
}
</style>
