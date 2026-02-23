<template>
  <div class="order-confirm-container">
    <div class="container">
      <div class="confirm-header">
        <h2>订单确认</h2>
        <div class="order-timer" v-if="paymentTimeout > 0">
          <el-icon><Clock /></el-icon>
          <span>支付剩余时间：{{ formatTime(paymentTimeout) }}</span>
        </div>
      </div>

      <el-row :gutter="20">
        <!-- 左侧内容 -->
        <el-col :span="16">
          <!-- 收货地址 -->
          <el-card class="address-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>收货地址</span>
                <el-button type="primary" size="small" @click="showAddressDialog = true">
                  选择地址
                </el-button>
              </div>
            </template>
            
            <div v-loading="loading" v-if="selectedAddress" class="selected-address">
              <div class="address-info">
                <div class="address-main">
                  <span class="receiver">{{ selectedAddress.receiver }}</span>
                  <span class="phone">{{ selectedAddress.phone }}</span>
                </div>
                <div class="address-detail">
                  {{ selectedAddress.province }} {{ selectedAddress.city }} {{ selectedAddress.district }} {{ selectedAddress.detail }}
                </div>
                <div v-if="selectedAddress.isDefault" class="default-tag">
                  <el-tag type="success" size="small">默认地址</el-tag>
                </div>
              </div>
            </div>
            
            <el-empty v-else description="请选择收货地址" />
          </el-card>

          <!-- 商品清单 -->
          <el-card class="products-card" shadow="hover">
            <template #header>
              <span>商品清单</span>
            </template>
            
            <div class="product-list">
              <div
                v-for="item in orderItems"
                :key="item.cartItemId"
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

          <!-- 支付方式 -->
          <el-card class="payment-card" shadow="hover">
            <template #header>
              <span>支付方式</span>
            </template>
            
            <div class="payment-methods">
              <div
                v-for="method in paymentMethods"
                :key="method.id"
                class="payment-method"
                :class="{ active: selectedPayment === method.id }"
                @click="selectPayment(method.id)"
              >
                <div class="method-icon">
                  <el-icon><component :is="method.icon" /></el-icon>
                </div>
                <div class="method-info">
                  <h4>{{ method.name }}</h4>
                  <p>{{ method.description }}</p>
                </div>
                <div class="method-radio">
                  <el-radio :value="selectedPayment" :label="method.id" />
                </div>
              </div>
            </div>
          </el-card>

          <!-- 优惠券选择 -->
          <el-card class="coupon-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>优惠券</span>
                <el-button type="primary" size="small" @click="showCouponDialog = true">
                  选择优惠券
                </el-button>
              </div>
            </template>
            
            <div v-if="selectedCoupon" class="selected-coupon">
              <div class="coupon-info">
                <h4>{{ selectedCoupon.name }}</h4>
                <p>{{ selectedCoupon.description }}</p>
                <span class="coupon-value">
                  <template v-if="selectedCoupon.type === 'discount'">
                    {{ selectedCoupon.value >= 1 ? selectedCoupon.value + '折' : (selectedCoupon.value * 10).toFixed(0) + '折' }}
                  </template>
                  <template v-else>
                    ¥{{ selectedCoupon.value }}
                  </template>
                </span>
              </div>
              <el-button type="danger" size="small" @click="removeCoupon">
                取消使用
              </el-button>
            </div>
            
            <div v-else class="no-coupon">
              <span>暂未使用优惠券</span>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧结算 -->
        <el-col :span="8">
          <el-card class="checkout-card" shadow="hover">
            <template #header>
              <span>费用明细</span>
            </template>
            
            <div class="checkout-details">
              <div class="detail-row">
                <span>商品总价：</span>
                <span>¥{{ orderSummary.productTotal.toFixed(2) }}</span>
              </div>
              <div class="detail-row">
                <span>配送费：</span>
                <span v-if="orderSummary.deliveryFee === 0">免费</span>
                <span v-else>¥{{ orderSummary.deliveryFee.toFixed(2) }}</span>
              </div>
              <div class="detail-row" v-if="orderSummary.couponDiscount > 0">
                <span>优惠券抵扣：</span>
                <span class="discount">-¥{{ orderSummary.couponDiscount.toFixed(2) }}</span>
              </div>
              <div class="detail-row" v-if="orderSummary.pointsDiscount > 0">
                <span>积分抵扣：</span>
                <span class="discount">-¥{{ orderSummary.pointsDiscount.toFixed(2) }}</span>
              </div>
              <el-divider />
              <div class="detail-row total-row">
                <span>实付金额：</span>
                <span class="total-amount">¥{{ orderSummary.finalAmount.toFixed(2) }}</span>
              </div>
            </div>

            <div class="checkout-actions">
              <el-button
                type="primary"
                size="large"
                :loading="submitting"
                :disabled="!canSubmit"
                @click="submitOrder"
                class="submit-button"
              >
                提交订单
              </el-button>
            </div>

            <div class="order-notes">
              <el-input
                v-model="orderNotes"
                type="textarea"
                placeholder="订单备注（选填）"
                :rows="3"
                maxlength="200"
                show-word-limit
              />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 地址选择对话框 -->
    <el-dialog
      v-model="showAddressDialog"
      title="选择收货地址"
      width="600px"
    >
      <div class="address-list">
        <div
          v-for="address in addressList"
          :key="address.id"
          class="address-item"
          :class="{ active: selectedAddress?.id === address.id }"
          @click="selectAddress(address)"
        >
          <div class="address-content">
            <div class="address-main">
              <span class="receiver">{{ address.receiver }}</span>
              <span class="phone">{{ address.phone }}</span>
              <el-tag v-if="address.isDefault" type="success" size="small">默认</el-tag>
            </div>
            <div class="address-detail">
              {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detail }}
            </div>
          </div>
          <div class="address-actions">
            <el-button type="primary" size="small" @click.stop="selectAddress(address)">
              选择
            </el-button>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showAddressDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAddress">确定</el-button>
      </template>
    </el-dialog>

    <!-- 优惠券选择对话框 -->
    <el-dialog
      v-model="showCouponDialog"
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
            <span v-if="coupon.type === 'discount'">{{ coupon.value }}折</span>
            <span v-else>¥{{ coupon.value }}</span>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showCouponDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmCoupon">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, CreditCard, Wallet, Money } from '@element-plus/icons-vue'
