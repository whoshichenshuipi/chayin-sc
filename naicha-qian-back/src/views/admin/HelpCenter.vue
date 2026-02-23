<template>
  <div class="help-center">
    <div class="page-header">
      <h2>帮助中心</h2>
      <div class="header-actions">
        <el-button type="primary" :icon="Document" @click="showFeedbackForm = true">
          问题反馈
        </el-button>
        <el-button :icon="Download" @click="downloadGuide">
          下载指南
        </el-button>
      </div>
    </div>

    <!-- 操作指南分类 -->
    <el-card class="guide-card">
      <template #header>
        <div class="card-header">
          <span>操作指南</span>
          <el-tag type="info">共 {{ totalGuideCount }} 个指南</el-tag>
        </div>
      </template>

      <div class="guide-categories">
        <div 
          v-for="category in guideCategories" 
          :key="category.id"
          class="guide-category"
        >
          <div class="category-header" @click="toggleCategory(category.id)">
            <div class="category-info">
              <el-icon class="category-icon">
                <component :is="category.icon" />
              </el-icon>
              <div class="category-details">
                <h3>{{ category.name }}</h3>
                <p>{{ category.description }}</p>
              </div>
            </div>
            <div class="category-meta">
              <span class="guide-count">{{ category.guides.length }} 个指南</span>
              <el-icon class="expand-icon" :class="{ 'expanded': expandedCategories.includes(category.id) }">
                <ArrowDown />
              </el-icon>
            </div>
          </div>

          <el-collapse-transition>
            <div v-show="expandedCategories.includes(category.id)" class="category-guides">
              <div 
                v-for="guide in category.guides" 
                :key="guide.id"
                class="guide-item"
                @click="openGuide(guide)"
              >
                <div class="guide-info">
                  <h4>{{ guide.title }}</h4>
                  <p>{{ guide.description }}</p>
                  <div class="guide-meta">
                    <el-tag size="small" :type="getDifficultyTag(guide.difficulty)">
                      {{ getDifficultyText(guide.difficulty) }}
                    </el-tag>
                    <span class="guide-duration">{{ guide.duration }}</span>
                    <span class="guide-updated">更新于 {{ guide.updatedAt }}</span>
                  </div>
                </div>
                <div class="guide-actions">
                  <el-button size="small" @click.stop="previewGuide(guide)">
                    预览
                  </el-button>
                  <el-button size="small" type="primary" @click.stop="downloadGuide(guide)">
                    下载
                  </el-button>
                </div>
              </div>
            </div>
          </el-collapse-transition>
        </div>
      </div>
    </el-card>

    <!-- 常见问题 -->
    <el-card class="faq-card">
      <template #header>
        <div class="card-header">
          <span>常见问题</span>
          <el-input
            v-model="faqSearchKeyword"
            placeholder="搜索问题..."
            :prefix-icon="Search"
            @input="handleFaqSearch"
            clearable
            style="width: 300px;"
          />
        </div>
      </template>

      <div class="faq-list">
        <div 
          v-for="faq in filteredFaqs" 
          :key="faq.id"
          class="faq-item"
        >
          <div class="faq-question" @click="toggleFaq(faq.id)">
            <h4>{{ faq.question }}</h4>
            <el-icon class="expand-icon" :class="{ 'expanded': expandedFaqs.includes(faq.id) }">
              <ArrowDown />
            </el-icon>
          </div>
          <el-collapse-transition>
            <div v-show="expandedFaqs.includes(faq.id)" class="faq-answer">
              <div class="answer-content" v-html="faq.answer"></div>
              <div class="answer-actions">
                <el-button 
                  size="small" 
                  :type="faq.isHelpful ? 'success' : 'default'"
                  @click="toggleHelpful(faq)"
                >
                  {{ faq.isHelpful ? '已点赞' : '有用' }}
                </el-button>
                <el-button size="small" @click="reportFaq(faq)">
                  举报
                </el-button>
              </div>
            </div>
          </el-collapse-transition>
        </div>
      </div>
    </el-card>

    <!-- 问题反馈 -->
    <el-card class="feedback-card">
      <template #header>
        <span>问题反馈</span>
      </template>
      
      <div class="feedback-stats">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-number">{{ feedbackStats.total }}</div>
              <div class="stat-label">总反馈数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-number">{{ feedbackStats.pending }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-number">{{ feedbackStats.processing }}</div>
              <div class="stat-label">处理中</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-number">{{ feedbackStats.completed }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <div class="feedback-list">
        <div 
          v-for="feedback in feedbackList" 
          :key="feedback.id"
          class="feedback-item"
          :class="{ 'urgent': feedback.priority === 'urgent' }"
        >
          <div class="feedback-header">
            <div class="feedback-info">
              <h4>{{ feedback.title }}</h4>
              <div class="feedback-meta">
                <el-tag :type="getFeedbackTypeTag(feedback.type)">
                  {{ getFeedbackTypeText(feedback.type) }}
                </el-tag>
                <el-tag :type="getStatusTag(feedback.status)">
                  {{ getStatusText(feedback.status) }}
                </el-tag>
                <span class="feedback-time">{{ formatTime(feedback.createdAt) }}</span>
              </div>
            </div>
            <div class="feedback-actions">
              <el-button size="small" @click="viewFeedback(feedback)">
                查看详情
              </el-button>
              <el-button 
                v-if="feedback.status === 'pending'"
                size="small" 
                type="primary" 
                @click="assignFeedback(feedback)"
              >
                分配处理
              </el-button>
            </div>
          </div>
          <p class="feedback-description">{{ feedback.description }}</p>
        </div>
      </div>
    </el-card>

    <!-- 问题反馈表单对话框 -->
    <el-dialog
      v-model="showFeedbackForm"
      title="提交问题反馈"
      width="600px"
    >
      <el-form
        ref="feedbackFormRef"
        :model="feedbackForm"
        :rules="feedbackRules"
        label-width="100px"
      >
        <el-form-item label="反馈类型" prop="type">
          <el-select v-model="feedbackForm.type" placeholder="请选择反馈类型">
            <el-option label="功能Bug" value="bug" />
            <el-option label="体验建议" value="suggestion" />
            <el-option label="需求申请" value="requirement" />
            <el-option label="其他问题" value="other" />
          </el-select>
        </el-form-item>

        <el-form-item label="问题标题" prop="title">
          <el-input
            v-model="feedbackForm.title"
            placeholder="请简要描述问题"
          />
        </el-form-item>

        <el-form-item label="问题描述" prop="description">
          <el-input
            v-model="feedbackForm.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述遇到的问题..."
          />
        </el-form-item>

        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="feedbackForm.priority">
            <el-radio label="normal">普通</el-radio>
            <el-radio label="important">重要</el-radio>
            <el-radio label="urgent">紧急</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="上传截图">
          <el-upload
            v-model:file-list="feedbackForm.attachments"
            action="#"
            :auto-upload="false"
            :limit="5"
            accept="image/*"
          >
            <el-button>选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持 JPG、PNG 格式，最多上传5张图片
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showFeedbackForm = false">取消</el-button>
        <el-button type="primary" @click="submitFeedback">提交反馈</el-button>
      </template>
    </el-dialog>

    <!-- 指南预览对话框 -->
    <el-dialog
      v-model="showGuidePreview"
      :title="previewGuide.title"
      width="800px"
    >
      <div class="guide-preview">
        <div class="preview-content" v-html="previewGuide.content"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Document, 
  Download, 
  Search, 
  ArrowDown,
  User,
  Shop,
  Tools,
  DataAnalysis,
  Setting
} from '@element-plus/icons-vue'

