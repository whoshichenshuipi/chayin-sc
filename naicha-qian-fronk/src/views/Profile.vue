<template>
  <div class="profile-container" v-loading="loading">
    <div class="container">
      <el-row :gutter="20">
        <!-- 左侧个人信息 -->
        <el-col :span="8">
          <el-card class="profile-card">
            <div class="profile-header">
              <el-avatar 
                :size="80" 
                :src="currentAvatar"
                :key="avatarKey"
              >
                {{ userStore.getUserName.charAt(0) }}
              </el-avatar>
              <h3>{{ profileForm.nickname || userStore.getUserName }}</h3>
              <p>{{ userStore.userInfo?.userType === 'user' ? '普通用户' : '用户' }}</p>
            </div>
            
            <el-divider />
            
            <div class="profile-stats">
              <div class="stat-item">
                <span class="stat-number">{{ userStats.orderCount }}</span>
                <span class="stat-label">订单数量</span>
              </div>
              <div class="stat-item">
                <span class="stat-number">¥{{ (userStats.totalAmount || 0).toFixed(2) }}</span>
                <span class="stat-label">累计消费</span>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧详细信息 -->
        <el-col :span="16">
          <el-card class="info-card">
            <template #header>
              <div class="card-header">
                <span>个人信息</span>
                <el-button type="primary" @click="editMode = true">
                  {{ editMode ? '保存' : '编辑' }}
                </el-button>
              </div>
            </template>

            <el-form
              ref="profileFormRef"
              :model="profileForm"
              :rules="profileRules"
              label-width="100px"
              class="profile-form"
            >
              <el-form-item label="用户名" prop="username">
                <el-input
                  v-model="profileForm.username"
                  :disabled="true"
                  placeholder="用户名不可修改"
                />
              </el-form-item>

              <el-form-item label="昵称" prop="nickname">
                <el-input
                  v-model="profileForm.nickname"
                  :disabled="!editMode"
                  placeholder="请输入昵称"
                />
              </el-form-item>

              <el-form-item label="邮箱" prop="email">
                <el-input
                  v-model="profileForm.email"
                  :disabled="!editMode"
                  placeholder="请输入邮箱"
                />
              </el-form-item>

              <el-form-item label="手机号" prop="phone">
                <el-input
                  v-model="profileForm.phone"
                  :disabled="!editMode"
                  placeholder="请输入手机号"
                />
              </el-form-item>

              <el-form-item label="性别" prop="gender">
                <el-select
                  v-model="profileForm.gender"
                  :disabled="!editMode"
                  placeholder="请选择性别"
                  style="width: 100%"
                >
                  <el-option label="男" value="male" />
                  <el-option label="女" value="female" />
                  <el-option label="其他" value="other" />
                </el-select>
              </el-form-item>

              <el-form-item label="头像" prop="avatar">
                <div class="avatar-upload">
                  <el-upload
                    class="avatar-uploader"
                    :show-file-list="false"
                    :disabled="!editMode"
                    :before-upload="beforeAvatarUpload"
                    :http-request="handleAvatarUpload"
                    accept="image/*"
                  >
                    <img 
                      v-if="profileForm.avatar" 
                      :src="getDisplayAvatar(profileForm.avatar)" 
                      class="avatar"
                      :key="`form-avatar-${avatarKey}`"
                      @error="handleFormAvatarError"
                      @load="handleFormAvatarLoad"
                    />
                    <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                  </el-upload>
                  <div v-if="editMode" class="avatar-tip">
                    <p>支持 JPG、PNG 格式，文件大小不超过 2MB</p>
                  </div>
                </div>
              </el-form-item>

              <el-form-item label="生日" prop="birthday">
                <el-date-picker
                  v-model="profileForm.birthday"
                  :disabled="!editMode"
                  type="date"
                  placeholder="请选择生日"
                  style="width: 100%"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
                <div v-if="isBirthdayToday" class="birthday-notice">
                  <el-icon color="#ff6b6b"><Star /></el-icon>
                  <span>今天是您的生日，祝您生日快乐！🎉</span>
                </div>
              </el-form-item>

              <el-form-item label="地址" prop="address">
                <el-input
                  v-model="profileForm.address"
                  :disabled="!editMode"
                  type="textarea"
                  placeholder="请输入详细地址"
                  :rows="3"
                />
              </el-form-item>

              <el-form-item v-if="editMode">
                <el-button type="primary" @click="handleSave">保存</el-button>
                <el-button @click="handleCancel">取消</el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 安全设置 -->
          <el-card class="security-card" style="margin-top: 20px;">
            <template #header>
              <span>安全设置</span>
            </template>

            <div class="security-items">
              <div class="security-item">
                <div class="item-info">
                  <h4>修改密码</h4>
                  <p>定期修改密码有助于保护账户安全</p>
                </div>
                <el-button type="primary" @click="showChangePassword = true">
                  修改
                </el-button>
              </div>

              <el-divider />

              <div class="security-item">
                <div class="item-info">
                  <h4>手机验证</h4>
                  <p>已绑定手机号：{{ maskedPhone }}</p>
                </div>
                <el-button type="primary" @click="ElMessage.info('更换手机号功能开发中')">更换</el-button>
              </div>

              <el-divider />

              <div class="security-item">
                <div class="item-info">
                  <h4>邮箱验证</h4>
                  <p>已绑定邮箱：{{ maskedEmail }}</p>
                </div>
                <el-button type="primary" @click="ElMessage.info('更换邮箱功能开发中')">更换</el-button>
              </div>

              <el-divider />

              <div class="security-item">
                <div class="item-info">
                  <h4>收货地址</h4>
                  <p>管理您的收货地址信息</p>
                </div>
                <el-button type="primary" @click="$router.push('/address')">
                  管理地址
                </el-button>
              </div>

              <el-divider />

              <div class="security-item">
                <div class="item-info">
                  <h4>我的消息</h4>
                  <p>查看订单通知、营销通知、系统通知</p>
                </div>
                <el-button type="primary" @click="$router.push('/messages')">
                  查看消息
                </el-button>
              </div>

              <el-divider />

              <div class="security-item">
                <div class="item-info">
                  <h4>帮助中心</h4>
                  <p>常见问题解答和在线客服</p>
                </div>
                <el-button type="primary" @click="$router.push('/help')">
                  获取帮助
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="showChangePassword"
      title="修改密码"
      width="400px"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入原密码"
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码"
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请确认新密码"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showChangePassword = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { Plus, Star } from '@element-plus/icons-vue'
// 导入 API
import { getUserInfo, updateUserInfo, changePassword, getUserStats } from '@/api/user'
import { uploadImage } from '@/api/upload'
// 导入图片工具函数
import { getImageUrl, handleImageError } from '@/utils/imageUtils'

