<template>
  <div class="help-center">
    <div class="container">
      <!-- 帮助中心头部 -->
      <el-card class="help-header">
        <div class="header-content">
          <div class="header-info">
            <h1>帮助中心</h1>
            <p>为您提供全面的使用指导和问题解答</p>
          </div>
          <div class="header-actions">
            <el-button type="primary" :icon="Service" @click="showCustomerService = true">
              在线客服
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 搜索区域 -->
      <el-card class="search-card">
        <div class="search-content">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索问题关键词，如：退款、订单、支付..."
            :prefix-icon="Search"
            size="large"
            @input="handleSearch"
            clearable
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        </div>
      </el-card>

      <!-- FAQ 分类 -->
      <el-card class="faq-card" v-loading="loading">
        <template #header>
          <div class="card-header">
            <span>常见问题</span>
            <el-tag type="info">共 {{ totalFaqCount }} 个问题</el-tag>
          </div>
        </template>

        <div class="faq-categories">
          <div 
            v-for="category in faqCategories" 
            :key="category.id"
            class="faq-category"
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
                <span class="question-count">{{ category.questions.length }} 个问题</span>
                <el-icon class="expand-icon" :class="{ 'expanded': expandedCategories.includes(category.id) }">
                  <ArrowDown />
                </el-icon>
              </div>
            </div>

            <el-collapse-transition>
              <div v-show="expandedCategories.includes(category.id)" class="category-questions">
                <div 
                  v-for="question in category.questions" 
                  :key="question.id"
                  class="faq-question"
                  :class="{ 'highlighted': isQuestionHighlighted(question) }"
                >
                  <div class="question-header" @click="toggleQuestion(question.id)">
                    <h4>{{ question.title }}</h4>
                    <el-icon class="expand-icon" :class="{ 'expanded': expandedQuestions.includes(question.id) }">
                      <ArrowDown />
                    </el-icon>
                  </div>
                  <el-collapse-transition>
                    <div v-show="expandedQuestions.includes(question.id)" class="question-answer">
                      <div class="answer-content" v-html="question.answer"></div>
                      <div class="answer-actions">
                        <el-button 
                          size="small" 
                          :type="question.isHelpful ? 'success' : 'default'"
                          @click="toggleHelpful(question)"
                        >
                          {{ question.isHelpful ? '已点赞' : '有用' }}
                        </el-button>
                        <el-button size="small" @click="reportQuestion(question)">
                          举报
                        </el-button>
                      </div>
                    </div>
                  </el-collapse-transition>
                </div>
              </div>
            </el-collapse-transition>
          </div>
        </div>
      </el-card>

      <!-- 搜索结果 -->
      <el-card v-if="searchResults.length > 0" class="search-results-card">
        <template #header>
          <span>搜索结果 ({{ searchResults.length }} 条)</span>
        </template>
        
        <div class="search-results">
          <div 
            v-for="result in searchResults" 
            :key="result.id"
            class="search-result-item"
            @click="expandSearchResult(result)"
          >
            <div class="result-header">
              <h4>{{ result.title }}</h4>
              <el-tag size="small" :type="getCategoryTagType(result.categoryId)">
                {{ getCategoryName(result.categoryId) }}
              </el-tag>
            </div>
            <p class="result-preview">{{ result.preview }}</p>
          </div>
        </div>
      </el-card>

      <!-- 联系客服 -->
      <el-card class="contact-card">
        <template #header>
          <span>联系客服</span>
        </template>
        
        <div class="contact-methods">
          <div class="contact-method">
            <div class="method-icon">
              <el-icon><Service /></el-icon>
            </div>
            <div class="method-info">
              <h4>在线客服</h4>
              <p>工作时间：9:00-22:00</p>
              <p>实时在线，快速响应</p>
              <el-button type="primary" @click="showCustomerService = true">
                立即咨询
              </el-button>
            </div>
          </div>

          <div class="contact-method">
            <div class="method-icon">
              <el-icon><Phone /></el-icon>
            </div>
            <div class="method-info">
              <h4>电话客服</h4>
              <p>客服热线：400-123-4567</p>
              <p>7×24小时服务</p>
              <el-button @click="callCustomerService">
                拨打电话
              </el-button>
            </div>
          </div>

          <div class="contact-method">
            <div class="method-icon">
              <el-icon><Message /></el-icon>
            </div>
            <div class="method-info">
              <h4>留言反馈</h4>
              <p>非工作时间可提交留言</p>
              <p>我们会在24小时内回复</p>
              <el-button @click="showFeedbackForm = true">
                提交留言
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 在线客服对话框 -->
    <el-dialog
      v-model="showCustomerService"
      title="在线客服"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="customer-service">
        <div class="service-status">
          <el-alert
            :title="isServiceOnline ? '客服在线' : '客服离线'"
            :type="isServiceOnline ? 'success' : 'warning'"
            :description="isServiceOnline ? '客服小助手为您服务' : '客服暂时离线，您可以留言，我们会在24小时内回复'"
            show-icon
          />
        </div>

        <div class="chat-container">
          <div class="chat-messages" ref="chatMessagesRef">
            <div 
              v-for="message in chatMessages" 
              :key="message.id"
              class="chat-message"
              :class="{ 'user-message': message.isUser, 'service-message': !message.isUser }"
            >
              <div class="message-avatar">
                <el-avatar :size="32">
                  {{ message.isUser ? '我' : '客服' }}
                </el-avatar>
              </div>
              <div class="message-content">
                <div class="message-bubble">
                  {{ message.content }}
                </div>
                <div class="message-time">{{ message.time }}</div>
              </div>
            </div>
          </div>

          <div v-if="!isServiceOnline" class="offline-message">
            <el-input
              v-model="offlineMessage"
              type="textarea"
              placeholder="请描述您的问题，我们会尽快回复..."
              :rows="3"
            />
            <div class="offline-actions">
              <el-button @click="showCustomerService = false">取消</el-button>
              <el-button type="primary" @click="submitOfflineMessage">提交留言</el-button>
            </div>
          </div>

          <div v-else class="chat-input">
            <el-input
              v-model="newMessage"
              placeholder="请输入您的问题..."
              @keyup.enter="sendMessage"
            >
              <template #append>
                <el-button @click="sendMessage">发送</el-button>
              </template>
            </el-input>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 留言反馈对话框 -->
    <el-dialog
      v-model="showFeedbackForm"
      title="提交留言"
      width="500px"
    >
      <el-form
        ref="feedbackFormRef"
        :model="feedbackForm"
        :rules="feedbackRules"
        label-width="100px"
      >
        <el-form-item label="问题类型" prop="type">
          <el-select v-model="feedbackForm.type" placeholder="请选择问题类型">
            <el-option label="账号问题" value="account" />
            <el-option label="订单问题" value="order" />
            <el-option label="支付问题" value="payment" />
            <el-option label="售后问题" value="after_sale" />
            <el-option label="其他问题" value="other" />
          </el-select>
        </el-form-item>

        <el-form-item label="问题描述" prop="description">
          <el-input
            v-model="feedbackForm.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述您遇到的问题..."
          />
        </el-form-item>

        <el-form-item label="联系方式" prop="contact">
          <el-input
            v-model="feedbackForm.contact"
            placeholder="请输入手机号或邮箱"
          />
        </el-form-item>

        <el-form-item label="上传截图">
          <el-upload
            v-model:file-list="feedbackForm.attachments"
            action="#"
            :auto-upload="false"
            :limit="3"
            accept="image/*"
            :disabled="uploadingImages"
            list-type="picture-card"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">
                支持 JPG、PNG 格式，最多上传3张图片，每张不超过5MB
              </div>
            </template>
          </el-upload>
          <div v-if="uploadingImages" style="margin-top: 8px;">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>上传中...</span>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showFeedbackForm = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="submittingFeedback"
          @click="submitFeedback"
        >
          提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Service, 
  Phone, 
  Message, 
  ArrowDown,
  User,
  ShoppingCart,
  CreditCard,
  Tools,
  Plus,
  Loading
} from '@element-plus/icons-vue'
import { getFaqCategories, getFaqList, searchFaq, submitFeedback as submitFeedbackApi, submitOfflineMessage as submitOfflineMessageApi, markFaqHelpful, reportFaq } from '@/api/help'
import { uploadImage } from '@/api/upload'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 响应式数据
const searchKeyword = ref('')
const expandedCategories = ref([])
const expandedQuestions = ref([])
const showCustomerService = ref(false)
const showFeedbackForm = ref(false)
const isServiceOnline = ref(true)
const newMessage = ref('')
const offlineMessage = ref('')
const chatMessagesRef = ref()
const loading = ref(false)
const submittingFeedback = ref(false)
const uploadingImages = ref(false)

