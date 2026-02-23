<template>
  <div class="page-container">
    <div class="page-header">
      <h2>店铺信息管理</h2>
      <p class="page-description">管理店铺基础信息、营业状态、公告和评价</p>
    </div>
    
    <!-- 基础信息维护 -->
    <el-card class="info-card">
      <template #header>
        <div class="card-header">
          <span>基础信息维护</span>
          <el-tag :type="getStatusType(statusForm.status)">
            {{ getStatusText(statusForm.status) }}
          </el-tag>
        </div>
      </template>
      
      <el-form :model="shopInfo" :rules="rules" ref="shopForm" label-width="120px" class="shop-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="店铺名称" prop="name" required>
              <el-input v-model="shopInfo.name" placeholder="请输入店铺名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone" required>
              <el-input v-model="shopInfo.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="店铺Logo" prop="logo">
          <ImageUpload
            v-model="shopInfo.logo"
            folder="shop/logo"
            placeholder="点击上传店铺Logo"
            tip="建议尺寸：200x200px，支持 JPG/PNG 格式，最大10MB"
            width="120px"
            height="120px"
          />
        </el-form-item>
        
        <el-form-item label="店铺简介" prop="description">
          <el-input 
            v-model="shopInfo.description" 
            type="textarea" 
            :rows="3"
            placeholder="如：专注手工奶茶10年，为您提供最纯正的口感体验"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="营业时间" prop="businessHours">
          <div class="business-hours">
            <div class="hours-item">
              <span class="hours-label">工作日</span>
              <el-time-picker
                v-model="shopInfo.businessHours.weekday"
                is-range
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                format="HH:mm"
                value-format="HH:mm"
              />
            </div>
            <div class="hours-item">
              <span class="hours-label">周末</span>
              <el-time-picker
                v-model="shopInfo.businessHours.weekend"
                is-range
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                format="HH:mm"
                value-format="HH:mm"
              />
            </div>
          </div>
        </el-form-item>
        
        <el-form-item label="店铺地址" prop="address" required>
          <div class="address-input">
            <el-input v-model="shopInfo.address" placeholder="请输入详细地址" />
            <el-button type="primary" @click="openMapDialog" class="map-btn">
              <el-icon><Location /></el-icon>
              地图定位
            </el-button>
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="saveShopInfo" :loading="saving">
            保存基础信息
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 店铺状态与公告管理 -->
    <el-card class="status-card">
      <template #header>
        <span>店铺状态与公告管理</span>
      </template>
      
      <el-form :model="statusForm" label-width="120px" class="status-form">
        <el-form-item label="营业状态" required>
          <el-radio-group v-model="statusForm.status">
            <el-radio :value="1">
              <el-icon><CircleCheck /></el-icon>
              营业中
            </el-radio>
            <el-radio :value="2">
              <el-icon><Clock /></el-icon>
              休息中
            </el-radio>
            <el-radio :value="3">
              <el-icon><CircleClose /></el-icon>
              暂停营业
            </el-radio>
          </el-radio-group>
          <div class="status-tip">
            <el-icon><InfoFilled /></el-icon>
            休息中状态下前台不展示店铺
          </div>
        </el-form-item>
        
        <el-form-item label="店铺公告">
          <el-input 
            v-model="statusForm.announcement" 
            type="textarea" 
            :rows="3"
            placeholder="如：今日新品：多肉葡萄上市、店内装修10月1日恢复营业等"
            maxlength="500"
            show-word-limit
          />
          <div class="announcement-tip">
            公告将实时同步到前台店铺详情页
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="saveStatus" :loading="statusSaving">
            保存状态设置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 评分与评价管理 -->
    <el-card class="rating-card">
      <template #header>
        <div class="card-header">
          <span>评分与评价管理</span>
          <el-button size="small" @click="refreshRatings">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
        </div>
      </template>
      
      <!-- 评分概览 -->
      <div class="rating-overview">
        <div class="overall-score">
          <div class="score-display">
            <span class="score-number">{{ ratingStats.overall }}</span>
            <div class="score-details">
              <el-rate v-model="ratingStats.overall" disabled show-score />
              <p class="score-text">综合评分</p>
            </div>
          </div>
        </div>
        
        <div class="rating-breakdown">
          <div class="rating-item">
            <span class="rating-label">商品质量</span>
            <el-rate v-model="ratingStats.quality" disabled show-score />
            <span class="rating-trend" :class="ratingStats.qualityTrend >= 0 ? 'positive' : 'negative'">
              {{ ratingStats.qualityTrend >= 0 ? '+' : '' }}{{ ratingStats.qualityTrend }}%
            </span>
          </div>
          <div class="rating-item">
            <span class="rating-label">配送速度</span>
            <el-rate v-model="ratingStats.delivery" disabled show-score />
            <span class="rating-trend" :class="ratingStats.deliveryTrend >= 0 ? 'positive' : 'negative'">
              {{ ratingStats.deliveryTrend >= 0 ? '+' : '' }}{{ ratingStats.deliveryTrend }}%
            </span>
          </div>
          <div class="rating-item">
            <span class="rating-label">服务态度</span>
            <el-rate v-model="ratingStats.service" disabled show-score />
            <span class="rating-trend" :class="ratingStats.serviceTrend >= 0 ? 'positive' : 'negative'">
              {{ ratingStats.serviceTrend >= 0 ? '+' : '' }}{{ ratingStats.serviceTrend }}%
            </span>
          </div>
        </div>
        
        <div class="rating-trend-chart">
          <h4>近7天评分趋势</h4>
          <div class="trend-analysis">
            <el-alert 
              v-if="ratingStats.overallTrend < 0"
              type="warning" 
              :title="`评分下降 ${Math.abs(ratingStats.overallTrend)}%，建议优化商品质量或服务`"
              show-icon
            />
            <el-alert 
              v-else-if="ratingStats.overallTrend > 0"
              type="success" 
              :title="`评分上升 ${ratingStats.overallTrend}%，继续保持`"
              show-icon
            />
            <el-alert 
              v-else
              type="info" 
              title="评分保持稳定"
              show-icon
            />
          </div>
        </div>
      </div>
      
      <!-- 评价列表 -->
      <div class="reviews-section">
        <div class="reviews-header">
          <h4>评价列表</h4>
          <el-select v-model="reviewFilter" @change="filterReviews" placeholder="筛选评价" style="width: 150px">
            <el-option label="全部评价" value="all" />
            <el-option label="5星好评" value="5" />
            <el-option label="4星好评" value="4" />
            <el-option label="3星中评" value="3" />
            <el-option label="2星差评" value="2" />
            <el-option label="1星差评" value="1" />
          </el-select>
        </div>
        
        <el-table :data="filteredReviews" v-loading="reviewsLoading" class="reviews-table">
          <el-table-column prop="customer" label="客户" width="100" />
          <el-table-column prop="rating" label="评分" width="120">
            <template #default="{ row }">
              <el-rate v-model="row.rating" disabled show-score />
            </template>
          </el-table-column>
          <el-table-column prop="comment" label="评价内容" min-width="300">
            <template #default="{ row }">
              <div class="comment-content">
                <p>{{ row.comment }}</p>
                <div v-if="row.images && row.images.length" class="comment-images">
                  <el-image
                    v-for="(img, index) in row.images"
                    :key="index"
                    :src="img"
                    :preview-src-list="row.images"
                    class="comment-image"
                    fit="cover"
                  />
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="评价时间" width="150" />
          <el-table-column label="商家回复" width="200">
            <template #default="{ row }">
              <div v-if="row.reply" class="merchant-reply">
                <p class="reply-content">{{ row.reply }}</p>
                <span class="reply-time">{{ row.replyTime }}</span>
              </div>
              <el-button v-else type="text" @click="openReplyDialog(row)">
                回复评价
              </el-button>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="text" @click="openReplyDialog(row)">
                {{ row.reply ? '编辑回复' : '回复' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
    
    <!-- 地图定位对话框 -->
    <el-dialog v-model="mapDialogVisible" title="地图定位" width="80%" top="5vh">
      <div class="map-container">
        <div class="map-placeholder">
          <el-icon><Location /></el-icon>
          <p>地图组件开发中...</p>
          <p>当前地址：{{ shopInfo.address }}</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="mapDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmLocation">确认位置</el-button>
      </template>
    </el-dialog>
    
    <!-- 回复评价对话框 -->
    <el-dialog v-model="replyDialogVisible" title="回复评价" width="600px">
      <div class="reply-dialog">
        <div class="original-review">
          <h4>原评价内容</h4>
          <div class="review-content">
            <el-rate v-model="currentReview.rating" disabled show-score />
            <p>{{ currentReview.comment }}</p>
            <span class="review-time">{{ currentReview.createTime }}</span>
          </div>
        </div>
        
        <el-form :model="replyForm" label-width="80px">
          <el-form-item label="回复内容">
            <el-input 
              v-model="replyForm.content" 
              type="textarea" 
              :rows="4"
              placeholder="请输入回复内容，如：感谢您的好评，我们会继续努力！"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply" :loading="replySaving">
          提交回复
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, 
  Location, 
  CircleCheck, 
  Clock, 
  CircleClose, 
  InfoFilled, 
  Refresh 
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import ImageUpload from '@/components/ImageUpload.vue'
import { shopApi } from '@/api/shop'

// 表单引用
const shopForm = ref(null)

// 加载状态
const saving = ref(false)
const statusSaving = ref(false)
const reviewsLoading = ref(false)
const replySaving = ref(false)
const loading = ref(false)

// 当前商家ID（从用户信息中获取）
const currentMerchantId = ref(1) // 这里应该从登录用户信息中获取


// 对话框状态
const mapDialogVisible = ref(false)
const replyDialogVisible = ref(false)

// 店铺基础信息
const shopInfo = reactive({
  name: '',
  phone: '',
  logo: '',
  description: '',
  businessHours: {
    weekday: ['09:00', '22:00'],
    weekend: ['10:00', '23:00']
  },
  address: ''
})

// 店铺状态表单
const statusForm = reactive({
  status: 1, // 1-营业中 2-休息中 3-暂停营业
  announcement: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' },
    { min: 2, max: 20, message: '店铺名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入店铺地址', trigger: 'blur' },
    { min: 10, max: 100, message: '地址长度在 10 到 100 个字符', trigger: 'blur' }
  ]
}

// 评分统计数据
const ratingStats = reactive({
  overall: 4.8,
  quality: 4.9,
  delivery: 4.6,
  service: 4.7,
  qualityTrend: 2.5,
  deliveryTrend: -1.2,
  serviceTrend: 0.8,
  overallTrend: 1.5
})

// 评价列表
const reviews = ref([
  {
    id: 1,
    customer: '张先生',
    rating: 5,
    comment: '奶茶很好喝，配送也很快！服务态度很好，推荐！',
    images: ['https://via.placeholder.com/100x100', 'https://via.placeholder.com/100x100'],
    createTime: '2024-01-15 10:30',
    reply: '感谢您的好评，我们会继续努力！',
    replyTime: '2024-01-15 11:00'
  },
  {
    id: 2,
    customer: '李女士',
    rating: 4,
    comment: '味道不错，就是有点甜了，下次可以少糖',
    images: [],
    createTime: '2024-01-14 15:20',
    reply: '',
    replyTime: ''
  },
  {
    id: 3,
    customer: '王先生',
    rating: 5,
    comment: '服务态度很好，奶茶口感很棒，会继续光顾的！',
    images: ['https://via.placeholder.com/100x100'],
    createTime: '2024-01-13 18:45',
    reply: '谢谢您的支持，期待您的再次光临！',
    replyTime: '2024-01-13 19:00'
  },
  {
    id: 4,
    customer: '赵女士',
    rating: 3,
    comment: '配送有点慢，不过味道还可以',
    images: [],
    createTime: '2024-01-12 12:30',
    reply: '',
    replyTime: ''
  },
  {
    id: 5,
    customer: '陈先生',
    rating: 5,
    comment: '非常满意，包装精美，味道正宗！',
    images: ['https://via.placeholder.com/100x100', 'https://via.placeholder.com/100x100', 'https://via.placeholder.com/100x100'],
    createTime: '2024-01-11 16:45',
    reply: '感谢您的认可，我们会保持品质！',
    replyTime: '2024-01-11 17:00'
  }
])

// 评价筛选
const reviewFilter = ref('all')
const filteredReviews = computed(() => {
  if (reviewFilter.value === 'all') {
    return reviews.value
  }
  return reviews.value.filter(review => review.rating === parseInt(reviewFilter.value))
})

// 当前评价和回复表单
const currentReview = ref({})
const replyForm = reactive({
  content: ''
})

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    1: '营业中',
    2: '休息中',
    3: '暂停营业'
  }
  return statusMap[status] || '未知状态'
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    1: 'success',
    2: 'warning',
    3: 'danger'
  }
  return typeMap[status] || 'info'
}


