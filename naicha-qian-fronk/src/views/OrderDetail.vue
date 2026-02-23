<template>
  <div class="order-detail-container">
    <div class="container">
      <div class="detail-header">
        <el-button @click="$router.back()" :icon="ArrowLeft">
          返回
        </el-button>
        <h2>订单详情</h2>
      </div>

      <div v-loading="loading" class="order-content">

      <el-row v-if="!loading" :gutter="20">
        <!-- 左侧订单信息 -->
        <el-col :span="16">
          <!-- 订单基本信息 -->
          <el-card class="order-info-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>订单信息</span>
                <el-tag :type="getStatusType(orderInfo.status)">
                  {{ getStatusText(orderInfo.status) }}
                </el-tag>
              </div>
            </template>
            
            <div class="order-basic-info">
              <div class="info-row">
                <span class="label">订单号：</span>
                <span class="value">{{ orderInfo.orderNumber }}</span>
                <el-button type="primary" size="small" @click="copyOrderNumber">
                  复制
                </el-button>
              </div>
              <div class="info-row">
                <span class="label">下单时间：</span>
                <span class="value">{{ orderInfo.createTime }}</span>
              </div>
              <div class="info-row">
                <span class="label">支付方式：</span>
                <span class="value">{{ orderInfo.paymentMethod }}</span>
              </div>
              <div class="info-row">
                <span class="label">支付时间：</span>
                <span class="value">{{ orderInfo.payTime || '未支付' }}</span>
              </div>
            </div>
          </el-card>

          <!-- 商品清单 -->
          <el-card class="products-card" shadow="hover">
            <template #header>
              <span>商品清单</span>
            </template>
            
            <div class="product-list">
              <div
                v-for="item in orderInfo.items"
                :key="item.id"
                class="product-item"
              >
                <SmartImage :src="item.image" :alt="item.name" image-class="product-image" />
                <div class="product-info">
                  <h4>{{ item.name }}</h4>
                  <p>{{ item.description }}</p>
                  <div v-if="item.selectedSpecs" class="product-specs">
                    <el-tag
                      v-for="(value, key) in item.selectedSpecs"
                      :key="key"
                      size="small"
                    >
                      {{ key }}: {{ value }}
                    </el-tag>
                  </div>
                </div>
                <div class="product-quantity">x{{ item.quantity }}</div>
                <div class="product-price">¥{{ item.price }}</div>
              </div>
            </div>
          </el-card>

          <!-- 收货地址 -->
          <el-card class="address-card" shadow="hover">
            <template #header>
              <span>收货地址</span>
            </template>
            
            <div class="address-info">
              <div class="address-main">
                <span class="receiver">{{ orderInfo.address.receiver }}</span>
                <span class="phone">{{ orderInfo.address.phone }}</span>
              </div>
              <div class="address-detail">
                {{ orderInfo.address.province }} {{ orderInfo.address.city }} {{ orderInfo.address.district }} {{ orderInfo.address.detail }}
              </div>
            </div>
          </el-card>

          <!-- 配送信息 -->
          <el-card v-if="orderInfo.status === 'processing' || orderInfo.status === 'shipped' || orderInfo.deliveryInfo" class="delivery-card" shadow="hover">
            <template #header>
              <span>配送信息</span>
            </template>
            
            <div class="delivery-info">
              <div v-if="orderInfo.status === 'processing'" class="processing-status">
                <el-icon><Clock /></el-icon>
                <span>商家处理中，请耐心等待</span>
              </div>
              
              <div v-else-if="orderInfo.deliveryInfo" class="shipped-status">
                <div v-if="orderInfo.deliveryInfo.deliveryPerson" class="delivery-person">
                  <span class="label">配送员：</span>
                  <span class="value">{{ orderInfo.deliveryInfo.deliveryPerson }}</span>
                </div>
                <div v-if="orderInfo.deliveryInfo.deliveryPhone" class="delivery-phone">
                  <span class="label">联系电话：</span>
                  <span class="value">{{ orderInfo.deliveryInfo.deliveryPhone }}</span>
                </div>
                <div v-if="orderInfo.deliveryInfo.deliveryCompany" class="delivery-company">
                  <span class="label">配送公司：</span>
                  <span class="value">{{ orderInfo.deliveryInfo.deliveryCompany }}</span>
                </div>
                <div v-if="orderInfo.deliveryInfo.thirdPartyOrderNo" class="delivery-tracking">
                  <span class="label">物流单号：</span>
                  <span class="value">{{ orderInfo.deliveryInfo.thirdPartyOrderNo }}</span>
                </div>
                <div v-if="orderInfo.deliveryInfo.currentLocation" class="delivery-location">
                  <span class="label">当前位置：</span>
                  <span class="value">{{ orderInfo.deliveryInfo.currentLocation }}</span>
                </div>
                <div v-if="orderInfo.deliveryInfo.estimatedTime" class="estimated-time">
                  <span class="label">预计送达：</span>
                  <span class="value">{{ orderInfo.deliveryInfo.estimatedTime }}</span>
                </div>
              </div>
              
              <div v-else-if="orderInfo.status === 'shipped'" class="shipped-status">
                <span>配送信息加载中...</span>
              </div>
            </div>
          </el-card>

          <!-- 订单状态变更记录 -->
          <el-card class="status-timeline-card" shadow="hover">
            <template #header>
              <span>订单状态变更记录</span>
            </template>
            
            <el-timeline>
              <el-timeline-item
                v-for="record in orderInfo.statusRecords"
                :key="record.id"
                :timestamp="record.timestamp"
                :type="getTimelineType(record.status)"
              >
                <div class="timeline-content">
                  <h4>{{ record.title }}</h4>
                  <p v-if="record.description">{{ record.description }}</p>
                </div>
              </el-timeline-item>
            </el-timeline>
          </el-card>
        </el-col>

        <!-- 右侧费用明细和操作 -->
        <el-col :span="8">
          <!-- 费用明细 -->
          <el-card class="cost-card" shadow="hover">
            <template #header>
              <span>费用明细</span>
            </template>
            
            <div class="cost-details">
              <div class="detail-row">
                <span>商品总价：</span>
                <span>¥{{ orderInfo.cost.productTotal.toFixed(2) }}</span>
              </div>
              <div class="detail-row">
                <span>配送费：</span>
                <span v-if="orderInfo.cost.deliveryFee === 0">免费</span>
                <span v-else>¥{{ orderInfo.cost.deliveryFee.toFixed(2) }}</span>
              </div>
              <div class="detail-row" v-if="orderInfo.cost.couponDiscount > 0">
                <span>优惠券抵扣：</span>
                <span class="discount">-¥{{ orderInfo.cost.couponDiscount.toFixed(2) }}</span>
              </div>
              <div class="detail-row" v-if="orderInfo.cost.pointsDiscount > 0">
                <span>积分抵扣：</span>
                <span class="discount">-¥{{ orderInfo.cost.pointsDiscount.toFixed(2) }}</span>
              </div>
              <el-divider />
              <div class="detail-row total-row">
                <span>实付金额：</span>
                <span class="total-amount">¥{{ orderInfo.cost.finalAmount.toFixed(2) }}</span>
              </div>
            </div>
          </el-card>

          <!-- 订单操作 -->
          <el-card class="actions-card" shadow="hover">
            <template #header>
              <span>订单操作</span>
            </template>
            
            <div class="order-actions">
              <el-button
                v-if="orderInfo.status === 'pending'"
                type="primary"
                @click="payOrder"
                class="action-button"
              >
                立即付款
              </el-button>
              
              <el-button
                v-if="orderInfo.status === 'pending'"
                @click="cancelOrder"
                class="action-button"
              >
                取消订单
              </el-button>
              
              <el-button
                v-if="orderInfo.status === 'shipped'"
                type="success"
                @click="confirmReceipt"
                class="action-button"
              >
                确认收货
              </el-button>
              
              <el-button
                v-if="orderInfo.status === 'completed'"
                @click="applyAfterSale"
                class="action-button"
              >
                申请售后
              </el-button>
              
              <el-button
                v-if="orderInfo.status === 'completed'"
                @click="reorder"
                class="action-button"
              >
                再次购买
              </el-button>
              
              <el-button
                v-if="orderInfo.status === 'processing' || orderInfo.status === 'shipped'"
                @click="trackOrder"
                class="action-button"
              >
                查看物流
              </el-button>
            </div>
          </el-card>

          <!-- 订单备注 -->
          <el-card v-if="orderInfo.notes" class="notes-card" shadow="hover">
            <template #header>
              <span>订单备注</span>
            </template>
            
            <div class="order-notes">
              {{ orderInfo.notes }}
            </div>
          </el-card>
        </el-col>
      </el-row>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Clock } from '@element-plus/icons-vue'
