<template>
  <div class="page-container">
    <div class="page-header">
      <h2>客户分析</h2>
      <el-button type="primary" @click="handleExportCustomerData">
        <el-icon><Download /></el-icon>
        导出客户数据
      </el-button>
    </div>

    <!-- 客户画像概览 -->
    <el-row :gutter="20" class="overview-cards">
      <el-col :span="8">
        <el-card class="overview-card">
          <div class="overview-content">
            <div class="overview-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ customerProfile.totalCustomers || 0 }}</div>
              <div class="overview-label">总客户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="overview-card">
          <div class="overview-content">
            <div class="overview-icon">
              <el-icon><Star /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ customerProfile.activeCustomers || 0 }}</div>
              <div class="overview-label">活跃客户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="overview-card">
          <div class="overview-content">
            <div class="overview-icon">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="overview-info">
              <div class="overview-value">{{ customerProfile.newCustomers || 0 }}</div>
              <div class="overview-label">新客户（本月）</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 消费频次分析 -->
    <el-card class="analysis-card">
      <template #header>
        <div class="card-header">
          <span>消费频次分析</span>
          <el-tag type="info">基于最近30天数据</el-tag>
        </div>
      </template>
      <div class="frequency-content">
        <div ref="frequencyChart" class="frequency-chart" v-loading="loadingFrequency"></div>
        <div class="frequency-stats">
          <div class="stat-card" v-for="(item, index) in frequencyStats" :key="index">
            <div class="stat-label">{{ item.label }}</div>
            <div class="stat-value">{{ item.value }}</div>
            <div class="stat-trend" :class="item.trend >= 0 ? 'positive' : 'negative'">
              <el-icon v-if="item.trend >= 0"><ArrowUp /></el-icon>
              <el-icon v-else><ArrowDown /></el-icon>
              <span>{{ Math.abs(item.trend) }}%</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 消费偏好分析 -->
    <el-card class="analysis-card">
      <template #header>
        <div class="card-header">
          <span>消费偏好分析</span>
          <el-tag type="info">基于最近90天数据</el-tag>
        </div>
      </template>
      <div class="preference-content">
        <div ref="preferenceChart" class="preference-chart" v-loading="loadingPreference"></div>
        <div class="preference-list">
          <div
            class="preference-item"
            v-for="(item, index) in preferenceData"
            :key="index"
          >
            <div class="preference-name">
              <el-icon><CircleCheck /></el-icon>
              <span>{{ item.name }}</span>
            </div>
            <div class="preference-progress">
              <el-progress
                :percentage="item.percentage"
                :color="getPreferenceColor(index)"
                :stroke-width="20"
                :show-text="false"
              />
            </div>
            <div class="preference-value">{{ item.percentage }}%</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 地域分布分析 -->
    <el-card class="analysis-card">
      <template #header>
        <div class="card-header">
          <span>地域分布分析</span>
          <el-tag type="info">基于收货地址统计</el-tag>
        </div>
      </template>
      <div class="region-content">
        <el-row :gutter="20">
          <el-col :span="16">
            <div ref="regionChart" class="region-chart" v-loading="loadingRegion"></div>
          </el-col>
          <el-col :span="8">
            <div class="region-list">
              <div
                class="region-item"
                v-for="(item, index) in regionData"
                :key="index"
              >
                <div class="region-rank">
                  <el-tag :type="index < 3 ? 'success' : 'info'" effect="dark">
                    {{ index + 1 }}
                  </el-tag>
                </div>
                <div class="region-info">
                  <div class="region-name">{{ item.name }}</div>
                  <div class="region-desc">{{ item.description }}</div>
                </div>
                <div class="region-count">{{ item.count }}人</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 客户画像详情表 -->
    <el-card class="analysis-card">
      <template #header>
        <div class="card-header">
          <span>客户画像详情</span>
          <el-button size="small" @click="handleExportDetail">导出详情</el-button>
        </div>
      </template>
      <el-table :data="customerDetailList" style="width: 100%" v-loading="loadingDetail">
        <el-table-column prop="customerName" label="客户名称" width="150" />
        <el-table-column prop="phone" label="联系方式" width="130" />
        <el-table-column prop="frequency" label="消费频次" width="120" sortable>
          <template #default="{ row }">
            <el-tag :type="getFrequencyTag(row.frequency)">
              {{ row.frequency }}次/周
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="preference" label="消费偏好" width="150">
          <template #default="{ row }">
            <el-tag type="warning">{{ row.preference }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="region" label="所属区域" width="180" />
        <el-table-column prop="totalAmount" label="累计消费" width="120" sortable>
          <template #default="{ row }">
            <span class="amount">¥{{ formatMoney(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastOrderTime" label="最近订单" width="150" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewCustomerDetail(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="detailCurrentPage"
          v-model:page-size="detailPageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="detailTotal"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleDetailSizeChange"
          @current-change="handleDetailCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  User,
  Star,
  TrendCharts,
  Download,
  ArrowUp,
  ArrowDown,
  CircleCheck
} from '@element-plus/icons-vue'
import {
  getCustomerProfile,
  getConsumptionFrequency,
  getConsumptionPreference,
  getRegionDistribution,
  getCustomerDetailList,
  exportCustomerData
} from '@/api/analytics'

// 响应式数据
const loadingFrequency = ref(false)
const loadingPreference = ref(false)
const loadingRegion = ref(false)
const loadingDetail = ref(false)

// 客户画像概览
const customerProfile = reactive({
  totalCustomers: 0,
  activeCustomers: 0,
  newCustomers: 0
})

// 图表引用
const frequencyChart = ref(null)
const preferenceChart = ref(null)
const regionChart = ref(null)

// 消费频次统计
const frequencyStats = ref([])

// 消费偏好数据
const preferenceData = ref([])

// 地域分布数据
const regionData = ref([])

// 客户详情列表
const customerDetailList = ref([])
const detailCurrentPage = ref(1)
const detailPageSize = ref(20)
const detailTotal = ref(0)

// 格式化金额
const formatMoney = (amount) => {
  if (!amount) return '0.00'
  return Number(amount).toFixed(2)
}

// 获取偏好颜色
const getPreferenceColor = (index) => {
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#9c27b0']
  return colors[index % colors.length]
}

// 获取消费频次标签
const getFrequencyTag = (frequency) => {
  if (frequency >= 3) return 'success'
  if (frequency >= 2) return 'warning'
  return 'info'
}

// 加载客户画像概览
const loadCustomerProfile = async () => {
  try {
    const response = await getCustomerProfile()
    if (response.code === 200 && response.data) {
      Object.assign(customerProfile, {
        totalCustomers: response.data.totalCustomers || 0,
        activeCustomers: response.data.activeCustomers || 0,
        newCustomers: response.data.newCustomers || 0
      })
    }
  } catch (error) {
    console.error('加载客户画像概览失败:', error)
    // 使用模拟数据
    Object.assign(customerProfile, {
      totalCustomers: 2845,
      activeCustomers: 1520,
      newCustomers: 156
    })
  }
}

// 加载消费频次分析
const loadConsumptionFrequency = async () => {
  try {
    loadingFrequency.value = true
    const response = await getConsumptionFrequency()
    if (response.code === 200 && response.data) {
      frequencyStats.value = response.data.stats || []
      createFrequencyChart(response.data)
    }
  } catch (error) {
    console.error('加载消费频次分析失败:', error)
    // 使用模拟数据
    frequencyStats.value = [
      { label: '每周2次以上', value: '856人 (30.1%)', trend: 5.2 },
      { label: '每周1-2次', value: '1420人 (49.9%)', trend: 2.1 },
      { label: '每月1-3次', value: '420人 (14.8%)', trend: -1.5 },
      { label: '偶尔消费', value: '149人 (5.2%)', trend: -3.2 }
    ]
    createFrequencyChart({
      categories: ['每周2次以上', '每周1-2次', '每月1-3次', '偶尔消费'],
      data: [856, 1420, 420, 149],
      percentages: [30.1, 49.9, 14.8, 5.2]
    })
  } finally {
    loadingFrequency.value = false
  }
}

// 创建消费频次图表
const createFrequencyChart = (chartData) => {
  if (!frequencyChart.value) return
  
  const { categories, data, percentages } = chartData
  const maxValue = Math.max(...data, 1)
  
  frequencyChart.value.innerHTML = `
    <div class="frequency-chart-wrapper">
      <div class="frequency-title">客户消费频次分布</div>
      <div class="frequency-bars">
        ${categories.map((cat, i) => {
          const height = (data[i] / maxValue) * 100
          return `
            <div class="frequency-bar-item">
              <div class="frequency-bar-wrapper">
                <div class="frequency-bar" style="height: ${height}%; background: linear-gradient(to top, ${getPreferenceColor(i)}, ${getPreferenceColor(i)}dd);">
                  <div class="frequency-bar-value">${data[i]}</div>
                </div>
              </div>
              <div class="frequency-bar-label">${cat}</div>
              <div class="frequency-bar-percent">${percentages[i]}%</div>
            </div>
          `
        }).join('')}
      </div>
    </div>
  `
}

// 加载消费偏好分析
const loadConsumptionPreference = async () => {
  try {
    loadingPreference.value = true
    const response = await getConsumptionPreference()
    if (response.code === 200 && response.data) {
      preferenceData.value = response.data || []
      createPreferenceChart(response.data)
    }
  } catch (error) {
    console.error('加载消费偏好分析失败:', error)
    // 使用模拟数据
    preferenceData.value = [
      { name: '偏好果茶', percentage: 45.6 },
      { name: '偏好奶茶', percentage: 38.2 },
      { name: '偏好咖啡', percentage: 12.5 },
      { name: '偏好其他', percentage: 3.7 }
    ]
    createPreferenceChart(preferenceData.value)
  } finally {
    loadingPreference.value = false
  }
}

// 创建消费偏好图表
const createPreferenceChart = (data) => {
  if (!preferenceChart.value) return
  
  const total = data.reduce((sum, item) => sum + item.percentage, 0)
  let currentAngle = -90
  
  preferenceChart.value.innerHTML = `
    <div class="preference-chart-wrapper">
      <div class="preference-title">客户消费偏好分布</div>
      <div class="pie-chart-container">
        <svg class="pie-chart" viewBox="0 0 200 200">
          ${data.map((item, index) => {
            const percentage = item.percentage / total
            const angle = percentage * 360
            const startAngle = currentAngle
            currentAngle += angle
            
            const startAngleRad = (startAngle * Math.PI) / 180
            const endAngleRad = (currentAngle * Math.PI) / 180
            const largeArcFlag = angle > 180 ? 1 : 0
            
            const x1 = 100 + 80 * Math.cos(startAngleRad)
            const y1 = 100 + 80 * Math.sin(startAngleRad)
            const x2 = 100 + 80 * Math.cos(endAngleRad)
            const y2 = 100 + 80 * Math.sin(endAngleRad)
            
            return `
              <path
                d="M 100 100 L ${x1} ${y1} A 80 80 0 ${largeArcFlag} 1 ${x2} ${y2} Z"
                fill="${getPreferenceColor(index)}"
                stroke="white"
                stroke-width="2"
              />
            `
          }).join('')}
          <circle cx="100" cy="100" r="50" fill="white" />
          <text x="100" y="100" text-anchor="middle" dominant-baseline="middle" font-size="14" font-weight="bold" fill="#303133">
            偏好分布
          </text>
        </svg>
      </div>
    </div>
  `
}

// 加载地域分布分析
const loadRegionDistribution = async () => {
  try {
    loadingRegion.value = true
    const response = await getRegionDistribution()
    if (response.code === 200 && response.data) {
      regionData.value = response.data || []
      createRegionChart(response.data)
    }
  } catch (error) {
    console.error('加载地域分布分析失败:', error)
    // 使用模拟数据
    regionData.value = [
      { name: 'XX小区', description: '距离店铺500米', count: 856 },
      { name: 'YY商业街', description: '距离店铺800米', count: 624 },
      { name: 'ZZ办公楼', description: '距离店铺1.2公里', count: 432 },
      { name: 'AA学校', description: '距离店铺2公里', count: 298 },
      { name: 'BB社区', description: '距离店铺1.5公里', count: 187 }
    ]
    createRegionChart(regionData.value)
  } finally {
    loadingRegion.value = false
  }
}

// 创建地域分布图表
const createRegionChart = (data) => {
  if (!regionChart.value) return
  
  const maxCount = Math.max(...data.map(item => item.count), 1)
  
  regionChart.value.innerHTML = `
    <div class="region-chart-wrapper">
      <div class="region-title">客户地域分布（前10区域）</div>
      <div class="region-bars">
        ${data.slice(0, 10).map((item, index) => {
          const height = (item.count / maxCount) * 100
          return `
            <div class="region-bar-item">
              <div class="region-bar-wrapper">
                <div class="region-bar" style="height: ${height}%; background: linear-gradient(to top, #409eff, #66b1ff);">
                  <div class="region-bar-value">${item.count}</div>
                </div>
              </div>
              <div class="region-bar-label">${item.name}</div>
            </div>
          `
        }).join('')}
      </div>
    </div>
  `
}

// 加载客户详情列表
const loadCustomerDetailList = async () => {
  try {
    loadingDetail.value = true
    const response = await getCustomerDetailList({
      current: detailCurrentPage.value,
      size: detailPageSize.value
    })
    
    if (response.code === 200 && response.data) {
      customerDetailList.value = response.data.records || []
      detailTotal.value = response.data.total || 0
      
      // 格式化数据
      customerDetailList.value = customerDetailList.value.map(item => ({
        ...item,
        customerName: item.customerName || item.phone || '匿名用户',
        phone: item.phone || '-',
        frequency: Number(item.frequency || 0).toFixed(2),
        preference: item.preference || '未分类',
        region: item.region || '未设置',
        totalAmount: Number(item.totalAmount || 0),
        lastOrderTime: item.lastOrderTime ? new Date(item.lastOrderTime).toLocaleString('zh-CN') : '-'
      }))
    }
  } catch (error) {
    console.error('加载客户详情列表失败:', error)
    // 使用模拟数据作为后备
    customerDetailList.value = [
      {
        customerName: '张**',
        phone: '138****5678',
        frequency: 3,
        preference: '偏好果茶',
        region: 'XX小区',
        totalAmount: 2850.50,
        lastOrderTime: '2024-01-15 14:30'
      }
    ]
    detailTotal.value = 100
  } finally {
    loadingDetail.value = false
  }
}

// 查看客户详情
const viewCustomerDetail = (row) => {
  ElMessage.info(`查看客户 ${row.customerName} 的详细信息`)
  // TODO: 实现客户详情查看功能
}

// 导出客户数据
const handleExportCustomerData = async () => {
  try {
    const params = {
      startTime: null,
      endTime: null
    }
    await exportCustomerData(params)
    ElMessage.success('客户数据导出成功')
  } catch (error) {
    console.error('导出客户数据失败:', error)
    ElMessage.error('导出客户数据失败')
  }
}

// 导出详情
const handleExportDetail = async () => {
  try {
    await exportCustomerData({
      type: 'detail',
      current: detailCurrentPage.value,
      size: detailPageSize.value
    })
    ElMessage.success('客户详情导出成功')
  } catch (error) {
    console.error('导出客户详情失败:', error)
    ElMessage.error('导出客户详情失败')
  }
}

// 分页处理
const handleDetailSizeChange = (val) => {
  detailPageSize.value = val
  detailCurrentPage.value = 1
  loadCustomerDetailList()
}

const handleDetailCurrentChange = (val) => {
  detailCurrentPage.value = val
  loadCustomerDetailList()
}

// 初始化
onMounted(async () => {
  await Promise.all([
    loadCustomerProfile(),
    loadConsumptionFrequency(),
    loadConsumptionPreference(),
    loadRegionDistribution(),
    loadCustomerDetailList()
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

.overview-cards {
  margin-bottom: 20px;
}

.overview-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.overview-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
}

.overview-content {
  display: flex;
  align-items: center;
  padding: 15px 0;
}

.overview-icon {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 32px;
  color: white;
  background: linear-gradient(135deg, #409eff, #66b1ff);
}

.overview-info {
  flex: 1;
}

.overview-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.overview-label {
  font-size: 14px;
  color: #909399;
}

.analysis-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.frequency-content {
  display: flex;
  gap: 30px;
}

.frequency-chart {
  flex: 1;
  min-height: 300px;
}

.frequency-stats {
  width: 300px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.stat-card {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 500;
}

.stat-trend.positive {
  color: #67c23a;
}

.stat-trend.negative {
  color: #f56c6c;
}

.frequency-chart-wrapper {
  width: 100%;
}

.frequency-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
  text-align: center;
}

.frequency-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 250px;
}

.frequency-bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.frequency-bar-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.frequency-bar {
  width: 80%;
  border-radius: 8px 8px 0 0;
  position: relative;
  transition: all 0.3s ease;
  min-height: 30px;
}

.frequency-bar:hover {
  opacity: 0.8;
}

.frequency-bar-value {
  position: absolute;
  top: -25px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 14px;
  font-weight: bold;
  color: #303133;
}

.frequency-bar-label {
  margin-top: 10px;
  font-size: 12px;
  color: #606266;
  text-align: center;
}

.frequency-bar-percent {
  margin-top: 5px;
  font-size: 14px;
  font-weight: 600;
  color: #409eff;
}

.preference-content {
  display: flex;
  gap: 30px;
}

.preference-chart {
  flex: 1;
  min-height: 350px;
}

.preference-list {
  width: 300px;
}

.preference-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 15px;
}

.preference-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #303133;
  width: 100px;
}

.preference-progress {
  flex: 1;
}

.preference-value {
  width: 60px;
  text-align: right;
  font-weight: 600;
  color: #409eff;
}

.preference-chart-wrapper {
  width: 100%;
}

.preference-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
  text-align: center;
}

.pie-chart-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

.pie-chart {
  width: 300px;
  height: 300px;
}

.region-content {
  min-height: 400px;
}

.region-chart {
  min-height: 400px;
}

.region-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.region-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.region-item:hover {
  background: #e9ecef;
  transform: translateX(5px);
}

.region-rank {
  width: 40px;
}

.region-info {
  flex: 1;
}

.region-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 5px;
}

.region-desc {
  font-size: 12px;
  color: #909399;
}

.region-count {
  font-weight: 600;
  color: #409eff;
}

.region-chart-wrapper {
  width: 100%;
}

.region-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
  text-align: center;
}

.region-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 350px;
}

.region-bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.region-bar-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.region-bar {
  width: 70%;
  border-radius: 8px 8px 0 0;
  position: relative;
  transition: all 0.3s ease;
  min-height: 30px;
}

.region-bar:hover {
  opacity: 0.8;
}

.region-bar-value {
  position: absolute;
  top: -25px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  font-weight: bold;
  color: #303133;
  white-space: nowrap;
}

.region-bar-label {
  margin-top: 10px;
  font-size: 11px;
  color: #606266;
  text-align: center;
  writing-mode: vertical-lr;
  text-orientation: upright;
}

.amount {
  font-weight: 500;
  color: #67c23a;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

:deep(.el-progress-bar__outer) {
  background-color: #ebeef5;
}
</style>
