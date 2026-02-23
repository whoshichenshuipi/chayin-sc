<template>
  <div class="cart-container">
    <div class="container">
      <div class="cart-header">
        <h2>购物车</h2>
        <p v-if="cartStore.getTotalCount > 0">
          共 {{ cartStore.getTotalCount }} 件商品
        </p>
      </div>

      <!-- 商品状态提醒 -->
      <div v-if="hasAlerts" class="alerts-section">
        <el-alert
          v-for="alert in allAlerts"
          :key="alert.id"
          :title="alert.title"
          :description="alert.description"
          :type="alert.type"
          :closable="true"
          @close="dismissAlert(alert)"
          class="alert-item"
        />
      </div>

      <!-- 购物车为空 -->
      <el-empty v-if="cartStore.getTotalCount === 0" description="购物车为空">
        <el-button type="primary" @click="$router.push('/products')">
          去购物
        </el-button>
      </el-empty>

      <!-- 购物车内容 -->
      <div v-else class="cart-content" v-loading="validating">
        <el-row :gutter="20">
          <!-- 商品列表 -->
          <el-col :span="16">
            <el-card class="cart-items-card">
              <template #header>
                <div class="card-header">
                  <el-checkbox
                    v-model="selectAll"
                    @change="handleSelectAll"
                  >
                    全选
                  </el-checkbox>
                  <span>商品信息</span>
                  <div class="header-actions">
                    <el-button 
                      v-if="selectedItems.length > 0"
                      type="danger" 
                      size="small"
                      @click="batchDelete"
                    >
                      批量删除 ({{ selectedItems.length }})
                    </el-button>
                    <el-button 
                      type="warning" 
                      size="small"
                      @click="clearCartDialog"
                    >
                      清空购物车
                    </el-button>
                  </div>
                </div>
              </template>

              <!-- 有效商品列表 -->
              <div v-if="cartStore.getValidItems.length > 0" class="cart-items">
                <div
                  v-for="item in cartStore.getValidItems"
                  :key="item.cartItemId"
                  class="cart-item"
                  :class="{ 'price-changed': item.hasPriceChanged, 'low-stock': item.stockStatus === 'low' }"
                >
                  <el-checkbox
                    v-model="item.selected"
                    @change="handleItemSelect"
                  />
                  
                  <div class="item-image">
                    <SmartImage :src="item.image" :alt="item.name" />
                    <!-- 状态标签 -->
                    <div v-if="item.hasPriceChanged" class="status-badge price-badge">
                      价格已更新
                    </div>
                    <div v-if="item.stockStatus === 'low'" class="status-badge stock-badge">
                      库存紧张
                    </div>
                  </div>
                  
                  <div class="item-info">
                    <h3>{{ item.name }}</h3>
                    <p>{{ item.description }}</p>
                    
                    <!-- 规格信息 -->
                    <div v-if="item.selectedSpecs" class="spec-info">
                      <el-tag 
                        v-for="(value, key) in item.selectedSpecs" 
                        :key="key" 
                        size="small"
                        class="spec-tag"
                      >
                        {{ key }}: {{ value }}
                      </el-tag>
                    </div>
                    
                    <div class="item-tags">
                      <el-tag v-for="tag in item.tags" :key="tag" size="small">
                        {{ tag }}
                      </el-tag>
                    </div>
                  </div>
                  
                  <div class="item-price">
                    <!-- 如果有促销活动，显示原价和折扣价格 -->
                    <div v-if="item.promotion && item.originalPrice && item.originalPrice > item.price" class="price-change">
                      <span class="old-price">¥{{ Number(item.originalPrice).toFixed(2) }}</span>
                      <span class="new-price">¥{{ Number(item.price).toFixed(2) }}</span>
                      <div class="promotion-tag" v-if="item.promotion">
                        <el-tag type="danger" size="small">{{ getPromotionTagText(item.promotion) }}</el-tag>
                      </div>
                    </div>
                    <!-- 如果价格已变动（但不是促销），显示价格变动 -->
                    <div v-else-if="item.hasPriceChanged && item.originalPrice" class="price-change">
                      <span class="old-price">¥{{ Number(item.originalPrice).toFixed(2) }}</span>
                      <span class="new-price">¥{{ Number(item.price).toFixed(2) }}</span>
                    </div>
                    <!-- 正常价格 -->
                    <span v-else class="price">¥{{ Number(item.price || 0).toFixed(2) }}</span>
                  </div>
                  
                  <div class="item-quantity">
                    <el-input-number
                      v-model="item.quantity"
                      :min="1"
                      :max="item.stock"
                      :disabled="item.stockStatus === 'low' && item.quantity >= item.stock"
                      @change="handleQuantityChange(item)"
                    />
                    <div v-if="item.stockStatus === 'low'" class="stock-warning">
                      仅剩{{ item.stock }}杯
                    </div>
                  </div>
                  
                  <div class="item-total">
                    <span class="total-price">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
                  </div>
                  
                  <div class="item-actions">
                    <el-button
                      type="danger"
                      :icon="Delete"
                      circle
                      size="small"
                      @click="removeItem(item.cartItemId)"
                    />
                  </div>
                </div>
              </div>

              <!-- 已下架商品列表 -->
              <div v-if="cartStore.getDiscontinuedItems.length > 0" class="discontinued-items">
                <el-divider>
                  <el-tag type="danger">已下架商品</el-tag>
                </el-divider>
                <div
                  v-for="item in cartStore.getDiscontinuedItems"
                  :key="item.cartItemId"
                  class="cart-item discontinued"
                >
                  <div class="item-image">
                    <SmartImage :src="item.image" :alt="item.name" />
                    <div class="status-badge discontinued-badge">
                      商品已下架
                    </div>
                  </div>
                  
                  <div class="item-info">
                    <h3>{{ item.name }}</h3>
                    <p class="discontinued-text">该商品已下架，无法购买</p>
                  </div>
                  
                  <div class="item-actions">
                    <el-button
                      type="danger"
                      :icon="Delete"
                      circle
                      size="small"
                      @click="removeItem(item.cartItemId)"
                    />
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>

          <!-- 结算区域 -->
          <el-col :span="8">
            <el-card class="checkout-card">
              <template #header>
                <span>结算信息</span>
              </template>

              <div class="checkout-info">
                <div class="info-row">
                  <span>商品总价：</span>
                  <span>¥{{ selectedTotal.toFixed(2) }}</span>
                </div>
                <div class="info-row">
                  <span>配送费：</span>
                  <span v-if="selectedTotal >= 30">免费</span>
                  <span v-else>¥5.00</span>
                </div>
                <div class="info-row" v-if="fullReduceDiscount > 0">
                  <span>满减优惠：</span>
                  <span class="discount-amount">-¥{{ fullReduceDiscount.toFixed(2) }}</span>
                </div>
                <div class="info-row">
                  <span>优惠券：</span>
                  <el-link type="primary" @click="showCoupons = true">
                    {{ selectedCoupon ? `已选: -¥${couponDiscount.toFixed(2)}` : '选择优惠券' }}
                  </el-link>
                </div>
                <el-divider />
                <div class="info-row total-row">
                  <span>实付金额：</span>
                  <span class="total-amount">¥{{ finalTotal.toFixed(2) }}</span>
                </div>
              </div>

              <div class="checkout-actions">
                <el-button
                  type="primary"
                  size="large"
                  :disabled="selectedItems.length === 0 || loading"
                  :loading="loading"
                  @click="goToCheckout"
                  class="checkout-button"
                >
                  立即结算 ({{ selectedItems.length }})
                </el-button>
              </div>
            </el-card>

            <!-- 推荐商品 -->
            <el-card class="recommend-card" style="margin-top: 20px;">
              <template #header>
                <span>推荐商品</span>
              </template>
              
              <div class="recommend-items">
                <div
                  v-for="product in recommendProducts"
                  :key="product.id"
                  class="recommend-item"
                  @click="addRecommendToCart(product)"
                >
                  <SmartImage :src="product.image" :alt="product.name" />
                  <div class="recommend-info">
                    <h4>{{ product.name }}</h4>
                    <p class="recommend-price">¥{{ product.price }}</p>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 优惠券选择对话框 -->
    <el-dialog
      v-model="showCoupons"
      title="选择优惠券"
      width="500px"
    >
      <div class="coupon-list">
        <div
          v-for="coupon in availableCoupons"
          :key="coupon.id"
          class="coupon-item"
          :class="{ active: selectedCoupon?.id === coupon.id }"
          @click="selectCoupon(coupon)"
        >
          <div class="coupon-info">
            <h4>{{ coupon.name }}</h4>
            <p>{{ coupon.description }}</p>
            <span class="coupon-condition">{{ coupon.condition }}</span>
          </div>
          <div class="coupon-value">
            <span v-if="coupon.type === 'discount'">
              {{ coupon.value > 1 ? coupon.value : (coupon.value * 10).toFixed(0) }}折
            </span>
            <span v-else>¥{{ coupon.value }}</span>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showCoupons = false">取消</el-button>
        <el-button type="primary" @click="applyCoupon">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { Delete } from '@element-plus/icons-vue'
