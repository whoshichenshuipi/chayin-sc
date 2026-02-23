<template>
  <div class="page-container">
    <div class="page-header">
      <h2>订单管理</h2>
      <p class="page-description">处理新订单、管理订单状态和批量操作</p>
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
          @click="batchProcess"
          :disabled="selectedOrders.length === 0"
        >
          <el-icon><Operation /></el-icon>
          批量处理 ({{ selectedOrders.length }})
        </el-button>
      </div>
    </div>

    <!-- 新订单提醒 -->
    <el-alert
      v-if="newOrderCount > 0"
      :title="`您有 ${newOrderCount} 个新订单待处理`"
      type="warning"
      :closable="false"
      show-icon
      class="new-order-alert"
    >
      <template #default>
        <div class="alert-content">
          <span>请及时处理新订单，避免影响客户体验</span>
          <el-button type="primary" size="small" @click="viewNewOrders">
            立即查看
          </el-button>
        </div>
      </template>
    </el-alert>

    <el-card>
      <!-- 搜索和筛选 -->
      <div class="search-form">
        <el-form :model="searchForm" inline>
          <el-form-item label="订单号">
            <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
          </el-form-item>
          <el-form-item label="订单状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="待接单" value="pending" />
              <el-option label="待发货" value="accepted" />
              <el-option label="已发货" value="shipped" />
              <el-option label="已完成" value="completed" />
              <el-option label="已拒单" value="rejected" />
              <el-option label="已取消" value="cancelled" />
            </el-select>
          </el-form-item>
          <el-form-item label="下单时间">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
            />
          </el-form-item>
          <el-form-item label="客户昵称">
            <el-input v-model="searchForm.customerName" placeholder="请输入客户昵称" clearable />
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
        <el-table-column prop="createTime" label="下单时间" width="150" />
        <el-table-column prop="customerName" label="客户昵称" width="120" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="address" label="收货地址" min-width="200">
          <template #default="{ row }">
            <div class="address-info">
              <div class="address-text">{{ row.address }}</div>
              <div class="address-detail">{{ row.addressDetail }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="items" label="商品清单" min-width="250">
          <template #default="{ row }">
            <div class="order-items">
              <div v-for="item in row.items" :key="item.id" class="order-item">
                <div class="item-info">
                  <span class="item-name">{{ item.name }}</span>
                  <span class="item-spec">{{ item.spec }}</span>
                </div>
                <div class="item-quantity">x{{ item.quantity }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="订单金额" width="150">
          <template #default="{ row }">
            <div class="amount-info">
              <div class="total-amount">实付：¥{{ row.actualAmount }}</div>
              <div v-if="row.discountAmount > 0" class="discount-amount">
                优惠：¥{{ row.discountAmount }}
              </div>
              <div class="payment-method">{{ getPaymentMethodText(row.paymentMethod) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="text" @click="viewOrderDetail(row)">查看详情</el-button>
              <template v-if="row.status === 'pending'">
                <el-button type="text" @click="acceptOrder(row)" class="text-success">
                  接单
                </el-button>
                <el-button type="text" @click="rejectOrder(row)" class="text-danger">
                  拒单
                </el-button>
              </template>
              <template v-else-if="row.status === 'accepted'">
                <el-button type="text" @click="shipOrder(row)">发货</el-button>
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

    <!-- 订单详情对话框 -->
    <el-dialog v-model="orderDetailVisible" title="订单详情" width="900px" top="5vh">
      <div v-if="currentOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ currentOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="客户昵称">{{ currentOrder.customerName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.phone }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusTag(currentOrder.status)">
              {{ getStatusText(currentOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ getPaymentMethodText(currentOrder.paymentMethod) }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">
            <div class="address-display">
              <div>{{ currentOrder.address }}</div>
              <div class="address-detail">{{ currentOrder.addressDetail }}</div>
            </div>
          </el-descriptions-item>
        </el-descriptions>
        
        <div style="margin-top: 20px;">
          <h4>商品清单</h4>
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
        
        <div style="margin-top: 20px; text-align: right;">
          <div class="amount-summary">
            <p>商品总额：¥{{ currentOrder.totalAmount }}</p>
            <p v-if="currentOrder.discountAmount > 0">优惠金额：-¥{{ currentOrder.discountAmount }}</p>
            <p><strong>实付金额：¥{{ currentOrder.actualAmount }}</strong></p>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 拒单对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒单原因" width="500px">
      <el-form :model="rejectForm" label-width="100px">
        <el-form-item label="拒单原因" required>
          <el-select v-model="rejectForm.reason" placeholder="请选择拒单原因" style="width: 100%">
            <el-option label="商品售罄" value="out_of_stock" />
            <el-option label="无法配送该地址" value="delivery_unavailable" />
            <el-option label="店铺休息" value="store_closed" />
            <el-option label="其他原因" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input
            v-model="rejectForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请详细说明拒单原因，此信息将通知给客户"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject" :loading="rejectLoading">
          确认拒单
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量处理对话框 -->
    <el-dialog v-model="batchDialogVisible" title="批量处理订单" width="600px">
      <div class="batch-content">
        <p>已选择 {{ selectedOrders.length }} 个订单进行批量处理：</p>
        <el-table :data="selectedOrders" size="small" max-height="300">
          <el-table-column prop="orderNo" label="订单号" />
          <el-table-column prop="customerName" label="客户" />
          <el-table-column prop="status" label="当前状态">
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
              <el-option label="标记为已查看" value="mark_viewed" />
              <el-option label="批量接单" value="batch_accept" />
              <el-option label="批量发货" value="batch_ship" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="batchForm.action === 'batch_accept'" label="备注">
            <el-input
              v-model="batchForm.remark"
              type="textarea"
              :rows="3"
              placeholder="批量接单备注（可选）"
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
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Operation } from '@element-plus/icons-vue'

const loading = ref(false)
const rejectLoading = ref(false)
const batchLoading = ref(false)

// 对话框状态
const orderDetailVisible = ref(false)
const rejectDialogVisible = ref(false)
const batchDialogVisible = ref(false)

// 当前订单
const currentOrder = ref(null)
const selectedOrders = ref([])

// 新订单提醒
const newOrderCount = ref(3)
const soundEnabled = ref(true)

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  status: '',
  dateRange: [],
  customerName: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 拒单表单
const rejectForm = reactive({
  reason: '',
  remark: ''
})

// 批量处理表单
const batchForm = reactive({
  action: '',
  remark: ''
})

// 订单列表数据
const orderList = ref([
  {
    id: 1,
    orderNo: 'ORD202401150001',
    customerName: '张先生',
    phone: '13800138001',
    address: '北京市朝阳区三里屯街道',
    addressDetail: '123号 2单元 301室',
    totalAmount: 45.00,
    discountAmount: 5.00,
    actualAmount: 40.00,
    paymentMethod: 'wechat',
    status: 'pending',
    createTime: '2024-01-15 10:30',
    isNew: true,
    items: [
      { id: 1, name: '经典珍珠奶茶', spec: '大杯', quantity: 2, price: 15.00 },
      { id: 2, name: '芒果果茶', spec: '中杯', quantity: 1, price: 18.00 }
    ]
  },
  {
    id: 2,
    orderNo: 'ORD202401150002',
    customerName: '李女士',
    phone: '13800138002',
    address: '上海市浦东新区陆家嘴',
    addressDetail: '456号 1单元 201室',
    totalAmount: 32.50,
    discountAmount: 0.00,
    actualAmount: 32.50,
    paymentMethod: 'alipay',
    status: 'accepted',
    createTime: '2024-01-15 09:45',
    isNew: false,
    items: [
      { id: 3, name: '拿铁咖啡', spec: '大杯', quantity: 1, price: 22.00 },
      { id: 4, name: '小食套餐', spec: '标准', quantity: 1, price: 10.50 }
    ]
  },
  {
    id: 3,
    orderNo: 'ORD202401150003',
    customerName: '王先生',
    phone: '13800138003',
    address: '广州市天河区珠江新城',
    addressDetail: '789号 3单元 501室',
    totalAmount: 28.00,
    discountAmount: 3.00,
    actualAmount: 25.00,
    paymentMethod: 'wechat',
    status: 'pending',
    createTime: '2024-01-15 09:15',
    isNew: true,
    items: [
      { id: 5, name: '柠檬茶', spec: '中杯', quantity: 2, price: 14.00 }
    ]
  },
  {
    id: 4,
    orderNo: 'ORD202401150004',
    customerName: '赵女士',
    phone: '13800138004',
    address: '深圳市南山区科技园',
    addressDetail: '101号 2单元 401室',
    totalAmount: 55.00,
    discountAmount: 10.00,
    actualAmount: 45.00,
    paymentMethod: 'alipay',
    status: 'shipped',
    createTime: '2024-01-15 08:30',
    isNew: false,
    items: [
      { id: 6, name: '奶茶套餐', spec: '标准', quantity: 1, price: 35.00 },
      { id: 7, name: '小食', spec: '标准', quantity: 1, price: 20.00 }
    ]
  }
])

// 筛选后的订单列表
const filteredOrderList = computed(() => {
  let filtered = orderList.value

  if (searchForm.orderNo) {
    filtered = filtered.filter(item => 
      item.orderNo.toLowerCase().includes(searchForm.orderNo.toLowerCase())
    )
  }

  if (searchForm.status) {
    filtered = filtered.filter(item => item.status === searchForm.status)
  }

  if (searchForm.customerName) {
    filtered = filtered.filter(item => 
      item.customerName.toLowerCase().includes(searchForm.customerName.toLowerCase())
    )
  }

  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    const [start, end] = searchForm.dateRange
    filtered = filtered.filter(item => {
      const orderDate = new Date(item.createTime)
      return orderDate >= start && orderDate <= end
    })
  }

  return filtered
})

// 获取状态标签类型
const getStatusTag = (status) => {
  const tagMap = {
    'pending': 'warning',
    'accepted': 'primary',
    'shipped': 'info',
    'completed': 'success',
    'rejected': 'danger',
    'cancelled': 'info'
  }
  return tagMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'pending': '待接单',
    'accepted': '待发货',
    'shipped': '已发货',
    'completed': '已完成',
    'rejected': '已拒单',
    'cancelled': '已取消'
  }
  return textMap[status] || '未知'
}

// 获取支付方式文本
const getPaymentMethodText = (method) => {
  const textMap = {
    'wechat': '微信支付',
    'alipay': '支付宝',
    'cash': '现金支付',
    'card': '银行卡'
  }
  return textMap[method] || '未知'
}

// 播放新订单提醒声音
const playNotificationSound = () => {
  if (soundEnabled.value) {
    // 这里可以播放自定义的提醒声音
    console.log('播放新订单提醒声音')
  }
}

// 显示新订单提醒
const showNewOrderNotification = () => {
  if (newOrderCount.value > 0) {
    playNotificationSound()
  }
}

// 搜索
const handleSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('搜索完成')
  }, 1000)
}

