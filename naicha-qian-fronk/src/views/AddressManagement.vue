<template>
  <div class="address-management-container">
    <div class="container">
      <el-card class="address-card" v-loading="loading">
        <template #header>
          <div class="card-header">
            <span>收货地址管理</span>
            <el-button type="primary" @click="showAddAddress = true">
              <el-icon><Plus /></el-icon>
              添加地址
            </el-button>
          </div>
        </template>

        <div v-if="addressList.length === 0 && !loading" class="empty-address">
          <el-empty description="暂无收货地址">
            <el-button type="primary" @click="showAddAddress = true">
              添加第一个地址
            </el-button>
          </el-empty>
        </div>

        <div v-else-if="!loading" class="address-list">
          <div
            v-for="(address, index) in addressList"
            :key="address.id"
            class="address-item"
            :class="{ 'default-address': address.isDefault }"
          >
            <div class="address-info">
              <div class="address-header">
                <div class="receiver-info">
                  <span class="receiver-name">{{ address.receiverName }}</span>
                  <span class="receiver-phone">{{ address.receiverPhone }}</span>
                  <el-tag v-if="address.isDefault" type="danger" size="small">默认</el-tag>
                </div>
                <div class="address-actions">
                  <el-button
                    type="primary"
                    size="small"
                    @click="editAddress(address)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    @click="deleteAddress(address.id)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
              
              <div class="address-detail">
                <p>{{ address.fullAddress }}</p>
                <p v-if="address.doorNumber" class="door-number">
                  门牌号：{{ address.doorNumber }}
                </p>
              </div>
            </div>
            
            <div v-if="!address.isDefault" class="set-default">
              <el-button
                type="text"
                @click="setDefaultAddress(address.id)"
              >
                设为默认地址
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 添加/编辑地址对话框 -->
    <el-dialog
      v-model="showAddAddress"
      :title="editingAddress ? '编辑地址' : '添加地址'"
      width="600px"
      @close="resetAddressForm"
    >
      <el-form
        ref="addressFormRef"
        :model="addressForm"
        :rules="addressRules"
        label-width="100px"
        class="address-form"
      >
        <el-form-item label="收货人" prop="receiverName">
          <el-input
            v-model="addressForm.receiverName"
            placeholder="请输入收货人姓名"
            maxlength="20"
          />
        </el-form-item>

        <el-form-item label="手机号" prop="receiverPhone">
          <el-input
            v-model="addressForm.receiverPhone"
            placeholder="请输入手机号"
            maxlength="11"
          />
        </el-form-item>

        <el-form-item label="所在地区" prop="region">
          <el-cascader
            v-model="addressForm.region"
            :options="regionOptions"
            placeholder="请选择省市区"
            style="width: 100%"
            :props="cascaderProps"
          />
        </el-form-item>

        <el-form-item label="详细地址" prop="detailAddress">
          <el-input
            v-model="addressForm.detailAddress"
            type="textarea"
            placeholder="请输入详细地址（街道、小区、楼栋等）"
            :rows="3"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="门牌号" prop="doorNumber">
          <el-input
            v-model="addressForm.doorNumber"
            placeholder="请输入门牌号（选填）"
            maxlength="20"
          />
        </el-form-item>

        <el-form-item label="设为默认">
          <el-switch
            v-model="addressForm.isDefault"
            active-text="设为默认地址"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showAddAddress = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAddress" :loading="saving">
          {{ editingAddress ? '更新' : '添加' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getAddressList, 
  createAddress, 
  updateAddress, 
  deleteAddress as deleteAddressApi, 
  setDefaultAddress as setDefaultAddressApi 
} from '@/api/address'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const showAddAddress = ref(false)
const saving = ref(false)
const loading = ref(false)
const addressFormRef = ref()
const editingAddress = ref(null)

// 地址列表
const addressList = ref([])

// 地址表单
const addressForm = reactive({
  receiverName: '',
  receiverPhone: '',
  region: [],
  detailAddress: '',
  doorNumber: '',
  isDefault: false
})

// 省市区级联选择器配置
const cascaderProps = {
  value: 'value',
  label: 'label',
  children: 'children'
}

// 省市区数据（简化版）
const regionOptions = ref([
  {
    value: '北京市',
    label: '北京市',
    children: [
      {
        value: '北京市',
        label: '北京市',
        children: [
          { value: '朝阳区', label: '朝阳区' },
          { value: '海淀区', label: '海淀区' },
          { value: '西城区', label: '西城区' },
          { value: '东城区', label: '东城区' }
        ]
      }
    ]
  },
  {
    value: '上海市',
    label: '上海市',
    children: [
      {
        value: '上海市',
        label: '上海市',
        children: [
          { value: '浦东新区', label: '浦东新区' },
          { value: '黄浦区', label: '黄浦区' },
          { value: '静安区', label: '静安区' },
          { value: '徐汇区', label: '徐汇区' }
        ]
      }
    ]
  },
  {
    value: '广东省',
    label: '广东省',
    children: [
      {
        value: '深圳市',
        label: '深圳市',
        children: [
          { value: '南山区', label: '南山区' },
          { value: '福田区', label: '福田区' },
          { value: '罗湖区', label: '罗湖区' }
        ]
      },
      {
        value: '广州市',
        label: '广州市',
        children: [
          { value: '天河区', label: '天河区' },
          { value: '越秀区', label: '越秀区' },
          { value: '海珠区', label: '海珠区' }
        ]
      }
    ]
  }
])

// 表单验证规则
const addressRules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  region: [
    { required: true, message: '请选择所在地区', trigger: 'change' }
  ],
  detailAddress: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, max: 100, message: '详细地址长度在 5 到 100 个字符', trigger: 'blur' }
  ]
}

