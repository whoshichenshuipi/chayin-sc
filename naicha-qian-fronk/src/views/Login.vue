<template>
  <div class="login-container">
    <!-- 背景光斑 -->
    <div class="background-blur"></div>
    
    <!-- 水印图案 -->
    <div class="watermark">
      <el-icon :size="200" color="rgba(230, 57, 70, 0.1)">
        <Coffee />
      </el-icon>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- 品牌Logo -->
      <div class="logo-section">
        <div class="logo-wrapper">
          <el-icon :size="60" :style="{ color: 'var(--color-primary)' }">
            <Coffee />
          </el-icon>
          <h1 class="brand-name">奶茶商城</h1>
          <p class="brand-slogan">品味生活，从这里开始</p>
        </div>
      </div>

      <!-- 登录/门店切换Tab -->
      <div class="login-tabs">
        <div 
          class="tab-item" 
          :class="{ active: loginType === 'user' }"
          @click="loginType = 'user'"
        >
          <el-icon><User /></el-icon>
          <span>用户登录</span>
        </div>
        <div 
          class="tab-item" 
          :class="{ active: loginType === 'merchant' }"
          @click="loginType = 'merchant'"
        >
          <el-icon><Shop /></el-icon>
          <span>门店登录</span>
        </div>
      </div>

      <!-- 登录表单 -->
      <el-form 
        ref="loginFormRef" 
        :model="loginForm" 
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入账号"
            size="large"
            class="login-input"
            :prefix-icon="User"
            @focus="handleInputFocus"
            @blur="handleInputBlur"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            class="login-input"
            :prefix-icon="Lock"
            show-password
            @focus="handleInputFocus"
            @blur="handleInputBlur"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <!-- 错误提示 -->
        <div v-if="errorMessage" class="error-message">
          <el-icon><WarningFilled /></el-icon>
          <span>{{ errorMessage }}</span>
        </div>

        <!-- 登录按钮 -->
        <el-button
          type="primary"
          size="large"
          class="login-button"
          :loading="loading"
          @click="handleLogin"
        >
          {{ loading ? '登录中...' : '登录' }}
        </el-button>
      </el-form>

      <!-- 功能链接 -->
      <div class="login-links">
        <a href="#" class="link-item" @click.prevent="handleForgotPassword">
          忘记密码？
        </a>
        <span class="divider">|</span>
        <a href="#" class="link-item" @click.prevent="handleRegister">
          注册账号
        </a>
      </div>

      <!-- 底部提示 -->
      <div class="login-tip">
        <el-icon><InfoFilled /></el-icon>
        <span>账号为{{ loginType === 'merchant' ? '门店编号' : '手机号' }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Coffee, Shop, WarningFilled, InfoFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { login as userLogin } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

const loginType = ref('user') // 'user' | 'merchant'
const loading = ref(false)
const errorMessage = ref('')
const loginFormRef = ref(null)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ]
}

const handleInputFocus = (e) => {
  e.target.closest('.login-input')?.classList.add('focused')
}

const handleInputBlur = (e) => {
  e.target.closest('.login-input')?.classList.remove('focused')
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    errorMessage.value = ''
    
    try {
      const response = await userLogin({
        username: loginForm.username,
        password: loginForm.password,
        userType: loginType.value
      })
      
      if (response.code === 200) {
        // 保存用户信息和token
        userStore.login(response.data.userInfo, response.data.token)
        
        ElMessage.success('登录成功')
        
        // 根据登录类型跳转
        if (loginType.value === 'merchant') {
          window.location.href = 'http://localhost:3002/home'
        } else {
          router.push('/home')
        }
      } else {
        errorMessage.value = response.message || '登录失败，请检查账号密码'
      }
    } catch (error) {
      console.error('登录错误:', error)
      errorMessage.value = error.response?.data?.message || '登录失败，请稍后重试'
    } finally {
      loading.value = false
    }
  })
}

const handleForgotPassword = () => {
  router.push('/forgot-password')
}

const handleRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: var(--color-bg-white);
  padding: var(--spacing-lg);
  overflow: hidden;
}

/* 背景渐变光斑 */
.background-blur {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 30%, rgba(230, 57, 70, 0.15) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(230, 57, 70, 0.15) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

/* 水印图案 */
.watermark {
  position: absolute;
  bottom: -50px;
  right: -50px;
  opacity: 0.1;
  pointer-events: none;
  z-index: 0;
  transform: rotate(-15deg);
}

/* 登录卡片 */
.login-card {
  position: relative;
  width: 100%;
  max-width: 450px;
  background: white;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  padding: var(--spacing-2xl);
  z-index: 1;
}

/* Logo区域 - 占界面1/5高度 */
.logo-section {
  height: 20vh;
  min-height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--spacing-xl);
}

.logo-wrapper {
  text-align: center;
}

