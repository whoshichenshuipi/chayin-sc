<template>
  <div class="activities-container">
    <div class="container">
      <div class="activities-header">
        <h2>营销活动</h2>
        <p>精彩活动，不容错过</p>
      </div>

      <!-- 活动分类 -->
      <div class="activity-tabs">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="全部活动" name="all" />
          <el-tab-pane label="优惠券" name="coupon" />
          <el-tab-pane label="满减活动" name="discount" />
          <el-tab-pane label="会员专享" name="member" />
          <el-tab-pane label="限时活动" name="limited" />
        </el-tabs>
      </div>

      <!-- 活动列表 -->
      <div class="activities-list" v-loading="loading">
        <el-card
          v-for="activity in filteredActivities"
          :key="activity.id"
          class="activity-card"
          shadow="hover"
        >
          <div class="activity-content">
            <div class="activity-image">
              <SmartImage :src="activity.image" :alt="activity.title" />
              <div class="activity-badge" :class="activity.badgeType">
                {{ activity.badgeText }}
              </div>
              <div class="activity-countdown" v-if="activity.isLimited">
                <el-icon><Clock /></el-icon>
                <span>{{ activity.countdown }}</span>
              </div>
            </div>
            
            <div class="activity-info">
              <h3>{{ activity.title }}</h3>
              <p class="activity-desc">{{ activity.description }}</p>
              
              <div class="activity-details">
                <div class="detail-item">
                  <el-icon><Calendar /></el-icon>
                  <span>活动时间：{{ activity.startTime }} - {{ activity.endTime }}</span>
                </div>
                <div class="detail-item">
                  <el-icon><User /></el-icon>
                  <span>参与人数：{{ activity.participants }}人</span>
                </div>
                <div class="detail-item">
                  <el-icon><Trophy /></el-icon>
                  <span>活动类型：{{ activity.type }}</span>
                </div>
              </div>

              <div class="activity-rules">
                <h4>活动规则：</h4>
                <ul>
                  <li v-for="rule in activity.rules" :key="rule">{{ rule }}</li>
                </ul>
              </div>

              <div class="activity-actions">
                <el-button
                  type="primary"
                  size="large"
                  :disabled="!activity.isActive || activity.hasParticipated"
                  @click="participateActivity(activity)"
                >
                  {{ activity.hasParticipated ? '已参与' : (activity.isActive ? '立即参与' : '活动已结束') }}
                </el-button>
                <el-button @click="viewActivityDetail(activity)">
                  查看详情
                </el-button>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 空状态 -->
        <el-empty v-if="filteredActivities.length === 0" description="暂无活动" />
      </div>

      <!-- 我的优惠券 -->
      <div class="my-coupons" v-if="activeTab === 'coupon'">
        <el-card class="coupons-card">
          <template #header>
            <div class="card-header">
              <span>我的优惠券</span>
              <el-button type="primary" @click="showCouponDialog = true">
                领取优惠券
              </el-button>
            </div>
          </template>

          <div class="coupons-list">
            <div
              v-for="coupon in myCoupons"
              :key="coupon.id"
              class="coupon-item"
              :class="{ used: coupon.isUsed, expired: coupon.isExpired }"
            >
              <div class="coupon-info">
                <div class="coupon-value">
                  <span v-if="coupon.type === 'discount'">{{ coupon.value }}折</span>
                  <span v-else-if="coupon.type === 'cash'">¥{{ coupon.value }}</span>
                  <span v-else>¥{{ coupon.value }}</span>
                </div>
                <div class="coupon-details">
                  <h4>{{ coupon.name }}</h4>
                  <p>{{ coupon.description }}</p>
                  <div class="coupon-condition">{{ coupon.condition }}</div>
                  <div class="coupon-time">
                    <el-icon><Clock /></el-icon>
                    <span>{{ coupon.expireTime }}</span>
                  </div>
                </div>
              </div>
              <div class="coupon-status">
                <el-tag v-if="coupon.isUsed" type="success">已使用</el-tag>
                <el-tag v-else-if="coupon.isExpired" type="info">已过期</el-tag>
                <el-tag v-else type="warning">未使用</el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 分页 -->
      <div class="pagination-container" v-if="filteredActivities.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[6, 12, 24]"
          :total="totalActivities"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 优惠券领取对话框 -->
    <el-dialog
      v-model="showCouponDialog"
      title="领取优惠券"
      width="600px"
    >
      <div class="coupon-dialog">
        <div
          v-for="coupon in availableCoupons"
          :key="coupon.id"
          class="coupon-option"
          @click="receiveCoupon(coupon)"
        >
          <div class="coupon-option-info">
            <div class="coupon-option-value">
              <span v-if="coupon.type === 'discount'">{{ coupon.value }}折</span>
              <span v-else-if="coupon.type === 'cash'">¥{{ coupon.value }}</span>
              <span v-else>¥{{ coupon.value }}</span>
            </div>
            <div class="coupon-option-details">
              <h4>{{ coupon.name }}</h4>
              <p>{{ coupon.description }}</p>
              <div class="coupon-option-condition">{{ coupon.condition }}</div>
            </div>
          </div>
          <el-button type="primary">领取</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock, Calendar, User, Trophy } from '@element-plus/icons-vue'
