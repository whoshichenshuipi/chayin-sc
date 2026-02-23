<template>
  <div class="page-container">
    <div class="page-header">
      <h2>数据报表</h2>
    </div>
    
    <!-- 报表筛选 -->
    <el-card class="filter-card">
      <el-form :model="reportForm" :inline="true" class="filter-form">
        <el-form-item label="报表类型">
          <el-select v-model="reportForm.type" @change="handleReportTypeChange" style="width: 150px">
            <el-option label="运营数据报表" value="operation" />
            <el-option label="销售数据报表" value="sales" />
            <el-option label="用户数据报表" value="user" />
            <el-option label="商家数据报表" value="merchant" />
            <el-option label="财务数据报表" value="finance" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间周期">
          <el-select v-model="reportForm.period" @change="handlePeriodChange" style="width: 120px">
            <el-option label="日报" value="daily" />
            <el-option label="周报" value="weekly" />
            <el-option label="月报" value="monthly" />
            <el-option label="年报" value="yearly" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="reportForm.dateRange"
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
          <el-button type="primary" @click="handleGenerateReport">生成报表</el-button>
          <el-button @click="handleExportExcel">导出Excel</el-button>
          <el-button @click="handleExportCSV">导出CSV</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 报表内容 -->
    <div v-if="reportData" class="report-content">
      <!-- 报表标题和摘要 -->
      <el-card class="report-header">
        <div class="report-title">
          <h3>{{ getReportTitle() }}</h3>
          <div class="report-meta">
            <span>生成时间：{{ reportData.generateTime }}</span>
            <span>统计周期：{{ reportData.period }}</span>
            <span>数据范围：{{ reportData.dateRange }}</span>
          </div>
        </div>
        <div class="report-summary">
          <div class="summary-item">
            <span class="summary-label">总用户数</span>
            <span class="summary-value">{{ reportData.summary.totalUsers }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">总订单数</span>
            <span class="summary-value">{{ reportData.summary.totalOrders }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">总交易额</span>
            <span class="summary-value">¥{{ formatMoney(reportData.summary.totalAmount) }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">活跃商家</span>
            <span class="summary-value">{{ reportData.summary.activeMerchants }}</span>
          </div>
        </div>
      </el-card>

      <!-- 图表区域 -->
      <div class="charts-section">
        <!-- 销量趋势图 -->
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>销量趋势分析</span>
              <div class="chart-actions">
                <el-button size="small" @click="exportChart('sales')">导出图表</el-button>
                <el-button size="small" @click="downloadChart('sales')">下载图片</el-button>
              </div>
            </div>
          </template>
          <div class="chart-container">
            <div class="chart-info">
              <div class="chart-description">
                <p>本图表展示了{{ reportData.period }}内的销量变化趋势，帮助分析销售表现。</p>
              </div>
              <div class="chart-stats">
                <div class="stat-item">
                  <span class="stat-label">最高销量</span>
                  <span class="stat-value">{{ reportData.charts.sales.data.length > 0 ? Math.max(...reportData.charts.sales.data.map(d => Number(d))) : 0 }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">平均销量</span>
                  <span class="stat-value">{{ reportData.charts.sales.data.length > 0 ? Math.round(reportData.charts.sales.data.reduce((a, b) => Number(a) + Number(b), 0) / reportData.charts.sales.data.length) : 0 }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">增长率</span>
                  <span class="stat-value" :class="reportData.charts.sales.growth >= 0 ? 'positive' : 'negative'">
                    {{ reportData.charts.sales.growth >= 0 ? '+' : '' }}{{ reportData.charts.sales.growth }}%
                  </span>
                </div>
              </div>
            </div>
            <div ref="salesChart" class="chart-area"></div>
          </div>
        </el-card>

        <!-- 交易额变化图 -->
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>交易额变化分析</span>
              <div class="chart-actions">
                <el-button size="small" @click="exportChart('transaction')">导出图表</el-button>
                <el-button size="small" @click="downloadChart('transaction')">下载图片</el-button>
              </div>
            </div>
          </template>
          <div class="chart-container">
            <div class="chart-info">
              <div class="chart-description">
                <p>本图表展示了{{ reportData.period }}内的交易额变化情况，反映平台收入趋势。</p>
              </div>
              <div class="chart-stats">
                <div class="stat-item">
                  <span class="stat-label">最高交易额</span>
                  <span class="stat-value">¥{{ reportData.charts.transaction.data.length > 0 ? formatMoney(Math.max(...reportData.charts.transaction.data.map(d => Number(d)))) : '0' }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">平均交易额</span>
                  <span class="stat-value">¥{{ reportData.charts.transaction.data.length > 0 ? formatMoney(reportData.charts.transaction.data.reduce((a, b) => Number(a) + Number(b), 0) / reportData.charts.transaction.data.length) : '0' }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">增长率</span>
                  <span class="stat-value" :class="reportData.charts.transaction.growth >= 0 ? 'positive' : 'negative'">
                    {{ reportData.charts.transaction.growth >= 0 ? '+' : '' }}{{ reportData.charts.transaction.growth }}%
                  </span>
                </div>
              </div>
            </div>
            <div ref="transactionChart" class="chart-area"></div>
          </div>
        </el-card>

        <!-- 订单状态占比图 -->
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>订单状态分布</span>
              <div class="chart-actions">
                <el-button size="small" @click="exportChart('orderStatus')">导出图表</el-button>
                <el-button size="small" @click="downloadChart('orderStatus')">下载图片</el-button>
              </div>
            </div>
          </template>
          <div class="chart-container">
            <div class="chart-info">
              <div class="chart-description">
                <p>本图表展示了订单状态的分布情况，帮助了解订单处理效率。</p>
              </div>
              <div class="chart-stats">
                <div class="stat-item">
                  <span class="stat-label">完成率</span>
                  <span class="stat-value">{{ reportData.charts.orderStatus.completionRate }}%</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">取消率</span>
                  <span class="stat-value">{{ reportData.charts.orderStatus.cancelRate }}%</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">待处理</span>
                  <span class="stat-value">{{ reportData.charts.orderStatus.pendingRate }}%</span>
                </div>
              </div>
            </div>
            <div ref="orderStatusChart" class="chart-area"></div>
          </div>
        </el-card>

        <!-- 用户增长趋势图 -->
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>用户增长趋势</span>
              <div class="chart-actions">
                <el-button size="small" @click="exportChart('userGrowth')">导出图表</el-button>
                <el-button size="small" @click="downloadChart('userGrowth')">下载图片</el-button>
              </div>
            </div>
          </template>
          <div class="chart-container">
            <div class="chart-info">
              <div class="chart-description">
                <p>本图表展示了用户注册和活跃度的变化趋势，反映平台用户发展情况。</p>
              </div>
              <div class="chart-stats">
                <div class="stat-item">
                  <span class="stat-label">新增用户</span>
                  <span class="stat-value">{{ reportData.charts.userGrowth.newUsers }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">活跃用户</span>
                  <span class="stat-value">{{ reportData.charts.userGrowth.activeUsers }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">增长率</span>
                  <span class="stat-value positive">+{{ reportData.charts.userGrowth.growth }}%</span>
                </div>
              </div>
            </div>
            <div ref="userGrowthChart" class="chart-area"></div>
          </div>
        </el-card>
      </div>

      <!-- 详细数据表格 -->
      <el-card class="data-table-card">
        <template #header>
          <div class="card-header">
            <span>详细数据</span>
            <el-button size="small" @click="exportTableData">导出表格数据</el-button>
          </div>
        </template>
        <el-table :data="reportData.tableData" stripe>
          <el-table-column prop="date" label="日期" width="120" />
          <el-table-column prop="newUsers" label="新增用户" width="100" />
          <el-table-column prop="newOrders" label="新增订单" width="100" />
          <el-table-column prop="sales" label="销量" width="100" />
          <el-table-column prop="transaction" label="交易额" width="120">
            <template #default="{ row }">
              ¥{{ formatMoney(row.transaction) }}
            </template>
          </el-table-column>
          <el-table-column prop="avgOrderValue" label="客单价" width="100">
            <template #default="{ row }">
              ¥{{ row.avgOrderValue ? parseFloat(row.avgOrderValue).toFixed(2) : '0.00' }}
            </template>
          </el-table-column>
          <el-table-column prop="completionRate" label="完成率" width="100">
            <template #default="{ row }">
              {{ row.completionRate ? parseFloat(row.completionRate).toFixed(1) : '0' }}%
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 无数据状态 -->
    <el-card v-else class="empty-card">
      <div class="empty-state">
        <el-icon size="64" color="#c0c4cc"><Document /></el-icon>
        <h3>暂无报表数据</h3>
        <p>请选择时间范围和报表类型，然后点击"生成报表"按钮</p>
        <el-button type="primary" @click="handleGenerateReport">生成报表</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import { generateDataReport } from '@/api/admin'

// 报表表单
const reportForm = reactive({
  type: 'operation',
  period: 'daily',
  dateRange: []
})

// 报表数据
const reportData = ref(null)

// 图表引用
const salesChart = ref(null)
const transactionChart = ref(null)
const orderStatusChart = ref(null)
const userGrowthChart = ref(null)

// 获取日期选择器类型
const getDatePickerType = () => {
  const typeMap = {
    daily: 'daterange',
    weekly: 'weekrange',
    monthly: 'monthrange',
    yearly: 'yearrange'
  }
  return typeMap[reportForm.period] || 'daterange'
}

// 获取日期格式
const getDateFormat = () => {
  const formatMap = {
    daily: 'YYYY-MM-DD',
    weekly: 'YYYY-MM-DD',
    monthly: 'YYYY-MM',
    yearly: 'YYYY'
  }
  return formatMap[reportForm.period] || 'YYYY-MM-DD'
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

// 获取报表标题
const getReportTitle = () => {
  const typeMap = {
    operation: '运营数据报表',
    sales: '销售数据报表',
    user: '用户数据报表',
    merchant: '商家数据报表',
    finance: '财务数据报表'
  }
  const periodMap = {
    daily: '日报',
    weekly: '周报',
    monthly: '月报',
    yearly: '年报'
  }
  return `${typeMap[reportForm.type]} - ${periodMap[reportForm.period]}`
}

// 处理报表类型变化
const handleReportTypeChange = () => {
  // 根据报表类型调整默认设置
  if (reportForm.type === 'finance') {
    reportForm.period = 'monthly'
  } else if (reportForm.type === 'user') {
    reportForm.period = 'weekly'
  }
}

// 格式化日期为字符串
const formatDateString = (date, period) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  
  switch (period) {
    case 'daily':
    case 'weekly':
      return `${year}-${month}-${day}`
    case 'monthly':
      return `${year}-${month}`
    case 'yearly':
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
  
  switch (reportForm.period) {
    case 'daily':
      start.setDate(now.getDate() - 7)
      break
    case 'weekly':
      start.setDate(now.getDate() - 28)
      break
    case 'monthly':
      start.setMonth(now.getMonth() - 6)
      break
    case 'yearly':
      start.setFullYear(now.getFullYear() - 2)
      break
  }
  
  reportForm.dateRange = [
    formatDateString(start, reportForm.period),
    formatDateString(now, reportForm.period)
  ]
}

// 处理日期变化
const handleDateChange = () => {
  // 日期变化时的处理逻辑
}

// 生成报表
const handleGenerateReport = async () => {
  if (!reportForm.dateRange || reportForm.dateRange.length !== 2) {
    ElMessage.warning('请选择时间范围')
    return
  }
  
  try {
    const [startDate, endDate] = reportForm.dateRange
    
    const response = await generateDataReport(reportForm.type, reportForm.period, startDate, endDate)
    
    if (response.code === 200 && response.data) {
      reportData.value = response.data
      
      // 更新图表
      nextTick(() => {
        updateCharts()
      })
      
      ElMessage.success('报表生成成功')
    } else {
      ElMessage.error(response.message || '报表生成失败')
    }
  } catch (error) {
    console.error('生成报表失败:', error)
    ElMessage.error('报表生成失败，请稍后重试')
  }
}



// 更新图表
const updateCharts = () => {
  if (!reportData.value || !reportData.value.charts) return
  
  // 销量趋势图
  if (salesChart.value && reportData.value.charts.sales) {
    createChart(salesChart.value, 'sales', reportData.value.charts.sales)
  }
  
  // 交易额变化图
  if (transactionChart.value && reportData.value.charts.transaction) {
    createChart(transactionChart.value, 'transaction', reportData.value.charts.transaction)
  }
  
  // 订单状态占比图
  if (orderStatusChart.value && reportData.value.charts.orderStatus) {
    createPieChart(orderStatusChart.value, reportData.value.charts.orderStatus)
  }
  
  // 用户增长趋势图
  if (userGrowthChart.value && reportData.value.charts.userGrowth) {
    createChart(userGrowthChart.value, 'userGrowth', reportData.value.charts.userGrowth)
  }
}

// 创建图表
const createChart = (container, type, chartData) => {
  if (!chartData || !chartData.categories || !chartData.data || chartData.categories.length === 0) {
    container.innerHTML = '<div class="mock-chart"><h4>暂无数据</h4></div>'
    return
  }
  
  const chartType = type === 'sales' ? '柱状图' : type === 'transaction' ? '折线图' : '折线图'
  const maxValue = Math.max(...chartData.data.map(d => Number(d)))
  const dataPoints = chartData.categories.length
  
  if (type === 'sales') {
    container.innerHTML = `
      <div class="mock-chart">
        <h4>${chartType}展示</h4>
        <div class="chart-data">
          ${chartData.categories.map((cat, index) => {
            const value = Number(chartData.data[index] || 0)
            const height = maxValue > 0 ? (value / maxValue * 200) : 0
            return `<div class="chart-item">
              <div class="chart-bar" style="height: ${height}px"></div>
              <span class="chart-label">${cat}</span>
              <span class="chart-value">${value}</span>
            </div>`
          }).join('')}
        </div>
      </div>
    `
  } else {
    container.innerHTML = `
      <div class="mock-chart">
        <h4>${chartType}展示</h4>
        <div class="chart-data">
          <div class="line-chart">
            ${chartData.categories.map((cat, index) => {
              const value = Number(chartData.data[index] || 0)
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

// 创建饼图
const createPieChart = (container, data) => {
  if (!data || !data.data || data.data.length === 0) {
    container.innerHTML = '<div class="mock-chart"><h4>暂无数据</h4></div>'
    return
  }
  
  container.innerHTML = `
    <div class="mock-chart">
      <h4>饼图展示</h4>
      <div class="pie-chart">
        ${data.data.map((item, index) => {
          const percentage = parseFloat(item.value || 0)
          return `<div class="pie-item" style="background: ${item.color || '#909399'}">
            <span>${item.name}: ${percentage.toFixed(1)}%</span>
          </div>`
        }).join('')}
      </div>
    </div>
  `
}

// 导出Excel
const handleExportExcel = () => {
  if (!reportData.value) {
    ElMessage.warning('请先生成报表')
    return
  }
  ElMessage.success('Excel导出功能开发中...')
}

// 导出CSV
const handleExportCSV = () => {
  if (!reportData.value) {
    ElMessage.warning('请先生成报表')
    return
  }
  ElMessage.success('CSV导出功能开发中...')
}

// 导出图表
const exportChart = (type) => {
  ElMessage.success(`${type} 图表导出功能开发中...`)
}

// 下载图表
const downloadChart = (type) => {
  ElMessage.success(`${type} 图表下载功能开发中...`)
}

// 导出表格数据
const exportTableData = () => {
  ElMessage.success('表格数据导出功能开发中...')
}

// 初始化
onMounted(() => {
  // 设置默认时间范围
  const now = new Date()
  const start = new Date()
  start.setDate(now.getDate() - 7)
  reportForm.dateRange = [
    formatDateString(start, reportForm.period),
    formatDateString(now, reportForm.period)
  ]
})
</script>

<style scoped>
.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  margin-bottom: 0;
}

.report-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.report-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.report-title h3 {
  margin: 0 0 10px 0;
  color: white;
}

.report-meta {
  display: flex;
  gap: 20px;
  font-size: 14px;
  opacity: 0.9;
}

.report-summary {
  display: flex;
  justify-content: space-around;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.summary-item {
  text-align: center;
}

.summary-label {
  display: block;
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 5px;
}

.summary-value {
  display: block;
  font-size: 24px;
  font-weight: bold;
}

.charts-section {
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

.chart-actions {
  display: flex;
  gap: 10px;
}

.chart-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chart-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.chart-description {
  flex: 1;
  margin-right: 20px;
}

.chart-description p {
  margin: 0;
  color: #666;
  line-height: 1.6;
}

.chart-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
}

.stat-value {
  display: block;
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.stat-value.positive {
  color: #67c23a;
}

.stat-value.negative {
  color: #f56c6c;
}

.chart-area {
  height: 300px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
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
  gap: 10px;
}

.chart-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.chart-bar {
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

.chart-label {
  font-size: 12px;
  color: #666;
  text-align: center;
}

.chart-value {
  font-size: 12px;
  font-weight: bold;
  color: #333;
}

.pie-chart {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.pie-item {
  padding: 15px 20px;
  border-radius: 25px;
  color: white;
  font-size: 14px;
  font-weight: bold;
  text-align: center;
  min-width: 120px;
}

.data-table-card {
  margin-top: 20px;
}

.empty-card {
  text-align: center;
  padding: 60px 20px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.empty-state h3 {
  margin: 0;
  color: #666;
}

.empty-state p {
  margin: 0;
  color: #999;
}
</style>

