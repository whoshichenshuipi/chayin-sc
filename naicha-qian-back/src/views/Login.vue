<template>
  <div class="login-container">
    <!-- 固定背景层 -->
    <div class="background-layer"></div>
    
    <!-- 表单容器 -->
    <div class="form-container">
      <!-- 表单切换容器 -->
      <div 
        ref="formsWrapperRef"
        class="forms-wrapper"
        :class="{ 'show-register': currentView === 'register' }"
      >
        <!-- 登录表单 -->
        <div ref="loginPanelRef" class="form-panel login-panel">
          <div class="panel-content">
            <div class="panel-header">
              <h2 class="title">茶饮零售系统 - 登录</h2>
              <p class="subtitle">请选择登录身份</p>
            </div>
            
            <div class="login-tabs">
              <el-tabs v-model="activeTab" @tab-change="handleTabChange">
                <el-tab-pane label="管理员登录" name="admin">
                  <el-form
                    ref="adminFormRef"
                    :model="adminForm"
                    :rules="adminRules"
                    class="login-form"
                  >
                    <el-form-item prop="username">
                      <el-input 
                        v-model="adminForm.username" 
                        placeholder="请输入管理员用户名"
                        class="custom-input"
                        size="large"
                      >
                        <template #prefix>
                          <el-icon><User /></el-icon>
                        </template>
                      </el-input>
                    </el-form-item>
                    <el-form-item prop="password">
                      <el-input 
                        v-model="adminForm.password" 
                        type="password" 
                        placeholder="请输入密码"
                        show-password
                        class="custom-input"
                        size="large"
                      >
                        <template #prefix>
                          <el-icon><Lock /></el-icon>
                        </template>
                      </el-input>
                    </el-form-item>
                    <el-form-item>
                      <el-checkbox v-model="rememberMe" class="remember-me">
                        记住我
                      </el-checkbox>
                    </el-form-item>
                    <el-form-item>
                      <el-button 
                        type="primary" 
                        @click="handleAdminLogin" 
                        :loading="loading"
                        class="login-btn"
                        size="large"
                      >
                        管理员登录
                      </el-button>
                    </el-form-item>
                  </el-form>
                </el-tab-pane>
                
                <el-tab-pane label="商家登录" name="merchant">
                  <el-form
                    ref="merchantFormRef"
                    :model="merchantForm"
                    :rules="merchantRules"
                    class="login-form"
                  >
                    <el-form-item prop="username">
                      <el-input 
                        v-model="merchantForm.username" 
                        placeholder="请输入商家用户名"
                        class="custom-input"
                        size="large"
                      >
                        <template #prefix>
                          <el-icon><User /></el-icon>
                        </template>
                      </el-input>
                    </el-form-item>
                    <el-form-item prop="password">
                      <el-input 
                        v-model="merchantForm.password" 
                        type="password" 
                        placeholder="请输入密码"
                        show-password
                        class="custom-input"
                        size="large"
                      >
                        <template #prefix>
                          <el-icon><Lock /></el-icon>
                        </template>
                      </el-input>
                    </el-form-item>
                    <el-form-item>
                      <el-checkbox v-model="rememberMe" class="remember-me">
                        记住我
                      </el-checkbox>
                    </el-form-item>
                    <el-form-item>
                      <el-button 
                        type="primary" 
                        @click="handleMerchantLogin" 
                        :loading="loading"
                        class="login-btn"
                        size="large"
                      >
                        商家登录
                      </el-button>
                    </el-form-item>
                  </el-form>
                </el-tab-pane>
                
                <el-tab-pane label="用户登录" name="user">
                  <el-form
                    ref="userFormRef"
                    :model="userForm"
                    :rules="userRules"
                    class="login-form"
                  >
                    <el-form-item prop="username">
                      <el-input 
                        v-model="userForm.username" 
                        placeholder="请输入用户名"
                        class="custom-input"
                        size="large"
                      >
                        <template #prefix>
                          <el-icon><User /></el-icon>
                        </template>
                      </el-input>
                    </el-form-item>
                    <el-form-item prop="password">
                      <el-input 
                        v-model="userForm.password" 
                        type="password" 
                        placeholder="请输入密码"
                        show-password
                        class="custom-input"
                        size="large"
                      >
                        <template #prefix>
                          <el-icon><Lock /></el-icon>
                        </template>
                      </el-input>
                    </el-form-item>
                    <el-form-item>
                      <el-checkbox v-model="rememberMe" class="remember-me">
                        记住我
                      </el-checkbox>
                    </el-form-item>
                    <el-form-item>
                      <el-button 
                        type="primary" 
                        @click="handleUserLogin" 
                        :loading="loading"
                        class="login-btn"
                        size="large"
                      >
                        用户登录
                      </el-button>
                    </el-form-item>
                  </el-form>
                </el-tab-pane>
              </el-tabs>
            </div>
            
            <div class="switch-hint">
              <span>还没有账号？</span>
              <span class="link-text" @click="switchToRegister">立即注册</span>
            </div>
            
            <div class="login-tips">
              <p><strong>测试账号：</strong></p>
              <p>管理员：admin / 123123123</p>
              <p>商家：merchant / 123123123</p>
            </div>
          </div>
        </div>
        
        <!-- 注册表单 -->
        <div ref="registerPanelRef" class="form-panel register-panel">
          <div class="panel-content">
            <div class="panel-header">
              <h2 class="title">茶饮零售系统 - 注册</h2>
              <p class="subtitle">创建您的账号</p>
            </div>
            
            <el-form
              ref="registerFormRef"
              :model="registerForm"
              :rules="registerRules"
              class="register-form"
            >
              <!-- 角色选择 -->
              <el-form-item label="注册类型" prop="userType">
                <el-radio-group v-model="registerForm.userType" @change="handleUserTypeChange" class="user-type-group">
                  <el-radio-button label="admin">管理员</el-radio-button>
                  <el-radio-button label="merchant">商家</el-radio-button>
                  <el-radio-button label="user">普通用户</el-radio-button>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item prop="username">
                <el-input 
                  v-model="registerForm.username" 
                  placeholder="请输入用户名"
                  @blur="validateUsername"
                  class="custom-input"
                  size="large"
                >
                  <template #prefix>
                    <el-icon><User /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item prop="password">
                <el-input 
                  v-model="registerForm.password" 
                  type="password" 
                  placeholder="请输入密码"
                  show-password
                  class="custom-input"
                  size="large"
                >
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item prop="confirmPassword">
                <el-input 
                  v-model="registerForm.confirmPassword" 
                  type="password" 
                  placeholder="请再次输入密码"
                  show-password
                  class="custom-input"
                  size="large"
                >
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              
              <!-- 管理员特有字段 -->
              <template v-if="registerForm.userType === 'admin'">
                <el-form-item prop="realName">
                  <el-input 
                    v-model="registerForm.realName" 
                    placeholder="请输入真实姓名"
                    class="custom-input"
                    size="large"
                  >
                    <template #prefix>
                      <el-icon><UserFilled /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </template>
              
              <el-form-item prop="email">
                <el-input 
                  v-model="registerForm.email" 
                  placeholder="请输入邮箱"
                  @blur="validateEmail"
                  class="custom-input"
                  size="large"
                >
                  <template #prefix>
                    <el-icon><Message /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item prop="phone">
                <el-input 
                  v-model="registerForm.phone" 
                  placeholder="请输入手机号"
                  @blur="validatePhone"
                  class="custom-input"
                  size="large"
                >
                  <template #prefix>
                    <el-icon><Phone /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              
              <!-- 用户和商家昵称 -->
              <template v-if="registerForm.userType !== 'admin'">
                <el-form-item prop="nickname">
                  <el-input 
                    v-model="registerForm.nickname" 
                    placeholder="请输入昵称"
                    class="custom-input"
                    size="large"
                  >
                    <template #prefix>
                      <el-icon><Avatar /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </template>

              <!-- 商家特有字段 -->
              <template v-if="registerForm.userType === 'merchant'">
                <el-form-item prop="shopName">
                  <el-input 
                    v-model="registerForm.shopName" 
                    placeholder="请输入店铺名称"
                    @blur="validateShopName"
                    class="custom-input"
                    size="large"
                  >
                    <template #prefix>
                      <el-icon><Shop /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
                
                <el-form-item prop="contactName">
                  <el-input 
                    v-model="registerForm.contactName" 
                    placeholder="请输入联系人姓名"
                    class="custom-input"
                    size="large"
                  >
                    <template #prefix>
                      <el-icon><UserFilled /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
                
                <el-form-item prop="contactPhone">
                  <el-input 
                    v-model="registerForm.contactPhone" 
                    placeholder="请输入联系电话"
                    @blur="validatePhone"
                    class="custom-input"
                    size="large"
                  >
                    <template #prefix>
                      <el-icon><Phone /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
                
                <el-form-item prop="address">
                  <el-input 
                    v-model="registerForm.address" 
                    placeholder="请输入店铺地址"
                    class="custom-input"
                    size="large"
                  >
                    <template #prefix>
                      <el-icon><Location /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </template>
              
              <el-form-item>
                <el-button 
                  type="primary" 
                  @click="handleRegister" 
                  :loading="registerLoading"
                  class="register-btn"
                  size="large"
                >
                  创建账号
                </el-button>
              </el-form-item>
            </el-form>
            
            <div class="switch-hint">
              <span>已有账号？</span>
              <span class="link-text" @click="switchToLogin">立即登录</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { 
  User, 
  Lock, 
  UserFilled, 
  Message, 
  Phone, 
  Avatar, 
  Shop, 
  Location 
} from '@element-plus/icons-vue'
import { login, register, checkUsername, checkEmail, checkPhone, checkShopName } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('admin')
const loading = ref(false)
const rememberMe = ref(false)
const currentView = ref('login') // 'login' 或 'register'
const formsWrapperRef = ref(null)
const loginPanelRef = ref(null)
const registerPanelRef = ref(null)

