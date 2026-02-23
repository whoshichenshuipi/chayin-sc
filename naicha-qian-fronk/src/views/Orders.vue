<template>
  <div class="orders-container">
    <div class="container">
      <div class="orders-header">
        <h2>我的订单</h2>
        <div class="order-stats">
          <div class="stat-item">
            <span class="stat-number">{{ orderStats.total }}</span>
            <span class="stat-label">全部订单</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ orderStats.pending }}</span>
            <span class="stat-label">待付款</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ orderStats.processing }}</span>
            <span class="stat-label">处理中</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ orderStats.completed }}</span>
            <span class="stat-label">已完成</span>
          </div>
        </div>
      </div>

      <!-- 订单筛选 -->
      <div class="order-filters">
        <div class="filter-tabs">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="全部" name="all" />
            <el-tab-pane label="待付款" name="pending" />
            <el-tab-pane label="待接单" name="waiting" />
            <el-tab-pane label="待发货" name="processing" />
            <el-tab-pane label="待收货" name="shipped" />
            <el-tab-pane label="已完成" name="completed" />
            <el-tab-pane label="已取消" name="cancelled" />
            <el-tab-pane label="售后中" name="after_sale" />
          </el-tabs>
        </div>
        
        <div class="filter-options">
          <div class="time-filter">
            <el-select v-model="timeFilter" placeholder="时间筛选" @change="handleTimeFilterChange">
              <el-option label="全部时间" value="all" />
              <el-option label="近7天" value="7days" />
              <el-option label="近30天" value="30days" />
              <el-option label="近3个月" value="3months" />
              <el-option label="自定义时间" value="custom" />
            </el-select>
          </div>
          
          <div v-if="timeFilter === 'custom'" class="custom-time">
            <el-date-picker
              v-model="customTimeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              @change="handleCustomTimeChange"
            />
          </div>
          
          <div class="search-filter">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索订单号或商品名称"
              :prefix-icon="Search"
              @input="handleSearch"
              clearable
            />
          </div>
          
          <div class="sort-filter">
            <el-select v-model="sortBy" placeholder="排序方式" @change="handleSortChange">
              <el-option label="下单时间" value="createTime" />
              <el-option label="订单金额" value="amount" />
              <el-option label="订单状态" value="status" />
            </el-select>
          </div>
        </div>
      </div>

      <!-- 订单列表 -->
      <div class="orders-list" v-loading="loading">
        <el-card
          v-for="order in filteredOrders"
          :key="order.id"
          class="order-card"
          shadow="hover"
        >
          <div class="order-header">
            <div class="order-info">
              <span class="order-number">订单号：{{ order.orderNumber }}</span>
              <span class="order-time">{{ order.createTime }}</span>
            </div>
            <div class="order-status">
              <el-tag :type="getStatusType(order.status)" class="status-tag">
                <el-icon v-if="order.status === 'completed'" class="status-icon success-icon">
                  <Check />
                </el-icon>
                <el-icon v-else-if="order.status === 'cancelled' || order.status === 'refunded'" class="status-icon error-icon">
                  <WarningFilled />
                </el-icon>
                {{ getStatusText(order.status) }}
              </el-tag>
            </div>
          </div>

          <div class="order-content">
            <div class="order-items">
              <div
                v-for="item in order.items"
                :key="item.id || item.productId"
                class="order-item"
              >
                <SmartImage :src="item.image" :alt="item.name" image-class="item-image" />
                <div class="item-info">
                  <h4>{{ item.name }}</h4>
                  <p v-if="item.description">{{ item.description }}</p>
                  <div v-if="item.tags && item.tags.length > 0" class="item-tags">
                    <el-tag v-for="tag in item.tags" :key="tag" size="small">
                      {{ tag }}
                    </el-tag>
                  </div>
                </div>
                <div class="item-quantity">x{{ item.quantity }}</div>
                <div class="item-price">¥{{ item.price.toFixed(2) }}</div>
              </div>
            </div>

            <div class="order-summary">
              <div class="summary-row">
                <span>商品总价：</span>
                <span>¥{{ order.totalAmount.toFixed(2) }}</span>
              </div>
              <div class="summary-row">
                <span>配送费：</span>
                <span>¥{{ order.deliveryFee.toFixed(2) }}</span>
              </div>
              <div class="summary-row" v-if="order.discount > 0">
                <span>优惠：</span>
                <span class="discount">-¥{{ order.discount.toFixed(2) }}</span>
              </div>
              <el-divider />
              <div class="summary-row total-row">
                <span>实付金额：</span>
                <span class="total-amount">¥{{ order.finalAmount.toFixed(2) }}</span>
              </div>
            </div>
          </div>

          <div class="order-actions">
            <el-button
              v-if="order.status === 'pending'"
              type="primary"
              @click="payOrder(order)"
            >
              立即付款
            </el-button>
            <el-button
              v-if="order.status === 'pending'"
              @click="cancelOrder(order)"
            >
              取消订单
            </el-button>
            <el-button
              v-if="order.status === 'completed'"
              @click="reorder(order)"
            >
              再次购买
            </el-button>
            <el-button
              v-if="order.status === 'completed'"
              @click="viewOrderDetail(order)"
            >
              查看详情
            </el-button>
            <el-button
              v-if="order.status === 'shipped' || order.status === 'processing'"
              @click="trackOrder(order)"
            >
              查看物流
            </el-button>
            
            <el-button
              v-if="order.status === 'shipped'"
              type="success"
              @click="confirmReceipt(order)"
            >
              确认收货
            </el-button>
            
            <el-button
              v-if="order.status === 'completed'"
              @click="applyAfterSale(order)"
            >
              申请售后
            </el-button>
          </div>
        </el-card>

        <!-- 空状态 -->
        <el-empty v-if="filteredOrders.length === 0" description="暂无订单" />
      </div>

      <!-- 分页 -->
      <div class="pagination-container" v-if="filteredOrders.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="totalOrders"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Check, WarningFilled } from '@element-plus/icons-vue'
