<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>商品管理</h2>
        <p class="page-description">管理商品信息、库存状态和销售数据</p>
      </div>
      <el-button type="primary" @click="addProduct">
        <el-icon><Plus /></el-icon>
        新增商品
      </el-button>
    </div>
    
    <el-card>
      <!-- 搜索条件 -->
      <div class="search-form">
        <el-form :model="searchForm" inline>
          <el-form-item label="商品名称">
            <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="分类">
            <el-cascader
              v-model="searchForm.categoryId"
              :options="categoryOptions"
              :props="cascaderProps"
              placeholder="请选择分类"
              clearable
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 140px">
              <el-option label="上架" :value="1" />
              <el-option label="下架" :value="0" />
              <el-option label="预售" :value="2" />
              <el-option label="审核中" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="库存状态">
            <el-select v-model="searchForm.stockStatus" placeholder="库存状态" clearable style="width: 120px">
              <el-option label="库存充足" value="sufficient" />
              <el-option label="库存不足" value="low" />
              <el-option label="缺货" value="out" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 商品列表 -->
      <el-table :data="productList" v-loading="loading" class="product-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="images" label="图片" width="100">
          <template #default="{ row }">
            <el-image 
              :src="getProductImage(row)" 
              :preview-src-list="getProductImages(row)"
              style="width: 60px; height: 60px; border-radius: 4px;"
              fit="cover"
              preview-teleported
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="150">
          <template #default="{ row }">
            <div class="product-info">
              <div class="product-name">{{ row.name }}</div>
              <div class="product-desc">{{ row.description }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="price" label="价格" width="120">
          <template #default="{ row }">
            <div class="price-info">
              <div class="original-price">¥{{ row.originalPrice }}</div>
              <div v-if="row.memberPrice" class="member-price">会员价：¥{{ row.memberPrice }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100">
          <template #default="{ row }">
            <div class="stock-info">
              <div class="stock-count" :class="getStockClass(row.stock, row.stockAlert)">
                {{ row.stock }}
              </div>
              <div class="stock-status">{{ getStockStatus(row.stock, row.stockAlert) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="150" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" link size="small" @click="editProduct(row)">编辑</el-button>
              <el-button type="primary" link size="small" @click="adjustStock(row)">调库存</el-button>
              <el-dropdown @command="(command) => handleStatusAction(command, row)">
                <el-button type="primary" link size="small">
                  状态管理<el-icon><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="toggle" :disabled="row.status === 3">
                      {{ row.status === 1 ? '下架' : '上架' }}
                    </el-dropdown-item>
                    <el-dropdown-item command="presale" :disabled="row.status === 3">
                      设为预售
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button type="danger" link size="small" @click="deleteProduct(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
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
    </el-card>
    
    <!-- 编辑商品对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑商品" width="80%" top="5vh">
      <div class="edit-dialog">
        <el-tabs v-model="activeTab" type="border-card">
          <el-tab-pane label="基本信息" name="basic">
            <el-form :model="editForm" :rules="editFormRules" ref="editFormRef" label-width="120px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="商品名称" prop="name">
                    <el-input v-model="editForm.name" placeholder="请输入商品名称" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="商品分类" prop="categoryId">
                    <el-cascader
                      v-model="editForm.categoryId"
                      :options="categoryOptions"
                      :props="cascaderProps"
                      placeholder="请选择分类"
                      style="width: 100%"
                      filterable
                      clearable
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="商品简介" prop="description">
                <el-input 
                  v-model="editForm.description" 
                  type="textarea" 
                  :rows="3"
                  placeholder="请输入商品简介"
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>
            </el-form>
          </el-tab-pane>
          
          <el-tab-pane label="价格设置" name="price">
            <el-form :model="editForm" :rules="editFormRules" ref="editFormRef" label-width="120px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="原价" prop="originalPrice">
                    <el-input-number 
                      v-model="editForm.originalPrice" 
                      :min="0.01" 
                      :precision="2" 
                      style="width: 100%"
                      placeholder="请输入原价"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="会员价">
                    <el-input-number 
                      v-model="editForm.memberPrice" 
                      :min="0" 
                      :precision="2" 
                      style="width: 100%"
                      placeholder="请输入会员价（可选）"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-tab-pane>
          
          <el-tab-pane label="库存管理" name="stock">
            <el-form :model="editForm" :rules="editFormRules" ref="editFormRef" label-width="120px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="当前库存" prop="stock">
                    <el-input-number 
                      v-model="editForm.stock" 
                      :min="0" 
                      style="width: 100%"
                      placeholder="请输入库存"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="库存预警值" prop="stockAlert">
                    <el-input-number 
                      v-model="editForm.stockAlert" 
                      :min="0" 
                      style="width: 100%"
                      placeholder="请输入库存预警值"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
      <template #footer>
        <el-button @click="handleEditDialogClose">取消</el-button>
        <el-button type="primary" @click="saveEdit" :loading="editSaving">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 调整库存对话框 -->
    <el-dialog v-model="stockDialogVisible" title="调整库存" width="500px">
      <el-form :model="stockForm" label-width="100px">
        <el-form-item label="商品名称">
          <el-input v-model="stockForm.productName" disabled />
        </el-form-item>
        <el-form-item label="当前库存">
          <el-input-number v-model="stockForm.currentStock" disabled style="width: 100%" />
        </el-form-item>
        <el-form-item label="调整后库存" required>
          <el-input-number v-model="stockForm.stock" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmStockAdjustment" :loading="stockSaving">确认调整</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowDown } from '@element-plus/icons-vue'
import { getProductList, getProductDetail, updateProduct, onSaleProduct, offSaleProduct, adjustStock as adjustStockApi, deleteProduct as deleteProductApi, getProductCategories } from '@/api/product'

const router = useRouter()

const loading = ref(false)
const editSaving = ref(false)
const stockSaving = ref(false)

// 对话框状态
const editDialogVisible = ref(false)
const stockDialogVisible = ref(false)
const activeTab = ref('basic')

// 搜索表单
const searchForm = reactive({
  name: '',
  categoryId: null,
  status: null,
  stockStatus: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 商品列表
const productList = ref([])

// 分类选项
const categoryOptions = ref([])

const cascaderProps = {
  value: 'id',
  label: 'name',
  children: 'children',
  expandTrigger: 'hover',
  emitPath: false // 只返回选中节点的值，不返回路径数组
}

// 编辑表单
const editForm = reactive({
  id: null,
  name: '',
  categoryId: null,
  categoryName: '',
  description: '',
  originalPrice: 0,
  memberPrice: 0,
  stock: 0,
  stockAlert: 0,
  images: [],
  // 规格信息（保存时需要保留）
  sweetnessOptions: [],
  temperatureOptions: [],
  addons: [],
  sizes: [],
  isPromotion: false,
  promotionTypes: []
})

// 表单引用
const editFormRef = ref(null)

// 表单验证规则
const editFormRules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '商品名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  originalPrice: [
    { required: true, message: '请输入原价', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '原价必须大于0', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存不能为负数', trigger: 'blur' }
  ],
  stockAlert: [
    { required: true, message: '请输入库存预警值', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存预警值不能为负数', trigger: 'blur' }
  ]
}

// 库存调整表单
const stockForm = reactive({
  productId: null,
  productName: '',
  currentStock: 0,
  stock: 0
})

// 获取商品图片
const getProductImage = (row) => {
  if (row.images && row.images.length > 0) {
    return row.images[0]
  }
  return 'https://via.placeholder.com/60x60'
}

// 获取商品图片列表
const getProductImages = (row) => {
  if (row.images && row.images.length > 0) {
    return row.images
  }
  return [getProductImage(row)]
}

// 获取状态标签类型
const getStatusTag = (status) => {
  const tagMap = {
    0: 'info',      // 下架
    1: 'success',   // 上架
    2: 'warning',   // 预售
    3: 'primary'    // 审核中
  }
  return tagMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    0: '下架',
    1: '上架',
    2: '预售',
    3: '审核中'
  }
  return textMap[status] || '未知'
}

// 获取库存状态
const getStockStatus = (stock, alert) => {
  if (stock === 0) return '缺货'
  if (stock <= alert) return '库存不足'
  return '库存充足'
}

// 获取库存样式类
const getStockClass = (stock, alert) => {
  if (stock === 0) return 'stock-out'
  if (stock <= alert) return 'stock-low'
  return 'stock-normal'
}

// 搜索
const handleSearch = async () => {
  loading.value = true
  try {
    await loadProductList()
    ElMessage.success('搜索完成')
  } catch (error) {
    console.error('搜索失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置搜索
const handleReset = () => {
  searchForm.name = ''
  searchForm.categoryId = null
  searchForm.status = null
  searchForm.stockStatus = ''
  handleSearch()
}

// 获取当前商家ID
const getCurrentMerchantId = () => {
  try {
    const user = localStorage.getItem('user')
    if (user) {
      const userInfo = JSON.parse(user)
      // 商家登录时使用 id 作为 merchantId
      return userInfo.id
    }
    return null
  } catch (error) {
    console.error('获取商家ID失败:', error)
    return null
  }
}

// 加载商品列表
const loadProductList = async () => {
  try {
    const merchantId = getCurrentMerchantId()
    
    const params = {
      merchantId: merchantId,
      current: pagination.currentPage,
      size: pagination.pageSize,
      name: searchForm.name || undefined,
      categoryId: searchForm.categoryId || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined,
      stockStatus: searchForm.stockStatus || undefined
    }
    
    // 移除undefined的参数
    Object.keys(params).forEach(key => {
      if (params[key] === undefined || params[key] === null || params[key] === '') {
        delete params[key]
      }
    })
    
    console.log('查询参数:', params)
    
    const response = await getProductList(params)
    
    if (response.code === 200 && response.data) {
      productList.value = response.data.records || []
      pagination.total = response.data.total || 0
      console.log('查询结果:', response.data)
    }
  } catch (error) {
    console.error('加载商品列表失败:', error)
    ElMessage.error('加载商品列表失败')
  }
}

// 新增商品
const addProduct = () => {
  router.push('/merchant/product/add')
}

// 编辑商品
const editProduct = async (row) => {
  loading.value = true
  try {
    // 从后端获取完整的商品详情
    const res = await getProductDetail(row.id)
    if (res.code === 200 && res.data) {
      const productData = res.data
      Object.assign(editForm, {
        id: productData.id,
        name: productData.name || '',
        categoryId: productData.categoryId || null,
        categoryName: productData.categoryName || '',
        description: productData.description || '',
        originalPrice: productData.originalPrice || 0,
        memberPrice: productData.memberPrice || null,
        stock: productData.stock || 0,
        stockAlert: productData.stockAlert || 0,
        images: productData.images || [],
        // 保留规格信息
        sweetnessOptions: productData.sweetnessOptions || [],
        temperatureOptions: productData.temperatureOptions || [],
        addons: productData.addons || [],
        sizes: productData.sizes || [],
        isPromotion: productData.isPromotion || false,
        promotionTypes: productData.promotionTypes || []
      })
    } else {
      // 如果获取详情失败，使用列表中的数据
      Object.assign(editForm, {
        id: row.id,
        name: row.name || '',
        categoryId: row.categoryId || null,
        categoryName: row.categoryName || '',
        description: row.description || '',
        originalPrice: row.originalPrice || 0,
        memberPrice: row.memberPrice || null,
        stock: row.stock || 0,
        stockAlert: row.stockAlert || 0,
        images: row.images || [],
        // 列表数据可能没有规格信息，设为默认值
        sweetnessOptions: [],
        temperatureOptions: [],
        addons: [],
        sizes: [],
        isPromotion: false,
        promotionTypes: []
      })
    }
    
    editDialogVisible.value = true
    activeTab.value = 'basic'
    
    // 清除表单验证状态
    if (editFormRef.value) {
      editFormRef.value.clearValidate()
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败，请重试')
    // 失败时仍然使用列表数据打开编辑对话框
    Object.assign(editForm, {
      id: row.id,
      name: row.name || '',
      categoryId: row.categoryId || null,
      categoryName: row.categoryName || '',
      description: row.description || '',
      originalPrice: row.originalPrice || 0,
      memberPrice: row.memberPrice || null,
      stock: row.stock || 0,
      stockAlert: row.stockAlert || 0,
      images: row.images || [],
      // 列表数据可能没有规格信息，设为默认值
      sweetnessOptions: [],
      temperatureOptions: [],
      addons: [],
      sizes: [],
      isPromotion: false,
      promotionTypes: []
    })
    editDialogVisible.value = true
    activeTab.value = 'basic'
  } finally {
    loading.value = false
  }
}

// 调整库存
const adjustStock = (row) => {
  stockForm.productId = row.id
  stockForm.productName = row.name
  stockForm.currentStock = row.stock
  stockForm.stock = row.stock
  stockDialogVisible.value = true
}

// 状态管理操作
const handleStatusAction = async (command, row) => {
  switch (command) {
    case 'toggle':
      await toggleStatus(row)
      break
    case 'presale':
      // TODO: 实现预售功能
      ElMessage.info('预售功能待实现')
      break
  }
}

// 切换状态
const toggleStatus = async (row) => {
  const action = row.status === 1 ? '下架' : '上架'
  try {
    await ElMessageBox.confirm(`确定要${action}该商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    if (row.status === 1) {
      await offSaleProduct(row.id)
    } else {
      await onSaleProduct(row.id)
    }
    
    ElMessage.success(`${action}成功`)
    await loadProductList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${action}失败:`, error)
      ElMessage.error(`${action}失败`)
    }
  }
}

// 确认库存调整
const confirmStockAdjustment = async () => {
  if (stockForm.stock < 0) {
    ElMessage.warning('库存不能为负数')
    return
  }
  
  if (stockForm.stock === stockForm.currentStock) {
    ElMessage.warning('库存数量未发生变化')
    return
  }
  
  stockSaving.value = true
  try {
    await adjustStockApi(stockForm.productId, stockForm.stock)
    ElMessage.success('库存调整成功')
    stockDialogVisible.value = false
    await loadProductList()
  } catch (error) {
    console.error('库存调整失败:', error)
    ElMessage.error('库存调整失败')
  } finally {
    stockSaving.value = false
  }
}

// 重置编辑表单
const resetEditForm = () => {
  Object.assign(editForm, {
    id: null,
    name: '',
    categoryId: null,
    categoryName: '',
    description: '',
    originalPrice: 0,
    memberPrice: null,
    stock: 0,
    stockAlert: 0,
    images: [],
    sweetnessOptions: [],
    temperatureOptions: [],
    addons: [],
    sizes: [],
    isPromotion: false,
    promotionTypes: []
  })
  if (editFormRef.value) {
    editFormRef.value.clearValidate()
  }
}

// 保存编辑
const saveEdit = async () => {
  if (!editFormRef.value) return
  
  try {
    // 表单验证
    await editFormRef.value.validate()
    
    // 验证 categoryId
    if (!editForm.categoryId) {
      ElMessage.warning('请选择商品分类')
      return
    }
    
    editSaving.value = true
    
    // 准备更新数据
    const updateData = {
      name: editForm.name.trim(),
      categoryId: editForm.categoryId,
      description: editForm.description || '',
      originalPrice: editForm.originalPrice,
      memberPrice: editForm.memberPrice || null,
      stock: editForm.stock,
      stockAlert: editForm.stockAlert,
      images: editForm.images || [],
      // 保留从详情中获取的规格信息
      sweetnessOptions: editForm.sweetnessOptions || [],
      temperatureOptions: editForm.temperatureOptions || [],
      addons: editForm.addons || [],
      sizes: editForm.sizes || [],
      isPromotion: editForm.isPromotion || false,
      promotionTypes: editForm.promotionTypes || []
    }
    
    await updateProduct(editForm.id, updateData)
    ElMessage.success('商品更新成功')
    editDialogVisible.value = false
    resetEditForm()
    await loadProductList()
  } catch (error) {
    if (error !== false) { // 表单验证失败返回 false
      console.error('保存失败:', error)
      ElMessage.error(error.message || '保存失败，请重试')
    }
  } finally {
    editSaving.value = false
  }
}

// 处理对话框关闭
const handleEditDialogClose = () => {
  editDialogVisible.value = false
  resetEditForm()
}

// 删除商品
const deleteProduct = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？删除后不可恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteProductApi(row.id)
    ElMessage.success('删除成功')
    await loadProductList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 分页处理
const handleSizeChange = async (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  await loadProductList()
}

const handleCurrentChange = async (val) => {
  pagination.currentPage = val
  await loadProductList()
}

// 加载分类数据
const loadCategories = async () => {
  try {
    const response = await getProductCategories()
    if (response.code === 200 && response.data) {
      categoryOptions.value = response.data || []
    }
  } catch (error) {
    console.error('加载分类失败:', error)
    // 使用临时分类数据
    categoryOptions.value = [
      {
        id: 1,
        name: '经典奶茶',
        children: [
          { id: 11, name: '珍珠系列' },
          { id: 12, name: '椰果系列' }
        ]
      },
      {
        id: 2,
        name: '果茶',
        children: [
          { id: 21, name: '柑橘系列' },
          { id: 22, name: '浆果系列' }
        ]
      }
    ]
  }
}

// 初始化
onMounted(async () => {
  await Promise.all([
    loadCategories(),
    loadProductList()
  ])
})
</script>

<style scoped>
.page-container {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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

/* 搜索表单 */
.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* 商品表格 */
.product-table {
  border-radius: 8px;
  overflow: hidden;
}

.product-info .product-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.product-info .product-desc {
  font-size: 12px;
  color: #666;
  line-height: 1.4;
}

.price-info .original-price {
  font-weight: 500;
  color: #333;
  margin-bottom: 2px;
}

.price-info .member-price {
  font-size: 12px;
  color: #f56c6c;
}

.stock-info .stock-count {
  font-weight: 500;
  margin-bottom: 2px;
}

.stock-info .stock-count.stock-normal {
  color: #67c23a;
}

.stock-info .stock-count.stock-low {
  color: #e6a23c;
}

.stock-info .stock-count.stock-out {
  color: #f56c6c;
}

.stock-info .stock-status {
  font-size: 12px;
  color: #666;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* 编辑对话框 */
.edit-dialog {
  max-height: 600px;
  overflow-y: auto;
}

/* 分页 */
.pagination {
  margin-top: 20px;
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-container {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }
  
  .search-form {
    padding: 15px;
  }
  
  .action-buttons {
    flex-direction: column;
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

/* 表格样式优化 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table__header) {
  background: #f8f9fa;
}

:deep(.el-table__row:hover) {
  background: #f5f7fa;
}

/* 标签样式 */
:deep(.el-tag) {
  border-radius: 4px;
}

/* 下拉菜单样式 */
:deep(.el-dropdown-menu) {
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 对话框样式 */
:deep(.el-dialog) {
  border-radius: 8px;
}

:deep(.el-dialog__header) {
  background: #f8f9fa;
  border-radius: 8px 8px 0 0;
}

/* 标签页样式 */
:deep(.el-tabs--border-card) {
  border-radius: 6px;
}

:deep(.el-tabs--border-card > .el-tabs__header) {
  background: #f8f9fa;
  border-radius: 6px 6px 0 0;
}

/* 级联选择器样式 */
:deep(.el-cascader) {
  width: 100%;
}

/* 数字输入框样式 */
:deep(.el-input-number) {
  width: 100%;
}

/* 加载状态 */
:deep(.el-loading-mask) {
  border-radius: 8px;
}
</style>
