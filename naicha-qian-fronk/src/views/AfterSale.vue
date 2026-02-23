<template>
  <div class="after-sale-container">
    <div class="container">
      <div class="after-sale-header">
        <h2>申请售后</h2>
        <p>订单完成7天内可申请售后</p>
      </div>

      <el-row :gutter="20">
        <!-- 左侧申请表单 -->
        <el-col :span="16">
          <!-- 订单信息 -->
          <el-card class="order-info-card" shadow="hover" v-loading="loading">
            <template #header>
              <span>订单信息</span>
            </template>
            
            <div class="order-info">
              <div class="order-basic">
                <div class="info-item">
                  <span class="label">订单号：</span>
                  <span class="value">{{ orderInfo.orderNumber }}</span>
                </div>
                <div class="info-item">
                  <span class="label">下单时间：</span>
                  <span class="value">{{ orderInfo.createTime }}</span>
                </div>
                <div class="info-item">
                  <span class="label">完成时间：</span>
                  <span class="value">{{ orderInfo.completeTime }}</span>
                </div>
              </div>
              
              <div class="order-items">
                <h4>商品清单</h4>
                <div
                  v-for="item in orderInfo.items"
                  :key="item.id"
                  class="order-item"
                >
                  <SmartImage :src="item.image" :alt="item.name" image-class="item-image" />
                  <div class="item-info">
                    <h5>{{ item.name }}</h5>
                    <p>{{ item.description }}</p>
                    <div class="item-specs">
                      <el-tag
                        v-for="(value, key) in item.selectedSpecs"
                        :key="key"
                        size="small"
                      >
                        {{ key }}: {{ value }}
                      </el-tag>
                    </div>
                  </div>
                  <div class="item-quantity">x{{ item.quantity }}</div>
                  <div class="item-price">¥{{ item.price }}</div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 售后申请表单 -->
          <el-card class="application-form-card" shadow="hover">
            <template #header>
              <span>售后申请</span>
              <el-alert
                v-if="existingApplication"
                :title="`已有售后申请，状态：${getRefundStatusText(existingApplication.status)}`"
                type="info"
                :closable="false"
                show-icon
                style="margin-top: 8px"
              />
            </template>
            
            <el-form
              ref="formRef"
              :model="applicationForm"
              :rules="formRules"
              label-width="120px"
              class="application-form"
              :disabled="!!existingApplication"
            >
              <!-- 售后类型 -->
              <el-form-item label="售后类型" prop="type">
                <el-radio-group v-model="applicationForm.type" @change="handleTypeChange">
                  <el-radio
                    v-for="type in afterSaleTypes"
                    :key="type.value"
                    :label="type.value"
                    class="type-option"
                  >
                    <div class="type-content">
                      <h4>{{ type.label }}</h4>
                      <p>{{ type.description }}</p>
                    </div>
                  </el-radio>
                </el-radio-group>
              </el-form-item>

              <!-- 申请商品 -->
              <el-form-item label="申请商品" prop="items">
                <div class="product-selection">
                  <el-checkbox-group v-model="applicationForm.items">
                    <div class="product-list">
                      <div
                        v-for="item in orderInfo.items"
                        :key="item.id"
                        class="product-card"
                        :class="{ 'is-selected': applicationForm.items.includes(item.id) }"
                      >
                        <el-checkbox :label="item.id" class="product-checkbox">
                          <div class="product-card-content">
                            <div class="product-image-wrapper">
                              <SmartImage :src="item.image" :alt="item.name" image-class="product-image" />
                              <div class="product-check-overlay">
                                <el-icon class="check-icon"><Check /></el-icon>
                              </div>
                            </div>
                            <div class="product-card-info">
                              <h5 class="product-name">{{ item.name }}</h5>
                              <div class="product-specs">
                                <el-tag
                                  v-for="(value, key) in item.selectedSpecs"
                                  :key="key"
                                  size="small"
                                  class="spec-tag"
                                >
                                  {{ key }}: {{ value }}
                                </el-tag>
                              </div>
                              <div class="product-price">¥{{ item.price }} × {{ item.quantity }}</div>
                            </div>
                          </div>
                        </el-checkbox>
                      </div>
                    </div>
                  </el-checkbox-group>
                </div>
              </el-form-item>

              <!-- 申请原因 -->
              <el-form-item label="申请原因" prop="reason">
                <el-select
                  v-model="applicationForm.reason"
                  placeholder="请选择申请原因"
                  style="width: 100%"
                >
                  <el-option
                    v-for="reason in getReasonsByType(applicationForm.type)"
                    :key="reason.value"
                    :label="reason.label"
                    :value="reason.value"
                  />
                </el-select>
              </el-form-item>

              <!-- 详细描述 -->
              <el-form-item label="详细描述" prop="description">
                <el-input
                  v-model="applicationForm.description"
                  type="textarea"
                  placeholder="请详细描述问题情况，以便我们更好地为您处理"
                  :rows="4"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>

              <!-- 凭证上传 -->
              <el-form-item label="凭证上传">
                <div class="upload-section">
                  <el-upload
                    v-model:file-list="fileList"
                    action="#"
                    list-type="picture-card"
                    :on-preview="handlePictureCardPreview"
                    :on-remove="handleRemove"
                    :on-change="handleChange"
                    :before-upload="beforeUpload"
                    :limit="6"
                    accept="image/*"
                    :disabled="uploadingImages"
                  >
                    <el-icon><Plus /></el-icon>
                  </el-upload>
                  <div v-if="uploadingImages" class="upload-loading">
                    <el-icon class="is-loading"><Loading /></el-icon>
                    <span>上传中...</span>
                  </div>
                  
                  <div class="upload-tips">
                    <p>• 支持上传商品照片、聊天记录截图等</p>
                    <p>• 最多上传6张图片，每张不超过5MB</p>
                    <p>• 图片格式：JPG、PNG、GIF</p>
                  </div>
                </div>
              </el-form-item>

              <!-- 联系方式 -->
              <el-form-item label="联系方式" prop="contact">
                <el-input
                  v-model="applicationForm.contact"
                  placeholder="请输入您的手机号码，方便我们联系您"
                  maxlength="11"
                />
              </el-form-item>

              <!-- 期望处理方式 -->
              <el-form-item v-if="applicationForm.type === 'refund'" label="期望处理方式">
                <el-radio-group v-model="applicationForm.refundMethod">
                  <el-radio label="original">原路退回</el-radio>
                  <el-radio label="wallet">退回钱包余额</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>

        <!-- 右侧说明和进度 -->
        <el-col :span="8">
          <!-- 售后说明 -->
          <el-card class="instructions-card" shadow="hover">
            <template #header>
              <span>售后说明</span>
            </template>
            
            <div class="instructions">
              <h4>申请条件</h4>
              <ul>
                <li>订单完成7天内可申请售后</li>
                <li>商品需保持原包装完整</li>
                <li>提供有效的购买凭证</li>
              </ul>
              
              <h4>处理流程</h4>
              <ol>
                <li>提交售后申请</li>
                <li>商家审核（1-2个工作日）</li>
                <li>处理结果通知</li>
                <li>退款/补发/换货</li>
              </ol>
              
              <h4>注意事项</h4>
              <ul>
                <li>请如实描述问题情况</li>
                <li>上传清晰的凭证图片</li>
                <li>保持联系方式畅通</li>
              </ul>
            </div>
          </el-card>

          <!-- 售后进度 -->
          <el-card v-if="existingApplication" class="progress-card" shadow="hover">
            <template #header>
              <span>售后进度</span>
            </template>
            
            <el-timeline>
              <el-timeline-item
                v-for="step in applicationProgress"
                :key="step.id"
                :timestamp="step.timestamp"
                :type="getTimelineType(step.status)"
              >
                <div class="timeline-content">
                  <h4>{{ step.title }}</h4>
                  <p v-if="step.description">{{ step.description }}</p>
                </div>
              </el-timeline-item>
            </el-timeline>
          </el-card>
        </el-col>
      </el-row>

      <!-- 提交按钮 -->
      <div class="submit-section" v-if="!existingApplication">
        <el-button @click="handleCancel">取消</el-button>
        <el-button
          type="primary"
          size="large"
          :loading="submitting"
          @click="handleSubmit"
        >
          提交申请
        </el-button>
      </div>
    </div>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="previewVisible" title="图片预览">
      <img :src="previewUrl" style="width: 100%" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Loading, Check } from '@element-plus/icons-vue'