import { getOrderPage, cancelOrder as cancelOrderApi, completeOrder as completeOrderApi, getOrderStatistics, payOrder as payOrderApi } from '@/api/order'
import { useCartStore } from '@/stores/cart'
import { getImageByIndex, getRandomImage } from '@/utils/imageLoader'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()

const activeTab = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const totalOrders = ref(0)
const loading = ref(false)

// 筛选条件
const timeFilter = ref('all')
const customTimeRange = ref([])
const searchKeyword = ref('')
const sortBy = ref('createTime')

const orderStats = ref({
  total: 0,
  pending: 0,
  processing: 0,
  completed: 0,
  shipped: 0,
  cancelled: 0
})

const orders = ref([])

// 订单状态映射（前端标签 -> 后端状态码）
const getStatusByTab = (tab) => {
  // 后端状态：0-待支付 1-已支付 2-已接单 3-制作中 4-已发货 5-已完成 6-已取消 7-已退款
  const tabMap = {
    'all': null, // 全部，不传status
    'pending': 0, // 待付款
    'waiting': 1, // 待接单（已支付但未接单）
    'processing': null, // 处理中（包含2、3、4，需要前端筛选）
    'shipped': 4, // 待收货（已发货）
    'completed': 5, // 已完成
    'cancelled': 6, // 已取消
    'after_sale': null // 售后中（可能需要单独查询）
  }
  return tabMap[tab] !== undefined ? tabMap[tab] : null
}

// 订单状态映射（后端数字 -> 前端字符串）
const mapOrderStatus = (status) => {
  const statusMap = {
    0: 'pending',
    1: 'waiting', // 已支付待接单
    2: 'processing',
    3: 'processing',
    4: 'shipped',
    5: 'completed',
    6: 'cancelled',
    7: 'refunded'
  }
  return statusMap[status] || 'pending'
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  try {
    const time = new Date(timeStr)
    if (isNaN(time.getTime())) return timeStr.toString()
    return time.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    return timeStr.toString()
  }
}

// 格式化订单数据
const formatOrder = (orderData) => {
  // 处理订单商品
  const items = (orderData.items || []).map(item => ({
    id: item.id,
    productId: item.productId,
    name: item.productName || '',
    description: item.productDescription || '',
    price: parseFloat(item.price || 0),
    quantity: item.quantity || 1,
    image: (() => {
      const img = item.productImage || ''
      if (!img || img === '/product-default.jpg' || img === 'undefined' || img === 'null') {
        return getImageByIndex(item.productId || item.id || 0) || getRandomImage() || '/product-default.jpg'
      }
      return img
    })(),
    tags: [] // 标签可能需要从商品信息中获取，暂时为空
  }))
  
  // 计算费用明细
  const totalAmount = parseFloat(orderData.totalAmount || 0)
  const discountAmount = parseFloat(orderData.discountAmount || 0)
  const payAmount = parseFloat(orderData.payAmount || 0)
  const deliveryFee = 0 // 配送费可能在后端单独字段，这里先设为0，或者从totalAmount计算
  
  return {
    id: orderData.id,
    orderNumber: orderData.orderNo || '',
    status: mapOrderStatus(orderData.status),
    statusCode: orderData.status, // 保存原始状态码
    createTime: formatTime(orderData.createdAt),
    totalAmount: totalAmount,
    deliveryFee: deliveryFee,
    discount: discountAmount,
    finalAmount: payAmount,
    items: items,
    payMethod: orderData.payMethod || '',
    payTime: formatTime(orderData.payTime),
    remark: orderData.remark || ''
  }
}

