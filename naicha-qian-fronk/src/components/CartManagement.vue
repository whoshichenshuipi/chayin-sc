<template>
  <div class="cart-management">
    <!-- 购物车管理表单 -->
    <el-card class="management-card">
      <template #header>
        <div class="card-header">
          <h3>购物车管理</h3>
          <div class="header-actions">
            <el-button type="primary" @click="refreshCart">
              <el-icon><Refresh /></el-icon>
              刷新状态
            </el-button>
            <el-button type="warning" @click="clearAllAlerts">
              <el-icon><Bell /></el-icon>
              清除提醒
            </el-button>
          </div>
        </div>
      </template>

      <!-- 商品状态统计 -->
      <div class="status-stats">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-statistic title="总商品数" :value="cartStore.getTotalCount" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="有效商品" :value="cartStore.getValidItems.length" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="价格变动" :value="cartStore.getPriceChangedItems.length" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="库存不足" :value="cartStore.getLowStockItems.length" />
          </el-col>
        </el-row>
      </div>

      <!-- 商品编辑表单 -->
      <div class="edit-forms">
        <el-form :model="editForm" label-width="100px" class="edit-form">
          <el-form-item label="批量操作">
            <el-button-group>
              <el-button @click="selectAllValid">全选有效商品</el-button>
              <el-button @click="unselectAll">取消全选</el-button>
              <el-button type="danger" @click="batchDeleteSelected">删除选中</el-button>
            </el-button-group>
          </el-form-item>

          <el-form-item label="清空选项">
            <el-radio-group v-model="clearOption">
              <el-radio label="all">清空全部</el-radio>
              <el-radio label="discontinued">仅清空下架商品</el-radio>
              <el-radio label="lowStock">仅清空库存不足商品</el-radio>
            </el-radio-group>
            <el-button type="warning" @click="executeClear">执行清空</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 商品列表编辑 -->
      <div class="item-editor">
        <el-table :data="cartStore.getCartItems" style="width: 100%">
          <el-table-column type="selection" width="55" />
          <el-table-column label="商品信息" min-width="200">
            <template #default="{ row }">
              <div class="product-cell">
                <img :src="row.image" :alt="row.name" class="product-image" />
                <div class="product-info">
                  <h4>{{ row.name }}</h4>
                  <p>{{ row.description }}</p>
                  <div v-if="row.selectedSpecs" class="specs">
                    <el-tag 
                      v-for="(value, key) in row.selectedSpecs" 
                      :key="key" 
                      size="small"
                    >
                      {{ key }}: {{ value }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <div class="status-indicators">
                <el-tag v-if="row.hasPriceChanged" type="warning" size="small">价格变动</el-tag>
                <el-tag v-if="row.stockStatus === 'low'" type="danger" size="small">库存不足</el-tag>
                <el-tag v-if="row.isDiscontinued" type="info" size="small">已下架</el-tag>
                <el-tag v-else type="success" size="small">正常</el-tag>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="价格" width="100">
            <template #default="{ row }">
              <div v-if="row.hasPriceChanged" class="price-change">
                <div class="old-price">¥{{ row.originalPrice }}</div>
                <div class="new-price">¥{{ row.price }}</div>
              </div>
              <div v-else class="price">¥{{ row.price }}</div>
            </template>
          </el-table-column>
          
          <el-table-column label="数量" width="120">
            <template #default="{ row }">
              <el-input-number
                v-model="row.quantity"
                :min="1"
                :max="row.stock"
                :disabled="row.isDiscontinued"
                @change="updateQuantity(row)"
                size="small"
              />
            </template>
          </el-table-column>
          
          <el-table-column label="小计" width="100">
            <template #default="{ row }">
              <span class="subtotal">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button
                type="danger"
                size="small"
                @click="removeItem(row.cartItemId)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { Refresh, Bell } from '@element-plus/icons-vue'

const cartStore = useCartStore()

const editForm = ref({
  selectedItems: []
})

const clearOption = ref('all')

// 全选有效商品
const selectAllValid = () => {
  cartStore.getValidItems.forEach(item => {
    item.selected = true
  })
  ElMessage.success('已全选有效商品')
}

// 取消全选
const unselectAll = () => {
  cartStore.getCartItems.forEach(item => {
    item.selected = false
  })
  ElMessage.success('已取消全选')
}

// 批量删除选中商品
const batchDeleteSelected = async () => {
  const selectedItems = cartStore.getCartItems.filter(item => item.selected)
  if (selectedItems.length === 0) {
    ElMessage.warning('请先选择要删除的商品')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedItems.length} 件商品吗？`,
      '批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const itemIds = selectedItems.map(item => item.cartItemId)
    cartStore.removeMultipleItems(itemIds)
    ElMessage.success(`已删除 ${itemIds.length} 件商品`)
  } catch {
    // 用户取消删除
  }
}

// 执行清空操作
const executeClear = async () => {
  let message = ''
  let confirmMessage = ''
  
  switch (clearOption.value) {
    case 'all':
      message = '确定要清空所有商品吗？'
      confirmMessage = '全部清空'
      break
    case 'discontinued':
      message = '确定要清空所有已下架商品吗？'
      confirmMessage = '清空下架商品'
      break
    case 'lowStock':
      message = '确定要清空所有库存不足商品吗？'
      confirmMessage = '清空库存不足商品'
      break
  }
  
  try {
    await ElMessageBox.confirm(message, '清空购物车', {
      confirmButtonText: confirmMessage,
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    switch (clearOption.value) {
      case 'all':
        cartStore.clearCart(false)
        ElMessage.success('已清空所有商品')
        break
      case 'discontinued':
        const discontinuedIds = cartStore.getDiscontinuedItems.map(item => item.cartItemId)
        cartStore.removeMultipleItems(discontinuedIds)
        ElMessage.success(`已清空 ${discontinuedIds.length} 件下架商品`)
        break
      case 'lowStock':
        const lowStockIds = cartStore.getLowStockItems.map(item => item.cartItemId)
        cartStore.removeMultipleItems(lowStockIds)
        ElMessage.success(`已清空 ${lowStockIds.length} 件库存不足商品`)
        break
    }
  } catch {
    // 用户取消操作
  }
}

// 更新商品数量
const updateQuantity = async (item) => {
  try {
    await cartStore.updateQuantity(item.cartItemId, item.quantity)
  } catch (error) {
    ElMessage.error(error.message)
    // 恢复原数量
    item.quantity = Math.min(item.quantity, item.stock)
  }
}

// 删除单个商品
const removeItem = async (cartItemId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    cartStore.removeFromCart(cartItemId)
    ElMessage.success('已删除商品')
  } catch {
    // 用户取消删除
  }
}

// 刷新购物车状态
const refreshCart = () => {
  cartStore.checkItemStatus()
  ElMessage.success('购物车状态已刷新')
}

// 清除所有提醒
const clearAllAlerts = () => {
  cartStore.clearAlerts('all')
  ElMessage.success('已清除所有提醒')
}
</script>

<style scoped>
.cart-management {
  padding: 20px;
}

.management-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.status-stats {
  margin-bottom: 24px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.edit-forms {
  margin-bottom: 24px;
}

.edit-form {
  background-color: #fafafa;
  padding: 20px;
  border-radius: 8px;
}

.item-editor {
  margin-top: 20px;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}

.product-info h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  color: #333;
}

.product-info p {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: #666;
  line-height: 1.4;
}

.specs {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.status-indicators {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.price-change {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.old-price {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
}

.new-price {
  font-size: 14px;
  font-weight: bold;
  color: #ff6b6b;
}

.price {
  font-size: 14px;
  font-weight: bold;
  color: #ff6b6b;
}

.subtotal {
  font-size: 14px;
  font-weight: bold;
  color: #ff6b6b;
}
</style>