import SmartImage from '@/components/SmartImage.vue'
import { getPromotionPage, getPromotionDetail, participatePromotion, checkUserParticipatedPromotion } from '@/api/promotion'
import { getCouponPage, getCouponDetail, receiveCoupon as receiveCouponApi, getMyCoupons } from '@/api/coupon'
import { useUserStore } from '@/stores/user'
import { getImageByIndex, getRandomImage } from '@/utils/imageLoader'

const userStore = useUserStore()

const activeTab = ref('all')
const currentPage = ref(1)
const pageSize = ref(6)
const totalActivities = ref(0)
const showCouponDialog = ref(false)
const loading = ref(false)

const activities = ref([])
const myCoupons = ref([])
const availableCoupons = ref([])

// 活动类型映射
const promotionTypeMap = {
  discount: { name: '限时折扣', category: 'limited', badge: '折扣', badgeType: 'limited' },
  full_reduce: { name: '满减活动', category: 'discount', badge: '满减', badgeType: 'discount' },
  buy_one_get_one: { name: '买一送一', category: 'discount', badge: '买赠', badgeType: 'gift' },
  second_half_price: { name: '第二件半价', category: 'discount', badge: '半价', badgeType: 'discount' }
}

// 优惠券类型映射
const couponTypeMap = {
  cash: { name: '现金券', label: '现金' },
  discount: { name: '折扣券', label: '折扣' },
  reduce: { name: '满减券', label: '满减' }
}

// 格式化活动数据
const formatPromotion = (promotion) => {
  const typeInfo = promotionTypeMap[promotion.type] || { name: promotion.type, category: 'limited', badge: '活动', badgeType: 'limited' }
  
  // 计算倒计时
  let countdown = ''
  let isLimited = false
  if (promotion.status === 'active' && promotion.endTime) {
    const endTime = new Date(promotion.endTime)
    const now = new Date()
    if (endTime > now) {
      isLimited = true
      const diff = endTime - now
      const hours = Math.floor(diff / (1000 * 60 * 60))
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
      const seconds = Math.floor((diff % (1000 * 60)) / 1000)
      countdown = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
    }
  }
  
  // 生成活动规则
  const rules = []
  if (promotion.type === 'discount' && promotion.discountRate) {
    rules.push(`限时折扣${promotion.discountRate}折`)
  }
  if (promotion.type === 'full_reduce' && promotion.fullReduceRules && promotion.fullReduceRules.length > 0) {
    promotion.fullReduceRules.forEach(rule => {
      rules.push(`满${rule.fullAmount}元减${rule.reduceAmount}元`)
    })
  }
  if (promotion.type === 'buy_one_get_one') {
    rules.push('指定商品买一送一')
    if (promotion.giftType === 'same') {
      rules.push('买A送A')
    } else if (promotion.giftType === 'different') {
      rules.push('买A送B')
    }
  }
  if (promotion.type === 'second_half_price') {
    rules.push('第二件半价')
  }
  if (promotion.participationLimit && promotion.participationLimit > 0) {
    rules.push(`每人限购${promotion.participationLimit}件`)
  }
  if (promotion.allowCoupon === false) {
    rules.push('不可与其他优惠同时使用')
  }
  if (!rules.length) {
    rules.push('详情请咨询商家')
  }
  
  return {
    id: promotion.id,
    title: promotion.name,
    description: promotion.description || promotion.name,
    type: typeInfo.name,
    category: typeInfo.category,
    image: getImageByIndex(promotion.id || 0) || getRandomImage() || '/activity-default.jpg', // 使用assets图片
    badgeText: typeInfo.badge,
    badgeType: typeInfo.badgeType,
    startTime: promotion.startTime ? new Date(promotion.startTime).toLocaleDateString('zh-CN') : '',
    endTime: promotion.endTime ? new Date(promotion.endTime).toLocaleDateString('zh-CN') : '',
    participants: promotion.participantCount || 0,
    isActive: promotion.status === 'active',
    isLimited,
    countdown,
    rules,
    hasParticipated: false, // 默认未参与，后续会检查
    raw: promotion // 保存原始数据
  }
}