// 格式化后端地址数据到前端格式
const formatAddress = (address) => {
  // 处理不同的后端数据格式
  let region = []
  if (address.region && Array.isArray(address.region)) {
    region = address.region
  } else if (address.province || address.city || address.district) {
    region = [
      address.province || '',
      address.city || '',
      address.district || ''
    ].filter(Boolean)
  }
  
  // 构建完整地址
  const fullAddress = region.length > 0 
    ? region.join('') + (address.detailAddress || address.detail_address || '') + (address.doorNumber || address.door_number || '')
    : address.fullAddress || address.full_address || ''
  
  return {
    id: address.id,
    receiverName: address.receiverName || address.receiver_name || '',
    receiverPhone: address.receiverPhone || address.receiver_phone || '',
    region: region,
    detailAddress: address.detailAddress || address.detail_address || '',
    doorNumber: address.doorNumber || address.door_number || '',
    fullAddress: fullAddress,
    isDefault: address.isDefault !== undefined ? address.isDefault : (address.is_default === 1 || address.is_default === true),
    raw: address // 保存原始数据
  }
}

// 格式化前端数据到后端格式
const formatAddressForBackend = () => {
  const region = addressForm.region || []
  return {
    receiverName: addressForm.receiverName,
    receiverPhone: addressForm.receiverPhone,
    province: region[0] || '',
    city: region[1] || '',
    district: region[2] || '',
    detailAddress: addressForm.detailAddress,
    doorNumber: addressForm.doorNumber || '',
    isDefault: addressForm.isDefault || false
  }
}

// 加载地址列表
const loadAddressList = async () => {
  try {
    loading.value = true
    const response = await getAddressList()
    const list = Array.isArray(response) ? response : (response?.list || response?.records || [])
    addressList.value = list.map(formatAddress)
  } catch (error) {
    console.error('加载地址列表失败:', error)
    // 如果是404，说明接口不存在，使用空列表
    if (error.response?.status === 404) {
      addressList.value = []
      console.warn('地址管理接口不存在，请联系后端开发人员实现')
    } else {
      ElMessage.error('加载地址列表失败')
      addressList.value = []
    }
  } finally {
    loading.value = false
  }
}

// 编辑地址
const editAddress = (address) => {
  editingAddress.value = address
  Object.assign(addressForm, {
    receiverName: address.receiverName,
    receiverPhone: address.receiverPhone,
    region: address.region || [],
    detailAddress: address.detailAddress,
    doorNumber: address.doorNumber || '',
    isDefault: address.isDefault
  })
  showAddAddress.value = true
}

// 删除地址
const deleteAddress = async (addressId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    try {
      await deleteAddressApi(addressId)
      ElMessage.success('地址删除成功')
      // 重新加载地址列表
      await loadAddressList()
    } catch (error) {
      console.error('删除地址失败:', error)
      // 如果是404，说明接口不存在，仅从本地删除
      if (error.response?.status === 404) {
        const index = addressList.value.findIndex(item => item.id === addressId)
        if (index > -1) {
          addressList.value.splice(index, 1)
          ElMessage.warning('地址已从本地删除（后端接口未实现）')
        }
      } else {
        ElMessage.error('删除地址失败')
      }
    }
  } catch {
    // 用户取消删除
  }
}

// 设为默认地址
const setDefaultAddress = async (addressId) => {
  try {
    await setDefaultAddressApi(addressId)
    ElMessage.success('默认地址设置成功')
    // 重新加载地址列表
    await loadAddressList()
  } catch (error) {
    console.error('设置默认地址失败:', error)
    // 如果是404，说明接口不存在，仅本地更新
    if (error.response?.status === 404) {
      addressList.value.forEach(address => {
        address.isDefault = address.id === addressId
      })
      ElMessage.warning('默认地址已设置（后端接口未实现）')
    } else {
      ElMessage.error('设置默认地址失败')
    }
  }
}