import { validateCartItems } from '@/api/cart'
import { getRecommendedProducts, getProductDetail } from '@/api/product'
import { getCouponPage } from '@/api/coupon'
import { getMyCoupons } from '@/api/coupon'
import { getPromotionPage } from '@/api/promotion'
import { useUserStore } from '@/stores/user'
import { getImageByIndex, getRandomImage } from '@/utils/imageLoader'
import { calculateCouponDiscount, calculatePromotionPrice, calculateFullReduceDiscount } from '@/utils/priceCalculator'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const showCoupons = ref(false)
const selectedCoupon = ref(null)
const loading = ref(false)
const validating = ref(false)

const availableCoupons = ref([])
const recommendProducts = ref([])

// 商品状态提醒
const allAlerts = computed(() => {
  const alerts = []
  
  // 价格变动提醒
  cartStore.priceAlerts.forEach(alert => {
    alerts.push({
      id: `price_${alert.cartItemId}`,
      title: '价格变动提醒',
      description: `${alert.productName} 价格从 ¥${alert.oldPrice} 变更为 ¥${alert.newPrice}`,
      type: 'warning',
      cartItemId: alert.cartItemId
    })
  })
  
  // 库存不足提醒
  cartStore.stockAlerts.forEach(alert => {
    alerts.push({
      id: `stock_${alert.cartItemId}`,
      title: '库存不足提醒',
      description: `${alert.productName} 库存仅剩 ${alert.currentStock} 杯`,
      type: 'warning',
      cartItemId: alert.cartItemId
    })
  })
  
  // 商品下架提醒
  cartStore.discontinuedAlerts.forEach(alert => {
    alerts.push({
      id: `discontinued_${alert.cartItemId}`,
      title: '商品下架提醒',
      description: `${alert.productName} 已下架，无法购买`,
      type: 'error',
      cartItemId: alert.cartItemId
    })
  })
  
  return alerts
})