import { getOrderDetail } from '@/api/order'
import { getRefundByOrderId, submitRefundRequest } from '@/api/orderRefund'
import { uploadImage } from '@/api/upload'
import { getImageByIndex, getRandomImage } from '@/utils/imageLoader'

const router = useRouter()
const route = useRoute()

// 响应式数据
const formRef = ref()
const submitting = ref(false)
const loading = ref(false)
const previewVisible = ref(false)
const previewUrl = ref('')
const fileList = ref([])
const uploadingImages = ref(false)

// 订单信息
const orderInfo = ref({
  id: null,
  orderNumber: '',
  createTime: '',
  completeTime: '',
  items: []
})

// 售后类型
const afterSaleTypes = ref([
  {
    value: 'refund',
    label: '仅退款',
    description: '商品未收到或不需要退货，仅申请退款'
  },
  {
    value: 'return_refund',
    label: '退货退款',
    description: '商品需要退回，退款处理'
  },
  {
    value: 'resend',
    label: '补发',
    description: '商品漏发或损坏，申请重新发货'
  }
])

// 申请原因
const refundReasons = ref([
  { value: 'not_received', label: '未收到商品' },
  { value: 'wrong_product', label: '商品与描述不符' },
  { value: 'quality_issue', label: '商品质量问题' },
  { value: 'damaged', label: '商品破损' },
  { value: 'expired', label: '商品过期' },
  { value: 'other', label: '其他原因' }
])

