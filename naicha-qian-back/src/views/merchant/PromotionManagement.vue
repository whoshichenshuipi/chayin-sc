<template>
  <div class="page-container">
    <div class="page-header">
      <h2>促销活动管理</h2>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        创建活动
      </el-button>
    </div>

    <!-- 促销活动列表 -->
    <el-card>
      <div class="table-header">
        <div class="search-bar">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索活动名称"
            style="width: 300px"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select v-model="typeFilter" placeholder="活动类型" style="width: 150px; margin-left: 10px">
            <el-option label="全部" value="" />
            <el-option label="限时折扣" value="discount" />
            <el-option label="满减活动" value="full_reduce" />
            <el-option label="买一送一" value="buy_one_get_one" />
            <el-option label="第二件半价" value="second_half_price" />
          </el-select>
          <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 150px; margin-left: 10px" @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="未开始" value="pending" />
            <el-option label="进行中" value="active" />
            <el-option label="已暂停" value="paused" />
            <el-option label="已结束" value="ended" />
          </el-select>
          <el-button type="primary" style="margin-left: 10px" @click="handleSearch">搜索</el-button>
        </div>
      </div>

      <el-table :data="promotions" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="活动名称" width="200" />
        <el-table-column prop="type" label="活动类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getPromotionTypeTag(row.type)">
              {{ getPromotionTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="活动描述" width="250">
          <template #default="{ row }">
            <div class="activity-desc">{{ getActivityDescription(row) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="participantCount" label="参与人数" width="100" />
        <el-table-column prop="orderCount" label="活动订单" width="100" />
        <el-table-column prop="salesAmount" label="活动销售额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ (row.salesAmount || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="discountAmount" label="优惠金额" width="120">
          <template #default="{ row }">
            <span class="amount discount">¥{{ (row.discountAmount || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="timeRange" label="活动时间" width="200">
          <template #default="{ row }">
            <div>{{ formatDateTime(row.startTime) }}</div>
            <div>{{ formatDateTime(row.endTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewAnalytics(row)">数据</el-button>
            <el-button size="small" @click="editPromotion(row)">编辑</el-button>
            <el-button 
              size="small" 
              :type="row.status === 'paused' ? 'success' : 'warning'"
              @click="togglePause(row)"
              :disabled="row.status === 'ended'"
            >
              {{ row.status === 'paused' ? '恢复' : '暂停' }}
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="endPromotion(row)"
              :disabled="row.status === 'ended'"
            >
              结束
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑促销活动对话框 -->
    <el-dialog 
      v-model="showCreateDialog" 
      :title="editingPromotion ? '编辑促销活动' : '创建促销活动'"
      width="700px"
      @close="resetForm"
    >
      <el-form 
        ref="promotionFormRef"
        :model="promotionForm" 
        :rules="promotionRules" 
        label-width="120px"
      >
        <el-form-item label="活动名称" prop="name">
          <el-input v-model="promotionForm.name" placeholder="请输入活动名称" />
        </el-form-item>

        <el-form-item label="活动类型" prop="type">
          <el-radio-group v-model="promotionForm.type" @change="onTypeChange">
            <el-radio value="discount">限时折扣</el-radio>
            <el-radio value="full_reduce">满减活动</el-radio>
            <el-radio value="buy_one_get_one">买一送一</el-radio>
            <el-radio value="second_half_price">第二件半价</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="活动时间" prop="timeRange">
          <el-date-picker
            v-model="promotionForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
          />
        </el-form-item>

        <!-- 限时折扣配置 -->
        <template v-if="promotionForm.type === 'discount'">
          <el-form-item label="活动商品" prop="productIds">
            <el-select
              v-model="promotionForm.productIds"
              multiple
              placeholder="请选择活动商品"
              style="width: 100%"
            >
              <el-option
                v-for="product in productList"
                :key="product.id"
                :label="product.name"
                :value="product.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="折扣比例" prop="discountRate">
            <el-input-number 
              v-model="promotionForm.discountRate" 
              :min="0.1" 
              :max="9.9" 
              :precision="1"
              style="width: 200px"
            />
            <span style="margin-left: 10px; color: #666;">折（如8表示8折）</span>
          </el-form-item>
        </template>

        <!-- 满减活动配置 -->
        <template v-if="promotionForm.type === 'full_reduce'">
          <el-form-item label="活动商品" prop="productIds">
            <el-select
              v-model="promotionForm.productIds"
              multiple
              placeholder="请选择活动商品（可选，用于显示和筛选）"
              style="width: 100%"
              clearable
            >
              <el-option
                v-for="product in productList"
                :key="product.id"
                :label="product.name"
                :value="product.id"
              />
            </el-select>
            <div style="font-size: 12px; color: #999; margin-top: 4px;">
              提示：满减活动可以绑定商品用于显示，但不影响单个商品价格
            </div>
          </el-form-item><el-form-item label="满减规则" prop="fullReduceRules">
            <div class="full-reduce-rules">
              <div 
                v-for="(rule, index) in promotionForm.fullReduceRules" 
                :key="index"
                class="rule-item"
              >
                <el-input-number 
                  v-model="rule.fullAmount" 
                  :min="0" 
                  :precision="2"
                  placeholder="满"
                  style="width: 120px"
                />
                <span style="margin: 0 10px;">减</span>
                <el-input-number 
                  v-model="rule.reduceAmount" 
                  :min="0" 
                  :precision="2"
                  placeholder="减"
                  style="width: 120px"
                />
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="removeFullReduceRule(index)"
                  style="margin-left: 10px;"
                >
                  删除
                </el-button>
              </div>
              <el-button type="primary" size="small" @click="addFullReduceRule">
                添加规则
              </el-button>
            </div>
          </el-form-item>
          <el-form-item label="叠加优惠券">
            <el-switch v-model="promotionForm.allowCoupon" />
            <span style="margin-left: 10px; color: #666;">是否允许叠加平台优惠券</span>
          </el-form-item>
        </template>

        <!-- 买一送一配置 -->
        <template v-if="promotionForm.type === 'buy_one_get_one'">
          <el-form-item label="活动商品" prop="productIds">
            <el-select
              v-model="promotionForm.productIds"
              multiple
              placeholder="请选择活动商品"
              style="width: 100%"
            >
              <el-option
                v-for="product in productList"
                :key="product.id"
                :label="product.name"
                :value="product.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="赠送方式" prop="giftType">
            <el-radio-group v-model="promotionForm.giftType">
              <el-radio value="same">买A送A（同款）</el-radio>
              <el-radio value="different">买A送B（指定商品）</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item 
            v-if="promotionForm.giftType === 'different'" 
            label="赠送商品" 
            prop="giftProductIds"
          >
            <el-select
              v-model="promotionForm.giftProductIds"
              multiple
              placeholder="请选择赠送商品"
              style="width: 100%"
            >
              <el-option
                v-for="product in productList"
                :key="product.id"
                :label="product.name"
                :value="product.id"
              />
            </el-select>
          </el-form-item>
        </template>

        <!-- 第二件半价配置 -->
        <template v-if="promotionForm.type === 'second_half_price'">
          <el-form-item label="活动商品" prop="productIds">
            <el-select
              v-model="promotionForm.productIds"
              multiple
              placeholder="请选择活动商品"
              style="width: 100%"
            >
              <el-option
                v-for="product in productList"
                :key="product.id"
                :label="product.name"
                :value="product.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="参与限制" prop="participationLimit">
            <el-input-number 
              v-model="promotionForm.participationLimit" 
              :min="1" 
              style="width: 200px"
            />
            <span style="margin-left: 10px; color: #666;">次（每个消费者限参与次数，0表示不限制）</span>
          </el-form-item>
        </template>

        <el-form-item label="活动描述">
          <el-input
            v-model="promotionForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入活动描述"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitPromotion">
          {{ editingPromotion ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 活动数据分析对话框 -->
    <el-dialog v-model="showAnalyticsDialog" title="活动数据分析" width="900px">
      <div v-if="selectedPromotion" class="analytics-content">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ selectedPromotion.participantCount }}</div>
                <div class="stat-label">参与人数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ selectedPromotion.orderCount }}</div>
                <div class="stat-label">活动订单</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-item">
                <div class="stat-value">¥{{ (selectedPromotion.salesAmount || 0).toFixed(2) }}</div>
                <div class="stat-label">活动销售额</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-item">
                <div class="stat-value">¥{{ (selectedPromotion.discountAmount || 0).toFixed(2) }}</div>
                <div class="stat-label">优惠金额</div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <div class="analytics-details" style="margin-top: 20px;">
          <h4>活动详情</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="活动名称">{{ selectedPromotion.name }}</el-descriptions-item>
            <el-descriptions-item label="活动类型">{{ getPromotionTypeText(selectedPromotion.type) }}</el-descriptions-item>
            <el-descriptions-item label="活动时间" :span="2">
              {{ formatDateTime(selectedPromotion.startTime) }} 至 {{ formatDateTime(selectedPromotion.endTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="活动状态">
              <el-tag :type="getStatusTag(selectedPromotion.status)">
                {{ getStatusText(selectedPromotion.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="活动描述" :span="2">{{ selectedPromotion.description || '无' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="order-details" style="margin-top: 20px;">
          <h4>活动订单明细</h4>
          <el-table :data="selectedPromotion.orderDetails || []" style="width: 100%">
            <el-table-column prop="orderId" label="订单号" width="150" />
            <el-table-column prop="userName" label="用户" width="120" />
            <el-table-column prop="orderTime" label="下单时间" width="150">
              <template #default="{ row }">
                {{ formatDateTime(row.orderTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="originalAmount" label="原价" width="100">
              <template #default="{ row }">
                ¥{{ (row.originalAmount || 0).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="discountAmount" label="优惠金额" width="100">
              <template #default="{ row }">
                ¥{{ (row.discountAmount || 0).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="finalAmount" label="实付金额" width="100">
              <template #default="{ row }">
                ¥{{ (row.finalAmount || 0).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="订单状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'completed' ? 'success' : 'warning'">
                  {{ row.status === 'completed' ? '已完成' : '进行中' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  createPromotion,
  updatePromotion,
  deletePromotion as deletePromotionApi,
  getPromotionDetail,
  getPromotionList,
  togglePausePromotion,
  endPromotion as endPromotionApi
} from '@/api/promotion'
import { getProductList } from '@/api/product'

// 响应式数据
const showCreateDialog = ref(false)
const showAnalyticsDialog = ref(false)
const editingPromotion = ref(null)
const selectedPromotion = ref(null)
const searchKeyword = ref('')
const typeFilter = ref('')
const statusFilter = ref('')
const promotionFormRef = ref()
const loading = ref(false)

// 表单数据
const promotionForm = reactive({
  name: '',
  type: 'discount',
  timeRange: [],
  productIds: [],
  discountRate: 8,
  fullReduceRules: [{ fullAmount: 0, reduceAmount: 0 }],
  allowCoupon: true,
  giftType: 'same',
  giftProductIds: [],
  participationLimit: 0,
  description: ''
})

// 表单验证规则
const promotionRules = {
  name: [
    { required: true, message: '请输入活动名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择活动类型', trigger: 'change' }
  ],
  timeRange: [
    { required: true, message: '请选择活动时间', trigger: 'change' }
  ],
  productIds: [
    // 满减活动不强制要求选择商品，其他类型需要
    { 
      validator: (rule, value, callback) => {
        if (promotionForm.type !== 'full_reduce' && (!value || value.length === 0)) {
          callback(new Error('请选择活动商品'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ],
  discountRate: [
    { required: true, message: '请输入折扣比例', trigger: 'blur' }
  ],
  fullReduceRules: [
    { required: true, message: '请设置满减规则', trigger: 'change' }
  ],
  giftType: [
    { required: true, message: '请选择赠送方式', trigger: 'change' }
  ],
  participationLimit: [
    { required: true, message: '请输入参与限制', trigger: 'blur' }
  ]
}

// 促销活动列表
const promotions = ref([])

// 商品列表
const productList = ref([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 搜索和筛选变化时重新加载数据
const handleSearch = () => {
  pagination.currentPage = 1
  loadPromotions()
}

// 方法
const getPromotionTypeText = (type) => {
  const typeMap = {
    discount: '限时折扣',
    full_reduce: '满减活动',
    buy_one_get_one: '买一送一',
    second_half_price: '第二件半价'
  }
  return typeMap[type] || type
}

const getPromotionTypeTag = (type) => {
  const tagMap = {
    discount: 'success',
    full_reduce: 'warning',
    buy_one_get_one: 'primary',
    second_half_price: 'info'
  }
  return tagMap[type] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    pending: '未开始',
    active: '进行中',
    paused: '已暂停',
    ended: '已结束'
  }
  return statusMap[status] || status
}

const getStatusTag = (status) => {
  const tagMap = {
    pending: 'info',
    active: 'success',
    paused: 'warning',
    ended: 'danger'
  }
  return tagMap[status] || 'info'
}

const getActivityDescription = (promotion) => {
  switch (promotion.type) {
    case 'discount':
      return promotion.discountRate ? `${promotion.discountRate}折优惠` : '限时折扣'
    case 'full_reduce':
      if (promotion.fullReduceRules && promotion.fullReduceRules.length > 0) {
        return promotion.fullReduceRules.map(rule => `满${rule.fullAmount}减${rule.reduceAmount}`).join('，')
      }
      return '满减活动'
    case 'buy_one_get_one':
      return promotion.giftType === 'same' ? '买A送A' : '买A送B'
    case 'second_half_price':
      return '第二件半价'
    default:
      return promotion.description || ''
  }
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  if (typeof dateTime === 'string') {
    // 如果是ISO格式或包含T的格式，转换为显示格式
    return dateTime.replace('T', ' ')
  }
  return dateTime
}

const onTypeChange = () => {
  // 重置相关字段
  promotionForm.productIds = []
  promotionForm.discountRate = 8
  promotionForm.fullReduceRules = [{ fullAmount: 0, reduceAmount: 0 }]
  promotionForm.giftType = 'same'
  promotionForm.giftProductIds = []
  promotionForm.participationLimit = 0
}

const addFullReduceRule = () => {
  promotionForm.fullReduceRules.push({ fullAmount: 0, reduceAmount: 0 })
}

const removeFullReduceRule = (index) => {
  if (promotionForm.fullReduceRules.length > 1) {
    promotionForm.fullReduceRules.splice(index, 1)
  }
}

const resetForm = () => {
  Object.assign(promotionForm, {
    name: '',
    type: 'discount',
    timeRange: [],
    productIds: [],
    discountRate: 8,
    fullReduceRules: [{ fullAmount: 0, reduceAmount: 0 }],
    allowCoupon: true,
    giftType: 'same',
    giftProductIds: [],
    participationLimit: 0,
    description: ''
  })
  editingPromotion.value = null
  promotionFormRef.value?.clearValidate()
}

const submitPromotion = async () => {
  try {
    await promotionFormRef.value.validate()
    
    loading.value = true
    
    // 格式化时间数据
    const formatTimeForRequest = (timeArray) => {
      if (!timeArray || timeArray.length !== 2) return []
      return timeArray.map(time => {
        if (time instanceof Date) {
          return time.toISOString().substring(0, 19)
        }
        if (typeof time === 'string') {
          return time.replace(' ', 'T').substring(0, 19)
        }
        return time
      })
    }
    
    // 构建请求数据
    const requestData = {
      name: promotionForm.name,
      type: promotionForm.type,
      timeRange: formatTimeForRequest(promotionForm.timeRange),
      description: promotionForm.description
    }
    
    // 根据活动类型添加相应字段
    if (promotionForm.type === 'discount') {
      requestData.productIds = promotionForm.productIds
      requestData.discountRate = promotionForm.discountRate
    } else if (promotionForm.type === 'full_reduce') {
      requestData.fullReduceRules = promotionForm.fullReduceRules
      requestData.allowCoupon = promotionForm.allowCoupon
      // 满减活动也可以绑定商品（用于显示和筛选）
      if (promotionForm.productIds && promotionForm.productIds.length > 0) {
        requestData.productIds = promotionForm.productIds
      }
    } else if (promotionForm.type === 'buy_one_get_one') {
      requestData.productIds = promotionForm.productIds
      requestData.giftType = promotionForm.giftType
      if (promotionForm.giftType === 'different') {
        requestData.giftProductIds = promotionForm.giftProductIds
      }
    } else if (promotionForm.type === 'second_half_price') {
      requestData.productIds = promotionForm.productIds
      requestData.participationLimit = promotionForm.participationLimit || 0
    }
    
    if (editingPromotion.value) {
      // 更新活动
      await updatePromotion(editingPromotion.value.id, requestData)
      ElMessage.success('活动更新成功')
    } else {
      // 创建活动
      await createPromotion(requestData)
      ElMessage.success('活动创建成功')
    }
    
    showCreateDialog.value = false
    resetForm()
    loadPromotions()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败，请重试')
  } finally {
    loading.value = false
  }
}

const editPromotion = async (promotion) => {
  try {
    loading.value = true
    // 重新获取详情以获取完整数据
    const response = await getPromotionDetail(promotion.id)
    const promotionDetail = response.data
    
    editingPromotion.value = promotionDetail
    
    // 转换时间格式
    const startTime = promotionDetail.startTime ? 
      (promotionDetail.startTime.includes('T') ? promotionDetail.startTime : promotionDetail.startTime.replace(' ', 'T')) : null
    const endTime = promotionDetail.endTime ? 
      (promotionDetail.endTime.includes('T') ? promotionDetail.endTime : promotionDetail.endTime.replace(' ', 'T')) : null
    
    Object.assign(promotionForm, {
      name: promotionDetail.name,
      type: promotionDetail.type,
      timeRange: startTime && endTime ? [new Date(startTime), new Date(endTime)] : [],
      productIds: promotionDetail.productIds || [],
      discountRate: promotionDetail.discountRate || 8,
      fullReduceRules: promotionDetail.fullReduceRules && promotionDetail.fullReduceRules.length > 0 
        ? promotionDetail.fullReduceRules 
        : [{ fullAmount: 0, reduceAmount: 0 }],
      allowCoupon: promotionDetail.allowCoupon !== false,
      giftType: promotionDetail.giftType || 'same',
      giftProductIds: promotionDetail.giftProductIds || [],
      participationLimit: promotionDetail.participationLimit || 0,
      description: promotionDetail.description || ''
    })
    showCreateDialog.value = true
  } catch (error) {
    console.error('获取活动详情失败:', error)
    ElMessage.error('获取活动详情失败')
  } finally {
    loading.value = false
  }
}

const viewAnalytics = async (promotion) => {
  try {
    loading.value = true
    const response = await getPromotionDetail(promotion.id)
    selectedPromotion.value = response.data
    showAnalyticsDialog.value = true
  } catch (error) {
    console.error('获取活动详情失败:', error)
    ElMessage.error('获取活动详情失败')
  } finally {
    loading.value = false
  }
}

const togglePause = async (promotion) => {
  const action = promotion.status === 'paused' ? '恢复' : '暂停'
  try {
    await ElMessageBox.confirm(
      `确定要${action}活动"${promotion.name}"吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    loading.value = true
    await togglePausePromotion(promotion.id)
    ElMessage.success(`活动${action}成功`)
    loadPromotions()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error(error.response?.data?.message || '操作失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

const endPromotion = async (promotion) => {
  try {
    await ElMessageBox.confirm(
      `确定要结束活动"${promotion.name}"吗？结束后将无法恢复！`,
      '确认结束',
      {
        confirmButtonText: '确定结束',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    loading.value = true
    await endPromotionApi(promotion.id)
    ElMessage.success('活动已结束')
    loadPromotions()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error(error.response?.data?.message || '操作失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

// 加载促销活动列表
const loadPromotions = async () => {
  try {
    loading.value = true
    const params = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      name: searchKeyword.value || undefined,
      type: typeFilter.value || undefined,
      status: statusFilter.value || undefined
    }
    
    const response = await getPromotionList(params)
    if (response.data) {
      promotions.value = response.data.records || []
      pagination.total = response.data.total || 0
    }
  } catch (error) {
    console.error('加载促销活动列表失败:', error)
    ElMessage.error('加载数据失败，请重试')
  } finally {
    loading.value = false
  }
}

// 加载商品列表
const loadProducts = async () => {
  try {
    const response = await getProductList({ pageNum: 1, pageSize: 100 })
    if (response.data && response.data.records) {
      productList.value = response.data.records
    } else if (Array.isArray(response.data)) {
      productList.value = response.data
    }
  } catch (error) {
    console.error('加载商品列表失败:', error)
  }
}

onMounted(() => {
  loadPromotions()
  loadProducts()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.table-header {
  margin-bottom: 20px;
}

.search-bar {
  display: flex;
  align-items: center;
}

.activity-desc {
  font-size: 12px;
  color: #666;
  line-height: 1.4;
}

.amount {
  font-weight: bold;
  color: #67c23a;
}

.amount.discount {
  color: #f56c6c;
}

.full-reduce-rules {
  width: 100%;
}

.rule-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.stat-card {
  text-align: center;
}

.stat-item {
  padding: 20px 0;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.analytics-content {
  padding: 10px 0;
}

.analytics-details h4,
.order-details h4 {
  margin: 0 0 15px 0;
  color: #303133;
}
</style>
