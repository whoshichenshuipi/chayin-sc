<template>
  <div class="page-container">
    <div class="page-header">
      <h2>商家评分</h2>
    </div>
    
    <el-card>
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 评分统计 -->
        <el-tab-pane label="评分统计" name="stats">
          <div class="filter-section">
            <el-button type="primary" @click="loadRatingStats">刷新统计</el-button>
          </div>
          
          <el-table :data="ratingStatsList" v-loading="statsLoading" style="width: 100%">
            <el-table-column prop="merchantName" label="商家名称" width="200" />
            <el-table-column prop="overallRating" label="综合评分" width="120">
              <template #default="{ row }">
                <el-rate 
                  :model-value="Number(row.overallRating)" 
                  disabled 
                  show-score
                  :score-template="row.overallRating ? row.overallRating.toFixed(1) : '0.0'"
                />
              </template>
            </el-table-column>
            <el-table-column prop="qualityRating" label="商品质量" width="120">
              <template #default="{ row }">
                <el-rate 
                  :model-value="Number(row.qualityRating)" 
                  disabled 
                  show-score
                  :score-template="row.qualityRating ? row.qualityRating.toFixed(1) : '0.0'"
                />
              </template>
            </el-table-column>
            <el-table-column prop="deliveryRating" label="配送速度" width="120">
              <template #default="{ row }">
                <el-rate 
                  :model-value="Number(row.deliveryRating)" 
                  disabled 
                  show-score
                  :score-template="row.deliveryRating ? row.deliveryRating.toFixed(1) : '0.0'"
                />
              </template>
            </el-table-column>
            <el-table-column prop="serviceRating" label="服务态度" width="120">
              <template #default="{ row }">
                <el-rate 
                  :model-value="Number(row.serviceRating)" 
                  disabled 
                  show-score
                  :score-template="row.serviceRating ? row.serviceRating.toFixed(1) : '0.0'"
                />
              </template>
            </el-table-column>
            <el-table-column prop="reviewCount" label="评价总数" width="100" />
            <el-table-column prop="positiveRate" label="好评率" width="100">
              <template #default="{ row }">
                {{ row.positiveRate ? row.positiveRate.toFixed(1) : '0.0' }}%
              </template>
            </el-table-column>
            <el-table-column label="评分分布" min-width="200">
              <template #default="{ row }">
                <div class="rating-distribution">
                  <span v-for="i in 5" :key="i" class="rating-bar">
                    {{ i }}星: {{ row.ratingDistribution && row.ratingDistribution[i] ? row.ratingDistribution[i] : 0 }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="text" @click="viewMerchantRatings(row.merchantId)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 评价列表 -->
        <el-tab-pane label="评价列表" name="reviews">
          <div class="filter-section">
            <el-form :inline="true" class="filter-form">
              <el-form-item label="商家名称">
                <el-input
                  v-model="filterForm.merchantName"
                  placeholder="请输入商家名称"
                  clearable
                />
              </el-form-item>
              <el-form-item label="评分">
                <el-select v-model="filterForm.rating" placeholder="请选择评分" clearable>
                  <el-option label="5星" :value="5" />
                  <el-option label="4星" :value="4" />
                  <el-option label="3星" :value="3" />
                  <el-option label="2星" :value="2" />
                  <el-option label="1星" :value="1" />
                </el-select>
              </el-form-item>
              <el-form-item label="评价时间">
                <el-date-picker
                  v-model="filterForm.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="searchReviews">查询</el-button>
                <el-button @click="resetFilter">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table :data="ratingList" v-loading="loading" style="width: 100%">
            <el-table-column prop="merchantName" label="商家名称" width="150" />
            <el-table-column prop="userName" label="评价用户" width="120" />
            <el-table-column prop="overallRating" label="综合评分" width="120">
              <template #default="{ row }">
                <el-rate 
                  :model-value="Number(row.overallRating)" 
                  disabled 
                  show-score
                  :score-template="row.overallRating ? row.overallRating.toFixed(1) : '0.0'"
                />
              </template>
            </el-table-column>
            <el-table-column label="细分评分" width="200">
              <template #default="{ row }">
                <div class="sub-ratings">
                  <span>质量: {{ row.qualityRating ? row.qualityRating.toFixed(1) : '0.0' }}</span>
                  <span>配送: {{ row.deliveryRating ? row.deliveryRating.toFixed(1) : '0.0' }}</span>
                  <span>服务: {{ row.serviceRating ? row.serviceRating.toFixed(1) : '0.0' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="comment" label="评价内容" min-width="200" show-overflow-tooltip />
            <el-table-column label="图片" width="80">
              <template #default="{ row }">
                <span v-if="row.images && row.images.length > 0">
                  {{ row.images.length }}张
                </span>
                <span v-else>无</span>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="评价时间" width="160">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="商家回复" width="100">
              <template #default="{ row }">
                <el-tag :type="row.merchantReply ? 'success' : 'info'">
                  {{ row.merchantReply ? '已回复' : '未回复' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="text" @click="viewReviewDetail(row)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination" v-if="pagination.total > 0">
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
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 评价详情对话框 -->
    <el-dialog
      v-model="reviewDetailVisible"
      title="评价详情"
      width="70%"
    >
      <div v-if="currentReview" class="review-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商家名称">{{ currentReview.merchantName }}</el-descriptions-item>
          <el-descriptions-item label="评价用户">{{ currentReview.userName }}</el-descriptions-item>
          <el-descriptions-item label="综合评分">
            <el-rate 
              :model-value="Number(currentReview.overallRating)" 
              disabled 
              show-score
              :score-template="currentReview.overallRating ? currentReview.overallRating.toFixed(1) : '0.0'"
            />
          </el-descriptions-item>
          <el-descriptions-item label="商品质量">
            <el-rate 
              :model-value="Number(currentReview.qualityRating)" 
              disabled 
              show-score
              :score-template="currentReview.qualityRating ? currentReview.qualityRating.toFixed(1) : '0.0'"
            />
          </el-descriptions-item>
          <el-descriptions-item label="配送速度">
            <el-rate 
              :model-value="Number(currentReview.deliveryRating)" 
              disabled 
              show-score
              :score-template="currentReview.deliveryRating ? currentReview.deliveryRating.toFixed(1) : '0.0'"
            />
          </el-descriptions-item>
          <el-descriptions-item label="服务态度">
            <el-rate 
              :model-value="Number(currentReview.serviceRating)" 
              disabled 
              show-score
              :score-template="currentReview.serviceRating ? currentReview.serviceRating.toFixed(1) : '0.0'"
            />
          </el-descriptions-item>
          <el-descriptions-item label="评价内容" :span="2">
            {{ currentReview.comment || '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="评价图片" :span="2" v-if="currentReview.images && currentReview.images.length > 0">
            <div class="review-images">
              <el-image
                v-for="(image, index) in currentReview.images"
                :key="index"
                :src="image"
                :preview-src-list="currentReview.images"
                class="review-image"
                fit="cover"
              />
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="商家回复" :span="2">
            {{ currentReview.merchantReply || '暂无回复' }}
          </el-descriptions-item>
          <el-descriptions-item label="回复时间">
            {{ currentReview.replyTime ? formatDateTime(currentReview.replyTime) : '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="有用数">{{ currentReview.helpfulCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="评价时间">{{ formatDateTime(currentReview.createTime) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMerchantRatingList, getMerchantRatingStatsList } from '@/api/admin'

const activeTab = ref('stats')
const loading = ref(false)
const statsLoading = ref(false)
const reviewDetailVisible = ref(false)
const currentReview = ref(null)

// 筛选表单
const filterForm = reactive({
  merchantName: '',
  rating: null,
  dateRange: []
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

const ratingStatsList = ref([])
const ratingList = ref([])

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// Tab切换
const handleTabChange = (tabName) => {
  if (tabName === 'stats') {
    loadRatingStats()
  } else if (tabName === 'reviews') {
    loadRatingList()
  }
}

// 加载评分统计列表
const loadRatingStats = async () => {
  statsLoading.value = true
  try {
    const response = await getMerchantRatingStatsList()
    if (response.code === 200 && response.data) {
      ratingStatsList.value = response.data
    } else {
      ElMessage.error(response.message || '获取评分统计失败')
    }
  } catch (error) {
    console.error('加载评分统计失败:', error)
    ElMessage.error('加载评分统计失败，请稍后重试')
  } finally {
    statsLoading.value = false
  }
}

// 加载评价列表
const loadRatingList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.currentPage,
      size: pagination.pageSize
    }
    
    if (filterForm.merchantName) {
      params.merchantName = filterForm.merchantName
    }
    
    if (filterForm.rating !== null && filterForm.rating !== '') {
      params.rating = filterForm.rating
    }
    
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startTime = filterForm.dateRange[0] + ' 00:00:00'
      params.endTime = filterForm.dateRange[1] + ' 23:59:59'
    }
    
    const response = await getMerchantRatingList(params)
    if (response.code === 200 && response.data) {
      ratingList.value = response.data.records || []
      pagination.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取评价列表失败')
    }
  } catch (error) {
    console.error('加载评价列表失败:', error)
    ElMessage.error('加载评价列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 查看商家评分详情
const viewMerchantRatings = (merchantId) => {
  filterForm.merchantName = ''
  filterForm.rating = null
  filterForm.dateRange = []
  // 可以通过设置商家ID过滤，这里简化处理，切换到评价列表tab
  activeTab.value = 'reviews'
  // 可以添加商家ID字段来精确查询
  loadRatingList()
}

// 查看评价详情
const viewReviewDetail = (row) => {
  currentReview.value = row
  reviewDetailVisible.value = true
}

// 搜索评价
const searchReviews = () => {
  pagination.currentPage = 1
  loadRatingList()
}

// 重置筛选
const resetFilter = () => {
  filterForm.merchantName = ''
  filterForm.rating = null
  filterForm.dateRange = []
  pagination.currentPage = 1
  loadRatingList()
}

// 分页处理
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  loadRatingList()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  loadRatingList()
}

onMounted(() => {
  loadRatingStats()
})
</script>

<style scoped>
.filter-section {
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 4px;
}

.filter-form {
  margin: 0;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.rating-distribution {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.rating-bar {
  font-size: 12px;
  color: #666;
}

.sub-ratings {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #666;
}

.review-detail {
  .review-images {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
  }
  
  .review-image {
    width: 120px;
    height: 80px;
    border-radius: 4px;
    border: 1px solid #d9d9d9;
    cursor: pointer;
  }
}
</style>
