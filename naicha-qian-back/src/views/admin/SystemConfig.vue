<template>
  <div class="page-container">
    <div class="page-header">
      <h2>系统配置管理</h2>
    </div>
    
    <el-card>
      <el-tabs v-model="activeTab">
        <!-- 基础参数配置 -->
        <el-tab-pane label="基础参数配置" name="basic">
          <el-form :model="basicConfig" label-width="150px" class="config-form">
            <el-form-item label="系统名称" required>
              <el-input 
                v-model="basicConfig.systemName" 
                placeholder="请输入系统名称"
                style="width: 300px;"
              />
            </el-form-item>
            
            <el-form-item label="系统Logo">
              <div class="logo-upload-section">
                <el-upload
                  class="logo-uploader"
                  :http-request="handleLogoUpload"
                  :show-file-list="false"
                  :before-upload="beforeLogoUpload"
                  accept="image/jpeg,image/png,image/jpg"
                >
                  <img v-if="basicConfig.logo" :src="basicConfig.logo" class="logo-preview" />
                  <div v-else class="logo-uploader-icon">
                    <el-icon v-if="!logoUploading"><Plus /></el-icon>
                    <el-icon v-else class="is-loading"><Loading /></el-icon>
                    <div class="upload-text">{{ logoUploading ? '上传中...' : '点击上传Logo' }}</div>
                  </div>
                </el-upload>
                <div class="logo-tips">
                  <p>建议尺寸：200x60px，支持PNG、JPG格式，文件大小不超过2MB</p>
                  <div class="logo-actions">
                    <el-button 
                      type="text" 
                      @click="removeLogo" 
                      v-if="basicConfig.logo"
                      :disabled="logoUploading"
                    >
                      移除Logo
                    </el-button>
                    <el-button 
                      type="text" 
                      @click="saveLogoToConfig" 
                      v-if="basicConfig.logo && logoChanged"
                      :loading="saving"
                    >
                      保存Logo
                    </el-button>
                  </div>
                </div>
              </div>
            </el-form-item>
            
            <el-form-item label="客服电话" required>
              <el-input 
                v-model="basicConfig.servicePhone" 
                placeholder="请输入客服电话"
                style="width: 300px;"
              />
            </el-form-item>
            
            <el-form-item label="在线客服入口">
              <el-input 
                v-model="basicConfig.onlineService" 
                placeholder="请输入在线客服链接"
                style="width: 400px;"
              />
              <div class="form-tip">如：https://chat.example.com</div>
            </el-form-item>
            
            <el-form-item label="默认配送费" required>
              <el-input-number 
                v-model="basicConfig.defaultDeliveryFee" 
                :min="0" 
                :max="50" 
                :precision="2"
                style="width: 200px;"
              />
              <span class="form-unit">元</span>
            </el-form-item>
            
            <el-form-item label="满减门槛基础值" required>
              <el-input-number 
                v-model="basicConfig.freeDeliveryThreshold" 
                :min="0" 
                :max="500"
                :precision="2"
                style="width: 200px;"
              />
              <span class="form-unit">元</span>
              <div class="form-tip">满此金额享受免费配送</div>
            </el-form-item>
            
            <el-form-item label="系统描述">
              <el-input 
                v-model="basicConfig.description" 
                type="textarea" 
                :rows="4"
                placeholder="请输入系统描述"
                style="width: 500px;"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveBasicConfig" :loading="saving" :disabled="loading">
                保存配置
              </el-button>
              <el-button @click="resetBasicConfig" :disabled="loading">恢复默认配置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 支付方式配置 -->
        <el-tab-pane label="支付方式配置" name="payment">
          <el-form :model="paymentConfig" label-width="150px" class="config-form">
            <el-form-item label="微信支付">
              <div class="payment-item">
                <el-switch v-model="paymentConfig.wechatPay.enabled" />
                <span class="payment-label">启用微信支付</span>
              </div>
              <div v-if="paymentConfig.wechatPay.enabled" class="payment-config">
                <el-form-item label="商户号">
                  <el-input 
                    v-model="paymentConfig.wechatPay.merchantId" 
                    placeholder="请输入微信支付商户号"
                    style="width: 300px;"
                  />
                </el-form-item>
                <el-form-item label="API密钥">
                  <el-input 
                    v-model="paymentConfig.wechatPay.apiKey" 
                    type="password"
                    placeholder="请输入API密钥"
                    style="width: 300px;"
                    show-password
                  />
                </el-form-item>
                <el-form-item label="回调地址">
                  <el-input 
                    v-model="paymentConfig.wechatPay.callbackUrl" 
                    placeholder="请输入回调地址"
                    style="width: 400px;"
                  />
                </el-form-item>
              </div>
            </el-form-item>
            
            <el-form-item label="支付宝支付">
              <div class="payment-item">
                <el-switch v-model="paymentConfig.alipay.enabled" />
                <span class="payment-label">启用支付宝支付</span>
              </div>
              <div v-if="paymentConfig.alipay.enabled" class="payment-config">
                <el-form-item label="商户号">
                  <el-input 
                    v-model="paymentConfig.alipay.merchantId" 
                    placeholder="请输入支付宝商户号"
                    style="width: 300px;"
                  />
                </el-form-item>
                <el-form-item label="应用ID">
                  <el-input 
                    v-model="paymentConfig.alipay.appId" 
                    placeholder="请输入应用ID"
                    style="width: 300px;"
                  />
                </el-form-item>
                <el-form-item label="私钥">
                  <el-input 
                    v-model="paymentConfig.alipay.privateKey" 
                    type="textarea"
                    :rows="3"
                    placeholder="请输入应用私钥"
                    style="width: 400px;"
                  />
                </el-form-item>
              </div>
            </el-form-item>
            
            <el-form-item label="银联支付">
              <div class="payment-item">
                <el-switch v-model="paymentConfig.unionPay.enabled" />
                <span class="payment-label">启用银联支付</span>
              </div>
              <div v-if="paymentConfig.unionPay.enabled" class="payment-config">
                <el-form-item label="商户号">
                  <el-input 
                    v-model="paymentConfig.unionPay.merchantId" 
                    placeholder="请输入银联商户号"
                    style="width: 300px;"
                  />
                </el-form-item>
                <el-form-item label="证书路径">
                  <el-input 
                    v-model="paymentConfig.unionPay.certPath" 
                    placeholder="请输入证书路径"
                    style="width: 400px;"
                  />
                </el-form-item>
              </div>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="savePaymentConfig" :loading="saving" :disabled="loading">
                保存配置
              </el-button>
              <el-button @click="testPaymentConnection" :loading="saving" :disabled="loading">测试支付连接</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 配送规则配置 -->
        <el-tab-pane label="配送规则配置" name="delivery">
          <el-form :model="deliveryConfig" label-width="150px" class="config-form">
            <el-form-item label="配送范围计算方式">
              <el-radio-group v-model="deliveryConfig.rangeType">
                <el-radio label="distance">按距离计算</el-radio>
                <el-radio label="region">按行政区划</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <div v-if="deliveryConfig.rangeType === 'distance'">
              <el-form-item label="配送距离限制">
                <el-input-number 
                  v-model="deliveryConfig.maxDistance" 
                  :min="1" 
                  :max="50"
                  style="width: 200px;"
                />
                <span class="form-unit">公里</span>
                <div class="form-tip">超出此距离不支持配送</div>
              </el-form-item>
            </div>
            
            <div v-if="deliveryConfig.rangeType === 'region'">
              <el-form-item label="支持配送区域">
                <el-select 
                  v-model="deliveryConfig.supportedRegions" 
                  multiple 
                  placeholder="请选择支持配送的区域"
                  style="width: 400px;"
                >
                  <el-option label="市区" value="downtown" />
                  <el-option label="郊区" value="suburb" />
                  <el-option label="开发区" value="development" />
                  <el-option label="高新区" value="high-tech" />
                </el-select>
              </el-form-item>
            </div>
            
            <el-form-item label="配送时间区间">
              <el-time-picker
                v-model="deliveryConfig.deliveryTimeRange"
                is-range
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                format="HH:mm"
                value-format="HH:mm"
                style="width: 300px;"
              />
              <div class="form-tip">超出时间不接单</div>
            </el-form-item>
            
            <el-form-item label="配送费计算规则">
              <div class="delivery-fee-rules">
                <div class="fee-rule-item">
                  <span class="rule-label">基础配送费：</span>
                  <el-input-number 
                    v-model="deliveryConfig.baseFee" 
                    :min="0" 
                    :max="50" 
                    :precision="2"
                    style="width: 120px;"
                  />
                  <span class="form-unit">元</span>
                </div>
                <div class="fee-rule-item">
                  <span class="rule-label">免费配送门槛：</span>
                  <el-input-number 
                    v-model="deliveryConfig.freeThreshold" 
                    :min="0" 
                    :max="500" 
                    :precision="2"
                    style="width: 120px;"
                  />
                  <span class="form-unit">元</span>
                </div>
                <div class="fee-rule-item">
                  <span class="rule-label">超出距离每公里加收：</span>
                  <el-input-number 
                    v-model="deliveryConfig.extraFeePerKm" 
                    :min="0" 
                    :max="20" 
                    :precision="2"
                    style="width: 120px;"
                  />
                  <span class="form-unit">元</span>
                </div>
              </div>
            </el-form-item>
            
            <el-form-item label="特殊区域限制">
              <div class="special-areas">
                <div 
                  v-for="(area, index) in deliveryConfig.specialAreas" 
                  :key="index" 
                  class="special-area-item"
                >
                  <el-input 
                    v-model="area.name" 
                    placeholder="请输入区域名称"
                    style="width: 200px; margin-right: 10px;"
                  />
                  <el-input 
                    v-model="area.reason" 
                    placeholder="请输入限制原因"
                    style="width: 200px; margin-right: 10px;"
                  />
                  <el-button 
                    type="danger" 
                    @click="removeSpecialArea(index)"
                    :icon="Delete"
                  >
                    删除
                  </el-button>
                </div>
                <el-button 
                  type="primary" 
                  @click="addSpecialArea"
                  :icon="Plus"
                  style="margin-top: 10px;"
                >
                  添加特殊区域
                </el-button>
              </div>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveDeliveryConfig" :loading="saving" :disabled="loading">
                保存配置
              </el-button>
              <el-button @click="resetDeliveryConfig" :disabled="loading">恢复默认配置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Loading } from '@element-plus/icons-vue'