const hasAlerts = computed(() => allAlerts.value.length > 0)

const selectAll = computed({
  get: () => cartStore.getValidItems.every(item => item.selected),
  set: (value) => {
    cartStore.getValidItems.forEach(item => {
      item.selected = value
    })
  }
})

const selectedItems = computed(() => 
  cartStore.getValidItems.filter(item => item.selected)
)

const selectedTotal = computed(() => 
  selectedItems.value.reduce((sum, item) => sum + (item.price * item.quantity), 0)
)

const deliveryFee = computed(() => 
  selectedTotal.value >= 30 ? 0 : 5
)

const couponDiscount = computed(() => {
  if (!selectedCoupon.value) return 0
  
  // 使用工具函数计算优惠券折扣（实时响应优惠券选择变化）
  return calculateCouponDiscount(selectedTotal.value, selectedCoupon.value)
})

// 计算满减优惠（从参与满减活动的商品中查找）
const fullReduceDiscount = computed(() => {
  // 查找参与满减活动的商品
  const fullReduceItems = selectedItems.value.filter(item => 
    item.promotion && item.promotion.type === 'full_reduce' && item.promotion.status === 'active'
  )
  
  if (fullReduceItems.length === 0) {
    return 0
  }
  
  // 获取满减活动（所有参与满减的商品应该属于同一个满减活动）
  const promotion = fullReduceItems[0].promotion
  
  // 计算满减优惠（基于商品总价）
  const discount = calculateFullReduceDiscount(selectedTotal.value, promotion)
  
  if (discount > 0) {
    console.log('💰 满减活动应用:', {
      活动名称: promotion.name,
      活动ID: promotion.id,
      商品总价: selectedTotal.value,
      满减规则: promotion.fullReduceRules,
      优惠金额: discount
    })
  }
  
  return discount
})

const finalTotal = computed(() => {
  const total = selectedTotal.value + deliveryFee.value - couponDiscount.value - fullReduceDiscount.value
  return Math.max(0, total)
})

// 格式化商品数据（用于推荐商品）
const formatRecommendProduct = (product) => {
  const images = product.images || []
  let imageUrl = Array.isArray(images) ? (images[0] || '') : (images || '')
  
  // 如果图片为空或无效，使用assets中的随机图片
  if (!imageUrl || imageUrl === '/product-default.jpg' || imageUrl === 'undefined' || imageUrl === 'null') {
    imageUrl = getImageByIndex(product.id || 0) || getRandomImage() || '/product-default.jpg'
  }
  
  return {
    id: product.id,
    name: product.name,
    price: product.price,
    originalPrice: product.originalPrice,
    image: imageUrl,
    stock: product.stock,
    status: product.status
  }
}

// 格式化优惠券数据
const formatCoupon = (coupon) => {
  let condition = ''
  if (coupon.threshold && coupon.threshold > 0) {
    condition = `满${coupon.threshold}元可用`
  } else {
    condition = '无门槛'
  }
  
  return {
    id: coupon.id,
    name: coupon.name,
    description: coupon.description || coupon.name,
    condition: condition,
    type: coupon.type,
    value: coupon.discount || coupon.value,
    threshold: coupon.threshold
  }
}

// 加载推荐商品
const loadRecommendProducts = async () => {
  try {
    const products = await getRecommendedProducts(3)
    recommendProducts.value = products.map(formatRecommendProduct)
  } catch (error) {
    console.error('加载推荐商品失败:', error)
    recommendProducts.value = []
  }
}

// 加载可用优惠券
const loadAvailableCoupons = async () => {
  try {
    // 如果用户已登录，尝试加载我的优惠券
    if (userStore.userInfo?.id) {
      try {
        const myCouponsResponse = await getMyCoupons({
          pageNum: 1,
          pageSize: 20,
          status: 'unused'
        })
        // getMyCoupons在404时会返回空列表，不会抛出异常
        const myCouponsList = myCouponsResponse?.records || myCouponsResponse?.list || []
        if (myCouponsList.length > 0) {
          availableCoupons.value = myCouponsList.map(formatCoupon)
          return
        }
      } catch (error) {
        // 只有非404错误才记录
        if (error.response?.status !== 404) {
          console.error('加载我的优惠券失败:', error)
        }
        // 404错误已经由API层处理，继续执行
      }
    }
    
    // 加载可领取的优惠券
    try {
      const response = await getCouponPage({
        pageNum: 1,
        pageSize: 20,
        status: 'active'
      })
      // getCouponPage在404时会返回空列表，不会抛出异常
      const couponsList = response?.records || response?.list || []
      availableCoupons.value = couponsList.map(formatCoupon)
    } catch (error) {
      // 只有非404错误才记录
      if (error.response?.status !== 404) {
        console.error('加载可领取优惠券失败:', error)
      }
      // 404错误已经由API层处理，设置为空数组
      availableCoupons.value = []
    }
  } catch (error) {
    // 兜底处理：任何未预期的错误
    console.error('加载优惠券失败:', error)
    availableCoupons.value = []
  }
}

