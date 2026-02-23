<template>
  <el-dialog
    v-model="visible"
    title="取消订单"
    width="500px"
    :close-on-click-modal="false"
  >
    <div class="cancel-form">
      <!-- 订单信息 -->
      <div class="order-info">
        <h4>订单信息</h4>
        <div class="info-item">
          <span class="label">订单号：</span>
          <span class="value">{{ orderInfo.orderNumber }}</span>
        </div>
        <div class="info-item">
          <span class="label">订单金额：</span>
          <span class="value">¥{{ orderInfo.amount }}</span>
        </div>
        <div class="info-item">
          <span class="label">下单时间：</span>
          <span class="value">{{ orderInfo.createTime }}</span>
        </div>
      </div>

      <!-- 取消原因选择 -->
      <div class="cancel-reasons">
        <h4>请选择取消原因：</h4>
        <el-radio-group v-model="selectedReason" @change="handleReasonChange">
          <el-radio
            v-for="reason in cancelReasons"
            :key="reason.value"
            :label="reason.value"
            class="reason-option"
          >
            {{ reason.label }}
          </el-radio>
        </el-radio-group>
      </div>

      <!-- 自定义原因 -->
      <div v-if="selectedReason === 'other'" class="custom-reason">
        <el-input
          v-model="customReason"
          type="textarea"
          placeholder="请详细说明取消原因"
          :rows="3"
          maxlength="200"
          show-word-limit
        />
      </div>

      <!-- 退款信息 -->
      <div v-if="showRefundInfo" class="refund-info">
        <h4>退款信息</h4>
        <div class="refund-details">
          <div class="refund-item">
            <span class="label">退款金额：</span>
            <span class="value">¥{{ orderInfo.amount }}</span>
          </div>
          <div class="refund-item">
            <span class="label">退款方式：</span>
            <span class="value">{{ orderInfo.paymentMethod }}</span>
          </div>
          <div class="refund-item">
            <span class="label">预计到账：</span>
            <span class="value">{{ getRefundTime() }}</span>
          </div>
        </div>
        <el-alert
          title="退款说明"
          type="info"
          :closable="false"
          show-icon
        >
          <p>• 退款将原路返回到您的支付账户</p>
          <p>• 微信/支付宝通常1-3个工作日到账</p>
          <p>• 银行卡退款可能需要3-7个工作日</p>
        </el-alert>
      </div>

      <!-- 取消限制说明 -->
      <div v-if="showCancelRestriction" class="cancel-restriction">
        <el-alert
          title="取消限制"
          type="warning"
          :closable="false"
          show-icon
        >
          <p>该订单已进入制作流程，无法直接取消。</p>
          <p>如需取消，请联系商家协商处理。</p>
        </el-alert>
        <div class="contact-info">
          <h5>联系方式：</h5>
          <p>客服电话：400-123-4567</p>
          <p>在线客服：点击右下角客服图标</p>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleCancel">取消</el-button>
      <el-button
        type="danger"
        :loading="submitting"
        :disabled="!canSubmit"
        @click="handleConfirm"
      >
        确认取消
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  orderInfo: {
    type: Object,
    required: true
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'confirm'])

// 响应式数据
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const selectedReason = ref('')
const customReason = ref('')
const submitting = ref(false)

// 取消原因选项
const cancelReasons = ref([
  { value: 'change_mind', label: '不想买了' },
  { value: 'wrong_product', label: '选错商品' },
  { value: 'price_high', label: '价格太贵' },
  { value: 'delivery_slow', label: '配送太慢' },
  { value: 'found_cheaper', label: '找到更便宜的' },
  { value: 'duplicate_order', label: '重复下单' },
  { value: 'other', label: '其他原因' }
])

// 计算属性
const showRefundInfo = computed(() => {
  return selectedReason.value && !showCancelRestriction.value
})

