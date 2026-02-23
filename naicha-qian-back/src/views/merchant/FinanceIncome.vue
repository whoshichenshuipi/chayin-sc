<template>
  <div class="page-container">
    <div class="page-header">
      <h2>财务管理</h2>
    </div>

    <!-- 财务概览 -->
    <el-row :gutter="20" class="summary-cards">
      <el-col :span="6">
        <el-card class="summary-card">
          <div class="card-content">
            <div class="card-icon income">
              <el-icon><Money /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-value">¥{{ Number(totalIncome).toFixed(2) }}</div>
              <div class="card-label">总收入</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="summary-card">
          <div class="card-content">
            <div class="card-icon expense">
              <el-icon><CreditCard /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-value">¥{{ Number(totalExpense).toFixed(2) }}</div>
              <div class="card-label">总支出</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="summary-card">
          <div class="card-content">
            <div class="card-icon profit">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-value">¥{{ Number(netProfit).toFixed(2) }}</div>
              <div class="card-label">净利润</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="summary-card">
          <div class="card-content">
            <div class="card-icon pending">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-value">¥{{ Number(pendingAmount).toFixed(2) }}</div>
              <div class="card-label">待结算</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 时间筛选 -->
    <el-card class="filter-card">
      <div class="filter-section">
        <div class="filter-item">
          <span class="filter-label">时间范围：</span>
          <el-radio-group v-model="timeRange" @change="onTimeRangeChange">
            <el-radio value="today">今日</el-radio>
            <el-radio value="week">本周</el-radio>
            <el-radio value="month">本月</el-radio>
            <el-radio value="custom">自定义</el-radio>
          </el-radio-group>
        </div>
        <div class="filter-item" v-if="timeRange === 'custom'">
          <el-date-picker
            v-model="customDateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            @change="onCustomDateChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 收入记录 -->
    <el-card class="record-card">
      <template #header>
        <div class="card-header">
          <span>收入记录</span>
          <el-button type="primary" @click="exportIncomeDataHandler">
            <el-icon><Download /></el-icon>
            导出收入明细
          </el-button>
        </div>
      </template>

      <el-table :data="filteredIncomeRecords" style="width: 100%" v-loading="loadingIncome">
        <el-table-column prop="orderNo" label="订单号" width="150" />
        <el-table-column prop="orderTime" label="下单时间" width="150" />
        <el-table-column prop="productName" label="商品名称" width="200" />
        <el-table-column prop="originalAmount" label="订单金额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ row.originalAmount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="platformFee" label="平台手续费" width="120">
          <template #default="{ row }">
            <span class="fee">¥{{ row.platformFee.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="incomeAmount" label="收入金额" width="120">
          <template #default="{ row }">
            <span class="income">¥{{ row.incomeAmount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="settlementStatus" label="结算状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.settlementStatus === 'settled' ? 'success' : 'warning'">
              {{ row.settlementStatus === 'settled' ? '已结算' : '待结算' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="settlementTime" label="结算时间" width="150">
          <template #default="{ row }">
            {{ row.settlementTime || '-' }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="incomeCurrentPage"
          v-model:page-size="incomePageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="incomeTotal"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleIncomeSizeChange"
          @current-change="handleIncomeCurrentChange"
        />
      </div>
    </el-card>

    <!-- 支出记录 -->
    <el-card class="record-card">
      <template #header>
        <div class="card-header">
          <span>支出记录</span>
          <el-button type="primary" @click="exportExpenseDataHandler">
            <el-icon><Download /></el-icon>
            导出支出明细
          </el-button>
        </div>
      </template>

      <el-table :data="filteredExpenseRecords" style="width: 100%" v-loading="loadingExpense">
        <el-table-column prop="expenseTime" label="支出时间" width="150" />
        <el-table-column prop="expenseType" label="支出类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getExpenseTypeTag(row.expenseType)">
              {{ getExpenseTypeText(row.expenseType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="支出金额" width="120">
          <template #default="{ row }">
            <span class="expense">¥{{ row.amount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="备注" min-width="200">
          <template #default="{ row }">
            <span class="description">{{ row.description }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="relatedOrderId" label="关联订单" width="150">
          <template #default="{ row }">
            {{ row.relatedOrderId || '-' }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="expenseCurrentPage"
          v-model:page-size="expensePageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="expenseTotal"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleExpenseSizeChange"
          @current-change="handleExpenseCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Money, CreditCard, TrendCharts, Clock, Download } from '@element-plus/icons-vue'
import { getFinanceSummary, getIncomeRecords, getExpenseRecords, exportIncomeData, exportExpenseData } from '@/api/finance'

// 响应式数据
const timeRange = ref('today')
const customDateRange = ref([])

// 财务概览数据
const totalIncome = ref(0)
const totalExpense = ref(0)
const netProfit = ref(0)
const pendingAmount = ref(0)

// 分页数据
const incomeCurrentPage = ref(1)
const incomePageSize = ref(20)
const incomeTotal = ref(0)
const incomeRecords = ref([])
const loadingIncome = ref(false)

const expenseCurrentPage = ref(1)
const expensePageSize = ref(20)
const expenseTotal = ref(0)
const expenseRecords = ref([])
const loadingExpense = ref(false)

// 计算属性
const filteredIncomeRecords = computed(() => {
  return incomeRecords.value
})

const filteredExpenseRecords = computed(() => {
  return expenseRecords.value
})

// 获取时间范围
const getTimeRange = () => {
  const now = new Date()
  let startTime = null
  let endTime = null

  if (timeRange.value === 'custom' && customDateRange.value && customDateRange.value.length === 2) {
    startTime = customDateRange.value[0]
    endTime = customDateRange.value[1]
  } else if (timeRange.value === 'today') {
    startTime = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    endTime = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59)
  } else if (timeRange.value === 'week') {
    const dayOfWeek = now.getDay()
    const diff = now.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1)
    startTime = new Date(now.getFullYear(), now.getMonth(), diff)
    endTime = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59)
  } else if (timeRange.value === 'month') {
    startTime = new Date(now.getFullYear(), now.getMonth(), 1)
    endTime = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59)
  }

  return { startTime, endTime }
}

// 格式化日期时间为字符串（用于API请求）
const formatDateTimeForAPI = (dateTime) => {
  if (!dateTime) return null
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 格式化后端返回的日期时间（用于显示）
const formatDateTimeForDisplay = (dateTime) => {
  if (!dateTime) return null
  const date = typeof dateTime === 'string' ? new Date(dateTime) : dateTime
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 加载财务概览
const loadFinanceSummary = async () => {
  try {
    const { startTime, endTime } = getTimeRange()
    const response = await getFinanceSummary(
      startTime ? formatDateTimeForAPI(startTime) : null,
      endTime ? formatDateTimeForAPI(endTime) : null
    )
    
    if (response.code === 200 && response.data) {
      totalIncome.value = Number(response.data.totalIncome) || 0
      totalExpense.value = Number(response.data.totalExpense) || 0
      netProfit.value = Number(response.data.netProfit) || 0
      pendingAmount.value = Number(response.data.pendingAmount) || 0
    }
  } catch (error) {
    console.error('加载财务概览失败:', error)
    ElMessage.error('加载财务概览失败')
  }
}

// 加载收入记录
const loadIncomeRecords = async () => {
  try {
    loadingIncome.value = true
    const { startTime, endTime } = getTimeRange()
    
    const queryDTO = {
      current: incomeCurrentPage.value,
      size: incomePageSize.value,
      startTime: startTime ? formatDateTimeForAPI(startTime) : null,
      endTime: endTime ? formatDateTimeForAPI(endTime) : null
    }

    const response = await getIncomeRecords(queryDTO)
    
    if (response.code === 200 && response.data) {
      incomeRecords.value = response.data.records || []
      incomeTotal.value = response.data.total || 0
      
      // 格式化数据
      incomeRecords.value = incomeRecords.value.map(record => ({
        ...record,
        orderTime: record.orderTime ? formatDateTimeForDisplay(record.orderTime) : '-',
        settlementTime: record.settlementTime ? formatDateTimeForDisplay(record.settlementTime) : null,
        originalAmount: Number(record.originalAmount) || 0,
        platformFee: Number(record.platformFee) || 0,
        incomeAmount: Number(record.incomeAmount) || 0
      }))
    }
  } catch (error) {
    console.error('加载收入记录失败:', error)
    ElMessage.error('加载收入记录失败')
  } finally {
    loadingIncome.value = false
  }
}

// 加载支出记录
const loadExpenseRecords = async () => {
  try {
    loadingExpense.value = true
    const { startTime, endTime } = getTimeRange()
    
    const queryDTO = {
      current: expenseCurrentPage.value,
      size: expensePageSize.value,
      startTime: startTime ? formatDateTimeForAPI(startTime) : null,
      endTime: endTime ? formatDateTimeForAPI(endTime) : null
    }

    const response = await getExpenseRecords(queryDTO)
    
    if (response.code === 200 && response.data) {
      expenseRecords.value = response.data.records || []
      expenseTotal.value = response.data.total || 0
      
      // 格式化数据
      expenseRecords.value = expenseRecords.value.map(record => ({
        ...record,
        expenseTime: record.expenseTime ? formatDateTimeForDisplay(record.expenseTime) : '-',
        amount: Number(record.amount) || 0
      }))
    }
  } catch (error) {
    console.error('加载支出记录失败:', error)
    ElMessage.error('加载支出记录失败')
  } finally {
    loadingExpense.value = false
  }
}

// 方法
const getExpenseTypeText = (type) => {
  const typeMap = {
    platform_fee: '平台手续费',
    coupon_cost: '优惠券成本',
    refund: '退款金额',
    other: '其他支出'
  }
  return typeMap[type] || type
}

const getExpenseTypeTag = (type) => {
  const tagMap = {
    platform_fee: 'info',
    coupon_cost: 'warning',
    refund: 'danger',
    other: 'default'
  }
  return tagMap[type] || 'default'
}

const onTimeRangeChange = (value) => {
  customDateRange.value = []
  loadAllData()
}

const onCustomDateChange = (value) => {
  if (value && value.length === 2) {
    loadAllData()
  }
}

// 加载所有数据
const loadAllData = () => {
  loadFinanceSummary()
  loadIncomeRecords()
  loadExpenseRecords()
}

const exportIncomeDataHandler = async () => {
  try {
    const { startTime, endTime } = getTimeRange()
    const queryDTO = {
      startTime: startTime ? formatDateTimeForAPI(startTime) : null,
      endTime: endTime ? formatDateTimeForAPI(endTime) : null
    }
    
    await exportIncomeData(queryDTO)
    ElMessage.success('收入明细导出成功')
  } catch (error) {
    console.error('导出收入明细失败:', error)
    ElMessage.error('导出收入明细失败')
  }
}

const exportExpenseDataHandler = async () => {
  try {
    const { startTime, endTime } = getTimeRange()
    const queryDTO = {
      startTime: startTime ? formatDateTimeForAPI(startTime) : null,
      endTime: endTime ? formatDateTimeForAPI(endTime) : null
    }
    
    await exportExpenseData(queryDTO)
    ElMessage.success('支出明细导出成功')
  } catch (error) {
    console.error('导出支出明细失败:', error)
    ElMessage.error('导出支出明细失败')
  }
}

const handleIncomeSizeChange = (val) => {
  incomePageSize.value = val
  incomeCurrentPage.value = 1
  loadIncomeRecords()
}

const handleIncomeCurrentChange = (val) => {
  incomeCurrentPage.value = val
  loadIncomeRecords()
}

const handleExpenseSizeChange = (val) => {
  expensePageSize.value = val
  expenseCurrentPage.value = 1
  loadExpenseRecords()
}

const handleExpenseCurrentChange = (val) => {
  expenseCurrentPage.value = val
  loadExpenseRecords()
}

onMounted(() => {
  loadAllData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.summary-cards {
  margin-bottom: 20px;
}

.summary-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-content {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 24px;
  color: white;
}

.card-icon.income {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.card-icon.expense {
  background: linear-gradient(135deg, #f56c6c, #f78989);
}

.card-icon.profit {
  background: linear-gradient(135deg, #409eff, #66b1ff);
}

.card-icon.pending {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
}

.card-info {
  flex: 1;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.card-label {
  font-size: 14px;
  color: #909399;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  font-weight: 500;
  color: #303133;
}

.record-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.amount {
  font-weight: bold;
  color: #67c23a;
}

.fee {
  font-weight: bold;
  color: #f56c6c;
}

.income {
  font-weight: bold;
  color: #409eff;
}

.expense {
  font-weight: bold;
  color: #f56c6c;
}

.description {
  color: #606266;
  font-size: 14px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
