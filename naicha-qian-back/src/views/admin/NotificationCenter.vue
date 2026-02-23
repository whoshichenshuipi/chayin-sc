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
            <div class="stat-icon urgent">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ urgentCount }}</div>
              <div class="stat-label">紧急通知</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon today">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ todayCount }}</div>
              <div class="stat-label">今日通知</div>
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
            <el-tab-pane label="商家申请" name="merchant_application">
              <template #label>
                <span>商家申请 
                  <el-badge :value="getCountByType('merchant_application')" :hidden="getCountByType('merchant_application') === 0" />
                </span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="用户申诉" name="user_complaint">
              <template #label>
                <span>用户申诉 
                  <el-badge :value="getCountByType('user_complaint')" :hidden="getCountByType('user_complaint') === 0" />
                </span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="系统故障" name="system_error">
              <template #label>
                <span>系统故障 
                  <el-badge :value="getCountByType('system_error')" :hidden="getCountByType('system_error') === 0" />
                </span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="合规检查" name="compliance_check">
              <template #label>
                <span>合规检查 
                  <el-badge :value="getCountByType('compliance_check')" :hidden="getCountByType('compliance_check') === 0" />
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
              <el-checkbox label="merchant_application">商家申请</el-checkbox>
              <el-checkbox label="user_complaint">用户申诉</el-checkbox>
              <el-checkbox label="system_error">系统故障</el-checkbox>
              <el-checkbox label="compliance_check">合规检查</el-checkbox>
            </el-checkbox-group>
          </el-form-item>

          <el-form-item label="通知方式">
            <el-checkbox-group v-model="notificationMethods">
              <el-checkbox label="popup">弹窗提醒</el-checkbox>
              <el-checkbox label="sms">短信通知</el-checkbox>
              <el-checkbox label="email">邮件通知</el-checkbox>
            </el-checkbox-group>
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

          <el-form-item label="紧急通知">
            <el-switch v-model="urgentNotificationEnabled" />
            <span style="margin-left: 8px; color: #666;">紧急通知不受免打扰时间限制</span>
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
  Calendar,
  Message,
  User,
  Shop,
  Tools,
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
const notificationTypes = ref(['merchant_application', 'user_complaint', 'system_error', 'compliance_check'])
const notificationMethods = ref(['popup', 'sms'])
const quietStartTime = ref('22:00')
const quietEndTime = ref('08:00')
const urgentNotificationEnabled = ref(true)

// 通知数据
const notifications = ref([
  {
    id: 1,
    type: 'merchant_application',
    title: '新商家入驻申请',
    content: '商家"张三奶茶店"提交了入驻申请，请及时审核。',
    priority: 'important',
    isRead: false,
    createdAt: '2023-12-01 14:30:00',
    actions: [
      { id: 1, text: '查看申请', type: 'primary' },
      { id: 2, text: '审核通过', type: 'success' },
      { id: 3, text: '拒绝申请', type: 'danger' }
    ]
  },
  {
    id: 2,
    type: 'system_error',
    title: '支付接口异常',
    content: '检测到支付接口响应异常，请立即检查支付服务状态。',
    priority: 'urgent',
    isRead: false,
    createdAt: '2023-12-01 13:45:00',
    actions: [
      { id: 1, text: '查看详情', type: 'primary' },
      { id: 2, text: '重启服务', type: 'warning' }
    ]
  },
  {
    id: 3,
    type: 'user_complaint',
    title: '用户申诉处理',
    content: '用户"李四"对订单#20231201001提出申诉，需要管理员介入处理。',
    priority: 'important',
    isRead: true,
    createdAt: '2023-12-01 10:20:00',
    actions: [
      { id: 1, text: '查看申诉', type: 'primary' },
      { id: 2, text: '联系用户', type: 'success' }
    ]
  },
  {
    id: 4,
    type: 'compliance_check',
    title: '合规检查报告',
    content: '本月合规检查已完成，发现3个需要关注的问题，请查看详细报告。',
    priority: 'normal',
    isRead: true,
    createdAt: '2023-11-30 16:00:00',
    actions: [
      { id: 1, text: '查看报告', type: 'primary' }
    ]
  },
  {
    id: 5,
    type: 'system_error',
    title: '数据备份失败',
    content: '今日数据备份任务失败，请检查备份服务状态和存储空间。',
    priority: 'urgent',
    isRead: false,
    createdAt: '2023-11-30 02:00:00',
    actions: [
      { id: 1, text: '查看日志', type: 'primary' },
      { id: 2, text: '手动备份', type: 'warning' }
    ]
  }
])

// 计算属性
const totalCount = computed(() => notifications.value.length)
const unreadCount = computed(() => notifications.value.filter(n => !n.isRead).length)
const urgentCount = computed(() => notifications.value.filter(n => n.priority === 'urgent' && !n.isRead).length)
const todayCount = computed(() => {
  const today = new Date().toDateString()
  return notifications.value.filter(n => new Date(n.createdAt).toDateString() === today).length
})

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
    merchant_application: Shop,
    user_complaint: User,
    system_error: Warning,
    compliance_check: Tools
  }
  return iconMap[type] || InfoFilled
}

const getNotificationIconClass = (type) => {
  const classMap = {
    merchant_application: 'merchant-icon',
    user_complaint: 'user-icon',
    system_error: 'error-icon',
    compliance_check: 'compliance-icon'
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

.stat-icon.urgent {
  background: #f56c6c;
}

.stat-icon.today {
  background: #67c23a;
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

.merchant-icon {
  color: #67c23a;
}

.user-icon {
  color: #409eff;
}

.error-icon {
  color: #f56c6c;
}

.compliance-icon {
  color: #e6a23c;
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