// 保存店铺信息
const saveShopInfo = async () => {
  if (!shopForm.value) return
  
  try {
    await shopForm.value.validate()
    saving.value = true
    
    const data = {
      shopName: shopInfo.name,
      shopLogo: shopInfo.logo,
      description: shopInfo.description,
      contactPhone: shopInfo.phone,
      address: shopInfo.address,
      weekday: {
        openTime: shopInfo.businessHours.weekday[0],
        closeTime: shopInfo.businessHours.weekday[1]
      },
      weekend: {
        openTime: shopInfo.businessHours.weekend[0],
        closeTime: shopInfo.businessHours.weekend[1]
      }
    }
    
    const response = await shopApi.updateShopInfo(data)
    
    if (response.code === 200) {
      ElMessage.success('店铺信息保存成功')
      await loadShopData() // 重新加载数据
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请检查表单信息')
  } finally {
    saving.value = false
  }
}

// 保存状态设置
const saveStatus = async () => {
  statusSaving.value = true
  try {
    const data = {
      businessStatus: statusForm.status,
      announcement: statusForm.announcement
    }
    
    const response = await shopApi.updateShopStatus(data)
    
    if (response.code === 200) {
      ElMessage.success('状态设置保存成功')
      await loadShopData() // 重新加载数据
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    statusSaving.value = false
  }
}

// 重置表单
const resetForm = () => {
  ElMessageBox.confirm('确定要重置表单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 重置表单数据
    Object.assign(shopInfo, {
      name: '甜蜜时光奶茶店',
      phone: '13800138001',
      logo: '',
      description: '专注手工奶茶10年，为您提供最纯正的口感体验',
      businessHours: {
        weekday: ['09:00', '22:00'],
        weekend: ['10:00', '23:00']
      },
      address: '北京市朝阳区三里屯街道123号'
    })
    ElMessage.success('表单已重置')
  }).catch(() => {
    ElMessage.info('已取消重置')
  })
}

// 打开地图对话框
const openMapDialog = () => {
  mapDialogVisible.value = true
}

// 确认位置
const confirmLocation = () => {
  ElMessage.success('位置已确认')
  mapDialogVisible.value = false
}

// 筛选评价
const filterReviews = () => {
  // 筛选逻辑已在 computed 中处理
}

// 加载店铺数据
const loadShopData = async () => {
  loading.value = true
  try {
    // 从后端获取店铺基础信息
    const response = await shopApi.getShopInfo()
    
    if (response.code === 200 && response.data) {
      const shopData = response.data
      
      // 填充基础信息
      shopInfo.name = shopData.shopName || ''
      shopInfo.phone = shopData.contactPhone || ''
      shopInfo.logo = shopData.shopLogo || ''
      shopInfo.description = shopData.description || ''
      shopInfo.address = shopData.address || ''
      
      // 填充营业时间
      if (shopData.weekdayHours && shopData.weekdayHours.length === 2) {
        shopInfo.businessHours.weekday = shopData.weekdayHours
      }
      if (shopData.weekendHours && shopData.weekendHours.length === 2) {
        shopInfo.businessHours.weekend = shopData.weekendHours
      }
      
      // 填充营业状态
      if (shopData.businessStatus) {
        statusForm.status = shopData.businessStatus
      }
      
      // 填充公告
      if (shopData.announcement) {
        statusForm.announcement = shopData.announcement
      }
    }
    
    // 加载评分统计
    await loadRatingStats()
    
    // 加载评价列表
    await loadReviews()
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载店铺信息失败，请重试')
  } finally {
    loading.value = false
  }
}

// 加载评分统计
const loadRatingStats = async () => {
  try {
    const response = await shopApi.getRatingStats()
    if (response.code === 200 && response.data) {
      ratingStats.overall = parseFloat(response.data.overall) || 0
      ratingStats.quality = parseFloat(response.data.quality) || 0
      ratingStats.delivery = parseFloat(response.data.delivery) || 0
      ratingStats.service = parseFloat(response.data.service) || 0
    }
  } catch (error) {
    console.error('加载评分统计失败:', error)
  }
}

// 加载评价列表
const loadReviews = async () => {
  reviewsLoading.value = true
  try {
    const response = await shopApi.getReviewList(reviewFilter.value === 'all' ? null : parseInt(reviewFilter.value))
    if (response.code === 200 && response.data) {
      // 转换后端数据格式到前端格式
      reviews.value = response.data.map(item => ({
        id: item.id,
        customer: item.userName,
        rating: item.overallRating,
        comment: item.comment,
        images: item.images ? JSON.parse(item.images) : [],
        createTime: item.createdAt,
        reply: item.merchantReply,
        replyTime: item.replyTime
      }))
    }
  } catch (error) {
    console.error('加载评价列表失败:', error)
  } finally {
    reviewsLoading.value = false
  }
}

// 刷新评分数据
const refreshRatings = async () => {
  await loadRatingStats()
  await loadReviews()
  ElMessage.success('数据已刷新')
}

// 打开回复对话框
const openReplyDialog = (review) => {
  currentReview.value = review
  replyForm.content = review.reply || ''
  replyDialogVisible.value = true
}

// 提交回复
const submitReply = async () => {
  if (!replyForm.content.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  replySaving.value = true
  try {
    const data = {
      reviewId: currentReview.value.id,
      reply: replyForm.content
    }
    
    const response = await shopApi.replyReview(data)
    
    if (response.code === 200) {
      ElMessage.success('回复提交成功')
      replyDialogVisible.value = false
      replyForm.content = ''
      
      // 重新加载评价列表
      await loadReviews()
    } else {
      ElMessage.error(response.message || '回复提交失败')
    }
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error('回复提交失败，请重试')
  } finally {
    replySaving.value = false
  }
}

// 初始化
onMounted(async () => {
  // 加载店铺数据
  await loadShopData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.page-description {
  margin: 0;
  color: #666;
  font-size: 14px;
}

/* 卡片样式 */
.info-card, .status-card, .rating-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 表单样式 */
.shop-form, .status-form {
  max-width: 800px;
}

.form-tip {
  margin-left: 10px;
  color: #666;
  font-size: 14px;
}


/* 营业时间样式 */
.business-hours {
  display: flex;
  gap: 30px;
  flex-wrap: wrap;
}

.hours-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.hours-label {
  font-weight: 500;
  color: #333;
  min-width: 60px;
}

/* 地址输入样式 */
.address-input {
  display: flex;
  gap: 10px;
  align-items: center;
}

.map-btn {
  white-space: nowrap;
}

/* 状态表单样式 */
.status-form {
  .status-tip {
    margin-top: 8px;
    color: #666;
    font-size: 12px;
    display: flex;
    align-items: center;
    gap: 4px;
  }
  
  .announcement-tip {
    margin-top: 8px;
    color: #666;
    font-size: 12px;
  }
}

/* 评分概览样式 */
.rating-overview {
  display: grid;
  grid-template-columns: 1fr 2fr 1fr;
  gap: 30px;
  margin-bottom: 30px;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 8px;
}

.overall-score {
  text-align: center;
}

.score-display {
  .score-number {
    font-size: 48px;
    font-weight: bold;
    color: #409eff;
    display: block;
    margin-bottom: 10px;
  }
  
  .score-text {
    margin: 10px 0 0 0;
    color: #666;
    font-size: 14px;
  }
}

.rating-breakdown {
  .rating-item {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
    padding: 10px;
    background: rgba(255, 255, 255, 0.7);
    border-radius: 6px;
    
    .rating-label {
      width: 80px;
      font-weight: 500;
      color: #333;
    }
    
    .rating-trend {
      margin-left: auto;
      font-size: 12px;
      font-weight: bold;
      
      &.positive {
        color: #67c23a;
      }
      
      &.negative {
        color: #f56c6c;
      }
    }
  }
}

.rating-trend-chart {
  h4 {
    margin: 0 0 15px 0;
    color: #333;
    font-size: 16px;
  }
}

/* 评价列表样式 */
.reviews-section {
  margin-top: 30px;
}

.reviews-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  h4 {
    margin: 0;
    color: #333;
    font-size: 18px;
  }
}

.reviews-table {
  .comment-content {
    p {
      margin: 0 0 8px 0;
      line-height: 1.5;
    }
  }
  
  .comment-images {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }
  
  .comment-image {
    width: 60px;
    height: 60px;
    border-radius: 4px;
    cursor: pointer;
  }
  
  .merchant-reply {
    .reply-content {
      margin: 0 0 5px 0;
      color: #333;
      font-size: 14px;
      line-height: 1.4;
    }
    
    .reply-time {
      color: #999;
      font-size: 12px;
    }
  }
}

/* 对话框样式 */
.map-container {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 8px;
}

.map-placeholder {
  text-align: center;
  color: #666;
  
  .el-icon {
    font-size: 48px;
    margin-bottom: 16px;
    color: #409eff;
  }
  
  p {
    margin: 8px 0;
    font-size: 14px;
  }
}

.reply-dialog {
  .original-review {
    margin-bottom: 20px;
    padding: 15px;
    background: #f8f9fa;
    border-radius: 6px;
    
    h4 {
      margin: 0 0 10px 0;
      color: #333;
      font-size: 14px;
    }
  }
  
  .review-content {
    p {
      margin: 8px 0;
      color: #666;
      line-height: 1.5;
    }
    
    .review-time {
      color: #999;
      font-size: 12px;
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-container {
    padding: 10px;
  }
  
  .rating-overview {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .business-hours {
    flex-direction: column;
    gap: 15px;
  }
  
  .address-input {
    flex-direction: column;
    align-items: stretch;
  }
  
  .reviews-header {
    flex-direction: column;
    gap: 10px;
  }
}

/* 动画效果 */
.el-card {
  transition: all 0.3s ease;
}

.el-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.el-button {
  transition: all 0.3s ease;
}

/* 加载状态 */
.el-loading-mask {
  border-radius: 8px;
}
</style>
