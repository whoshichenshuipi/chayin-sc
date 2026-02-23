<template>
  <div class="page-container">
    <div class="page-header">
      <h2>系统日志管理</h2>
    </div>
    
    <!-- 日志类型切换 -->
    <el-card class="log-type-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="操作日志" name="operation">
          <div class="tab-content">
            <p>记录管理员所有操作：操作人、操作时间、操作模块、操作内容、操作结果</p>
          </div>
        </el-tab-pane>
        <el-tab-pane label="系统日志" name="system">
          <div class="tab-content">
            <p>记录系统运行事件：系统启动/关闭、数据库连接异常、支付接口调用失败、异常登录</p>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 搜索和操作区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="日志级别">
          <el-select v-model="searchForm.level" placeholder="选择日志级别" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="INFO" value="info" />
            <el-option label="WARN" value="warn" />
            <el-option label="ERROR" value="error" />
            <el-option label="DEBUG" value="debug" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作模块">
          <el-select v-model="searchForm.module" placeholder="选择操作模块" clearable style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="用户管理" value="user" />
            <el-option label="商家管理" value="merchant" />
            <el-option label="订单管理" value="order" />
            <el-option label="系统配置" value="system" />
            <el-option label="数据统计" value="statistics" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作用户">
          <el-input v-model="searchForm.user" placeholder="输入用户名" style="width: 120px" />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 300px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button @click="handleBackup" :loading="backupLoading">
          <el-icon><Download /></el-icon>
          手动备份
        </el-button>
        <el-button @click="handleExport" :loading="exportLoading">
          <el-icon><Document /></el-icon>
          导出日志
        </el-button>
        <el-button @click="handleAutoBackup">
          <el-icon><Setting /></el-icon>
          自动备份设置
        </el-button>
        <el-button @click="handleCleanLogs" type="danger">
          <el-icon><Delete /></el-icon>
          清理日志
        </el-button>
      </div>
    </el-card>
    
    <!-- 日志列表 -->
    <el-card>
      <el-table :data="logList" v-loading="loading" stripe>
        <el-table-column prop="id" label="日志ID" width="80" />
        <el-table-column prop="level" label="级别" width="80">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.level)" size="small">
              {{ row.level.toUpperCase() }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="user" label="操作用户" width="120" />
        <el-table-column prop="module" label="操作模块" width="120">
          <template #default="{ row }">
            <el-tag :type="getModuleTagType(row.module)" size="small">
              {{ getModuleText(row.module) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="result" label="操作结果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.result === 'success' ? 'success' : 'danger'" size="small">
              {{ row.result === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="130" />
        <el-table-column prop="createTime" label="操作时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
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
    
    <!-- 日志详情对话框 -->
    <el-dialog v-model="detailVisible" title="日志详情" width="800px">
      <div v-if="currentLog" class="log-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
          <el-descriptions-item label="日志级别">
            <el-tag :type="getLevelTagType(currentLog.level)">
              {{ currentLog.level.toUpperCase() }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作用户">{{ currentLog.user }}</el-descriptions-item>
          <el-descriptions-item label="操作模块">
            <el-tag :type="getModuleTagType(currentLog.module)">
              {{ getModuleText(currentLog.module) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="IP地址">{{ currentLog.ip }}</el-descriptions-item>
          <el-descriptions-item label="操作结果">
            <el-tag :type="currentLog.result === 'success' ? 'success' : 'danger'">
              {{ currentLog.result === 'success' ? '成功' : '失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作时间" :span="2">{{ formatDateTime(currentLog.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="操作内容" :span="2">{{ currentLog.action }}</el-descriptions-item>
          <el-descriptions-item label="详细信息" :span="2">
            <div class="log-detail-content">
              <pre>{{ currentLog.detail }}</pre>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="请求参数" :span="2" v-if="currentLog.params">
            <div class="log-detail-content">
              <pre>{{ JSON.stringify(currentLog.params, null, 2) }}</pre>
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 自动备份设置对话框 -->
    <el-dialog v-model="backupDialogVisible" title="自动备份设置" width="500px">
      <el-form :model="backupForm" :rules="backupRules" ref="backupFormRef" label-width="120px" v-if="backupDialogVisible">
        <el-form-item label="启用自动备份" prop="enabled">
          <el-switch v-model="backupForm.enabled" />
        </el-form-item>
        <el-form-item label="备份频率" prop="frequency" v-if="backupForm.enabled">
          <el-select v-model="backupForm.frequency" placeholder="选择备份频率" style="width: 100%">
            <el-option label="每日" value="daily" />
            <el-option label="每周" value="weekly" />
            <el-option label="每月" value="monthly" />
          </el-select>
        </el-form-item>
        <el-form-item label="备份时间" prop="time" v-if="backupForm.enabled">
          <el-time-picker v-model="backupForm.time" placeholder="选择备份时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="保留天数" prop="retentionDays" v-if="backupForm.enabled">
          <el-input-number v-model="backupForm.retentionDays" :min="1" :max="365" style="width: 100%" />
          <span class="form-tip">超过指定天数的日志将被自动清理</span>
        </el-form-item>
        <el-form-item label="备份路径" prop="backupPath" v-if="backupForm.enabled">
          <el-input v-model="backupForm.backupPath" placeholder="请输入备份路径" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="backupDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveBackupSettings">保存设置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Document, Setting, Delete } from '@element-plus/icons-vue'
import * as systemLogApi from '@/api/systemLog'

// 当前激活的标签页
const activeTab = ref('operation')

// 加载状态
const loading = ref(false)
const backupLoading = ref(false)
const exportLoading = ref(false)

// 对话框状态
const detailVisible = ref(false)
const backupDialogVisible = ref(false)
const currentLog = ref(null)
const backupFormRef = ref(null)

// 搜索表单
const searchForm = reactive({
  level: '',
  module: '',
  user: '',
  dateRange: []
})

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 自动备份设置表单
const backupForm = reactive({
  enabled: false,
  frequency: 'daily',
  time: null,
  retentionDays: 30,
  backupPath: '/backup/logs'
})

const backupRules = {
  frequency: [{ required: true, message: '请选择备份频率', trigger: 'change' }],
  time: [{ required: true, message: '请选择备份时间', trigger: 'change' }],
  retentionDays: [{ required: true, message: '请输入保留天数', trigger: 'blur' }],
  backupPath: [{ required: true, message: '请输入备份路径', trigger: 'blur' }]
}

const logList = ref([])

// 获取日志级别标签类型
const getLevelTagType = (level) => {
  const typeMap = {
    info: 'info',
    warn: 'warning',
    error: 'danger',
    debug: 'primary'
  }
  return typeMap[level] || 'info'
}

// 获取模块标签类型
const getModuleTagType = (module) => {
  const typeMap = {
    user: 'primary',
    merchant: 'success',
    order: 'warning',
    system: 'info',
    statistics: 'danger'
  }
  return typeMap[module] || 'info'
}

// 获取模块文本
const getModuleText = (module) => {
  const textMap = {
    user: '用户管理',
    merchant: '商家管理',
    order: '订单管理',
    system: '系统配置',
    statistics: '数据统计',
    database: '数据库',
    payment: '支付',
    security: '安全'
  }
  return textMap[module] || '未知'
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  if (typeof dateTime === 'string') {
    return dateTime.replace('T', ' ').substring(0, 19)
  }
  return dateTime
}

// 处理标签页切换
const handleTabChange = (tabName) => {
  pagination.currentPage = 1
  loadLogs()
}

// 加载日志数据
const loadLogs = async () => {
  loading.value = true
  try {
    const params = {
      type: activeTab.value,
      level: searchForm.level || undefined,
      module: searchForm.module || undefined,
      user: searchForm.user || undefined,
      startTime: searchForm.dateRange && searchForm.dateRange.length > 0 
        ? (searchForm.dateRange[0] instanceof Date ? searchForm.dateRange[0].toISOString() : searchForm.dateRange[0])
        : undefined,
      endTime: searchForm.dateRange && searchForm.dateRange.length > 1 
        ? (searchForm.dateRange[1] instanceof Date ? searchForm.dateRange[1].toISOString() : searchForm.dateRange[1])
        : undefined,
      current: pagination.currentPage,
      size: pagination.pageSize
    }
    
    // 移除 undefined 参数
    Object.keys(params).forEach(key => params[key] === undefined && delete params[key])
    
    const response = await systemLogApi.getLogList(params)
    if (response.code === 200 && response.data) {
      logList.value = response.data.records || []
      pagination.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '加载日志失败')
    }
  } catch (error) {
    console.error('加载日志失败:', error)
    ElMessage.error('加载日志失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  loadLogs()
}

// 重置搜索
const handleReset = () => {
  searchForm.level = ''
  searchForm.module = ''
  searchForm.user = ''
  searchForm.dateRange = []
  pagination.currentPage = 1
  loadLogs()
}

// 分页处理
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  loadLogs()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  loadLogs()
}

// 查看详情
const viewDetail = async (row) => {
  try {
    const response = await systemLogApi.getLogById(row.id)
    if (response.code === 200 && response.data) {
      currentLog.value = response.data
      detailVisible.value = true
    } else {
      ElMessage.error(response.message || '获取日志详情失败')
    }
  } catch (error) {
    console.error('获取日志详情失败:', error)
    ElMessage.error('获取日志详情失败')
  }
}

// 手动备份
const handleBackup = async () => {
  try {
    await ElMessageBox.confirm('确定要执行手动备份吗？', '确认备份', {
      type: 'warning'
    })
    
    backupLoading.value = true
    const response = await systemLogApi.backupLogs()
    
    if (response.code === 200) {
      ElMessage.success(response.message || '日志备份完成')
    } else {
      ElMessage.error(response.message || '备份失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('备份失败:', error)
      ElMessage.error('备份失败')
    }
  } finally {
    backupLoading.value = false
  }
}

// 导出日志
const handleExport = async () => {
  try {
    await ElMessageBox.confirm('确定要导出日志吗？', '确认导出', {
      type: 'warning'
    })
    
    exportLoading.value = true
    const queryDTO = {
      type: activeTab.value,
      level: searchForm.level || undefined,
      module: searchForm.module || undefined,
      user: searchForm.user || undefined,
      startTime: searchForm.dateRange && searchForm.dateRange.length > 0 
        ? (searchForm.dateRange[0] instanceof Date ? searchForm.dateRange[0].toISOString() : searchForm.dateRange[0])
        : undefined,
      endTime: searchForm.dateRange && searchForm.dateRange.length > 1 
        ? (searchForm.dateRange[1] instanceof Date ? searchForm.dateRange[1].toISOString() : searchForm.dateRange[1])
        : undefined
    }
    
    // 移除 undefined 参数
    Object.keys(queryDTO).forEach(key => queryDTO[key] === undefined && delete queryDTO[key])
    
    const response = await systemLogApi.exportLogs(queryDTO)
    if (response.code === 200) {
      ElMessage.success(response.message || '日志导出完成')
    } else {
      ElMessage.error(response.message || '导出失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('导出失败:', error)
      ElMessage.error('导出失败')
    }
  } finally {
    exportLoading.value = false
  }
}

// 自动备份设置
const handleAutoBackup = async () => {
  try {
    const response = await systemLogApi.getBackupConfig()
    if (response.code === 200 && response.data) {
      const config = response.data
      backupForm.enabled = config.enabled || false
      backupForm.frequency = config.frequency || 'daily'
      backupForm.time = config.time || null
      backupForm.retentionDays = config.retentionDays || 30
      backupForm.backupPath = config.backupPath || '/backup/logs'
    }
    backupDialogVisible.value = true
  } catch (error) {
    console.error('获取备份配置失败:', error)
    backupDialogVisible.value = true
  }
}

// 保存备份设置
const handleSaveBackupSettings = async () => {
  if (!backupFormRef.value) return
  
  try {
    await backupFormRef.value.validate()
    
    const configDTO = {
      enabled: backupForm.enabled,
      frequency: backupForm.frequency,
      time: backupForm.time,
      retentionDays: backupForm.retentionDays,
      backupPath: backupForm.backupPath
    }
    
    const response = await systemLogApi.saveBackupConfig(configDTO)
    if (response.code === 200) {
      ElMessage.success(response.message || '备份设置保存成功')
      backupDialogVisible.value = false
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存备份配置失败:', error)
    if (error !== false) { // false 表示表单验证失败
      ElMessage.error('保存失败')
    }
  }
}

// 清理日志
const handleCleanLogs = async () => {
  try {
    const { value: retentionDays } = await ElMessageBox.prompt(
      '请输入要保留的天数（超过此天数的日志将被清理）',
      '清理日志',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^\d+$/,
        inputErrorMessage: '请输入有效的天数',
        inputValue: backupForm.retentionDays?.toString() || '30'
      }
    )
    
    if (retentionDays) {
      const response = await systemLogApi.cleanLogs(parseInt(retentionDays))
      if (response.code === 200) {
        ElMessage.success(response.message || '日志清理完成')
        loadLogs()
      } else {
        ElMessage.error(response.message || '清理失败')
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清理失败:', error)
      ElMessage.error('清理失败')
    }
  }
}

// 初始化
onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.log-type-card {
  margin-bottom: 20px;
}

.tab-content {
  padding: 10px 0;
  color: #666;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  gap: 10px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.log-detail {
  max-height: 600px;
  overflow-y: auto;
}

.log-detail-content {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
}

.log-detail-content pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-size: 12px;
  line-height: 1.5;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}
</style>