import { useCartStore } from '@/stores/cart'
import { getAddressList } from '@/api/address'
import { getMyCoupons } from '@/api/coupon'
import { createOrder, payOrder } from '@/api/order'
import { getImageByIndex, getRandomImage } from '@/utils/imageLoader'
import { calculateCouponDiscount, calculateOrderTotal } from '@/utils/priceCalculator'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()

// 响应式数据
const showAddressDialog = ref(false)
const showCouponDialog = ref(false)
const submitting = ref(false)
const loading = ref(false)
const orderNotes = ref('')
const selectedAddress = ref(null)
const selectedPayment = ref('wechat')
const selectedCoupon = ref(null)
const paymentTimeout = ref(15 * 60) // 15分钟倒计时

// 订单商品 - 从购物车获取选中的商品
const orderItems = ref([])

// 地址列表
const addressList = ref([])

// 支付方式
const paymentMethods = ref([
  {
    id: 'wechat',
    name: '微信支付',
    description: '推荐使用，安全便捷',
    icon: 'CreditCard'
  },
  {
    id: 'alipay',
    name: '支付宝',
    description: '支持花呗分期',
    icon: 'Money'
  },
  {
    id: 'wallet',
    name: '余额支付',
    description: '使用账户余额',
    icon: 'Wallet'
  }
])

// 可用优惠券
const availableCoupons = ref([])