import { getOrderDetail, getOrderByOrderNo, cancelOrder as cancelOrderApi, completeOrder as completeOrderApi, getOrderShipment } from '@/api/order'
import { useCartStore } from '@/stores/cart'
import { getImageByIndex, getRandomImage } from '@/utils/imageLoader'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()

// 加载状态
const loading = ref(false)

// 订单信息
const orderInfo = ref({
  id: null,
  orderNumber: '',
  status: 'pending',
  createTime: '',
  payTime: '',
  paymentMethod: '',
  address: {
    receiver: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: ''
  },
  items: [],
  deliveryInfo: null,
  statusRecords: [],
  cost: {
    productTotal: 0,
    deliveryFee: 0,
    couponDiscount: 0,
    pointsDiscount: 0,
    finalAmount: 0
  },
  notes: ''
})

// 订单状态映射（后端数字 -> 前端字符串）
const mapOrderStatus = (status) => {
  // 后端状态：0-待支付 1-已支付 2-已接单 3-制作中 4-已发货 5-已完成 6-已取消 7-已退款
  const statusMap = {
    0: 'pending',
    1: 'waiting', // 已支付待接单
    2: 'processing', // 已接单
    3: 'processing', // 制作中
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
const formatOrderData = (orderData) => {
  // 处理地址（deliveryAddress可能是完整字符串，需要拆分）
  let address = {
    receiver: orderData.userName || '',
    phone: orderData.userPhone || '',
    province: '',
    city: '',
    district: '',
    detail: orderData.deliveryAddress || ''
  }
  
  // 如果deliveryAddress是完整地址，尝试解析
  if (orderData.deliveryAddress) {
    const addressParts = orderData.deliveryAddress.split(/省|市|区|县/)
    if (addressParts.length > 0) {
      address.province = addressParts[0] + (orderData.deliveryAddress.includes('省') ? '省' : '')
      if (addressParts.length > 1) {
        address.city = addressParts[1] + (orderData.deliveryAddress.includes('市') ? '市' : '')
        if (addressParts.length > 2) {
          address.district = addressParts[2] + (orderData.deliveryAddress.includes('区') ? '区' : (orderData.deliveryAddress.includes('县') ? '县' : ''))
          if (addressParts.length > 3) {
            address.detail = addressParts.slice(3).join('')
          }
        }
      }
    }
  }
  
  // 处理订单商品
  const items = (orderData.items || []).map(item => ({
    id: item.id,
    productId: item.productId,
    name: item.productName || '',
    description: item.productDescription || '',
    price: item.price || 0,
    quantity: item.quantity || 1,
    image: (() => {
      const img = item.productImage || ''
      if (!img || img === '/product-default.jpg' || img === 'undefined' || img === 'null') {
        return getImageByIndex(item.productId || item.id || 0) || getRandomImage() || '/product-default.jpg'
      }
      return img
    })(),
    selectedSpecs: item.selectedSpecs || {} // 规格信息可能在extraData中
  }))
  
  // 处理费用明细
  const cost = {
    productTotal: parseFloat(orderData.totalAmount || 0),
    deliveryFee: 0, // 配送费可能在后端单独字段，这里先设为0
    couponDiscount: parseFloat(orderData.discountAmount || 0),
    pointsDiscount: 0, // 积分抵扣，后端可能没有单独字段
    finalAmount: parseFloat(orderData.payAmount || 0)
  }
  
  // 生成订单状态记录
  const statusRecords = []
  if (orderData.createdAt) {
    statusRecords.push({
      id: 1,
      status: 'created',
      title: '订单提交成功',
      description: '您的订单已提交，等待支付',
      timestamp: formatTime(orderData.createdAt)
    })
  }
  if (orderData.payTime) {
    statusRecords.push({
      id: 2,
      status: 'paid',
      title: '支付成功',
      description: '订单支付完成',
      timestamp: formatTime(orderData.payTime)
    })
  }
  if (orderData.status >= 2 && orderData.status <= 3) {
    statusRecords.push({
      id: 3,
      status: 'accepted',
      title: '商家已接单',
      description: '商家已接单，正在制作中',
      timestamp: formatTime(orderData.updatedAt || orderData.createdAt)
    })
  }
  if (orderData.deliveryTime) {
    statusRecords.push({
      id: 4,
      status: 'shipped',
      title: '商品已发货',
      description: '商品已发货，配送员正在配送中',
      timestamp: formatTime(orderData.deliveryTime)
    })
  }
  if (orderData.status === 5) {
    statusRecords.push({
      id: 5,
      status: 'completed',
      title: '订单已完成',
      description: '订单已完成，感谢您的购买',
      timestamp: formatTime(orderData.updatedAt)
    })
  }
  
  // 处理支付方式文本
  const payMethodMap = {
    'wechat': '微信支付',
    'alipay': '支付宝',
    'wallet': '余额支付'
  }
  
  return {
    id: orderData.id,
    orderNumber: orderData.orderNo || '',
    status: mapOrderStatus(orderData.status),
    statusCode: orderData.status, // 保存原始状态码
    createTime: formatTime(orderData.createdAt),
    payTime: formatTime(orderData.payTime),
    paymentMethod: payMethodMap[orderData.payMethod] || orderData.payMethod || '未知',
    address: address,
    items: items,
    deliveryInfo: null, // 稍后从配送接口获取
    statusRecords: statusRecords,
    cost: cost,
    notes: orderData.remark || ''
  }
}

// 格式化配送信息
const formatDeliveryInfo = (shipmentData) => {
  if (!shipmentData) return null
  
  return {
    deliveryPerson: shipmentData.deliveryPerson || shipmentData.delivery_person || '',
    deliveryPhone: shipmentData.deliveryPhone || shipmentData.delivery_phone || '',
    deliveryCompany: shipmentData.deliveryCompany || shipmentData.delivery_company || '',
    thirdPartyOrderNo: shipmentData.thirdPartyOrderNo || shipmentData.third_party_order_no || '',
    shippingStatus: shipmentData.shippingStatus || shipmentData.shipping_status || '',
    currentLocation: shipmentData.currentLocation || '配送中',
    estimatedTime: shipmentData.estimatedTimeText || shipmentData.estimated_time_text || (shipmentData.estimatedTime ? formatTime(shipmentData.estimatedTime) : '')
  }
}

// 加载订单详情
const loadOrderDetail = async (orderId) => {
  try {
    loading.value = true
    
    // 根据参数类型判断是ID还是订单号
    const isNumeric = /^\d+$/.test(orderId)
    let orderData
    
    if (isNumeric) {
      orderData = await getOrderDetail(parseInt(orderId))
    } else {
      orderData = await getOrderByOrderNo(orderId)
    }
    
    if (!orderData) {
      ElMessage.error('订单不存在')
      router.back()
      return
    }
    
    // 格式化订单数据
    orderInfo.value = formatOrderData(orderData)
    
    // 如果订单已发货或已发货，加载配送信息
    if (orderData.status >= 4) {
      try {
        const shipmentData = await getOrderShipment(orderData.id)
        if (shipmentData) {
          orderInfo.value.deliveryInfo = formatDeliveryInfo(shipmentData)
        }
      } catch (error) {
        console.warn('加载配送信息失败:', error)
      }
    }
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 方法
const getStatusType = (status) => {
  const statusMap = {
    pending: 'warning',
    waiting: 'info',
    processing: 'primary',
    shipped: 'info',
    completed: 'success',
    cancelled: 'info',
    refunded: 'warning'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    pending: '待付款',
    waiting: '待接单',
    processing: '处理中',
    shipped: '已发货',
    completed: '已完成',
    cancelled: '已取消',
    refunded: '已退款'
  }
  return statusMap[status] || '未知状态'
}

const getTimelineType = (status) => {
  const typeMap = {
    created: 'primary',
    paid: 'success',
    accepted: 'primary',
    shipped: 'info',
    completed: 'success',
    cancelled: 'warning'
  }
  return typeMap[status] || 'primary'
}

const copyOrderNumber = () => {
  navigator.clipboard.writeText(orderInfo.value.orderNumber).then(() => {
    ElMessage.success('订单号已复制到剪贴板')
  })
}

const payOrder = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要支付这个订单吗？\n订单号：${orderInfo.value.orderNumber}\n实付金额：¥${orderInfo.value.cost.finalAmount.toFixed(2)}`,
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
      duration: 0, // 不自动关闭
      showClose: false
    })
    
    // 等待1秒后完成支付（模拟支付流程）
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 关闭加载提示
    loadingMessage.close()
    
    // 显示支付成功
    ElMessage.success('支付成功！')
    
    // 直接更新订单状态（不调用真实接口，模拟支付成功）
    if (orderInfo.value.statusCode === 0 || orderInfo.value.status === 'pending') {
      // 更新订单状态：从0（待支付）变为1（已支付）
      orderInfo.value.statusCode = 1
      orderInfo.value.status = mapOrderStatus(1)
      orderInfo.value.paymentMethod = '微信支付'
      
      // 更新支付时间
      const now = new Date().toISOString()
      orderInfo.value.payTime = formatTime(now)
      
      // 添加支付成功的状态记录（如果状态记录数组存在）
      if (orderInfo.value.statusRecords && Array.isArray(orderInfo.value.statusRecords)) {
        // 检查是否已经有支付记录
        const hasPaidRecord = orderInfo.value.statusRecords.some(record => record.status === 'paid')
        if (!hasPaidRecord) {
          orderInfo.value.statusRecords.push({
            id: orderInfo.value.statusRecords.length + 1,
            status: 'paid',
            title: '支付成功',
            description: '订单支付完成',
            timestamp: formatTime(now)
          })
        }
      }
    }
    
    // 重新加载订单详情以同步后端数据
    await loadOrderDetail(orderInfo.value.id)
  } catch {
    // 用户取消支付
  }
}

const cancelOrder = async () => {
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
      await cancelOrderApi(orderInfo.value.id)
      orderInfo.value.status = 'cancelled'
      ElMessage.success('订单已取消')
      // 重新加载订单详情
      await loadOrderDetail(orderInfo.value.id)
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

const confirmReceipt = async () => {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？', '确认收货', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    try {
      await completeOrderApi(orderInfo.value.id)
      orderInfo.value.status = 'completed'
      ElMessage.success('确认收货成功')
      // 重新加载订单详情
      await loadOrderDetail(orderInfo.value.id)
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

const applyAfterSale = () => {
  ElMessage.info('跳转到售后申请页面')
  // 跳转到售后申请页面
  router.push({
    path: '/after-sale',
    query: {
      orderId: orderInfo.value.orderNumber
    }
  })
}

const reorder = () => {
  // 将订单商品添加到购物车
  orderInfo.value.items.forEach(item => {
    try {
      cartStore.addToCart({
        id: item.productId,
        name: item.name,
        description: item.description,
        price: item.price,
        image: item.image,
        stock: 999 // 假设有库存
      }, item.quantity, item.selectedSpecs)
    } catch (error) {
      console.error('添加商品到购物车失败:', error)
    }
  })
  ElMessage.success('商品已添加到购物车')
  router.push('/cart')
}

const trackOrder = () => {
  // 跳转到物流跟踪页面，使用订单ID或订单号
  router.push({
    path: '/orders',
    query: {
      orderId: orderInfo.value.id || orderInfo.value.orderNumber,
      tab: 'logistics'
    }
  })
}

onMounted(() => {
  // 根据路由参数获取订单信息
  const orderId = route.query.orderId || route.params.orderId
  if (orderId) {
    loadOrderDetail(orderId)
  } else {
    ElMessage.error('订单ID不能为空')
    router.back()
  }
})
</script>

<style scoped>
.order-detail-container {
  padding: 20px 0;
  min-height: calc(100vh - 120px);
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.detail-header h2 {
  color: #333;
  margin: 0;
}

.order-content {
  min-height: 400px;
}

.order-info-card,
.products-card,
.address-card,
.delivery-card,
.status-timeline-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-basic-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.label {
  color: #666;
  min-width: 80px;
}

.value {
  color: #333;
  flex: 1;
}

.product-list {
  max-height: 400px;
  overflow-y: auto;
}

.product-item {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  gap: 16px;
}

.product-item:last-child {
  border-bottom: none;
}

.product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-info h4 {
  margin-bottom: 4px;
  color: #333;
  font-size: 16px;
}

.product-info p {
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
}

.product-specs {
  margin-bottom: 8px;
}

.product-specs .el-tag {
  margin-right: 4px;
  margin-bottom: 4px;
}

.product-quantity {
  width: 60px;
  text-align: center;
  color: #666;
}

.product-price {
  width: 80px;
  text-align: right;
  font-weight: bold;
  color: #ff6b6b;
}

.address-info {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.address-main {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.receiver {
  font-weight: bold;
  color: #333;
}

.phone {
  color: #666;
}

.address-detail {
  color: #666;
  line-height: 1.4;
}

.delivery-info {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.processing-status {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1890ff;
}

.shipped-status {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.delivery-person,
.delivery-phone,
.delivery-location,
.estimated-time {
  display: flex;
  gap: 8px;
}

.delivery-person .label,
.delivery-phone .label,
.delivery-location .label,
.estimated-time .label {
  color: #666;
  min-width: 80px;
}

.delivery-person .value,
.delivery-phone .value,
.delivery-location .value,
.estimated-time .value {
  color: #333;
}

.timeline-content h4 {
  margin-bottom: 4px;
  color: #333;
}

.timeline-content p {
  color: #666;
  font-size: 14px;
}

.cost-card,
.actions-card,
.notes-card {
  margin-bottom: 20px;
}

.cost-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  color: #666;
}

.discount {
  color: #67c23a;
}

.total-row {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.total-amount {
  color: #ff6b6b;
  font-size: 18px;
}

.order-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-button {
  width: 100%;
}

.order-notes {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  color: #666;
  line-height: 1.4;
}

@media (max-width: 768px) {
  .product-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .product-image {
    width: 50px;
    height: 50px;
  }
  
  .product-quantity,
  .product-price {
    width: auto;
  }
  
  .cost-card,
  .actions-card,
  .notes-card {
    position: static;
    margin-top: 20px;
  }
}
</style>
