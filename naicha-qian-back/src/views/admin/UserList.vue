<template>
  <div class="page-container">
    <div class="page-header">
      <h2>消费者管理</h2>
    </div>
    
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="搜索类型">
          <el-select v-model="searchForm.type" placeholder="选择搜索类型" style="width: 120px">
            <el-option label="用户名" value="username" />
            <el-option label="手机号" value="phone" />
            <el-option label="账号ID" value="id" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input 
            v-model="searchForm.keyword" 
            placeholder="请输入搜索关键词"
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="账号状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="正常" value="normal" />
            <el-option label="限制下单" value="limited" />
            <el-option label="冻结" value="frozen" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card>
      <el-table :data="userList" v-loading="loading" stripe>
        <el-table-column prop="id" label="账号ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="registerTime" label="注册时间" width="160" />
        <el-table-column prop="totalAmount" label="消费总额" width="100">
          <template #default="{ row }">
            <span class="amount">¥{{ row.totalAmount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderCount" label="订单数量" width="80" />
        <el-table-column prop="status" label="账号状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewDetail(row)">查看详情</el-button>
            <el-button 
              size="small" 
              type="warning" 
              @click="handleViolation(row)"
              :disabled="row.status === 'frozen' || row.status === 3"
            >
              违规处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
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
    </el-card>

    <!-- 用户详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="用户详情" width="600px">
      <div v-if="currentUser" class="user-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="账号ID">{{ currentUser.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ currentUser.nickname }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentUser.phone }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ currentUser.registerTime }}</el-descriptions-item>
          <el-descriptions-item label="最后登录">{{ currentUser.lastLoginTime }}</el-descriptions-item>
          <el-descriptions-item label="消费总额">
            <span class="amount">¥{{ currentUser.totalAmount.toFixed(2) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="订单数量">{{ currentUser.orderCount }}</el-descriptions-item>
          <el-descriptions-item label="账号状态">
            <el-tag :type="getStatusType(currentUser.status)">
              {{ getStatusText(currentUser.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="违规记录" :span="2">
            <div v-if="currentUser.violations && currentUser.violations.length > 0">
              <el-timeline>
                <el-timeline-item 
                  v-for="violation in currentUser.violations" 
                  :key="violation.id"
                  :timestamp="violation.time"
                  :type="getViolationType(violation.type)"
                >
                  <div>
                    <strong>{{ violation.typeText || violation.type }}</strong>
                    <p>{{ violation.reason }}</p>
                    <small>处理人：{{ violation.handler }}</small>
                  </div>
                </el-timeline-item>
              </el-timeline>
            </div>
            <span v-else class="no-violations">无违规记录</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 违规处理对话框 -->
    <el-dialog v-model="violationDialogVisible" title="违规处理" width="500px">
      <el-form :model="violationForm" :rules="violationRules" ref="violationFormRef" label-width="100px">
        <el-form-item label="处理类型" prop="type">
          <el-select v-model="violationForm.type" placeholder="选择处理类型" style="width: 100%">
            <el-option label="警告" value="warning" />
            <el-option label="限制下单" value="limit" />
            <el-option label="冻结账号" value="freeze" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理原因" prop="reason">
          <el-input 
            v-model="violationForm.reason" 
            type="textarea" 
            :rows="4"
            placeholder="请输入处理原因"
          />
        </el-form-item>
        <el-form-item v-if="violationForm.type === 'limit'" label="限制天数">
          <el-input-number v-model="violationForm.limitDays" :min="1" :max="30" />
          <span class="form-tip">天（每日最多3单）</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="violationDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitViolation">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, getUserDetail, handleUserViolation } from '@/api/user'

// 搜索表单
const searchForm = reactive({
  type: 'username',
  keyword: '',
  status: ''
})

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 用户列表数据
const userList = ref([])
const loading = ref(false)

// 对话框状态
const detailDialogVisible = ref(false)
const violationDialogVisible = ref(false)
const currentUser = ref(null)

// 违规处理表单
const violationForm = reactive({
  type: '',
  reason: '',
  limitDays: 7
})

const violationFormRef = ref(null)

const violationRules = {
  type: [{ required: true, message: '请选择处理类型', trigger: 'change' }],
  reason: [{ required: true, message: '请输入处理原因', trigger: 'blur' }]
}

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    // 转换状态：前端显示的状态字符串 -> 后端需要的状态码
    const statusMap = {
      'normal': 1,
      'limited': 2,
      'frozen': 3
    }
    
    const params = {
      type: searchForm.type,
      keyword: searchForm.keyword || undefined,
      status: searchForm.status ? statusMap[searchForm.status] : undefined,
      current: pagination.currentPage,
      size: pagination.pageSize
    }
    
    // 移除undefined参数
    Object.keys(params).forEach(key => {
      if (params[key] === undefined || params[key] === '') {
        delete params[key]
      }
    })
    
    const response = await getUserList(params)
    
    if (response.code === 200 && response.data) {
      const pageData = response.data
      
      // 转换数据格式
      userList.value = pageData.records.map(user => ({
        ...user,
        registerTime: formatDateTime(user.registerTime),
        lastLoginTime: user.lastLoginTime ? formatDateTime(user.lastLoginTime) : null,
        status: convertStatusToText(user.status),
        totalAmount: user.totalAmount || 0,
        orderCount: user.orderCount || 0
      }))
      
      pagination.total = pageData.total || 0
    } else {
      ElMessage.error(response.message || '获取用户列表失败')
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchUserList()
}

// 重置搜索
const handleReset = () => {
  searchForm.type = 'username'
  searchForm.keyword = ''
  searchForm.status = ''
  pagination.currentPage = 1
  fetchUserList()
}

// 分页处理
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  fetchUserList()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  fetchUserList()
}

// 查看用户详情
const handleViewDetail = async (user) => {
  try {
    const response = await getUserDetail(user.id)
    if (response.code === 200 && response.data) {
      const userData = response.data
      currentUser.value = {
        ...userData,
        registerTime: formatDateTime(userData.registerTime),
        lastLoginTime: userData.lastLoginTime ? formatDateTime(userData.lastLoginTime) : null,
        status: convertStatusToText(userData.status),
        violations: userData.violations ? userData.violations.map(v => ({
          ...v,
          time: formatDateTime(v.time),
          type: v.typeText || getViolationTypeText(v.type)
        })) : []
      }
      detailDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取用户详情失败')
    }
  } catch (error) {
    console.error('获取用户详情失败:', error)
    ElMessage.error('获取用户详情失败')
  }
}

// 违规处理
const handleViolation = (user) => {
  currentUser.value = user
  violationForm.type = ''
  violationForm.reason = ''
  violationForm.limitDays = 7
  violationDialogVisible.value = true
}

// 提交违规处理
const handleSubmitViolation = async () => {
  if (!violationFormRef.value) return
  
  try {
    await violationFormRef.value.validate()
    
    const requestData = {
      userId: currentUser.value.id,
      type: violationForm.type,
      reason: violationForm.reason,
      limitDays: violationForm.type === 'limit' ? violationForm.limitDays : undefined
    }
    
    const response = await handleUserViolation(requestData)
    
    if (response.code === 200) {
      ElMessage.success('处理成功')
      violationDialogVisible.value = false
      // 重新获取用户列表
      fetchUserList()
      // 如果详情对话框打开着，重新获取详情
      if (detailDialogVisible.value) {
        handleViewDetail(currentUser.value)
      }
    } else {
      ElMessage.error(response.message || '处理失败')
    }
  } catch (error) {
    if (error.message) {
      // 验证失败，不显示错误消息
      return
    }
    console.error('处理违规失败:', error)
    ElMessage.error('处理失败')
  }
}

// 转换状态码为文本（用于显示）
const convertStatusToText = (status) => {
  if (typeof status === 'string') {
    return status
  }
  const statusMap = {
    1: 'normal',
    2: 'limited',
    3: 'frozen',
    0: 'disabled'
  }
  return statusMap[status] || 'normal'
}

// 获取状态类型（用于el-tag的type属性）
const getStatusType = (status) => {
  // 如果已经是文本形式，直接使用
  if (typeof status === 'string') {
    const statusMap = {
      normal: 'success',
      limited: 'warning',
      frozen: 'danger',
      disabled: 'info'
    }
    return statusMap[status] || 'info'
  }
  // 如果是数字，先转换
  const textStatus = convertStatusToText(status)
  const statusMap = {
    normal: 'success',
    limited: 'warning',
    frozen: 'danger',
    disabled: 'info'
  }
  return statusMap[textStatus] || 'info'
}

// 获取状态文本（用于显示）
const getStatusText = (status) => {
  // 如果已经是文本形式，直接映射
  if (typeof status === 'string') {
    const statusMap = {
      normal: '正常',
      limited: '限制下单',
      frozen: '冻结',
      disabled: '禁用'
    }
    return statusMap[status] || '未知'
  }
  // 如果是数字，先转换
  const textStatus = convertStatusToText(status)
  const statusMap = {
    normal: '正常',
    limited: '限制下单',
    frozen: '冻结',
    disabled: '禁用'
  }
  return statusMap[textStatus] || '未知'
}

// 获取违规类型（用于el-timeline-item的type属性）
const getViolationType = (type) => {
  // type可能是中文或英文
  const typeMap = {
    '警告': 'primary',
    '限制下单': 'warning',
    '冻结账号': 'danger',
    'warning': 'primary',
    'limit': 'warning',
    'freeze': 'danger'
  }
  return typeMap[type] || 'info'
}

// 获取违规类型文本
const getViolationTypeText = (type) => {
  const typeMap = {
    warning: '警告',
    limit: '限制下单',
    freeze: '冻结账号'
  }
  return typeMap[type] || type
}

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

// 初始化
onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.search-card {
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 0;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.user-detail {
  max-height: 500px;
  overflow-y: auto;
}

.amount {
  color: #f56c6c;
  font-weight: bold;
}

.no-violations {
  color: #909399;
  font-style: italic;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}
</style>
