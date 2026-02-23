<template>
  <div class="page-container">
    <div class="page-header">
      <h2>商品分类管理</h2>
      <p class="page-description">管理系统一级分类和店铺自定义二级分类，支持拖拽排序</p>
      <el-button type="primary" @click="addCategory">
        <el-icon><Plus /></el-icon>
        添加分类
      </el-button>
    </div>
    
    <el-card>
      <!-- 分类树形结构 -->
      <div class="category-tree">
        <el-tree
          ref="categoryTreeRef"
          :data="categoryTree"
          :props="treeProps"
          node-key="id"
          :expand-on-click-node="false"
          :draggable="true"
          :allow-drop="allowDrop"
          :allow-drag="allowDrag"
          @node-drop="handleNodeDrop"
          class="category-tree-component"
        >
          <template #default="{ node, data }">
            <div class="tree-node">
              <div class="node-content">
                <el-icon class="node-icon">
                  <Folder v-if="data.level === 1" />
                  <Document v-else />
                </el-icon>
                <span class="node-label">{{ data.name }}</span>
                <el-tag v-if="data.productCount > 0" size="small" type="info">
                  {{ data.productCount }}个商品
                </el-tag>
                <el-tag 
                  :type="data.status === 1 ? 'success' : 'info'" 
                  size="small"
                >
                  {{ data.statusText || (data.status === 1 ? '启用' : '禁用') }}
                </el-tag>
              </div>
              <div class="node-actions">
                <el-button type="text" size="small" @click="editCategory(data)">
                  编辑
                </el-button>
                <el-button 
                  type="text" 
                  size="small" 
                  @click="toggleStatus(data)"
                  :class="data.status === 1 ? 'text-warning' : 'text-success'"
                >
                  {{ data.status === 1 ? '禁用' : '启用' }}
                </el-button>
                <el-button 
                  type="text" 
                  size="small" 
                  @click="deleteCategory(data)" 
                  class="text-danger"
                  :disabled="data.productCount > 0"
                >
                  删除
                </el-button>
              </div>
            </div>
          </template>
        </el-tree>
      </div>
    </el-card>
    
    <!-- 添加/编辑分类对话框 -->
    <el-dialog v-model="categoryDialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="categoryForm" :rules="categoryRules" ref="categoryFormRef" label-width="100px">
        <el-form-item label="上级分类" prop="parentId">
          <el-select v-model="categoryForm.parentId" placeholder="选择上级分类（留空为一级分类）" clearable>
            <el-option 
              v-for="category in parentCategories" 
              :key="category.id" 
              :label="category.name" 
              :value="category.id" 
            />
          </el-select>
          <span class="form-tip">留空或选择0表示一级分类</span>
        </el-form-item>
        
        <el-form-item label="分类名称" prop="name" required>
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        
        <el-form-item label="分类描述" prop="description">
          <el-input 
            v-model="categoryForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入分类描述"
          />
        </el-form-item>
        
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="categoryForm.sort" :min="0" placeholder="排序值" />
          <span class="form-tip">数值越小排序越靠前</span>
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="categoryForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="handleDialogClose">取消</el-button>
        <el-button type="primary" @click="saveCategory" :loading="categorySaving">
          保存
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 删除确认对话框 -->
    <el-dialog v-model="deleteDialogVisible" title="删除确认" width="500px">
      <div class="delete-warning">
        <el-icon class="warning-icon"><Warning /></el-icon>
        <div class="warning-content">
          <h4>确定要删除分类 "{{ currentCategory?.name }}" 吗？</h4>
          <p v-if="currentCategory?.productCount > 0" class="warning-text">
            该分类下还有 {{ currentCategory.productCount }} 个商品，无法删除！
          </p>
          <p v-else class="warning-text">
            删除后不可恢复，请谨慎操作。
          </p>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button 
          type="danger" 
          @click="confirmDelete" 
          :loading="deleteSaving"
          :disabled="currentCategory?.productCount > 0"
        >
          确定删除
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Folder, Document, Warning } from '@element-plus/icons-vue'
import {
  getCategoryTree,
  getCategoryById,
  createCategory,
  updateCategory,
  deleteCategory as deleteCategoryAPI,
  updateCategoryStatus,
  updateCategorySort
} from '@/api/category'

const loading = ref(false)
const categorySaving = ref(false)
const deleteSaving = ref(false)

// 对话框状态
const categoryDialogVisible = ref(false)
const deleteDialogVisible = ref(false)

// 表单引用
const categoryFormRef = ref(null)
const categoryTreeRef = ref(null)

// 当前操作的分类
const currentCategory = ref(null)
const isEdit = ref(false)

// 分类树数据
const categoryTree = ref([])

