<template>
  <div class="forgot-password-container">
    <div class="forgot-password-box">
      <div class="forgot-password-header">
        <el-icon size="32" color="#ff6b6b">
          <Coffee />
        </el-icon>
        <h2>忘记密码</h2>
        <p>通过手机号验证码重置密码</p>
      </div>

      <el-form
        ref="forgotPasswordFormRef"
        :model="forgotPasswordForm"
        :rules="forgotPasswordRules"
        class="forgot-password-form"
        @submit.prevent="handleResetPassword"
      >
        <el-form-item prop="phone">
          <el-input
            v-model="forgotPasswordForm.phone"
            placeholder="请输入注册时的手机号"
            size="large"
            :prefix-icon="Phone"
            maxlength="11"
          />
        </el-form-item>

        <el-form-item prop="verificationCode">
          <div class="verification-code-group">
            <el-input
              v-model="forgotPasswordForm.verificationCode"
              placeholder="请输入验证码"
              size="large"
              :prefix-icon="Message"
              maxlength="6"
              style="flex: 1; margin-right: 12px;"
            />
            <el-button
              type="primary"
              :disabled="!canSendCode || countdown > 0 || sendingCode"
              :loading="sendingCode"
              @click="sendVerificationCode"
              style="width: 120px;"
            >
              {{ countdown > 0 ? `${countdown}s` : (sendingCode ? '发送中...' : '获取验证码') }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item prop="newPassword">
          <el-input
            v-model="forgotPasswordForm.newPassword"
            type="password"
            placeholder="请设置新密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="forgotPasswordForm.confirmPassword"
            type="password"
            placeholder="请确认新密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="reset-password-button"
            :loading="loading"
            @click="handleResetPassword"
          >
            重置密码
          </el-button>
        </el-form-item>

        <el-form-item>
          <div class="back-login">
            <el-link type="primary" @click="$router.push('/login')">
              返回登录
            </el-link>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Coffee, Phone, Message, Lock } from '@element-plus/icons-vue'
import { sendVerificationCode as sendVerificationCodeApi, resetPassword as resetPasswordApi } from '@/api/user'

const router = useRouter()

const forgotPasswordFormRef = ref()
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
const canSendCode = ref(false)
let countdownTimer = null

const forgotPasswordForm = reactive({
  phone: '',
  verificationCode: '',
  newPassword: '',
  confirmPassword: ''
})

// 监听手机号变化，重置发送验证码按钮状态
watch(() => forgotPasswordForm.phone, (newValue) => {
  const phoneRegex = /^1[3-9]\d{9}$/
  canSendCode.value = phoneRegex.test(newValue)
})

// 验证手机号格式
const validatePhone = (rule, value, callback) => {
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!value) {
    callback(new Error('请输入手机号'))
    canSendCode.value = false
  } else if (!phoneRegex.test(value)) {
    callback(new Error('请输入正确的手机号格式'))
    canSendCode.value = false
  } else {
    canSendCode.value = true
    callback()
  }
}

// 验证确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== forgotPasswordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 验证验证码格式（6位数字）
const validateVerificationCode = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入验证码'))
  } else if (!/^\d{6}$/.test(value)) {
    callback(new Error('验证码为6位数字'))
  } else {
    callback()
  }
}

const forgotPasswordRules = {
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  verificationCode: [
    { validator: validateVerificationCode, trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请设置新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
    { pattern: /^(?=.*[A-Za-z])(?=.*\d)/, message: '密码必须包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 发送验证码
const sendVerificationCode = async () => {
  if (!canSendCode.value || countdown.value > 0 || sendingCode.value) {
    return
  }
  
  // 先验证手机号格式
  if (!forgotPasswordFormRef.value) return
  
  try {
    await forgotPasswordFormRef.value.validateField('phone', async (valid) => {
      if (!valid) {
        ElMessage.warning('请先输入正确的手机号')
        return
      }
      
      sendingCode.value = true
      
      try {
        await sendVerificationCodeApi(forgotPasswordForm.phone, 'forgot-password')
        
        ElMessage.success('验证码已发送，请注意查收')
        
        // 开始倒计时
        countdown.value = 60
        if (countdownTimer) {
          clearInterval(countdownTimer)
        }
        countdownTimer = setInterval(() => {
          countdown.value--
          if (countdown.value <= 0) {
            clearInterval(countdownTimer)
            countdownTimer = null
          }
        }, 1000)
      } catch (error) {
        console.error('发送验证码失败:', error)
        if (error.message && !error.message.includes('暂未实现')) {
          ElMessage.error(error.message || '验证码发送失败，请重试')
        }
      } finally {
        sendingCode.value = false
      }
    })
  } catch (error) {
    console.error('验证手机号失败:', error)
    sendingCode.value = false
  }
}

// 组件销毁时清除定时器
onBeforeUnmount(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})

const handleResetPassword = async () => {
  if (!forgotPasswordFormRef.value) return
  
  await forgotPasswordFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      
      try {
        await resetPasswordApi({
          phone: forgotPasswordForm.phone,
          verificationCode: forgotPasswordForm.verificationCode,
          newPassword: forgotPasswordForm.newPassword
        })
        
        ElMessage.success('密码重置成功！请使用新密码登录')
        
        // 延迟跳转，让用户看到成功消息
        setTimeout(() => {
          router.push('/login')
        }, 1000)
      } catch (error) {
        console.error('重置密码失败:', error)
        ElMessage.error(error.message || '密码重置失败，请重试')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.forgot-password-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.forgot-password-box {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  padding: 40px;
  width: 100%;
  max-width: 450px;
}

.forgot-password-header {
  text-align: center;
  margin-bottom: 30px;
}

.forgot-password-header h2 {
  margin: 16px 0 8px;
  color: #333;
  font-size: 24px;
}

.forgot-password-header p {
  color: #666;
  font-size: 14px;
}

.forgot-password-form {
  margin-top: 20px;
}

.verification-code-group {
  display: flex;
  align-items: center;
}

.reset-password-button {
  width: 100%;
  height: 45px;
  font-size: 16px;
}

.back-login {
  text-align: center;
  color: #666;
  font-size: 14px;
}

@media (max-width: 480px) {
  .forgot-password-box {
    padding: 30px 20px;
  }
  
  .verification-code-group {
    flex-direction: column;
    gap: 12px;
  }
  
  .verification-code-group .el-button {
    width: 100%;
  }
}
</style>