// 调整容器高度
const adjustContainerHeight = () => {
  nextTick(() => {
    setTimeout(() => {
      if (!formsWrapperRef.value) return
      
      const activePanel = currentView.value === 'login' 
        ? loginPanelRef.value 
        : registerPanelRef.value
      
      if (activePanel) {
        const panelHeight = activePanel.scrollHeight || activePanel.offsetHeight
        if (panelHeight > 0) {
          formsWrapperRef.value.style.height = panelHeight + 'px'
        }
      }
    }, 100)
  })
}

// 切换视图
const switchToRegister = () => {
  currentView.value = 'register'
  updateRegisterRules()
  adjustContainerHeight()
}

const switchToLogin = () => {
  currentView.value = 'login'
  adjustContainerHeight()
}

// 监听视图变化
watch(currentView, () => {
  adjustContainerHeight()
})

// 管理员表单
const adminFormRef = ref()
const adminForm = reactive({
  username: '',
  password: ''
})

const adminRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 商家表单
const merchantFormRef = ref()
const merchantForm = reactive({
  username: '',
  password: ''
})

const merchantRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 用户表单
const userFormRef = ref()
const userForm = reactive({
  username: '',
  password: ''
})

const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 注册相关
const registerLoading = ref(false)
const registerFormRef = ref()
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  nickname: '',
  realName: '',
  userType: 'user',
  shopName: '',
  contactName: '',
  contactPhone: '',
  address: ''
})