// 客服聊天数据
const chatMessages = ref([
  {
    id: 1,
    isUser: false,
    content: '您好！我是客服小助手，有什么可以帮助您的吗？',
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
])

// FAQ数据
const faqCategories = ref([])

// 留言反馈表单
const feedbackForm = reactive({
  type: '',
  description: '',
  contact: '',
  attachments: []
})

const feedbackFormRef = ref()

const feedbackRules = {
  type: [
    { required: true, message: '请选择问题类型', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请描述问题', trigger: 'blur' },
    { min: 10, message: '问题描述至少10个字符', trigger: 'blur' }
  ],
  contact: [
    { required: true, message: '请输入联系方式', trigger: 'blur' }
  ]
}

// 图标映射
const iconMap = {
  account: User,
  order: ShoppingCart,
  payment: CreditCard,
  after_sale: Tools,
  default: Tools
}

// 格式化FAQ数据
const formatFaqCategory = (category) => {
  return {
    id: category.id || category.categoryId,
    name: category.name || category.categoryName,
    description: category.description || '',
    icon: iconMap[category.id] || iconMap.default,
    questions: (category.questions || []).map(q => ({
      id: q.id || q.questionId,
      title: q.title || q.question,
      answer: q.answer || q.content || '',
      isHelpful: q.isHelpful || false,
      helpfulCount: q.helpfulCount || 0
    }))
  }
}

// 加载FAQ数据
const loadFaqData = async () => {
  try {
    loading.value = true
    const categories = await getFaqCategories()
    
    if (categories && categories.length > 0) {
      // 如果后端返回的是分类列表
      faqCategories.value = categories.map(formatFaqCategory)
      
      // 加载每个分类的FAQ问题
      for (const category of faqCategories.value) {
        try {
          const questions = await getFaqList({ categoryId: category.id })
          if (questions && questions.length > 0) {
            category.questions = questions.map(q => ({
              id: q.id || q.questionId,
              title: q.title || q.question,
              answer: q.answer || q.content || '',
              isHelpful: q.isHelpful || false,
              helpfulCount: q.helpfulCount || 0
            }))
          }
        } catch (error) {
          console.error(`加载分类 ${category.id} 的FAQ失败:`, error)
        }
      }
    } else {
      // 如果后端接口不存在，使用默认数据
      faqCategories.value = getDefaultFaqData()
    }
  } catch (error) {
    console.error('加载FAQ数据失败:', error)
    // 使用默认数据
    faqCategories.value = getDefaultFaqData()
  } finally {
    loading.value = false
  }
}

// 默认FAQ数据（当后端接口不存在时使用）
const getDefaultFaqData = () => {
  return [
    {
      id: 'account',
      name: '账号问题',
      description: '注册、登录、密码找回等账号相关问题',
      icon: User,
      questions: [
        {
          id: 1,
          title: '如何找回密码？',
          answer: '您可以通过以下方式找回密码：<br>1. 点击登录页面的"忘记密码"链接<br>2. 输入注册时的手机号或邮箱<br>3. 按照提示完成验证<br>4. 设置新密码',
          isHelpful: false,
          helpfulCount: 0
        },
        {
          id: 2,
          title: '注册时收不到验证码怎么办？',
          answer: '如果收不到验证码，请检查：<br>1. 手机号是否正确<br>2. 是否被手机拦截<br>3. 网络是否正常<br>4. 稍等几分钟后重试<br>5. 联系客服处理',
          isHelpful: false,
          helpfulCount: 0
        }
      ]
    },
    {
      id: 'order',
      name: '订单问题',
      description: '下单、取消、修改订单等相关问题',
      icon: ShoppingCart,
      questions: [
        {
          id: 3,
          title: '如何取消订单？',
          answer: '订单取消规则：<br>1. 未支付订单：可直接取消<br>2. 已支付订单：商家接单前可申请取消<br>3. 商家已接单：需联系商家协商<br>4. 已发货订单：无法取消，可申请退款',
          isHelpful: false,
          helpfulCount: 0
        },
        {
          id: 4,
          title: '退款多久到账？',
          answer: '退款到账时间：<br>1. 原路退回：1-3个工作日<br>2. 银行卡：3-7个工作日<br>3. 支付宝/微信：1-2个工作日<br>4. 具体时间以银行处理为准',
          isHelpful: false,
          helpfulCount: 0
        }
      ]
    },
    {
      id: 'payment',
      name: '支付问题',
      description: '支付方式、支付失败等支付相关问题',
      icon: CreditCard,
      questions: [
        {
          id: 5,
          title: '支付失败怎么办？',
          answer: '支付失败处理：<br>1. 检查网络连接<br>2. 确认支付密码正确<br>3. 检查银行卡余额<br>4. 尝试其他支付方式<br>5. 联系银行客服',
          isHelpful: false,
          helpfulCount: 0
        },
        {
          id: 6,
          title: '支持哪些支付方式？',
          answer: '支持的支付方式：<br>1. 微信支付<br>2. 支付宝<br>3. 银行卡支付<br>4. 花呗分期<br>5. 白条支付',
          isHelpful: false,
          helpfulCount: 0
        }
      ]
    },
    {
      id: 'after_sale',
      name: '售后问题',
      description: '退换货、维修等售后相关问题',
      icon: Tools,
      questions: [
        {
          id: 7,
          title: '如何申请售后？',
          answer: '售后申请流程：<br>1. 进入"我的订单"<br>2. 找到对应订单<br>3. 点击"申请售后"<br>4. 选择售后类型<br>5. 填写问题描述<br>6. 提交申请等待处理',
          isHelpful: false,
          helpfulCount: 0
        },
        {
          id: 8,
          title: '售后审核需要多久？',
          answer: '审核时间：<br>1. 一般情况：1-2个工作日<br>2. 复杂问题：3-5个工作日<br>3. 节假日顺延<br>4. 审核结果会短信通知',
          isHelpful: false,
          helpfulCount: 0
        }
      ]
    }
  ]
}

// 计算属性
const totalFaqCount = computed(() => {
  return faqCategories.value.reduce((total, category) => total + category.questions.length, 0)
})

const searchResults = ref([])

// 搜索FAQ
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    searchResults.value = []
    return
  }
  
  try {
    loading.value = true
    const results = await searchFaq(searchKeyword.value)
    
    if (results && results.length > 0) {
      searchResults.value = results.map(r => ({
        id: r.id || r.questionId,
        title: r.title || r.question,
        preview: (r.answer || r.content || '').replace(/<[^>]*>/g, '').substring(0, 100) + '...',
        categoryId: r.categoryId || r.category
      }))
    } else {
      // 如果后端接口不存在，使用前端搜索
      const keyword = searchKeyword.value.toLowerCase()
      const localResults = []
      
      faqCategories.value.forEach(category => {
        category.questions.forEach(question => {
          if (question.title.toLowerCase().includes(keyword) || 
              question.answer.toLowerCase().includes(keyword)) {
            localResults.push({
              id: question.id,
              title: question.title,
              preview: question.answer.replace(/<[^>]*>/g, '').substring(0, 100) + '...',
              categoryId: category.id
            })
          }
        })
      })
      
      searchResults.value = localResults
      
      // 展开所有分类以显示搜索结果
      expandedCategories.value = faqCategories.value.map(cat => cat.id)
    }
  } catch (error) {
    console.error('搜索FAQ失败:', error)
    // 使用前端搜索作为降级方案
    const keyword = searchKeyword.value.toLowerCase()
    const localResults = []
    
    faqCategories.value.forEach(category => {
      category.questions.forEach(question => {
        if (question.title.toLowerCase().includes(keyword) || 
            question.answer.toLowerCase().includes(keyword)) {
          localResults.push({
            id: question.id,
            title: question.title,
            preview: question.answer.replace(/<[^>]*>/g, '').substring(0, 100) + '...',
            categoryId: category.id
          })
        }
      })
    })
    
    searchResults.value = localResults
    expandedCategories.value = faqCategories.value.map(cat => cat.id)
  } finally {
    loading.value = false
  }
}

