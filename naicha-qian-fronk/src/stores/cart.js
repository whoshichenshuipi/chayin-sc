import { defineStore } from 'pinia'
import { savePromotionUserCart, checkUserParticipatedPromotion } from '@/api/promotion'
import { calculatePromotionPrice } from '@/utils/priceCalculator'
import { ElMessage } from 'element-plus'

export const useCartStore = defineStore('cart', {
  state: () => {
    // 从 localStorage 恢复购物车数据
    const savedCart = localStorage.getItem('cart_items')
    let items = []
    if (savedCart) {
      try {
        items = JSON.parse(savedCart)
        // 确保每个商品都有必要的字段
        items = items.map(item => ({
          ...item,
          selected: item.selected !== undefined ? item.selected : true,
          hasPriceChanged: item.hasPriceChanged || false,
          isDiscontinued: item.isDiscontinued || false,
          stockStatus: item.stockStatus || 'sufficient'
        }))
      } catch (error) {
        console.error('恢复购物车数据失败:', error)
        items = []
      }
    }
    
    return {
      items,
      totalCount: 0,
      totalPrice: 0,
      // 商品状态提醒
      priceAlerts: [], // 价格变动提醒
      stockAlerts: [], // 库存不足提醒
      discontinuedAlerts: [] // 商品下架提醒
    }
  },

  getters: {
    getCartItems: (state) => state.items,
    getTotalCount: (state) => state.totalCount,
    getTotalPrice: (state) => state.totalPrice,
    // 获取有效商品（未下架）
    getValidItems: (state) => state.items.filter(item => !item.isDiscontinued),
    // 获取已下架商品
    getDiscontinuedItems: (state) => state.items.filter(item => item.isDiscontinued),
    // 获取有价格变动的商品
    getPriceChangedItems: (state) => state.items.filter(item => item.hasPriceChanged),
    // 获取库存不足的商品
    getLowStockItems: (state) => state.items.filter(item => item.stockStatus === 'low')
  },

  actions: {
    // 生成商品唯一ID（包含规格信息）
    generateItemId(product, selectedSpecs) {
      const specString = JSON.stringify(selectedSpecs || {})
      return `${product.id}_${btoa(specString)}`
    },

    // 添加商品到购物车（支持规格匹配）
    async addToCart(product, quantity = 1, selectedSpecs = {}, promotion = null) {
      const itemId = this.generateItemId(product, selectedSpecs)
      const existingItem = this.items.find(item => item.cartItemId === itemId)
      
      // 如果有营销活动，检查用户是否已参与
      let hasParticipatedPromotion = false
      if (promotion && promotion.id) {
        try {
          hasParticipatedPromotion = await checkUserParticipatedPromotion(promotion.id)
          if (hasParticipatedPromotion) {
            // 检查是否是同一个购物车项（允许更新数量）
            const existingPromotionItem = this.items.find(
              item => item.promotionId === promotion.id && item.cartItemId !== itemId
            )
            if (existingPromotionItem) {
              throw new Error('您已参与过该营销活动，每个用户对同一活动只能参与一次')
            }
          }
        } catch (error) {
          if (error.message && error.message.includes('已参与')) {
            throw error
          }
          // 如果检查失败，记录日志但不阻止添加
          console.warn('检查营销活动参与状态失败:', error)
          hasParticipatedPromotion = false
        }
      }
      
      if (existingItem) {
        // 检查库存限制
        const newQuantity = existingItem.quantity + quantity
        if (newQuantity > product.stock) {
          throw new Error(`库存不足，最多只能添加 ${product.stock} 杯`)
        }
        existingItem.quantity = newQuantity
        
        // 如果有营销活动且用户已参与，重新计算价格
        if (promotion && hasParticipatedPromotion) {
          const originalPrice = existingItem.originalPrice || existingItem.price
          const promotionPriceInfo = calculatePromotionPrice(originalPrice, promotion, newQuantity)
          existingItem.price = promotionPriceInfo.price
          existingItem.promotionId = promotion.id
          existingItem.promotion = promotion
          existingItem.discountAmount = promotionPriceInfo.discount
        } else if (promotion && !hasParticipatedPromotion) {
          // 有促销活动但用户未参与，使用原价
          existingItem.price = existingItem.originalPrice || existingItem.price
          existingItem.promotionId = promotion.id
          existingItem.promotion = promotion
          existingItem.discountAmount = 0
        }
        
        // 更新营销活动参与记录（只有在用户参与了促销活动时才保存）
        if (promotion && hasParticipatedPromotion && existingItem.promotionId) {
          await this.savePromotionRecord(existingItem, promotion, newQuantity)
        }
      } else {
        // 检查库存
        if (quantity > product.stock) {
          throw new Error(`库存不足，最多只能添加 ${product.stock} 杯`)
        }
        
        // 计算促销价格（只有在用户参与了促销活动时才应用折扣）
        const originalPrice = product.price
        let finalPrice = originalPrice
        let discountAmount = 0
        
        if (promotion && hasParticipatedPromotion) {
          const promotionPriceInfo = calculatePromotionPrice(originalPrice, promotion, quantity)
          finalPrice = promotionPriceInfo.price
          discountAmount = promotionPriceInfo.discount
        }
        
        const cartItem = {
          ...product,
          cartItemId: itemId,
          quantity,
          selectedSpecs: { ...selectedSpecs },
          originalPrice: originalPrice, // 记录原始价格
          price: finalPrice, // 使用折扣后的价格
          discountAmount: discountAmount, // 优惠金额
          hasPriceChanged: false,
          isDiscontinued: false,
          stockStatus: product.stock < 10 ? 'low' : 'sufficient',
          selected: true, // 默认选中
          addedAt: new Date().toISOString(),
          promotionId: promotion?.id || null,
          promotion: promotion || null,
          // 确保促销标签被保存
          promotionTypes: product.promotionTypes || product.tags || [],
          merchantId: product.merchantId || null
        }
        
        console.log('🛒 添加商品到购物车:', {
          productId: cartItem.id,
          name: cartItem.name,
          promotionTypes: cartItem.promotionTypes,
          merchantId: cartItem.merchantId
        })
        
        this.items.push(cartItem)
        
        // 保存营销活动参与记录（只有在用户参与了促销活动时才保存）
        if (promotion && hasParticipatedPromotion) {
          await this.savePromotionRecord(cartItem, promotion, quantity)
        }
      }
      
      this.calculateTotals()
      this.saveToLocalStorage()
    },
    
    // 保存营销活动参与记录
    async savePromotionRecord(cartItem, promotion, quantity) {
      try {
        // 计算促销价格和优惠金额
        const originalPrice = cartItem.originalPrice || cartItem.price
        const promotionPriceInfo = calculatePromotionPrice(originalPrice, promotion, quantity)
        
        await savePromotionUserCart({
          promotionId: promotion.id,
          productId: cartItem.id || cartItem.productId,
          cartItemId: cartItem.cartItemId,
          quantity: quantity,
          originalPrice: originalPrice,
          promotionPrice: promotionPriceInfo.price,
          discountAmount: promotionPriceInfo.discount
        })
      } catch (error) {
        console.error('保存营销活动参与记录失败:', error)
        // 如果是重复参与的错误，抛出异常
        if (error.response?.data?.message && error.response.data.message.includes('已参与')) {
          throw new Error(error.response.data.message || '您已参与过该营销活动，每个用户对同一活动只能参与一次')
        }
        // 其他错误静默失败，不影响购物车添加
      }
    },

    // 更新商品数量
    async updateQuantity(cartItemId, quantity) {
      const item = this.items.find(item => item.cartItemId === cartItemId)
      if (item) {
        if (quantity > item.stock) {
          throw new Error(`库存不足，最多只能选择 ${item.stock} 杯`)
        }
        if (quantity < 1) {
          throw new Error('数量不能少于1')
        }
        item.quantity = quantity
        
        // 如果有营销活动，检查用户是否参与，然后重新计算价格
        if (item.promotion && item.promotion.id) {
          try {
            const hasParticipated = await checkUserParticipatedPromotion(item.promotion.id)
            if (hasParticipated) {
              const originalPrice = item.originalPrice || item.price
              const promotionPriceInfo = calculatePromotionPrice(originalPrice, item.promotion, quantity)
              item.price = promotionPriceInfo.price
              item.discountAmount = promotionPriceInfo.discount
              
              // 更新营销活动参与记录
              await this.savePromotionRecord(item, item.promotion, quantity)
            } else {
              // 用户未参与，使用原价
              item.price = item.originalPrice || item.price
              item.discountAmount = 0
            }
          } catch (error) {
            console.warn('检查营销活动参与状态失败:', error)
            // 检查失败时，使用原价
            item.price = item.originalPrice || item.price
            item.discountAmount = 0
          }
        }
        
        this.calculateTotals()
        this.saveToLocalStorage()
      }
    },

    // 删除单个商品
    removeFromCart(cartItemId) {
      this.items = this.items.filter(item => item.cartItemId !== cartItemId)
      this.calculateTotals()
      this.saveToLocalStorage()
    },

    // 批量删除商品
    removeMultipleItems(cartItemIds) {
      this.items = this.items.filter(item => !cartItemIds.includes(item.cartItemId))
      this.calculateTotals()
      this.saveToLocalStorage()
    },

    // 清空购物车（支持保留未失效优惠商品）
    clearCart(keepValidItems = false) {
      if (keepValidItems) {
        this.items = this.items.filter(item => !item.isDiscontinued)
      } else {
        this.items = []
        this.clearLocalStorage()
      }
      this.calculateTotals()
      this.saveToLocalStorage()
    },

    // 检查商品状态更新（同步后端商品信息）
    async checkItemStatus() {
      // 如果购物车为空，直接返回
      if (this.items.length === 0) {
        return
      }
      
      // 这里可以调用API验证商品状态
      // 由于购物车数据在前端，我们通过验证逻辑来检查
      this.items.forEach(item => {
        // 检查价格变动
        if (item.originalPrice && item.originalPrice !== item.price) {
          item.hasPriceChanged = true
          this.addPriceAlert(item)
        }
        
        // 检查库存状态
        if (item.stock !== undefined && item.stock < 5) {
          item.stockStatus = 'low'
          this.addStockAlert(item)
        }
        
        // 检查商品是否下架
        if (item.stock === 0 || item.status === 0 || item.isDiscontinued) {
          item.isDiscontinued = true
          this.addDiscontinuedAlert(item)
        }
      })
    },
    
    // 同步商品信息（从后端获取最新商品数据）
    async syncCartItems() {
      // 这个函数可以由外部API调用来同步商品信息
      // 目前购物车存储在前端，暂不实现同步
    },
    
    // 更新商品信息（用于从后端验证后更新）
    updateItemInfo(cartItemId, productInfo) {
      const item = this.items.find(item => item.cartItemId === cartItemId)
      if (item) {
        const oldPrice = item.price
        const oldStock = item.stock
        
        // 更新商品基础信息
        const originalPrice = productInfo.originalPrice || productInfo.price
        // 如果还没有保存原价，保存原价
        if (!item.originalPrice || item.originalPrice <= 0) {
          item.originalPrice = originalPrice
        }
        item.stock = productInfo.stock
        item.status = productInfo.status
        item.name = productInfo.name || item.name
        item.description = productInfo.description || item.description
        item.image = productInfo.images?.[0] || productInfo.image || item.image
        
        // 如果有促销活动，检查用户是否参与后再计算价格
        if (item.promotion && item.promotion.id) {
          // 异步检查用户参与状态（这里需要调用者await）
          checkUserParticipatedPromotion(item.promotion.id).then(hasParticipated => {
            if (hasParticipated) {
              const promotionPriceInfo = calculatePromotionPrice(item.originalPrice, item.promotion, item.quantity)
              item.price = promotionPriceInfo.price
              item.discountAmount = promotionPriceInfo.discount
            } else {
              // 用户未参与，使用原价
              item.price = item.originalPrice
              item.discountAmount = 0
            }
            this.calculateTotals()
          }).catch(error => {
            console.warn('检查促销活动参与状态失败:', error)
            // 检查失败时，使用原价
            item.price = item.originalPrice
            item.discountAmount = 0
            this.calculateTotals()
          })
        } else {
          // 没有促销活动，使用商品价格
          item.price = productInfo.price
          item.discountAmount = 0
        }
        
        // 检查价格变动（对比原价）
        if (oldPrice !== item.price && item.originalPrice !== undefined) {
          const oldOriginalPrice = item.originalPrice || oldPrice
          if (Math.abs(oldOriginalPrice - originalPrice) > 0.01) {
            item.hasPriceChanged = true
            this.addPriceAlert(item)
          }
        }
        
        // 检查库存状态
        if (productInfo.stock < 5) {
          item.stockStatus = 'low'
          this.addStockAlert(item)
        } else {
          item.stockStatus = 'sufficient'
        }
        
        // 检查商品是否下架
        if (productInfo.status !== 1 || productInfo.stock === 0) {
          item.isDiscontinued = true
          this.addDiscontinuedAlert(item)
        } else {
          item.isDiscontinued = false
        }
        
        // 如果库存不足，限制数量
        if (item.quantity > productInfo.stock) {
          item.quantity = productInfo.stock
        }
        
        this.calculateTotals()
        this.saveToLocalStorage()
      }
    },

    // 添加价格变动提醒
    addPriceAlert(item) {
      const existingAlert = this.priceAlerts.find(alert => alert.cartItemId === item.cartItemId)
      if (!existingAlert) {
        this.priceAlerts.push({
          cartItemId: item.cartItemId,
          productName: item.name,
          oldPrice: item.originalPrice,
          newPrice: item.price,
          changeAmount: item.price - item.originalPrice,
          timestamp: new Date().toISOString()
        })
      }
    },

    // 添加库存不足提醒
    addStockAlert(item) {
      const existingAlert = this.stockAlerts.find(alert => alert.cartItemId === item.cartItemId)
      if (!existingAlert) {
        this.stockAlerts.push({
          cartItemId: item.cartItemId,
          productName: item.name,
          currentStock: item.stock,
          timestamp: new Date().toISOString()
        })
      }
    },

    // 添加商品下架提醒
    addDiscontinuedAlert(item) {
      const existingAlert = this.discontinuedAlerts.find(alert => alert.cartItemId === item.cartItemId)
      if (!existingAlert) {
        this.discontinuedAlerts.push({
          cartItemId: item.cartItemId,
          productName: item.name,
          timestamp: new Date().toISOString()
        })
      }
    },

    // 清除提醒
    clearAlerts(type = 'all') {
      if (type === 'all' || type === 'price') {
        this.priceAlerts = []
      }
      if (type === 'all' || type === 'stock') {
        this.stockAlerts = []
      }
      if (type === 'all' || type === 'discontinued') {
        this.discontinuedAlerts = []
      }
    },

    // 计算总价
    calculateTotals() {
      this.totalCount = this.items.reduce((sum, item) => sum + item.quantity, 0)
      this.totalPrice = this.items.reduce((sum, item) => sum + (item.price * item.quantity), 0)
    },
    
    // 保存购物车数据到 localStorage
    saveToLocalStorage() {
      try {
        // 只保存必要的字段，避免存储过大的对象
        const itemsToSave = this.items.map(item => ({
          id: item.id,
          productId: item.productId,
          cartItemId: item.cartItemId,
          name: item.name,
          description: item.description,
          price: item.price,
          originalPrice: item.originalPrice,
          quantity: item.quantity,
          selectedSpecs: item.selectedSpecs,
          image: item.image,
          images: item.images,
          stock: item.stock,
          status: item.status,
          merchantId: item.merchantId,
          selected: item.selected,
          hasPriceChanged: item.hasPriceChanged,
          isDiscontinued: item.isDiscontinued,
          stockStatus: item.stockStatus,
          discountAmount: item.discountAmount,
          promotionId: item.promotionId,
          // 保存促销活动的关键信息（不保存整个对象）
          promotion: item.promotion ? {
            id: item.promotion.id,
            type: item.promotion.type,
            discountRate: item.promotion.discountRate,
            status: item.promotion.status
          } : null,
          addedAt: item.addedAt
        }))
        localStorage.setItem('cart_items', JSON.stringify(itemsToSave))
      } catch (error) {
        console.error('保存购物车数据到 localStorage 失败:', error)
      }
    },
    
    // 从 localStorage 加载购物车数据
    loadFromLocalStorage() {
      try {
        const savedCart = localStorage.getItem('cart_items')
        if (savedCart) {
          const items = JSON.parse(savedCart)
          this.items = items.map(item => ({
            ...item,
            selected: item.selected !== undefined ? item.selected : true,
            hasPriceChanged: item.hasPriceChanged || false,
            isDiscontinued: item.isDiscontinued || false,
            stockStatus: item.stockStatus || 'sufficient'
          }))
          this.calculateTotals()
        }
      } catch (error) {
        console.error('从 localStorage 加载购物车数据失败:', error)
        this.items = []
      }
    },
    
    // 清除 localStorage 中的购物车数据
    clearLocalStorage() {
      localStorage.removeItem('cart_items')
    }
  }
})
