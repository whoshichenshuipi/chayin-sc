<template>
  <div class="page-container">
    <div class="page-header">
      <h2>优惠券管理</h2>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        创建优惠券
      </el-button>
    </div>

    <!-- 优惠券列表 -->
    <el-card>
      <div class="table-header">
        <div class="search-bar">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索优惠券名称"
            style="width: 300px"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select 
            v-model="statusFilter" 
            placeholder="状态筛选" 
            style="width: 150px; margin-left: 10px"
            @change="handleStatusFilterChange"
          >
            <el-option label="全部" value="" />
            <el-option label="未开始" value="pending" />
            <el-option label="进行中" value="active" />
            <el-option label="已暂停" value="paused" />
            <el-option label="已结束" value="ended" />
          </el-select>
          <el-button type="primary" style="margin-left: 10px" @click="handleSearch">搜索</el-button>
        </div>
      </div>

      <el-table :data="filteredCoupons" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="优惠券名称" width="200" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getCouponTypeTag(row.type)">
              {{ getCouponTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="discount" label="优惠内容" width="150">
          <template #default="{ row }">
            <span v-if="row.type === 'cash'">{{ row.discount }}元</span>
            <span v-else-if="row.type === 'discount'">{{ row.discount }}折</span>
            <span v-else>满{{ row.threshold }}减{{ row.discount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="threshold" label="使用门槛" width="120">
          <template #default="{ row }">
            <span v-if="row.threshold > 0">满{{ row.threshold }}元</span>
            <span v-else>无门槛</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalQuantity" label="发放数量" width="100" />
        <el-table-column prop="remainingQuantity" label="剩余数量" width="100" />
        <el-table-column prop="receivedCount" label="领取人数" width="100" />
        <el-table-column prop="usedCount" label="使用人数" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="validPeriod" label="有效期" width="200">
          <template #default="{ row }">
            <div>{{ formatDateTime(row.validStartTime) }}</div>
            <div>{{ formatDateTime(row.validEndTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetails(row)">详情</el-button>
            <el-button size="small" @click="editCoupon(row)">编辑</el-button>
            <el-button 
              size="small" 
              :type="row.status === 'paused' ? 'success' : 'warning'"
              @click="togglePause(row)"
            >
              {{ row.status === 'paused' ? '恢复' : '暂停' }}
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="deleteCoupon(row)"
              :disabled="!canDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑优惠券对话框 -->
    <el-dialog 
      v-model="showCreateDialog" 
      :title="editingCoupon ? '编辑优惠券' : '创建优惠券'"
      width="600px"
      @close="resetForm"
    >
      <el-form 
        ref="couponFormRef"
        :model="couponForm" 
        :rules="couponRules" 
        label-width="120px"
      >
        <el-form-item label="优惠券名称" prop="name">
          <el-input v-model="couponForm.name" placeholder="请输入优惠券名称" />
        </el-form-item>

        <el-form-item label="优惠券类型" prop="type">
          <el-radio-group v-model="couponForm.type">
            <el-radio value="cash">现金券</el-radio>
            <el-radio value="discount">折扣券</el-radio>
            <el-radio value="reduce">满减券</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="使用门槛" prop="threshold">
          <el-input-number 
            v-model="couponForm.threshold" 
            :min="0" 
            :precision="2"
            placeholder="0表示无门槛"
            style="width: 200px"
          />
          <span style="margin-left: 10px; color: #666;">元（0表示无门槛）</span>
        </el-form-item>

        <el-form-item 
          :label="getDiscountLabel()" 
          prop="discount"
        >
          <el-input-number 
            v-model="couponForm.discount" 
            :min="0" 
            :precision="2"
            :max="couponForm.type === 'discount' ? 10 : undefined"
            style="width: 200px"
          />
          <span style="margin-left: 10px; color: #666;">
            {{ getDiscountUnit() }}
          </span>
        </el-form-item>

        <el-form-item label="发放数量" prop="totalQuantity">
          <el-input-number 
            v-model="couponForm.totalQuantity" 
            :min="1" 
            style="width: 200px"
          />
          <span style="margin-left: 10px; color: #666;">张</span>
        </el-form-item>

        <el-form-item label="领取时间" prop="receiveTime">
          <el-date-picker
            v-model="couponForm.receiveTime"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="使用有效期" prop="validTime">
          <el-date-picker
            v-model="couponForm.validTime"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="适用商品" prop="productScope">
          <el-radio-group v-model="couponForm.productScope">
            <el-radio value="all">全店通用</el-radio>
            <el-radio value="specific">指定商品</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item 
          v-if="couponForm.productScope === 'specific'" 
          label="选择商品" 
          prop="productIds"
        >
          <el-select
            v-model="couponForm.productIds"
            multiple
            placeholder="请选择适用商品"
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

        <el-form-item label="优惠券描述">
          <el-input
            v-model="couponForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入优惠券描述"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitCoupon">
          {{ editingCoupon ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 优惠券详情对话框 -->
    <el-dialog v-model="showDetailsDialog" title="优惠券详情" width="800px">
      <div v-if="selectedCoupon" class="coupon-details">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="优惠券名称">{{ selectedCoupon.name }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ getCouponTypeText(selectedCoupon.type) }}</el-descriptions-item>
          <el-descriptions-item label="使用门槛">
            {{ selectedCoupon.threshold > 0 ? `满${selectedCoupon.threshold}元` : '无门槛' }}
          </el-descriptions-item>
          <el-descriptions-item label="优惠内容">
            <span v-if="selectedCoupon.type === 'cash'">{{ selectedCoupon.discount }}元</span>
            <span v-else-if="selectedCoupon.type === 'discount'">{{ selectedCoupon.discount }}折</span>
            <span v-else>满{{ selectedCoupon.threshold }}减{{ selectedCoupon.discount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="发放数量">{{ selectedCoupon.totalQuantity }}张</el-descriptions-item>
          <el-descriptions-item label="剩余数量">{{ selectedCoupon.remainingQuantity }}张</el-descriptions-item>
          <el-descriptions-item label="领取人数">{{ selectedCoupon.receivedCount }}人</el-descriptions-item>
          <el-descriptions-item label="使用人数">{{ selectedCoupon.usedCount }}人</el-descriptions-item>
          <el-descriptions-item label="领取时间" :span="2">
            {{ formatDateTime(selectedCoupon.receiveStartTime) }} 至 {{ formatDateTime(selectedCoupon.receiveEndTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="使用有效期" :span="2">
            {{ formatDateTime(selectedCoupon.validStartTime) }} 至 {{ formatDateTime(selectedCoupon.validEndTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="适用商品" :span="2">
            {{ selectedCoupon.productScope === 'all' ? '全店通用' : '指定商品' }}
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ selectedCoupon.description || '无' }}</el-descriptions-item>
        </el-descriptions>

        <div class="usage-details" style="margin-top: 20px;">
          <h4>使用明细</h4>
          <el-table :data="selectedCoupon.usageDetails" style="width: 100%">
            <el-table-column prop="userName" label="用户" width="120" />
            <el-table-column prop="receivedTime" label="领取时间" width="150">
              <template #default="{ row }">
                {{ formatDateTime(row.receivedTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="usedTime" label="使用时间" width="150">
              <template #default="{ row }">
                {{ row.usedTime ? formatDateTime(row.usedTime) : '未使用' }}
              </template>
            </el-table-column>
            <el-table-column prop="orderNo" label="订单号" width="150">
              <template #default="{ row }">
                {{ row.orderNo || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.usedTime ? 'success' : 'info'">
                  {{ row.usedTime ? '已使用' : '未使用' }}
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
  createCoupon, 
  updateCoupon, 
  deleteCoupon as deleteCouponApi, 
  getCouponDetail, 
  getCouponList, 
  togglePauseCoupon 
} from '@/api/coupon'
import { getProductList } from '@/api/product'

// 响应式数据
const showCreateDialog = ref(false)
const showDetailsDialog = ref(false)
const editingCoupon = ref(null)
const selectedCoupon = ref(null)
const searchKeyword = ref('')
const statusFilter = ref('')
const couponFormRef = ref()
const loading = ref(false)

// 表单数据
const couponForm = reactive({
  name: '',
  type: 'cash',
  threshold: 0,
  discount: 0,
  totalQuantity: 100,
  receiveTime: [],
  validTime: [],
  productScope: 'all',
  productIds: [],
  description: ''
})

// 表单验证规则
const couponRules = {
  name: [
    { required: true, message: '请输入优惠券名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择优惠券类型', trigger: 'change' }
  ],
  threshold: [
    { required: true, message: '请输入使用门槛', trigger: 'blur' }
  ],
  discount: [
    { required: true, message: '请输入优惠金额/折扣', trigger: 'blur' }
  ],
  totalQuantity: [
    { required: true, message: '请输入发放数量', trigger: 'blur' }
  ],
  receiveTime: [
    { required: true, message: '请选择领取时间', trigger: 'change' }
  ],
  validTime: [
    { required: true, message: '请选择使用有效期', trigger: 'change' }
  ],
  productScope: [
    { required: true, message: '请选择适用商品范围', trigger: 'change' }
  ]
}

// 优惠券列表
const coupons = ref([])

// 商品列表（用于选择适用商品）
const productList = ref([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 计算属性 - 前端筛选（如果需要的话）
const filteredCoupons = computed(() => {
  // 由于后端已经支持搜索和筛选，这里可以直接返回列表
  // 如果需要前端再次过滤，可以在这里添加逻辑
  return coupons.value
})

// 搜索和筛选变化时重新加载数据
const handleSearch = () => {
  pagination.currentPage = 1
  loadCoupons()
}

const handleStatusFilterChange = () => {
  pagination.currentPage = 1
  loadCoupons()
}

// 方法
const getCouponTypeText = (type) => {
  const typeMap = {
    cash: '现金券',
    discount: '折扣券',
    reduce: '满减券'
  }
  return typeMap[type] || type
}

const getCouponTypeTag = (type) => {
  const tagMap = {
    cash: 'success',
    discount: 'warning',
    reduce: 'primary'
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

const getDiscountLabel = () => {
  const labelMap = {
    cash: '优惠金额',
    discount: '折扣比例',
    reduce: '优惠金额'
  }
  return labelMap[couponForm.type] || '优惠金额'
}

const getDiscountUnit = () => {
  const unitMap = {
    cash: '元',
    discount: '折（如9表示9折）',
    reduce: '元'
  }
  return unitMap[couponForm.type] || '元'
}

const canDelete = (coupon) => {
  return coupon.status === 'pending' || coupon.status === 'ended'
}

const resetForm = () => {
  Object.assign(couponForm, {
    name: '',
    type: 'cash',
    threshold: 0,
    discount: 0,
    totalQuantity: 100,
    receiveTime: [],
    validTime: [],
    productScope: 'all',
    productIds: [],
    description: ''
  })
  editingCoupon.value = null
  couponFormRef.value?.clearValidate()
}

const submitCoupon = async () => {
  try {
    await couponFormRef.value.validate()
    
    loading.value = true
    
    // 格式化时间数据（将 Date 对象转换为 ISO 字符串格式，Jackson 期望 ISO 格式：yyyy-MM-ddTHH:mm:ss）
    const formatTimeForRequest = (timeArray) => {
      if (!timeArray || timeArray.length !== 2) return []
      return timeArray.map(time => {
        if (time instanceof Date) {
          // 使用 ISO 格式（保留 T），例如：2025-10-31T16:00:00
          return time.toISOString().substring(0, 19)
        }
        // 如果已经是字符串，确保格式正确
        if (typeof time === 'string') {
          // 如果是空格分隔的格式，转换为 T 分隔
          return time.replace(' ', 'T').substring(0, 19)
        }
        return time
      })
    }
    
    // 构建请求数据
    const requestData = {
      name: couponForm.name,
      type: couponForm.type,
      threshold: couponForm.threshold,
      discount: couponForm.discount,
      totalQuantity: couponForm.totalQuantity,
      receiveTime: formatTimeForRequest(couponForm.receiveTime),
      validTime: formatTimeForRequest(couponForm.validTime),
      productScope: couponForm.productScope,
      productIds: couponForm.productScope === 'specific' ? couponForm.productIds : [],
      description: couponForm.description
    }
    
    if (editingCoupon.value) {
      // 编辑优惠券
      const response = await updateCoupon(editingCoupon.value.id, requestData)
      if (response.code === 200) {
        ElMessage.success('优惠券更新成功')
        await loadCoupons()
      } else {
        ElMessage.error(response.message || '更新失败')
      }
    } else {
      // 创建优惠券
      const response = await createCoupon(requestData)
      if (response.code === 200) {
        ElMessage.success('优惠券创建成功')
        await loadCoupons()
      } else {
        ElMessage.error(response.message || '创建失败')
      }
    }
    
    showCreateDialog.value = false
    resetForm()
  } catch (error) {
    console.error('提交失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error.message && error.message !== 'cancel') {
      ElMessage.error(error.message)
    }
  } finally {
    loading.value = false
  }
}

const editCoupon = async (coupon) => {
  try {
    // 加载优惠券详情
    const response = await getCouponDetail(coupon.id)
    if (response.code === 200 && response.data) {
      const couponData = response.data
      editingCoupon.value = couponData
      
      // 格式化时间为数组（用于日期选择器）
      const formatDateTimeForPicker = (dateTimeStr) => {
        if (!dateTimeStr) return null
        if (typeof dateTimeStr === 'string') {
          // 将 "2024-01-01 00:00:00" 转换为 "2024-01-01T00:00:00" 或 Date 对象
          const date = new Date(dateTimeStr.replace(' ', 'T'))
          return date
        }
        return new Date(dateTimeStr)
      }
      
      Object.assign(couponForm, {
        name: couponData.name,
        type: couponData.type,
        threshold: couponData.threshold,
        discount: couponData.discount,
        totalQuantity: couponData.totalQuantity,
        receiveTime: couponData.receiveStartTime && couponData.receiveEndTime 
          ? [formatDateTimeForPicker(couponData.receiveStartTime), formatDateTimeForPicker(couponData.receiveEndTime)]
          : [],
        validTime: couponData.validStartTime && couponData.validEndTime
          ? [formatDateTimeForPicker(couponData.validStartTime), formatDateTimeForPicker(couponData.validEndTime)]
          : [],
        productScope: couponData.productScope,
        productIds: couponData.productIds || [],
        description: couponData.description || ''
      })
      showCreateDialog.value = true
    } else {
      ElMessage.error('加载优惠券详情失败')
    }
  } catch (error) {
    console.error('加载优惠券详情失败:', error)
    ElMessage.error('加载优惠券详情失败')
  }
}

const viewDetails = async (coupon) => {
  try {
    loading.value = true
    const response = await getCouponDetail(coupon.id)
    if (response.code === 200 && response.data) {
      selectedCoupon.value = response.data
      showDetailsDialog.value = true
    } else {
      ElMessage.error('加载优惠券详情失败')
    }
  } catch (error) {
    console.error('加载优惠券详情失败:', error)
    ElMessage.error('加载优惠券详情失败')
  } finally {
    loading.value = false
  }
}

const togglePause = async (coupon) => {
  const action = coupon.status === 'paused' ? '恢复' : '暂停'
  try {
    await ElMessageBox.confirm(
      `确定要${action}优惠券"${coupon.name}"吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    loading.value = true
    const response = await togglePauseCoupon(coupon.id)
    if (response.code === 200) {
      ElMessage.success(`优惠券${action}成功`)
      await loadCoupons()
    } else {
      ElMessage.error(response.message || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  } finally {
    loading.value = false
  }
}

const deleteCoupon = async (coupon) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除优惠券"${coupon.name}"吗？删除后不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    loading.value = true
    const response = await deleteCouponApi(coupon.id)
    if (response.code === 200) {
      ElMessage.success('优惠券删除成功')
      await loadCoupons()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  } finally {
    loading.value = false
  }
}

// 加载优惠券列表
const loadCoupons = async () => {
  try {
    loading.value = true
    const queryParams = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      name: searchKeyword.value || undefined,
      status: statusFilter.value || undefined
    }
    
    // 移除undefined的参数
    Object.keys(queryParams).forEach(key => {
      if (queryParams[key] === undefined) {
        delete queryParams[key]
      }
    })
    
    const response = await getCouponList(queryParams)
    if (response.code === 200 && response.data) {
      coupons.value = response.data.records || []
      pagination.total = response.data.total || 0
      
      // 格式化时间显示
      coupons.value.forEach(coupon => {
        if (coupon.receiveStartTime) {
          coupon.startTime = formatDateTime(coupon.receiveStartTime)
        }
        if (coupon.receiveEndTime) {
          coupon.endTime = formatDateTime(coupon.receiveEndTime)
        }
      })
    } else {
      ElMessage.error(response.message || '加载优惠券列表失败')
    }
  } catch (error) {
    console.error('加载优惠券列表失败:', error)
    ElMessage.error('加载优惠券列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期时间显示
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  if (typeof dateTimeStr === 'string') {
    // 如果是ISO格式字符串，转换为显示格式
    const date = new Date(dateTimeStr)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}`
  }
  return dateTimeStr
}

// 加载商品列表（用于选择适用商品）
const loadProductList = async () => {
  try {
    const params = {
      current: 1,
      size: 1000 // 获取所有商品
    }
    const response = await getProductList(params)
    if (response.code === 200 && response.data) {
      productList.value = (response.data.records || []).map(product => ({
        id: product.id,
        name: product.name
      }))
    }
  } catch (error) {
    console.error('加载商品列表失败:', error)
    // 如果加载失败，使用空列表
    productList.value = []
  }
}

onMounted(async () => {
  await Promise.all([
    loadCoupons(),
    loadProductList()
  ])
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

.coupon-details {
  padding: 10px 0;
}

.usage-details h4 {
  margin: 0 0 15px 0;
  color: #303133;
}
</style>
