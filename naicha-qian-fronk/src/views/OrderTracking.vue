<template>
  <div class="order-tracking-container">
    <div class="container">
      <div class="tracking-header">
        <el-button @click="$router.back()" :icon="ArrowLeft">
          返回
        </el-button>
        <h2>订单跟踪</h2>
      </div>

      <div v-loading="loading">
      <el-row v-if="!loading && trackingInfo.orderNumber" :gutter="20">
        <!-- 左侧跟踪信息 -->
        <el-col :span="16">
          <!-- 订单基本信息 -->
          <el-card class="order-info-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>订单信息</span>
                <el-tag :type="getStatusType(trackingInfo.status)">
                  {{ getStatusText(trackingInfo.status) }}
                </el-tag>
              </div>
            </template>
            
            <div class="order-basic-info">
              <div class="info-row">
                <span class="label">订单号：</span>
                <span class="value">{{ trackingInfo.orderNumber }}</span>
                <el-button type="primary" size="small" @click="copyOrderNumber">
                  复制
                </el-button>
              </div>
              <div class="info-row">
                <span class="label">下单时间：</span>
                <span class="value">{{ trackingInfo.createTime }}</span>
              </div>
              <div v-if="trackingInfo.estimatedDelivery" class="info-row">
                <span class="label">预计送达：</span>
                <span class="value">{{ trackingInfo.estimatedDelivery }}</span>
              </div>
            </div>
          </el-card>

          <!-- 配送信息 -->
          <el-card class="delivery-card" shadow="hover">
            <template #header>
              <span>配送信息</span>
            </template>
            
            <div class="delivery-info">
              <div v-if="trackingInfo.deliveryPerson" class="delivery-person">
                <div class="person-info">
                  <el-icon><User /></el-icon>
                  <div class="person-details">
                    <h4>{{ trackingInfo.deliveryPerson }}</h4>
                    <p>配送员</p>
                  </div>
                </div>
                <div class="contact-actions">
                  <el-button type="primary" size="small" @click="callDelivery">
                    <el-icon><Phone /></el-icon>
                    联系配送员
                  </el-button>
                </div>
              </div>
              
              <div v-if="trackingInfo.currentLocation" class="location-info">
                <div class="location-header">
                  <el-icon><Location /></el-icon>
                  <span>当前位置</span>
                </div>
                <div class="location-detail">
                  {{ trackingInfo.currentLocation }}
                </div>
                <div class="location-time">
                  更新时间：{{ trackingInfo.locationUpdateTime }}
                </div>
              </div>
            </div>
          </el-card>

          <!-- 配送轨迹 -->
          <el-card class="tracking-timeline-card" shadow="hover">
            <template #header>
              <span>配送轨迹</span>
            </template>
            
            <el-timeline>
              <el-timeline-item
                v-for="track in trackingInfo.trackingRecords"
                :key="track.id"
                :timestamp="track.timestamp"
                :type="getTimelineType(track.status)"
                :icon="getTimelineIcon(track.status)"
              >
                <div class="timeline-content">
                  <h4>{{ track.title }}</h4>
                  <p v-if="track.description">{{ track.description }}</p>
                  <div v-if="track.location" class="track-location">
                    <el-icon><Location /></el-icon>
                    <span>{{ track.location }}</span>
                  </div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </el-card>
        </el-col>

        <!-- 右侧商品和操作 -->
        <el-col :span="8">
          <!-- 商品信息 -->
          <el-card class="products-card" shadow="hover">
            <template #header>
              <span>商品信息</span>
            </template>
            
            <div class="product-list">
              <div
                v-for="item in trackingInfo.items"
                :key="item.id"
                class="product-item"
              >
                <SmartImage :src="item.image" :alt="item.name" image-class="product-image" />
                <div class="product-info">
                  <h5>{{ item.name }}</h5>
                  <p>{{ item.description }}</p>
                  <div class="product-specs">
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
                <span class="receiver">{{ trackingInfo.address.receiver }}</span>
                <span class="phone">{{ trackingInfo.address.phone }}</span>
              </div>
              <div class="address-detail">
                {{ trackingInfo.address.province }} {{ trackingInfo.address.city }} {{ trackingInfo.address.district }} {{ trackingInfo.address.detail }}
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
                v-if="trackingInfo.status === 'shipped'"
                type="success"
                @click="confirmReceipt"
                class="action-button"
              >
                确认收货
              </el-button>
              
              <el-button
                v-if="trackingInfo.status === 'shipped'"
                @click="contactDelivery"
                class="action-button"
              >
                联系配送员
              </el-button>
              
              <el-button
                v-if="trackingInfo.status === 'delivered'"
                @click="rateOrder"
                class="action-button"
              >
                评价订单
              </el-button>
              
              <el-button
                v-if="trackingInfo.status === 'delivered'"
                @click="applyAfterSale"
                class="action-button"
              >
                申请售后
              </el-button>
            </div>
          </el-card>

          <!-- 配送统计 -->
          <el-card class="stats-card" shadow="hover">
            <template #header>
              <span>配送统计</span>
            </template>
            
            <div class="delivery-stats">
              <div v-if="trackingInfo.deliveryDistance" class="stat-item">
                <span class="stat-label">配送距离</span>
                <span class="stat-value">{{ trackingInfo.deliveryDistance }}km</span>
              </div>
              <div v-if="trackingInfo.deliveryDuration" class="stat-item">
                <span class="stat-label">配送时长</span>
                <span class="stat-value">{{ trackingInfo.deliveryDuration }}</span>
              </div>
              <div v-if="trackingInfo.deliveryRating" class="stat-item">
                <span class="stat-label">配送员评分</span>
                <span class="stat-value">{{ trackingInfo.deliveryRating }}分</span>
              </div>
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
import { ArrowLeft, User, Phone, Location } from '@element-plus/icons-vue'
import { getImageByIndex, getRandomImage } from '@/utils/imageLoader'
import { 
  getOrderDetail, 
  getOrderByOrderNo, 
  getOrderShipment, 
  getOrderShipmentByOrderNo,
  completeOrder as completeOrderApi 
} from '@/api/order'