const returnRefundReasons = ref([
  { value: 'wrong_product', label: '商品与描述不符' },
  { value: 'quality_issue', label: '商品质量问题' },
  { value: 'damaged', label: '商品破损' },
  { value: 'not_satisfied', label: '不满意' },
  { value: 'other', label: '其他原因' }
])

const resendReasons = ref([
  { value: 'missing', label: '商品漏发' },
  { value: 'damaged', label: '商品破损' },
  { value: 'wrong_product', label: '发错商品' },
  { value: 'other', label: '其他原因' }
])

// 申请表单
const applicationForm = ref({
  type: '',
  items: [],
  reason: '',
  description: '',
  contact: '',
  refundMethod: 'original'
})

// 表单验证规则
const formRules = ref({
  type: [
    { required: true, message: '请选择售后类型', trigger: 'change' }
  ],
  items: [
    { required: true, message: '请选择申请商品', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请选择申请原因', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请详细描述问题', trigger: 'blur' },
    { min: 10, message: '描述至少10个字符', trigger: 'blur' }
  ],
  contact: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
})

// 现有申请
const existingApplication = ref(null)

// 申请进度
const applicationProgress = ref([])

// 计算属性
const getReasonsByType = (type) => {
  switch (type) {
    case 'refund':
      return refundReasons.value
    case 'return_refund':
      return returnRefundReasons.value
    case 'resend':
      return resendReasons.value
    default:
      return []
  }
}

// 方法
const handleTypeChange = (type) => {
  applicationForm.value.reason = ''
  applicationForm.value.items = []
}

const handlePictureCardPreview = (file) => {
  previewUrl.value = file.url
  previewVisible.value = true
}