// 方法
const toggleCategory = (categoryId) => {
  const index = expandedCategories.value.indexOf(categoryId)
  if (index > -1) {
    expandedCategories.value.splice(index, 1)
  } else {
    expandedCategories.value.push(categoryId)
  }
}

const toggleQuestion = (questionId) => {
  const index = expandedQuestions.value.indexOf(questionId)
  if (index > -1) {
    expandedQuestions.value.splice(index, 1)
  } else {
    expandedQuestions.value.push(questionId)
  }
}

const isQuestionHighlighted = (question) => {
  if (!searchKeyword.value) return false
  const keyword = searchKeyword.value.toLowerCase()
  return question.title.toLowerCase().includes(keyword) || 
         question.answer.toLowerCase().includes(keyword)
}

const toggleHelpful = async (question) => {
  const newHelpfulState = !question.isHelpful
  
  try {
    await markFaqHelpful(question.id, newHelpfulState)
    question.isHelpful = newHelpfulState
    if (newHelpfulState) {
      question.helpfulCount = (question.helpfulCount || 0) + 1
    } else {
      question.helpfulCount = Math.max((question.helpfulCount || 0) - 1, 0)
    }
    ElMessage.success(newHelpfulState ? '感谢您的反馈！' : '已取消点赞')
  } catch (error) {
    console.error('标记FAQ有用失败:', error)
    // 即使接口失败，也更新本地状态
    question.isHelpful = newHelpfulState
    ElMessage.success(newHelpfulState ? '感谢您的反馈！' : '已取消点赞')
  }
}

