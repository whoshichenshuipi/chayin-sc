<template>
  <div class="page-container">
    <div class="page-header">
      <h2>订单取消与退款处理</h2>
      <p class="page-description">处理消费者取消申请、主动退款申请和退款状态跟踪</p>
      <div class="header-actions">
        <el-button 
          type="success" 
          @click="refreshOrders"
          :loading="loading"
        >
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-button 
          type="primary" 
          @click="batchProcess"
          :disabled="selectedOrders.length === 0"
        >
          <el-icon><Operation /></el-icon>
          批量处理 ({{ selectedOrders.length }})
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.pendingCancel }}</div>
          <div class="stat-label">待处理取消申请</div>
        </div>
        <el-icon class="stat-icon" color="#E6A23C"><Warning /></el-icon>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.pendingRefund }}</div>
          <div class="stat-label">待处理退款申请</div>
        </div>
        <el-icon class="stat-icon" color="#F56C6C"><Money /></el-icon>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.completedRefund }}</div>
          <div class="stat-label">已完成退款</div>
        </div>
        <el-icon class="stat-icon" color="#67C23A"><Check /></el-icon>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-number">¥{{ stats.totalRefundAmount }}</div>
          <div class="stat-label">本月退款总额</div>
        </div>
        <el-icon class="stat-icon" color="#409EFF"><TrendCharts /></el-icon>
      </el-card>
    </div>

    <el-card>
      <!-- 搜索和筛选 -->
      <div class="search-form">
        <el-form :model="searchForm" inline>
          <el-form-item label="订单号">
            <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
          </el-form-item>
          <el-form-item label="处理类型">
            <el-select v-model="searchForm.processType" placeholder="请选择处理类型" clearable>
              <el-option label="取消申请" value="cancel" />
              <el-option label="退款申请" value="refund" />
            </el-select>
          </el-form-item>
          <el-form-item label="处理状态">
            <el-select v-model="searchForm.status" placeholder="请选择处理状态" clearable>
              <el-option label="待处理" value="pending" />
              <el-option label="已同意" value="approved" />
              <el-option label="已拒绝" value="rejected" />
              <el-option label="已完成" value="completed" />
            </el-select>
          </el-form-item>
          <el-form-item label="申请时间">
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
        <el-table-column prop="orderNo" label="订单号" width="150" />
        <el-table-column prop="customerName" label="客户" width="120" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="processType" label="处理类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getProcessTypeTag(row.processType)">
              {{ getProcessTypeText(row.processType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderStatus" label="订单状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusTag(row.orderStatus)">
              {{ getOrderStatusText(row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="订单金额" width="120">
          <template #default="{ row }">
            <div class="amount-info">
              <div class="total-amount">¥{{ row.totalAmount }}</div>
              <div v-if="row.refundAmount" class="refund-amount">
                退款：¥{{ row.refundAmount }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="申请原因" min-width="200">
          <template #default="{ row }">
            <div class="reason-info">
              <div class="reason-text">{{ row.reason }}</div>
              <div v-if="row.detailReason" class="detail-reason">
                {{ row.detailReason }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="处理状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="150" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="text" @click="viewDetail(row)">查看详情</el-button>
              <template v-if="row.status === 'pending'">
                <el-button type="text" @click="approveRequest(row)" class="text-success">
                  同意
                </el-button>
                <el-button type="text" @click="rejectRequest(row)" class="text-danger">
                  拒绝
                </el-button>
              </template>
              <template v-else-if="row.processType === 'refund' && row.status === 'approved'">
                <el-button type="text" @click="processRefund(row)">处理退款</el-button>
                <el-button type="text" @click="completeRefundOrder(row)" class="text-success">
                  完成退款
                </el-button>
              </template>
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="申请详情" width="800px">
      <div v-if="currentOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="客户姓名">{{ currentOrder.customerName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.phone }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getOrderStatusTag(currentOrder.orderStatus)">
              {{ getOrderStatusText(currentOrder.orderStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理类型">
            <el-tag :type="getProcessTypeTag(currentOrder.processType)">
              {{ getProcessTypeText(currentOrder.processType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="getStatusTag(currentOrder.status)">
              {{ getStatusText(currentOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ currentOrder.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ currentOrder.applyTime }}</el-descriptions-item>
          <el-descriptions-item label="申请原因" :span="2">{{ currentOrder.reason }}</el-descriptions-item>
          <el-descriptions-item label="详细说明" :span="2">{{ currentOrder.detailReason || '无' }}</el-descriptions-item>
        </el-descriptions>
        
        <!-- 商品信息 -->
        <div style="margin-top: 20px;">
          <h4>商品信息</h4>
          <el-table :data="currentOrder.items" size="small">
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="spec" label="规格" width="120" />
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="price" label="单价" width="100">
              <template #default="{ row }">
                ¥{{ row.price }}
              </template>
            </el-table-column>
            <el-table-column label="小计" width="100">
              <template #default="{ row }">
                ¥{{ (row.price * row.quantity).toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 退款信息 -->
        <div v-if="currentOrder.processType === 'refund'" style="margin-top: 20px;">
          <h4>退款信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="退款类型">
              <el-tag :type="currentOrder.refundType === 'full' ? 'success' : 'warning'">
                {{ currentOrder.refundType === 'full' ? '全额退款' : '部分退款' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="退款金额">¥{{ currentOrder.refundAmount }}</el-descriptions-item>
            <el-descriptions-item label="退款原因" :span="2">{{ currentOrder.refundReason }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 凭证信息 -->
        <div v-if="currentOrder.attachments && currentOrder.attachments.length > 0" style="margin-top: 20px;">
          <h4>相关凭证</h4>
          <div class="attachments">
            <div v-for="(attachment, index) in currentOrder.attachments" :key="index" class="attachment-item">
              <el-image
                :src="attachment.url"
                :preview-src-list="currentOrder.attachments.map(a => a.url)"
                style="width: 100px; height: 100px; border-radius: 4px;"
                fit="cover"
              />
              <div class="attachment-name">{{ attachment.name }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 同意申请对话框 -->
    <el-dialog v-model="approveDialogVisible" title="同意申请" width="500px">
      <div v-if="currentOrder">
        <el-alert
          :title="getApproveAlertTitle()"
          :type="getApproveAlertType()"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
        />
        
        <el-form :model="approveForm" label-width="100px">
          <el-form-item label="处理备注">
            <el-input
              v-model="approveForm.remark"
              type="textarea"
              :rows="3"
              placeholder="处理备注（可选）"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmApprove" :loading="approveLoading">
          确认同意
        </el-button>
      </template>
    </el-dialog>

    <!-- 拒绝申请对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝申请" width="500px">
      <div v-if="currentOrder">
        <el-form :model="rejectForm" label-width="100px">
          <el-form-item label="拒绝原因" required>
            <el-select v-model="rejectForm.reason" placeholder="请选择拒绝原因" style="width: 100%">
              <el-option label="商品已制作完成，无法取消" value="product_ready" />
              <el-option label="商品已发货，无法取消" value="product_shipped" />
              <el-option label="订单已完成，无法取消" value="order_completed" />
              <el-option label="其他原因" value="other" />
            </el-select>
          </el-form-item>
          <el-form-item label="详细说明">
            <el-input
              v-model="rejectForm.detail"
              type="textarea"
              :rows="4"
              placeholder="请详细说明拒绝原因，此信息将通知给客户"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject" :loading="rejectLoading">
          确认拒绝
        </el-button>
      </template>
    </el-dialog>

    <!-- 主动退款对话框 -->
    <el-dialog v-model="refundDialogVisible" title="主动退款申请" width="600px">
      <div v-if="currentOrder">
        <el-descriptions :column="2" border style="margin-bottom: 20px;">
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="客户">{{ currentOrder.customerName }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ currentOrder.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getOrderStatusTag(currentOrder.orderStatus)">
              {{ getOrderStatusText(currentOrder.orderStatus) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-form :model="refundForm" label-width="120px">
          <el-form-item label="退款类型" required>
            <el-radio-group v-model="refundForm.refundType">
              <el-radio value="full">全额退款</el-radio>
              <el-radio value="partial">部分退款</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="退款金额" required>
            <el-input-number
              v-model="refundForm.refundAmount"
              :min="0"
              :max="currentOrder.totalAmount"
              :precision="2"
              placeholder="请输入退款金额"
              style="width: 100%"
            />
          </el-form-item>
          
          <el-form-item label="退款原因" required>
            <el-select v-model="refundForm.reason" placeholder="请选择退款原因" style="width: 100%">
              <el-option label="少发商品" value="missing_product" />
              <el-option label="商品损坏" value="damaged_product" />
              <el-option label="商品质量问题" value="quality_issue" />
              <el-option label="配送错误" value="delivery_error" />
              <el-option label="其他原因" value="other" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="详细说明">
            <el-input
              v-model="refundForm.detail"
              type="textarea"
              :rows="3"
              placeholder="请详细说明退款原因"
            />
          </el-form-item>
          
          <el-form-item label="上传凭证">
            <el-upload
              v-model:file-list="refundForm.attachments"
              action="#"
              list-type="picture-card"
              :auto-upload="false"
              :on-change="handleAttachmentChange"
              :on-remove="handleAttachmentRemove"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">支持上传图片，如少发商品照片、损坏商品照片等</div>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRefund" :loading="refundLoading">
          提交退款申请
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量处理对话框 -->
    <el-dialog v-model="batchDialogVisible" title="批量处理" width="600px">
      <div class="batch-content">
        <p>已选择 {{ selectedOrders.length }} 个申请进行批量处理：</p>
        <el-table :data="selectedOrders" size="small" max-height="300">
          <el-table-column prop="orderNo" label="订单号" />
          <el-table-column prop="customerName" label="客户" />
          <el-table-column prop="processType" label="类型">
            <template #default="{ row }">
              <el-tag :type="getProcessTypeTag(row.processType)" size="small">
                {{ getProcessTypeText(row.processType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              <el-tag :type="getStatusTag(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        
        <el-form :model="batchForm" label-width="100px" style="margin-top: 20px;">
          <el-form-item label="批量操作">
            <el-select v-model="batchForm.action" placeholder="请选择操作" style="width: 100%">
              <el-option label="批量同意" value="batch_approve" />
              <el-option label="批量拒绝" value="batch_reject" />
            </el-select>
          </el-form-item>
          <el-form-item label="处理备注">
            <el-input
              v-model="batchForm.remark"
              type="textarea"
              :rows="3"
              placeholder="批量处理备注（可选）"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBatchProcess" :loading="batchLoading">
          确认处理
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Operation, Warning, Money, Check, TrendCharts, Plus } from '@element-plus/icons-vue'
import { 
  getRefundPage, 
  getRefundDetail, 
  getRefundByOrderId, 
  submitRefundRequest, 
  processRefundRequest, 
  batchProcessRefundRequest, 
  getRefundStats, 
  completeRefund 
} from '@/api/orderRefund'

const loading = ref(false)
const approveLoading = ref(false)
const rejectLoading = ref(false)
const refundLoading = ref(false)
const batchLoading = ref(false)

// 对话框状态
const detailDialogVisible = ref(false)
const approveDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const refundDialogVisible = ref(false)
const batchDialogVisible = ref(false)

// 当前订单
const currentOrder = ref(null)
const selectedOrders = ref([])

// 统计数据
const stats = reactive({
  pendingCancel: 0,
  pendingRefund: 0,
  completedRefund: 0,
  totalRefundAmount: 0
})

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  processType: '',
  status: '',
  dateRange: []
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 同意表单
const approveForm = reactive({
  remark: ''
})

// 拒绝表单
const rejectForm = reactive({
  reason: '',
  detail: ''
})

// 退款表单
const refundForm = reactive({
  refundType: 'full',
  refundAmount: 0,
  reason: '',
  detail: '',
  attachments: []
})

// 批量处理表单
const batchForm = reactive({
  action: '',
  remark: ''
})

// 订单列表数据
const orderList = ref([])

// 筛选后的订单列表
const filteredOrderList = computed(() => {
  return orderList.value
})

// 获取处理类型标签类型
const getProcessTypeTag = (type) => {
  const tagMap = {
    'cancel': 'warning',
    'refund': 'danger'
  }
  return tagMap[type] || 'info'
}

// 获取处理类型文本
const getProcessTypeText = (type) => {
  const textMap = {
    'cancel': '取消申请',
    'refund': '退款申请'
  }
  return textMap[type] || '未知'
}

// 获取订单状态标签类型
const getOrderStatusTag = (status) => {
  const tagMap = {
    'pending': 'warning',
    'accepted': 'primary',
    'shipped': 'info',
    'completed': 'success',
    'cancelled': 'info'
  }
  return tagMap[status] || 'info'
}

// 获取订单状态文本
const getOrderStatusText = (status) => {
  const textMap = {
    'pending': '待接单',
    'accepted': '待发货',
    'shipped': '已发货',
    'completed': '已完成',
    'cancelled': '已取消'
  }
  return textMap[status] || '未知'
}

// 获取状态标签类型
const getStatusTag = (status) => {
  const tagMap = {
    'pending': 'warning',
    'approved': 'success',
    'rejected': 'danger',
    'completed': 'info'
  }
  return tagMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'pending': '待处理',
    'approved': '已同意',
    'rejected': '已拒绝',
    'completed': '已完成'
  }
  return textMap[status] || '未知'
}

// 获取同意提示标题
const getApproveAlertTitle = () => {
  if (currentOrder.value.processType === 'cancel') {
    return '同意取消订单'
  } else {
    return '同意退款申请'
  }
}

// 获取同意提示类型
const getApproveAlertType = () => {
  if (currentOrder.value.processType === 'cancel') {
    return 'warning'
  } else {
    return 'success'
  }
}

// 搜索
const handleSearch = async () => {
  loading.value = true
  try {
    const queryData = {
      orderNo: searchForm.orderNo,
      processType: searchForm.processType,
      status: searchForm.status,
      startTime: searchForm.dateRange && searchForm.dateRange.length === 2 ? searchForm.dateRange[0] : null,
      endTime: searchForm.dateRange && searchForm.dateRange.length === 2 ? searchForm.dateRange[1] : null,
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize
    }
    
    const response = await getRefundPage(queryData)
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
  searchForm.processType = ''
  searchForm.status = ''
  searchForm.dateRange = []
  handleSearch()
}

// 刷新订单
const refreshOrders = async () => {
  loading.value = true
  try {
    // 刷新统计数据
    await loadStats()
    
    // 刷新订单列表
    const queryData = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize
    }
    
    const response = await getRefundPage(queryData)
    if (response.code === 200) {
      orderList.value = response.data.records || []
      pagination.total = response.data.total || 0
      ElMessage.success('数据已刷新')
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

// 查看详情
const viewDetail = async (row) => {
  try {
    const response = await getRefundDetail(row.id)
    if (response.code === 200) {
      currentOrder.value = response.data
      detailDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取详情失败')
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败，请重试')
  }
}

// 同意申请
const approveRequest = (row) => {
  currentOrder.value = row
  approveForm.remark = ''
  approveDialogVisible.value = true
}

// 确认同意
const confirmApprove = async () => {
  approveLoading.value = true
  try {
    const processData = {
      refundId: currentOrder.value.id,
      action: 'approve',
      remark: approveForm.remark
    }
    
    const response = await processRefundRequest(processData)
    if (response.code === 200) {
      if (currentOrder.value.processType === 'cancel') {
        ElMessage.success('订单已取消，系统将自动退款')
      } else {
        ElMessage.success('退款申请已同意，等待处理')
      }
      
      approveDialogVisible.value = false
      // 刷新列表
      await refreshOrders()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败，请重试')
  } finally {
    approveLoading.value = false
  }
}

// 拒绝申请
const rejectRequest = (row) => {
  currentOrder.value = row
  rejectForm.reason = ''
  rejectForm.detail = ''
  rejectDialogVisible.value = true
}

// 确认拒绝
const confirmReject = async () => {
  if (!rejectForm.reason) {
    ElMessage.warning('请选择拒绝原因')
    return
  }
  
  rejectLoading.value = true
  try {
    const processData = {
      refundId: currentOrder.value.id,
      action: 'reject',
      rejectReason: rejectForm.reason,
      rejectDetail: rejectForm.detail
    }
    
    const response = await processRefundRequest(processData)
    if (response.code === 200) {
      ElMessage.success('申请已拒绝，客户将收到通知')
      rejectDialogVisible.value = false
      // 刷新列表
      await refreshOrders()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败，请重试')
  } finally {
    rejectLoading.value = false
  }
}

// 处理退款
const processRefund = (row) => {
  currentOrder.value = row
  refundForm.refundType = 'full'
  refundForm.refundAmount = row.totalAmount
  refundForm.reason = ''
  refundForm.detail = ''
  refundForm.attachments = []
  refundDialogVisible.value = true
}

// 确认退款
const confirmRefund = async () => {
  if (!refundForm.reason) {
    ElMessage.warning('请选择退款原因')
    return
  }
  
  if (refundForm.refundAmount <= 0) {
    ElMessage.warning('请输入有效的退款金额')
    return
  }
  
  refundLoading.value = true
  try {
    const refundData = {
      orderId: currentOrder.value.orderId,
      processType: 'refund',
      reason: refundForm.reason,
      detailReason: refundForm.detail,
      refundAmount: refundForm.refundAmount,
      refundType: refundForm.refundType,
      refundReason: refundForm.reason,
      attachments: refundForm.attachments
    }
    
    const response = await submitRefundRequest(refundData)
    if (response.code === 200) {
      ElMessage.success('退款申请已提交，等待审核')
      refundDialogVisible.value = false
      // 刷新列表
      await refreshOrders()
    } else {
      ElMessage.error(response.message || '提交失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败，请重试')
  } finally {
    refundLoading.value = false
  }
}

// 处理附件变化
const handleAttachmentChange = (file, fileList) => {
  refundForm.attachments = fileList
}

// 完成退款
const completeRefundOrder = async (row) => {
  try {
    await ElMessageBox.confirm('确定要完成退款吗？', '完成退款', {
      confirmButtonText: '确定完成',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await completeRefund(row.id)
    if (response.code === 200) {
      ElMessage.success('退款完成，客户将收到退款成功通知')
      // 刷新列表
      await refreshOrders()
    } else {
      ElMessage.error(response.message || '完成退款失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('完成退款失败:', error)
      ElMessage.error('完成退款失败，请重试')
    }
  }
}

// 批量处理
const batchProcess = () => {
  if (selectedOrders.value.length === 0) {
    ElMessage.warning('请选择要处理的申请')
    return
  }
  
  batchForm.action = ''
  batchForm.remark = ''
  batchDialogVisible.value = true
}

// 确认批量处理
const confirmBatchProcess = async () => {
  if (!batchForm.action) {
    ElMessage.warning('请选择批量操作')
    return
  }
  
  batchLoading.value = true
  try {
    const batchData = {
      refundIds: selectedOrders.value.map(order => order.id),
      action: batchForm.action,
      remark: batchForm.remark
    }
    
    const response = await batchProcessRefundRequest(batchData)
    if (response.code === 200) {
      selectedOrders.value = []
      ElMessage.success(`批量处理完成，成功处理 ${batchData.refundIds.length} 个申请`)
      batchDialogVisible.value = false
      // 刷新列表
      await refreshOrders()
    } else {
      ElMessage.error(response.message || '批量处理失败')
    }
  } catch (error) {
    console.error('批量处理失败:', error)
    ElMessage.error('批量处理失败，请重试')
  } finally {
    batchLoading.value = false
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

// 加载统计数据
const loadStats = async () => {
  try {
    const response = await getRefundStats()
    if (response.code === 200) {
      stats.pendingCancel = response.data.pendingCancel || 0
      stats.pendingRefund = response.data.pendingRefund || 0
      stats.completedRefund = response.data.completedRefund || 0
      stats.totalRefundAmount = response.data.totalRefundAmount || 0
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 初始化
onMounted(async () => {
  await loadStats()
  await refreshOrders()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
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

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.stat-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.stat-card :deep(.el-card__body) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
}

.stat-content {
  .stat-number {
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin-bottom: 4px;
  }
  
  .stat-label {
    font-size: 14px;
    color: #666;
  }
}

.stat-icon {
  font-size: 32px;
  opacity: 0.8;
}

/* 搜索表单 */
.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* 订单表格 */
.order-table {
  border-radius: 8px;
  overflow: hidden;
}

.amount-info {
  .total-amount {
    font-weight: 500;
    color: #333;
    margin-bottom: 2px;
  }
  
  .refund-amount {
    font-size: 12px;
    color: #f56c6c;
  }
}

.reason-info {
  .reason-text {
    font-weight: 500;
    color: #333;
    margin-bottom: 4px;
  }
  
  .detail-reason {
    font-size: 12px;
    color: #666;
    line-height: 1.4;
  }
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

/* 凭证信息 */
.attachments {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.attachment-item {
  text-align: center;
  
  .attachment-name {
    font-size: 12px;
    color: #666;
    margin-top: 4px;
  }
}

/* 上传提示 */
.upload-tip {
  font-size: 12px;
  color: #666;
  margin-top: 8px;
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
.text-success {
  color: #67C23A;
}

.text-danger {
  color: #F56C6C;
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
  
  .stats-cards {
    grid-template-columns: 1fr;
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

/* 上传组件样式 */
:deep(.el-upload--picture-card) {
  border-radius: 6px;
}

/* 加载状态 */
.el-loading-mask {
  border-radius: 8px;
}
</style>