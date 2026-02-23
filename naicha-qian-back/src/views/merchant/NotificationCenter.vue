<template>
  <div class="notification-center">
    <div class="page-header">
      <h2>消息通知中心</h2>
      <div class="header-actions">
        <el-button type="primary" :icon="Bell" @click="showNotificationSettings = true">
          通知设置
        </el-button>
        <el-button :icon="Refresh" @click="refreshNotifications">
          刷新
        </el-button>
      </div>
    </div>

    <!-- 通知统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon unread">
              <el-icon><Bell /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ unreadCount }}</div>
              <div class="stat-label">未读消息</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon order">
              <el-icon><ShoppingCart /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ orderCount }}</div>
              <div class="stat-label">新订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon warning">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ warningCount }}</div>
              <div class="stat-label">库存预警</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon><Message /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ totalCount }}</div>
              <div class="stat-label">总通知数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 通知筛选 -->
    <el-card class="filter-card">
      <div class="filter-content">
        <div class="filter-left">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="全部通知" name="all">
              <template #label>
                <span>全部通知 
                  <el-badge :value="totalCount" :hidden="totalCount === 0" />
                </span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="新订单" name="new_order">
              <template #label>
                <span>新订单 
                  <el-badge :value="getCountByType('new_order')" :hidden="getCountByType('new_order') === 0" />
                </span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="订单取消" name="order_cancel">
              <template #label>
                <span>订单取消 
                  <el-badge :value="getCountByType('order_cancel')" :hidden="getCountByType('order_cancel') === 0" />
                </span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="售后申请" name="after_sale">
              <template #label>
                <span>售后申请 
                  <el-badge :value="getCountByType('after_sale')" :hidden="getCountByType('after_sale') === 0" />
                </span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="库存预警" name="stock_warning">
              <template #label>
                <span>库存预警 
                  <el-badge :value="getCountByType('stock_warning')" :hidden="getCountByType('stock_warning') === 0" />
                </span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="结算通知" name="settlement">
              <template #label>
                <span>结算通知 
                  <el-badge :value="getCountByType('settlement')" :hidden="getCountByType('settlement') === 0" />
                </span>
              </template>
            </el-tab-pane>
          </el-tabs>
        </div>
        
        <div class="filter-right">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索通知内容..."
            :prefix-icon="Search"
            @input="handleSearch"
            clearable
            style="width: 300px;"
          />
          <el-select v-model="priorityFilter" @change="handleFilter" placeholder="优先级" style="width: 120px;">
            <el-option label="全部" value="" />
            <el-option label="紧急" value="urgent" />
            <el-option label="重要" value="important" />
            <el-option label="普通" value="normal" />
          </el-select>
          <el-button :icon="MarkRead" @click="markAllRead" :disabled="unreadCount === 0">
            全部已读
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 通知列表 -->
    <el-card class="notification-list-card">
      <div class="notification-list">
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>
        
        <div v-else-if="filteredNotifications.length === 0" class="empty-state">
          <el-empty description="暂无通知" />
        </div>

        <div v-else class="notification-items">
          <div 
            v-for="notification in filteredNotifications" 
            :key="notification.id"
            class="notification-item"
            :class="{ 
              'unread': !notification.isRead, 
              'urgent': notification.priority === 'urgent',
              'important': notification.priority === 'important'
            }"
            @click="handleNotificationClick(notification)"
          >
            <div class="notification-icon">
              <el-icon :class="getNotificationIconClass(notification.type)">
                <component :is="getNotificationIcon(notification.type)" />
              </el-icon>
            </div>

            <div class="notification-content">
              <div class="notification-header">
                <h4 class="notification-title">{{ notification.title }}</h4>
                <div class="notification-meta">
                  <el-tag 
                    :type="getPriorityTagType(notification.priority)" 
                    size="small"
                  >
                    {{ getPriorityText(notification.priority) }}
                  </el-tag>
                  <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
                </div>
              </div>
              
              <p class="notification-text">{{ notification.content }}</p>
              
              <div v-if="notification.actions && notification.actions.length > 0" class="notification-actions">
                <el-button 
                  v-for="action in notification.actions" 
                  :key="action.id"
                  :type="action.type || 'primary'"
                  size="small"
                  @click.stop="handleNotificationAction(notification, action)"
                >
                  {{ action.text }}
                </el-button>
              </div>
            </div>

            <div class="notification-status">
              <el-icon v-if="!notification.isRead" class="unread-dot">
                <CircleFilled />
              </el-icon>
              <el-button 
                size="small"
                :type="notification.isRead ? 'info' : 'primary'"
                @click.stop="toggleRead(notification)"
              >
                {{ notification.isRead ? '标记未读' : '标记已读' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalNotifications"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 通知设置对话框 -->
    <el-dialog
      v-model="showNotificationSettings"
      title="通知设置"
      width="600px"
    >
      <div class="notification-settings">
        <el-form label-width="150px">
          <el-form-item label="接收通知类型">
            <el-checkbox-group v-model="notificationTypes">
              <el-checkbox label="new_order">新订单</el-checkbox>
              <el-checkbox label="order_cancel">订单取消</el-checkbox>
              <el-checkbox label="after_sale">售后申请</el-checkbox>
              <el-checkbox label="stock_warning">库存预警</el-checkbox>
              <el-checkbox label="settlement">结算通知</el-checkbox>
            </el-checkbox-group>
          </el-form-item>

          <el-form-item label="通知方式">
            <el-checkbox-group v-model="notificationMethods">
              <el-checkbox label="popup">弹窗提醒</el-checkbox>
              <el-checkbox label="sound">声音提醒</el-checkbox>
              <el-checkbox label="sms">短信通知</el-checkbox>
            </el-checkbox-group>
          </el-form-item>

          <el-form-item label="声音提醒">
            <el-switch v-model="soundEnabled" />
            <span style="margin-left: 8px; color: #666;">开启声音提醒</span>
          </el-form-item>

          <el-form-item label="免打扰时间">
            <el-time-picker
              v-model="quietStartTime"
              placeholder="开始时间"
              format="HH:mm"
              value-format="HH:mm"
            />
            <span style="margin: 0 8px;">至</span>
            <el-time-picker
              v-model="quietEndTime"
              placeholder="结束时间"
              format="HH:mm"
              value-format="HH:mm"
            />
          </el-form-item>

          <el-form-item label="大额订单提醒">
            <el-input-number 
              v-model="largeOrderThreshold" 
              :min="100" 
              :max="10000" 
              :step="100"
              placeholder="订单金额阈值"
            />
            <span style="margin-left: 8px; color: #666;">超过此金额的订单会发送短信提醒</span>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="showNotificationSettings = false">取消</el-button>
        <el-button type="primary" @click="saveNotificationSettings">保存设置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Bell, 
  Refresh, 
  Search, 
  MarkRead, 
  CircleFilled,
  Warning,
  ShoppingCart,
  Message,
  User,
  Tools,
  Money,
  InfoFilled
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const activeTab = ref('all')
const searchKeyword = ref('')
const priorityFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const showNotificationSettings = ref(false)

// 通知设置
const notificationTypes = ref(['new_order', 'order_cancel', 'after_sale', 'stock_warning', 'settlement'])
const notificationMethods = ref(['popup', 'sound'])
const soundEnabled = ref(true)
const quietStartTime = ref('22:00')
const quietEndTime = ref('08:00')
const largeOrderThreshold = ref(1000)

// 通知数据
const notifications = ref([
  {
    id: 1,
    type: 'new_order',
    title: '新订单提醒',
    content: '您有新的订单 #20231201001，订单金额：¥128.00，请及时处理。',
    priority: 'important',
    isRead: false,
    createdAt: '2023-12-01 14:30:00',
    actions: [
      { id: 1, text: '查看订单', type: 'primary' },
      { id: 2, text: '接单', type: 'success' }
    ]
  },
  {
    id: 2,
    type: 'order_cancel',
    title: '订单取消申请',
    content: '用户申请取消订单 #20231201002，请及时处理。',
    priority: 'urgent',
    isRead: false,
    createdAt: '2023-12-01 13:45:00',
    actions: [
      { id: 1, text: '查看申请', type: 'primary' },
      { id: 2, text: '同意取消', type: 'success' },
      { id: 3, text: '拒绝取消', type: 'warning' }
    ]
  },
  {
    id: 3,
    type: 'after_sale',
    title: '售后申请',
    content: '用户对订单 #20231201003 申请售后，问题描述：商品质量问题。',
    priority: 'important',
    isRead: true,
    createdAt: '2023-12-01 10:20:00',
    actions: [
      { id: 1, text: '查看申请', type: 'primary' },
      { id: 2, text: '处理售后', type: 'success' }
    ]
  },
  {
    id: 4,
    type: 'stock_warning',
    title: '库存预警',
    content: '商品"珍珠奶茶"库存不足，当前库存：5件，建议及时补货。',
    priority: 'urgent',
    isRead: false,
    createdAt: '2023-12-01 09:15:00',
    actions: [
      { id: 1, text: '查看库存', type: 'primary' },
      { id: 2, text: '立即补货', type: 'warning' }
    ]
  },
  {
    id: 5,
    type: 'settlement',
    title: '结算单生成',
    content: '您的11月结算单已生成，结算金额：¥12,580.00，请查看详情。',
    priority: 'normal',
    isRead: true,
    createdAt: '2023-12-01 08:00:00',
    actions: [
      { id: 1, text: '查看结算单', type: 'primary' }
    ]
  },
  {
    id: 6,
    type: 'new_order',
    title: '大额订单提醒',
    content: '您有新的订单 #20231201004，订单金额：¥2,580.00，请注意查收。',
    priority: 'urgent',
    isRead: false,
    createdAt: '2023-12-01 15:30:00',
    actions: [
      { id: 1, text: '查看订单', type: 'primary' },
      { id: 2, text: '接单', type: 'success' }
    ]
  }
])

// 计算属性
const totalCount = computed(() => notifications.value.length)
const unreadCount = computed(() => notifications.value.filter(n => !n.isRead).length)
const orderCount = computed(() => notifications.value.filter(n => n.type === 'new_order' && !n.isRead).length)
const warningCount = computed(() => notifications.value.filter(n => n.type === 'stock_warning' && !n.isRead).length)

const filteredNotifications = computed(() => {
  let filtered = notifications.value

  // 按类型筛选
  if (activeTab.value !== 'all') {
    filtered = filtered.filter(n => n.type === activeTab.value)
  }

  // 按优先级筛选
  if (priorityFilter.value) {
    filtered = filtered.filter(n => n.priority === priorityFilter.value)
  }

  // 搜索筛选
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(n => 
      n.title.toLowerCase().includes(keyword) || 
      n.content.toLowerCase().includes(keyword)
    )
  }

  // 排序
  filtered = [...filtered].sort((a, b) => {
    // 未读优先
    if (a.isRead !== b.isRead) {
      return a.isRead ? 1 : -1
    }
    // 紧急程度排序
    const priorityOrder = { urgent: 3, important: 2, normal: 1 }
    if (priorityOrder[a.priority] !== priorityOrder[b.priority]) {
      return priorityOrder[b.priority] - priorityOrder[a.priority]
    }
    // 时间排序
    return new Date(b.createdAt) - new Date(a.createdAt)
  })

  return filtered
})

const totalNotifications = computed(() => filteredNotifications.value.length)

// 方法
const getCountByType = (type) => {
  return notifications.value.filter(n => n.type === type).length
}

const getNotificationIcon = (type) => {
  const iconMap = {
    new_order: ShoppingCart,
    order_cancel: User,
    after_sale: Tools,
    stock_warning: Warning,
    settlement: Money
  }
  return iconMap[type] || InfoFilled
}

const getNotificationIconClass = (type) => {
  const classMap = {
    new_order: 'order-icon',
    order_cancel: 'cancel-icon',
    after_sale: 'after-sale-icon',
    stock_warning: 'warning-icon',
    settlement: 'settlement-icon'
  }
  return classMap[type] || ''
}

const getPriorityText = (priority) => {
  const textMap = {
    urgent: '紧急',
    important: '重要',
    normal: '普通'
  }
  return textMap[priority] || '普通'
}

const getPriorityTagType = (priority) => {
  const tagMap = {
    urgent: 'danger',
    important: 'warning',
    normal: 'info'
  }
  return tagMap[priority] || 'info'
}

const formatTime = (timeStr) => {
  const time = new Date(timeStr)
  const now = new Date()
  const diff = now - time
  
  if (diff < 60000) { // 1分钟内
    return '刚刚'
  } else if (diff < 3600000) { // 1小时内
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) { // 1天内
    return `${Math.floor(diff / 3600000)}小时前`
  } else {
    return time.toLocaleDateString() + ' ' + time.toLocaleTimeString().slice(0, 5)
  }
}

const handleTabChange = (tab) => {
  currentPage.value = 1
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleFilter = () => {
  currentPage.value = 1
}

const handleNotificationClick = (notification) => {
  if (!notification.isRead) {
    notification.isRead = true
  }
}

const handleNotificationAction = (notification, action) => {
  ElMessage.success(`执行操作: ${action.text}`)
  // 这里可以根据action.id执行不同的操作
}

const toggleRead = (notification) => {
  notification.isRead = !notification.isRead
  ElMessage.success(notification.isRead ? '已标记为已读' : '已标记为未读')
}

const markAllRead = () => {
  notifications.value.forEach(notification => {
    notification.isRead = true
  })
  ElMessage.success('已标记全部为已读')
}

const refreshNotifications = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('刷新成功')
  }, 1000)
}