// 格式化订单数据
const formatOrderData = (order) => {
  const items = order.items || []
  const formattedItems = items.map(item => {
    // 处理商品规格（从后端的specs或specifications字段）
    let selectedSpecs = {}
    if (item.specs && typeof item.specs === 'object') {
      selectedSpecs = item.specs
    } else if (item.specifications && typeof item.specifications === 'object') {
      selectedSpecs = item.specifications
    } else if (item.specs && typeof item.specs === 'string') {
      try {
        selectedSpecs = JSON.parse(item.specs)
      } catch (e) {
        selectedSpecs = {}
      }
    }
    
    return {
      id: item.id || item.productId,
      productId: item.productId,
      name: item.productName || item.name || '',
      description: item.description || '',
      price: item.price || 0,
      quantity: item.quantity || 1,
      image: (() => {
        const img = item.productImage || item.image || ''
        if (!img || img === '/product-default.jpg' || img === 'undefined' || img === 'null') {
          return getImageByIndex(item.productId || item.id || 0) || getRandomImage() || '/product-default.jpg'
        }
        return img
      })(),
      selectedSpecs: selectedSpecs
    }
  })
  
  return {
    id: order.id,
    orderNumber: order.orderNo || order.orderNumber || '',
    createTime: order.createdAt ? new Date(order.createdAt).toLocaleString('zh-CN') : '',
    completeTime: order.deliveryTime ? new Date(order.deliveryTime).toLocaleString('zh-CN') : '',
    items: formattedItems
  }
}

// 格式化退款申请进度
const formatRefundProgress = (refund) => {
  const progress = []
  
  // 申请提交
  if (refund.applyTime) {
    progress.push({
      id: 1,
      status: 'submitted',
      title: '申请已提交',
      description: '您的售后申请已提交，等待商家审核',
      timestamp: new Date(refund.applyTime).toLocaleString('zh-CN')
    })
  }
  
  // 审核中
  if (refund.status === 'pending') {
    progress.push({
      id: 2,
      status: 'reviewing',
      title: '商家审核中',
      description: '商家正在审核您的申请',
      timestamp: refund.applyTime ? new Date(refund.applyTime).toLocaleString('zh-CN') : ''
    })
  }
  
  // 已同意
  if (refund.status === 'approved') {
    progress.push({
      id: 3,
      status: 'approved',
      title: '申请已同意',
      description: refund.processRemark || '商家已同意您的申请',
      timestamp: refund.processTime ? new Date(refund.processTime).toLocaleString('zh-CN') : ''
    })
  }
  
  // 已拒绝
  if (refund.status === 'rejected') {
    progress.push({
      id: 3,
      status: 'rejected',
      title: '申请已拒绝',
      description: refund.rejectReason || refund.processRemark || '商家拒绝了您的申请',
      timestamp: refund.processTime ? new Date(refund.processTime).toLocaleString('zh-CN') : ''
    })
  }
  
  // 已完成
  if (refund.status === 'completed') {
    progress.push({
      id: 4,
      status: 'completed',
      title: '退款已完成',
      description: '退款已处理完成',
      timestamp: refund.completeTime ? new Date(refund.completeTime).toLocaleString('zh-CN') : ''
    })
  }
  
  return progress
}

// 加载订单信息
const loadOrderInfo = async (orderId) => {
  try {
    loading.value = true
    const order = await getOrderDetail(orderId)
    orderInfo.value = formatOrderData(order)
  } catch (error) {
    console.error('加载订单信息失败:', error)
    ElMessage.error('加载订单信息失败')
    // 如果是404，尝试使用订单号查询
    if (error.response?.status === 404 && route.query.orderNo) {
      try {
        // 这里可以使用订单号查询接口（如果后端提供）
      } catch (e) {
        // 忽略
      }
    }
  } finally {
    loading.value = false
  }
}

// 加载现有退款申请
const loadExistingRefund = async (orderId) => {
  try {
    const refund = await getRefundByOrderId(orderId)
    if (refund) {
      existingApplication.value = refund
      applicationProgress.value = formatRefundProgress(refund)
    }
  } catch (error) {
    // 如果返回404，说明没有现有申请，这是正常的
    if (error.response?.status !== 404) {
      console.error('加载退款申请失败:', error)
    }
    existingApplication.value = null
    applicationProgress.value = []
  }
}

const handleRemove = (file) => {
  const index = fileList.value.findIndex(f => f.uid === file.uid)
  if (index > -1) {
    fileList.value.splice(index, 1)
  }
}