// 树形组件配置
const treeProps = {
  children: 'children',
  label: 'name'
}

// 分类表单
const categoryForm = reactive({
  id: null,
  parentId: null,
  name: '',
  description: '',
  sort: 0,
  status: 1
})

// 表单验证规则
const categoryRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 20, message: '分类名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 上级分类选项（只显示一级分类，且排除自己和子分类）
const parentCategories = computed(() => {
  const parents = []
  const excludeIds = new Set()
  
  // 编辑时排除自己及其所有子分类
  if (isEdit.value && currentCategory.value) {
    excludeIds.add(currentCategory.value.id)
    // 递归添加所有子分类ID
    const collectChildIds = (node) => {
      if (node.children && node.children.length > 0) {
        node.children.forEach(child => {
          excludeIds.add(child.id)
          collectChildIds(child)
        })
      }
    }
    // 找到当前分类节点
    const findCategoryInTree = (nodes, targetId) => {
      for (const node of nodes) {
        if (node.id === targetId) {
          return node
        }
        if (node.children) {
          const found = findCategoryInTree(node.children, targetId)
          if (found) return found
        }
      }
      return null
    }
    const currentCategoryNode = findCategoryInTree(categoryTree.value, currentCategory.value.id)
    if (currentCategoryNode) {
      collectChildIds(currentCategoryNode)
    }
  }
  
  categoryTree.value.forEach(category => {
    if (category.level === 1 && !excludeIds.has(category.id)) {
      parents.push({
        id: category.id,
        name: category.name
      })
    }
  })
  return parents
})

// 对话框标题
const dialogTitle = computed(() => {
  return isEdit.value ? '编辑分类' : '添加分类'
})

// 重置表单
const resetCategoryForm = () => {
  Object.assign(categoryForm, {
    id: null,
    parentId: null,
    name: '',
    description: '',
    sort: 0,
    status: 1
  })
  // 清除表单验证状态
  if (categoryFormRef.value) {
    categoryFormRef.value.clearValidate()
  }
}

// 添加分类
const addCategory = () => {
  isEdit.value = false
  currentCategory.value = null
  resetCategoryForm()
  categoryDialogVisible.value = true
}

// 编辑分类
const editCategory = async (data) => {
  isEdit.value = true
  currentCategory.value = data
  
  // 从后端获取最新的分类详情，确保数据完整
  try {
    const res = await getCategoryById(data.id)
    if (res.code === 200 && res.data) {
      const categoryData = res.data
      Object.assign(categoryForm, {
        id: categoryData.id,
        parentId: categoryData.parentId || null,
        name: categoryData.name || '',
        description: categoryData.description || '',
        sort: categoryData.sort || 0,
        status: categoryData.status !== undefined ? categoryData.status : 1
      })
    } else {
      // 如果获取详情失败，使用树节点数据
      Object.assign(categoryForm, {
        id: data.id,
        parentId: data.parentId || null,
        name: data.name || '',
        description: data.description || '',
        sort: data.sort || 0,
        status: data.status !== undefined ? data.status : 1
      })
    }
  } catch (error) {
    console.error('获取分类详情失败:', error)
    // 如果获取详情失败，使用树节点数据
    Object.assign(categoryForm, {
      id: data.id,
      parentId: data.parentId || null,
      name: data.name || '',
      description: data.description || '',
      sort: data.sort || 0,
      status: data.status !== undefined ? data.status : 1
    })
  }
  
  categoryDialogVisible.value = true
}

// 保存分类
const saveCategory = async () => {
  if (!categoryFormRef.value) return
  
  try {
    await categoryFormRef.value.validate()
    categorySaving.value = true
    
    // 统一处理 parentId：null、0 或 undefined 都转换为 null（后端会将 null 视为 0）
    const submitData = {
      ...categoryForm,
      parentId: categoryForm.parentId && categoryForm.parentId > 0 ? categoryForm.parentId : null
    }
    
    if (isEdit.value) {
      // 更新分类
      await updateCategory(categoryForm.id, submitData)
      ElMessage.success('分类更新成功')
    } else {
      // 添加分类
      await createCategory(submitData)
      ElMessage.success('分类添加成功')
    }
    
    categoryDialogVisible.value = false
    // 重置表单
    resetCategoryForm()
    await loadCategoryTree()
  } catch (error) {
    console.error('保存分类失败:', error)
    ElMessage.error(error.message || '保存失败，请检查表单信息')
  } finally {
    categorySaving.value = false
  }
}