// 保存地址
const handleSaveAddress = async () => {
  if (!addressFormRef.value) return
  
  await addressFormRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      
      try {
        const addressData = formatAddressForBackend()
        
        if (editingAddress.value) {
          // 编辑地址
          try {
            await updateAddress(editingAddress.value.id, addressData)
            ElMessage.success('地址更新成功')
            showAddAddress.value = false
            resetAddressForm()
            // 重新加载地址列表
            await loadAddressList()
          } catch (error) {
            console.error('更新地址失败:', error)
            // 如果是404，说明接口不存在，仅本地更新
            if (error.response?.status === 404) {
              const index = addressList.value.findIndex(item => item.id === editingAddress.value.id)
              if (index > -1) {
                const fullAddress = addressForm.region.join('') + addressForm.detailAddress + (addressForm.doorNumber || '')
                Object.assign(addressList.value[index], {
                  receiverName: addressForm.receiverName,
                  receiverPhone: addressForm.receiverPhone,
                  region: [...addressForm.region],
                  detailAddress: addressForm.detailAddress,
                  doorNumber: addressForm.doorNumber,
                  fullAddress: fullAddress,
                  isDefault: addressForm.isDefault
                })
              }
              ElMessage.warning('地址已更新（后端接口未实现）')
              showAddAddress.value = false
              resetAddressForm()
            } else {
              ElMessage.error('更新地址失败')
            }
          }
        } else {
          // 添加新地址
          try {
            const result = await createAddress(addressData)
            ElMessage.success('地址添加成功')
            showAddAddress.value = false
            resetAddressForm()
            // 重新加载地址列表
            await loadAddressList()
          } catch (error) {
            console.error('添加地址失败:', error)
            // 如果是404，说明接口不存在，仅本地添加
            if (error.response?.status === 404) {
              const fullAddress = addressForm.region.join('') + addressForm.detailAddress + (addressForm.doorNumber || '')
              const newAddress = {
                id: Date.now(),
                receiverName: addressForm.receiverName,
                receiverPhone: addressForm.receiverPhone,
                region: [...addressForm.region],
                detailAddress: addressForm.detailAddress,
                doorNumber: addressForm.doorNumber,
                fullAddress: fullAddress,
                isDefault: addressForm.isDefault
              }
              
              // 如果设为默认，取消其他地址的默认状态
              if (addressForm.isDefault) {
                addressList.value.forEach(address => {
                  address.isDefault = false
                })
              }
              
              addressList.value.push(newAddress)
              ElMessage.warning('地址已添加（后端接口未实现）')
              showAddAddress.value = false
              resetAddressForm()
            } else {
              ElMessage.error('添加地址失败')
            }
          }
        }
      } catch (error) {
        console.error('保存地址失败:', error)
        ElMessage.error('操作失败，请重试')
      } finally {
        saving.value = false
      }
    }
  })
}

// 重置表单
const resetAddressForm = () => {
  Object.assign(addressForm, {
    receiverName: '',
    receiverPhone: '',
    region: [],
    detailAddress: '',
    doorNumber: '',
    isDefault: false
  })
  editingAddress.value = null
  if (addressFormRef.value) {
    addressFormRef.value.resetFields()
  }
}

onMounted(() => {
  loadAddressList()
})
</script>

<style scoped>
.address-management-container {
  padding: 20px 0;
  min-height: calc(100vh - 120px);
}

.address-card {
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-address {
  text-align: center;
  padding: 40px 0;
}

.address-list {
  display: grid;
  gap: 16px;
}

.address-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s;
  position: relative;
}

.address-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.default-address {
  border-color: #ff6b6b;
  background: linear-gradient(135deg, #fff5f5, #ffffff);
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.receiver-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.receiver-name {
  font-weight: 600;
  font-size: 16px;
  color: #333;
}

.receiver-phone {
  color: #666;
  font-size: 14px;
}

.address-actions {
  display: flex;
  gap: 8px;
}

.address-detail {
  color: #666;
  line-height: 1.6;
}

.address-detail p {
  margin: 0 0 4px 0;
}

.door-number {
  color: #999;
  font-size: 14px;
}

.set-default {
  margin-top: 12px;
  text-align: right;
}

.address-form {
  max-width: 500px;
}

@media (max-width: 768px) {
  .address-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .address-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .receiver-info {
    flex-wrap: wrap;
  }
}
</style>
