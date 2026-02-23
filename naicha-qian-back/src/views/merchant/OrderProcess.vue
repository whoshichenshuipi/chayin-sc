<template>
  <div class="page-container">
    <div class="page-header">
      <h2>订单处理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="refreshData">刷新</el-button>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <div class="statistics-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.pendingCount || 0 }}</div>
              <div class="stat-label">待处理订单</div>
            </div>
            <div class="stat-icon pending">
              <el-icon><Clock /></el-icon>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.acceptedCount || 0 }}</div>
              <div class="stat-label">已接单订单</div>
            </div>
            <div class="stat-icon accepted">
              <el-icon><Check /></el-icon>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.makingCount || 0 }}</div>
              <div class="stat-label">制作中订单</div>
            </div>
            <div class="stat-icon making">
              <el-icon><Tools /></el-icon>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.todayProcessedCount || 0 }}</div>
              <div class="stat-label">今日已处理</div>
            </div>
            <div class="stat-icon processed">
              <el-icon><SuccessFilled /></el-icon>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <el-card>
      <!-- 搜索条件 -->
      <div class="search-form">
        <el-form :model="searchForm" inline>
          <el-form-item label="订单号">
            <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
          </el-form-item>
          <el-form-item label="订单状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="已支付" value="1" />
              <el-option label="已接单" value="2" />
              <el-option label="制作中" value="3" />
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
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 批量操作 -->
      <div class="batch-actions" v-if="selectedOrders.length > 0">
        <el-alert 
          :title="`已选择 ${selectedOrders.length} 个订单`" 
          type="info" 
          show-icon 
          :closable="false"
        />
        <div class="batch-buttons">
          <el-button type="primary" @click="batchAccept">批量接单</el-button>
          <el-button type="success" @click="batchStartMaking">批量开始制作</el-button>
          <el-button type="warning" @click="batchFinishMaking">批量制作完成</el-button>
          <el-button @click="clearSelection">取消选择</el-button>
        </div>
      </div>
      
      <!-- 订单列表 -->
      <el-table 
        :data="orderList" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        row-key="id"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="orderNo" label="订单号" width="150" />
        <el-table-column prop="userName" label="客户" width="100" />
        <el-table-column prop="userPhone" label="联系电话" width="130" />
        <el-table-column prop="items" label="商品" min-width="200">
          <template #default="{ row }">
            <div v-for="item in row.items" :key="item.id" class="order-item">
              <span>{{ item.productName }} x{{ item.quantity }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="payAmount" label="金额" width="100">
          <template #default="{ row }">
            ¥{{ row.payAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" width="150" />
        <el-table-column label="操作" width="300">
          <template #default="{ row }">
            <el-button type="text" @click="viewOrder(row)">查看</el-button>
            <el-button 
              type="primary" 
              @click="acceptOrder(row)"
              v-if="row.status === 1"
              size="small"
            >
              接单
            </el-button>
            <el-button 
              type="success" 
              @click="startMaking(row)"
              v-if="row.status === 2"
              size="small"
            >
              开始制作
            </el-button>
            <el-button 
              type="warning" 
              @click="finishMaking(row)"
              v-if="row.status === 3"
              size="small"
            >
              制作完成
            </el-button>
            <el-button 
              type="info" 
              @click="showProcessDialog(row)"
              v-if="row.status === 1 || row.status === 2 || row.status === 3"
              size="small"
            >
              处理
            </el-button>
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
    <el-dialog v-model="orderDetailVisible" title="订单详情" width="800px">
      <div v-if="currentOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="客户姓名">{{ currentOrder.userName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.userPhone }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusTag(currentOrder.status)">
              {{ getStatusText(currentOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ currentOrder.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="配送地址" :span="2">{{ currentOrder.deliveryAddress }}</el-descriptions-item>
          <el-descriptions-item label="订单总金额">¥{{ currentOrder.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="优惠金额">¥{{ currentOrder.discountAmount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="实付金额">¥{{ currentOrder.payAmount }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ currentOrder.payMethod || '未支付' }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ currentOrder.payTime || '未支付' }}</el-descriptions-item>
          <el-descriptions-item label="发货时间">{{ currentOrder.deliveryTime || '未发货' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
        
        <div style="margin-top: 20px;">
          <h4>商品清单</h4>
          <el-table :data="currentOrder.items" size="small">
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="price" label="单价" width="100">
              <template #default="{ row }">
                ¥{{ row.price }}
              </template>
            </el-table-column>
            <el-table-column label="小计" width="100">
              <template #default="{ row }">
                ¥{{ row.totalAmount }}
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <div style="margin-top: 20px; text-align: right;">
          <p><strong>订单总额：¥{{ currentOrder.payAmount }}</strong></p>
        </div>
      </div>
    </el-dialog>
    
    <!-- 订单处理对话框 -->
    <el-dialog v-model="processDialogVisible" title="订单处理" width="500px">
      <el-form :model="processForm" label-width="100px">
        <el-form-item label="订单号">
          <el-input v-model="processForm.orderNo" disabled />
        </el-form-item>
        <el-form-item label="处理类型">
          <el-select v-model="processForm.processType" placeholder="请选择处理类型">
            <el-option label="接单" :value="1" />
            <el-option label="开始制作" :value="2" />
            <el-option label="制作完成" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input 
            v-model="processForm.processRemark" 
            type="textarea" 
            :rows="3"
            placeholder="请输入处理备注"
          />
        </el-form-item>
        <el-form-item label="预计完成时间" v-if="processForm.processType === 2">
          <el-date-picker
            v-model="processForm.estimatedTime"
            type="datetime"
            placeholder="选择预计完成时间"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmProcess">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Check, Tools, SuccessFilled } from '@element-plus/icons-vue'
import { 
  getPendingOrderPage, 
  getOrderDetail,
  acceptOrder as acceptOrderAPI, 
  startMaking as startMakingAPI, 
  finishMaking as finishMakingAPI, 
  batchProcessOrders,
  getOrderProcessStatistics 
} from '@/api/orderProcess'

const loading = ref(false)
const orderDetailVisible = ref(false)
const processDialogVisible = ref(false)
const currentOrder = ref(null)
const selectedOrders = ref([])
const statistics = ref({})

const searchForm = reactive({
  orderNo: '',
  status: '',
  dateRange: []
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

const orderList = ref([])

const processForm = reactive({
  orderId: null,
  orderNo: '',
  processType: 1,
  processRemark: '',
  estimatedTime: null
})

// 订单状态映射
const statusMap = {
  0: { text: '待支付', type: 'warning' },
  1: { text: '已支付', type: 'info' },
  2: { text: '已接单', type: 'primary' },
  3: { text: '制作中', type: 'primary' },
  4: { text: '已发货', type: 'success' },
  5: { text: '已完成', type: 'success' },
  6: { text: '已取消', type: 'danger' },
  7: { text: '已退款', type: 'danger' }
}

const getStatusTag = (status) => {
  return statusMap[status]?.type || 'info'
}

const getStatusText = (status) => {
  return statusMap[status]?.text || '未知'
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const response = await getOrderProcessStatistics()
    if (response.code === 200) {
      statistics.value = response.data
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 加载订单列表
const loadOrderList = async () => {
  loading.value = true
  try {
    const queryData = {
      orderNo: searchForm.orderNo,
      status: searchForm.status ? parseInt(searchForm.status) : null,
      startTime: searchForm.dateRange && searchForm.dateRange[0] ? searchForm.dateRange[0] : null,
      endTime: searchForm.dateRange && searchForm.dateRange[1] ? searchForm.dateRange[1] : null,
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize
    }

    const response = await getPendingOrderPage(queryData)
    if (response.code === 200) {
      orderList.value = response.data.records || []
      pagination.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取订单列表失败')
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  loadOrderList()
}

const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.status = ''
  searchForm.dateRange = []
  pagination.currentPage = 1
  loadOrderList()
}

const refreshData = () => {
  loadStatistics()
  loadOrderList()
}

const viewOrder = async (row) => {
  try {
    const response = await getOrderDetail(row.id)
    if (response.code === 200) {
      currentOrder.value = response.data
      orderDetailVisible.value = true
    } else {
      ElMessage.error(response.message || '获取订单详情失败')
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  }
}

const acceptOrder = async (row) => {
  try {
    await ElMessageBox.confirm('确定要接单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const processData = {
      orderId: row.id,
      processType: 1,
      processRemark: '订单已接单'
    }
    
    const response = await acceptOrderAPI(processData)
    if (response.code === 200) {
      ElMessage.success('接单成功')
      refreshData()
    } else {
      ElMessage.error(response.message || '接单失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('接单失败:', error)
      ElMessage.error('接单失败')
    }
  }
}

const startMaking = async (row) => {
  try {
    await ElMessageBox.confirm('确定要开始制作吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const processData = {
      orderId: row.id,
      processType: 2,
      processRemark: '开始制作'
    }
    
    const response = await startMakingAPI(processData)
    if (response.code === 200) {
      ElMessage.success('开始制作')
      refreshData()
    } else {
      ElMessage.error(response.message || '开始制作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('开始制作失败:', error)
      ElMessage.error('开始制作失败')
    }
  }
}

const finishMaking = async (row) => {
  try {
    await ElMessageBox.confirm('确定制作完成吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const processData = {
      orderId: row.id,
      processType: 3,
      processRemark: '制作完成'
    }
    
    const response = await finishMakingAPI(processData)
    if (response.code === 200) {
      ElMessage.success('制作完成')
      refreshData()
    } else {
      ElMessage.error(response.message || '制作完成失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('制作完成失败:', error)
      ElMessage.error('制作完成失败')
    }
  }
}

const showProcessDialog = (row) => {
  processForm.orderId = row.id
  processForm.orderNo = row.orderNo
  processForm.processType = row.status === 1 ? 1 : row.status === 2 ? 2 : 3
  processForm.processRemark = ''
  processForm.estimatedTime = null
  processDialogVisible.value = true
}

const confirmProcess = async () => {
  try {
    let response
    const processData = {
      orderId: processForm.orderId,
      processType: processForm.processType,
      processRemark: processForm.processRemark
    }
    
    switch (processForm.processType) {
      case 1:
        response = await acceptOrderAPI(processData)
        break
      case 2:
        response = await startMakingAPI(processData)
        break
      case 3:
        response = await finishMakingAPI(processData)
        break
      default:
        ElMessage.error('未知的处理类型')
        return
    }
    
    if (response.code === 200) {
      ElMessage.success('处理成功')
      processDialogVisible.value = false
      refreshData()
    } else {
      ElMessage.error(response.message || '处理失败')
    }
  } catch (error) {
    console.error('处理失败:', error)
    ElMessage.error('处理失败')
  }
}

// 批量操作
const handleSelectionChange = (selection) => {
  selectedOrders.value = selection
}

const clearSelection = () => {
  selectedOrders.value = []
}

const batchAccept = async () => {
  if (selectedOrders.value.length === 0) {
    ElMessage.warning('请选择要处理的订单')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要批量接单 ${selectedOrders.value.length} 个订单吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const orderIds = selectedOrders.value.map(order => order.id)
    const response = await batchProcessOrders(orderIds, 1)
    
    if (response.code === 200) {
      ElMessage.success('批量接单成功')
      clearSelection()
      refreshData()
    } else {
      ElMessage.error(response.message || '批量接单失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量接单失败:', error)
      ElMessage.error('批量接单失败')
    }
  }
}

const batchStartMaking = async () => {
  if (selectedOrders.value.length === 0) {
    ElMessage.warning('请选择要处理的订单')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要批量开始制作 ${selectedOrders.value.length} 个订单吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const orderIds = selectedOrders.value.map(order => order.id)
    const response = await batchProcessOrders(orderIds, 2)
    
    if (response.code === 200) {
      ElMessage.success('批量开始制作成功')
      clearSelection()
      refreshData()
    } else {
      ElMessage.error(response.message || '批量开始制作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量开始制作失败:', error)
      ElMessage.error('批量开始制作失败')
    }
  }
}

const batchFinishMaking = async () => {
  if (selectedOrders.value.length === 0) {
    ElMessage.warning('请选择要处理的订单')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要批量制作完成 ${selectedOrders.value.length} 个订单吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const orderIds = selectedOrders.value.map(order => order.id)
    const response = await batchProcessOrders(orderIds, 3)
    
    if (response.code === 200) {
      ElMessage.success('批量制作完成成功')
      clearSelection()
      refreshData()
    } else {
      ElMessage.error(response.message || '批量制作完成失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量制作完成失败:', error)
      ElMessage.error('批量制作完成失败')
    }
  }
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  loadOrderList()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  loadOrderList()
}

onMounted(() => {
  refreshData()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.statistics-cards {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.stat-icon {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
}

.stat-icon.pending {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
}

.stat-icon.accepted {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
}

.stat-icon.making {
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
}

.stat-icon.processed {
  background: linear-gradient(135deg, #d299c2 0%, #fef9d7 100%);
}

.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 4px;
}

.batch-actions {
  margin-bottom: 20px;
  padding: 15px;
  background: #f0f9ff;
  border-radius: 4px;
  border: 1px solid #e1f5fe;
}

.batch-buttons {
  margin-top: 15px;
  display: flex;
  gap: 10px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.order-item {
  margin-bottom: 5px;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .statistics-cards .el-col {
    margin-bottom: 15px;
  }
  
  .batch-buttons {
    flex-wrap: wrap;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