// 重新加载购物车商品的促销活动并更新价格
const reloadCartItemPromotions = async () => {
  console.log('🛒 开始重新加载购物车商品促销活动，商品数量:', cartStore.items.length)
  if (cartStore.items.length === 0) {
    console.log('🛒 购物车为空，跳过加载促销活动')
    return
  }
  
  try {
    // 获取所有商户ID
    const merchantIds = [...new Set(cartStore.items.map(item => item.merchantId).filter(id => id))]
    console.log('🛒 需要加载促销活动的商户ID:', merchantIds)
    console.log('🛒 购物车商品列表:', cartStore.items.map(item => ({
      id: item.id,
      productId: item.productId,
      name: item.name,
      merchantId: item.merchantId,
      price: item.price,
      originalPrice: item.originalPrice
    })))
    
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
        console.log(`🛒 商户 ${merchantId} 的促销活动 (共${promotions.length}个):`, promotions.map(p => ({
          id: p.id,
          名称: p.name,
          类型: p.type,
          商品ID列表: p.productIds || [],
          商品数量: p.productIds?.length || 0,
          折扣率: p.discountRate,
          满减规则: p.fullReduceRules,
          状态: p.status,
          开始时间: p.startTime,
          结束时间: p.endTime
        })))
        
        // 为每个购物车商品匹配促销活动并更新价格（使用 for...of 以支持 await）
        for (const item of cartStore.items) {
          if (item.merchantId === merchantId) {
            // 使用商品ID（可能是id或productId）
            const productId = item.id || item.productId
            if (!productId) {
              console.warn(`🛒 商品缺少ID:`, {
                商品名称: item.name,
                itemId: item.id,
                productId: item.productId,
                merchantId: item.merchantId
              })
              continue
            }
            
            // 如果商品没有促销标签，尝试从后端获取商品详情
            if ((!item.promotionTypes || item.promotionTypes.length === 0) && productId) {
              try {
                console.log(`  🔄 商品 ${productId} 缺少促销标签，尝试从后端获取商品详情...`)
                const { getProductDetail } = await import('@/api/product')
                const productDetail = await getProductDetail(productId)
                if (productDetail && productDetail.promotionTypes) {
                  item.promotionTypes = productDetail.promotionTypes
                  item.merchantId = productDetail.merchantId || item.merchantId
                  console.log(`  ✅ 已更新商品 ${productId} 的促销标签:`, productDetail.promotionTypes)
                  // 保存更新后的商品信息
                  cartStore.saveToLocalStorage()
                }
              } catch (error) {
                console.warn(`  ⚠️ 获取商品 ${productId} 详情失败:`, error)
              }
            }
            
            console.log(`\n📦 开始处理商品:`, {
              商品ID: productId,
              商品名称: item.name,
              商户ID: item.merchantId,
              当前价格: item.price,
              原价: item.originalPrice,
              数量: item.quantity,
              商品促销标签: item.promotionTypes || item.tags || []
            })
            
            // 查找匹配的促销活动
            // 1. 如果 productIds 包含该商品ID（所有类型都支持）
            // 2. 如果 productIds 为空，通过商品的促销标签匹配（仅限影响价格的类型）
            console.log(`🔍 开始匹配商品 ${productId} 的促销活动，可用的促销活动数量: ${promotions.length}`)
            
            const activePromotion = promotions.find(p => {
              // 检查活动类型是否影响价格
              const isPriceAffecting = ['discount', 'second_half_price', 'buy_one_get_one'].includes(p.type)
              
              console.log(`  ⚙️ 检查促销活动 ${p.id} (${p.name}):`, {
                活动类型: p.type,
                是否影响价格: isPriceAffecting,
                商品ID列表: p.productIds || [],
                商品ID列表长度: p.productIds?.length || 0
              })
              
              // 如果 productIds 包含该商品ID
              if (p.productIds && Array.isArray(p.productIds) && p.productIds.length > 0) {
                const isIncluded = p.productIds.includes(productId)
                console.log(`    ✓ 促销活动 ${p.id} 有商品ID列表，商品 ${productId} ${isIncluded ? '包含' : '不包含'}在列表中`)
                
                if (isIncluded) {
                  const shouldMatch = isPriceAffecting || p.type === 'full_reduce'
                  console.log(`    ${shouldMatch ? '✅' : '❌'} 商品 ${productId} 通过 productIds 匹配促销活动 ${p.id} (${p.name}):`, {
                    活动类型: p.type,
                    活动名称: p.name,
                    是否影响价格: isPriceAffecting,
                    是否匹配: shouldMatch,
                    商品ID列表: p.productIds
                  })
                  // 满减活动虽然不直接影响价格，但可以匹配用于显示活动信息
                  // 其他类型如果影响价格，则返回true
                  return shouldMatch
                }
                return false
              }
              
              // 如果 productIds 为空，通过商品的促销标签匹配（仅限影响价格的类型）
              // 满减活动不通过标签匹配，必须明确绑定商品
              if ((!p.productIds || p.productIds.length === 0) && isPriceAffecting) {
                // 检查商品是否有促销标签，如果有且类型匹配，则认为参与了活动
                const itemPromotionTypes = item.promotionTypes || item.tags || []
                const hasPromotionTag = itemPromotionTypes.some(tag => {
                  const tagMap = {
                    'discount': 'discount',
                    'flash-sale': 'discount',
                    'combo': 'buy_one_get_one',
                    'second-half': 'second_half_price'
                  }
                  return tagMap[tag] === p.type
                })
                
                console.log(`    ⚙️ 促销活动 ${p.id} 没有商品ID列表，检查促销标签:`, {
                  商品促销标签: itemPromotionTypes,
                  是否匹配: hasPromotionTag
                })
                
                if (hasPromotionTag) {
                  console.log(`    ✅ 商品 ${productId} 通过促销标签匹配促销活动 ${p.id} (${p.name}):`, {
                    活动类型: p.type,
                    活动名称: p.name,
                    商品促销标签: itemPromotionTypes
                  })
                  return true
                }
              } else if (!p.productIds || p.productIds.length === 0) {
                console.log(`    ❌ 促销活动 ${p.id} 没有商品ID列表，且${isPriceAffecting ? '影响价格但' : ''}不通过标签匹配`)
              }
              
              return false
            })
            
            if (activePromotion) {
              console.log(`\n✅ 商品 ${productId} (${item.name}) 找到匹配的促销活动:`)
              console.log(`  活动ID: ${activePromotion.id}`)
              console.log(`  活动名称: ${activePromotion.name || '未命名'}`)
              console.log(`  活动类型: ${activePromotion.type}`)
              console.log(`  活动描述: ${activePromotion.description || '无'}`)
              console.log(`  折扣率: ${activePromotion.discountRate || '无'}`)
              console.log(`  满减规则:`, activePromotion.fullReduceRules || '无')
              console.log(`  活动状态: ${activePromotion.status}`)
              console.log(`  开始时间: ${activePromotion.startTime}`)
              console.log(`  结束时间: ${activePromotion.endTime}`)
              console.log(`  商品ID列表:`, activePromotion.productIds || [])
              
              // 更新促销活动信息
              item.promotion = activePromotion
              item.promotionId = activePromotion.id
              
              // 确保保存了原始价格（如果还没有保存，使用当前价格作为原价）
              if (!item.originalPrice || item.originalPrice <= 0) {
                item.originalPrice = item.price
                console.log(`  💰 设置商品原价: ${item.originalPrice}`)
              }
              
              // 满减活动不直接影响单个商品价格，跳过价格计算
              if (activePromotion.type === 'full_reduce') {
                console.log(`  ℹ️ 商品 ${productId} 参与满减活动 ${activePromotion.id}，但不影响单个商品价格`)
                console.log(`  💰 满减活动在订单总价上应用，当前商品价格保持不变: ${item.price}`)
                // 满减活动不修改商品价格，保持原价
                item.discountAmount = 0
              } else {
                // 其他类型的促销活动需要检查用户参与状态并计算价格
                try {
                  console.log(`  🔍 检查用户是否参与促销活动 ${activePromotion.id}...`)
                  const hasParticipated = await checkUserParticipatedPromotion(activePromotion.id)
                  console.log(`  ${hasParticipated ? '✅' : '❌'} 用户${hasParticipated ? '已' : '未'}参与促销活动 ${activePromotion.id}`)
                  
                  if (hasParticipated) {
                    // 用户已参与，应用促销价格（使用保存的原价）
                    const originalPrice = item.originalPrice
                    const promotionPriceInfo = calculatePromotionPrice(originalPrice, activePromotion, item.quantity)
                    console.log(`  💰 应用促销价格计算:`)
                    console.log(`    原价: ¥${originalPrice}`)
                    console.log(`    数量: ${item.quantity}`)
                    console.log(`    活动类型: ${activePromotion.type}`)
                    console.log(`    折扣率: ${activePromotion.discountRate || '无'}`)
                    console.log(`    促销后价格: ¥${promotionPriceInfo.price}`)
                    console.log(`    优惠金额: ¥${promotionPriceInfo.discount}`)
                    
                    item.price = promotionPriceInfo.price
                    item.discountAmount = promotionPriceInfo.discount
                    // 确保原价保持不变
                    item.originalPrice = originalPrice
                    
                    console.log(`  ✅ 商品价格已更新: 原价 ¥${item.originalPrice} → 促销价 ¥${item.price} (优惠 ¥${item.discountAmount})`)
                  } else {
                    // 用户未参与，恢复原价
                    console.log(`  ⚠️ 用户未参与促销活动，恢复原价`)
                    if (item.originalPrice) {
                      item.price = item.originalPrice
                      console.log(`  💰 商品价格已恢复为原价: ¥${item.price}`)
                    }
                    item.discountAmount = 0
                  }
                } catch (error) {
                  console.error(`  ❌ 检查促销活动 ${activePromotion.id} 参与状态失败:`, error)
                  // 检查失败时，如果有原价则恢复原价，否则保持当前价格
                  if (item.originalPrice) {
                    item.price = item.originalPrice
                    console.log(`  💰 检查失败，恢复原价: ¥${item.price}`)
                  }
                  item.discountAmount = 0
                }
              }
              
              console.log(`  📊 商品 ${productId} 最终状态:`, {
                名称: item.name,
                原价: item.originalPrice,
                当前价格: item.price,
                优惠金额: item.discountAmount,
                数量: item.quantity,
                促销活动ID: item.promotionId,
                促销活动类型: item.promotion?.type
              })
            } else {
              // 如果没有促销活动，恢复原价
              console.log(`\n❌ 商品 ${productId} (${item.name}) 没有匹配的促销活动`)
              console.log(`  检查了 ${promotions.length} 个促销活动，但都没有匹配`)
              console.log(`  商品促销标签:`, item.promotionTypes || item.tags || [])
              
              if (item.originalPrice && item.originalPrice !== item.price) {
                console.log(`  💰 恢复原价: ¥${item.price} → ¥${item.originalPrice}`)
                item.price = item.originalPrice
                item.promotion = null
                item.promotionId = null
              } else {
                console.log(`  💰 价格保持不变: ¥${item.price}`)
              }
            }
          } else {
            console.log(`🛒 商品缺少ID或商户ID不匹配:`, {
              productId: productId,
              itemMerchantId: item.merchantId,
              targetMerchantId: merchantId
            })
          }
        }
      } catch (error) {
        console.error(`加载商户 ${merchantId} 的促销活动失败:`, error)
        // 继续处理其他商户
      }
    }
    
    // 重新计算总价并保存
    cartStore.calculateTotals()
    cartStore.saveToLocalStorage()
    
    console.log(`\n📊 促销活动加载完成，购物车商品汇总:`)
    console.log(`  商品总数: ${cartStore.items.length}`)
    console.log(`  参与促销的商品: ${cartStore.items.filter(item => item.promotionId).length}`)
    console.log(`  未参与促销的商品: ${cartStore.items.filter(item => !item.promotionId).length}`)
    console.log(`\n  商品详情:`)
    cartStore.items.forEach((item, index) => {
      const productId = item.id || item.productId
      console.log(`  ${index + 1}. 商品 ${productId} (${item.name}):`)
      console.log(`     原价: ¥${item.originalPrice || item.price}`)
      console.log(`     当前价格: ¥${item.price}`)
      console.log(`     优惠金额: ¥${item.discountAmount || 0}`)
      console.log(`     数量: ${item.quantity}`)
      if (item.promotionId) {
        console.log(`     ✅ 参与促销活动: ID=${item.promotionId}, 类型=${item.promotion?.type || '未知'}, 名称=${item.promotion?.name || '未命名'}`)
      } else {
        console.log(`     ❌ 未参与促销活动`)
      }
    })
    console.log(`\n  购物车总计:`)
    console.log(`    商品总价: ¥${cartStore.totalPrice || 0}`)
    console.log(`    优惠总金额: ¥${cartStore.items.reduce((sum, item) => sum + (item.discountAmount || 0) * item.quantity, 0)}`)
  } catch (error) {
    console.error('❌ 重新加载购物车商品促销活动失败:', error)
    // 不显示错误，静默失败
  }
}

