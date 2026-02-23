<template>
  <div class="page-container">
    <div class="page-header">
      <h2>新增商品</h2>
      <p class="page-description">填写商品信息，支持规格设置、库存管理和价格配置</p>
    </div>
    
    <el-form :model="productForm" :rules="rules" ref="productFormRef" label-width="120px" class="product-form">
      <!-- 基本信息 -->
      <el-card class="form-section">
        <template #header>
          <div class="section-header">
            <span>基本信息</span>
            <el-tag type="info">必填</el-tag>
          </div>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品名称" prop="name" required>
              <el-input v-model="productForm.name" placeholder="请输入商品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品分类" prop="categoryId" required>
              <el-cascader
                v-model="productForm.categoryId"
                :options="categoryOptions"
                :props="cascaderProps"
                placeholder="请选择分类（支持搜索）"
                style="width: 100%"
                filterable
                clearable
                :loading="categoryLoading"
                :disabled="categoryLoading || categoryOptions.length === 0"
              />
              <div style="font-size: 12px; color: #999; margin-top: 4px;">
                <span v-if="categoryLoading">加载中...</span>
                <span v-else>
                  分类数量: {{ categoryOptions.length }} 
                  <span v-if="categoryOptions.length === 0" style="color: #f56c6c;">
                    （暂无分类，请先在分类管理中创建分类）
                  </span>
                </span>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="商品简介" prop="description">
          <el-input 
            v-model="productForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="如：精选优质茶叶，搭配新鲜牛奶，口感醇厚"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="轮播图片" prop="images">
          <el-upload
            v-model:file-list="productForm.images"
            :http-request="uploadImage"
            list-type="picture-card"
            :on-preview="handlePictureCardPreview"
            :on-remove="handleRemove"
            :before-upload="beforeUpload"
            :limit="5"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">建议尺寸：750x750px，最多上传5张图片</div>
        </el-form-item>
      </el-card>
      
      <!-- 规格信息 -->
      <el-card class="form-section">
        <template #header>
          <div class="section-header">
            <span>规格信息</span>
            <el-tag type="warning">可选</el-tag>
          </div>
        </template>
        
        <el-form-item label="甜度选项">
          <div class="option-group">
            <el-checkbox-group v-model="productForm.sweetnessOptions">
              <el-checkbox label="no-sugar">无糖</el-checkbox>
              <el-checkbox label="half-sugar">半糖</el-checkbox>
              <el-checkbox label="seven-sugar">七分糖</el-checkbox>
              <el-checkbox label="full-sugar">全糖</el-checkbox>
            </el-checkbox-group>
          </div>
        </el-form-item>
        
        <el-form-item label="温度选项">
          <div class="option-group">
            <el-checkbox-group v-model="productForm.temperatureOptions">
              <el-checkbox label="ice">加冰</el-checkbox>
              <el-checkbox label="room-temp">常温</el-checkbox>
              <el-checkbox label="hot">热饮</el-checkbox>
            </el-checkbox-group>
          </div>
        </el-form-item>
        
        <el-form-item label="加料选项">
          <div class="addon-section">
            <div v-for="(addon, index) in productForm.addons" :key="index" class="addon-item">
              <el-row :gutter="10">
                <el-col :span="8">
                  <el-input v-model="addon.name" placeholder="加料名称" />
                </el-col>
                <el-col :span="6">
                  <el-input-number v-model="addon.price" :min="0" :precision="2" placeholder="价格" />
                </el-col>
                <el-col :span="6">
                  <el-input-number v-model="addon.stock" :min="0" placeholder="库存" />
                </el-col>
                <el-col :span="4">
                  <el-button type="danger" @click="removeAddon(index)" size="small">删除</el-button>
                </el-col>
              </el-row>
            </div>
            <el-button type="primary" @click="addAddon" class="add-addon-btn">
              <el-icon><Plus /></el-icon>
              添加加料
            </el-button>
          </div>
        </el-form-item>
        
        <el-form-item label="杯型规格">
          <div class="size-section">
            <div v-for="(size, index) in productForm.sizes" :key="index" class="size-item">
              <el-row :gutter="10">
                <el-col :span="8">
                  <el-input v-model="size.name" placeholder="杯型名称" />
                </el-col>
                <el-col :span="6">
                  <el-input-number v-model="size.price" :min="0" :precision="2" placeholder="价格" />
                </el-col>
                <el-col :span="6">
                  <el-input v-model="size.capacity" placeholder="容量" />
                </el-col>
                <el-col :span="4">
                  <el-button type="danger" @click="removeSize(index)" size="small">删除</el-button>
                </el-col>
              </el-row>
            </div>
            <el-button type="primary" @click="addSize" class="add-size-btn">
              <el-icon><Plus /></el-icon>
              添加杯型
            </el-button>
          </div>
        </el-form-item>
      </el-card>
      
      <!-- 库存信息 -->
      <el-card class="form-section">
        <template #header>
          <div class="section-header">
            <span>库存信息</span>
            <el-tag type="success">必填</el-tag>
          </div>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="初始库存" prop="stock" required>
              <el-input-number v-model="productForm.stock" :min="0" placeholder="请输入初始库存" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存预警值" prop="stockAlert" required>
              <el-input-number v-model="productForm.stockAlert" :min="0" placeholder="如：10" style="width: 100%" />
              <span class="form-tip">库存低于此值时系统提醒补货</span>
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>
      
      <!-- 价格信息 -->
      <el-card class="form-section">
        <template #header>
          <div class="section-header">
            <span>价格信息</span>
            <el-tag type="danger">必填</el-tag>
          </div>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="原价" prop="originalPrice" required>
              <el-input-number v-model="productForm.originalPrice" :min="0" :precision="2" placeholder="请输入原价" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="会员价" prop="memberPrice">
              <el-input-number v-model="productForm.memberPrice" :min="0" :precision="2" placeholder="会员专享价格" style="width: 100%" />
              <span class="form-tip">仅会员可见，不填则无会员价</span>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="促销活动">
          <el-checkbox-group v-model="productForm.promotionTypes">
            <el-checkbox label="new-user">新用户专享</el-checkbox>
            <el-checkbox label="flash-sale">限时秒杀</el-checkbox>
            <el-checkbox label="combo">套餐优惠</el-checkbox>
            <el-checkbox label="discount">满减活动</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-card>
      
      <!-- 提交按钮 -->
      <div class="form-actions">
        <el-button type="primary" @click="submitProduct" :loading="saving" size="large">
          <el-icon><Check /></el-icon>
          提交商品
        </el-button>
        <el-button @click="resetForm" size="large">
          <el-icon><Refresh /></el-icon>
          重置表单
        </el-button>
      </div>
    </el-form>
    
    <!-- 图片预览对话框 -->
    <el-dialog v-model="previewVisible" title="图片预览" width="80%" top="5vh">
      <div class="preview-container">
        <el-image :src="previewImage" fit="contain" style="width: 100%; height: 400px;" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Check, Refresh } from '@element-plus/icons-vue'
