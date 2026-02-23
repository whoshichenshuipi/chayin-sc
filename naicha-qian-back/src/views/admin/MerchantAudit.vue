<template>
  <div class="page-container">
    <div class="page-header">
      <h2>商家入驻审核</h2>
    </div>
    
    <el-card>
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 待审核申请 -->
        <el-tab-pane label="待审核申请" name="pending">
          <div class="filter-section">
            <el-form :inline="true" class="filter-form">
              <el-form-item label="申请时间">
                <el-date-picker
                  v-model="filterDateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-form-item label="店铺名称">
                <el-input
                  v-model="filterForm.shopName"
                  placeholder="请输入店铺名称"
                  clearable
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="searchApplications">查询</el-button>
                <el-button @click="resetFilter">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table :data="pendingApplications" v-loading="loading" style="width: 100%">
            <el-table-column prop="id" label="申请ID" width="100" />
            <el-table-column prop="shopName" label="店铺名称" width="150" />
            <el-table-column prop="contactName" label="申请人" width="120" />
            <el-table-column prop="contactPhone" label="联系电话" width="130" />
            <el-table-column prop="createTime" label="申请时间" width="160">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag type="warning">待审核</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="280">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewApplication(scope.row)">
                  查看详情
                </el-button>
                <el-button type="success" size="small" @click="approveApplication(scope.row)">
                  审核通过
                </el-button>
                <el-button type="danger" size="small" @click="rejectApplication(scope.row)">
                  审核拒绝
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination" v-if="pendingPagination.total > 0">
            <el-pagination
              v-model:current-page="pendingPagination.currentPage"
              v-model:page-size="pendingPagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="pendingPagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handlePendingSizeChange"
              @current-change="handlePendingCurrentChange"
            />
          </div>
        </el-tab-pane>

        <!-- 审核历史 -->
        <el-tab-pane label="审核历史" name="history">
          <div class="filter-section">
            <el-form :inline="true" class="filter-form">
              <el-form-item label="审核结果">
                <el-select v-model="historyFilter.status" placeholder="请选择审核结果" clearable>
                  <el-option label="审核通过" :value="1" />
                  <el-option label="审核拒绝" :value="2" />
                </el-select>
              </el-form-item>
              <el-form-item label="审核时间">
                <el-date-picker
                  v-model="historyFilter.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="searchHistory">查询</el-button>
                <el-button @click="resetHistoryFilter">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table :data="auditHistory" v-loading="historyLoading" style="width: 100%">
            <el-table-column prop="id" label="申请ID" width="100" />
            <el-table-column prop="shopName" label="店铺名称" width="150" />
            <el-table-column prop="contactName" label="申请人" width="120" />
            <el-table-column prop="auditTime" label="审核时间" width="160">
              <template #default="{ row }">
                {{ formatDateTime(row.auditTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="auditorName" label="审核人" width="120">
              <template #default="{ row }">
                {{ row.auditorName || '系统' }}
              </template>
            </el-table-column>
            <el-table-column prop="result" label="审核结果" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.result === 'approved' ? 'success' : 'danger'">
                  {{ scope.row.result === 'approved' ? '通过' : '拒绝' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="comment" label="审核意见" min-width="200">
              <template #default="{ row }">
                {{ row.comment || '无' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewAuditDetail(scope.row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination" v-if="historyPagination.total > 0">
            <el-pagination
              v-model:current-page="historyPagination.currentPage"
              v-model:page-size="historyPagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="historyPagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleHistorySizeChange"
              @current-change="handleHistoryCurrentChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 申请详情对话框 -->
    <el-dialog
      v-model="applicationDialog.visible"
      :title="`申请详情 - ${applicationDialog.data?.shopName}`"
      width="80%"
      :close-on-click-modal="false"
    >
      <div v-if="applicationDialog.data" class="application-detail">
        <!-- 基本信息 -->
        <el-card class="detail-section">
          <template #header>
            <h3>基本信息</h3>
          </template>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <label>店铺名称：</label>
                <span>{{ applicationDialog.data.shopName }}</span>
              </div>
              <div class="info-item">
                <label>申请人：</label>
                <span>{{ applicationDialog.data.contactName }}</span>
              </div>
              <div class="info-item">
                <label>联系电话：</label>
                <span>{{ applicationDialog.data.contactPhone }}</span>
              </div>
              <div class="info-item">
                <label>店铺地址：</label>
                <span>{{ applicationDialog.data.address }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label>申请时间：</label>
                <span>{{ formatDateTime(applicationDialog.data.createTime) }}</span>
              </div>
              <div class="info-item">
                <label>用户名：</label>
                <span>{{ applicationDialog.data.username }}</span>
              </div>
              <div class="info-item">
                <label>联系邮箱：</label>
                <span>{{ applicationDialog.data.contactEmail || '未填写' }}</span>
              </div>
              <div class="info-item">
                <label>店铺描述：</label>
                <span>{{ applicationDialog.data.description || '未填写' }}</span>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 资质材料 -->
        <el-card class="detail-section" v-if="applicationDialog.data.businessLicense">
          <template #header>
            <h3>资质材料</h3>
          </template>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="document-item">
                <label>营业执照：</label>
                <div class="document-preview">
                  <el-image
                    :src="applicationDialog.data.businessLicense"
                    :preview-src-list="[applicationDialog.data.businessLicense]"
                    class="document-image"
                    fit="cover"
                    v-if="applicationDialog.data.businessLicense"
                  />
                  <span v-else>未上传</span>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 审核表单 -->
        <el-card class="detail-section">
          <template #header>
            <h3>审核处理</h3>
          </template>
          <el-form :model="auditForm" label-width="120px">
            <el-form-item label="审核结果" required>
              <el-radio-group v-model="auditForm.result">
                <el-radio label="approved">审核通过</el-radio>
                <el-radio label="rejected">审核拒绝</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item v-if="auditForm.result === 'approved'" label="自动生成账号">
              <el-switch v-model="auditForm.autoGenerateAccount" />
              <span class="form-tip">开启后将自动生成商家账号密码并发送至预留手机号</span>
            </el-form-item>
            
            <el-form-item label="审核意见" required>
              <el-input
                v-model="auditForm.comment"
                type="textarea"
                :rows="4"
                :placeholder="auditForm.result === 'approved' ? '请输入审核通过意见' : '请输入拒绝理由'"
              />
            </el-form-item>
            
            <el-form-item v-if="auditForm.result === 'rejected'" label="驳回原因">
              <el-select v-model="auditForm.rejectReason" placeholder="请选择驳回原因" style="width: 300px;">
                <el-option label="营业执照过期" value="business_license_expired" />
                <el-option label="食品经营许可证过期" value="food_license_expired" />
                <el-option label="身份证信息不清晰" value="id_card_unclear" />
                <el-option label="店铺实景照片不符合要求" value="shop_photos_invalid" />
                <el-option label="其他资质问题" value="other_qualification" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="applicationDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitAudit" :loading="submitting">
            提交审核
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPendingAuditMerchants, getAuditHistory, auditMerchant, approveMerchant, rejectMerchant, getMerchantDetail } from '@/api/admin'

const activeTab = ref('pending')
const submitting = ref(false)
const loading = ref(false)
const historyLoading = ref(false)

// 筛选表单
const filterForm = reactive({
  shopName: ''
})

const filterDateRange = ref([])
const historyFilter = reactive({
  status: null,
  dateRange: []
})

// 分页
const pendingPagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

const historyPagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 审核表单
const auditForm = reactive({
  result: '',
  autoGenerateAccount: true,
  comment: '',
  rejectReason: ''
})

// 申请详情对话框
const applicationDialog = reactive({
  visible: false,
  data: null
})

const pendingApplications = ref([])
const auditHistory = ref([])

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
  if (tabName === 'pending') {
    loadPendingApplications()
  } else if (tabName === 'history') {
    loadAuditHistory()
  }
}

// 加载待审核申请列表
const loadPendingApplications = async () => {
  loading.value = true
  try {
    const params = {
      current: pendingPagination.currentPage,
      size: pendingPagination.pageSize
    }
    
    if (filterForm.shopName) {
      params.shopName = filterForm.shopName
    }
    
    if (filterDateRange.value && filterDateRange.value.length === 2) {
      params.startTime = filterDateRange.value[0] + ' 00:00:00'
      params.endTime = filterDateRange.value[1] + ' 23:59:59'
    }
    
    const response = await getPendingAuditMerchants(params)
    if (response.code === 200 && response.data) {
      pendingApplications.value = response.data.records || []
      pendingPagination.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取待审核列表失败')
    }
  } catch (error) {
    console.error('加载待审核列表失败:', error)
    ElMessage.error('加载待审核列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 加载审核历史
const loadAuditHistory = async () => {
  historyLoading.value = true
  try {
    const params = {
      current: historyPagination.currentPage,
      size: historyPagination.pageSize
    }
    
    if (historyFilter.status !== null && historyFilter.status !== '') {
      params.status = historyFilter.status
    }
    
    if (historyFilter.dateRange && historyFilter.dateRange.length === 2) {
      params.startTime = historyFilter.dateRange[0] + ' 00:00:00'
      params.endTime = historyFilter.dateRange[1] + ' 23:59:59'
    }
    
    const response = await getAuditHistory(params)
    if (response.code === 200 && response.data) {
      auditHistory.value = response.data.records || []
      historyPagination.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取审核历史失败')
    }
  } catch (error) {
    console.error('加载审核历史失败:', error)
    ElMessage.error('加载审核历史失败，请稍后重试')
  } finally {
    historyLoading.value = false
  }
}

// 查看申请详情
const viewApplication = async (row) => {
  try {
    loading.value = true
    const response = await getMerchantDetail(row.id)
    if (response.code === 200 && response.data) {
      applicationDialog.data = response.data
      applicationDialog.visible = true
      
      // 重置审核表单
      auditForm.result = ''
      auditForm.autoGenerateAccount = true
      auditForm.comment = ''
      auditForm.rejectReason = ''
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

// 快速审核通过
const approveApplication = async (row) => {
  try {
    await ElMessageBox.confirm('确定要审核通过此申请吗？', '确认审核', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })
    
    const response = await approveMerchant(row.id)
    if (response.code === 200) {
      ElMessage.success('审核通过成功')
      await loadPendingApplications()
    } else {
      ElMessage.error(response.message || '审核失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核通过失败:', error)
      ElMessage.error('审核失败，请稍后重试')
    }
  }
}

// 快速审核拒绝
const rejectApplication = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝理由', '审核拒绝', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入拒绝理由',
      inputValidator: (value) => {
        if (!value) {
          return '请输入拒绝理由'
        }
        return true
      }
    })
    
    const response = await rejectMerchant(row.id, value)
    if (response.code === 200) {
      ElMessage.success('已拒绝申请')
      await loadPendingApplications()
    } else {
      ElMessage.error(response.message || '审核失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核拒绝失败:', error)
      ElMessage.error('审核失败，请稍后重试')
    }
  }
}

// 提交审核
const submitAudit = async () => {
  if (!auditForm.result) {
    ElMessage.warning('请选择审核结果')
    return
  }
  
  if (!auditForm.comment) {
    ElMessage.warning('请输入审核意见')
    return
  }
  
  if (auditForm.result === 'rejected' && !auditForm.rejectReason) {
    ElMessage.warning('请选择驳回原因')
    return
  }
  
  submitting.value = true
  
  try {
    const requestData = {
      merchantId: applicationDialog.data.id,
      result: auditForm.result,
      comment: auditForm.comment,
      autoGenerateAccount: auditForm.autoGenerateAccount,
      rejectReason: auditForm.rejectReason
    }
    
    const response = await auditMerchant(requestData)
    if (response.code === 200) {
      if (auditForm.result === 'approved') {
        ElMessage.success('审核通过成功')
      } else {
        ElMessage.success('审核拒绝成功')
      }
      
      applicationDialog.visible = false
      await loadPendingApplications()
    } else {
      ElMessage.error(response.message || '审核失败')
    }
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 查看审核历史详情
const viewAuditDetail = async (row) => {
  try {
    loading.value = true
    const response = await getMerchantDetail(row.id)
    if (response.code === 200 && response.data) {
      applicationDialog.data = response.data
      applicationDialog.visible = true
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

// 搜索申请
const searchApplications = () => {
  pendingPagination.currentPage = 1
  loadPendingApplications()
}

// 重置筛选
const resetFilter = () => {
  filterForm.shopName = ''
  filterDateRange.value = []
  pendingPagination.currentPage = 1
  loadPendingApplications()
}

// 搜索历史
const searchHistory = () => {
  historyPagination.currentPage = 1
  loadAuditHistory()
}

// 重置历史筛选
const resetHistoryFilter = () => {
  historyFilter.status = null
  historyFilter.dateRange = []
  historyPagination.currentPage = 1
  loadAuditHistory()
}

// 分页处理
const handlePendingSizeChange = (val) => {
  pendingPagination.pageSize = val
  pendingPagination.currentPage = 1
  loadPendingApplications()
}

const handlePendingCurrentChange = (val) => {
  pendingPagination.currentPage = val
  loadPendingApplications()
}

const handleHistorySizeChange = (val) => {
  historyPagination.pageSize = val
  historyPagination.currentPage = 1
  loadAuditHistory()
}

const handleHistoryCurrentChange = (val) => {
  historyPagination.currentPage = val
  loadAuditHistory()
}

onMounted(() => {
  loadPendingApplications()
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

.application-detail {
  .detail-section {
    margin-bottom: 20px;
  }
  
  .info-item {
    display: flex;
    margin-bottom: 10px;
    
    label {
      width: 100px;
      font-weight: 500;
      color: #666;
    }
    
    span {
      flex: 1;
    }
  }
  
  .document-item {
    margin-bottom: 20px;
    
    label {
      display: block;
      margin-bottom: 10px;
      font-weight: 500;
      color: #666;
    }
  }
  
  .document-preview {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
  }
  
  .document-image {
    width: 120px;
    height: 80px;
    border-radius: 4px;
    border: 1px solid #d9d9d9;
    cursor: pointer;
  }
}

.form-tip {
  margin-left: 10px;
  color: #666;
  font-size: 12px;
}

.dialog-footer {
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .application-detail {
    .info-item {
      flex-direction: column;
      
      label {
        width: auto;
        margin-bottom: 5px;
      }
    }
    
    .document-preview {
      justify-content: center;
    }
  }
}
</style>