import { 
  getBasicConfig, 
  saveBasicConfig as saveBasicConfigApi,
  getPaymentConfig,
  savePaymentConfig as savePaymentConfigApi,
  getDeliveryConfig,
  saveDeliveryConfig as saveDeliveryConfigApi,
  testPaymentConnection as testPaymentConnectionApi
} from '@/api/systemConfig'
import { uploadApi } from '@/api/upload'

const activeTab = ref('basic')
const saving = ref(false)
const loading = ref(false)
const logoUploading = ref(false)
const originalLogo = ref('') // 保存原始Logo URL，用于判断是否变化

// 基础参数配置
const basicConfig = reactive({
  systemName: '奶茶商城',
  logo: '',
  servicePhone: '400-123-4567',
  onlineService: 'https://chat.example.com',
  defaultDeliveryFee: 5.00,
  freeDeliveryThreshold: 30.00,
  description: '专业的奶茶订购平台，为您提供优质的奶茶服务'
})

// 支付方式配置
const paymentConfig = reactive({
  wechatPay: {
    enabled: true,
    merchantId: '',
    apiKey: '',
    callbackUrl: ''
  },
  alipay: {
    enabled: true,
    merchantId: '',
    appId: '',
    privateKey: ''
  },
  unionPay: {
    enabled: false,
    merchantId: '',
    certPath: ''
  }
})