const showCancelRestriction = computed(() => {
  // 根据订单状态判断是否显示取消限制
  return props.orderInfo.status === 'processing' && props.orderInfo.processingTime > 5
})

const canSubmit = computed(() => {
  if (selectedReason.value === 'other') {
    return customReason.value.trim().length > 0
  }
  return selectedReason.value !== ''
})

// 方法
const handleReasonChange = (value) => {
  if (value !== 'other') {
    customReason.value = ''
  }
}

const getRefundTime = () => {
  const paymentMethod = props.orderInfo.paymentMethod
  if (paymentMethod.includes('微信') || paymentMethod.includes('支付宝')) {
    return '1-3个工作日'
  } else if (paymentMethod.includes('银行卡')) {
    return '3-7个工作日'
  }
  return '1-7个工作日'
}

const handleCancel = () => {
  visible.value = false
  resetForm()
}

const handleConfirm = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要取消这个订单吗？取消后将无法恢复。',
      '确认取消',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '再想想',
        type: 'warning'
      }
    )

    submitting.value = true

    // 模拟取消订单
    await new Promise(resolve => setTimeout(resolve, 1500))

    const cancelData = {
      orderId: props.orderInfo.orderNumber,
      reason: selectedReason.value,
      customReason: selectedReason.value === 'other' ? customReason.value : '',
      refundInfo: {
        amount: props.orderInfo.amount,
        method: props.orderInfo.paymentMethod,
        estimatedTime: getRefundTime()
      }
    }

    emit('confirm', cancelData)
    ElMessage.success('订单已取消，退款将原路返回')
    visible.value = false
    resetForm()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消订单失败，请重试')
    }
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  selectedReason.value = ''
  customReason.value = ''
  submitting.value = false
}

// 监听对话框关闭
watch(visible, (newVal) => {
  if (!newVal) {
    resetForm()
  }
})
</script>

<style scoped>
.cancel-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-info {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.order-info h4 {
  margin-bottom: 12px;
  color: #333;
  font-size: 16px;
}

.info-item {
  display: flex;
  margin-bottom: 8px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  color: #666;
  min-width: 80px;
}

.value {
  color: #333;
  font-weight: 500;
}

.cancel-reasons h4 {
  margin-bottom: 12px;
  color: #333;
  font-size: 16px;
}

.reason-option {
  display: block;
  margin-bottom: 8px;
  padding: 8px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.reason-option:hover {
  border-color: #ff6b6b;
  background-color: #fff5f5;
}

.reason-option.is-checked {
  border-color: #ff6b6b;
  background-color: #fff5f5;
}

.custom-reason {
  margin-top: 12px;
}

.refund-info {
  padding: 16px;
  background: #f0f9ff;
  border-radius: 8px;
  border: 1px solid #bae6fd;
}

.refund-info h4 {
  margin-bottom: 12px;
  color: #333;
  font-size: 16px;
}

.refund-details {
  margin-bottom: 16px;
}

.refund-item {
  display: flex;
  margin-bottom: 8px;
}

.refund-item:last-child {
  margin-bottom: 0;
}

.refund-item .label {
  color: #666;
  min-width: 80px;
}

.refund-item .value {
  color: #333;
  font-weight: 500;
}

.cancel-restriction {
  padding: 16px;
  background: #fff7e6;
  border-radius: 8px;
  border: 1px solid #ffd591;
}

.contact-info {
  margin-top: 16px;
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;
}

.contact-info h5 {
  margin-bottom: 8px;
  color: #333;
  font-size: 14px;
}

.contact-info p {
  margin-bottom: 4px;
  color: #666;
  font-size: 14px;
}

.contact-info p:last-child {
  margin-bottom: 0;
}

:deep(.el-alert) {
  margin-top: 12px;
}

:deep(.el-alert__content) {
  padding: 0;
}

:deep(.el-alert p) {
  margin-bottom: 4px;
  font-size: 14px;
}

:deep(.el-alert p:last-child) {
  margin-bottom: 0;
}
</style>
