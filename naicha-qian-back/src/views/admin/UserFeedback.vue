<template>
  <div class="page-container">
    <div class="page-header">
      <h2>用户反馈管理</h2>
    </div>
    
    <!-- 搜索和筛选区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="反馈类型">
          <el-select v-model="searchForm.type" placeholder="选择反馈类型" style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="功能问题" value="bug" />
            <el-option label="界面问题" value="ui" />
            <el-option label="建议类" value="suggestion" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.status" placeholder="选择处理状态" style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已处理" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input 
            v-model="searchForm.keyword" 
            placeholder="搜索反馈内容"
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 反馈列表 -->
    <el-card>
      <el-table :data="feedbackList" v-loading="loading" stripe>
        <el-table-column prop="id" label="反馈ID" width="80" />
        <el-table-column prop="userInfo" label="用户信息" width="150">
          <template #default="{ row }">
            <div>
              <div>{{ row.userName }}</div>
              <div class="user-phone">{{ row.userPhone }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="反馈类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="反馈标题" min-width="200" />
        <el-table-column prop="content" label="反馈内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="submitTime" label="提交时间" width="160" />
        <el-table-column prop="status" label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assignee" label="负责人" width="100" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewDetail(row)">查看详情</el-button>
            <el-button 
              size="small" 
              type="primary" 
              @click="handleAssign(row)"
              v-if="row.status === 'pending'"
            >
              分配处理
            </el-button>
            <el-button 
              size="small" 
              type="success" 
              @click="handleComplete(row)"
              v-if="row.status === 'processing'"
            >
              完成处理
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

    <!-- 反馈详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="反馈详情" width="800px">
      <div v-if="currentFeedback" class="feedback-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="反馈ID">{{ currentFeedback.id }}</el-descriptions-item>
          <el-descriptions-item label="用户信息">
            {{ currentFeedback.userName }} ({{ currentFeedback.userPhone }})
          </el-descriptions-item>
          <el-descriptions-item label="反馈类型">
            <el-tag :type="getTypeTagType(currentFeedback.type)">
              {{ getTypeText(currentFeedback.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="getStatusTagType(currentFeedback.status)">
              {{ getStatusText(currentFeedback.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ currentFeedback.submitTime }}</el-descriptions-item>
          <el-descriptions-item label="负责人">{{ currentFeedback.assignee || '未分配' }}</el-descriptions-item>
          <el-descriptions-item label="反馈标题" :span="2">{{ currentFeedback.title }}</el-descriptions-item>
          <el-descriptions-item label="反馈内容" :span="2">
            <div class="feedback-content">{{ currentFeedback.content }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="处理结果" :span="2" v-if="currentFeedback.result">
            <div class="feedback-result">{{ currentFeedback.result }}</div>
            <div class="result-time">处理时间：{{ currentFeedback.completeTime }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 分配处理对话框 -->
    <el-dialog v-model="assignDialogVisible" title="分配处理" width="500px">
      <el-form :model="assignForm" :rules="assignRules" ref="assignFormRef" label-width="100px">
        <el-form-item label="分配给" prop="assignee">
          <el-select v-model="assignForm.assignee" placeholder="选择负责人" style="width: 100%">
            <el-option label="技术团队A" value="技术团队A" />
            <el-option label="技术团队B" value="技术团队B" />
            <el-option label="产品团队" value="产品团队" />
            <el-option label="客服团队" value="客服团队" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理说明" prop="note">
          <el-input 
            v-model="assignForm.note" 
            type="textarea" 
            :rows="3"
            placeholder="请输入处理说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitAssign">确认分配</el-button>
      </template>
    </el-dialog>

    <!-- 完成处理对话框 -->
    <el-dialog v-model="completeDialogVisible" title="完成处理" width="600px">
      <el-form :model="completeForm" :rules="completeRules" ref="completeFormRef" label-width="100px">
        <el-form-item label="处理结果" prop="result">
          <el-input 
            v-model="completeForm.result" 
            type="textarea" 
            :rows="4"
            placeholder="请填写处理结果，此内容将反馈给用户"
          />
        </el-form-item>
        <el-form-item label="是否通知用户" prop="notifyUser">
          <el-switch v-model="completeForm.notifyUser" />
          <span class="form-tip">开启后将通过前台消息通知用户</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitComplete">确认完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  getUserFeedbackList, 
  getUserFeedbackDetail, 
  assignUserFeedback, 
  completeUserFeedback 
} from '@/api/admin'

// 搜索表单
const searchForm = reactive({
  type: '',
  status: '',
  keyword: ''
})

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 反馈列表数据
const feedbackList = ref([])
const loading = ref(false)

// 对话框状态
const detailDialogVisible = ref(false)
const assignDialogVisible = ref(false)
const completeDialogVisible = ref(false)
const currentFeedback = ref(null)

// 分配表单
const assignForm = reactive({
  assignee: '',
  note: ''
})

const assignFormRef = ref(null)

const assignRules = {
  assignee: [{ required: true, message: '请选择负责人', trigger: 'change' }],
  note: [{ required: true, message: '请输入处理说明', trigger: 'blur' }]
}

// 完成处理表单
const completeForm = reactive({
  result: '',
  notifyUser: true
})

const completeFormRef = ref(null)

const completeRules = {
  result: [{ required: true, message: '请填写处理结果', trigger: 'blur' }]
}

// 获取反馈列表
const getFeedbackList = async () => {
  loading.value = true
  try {
    const params = {
      type: searchForm.type || undefined,
      status: searchForm.status || undefined,
      keyword: searchForm.keyword || undefined,
      current: pagination.currentPage,
      size: pagination.pageSize
    }
    
    // 移除undefined参数
    Object.keys(params).forEach(key => {
      if (params[key] === undefined || params[key] === '') {
        delete params[key]
      }
    })
    
    const response = await getUserFeedbackList(params)
    
    if (response.code === 200 && response.data) {
      const pageData = response.data
      
      // 转换数据格式
      feedbackList.value = pageData.records.map(feedback => ({
        ...feedback,
        submitTime: formatDateTime(feedback.submitTime),
        assignTime: feedback.assignTime ? formatDateTime(feedback.assignTime) : null,
        completeTime: feedback.completeTime ? formatDateTime(feedback.completeTime) : null
      }))
      
      pagination.total = pageData.total || 0
    } else {
      ElMessage.error(response.message || '获取反馈列表失败')
    }
  } catch (error) {
    console.error('获取反馈列表失败:', error)
    ElMessage.error('获取反馈列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  getFeedbackList()
}

// 重置搜索
const handleReset = () => {
  searchForm.type = ''
  searchForm.status = ''
  searchForm.keyword = ''
  pagination.currentPage = 1
  getFeedbackList()
}

// 分页处理
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  getFeedbackList()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  getFeedbackList()
}

// 查看反馈详情
const handleViewDetail = async (feedback) => {
  try {
    const response = await getUserFeedbackDetail(feedback.id)
    if (response.code === 200 && response.data) {
      const feedbackData = response.data
      currentFeedback.value = {
        ...feedbackData,
        submitTime: formatDateTime(feedbackData.submitTime),
        assignTime: feedbackData.assignTime ? formatDateTime(feedbackData.assignTime) : null,
        completeTime: feedbackData.completeTime ? formatDateTime(feedbackData.completeTime) : null
      }
      detailDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取反馈详情失败')
    }
  } catch (error) {
    console.error('获取反馈详情失败:', error)
    ElMessage.error('获取反馈详情失败')
  }
}

// 分配处理
const handleAssign = (feedback) => {
  currentFeedback.value = feedback
  assignForm.assignee = ''
  assignForm.note = ''
  assignDialogVisible.value = true
}

// 提交分配
const handleSubmitAssign = async () => {
  if (!assignFormRef.value) return
  
  try {
    await assignFormRef.value.validate()
    
    const requestData = {
      feedbackId: currentFeedback.value.id,
      assignee: assignForm.assignee,
      note: assignForm.note
    }
    
    const response = await assignUserFeedback(requestData)
    
    if (response.code === 200) {
      ElMessage.success('分配成功')
      assignDialogVisible.value = false
      // 重新获取反馈列表
      getFeedbackList()
      // 如果详情对话框打开着，重新获取详情
      if (detailDialogVisible.value) {
        handleViewDetail(currentFeedback.value)
      }
    } else {
      ElMessage.error(response.message || '分配失败')
    }
  } catch (error) {
    if (error.message) {
      // 验证失败，不显示错误消息
      return
    }
    console.error('分配失败:', error)
    ElMessage.error('分配失败')
  }
}

// 完成处理
const handleComplete = (feedback) => {
  currentFeedback.value = feedback
  completeForm.result = ''
  completeForm.notifyUser = true
  completeDialogVisible.value = true
}

// 提交完成处理
const handleSubmitComplete = async () => {
  if (!completeFormRef.value) return
  
  try {
    await completeFormRef.value.validate()
    
    const requestData = {
      feedbackId: currentFeedback.value.id,
      result: completeForm.result,
      notifyUser: completeForm.notifyUser
    }
    
    const response = await completeUserFeedback(requestData)
    
    if (response.code === 200) {
      if (completeForm.notifyUser) {
        ElMessage.success('处理完成，已通知用户')
      } else {
        ElMessage.success('处理完成')
      }
      
      completeDialogVisible.value = false
      // 重新获取反馈列表
      getFeedbackList()
      // 如果详情对话框打开着，重新获取详情
      if (detailDialogVisible.value) {
        handleViewDetail(currentFeedback.value)
      }
    } else {
      ElMessage.error(response.message || '处理失败')
    }
  } catch (error) {
    if (error.message) {
      // 验证失败，不显示错误消息
      return
    }
    console.error('处理失败:', error)
    ElMessage.error('处理失败')
  }
}

// 获取类型标签类型
const getTypeTagType = (type) => {
  const typeMap = {
    bug: 'danger',
    ui: 'warning',
    suggestion: 'info'
  }
  return typeMap[type] || 'info'
}

// 获取类型文本
const getTypeText = (type) => {
  const typeMap = {
    bug: '功能问题',
    ui: '界面问题',
    suggestion: '建议类'
  }
  return typeMap[type] || '未知'
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const statusMap = {
    pending: 'warning',
    processing: 'primary',
    completed: 'success'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    pending: '待处理',
    processing: '处理中',
    completed: '已处理'
  }
  return statusMap[status] || '未知'
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
  getFeedbackList()
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

.feedback-detail {
  max-height: 600px;
  overflow-y: auto;
}

.user-phone {
  color: #909399;
  font-size: 12px;
}

.feedback-content {
  white-space: pre-wrap;
  line-height: 1.6;
}

.feedback-result {
  white-space: pre-wrap;
  line-height: 1.6;
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  border-left: 4px solid #67c23a;
}

.result-time {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}
</style>