// 监听用户类型变化，可能需要调整高度
watch(() => registerForm.userType, () => {
  if (currentView.value === 'register') {
    adjustContainerHeight()
  }
})

// 验证规则
const registerRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度必须在3-50个字符之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在6-20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  userType: [
    { required: true, message: '请选择注册类型', trigger: 'change' }
  ]
})

// 更新验证规则
const updateRegisterRules = () => {
  // 清空特定角色字段的验证规则
  delete registerRules.realName
  delete registerRules.nickname
  delete registerRules.shopName
  delete registerRules.contactName
  delete registerRules.contactPhone
  delete registerRules.address

  // 根据角色添加特定字段的验证
  if (registerForm.userType === 'admin') {
    registerRules.realName = [
      { required: true, message: '请输入真实姓名', trigger: 'blur' }
    ]
  } else if (registerForm.userType === 'merchant') {
    registerRules.nickname = [
      { max: 50, message: '昵称长度不能超过50个字符', trigger: 'blur' }
    ]
    registerRules.shopName = [
      { required: true, message: '请输入店铺名称', trigger: 'blur' }
    ]
    registerRules.contactName = [
      { required: true, message: '请输入联系人姓名', trigger: 'blur' }
    ]
    registerRules.contactPhone = [
      { required: true, message: '请输入联系电话', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '联系电话格式不正确', trigger: 'blur' }
    ]
    registerRules.address = [
      { required: true, message: '请输入店铺地址', trigger: 'blur' }
    ]
  } else {
    registerRules.nickname = [
      { required: true, message: '请输入昵称', trigger: 'blur' },
      { max: 50, message: '昵称长度不能超过50个字符', trigger: 'blur' }
    ]
  }
}

