<template>
  <div class="message-center">
    <div class="container">
      <el-card class="message-header">
        <div class="header-content">
          <h2>我的消息</h2>
          <div class="header-actions">
            <el-button 
              type="primary" 
              :icon="CircleCheck" 
              @click="markAllRead"
              :disabled="unreadCount === 0"
            >
              全部标记已读
            </el-button>
            <el-button 
              :icon="Delete" 
              @click="showDeleteDialog = true"
              :disabled="selectedMessages.length === 0"
            >
              删除选中
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 消息筛选 -->
      <el-card class="filter-card">
        <div class="filter-content">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="全部消息" name="all">
              <template #label>
                <span>全部消息 
                  <el-badge :value="totalCount" :hidden="totalCount === 0" />
                </span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="订单通知" name="order">
              <template #label>
                <span>订单通知 
                  <el-badge :value="getUnreadCountByType('order')" :hidden="getUnreadCountByType('order') === 0" />
                </span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="营销通知" name="marketing">
              <template #label>
                <span>营销通知 
                  <el-badge :value="getUnreadCountByType('marketing')" :hidden="getUnreadCountByType('marketing') === 0" />
                </span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="系统通知" name="system">
              <template #label>
                <span>系统通知 
                  <el-badge :value="getUnreadCountByType('system')" :hidden="getUnreadCountByType('system') === 0" />
                </span>
              </template>
            </el-tab-pane>
          </el-tabs>

          <div class="filter-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索消息内容..."
              :prefix-icon="Search"
              @input="handleSearch"
              clearable
              style="width: 300px;"
            />
            <el-select v-model="sortBy" @change="handleSort" placeholder="排序方式" style="width: 120px;">
              <el-option label="时间倒序" value="time_desc" />
              <el-option label="时间正序" value="time_asc" />
            </el-select>
          </div>
        </div>
      </el-card>

      <!-- 消息列表 -->
      <el-card class="message-list-card">
        <div class="message-list">
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="5" animated />
          </div>
          
          <div v-else-if="filteredMessages.length === 0" class="empty-state">
            <el-empty description="暂无消息" />
          </div>

          <div v-else class="message-items">
            <div 
              v-for="message in filteredMessages" 
              :key="message.id"
              class="message-item"
              :class="{ 'unread': !message.isRead, 'selected': selectedMessages.includes(message.id) }"
              @click="handleMessageClick(message)"
            >
              <div class="message-checkbox">
                <el-checkbox 
                  :model-value="selectedMessages.includes(message.id)"
                  @change="(checked) => handleSelectMessage(message.id, checked)"
                  @click.stop
                />
              </div>
              
              <div class="message-icon">
                <el-icon :class="getMessageIconClass(message.type)">
                  <component :is="getMessageIcon(message.type)" />
                </el-icon>
              </div>

              <div class="message-content">
                <div class="message-header">
                  <h4 class="message-title">{{ message.title }}</h4>
                  <div class="message-meta">
                    <span class="message-time">{{ formatTime(message.createdAt) }}</span>
                    <el-tag 
                      :type="getMessageTypeTag(message.type)" 
                      size="small"
                    >
                      {{ getMessageTypeText(message.type) }}
                    </el-tag>
                  </div>
                </div>
                
                <p class="message-text">{{ message.content }}</p>
                
                <div v-if="message.actions && message.actions.length > 0" class="message-actions">
                  <el-button 
                    v-for="action in message.actions" 
                    :key="action.id"
                    :type="action.type || 'primary'"
                    size="small"
                    @click.stop="handleMessageAction(message, action)"
                  >
                    {{ action.text }}
                  </el-button>
                </div>
              </div>

              <div class="message-status">
                <span v-if="!message.isRead" class="unread-dot"></span>
                <el-button 
                  v-if="message.type === 'marketing'"
                  :type="message.isMuted ? 'info' : 'warning'"
                  size="small"
                  @click.stop="toggleMute(message)"
                >
                  {{ message.isMuted ? '取消免打扰' : '免打扰' }}
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="totalMessages > 0" class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="totalMessages"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :disabled="loading"
          />
        </div>
      </el-card>
    </div>

    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="showDeleteDialog"
      title="删除消息"
      width="400px"
    >
      <p>确定要删除选中的 {{ selectedMessages.length }} 条消息吗？删除后无法恢复。</p>
      <template #footer>
        <el-button @click="showDeleteDialog = false">取消</el-button>
        <el-button type="danger" @click="handleDeleteSelected">确定删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  CircleCheck, 
  Delete, 
  Search, 
  Bell,
  ShoppingCart,
  Present,
  Warning,
  InfoFilled
} from '@element-plus/icons-vue'
import { 
  getNotificationPage, 
  markAsRead, 
  markAllAsRead as markAllAsReadApi, 
  deleteNotification, 
  batchDeleteNotifications as batchDeleteNotificationsApi,
  getUnreadCount 
} from '@/api/notification'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const activeTab = ref('all')
const searchKeyword = ref('')
const sortBy = ref('time_desc')
const currentPage = ref(1)
const pageSize = ref(20)
const showDeleteDialog = ref(false)
const selectedMessages = ref([])
const totalMessages = ref(0)