// 验证购物车商品状态
const validateCartItemsStatus = async () => {
  console.log('🛒 开始验证购物车商品状态')
  if (cartStore.items.length === 0) {
    console.log('🛒 购物车为空，跳过验证')
    return
  }
  
  try {
    validating.value = true
    const validations = await validateCartItems(cartStore.items)
    
    // 更新每个商品的状态
    validations.forEach(validation => {
      if (validation.product) {
        cartStore.updateItemInfo(validation.cartItemId, validation.product)
      }
    })
    
    // 重新加载促销活动并更新价格
    console.log('🛒 开始重新加载促销活动')
    await reloadCartItemPromotions()
    
    // 检查商品状态
    cartStore.checkItemStatus()
  } catch (error) {
    console.error('❌ 验证购物车商品状态失败:', error)
  } finally {
    validating.value = false
  }
}

onMounted(async () => {
  // 初始化商品选中状态
  cartStore.getCartItems.forEach(item => {
    if (item.selected === undefined) {
      item.selected = true
    }
    // 确保每个商品都有 originalPrice（如果还没有）
    if (!item.originalPrice || item.originalPrice <= 0) {
      item.originalPrice = item.price || 0
    }
  })
  
  // 验证购物车商品状态（检查价格、库存等）
  await validateCartItemsStatus()
  
  // 加载推荐商品
  await loadRecommendProducts()
  
  // 加载可用优惠券
  await loadAvailableCoupons()
})