const handleTabChange = (tabName) => {
  // 清空表单
  adminForm.username = ''
  adminForm.password = ''
  merchantForm.username = ''
  merchantForm.password = ''
  userForm.username = ''
  userForm.password = ''
}

const handleAdminLogin = async () => {
  if (!adminFormRef.value) return
  
  await adminFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await login({
          username: adminForm.username,
          password: adminForm.password,
          userType: 'admin'
        })
        
        // 处理响应数据
        let userData = null
        if (response && response.data) {
          userData = response.data
        } else if (response && response.id) {
          userData = response
        } else {
          throw new Error('登录响应数据格式错误')
        }
        
        // 登录成功
        userStore.login({
          id: userData.id,
          name: userData.realName || userData.username,
          role: 'admin',
          token: 'admin_token_' + userData.id,
          permissions: ['system:config', 'merchant:manage', 'user:manage', 'data:view']
        })
        
        ElMessage.success('管理员登录成功')
        
        // 使用后端返回的跳转地址，如果没有则使用默认路径
        const redirectUrl = userData.redirectUrl || '/admin/dashboard'
        if (redirectUrl.startsWith('http')) {
          window.location.replace(redirectUrl)
        } else {
          router.push(redirectUrl)
        }
      } catch (error) {
        console.error('登录失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

const handleMerchantLogin = async () => {
  if (!merchantFormRef.value) return
  
  await merchantFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await login({
          username: merchantForm.username,
          password: merchantForm.password,
          userType: 'merchant'
        })
        
        // 处理响应数据
        let userData = null
        if (response && response.data) {
          userData = response.data
        } else if (response && response.id) {
          userData = response
        } else {
          throw new Error('登录响应数据格式错误')
        }
        
        // 登录成功
        // 商家登录时，merchantId就是id
        userStore.login({
          id: userData.id,
          name: userData.shopName || userData.username,
          role: 'merchant',
          token: 'merchant_token_' + userData.id,
          permissions: ['shop:manage', 'product:manage', 'order:manage', 'marketing:manage'],
          merchantId: userData.id || userData.merchantId // 商家ID就是登录用户的ID
        })
        
        ElMessage.success('商家登录成功')
        
        // 使用后端返回的跳转地址，如果没有则使用默认路径
        const redirectUrl = userData.redirectUrl || '/merchant/dashboard'
        if (redirectUrl.startsWith('http')) {
          window.location.replace(redirectUrl)
        } else {
          router.push(redirectUrl)
        }
      } catch (error) {
        console.error('登录失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

const handleUserLogin = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await login({
          username: userForm.username,
          password: userForm.password,
          userType: 'user'
        })
        
        console.log('登录响应完整数据:', JSON.stringify(response, null, 2)) // 调试用
        
        // 处理响应数据，兼容不同的响应格式
        // 后端返回格式：Result<UserInfoResponse> = { code: 200, message: "...", data: { ... } }
        // request.js 响应拦截器会返回整个 Result 对象（如果 code === 200）
        let userData = null
        if (response && response.data) {
          // 如果响应有 data 字段（Result 格式）
          userData = response.data
          console.log('用户数据:', JSON.stringify(userData, null, 2)) // 调试用
          console.log('跳转地址:', userData.redirectUrl) // 调试用
        } else if (response && response.id) {
          // 如果直接返回用户数据
          userData = response
          console.log('直接返回用户数据:', userData)
        } else {
          console.error('登录响应数据格式错误，完整响应:', JSON.stringify(response, null, 2))
          throw new Error('登录响应数据格式错误：响应中没有 data 字段或 id 字段')
        }
        
        if (!userData) {
          console.error('用户数据为空，响应:', JSON.stringify(response, null, 2))
          throw new Error('无法获取用户数据')
        }
        
        if (!userData.id) {
          console.error('用户ID为空，用户数据:', JSON.stringify(userData, null, 2))
          throw new Error('用户数据不完整：缺少ID')
        }
        
        // 普通用户登录成功后，跳转到前端用户界面（3000端口）
        // 将用户信息存储到 localStorage，供前端系统使用
        // 注意：不要保存到后台管理系统的 user store 中
        const userInfo = {
          id: userData.id,
          username: userData.username,
          nickname: userData.nickname || userData.username,
          email: userData.email || '',
          phone: userData.phone || '',
          avatar: userData.avatar || '',
          userType: 'user',
          birthday: userData.birthday || '',
          address: userData.address || '',
          gender: userData.gender || 0
        }
        
        // 生成 token（实际应该从后端获取，如果后端没有返回token则生成一个临时的）
        const token = userData.token || 'user_token_' + userData.id
        
        // 获取后端返回的跳转地址（如果后端有返回）
        const redirectUrl = userData.redirectUrl || 'http://localhost:3000/home'
        
        console.log('准备跳转到:', redirectUrl) // 调试用
        console.log('用户信息已准备:', JSON.stringify(userInfo, null, 2)) // 调试用
        
        // 存储到 localStorage，供前端系统读取（注意：使用不同的 key 避免冲突）
        // 确保在跳转前保存完成
        localStorage.setItem('userInfo', JSON.stringify(userInfo))
        localStorage.setItem('token', token)
        
        // 验证保存是否成功
        const savedToken = localStorage.getItem('token')
        const savedUserInfo = localStorage.getItem('userInfo')
        
        console.log('localStorage 已保存用户信息') // 调试用
        console.log('验证保存结果:', { 
          tokenSaved: !!savedToken, 
          userInfoSaved: !!savedUserInfo,
          userInfoType: savedUserInfo ? JSON.parse(savedUserInfo).userType : null
        })
        
        // 额外验证：确保数据确实保存了
        if (!savedToken || !savedUserInfo) {
          console.error('localStorage 保存失败，重试保存...')
          // 重试保存
          localStorage.setItem('userInfo', JSON.stringify(userInfo))
          localStorage.setItem('token', token)
        }
        
        // 不要保存到后台管理系统的 user store 中（普通用户）
        // 避免路由守卫拦截
        
        ElMessage.success('用户登录成功，正在跳转到用户界面...')
        
        // 在 URL 中添加 token 参数作为备用方案（防止 localStorage 跨域问题）
        const urlParams = new URLSearchParams()
        urlParams.set('token', token)
        urlParams.set('userInfo', encodeURIComponent(JSON.stringify(userInfo)))
        const redirectUrlWithParams = redirectUrl.includes('?') 
          ? `${redirectUrl}&${urlParams.toString()}`
          : `${redirectUrl}?${urlParams.toString()}`
        
        console.log('立即执行跳转，目标地址:', redirectUrlWithParams)
        
        // 使用 setTimeout 确保 localStorage 保存完成后再跳转
        // 给一些时间让浏览器完成 localStorage 的写入操作
        setTimeout(() => {
          window.location.replace(redirectUrlWithParams)
        }, 100)
      } catch (error) {
        console.error('登录失败:', error)
        ElMessage.error(error.message || '登录失败，请检查用户名和密码')
        loading.value = false
      }
    }
  })
}