// 格式化优惠券数据
const formatCoupon = (coupon, isUserCoupon = false) => {
  const typeInfo = couponTypeMap[coupon.type] || { name: coupon.type, label: coupon.type }
  
  let value = coupon.discount
  let condition = ''
  if (coupon.threshold && coupon.threshold > 0) {
    condition = `满${coupon.threshold}元可用`
  } else {
    condition = '无门槛'
  }
  
  // 判断是否过期（仅对用户优惠券）
  let isExpired = false
  let isUsed = false
  if (isUserCoupon) {
    isUsed = coupon.status === 'used'
    if (coupon.status === 'expired' || (coupon.validEndTime && new Date(coupon.validEndTime) < new Date())) {
      isExpired = true
    }
  }
  
  return {
    id: coupon.id,
    name: coupon.name,
    description: coupon.description || coupon.name,
    type: coupon.type,
    value,
    condition,
    expireTime: coupon.validEndTime ? new Date(coupon.validEndTime).toLocaleDateString('zh-CN') : '',
    isUsed,
    isExpired,
    raw: coupon
  }
}

// 筛选后的活动列表（包含分页）
const filteredActivities = computed(() => {
  let result = [...activities.value]
  
  if (activeTab.value !== 'all') {
    if (activeTab.value === 'coupon') {
      // 优惠券标签下，返回空（优惠券在单独的卡片中显示）
      totalActivities.value = 0
      return []
    } else {
      // 根据活动类型筛选
      const categoryMap = {
        discount: ['discount', 'full_reduce'],
        member: [], // 会员专享需要特殊处理
        limited: ['discount', 'buy_one_get_one', 'second_half_price']
      }
      const types = categoryMap[activeTab.value] || []
      if (types.length > 0) {
        result = result.filter(activity => {
          const rawType = activity.raw?.type || ''
          return types.includes(rawType)
        })
      }
    }
  }
  
  totalActivities.value = result.length
  
  // 分页处理
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return result.slice(start, end)
})

// 加载活动列表
const loadActivities = async () => {
  try {
    loading.value = true
    const params = {
      pageNum: 1,
      pageSize: 100 // 先加载所有数据，前端分页
    }
    
    // 根据标签加载不同状态的活动
    if (activeTab.value === 'all') {
      // 不传 status，获取所有活动
    } else if (activeTab.value !== 'coupon') {
      params.status = 'active' // 非优惠券标签，只显示进行中的活动
    }
    
    const response = await getPromotionPage(params)
    const records = response?.records || response?.list || []
    
    // 格式化活动数据
    const formattedActivities = records.map(formatPromotion)
    
    // 检查用户是否已参与每个活动（仅在有用户信息时检查）
    if (userStore.userInfo?.id) {
      await Promise.all(
        formattedActivities.map(async (activity) => {
          try {
            const hasParticipated = await checkUserParticipatedPromotion(activity.id)
            activity.hasParticipated = hasParticipated
          } catch (error) {
            console.warn(`检查活动 ${activity.id} 参与状态失败:`, error)
            // 检查失败时，默认设为未参与
            activity.hasParticipated = false
          }
        })
      )
    }
    
    activities.value = formattedActivities
    // totalActivities 在 computed 中计算
  } catch (error) {
    console.error('加载活动列表失败:', error)
    ElMessage.error('加载活动列表失败')
    activities.value = []
    totalActivities.value = 0
  } finally {
    loading.value = false
  }
}