const handleChange = (file, fileList) => {
  // 处理文件变化
}

const beforeUpload = async (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  
  // 上传图片
  try {
    uploadingImages.value = true
    const result = await uploadImage(file, 'refund')
    
    // 处理返回的URL（可能是字符串或对象）
    const imageUrl = typeof result === 'string' ? result : (result?.url || result?.data?.url || result?.url)
    
    if (!imageUrl) {
      throw new Error('上传失败，未返回图片URL')
    }
    
    // 添加到文件列表
    fileList.value.push({
      uid: file.uid,
      name: file.name,
      status: 'success',
      url: imageUrl,
      raw: file
    })
    
    ElMessage.success('图片上传成功')
    return false // 阻止自动上传
  } catch (error) {
    console.error('上传图片失败:', error)
    ElMessage.error('图片上传失败，请重试')
    return false
  } finally {
    uploadingImages.value = false
  }
}

const getTimelineType = (status) => {
  const typeMap = {
    submitted: 'primary',
    reviewing: 'warning',
    approved: 'success',
    rejected: 'danger',
    completed: 'success'
  }
  return typeMap[status] || 'primary'
}

const getRefundStatusText = (status) => {
  const statusMap = {
    pending: '待处理',
    approved: '已同意',
    rejected: '已拒绝',
    completed: '已完成'
  }
  return statusMap[status] || '未知状态'
}

const handleCancel = () => {
  router.back()
}

// 映射售后类型到后端processType
const mapTypeToProcessType = (type) => {
  const typeMap = {
    refund: 'refund', // 仅退款
    return_refund: 'refund', // 退货退款
    resend: 'cancel' // 补发（可能需要取消原订单或单独处理）
  }
  return typeMap[type] || 'refund'
}

// 映射前端原因到后端原因
const mapReasonToBackend = (reason, type) => {
  // 后端原因可能与前端不完全一致，这里做映射
  // 如果后端直接接受前端原因值，可以返回原值
  return reason
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    if (!orderInfo.value.id) {
      ElMessage.error('订单信息加载失败，请刷新页面重试')
      return
    }
    
    await ElMessageBox.confirm(
      '确定要提交售后申请吗？',
      '确认提交',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    submitting.value = true

    // 准备附件数据
    const attachments = fileList.value
      .filter(file => file.status === 'success' && file.url)
      .map(file => ({
        name: file.name,
        url: file.url,
        type: 'image',
        size: file.raw?.size || 0
      }))

    // 构建退款申请数据
    const refundData = {
      orderId: orderInfo.value.id,
      processType: mapTypeToProcessType(applicationForm.value.type),
      reason: mapReasonToBackend(applicationForm.value.reason, applicationForm.value.type),
      detailReason: applicationForm.value.description,
      refundType: applicationForm.value.type === 'refund' ? 'full' : 'full', // 默认全额退款
      refundReason: applicationForm.value.reason,
      attachments: attachments.length > 0 ? attachments : null
    }
    
    // 如果是退款类型，计算退款金额
    if (applicationForm.value.type === 'refund' || applicationForm.value.type === 'return_refund') {
      // 计算选中商品的退款金额
      const selectedItems = orderInfo.value.items.filter(item => 
        applicationForm.value.items.includes(item.id)
      )
      refundData.refundAmount = selectedItems.reduce((sum, item) => {
        return sum + (item.price * item.quantity)
      }, 0)
    }

    try {
      await submitRefundRequest(refundData)
      ElMessage.success('售后申请已提交，请等待商家审核')
      router.push('/orders')
    } catch (error) {
      console.error('提交售后申请失败:', error)
      ElMessage.error('提交失败，请重试')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('表单验证失败:', error)
    }
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  // 根据路由参数获取订单信息
  const orderId = route.query.orderId
  if (orderId) {
    await loadOrderInfo(orderId)
    await loadExistingRefund(orderId)
  } else {
    ElMessage.warning('缺少订单ID参数')
    router.push('/orders')
  }
})
</script>