import { createProduct, getProductCategories } from '@/api/product'
import { uploadApi } from '@/api/upload'
import request from '@/utils/request'

const router = useRouter()
const productFormRef = ref(null)

// 加载状态
const saving = ref(false)
const categoryLoading = ref(false)

// 图片预览
const previewVisible = ref(false)
const previewImage = ref('')

// 商品表单数据
const productForm = reactive({
  // 基本信息
  name: '',
  categoryId: null,
  description: '',
  images: [],
  imageUrls: [],
  
  // 规格信息
  sweetnessOptions: ['full-sugar', 'seven-sugar'],
  temperatureOptions: ['ice', 'room-temp', 'hot'],
  addons: [
    { name: '珍珠', price: 2.00, stock: 100 },
    { name: '椰果', price: 2.00, stock: 80 }
  ],
  sizes: [
    { name: '中杯', price: 0, capacity: '500ml' },
    { name: '大杯', price: 3.00, capacity: '700ml' }
  ],
  
  // 库存信息
  stock: 100,
  stockAlert: 10,
  
  // 价格信息
  originalPrice: 0,
  memberPrice: 0,
  promotionTypes: []
})

// 分类选项
const categoryOptions = ref([])

const cascaderProps = {
  value: 'id',      // 使用id字段，与后端返回的数据结构匹配
  label: 'name',    // 使用name字段
  children: 'children',
  expandTrigger: 'hover',
  emitPath: false   // 只返回选中节点的值，不返回路径数组
}

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '商品名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  stock: [
    { required: true, message: '请输入初始库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存不能小于0', trigger: 'blur' }
  ],
  stockAlert: [
    { required: true, message: '请输入库存预警值', trigger: 'blur' },
    { type: 'number', min: 0, message: '预警值不能小于0', trigger: 'blur' }
  ],
  originalPrice: [
    { required: true, message: '请输入原价', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能小于0', trigger: 'blur' }
  ]
}