// 加载可领取的优惠券
const loadAvailableCoupons = async () => {
  try {
    const response = await getCouponPage({
      pageNum: 1,
      pageSize: 20,
      status: 'active'
    })
    const records = response?.records || response?.list || []
    availableCoupons.value = records.map(coupon => formatCoupon(coupon, false))
  } catch (error) {
    console.error('加载可领取优惠券失败:', error)
    // 如果是404，说明接口不存在，不显示错误消息（已在API中处理）
    if (error.response?.status !== 404) {
      ElMessage.error('加载可领取优惠券失败')
    }
    availableCoupons.value = []
  }
}

// 加载我的优惠券
const loadMyCoupons = async () => {
  try {
    if (!userStore.userInfo?.id) {
      myCoupons.value = []
      return
    }
    
    const response = await getMyCoupons({
      pageNum: 1,
      pageSize: 50
    })
    const records = response?.records || response?.list || []
    myCoupons.value = records.map(coupon => formatCoupon(coupon, true))
  } catch (error) {
    console.error('加载我的优惠券失败:', error)
    // 如果是404，说明接口不存在，不显示错误消息
    if (error.response?.status !== 404) {
      ElMessage.error('加载我的优惠券失败')
    }
    myCoupons.value = []
  }
}

onMounted(() => {
  loadActivities()
  if (activeTab.value === 'coupon') {
    loadAvailableCoupons()
    loadMyCoupons()
  }
})

// 监听标签切换
watch(activeTab, (newTab) => {
  currentPage.value = 1
  if (newTab === 'coupon') {
    loadAvailableCoupons()
    loadMyCoupons()
  } else {
    loadActivities()
  }
})

const handleTabChange = (tab) => {
  activeTab.value = tab
  currentPage.value = 1
}

// 用于防止重复点击的标记
const participatingActivities = new Set()

const participateActivity = async (activity) => {
  // 如果正在参与中，直接返回（防止重复点击）
  if (participatingActivities.has(activity.id)) {
    return
  }
  
  // 如果已参与，直接返回，不调用接口
  if (activity.hasParticipated) {
    ElMessage.warning('你已参加活动，不要重复参加')
    return
  }
  
  // 如果活动未激活，返回
  if (!activity.isActive) {
    ElMessage.warning('活动已结束')
    return
  }
  
  // 标记为正在参与
  participatingActivities.add(activity.id)
  
  try {
    // 先检查用户是否已参与（双重检查，防止并发）
    const hasParticipated = await checkUserParticipatedPromotion(activity.id)
    if (hasParticipated) {
      activity.hasParticipated = true
      ElMessage.warning('你已参加活动，不要重复参加')
      return
    }
    
    // 参与活动（这个接口会检查并更新参与人数）
    await participatePromotion(activity.id)
    ElMessage.success(`已参与活动：${activity.title}`)
    
    // 标记为已参与
    activity.hasParticipated = true
    
    // 更新参与人数（不重新加载整个列表，只更新当前活动的参与人数）
    const activityIndex = activities.value.findIndex(a => a.id === activity.id)
    if (activityIndex !== -1) {
      // 从后端重新获取参与人数（或者简单+1）
      const currentParticipants = activity.participants || 0
      activities.value[activityIndex].participants = currentParticipants + 1
    }
  } catch (error) {
    console.error('参与活动失败:', error)
    // 检查是否是重复参与的错误
    if (error.message && (error.message.includes('已参与') || error.message.includes('不要重复参加'))) {
      activity.hasParticipated = true
      ElMessage.warning(error.message || '你已参加活动，不要重复参加')
      // 不更新参与人数，因为用户已经参与过了
    } else {
      ElMessage.error(error.message || '参与活动失败')
    }
  } finally {
    // 移除参与标记
    participatingActivities.delete(activity.id)
  }
}

const viewActivityDetail = async (activity) => {
  try {
    const detail = await getPromotionDetail(activity.id)
    // 这里可以打开一个详情对话框或跳转到详情页
    ElMessage.info(`活动详情：${activity.title}`)
    console.log('活动详情:', detail)
  } catch (error) {
    console.error('获取活动详情失败:', error)
    ElMessage.error('获取活动详情失败')
  }
}

