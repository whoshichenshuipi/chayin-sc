<template>
  <div class="page-container">
    <div class="page-header">
      <h2>销售统计</h2>
      <el-button type="primary" @click="handleExportAll">
        <el-icon><Download /></el-icon>
        导出全部数据
      </el-button>
    </div>

    <!-- 核心数据概览 -->
    <el-row :gutter="20" class="summary-cards">
      <el-col :span="4">
        <el-card class="summary-card">
          <div class="card-content">
            <div class="card-icon today-order">
              <el-icon><ShoppingBag /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-value">{{ coreData.todayOrderCount || 0 }}</div>
              <div class="card-label">今日订单量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="summary-card">
          <div class="card-content">
            <div class="card-icon today-sales">
              <el-icon><Money /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-value">¥{{ formatMoney(coreData.todaySalesAmount) }}</div>
              <div class="card-label">今日销售额</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="summary-card">
          <div class="card-content">
            <div class="card-icon month-order">
              <el-icon><Document /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-value">{{ coreData.monthOrderCount || 0 }}</div>
              <div class="card-label">本月订单量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="summary-card">
          <div class="card-content">
            <div class="card-icon month-sales">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-value">¥{{ formatMoney(coreData.monthSalesAmount) }}</div>
              <div class="card-label">本月销售额</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="summary-card">
          <div class="card-content">
            <div class="card-icon rating">
              <el-icon><Star /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-value">{{ coreData.shopRating || '0.0' }}</div>
              <div class="card-label">店铺评分</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="summary-card">
          <div class="card-content">
            <div class="card-icon repurchase">
              <el-icon><RefreshRight /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-value">{{ coreData.repurchaseRate || '0.0' }}%</div>
              <div class="card-label">复购率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据趋势图 -->
    <el-row :gutter="20" class="charts-section">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>近7天订单量趋势</span>
              <el-button size="small" @click="exportChart('order')">导出</el-button>
            </div>
          </template>
          <div ref="orderTrendChart" class="chart-container" v-loading="loadingOrderTrend"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>近30天销售额趋势</span>
              <el-button size="small" @click="exportChart('sales')">导出</el-button>
            </div>
          </template>
          <div ref="salesTrendChart" class="chart-container" v-loading="loadingSalesTrend"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 商品数据 -->
    <el-card class="product-data-card">
      <template #header>
        <div class="card-header">
          <span>商品数据</span>
          <div>
            <el-button size="small" @click="handleExportProductData">导出商品数据</el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="productTab" @tab-change="handleProductTabChange">
        <!-- 商品销量排行 -->
        <el-tab-pane label="销量排行" name="ranking">
          <el-table :data="productRanking" style="width: 100%" v-loading="loadingRanking">
            <el-table-column type="index" label="排名" width="80">
              <template #default="{ $index }">
                <el-tag :type="$index < 3 ? 'success' : 'info'" effect="dark">
                  {{ $index + 1 }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="productName" label="商品名称" min-width="150" />
            <el-table-column prop="salesCount" label="销量" width="120" sortable>
              <template #default="{ row }">
                <span class="sales-count">{{ row.salesCount || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="salesAmount" label="销售额" width="120" sortable>
              <template #default="{ row }">
                <span class="sales-amount">¥{{ formatMoney(row.salesAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="goodRate" label="好评率" width="120" sortable>
              <template #default="{ row }">
                <el-progress
                  :percentage="Number(row.goodRate || 0)"
                  :color="getGoodRateColor(row.goodRate)"
                  :stroke-width="8"
                />
                <span class="good-rate-text">{{ row.goodRate || 0 }}%</span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 热门商品 -->
        <el-tab-pane label="热门商品" name="hot">
          <el-table :data="hotProducts" style="width: 100%" v-loading="loadingHot">
            <el-table-column prop="productName" label="商品名称" min-width="150" />
            <el-table-column prop="salesCount" label="销量" width="120" sortable>
              <template #default="{ row }">
                <span class="sales-count hot">{{ row.salesCount || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="salesAmount" label="销售额" width="120" sortable>
              <template #default="{ row }">
                <span class="sales-amount">¥{{ formatMoney(row.salesAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="growthRate" label="增长率" width="120">
              <template #default="{ row }">
                <span :class="['growth-rate', row.growthRate >= 0 ? 'positive' : 'negative']">
                  {{ row.growthRate >= 0 ? '+' : '' }}{{ row.growthRate || 0 }}%
                </span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag type="success" effect="dark">热销</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 滞销商品 -->
        <el-tab-pane label="滞销商品" name="slow">
          <el-table :data="slowProducts" style="width: 100%" v-loading="loadingSlow">
            <el-table-column prop="productName" label="商品名称" min-width="150" />
            <el-table-column prop="salesCount" label="销量" width="120" sortable>
              <template #default="{ row }">
                <span class="sales-count slow">{{ row.salesCount || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="salesAmount" label="销售额" width="120" sortable>
              <template #default="{ row }">
                <span class="sales-amount">¥{{ formatMoney(row.salesAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="stock" label="库存" width="100">
              <template #default="{ row }">
                <span :class="['stock-count', row.stock <= row.stockAlert ? 'low' : '']">
                  {{ row.stock || 0 }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="建议" width="200">
              <template #default="{ row }">
                <el-tag type="warning">建议调整库存或促销</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ShoppingBag,
  Money,
  Document,
  TrendCharts,
  Star,
  RefreshRight,
  Download
} from '@element-plus/icons-vue'
import {
  getCoreData,
  getOrderTrend,
  getSalesTrend,
  getProductRanking,
  getHotProducts,
  getSlowProducts,
  exportCoreData,
  exportProductData,
  exportAllData
} from '@/api/analytics'

// 响应式数据
const loadingOrderTrend = ref(false)
const loadingSalesTrend = ref(false)
const loadingRanking = ref(false)
const loadingHot = ref(false)
const loadingSlow = ref(false)

const productTab = ref('ranking')

// 核心数据
const coreData = reactive({
  todayOrderCount: 0,
  todaySalesAmount: 0,
  monthOrderCount: 0,
  monthSalesAmount: 0,
  shopRating: '0.0',
  repurchaseRate: '0.0'
})

// 图表引用
const orderTrendChart = ref(null)
const salesTrendChart = ref(null)

// 商品数据
const productRanking = ref([])
const hotProducts = ref([])
const slowProducts = ref([])

// 格式化金额
const formatMoney = (amount) => {
  if (!amount) return '0.00'
  return Number(amount).toFixed(2)
}

// 获取好评率颜色
const getGoodRateColor = (rate) => {
  const numRate = Number(rate || 0)
  if (numRate >= 90) return '#67c23a'
  if (numRate >= 70) return '#e6a23c'
  return '#f56c6c'
}

// 加载核心数据
const loadCoreData = async () => {
  try {
    const response = await getCoreData()
    if (response.code === 200 && response.data) {
      Object.assign(coreData, {
        todayOrderCount: response.data.todayOrderCount || 0,
        todaySalesAmount: response.data.todaySalesAmount || 0,
        monthOrderCount: response.data.monthOrderCount || 0,
        monthSalesAmount: response.data.monthSalesAmount || 0,
        shopRating: (response.data.shopRating || 0).toFixed(1),
        repurchaseRate: (response.data.repurchaseRate || 0).toFixed(1)
      })
    }
  } catch (error) {
    console.error('加载核心数据失败:', error)
    // 使用模拟数据
    Object.assign(coreData, {
      todayOrderCount: 125,
      todaySalesAmount: 9850.00,
      monthOrderCount: 3420,
      monthSalesAmount: 268500.00,
      shopRating: '4.8',
      repurchaseRate: '65.5'
    })
  }
}

// 加载订单量趋势
const loadOrderTrend = async () => {
  try {
    loadingOrderTrend.value = true
    const response = await getOrderTrend(7)
    if (response.code === 200 && response.data) {
      createOrderTrendChart(response.data)
    }
  } catch (error) {
    console.error('加载订单量趋势失败:', error)
    // 使用模拟数据
    createOrderTrendChart({
      categories: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      data: [85, 92, 78, 105, 128, 156, 142]
    })
  } finally {
    loadingOrderTrend.value = false
  }
}

// 加载销售额趋势
const loadSalesTrend = async () => {
  try {
    loadingSalesTrend.value = true
    const response = await getSalesTrend(30)
    if (response.code === 200 && response.data) {
      createSalesTrendChart(response.data)
    }
  } catch (error) {
    console.error('加载销售额趋势失败:', error)
    // 使用模拟数据
    const dates = []
    const amounts = []
    for (let i = 29; i >= 0; i--) {
      const date = new Date()
      date.setDate(date.getDate() - i)
      dates.push(`${date.getMonth() + 1}/${date.getDate()}`)
      amounts.push(Math.floor(Math.random() * 5000) + 5000)
    }
    createSalesTrendChart({
      categories: dates,
      data: amounts
    })
  } finally {
    loadingSalesTrend.value = false
  }
}

// 创建订单量趋势图（折线图）
const createOrderTrendChart = (chartData) => {
  if (!orderTrendChart.value) return
  
  const { categories, data } = chartData
  const maxValue = Math.max(...data, 1)
  
  orderTrendChart.value.innerHTML = `
    <div class="chart-wrapper">
      <div class="chart-title">近7天订单量变化趋势</div>
      <div class="line-chart-container">
        <div class="chart-y-axis">
          ${Array.from({ length: 5 }, (_, i) => {
            const value = Math.round((maxValue / 4) * (4 - i))
            return `<div class="y-label">${value}</div>`
          }).join('')}
        </div>
        <div class="chart-content">
          <svg class="line-chart" viewBox="0 0 600 300">
            <defs>
              <linearGradient id="orderGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" style="stop-color:#409eff;stop-opacity:0.3" />
                <stop offset="100%" style="stop-color:#409eff;stop-opacity:0" />
              </linearGradient>
            </defs>
            <polyline
              points="${categories.map((_, i) => {
                const x = (i / (categories.length - 1)) * 600
                const y = 300 - (data[i] / maxValue) * 250
                return `${x},${y}`
              }).join(' ')}"
              fill="none"
              stroke="#409eff"
              stroke-width="3"
            />
            <polygon
              points="0,300 ${categories.map((_, i) => {
                const x = (i / (categories.length - 1)) * 600
                const y = 300 - (data[i] / maxValue) * 250
                return `${x},${y}`
              }).join(' ')} 600,300"
              fill="url(#orderGradient)"
            />
            ${categories.map((_, i) => {
              const x = (i / (categories.length - 1)) * 600
              const y = 300 - (data[i] / maxValue) * 250
              return `<circle cx="${x}" cy="${y}" r="5" fill="#409eff" />`
            }).join('')}
          </svg>
          <div class="chart-x-axis">
            ${categories.map(cat => `<div class="x-label">${cat}</div>`).join('')}
          </div>
        </div>
      </div>
      <div class="chart-legend">
        <div class="legend-item">
          <span class="legend-color" style="background: #409eff;"></span>
          <span>订单量</span>
        </div>
      </div>
    </div>
  `
}

// 创建销售额趋势图（柱状图）
const createSalesTrendChart = (chartData) => {
  if (!salesTrendChart.value) return
  
  const { categories, data } = chartData
  const maxValue = Math.max(...data, 1)
  
  salesTrendChart.value.innerHTML = `
    <div class="chart-wrapper">
      <div class="chart-title">近30天销售额变化趋势</div>
      <div class="bar-chart-container">
        <div class="chart-y-axis">
          ${Array.from({ length: 5 }, (_, i) => {
            const value = Math.round((maxValue / 4) * (4 - i))
            return `<div class="y-label">¥${(value / 1000).toFixed(1)}k</div>`
          }).join('')}
        </div>
        <div class="chart-content">
          <div class="bar-chart">
            ${categories.map((cat, i) => {
              const height = (data[i] / maxValue) * 100
              return `
                <div class="bar-item" style="flex: 1">
                  <div class="bar-wrapper" style="height: 250px">
                    <div class="bar" style="height: ${height}%; background: linear-gradient(to top, #67c23a, #85ce61);" title="${cat}: ¥${formatMoney(data[i])}">
                      <span class="bar-value">¥${(data[i] / 1000).toFixed(1)}k</span>
                    </div>
                  </div>
                  <div class="x-label">${i % 5 === 0 ? cat : ''}</div>
                </div>
              `
            }).join('')}
          </div>
        </div>
      </div>
      <div class="chart-legend">
        <div class="legend-item">
          <span class="legend-color" style="background: #67c23a;"></span>
          <span>销售额</span>
        </div>
      </div>
    </div>
  `
}

// 加载商品销量排行
const loadProductRanking = async () => {
  try {
    loadingRanking.value = true
    const response = await getProductRanking({
      current: 1,
      size: 20
    })
    if (response.code === 200 && response.data) {
      productRanking.value = response.data.records || []
    }
  } catch (error) {
    console.error('加载商品销量排行失败:', error)
    // 使用模拟数据
    productRanking.value = [
      { productName: '招牌珍珠奶茶', salesCount: 1250, salesAmount: 62500, goodRate: 96 },
      { productName: '经典原味奶茶', salesCount: 980, salesAmount: 49000, goodRate: 94 },
      { productName: '芒果冰沙', salesCount: 856, salesAmount: 51360, goodRate: 92 },
      { productName: '草莓奶盖', salesCount: 720, salesAmount: 43200, goodRate: 89 },
      { productName: '柠檬蜂蜜茶', salesCount: 650, salesAmount: 32500, goodRate: 91 }
    ]
  } finally {
    loadingRanking.value = false
  }
}

// 加载热门商品
const loadHotProducts = async () => {
  try {
    loadingHot.value = true
    const response = await getHotProducts({
      current: 1,
      size: 20
    })
    if (response.code === 200 && response.data) {
      hotProducts.value = response.data.records || []
    }
  } catch (error) {
    console.error('加载热门商品失败:', error)
    // 使用模拟数据
    hotProducts.value = [
      { productName: '招牌珍珠奶茶', salesCount: 1250, salesAmount: 62500, growthRate: 25.6 },
      { productName: '经典原味奶茶', salesCount: 980, salesAmount: 49000, growthRate: 18.3 },
      { productName: '芒果冰沙', salesCount: 856, salesAmount: 51360, growthRate: 15.2 }
    ]
  } finally {
    loadingHot.value = false
  }
}

// 加载滞销商品
const loadSlowProducts = async () => {
  try {
    loadingSlow.value = true
    const response = await getSlowProducts({
      current: 1,
      size: 20
    })
    if (response.code === 200 && response.data) {
      slowProducts.value = response.data.records || []
    }
  } catch (error) {
    console.error('加载滞销商品失败:', error)
    // 使用模拟数据
    slowProducts.value = [
      { productName: '抹茶奶盖', salesCount: 25, salesAmount: 1250, stock: 150, stockAlert: 100 },
      { productName: '红豆奶茶', salesCount: 18, salesAmount: 900, stock: 80, stockAlert: 50 }
    ]
  } finally {
    loadingSlow.value = false
  }
}

// 处理商品标签切换
const handleProductTabChange = (tab) => {
  if (tab === 'ranking') {
    loadProductRanking()
  } else if (tab === 'hot') {
    loadHotProducts()
  } else if (tab === 'slow') {
    loadSlowProducts()
  }
}

// 导出图表
const exportChart = (type) => {
  ElMessage.success(`${type === 'order' ? '订单量' : '销售额'}趋势图导出功能开发中...`)
}

// 导出商品数据
const handleExportProductData = async () => {
  try {
    const params = {
      tab: productTab.value,
      startTime: null,
      endTime: null
    }
    await exportProductData(params)
    ElMessage.success('商品数据导出成功')
  } catch (error) {
    console.error('导出商品数据失败:', error)
    ElMessage.error('导出商品数据失败')
  }
}

// 导出全部数据
const handleExportAll = async () => {
  try {
    const params = {
      startTime: null,
      endTime: null
    }
    await exportAllData(params)
    ElMessage.success('全部数据导出成功')
  } catch (error) {
    console.error('导出全部数据失败:', error)
    ElMessage.error('导出全部数据失败')
  }
}

// 初始化
onMounted(async () => {
  await Promise.all([
    loadCoreData(),
    loadOrderTrend(),
    loadSalesTrend(),
    loadProductRanking()
  ])
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
  margin: 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.summary-cards {
  margin-bottom: 20px;
}

.summary-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.summary-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
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
  margin-right: 15px;
  font-size: 24px;
  color: white;
}

.card-icon.today-order {
  background: linear-gradient(135deg, #409eff, #66b1ff);
}

.card-icon.today-sales {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.card-icon.month-order {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
}

.card-icon.month-sales {
  background: linear-gradient(135deg, #f56c6c, #f78989);
}

.card-icon.rating {
  background: linear-gradient(135deg, #e6a23c, #f0c675);
}

.card-icon.repurchase {
  background: linear-gradient(135deg, #909399, #b3b6bb);
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

.charts-section {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  min-height: 350px;
  padding: 20px;
}

.chart-wrapper {
  width: 100%;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
  text-align: center;
}

.line-chart-container,
.bar-chart-container {
  display: flex;
  position: relative;
}

.chart-y-axis {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  width: 50px;
  padding-right: 10px;
  font-size: 12px;
  color: #909399;
}

.y-label {
  text-align: right;
}

.chart-content {
  flex: 1;
}

.line-chart {
  width: 100%;
  height: 300px;
}

.chart-x-axis {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
}

.x-label {
  text-align: center;
}

.bar-chart {
  display: flex;
  align-items: flex-end;
  height: 280px;
  gap: 2px;
}

.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.bar-wrapper {
  width: 100%;
  display: flex;
  align-items: flex-end;
}

.bar {
  width: 100%;
  border-radius: 4px 4px 0 0;
  position: relative;
  transition: all 0.3s ease;
}

.bar:hover {
  opacity: 0.8;
}

.bar-value {
  position: absolute;
  top: -20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 10px;
  color: #303133;
  white-space: nowrap;
  display: none;
}

.bar:hover .bar-value {
  display: block;
}

.chart-legend {
  display: flex;
  justify-content: center;
  margin-top: 15px;
  gap: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 2px;
}

.product-data-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.sales-count {
  font-weight: 500;
  color: #303133;
}

.sales-count.hot {
  color: #f56c6c;
}

.sales-count.slow {
  color: #909399;
}

.sales-amount {
  font-weight: 500;
  color: #67c23a;
}

.good-rate-text {
  margin-left: 10px;
  font-size: 12px;
  color: #606266;
}

.growth-rate {
  font-weight: 500;
}

.growth-rate.positive {
  color: #67c23a;
}

.growth-rate.negative {
  color: #f56c6c;
}

.stock-count {
  font-weight: 500;
  color: #303133;
}

.stock-count.low {
  color: #f56c6c;
}

:deep(.el-progress) {
  display: inline-block;
  width: 80px;
}

:deep(.el-progress-bar__outer) {
  background-color: #ebeef5;
}
</style>