// 配送规则配置
const deliveryConfig = reactive({
  rangeType: 'distance', // distance | region
  maxDistance: 5,
  supportedRegions: ['downtown'],
  deliveryTimeRange: ['09:00', '22:00'],
  baseFee: 5.00,
  freeThreshold: 30.00,
  extraFeePerKm: 2.00,
  specialAreas: [
    { name: '偏远山区', reason: '该地址暂不支持配送' },
    { name: '工业园区', reason: '配送时间较长，请耐心等待' }
  ]
})

// 默认配置（用于恢复）
const defaultBasicConfig = {
  systemName: '奶茶商城',
  logo: '',
  servicePhone: '400-123-4567',
  onlineService: '',
  defaultDeliveryFee: 5.00,
  freeDeliveryThreshold: 30.00,
  description: ''
}

const defaultPaymentConfig = {
  wechatPay: { enabled: false, merchantId: '', apiKey: '', callbackUrl: '' },
  alipay: { enabled: false, merchantId: '', appId: '', privateKey: '' },
  unionPay: { enabled: false, merchantId: '', certPath: '' }
}

const defaultDeliveryConfig = {
  rangeType: 'distance',
  maxDistance: 5,
  supportedRegions: ['downtown'],
  deliveryTimeRange: ['09:00', '22:00'],
  baseFee: 5.00,
  freeThreshold: 30.00,
  extraFeePerKm: 2.00,
  specialAreas: []
}

