<template>
  <div class="page-container">
    <div class="page-header">
      <h2>商家信息管理</h2>
    </div>
    
    <el-card>
      <el-tabs v-model="activeTab">
        <!-- 商家列表 -->
        <el-tab-pane label="商家列表" name="list">
          <div class="filter-section">
            <el-form :inline="true" class="filter-form">
              <el-form-item label="店铺名称">
                <el-input
                  v-model="filterForm.shopName"
                  placeholder="请输入店铺名称"
                  clearable
                />
              </el-form-item>
              <el-form-item label="店铺ID">
                <el-input
                  v-model="filterForm.shopId"
                  placeholder="请输入店铺ID"
                  clearable
                />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="filterForm.status" placeholder="请选择状态" clearable>
                  <el-option label="营业中" value="open" />
                  <el-option label="休息中" value="closed" />
                  <el-option label="整改中" value="suspended" />
                  <el-option label="已冻结" value="frozen" />
                </el-select>
              </el-form-item>
              <el-form-item label="评分">
                <el-select v-model="filterForm.rating" placeholder="请选择评分" clearable>
                  <el-option label="5星" value="5" />
                  <el-option label="4星以上" value="4" />
                  <el-option label="3星以上" value="3" />
                  <el-option label="3星以下" value="2" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="searchMerchants">查询</el-button>
                <el-button @click="resetFilter">重置</el-button>
                <el-button type="success" @click="exportData">导出数据</el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table :data="merchantList" style="width: 100%">
            <el-table-column prop="shopId" label="店铺ID" width="100" />
            <el-table-column prop="shopName" label="店铺名称" width="150" />
            <el-table-column prop="address" label="地址" width="200" show-overflow-tooltip />
            <el-table-column prop="contact" label="联系方式" width="130" />
            <el-table-column prop="businessHours" label="营业时间" width="150" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="rating" label="评分" width="100">
              <template #default="scope">
                <el-rate
                  v-model="scope.row.rating"
                  disabled
                  show-score
                  text-color="#ff9900"
                />
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="入驻时间" width="160" />
            <el-table-column label="操作" width="250" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewMerchant(scope.row)">
                  查看详情
                </el-button>
                <el-button type="warning" size="small" @click="editMerchant(scope.row)">
                  编辑信息
                </el-button>
                <el-button 
                  v-if="scope.row.status !== 'frozen'"
                  type="danger" 
                  size="small" 
                  @click="freezeMerchant(scope.row)"
                >
                  冻结账号
                </el-button>
                <el-button 
                  v-if="scope.row.status === 'frozen'"
                  type="success" 
                  size="small" 
                  @click="unfreezeMerchant(scope.row)"
                >
                  解冻账号
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

        <!-- 状态管理 -->
        <el-tab-pane label="状态管理" name="status">
          <div class="status-management">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-card class="status-card">
                  <div class="status-item">
                    <div class="status-icon open">
                      <el-icon><Shop /></el-icon>
                    </div>
                    <div class="status-info">
                      <div class="status-count">{{ statusStats.open }}</div>
                      <div class="status-label">营业中</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="status-card">
                  <div class="status-item">
                    <div class="status-icon closed">
                      <el-icon><Clock /></el-icon>
                    </div>
                    <div class="status-info">
                      <div class="status-count">{{ statusStats.closed }}</div>
                      <div class="status-label">休息中</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="status-card">
                  <div class="status-item">
                    <div class="status-icon suspended">
                      <el-icon><Warning /></el-icon>
                    </div>
                    <div class="status-info">
                      <div class="status-count">{{ statusStats.suspended }}</div>
                      <div class="status-label">整改中</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="status-card">
                  <div class="status-item">
                    <div class="status-icon frozen">
                      <el-icon><Lock /></el-icon>
                    </div>
                    <div class="status-info">
                      <div class="status-count">{{ statusStats.frozen }}</div>
                      <div class="status-label">已冻结</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 商家详情对话框 -->
    <el-dialog
      v-model="merchantDialog.visible"
      :title="`商家详情 - ${merchantDialog.data?.shopName}`"
      width="80%"
      :close-on-click-modal="false"
    >
      <div v-if="merchantDialog.data" class="merchant-detail">
        <!-- 基本信息 -->
        <el-card class="detail-section">
          <template #header>
            <h3>基本信息</h3>
          </template>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <label>店铺ID：</label>
                <span>{{ merchantDialog.data.shopId }}</span>
              </div>
              <div class="info-item">
                <label>店铺名称：</label>
                <span>{{ merchantDialog.data.shopName }}</span>
              </div>
              <div class="info-item">
                <label>联系人：</label>
                <span>{{ merchantDialog.data.contactPerson }}</span>
              </div>
              <div class="info-item">
                <label>联系电话：</label>
                <span>{{ merchantDialog.data.contact }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label>店铺地址：</label>
                <span>{{ merchantDialog.data.address }}</span>
              </div>
              <div class="info-item">
                <label>营业时间：</label>
                <span>{{ merchantDialog.data.businessHours }}</span>
              </div>
              <div class="info-item">
                <label>当前状态：</label>
                <el-tag :type="getStatusType(merchantDialog.data.status)">
                  {{ getStatusText(merchantDialog.data.status) }}
                </el-tag>
              </div>
              <div class="info-item">
                <label>入驻时间：</label>
                <span>{{ merchantDialog.data.createTime }}</span>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 经营数据 -->
        <el-card class="detail-section">
          <template #header>
            <h3>经营数据</h3>
          </template>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="data-item">
                <div class="data-value">{{ merchantDialog.data.totalOrders }}</div>
                <div class="data-label">总订单数</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="data-item">
                <div class="data-value">{{ merchantDialog.data.totalSales }}</div>
                <div class="data-label">总销售额(元)</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="data-item">
                <div class="data-value">{{ merchantDialog.data.rating }}</div>
                <div class="data-label">平均评分</div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 违规记录 -->
        <el-card class="detail-section">
          <template #header>
            <h3>违规记录</h3>
          </template>
          <el-table :data="merchantDialog.data.violations" style="width: 100%">
            <el-table-column prop="date" label="违规时间" width="160" />
            <el-table-column prop="type" label="违规类型" width="120" />
            <el-table-column prop="description" label="违规描述" min-width="200" />
            <el-table-column prop="penalty" label="处罚措施" width="120" />
            <el-table-column prop="status" label="处理状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'resolved' ? 'success' : 'warning'">
                  {{ scope.row.status === 'resolved' ? '已处理' : '待处理' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
    </el-dialog>

    <!-- 编辑商家信息对话框 -->
    <el-dialog
      v-model="editDialog.visible"
      title="编辑商家信息"
      width="60%"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" label-width="120px" :rules="editRules" ref="editFormRef">
        <el-form-item label="店铺名称" prop="shopName">
          <el-input v-model="editForm.shopName" placeholder="请输入店铺名称" />
        </el-form-item>
        
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="editForm.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        
        <el-form-item label="联系电话" prop="contact">
          <el-input v-model="editForm.contact" placeholder="请输入联系电话" />
        </el-form-item>
        
        <el-form-item label="店铺地址" prop="address">
          <el-input v-model="editForm.address" placeholder="请输入店铺地址" />
        </el-form-item>
        
        <el-form-item label="营业时间" prop="businessHours">
          <el-input v-model="editForm.businessHours" placeholder="请输入营业时间" />
        </el-form-item>
        
        <el-form-item label="店铺状态" prop="status">
          <el-select v-model="editForm.status" placeholder="请选择店铺状态">
            <el-option label="营业中" value="open" />
            <el-option label="休息中" value="closed" />
            <el-option label="整改中" value="suspended" />
            <el-option label="已冻结" value="frozen" />
          </el-select>
        </el-form-item>
        
        <el-form-item v-if="editForm.status === 'suspended'" label="整改原因">
          <el-input
            v-model="editForm.suspensionReason"
            type="textarea"
            :rows="3"
            placeholder="请输入整改原因"
          />
        </el-form-item>
        
        <el-form-item v-if="editForm.status === 'frozen'" label="冻结原因">
          <el-input
            v-model="editForm.freezeReason"
            type="textarea"
            :rows="3"
            placeholder="请输入冻结原因"
          />
        </el-form-item>
        
        <el-form-item v-if="editForm.status === 'frozen'" label="解冻条件">
          <el-input
            v-model="editForm.unfreezeCondition"
            type="textarea"
            :rows="3"
            placeholder="请输入解冻条件"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="saveEdit" :loading="saving">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 冻结账号对话框 -->
    <el-dialog
      v-model="freezeDialog.visible"
      title="冻结商家账号"
      width="50%"
      :close-on-click-modal="false"
    >
      <el-form :model="freezeForm" label-width="120px" :rules="freezeRules" ref="freezeFormRef">
        <el-form-item label="商家信息">
          <div class="merchant-info">
            <p><strong>店铺名称：</strong>{{ freezeDialog.merchant?.shopName }}</p>
            <p><strong>店铺ID：</strong>{{ freezeDialog.merchant?.shopId }}</p>
            <p><strong>联系人：</strong>{{ freezeDialog.merchant?.contactPerson }}</p>
          </div>
        </el-form-item>
        
        <el-form-item label="冻结原因" prop="reason">
          <el-select v-model="freezeForm.reason" placeholder="请选择冻结原因">
            <el-option label="严重违规" value="serious_violation" />
            <el-option label="多次违规" value="multiple_violations" />
            <el-option label="恶意刷单" value="fraudulent_orders" />
            <el-option label="食品安全问题" value="food_safety" />
            <el-option label="其他违规行为" value="other" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="详细说明" prop="description">
          <el-input
            v-model="freezeForm.description"
            type="textarea"
            :rows="4"
            placeholder="请详细说明冻结原因"
          />
        </el-form-item>
        
        <el-form-item label="解冻条件" prop="unfreezeCondition">
          <el-input
            v-model="freezeForm.unfreezeCondition"
            type="textarea"
            :rows="3"
            placeholder="请输入解冻条件"
          />
        </el-form-item>
        
        <el-form-item label="通知方式">
          <el-checkbox-group v-model="freezeForm.notificationMethods">
            <el-checkbox label="sms">短信通知</el-checkbox>
            <el-checkbox label="email">邮件通知</el-checkbox>
            <el-checkbox label="system">系统消息</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="freezeDialog.visible = false">取消</el-button>
          <el-button type="danger" @click="confirmFreeze" :loading="freezing">确认冻结</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Shop, Clock, Warning, Lock } from '@element-plus/icons-vue'

const activeTab = ref('list')
const saving = ref(false)
const freezing = ref(false)

// 筛选表单
const filterForm = reactive({
  shopName: '',
  shopId: '',
  status: '',
  rating: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 状态统计
const statusStats = reactive({
  open: 15,
  closed: 8,
  suspended: 3,
  frozen: 2
})

// 商家详情对话框
const merchantDialog = reactive({
  visible: false,
  data: null
})

// 编辑对话框
const editDialog = reactive({
  visible: false
})

const editForm = reactive({
  shopName: '',
  contactPerson: '',
  contact: '',
  address: '',
  businessHours: '',
  status: '',
  suspensionReason: '',
  freezeReason: '',
  unfreezeCondition: ''
})

const editRules = {
  shopName: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contact: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  address: [{ required: true, message: '请输入店铺地址', trigger: 'blur' }],
  businessHours: [{ required: true, message: '请输入营业时间', trigger: 'blur' }],
  status: [{ required: true, message: '请选择店铺状态', trigger: 'change' }]
}

// 冻结对话框
const freezeDialog = reactive({
  visible: false,
  merchant: null
})

const freezeForm = reactive({
  reason: '',
  description: '',
  unfreezeCondition: '',
  notificationMethods: ['sms', 'system']
})

const freezeRules = {
  reason: [{ required: true, message: '请选择冻结原因', trigger: 'change' }],
  description: [{ required: true, message: '请输入详细说明', trigger: 'blur' }],
  unfreezeCondition: [{ required: true, message: '请输入解冻条件', trigger: 'blur' }]
}

// 模拟数据
const merchantList = ref([
  {
    shopId: 'M001',
    shopName: '甜蜜奶茶店',
    address: '北京市朝阳区三里屯街道123号',
    contact: '13800138001',
    contactPerson: '张三',
    businessHours: '09:00-22:00',
    status: 'open',
    rating: 4.5,
    createTime: '2024-01-15 10:30:00',
    totalOrders: 1250,
    totalSales: 45600,
    violations: [
      {
        date: '2024-01-20 14:30:00',
        type: '食品安全',
        description: '售卖过期商品',
        penalty: '警告',
        status: 'resolved'
      }
    ]
  },
  {
    shopId: 'M002',
    shopName: '香浓咖啡屋',
    address: '上海市浦东新区陆家嘴街道456号',
    contact: '13800138002',
    contactPerson: '李四',
    businessHours: '08:00-23:00',
    status: 'suspended',
    rating: 3.8,
    createTime: '2024-01-16 14:20:00',
    totalOrders: 890,
    totalSales: 32100,
    violations: [
      {
        date: '2024-01-22 16:45:00',
        type: '虚假宣传',
        description: '夸大产品功效',
        penalty: '限制上架',
        status: 'pending'
      }
    ]
  },
  {
    shopId: 'M003',
    shopName: '老字号茶庄',
    address: '广州市天河区珠江新城789号',
    contact: '13800138003',
    contactPerson: '王五',
    businessHours: '10:00-21:00',
    status: 'frozen',
    rating: 2.1,
    createTime: '2024-01-10 09:15:00',
    totalOrders: 450,
    totalSales: 18900,
    violations: [
      {
        date: '2024-01-25 11:20:00',
        type: '严重违规',
        description: '多次拒绝售后',
        penalty: '永久封禁',
        status: 'resolved'
      }
    ]
  }
])

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    open: 'success',
    closed: 'info',
    suspended: 'warning',
    frozen: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    open: '营业中',
    closed: '休息中',
    suspended: '整改中',
    frozen: '已冻结'
  }
  return textMap[status] || '未知'
}

// 查看商家详情
const viewMerchant = (row) => {
  merchantDialog.data = row
  merchantDialog.visible = true
}

// 编辑商家信息
const editMerchant = (row) => {
  editDialog.visible = true
  Object.assign(editForm, {
    shopName: row.shopName,
    contactPerson: row.contactPerson,
    contact: row.contact,
    address: row.address,
    businessHours: row.businessHours,
    status: row.status,
    suspensionReason: '',
    freezeReason: '',
    unfreezeCondition: ''
  })
}

// 保存编辑
const saveEdit = async () => {
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('商家信息更新成功')
    editDialog.visible = false
    
    // 更新列表数据
    const index = merchantList.value.findIndex(item => item.shopId === editForm.shopId)
    if (index > -1) {
      Object.assign(merchantList.value[index], editForm)
    }
  } catch (error) {
    ElMessage.error('保存失败，请重试')
  }
}

// 冻结商家账号
const freezeMerchant = (row) => {
  freezeDialog.merchant = row
  freezeDialog.visible = true
  
  // 重置表单
  freezeForm.reason = ''
  freezeForm.description = ''
  freezeForm.unfreezeCondition = ''
  freezeForm.notificationMethods = ['sms', 'system']
}

// 确认冻结
const confirmFreeze = async () => {
  freezing.value = true
  
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('商家账号已冻结，已发送通知')
    freezeDialog.visible = false
    
    // 更新商家状态
    const index = merchantList.value.findIndex(item => item.shopId === freezeDialog.merchant.shopId)
    if (index > -1) {
      merchantList.value[index].status = 'frozen'
    }
  } catch (error) {
    ElMessage.error('冻结失败，请重试')
  } finally {
    freezing.value = false
  }
}