// 响应式数据
const showFeedbackForm = ref(false)
const showGuidePreview = ref(false)
const expandedCategories = ref([])
const expandedFaqs = ref([])
const faqSearchKeyword = ref('')

// 反馈表单
const feedbackForm = reactive({
  type: '',
  title: '',
  description: '',
  priority: 'normal',
  attachments: []
})

const feedbackRules = {
  type: [
    { required: true, message: '请选择反馈类型', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入问题标题', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请描述问题', trigger: 'blur' },
    { min: 10, message: '问题描述至少10个字符', trigger: 'blur' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ]
}

// 指南数据
const guideCategories = ref([
  {
    id: 'admin',
    name: '管理员操作指南',
    description: '管理员后台功能操作指南',
    icon: User,
    guides: [
      {
        id: 1,
        title: '商家审核操作指南',
        description: '详细介绍商家入驻申请的审核流程和操作步骤',
        difficulty: 'easy',
        duration: '5分钟',
        updatedAt: '2023-12-01',
        content: '<h3>商家审核操作指南</h3><p>1. 进入商家审核页面</p><p>2. 查看商家提交的资质材料</p><p>3. 核实商家信息真实性</p><p>4. 做出审核决定</p>'
      },
      {
        id: 2,
        title: '用户管理操作指南',
        description: '用户信息查看、编辑、禁用等操作说明',
        difficulty: 'medium',
        duration: '8分钟',
        updatedAt: '2023-11-30',
        content: '<h3>用户管理操作指南</h3><p>1. 查看用户列表</p><p>2. 搜索和筛选用户</p><p>3. 编辑用户信息</p><p>4. 禁用/启用用户账号</p>'
      }
    ]
  },
  {
    id: 'merchant',
    name: '商家操作指南',
    description: '商家后台功能操作指南',
    icon: Shop,
    guides: [
      {
        id: 3,
        title: '商品上架操作指南',
        description: '商品信息填写、图片上传、分类设置等操作步骤',
        difficulty: 'easy',
        duration: '6分钟',
        updatedAt: '2023-12-01',
        content: '<h3>商品上架操作指南</h3><p>1. 进入商品管理页面</p><p>2. 点击添加商品</p><p>3. 填写商品信息</p><p>4. 上传商品图片</p><p>5. 设置商品分类</p><p>6. 提交审核</p>'
      },
      {
        id: 4,
        title: '订单处理操作指南',
        description: '订单查看、接单、发货、退款等操作说明',
        difficulty: 'medium',
        duration: '10分钟',
        updatedAt: '2023-11-30',
        content: '<h3>订单处理操作指南</h3><p>1. 查看新订单</p><p>2. 确认订单信息</p><p>3. 接单处理</p><p>4. 安排发货</p><p>5. 更新物流信息</p>'
      }
    ]
  },
  {
    id: 'system',
    name: '系统管理指南',
    description: '系统配置、维护、监控等操作指南',
    icon: Setting,
    guides: [
      {
        id: 5,
        title: '系统配置操作指南',
        description: '系统参数配置、支付设置、短信配置等操作说明',
        difficulty: 'hard',
        duration: '15分钟',
        updatedAt: '2023-11-29',
        content: '<h3>系统配置操作指南</h3><p>1. 进入系统配置页面</p><p>2. 配置基础参数</p><p>3. 设置支付方式</p><p>4. 配置短信服务</p><p>5. 保存配置</p>'
      }
    ]
  }
])

// FAQ 数据
const faqs = ref([
  {
    id: 1,
    question: '商家审核时无法查看资质照片怎么办？',
    answer: '如果无法查看资质照片，请检查：<br>1. 网络连接是否正常<br>2. 浏览器是否支持图片显示<br>3. 图片文件是否损坏<br>4. 联系技术支持处理',
    isHelpful: false
  },
  {
    id: 2,
    question: '商品编辑页面无法保存怎么办？',
    answer: '商品编辑无法保存的解决方法：<br>1. 检查必填字段是否完整<br>2. 确认图片上传是否成功<br>3. 清除浏览器缓存<br>4. 重新登录系统',
    isHelpful: false
  },
  {
    id: 3,
    question: '数据统计图表显示异常怎么办？',
    answer: '数据统计图表异常处理：<br>1. 刷新页面重新加载<br>2. 检查数据时间范围<br>3. 确认数据源连接<br>4. 联系技术支持',
    isHelpful: false
  }
])

// 反馈数据
const feedbackStats = ref({
  total: 25,
  pending: 8,
  processing: 5,
  completed: 12
})

const feedbackList = ref([
  {
    id: 1,
    type: 'bug',
    title: '商品编辑页面无法保存',
    description: '在商品编辑页面填写完信息后，点击保存按钮没有反应，页面也没有任何提示。',
    status: 'pending',
    priority: 'important',
    createdAt: '2023-12-01 14:30:00'
  },
  {
    id: 2,
    type: 'suggestion',
    title: '建议增加批量操作功能',
    description: '希望能够在商品列表页面增加批量上架、下架、删除等功能，提高操作效率。',
    status: 'processing',
    priority: 'normal',
    createdAt: '2023-11-30 10:20:00'
  },
  {
    id: 3,
    type: 'requirement',
    title: '需要增加数据导出功能',
    description: '希望能够将订单数据、销售数据等导出为Excel格式，方便进行数据分析。',
    status: 'completed',
    priority: 'normal',
    createdAt: '2023-11-29 16:45:00'
  }
])

// 计算属性
const totalGuideCount = computed(() => {
  return guideCategories.value.reduce((total, category) => total + category.guides.length, 0)
})

const filteredFaqs = computed(() => {
  if (!faqSearchKeyword.value) return faqs.value
  
  const keyword = faqSearchKeyword.value.toLowerCase()
  return faqs.value.filter(faq => 
    faq.question.toLowerCase().includes(keyword) || 
    faq.answer.toLowerCase().includes(keyword)
  )
})

// 方法
const toggleCategory = (categoryId) => {
  const index = expandedCategories.value.indexOf(categoryId)
  if (index > -1) {
    expandedCategories.value.splice(index, 1)
  } else {
    expandedCategories.value.push(categoryId)
  }
}

const toggleFaq = (faqId) => {
  const index = expandedFaqs.value.indexOf(faqId)
  if (index > -1) {
    expandedFaqs.value.splice(index, 1)
  } else {
    expandedFaqs.value.push(faqId)
  }
}

const getDifficultyText = (difficulty) => {
  const textMap = {
    easy: '简单',
    medium: '中等',
    hard: '困难'
  }
  return textMap[difficulty] || '简单'
}

const getDifficultyTag = (difficulty) => {
  const tagMap = {
    easy: 'success',
    medium: 'warning',
    hard: 'danger'
  }
  return tagMap[difficulty] || 'success'
}

const openGuide = (guide) => {
  previewGuide.value = guide
  showGuidePreview.value = true
}

const previewGuide = (guide) => {
  previewGuide.value = guide
  showGuidePreview.value = true
}

const downloadGuide = (guide) => {
  ElMessage.success(`正在下载: ${guide.title}`)
  // 这里实现实际的下载逻辑
}

const handleFaqSearch = () => {
  // 搜索逻辑已在计算属性中处理
}

const toggleHelpful = (faq) => {
  faq.isHelpful = !faq.isHelpful
  ElMessage.success(faq.isHelpful ? '感谢您的反馈！' : '已取消点赞')
}

const reportFaq = (faq) => {
  ElMessageBox.confirm('确定要举报这个问题吗？', '举报确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('举报已提交，我们会尽快处理')
  })
}