const handleSelectAll = (checked) => {
  cartStore.getValidItems.forEach(item => {
    item.selected = checked
  })
}

const handleItemSelect = () => {
  // 自动更新全选状态
}

// 获取促销活动标签文本
const getPromotionTagText = (promotion) => {
  if (!promotion) return ''
  
  const typeMap = {
    'discount': `${promotion.discountRate || ''}折`,
    'second_half_price': '第二件半价',
    'buy_one_get_one': '买一送一',
    'full_reduce': '满减'
  }
  
  return typeMap[promotion.type] || '促销'
}

const handleQuantityChange = async (item) => {
  try {
    await cartStore.updateQuantity(item.cartItemId, item.quantity)
  } catch (error) {
    ElMessage.error(error.message)
    // 恢复原数量
    item.quantity = Math.min(item.quantity, item.stock)
  }
}

const removeItem = async (cartItemId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    cartStore.removeFromCart(cartItemId)
    ElMessage.success('已删除商品')
  } catch {
    // 用户取消删除
  }
}

// 批量删除
const batchDelete = async () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择要删除的商品')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedItems.value.length} 件商品吗？`, 
      '批量删除', 
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const itemIds = selectedItems.value.map(item => item.cartItemId)
    cartStore.removeMultipleItems(itemIds)
    ElMessage.success(`已删除 ${itemIds.length} 件商品`)
  } catch {
    // 用户取消删除
  }
}

