<template>
  <div class="page-container">
    <div class="page-header">
      <h2>订单发货与跟踪</h2>
      <p class="page-description">管理订单发货、配送跟踪和配送进度更新</p>
      <div class="header-actions">
        <el-button 
          type="success" 
          @click="refreshOrders"
          :loading="loading"
        >
          <el-icon><Refresh /></el-icon>
          刷新订单
        </el-button>
        <el-button 
          type="primary" 
          @click="batchShip"
          :disabled="selectedOrders.length === 0"
        >
          <el-icon><Operation /></el-icon>
          批量发货 ({{ selectedOrders.length }})
        </el-button>
      </div>
    </div>

    <el-card>
      <!-- 搜索和筛选 -->
      <div class="search-form">
        <el-form :model="searchForm" inline>
          <el-form-item label="订单号">
            <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
          </el-form-item>
          <el-form-item label="配送状态">
            <el-select v-model="searchForm.shippingStatus" placeholder="请选择配送状态" clearable>
              <el-option 
                v-for="option in shippingStatusOptions" 
                :key="option.value" 
                :label="option.label" 
                :value="option.value" 
              />
            </el-select>
          </el-form-item>
          <el-form-item label="配送方式">
            <el-select v-model="searchForm.deliveryType" placeholder="请选择配送方式" clearable>
              <el-option 
                v-for="option in deliveryTypeOptions" 
                :key="option.value" 
                :label="option.label" 
                :value="option.value" 
              />
            </el-select>
          </el-form-item>
          <el-form-item label="发货时间">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 订单列表 -->
      <el-table 
        :data="filteredOrderList" 
        v-loading="loading" 
        class="order-table"
        @selection-change="handleSelectionChange"
        row-key="id"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="orderNo" label="订单号" width="140" show-overflow-tooltip />
        <el-table-column prop="customerName" label="客户" width="100" show-overflow-tooltip />
        <el-table-column prop="phone" label="联系电话" width="120" show-overflow-tooltip />
        <el-table-column prop="address" label="收货地址" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="address-info">
              <div class="address-text">{{ row.address }}</div>
              <div class="address-detail">{{ row.addressDetail }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="deliveryType" label="配送方式" width="100">
          <template #default="{ row }">
            <el-tag :type="getDeliveryTypeTag(row.deliveryType)" size="small">
              {{ getDeliveryTypeText(row.deliveryType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="shippingStatus" label="配送状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getShippingStatusTag(row.shippingStatus)" :effect="row.shippingStatus ? 'dark' : 'plain'" size="small">
              {{ getShippingStatusText(row.shippingStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="shippingInfo" label="配送信息" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <div v-if="row.deliveryType === 'self'" class="shipping-info">
              <div class="delivery-person">
                <span class="label">配送员：</span>
                <span>{{ row.deliveryPerson }}</span>
              </div>
              <div class="delivery-phone">
                <span class="label">电话：</span>
                <span>{{ row.deliveryPhone }}</span>
              </div>
            </div>
            <div v-else-if="row.deliveryType === 'third_party'" class="shipping-info">
              <div class="third-party-order">
                <span class="label">配送单号：</span>
                <span>{{ row.thirdPartyOrderNo }}</span>
              </div>
              <div class="delivery-company">
                <span class="label">配送公司：</span>
                <span>{{ row.deliveryCompany }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="shippingTime" label="发货时间" width="140" show-overflow-tooltip />
        <el-table-column prop="estimatedTime" label="预计送达" width="140" show-overflow-tooltip>
          <template #default="{ row }">
            <div v-if="row.estimatedTime" class="estimated-time">
              {{ row.estimatedTime }}
            </div>
            <div v-else class="no-estimate">-</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="text" @click="viewOrderDetail(row)">查看详情</el-button>
              <template v-if="canShipOrder(row)">
                <el-button type="text" @click="shipOrder(row)" class="text-primary">
                  发货
                </el-button>
              </template>
              <template v-else-if="canUpdateProgress(row)">
                <el-button type="text" @click="updateProgress(row)" class="text-warning">
                  更新进度
                </el-button>
                <el-button type="text" @click="completeDeliveryOrder(row)" class="text-success">
                  完成配送
                </el-button>
              </template>
              <el-button type="text" @click="trackDelivery(row)">配送跟踪</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 发货对话框 -->
    <el-dialog v-model="shipDialogVisible" title="订单发货" width="600px">
      <div v-if="currentOrder">
        <el-descriptions :column="2" border style="margin-bottom: 20px;">
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="客户">{{ currentOrder.customerName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.phone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.address }}</el-descriptions-item>
        </el-descriptions>

        <el-form :model="shipForm" label-width="120px">
          <el-form-item label="配送方式" required>
            <el-radio-group v-model="shipForm.deliveryType">
              <el-radio 
                v-for="option in deliveryTypeOptions" 
                :key="option.value" 
                :value="option.value"
              >
                {{ option.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>

          <!-- 自配送信息 -->
          <template v-if="shipForm.deliveryType === 'self'">
            <el-form-item label="配送员姓名" required>
              <el-input v-model="shipForm.deliveryPerson" placeholder="请输入配送员姓名" />
            </el-form-item>
            <el-form-item label="配送员电话" required>
              <el-input v-model="shipForm.deliveryPhone" placeholder="请输入配送员电话" />
            </el-form-item>
            <el-form-item label="预计送达时间">
              <el-date-picker
                v-model="shipForm.estimatedTime"
                type="datetime"
                placeholder="选择预计送达时间"
                style="width: 100%"
              />
            </el-form-item>
          </template>

          <!-- 第三方配送信息 -->
          <template v-else-if="shipForm.deliveryType === 'third_party'">
            <el-form-item label="配送公司" required>
              <el-select v-model="shipForm.deliveryCompany" placeholder="请选择配送公司" style="width: 100%">
                <el-option 
                  v-for="option in deliveryCompanyOptions" 
                  :key="option.value" 
                  :label="option.label" 
                  :value="option.value" 
                />
              </el-select>
            </el-form-item>
            <el-form-item label="配送单号" required>
              <el-input v-model="shipForm.thirdPartyOrderNo" placeholder="请输入第三方配送单号" />
            </el-form-item>
            <el-form-item label="预计送达时间">
              <el-date-picker
                v-model="shipForm.estimatedTime"
                type="datetime"
                placeholder="选择预计送达时间"
                style="width: 100%"
              />
            </el-form-item>
          </template>

          <el-form-item label="发货备注">
            <el-input
              v-model="shipForm.remark"
              type="textarea"
              :rows="3"
              placeholder="发货备注（可选）"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmShip" :loading="shipLoading">
          确认发货
        </el-button>
      </template>
    </el-dialog>

    <!-- 更新配送进度对话框 -->
    <el-dialog v-model="progressDialogVisible" title="更新配送进度" width="500px">
      <div v-if="currentOrder">
        <el-descriptions :column="1" border style="margin-bottom: 20px;">
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="配送员">{{ currentOrder.deliveryPerson }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.deliveryPhone }}</el-descriptions-item>
        </el-descriptions>

        <el-form :model="progressForm" label-width="100px">
          <el-form-item label="当前状态">
            <el-tag :type="getShippingStatusTag(currentOrder.shippingStatus)">
              {{ getShippingStatusText(currentOrder.shippingStatus) }}
            </el-tag>
          </el-form-item>
          <el-form-item label="更新状态" required>
            <el-select v-model="progressForm.status" placeholder="请选择新的配送状态" style="width: 100%">
              <el-option 
                v-for="option in progressStatusOptions" 
                :key="option.value" 
                :label="option.label" 
                :value="option.value" 
              />
            </el-select>
          </el-form-item>
          <el-form-item label="进度描述">
            <el-input
              v-model="progressForm.description"
              type="textarea"
              :rows="3"
              placeholder="详细描述当前配送进度，如：已出发，预计20分钟送达"
            />
          </el-form-item>
          <el-form-item label="预计送达时间">
            <el-date-picker
              v-model="progressForm.estimatedTime"
              type="datetime"
              placeholder="更新预计送达时间"
              style="width: 100%"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="progressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmProgress" :loading="progressLoading">
          更新进度
        </el-button>
      </template>
    </el-dialog>

    <!-- 配送跟踪对话框 -->
    <el-dialog v-model="trackDialogVisible" title="配送跟踪" width="800px">
      <div v-if="currentOrder">
        <el-descriptions :column="2" border style="margin-bottom: 20px;">
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="配送方式">
            <el-tag :type="getDeliveryTypeTag(currentOrder.deliveryType)">
              {{ getDeliveryTypeText(currentOrder.deliveryType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="配送员">{{ currentOrder.deliveryPerson }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.deliveryPhone }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getShippingStatusTag(currentOrder.shippingStatus)">
              {{ getShippingStatusText(currentOrder.shippingStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="预计送达">{{ currentOrder.estimatedTime || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 配送进度时间线 -->
        <div class="delivery-timeline">
          <h4>配送进度</h4>
          <el-timeline>
            <el-timeline-item
              v-for="(progress, index) in currentOrder.progressHistory"
              :key="index"
              :timestamp="progress.time"
              :type="getTimelineType(progress.status)"
            >
              <div class="timeline-content">
                <div class="progress-status">{{ progress.status }}</div>
                <div class="progress-description">{{ progress.description }}</div>
                <div v-if="progress.operator" class="progress-operator">操作人：{{ progress.operator }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>

        <!-- 第三方配送信息 -->
        <div v-if="currentOrder.deliveryType === 'third_party'" class="third-party-info">
          <h4>第三方配送信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="配送公司">{{ currentOrder.deliveryCompany }}</el-descriptions-item>
            <el-descriptions-item label="配送单号">{{ currentOrder.thirdPartyOrderNo }}</el-descriptions-item>
            <el-descriptions-item label="配送员位置" :span="2">
              <el-button type="text" @click="viewLocation">查看实时位置</el-button>
            </el-descriptions-item>
            <el-descriptions-item label="预计送达时间" :span="2">
              {{ currentOrder.estimatedTime || '暂无信息' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      <template #footer>
        <el-button @click="trackDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="refreshTracking">刷新跟踪</el-button>
      </template>
    </el-dialog>

    <!-- 批量发货对话框 -->
    <el-dialog v-model="batchShipDialogVisible" title="批量发货" width="600px">
      <div class="batch-content">
        <p>已选择 {{ selectedOrders.length }} 个订单进行批量发货：</p>
        <el-table :data="selectedOrders" size="small" max-height="300">
          <el-table-column prop="orderNo" label="订单号" />
          <el-table-column prop="customerName" label="客户" />
          <el-table-column prop="address" label="收货地址" />
        </el-table>
        
        <el-form :model="batchShipForm" label-width="120px" style="margin-top: 20px;">
          <el-form-item label="配送方式" required>
            <el-radio-group v-model="batchShipForm.deliveryType">
              <el-radio 
                v-for="option in deliveryTypeOptions" 
                :key="option.value" 
                :value="option.value"
              >
                {{ option.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
          
          <template v-if="batchShipForm.deliveryType === 'self'">
            <el-form-item label="配送员姓名" required>
              <el-input v-model="batchShipForm.deliveryPerson" placeholder="请输入配送员姓名" />
            </el-form-item>
            <el-form-item label="配送员电话" required>
              <el-input v-model="batchShipForm.deliveryPhone" placeholder="请输入配送员电话" />
            </el-form-item>
          </template>
          
          <template v-else-if="batchShipForm.deliveryType === 'third_party'">
            <el-form-item label="配送公司" required>
              <el-select v-model="batchShipForm.deliveryCompany" placeholder="请选择配送公司" style="width: 100%">
                <el-option 
                  v-for="option in deliveryCompanyOptions" 
                  :key="option.value" 
                  :label="option.label" 
                  :value="option.value" 
                />
              </el-select>
            </el-form-item>
          </template>
          
          <el-form-item label="批量备注">
            <el-input
              v-model="batchShipForm.remark"
              type="textarea"
              :rows="3"
              placeholder="批量发货备注（可选）"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="batchShipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBatchShip" :loading="batchShipLoading">
          确认批量发货
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Operation } from '@element-plus/icons-vue'
import { 
  getShipmentPage, 
  getShipmentDetail, 
  shipOrder as shipOrderApi, 
  batchShipOrder, 
  updateShippingProgress, 
  completeDelivery 
} from '@/api/orderShipment'

// 配送状态选项
const shippingStatusOptions = [
  { label: '待发货', value: 'pending' },
  { label: '配送中', value: 'shipping' },
  { label: '已送达', value: 'delivered' },
  { label: '配送异常', value: 'exception' }
]

// 配送方式选项
const deliveryTypeOptions = [
  { label: '商家自配送', value: 'self' },
  { label: '第三方配送', value: 'third_party' }
]

// 配送进度状态选项
const progressStatusOptions = [
  { label: '已出发，正在配送中', value: 'on_the_way' },
  { label: '已到达小区门口', value: 'arrived_community' },
  { label: '已到达楼下', value: 'arrived_building' },
  { label: '正在上楼', value: 'going_upstairs' },
  { label: '已到达门口', value: 'arrived_door' },
  { label: '配送异常', value: 'exception' }
]

// 配送公司选项
const deliveryCompanyOptions = [
  { label: '美团配送', value: 'meituan' },
  { label: '饿了么蜂鸟', value: 'fengniao' },
  { label: '达达配送', value: 'dada' },
  { label: '顺丰同城', value: 'sf' },
  { label: '其他', value: 'other' }
]

// 对话框状态
const shipDialogVisible = ref(false)
const progressDialogVisible = ref(false)
const trackDialogVisible = ref(false)
const batchShipDialogVisible = ref(false)

// 当前订单
const currentOrder = ref(null)
const selectedOrders = ref([])

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  shippingStatus: '',
  deliveryType: '',
  dateRange: []
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 发货表单
const shipForm = reactive({
  deliveryType: 'self',
  deliveryPerson: '',
  deliveryPhone: '',
  deliveryCompany: '',
  thirdPartyOrderNo: '',
  estimatedTime: '',
  remark: ''
})

// 进度更新表单
const progressForm = reactive({
  status: '',
  description: '',
  estimatedTime: ''
})

// 批量发货表单
const batchShipForm = reactive({
  deliveryType: 'self',
  deliveryPerson: '',
  deliveryPhone: '',
  deliveryCompany: '',
  remark: ''
})

// 订单列表数据
const orderList = ref([])

// 加载状态
const loading = ref(false)
const shipLoading = ref(false)
const progressLoading = ref(false)
const batchShipLoading = ref(false)

// 筛选后的订单列表
const filteredOrderList = computed(() => {
  return orderList.value
})

// 验证订单是否可以发货
const canShipOrder = (order) => {
  // 检查订单状态和配送状态
  return order && (
    (order.status === 1 || order.status === 2 || order.status === 3) && // 已支付、已接单、制作中
    (order.shippingStatus === 'pending' || !order.shippingStatus) // 待发货或未发货
  )
}

// 验证订单是否可以更新进度
const canUpdateProgress = (order) => {
  return order && order.shippingStatus === 'shipping'
}

// 验证订单是否可以完成配送
const canCompleteDelivery = (order) => {
  return order && order.shippingStatus === 'shipping'
}

// 获取配送方式文本
const getDeliveryTypeText = (type) => {
  const textMap = {
    'self': '商家自配送',
    'third_party': '第三方配送'
  }
  return textMap[type] || '未知'
}

// 获取配送方式标签类型
const getDeliveryTypeTag = (type) => {
  const tagMap = {
    'self': 'primary',
    'third_party': 'success'
  }
  return tagMap[type] || 'info'
}

// 获取配送状态标签类型
const getShippingStatusTag = (status) => {
  if (!status) return 'info'
  const tagMap = {
    'pending': 'warning',
    'shipping': 'primary',
    'delivered': 'success',
    'exception': 'danger'
  }
  return tagMap[status] || 'info'
}

// 获取配送状态文本
const getShippingStatusText = (status) => {
  if (!status) return '未发货'
  const textMap = {
    'pending': '待发货',
    'shipping': '配送中',
    'delivered': '已送达',
    'exception': '配送异常'
  }
  return textMap[status] || '未知'
}

// 获取时间线类型
const getTimelineType = (status) => {
  const typeMap = {
    '已发货': 'primary',
    '配送中': 'warning',
    '已送达': 'success',
    '配送异常': 'danger'
  }
  return typeMap[status] || 'info'
}

// 搜索
const handleSearch = async () => {
  loading.value = true
  try {
    const queryData = {
      orderNo: searchForm.orderNo,
      shippingStatus: searchForm.shippingStatus,
      deliveryType: searchForm.deliveryType,
      startTime: searchForm.dateRange && searchForm.dateRange.length === 2 ? searchForm.dateRange[0] : null,
      endTime: searchForm.dateRange && searchForm.dateRange.length === 2 ? searchForm.dateRange[1] : null,
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize
    }
    
    const response = await getShipmentPage(queryData)
    if (response.code === 200) {
      orderList.value = response.data.records || []
      pagination.total = response.data.total || 0
      ElMessage.success('搜索完成')
    } else {
      ElMessage.error(response.message || '搜索失败')
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败，请重试')
  } finally {
    loading.value = false
  }
}

// 重置搜索
const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.shippingStatus = ''
  searchForm.deliveryType = ''
  searchForm.dateRange = []
  handleSearch()
}

// 刷新订单
const refreshOrders = async () => {
  loading.value = true
  try {
    const queryData = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize
    }
    
    const response = await getShipmentPage(queryData)
    if (response.code === 200) {
      orderList.value = response.data.records || []
      pagination.total = response.data.total || 0
      ElMessage.success('订单已刷新')
    } else {
      ElMessage.error(response.message || '刷新失败')
    }
  } catch (error) {
    console.error('刷新失败:', error)
    ElMessage.error('刷新失败，请重试')
  } finally {
    loading.value = false
  }
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedOrders.value = selection
}

// 查看订单详情
const viewOrderDetail = async (row) => {
  try {
    const orderId = row.orderId || row.id
    if (!orderId) {
      ElMessage.error('无法获取订单ID')
      return
    }
    
    const response = await getShipmentDetail(orderId)
    if (response && response.code === 200) {
      currentOrder.value = response.data
      trackDialogVisible.value = true
    } else {
      const errorMsg = response?.message || '获取订单详情失败'
      ElMessage.error(errorMsg)
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '获取订单详情失败，请重试'
    ElMessage.error(errorMsg)
  }
}

// 发货
const shipOrder = (row) => {
  currentOrder.value = row
  // 重置表单为默认值
  shipForm.deliveryType = 'self'
  shipForm.deliveryPerson = ''
  shipForm.deliveryPhone = ''
  shipForm.deliveryCompany = ''
  shipForm.thirdPartyOrderNo = ''
  shipForm.estimatedTime = ''
  shipForm.remark = ''
  shipDialogVisible.value = true
}

// 格式化时间为后端需要的格式
const formatDateTime = (dateTime) => {
  if (!dateTime) return null
  if (typeof dateTime === 'string') {
    return dateTime.replace('T', ' ').substring(0, 19)
  }
  if (dateTime instanceof Date) {
    const year = dateTime.getFullYear()
    const month = String(dateTime.getMonth() + 1).padStart(2, '0')
    const day = String(dateTime.getDate()).padStart(2, '0')
    const hours = String(dateTime.getHours()).padStart(2, '0')
    const minutes = String(dateTime.getMinutes()).padStart(2, '0')
    const seconds = String(dateTime.getSeconds()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  }
  return dateTime
}

// 确认发货
const confirmShip = async () => {
  // 验证表单
  if (shipForm.deliveryType === 'self') {
    if (!shipForm.deliveryPerson || !shipForm.deliveryPhone) {
      ElMessage.warning('请填写配送员信息')
      return
    }
  } else if (shipForm.deliveryType === 'third_party') {
    if (!shipForm.deliveryCompany || !shipForm.thirdPartyOrderNo) {
      ElMessage.warning('请填写第三方配送信息')
      return
    }
  }

  // 验证订单状态
  if (!canShipOrder(currentOrder.value)) {
    ElMessage.warning('当前订单状态不允许发货')
    return
  }
  
  shipLoading.value = true
  try {
    // 获取订单ID（可能是orderId或id）
    const orderId = currentOrder.value.orderId || currentOrder.value.id
    if (!orderId) {
      ElMessage.error('无法获取订单ID')
      return
    }
    
    const shipData = {
      orderId: orderId,
      deliveryType: shipForm.deliveryType,
      deliveryPerson: shipForm.deliveryPerson || null,
      deliveryPhone: shipForm.deliveryPhone || null,
      deliveryCompany: shipForm.deliveryCompany || null,
      thirdPartyOrderNo: shipForm.thirdPartyOrderNo || null,
      estimatedTime: shipForm.estimatedTime ? formatDateTime(shipForm.estimatedTime) : null,
      remark: shipForm.remark || null
    }
    
    const response = await shipOrderApi(shipData)
    if (response && response.code === 200) {
      ElMessage.success('订单发货成功，订单状态已更新为已发货')
      shipDialogVisible.value = false
      // 重置表单
      shipForm.deliveryType = 'self'
      shipForm.deliveryPerson = ''
      shipForm.deliveryPhone = ''
      shipForm.deliveryCompany = ''
      shipForm.thirdPartyOrderNo = ''
      shipForm.estimatedTime = ''
      shipForm.remark = ''
      // 刷新列表
      await refreshOrders()
    } else {
      const errorMsg = response?.message || error.response?.data?.message || '发货失败'
      ElMessage.error(errorMsg)
    }
  } catch (error) {
    console.error('发货失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '发货失败，请重试'
    ElMessage.error(errorMsg)
  } finally {
    shipLoading.value = false
  }
}

// 更新进度
const updateProgress = (row) => {
  currentOrder.value = row
  progressForm.status = ''
  progressForm.description = ''
  progressForm.estimatedTime = ''
  progressDialogVisible.value = true
}

// 确认更新进度
const confirmProgress = async () => {
  if (!progressForm.status) {
    ElMessage.warning('请选择配送状态')
    return
  }

  // 验证订单状态
  if (!canUpdateProgress(currentOrder.value)) {
    ElMessage.warning('当前订单状态不允许更新进度')
    return
  }
  
  progressLoading.value = true
  try {
    const orderId = currentOrder.value.orderId || currentOrder.value.id
    if (!orderId) {
      ElMessage.error('无法获取订单ID')
      return
    }
    
    const progressData = {
      orderId: orderId,
      status: progressForm.status,
      description: progressForm.description || null,
      estimatedTime: progressForm.estimatedTime ? formatDateTime(progressForm.estimatedTime) : null
    }
    
    const response = await updateShippingProgress(progressData)
    if (response && response.code === 200) {
      ElMessage.success('配送进度已更新，客户可实时查看')
      progressDialogVisible.value = false
      // 重置表单
      progressForm.status = ''
      progressForm.description = ''
      progressForm.estimatedTime = ''
      // 刷新列表
      await refreshOrders()
    } else {
      const errorMsg = response?.message || error.response?.data?.message || '更新失败'
      ElMessage.error(errorMsg)
    }
  } catch (error) {
    console.error('更新失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '更新失败，请重试'
    ElMessage.error(errorMsg)
  } finally {
    progressLoading.value = false
  }
}

// 完成配送
const completeDeliveryOrder = async (row) => {
  // 验证订单状态
  if (!canCompleteDelivery(row)) {
    ElMessage.warning('当前订单状态不允许完成配送')
    return
  }

  try {
    await ElMessageBox.confirm('确定要完成配送吗？完成后订单状态将更新为已完成', '完成配送', {
      confirmButtonText: '确定完成',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const orderId = row.orderId || row.id
    if (!orderId) {
      ElMessage.error('无法获取订单ID')
      return
    }
    
    const response = await completeDelivery(orderId)
    if (response && response.code === 200) {
      ElMessage.success('配送完成，订单状态已更新为已完成')
      // 刷新列表
      await refreshOrders()
    } else {
      const errorMsg = response?.message || error.response?.data?.message || '完成配送失败'
      ElMessage.error(errorMsg)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('完成配送失败:', error)
      const errorMsg = error.response?.data?.message || error.message || '完成配送失败，请重试'
      ElMessage.error(errorMsg)
    }
  }
}

// 配送跟踪
const trackDelivery = (row) => {
  currentOrder.value = row
  trackDialogVisible.value = true
}

// 查看位置
const viewLocation = () => {
  ElMessage.info('查看配送员实时位置功能')
}

// 刷新跟踪
const refreshTracking = () => {
  ElMessage.success('跟踪信息已刷新')
}

// 批量发货
const batchShip = () => {
  if (selectedOrders.value.length === 0) {
    ElMessage.warning('请选择要发货的订单')
    return
  }
  
  batchShipForm.deliveryType = 'self'
  batchShipForm.deliveryPerson = ''
  batchShipForm.deliveryPhone = ''
  batchShipForm.deliveryCompany = ''
  batchShipForm.remark = ''
  batchShipDialogVisible.value = true
}

// 确认批量发货
const confirmBatchShip = async () => {
  if (batchShipForm.deliveryType === 'self') {
    if (!batchShipForm.deliveryPerson || !batchShipForm.deliveryPhone) {
      ElMessage.warning('请填写配送员信息')
      return
    }
  } else if (batchShipForm.deliveryType === 'third_party') {
    if (!batchShipForm.deliveryCompany) {
      ElMessage.warning('请选择配送公司')
      return
    }
    // 批量第三方配送时，可以为空（每个订单单独填写）
  }
  
  batchShipLoading.value = true
  try {
    // 获取订单ID列表（可能是orderId或id）
    const orderIds = selectedOrders.value.map(order => order.orderId || order.id).filter(id => id)
    
    if (orderIds.length === 0) {
      ElMessage.warning('请选择要发货的订单')
      return
    }
    
    const batchData = {
      orderIds: orderIds,
      deliveryType: batchShipForm.deliveryType,
      deliveryPerson: batchShipForm.deliveryPerson || null,
      deliveryPhone: batchShipForm.deliveryPhone || null,
      deliveryCompany: batchShipForm.deliveryCompany || null,
      thirdPartyOrderNo: batchShipForm.thirdPartyOrderNo || null,
      remark: batchShipForm.remark || null
    }
    
    const response = await batchShipOrder(batchData)
    if (response && response.code === 200) {
      selectedOrders.value = []
      ElMessage.success(`批量发货完成，成功发货 ${orderIds.length} 个订单，订单状态已更新`)
      batchShipDialogVisible.value = false
      // 重置表单
      batchShipForm.deliveryType = 'self'
      batchShipForm.deliveryPerson = ''
      batchShipForm.deliveryPhone = ''
      batchShipForm.deliveryCompany = ''
      batchShipForm.remark = ''
      // 刷新列表
      await refreshOrders()
    } else {
      const errorMsg = response?.message || error.response?.data?.message || '批量发货失败'
      ElMessage.error(errorMsg)
    }
  } catch (error) {
    console.error('批量发货失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '批量发货失败，请重试'
    ElMessage.error(errorMsg)
  } finally {
    batchShipLoading.value = false
  }
}

// 分页处理
const handleSizeChange = (val) => {
  pagination.pageSize = val
  handleSearch()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  handleSearch()
}

// 初始化
onMounted(async () => {
  await refreshOrders()
})
</script>

<style scoped>
.page-container {
  padding: 15px;
  background: #f5f5f5;
  min-height: 100vh;
  max-width: 1600px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.page-description {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

/* 搜索表单 */
.search-form {
  margin-bottom: 15px;
  padding: 15px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.search-form :deep(.el-form-item) {
  margin-bottom: 12px;
}

/* 订单表格 */
.order-table {
  border-radius: 8px;
  overflow: hidden;
}

.address-info {
  .address-text {
    font-weight: 500;
    color: #333;
    margin-bottom: 2px;
    font-size: 13px;
    line-height: 1.4;
  }
  
  .address-detail {
    font-size: 12px;
    color: #666;
    line-height: 1.3;
  }
}

.shipping-info {
  .delivery-person,
  .delivery-phone,
  .third-party-order,
  .delivery-company {
    margin-bottom: 3px;
    font-size: 13px;
    line-height: 1.4;
    
    .label {
      color: #666;
      margin-right: 3px;
    }
  }
}

.estimated-time {
  color: #67C23A;
  font-weight: 500;
}

.no-estimate {
  color: #999;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.action-buttons .el-button {
  font-size: 13px;
  padding: 4px 8px;
}

/* 配送时间线 */
.delivery-timeline {
  margin-top: 20px;
  
  h4 {
    margin-bottom: 15px;
    color: #333;
  }
}

.timeline-content {
  .progress-status {
    font-weight: 500;
    color: #333;
    margin-bottom: 4px;
  }
  
  .progress-description {
    color: #666;
    margin-bottom: 4px;
  }
  
  .progress-operator {
    font-size: 12px;
    color: #999;
  }
}

/* 第三方配送信息 */
.third-party-info {
  margin-top: 20px;
  
  h4 {
    margin-bottom: 15px;
    color: #333;
  }
}

/* 批量处理内容 */
.batch-content {
  p {
    margin-bottom: 15px;
    color: #666;
  }
}

/* 分页 */
.pagination {
  margin-top: 20px;
  text-align: right;
}

/* 文本颜色 */
.text-primary {
  color: #409EFF;
}

.text-warning {
  color: #E6A23C;
}

.text-success {
  color: #67C23A;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-container {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .search-form {
    padding: 15px;
  }
  
  .action-buttons {
    flex-direction: row;
    flex-wrap: wrap;
  }
}

/* 动画效果 */
.el-card {
  transition: all 0.3s ease;
}

.el-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.el-button {
  transition: all 0.3s ease;
}

/* 表格样式优化 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table__header) {
  background: #f8f9fa;
}

:deep(.el-table__row:hover) {
  background: #f5f7fa;
}

/* 标签样式 */
:deep(.el-tag) {
  border-radius: 4px;
}

/* 对话框样式 */
:deep(.el-dialog) {
  border-radius: 8px;
}

:deep(.el-dialog__header) {
  background: #f8f9fa;
  border-radius: 8px 8px 0 0;
}

/* 描述列表样式 */
:deep(.el-descriptions) {
  border-radius: 6px;
}

/* 时间线样式 */
:deep(.el-timeline-item__timestamp) {
  color: #666;
  font-size: 12px;
}

/* 加载状态 */
.el-loading-mask {
  border-radius: 8px;
}
</style>