// 解冻商家账号
const unfreezeMerchant = async (row) => {
  try {
    await ElMessageBox.confirm('确定要解冻此商家账号吗？', '确认解冻', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })
    
    // 模拟解冻
    ElMessage.success('商家账号已解冻')
    
    // 更新商家状态
    const index = merchantList.value.findIndex(item => item.shopId === row.shopId)
    if (index > -1) {
      merchantList.value[index].status = 'open'
    }
  } catch {
    // 用户取消
  }
}

// 搜索商家
const searchMerchants = () => {
  ElMessage.info('搜索功能待实现')
}

// 重置筛选
const resetFilter = () => {
  Object.assign(filterForm, {
    shopName: '',
    shopId: '',
    status: '',
    rating: ''
  })
}

// 导出数据
const exportData = () => {
  ElMessage.success('数据导出功能待实现')
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
  pagination.total = merchantList.value.length
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

.status-management {
  margin-bottom: 20px;
}

.status-card {
  .status-item {
    display: flex;
    align-items: center;
    padding: 20px;
    
    .status-icon {
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
      
      &.open {
        background: #67c23a;
      }
      
      &.closed {
        background: #909399;
      }
      
      &.suspended {
        background: #e6a23c;
      }
      
      &.frozen {
        background: #f56c6c;
      }
    }
    
    .status-info {
      flex: 1;
      
      .status-count {
        font-size: 24px;
        font-weight: bold;
        color: #333;
        margin-bottom: 5px;
      }
      
      .status-label {
        font-size: 14px;
        color: #666;
      }
    }
  }
}

.merchant-detail {
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
  
  .data-item {
    text-align: center;
    padding: 20px;
    background: #f5f5f5;
    border-radius: 4px;
    
    .data-value {
      font-size: 24px;
      font-weight: bold;
      color: #409eff;
      margin-bottom: 5px;
    }
    
    .data-label {
      font-size: 14px;
      color: #666;
    }
  }
}

.merchant-info {
  p {
    margin: 5px 0;
    color: #666;
  }
}

.dialog-footer {
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .status-card .status-item {
    flex-direction: column;
    text-align: center;
    
    .status-icon {
      margin-right: 0;
      margin-bottom: 10px;
    }
  }
  
  .merchant-detail .info-item {
    flex-direction: column;
    
    label {
      width: auto;
      margin-bottom: 5px;
    }
  }
}
</style>