const receiveCoupon = async (coupon) => {
  try {
    await receiveCouponApi(coupon.id)
    ElMessage.success(`已领取优惠券：${coupon.name}`)
    showCouponDialog.value = false
    // 重新加载我的优惠券
    await loadMyCoupons()
    // 重新加载可领取优惠券
    await loadAvailableCoupons()
  } catch (error) {
    console.error('领取优惠券失败:', error)
    // 错误消息已在 request 拦截器中显示，这里只做日志记录
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}
</script>

<style scoped>
.activities-container {
  padding: 20px 0;
  min-height: calc(100vh - 120px);
}

.activities-header {
  text-align: center;
  margin-bottom: 40px;
}

.activities-header h2 {
  color: #333;
  margin-bottom: 8px;
  font-size: 28px;
}

.activities-header p {
  color: #666;
  font-size: 16px;
}

.activity-tabs {
  margin-bottom: 30px;
}

.activities-list {
  margin-bottom: 40px;
}

.activity-card {
  margin-bottom: 20px;
}

.activity-content {
  display: flex;
  gap: 20px;
}

.activity-image {
  position: relative;
  width: 200px;
  height: 150px;
  flex-shrink: 0;
}

.activity-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.activity-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  color: white;
}

.activity-badge.new {
  background: #67c23a;
}

.activity-badge.discount {
  background: #ff6b6b;
}

.activity-badge.member {
  background: #409eff;
}

.activity-badge.limited {
  background: #e6a23c;
}

.activity-badge.gift {
  background: #f56c6c;
}

.activity-badge.points {
  background: #909399;
}

.activity-countdown {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.activity-info {
  flex: 1;
}

.activity-info h3 {
  margin-bottom: 8px;
  color: #333;
  font-size: 20px;
}

.activity-desc {
  color: #666;
  margin-bottom: 16px;
  line-height: 1.5;
}

.activity-details {
  margin-bottom: 16px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: #666;
  font-size: 14px;
}

.activity-rules {
  margin-bottom: 20px;
}

.activity-rules h4 {
  margin-bottom: 8px;
  color: #333;
  font-size: 14px;
}

.activity-rules ul {
  margin: 0;
  padding-left: 20px;
}

.activity-rules li {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 4px;
}

.activity-actions {
  display: flex;
  gap: 12px;
}

.my-coupons {
  margin-top: 40px;
}

.coupons-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.coupons-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.coupon-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  transition: all 0.3s;
}

.coupon-item:hover {
  border-color: #ff6b6b;
}

.coupon-item.used {
  opacity: 0.6;
  background-color: #f5f5f5;
}

.coupon-item.expired {
  opacity: 0.6;
  background-color: #f5f5f5;
}

.coupon-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.coupon-value {
  font-size: 24px;
  font-weight: bold;
  color: #ff6b6b;
  min-width: 80px;
}

.coupon-details h4 {
  margin-bottom: 4px;
  color: #333;
}

.coupon-details p {
  color: #666;
  font-size: 14px;
  margin-bottom: 4px;
}

.coupon-condition {
  color: #999;
  font-size: 12px;
  margin-bottom: 4px;
}

.coupon-time {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #999;
  font-size: 12px;
}

.coupon-dialog {
  max-height: 400px;
  overflow-y: auto;
}

.coupon-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.coupon-option:hover {
  border-color: #ff6b6b;
  background-color: #fff5f5;
}

.coupon-option-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.coupon-option-value {
  font-size: 20px;
  font-weight: bold;
  color: #ff6b6b;
  min-width: 60px;
}

.coupon-option-details h4 {
  margin-bottom: 4px;
  color: #333;
}

.coupon-option-details p {
  color: #666;
  font-size: 14px;
  margin-bottom: 4px;
}

.coupon-option-condition {
  color: #999;
  font-size: 12px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

@media (max-width: 768px) {
  .activity-content {
    flex-direction: column;
  }
  
  .activity-image {
    width: 100%;
    height: 200px;
  }
  
  .activity-actions {
    flex-direction: column;
  }
  
  .coupon-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .coupon-info {
    width: 100%;
  }
}
</style>