// 格式化地址数据
const formatAddress = (address) => {
  // 处理后端可能返回的不同格式
  return {
    id: address.id,
    receiver: address.receiver || address.receiverName || address.receiver_name || '',
    phone: address.phone || address.phoneNumber || address.phone_number || '',
    province: address.province || (Array.isArray(address.region) ? address.region[0] : ''),
    city: address.city || (Array.isArray(address.region) && address.region.length > 1 ? address.region[1] : ''),
    district: address.district || (Array.isArray(address.region) && address.region.length > 2 ? address.region[2] : ''),
    detail: address.detail || address.addressDetail || address.address_detail || '',
    isDefault: address.isDefault === 1 || address.isDefault === true || address.is_default === 1 || address.is_default === true
  }
}

// 格式化优惠券数据
const formatCoupon = (coupon) => {
  // 处理优惠券阈值（后端可能是minAmount或threshold）
  const minAmount = coupon.minAmount || coupon.min_amount || coupon.threshold || 0
  let condition = ''
  if (minAmount > 0) {
    condition = `满${minAmount}元可用`
  } else {
    condition = coupon.condition || '无门槛'
  }
  
  // 处理优惠券值（折扣可能是0.9表示9折，或者9表示9折）
  let value = coupon.value
  if (coupon.type === 'discount') {
    // 如果是折扣类型，确保值在0-1之间（0.9表示9折）
    if (value > 1) {
      value = value / 10
    }
  }
  
  return {
    id: coupon.id,
    name: coupon.name || coupon.title || '',
    description: coupon.description || coupon.desc || '',
    condition: condition,
    type: coupon.type || 'reduce',
    value: value || 0,
    minAmount: minAmount
  }
}

// 加载地址列表
const loadAddressList = async () => {
  try {
    const response = await getAddressList()
    const addresses = Array.isArray(response) ? response : (response?.records || response?.list || [])
    addressList.value = addresses.map(formatAddress)
    
    // 设置默认地址
    if (!selectedAddress.value && addressList.value.length > 0) {
      const defaultAddr = addressList.value.find(addr => addr.isDefault) || addressList.value[0]
      selectedAddress.value = defaultAddr
    }
  } catch (error) {
    console.error('加载地址列表失败:', error)
    // 如果接口不存在，使用空列表
    if (error.response?.status === 404) {
      console.warn('地址接口不存在，请后端添加')
      addressList.value = []
    } else {
      ElMessage.error('加载地址列表失败')
    }
  }
}

// 加载可用优惠券
const loadAvailableCoupons = async () => {
  try {
    const response = await getMyCoupons({ status: 'unused' })
    const coupons = response?.records || response?.list || (Array.isArray(response) ? response : [])
    availableCoupons.value = coupons.map(formatCoupon)
    
    // 如果有URL参数传入的优惠券ID，尝试选择
    const couponId = route.query.couponId
    if (couponId) {
      const coupon = availableCoupons.value.find(c => c.id === parseInt(couponId))
      if (coupon) {
        selectedCoupon.value = coupon
      }
    }
  } catch (error) {
    console.error('加载优惠券列表失败:', error)
    // 如果接口不存在，使用空列表
    if (error.response?.status === 404) {
      console.warn('优惠券接口不存在，请后端添加')
      availableCoupons.value = []
    }
  }
}

// 计算属性
const orderSummary = computed(() => {
  // 计算商品总价
  const productTotal = orderItems.value.reduce((sum, item) => sum + (item.price * item.quantity), 0)
  const deliveryFee = productTotal >= 30 ? 0 : 5
  
  // 使用工具函数计算优惠券折扣（实时响应优惠券选择变化）
  const couponDiscount = selectedCoupon.value 
    ? calculateCouponDiscount(productTotal, selectedCoupon.value)
    : 0
  
  const pointsDiscount = 0 // 积分抵扣，暂时为0
  const finalAmount = Math.max(0, productTotal + deliveryFee - couponDiscount - pointsDiscount)
  
  return {
    productTotal,
    deliveryFee,
    couponDiscount,
    pointsDiscount,
    finalAmount
  }
})