// 消息数据
const messages = ref([])

// 通知类型映射（后端类型 -> 前端类型）
const mapNotificationType = (backendType) => {
  if (!backendType) return 'system'
  
  // 后端通知类型可能是：order_created, order_paid, order_shipped, order_completed, 
  // promotion_new, coupon_received, system_update等
  if (backendType.includes('order')) {
    return 'order'
  } else if (backendType.includes('promotion') || backendType.includes('coupon') || backendType.includes('marketing')) {
    return 'marketing'
  } else {
    return 'system'
  }
}

// 格式化通知数据
const formatNotification = (notification) => {
  // 处理isRead（后端是Integer 0/1，前端是Boolean）
  const isRead = notification.isRead === 1 || notification.isRead === true
  
  // 处理actions（可能是字符串JSON或数组）
  let actions = []
  if (notification.actions) {
    if (typeof notification.actions === 'string') {
      try {
        actions = JSON.parse(notification.actions)
      } catch (e) {
        console.warn('解析通知操作按钮失败:', e)
      }
    } else if (Array.isArray(notification.actions)) {
      actions = notification.actions
    }
  }
  
  // 处理时间
  const createdAt = notification.createdAt || notification.createTime || notification.created_at
  
  return {
    id: notification.id,
    type: mapNotificationType(notification.type),
    backendType: notification.type, // 保存原始类型
    title: notification.title || '',
    content: notification.content || '',
    isRead: isRead,
    isMuted: false, // 后端暂无此字段
    createdAt: createdAt,
    actions: actions.map((action, index) => ({
      id: action.id || index + 1,
      text: action.text || '',
      type: action.type || 'primary',
      action: action.action || ''
    })),
    relatedId: notification.relatedId || notification.related_id,
    relatedType: notification.relatedType || notification.related_type,
    extraData: notification.extraData || notification.extra_data
  }
}

// 计算属性
const totalCount = computed(() => messages.value.length)
const unreadCount = computed(() => messages.value.filter(m => !m.isRead).length)

const filteredMessages = computed(() => {
  let filtered = [...messages.value]

  // 按类型筛选
  if (activeTab.value !== 'all') {
    filtered = filtered.filter(m => m.type === activeTab.value)
  }

  // 搜索筛选
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(m => 
      m.title.toLowerCase().includes(keyword) || 
      m.content.toLowerCase().includes(keyword)
    )
  }

  // 排序
  filtered.sort((a, b) => {
    const timeA = new Date(a.createdAt).getTime()
    const timeB = new Date(b.createdAt).getTime()
    if (sortBy.value === 'time_desc') {
      return timeB - timeA
    } else {
      return timeA - timeB
    }
  })

  // 注意：后端已经做了分页，这里不需要再次分页
  // 但如果搜索在前端进行，则需要进行前端分页
  // 目前先返回所有过滤后的结果，分页由后端控制
  return filtered
})

// 方法
const getUnreadCountByType = (type) => {
  return messages.value.filter(m => m.type === type && !m.isRead).length
}

const getMessageIcon = (type) => {
  const iconMap = {
    order: ShoppingCart,
    marketing: Present,
    system: InfoFilled
  }
  return iconMap[type] || Bell
}

const getMessageIconClass = (type) => {
  const classMap = {
    order: 'order-icon',
    marketing: 'marketing-icon',
    system: 'system-icon'
  }
  return classMap[type] || ''
}

const getMessageTypeText = (type) => {
  const textMap = {
    order: '订单通知',
    marketing: '营销通知',
    system: '系统通知'
  }
  return textMap[type] || '未知'
}