// 加载订单列表
const loadOrders = async () => {
  try {
    loading.value = true
    
    // 构建查询参数
    const queryParams = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
  
  // 状态筛选
    const status = getStatusByTab(activeTab.value)
    if (status !== null) {
      queryParams.status = status
  }
  
  // 时间筛选
  if (timeFilter.value !== 'all') {
    const formatDateTime = (date) => {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      const seconds = String(date.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    }
    
    const now = new Date()
    let startDate = null
    let endDate = now
    
    switch (timeFilter.value) {
      case '7days':
        startDate = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
        break
      case '30days':
        startDate = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
        break
      case '3months':
        startDate = new Date(now.getTime() - 90 * 24 * 60 * 60 * 1000)
        break
      case 'custom':
        if (customTimeRange.value && customTimeRange.value.length === 2) {
          // 自定义时间范围，直接使用选择器返回的格式（YYYY-MM-DD HH:mm:ss）
          queryParams.startTime = customTimeRange.value[0]
          queryParams.endTime = customTimeRange.value[1]
        }
        break
    }
    
    if (startDate && timeFilter.value !== 'custom') {
      queryParams.startTime = formatDateTime(startDate)
      queryParams.endTime = formatDateTime(endDate)
    }
  }
  
    // 搜索关键词（订单号）
  if (searchKeyword.value.trim()) {
      queryParams.orderNo = searchKeyword.value.trim()
    }
    
    const response = await getOrderPage(queryParams)
    
    // 处理分页响应格式（后端返回的是 IPage 对象，包含 records 和 total）
    let records = []
    let total = 0
    
    if (response) {
      records = response.records || response.list || (Array.isArray(response) ? response : [])
      total = response.total || records.length
    }
    
    // 格式化订单数据
    let formattedOrders = records.map(formatOrder)
    
    // 前端筛选（如果后端不支持某些筛选）
    // 处理"处理中"标签页（包含状态2、3、4）
    if (activeTab.value === 'processing') {
      formattedOrders = formattedOrders.filter(order => 
        order.statusCode >= 2 && order.statusCode <= 4
      )
    }
    
    // 前端搜索（如果后端不支持商品名称搜索）
    if (searchKeyword.value.trim() && !queryParams.orderNo) {
    const keyword = searchKeyword.value.toLowerCase()
      formattedOrders = formattedOrders.filter(order => 
      order.orderNumber.toLowerCase().includes(keyword) ||
      order.items.some(item => 
        item.name.toLowerCase().includes(keyword) ||
        item.description.toLowerCase().includes(keyword)
      )
    )
  }
  
    // 前端排序
    formattedOrders.sort((a, b) => {
    switch (sortBy.value) {
      case 'createTime':
        return new Date(b.createTime) - new Date(a.createTime)
      case 'amount':
        return b.finalAmount - a.finalAmount
      case 'status':
          return a.statusCode - b.statusCode
      default:
        return 0
    }
  })
  
    orders.value = formattedOrders
    totalOrders.value = total
    
    // 计算订单统计（如果后端没有统计接口）
    calculateOrderStats()
  } catch (error) {
    console.error('加载订单列表失败:', error)
    ElMessage.error('加载订单列表失败')
    orders.value = []
    totalOrders.value = 0
  } finally {
    loading.value = false
  }
}

  // 计算订单统计
const calculateOrderStats = () => {
  orderStats.value = {
    total: orders.value.length,
    pending: orders.value.filter(o => o.statusCode === 0).length,
    processing: orders.value.filter(o => o.statusCode >= 2 && o.statusCode <= 4).length,
    completed: orders.value.filter(o => o.statusCode === 5).length,
    shipped: orders.value.filter(o => o.statusCode === 4).length,
    cancelled: orders.value.filter(o => o.statusCode === 6).length
  }
}

// 加载订单统计（从后端获取）
const loadOrderStats = async () => {
  try {
    const stats = await getOrderStatistics()
    if (stats) {
      orderStats.value = {
        total: stats.total || stats.totalCount || 0,
        pending: stats.pendingCount || stats.pending || 0,
        processing: stats.processingCount || stats.processing || 0,
        completed: stats.completedCount || stats.completed || 0,
        shipped: stats.shippedCount || stats.shipped || 0,
        cancelled: stats.cancelledCount || stats.cancelled || 0
      }
    } else {
      // 如果后端没有返回数据，使用前端计算
      calculateOrderStats()
    }
  } catch (error) {
    console.warn('加载订单统计失败，使用前端计算:', error)
    // 如果接口不存在或出错，使用前端计算
    calculateOrderStats()
  }
}

const filteredOrders = computed(() => {
  // 由于后端已经做了分页和筛选，这里直接返回orders
  // 但如果需要前端二次筛选（如商品名称搜索），可以在这里处理
  return orders.value
})

onMounted(async () => {
  // 如果有URL参数（如从其他页面跳转过来），使用参数筛选
  const queryOrderId = route.query.orderId
  const queryStatus = route.query.status
  const queryTab = route.query.tab
  
  if (queryTab) {
    activeTab.value = queryTab
  }
  
  if (queryStatus) {
    activeTab.value = queryStatus
  }
  
  // 加载订单列表
  await loadOrders()
  
  // 尝试加载订单统计
  await loadOrderStats()
})

// 注意：标签切换、时间筛选、搜索、排序都通过各自的 handle 函数处理，避免使用 watch 导致重复请求

const handleTabChange = (tab) => {
  activeTab.value = tab
  currentPage.value = 1
  loadOrders()
}

const getStatusText = (status) => {
  const statusMap = {
    pending: '待付款',
    waiting: '待接单',
    processing: '处理中',
    shipped: '待收货',
    completed: '已完成',
    cancelled: '已取消',
    refunded: '已退款'
  }
  return statusMap[status] || '未知状态'
}

const getStatusType = (status) => {
  // 订单状态颜色映射：成功状态用绿色，警告/错误状态用红色
  const statusMap = {
    pending: 'danger', // 待付款 - 红色
    waiting: 'warning', // 待接单 - 橙色
    processing: 'primary', // 处理中 - 蓝色
    shipped: 'success', // 待收货 - 绿色
    completed: 'success', // 已完成 - 绿色（使用绿色对勾）
    cancelled: 'info', // 已取消 - 灰色
    refunded: 'danger' // 已退款 - 红色
  }
  return statusMap[status] || 'info'
}

// 确认收货
const confirmReceipt = async (order) => {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？', '确认收货', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    try {
      await completeOrderApi(order.id)
      ElMessage.success('确认收货成功')
      
      // 重新加载订单列表
      await loadOrders()
      await loadOrderStats()
    } catch (error) {
      console.error('确认收货失败:', error)
      ElMessage.error('确认收货失败，请重试')
    }
  } catch (error) {
    // 用户取消操作
    if (error !== 'cancel') {
      console.error('确认收货操作失败:', error)
    }
  }
}

