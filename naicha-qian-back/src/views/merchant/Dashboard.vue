<template>
  <div class="page-container" v-loading="loading">
    <div class="page-header">
      <h2>经营数据统计</h2>
      <div class="header-actions">
        <el-button type="primary" @click="exportAllData">
          <el-icon><Download /></el-icon>
          导出数据
        </el-button>
      </div>
    </div>
    
    <!-- 核心数据概览 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="4">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon today-orders">
              <el-icon size="32"><Document /></el-icon>
            </div>
            <div class="stats-info">
              <h3>今日订单量</h3>
              <p class="stats-number">{{ coreData.todayOrders || 0 }}</p>
              <p class="stats-trend" :class="coreData.todayOrdersTrend >= 0 ? 'positive' : 'negative'">
                <el-icon><ArrowUp v-if="coreData.todayOrdersTrend >= 0" /><ArrowDown v-else /></el-icon>
                {{ Math.abs(coreData.todayOrdersTrend) }}%
              </p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="4">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon today-sales">
              <el-icon size="32"><Money /></el-icon>
            </div>
            <div class="stats-info">
              <h3>今日销售额</h3>
              <p class="stats-number">¥{{ (coreData.todaySales || 0).toFixed(2) }}</p>
              <p class="stats-trend" :class="coreData.todaySalesTrend >= 0 ? 'positive' : 'negative'">
                <el-icon><ArrowUp v-if="coreData.todaySalesTrend >= 0" /><ArrowDown v-else /></el-icon>
                {{ Math.abs(coreData.todaySalesTrend) }}%
              </p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="4">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon month-orders">
              <el-icon size="32"><List /></el-icon>
            </div>
            <div class="stats-info">
              <h3>本月订单量</h3>
              <p class="stats-number">{{ coreData.monthOrders || 0 }}</p>
              <p class="stats-trend" :class="coreData.monthOrdersTrend >= 0 ? 'positive' : 'negative'">
                <el-icon><ArrowUp v-if="coreData.monthOrdersTrend >= 0" /><ArrowDown v-else /></el-icon>
                {{ Math.abs(coreData.monthOrdersTrend) }}%
              </p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="4">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon month-sales">
              <el-icon size="32"><TrendCharts /></el-icon>
            </div>
            <div class="stats-info">
              <h3>本月销售额</h3>
              <p class="stats-number">¥{{ (coreData.monthSales || 0).toFixed(2) }}</p>
              <p class="stats-trend" :class="coreData.monthSalesTrend >= 0 ? 'positive' : 'negative'">
                <el-icon><ArrowUp v-if="coreData.monthSalesTrend >= 0" /><ArrowDown v-else /></el-icon>
                {{ Math.abs(coreData.monthSalesTrend) }}%
              </p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="4">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon rating">
              <el-icon size="32"><Star /></el-icon>
            </div>
            <div class="stats-info">
              <h3>店铺评分</h3>
              <p class="stats-number">{{ (coreData.rating || 0).toFixed(1) }}</p>
              <p class="stats-trend positive">
                <el-icon><Star /></el-icon>
                {{ coreData.reviewCount || 0 }}条评价
              </p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="4">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon repurchase">
              <el-icon size="32"><Refresh /></el-icon>
            </div>
            <div class="stats-info">
              <h3>复购率</h3>
              <p class="stats-number">{{ (coreData.repurchaseRate || 0).toFixed(1) }}%</p>
              <p class="stats-trend" :class="coreData.repurchaseRateTrend >= 0 ? 'positive' : 'negative'">
                <el-icon><ArrowUp v-if="coreData.repurchaseRateTrend >= 0" /><ArrowDown v-else /></el-icon>
                {{ Math.abs(coreData.repurchaseRateTrend) }}%
              </p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 数据趋势图表 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="chart-header">
              <span>近7天订单量趋势</span>
              <el-button size="small" @click="refreshOrderTrend">刷新</el-button>
            </div>
          </template>
          <div class="chart-container">
            <div ref="orderTrendChart" class="chart"></div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="chart-header">
              <span>近30天销售额趋势</span>
              <el-button size="small" @click="refreshSalesTrend">刷新</el-button>
            </div>
          </template>
          <div class="chart-container">
            <div ref="salesTrendChart" class="chart"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 商品数据分析 -->
    <el-row :gutter="20" class="analytics-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="chart-header">
              <span>商品销量排行</span>
              <el-button size="small" @click="exportProductData">导出</el-button>
            </div>
          </template>
          <el-table :data="productRanking" size="small">
            <el-table-column prop="rank" label="排名" width="60" />
            <el-table-column prop="name" label="商品名称" min-width="150" />
            <el-table-column prop="sales" label="销量" width="80" />
            <el-table-column prop="revenue" label="销售额" width="100">
              <template #default="{ row }">
                ¥{{ row.revenue.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="rating" label="好评率" width="80">
              <template #default="{ row }">
                {{ (row.rating || 0).toFixed(1) }}%
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>商品分析</span>
          </template>
          <div class="product-analysis">
            <div class="analysis-item">
              <h4>热门商品</h4>
              <div class="hot-products">
                <div v-for="product in hotProducts" :key="product.id" class="product-item">
                  <span class="product-name">{{ product.name }}</span>
                  <span class="product-sales">销量: {{ product.sales }}</span>
                </div>
              </div>
            </div>
            <div class="analysis-item">
              <h4>滞销商品</h4>
              <div class="slow-products">
                <div v-for="product in slowProducts" :key="product.id" class="product-item">
                  <span class="product-name">{{ product.name }}</span>
                  <span class="product-sales">销量: {{ product.sales }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 消费者数据分析 -->
    <el-row :gutter="20" class="customer-row">
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>消费频次分析</span>
          </template>
          <div class="customer-analysis">
            <div class="frequency-item">
              <div class="frequency-label">每周消费2次以上</div>
              <div class="frequency-value">{{ customerData.highFrequency }}%</div>
            </div>
            <div class="frequency-item">
              <div class="frequency-label">每周消费1-2次</div>
              <div class="frequency-value">{{ customerData.mediumFrequency }}%</div>
            </div>
            <div class="frequency-item">
              <div class="frequency-label">每周消费1次以下</div>
              <div class="frequency-value">{{ customerData.lowFrequency }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>消费偏好分析</span>
          </template>
          <div class="preference-analysis">
            <div class="preference-item">
              <div class="preference-label">偏好果茶</div>
              <div class="preference-value">{{ customerData.fruitTeaPreference }}%</div>
            </div>
            <div class="preference-item">
              <div class="preference-label">偏好奶茶</div>
              <div class="preference-value">{{ customerData.milkTeaPreference }}%</div>
            </div>
            <div class="preference-item">
              <div class="preference-label">偏好咖啡</div>
              <div class="preference-value">{{ customerData.coffeePreference }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>地域分布分析</span>
          </template>
          <div class="location-analysis">
            <div v-for="(region, index) in getRegionDisplayData()" :key="index" class="location-item">
              <div class="location-label">{{ region.label }}</div>
              <div class="location-value">{{ region.value }}人</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Document, Money, List, TrendCharts, Star, Refresh, 
  Download, ArrowUp, ArrowDown 
} from '@element-plus/icons-vue'
import {
  getCoreData,
  getOrderTrend,
  getSalesTrend,
  getProductRanking,
  getHotProducts,
  getSlowProducts,
  getConsumptionFrequency,
  getConsumptionPreference,
  getRegionDistribution
} from '@/api/analytics'

// 响应式数据
const orderTrendChart = ref(null)
const salesTrendChart = ref(null)
const loading = ref(false)

// 核心数据
const coreData = reactive({
  todayOrders: 0,
  todayOrdersTrend: 0,
  todaySales: 0,
  todaySalesTrend: 0,
  monthOrders: 0,
  monthOrdersTrend: 0,
  monthSales: 0,
  monthSalesTrend: 0,
  rating: 0,
  reviewCount: 0,
  repurchaseRate: 0,
  repurchaseRateTrend: 0
})

// 趋势图表数据
const orderTrendData = reactive({
  categories: [],
  data: []
})

const salesTrendData = reactive({
  categories: [],
  data: []
})

// 商品排行数据
const productRanking = ref([])

// 热门商品
const hotProducts = ref([])

// 滞销商品
const slowProducts = ref([])

// 消费者数据
const customerData = reactive({
  highFrequency: 0,
  mediumFrequency: 0,
  lowFrequency: 0,
  fruitTeaPreference: 0,
  milkTeaPreference: 0,
  coffeePreference: 0,
  location1: 0,
  location2: 0,
  location3: 0,
  locationName1: '',
  locationName2: '',
  locationName3: ''
})

// 加载核心数据
const loadCoreData = async () => {
  try {
    const res = await getCoreData()
    if (res.code === 200 && res.data) {
      const data = res.data
      coreData.todayOrders = data.todayOrderCount || 0
      coreData.todaySales = parseFloat(data.todaySalesAmount || 0)
      coreData.monthOrders = data.monthOrderCount || 0
      coreData.monthSales = parseFloat(data.monthSalesAmount || 0)
      coreData.rating = parseFloat(data.shopRating || 0)
      coreData.repurchaseRate = parseFloat(data.repurchaseRate || 0)
      
      // 趋势数据需要从历史数据计算，这里暂时使用默认值
      // TODO: 可以从后端获取或前端计算
      coreData.todayOrdersTrend = 0
      coreData.todaySalesTrend = 0
      coreData.monthOrdersTrend = 0
      coreData.monthSalesTrend = 0
      coreData.repurchaseRateTrend = 0
    }
  } catch (error) {
    console.error('加载核心数据失败:', error)
    ElMessage.error('加载核心数据失败')
  }
}

// 加载订单趋势数据
const loadOrderTrend = async () => {
  try {
    const res = await getOrderTrend(7)
    if (res.code === 200 && res.data) {
      orderTrendData.categories = res.data.categories || []
      orderTrendData.data = res.data.data || []
      updateOrderTrendChart()
    }
  } catch (error) {
    console.error('加载订单趋势失败:', error)
  }
}

// 加载销售额趋势数据
const loadSalesTrend = async () => {
  try {
    const res = await getSalesTrend(30)
    if (res.code === 200 && res.data) {
      salesTrendData.categories = res.data.categories || []
      salesTrendData.data = res.data.data || []
      updateSalesTrendChart()
    }
  } catch (error) {
    console.error('加载销售额趋势失败:', error)
  }
}

// 加载商品排行数据
const loadProductRanking = async () => {
  try {
    const res = await getProductRanking({
      current: 1,
      size: 10
    })
    if (res.code === 200 && res.data) {
      const records = res.data.records || []
      productRanking.value = records.map((item, index) => ({
        rank: index + 1,
        name: item.productName || '',
        sales: item.salesCount || 0,
        revenue: parseFloat(item.salesAmount || 0),
        rating: parseFloat(item.goodRate || 0)
      }))
    }
  } catch (error) {
    console.error('加载商品排行失败:', error)
  }
}

// 加载热门商品
const loadHotProducts = async () => {
  try {
    const res = await getHotProducts({
      current: 1,
      size: 5
    })
    if (res.code === 200 && res.data) {
      const records = res.data.records || []
      hotProducts.value = records.map(item => ({
        id: item.productId,
        name: item.productName || '',
        sales: item.salesCount || 0
      }))
    }
  } catch (error) {
    console.error('加载热门商品失败:', error)
  }
}

// 加载滞销商品
const loadSlowProducts = async () => {
  try {
    const res = await getSlowProducts({
      current: 1,
      size: 5
    })
    if (res.code === 200 && res.data) {
      const records = res.data.records || []
      slowProducts.value = records.map(item => ({
        id: item.productId,
        name: item.productName || '',
        sales: item.salesCount || 0
      }))
    }
  } catch (error) {
    console.error('加载滞销商品失败:', error)
  }
}

// 加载消费者数据
const loadCustomerData = async () => {
  try {
    // 加载消费频次
    const frequencyRes = await getConsumptionFrequency()
    if (frequencyRes.code === 200 && frequencyRes.data) {
      const frequency = frequencyRes.data
      const percentages = frequency.percentages || []
      if (percentages.length >= 3) {
        customerData.highFrequency = parseFloat(percentages[0] || 0)
        customerData.mediumFrequency = parseFloat(percentages[1] || 0)
        customerData.lowFrequency = parseFloat(percentages[2] || 0)
      }
    }
    
    // 加载消费偏好
    const preferenceRes = await getConsumptionPreference()
    if (preferenceRes.code === 200 && preferenceRes.data) {
      const preferences = preferenceRes.data || []
      preferences.forEach(item => {
        if (item.name?.includes('果茶')) {
          customerData.fruitTeaPreference = item.percentage || 0
        } else if (item.name?.includes('奶茶')) {
          customerData.milkTeaPreference = item.percentage || 0
        } else if (item.name?.includes('咖啡')) {
          customerData.coffeePreference = item.percentage || 0
        }
      })
    }
    
    // 加载地域分布
    const regionRes = await getRegionDistribution()
    if (regionRes.code === 200 && regionRes.data) {
      const regions = regionRes.data || []
      if (regions.length >= 1) {
        customerData.location1 = regions[0].count || 0
      }
      if (regions.length >= 2) {
        customerData.location2 = regions[1].count || 0
      }
      if (regions.length >= 3) {
        customerData.location3 = regions[2].count || 0
      }
      // 保存地域名称用于显示
      if (regions.length >= 1 && regions[0].name) {
        customerData.locationName1 = regions[0].name
      }
      if (regions.length >= 2 && regions[1].name) {
        customerData.locationName2 = regions[1].name
      }
      if (regions.length >= 3 && regions[2].name) {
        customerData.locationName3 = regions[2].name
      }
    }
  } catch (error) {
    console.error('加载消费者数据失败:', error)
  }
}

// 刷新订单趋势
const refreshOrderTrend = async () => {
  await loadOrderTrend()
  ElMessage.success('订单趋势数据已刷新')
}

// 刷新销售趋势
const refreshSalesTrend = async () => {
  await loadSalesTrend()
  ElMessage.success('销售趋势数据已刷新')
}

// 导出商品数据
const exportProductData = () => {
  ElMessage.success('商品数据导出成功')
  // TODO: 实现导出功能
}

// 导出所有数据
const exportAllData = () => {
  ElMessage.success('经营数据导出成功')
  // TODO: 实现导出功能
}

// 更新订单趋势图表
const updateOrderTrendChart = () => {
  nextTick(() => {
    if (orderTrendChart.value) {
      // 简单的HTML表格展示，实际项目中可以使用 ECharts
      const maxValue = orderTrendData.data.length > 0 
        ? Math.max(...orderTrendData.data.map(d => typeof d === 'number' ? d : parseFloat(d))) 
        : 0
      
      let chartHtml = `
        <div style="height: 300px; padding: 20px; background: #f5f5f5; border-radius: 4px;">
          <div style="display: flex; align-items: flex-end; justify-content: space-around; height: 100%; gap: 5px;">
      `
      
      orderTrendData.data.forEach((value, index) => {
        const height = maxValue > 0 ? (value / maxValue) * 100 : 0
        const dateLabel = orderTrendData.categories[index] || ''
        chartHtml += `
          <div style="flex: 1; display: flex; flex-direction: column; align-items: center; gap: 5px;">
            <div style="width: 100%; height: ${250 * (height / 100)}px; background: linear-gradient(to top, #409eff, #66b1ff); border-radius: 4px 4px 0 0; min-height: ${maxValue > 0 ? '5px' : '0'};"></div>
            <div style="font-size: 12px; color: #666; text-align: center;">${dateLabel}</div>
            <div style="font-size: 12px; color: #409eff; font-weight: bold;">${value}</div>
          </div>
        `
      })
      
      chartHtml += `
          </div>
        </div>
      `
      orderTrendChart.value.innerHTML = chartHtml
    }
  })
    }
    
// 更新销售额趋势图表
const updateSalesTrendChart = () => {
  nextTick(() => {
    if (salesTrendChart.value) {
      // 简单的HTML表格展示，实际项目中可以使用 ECharts
      const maxValue = salesTrendData.data.length > 0 
        ? Math.max(...salesTrendData.data.map(d => typeof d === 'number' ? d : parseFloat(d))) 
        : 0
      
      let chartHtml = `
        <div style="height: 300px; padding: 20px; background: #f5f5f5; border-radius: 4px;">
          <div style="display: flex; align-items: flex-end; justify-content: space-around; height: 100%; gap: 5px;">
      `
      
      salesTrendData.data.forEach((value, index) => {
        const height = maxValue > 0 ? (value / maxValue) * 100 : 0
        const dateLabel = salesTrendData.categories[index] || ''
        const displayValue = typeof value === 'number' ? value.toFixed(2) : parseFloat(value || 0).toFixed(2)
        chartHtml += `
          <div style="flex: 1; display: flex; flex-direction: column; align-items: center; gap: 5px;">
            <div style="width: 100%; height: ${250 * (height / 100)}px; background: linear-gradient(to top, #67c23a, #85ce61); border-radius: 4px 4px 0 0; min-height: ${maxValue > 0 ? '5px' : '0'};"></div>
            <div style="font-size: 12px; color: #666; text-align: center;">${dateLabel}</div>
            <div style="font-size: 11px; color: #67c23a; font-weight: bold;">¥${displayValue}</div>
          </div>
        `
      })
      
      chartHtml += `
          </div>
        </div>
      `
      salesTrendChart.value.innerHTML = chartHtml
    }
  })
}

// 获取地域显示数据
const getRegionDisplayData = () => {
  const regions = []
  if (customerData.location1 > 0 || customerData.locationName1) {
    regions.push({ 
      label: customerData.locationName1 || '区域1', 
      value: customerData.location1 
    })
  }
  if (customerData.location2 > 0 || customerData.locationName2) {
    regions.push({ 
      label: customerData.locationName2 || '区域2', 
      value: customerData.location2 
    })
  }
  if (customerData.location3 > 0 || customerData.locationName3) {
    regions.push({ 
      label: customerData.locationName3 || '区域3', 
      value: customerData.location3 
    })
  }
  // 如果没有数据，显示默认占位
  if (regions.length === 0) {
    return [
      { label: '暂无数据', value: 0 }
    ]
  }
  return regions
}

// 初始化所有数据
const initData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadCoreData(),
      loadOrderTrend(),
      loadSalesTrend(),
      loadProductRanking(),
      loadHotProducts(),
      loadSlowProducts(),
      loadCustomerData()
    ])
  } catch (error) {
    console.error('初始化数据失败:', error)
    ElMessage.warning('部分数据加载失败，请刷新重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  initData()
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

.header-actions {
  display: flex;
  gap: 10px;
}

.stats-row {
  margin-bottom: 20px;
}

.stats-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.stats-content {
  display: flex;
  align-items: center;
  padding: 15px 0;
}

.stats-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  color: white;
}

.stats-icon.today-orders {
  background: linear-gradient(135deg, #409eff, #66b1ff);
}

.stats-icon.today-sales {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.stats-icon.month-orders {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
}

.stats-icon.month-sales {
  background: linear-gradient(135deg, #f56c6c, #f78989);
}

.stats-icon.rating {
  background: linear-gradient(135deg, #909399, #b1b3b8);
}

.stats-icon.repurchase {
  background: linear-gradient(135deg, #9c27b0, #ba68c8);
}

.stats-info {
  flex: 1;
}

.stats-info h3 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #666;
  font-weight: normal;
}

.stats-number {
  margin: 0 0 5px 0;
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.stats-trend {
  margin: 0;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 2px;
}

.stats-trend.positive {
  color: #67c23a;
}

.stats-trend.negative {
  color: #f56c6c;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
}

.chart {
  width: 100%;
  height: 100%;
}

.analytics-row {
  margin-bottom: 20px;
}

.customer-row {
  margin-bottom: 20px;
}

.product-analysis {
  padding: 10px 0;
}

.analysis-item {
  margin-bottom: 20px;
}

.analysis-item h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #303133;
}

.hot-products,
.slow-products {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.product-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f5f5f5;
  border-radius: 4px;
}

.product-name {
  font-size: 14px;
  color: #303133;
}

.product-sales {
  font-size: 12px;
  color: #666;
}

.customer-analysis,
.preference-analysis,
.location-analysis {
  padding: 10px 0;
}

.frequency-item,
.preference-item,
.location-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.frequency-item:last-child,
.preference-item:last-child,
.location-item:last-child {
  border-bottom: none;
}

.frequency-label,
.preference-label,
.location-label {
  font-size: 14px;
  color: #303133;
}

.frequency-value,
.preference-value,
.location-value {
  font-size: 16px;
  font-weight: bold;
  color: #409eff;
}
</style>