const showRegisterDialog = (userType = 'user') => {
  // 重置注册表单
  Object.assign(registerForm, {
    username: '',
    password: '',
    confirmPassword: '',
    email: '',
    phone: '',
    nickname: '',
    realName: '',
    userType: userType,
    shopName: '',
    contactName: '',
    contactPhone: '',
    address: ''
  })
  
  // 更新验证规则
  updateRegisterRules()
  
  // 切换到注册视图
  switchToRegister()
}

const handleUserTypeChange = (userType) => {
  // 切换角色时清空相关字段
  if (userType === 'merchant') {
    // 商家字段清空，但保留基本信息
    registerForm.shopName = ''
    registerForm.contactName = ''
    registerForm.contactPhone = ''
    registerForm.address = ''
    registerForm.realName = ''
  } else if (userType === 'admin') {
    // 管理员字段清空
    registerForm.nickname = ''
    registerForm.shopName = ''
    registerForm.contactName = ''
    registerForm.contactPhone = ''
    registerForm.address = ''
  } else {
    // 用户字段清空
    registerForm.shopName = ''
    registerForm.contactName = ''
    registerForm.contactPhone = ''
    registerForm.address = ''
    registerForm.realName = ''
  }
  
  // 更新验证规则
  updateRegisterRules()
  
  // 清除表单验证状态
  if (registerFormRef.value) {
    registerFormRef.value.clearValidate()
  }
  
  // 调整容器高度
  adjustContainerHeight()
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      registerLoading.value = true
      try {
        const response = await register(registerForm)
        
        // 处理响应数据
        let registerUserData = null
        if (response && response.data) {
          registerUserData = response.data
        } else if (response && response.id) {
          registerUserData = response
        } else {
          throw new Error('注册响应数据格式错误')
        }
        
        // 获取后端返回的跳转地址
        const redirectUrl = registerUserData.redirectUrl
        
        ElMessage.success('注册成功')
        currentView.value = 'login' // 注册成功后切换回登录视图
        
        // 根据用户类型处理注册后的逻辑
        if (registerForm.userType === 'admin') {
          // 管理员注册后保存到 store 并跳转
          userStore.login({
            id: registerUserData.id,
            name: registerUserData.realName || registerUserData.username,
            role: 'admin',
            token: 'admin_token_' + registerUserData.id,
            permissions: ['system:config', 'merchant:manage', 'user:manage', 'data:view']
          })
          
          if (redirectUrl && redirectUrl.startsWith('http')) {
            window.location.replace(redirectUrl)
          } else {
            router.push(redirectUrl || '/admin/dashboard')
          }
        } else if (registerForm.userType === 'merchant') {
          // 商家注册后保存到 store 并跳转
          userStore.login({
            id: registerUserData.id,
            name: registerUserData.shopName || registerUserData.username,
            role: 'merchant',
            token: 'merchant_token_' + registerUserData.id,
            permissions: ['shop:manage', 'product:manage', 'order:manage', 'marketing:manage'],
            merchantId: registerUserData.id || registerUserData.merchantId // 商家ID就是注册用户的ID
          })
          
          if (redirectUrl && redirectUrl.startsWith('http')) {
            window.location.replace(redirectUrl)
          } else {
            router.push(redirectUrl || '/merchant/dashboard')
          }
        } else {
          // 普通用户注册成功后，跳转到前端用户界面（3000端口）
          const userInfo = {
            id: registerUserData.id,
            username: registerUserData.username,
            nickname: registerUserData.nickname || registerUserData.username,
            email: registerUserData.email || '',
            phone: registerUserData.phone || '',
            avatar: registerUserData.avatar || '',
            userType: 'user',
            birthday: registerUserData.birthday || '',
            address: registerUserData.address || '',
            gender: registerUserData.gender || 0
          }
          
          const token = registerUserData.token || 'user_token_' + registerUserData.id
          
          // 存储到 localStorage，供前端系统读取
          localStorage.setItem('userInfo', JSON.stringify(userInfo))
          localStorage.setItem('token', token)
          
          ElMessage.success('注册成功，正在跳转到用户界面...')
          
          // 立即执行跳转
          const finalRedirectUrl = redirectUrl || 'http://localhost:3000/home'
          console.log('注册后跳转到:', finalRedirectUrl)
          window.location.replace(finalRedirectUrl)
        }
      } catch (error) {
        console.error('注册失败:', error)
      } finally {
        registerLoading.value = false
      }
    }
  })
}