const getMessageTypeTag = (type) => {
  const tagMap = {
    order: 'success',
    marketing: 'warning',
    system: 'info'
  }
  return tagMap[type] || 'info'
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  
  try {
    // 处理不同的时间格式（LocalDateTime可能是字符串或Date对象）
    let time
    if (typeof timeStr === 'string') {
      // 如果字符串包含T，说明是ISO格式
      if (timeStr.includes('T')) {
        time = new Date(timeStr)
      } else {
        // 否则尝试直接解析
        time = new Date(timeStr)
      }
    } else if (timeStr instanceof Date) {
      time = timeStr
    } else {
      time = new Date(timeStr)
    }
    
    // 检查时间是否有效
    if (isNaN(time.getTime())) {
      return timeStr.toString() // 如果无法解析，返回原字符串
    }
    
    const now = new Date()
    const diff = now - time
    
    if (diff < 0) {
      // 时间在未来，显示完整时间
      return time.toLocaleDateString('zh-CN') + ' ' + time.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    } else if (diff < 60000) { // 1分钟内
      return '刚刚'
    } else if (diff < 3600000) { // 1小时内
      return `${Math.floor(diff / 60000)}分钟前`
    } else if (diff < 86400000) { // 1天内
      return `${Math.floor(diff / 3600000)}小时前`
    } else if (diff < 86400000 * 7) { // 1周内
      return `${Math.floor(diff / 86400000)}天前`
    } else {
      return time.toLocaleDateString('zh-CN') + ' ' + time.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }
  } catch (error) {
    console.warn('格式化时间失败:', error, timeStr)
    return timeStr.toString()
  }
}

const handleTabChange = async (tab) => {
  currentPage.value = 1
  selectedMessages.value = []
  // 重新加载消息
  await loadMessages()
}

const handleSearch = () => {
  currentPage.value = 1
  selectedMessages.value = []
  // 搜索由前端filteredMessages处理，不需要重新加载
}

const handleSort = () => {
  currentPage.value = 1
  // 排序由前端filteredMessages处理，不需要重新加载
}

// 加载通知列表
const loadMessages = async () => {
  try {
    loading.value = true
    
    // 构建查询参数
    const queryParams = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    
    // 根据标签页设置类型筛选（映射前端类型到后端类型）
    if (activeTab.value !== 'all') {
      // 前端类型：order, marketing, system
      // 后端类型可能是：order_*, promotion_*, system_*等
      // 这里先不传type，由前端过滤
    }
    
    const response = await getNotificationPage(queryParams)
    
    // 处理分页响应格式
    const records = response?.records || response?.list || (Array.isArray(response) ? response : [])
    
    // 格式化通知数据
    messages.value = records.map(formatNotification)
    
    // 更新总数（后端分页的总数）
    totalMessages.value = response?.total || records.length
    
    // 如果有关键词搜索，更新显示的消息数（但不改变总数）
    // 搜索由computed的filteredMessages处理
  } catch (error) {
    console.error('加载消息列表失败:', error)
    // 如果是404，说明接口不存在，使用空列表
    if (error.response?.status === 404) {
      messages.value = []
      totalMessages.value = 0
      console.warn('通知接口不存在，请后端添加')
    } else {
      ElMessage.error('加载消息列表失败')
      messages.value = []
      totalMessages.value = 0
    }
  } finally {
    loading.value = false
  }
}

const handleMessageClick = async (message) => {
  if (!message.isRead) {
    try {
      await markAsRead(message.id)
      message.isRead = true
      
      // 更新未读数量
      await loadUnreadCount()
    } catch (error) {
      console.error('标记已读失败:', error)
      // 即使失败也更新本地状态
      message.isRead = true
    }
  }
  
  // 如果有操作按钮，可以根据action执行相应操作
  // 这里可以根据relatedId和relatedType跳转到相应页面
}

const handleSelectMessage = (messageId, checked) => {
  if (checked) {
    selectedMessages.value.push(messageId)
  } else {
    const index = selectedMessages.value.indexOf(messageId)
    if (index > -1) {
      selectedMessages.value.splice(index, 1)
    }
  }
}