const router = useRouter()
const route = useRoute()

// 加载状态
const loading = ref(false)

// 跟踪信息
const trackingInfo = ref({
  orderNumber: '',
  orderId: null,
  status: 'pending',
  createTime: '',
  estimatedDelivery: '',
  deliveryPerson: '',
  deliveryPhone: '',
  currentLocation: '',
  locationUpdateTime: '',
  deliveryDistance: '',
  deliveryDuration: '',
  deliveryRating: '',
  items: [],
  address: {
    receiver: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: ''
  },
  trackingRecords: []
})

// 订单状态映射（后端数字 -> 前端字符串）
const mapOrderStatus = (status) => {
  // 后端状态：0-待支付 1-已支付 2-已接单 3-制作中 4-已发货 5-已完成 6-已取消 7-已退款
  const statusMap = {
    0: 'pending',
    1: 'processing',
    2: 'processing',
    3: 'processing',
    4: 'shipped',
    5: 'delivered',
    6: 'cancelled',
    7: 'refunded'
  }
  return statusMap[status] || 'pending'
}

// 配送状态映射
const mapShippingStatus = (status) => {
  const statusMap = {
    'pending': 'shipped', // 待发货也显示为配送中
    'shipping': 'shipped',
    'delivered': 'delivered',
    'exception': 'shipped'
  }
  return statusMap[status] || 'shipped'
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
      minute: '2-digit',
      second: '2-digit'
    }).replace(/\//g, '-')
  } catch (error) {
    return timeStr.toString()
  }
}

