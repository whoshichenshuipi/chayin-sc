<template>
  <div class="products-container">
    <div class="container">
      <!-- 搜索栏 -->
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

      <!-- 多级筛选栏 -->
      <div class="filter-bar">
        <!-- 基础筛选 -->
        <div class="filter-row">
          <el-row :gutter="20">
            <el-col :span="4">
              <el-select v-model="filters.category" placeholder="选择分类" clearable @change="handleCategoryChange">
                <el-option label="全部" value="" />
                <el-option 
                  v-for="category in categories" 
                  :key="category.id"
                  :label="category.name" 
                  :value="category.name" 
                />
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-select v-model="filters.priceRange" placeholder="价格区间" clearable>
                <el-option label="全部" value="" />
                <el-option label="0-15元" value="0-15" />
                <el-option label="15-25元" value="15-25" />
                <el-option label="25元以上" value="25+" />
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-select v-model="filters.sortBy" placeholder="排序方式">
                <el-option label="默认排序" value="default" />
                <el-option label="价格从低到高" value="price-asc" />
                <el-option label="价格从高到低" value="price-desc" />
                <el-option label="销量从高到低" value="sales-desc" />
                <el-option label="好评率从高到低" value="rating-desc" />
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-checkbox v-model="filters.hasDiscount">仅看有优惠</el-checkbox>
            </el-col>
            <el-col :span="8">
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="resetFilters">重置</el-button>
            </el-col>
          </el-row>
        </div>

        <!-- 详细属性筛选 -->
        <div class="filter-row detailed-filters" v-if="showDetailedFilters">
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="filter-group">
                <label>甜度：</label>
                <el-checkbox-group v-model="filters.sweetness">
                  <el-checkbox label="全糖">全糖</el-checkbox>
                  <el-checkbox label="七分糖">七分糖</el-checkbox>
                  <el-checkbox label="五分糖">五分糖</el-checkbox>
                  <el-checkbox label="三分糖">三分糖</el-checkbox>
                  <el-checkbox label="无糖">无糖</el-checkbox>
                </el-checkbox-group>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="filter-group">
                <label>温度：</label>
                <el-checkbox-group v-model="filters.temperature">
                  <el-checkbox label="热饮">热饮</el-checkbox>
                  <el-checkbox label="常温">常温</el-checkbox>
                  <el-checkbox label="冷饮">冷饮</el-checkbox>
                </el-checkbox-group>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="filter-group">
                <label>规格：</label>
                <el-checkbox-group v-model="filters.size">
                  <el-checkbox label="中杯">中杯</el-checkbox>
                  <el-checkbox label="大杯">大杯</el-checkbox>
                  <el-checkbox label="超大杯">超大杯</el-checkbox>
                </el-checkbox-group>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="filter-group">
                <label>商家筛选：</label>
                <el-select v-model="filters.merchantSort" placeholder="商家排序" clearable>
                  <el-option label="评分从高到低" value="rating-desc" />
                  <el-option label="配送速度最快" value="delivery-fast" />
                  <el-option label="起送价从低到高" value="min-order-asc" />
                </el-select>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 展开/收起详细筛选 -->
        <div class="filter-toggle">
          <el-button 
            type="text" 
            @click="toggleDetailedFilters"
            :icon="showDetailedFilters ? 'ArrowUp' : 'ArrowDown'"
          >
            {{ showDetailedFilters ? '收起详细筛选' : '展开详细筛选' }}
          </el-button>
        </div>
      </div>

      <!-- 商品列表 -->
      <div class="products-content" v-loading="loading">
        <el-row :gutter="20">
          <el-col :span="6" v-for="product in products" :key="product.id">
            <el-card class="product-card" shadow="hover">
              <div class="product-image" @click="goToProductDetail(product)">
                <SmartImage 
                  :src="product.image" 
                  :alt="product.name"
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
                <h3 @click="goToProductDetail(product)">{{ product.name }}</h3>
                <p class="product-desc">{{ product.description }}</p>
                <div class="product-tags" v-if="product.tags && product.tags.length > 0">
                  <el-tag v-for="tag in product.tags" :key="tag" size="small">
                    {{ tag }}
                  </el-tag>
                </div>
                <div class="product-price">
                  <span class="current-price">¥{{ getProductDisplayPrice(product).toFixed(2) }}</span>
                  <span class="original-price" v-if="getProductOriginalPrice(product) > getProductDisplayPrice(product)">
                    ¥{{ getProductOriginalPrice(product).toFixed(2) }}
                  </span>
                </div>
                <div class="product-actions">
                  <el-button type="primary" @click="addToCart(product)">
                    <el-icon><ShoppingCart /></el-icon>
                    加入购物车
                  </el-button>
                  <el-button @click="toggleFavorite(product)">
                    <el-icon>
                      <component :is="product.isFavorite ? 'StarFilled' : 'Star'" />
                    </el-icon>
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 空状态 -->
        <el-empty v-if="!loading && products.length === 0" description="暂无商品" />
      </div>

      <!-- 分页 -->
      <div class="pagination-container" v-if="totalProducts > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[12, 24, 48]"
          :total="totalProducts"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { getImagesByRange } from '@/utils/imageLoader'