<style scoped>
.after-sale-container {
  padding: 32px 0;
  min-height: calc(100vh - 120px);
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
}

.after-sale-header {
  margin-bottom: 32px;
  text-align: center;
  padding: 24px 0;
}

.after-sale-header h2 {
  color: var(--color-text-primary);
  margin-bottom: 12px;
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-secondary) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.after-sale-header p {
  color: var(--color-text-secondary);
  margin: 0;
  font-size: var(--font-size-base);
}

.order-info-card,
.application-form-card {
  margin-bottom: 24px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  transition: all var(--transition-base);
}

.order-info-card:hover,
.application-form-card:hover {
  box-shadow: var(--shadow-elevated);
  transform: translateY(-2px);
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.order-basic {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
  background: linear-gradient(135deg, var(--color-primary-lighter) 0%, var(--color-secondary-lighter) 100%);
  border-radius: var(--radius-md);
  border-left: 4px solid var(--color-primary);
}

.info-item {
  display: flex;
  gap: 12px;
  align-items: center;
}

.label {
  color: var(--color-text-secondary);
  min-width: 100px;
  font-weight: var(--font-weight-medium);
}

.value {
  color: var(--color-text-primary);
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-base);
}

.order-items h4 {
  margin-bottom: 16px;
  color: var(--color-text-primary);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  padding-bottom: 12px;
  border-bottom: 2px solid var(--color-bg-gray-light);
}

.order-item {
  display: flex;
  align-items: center;
  padding: 16px;
  margin-bottom: 12px;
  background: var(--color-bg-white);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-bg-gray-light);
  gap: 16px;
  transition: all var(--transition-base);
}

.order-item:hover {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-md);
  transform: translateX(4px);
}

.order-item:last-child {
  margin-bottom: 0;
}

.item-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: var(--radius-md);
  border: 2px solid var(--color-bg-gray-light);
  transition: all var(--transition-base);
}

.order-item:hover .item-image {
  border-color: var(--color-primary);
  transform: scale(1.05);
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-info h5 {
  margin-bottom: 8px;
  color: var(--color-text-primary);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
}

.item-info p {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  margin-bottom: 8px;
  line-height: var(--line-height-normal);
}

.item-specs {
  margin-bottom: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.item-specs .el-tag {
  margin: 0;
  border-radius: var(--radius-sm);
}

.item-quantity {
  width: 50px;
  text-align: center;
  color: var(--color-text-secondary);
  font-weight: var(--font-weight-medium);
  padding: 8px 12px;
  background: var(--color-bg-gray-light);
  border-radius: var(--radius-sm);
}

.item-price {
  width: 80px;
  text-align: right;
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
  font-size: var(--font-size-lg);
}

.application-form {
  padding: 24px 0;
}

.type-option {
  display: block;
  margin-bottom: 16px;
  padding: 20px;
  border: 2px solid var(--color-bg-gray-light);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-base);
  background: var(--color-bg-white);
  position: relative;
  overflow: hidden;
}

.type-option::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: var(--color-primary);
  transform: scaleY(0);
  transition: transform var(--transition-base);
}

.type-option:hover {
  border-color: var(--color-primary);
  background: linear-gradient(135deg, var(--color-primary-lighter) 0%, transparent 100%);
  transform: translateX(4px);
  box-shadow: var(--shadow-md);
}

.type-option:hover::before {
  transform: scaleY(1);
}

.type-option.is-checked {
  border-color: var(--color-primary);
  background: linear-gradient(135deg, var(--color-primary-lighter) 0%, var(--color-secondary-lighter) 100%);
  box-shadow: var(--shadow-card);
}

.type-option.is-checked::before {
  transform: scaleY(1);
}

.type-content h4 {
  margin-bottom: 8px;
  color: var(--color-text-primary);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
}

.type-content p {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  margin: 0;
  line-height: var(--line-height-relaxed);
}

.product-selection {
  padding: 12px;
  background: var(--color-bg-white);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-bg-gray-light);
}

.product-list {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding: 8px 0;
  scroll-behavior: smooth;
}