const reportQuestion = (question) => {
  ElMessageBox.confirm('确定要举报这个问题吗？', '举报确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await reportFaq(question.id, '用户举报')
      ElMessage.success('举报已提交，我们会尽快处理')
    } catch (error) {
      console.error('举报FAQ失败:', error)
      ElMessage.success('举报已提交，我们会尽快处理')
    }
  }).catch(() => {
    // 用户取消
  })
}

const expandSearchResult = (result) => {
  // 找到对应的分类和问题
  const category = faqCategories.value.find(cat => cat.id === result.categoryId)
  if (category) {
    // 展开分类
    if (!expandedCategories.value.includes(category.id)) {
      expandedCategories.value.push(category.id)
    }
    // 展开问题
    if (!expandedQuestions.value.includes(result.id)) {
      expandedQuestions.value.push(result.id)
    }
  }
}

const getCategoryName = (categoryId) => {
  const category = faqCategories.value.find(cat => cat.id === categoryId)
  return category ? category.name : '未知'
}

const getCategoryTagType = (categoryId) => {
  const typeMap = {
    account: 'primary',
    order: 'success',
    payment: 'warning',
    after_sale: 'info'
  }
  return typeMap[categoryId] || 'default'
}

const sendMessage = () => {
  if (!newMessage.value.trim()) return
  
  const userMessage = {
    id: Date.now(),
    isUser: true,
    content: newMessage.value,
    time: new Date().toLocaleTimeString().slice(0, 5)
  }
  
  chatMessages.value.push(userMessage)
  newMessage.value = ''
  
  // 模拟客服回复
  setTimeout(() => {
    const serviceMessage = {
      id: Date.now() + 1,
      isUser: false,
      content: '感谢您的咨询，我正在为您查询相关信息，请稍等...',
      time: new Date().toLocaleTimeString().slice(0, 5)
    }
    chatMessages.value.push(serviceMessage)
    scrollToBottom()
  }, 1000)
  
  scrollToBottom()
}