.logo-wrapper .el-icon {
  margin-bottom: var(--spacing-md);
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.brand-name {
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-secondary) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: var(--spacing-xs);
}

.brand-slogan {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

/* 登录/门店切换Tab */
.login-tabs {
  display: flex;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-xl);
  border-bottom: 2px solid var(--color-bg-gray-light);
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-md);
  cursor: pointer;
  border-radius: var(--radius-md) var(--radius-md) 0 0;
  transition: all var(--transition-fast);
  color: var(--color-gray-medium);
  font-weight: var(--font-weight-medium);
}

.tab-item:hover {
  background: var(--color-bg-gray-light);
  color: var(--color-secondary);
}

.tab-item.active {
  color: var(--color-secondary);
  border-bottom: 3px solid var(--color-secondary);
  background: transparent;
  font-weight: var(--font-weight-semibold);
}

.tab-item .el-icon {
  font-size: var(--font-size-lg);
}

/* 登录表单 */
.login-form {
  margin-bottom: var(--spacing-lg);
}

.login-input {
  width: 100%;
}

/* 输入框样式定制 */
.login-input :deep(.el-input__wrapper) {
  background: white;
  border: 2px solid var(--color-bg-gray-light);
  border-radius: var(--radius-lg);
  padding: var(--spacing-sm) var(--spacing-md);
  transition: all var(--transition-base);
  box-shadow: none;
}

.login-input :deep(.el-input__wrapper:hover) {
  border-color: var(--color-primary-light);
}

.login-input.focused :deep(.el-input__wrapper),
.login-input :deep(.el-input.is-focus .el-input__wrapper) {
  border-color: var(--color-primary);
  border-width: 3px;
  box-shadow: 0 0 0 4px rgba(230, 57, 70, 0.1);
  animation: breathe 2s ease-in-out infinite;
}

@keyframes breathe {
  0%, 100% {
    box-shadow: 0 0 0 4px rgba(42, 157, 143, 0.1);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(42, 157, 143, 0.15);
  }
}

/* 输入框图标颜色 - 绿色 */
.login-input :deep(.el-input__prefix) {
  color: var(--color-secondary);
}

.login-input :deep(.el-input__prefix .el-icon) {
  font-size: var(--font-size-lg);
}

/* 错误提示 */
.error-message {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  margin-bottom: var(--spacing-md);
  padding: var(--spacing-sm);
  background: var(--color-primary-lighter);
  border-radius: var(--radius-md);
}

.error-message .el-icon {
  color: var(--color-primary);
}

/* 登录按钮 - 红色渐变 */
.login-button {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, var(--color-primary) 0%, #C1121F 100%);
  border: none;
  border-radius: var(--radius-lg);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: white;
  box-shadow: var(--shadow-md);
  transition: all var(--transition-fast);
}

.login-button:hover:not(:disabled) {
  background: linear-gradient(135deg, var(--color-primary-light) 0%, var(--color-primary) 100%);
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
  filter: brightness(1.1);
}

.login-button:active:not(:disabled) {
  transform: translateY(0);
}

/* 功能链接 - 绿色文字 */
.login-links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-lg);
}

.link-item {
  color: var(--color-secondary);
  font-size: var(--font-size-sm);
  text-decoration: none;
  transition: all var(--transition-fast);
  position: relative;
}

.link-item::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: var(--color-secondary);
  transition: width var(--transition-fast);
}

.link-item:hover {
  color: var(--color-secondary-dark);
}

.link-item:hover::after {
  width: 100%;
}

.divider {
  color: var(--color-gray-light);
}

/* 底部提示 - 绿色小字 */
.login-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  color: var(--color-secondary);
  font-size: var(--font-size-xs);
}

.login-tip .el-icon {
  font-size: var(--font-size-sm);
}

/* 加载状态 - 红绿色循环进度条 */
.login-button.is-loading {
  position: relative;
  overflow: hidden;
}

.login-button.is-loading::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(230, 57, 70, 0.5),
    rgba(42, 157, 143, 0.5),
    transparent
  );
  animation: progress 1.5s infinite;
}

@keyframes progress {
  0% {
    left: -100%;
  }
  100% {
    left: 100%;
  }
}

/* 响应式适配 */
@media (max-width: 768px) {
  .login-card {
    padding: var(--spacing-lg);
    max-width: 100%;
  }
  
  .logo-section {
    min-height: 100px;
    margin-bottom: var(--spacing-lg);
  }
  
  .brand-name {
    font-size: var(--font-size-2xl);
  }
  
  .login-tabs {
    margin-bottom: var(--spacing-lg);
  }
}

@media (max-width: 480px) {
  .login-container {
    padding: var(--spacing-md);
  }
  
  .login-card {
    padding: var(--spacing-md);
  }
  
  .logo-section {
    min-height: 80px;
    margin-bottom: var(--spacing-md);
  }
  
  .brand-name {
    font-size: var(--font-size-xl);
  }
}
</style>