.product-list::-webkit-scrollbar {
  height: 6px;
}

.product-list::-webkit-scrollbar-track {
  background: var(--color-bg-gray-light);
  border-radius: 3px;
}

.product-list::-webkit-scrollbar-thumb {
  background: var(--color-primary);
  border-radius: 3px;
}

.product-list::-webkit-scrollbar-thumb:hover {
  background: var(--color-primary-dark);
}

.product-card {
  flex: 0 0 auto;
  width: 200px;
  min-width: 180px;
  transition: all var(--transition-base);
}

.product-card:hover {
  transform: translateY(-4px);
}

.product-card.is-selected {
  position: relative;
}

.product-card.is-selected::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-secondary) 100%);
  border-radius: var(--radius-md);
  z-index: -1;
  opacity: 0.3;
}

.product-checkbox {
  width: 100%;
  height: 100%;
}

.product-checkbox :deep(.el-checkbox__input) {
  display: none;
}

.product-checkbox :deep(.el-checkbox__label) {
  padding: 0;
  width: 100%;
  cursor: pointer;
}

.product-card-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  border: 2px solid var(--color-bg-gray-light);
  border-radius: var(--radius-md);
  background: var(--color-bg-white);
  transition: all var(--transition-base);
  height: 100%;
  position: relative;
  overflow: hidden;
}

.product-card:hover .product-card-content,
.product-card.is-selected .product-card-content {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-md);
  background: linear-gradient(135deg, var(--color-primary-lighter) 0%, transparent 100%);
}

.product-image-wrapper {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--color-bg-gray-light);
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
}

.product-card:hover .product-image,
.product-card.is-selected .product-image {
  transform: scale(1.05);
}

.product-check-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all var(--transition-base);
  border-radius: var(--radius-md);
}

.product-card.is-selected .product-check-overlay {
  opacity: 1;
  background: rgba(64, 158, 255, 0.7);
}

.check-icon {
  font-size: 32px;
  color: white;
  font-weight: bold;
}

.product-card-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.product-name {
  margin: 0;
  color: var(--color-text-primary);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-specs {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.spec-tag {
  margin: 0;
  font-size: 11px;
  padding: 2px 6px;
  line-height: 1.4;
}

.product-price {
  color: var(--color-primary);
  font-weight: var(--font-weight-bold);
  font-size: var(--font-size-base);
  margin-top: auto;
}

.upload-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.upload-tips {
  padding: 16px;
  background: linear-gradient(135deg, var(--color-secondary-lighter) 0%, var(--color-primary-lighter) 100%);
  border-radius: var(--radius-md);
  border-left: 4px solid var(--color-secondary);
}

.upload-tips p {
  margin-bottom: 6px;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  line-height: var(--line-height-normal);
}

.upload-tips p:last-child {
  margin-bottom: 0;
}

.upload-tips p::before {
  content: '•';
  color: var(--color-secondary);
  font-weight: bold;
  margin-right: 8px;
}

.upload-loading {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 12px;
  background: var(--color-primary-lighter);
  border-radius: var(--radius-md);
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.instructions-card,
.progress-card {
  margin-bottom: 24px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  transition: all var(--transition-base);
}

.instructions-card:hover,
.progress-card:hover {
  box-shadow: var(--shadow-elevated);
  transform: translateY(-2px);
}

.instructions h4 {
  margin-bottom: 12px;
  color: var(--color-text-primary);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  padding-bottom: 8px;
  border-bottom: 2px solid var(--color-bg-gray-light);
  position: relative;
}

.instructions h4::before {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 60px;
  height: 2px;
  background: linear-gradient(90deg, var(--color-primary) 0%, var(--color-secondary) 100%);
}

.instructions ul,
.instructions ol {
  margin-bottom: 20px;
  padding-left: 24px;
}

.instructions li {
  margin-bottom: 8px;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  line-height: var(--line-height-relaxed);
  position: relative;
}

.instructions ul li::marker {
  color: var(--color-primary);
}

.instructions ol li::marker {
  color: var(--color-secondary);
  font-weight: var(--font-weight-semibold);
}

.instructions li:last-child {
  margin-bottom: 0;
}

.timeline-content h4 {
  margin-bottom: 8px;
  color: var(--color-text-primary);
  font-weight: var(--font-weight-semibold);
}

.timeline-content p {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  line-height: var(--line-height-normal);
}

.submit-section {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 48px;
  padding: 32px 0;
  border-top: 2px solid var(--color-bg-gray-light);
  background: linear-gradient(135deg, var(--color-bg-white) 0%, var(--color-primary-lighter) 100%);
  border-radius: var(--radius-lg);
  margin-left: -20px;
  margin-right: -20px;
  padding-left: 20px;
  padding-right: 20px;
}

.submit-section .el-button {
  min-width: 120px;
  height: 44px;
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
}

.submit-section .el-button--primary {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-secondary) 100%);
  border: none;
  box-shadow: var(--shadow-md);
}

.submit-section .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-elevated);
}