// 检查用户名是否存在
const validateUsername = async () => {
  if (!registerForm.username) return
  
  try {
    const response = await checkUsername(registerForm.username, registerForm.userType)
    if (response.data) {
      ElMessage.warning('用户名已存在')
    }
  } catch (error) {
    console.error('检查用户名失败:', error)
  }
}

// 检查邮箱是否存在
const validateEmail = async () => {
  if (!registerForm.email) return
  
  try {
    const response = await checkEmail(registerForm.email)
    if (response.data) {
      ElMessage.warning('邮箱已存在')
    }
  } catch (error) {
    console.error('检查邮箱失败:', error)
  }
}

// 检查手机号是否存在
const validatePhone = async () => {
  if (!registerForm.phone) return
  
  try {
    const response = await checkPhone(registerForm.phone)
    if (response.data) {
      ElMessage.warning('手机号已存在')
    }
  } catch (error) {
    console.error('检查手机号失败:', error)
  }
}

// 检查店铺名称是否存在
const validateShopName = async () => {
  if (!registerForm.shopName) return
  
  try {
    const response = await checkShopName(registerForm.shopName)
    if (response.data) {
      ElMessage.warning('店铺名称已存在')
    }
  } catch (error) {
    console.error('检查店铺名称失败:', error)
  }
}