// 清空购物车对话框
const clearCartDialog = async () => {
  try {
    const result = await ElMessageBox.confirm(
      '确定要清空购物车吗？',
      '清空购物车',
      {
        distinguishCancelAndClose: true,
        confirmButtonText: '全部清空',
        cancelButtonText: '保留有效商品',
        type: 'warning'
      }
    )
    
    cartStore.clearCart(false)
    ElMessage.success('购物车已清空')
  } catch (action) {
    if (action === 'cancel') {
      // 保留未失效优惠商品
      cartStore.clearCart(true)
      ElMessage.success('已清空失效商品，保留有效商品')
    }
  }
}

const goToCheckout = async () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }
  
  // 验证选中商品状态
  try {
    loading.value = true
    const validations = await validateCartItems(selectedItems.value)
    
    // 检查是否有无效商品
    const invalidItems = validations.filter(v => !v.isValid)
    if (invalidItems.length > 0) {
      const invalidNames = invalidItems.map(item => {
        const cartItem = selectedItems.value.find(i => i.cartItemId === item.cartItemId)
        return cartItem?.name || '商品'
      }).join('、')
      
      ElMessage.warning(`${invalidNames}已失效，请移除后重新结算`)
      return
    }
    
    // 更新选中商品的最新状态
    validations.forEach(validation => {
      if (validation.product) {
        cartStore.updateItemInfo(validation.cartItemId, validation.product)
      }
    })
    
    // 将选中商品和优惠券信息传递到订单确认页
    router.push({
      path: '/order-confirm',
      query: {
        couponId: selectedCoupon.value?.id || ''
      }
    })
  } catch (error) {
    console.error('验证商品状态失败:', error)
    ElMessage.error('验证商品状态失败，请重试')
  } finally {
    loading.value = false
  }
}

const selectCoupon = (coupon) => {
  selectedCoupon.value = coupon
}

const applyCoupon = () => {
  if (selectedCoupon.value) {
    ElMessage.success('优惠券已应用')
  }
  showCoupons.value = false
}

const addRecommendToCart = async (product) => {
  try {
    // 获取完整的商品详情
    const productDetail = await getProductDetail(product.id)
    
    // 格式化商品数据（包含所有必要字段，特别是 promotionTypes）
    const cartProduct = {
      id: productDetail.id,
      name: productDetail.name,
      description: productDetail.description,
      price: productDetail.price,
      originalPrice: productDetail.originalPrice || productDetail.price,
      stock: productDetail.stock,
      status: productDetail.status,
      images: productDetail.images || [],
      promotionTypes: productDetail.promotionTypes || [], // 确保包含促销标签
      merchantId: productDetail.merchantId, // 确保包含商户ID
      image: (() => {
        const img = Array.isArray(productDetail.images) ? (productDetail.images[0] || '') : (productDetail.images || '')
        if (!img || img === '/product-default.jpg' || img === 'undefined' || img === 'null') {
          return getImageByIndex(productDetail.id || 0) || getRandomImage() || '/product-default.jpg'
        }
        return img
      })()
    }
    
    console.log('🛒 添加到购物车的商品信息:', {
      id: cartProduct.id,
      name: cartProduct.name,
      promotionTypes: cartProduct.promotionTypes,
      merchantId: cartProduct.merchantId
    })
    
    cartStore.addToCart(cartProduct, 1, {})
    ElMessage.success('已添加到购物车')
    
    // 重新验证商品状态
    await validateCartItemsStatus()
  } catch (error) {
    console.error('添加到购物车失败:', error)
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('添加到购物车失败')
    }
  }
}

