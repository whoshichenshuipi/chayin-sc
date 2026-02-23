<template>
  <div class="page-container">
    <div class="page-header">
      <h2>商家违规处理</h2>
    </div>
    
    <el-card>
      <el-tabs v-model="activeTab">
        <!-- 违规记录 -->
        <el-tab-pane label="违规记录" name="violations">
          <div class="filter-section">
            <el-form :inline="true" class="filter-form">
              <el-form-item label="商家名称">
                <el-input
                  v-model="filterForm.merchantName"
                  placeholder="请输入商家名称"
                  clearable
                />
              </el-form-item>
              <el-form-item label="违规类型">
                <el-select v-model="filterForm.violationType" placeholder="请选择违规类型" clearable>
                  <el-option label="食品安全" value="food_safety" />
                  <el-option label="虚假宣传" value="false_advertising" />
                  <el-option label="拒绝售后" value="refuse_service" />
                  <el-option label="恶意刷单" value="fraudulent_orders" />
                  <el-option label="其他违规" value="other" />
                </el-select>
              </el-form-item>
              <el-form-item label="处理状态">
                <el-select v-model="filterForm.status" placeholder="请选择处理状态" clearable>
                  <el-option label="待处理" value="pending" />
                  <el-option label="已处理" value="resolved" />
                  <el-option label="申诉中" value="appealing" />
                </el-select>
              </el-form-item>
              <el-form-item label="违规时间">
                <el-date-picker
                  v-model="filterForm.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="searchViolations">查询</el-button>
                <el-button @click="resetFilter">重置</el-button>
                <el-button type="success" @click="exportViolations">导出记录</el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table :data="violationList" style="width: 100%">
            <el-table-column prop="id" label="违规ID" width="100" />
            <el-table-column prop="merchantName" label="商家名称" width="150" />
            <el-table-column prop="merchantId" label="商家ID" width="100" />
            <el-table-column prop="violationType" label="违规类型" width="120">
              <template #default="scope">
                <el-tag :type="getViolationTypeColor(scope.row.violationType)">
                  {{ getViolationTypeText(scope.row.violationType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="违规描述" min-width="200" show-overflow-tooltip />
            <el-table-column prop="violationTime" label="违规时间" width="160" />
            <el-table-column prop="penalty" label="处罚措施" width="120">
              <template #default="scope">
                <el-tag :type="getPenaltyTypeColor(scope.row.penalty)">
                  {{ scope.row.penalty }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="处理状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewViolationDetail(scope.row)">
                  查看详情
                </el-button>
                <el-button 
                  v-if="scope.row.status === 'pending'"
                  type="warning" 
                  size="small" 
                  @click="handleViolation(scope.row)"
                >
                  处理违规
                </el-button>
                <el-button 
                  v-if="scope.row.status === 'resolved'"
                  type="success" 
                  size="small" 
                  @click="generateNotice(scope.row)"
                >
                  生成通知书
                </el-button>
              </template>
            </el-table-column>
          </el-table>

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
        </el-tab-pane>

        <!-- 处罚统计 -->
        <el-tab-pane label="处罚统计" name="statistics">
          <div class="statistics-section">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-item">
                    <div class="stat-icon warning">
                      <el-icon><Warning /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-count">{{ statistics.totalViolations }}</div>
                      <div class="stat-label">总违规次数</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-item">
                    <div class="stat-icon pending">
                      <el-icon><Clock /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-count">{{ statistics.pendingViolations }}</div>
                      <div class="stat-label">待处理</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-item">
                    <div class="stat-icon resolved">
                      <el-icon><Check /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-count">{{ statistics.resolvedViolations }}</div>
                      <div class="stat-label">已处理</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <div class="stat-item">
                    <div class="stat-icon penalty">
                      <el-icon><Money /></el-icon>
                    </div>
                    <div class="stat-info">
                      <div class="stat-count">{{ statistics.totalPenalties }}</div>
                      <div class="stat-label">总处罚金额(元)</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>

          <!-- 违规类型分布 -->
          <el-card class="chart-card">
            <template #header>
              <h3>违规类型分布</h3>
            </template>
            <div class="chart-container">
              <div class="chart-placeholder">
                <p>违规类型分布图表</p>
                <p>（此处可集成图表库如 ECharts）</p>
              </div>
            </div>
          </el-card>

          <!-- 处罚措施统计 -->
          <el-card class="chart-card">
            <template #header>
              <h3>处罚措施统计</h3>
            </template>
            <div class="chart-container">
              <div class="chart-placeholder">
                <p>处罚措施统计图表</p>
                <p>（此处可集成图表库如 ECharts）</p>
              </div>
            </div>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 违规详情对话框 -->
    <el-dialog
      v-model="violationDialog.visible"
      :title="`违规详情 - ${violationDialog.data?.merchantName}`"
      width="80%"
      :close-on-click-modal="false"
    >
      <div v-if="violationDialog.data" class="violation-detail">
        <!-- 基本信息 -->
        <el-card class="detail-section">
          <template #header>
            <h3>违规信息</h3>
          </template>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <label>违规ID：</label>
                <span>{{ violationDialog.data.id }}</span>
              </div>
              <div class="info-item">
                <label>商家名称：</label>
                <span>{{ violationDialog.data.merchantName }}</span>
              </div>
              <div class="info-item">
                <label>商家ID：</label>
                <span>{{ violationDialog.data.merchantId }}</span>
              </div>
              <div class="info-item">
                <label>违规类型：</label>
                <el-tag :type="getViolationTypeColor(violationDialog.data.violationType)">
                  {{ getViolationTypeText(violationDialog.data.violationType) }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label>违规时间：</label>
                <span>{{ violationDialog.data.violationTime }}</span>
              </div>
              <div class="info-item">
                <label>举报人：</label>
                <span>{{ violationDialog.data.reporter }}</span>
              </div>
              <div class="info-item">
                <label>处理状态：</label>
                <el-tag :type="getStatusType(violationDialog.data.status)">
                  {{ getStatusText(violationDialog.data.status) }}
                </el-tag>
              </div>
              <div class="info-item">
                <label>处理人：</label>
                <span>{{ violationDialog.data.handler || '未处理' }}</span>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 违规描述 -->
        <el-card class="detail-section">
          <template #header>
            <h3>违规描述</h3>
          </template>
          <div class="violation-description">
            <p>{{ violationDialog.data.description }}</p>
            <div v-if="violationDialog.data.evidence" class="evidence-section">
              <h4>证据材料：</h4>
              <div class="evidence-images">
                <el-image
                  v-for="(image, index) in violationDialog.data.evidence"
                  :key="index"
                  :src="image"
                  :preview-src-list="violationDialog.data.evidence"
                  class="evidence-image"
                  fit="cover"
                />
              </div>
            </div>
          </div>
        </el-card>

        <!-- 处理记录 -->
        <el-card class="detail-section">
          <template #header>
            <h3>处理记录</h3>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="(record, index) in violationDialog.data.processingRecords"
              :key="index"
              :timestamp="record.time"
              :type="record.type"
            >
              <div class="timeline-content">
                <h4>{{ record.action }}</h4>
                <p>{{ record.description }}</p>
                <p v-if="record.operator"><strong>操作人：</strong>{{ record.operator }}</p>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </div>
    </el-dialog>

    <!-- 处理违规对话框 -->
    <el-dialog
      v-model="handleDialog.visible"
      title="处理违规"
      width="60%"
      :close-on-click-modal="false"
    >
      <el-form :model="handleForm" label-width="120px" :rules="handleRules" ref="handleFormRef">
        <el-form-item label="违规信息">
          <div class="violation-info">
            <p><strong>商家：</strong>{{ handleDialog.violation?.merchantName }}</p>
            <p><strong>违规类型：</strong>{{ getViolationTypeText(handleDialog.violation?.violationType) }}</p>
            <p><strong>违规描述：</strong>{{ handleDialog.violation?.description }}</p>
          </div>
        </el-form-item>
        
        <el-form-item label="处罚措施" prop="penalty">
          <el-select v-model="handleForm.penalty" placeholder="请选择处罚措施">
            <el-option label="警告" value="warning" />
            <el-option label="罚款" value="fine" />
            <el-option label="限制上架" value="restrict_listing" />
            <el-option label="永久封禁" value="permanent_ban" />
          </el-select>
        </el-form-item>
        
        <el-form-item v-if="handleForm.penalty === 'fine'" label="罚款金额" prop="fineAmount">
          <el-input-number
            v-model="handleForm.fineAmount"
            :min="0"
            :max="10000"
            :precision="2"
            placeholder="请输入罚款金额"
          />
          <span class="form-unit">元</span>
        </el-form-item>
        
        <el-form-item v-if="handleForm.penalty === 'restrict_listing'" label="限制天数" prop="restrictDays">
          <el-input-number
            v-model="handleForm.restrictDays"
            :min="1"
            :max="30"
            placeholder="请输入限制天数"
          />
          <span class="form-unit">天</span>
        </el-form-item>
        
        <el-form-item label="处理意见" prop="opinion">
          <el-input
            v-model="handleForm.opinion"
            type="textarea"
            :rows="4"
            placeholder="请输入处理意见"
          />
        </el-form-item>
        
        <el-form-item label="通知方式">
          <el-checkbox-group v-model="handleForm.notificationMethods">
            <el-checkbox label="sms">短信通知</el-checkbox>
            <el-checkbox label="email">邮件通知</el-checkbox>
            <el-checkbox label="system">系统消息</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitHandle" :loading="handling">提交处理</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 违规处理通知书对话框 -->
    <el-dialog
      v-model="noticeDialog.visible"
      title="违规处理通知书"
      width="70%"
      :close-on-click-modal="false"
    >
      <div v-if="noticeDialog.data" class="notice-content">
        <div class="notice-header">
          <h2>违规处理通知书</h2>
          <p>通知书编号：{{ noticeDialog.data.noticeId }}</p>
        </div>
        
        <div class="notice-body">
          <div class="notice-section">
            <h3>商家信息</h3>
            <p><strong>商家名称：</strong>{{ noticeDialog.data.merchantName }}</p>
            <p><strong>商家ID：</strong>{{ noticeDialog.data.merchantId }}</p>
            <p><strong>联系人：</strong>{{ noticeDialog.data.contactPerson }}</p>
            <p><strong>联系电话：</strong>{{ noticeDialog.data.contact }}</p>
          </div>
          
          <div class="notice-section">
            <h3>违规信息</h3>
            <p><strong>违规时间：</strong>{{ noticeDialog.data.violationTime }}</p>
            <p><strong>违规类型：</strong>{{ getViolationTypeText(noticeDialog.data.violationType) }}</p>
            <p><strong>违规行为：</strong>{{ noticeDialog.data.description }}</p>
          </div>
          
          <div class="notice-section">
            <h3>处罚措施</h3>
            <p><strong>处罚类型：</strong>{{ noticeDialog.data.penalty }}</p>
            <p v-if="noticeDialog.data.fineAmount"><strong>罚款金额：</strong>{{ noticeDialog.data.fineAmount }}元</p>
            <p v-if="noticeDialog.data.restrictDays"><strong>限制天数：</strong>{{ noticeDialog.data.restrictDays }}天</p>
            <p><strong>处理意见：</strong>{{ noticeDialog.data.opinion }}</p>
          </div>
          
          <div class="notice-section">
            <h3>其他说明</h3>
            <p>1. 请商家在收到本通知书后立即整改违规行为</p>
            <p>2. 如有异议，可在收到通知书后3个工作日内提出申诉</p>
            <p>3. 整改完成后，请及时联系平台进行复查</p>
          </div>
        </div>
        
        <div class="notice-footer">
          <p><strong>签发人：</strong>{{ noticeDialog.data.issuer }}</p>
          <p><strong>签发时间：</strong>{{ noticeDialog.data.issueTime }}</p>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="noticeDialog.visible = false">关闭</el-button>
          <el-button type="primary" @click="printNotice">打印通知书</el-button>
          <el-button type="success" @click="sendNotice">发送通知书</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Warning, Clock, Check, Money } from '@element-plus/icons-vue'

const activeTab = ref('violations')
const handling = ref(false)

// 筛选表单
const filterForm = reactive({
  merchantName: '',
  violationType: '',
  status: '',
  dateRange: []
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 统计数据
const statistics = reactive({
  totalViolations: 45,
  pendingViolations: 8,
  resolvedViolations: 37,
  totalPenalties: 12500
})

// 违规详情对话框
const violationDialog = reactive({
  visible: false,
  data: null
})

// 处理违规对话框
const handleDialog = reactive({
  visible: false,
  violation: null
})

const handleForm = reactive({
  penalty: '',
  fineAmount: 0,
  restrictDays: 7,
  opinion: '',
  notificationMethods: ['sms', 'system']
})

const handleRules = {
  penalty: [{ required: true, message: '请选择处罚措施', trigger: 'change' }],
  opinion: [{ required: true, message: '请输入处理意见', trigger: 'blur' }]
}

// 通知书对话框
const noticeDialog = reactive({
  visible: false,
  data: null
})

// 模拟数据
const violationList = ref([
  {
    id: 'V001',
    merchantName: '甜蜜奶茶店',
    merchantId: 'M001',
    violationType: 'food_safety',
    description: '售卖过期商品，违反食品安全规定',
    violationTime: '2024-01-20 14:30:00',
    penalty: '警告',
    status: 'resolved',
    reporter: '用户A',
    handler: '管理员',
    evidence: ['/images/evidence1.jpg', '/images/evidence2.jpg'],
    processingRecords: [
      {
        time: '2024-01-20 14:30:00',
        action: '违规举报',
        description: '用户举报商家售卖过期商品',
        operator: '系统',
        type: 'primary'
      },
      {
        time: '2024-01-20 15:00:00',
        action: '开始处理',
        description: '管理员开始处理违规事件',
        operator: '管理员',
        type: 'warning'
      },
      {
        time: '2024-01-20 16:00:00',
        action: '处理完成',
        description: '已对商家进行警告处理',
        operator: '管理员',
        type: 'success'
      }
    ]
  },
  {
    id: 'V002',
    merchantName: '香浓咖啡屋',
    merchantId: 'M002',
    violationType: 'false_advertising',
    description: '夸大产品功效，进行虚假宣传',
    violationTime: '2024-01-22 16:45:00',
    penalty: '限制上架',
    status: 'pending',
    reporter: '用户B',
    handler: null,
    evidence: ['/images/evidence3.jpg'],
    processingRecords: [
      {
        time: '2024-01-22 16:45:00',
        action: '违规举报',
        description: '用户举报商家虚假宣传',
        operator: '系统',
        type: 'primary'
      }
    ]
  },
  {
    id: 'V003',
    merchantName: '老字号茶庄',
    merchantId: 'M003',
    violationType: 'refuse_service',
    description: '多次拒绝售后，态度恶劣',
    violationTime: '2024-01-25 11:20:00',
    penalty: '永久封禁',
    status: 'resolved',
    reporter: '用户C',
    handler: '管理员',
    evidence: ['/images/evidence4.jpg', '/images/evidence5.jpg'],
    processingRecords: [
      {
        time: '2024-01-25 11:20:00',
        action: '违规举报',
        description: '用户举报商家拒绝售后',
        operator: '系统',
        type: 'primary'
      },
      {
        time: '2024-01-25 14:00:00',
        action: '严重违规处理',
        description: '由于多次违规，对商家进行永久封禁',
        operator: '管理员',
        type: 'danger'
      }
    ]
  }
])

// 获取违规类型颜色
const getViolationTypeColor = (type) => {
  const colorMap = {
    food_safety: 'danger',
    false_advertising: 'warning',
    refuse_service: 'danger',
    fraudulent_orders: 'warning',
    other: 'info'
  }
  return colorMap[type] || 'info'
}

// 获取违规类型文本
const getViolationTypeText = (type) => {
  const textMap = {
    food_safety: '食品安全',
    false_advertising: '虚假宣传',
    refuse_service: '拒绝售后',
    fraudulent_orders: '恶意刷单',
    other: '其他违规'
  }
  return textMap[type] || '未知'
}

// 获取处罚类型颜色
const getPenaltyTypeColor = (penalty) => {
  const colorMap = {
    '警告': 'warning',
    '罚款': 'danger',
    '限制上架': 'warning',
    '永久封禁': 'danger'
  }
  return colorMap[penalty] || 'info'
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    pending: 'warning',
    resolved: 'success',
    appealing: 'info'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    pending: '待处理',
    resolved: '已处理',
    appealing: '申诉中'
  }
  return textMap[status] || '未知'
}

// 查看违规详情
const viewViolationDetail = (row) => {
  violationDialog.data = row
  violationDialog.visible = true
}

// 处理违规
const handleViolation = (row) => {
  handleDialog.violation = row
  handleDialog.visible = true
  
  // 重置表单
  handleForm.penalty = ''
  handleForm.fineAmount = 0
  handleForm.restrictDays = 7
  handleForm.opinion = ''
  handleForm.notificationMethods = ['sms', 'system']
}

// 提交处理
const submitHandle = async () => {
  handling.value = true
  
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('违规处理完成，已发送通知')
    handleDialog.visible = false
    
    // 更新违规状态
    const index = violationList.value.findIndex(item => item.id === handleDialog.violation.id)
    if (index > -1) {
      violationList.value[index].status = 'resolved'
      violationList.value[index].penalty = handleForm.penalty
    }
  } catch (error) {
    ElMessage.error('处理失败，请重试')
  } finally {
    handling.value = false
  }
}

// 生成通知书
const generateNotice = (row) => {
  noticeDialog.data = {
    noticeId: `NOTICE-${row.id}`,
    merchantName: row.merchantName,
    merchantId: row.merchantId,
    contactPerson: '联系人',
    contact: '联系电话',
    violationTime: row.violationTime,
    violationType: row.violationType,
    description: row.description,
    penalty: row.penalty,
    fineAmount: handleForm.fineAmount,
    restrictDays: handleForm.restrictDays,
    opinion: handleForm.opinion,
    issuer: '管理员',
    issueTime: new Date().toLocaleString()
  }
  noticeDialog.visible = true
}

// 打印通知书
const printNotice = () => {
  ElMessage.success('通知书打印功能待实现')
}

// 发送通知书
const sendNotice = () => {
  ElMessage.success('通知书已发送至商家')
  noticeDialog.visible = false
}

// 搜索违规记录
const searchViolations = () => {
  ElMessage.info('搜索功能待实现')
}

// 重置筛选
const resetFilter = () => {
  Object.assign(filterForm, {
    merchantName: '',
    violationType: '',
    status: '',
    dateRange: []
  })
}

// 导出违规记录
const exportViolations = () => {
  ElMessage.success('违规记录导出功能待实现')
}

// 分页处理
const handleSizeChange = (val) => {
  pagination.pageSize = val
  // 重新加载数据
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  // 重新加载数据
}

onMounted(() => {
  pagination.total = violationList.value.length
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

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.statistics-section {
  margin-bottom: 20px;
}

.stat-card {
  .stat-item {
    display: flex;
    align-items: center;
    padding: 20px;
    
    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 20px;
      
      .el-icon {
        font-size: 24px;
        color: white;
      }
      
      &.warning {
        background: #e6a23c;
      }
      
      &.pending {
        background: #909399;
      }
      
      &.resolved {
        background: #67c23a;
      }
      
      &.penalty {
        background: #f56c6c;
      }
    }
    
    .stat-info {
      flex: 1;
      
      .stat-count {
        font-size: 24px;
        font-weight: bold;
        color: #333;
        margin-bottom: 5px;
      }
      
      .stat-label {
        font-size: 14px;
        color: #666;
      }
    }
  }
}

.chart-card {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 4px;
}

.chart-placeholder {
  text-align: center;
  color: #666;
  
  p {
    margin: 5px 0;
  }
}

.violation-detail {
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
  
  .violation-description {
    p {
      margin-bottom: 15px;
      line-height: 1.6;
    }
  }
  
  .evidence-section {
    margin-top: 20px;
    
    h4 {
      margin-bottom: 10px;
      color: #666;
    }
  }
  
  .evidence-images {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
  }
  
  .evidence-image {
    width: 120px;
    height: 80px;
    border-radius: 4px;
    border: 1px solid #d9d9d9;
    cursor: pointer;
  }
  
  .timeline-content {
    h4 {
      margin: 0 0 5px 0;
      color: #333;
    }
    
    p {
      margin: 5px 0;
      color: #666;
      line-height: 1.5;
    }
  }
}

.violation-info {
  p {
    margin: 5px 0;
    color: #666;
  }
}

.notice-content {
  .notice-header {
    text-align: center;
    margin-bottom: 30px;
    padding-bottom: 20px;
    border-bottom: 2px solid #409eff;
    
    h2 {
      margin: 0 0 10px 0;
      color: #333;
    }
    
    p {
      margin: 0;
      color: #666;
    }
  }
  
  .notice-body {
    .notice-section {
      margin-bottom: 25px;
      
      h3 {
        margin: 0 0 15px 0;
        color: #409eff;
        border-left: 4px solid #409eff;
        padding-left: 10px;
      }
      
      p {
        margin: 8px 0;
        line-height: 1.6;
        color: #333;
      }
    }
  }
  
  .notice-footer {
    margin-top: 30px;
    padding-top: 20px;
    border-top: 1px solid #d9d9d9;
    text-align: right;
    
    p {
      margin: 5px 0;
      color: #666;
    }
  }
}

.form-unit {
  margin-left: 8px;
  color: #666;
}

.dialog-footer {
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stat-card .stat-item {
    flex-direction: column;
    text-align: center;
    
    .stat-icon {
      margin-right: 0;
      margin-bottom: 10px;
    }
  }
  
  .violation-detail .info-item {
    flex-direction: column;
    
    label {
      width: auto;
      margin-bottom: 5px;
    }
  }
  
  .evidence-images {
    justify-content: center;
  }
}
</style>