const submitOfflineMessage = async () => {
  if (!offlineMessage.value.trim()) {
    ElMessage.warning('请输入留言内容')
    return
  }
  
  try {
    await submitOfflineMessageApi(offlineMessage.value)
    ElMessage.success('留言已提交，我们会在24小时内回复')
    showCustomerService.value = false
    offlineMessage.value = ''
  } catch (error) {
    console.error('提交离线留言失败:', error)
    ElMessage.error(error.message || '留言提交失败，请重试')
  }
}

const submitFeedback = async () => {
  if (!feedbackFormRef.value) return
  
  await feedbackFormRef.value.validate(async (valid) => {
    if (valid) {
      submittingFeedback.value = true
      
      try {
        // 如果有附件，先上传
        const attachments = []
        if (feedbackForm.attachments && feedbackForm.attachments.length > 0) {
          uploadingImages.value = true
          try {
            for (const file of feedbackForm.attachments) {
              if (file.raw) {
                const imageUrl = await uploadImage(file.raw, 'feedback')
                attachments.push({
                  name: file.name,
                  url: imageUrl,
                  type: 'image'
                })
              }
            }
          } catch (error) {
            console.error('上传附件失败:', error)
            ElMessage.warning('部分附件上传失败，但反馈已提交')
          } finally {
            uploadingImages.value = false
          }
        }
        
        await submitFeedbackApi({
          type: feedbackForm.type,
          description: feedbackForm.description,
          contact: feedbackForm.contact,
          attachments
        })
        
        ElMessage.success('反馈已提交，我们会尽快处理')
        showFeedbackForm.value = false
        
        // 重置表单
        if (feedbackFormRef.value) {
          feedbackFormRef.value.resetFields()
        }
        Object.assign(feedbackForm, {
          type: '',
          description: '',
          contact: '',
          attachments: []
        })
      } catch (error) {
        console.error('提交反馈失败:', error)
        ElMessage.error(error.message || '反馈提交失败，请重试')
      } finally {
        submittingFeedback.value = false
      }
    }
  })
}