// 重置搜索
const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.status = ''
  searchForm.dateRange = []
  searchForm.customerName = ''
  handleSearch()
}

// 刷新订单
const refreshOrders = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    newOrderCount.value = Math.floor(Math.random() * 5) + 1
    showNewOrderNotification()
    ElMessage.success('订单已刷新')
  }, 1000)
}

// 查看新订单
const viewNewOrders = () => {
  searchForm.status = 'pending'
  handleSearch()
}

// 查看订单详情
const viewOrderDetail = (row) => {
  currentOrder.value = row
  orderDetailVisible.value = true
}

// 接单
const acceptOrder = async (row) => {
  try {
    await ElMessageBox.confirm('确定要接受这个订单吗？', '接单确认', {
      confirmButtonText: '确定接单',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    row.status = 'accepted'
    row.isNew = false
    newOrderCount.value = Math.max(0, newOrderCount.value - 1)
    ElMessage.success('订单已接受，请及时准备商品')
  } catch {
    // 用户取消
  }
}

// 拒单
const rejectOrder = (row) => {
  currentOrder.value = row
  rejectForm.reason = ''
  rejectForm.remark = ''
  rejectDialogVisible.value = true
}

// 确认拒单
const confirmReject = async () => {
  if (!rejectForm.reason) {
    ElMessage.warning('请选择拒单原因')
    return
  }
  
  rejectLoading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    currentOrder.value.status = 'rejected'
    currentOrder.value.rejectReason = rejectForm.reason
    currentOrder.value.rejectRemark = rejectForm.remark
    currentOrder.value.isNew = false
    newOrderCount.value = Math.max(0, newOrderCount.value - 1)
    
    ElMessage.success('订单已拒单，客户将收到通知')
    rejectDialogVisible.value = false
  } catch (error) {
    ElMessage.error('拒单失败，请重试')
  } finally {
    rejectLoading.value = false
  }
}

// 发货
const shipOrder = async (row) => {
  try {
    await ElMessageBox.confirm('确定要发货吗？', '发货确认', {
      confirmButtonText: '确定发货',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    row.status = 'shipped'
    ElMessage.success('订单已发货')
  } catch {
    // 用户取消
  }
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedOrders.value = selection
}

// 批量处理
const batchProcess = () => {
  if (selectedOrders.value.length === 0) {
    ElMessage.warning('请选择要处理的订单')
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
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    let successCount = 0
    selectedOrders.value.forEach(order => {
      switch (batchForm.action) {
        case 'mark_viewed':
          order.isNew = false
          successCount++
          break
        case 'batch_accept':
          if (order.status === 'pending') {
            order.status = 'accepted'
            order.isNew = false
            successCount++
          }
          break
        case 'batch_ship':
          if (order.status === 'accepted') {
            order.status = 'shipped'
            successCount++
          }
          break
      }
    })
    
    newOrderCount.value = Math.max(0, newOrderCount.value - selectedOrders.value.filter(o => o.isNew).length)
    selectedOrders.value = []
    
    ElMessage.success(`批量处理完成，成功处理 ${successCount} 个订单`)
    batchDialogVisible.value = false
  } catch (error) {
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

// 初始化
onMounted(() => {
  pagination.total = orderList.value.length
  showNewOrderNotification()
})

// 清理
onUnmounted(() => {
  // 清理定时器等资源
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

/* 新订单提醒 */
.new-order-alert {
  margin-bottom: 20px;
  border-radius: 8px;
}

.alert-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.address-info {
  .address-text {
    font-weight: 500;
    color: #333;
    margin-bottom: 2px;
  }
  
  .address-detail {
    font-size: 12px;
    color: #666;
  }
}

.order-items {
  .order-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 4px;
    padding: 4px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
      margin-bottom: 0;
    }
  }
  
  .item-info {
    flex: 1;
    
    .item-name {
      font-weight: 500;
      color: #333;
      margin-right: 8px;
    }
    
    .item-spec {
      font-size: 12px;
      color: #666;
    }
  }
  
  .item-quantity {
    font-weight: 500;
    color: #666;
  }
}

.amount-info {
  .total-amount {
    font-weight: 500;
    color: #333;
    margin-bottom: 2px;
  }
  
  .discount-amount {
    font-size: 12px;
    color: #f56c6c;
    margin-bottom: 2px;
  }
  
  .payment-method {
    font-size: 12px;
    color: #666;
  }
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

/* 地址显示 */
.address-display {
  .address-detail {
    font-size: 12px;
    color: #666;
    margin-top: 4px;
  }
}

/* 金额汇总 */
.amount-summary {
  p {
    margin: 4px 0;
    
    &:last-child {
      font-size: 16px;
      font-weight: 600;
      color: #f56c6c;
    }
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

/* 加载状态 */
.el-loading-mask {
  border-radius: 8px;
}
</style>
