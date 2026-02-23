<template>
  <div class="page-container">
    <div class="page-header">
      <h2>商家列表</h2>
    </div>
    
    <el-card>
      <!-- 搜索条件 -->
      <div class="search-form">
        <el-form :model="searchForm" inline>
          <el-form-item label="商家名称">
            <el-input v-model="searchForm.name" placeholder="请输入商家名称" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="待审核" :value="0" />
              <el-option label="已通过" :value="1" />
              <el-option label="已拒绝" :value="2" />
              <el-option label="已禁用" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="注册时间">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 商家列表 -->
      <el-table :data="merchantList" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="shopName" label="商家名称" width="150" />
        <el-table-column prop="contactName" label="负责人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="address" label="店铺地址" min-width="200" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="100">
          <template #default="{ row }">
            <el-rate 
              :model-value="row.rating ? Number(row.rating) : 0" 
              disabled 
              show-score 
              :score-template="row.rating ? row.rating.toFixed(1) : '暂无评分'"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="text" @click="viewDetail(row)">详情</el-button>
            <el-button 
              type="text" 
              @click="handleStatus(row)"
              :disabled="row.status === 3"
            >
              {{ row.status === 1 ? '禁用' : row.status === 3 ? '已禁用' : '启用' }}
            </el-button>
            <el-button type="text" @click="editMerchant(row)">编辑</el-button>
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
    
    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="商家详情" width="800px">
      <div v-if="currentMerchant">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商家ID">{{ currentMerchant.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentMerchant.username }}</el-descriptions-item>
          <el-descriptions-item label="商家名称">{{ currentMerchant.shopName }}</el-descriptions-item>
          <el-descriptions-item label="负责人">{{ currentMerchant.contactName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentMerchant.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="联系邮箱">{{ currentMerchant.contactEmail || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="店铺地址" :span="2">{{ currentMerchant.address }}</el-descriptions-item>
          <el-descriptions-item label="营业执照">{{ currentMerchant.businessLicense || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTag(currentMerchant.status)">
              {{ getStatusText(currentMerchant.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="营业状态">
            <el-tag :type="getBusinessStatusTag(currentMerchant.businessStatus)">
              {{ getBusinessStatusText(currentMerchant.businessStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="评分">
            <el-rate 
              :model-value="currentMerchant.rating ? Number(currentMerchant.rating) : 0" 
              disabled 
              show-score
              :score-template="currentMerchant.rating ? currentMerchant.rating.toFixed(1) : '暂无评分'"
            />
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatDateTime(currentMerchant.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="最后登录">
            {{ currentMerchant.lastLogin ? formatDateTime(currentMerchant.lastLogin) : '未登录' }}
          </el-descriptions-item>
          <el-descriptions-item label="店铺描述" :span="2">
            {{ currentMerchant.description || '暂无描述' }}
          </el-descriptions-item>
          <el-descriptions-item label="店铺公告" :span="2">
            {{ currentMerchant.announcement || '暂无公告' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMerchantList, getMerchantDetail, updateMerchantStatus } from '@/api/admin'

const loading = ref(false)
const detailVisible = ref(false)
const currentMerchant = ref(null)

const searchForm = reactive({
  name: '',
  status: null,
  dateRange: []
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

const merchantList = ref([])

// 状态标签映射（后端：0-待审核 1-已审核 2-已拒绝 3-已禁用）
const getStatusTag = (status) => {
  const tagMap = {
    0: 'warning',
    1: 'success',
    2: 'danger',
    3: 'info'
  }
  return tagMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝',
    3: '已禁用'
  }
  return textMap[status] || '未知'
}

// 营业状态标签映射（1-营业中 2-休息中 3-暂停营业）
const getBusinessStatusTag = (status) => {
  const tagMap = {
    1: 'success',
    2: 'warning',
    3: 'danger'
  }
  return tagMap[status] || 'info'
}

const getBusinessStatusText = (status) => {
  const textMap = {
    1: '营业中',
    2: '休息中',
    3: '暂停营业'
  }
  return textMap[status] || '未知'
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

// 加载商家列表
const loadMerchantList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.currentPage,
      size: pagination.pageSize
    }
    
    // 添加查询条件
    if (searchForm.name) {
      params.name = searchForm.name
    }
    if (searchForm.status !== null && searchForm.status !== '') {
      params.status = searchForm.status
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startTime = searchForm.dateRange[0] + ' 00:00:00'
      params.endTime = searchForm.dateRange[1] + ' 23:59:59'
    }
    
    const response = await getMerchantList(params)
    if (response.code === 200 && response.data) {
      merchantList.value = response.data.records || []
      pagination.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取商家列表失败')
    }
  } catch (error) {
    console.error('加载商家列表失败:', error)
    ElMessage.error('加载商家列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  loadMerchantList()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.status = null
  searchForm.dateRange = []
  pagination.currentPage = 1
  loadMerchantList()
}

const viewDetail = async (row) => {
  try {
    loading.value = true
    const response = await getMerchantDetail(row.id)
    if (response.code === 200 && response.data) {
      currentMerchant.value = response.data
      detailVisible.value = true
    } else {
      ElMessage.error(response.message || '获取商家详情失败')
    }
  } catch (error) {
    console.error('获取商家详情失败:', error)
    ElMessage.error('获取商家详情失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleStatus = async (row) => {
  let action = ''
  let newStatus = null
  
  if (row.status === 1) {
    // 已通过 -> 禁用
    action = '禁用'
    newStatus = 3
  } else if (row.status === 3) {
    // 已禁用 -> 启用（恢复为已审核）
    action = '启用'
    newStatus = 1
  } else {
    ElMessage.warning('该状态下不能执行此操作')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要${action}该商家吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await updateMerchantStatus(row.id, newStatus)
    if (response.code === 200) {
      ElMessage.success(`${action}成功`)
      // 更新本地数据
      row.status = newStatus
      // 重新加载列表
      await loadMerchantList()
    } else {
      ElMessage.error(response.message || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新商家状态失败:', error)
      ElMessage.error(`${action}失败，请稍后重试`)
    }
  }
}

const editMerchant = (row) => {
  ElMessage.info('编辑功能开发中...')
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  loadMerchantList()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  loadMerchantList()
}

onMounted(() => {
  loadMerchantList()
})
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 4px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
