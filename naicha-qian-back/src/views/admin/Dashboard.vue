<template>
  <div class="page-container">
    <div class="page-header">
      <h2>管理员仪表盘</h2>
      <el-button 
        :icon="Refresh" 
        @click="loadData" 
        :loading="loading"
        circle
        size="small"
      />
    </div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon">
              <el-icon size="40" color="#409EFF"><Shop /></el-icon>
            </div>
            <div class="stats-info">
              <h3>商家总数</h3>
              <p class="stats-number">{{ formatNumber(stats.totalMerchants) }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon">
              <el-icon size="40" color="#67C23A"><User /></el-icon>
            </div>
            <div class="stats-info">
              <h3>用户总数</h3>
              <p class="stats-number">{{ formatNumber(stats.totalUsers) }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon">
              <el-icon size="40" color="#E6A23C"><Document /></el-icon>
            </div>
            <div class="stats-info">
              <h3>订单总数</h3>
              <p class="stats-number">{{ formatNumber(stats.totalOrders) }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon">
              <el-icon size="40" color="#F56C6C"><Money /></el-icon>
            </div>
            <div class="stats-info">
              <h3>平台收入</h3>
              <p class="stats-number">{{ formatCurrency(stats.platformRevenue) }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>用户增长趋势（最近{{ days }}天）</span>
          </template>
          <div class="chart-container">
            <div v-if="userGrowthTrend.categories && userGrowthTrend.categories.length > 0" class="chart-data">
              <div class="chart-bars">
                <div 
                  v-for="(value, index) in userGrowthTrend.data" 
                  :key="index"
                  class="chart-bar-item"
                >
                  <div class="bar-label">{{ userGrowthTrend.categories[index] }}</div>
                  <div class="bar-wrapper">
                    <div 
                      class="bar" 
                      :style="{ height: `${(value / maxUserValue) * 100}%` }"
                    ></div>
                  </div>
                  <div class="bar-value">{{ value }}</div>
                </div>
              </div>
            </div>
            <div v-else class="chart-placeholder">
              <p>暂无数据</p>
              <p class="chart-desc">最近{{ days }}天没有用户注册</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>订单统计（最近{{ days }}天）</span>
          </template>
          <div class="chart-container">
            <div v-if="orderStatsTrend.categories && orderStatsTrend.categories.length > 0" class="chart-data">
              <div class="chart-bars">
                <div 
                  v-for="(category, index) in orderStatsTrend.categories" 
                  :key="index"
                  class="chart-bar-item"
                >
                  <div class="bar-label">{{ category }}</div>
                  <div class="bar-wrapper">
                    <div 
                      class="bar bar-order" 
                      :style="{ height: `${(orderStatsTrend.orderCounts[index] / maxOrderCount) * 100}%` }"
                    ></div>
                  </div>
                  <div class="bar-value-group">
                    <div class="bar-value">{{ orderStatsTrend.orderCounts[index] }}单</div>
                    <div class="bar-amount">¥{{ formatAmount(orderStatsTrend.orderAmounts[index]) }}</div>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="chart-placeholder">
              <p>暂无数据</p>
              <p class="chart-desc">最近{{ days }}天没有订单</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 最近活动 -->
    <el-row :gutter="20" class="activity-row">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>最近活动</span>
          </template>
          <el-timeline v-if="recentActivities && recentActivities.length > 0">
            <el-timeline-item 
              v-for="(activity, index) in recentActivities" 
              :key="index"
              :timestamp="activity.timestamp" 
              placement="top"
            >
              <el-card>
                <h4>{{ activity.title }}</h4>
                <p>{{ activity.description }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
          <div v-else class="empty-activities">
            <p>暂无活动记录</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Shop, User, Document, Money, Refresh } from '@element-plus/icons-vue'
import { getDashboardData } from '@/api/admin'

// 响应式数据
const loading = ref(false)
const days = ref(30)

// 统计数据
const stats = reactive({
  totalMerchants: 0,
  totalUsers: 0,
  totalOrders: 0,
  platformRevenue: 0
})

// 用户增长趋势
const userGrowthTrend = reactive({
  categories: [],
  data: []
})

// 订单统计趋势
const orderStatsTrend = reactive({
  categories: [],
  orderCounts: [],
  orderAmounts: []
})

// 最近活动
const recentActivities = ref([])

// 计算最大值用于图表显示
const maxUserValue = computed(() => {
  if (userGrowthTrend.data.length === 0) return 1
  return Math.max(...userGrowthTrend.data.map(v => typeof v === 'number' ? v : parseFloat(v || 0)))
})

const maxOrderCount = computed(() => {
  if (orderStatsTrend.orderCounts.length === 0) return 1
  return Math.max(...orderStatsTrend.orderCounts.map(v => typeof v === 'number' ? v : parseFloat(v || 0)))
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getDashboardData(days.value)
    if (res.code === 200 && res.data) {
      const data = res.data
      
      // 更新统计数据
      if (data.stats) {
        stats.totalMerchants = data.stats.totalMerchants || 0
        stats.totalUsers = data.stats.totalUsers || 0
        stats.totalOrders = data.stats.totalOrders || 0
        stats.platformRevenue = data.stats.platformRevenue || 0
      }
      
      // 更新用户增长趋势
      if (data.userGrowthTrend) {
        userGrowthTrend.categories = data.userGrowthTrend.categories || []
        userGrowthTrend.data = data.userGrowthTrend.data || []
      }
      
      // 更新订单统计趋势
      if (data.orderStatsTrend) {
        orderStatsTrend.categories = data.orderStatsTrend.categories || []
        orderStatsTrend.orderCounts = data.orderStatsTrend.orderCounts || []
        orderStatsTrend.orderAmounts = data.orderStatsTrend.orderAmounts || []
      }
      
      // 更新最近活动
      recentActivities.value = data.recentActivities || []
    }
  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
    ElMessage.error('加载仪表盘数据失败')
  } finally {
    loading.value = false
  }
}

// 格式化数字（添加千分位）
const formatNumber = (num) => {
  if (num === null || num === undefined) return '0'
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 格式化货币
const formatCurrency = (amount) => {
  if (!amount) return '¥0'
  const num = typeof amount === 'string' ? parseFloat(amount) : amount
  return '¥' + num.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 格式化金额（不含货币符号）
const formatAmount = (amount) => {
  if (!amount) return '0.00'
  const num = typeof amount === 'string' ? parseFloat(amount) : amount
  return num.toFixed(2)
}

// 组件挂载时加载数据
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stats-card {
  .stats-content {
    display: flex;
    align-items: center;
    
    .stats-icon {
      margin-right: 20px;
    }
    
    .stats-info {
      h3 {
        margin: 0 0 10px 0;
        font-size: 14px;
        color: #666;
        font-weight: normal;
      }
      
      .stats-number {
        margin: 0;
        font-size: 24px;
        font-weight: bold;
        color: #333;
      }
    }
  }
}

.chart-row {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
  overflow-x: auto;
}

.chart-data {
  height: 100%;
  padding: 20px;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 100%;
  gap: 10px;
}

.chart-bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 60px;
  height: 100%;
}

.bar-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 5px;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
}

.bar-wrapper {
  flex: 1;
  width: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  margin-bottom: 5px;
  min-height: 100px;
}

.bar {
  width: 100%;
  max-width: 40px;
  background: linear-gradient(to top, #409EFF, #79BBFF);
  border-radius: 4px 4px 0 0;
  transition: all 0.3s;
  min-height: 5px;
}

.bar-order {
  background: linear-gradient(to top, #E6A23C, #EEBE77);
}

.bar-value {
  font-size: 12px;
  color: #333;
  font-weight: bold;
  text-align: center;
  margin-top: 5px;
}

.bar-value-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 5px;
}

.bar-amount {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}

.chart-placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 4px;
  
  p {
    margin: 0;
    color: #666;
  }
  
  .chart-desc {
    font-size: 12px;
    margin-top: 5px;
  }
}

.activity-row {
  .el-timeline {
    padding-left: 0;
  }
}

.empty-activities {
  padding: 40px;
  text-align: center;
  color: #999;
}
</style>