// 方法
const getStatusType = (status) => {
  const statusMap = {
    pending: 'warning',
    processing: 'primary',
    shipped: 'info',
    delivered: 'success',
    cancelled: 'info'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    pending: '待付款',
    processing: '处理中',
    shipped: '配送中',
    delivered: '已送达',
    cancelled: '已取消'
  }
  return statusMap[status] || '未知状态'
}

const getTimelineType = (status) => {
  const typeMap = {
    created: 'primary',
    paid: 'success',
    accepted: 'primary',
    prepared: 'primary',
    picked_up: 'info',
    in_transit: 'info',
    delivered: 'success'
  }
  return typeMap[status] || 'primary'
}

const getTimelineIcon = (status) => {
  const iconMap = {
    created: 'Document',
    paid: 'CreditCard',
    accepted: 'Check',
    prepared: 'Box',
    picked_up: 'Truck',
    in_transit: 'Location',
    shipping: 'Location',
    delivered: 'CircleCheck',
    exception: 'Warning'
  }
  return iconMap[status] || 'Document'
}

// 格式化订单数据
const formatOrderData = (orderData) => {
  // 处理地址
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
    selectedSpecs: item.selectedSpecs || {}
  }))
  
  return {
    orderId: orderData.id,
    orderNumber: orderData.orderNo || '',
    status: mapOrderStatus(orderData.status),
    statusCode: orderData.status,
    createTime: formatTime(orderData.createdAt),
    items: items,
    address: address
  }
}

// 格式化配送信息
const formatShipmentData = (shipmentData, orderData) => {
  if (!shipmentData) return null
  
  // 处理配送轨迹记录
  const trackingRecords = []
  
  // 添加订单状态记录
  if (orderData) {
    if (orderData.createdAt) {
      trackingRecords.push({
        id: 1,
        status: 'created',
        title: '订单已创建',
        description: '您的订单已提交，等待支付',
        timestamp: formatTime(orderData.createdAt),
        location: '商家店铺'
      })
    }
    if (orderData.payTime) {
      trackingRecords.push({
        id: 2,
        status: 'paid',
        title: '支付成功',
        description: '订单支付完成，商家开始制作',
        timestamp: formatTime(orderData.payTime),
        location: '商家店铺'
      })
    }
    if (orderData.status >= 2 && orderData.status <= 3) {
      trackingRecords.push({
        id: 3,
        status: 'accepted',
        title: '商家已接单',
        description: '商家已接单，正在制作中',
        timestamp: formatTime(orderData.updatedAt || orderData.createdAt),
        location: '商家店铺'
      })
    }
  }
  
  // 添加配送进度历史
  if (shipmentData.progressHistory && shipmentData.progressHistory.length > 0) {
    shipmentData.progressHistory.forEach((progress, index) => {
      const statusMap = {
        'pending': { title: '待发货', description: '订单已准备发货' },
        'shipping': { title: '配送中', description: progress.description || '配送员正在配送中' },
        'delivered': { title: '已送达', description: progress.description || '商品已送达' },
        'exception': { title: '配送异常', description: progress.description || '配送过程中出现异常' }
      }
      
      const statusInfo = statusMap[progress.status] || { title: '配送更新', description: progress.description || '' }
      
      trackingRecords.push({
        id: 100 + index,
        status: progress.status || 'shipping',
        title: statusInfo.title,
        description: statusInfo.description,
        timestamp: progress.timeText || formatTime(progress.time),
        location: progress.description || ''
      })
    })
  } else if (shipmentData.shippingTime) {
    // 如果没有进度历史，根据发货时间生成记录
    trackingRecords.push({
      id: 4,
      status: 'shipping',
      title: '商品已发货',
      description: '商品已发货，配送员正在配送中',
      timestamp: shipmentData.shippingTimeText || formatTime(shipmentData.shippingTime),
      location: '商家店铺'
    })
  }
  
  // 计算配送距离和时长（如果有相关信息）
  let deliveryDistance = ''
  let deliveryDuration = ''
  if (shipmentData.estimatedTime && shipmentData.shippingTime) {
    try {
      const shippingTime = new Date(shipmentData.shippingTime)
      const estimatedTime = new Date(shipmentData.estimatedTime)
      const duration = Math.round((estimatedTime - shippingTime) / (1000 * 60)) // 分钟
      deliveryDuration = duration > 0 ? `${duration}分钟` : ''
    } catch (e) {
      console.warn('计算配送时长失败:', e)
    }
  }
  
  return {
    deliveryPerson: shipmentData.deliveryPerson || '',
    deliveryPhone: shipmentData.deliveryPhone || '',
    currentLocation: shipmentData.shippingStatusText || '配送中',
    locationUpdateTime: shipmentData.shippingTimeText || '',
    estimatedDelivery: shipmentData.estimatedTimeText || '',
    deliveryDistance: deliveryDistance,
    deliveryDuration: deliveryDuration,
    deliveryRating: '', // 后端暂无此字段
    trackingRecords: trackingRecords.sort((a, b) => {
      // 按时间排序
      return new Date(a.timestamp) - new Date(b.timestamp)
    })
  }
}