// 计算Logo是否改变
const logoChanged = computed(() => {
  return basicConfig.logo !== originalLogo.value
})

// Logo上传处理（自定义上传）
const handleLogoUpload = async (options) => {
  const file = options.file
  
  try {
    logoUploading.value = true
    
    // 如果之前有Logo，先删除旧文件
    if (originalLogo.value && originalLogo.value !== basicConfig.logo) {
      try {
        await uploadApi.deleteFile(originalLogo.value)
      } catch (error) {
        console.warn('删除旧Logo失败:', error)
        // 继续上传新Logo，不阻断流程
      }
    }
    
    const result = await uploadApi.uploadImage(file, 'logo')
    
    if (result.code === 200 && result.data) {
      basicConfig.logo = result.data
      ElMessage.success('Logo上传成功，请点击保存配置以保存')
    } else {
      ElMessage.error(result.message || 'Logo上传失败')
    }
  } catch (error) {
    console.error('Logo上传失败:', error)
    ElMessage.error('Logo上传失败，请重试')
  } finally {
    logoUploading.value = false
  }
}

// 文件上传前验证
const beforeLogoUpload = (file) => {
  // 验证文件类型
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/jpg'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('Logo只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('Logo大小不能超过 2MB!')
    return false
  }
  
  // 验证图片尺寸（可选，如果需要）
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        // 可以在这里验证图片尺寸
        // if (img.width !== 200 || img.height !== 60) {
        //   ElMessage.warning('建议尺寸为 200x60px')
        // }
        resolve(true)
      }
      img.onerror = () => {
        ElMessage.error('图片格式错误，请上传有效的图片文件')
        resolve(false)
      }
      img.src = e.target.result
    }
    reader.onerror = () => {
      ElMessage.error('文件读取失败')
      resolve(false)
    }
    reader.readAsDataURL(file)
  })
}