const userStore = useUserStore()

const editMode = ref(false)
const showChangePassword = ref(false)
const loading = ref(false)
const profileFormRef = ref()
const passwordFormRef = ref()
const avatarKey = ref(0) // 用于强制刷新头像

const profileForm = reactive({
  username: '',
  email: '',
  phone: '',
  gender: '',
  avatar: '',
  birthday: '',
  address: '',
  nickname: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const userStats = reactive({
  orderCount: 0,
  totalAmount: 0
})

// 性别映射：前端值 -> 后端值
const genderMap = {
  'male': 1,
  'female': 2,
  'other': 0,
  '': 0
}

const genderReverseMap = {
  1: 'male',
  2: 'female',
  0: 'other'
}

// 掩码显示手机号和邮箱
const maskedPhone = computed(() => {
  if (!profileForm.phone) return '未绑定'
  if (profileForm.phone.length <= 7) return profileForm.phone
  return profileForm.phone.substring(0, 3) + '****' + profileForm.phone.substring(7)
})

const maskedEmail = computed(() => {
  if (!profileForm.email) return '未绑定'
  const atIndex = profileForm.email.indexOf('@')
  if (atIndex <= 0) return profileForm.email
  const prefix = profileForm.email.substring(0, Math.min(3, atIndex))
  return prefix + '****' + profileForm.email.substring(atIndex)
})

const profileRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 计算属性：检查今天是否是生日
const isBirthdayToday = computed(() => {
  if (!profileForm.birthday) return false
  
  const today = new Date()
  const birthday = new Date(profileForm.birthday)
  
  return today.getMonth() === birthday.getMonth() && 
         today.getDate() === birthday.getDate()
})

// 获取显示的头像 URL
const getDisplayAvatar = (avatar) => {
  if (!avatar) return null
  
  // 如果是 data URI，直接返回
  if (avatar.startsWith('data:')) {
    return avatar
  }
  
  // 否则处理为完整的 URL
  return getImageUrl(avatar)
}

// 计算属性：当前显示的头像（优先使用表单中的头像，然后是 store 中的）
const currentAvatar = computed(() => {
  // 如果表单中有头像且不是 data URI（说明是服务器 URL），优先使用表单中的
  if (profileForm.avatar && !profileForm.avatar.startsWith('data:')) {
    return getDisplayAvatar(profileForm.avatar)
  }
  // 否则使用 store 中的头像
  return getDisplayAvatar(userStore.getUserAvatar)
})

// 处理头像加载错误（左侧头像通过 getDisplayAvatar 自动处理）

// 头像加载重试计数
const avatarRetryCount = ref({})

// 处理表单头像加载错误
const handleFormAvatarError = (event) => {
  const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHZpZXdCb3g9IjAgMCA0MCA0MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPGNpcmNsZSBjeD0iMjAiIGN5PSIyMCIgcj0iMjAiIGZpbGw9IiNFNjM5NDYiLz4KPHBhdGggZD0iTTIwIDEyQzE2LjY4NjMgMTIgMTQgMTQuNjg2MyAxNCAxOEMxNCAyMS4zMTM3IDE2LjY4NjMgMjQgMjAgMjRDMjMuMzEzNyAyNCAyNiAyMS4zMTM3IDI2IDE4QzI2IDE0LjY4NjMgMjMuMzEzNyAxMiAyMCAxMloiIGZpbGw9IndoaXRlIi8+CjxwYXRoIGQ9Ik0xMCAzMEMxMCAyNS41ODE3IDEzLjU4MTcgMjIgMTggMjJIMjJDMjYuNDE4MyAyMiAzMCAyNS41ODE3IDMwIDMwVjM0SDEwVjMwWiIgZmlsbD0id2hpdGUiLz4KPC9zdmc+'
  
  const avatarUrl = profileForm.avatar
  if (!avatarUrl || avatarUrl.startsWith('data:')) {
    // 如果已经是默认头像或没有头像，直接使用默认头像
    handleImageError(event, defaultAvatar)
    return
  }
  
  // 获取当前 URL 的重试次数
  const retryKey = avatarUrl
  const retryCount = avatarRetryCount.value[retryKey] || 0
  
  console.warn(`头像加载失败 (重试 ${retryCount}/2):`, avatarUrl, event)
  
  // 如果重试次数少于 2 次，尝试重新加载
  if (retryCount < 2) {
    avatarRetryCount.value[retryKey] = retryCount + 1
    // 延迟后重新加载（可能是网络延迟或 OSS 还未完全同步）
    setTimeout(() => {
      if (event && event.target) {
        // 强制重新加载，添加时间戳防止缓存
        const separator = avatarUrl.includes('?') ? '&' : '?'
        event.target.src = `${avatarUrl}${separator}_t=${Date.now()}`
      }
    }, 1000 * (retryCount + 1)) // 第1次重试延迟1秒，第2次延迟2秒
  } else {
    // 重试多次后仍然失败，可能是 CORS 问题或图片不存在
    console.error('头像加载多次重试后仍然失败，可能的原因：', {
      url: avatarUrl,
      cors: '检查 OSS CORS 配置',
      network: '检查网络连接',
      file: '检查文件是否已上传成功'
    })
    // 不要覆盖 OSS URL，保留它以便用户手动检查
    // 只更新显示为默认头像
    handleImageError(event, defaultAvatar)
  }
}

// 处理表单头像加载成功
const handleFormAvatarLoad = (event) => {
  // 头像加载成功，清除重试计数
  const avatarUrl = profileForm.avatar
  if (avatarUrl && !avatarUrl.startsWith('data:')) {
    delete avatarRetryCount.value[avatarUrl]
    console.log('头像加载成功:', avatarUrl)
  }
}

// 头像上传前的验证
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 处理头像上传
const handleAvatarUpload = async (options) => {
  const { file } = options
  
  try {
    loading.value = true
    
    // 保存当前头像，用于上传失败时恢复
    const previousAvatar = profileForm.avatar || userStore.userInfo?.avatar
    
    // 先创建本地预览（仅用于临时显示，上传成功后会被替换为 URL）
    const reader = new FileReader()
    reader.onload = (e) => {
      // 临时显示预览（但会在上传成功后立即替换为 URL）
      profileForm.avatar = e.target.result
      avatarKey.value++ // 强制刷新显示
    }
    reader.readAsDataURL(file)
    
    // 上传到服务器（等待上传完成）
    const imageUrl = await uploadImage(file, 'avatars')
    
    // uploadImage 已经处理了返回格式，应该直接返回字符串 URL
    if (!imageUrl || typeof imageUrl !== 'string') {
      console.error('上传返回格式错误:', imageUrl)
      ElMessage.error('头像上传失败：返回格式错误，请重试')
      // 如果上传失败，恢复之前的头像
      if (previousAvatar) {
        profileForm.avatar = previousAvatar.startsWith('data:') ? previousAvatar : getImageUrl(previousAvatar)
      } else {
        profileForm.avatar = ''
      }
      avatarKey.value++
      return
    }
    
    // 验证 URL 是否包含无效字符（比如中文消息）
    if (imageUrl.includes('图片上传成功') || imageUrl.includes('上传成功') || imageUrl.includes('成功')) {
      console.error('URL 包含无效字符（可能是错误消息）:', imageUrl)
      ElMessage.error('头像上传失败：服务器返回格式错误，请重试')
      if (previousAvatar) {
        profileForm.avatar = previousAvatar.startsWith('data:') ? previousAvatar : getImageUrl(previousAvatar)
      } else {
        profileForm.avatar = ''
      }
      avatarKey.value++
      return
    }
    
    // 确保图片 URL 是完整的
    // 如果已经是完整 URL，getImageUrl 会直接返回
    // 如果是相对路径，会自动拼接 baseURL
    const fullImageUrl = getImageUrl(imageUrl)
    
    // 再次验证处理后的 URL
    if (!fullImageUrl || fullImageUrl.includes('图片上传成功') || fullImageUrl.includes('上传成功')) {
      console.error('URL 处理失败，包含无效字符:', fullImageUrl)
      ElMessage.error('头像上传失败：URL 格式错误，请重试')
      if (previousAvatar) {
        profileForm.avatar = previousAvatar.startsWith('data:') ? previousAvatar : getImageUrl(previousAvatar)
      } else {
        profileForm.avatar = ''
      }
      avatarKey.value++
      return
    }
    
    // 验证 URL 长度（正常的 URL 不应该超过 1000 字符）
    if (fullImageUrl.length > 1000) {
      console.error('头像 URL 过长，可能包含异常数据:', fullImageUrl.length)
      ElMessage.error('头像上传失败：URL 格式异常，请重试')
      if (previousAvatar) {
        profileForm.avatar = previousAvatar.startsWith('data:') ? previousAvatar : getImageUrl(previousAvatar)
      } else {
        profileForm.avatar = ''
      }
      avatarKey.value++
      return
    }

    // 更新为服务器返回的 URL（不是 base64）
    profileForm.avatar = fullImageUrl
    
    // 清除该 URL 的重试计数
    delete avatarRetryCount.value[fullImageUrl]
    
    // 更新 store 中的头像（包含完整的用户信息）
    userStore.updateUserInfo({ 
      ...userStore.userInfo,
      avatar: fullImageUrl 
    })
    
    // 强制刷新头像显示（延迟一下，确保 OSS 文件已完全同步）
    avatarKey.value++
    
    ElMessage.success('头像上传成功!')
    
    // 等待一小段时间后再次刷新，确保图片可以加载
    setTimeout(() => {
      avatarKey.value++
    }, 500)
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败，请重试')
  } finally {
    loading.value = false
  }
}

// 加载用户信息
const loadUserInfo = async () => {
  if (!userStore.userInfo?.id) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    loading.value = true
    const userInfo = await getUserInfo(userStore.userInfo.id)
    
    if (userInfo) {
      // 转换数据格式
      Object.assign(profileForm, {
        username: userInfo.username || '',
        email: userInfo.email || '',
        phone: userInfo.phone || '',
        gender: genderReverseMap[userInfo.gender] || '',
        avatar: userInfo.avatar ? getImageUrl(userInfo.avatar) : '',
        birthday: userInfo.birthday ? formatDate(userInfo.birthday) : '',
        address: userInfo.address || '',
        nickname: userInfo.nickname || userInfo.username || ''
      })
      
      // 更新 store（确保头像也被更新）
      const updatedUserInfo = {
        ...userStore.userInfo,
        ...profileForm
      }
      userStore.updateUserInfo(updatedUserInfo)
      
      // 强制刷新头像显示
      avatarKey.value++
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    // 如果获取失败，使用 store 中的信息
    if (userStore.userInfo) {
      Object.assign(profileForm, {
        username: userStore.userInfo.username || '',
        email: userStore.userInfo.email || '',
        phone: userStore.userInfo.phone || '',
        gender: userStore.userInfo.gender || '',
        avatar: userStore.userInfo.avatar ? getImageUrl(userStore.userInfo.avatar) : '',
        birthday: userStore.userInfo.birthday || '',
        address: userStore.userInfo.address || '',
        nickname: userStore.userInfo.nickname || userStore.userInfo.username || ''
      })
    }
  } finally {
    loading.value = false
  }
}

// 加载用户统计
const loadUserStats = async () => {
  if (!userStore.userInfo?.id) return
  
  try {
    const stats = await getUserStats(userStore.userInfo.id)
    if (stats) {
      userStats.orderCount = stats.orderCount || 0
      userStats.totalAmount = parseFloat(stats.totalAmount || 0)
    }
  } catch (error) {
    console.error('加载用户统计失败:', error)
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  if (typeof date === 'string') return date
  // 处理 LocalDate 格式
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

onMounted(async () => {
  // 初始化用户信息（从 store）
  if (userStore.userInfo) {
    Object.assign(profileForm, {
      username: userStore.userInfo.username || '',
      email: userStore.userInfo.email || '',
      phone: userStore.userInfo.phone || '',
      gender: userStore.userInfo.gender || '',
      avatar: userStore.userInfo.avatar ? getImageUrl(userStore.userInfo.avatar) : '',
      birthday: userStore.userInfo.birthday || '',
      address: userStore.userInfo.address || '',
      nickname: userStore.userInfo.nickname || userStore.userInfo.username || ''
    })
  }
  
  // 从服务器加载最新信息
  await Promise.all([
    loadUserInfo(),
    loadUserStats()
  ])
})

const handleSave = async () => {
  if (!profileFormRef.value) return
  
  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        
        if (!userStore.userInfo?.id) {
          ElMessage.error('请先登录')
          return
        }
        
        // 转换数据格式
        // 处理头像：如果是 base64 数据（data: 开头），则不发送，使用原来的头像或 null
        let avatarToSave = profileForm.avatar
        if (avatarToSave && avatarToSave.startsWith('data:')) {
          // 如果是 base64 数据，说明用户只预览了但还没有上传成功
          // 使用原来的头像 URL，如果没有则使用 null（不更新头像）
          avatarToSave = userStore.userInfo?.avatar || null
          console.warn('检测到 base64 头像数据，使用原头像 URL:', avatarToSave)
        } else if (avatarToSave) {
          // 如果是 URL，确保是完整的 URL
          avatarToSave = getImageUrl(avatarToSave)
          // 验证 URL 长度（超过 1000 字符的 URL 可能是异常的）
          if (avatarToSave && avatarToSave.length > 1000) {
            console.warn('头像 URL 过长，可能包含异常数据:', avatarToSave.length)
            avatarToSave = userStore.userInfo?.avatar || null
          }
        }
        
        const updateData = {
          username: profileForm.username,
          email: profileForm.email,
          phone: profileForm.phone,
          gender: genderMap[profileForm.gender] || 0,
          avatar: avatarToSave, // 使用处理后的头像
          birthday: profileForm.birthday,
          address: profileForm.address,
          nickname: profileForm.nickname || profileForm.username
        }
        
        // 调用更新接口
        try {
          const result = await updateUserInfo(userStore.userInfo.id, updateData)
          
          // 更新 store（包含完整的用户信息）
          const updatedUserInfo = {
            ...userStore.userInfo,
            ...profileForm,
            gender: updateData.gender,
            avatar: avatarToSave || profileForm.avatar // 使用处理后的头像（URL），确保不是 base64
          }
          userStore.updateUserInfo(updatedUserInfo)
          
          // 强制刷新头像显示
          avatarKey.value++
          
          editMode.value = false
          
          // 重新加载用户信息以确保数据同步
          await loadUserInfo()
          
          // 检查是否是仅本地更新
          if (result && result.message && result.message.includes('仅更新本地存储')) {
            ElMessage.warning('个人信息已保存到本地，但后端接口未实现，请联系管理员')
          } else {
            ElMessage.success('保存成功！')
          }
        } catch (error) {
          // 即使后端失败，也更新本地存储（但不刷新界面）
          userStore.updateUserInfo({
            ...userStore.userInfo,
            ...profileForm,
            gender: updateData.gender
          })
          throw error // 重新抛出错误以显示错误提示
        }
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error(error.message || '保存失败，请重试')
      } finally {
        loading.value = false
      }
    }
  })
}