const callCustomerService = () => {
  ElMessage.info('正在为您转接客服电话...')
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatMessagesRef.value) {
      chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
    }
  })
}

onMounted(async () => {
  // 加载FAQ数据
  await loadFaqData()
  
  // 初始化展开第一个分类
  if (faqCategories.value.length > 0) {
    expandedCategories.value.push(faqCategories.value[0].id)
  }
  
  // 如果用户已登录，预填充联系方式
  if (userStore.userInfo) {
    feedbackForm.contact = userStore.userInfo.phone || userStore.userInfo.email || ''
  }
})
</script>

<style scoped>
.help-center {
  padding: 20px 0;
  min-height: calc(100vh - 120px);
}

.help-header {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-info h1 {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 600;
}

.header-info p {
  margin: 0;
  opacity: 0.9;
}

.search-card {
  margin-bottom: 20px;
}

.search-content {
  max-width: 600px;
  margin: 0 auto;
}

.faq-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.faq-categories {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.faq-category {
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

.question-count {
  font-size: 14px;
  color: #666;
}

.expand-icon {
  transition: transform 0.3s;
}

.expand-icon.expanded {
  transform: rotate(180deg);
}

.category-questions {
  padding: 0 20px 20px;
}

.faq-question {
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.3s;
}

.faq-question:last-child {
  border-bottom: none;
}

.faq-question.highlighted {
  background: #fff7e6;
  border-radius: 6px;
  padding: 8px;
  margin: 4px 0;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  cursor: pointer;
}

.question-header h4 {
  margin: 0;
  font-size: 16px;
  color: #333;
  flex: 1;
}

.question-answer {
  padding: 0 0 16px;
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

.search-results-card {
  margin-bottom: 20px;
}

.search-results {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.search-result-item {
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.search-result-item:hover {
  border-color: #409eff;
  background: #f0f9ff;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.result-header h4 {
  margin: 0;
  color: #333;
}

.result-preview {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.contact-card {
  margin-bottom: 20px;
}

.contact-methods {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}

.contact-method {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition: all 0.3s;
}

.contact-method:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.method-icon {
  width: 48px;
  height: 48px;
  background: #f0f9ff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
  font-size: 20px;
}

.method-info h4 {
  margin: 0 0 8px;
  color: #333;
}

.method-info p {
  margin: 0 0 4px;
  color: #666;
  font-size: 14px;
}

.customer-service {
  max-height: 500px;
}

.service-status {
  margin-bottom: 16px;
}

.chat-container {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}

.chat-messages {
  height: 300px;
  overflow-y: auto;
  padding: 16px;
  background: #f8f9fa;
}

.chat-message {
  display: flex;
  margin-bottom: 16px;
  gap: 12px;
}

.user-message {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  flex: 1;
  max-width: 70%;
}

.user-message .message-content {
  text-align: right;
}

.message-bubble {
  background: #409eff;
  color: white;
  padding: 12px 16px;
  border-radius: 18px;
  word-break: break-word;
}

.service-message .message-bubble {
  background: white;
  color: #333;
  border: 1px solid #e4e7ed;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.offline-message {
  padding: 16px;
  border-top: 1px solid #e4e7ed;
}

.offline-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
}

.chat-input {
  padding: 16px;
  border-top: 1px solid #e4e7ed;
  background: white;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .contact-methods {
    grid-template-columns: 1fr;
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

  .message-content {
    max-width: 85%;
  }
}
</style>