// 申请售后
const applyAfterSale = (order) => {
  router.push({
    path: '/after-sale',
    query: {
      orderId: order.id || order.orderNumber
    }
  })
}

const payOrder = async (order) => {
  try {
    await ElMessageBox.confirm(
      `确定要支付这个订单吗？\n订单号：${order.orderNumber}\n实付金额：¥${order.finalAmount.toFixed(2)}`,
      '确认支付',
      {
        confirmButtonText: '确定支付',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 显示支付中提示
    const loadingMessage = ElMessage({
      message: '支付处理中，请稍候...',
      type: 'info',
      duration: 0,
      showClose: false
    })
    
    try {
      // 调用支付接口（模拟支付，默认使用微信支付）
      const payMethod = order.payMethod || 'wechat'
      const result = await payOrderApi(order.id, payMethod)
      
      // 关闭加载提示
      loadingMessage.close()
      
      if (result) {
        ElMessage.success('支付成功！')
        
        // 重新加载订单列表和统计信息以同步后端数据
        await Promise.all([
          loadOrders(),
          loadOrderStats()
        ])
      } else {
        ElMessage.error('支付失败，请重试')
      }
    } catch (error) {
      // 关闭加载提示
      loadingMessage.close()
      
      console.error('支付失败:', error)
      ElMessage.error(error.response?.data?.message || error.message || '支付失败，请重试')
    }
  } catch (error) {
    // 用户取消支付
    if (error !== 'cancel') {
      console.error('支付操作失败:', error)
    }
  }
}

const cancelOrder = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？', '确认取消', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 显示取消原因输入
    const { value } = await ElMessageBox.prompt('请输入取消原因（选填）：', '取消订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '请输入取消原因',
      inputValidator: () => true // 允许为空
    })
    
    try {
      await cancelOrderApi(order.id)
    ElMessage.success('订单已取消')
    
      // 重新加载订单列表
      await loadOrders()
      await loadOrderStats()
    } catch (error) {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败，请重试')
    }
  } catch (error) {
    // 用户取消操作
    if (error !== 'cancel') {
      console.error('取消订单操作失败:', error)
    }
  }
}