const handleCancel = () => {
  editMode.value = false
  // 恢复原始数据（重新加载）
  loadUserInfo().then(() => {
    // 刷新头像显示
    avatarKey.value++
  })
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        
        await changePassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        
        showChangePassword.value = false
        ElMessage.success('密码修改成功！')
        
        // 清空表单
        Object.assign(passwordForm, {
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        })
      } catch (error) {
        console.error('密码修改失败:', error)
        ElMessage.error(error.message || '密码修改失败，请重试')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.profile-container {
  padding: 20px 0;
  min-height: calc(100vh - 120px);
}

.profile-card {
  text-align: center;
}

.profile-header h3 {
  margin: 16px 0 8px;
  color: #333;
}

.profile-header p {
  color: #666;
  font-size: 14px;
}

.profile-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 20px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 24px;
  font-weight: bold;
  color: #ff6b6b;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-form {
  max-width: 600px;
}

.security-items {
  max-width: 600px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
}

.item-info h4 {
  margin: 0 0 4px;
  color: #333;
}

.item-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
}

.avatar {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 6px;
}

.avatar-tip {
  margin-top: 8px;
  color: #999;
  font-size: 12px;
}

.avatar-tip p {
  margin: 0;
}

.birthday-notice {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  padding: 8px 12px;
  background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
  color: white;
  border-radius: 6px;
  font-size: 14px;
  animation: birthdayPulse 2s infinite;
}

@keyframes birthdayPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

@media (max-width: 768px) {
  .profile-container .el-col {
    margin-bottom: 20px;
  }
}
</style>