import { calculatePromotionPrice } from '@/utils/priceCalculator'
import { Search, ShoppingCart, Star, StarFilled } from '@element-plus/icons-vue'
// 导入 API
import { getProductList } from '@/api/product'
import { getCategoryTree } from '@/api/category'
import { getPromotionPage, checkUserParticipatedPromotion } from '@/api/promotion'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()
const userStore = useUserStore()

const searchQuery = ref('')
const showDetailedFilters = ref(false)
const loading = ref(false)

const filters = reactive({
  category: '',
  categoryId: null, // 分类ID
  priceRange: '',
  sortBy: 'default',
  hasDiscount: false,
  sweetness: [],
  temperature: [],
  size: [],
  merchantSort: '',
  merchantId: null // 商家ID
})

const currentPage = ref(1)
const pageSize = ref(12)
const totalProducts = ref(0)
const products = ref([])
const categories = ref([]) // 分类列表
const productPromotions = ref({}) // 商品促销活动映射 { productId: promotion }
// 促销活动用户参与状态映射（促销活动ID -> 是否已参与）
const promotionParticipatedStatus = ref({})

// 使用 assets 目录下的图片（从第14张开始）
const productImages = getImagesByRange(14, 20)

// 数据转换：将后端商品数据转换为前端格式
const convertProduct = (product) => {
  // 处理图片：优先使用后端返回的图片，如果没有则使用本地图片
  let image = null
  if (product.images && product.images.length > 0) {
    // 如果图片是相对路径，需要转换为完整URL
    image = product.images[0].startsWith('http') 
      ? product.images[0] 
      : product.images[0].startsWith('/') 
        ? product.images[0] 
        : `/api/files/${product.images[0]}`
  } else {
    // 使用本地图片作为默认值
    const imageIndex = product.id ? (product.id % productImages.length) : 0
    image = productImages[imageIndex] || productImages[0] || '/product-default.jpg'
  }
  
  // 如果图片还是无效，使用默认占位图
  if (!image || image === 'undefined' || image === 'null') {
    const imageIndex = product.id ? (product.id % productImages.length) : 0
    image = productImages[imageIndex] || productImages[0] || '/product-default.jpg'
  }
  
  return {
    id: product.id,
    name: product.name,
    description: product.description || '',
    price: product.price ? parseFloat(product.price) : 0,
    originalPrice: product.originalPrice ? parseFloat(product.originalPrice) : null,
    image: image,
    images: product.images || [image], // 保存所有图片
    category: product.categoryName || '',
    categoryId: product.categoryId,
    tags: product.promotionTypes || product.tags || [],
    isHot: product.sales > 100,
    isNew: false, // 可以根据创建时间判断
    isFavorite: false,
    sales: product.sales || 0,
    rating: 4.5, // 默认评分，实际应从评价系统获取
    hasDiscount: !!product.originalPrice || product.isPromotion,
    sweetness: product.sweetnessOptions || ['全糖', '七分糖', '五分糖', '三分糖', '无糖'],
    temperature: product.temperatureOptions || ['热饮', '常温', '冷饮'],
    size: product.sizes ? product.sizes.map(s => s.name) : ['中杯', '大杯', '超大杯'],
    merchantId: product.merchantId,
    stock: product.stock || 999 // 默认库存
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

// 加载分类列表
const loadCategories = async () => {
  try {
    const categoryList = await getCategoryTree()
    if (categoryList && categoryList.length > 0) {
      // 过滤启用状态的分类，并展开树形结构
      const flattenCategories = (cats, result = []) => {
        cats.forEach(cat => {
          if (cat.status === 1) {
            result.push(cat)
            if (cat.children && cat.children.length > 0) {
              flattenCategories(cat.children, result)
            }
          }
        })
        return result
      }
      categories.value = flattenCategories(categoryList)
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 根据分类名称查找分类ID
const findCategoryIdByName = (categoryName) => {
  const category = categories.value.find(cat => cat.name === categoryName)
  return category ? category.id : null
}

// 加载商品列表
const loadProducts = async () => {
  try {
    loading.value = true
    
    // 构建查询参数
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      status: 1 // 只获取上架商品
    }
    
    // 分类筛选
    if (filters.category) {
      const categoryId = findCategoryIdByName(filters.category)
      if (categoryId) {
        params.categoryId = categoryId
      }
    } else if (filters.categoryId) {
      params.categoryId = filters.categoryId
    }
    
    // 搜索关键词
    if (searchQuery.value.trim()) {
      params.name = searchQuery.value.trim()
    }
    
    // 商家筛选
    if (filters.merchantId) {
      params.merchantId = filters.merchantId
    }
    
    // 调用 API
    const result = await getProductList(params)
    
    // 处理返回数据
    if (result) {
      const productList = result.records || result.list || (Array.isArray(result) ? result : [])
      
      // 转换为前端格式
      let convertedProducts = productList.map(convertProduct)
      
      // 前端价格筛选（后端不支持，所以前端筛选）
      if (filters.priceRange) {
        const [min, max] = filters.priceRange.split('-')
        if (max && max !== '+') {
          convertedProducts = convertedProducts.filter(p => 
            p.price >= parseInt(min) && p.price <= parseInt(max)
          )
        } else if (max === '+') {
          convertedProducts = convertedProducts.filter(p => p.price >= parseInt(min))
        }
      }
      
      // 优惠筛选
      if (filters.hasDiscount) {
        convertedProducts = convertedProducts.filter(p => p.hasDiscount)
      }
      
      // 前端排序（部分排序可能需要在后端实现，这里先做前端排序）
      switch (filters.sortBy) {
        case 'price-asc':
          convertedProducts.sort((a, b) => a.price - b.price)
          break
        case 'price-desc':
          convertedProducts.sort((a, b) => b.price - a.price)
          break
        case 'sales-desc':
          convertedProducts.sort((a, b) => b.sales - a.sales)
          break
        case 'rating-desc':
          convertedProducts.sort((a, b) => b.rating - a.rating)
          break
      }
      
      products.value = convertedProducts
      totalProducts.value = result.total || convertedProducts.length
      
      // 加载商品促销活动
      await loadProductPromotions(convertedProducts)
    } else {
      products.value = []
      totalProducts.value = 0
    }
  } catch (error) {
    console.error('加载商品失败:', error)
    ElMessage.error('加载商品失败，请稍后重试')
    products.value = []
    totalProducts.value = 0
  } finally {
    loading.value = false
  }
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

// 监听筛选条件变化，重新加载数据（排除 currentPage 和 pageSize，它们有单独的处理）
watch([() => filters.category, () => filters.categoryId, () => filters.priceRange, () => filters.sortBy, () => filters.hasDiscount, () => filters.merchantId, searchQuery], () => {
  // 筛选条件变化时，重置到第一页并重新加载
  if (currentPage.value !== 1) {
    currentPage.value = 1
  } else {
    loadProducts()
  }
}, { deep: true })

// 处理路由参数
const handleRouteParams = async () => {
  // 处理路由参数
  if (route.query.search) {
    searchQuery.value = route.query.search
  } else if (!route.query.search && route.query.search === undefined) {
    // 如果路由中没有 search 参数，保持当前搜索值（不清空）
  }
  
  if (route.query.category) {
    filters.category = route.query.category
    const categoryId = findCategoryIdByName(route.query.category)
    if (categoryId) {
      filters.categoryId = categoryId
    }
  } else if (route.query.category === '') {
    // 如果明确传入空字符串，清空分类
    filters.category = ''
    filters.categoryId = null
  }
  
  if (route.query.merchant) {
    filters.merchantId = parseInt(route.query.merchant)
  } else if (route.query.merchant === '') {
    filters.merchantId = null
  }
  
  // 加载商品
  await loadProducts()
}

onMounted(async () => {
  // 先加载分类
  await loadCategories()
  
  // 处理路由参数并加载商品
  await handleRouteParams()
})

// 监听路由变化
watch(() => route.query, async () => {
  await handleRouteParams()
}, { deep: true })

// 分类变化处理
const handleCategoryChange = (value) => {
  if (value) {
    const categoryId = findCategoryIdByName(value)
    filters.categoryId = categoryId
  } else {
    filters.categoryId = null
  }
}

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    loadProducts()
  } else {
    ElMessage.warning('请输入搜索关键词')
  }
}

const resetFilters = () => {
  searchQuery.value = ''
  Object.assign(filters, {
    category: '',
    categoryId: null,
    priceRange: '',
    sortBy: 'default',
    hasDiscount: false,
    sweetness: [],
    temperature: [],
    size: [],
    merchantSort: '',
    merchantId: null
  })
  showDetailedFilters.value = false
  currentPage.value = 1
  loadProducts()
}

const toggleDetailedFilters = () => {
  showDetailedFilters.value = !showDetailedFilters.value
}

const goToProductDetail = (product) => {
  router.push(`/products/${product.id}`)
}

const addToCart = (product) => {
  cartStore.addToCart(product)
  ElMessage.success('已添加到购物车')
}

const toggleFavorite = (product) => {
  product.isFavorite = !product.isFavorite
  ElMessage.success(product.isFavorite ? '已添加到收藏' : '已取消收藏')
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadProducts()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
  loadProducts()
}

// 图片加载失败处理
const handleImageError = (event, product) => {
  console.warn('商品图片加载失败:', product.image, '使用默认图片')
  // 使用默认图片
  const defaultImage = productImages[product.id % productImages.length] || productImages[0]
  if (defaultImage) {
    event.target.src = defaultImage
  } else {
    // 如果还是没有，使用占位图
    event.target.src = '/product-default.jpg'
    event.target.onerror = null // 防止无限循环
  }
}
</script>

<style scoped>
.products-container {
  padding: 20px 0;
  min-height: calc(100vh - 120px);
}

/* 搜索栏样式 */
.search-bar {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-bar .el-input {
  max-width: 600px;
  margin: 0 auto;
}

/* 筛选栏样式 */
.filter-bar {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.filter-row {
  margin-bottom: 16px;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.detailed-filters {
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
  margin-top: 16px;
}

.filter-group {
  margin-bottom: 12px;
}

.filter-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.filter-group .el-checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-toggle {
  text-align: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.products-content {
  margin-bottom: 40px;
}

.product-card {
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.product-card:hover {
  transform: translateY(-5px);
}

.product-image {
  position: relative;
  height: 200px;
  overflow: hidden;
  cursor: pointer;
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
  z-index: 2;
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

.product-info {
  padding: 16px;
}

.product-info h3 {
  margin-bottom: 8px;
  color: #333;
  cursor: pointer;
  transition: color 0.3s;
}

.product-info h3:hover {
  color: var(--color-primary);
}

.product-desc {
  color: #666;
  font-size: 14px;
  margin-bottom: 12px;
  line-height: 1.4;
}

.product-tags {
  margin-bottom: 12px;
}

.product-tags .el-tag {
  margin-right: 8px;
  margin-bottom: 4px;
}

.product-price {
  margin-bottom: 16px;
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

.product-actions {
  display: flex;
  gap: var(--spacing-sm);
}

.product-actions .el-button {
  flex: 1;
  border-radius: var(--radius-lg);
  font-weight: var(--font-weight-medium);
  transition: all var(--transition-fast);
}

/* 加入购物车按钮 - 绿色 */
.product-actions .el-button--primary {
  background: var(--color-secondary);
  border-color: var(--color-secondary);
}

.product-actions .el-button--primary:hover {
  background: var(--color-secondary-dark);
  border-color: var(--color-secondary-dark);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

@media (max-width: 768px) {
  .filter-bar .el-col {
    margin-bottom: 10px;
  }
  
  .products-content .el-col {
    margin-bottom: 20px;
  }
}
</style>
