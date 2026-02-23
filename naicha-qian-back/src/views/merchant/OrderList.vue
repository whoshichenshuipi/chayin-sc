<template>
  <div class="page-container">
    <div class="page-header">
      <h2>订单管理</h2>
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
              <el-option label="待支付" value="0" />
              <el-option label="已支付" value="1" />
              <el-option label="已接单" value="2" />
              <el-option label="制作中" value="3" />
              <el-option label="已发货" value="4" />
              <el-option label="已完成" value="5" />
              <el-option label="已取消" value="6" />
              <el-option label="已退款" value="7" />
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
      
      <!-- 订单列表 -->
      <el-table :data="orderList" v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="150" />
        <el-table-column prop="userName" label="客户" width="100" />
        <el-table-column prop="userPhone" label="联系电话" width="130" />
        <el-table-column prop="items" label="商品" min-width="200">
          <template #default="{ row }">
            <div v-if="row.items && row.items.length > 0">
              <div v-for="item in row.items" :key="item.id || item.productId" class="order-item">
                <span>{{ item.productName || '商品' }} x{{ item.quantity || 1 }}</span>
              </div>
            </div>
            <span v-else class="text-muted">暂无商品</span>
          </template>
        </el-table-column>
        <el-table-column prop="payAmount" label="金额" width="100">
          <template #default="{ row }">
            ¥{{ (row.payAmount || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button type="text" @click="viewOrder(row)">查看</el-button>
            <el-button 
              type="text" 
              @click="processOrderAction(row)"
              v-if="row.status === 1"
            >
              接单
            </el-button>
            <el-button 
              type="text" 
              @click="shipOrderAction(row)"
              v-if="row.status === 2 || row.status === 3"
            >
              发货
            </el-button>
            <el-button 
              type="text" 
              @click="completeOrderAction(row)"
              v-if="row.status === 4"
            >
              完成
            </el-button>
            <el-button 
              type="text" 
              @click="cancelOrderAction(row)"
              v-if="row.status === 0 || row.status === 1"
            >
              取消
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
          <el-descriptions-item label="下单时间">{{ formatDateTime(currentOrder.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="配送地址" :span="2">{{ currentOrder.deliveryAddress || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="订单总金额">¥{{ (currentOrder.totalAmount || 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="优惠金额">¥{{ (currentOrder.discountAmount || 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="实付金额">¥{{ (currentOrder.payAmount || 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ getPayMethodText(currentOrder.payMethod) }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ formatDateTime(currentOrder.payTime) || '未支付' }}</el-descriptions-item>
          <el-descriptions-item label="发货时间">{{ formatDateTime(currentOrder.deliveryTime) || '未发货' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
        
        <div style="margin-top: 20px;">
          <h4>商品清单</h4>
          <el-table :data="currentOrder.items || []" size="small">
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="price" label="单价" width="100">
              <template #default="{ row }">
                ¥{{ (row.price || 0).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="小计" width="100">
              <template #default="{ row }">
                ¥{{ (row.totalAmount || 0).toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <div style="margin-top: 20px; text-align: right;">
          <p><strong>订单总额：¥{{ (currentOrder.payAmount || 0).toFixed(2) }}</strong></p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderPage, getOrderDetail, processOrder, shipOrder, completeOrder, cancelOrder } from '@/api/order'

const loading = ref(false)
const orderDetailVisible = ref(false)
const currentOrder = ref(null)

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

// 支付方式文本映射
const getPayMethodText = (payMethod) => {
  const payMethodMap = {
    'wechat': '微信支付',
    'alipay': '支付宝',
    'wallet': '余额支付'
  }
  return payMethod ? (payMethodMap[payMethod] || payMethod) : '未支付'
}

// 格式化时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  if (typeof dateTime === 'string') {
    return dateTime.replace('T', ' ').substring(0, 19)
  }
  if (dateTime instanceof Date) {
    return dateTime.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    }).replace(/\//g, '-')
  }
  return dateTime
}

// 格式化订单数据
const formatOrder = (order) => {
  return {
    ...order,
    createdAt: formatDateTime(order.createdAt),
    payTime: formatDateTime(order.payTime),
    deliveryTime: formatDateTime(order.deliveryTime),
    updatedAt: formatDateTime(order.updatedAt),
    // 确保 items 存在
    items: order.items || []
  }
}

// 加载订单列表
const loadOrderList = async () => {
  loading.value = true
  try {
    const queryData = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize
    }
    
    // 订单号搜索
    if (searchForm.orderNo && searchForm.orderNo.trim()) {
      queryData.orderNo = searchForm.orderNo.trim()
    }
    
    // 状态筛选
    if (searchForm.status) {
      queryData.status = parseInt(searchForm.status)
    }
    
    // 时间范围筛选
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      const formatTime = (date) => {
        if (!date) return null
        if (typeof date === 'string') {
          return date.replace('T', ' ')
        }
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const hours = String(date.getHours()).padStart(2, '0')
        const minutes = String(date.getMinutes()).padStart(2, '0')
        const seconds = String(date.getSeconds()).padStart(2, '0')
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
      }
      
      queryData.startTime = formatTime(searchForm.dateRange[0])
      queryData.endTime = formatTime(searchForm.dateRange[1])
    }

    const response = await getOrderPage(queryData)
    
    // 处理响应数据
    if (response && response.code === 200) {
      const pageData = response.data || response
      const records = pageData.records || pageData.list || (Array.isArray(pageData) ? pageData : [])
      
      // 格式化订单数据
      orderList.value = records.map(formatOrder)
      pagination.total = pageData.total || records.length
    } else {
      const errorMsg = response?.message || '获取订单列表失败'
      ElMessage.error(errorMsg)
      orderList.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '获取订单列表失败'
    ElMessage.error(errorMsg)
    orderList.value = []
    pagination.total = 0
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

const viewOrder = async (row) => {
  try {
    const response = await getOrderDetail(row.id)
    if (response && response.code === 200) {
      const orderData = response.data || response
      currentOrder.value = formatOrder(orderData)
      orderDetailVisible.value = true
    } else {
      const errorMsg = response?.message || '获取订单详情失败'
      ElMessage.error(errorMsg)
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '获取订单详情失败'
    ElMessage.error(errorMsg)
  }
}

const processOrderAction = async (row) => {
  try {
    await ElMessageBox.confirm('确定要接单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await processOrder(row.id)
    if (response && response.code === 200) {
      ElMessage.success('接单成功')
      // 重新加载订单列表以同步数据
      await loadOrderList()
    } else {
      const errorMsg = response?.message || error.response?.data?.message || '接单失败'
      ElMessage.error(errorMsg)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('接单失败:', error)
      const errorMsg = error.response?.data?.message || error.message || '接单失败'
      ElMessage.error(errorMsg)
    }
  }
}

const shipOrderAction = async (row) => {
  try {
    await ElMessageBox.confirm('确定要发货吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await shipOrder(row.id)
    if (response && response.code === 200) {
      ElMessage.success('发货成功')
      // 重新加载订单列表以同步数据
      await loadOrderList()
      // 如果订单详情对话框打开，也需要刷新
      if (orderDetailVisible.value && currentOrder.value?.id === row.id) {
        await viewOrder(row)
      }
    } else {
      const errorMsg = response?.message || error.response?.data?.message || '发货失败'
      ElMessage.error(errorMsg)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发货失败:', error)
      const errorMsg = error.response?.data?.message || error.message || '发货失败'
      ElMessage.error(errorMsg)
    }
  }
}

const completeOrderAction = async (row) => {
  try {
    await ElMessageBox.confirm('确定要完成这个订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await completeOrder(row.id)
    if (response && response.code === 200) {
      ElMessage.success('订单完成')
      // 重新加载订单列表以同步数据
      await loadOrderList()
      // 如果订单详情对话框打开，也需要刷新
      if (orderDetailVisible.value && currentOrder.value?.id === row.id) {
        await viewOrder(row)
      }
    } else {
      const errorMsg = response?.message || error.response?.data?.message || '订单完成失败'
      ElMessage.error(errorMsg)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('完成订单失败:', error)
      const errorMsg = error.response?.data?.message || error.message || '完成订单失败'
      ElMessage.error(errorMsg)
    }
  }
}

const cancelOrderAction = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？取消后无法恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await cancelOrder(row.id)
    if (response && response.code === 200) {
      ElMessage.success('订单取消成功')
      // 重新加载订单列表以同步数据
      await loadOrderList()
      // 如果订单详情对话框打开，也需要刷新
      if (orderDetailVisible.value && currentOrder.value?.id === row.id) {
        await viewOrder(row)
      }
    } else {
      const errorMsg = response?.message || error.response?.data?.message || '订单取消失败'
      ElMessage.error(errorMsg)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      const errorMsg = error.response?.data?.message || error.message || '取消订单失败'
      ElMessage.error(errorMsg)
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
  loadOrderList()
})
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 4px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.order-item {
  margin-bottom: 5px;
  font-size: 14px;
}
</style>