const saveNotificationSettings = () => {
  ElMessage.success('通知设置已保存')
  showNotificationSettings.value = false
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

onMounted(() => {
  // 初始化数据
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 1000)
})
</script>

<style scoped>
.notification-center {
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
  color: #333;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
}

.stat-icon.unread {
  background: #409eff;
}

.stat-icon.order {
  background: #67c23a;
}

.stat-icon.warning {
  background: #e6a23c;
}

.stat-icon.total {
  background: #909399;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
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

.filter-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.notification-list-card {
  min-height: 400px;
}

.notification-list {
  min-height: 300px;
}

.loading-container {
  padding: 20px;
}

.empty-state {
  padding: 40px 20px;
  text-align: center;
}

.notification-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;
}

.notification-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.notification-item.unread {
  background: #f0f9ff;
  border-color: #409eff;
}

.notification-item.urgent {
  border-left: 4px solid #f56c6c;
}

.notification-item.important {
  border-left: 4px solid #e6a23c;
}

.notification-icon {
  margin-right: 16px;
  margin-top: 4px;
}

.notification-icon .el-icon {
  font-size: 20px;
}

.order-icon {
  color: #67c23a;
}

.cancel-icon {
  color: #f56c6c;
}

.after-sale-icon {
  color: #409eff;
}

.warning-icon {
  color: #e6a23c;
}

.settlement-icon {
  color: #67c23a;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.notification-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
}

.notification-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.notification-time {
  font-size: 12px;
  color: #999;
}

.notification-text {
  margin: 0 0 12px;
  color: #666;
  line-height: 1.5;
  word-break: break-word;
}

.notification-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.notification-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin-left: 12px;
}

.unread-dot {
  color: #f56c6c;
  font-size: 12px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.notification-settings {
  max-height: 400px;
  overflow-y: auto;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .filter-content {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-right {
    justify-content: space-between;
  }

  .notification-item {
    flex-direction: column;
    align-items: stretch;
  }

  .notification-icon {
    margin-right: 0;
    margin-bottom: 8px;
  }

  .notification-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .notification-status {
    margin-left: 0;
    margin-top: 12px;
    flex-direction: row;
    justify-content: space-between;
  }
}
</style>