// 加载订单跟踪信息
const loadTrackingInfo = async (orderId) => {
  try {
    loading.value = true
    
    // 根据参数类型判断是ID还是订单号
    const isNumeric = /^\d+$/.test(orderId)
    let orderData
    let shipmentData
    
    // 获取订单详情
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
    const formattedOrder = formatOrderData(orderData)
    
    // 初始化跟踪记录（基于订单状态）
    const initialTrackingRecords = []
    if (orderData.createdAt) {
      initialTrackingRecords.push({
        id: 1,
        status: 'created',
        title: '订单已创建',
        description: '您的订单已提交，等待支付',
        timestamp: formatTime(orderData.createdAt),
        location: '商家店铺'
      })
    }
    if (orderData.payTime) {
      initialTrackingRecords.push({
        id: 2,
        status: 'paid',
        title: '支付成功',
        description: '订单支付完成，商家开始制作',
        timestamp: formatTime(orderData.payTime),
        location: '商家店铺'
      })
    }
    if (orderData.status >= 2 && orderData.status <= 3) {
      initialTrackingRecords.push({
        id: 3,
        status: 'accepted',
        title: '商家已接单',
        description: '商家已接单，正在制作中',
        timestamp: formatTime(orderData.updatedAt || orderData.createdAt),
        location: '商家店铺'
      })
    }
    
    trackingInfo.value = {
      ...trackingInfo.value,
      ...formattedOrder,
      trackingRecords: initialTrackingRecords
    }
    
    // 获取配送信息
    try {
      if (isNumeric) {
        shipmentData = await getOrderShipment(orderData.id)
      } else {
        shipmentData = await getOrderShipmentByOrderNo(orderData.orderNo)
      }
      
      if (shipmentData) {
        // 如果订单已发货，更新状态为配送中
        if (orderData.status >= 4) {
          trackingInfo.value.status = mapShippingStatus(shipmentData.shippingStatus)
        }
        
        // 格式化配送信息
        const formattedShipment = formatShipmentData(shipmentData, orderData)
        if (formattedShipment) {
          trackingInfo.value = {
            ...trackingInfo.value,
            ...formattedShipment,
            trackingRecords: formattedShipment.trackingRecords || trackingInfo.value.trackingRecords
          }
        }
      } else if (orderData.status >= 4) {
        // 订单已发货但没有配送信息，显示为配送中
        trackingInfo.value.status = 'shipped'
        // 添加发货记录
        trackingInfo.value.trackingRecords.push({
          id: 4,
          status: 'shipping',
          title: '商品已发货',
          description: '商品已发货，等待配送',
          timestamp: formatTime(orderData.deliveryTime || orderData.updatedAt),
          location: '商家店铺'
        })
      }
    } catch (error) {
      console.warn('加载配送信息失败:', error)
      // 即使配送信息加载失败，也继续显示订单信息
      if (orderData.status >= 4) {
        trackingInfo.value.status = 'shipped'
      }
    }
  } catch (error) {
    console.error('加载订单跟踪信息失败:', error)
    ElMessage.error('加载订单跟踪信息失败')
    router.back()
  } finally {
    loading.value = false
  }
}