const reorder = (order) => {
  // 将订单商品添加到购物车
  order.items.forEach(item => {
    try {
      cartStore.addToCart({
        id: item.productId,
        name: item.name,
        description: item.description,
        price: item.price,
        image: item.image,
        stock: 999 // 假设有库存
      }, item.quantity, {})
    } catch (error) {
      console.error('添加商品到购物车失败:', error)
      ElMessage.warning(`添加商品 ${item.name} 到购物车失败: ${error.message}`)
    }
  })
  ElMessage.success('商品已添加到购物车')
  router.push('/cart')
}

const viewOrderDetail = (order) => {
  router.push({
    path: '/order-detail',
    query: {
      orderId: order.id || order.orderNumber
    }
  })
}

const trackOrder = (order) => {
  // 跳转到订单详情页面的物流标签
  router.push({
    path: '/order-detail',
    query: {
      orderId: order.id || order.orderNumber,
      tab: 'logistics'
    }
  })
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadOrders()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadOrders()
  
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 筛选方法
const handleTimeFilterChange = (value) => {
  timeFilter.value = value
  if (value !== 'custom') {
    customTimeRange.value = []
  }
  currentPage.value = 1
  loadOrders()
}

const handleCustomTimeChange = (value) => {
  customTimeRange.value = value
  currentPage.value = 1
  loadOrders()
}

const handleSearch = (value) => {
  searchKeyword.value = value
  currentPage.value = 1
  // 使用防抖，避免频繁请求
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    loadOrders()
  }, 500)
}

let searchTimer = null

const handleSortChange = (value) => {
  sortBy.value = value
  currentPage.value = 1
  loadOrders()
}
</script>

<style scoped>
.orders-container {
  padding: 20px 0;
  min-height: calc(100vh - 120px);
}

.orders-header {
  margin-bottom: 30px;
}

.orders-header h2 {
  color: #333;
  margin-bottom: 20px;
}

.order-stats {
  display: flex;
  gap: 30px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 24px;
  font-weight: bold;
  color: #ff6b6b;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.order-filters {
  margin-bottom: 20px;
}

.filter-tabs {
  margin-bottom: 16px;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.time-filter,
.search-filter,
.sort-filter {
  min-width: 150px;
}

.custom-time {
  min-width: 300px;
}

.search-filter {
  flex: 1;
  min-width: 200px;
}

.orders-list {
  margin-bottom: 40px;
}

.order-card {
  margin-bottom: 20px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-number {
  font-weight: bold;
  color: #333;
}

.order-time {
  font-size: 14px;
  color: #666;
}

.order-content {
  margin-bottom: 16px;
}

.order-items {
  margin-bottom: 16px;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
  gap: 16px;
}

.order-item:last-child {
  border-bottom: none;
}

.item-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-info h4 {
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

.item-tags {
  margin-bottom: 8px;
}

.item-tags .el-tag {
  margin-right: 4px;
  margin-bottom: 4px;
}

.item-quantity {
  width: 60px;
  text-align: center;
  color: #666;
}

.item-price {
  width: 80px;
  text-align: right;
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
}

.order-summary {
  background: var(--color-bg-white);
  padding: var(--spacing-md);
  border-radius: var(--radius-lg);
}

/* 订单状态标签样式 */
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-weight: var(--font-weight-medium);
}

.status-icon {
  font-size: var(--font-size-sm);
}

.success-icon {
  color: var(--color-secondary);
}

.error-icon {
  color: var(--color-primary);
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  color: #666;
}

.summary-row:last-child {
  margin-bottom: 0;
}

.discount {
  color: var(--color-secondary);
}

.total-row {
  font-weight: bold;
  color: #333;
}

.total-amount {
  color: var(--color-primary);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
}

.order-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

@media (max-width: 768px) {
  .order-stats {
    flex-wrap: wrap;
    gap: 20px;
  }
  
  .filter-options {
    flex-direction: column;
    align-items: stretch;
  }
  
  .time-filter,
  .search-filter,
  .sort-filter {
    min-width: auto;
    width: 100%;
  }
  
  .custom-time {
    min-width: auto;
    width: 100%;
  }
  
  .order-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .item-image {
    width: 50px;
    height: 50px;
  }
  
  .item-quantity,
  .item-price {
    width: auto;
  }
  
  .order-actions {
    flex-direction: column;
  }
}
</style>