// 组件挂载后调整高度
onMounted(() => {
  adjustContainerHeight()
  // 监听窗口大小变化
  window.addEventListener('resize', adjustContainerHeight)
})
</script>

<style scoped>
/* 主容器 */
.login-container {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  position: relative;
}

/* 固定背景层 */
.background-layer {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  /* 背景图片：请替换为您的图片路径，可以是本地路径或URL */
  /* 推荐使用水果茶饮主题的图片，尺寸建议 1920x1080 或更大 */
  background-image: url('https://images.unsplash.com/photo-1551024506-0bccd828d307?w=1920&h=1080&fit=crop&q=80');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  z-index: 0;
  /* 如果图片加载失败，使用渐变背景作为备用 */
  background-color: #f5f5f5;
}

.background-layer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    135deg,
    rgba(255, 107, 107, 0.85) 0%,
    rgba(238, 90, 111, 0.75) 50%,
    rgba(46, 213, 115, 0.75) 50%,
    rgba(30, 132, 73, 0.85) 100%
  );
  backdrop-filter: blur(2px);
}

/* 表单容器 */
.form-container {
  position: relative;
  z-index: 10;
  height: 100vh;
  width: 100vw;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

/* 表单切换容器 */
.forms-wrapper {
  position: relative;
  width: 100%;
  max-width: 480px;
  min-height: 600px;
  overflow: visible;
  transition: height 0.4s ease;
}

/* 表单面板 */
.form-panel {
  position: absolute;
  width: 100%;
  top: 0;
  left: 0;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.4s ease;
  will-change: transform, opacity;
}

/* 注册表单容器 - 自动高度 */
.register-panel .panel-content {
  min-height: auto;
  max-height: 85vh;
  overflow-y: auto;
}

/* 注册表单内容区域 */
.register-form {
  max-height: none;
}

/* 登录表单 - 默认显示 */
.login-panel {
  transform: translateX(0);
  opacity: 1;
  z-index: 2;
}

/* 注册表单 - 默认隐藏 */
.register-panel {
  transform: translateX(100%);
  opacity: 0;
  z-index: 1;
}

/* 切换到注册时 */
.forms-wrapper.show-register .login-panel {
  transform: translateX(-100%);
  opacity: 0;
}

.forms-wrapper.show-register .register-panel {
  transform: translateX(0);
  opacity: 1;
  z-index: 2;
}

/* 面板内容 */
.panel-content {
  width: 100%;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 24px;
  padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(255, 255, 255, 0.5);
  position: relative;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

/* 标题样式 */
.panel-header {
  text-align: center;
  margin-bottom: 36px;
}

.title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 12px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 50%, #2ed573 50%, #1e8449 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.5px;
}

.subtitle {
  color: #666;
  font-size: 15px;
  font-weight: 400;
}

/* 表单样式 */
.login-form,
.register-form {
  margin-top: 24px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 22px;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

/* 输入框样式 */
.custom-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid rgba(0, 0, 0, 0.1);
  background-color: rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  padding: 0 16px;
}

.custom-input :deep(.el-input__wrapper:hover) {
  border-color: rgba(255, 107, 107, 0.5);
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.15);
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: #ff6b6b;
  box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.1), 0 4px 12px rgba(255, 107, 107, 0.2);
}