const copyOrderNumber = () => {
  navigator.clipboard.writeText(trackingInfo.value.orderNumber).then(() => {
    ElMessage.success('订单号已复制到剪贴板')
  })
}

const callDelivery = () => {
  if (!trackingInfo.value.deliveryPhone) {
    ElMessage.warning('配送员电话信息不可用')
    return
  }
  // 拨打电话
  window.location.href = `tel:${trackingInfo.value.deliveryPhone}`
}

const contactDelivery = () => {
  callDelivery()
}

const confirmReceipt = async () => {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？', '确认收货', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    try {
      await completeOrderApi(trackingInfo.value.orderId)
      trackingInfo.value.status = 'delivered'
      ElMessage.success('确认收货成功')
      // 重新加载跟踪信息
      await loadTrackingInfo(trackingInfo.value.orderId || trackingInfo.value.orderNumber)
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

const rateOrder = () => {
  ElMessage.info('评价订单功能待开发')
}

const applyAfterSale = () => {
  router.push({
    path: '/after-sale',
    query: {
      orderId: trackingInfo.value.orderNumber
    }
  })
}

onMounted(() => {
  // 根据路由参数获取跟踪信息
  const orderId = route.query.orderId || route.params.orderId
  if (orderId) {
    loadTrackingInfo(orderId)
  } else {
    ElMessage.error('订单ID不能为空')
    router.back()
  }
})
</script>

<style scoped>
.order-tracking-container {
  padding: 20px 0;
  min-height: calc(100vh - 120px);
}

.tracking-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.tracking-header h2 {
  color: #333;
  margin: 0;
}

.order-info-card,
.delivery-card,
.tracking-timeline-card {
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
  font-weight: 500;
}

.delivery-info {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.delivery-person {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.person-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.person-details h4 {
  margin-bottom: 4px;
  color: #333;
  font-size: 16px;
}

.person-details p {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.location-info {
  padding: 16px;
  background: #f0f9ff;
  border-radius: 8px;
  border: 1px solid #bae6fd;
}

.location-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: #1890ff;
  font-weight: 500;
}

.location-detail {
  color: #333;
  font-size: 16px;
  margin-bottom: 8px;
}

.location-time {
  color: #666;
  font-size: 12px;
}

.timeline-content h4 {
  margin-bottom: 4px;
  color: #333;
}

.timeline-content p {
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
}

.track-location {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #1890ff;
  font-size: 14px;
}

.products-card,
.address-card,
.actions-card,
.stats-card {
  margin-bottom: 20px;
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.product-item:last-child {
  border-bottom: none;
}

.product-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-info h5 {
  margin-bottom: 4px;
  color: #333;
  font-size: 14px;
}

.product-info p {
  color: #666;
  font-size: 12px;
  margin-bottom: 4px;
}

.product-specs {
  margin-bottom: 4px;
}

.product-specs .el-tag {
  margin-right: 4px;
  margin-bottom: 4px;
}

.product-quantity {
  width: 40px;
  text-align: center;
  color: #666;
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

.order-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-button {
  width: 100%;
}

.delivery-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.stat-value {
  color: #333;
  font-weight: 500;
  font-size: 16px;
}

@media (max-width: 768px) {
  .delivery-person {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .product-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .product-image {
    width: 40px;
    height: 40px;
  }
  
  .product-quantity {
    width: auto;
  }
  
  .products-card,
  .address-card,
  .actions-card,
  .stats-card {
    position: static;
    margin-top: 20px;
  }
}
</style>
