<template>
  <div class="page-container">
    <div class="page-header">
      <h2>数据统计分析</h2>
    </div>
    
    <!-- 时间筛选 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" :inline="true" class="filter-form">
        <el-form-item label="统计周期">
          <el-select v-model="filterForm.period" @change="handlePeriodChange" style="width: 120px">
            <el-option label="日" value="day" />
            <el-option label="周" value="week" />
            <el-option label="月" value="month" />
            <el-option label="年" value="year" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            :type="getDatePickerType()"
            :format="getDateFormat()"
            :value-format="getDateFormat()"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRefresh">刷新数据</el-button>
          <el-button @click="handleExport">导出报表</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 核心指标卡片 -->
    <div class="metrics-grid">
      <el-card class="metric-card">
        <div class="metric-content">
          <div class="metric-icon user-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="metric-info">
            <div class="metric-title">注册用户</div>
            <div class="metric-value">{{ userStats.total }}</div>
            <div class="metric-change">
              <span :class="userStats.growth >= 0 ? 'positive' : 'negative'">
                {{ userStats.growth >= 0 ? '+' : '' }}{{ userStats.growth }}%
              </span>
              <span class="metric-label">较上期</span>
            </div>
          </div>
        </div>
        <div class="metric-detail">
          <div class="detail-item">
            <span class="detail-label">新增用户</span>
            <span class="detail-value">{{ userStats.new }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">累计用户</span>
            <span class="detail-value">{{ userStats.total }}</span>
          </div>
        </div>
      </el-card>

      <el-card class="metric-card">
        <div class="metric-content">
          <div class="metric-icon merchant-icon">
            <el-icon><Shop /></el-icon>
          </div>
          <div class="metric-info">
            <div class="metric-title">商家数量</div>
            <div class="metric-value">{{ merchantStats.total }}</div>
            <div class="metric-change">
              <span :class="merchantStats.growth >= 0 ? 'positive' : 'negative'">
                {{ merchantStats.growth >= 0 ? '+' : '' }}{{ merchantStats.growth }}%
              </span>
              <span class="metric-label">较上期</span>
            </div>
          </div>
        </div>
        <div class="metric-detail">
          <div class="detail-item">
            <span class="detail-label">新增商家</span>
            <span class="detail-value">{{ merchantStats.new }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">活跃商家</span>
            <span class="detail-value">{{ merchantStats.active }}</span>
          </div>
        </div>
      </el-card>

      <el-card class="metric-card">
        <div class="metric-content">
          <div class="metric-icon order-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="metric-info">
            <div class="metric-title">订单数量</div>
            <div class="metric-value">{{ orderStats.total }}</div>
            <div class="metric-change">
              <span :class="orderStats.growth >= 0 ? 'positive' : 'negative'">
                {{ orderStats.growth >= 0 ? '+' : '' }}{{ orderStats.growth }}%
              </span>
              <span class="metric-label">较上期</span>
            </div>
          </div>
        </div>
        <div class="metric-detail">
          <div class="detail-item">
            <span class="detail-label">新增订单</span>
            <span class="detail-value">{{ orderStats.new }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">完成订单</span>
            <span class="detail-value">{{ orderStats.completed }}</span>
          </div>
        </div>
      </el-card>

      <el-card class="metric-card">
        <div class="metric-content">
          <div class="metric-icon money-icon">
            <el-icon><Money /></el-icon>
          </div>
          <div class="metric-info">
            <div class="metric-title">交易金额</div>
            <div class="metric-value">¥{{ formatMoney(transactionStats.current) }}</div>
            <div class="metric-change">
              <span :class="transactionStats.growth >= 0 ? 'positive' : 'negative'">
                {{ transactionStats.growth >= 0 ? '+' : '' }}{{ transactionStats.growth }}%
              </span>
              <span class="metric-label">较上期</span>
            </div>
          </div>
        </div>
        <div class="metric-detail">
          <div class="detail-item">
            <span class="detail-label">本期交易</span>
            <span class="detail-value">¥{{ formatMoney(transactionStats.current) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">客单价</span>
            <span class="detail-value">¥{{ formatMoney(transactionStats.avgOrder) }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <!-- 销量趋势图 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>销量趋势</span>
            <el-button size="small" @click="exportChart('sales')">导出</el-button>
          </div>
        </template>
        <div ref="salesChart" class="chart-container"></div>
      </el-card>

      <!-- 交易额变化图 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>交易额变化</span>
            <el-button size="small" @click="exportChart('transaction')">导出</el-button>
          </div>
        </template>
        <div ref="transactionChart" class="chart-container"></div>
      </el-card>

      <!-- 订单状态占比图 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>订单状态占比</span>
            <el-button size="small" @click="exportChart('orderStatus')">导出</el-button>
          </div>
        </template>
        <div ref="orderStatusChart" class="chart-container"></div>
      </el-card>

      <!-- 用户增长趋势图 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>用户增长趋势</span>
            <el-button size="small" @click="exportChart('userGrowth')">导出</el-button>
          </div>
        </template>
        <div ref="userGrowthChart" class="chart-container"></div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Shop, Document, Money } from '@element-plus/icons-vue'
import { getDataOverview } from '@/api/admin'

// 筛选表单
const filterForm = reactive({
  period: 'day',
  dateRange: []
})

// 统计数据
const userStats = reactive({
  total: 0,
  new: 0,
  growth: 0
})

const merchantStats = reactive({
  total: 0,
  new: 0,
  active: 0,
  growth: 0
})

const orderStats = reactive({
  total: 0,
  new: 0,
  completed: 0,
  growth: 0
})

const transactionStats = reactive({
  total: 0,
  current: 0,
  avgOrder: 0,
  growth: 0
})

// 图表数据
const salesTrend = ref({
  categories: [],
  data: []
})

const transactionTrend = ref({
  categories: [],
  data: []
})

const orderStatusDistribution = ref([])

const userGrowthTrend = ref({
  categories: [],
  data: []
})

// 图表引用
const salesChart = ref(null)
const transactionChart = ref(null)
const orderStatusChart = ref(null)
const userGrowthChart = ref(null)

// 获取日期选择器类型
const getDatePickerType = () => {
  const typeMap = {
    day: 'daterange',
    week: 'weekrange',
    month: 'monthrange',
    year: 'yearrange'
  }
  return typeMap[filterForm.period] || 'daterange'
}

// 获取日期格式
const getDateFormat = () => {
  const formatMap = {
    day: 'YYYY-MM-DD',
    week: 'YYYY-MM-DD',
    month: 'YYYY-MM',
    year: 'YYYY'
  }
  return formatMap[filterForm.period] || 'YYYY-MM-DD'
}

// 格式化金额
const formatMoney = (amount) => {
  if (!amount || amount === 0) return '0'
  const numAmount = typeof amount === 'number' ? amount : parseFloat(amount)
  if (numAmount >= 10000) {
    return (numAmount / 10000).toFixed(1) + '万'
  }
  return numAmount.toFixed(2)
}

// 格式化日期为字符串
const formatDateString = (date, period) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  
  switch (period) {
    case 'day':
    case 'week':
      return `${year}-${month}-${day}`
    case 'month':
      return `${year}-${month}`
    case 'year':
      return String(year)
    default:
      return `${year}-${month}-${day}`
  }
}

// 处理周期变化
const handlePeriodChange = () => {
  // 根据周期设置默认时间范围
  const now = new Date()
  const start = new Date()
  
  switch (filterForm.period) {
    case 'day':
      start.setDate(now.getDate() - 7)
      break
    case 'week':
      start.setDate(now.getDate() - 28)
      break
    case 'month':
      start.setMonth(now.getMonth() - 6)
      break
    case 'year':
      start.setFullYear(now.getFullYear() - 2)
      break
  }
  
  filterForm.dateRange = [
    formatDateString(start, filterForm.period),
    formatDateString(now, filterForm.period)
  ]
  loadData()
}

// 处理日期变化
const handleDateChange = () => {
  loadData()
}

// 刷新数据
const handleRefresh = () => {
  loadData()
  ElMessage.success('数据已刷新')
}

// 导出报表
const handleExport = () => {
  ElMessage.success('报表导出功能开发中...')
}

// 导出图表
const exportChart = (type) => {
  ElMessage.success(`${type} 图表导出功能开发中...`)
}

// 加载数据
const loadData = async () => {
  if (!filterForm.dateRange || filterForm.dateRange.length !== 2) {
    ElMessage.warning('请选择时间范围')
    return
  }

  try {
    const [startDate, endDate] = filterForm.dateRange
    
    const response = await getDataOverview(filterForm.period, startDate, endDate)
    
    if (response.code === 200 && response.data) {
      const data = response.data
      
      // 更新用户统计数据
      if (data.userStats) {
        userStats.total = data.userStats.total || 0
        userStats.new = data.userStats.newUsers || 0
        userStats.growth = parseFloat(data.userStats.growth || 0)
      }
      
      // 更新商家统计数据
      if (data.merchantStats) {
        merchantStats.total = data.merchantStats.total || 0
        merchantStats.new = data.merchantStats.newMerchants || 0
        merchantStats.active = data.merchantStats.activeMerchants || 0
        merchantStats.growth = parseFloat(data.merchantStats.growth || 0)
      }
      
      // 更新订单统计数据
      if (data.orderStats) {
        orderStats.total = data.orderStats.total || 0
        orderStats.new = data.orderStats.newOrders || 0
        orderStats.completed = data.orderStats.completedOrders || 0
        orderStats.growth = parseFloat(data.orderStats.growth || 0)
      }
      
      // 更新交易统计数据
      if (data.transactionStats) {
        transactionStats.total = parseFloat(data.transactionStats.total || 0)
        transactionStats.current = parseFloat(data.transactionStats.current || 0)
        transactionStats.avgOrder = parseFloat(data.transactionStats.avgOrder || 0)
        transactionStats.growth = parseFloat(data.transactionStats.growth || 0)
      }
      
      // 更新图表数据
      if (data.salesTrend) {
        salesTrend.value = {
          categories: data.salesTrend.categories || [],
          data: data.salesTrend.data || []
        }
      }
      
      if (data.transactionTrend) {
        transactionTrend.value = {
          categories: data.transactionTrend.categories || [],
          data: data.transactionTrend.data || []
        }
      }
      
      if (data.orderStatusDistribution) {
        orderStatusDistribution.value = data.orderStatusDistribution || []
      }
      
      if (data.userGrowthTrend) {
        userGrowthTrend.value = {
          categories: data.userGrowthTrend.categories || [],
          data: data.userGrowthTrend.data || []
        }
      }
      
      // 更新图表
      updateCharts()
    } else {
      ElMessage.error(response.message || '获取数据失败')
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败，请稍后重试')
  }
}

// 更新图表
const updateCharts = () => {
  nextTick(() => {
    // 使用真实数据创建图表
    createCharts()
  })
}

// 创建图表
const createCharts = () => {
  // 销量趋势图
  if (salesChart.value && salesTrend.value.categories.length > 0) {
    const maxValue = Math.max(...salesTrend.value.data.map(d => Number(d)))
    salesChart.value.innerHTML = `
      <div class="mock-chart">
        <h4>销量趋势 (柱状图)</h4>
        <div class="chart-data">
          ${salesTrend.value.categories.map((cat, index) => {
            const value = Number(salesTrend.value.data[index] || 0)
            const height = maxValue > 0 ? (value / maxValue * 200) : 0
            return `<div class="bar-item">
              <div class="bar" style="height: ${height}px"></div>
              <span>${cat}</span>
            </div>`
          }).join('')}
        </div>
      </div>
    `
  }
  
  // 交易额变化图
  if (transactionChart.value && transactionTrend.value.categories.length > 0) {
    const maxValue = Math.max(...transactionTrend.value.data.map(d => Number(d)))
    const dataPoints = transactionTrend.value.categories.length
    transactionChart.value.innerHTML = `
      <div class="mock-chart">
        <h4>交易额变化 (折线图)</h4>
        <div class="chart-data">
          <div class="line-chart">
            ${transactionTrend.value.categories.map((cat, index) => {
              const value = Number(transactionTrend.value.data[index] || 0)
              const height = maxValue > 0 ? (value / maxValue * 200) : 0
              const left = dataPoints > 1 ? (index / (dataPoints - 1) * 100) : 50
              return `<div class="line-point" style="left: ${left}%; bottom: ${height}px">
                <span>${cat}</span>
              </div>`
            }).join('')}
          </div>
        </div>
      </div>
    `
  }
  
  // 订单状态占比图
  if (orderStatusChart.value && orderStatusDistribution.value.length > 0) {
    orderStatusChart.value.innerHTML = `
      <div class="mock-chart">
        <h4>订单状态占比 (饼图)</h4>
        <div class="pie-chart">
          ${orderStatusDistribution.value.map((item, index) => {
            const percentage = parseFloat(item.percentage || 0)
            return `<div class="pie-item" style="background: hsl(${index * 90}, 70%, 60%)">
              <span>${item.name}: ${percentage.toFixed(1)}%</span>
            </div>`
          }).join('')}
        </div>
      </div>
    `
  }
  
  // 用户增长趋势图
  if (userGrowthChart.value && userGrowthTrend.value.categories.length > 0) {
    const maxValue = Math.max(...userGrowthTrend.value.data.map(d => Number(d)))
    const dataPoints = userGrowthTrend.value.categories.length
    userGrowthChart.value.innerHTML = `
      <div class="mock-chart">
        <h4>用户增长趋势 (折线图)</h4>
        <div class="chart-data">
          <div class="line-chart">
            ${userGrowthTrend.value.categories.map((cat, index) => {
              const value = Number(userGrowthTrend.value.data[index] || 0)
              const height = maxValue > 0 ? (value / maxValue * 200) : 0
              const left = dataPoints > 1 ? (index / (dataPoints - 1) * 100) : 50
              return `<div class="line-point" style="left: ${left}%; bottom: ${height}px">
                <span>${cat}</span>
              </div>`
            }).join('')}
          </div>
        </div>
      </div>
    `
  }
}

// 初始化
onMounted(() => {
  // 设置默认时间范围
  const now = new Date()
  const start = new Date()
  start.setDate(now.getDate() - 7)
  filterForm.dateRange = [
    formatDateString(start, filterForm.period),
    formatDateString(now, filterForm.period)
  ]
  
  // 加载数据
  loadData()
})
</script>

<style scoped>
.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  margin-bottom: 0;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.metric-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.metric-content {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.metric-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
}

.user-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.merchant-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.order-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.money-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.metric-info {
  flex: 1;
}

.metric-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.metric-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.metric-change {
  display: flex;
  align-items: center;
  gap: 5px;
}

.positive {
  color: #67c23a;
  font-weight: bold;
}

.negative {
  color: #f56c6c;
  font-weight: bold;
}

.metric-label {
  font-size: 12px;
  color: #999;
}

.metric-detail {
  display: flex;
  justify-content: space-between;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.detail-item {
  text-align: center;
}

.detail-label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
}

.detail-value {
  display: block;
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 20px;
}

.chart-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
  padding: 20px;
}

.mock-chart {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.mock-chart h4 {
  margin: 0 0 20px 0;
  text-align: center;
  color: #666;
}

.chart-data {
  flex: 1;
  display: flex;
  align-items: end;
  justify-content: space-around;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.bar {
  width: 30px;
  background: linear-gradient(to top, #4facfe, #00f2fe);
  border-radius: 4px 4px 0 0;
  min-height: 20px;
}

.line-chart {
  position: relative;
  width: 100%;
  height: 200px;
  border-bottom: 2px solid #ddd;
  border-left: 2px solid #ddd;
}

.line-point {
  position: absolute;
  width: 8px;
  height: 8px;
  background: #4facfe;
  border-radius: 50%;
  transform: translateX(-50%);
}

.pie-chart {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.pie-item {
  padding: 10px 15px;
  border-radius: 20px;
  color: white;
  font-size: 12px;
  font-weight: bold;
}
</style>