const getFeedbackTypeText = (type) => {
  const textMap = {
    bug: '功能Bug',
    suggestion: '体验建议',
    requirement: '需求申请',
    other: '其他问题'
  }
  return textMap[type] || '未知'
}

const getFeedbackTypeTag = (type) => {
  const tagMap = {
    bug: 'danger',
    suggestion: 'warning',
    requirement: 'success',
    other: 'info'
  }
  return tagMap[type] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    pending: '待处理',
    processing: '处理中',
    completed: '已完成'
  }
  return textMap[status] || '未知'
}

const getStatusTag = (status) => {
  const tagMap = {
    pending: 'warning',
    processing: 'primary',
    completed: 'success'
  }
  return tagMap[status] || 'info'
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

const viewFeedback = (feedback) => {
  ElMessage.info(`查看反馈详情: ${feedback.title}`)
}

const assignFeedback = (feedback) => {
  ElMessage.success(`已分配处理: ${feedback.title}`)
  feedback.status = 'processing'
}

const submitFeedback = () => {
  ElMessage.success('反馈已提交，我们会尽快处理')
  showFeedbackForm.value = false
  // 重置表单
  Object.assign(feedbackForm, {
    type: '',
    title: '',
    description: '',
    priority: 'normal',
    attachments: []
  })
}

onMounted(() => {
  // 初始化展开第一个分类
  if (guideCategories.value.length > 0) {
    expandedCategories.value.push(guideCategories.value[0].id)
  }
})
</script>

<style scoped>
.help-center {
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

.guide-card, .faq-card, .feedback-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.guide-categories {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.guide-category {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}

.category-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  cursor: pointer;
  transition: all 0.3s;
}

.category-header:hover {
  background: #e9ecef;
}

.category-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.category-icon {
  font-size: 24px;
  color: #409eff;
}

.category-details h3 {
  margin: 0 0 4px;
  font-size: 18px;
  color: #333;
}

.category-details p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.category-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.guide-count {
  font-size: 14px;
  color: #666;
}

.expand-icon {
  transition: transform 0.3s;
}

.expand-icon.expanded {
  transform: rotate(180deg);
}

.category-guides {
  padding: 0 20px 20px;
}

.guide-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s;
}

.guide-item:hover {
  background: #f8f9fa;
}

.guide-item:last-child {
  border-bottom: none;
}

.guide-info h4 {
  margin: 0 0 8px;
  color: #333;
}

.guide-info p {
  margin: 0 0 8px;
  color: #666;
  font-size: 14px;
}

.guide-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #999;
}

.guide-actions {
  display: flex;
  gap: 8px;
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.faq-item {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  overflow: hidden;
}

.faq-question {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  cursor: pointer;
  background: #f8f9fa;
}

.faq-question h4 {
  margin: 0;
  color: #333;
}

.faq-answer {
  padding: 0 16px 16px;
}

.answer-content {
  margin-bottom: 12px;
  line-height: 1.6;
  color: #666;
}

.answer-actions {
  display: flex;
  gap: 8px;
}

.feedback-stats {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.feedback-item {
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  transition: all 0.3s;
}

.feedback-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.feedback-item.urgent {
  border-left: 4px solid #f56c6c;
}

.feedback-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.feedback-info h4 {
  margin: 0 0 8px;
  color: #333;
}

.feedback-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.feedback-time {
  color: #999;
}

.feedback-actions {
  display: flex;
  gap: 8px;
}

.feedback-description {
  margin: 0;
  color: #666;
  line-height: 1.5;
}

.guide-preview {
  max-height: 500px;
  overflow-y: auto;
}

.preview-content {
  line-height: 1.6;
  color: #333;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .category-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .category-meta {
    align-self: stretch;
    justify-content: space-between;
  }

  .guide-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .feedback-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
