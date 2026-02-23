<template>
  <div class="image-upload">
    <el-upload
      class="uploader"
      :action="uploadUrl"
      :headers="uploadHeaders"
      :show-file-list="false"
      :on-success="handleSuccess"
      :on-error="handleError"
      :before-upload="beforeUpload"
      :data="uploadData"
      accept="image/*"
      name="file"
    >
      <img v-if="imageUrl" :src="imageUrl" class="uploaded-image" />
      <div v-else class="upload-placeholder">
        <el-icon class="upload-icon"><Plus /></el-icon>
        <div class="upload-text">{{ placeholder }}</div>
      </div>
    </el-upload>
    
    <div v-if="showTip" class="upload-tip">
      {{ tip }}
    </div>
    
    <div v-if="imageUrl && showDelete" class="upload-actions">
      <el-button type="danger" size="small" @click="deleteImage">
        <el-icon><Delete /></el-icon>
        删除
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { uploadApi } from '@/api/upload'

// Props
const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  folder: {
    type: String,
    default: 'images'
  },
  placeholder: {
    type: String,
    default: '点击上传图片'
  },
  tip: {
    type: String,
    default: '支持 JPG/PNG 格式，最大10MB'
  },
  showTip: {
    type: Boolean,
    default: true
  },
  showDelete: {
    type: Boolean,
    default: true
  },
  width: {
    type: String,
    default: '120px'
  },
  height: {
    type: String,
    default: '120px'
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'success', 'error'])

// 响应式数据
const imageUrl = ref(props.modelValue)

// 上传配置
const uploadUrl = ref('http://localhost:8081/api/upload/image')
const uploadHeaders = computed(() => {
  const user = localStorage.getItem('user')
  if (user) {
    const userInfo = JSON.parse(user)
    if (userInfo.token) {
      return {
        'Authorization': `Bearer ${userInfo.token}`
      }
    }
  }
  return {}
})

const uploadData = computed(() => ({
  folder: props.folder
}))

// 监听modelValue变化
watch(() => props.modelValue, (newVal) => {
  imageUrl.value = newVal
})

// 上传成功处理
const handleSuccess = (response, file) => {
  if (response.code === 200) {
    imageUrl.value = response.data
    emit('update:modelValue', response.data)
    emit('success', response.data)
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
    emit('error', response.message)
  }
}

// 上传错误处理
const handleError = (error, file) => {
  console.error('上传错误:', error)
  ElMessage.error('图片上传失败，请重试')
  emit('error', error)
}

// 文件上传前验证
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片格式文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('上传图片大小不能超过 10MB!')
    return false
  }
  return true
}

// 删除图片
const deleteImage = async () => {
  if (!imageUrl.value) return
  
  try {
    const response = await uploadApi.deleteFile(imageUrl.value)
    
    if (response.code === 200) {
      imageUrl.value = ''
      emit('update:modelValue', '')
      ElMessage.success('图片删除成功')
    } else {
      ElMessage.error('图片删除失败')
    }
  } catch (error) {
    console.error('删除图片失败:', error)
    ElMessage.error('图片删除失败')
  }
}
</script>

<style scoped>
.image-upload {
  display: inline-block;
}

.uploader {
  .uploaded-image {
    width: v-bind(width);
    height: v-bind(height);
    display: block;
    object-fit: cover;
    border-radius: 8px;
    border: 2px solid #e4e7ed;
  }
  
  .upload-placeholder {
    width: v-bind(width);
    height: v-bind(height);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border: 2px dashed #d9d9d9;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
    background-color: #fafafa;
    
    .upload-icon {
      font-size: 28px;
      color: #8c939d;
      margin-bottom: 8px;
    }
    
    .upload-text {
      color: #8c939d;
      font-size: 14px;
    }
  }
  
  .upload-placeholder:hover {
    border-color: #409eff;
    background-color: #f0f9ff;
    
    .upload-icon,
    .upload-text {
      color: #409eff;
    }
  }
}

.upload-tip {
  margin-top: 8px;
  color: #999;
  font-size: 12px;
}

.upload-actions {
  margin-top: 8px;
  text-align: center;
}
</style>