// 移除Logo
const removeLogo = async () => {
  try {
    await ElMessageBox.confirm('确定要移除Logo吗？移除后需要保存配置才能生效。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 如果Logo已保存，删除服务器上的文件
    if (originalLogo.value) {
      try {
        await uploadApi.deleteFile(originalLogo.value)
      } catch (error) {
        console.warn('删除Logo文件失败:', error)
      }
    }
    
  basicConfig.logo = ''
    ElMessage.success('Logo已移除，请保存配置以生效')
  } catch {
    // 用户取消
  }
}

// 单独保存Logo（可选功能）
const saveLogoToConfig = async () => {
  if (!logoChanged.value) {
    ElMessage.info('Logo未变化，无需保存')
    return
  }
  
  saving.value = true
  try {
    const result = await saveBasicConfigApi(basicConfig)
    if (result.code === 200) {
      originalLogo.value = basicConfig.logo
      ElMessage.success('Logo保存成功')
    } else {
      ElMessage.error(result.message || '保存失败，请重试')
    }
  } catch (error) {
    console.error('保存Logo失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

// 加载配置
const loadBasicConfig = async () => {
  try {
    loading.value = true
    const result = await getBasicConfig()
    if (result.code === 200 && result.data) {
      const data = result.data
      const logoUrl = data.logo || ''
      Object.assign(basicConfig, {
        systemName: data.systemName || '',
        logo: logoUrl,
        servicePhone: data.servicePhone || '',
        onlineService: data.onlineService || '',
        defaultDeliveryFee: typeof data.defaultDeliveryFee === 'number' 
          ? data.defaultDeliveryFee 
          : parseFloat(data.defaultDeliveryFee || 5.00),
        freeDeliveryThreshold: typeof data.freeDeliveryThreshold === 'number'
          ? data.freeDeliveryThreshold
          : parseFloat(data.freeDeliveryThreshold || 30.00),
        description: data.description || ''
      })
      // 保存原始Logo URL
      originalLogo.value = logoUrl
    }
  } catch (error) {
    console.error('加载基础配置失败:', error)
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

const loadPaymentConfig = async () => {
  try {
    loading.value = true
    const result = await getPaymentConfig()
    if (result.code === 200 && result.data) {
      const data = result.data
      if (data.wechatPay) {
        Object.assign(paymentConfig.wechatPay, {
          enabled: data.wechatPay.enabled !== undefined ? data.wechatPay.enabled : true,
          merchantId: data.wechatPay.merchantId || '',
          apiKey: data.wechatPay.apiKey || '',
          callbackUrl: data.wechatPay.callbackUrl || ''
        })
      }
      if (data.alipay) {
        Object.assign(paymentConfig.alipay, {
          enabled: data.alipay.enabled !== undefined ? data.alipay.enabled : true,
          merchantId: data.alipay.merchantId || '',
          appId: data.alipay.appId || '',
          privateKey: data.alipay.privateKey || ''
        })
      }
      if (data.unionPay) {
        Object.assign(paymentConfig.unionPay, {
          enabled: data.unionPay.enabled !== undefined ? data.unionPay.enabled : false,
          merchantId: data.unionPay.merchantId || '',
          certPath: data.unionPay.certPath || ''
        })
      }
    }
  } catch (error) {
    console.error('加载支付配置失败:', error)
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

const loadDeliveryConfig = async () => {
  try {
    loading.value = true
    const result = await getDeliveryConfig()
    if (result.code === 200 && result.data) {
      const data = result.data
      Object.assign(deliveryConfig, {
        rangeType: data.rangeType || 'distance',
        maxDistance: data.maxDistance || 5,
        supportedRegions: data.supportedRegions || ['downtown'],
        deliveryTimeRange: data.deliveryTimeRange || ['09:00', '22:00'],
        baseFee: typeof data.baseFee === 'number'
          ? data.baseFee
          : parseFloat(data.baseFee || 5.00),
        freeThreshold: typeof data.freeThreshold === 'number'
          ? data.freeThreshold
          : parseFloat(data.freeThreshold || 30.00),
        extraFeePerKm: typeof data.extraFeePerKm === 'number'
          ? data.extraFeePerKm
          : parseFloat(data.extraFeePerKm || 2.00),
        specialAreas: data.specialAreas || []
      })
    }
  } catch (error) {
    console.error('加载配送配置失败:', error)
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

// 组件挂载时加载配置
onMounted(() => {
  loadBasicConfig()
  loadPaymentConfig()
  loadDeliveryConfig()
})

// 特殊区域管理
const addSpecialArea = () => {
  deliveryConfig.specialAreas.push({
    name: '',
    reason: ''
  })
}

const removeSpecialArea = (index) => {
  deliveryConfig.specialAreas.splice(index, 1)
}

// 保存配置
const saveBasicConfig = async () => {
  saving.value = true
  try {
    const result = await saveBasicConfigApi(basicConfig)
    if (result.code === 200) {
      // 保存成功后更新原始Logo URL
      originalLogo.value = basicConfig.logo
    ElMessage.success('基础配置保存成功')
    } else {
      ElMessage.error(result.message || '保存失败，请重试')
    }
  } catch (error) {
    console.error('保存基础配置失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const savePaymentConfig = async () => {
  saving.value = true
  try {
    const result = await savePaymentConfigApi(paymentConfig)
    if (result.code === 200) {
    ElMessage.success('支付配置保存成功')
    } else {
      ElMessage.error(result.message || '保存失败，请重试')
    }
  } catch (error) {
    console.error('保存支付配置失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const saveDeliveryConfig = async () => {
  saving.value = true
  try {
    const result = await saveDeliveryConfigApi(deliveryConfig)
    if (result.code === 200) {
    ElMessage.success('配送配置保存成功')
    } else {
      ElMessage.error(result.message || '保存失败，请重试')
    }
  } catch (error) {
    console.error('保存配送配置失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

// 恢复默认配置
const resetBasicConfig = async () => {
  try {
    await ElMessageBox.confirm('确定要恢复默认配置吗？当前配置将被覆盖！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 如果恢复默认配置时清空了Logo，需要删除服务器文件
    if (basicConfig.logo && !defaultBasicConfig.logo) {
      try {
        await uploadApi.deleteFile(basicConfig.logo)
      } catch (error) {
        console.warn('删除Logo文件失败:', error)
      }
    }
    
    Object.assign(basicConfig, defaultBasicConfig)
    originalLogo.value = defaultBasicConfig.logo || ''
    ElMessage.success('已恢复默认配置，请保存以生效')
  } catch {
    // 用户取消
  }
}

const resetDeliveryConfig = async () => {
  try {
    await ElMessageBox.confirm('确定要恢复默认配置吗？当前配置将被覆盖！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    Object.assign(deliveryConfig, defaultDeliveryConfig)
    ElMessage.success('已恢复默认配置')
  } catch {
    // 用户取消
  }
}

// 测试支付连接
const testPaymentConnection = async () => {
  try {
    await ElMessageBox.confirm('确定要测试支付连接吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    saving.value = true
    const result = await testPaymentConnectionApi()
    if (result.code === 200) {
    ElMessage.success('支付连接测试成功')
    } else {
      ElMessage.error(result.message || '支付连接测试失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('测试支付连接失败:', error)
      ElMessage.error('测试失败，请重试')
    }
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.config-form {
  max-width: 800px;
}

.form-tip {
  margin-left: 10px;
  color: #666;
  font-size: 12px;
}

.form-unit {
  margin-left: 8px;
  color: #666;
}

/* Logo上传样式 */
.logo-upload-section {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

.logo-uploader {
  .logo-preview {
    width: 200px;
    height: 60px;
    display: block;
    object-fit: contain;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
  }
  
  .logo-uploader-icon {
    width: 200px;
    height: 60px;
    border: 1px dashed #d9d9d9;
    border-radius: 4px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: border-color 0.3s;
    
    &:hover {
      border-color: #409EFF;
    }
    
    .el-icon {
      font-size: 20px;
      color: #8c939d;
      margin-bottom: 5px;
      
      &.is-loading {
        animation: rotating 2s linear infinite;
      }
    }
    
    .upload-text {
      font-size: 12px;
      color: #8c939d;
    }
  }
}

@keyframes rotating {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.logo-tips {
  flex: 1;
  
  p {
    margin: 0 0 10px 0;
    color: #666;
    font-size: 12px;
  }
  
  .logo-actions {
    display: flex;
    gap: 10px;
    align-items: center;
  }
}

/* 支付配置样式 */
.payment-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  
  .payment-label {
    margin-left: 10px;
    font-weight: 500;
  }
}

.payment-config {
  margin-left: 30px;
  padding: 15px;
  background: #f5f5f5;
  border-radius: 4px;
  margin-top: 10px;
}

/* 配送费规则样式 */
.delivery-fee-rules {
  .fee-rule-item {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
    
    .rule-label {
      width: 150px;
      font-weight: 500;
    }
  }
}

/* 特殊区域样式 */
.special-areas {
  .special-area-item {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
    padding: 10px;
    background: #f9f9f9;
    border-radius: 4px;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .config-form {
    max-width: 100%;
  }
  
  .logo-upload-section {
    flex-direction: column;
  }
  
  .special-area-item {
    flex-direction: column;
    align-items: flex-start;
    
    .el-input {
      margin-bottom: 10px;
    }
  }
}
</style>