.custom-input :deep(.el-input__inner) {
  color: #333;
  font-size: 15px;
}

.custom-input :deep(.el-input__inner::placeholder) {
  color: #999;
}

.custom-input :deep(.el-input__prefix) {
  color: #ff6b6b;
  margin-right: 8px;
}

/* 记住我复选框 */
.remember-me {
  color: #666;
}

.remember-me :deep(.el-checkbox__label) {
  color: #666;
  font-size: 14px;
}

.remember-me :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #ff6b6b;
  border-color: #ff6b6b;
}

/* 按钮样式 */
.login-btn,
.register-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: none;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  color: white;
  box-shadow: 0 4px 16px rgba(255, 107, 107, 0.4);
  letter-spacing: 0.5px;
}

.login-btn:hover,
.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(255, 107, 107, 0.5);
  background: linear-gradient(135deg, #ff5252 0%, #e53935 100%);
}

.login-btn:active,
.register-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 12px rgba(255, 107, 107, 0.4);
}

/* 切换提示 */
.switch-hint {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #666;
}

.link-text {
  color: #ff6b6b;
  font-weight: 600;
  cursor: pointer;
  margin-left: 6px;
  transition: all 0.3s ease;
  position: relative;
  display: inline-block;
}

.link-text::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: #ff6b6b;
  transition: width 0.3s ease;
}

.link-text:hover {
  color: #ee5a6f;
}

.link-text:hover::after {
  width: 100%;
}

/* 登录提示 */
.login-tips {
  margin-top: 24px;
  padding: 16px;
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.1) 0%, rgba(46, 213, 115, 0.1) 100%);
  border: 1px solid rgba(255, 107, 107, 0.2);
  border-radius: 12px;
  font-size: 13px;
  color: #666;
  line-height: 1.6;
}

.login-tips p {
  margin: 6px 0;
}

.login-tips strong {
  color: #ff6b6b;
  font-weight: 600;
}

/* Tabs 样式 */
.login-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
}

.login-tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: rgba(0, 0, 0, 0.06);
}

.login-tabs :deep(.el-tabs__item) {
  color: #666;
  font-weight: 500;
  font-size: 15px;
  padding: 0 20px;
  transition: all 0.3s ease;
}

.login-tabs :deep(.el-tabs__item.is-active) {
  color: #ff6b6b;
  font-weight: 600;
}

.login-tabs :deep(.el-tabs__active-bar) {
  background: linear-gradient(90deg, #ff6b6b 0%, #ee5a6f 100%);
  height: 3px;
}

.login-tabs :deep(.el-tabs__item:hover) {
  color: #ff6b6b;
}

/* 注册表单特殊样式 */
.user-type-group {
  width: 100%;
  display: flex;
  gap: 8px;
}

.user-type-group :deep(.el-radio-button) {
  flex: 1;
}

.user-type-group :deep(.el-radio-button__inner) {
  width: 100%;
  border-color: rgba(255, 107, 107, 0.3);
  color: #666;
  background: rgba(255, 255, 255, 0.9);
  transition: all 0.3s ease;
  font-weight: 500;
}

.user-type-group :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  border-color: #ff6b6b;
  color: white;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
}

.user-type-group :deep(.el-radio-button__inner:hover) {
  border-color: #ff6b6b;
  color: #ff6b6b;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .form-container {
    padding: 16px;
  }

  .panel-content {
    padding: 36px 24px;
  }
  
  .title {
    font-size: 26px;
  }

  .subtitle {
    font-size: 14px;
  }

  .forms-wrapper {
    max-width: 100%;
  }

  .login-tabs :deep(.el-tabs__item) {
    padding: 0 12px;
    font-size: 14px;
  }

  .user-type-group {
    flex-direction: column;
    gap: 12px;
  }

  .user-type-group :deep(.el-radio-button) {
    width: 100%;
  }
}

/* 滚动条样式 */
:deep(.el-scrollbar__bar) {
  opacity: 0.5;
}

/* 加载状态 */
:deep(.el-button.is-loading) {
  opacity: 0.8;
}

/* 表单标签样式 */
.register-form :deep(.el-form-item__label) {
  color: #333;
  font-weight: 500;
  font-size: 14px;
}
</style>