const canSubmit = computed(() => {
  return selectedAddress.value && selectedPayment.value
})

// 方法
const formatTime = (seconds) => {
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`
}

const selectAddress = (address) => {
  selectedAddress.value = address
}

const confirmAddress = () => {
  showAddressDialog.value = false
}

const selectPayment = (paymentId) => {
  selectedPayment.value = paymentId
}

const selectCoupon = (coupon) => {
  selectedCoupon.value = coupon
}

const confirmCoupon = () => {
  showCouponDialog.value = false
}

const removeCoupon = () => {
  selectedCoupon.value = null
}

// 格式化订单商品数据（匹配后端格式）
const formatOrderItems = () => {
  return orderItems.value.map(item => ({
    productId: item.id || item.productId,
    productName: item.name || item.productName,
    productImage: (() => {
      const img = item.image || item.productImage || item.picture || ''
      if (!img || img === '/product-default.jpg' || img === 'undefined' || img === 'null') {
        return getImageByIndex(item.id || item.productId || 0) || getRandomImage() || '/product-default.jpg'
      }
      return img
    })(),
    price: item.price,
    quantity: item.quantity,
    cartItemId: item.cartItemId || null, // 购物车项ID，用于关联营销活动参与记录
    selectedSpecs: item.selectedSpecs || {} // 规格信息
  }))
}

const submitOrder = async () => {
  if (!canSubmit.value) {
    ElMessage.warning('请完善订单信息')
    return
  }
  
  // 二次确认
  try {
    await ElMessageBox.confirm(
      `确认提交订单？实付金额：¥${orderSummary.value.finalAmount.toFixed(2)}`,
      '确认订单',
      {
        confirmButtonText: '确认提交',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return // 用户取消
  }
  
  try {
    submitting.value = true
    
    // 构建订单数据
    const orderData = {
      addressId: selectedAddress.value.id,
      payMethod: selectedPayment.value, // wechat/alipay/wallet
      couponId: selectedCoupon.value?.id || null,
      items: formatOrderItems(),
      remark: orderNotes.value || null
    }
    
    // 调用创建订单API
    const result = await createOrder(orderData)
    
    // 如果订单创建成功，自动执行支付
    if (result?.id || result?.orderNo) {
      const orderId = result.id || result.orderNo
      
      // 显示支付处理中提示
      const loadingMessage = ElMessage({
        message: '订单创建成功，正在处理支付...',
        type: 'info',
        duration: 0,
        showClose: false
      })
      
      try {
        // 调用支付接口（模拟支付）
        await payOrder(orderId, orderData.payMethod)
        
        // 关闭加载提示
        loadingMessage.close()
        
        ElMessage.success('订单创建成功，支付成功！')
        
        // 清空购物车中已下单的商品
        const selectedCartItems = cartStore.getValidItems.filter(item => item.selected)
        const cartItemIds = selectedCartItems.map(item => item.cartItemId)
        cartStore.removeMultipleItems(cartItemIds)
        
        // 跳转到订单列表（订单状态会在页面加载时从后端获取，应该已经是已支付状态）
        router.push(`/orders?orderId=${orderId}`)
      } catch (payError) {
        // 关闭加载提示
        loadingMessage.close()
        
        console.error('支付失败:', payError)
        ElMessage.warning('订单创建成功，但支付失败，请前往订单列表完成支付')
        
        // 清空购物车中已下单的商品
        const selectedCartItems = cartStore.getValidItems.filter(item => item.selected)
        const cartItemIds = selectedCartItems.map(item => item.cartItemId)
        cartStore.removeMultipleItems(cartItemIds)
        
        // 跳转到订单列表
        router.push(`/orders?orderId=${orderId}`)
      }
    } else {
      ElMessage.success('订单提交成功！')
      router.push('/orders')
    }
  } catch (error) {
    console.error('订单提交失败:', error)
    ElMessage.error(error.message || '订单提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 倒计时
let timer = null
onMounted(async () => {
  loading.value = true
  
  try {
    // 从购物车获取选中的商品
    const selectedCartItems = cartStore.getValidItems.filter(item => item.selected)
    if (selectedCartItems.length === 0) {
      ElMessage.warning('购物车中没有选中的商品')
      router.push('/cart')
      return
    }
    
    // 设置订单商品
    orderItems.value = selectedCartItems.map(item => ({
      ...item,
      cartItemId: item.cartItemId,
      id: item.id || item.productId, // 确保有productId
      name: item.name || item.productName,
      description: item.description || item.desc || '',
      price: item.price,
      quantity: item.quantity,
      image: (() => {
        const img = item.image || item.productImage || item.picture || ''
        if (!img || img === '/product-default.jpg' || img === 'undefined' || img === 'null') {
          return getImageByIndex(item.id || item.productId || 0) || getRandomImage() || '/product-default.jpg'
        }
        return img
      })(),
      selectedSpecs: item.selectedSpecs || {}
    }))
    
    // 并行加载地址列表和优惠券列表
    await Promise.all([
      loadAddressList(),
      loadAvailableCoupons()
    ])
  } catch (error) {
    console.error('初始化失败:', error)
  } finally {
    loading.value = false
  }
  
  // 开始倒计时
  timer = setInterval(() => {
    if (paymentTimeout.value > 0) {
      paymentTimeout.value--
    } else {
      ElMessage.warning('订单已超时，请重新下单')
      router.push('/cart')
    }
  }, 1000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.order-confirm-container {
  padding: 20px 0;
  min-height: calc(100vh - 120px);
}

.confirm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.confirm-header h2 {
  color: #333;
  margin: 0;
}

.order-timer {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #ff6b6b;
  font-weight: bold;
}

.address-card,
.products-card,
.payment-card,
.coupon-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.selected-address {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.address-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.address-main {
  display: flex;
  align-items: center;
  gap: 12px;
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

.default-tag {
  margin-top: 8px;
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
  margin-bottom: var(--spacing-sm);
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
}

.product-specs .el-tag {
  background-color: var(--color-secondary-lighter);
  color: var(--color-secondary-dark);
  border: 1px solid var(--color-secondary-light);
  border-radius: var(--radius-md);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
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

.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payment-method {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.payment-method:hover {
  border-color: #ff6b6b;
}

.payment-method.active {
  border-color: #ff6b6b;
  background-color: #fff5f5;
}

.method-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  border-radius: 50%;
  margin-right: 16px;
}

.method-info {
  flex: 1;
}

.method-info h4 {
  margin-bottom: 4px;
  color: #333;
}

.method-info p {
  color: #666;
  font-size: 14px;
}

.method-radio {
  margin-left: 16px;
}

.selected-coupon {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
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

.coupon-value {
  color: #ff6b6b;
  font-weight: bold;
}

.no-coupon {
  text-align: center;
  color: #999;
  padding: 20px;
}

.checkout-card {
  position: sticky;
  top: 20px;
}

.checkout-details {
  margin-bottom: 20px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
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
  color: var(--color-primary);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
}

.checkout-actions {
  margin-bottom: 20px;
}

.submit-button {
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

.submit-button:hover:not(:disabled) {
  background: linear-gradient(135deg, var(--color-primary-light) 0%, var(--color-primary) 100%);
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
  filter: brightness(1.1);
}

.submit-button:active:not(:disabled) {
  transform: translateY(0);
}

.order-notes {
  margin-top: 20px;
}

.address-list {
  max-height: 400px;
  overflow-y: auto;
}

.address-item {
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

.address-item:hover {
  border-color: #ff6b6b;
}

.address-item.active {
  border-color: #ff6b6b;
  background-color: #fff5f5;
}

.address-content {
  flex: 1;
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
  .confirm-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
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
  
  .checkout-card {
    position: static;
    margin-top: 20px;
  }
}
</style>