.submit-section .el-button--default:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  transform: translateY(-2px);
}

/* 卡片头部样式优化 */
:deep(.el-card__header) {
  background: linear-gradient(135deg, var(--color-primary-lighter) 0%, var(--color-secondary-lighter) 100%);
  border-bottom: 2px solid var(--color-bg-gray-light);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  padding: 16px 20px;
}

:deep(.el-card__body) {
  padding: 24px;
}

/* 表单样式优化 */
:deep(.el-form-item__label) {
  color: var(--color-text-primary);
  font-weight: var(--font-weight-medium);
}

:deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

:deep(.el-textarea__inner) {
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
}

:deep(.el-textarea__inner:focus) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: var(--radius-md);
}

/* 上传组件样式优化 */
:deep(.el-upload--picture-card) {
  border-radius: var(--radius-md);
  border: 2px dashed var(--color-bg-gray-light);
  transition: all var(--transition-base);
}

:deep(.el-upload--picture-card:hover) {
  border-color: var(--color-primary);
  background: var(--color-primary-lighter);
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  border-radius: var(--radius-md);
  border: 2px solid var(--color-bg-gray-light);
  transition: all var(--transition-base);
}

:deep(.el-upload-list--picture-card .el-upload-list__item:hover) {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-md);
}

/* 时间线样式优化 */
:deep(.el-timeline-item__node) {
  background-color: var(--color-primary);
  border-color: var(--color-primary);
}

:deep(.el-timeline-item__timestamp) {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

/* 警告提示样式优化 */
:deep(.el-alert) {
  border-radius: var(--radius-md);
  border-left: 4px solid var(--color-info);
}

@media (max-width: 768px) {
  .after-sale-container {
    padding: 16px 0;
  }
  
  .after-sale-header {
    margin-bottom: 24px;
    padding: 16px 0;
  }
  
  .after-sale-header h2 {
    font-size: var(--font-size-2xl);
  }
  
  .order-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    padding: 12px;
  }
  
  .item-image {
    width: 60px;
    height: 60px;
  }
  
  .item-quantity,
  .item-price {
    width: auto;
    align-self: flex-end;
  }
  
  .type-option {
    padding: 16px;
  }
  
  .product-list {
    gap: 12px;
    padding: 4px 0;
  }
  
  .product-card {
    width: 160px;
    min-width: 140px;
  }
  
  .product-card-content {
    padding: 12px;
  }
  
  .product-name {
    font-size: var(--font-size-sm);
  }
  
  .spec-tag {
    font-size: 10px;
    padding: 1px 4px;
  }
  
  .product-price {
    font-size: var(--font-size-sm);
  }
  
  .check-icon {
    font-size: 24px;
  }
  
  .instructions-card,
  .progress-card {
    position: static;
    margin-top: 20px;
  }
  
  .submit-section {
    flex-direction: column;
    gap: 12px;
    padding: 24px 16px;
    margin-left: -16px;
    margin-right: -16px;
    padding-left: 16px;
    padding-right: 16px;
  }
  
  .submit-section .el-button {
    width: 100%;
  }
}
</style>