// 加载分类树
const loadCategoryTree = async () => {
  loading.value = true
  try {
    const res = await getCategoryTree()
    console.log('分类API响应:', res)
    if (res.code === 200 && res.data) {
      categoryTree.value = res.data || []
      console.log('分类树数据:', categoryTree.value)
    } else {
      console.warn('分类数据为空或异常:', res)
    }
  } catch (error) {
    console.error('加载分类失败:', error)
    ElMessage.error('加载分类列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 切换状态
const toggleStatus = async (data) => {
  const newStatus = data.status === 1 ? 0 : 1
  const action = data.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}该分类吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await updateCategoryStatus(data.id, newStatus)
    ElMessage.success(`${action}成功`)
    await loadCategoryTree()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || `${action}失败`)
    }
  }
}

// 删除分类
const deleteCategory = (data) => {
  currentCategory.value = data
  deleteDialogVisible.value = true
}

// 处理对话框关闭
const handleDialogClose = () => {
  categoryDialogVisible.value = false
  resetCategoryForm()
}

// 确认删除
const confirmDelete = async () => {
  if (currentCategory.value.productCount > 0) {
    ElMessage.warning('该分类下还有商品，无法删除！')
    return
  }
  
  deleteSaving.value = true
  try {
    await deleteCategoryAPI(currentCategory.value.id)
    ElMessage.success('删除成功')
    deleteDialogVisible.value = false
    await loadCategoryTree()
  } catch (error) {
    ElMessage.error(error.message || '删除失败，请重试')
  } finally {
    deleteSaving.value = false
  }
}

// 拖拽相关
const allowDrop = (draggingNode, dropNode, type) => {
  // 只允许同级拖拽
  return draggingNode.data.level === dropNode.data.level
}

const allowDrag = (draggingNode) => {
  // 允许拖拽所有节点
  return true
}

// 处理拖拽完成
const handleNodeDrop = async (draggingNode, dropNode, dropType, ev) => {
  try {
    // 收集所有分类的ID（按排序顺序）
    const collectIds = (nodes) => {
      const ids = []
      nodes.forEach(node => {
        ids.push(node.id)
        if (node.children) {
          ids.push(...collectIds(node.children))
        }
      })
      return ids
    }
    
    const ids = collectIds(categoryTree.value)
    await updateCategorySort(ids)
    ElMessage.success('排序更新成功')
  } catch (error) {
    ElMessage.error(error.message || '排序更新失败')
  }
}

// 初始化
onMounted(() => {
  loadCategoryTree()
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
  align-items: center;
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

/* 分类树样式 */
.category-tree {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.category-tree-component {
  background: transparent;
}

.tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.tree-node:last-child {
  border-bottom: none;
}

.node-content {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.node-icon {
  font-size: 16px;
  color: #409eff;
}

.node-label {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.node-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s;
}

.tree-node:hover .node-actions {
  opacity: 1;
}

/* 表单样式 */
.form-tip {
  margin-left: 10px;
  color: #666;
  font-size: 12px;
}

/* 删除警告样式 */
.delete-warning {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 20px;
  background: #fef0f0;
  border: 1px solid #fbc4c4;
  border-radius: 6px;
}

.warning-icon {
  font-size: 24px;
  color: #f56c6c;
  margin-top: 2px;
}

.warning-content {
  flex: 1;
}

.warning-content h4 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 16px;
}

.warning-text {
  margin: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

/* 文本颜色 */
.text-warning {
  color: #E6A23C;
}

.text-success {
  color: #67C23A;
}

.text-danger {
  color: #F56C6C;
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
  
  .tree-node {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  
  .node-content {
    justify-content: space-between;
  }
  
  .node-actions {
    opacity: 1;
    justify-content: center;
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

/* 树形组件样式 */
:deep(.el-tree) {
  background: transparent;
}

:deep(.el-tree-node__content) {
  height: auto;
  padding: 0;
  border-radius: 4px;
  transition: all 0.3s;
}

:deep(.el-tree-node__content:hover) {
  background: #f5f7fa;
}

:deep(.el-tree-node__expand-icon) {
  display: none;
}

:deep(.el-tree-node__label) {
  font-size: 14px;
}

/* 标签样式 */
:deep(.el-tag) {
  border-radius: 4px;
  font-size: 12px;
}

/* 对话框样式 */
:deep(.el-dialog) {
  border-radius: 8px;
}

:deep(.el-dialog__header) {
  background: #f8f9fa;
  border-radius: 8px 8px 0 0;
}

/* 表单样式 */
:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-select) {
  width: 100%;
}

/* 加载状态 */
.el-loading-mask {
  border-radius: 8px;
}

/* 拖拽样式 */
:deep(.el-tree-node.is-drop-inner) {
  background: #e6f7ff;
  border: 1px dashed #409eff;
}

:deep(.el-tree-node.is-drop-inner .el-tree-node__content) {
  background: #e6f7ff;
}
</style>