const handleMessageAction = (message, action) => {
  // 根据action.action类型执行不同操作
  const actionType = action.action || ''
  const relatedId = message.relatedId
  const relatedType = message.relatedType
  
  if (actionType === 'view_order' || action.text === '查看订单') {
    if (relatedId) {
      router.push(`/orders?orderId=${relatedId}`)
    } else {
      router.push('/orders')
    }
  } else if (actionType === 'view_logistics' || action.text === '物流跟踪') {
    if (relatedId) {
      router.push(`/orders?orderId=${relatedId}&tab=logistics`)
    } else {
      router.push('/orders')
    }
  } else if (actionType === 'use_coupon' || action.text === '立即使用') {
    router.push('/coupons')
  } else if (actionType === 'change_password' || action.text === '修改密码') {
    router.push('/profile?tab=security')
  } else if (actionType === 'evaluate' || action.text === '评价商品') {
    if (relatedId) {
      router.push(`/orders?orderId=${relatedId}&tab=review`)
    } else {
      router.push('/orders')
    }
  } else if (actionType === 'view_benefits' || action.text === '查看权益') {
    router.push('/profile?tab=vip')
  } else {
    ElMessage.info(`执行操作: ${action.text}`)
  }
}

const toggleMute = (message) => {
  message.isMuted = !message.isMuted
  ElMessage.success(message.isMuted ? '已设置免打扰' : '已取消免打扰')
}

const markAllRead = async () => {
  try {
    await markAllAsReadApi()
    
    // 更新本地状态
    messages.value.forEach(message => {
      message.isRead = true
    })
    
    ElMessage.success('已标记全部为已读')
    
    // 更新未读数量
    await loadUnreadCount()
    
    // 重新加载消息列表
    await loadMessages()
  } catch (error) {
    console.error('标记全部已读失败:', error)
    ElMessage.error('标记已读失败，请重试')
  }
}

const handleDeleteSelected = async () => {
  if (selectedMessages.value.length === 0) {
    ElMessage.warning('请选择要删除的消息')
    return
  }
  
  try {
    if (selectedMessages.value.length === 1) {
      await deleteNotification(selectedMessages.value[0])
    } else {
      await batchDeleteNotificationsApi(selectedMessages.value)
    }
    
    // 更新本地状态
    messages.value = messages.value.filter(m => !selectedMessages.value.includes(m.id))
    selectedMessages.value = []
    showDeleteDialog.value = false
    ElMessage.success('删除成功')
    
    // 重新加载消息列表
    await loadMessages()
  } catch (error) {
    console.error('删除消息失败:', error)
    ElMessage.error('删除失败，请重试')
  }
}

// 加载未读数量
const loadUnreadCount = async () => {
  try {
    const count = await getUnreadCount()
    // 可以更新全局未读数量（如果有全局状态管理）
  } catch (error) {
    console.error('加载未读数量失败:', error)
  }
}

const handleSizeChange = async (size) => {
  pageSize.value = size
  currentPage.value = 1
  await loadMessages()
}

const handleCurrentChange = async (page) => {
  currentPage.value = page
  await loadMessages()
  
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 监听标签切换
watch(activeTab, async (newTab) => {
  currentPage.value = 1
  selectedMessages.value = []
  await loadMessages()
})

onMounted(async () => {
  // 加载消息列表
  await loadMessages()
  
  // 加载未读数量
  await loadUnreadCount()
})
</script>

<style scoped>
.message-center {
  padding: 20px 0;
  min-height: calc(100vh - 120px);
}

.message-header {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h2 {
  margin: 0;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.filter-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.message-list-card {
  min-height: 400px;
}

.message-list {
  min-height: 300px;
}

.loading-container {
  padding: 20px;
}

.empty-state {
  padding: 40px 20px;
  text-align: center;
}

.message-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;
}

.message-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.message-item.unread {
  background: #f0f9ff;
  border-color: #409eff;
}

.message-item.selected {
  border-color: #67c23a;
  background: #f0f9ff;
}

.message-checkbox {
  margin-right: 12px;
  margin-top: 4px;
}

.message-icon {
  margin-right: 16px;
  margin-top: 4px;
}

.message-icon .el-icon {
  font-size: 20px;
}

.order-icon {
  color: #67c23a;
}

.marketing-icon {
  color: #e6a23c;
}

.system-icon {
  color: #409eff;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.message-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-text {
  margin: 0 0 12px;
  color: #666;
  line-height: 1.5;
  word-break: break-word;
}

.message-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.message-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin-left: 12px;
}

.unread-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #f56c6c;
  flex-shrink: 0;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .filter-content {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-actions {
    justify-content: space-between;
  }

  .message-item {
    flex-direction: column;
    align-items: stretch;
  }

  .message-checkbox {
    margin-right: 0;
    margin-bottom: 8px;
  }

  .message-icon {
    margin-right: 0;
    margin-bottom: 8px;
  }

  .message-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .message-status {
    margin-left: 0;
    margin-top: 12px;
    flex-direction: row;
    justify-content: space-between;
  }
}
</style>