// 关闭提醒
const dismissAlert = (alert) => {
  if (alert.id.startsWith('price_')) {
    cartStore.clearAlerts('price')
  } else if (alert.id.startsWith('stock_')) {
    cartStore.clearAlerts('stock')
  } else if (alert.id.startsWith('discontinued_')) {
    cartStore.clearAlerts('discontinued')
  }
}
</script>

<style scoped>
.cart-container {
  padding: 20px 0;
  min-height: calc(100vh - 120px);
}

.cart-header {
  margin-bottom: 20px;
}

.cart-header h2 {
  color: #333;
  margin-bottom: 8px;
}

.cart-header p {
  color: #666;
}

/* 商品状态提醒样式 */
.alerts-section {
  margin-bottom: 20px;
}

.alert-item {
  margin-bottom: 12px;
}

.cart-content {
  margin-top: 20px;
}

.cart-items-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.cart-items {
  max-height: 600px;
  overflow-y: auto;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  gap: 16px;
  transition: all 0.3s;
}

.cart-item:last-child {
  border-bottom: none;
}

/* 价格变动样式 */
.cart-item.price-changed {
  background-color: #fff7e6;
  border-left: 4px solid #ff9500;
  padding-left: 12px;
}

/* 库存不足样式 */
.cart-item.low-stock {
  background-color: #fff2f0;
  border-left: 4px solid #ff4d4f;
  padding-left: 12px;
}

/* 已下架商品样式 */
.cart-item.discontinued {
  background-color: #f5f5f5;
  opacity: 0.6;
}

.item-image {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
  position: relative;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

/* 状态标签 */
.status-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 10px;
  color: white;
  font-weight: bold;
  z-index: 1;
}

.price-badge {
  background-color: #ff9500;
}

.stock-badge {
  background-color: #ff4d4f;
}

.discontinued-badge {
  background-color: #8c8c8c;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-info h3 {
  margin-bottom: 4px;
  color: #333;
  font-size: 16px;
}

.item-info p {
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
  line-height: 1.4;
}

/* 规格信息样式 - 绿色区分 */
.spec-info {
  margin-bottom: var(--spacing-sm);
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
}

.spec-tag {
  background-color: var(--color-secondary-lighter);
  color: var(--color-secondary-dark);
  border: 1px solid var(--color-secondary-light);
  border-radius: var(--radius-md);
  padding: var(--spacing-xs) var(--spacing-sm);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
}

.item-tags {
  margin-bottom: 8px;
}

.item-tags .el-tag {
  margin-right: 4px;
  margin-bottom: 4px;
}

/* 已下架商品文字样式 */
.discontinued-text {
  color: #8c8c8c !important;
  font-style: italic;
}

.item-price {
  width: 80px;
  text-align: center;
}

.price {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
}

/* 价格变动样式 */
.price-change {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.old-price {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
}

.new-price {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
}

.promotion-tag {
  margin-top: 2px;
}

.item-quantity {
  width: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

/* 库存警告样式 */
.stock-warning {
  font-size: 10px;
  color: #ff4d4f;
  text-align: center;
  white-space: nowrap;
}

.item-total {
  width: 100px;
  text-align: center;
}

.total-price {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
}

.item-actions {
  width: 40px;
  text-align: center;
}

.checkout-card {
  position: sticky;
  top: 20px;
}

.checkout-info {
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  color: #666;
}

.total-row {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.total-amount {
  color: var(--color-primary);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
}

.checkout-actions {
  margin-top: 20px;
}

.checkout-button {
  width: 100%;
  height: 48px;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  background: linear-gradient(135deg, var(--color-primary) 0%, #C1121F 100%);
  border: none;
  border-radius: var(--radius-lg);
  color: white;
  box-shadow: var(--shadow-md);
  transition: all var(--transition-fast);
}

.checkout-button:hover:not(:disabled) {
  background: linear-gradient(135deg, var(--color-primary-light) 0%, var(--color-primary) 100%);
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
  filter: brightness(1.1);
}

.checkout-button:active:not(:disabled) {
  transform: translateY(0);
}

.recommend-card {
  margin-top: 20px;
}

.recommend-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recommend-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.recommend-item:hover {
  background-color: #f5f5f5;
}

.recommend-item img {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}

.recommend-info h4 {
  margin-bottom: 4px;
  color: #333;
  font-size: 14px;
}

.recommend-price {
  color: #ff6b6b;
  font-weight: bold;
  font-size: 14px;
}

.coupon-list {
  max-height: 400px;
  overflow-y: auto;
}

.coupon-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.coupon-item:hover {
  border-color: #ff6b6b;
}

.coupon-item.active {
  border-color: #ff6b6b;
  background-color: #fff5f5;
}

.coupon-info h4 {
  margin-bottom: 4px;
  color: #333;
}

.coupon-info p {
  color: #666;
  font-size: 14px;
  margin-bottom: 4px;
}

.coupon-condition {
  color: #999;
  font-size: 12px;
}

.coupon-value {
  font-size: 18px;
  font-weight: bold;
  color: #ff6b6b;
}

@media (max-width: 768px) {
  .cart-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .item-image {
    width: 60px;
    height: 60px;
  }
  
  .item-quantity,
  .item-total {
    width: auto;
  }
  
  .checkout-card {
    position: static;
    margin-top: 20px;
  }
}
</style>