// 文件上传前验证
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片格式文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('上传图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 图片上传
const uploadImage = async (options) => {
  try {
    const formData = new FormData()
    formData.append('file', options.file)
    formData.append('folder', 'product')
    
    const response = await request.post('/api/upload/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (response.code === 200 && response.data) {
      // 将返回的图片URL添加到数组中
      productForm.imageUrls.push(response.data.url || response.data)
      ElMessage.success('图片上传成功')
    } else {
      ElMessage.error('图片上传失败')
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败')
  }
}

// 图片预览
const handlePictureCardPreview = (file) => {
  previewImage.value = file.url || file.url
  previewVisible.value = true
}

// 移除图片
const handleRemove = (file, fileList) => {
  productForm.images = fileList
  // 同时从imageUrls中移除
  const index = productForm.imageUrls.indexOf(file.url || file.url)
  if (index > -1) {
    productForm.imageUrls.splice(index, 1)
  }
}

// 添加加料
const addAddon = () => {
  productForm.addons.push({
    name: '',
    price: 0,
    stock: 0
  })
}

// 删除加料
const removeAddon = (index) => {
  productForm.addons.splice(index, 1)
}

// 添加杯型
const addSize = () => {
  productForm.sizes.push({
    name: '',
    price: 0,
    capacity: ''
  })
}

// 删除杯型
const removeSize = (index) => {
  productForm.sizes.splice(index, 1)
}

// 获取当前商家ID
const getCurrentMerchantId = () => {
  try {
    const user = localStorage.getItem('user')
    if (user) {
      const userInfo = JSON.parse(user)
      return userInfo.id
    }
    return null
  } catch (error) {
    console.error('获取商家ID失败:', error)
    return null
  }
}

// 提交商品
const submitProduct = async () => {
  if (!productFormRef.value) return
  
  try {
    await productFormRef.value.validate()
    
    // 验证图片
    if (productForm.imageUrls.length === 0) {
      ElMessage.warning('请至少上传一张商品图片')
      return
    }
    
    saving.value = true
    
    const merchantId = getCurrentMerchantId()
    if (!merchantId) {
      ElMessage.error('无法获取商家信息，请重新登录')
      return
    }
    
    // 验证分类ID（由于设置了 emitPath: false，这里应该已经是单个ID值）
    const categoryId = productForm.categoryId
    
    if (!categoryId) {
      ElMessage.error('请选择商品分类')
      return
    }
    
    // 构建提交数据
    const submitData = {
      name: productForm.name,
      categoryId: categoryId,  // 确保是单个ID值
      description: productForm.description,
      originalPrice: productForm.originalPrice,
      memberPrice: productForm.memberPrice || 0,
      stock: productForm.stock,
      stockAlert: productForm.stockAlert,
      images: productForm.imageUrls,
      sweetnessOptions: productForm.sweetnessOptions,
      temperatureOptions: productForm.temperatureOptions,
      addons: productForm.addons.filter(addon => addon.name), // 只包含有名称的加料
      sizes: productForm.sizes.filter(size => size.name), // 只包含有名称的杯型
      isPromotion: productForm.promotionTypes.length > 0,
      promotionTypes: productForm.promotionTypes
    }
    
    console.log('提交数据:', submitData)
    
    const response = await createProduct(merchantId, submitData)
    
    if (response.code === 200) {
      ElMessage.success('商品创建成功！')
      
      // 重置表单
      resetForm()
      
      // 跳转到商品列表
      router.push('/merchant/product/list')
    }
  } catch (error) {
    console.error('提交失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('提交失败，请检查表单信息')
    }
  } finally {
    saving.value = false
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(productForm, {
    name: '',
    categoryId: null,
    description: '',
    images: [],
    imageUrls: [],
    sweetnessOptions: ['full-sugar', 'seven-sugar'],
    temperatureOptions: ['ice', 'room-temp', 'hot'],
    addons: [
      { name: '珍珠', price: 2.00, stock: 100 },
      { name: '椰果', price: 2.00, stock: 80 }
    ],
    sizes: [
      { name: '中杯', price: 0, capacity: '500ml' },
      { name: '大杯', price: 3.00, capacity: '700ml' }
    ],
    stock: 100,
    stockAlert: 10,
    originalPrice: 0,
    memberPrice: 0,
    promotionTypes: []
  })
  
  // 重置表单验证
  if (productFormRef.value) {
    productFormRef.value.clearValidate()
  }
}

// 加载分类列表
const loadCategories = async () => {
  categoryLoading.value = true
  try {
    console.log('开始加载分类列表...')
    const response = await getProductCategories()
    console.log('分类API响应:', response)
    
    if (response.code === 200 && response.data) {
      // 直接使用后端返回的数据，确保数据结构正确
      categoryOptions.value = response.data
      console.log('分类数据已加载:', response.data)
      console.log('分类数量:', categoryOptions.value.length)
      
      // 检查数据格式
      if (categoryOptions.value.length > 0) {
        const firstCategory = categoryOptions.value[0]
        console.log('第一个分类示例:', {
          id: firstCategory.id,
          name: firstCategory.name,
          hasChildren: !!firstCategory.children
        })
      }
    } else {
      console.warn('分类数据为空或异常:', response)
      categoryOptions.value = []
    }
  } catch (error) {
    console.error('加载分类失败:', error)
    ElMessage.error('加载分类失败: ' + (error.message || '未知错误'))
    categoryOptions.value = []
  } finally {
    categoryLoading.value = false
  }
}

// 初始化
onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
/* 样式保持不变，从原有文件复制 */
.page-container {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.page-description {
  margin: 0;
  color: #666;
  font-size: 14px;
}

/* 表单样式 */
.product-form {
  max-width: 1000px;
  margin: 0 auto;
}

.form-section {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  margin-left: 10px;
  color: #666;
  font-size: 12px;
}

.upload-tip {
  margin-top: 8px;
  color: #999;
  font-size: 12px;
}

/* 选项组样式 */
.option-group {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

/* 加料和杯型管理 */
.addon-section, .size-section {
  margin-bottom: 15px;
}

.addon-item, .size-item {
  margin-bottom: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.add-addon-btn, .add-size-btn {
  margin-top: 10px;
  width: 100%;
}

/* 表单操作按钮 */
.form-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

/* 图片预览 */
.preview-container {
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-container {
    padding: 10px;
  }
  
  .form-actions {
    flex-direction: column;
    gap: 10px;
  }
  
  .option-group {
    flex-direction: column;
    gap: 10px;
  }
}

/* 动画效果 */
.el-card {
  transition: all 0.3s ease;
}

.el-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.el-button {
  transition: all 0.3s ease;
}

/* 加载状态 */
.el-loading-mask {
  border-radius: 8px;
}

/* 自定义上传样式 */
:deep(.el-upload--picture-card) {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  border: 2px dashed #d9d9d9;
  transition: all 0.3s;
}

:deep(.el-upload--picture-card:hover) {
  border-color: #409eff;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 120px;
  height: 120px;
  border-radius: 8px;
}

/* 级联选择器样式 */
:deep(.el-cascader) {
  width: 100%;
}

/* 数字输入框样式 */
:deep(.el-input-number) {
  width: 100%;
}

/* 复选框组样式 */
:deep(.el-checkbox-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

:deep(.el-checkbox) {
  margin-right: 0;
}

/* 对话框样式 */
:deep(.el-dialog) {
  border-radius: 8px;
}

:deep(.el-dialog__header) {
  background: #f8f9fa;
  border-radius: 8px 8px 0 0;
}
</style>
