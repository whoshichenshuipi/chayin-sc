/**
 * 价格计算工具函数
 * 用于计算优惠券折扣、促销活动折扣等
 */

/**
 * 计算优惠券折扣金额
 * @param {Number} totalAmount - 订单总金额
 * @param {Object} coupon - 优惠券对象
 * @param {String} coupon.type - 优惠券类型: cash-现金券, discount-折扣券, reduce-满减券
 * @param {Number} coupon.value - 优惠券面值（折扣券为折扣比例，如0.9表示9折或90表示90%）
 * @param {Number} coupon.threshold - 使用门槛（最小金额）
 * @returns {Number} 折扣金额
 */
export function calculateCouponDiscount(totalAmount, coupon) {
  if (!coupon || !totalAmount || totalAmount <= 0) {
    return 0
  }

  const { type, value, threshold } = coupon
  const minAmount = threshold || 0

  // 检查是否满足使用门槛
  if (totalAmount < minAmount) {
    return 0
  }

  let discount = 0

  if (type === 'cash' || type === 'reduce') {
    // 现金券或满减券，直接减固定金额
    discount = Math.min(Number(value) || 0, totalAmount) // 不能超过商品总价
  } else if (type === 'discount') {
    // 折扣券，计算折扣金额
    const discountRate = Number(value) || 0
    if (discountRate > 1) {
      // 如果value是百分比（如90表示90%），需要转换
      discount = totalAmount * (1 - discountRate / 100)
    } else {
      // 如果value是小数（如0.9表示9折）
      discount = totalAmount * (1 - discountRate)
    }
  }

  // 折扣金额不能超过订单总金额
  return Math.min(discount, totalAmount)
}

/**
 * 计算促销活动折扣后的商品价格
 * @param {Number} originalPrice - 商品原价
 * @param {Object} promotion - 促销活动对象
 * @param {String} promotion.type - 活动类型: discount-限时折扣, full_reduce-满减活动, buy_one_get_one-买一送一, second_half_price-第二件半价
 * @param {Number} promotion.discountRate - 折扣比例（限时折扣使用，如8表示8折）
 * @param {Number} quantity - 购买数量（用于计算第二件半价）
 * @returns {Object} 包含促销价格和折扣信息的对象
 */
export function calculatePromotionPrice(originalPrice, promotion, quantity = 1) {
  if (!promotion || !originalPrice || originalPrice <= 0) {
    return {
      price: originalPrice,
      originalPrice: originalPrice,
      discount: 0,
      discountText: ''
    }
  }

  const { type, discountRate, status } = promotion

  // 检查活动状态
  if (status !== 'active') {
    return {
      price: originalPrice,
      originalPrice: originalPrice,
      discount: 0,
      discountText: ''
    }
  }

  let price = originalPrice
  let discount = 0
  let discountText = ''

  if (type === 'discount' && discountRate) {
    // 限时折扣：8表示8折，即原价 * 0.8
    const rate = Number(discountRate)
    if (rate > 0 && rate <= 10) {
      price = originalPrice * (rate / 10)
      discount = originalPrice - price
      discountText = `${rate}折`
    }
  } else if (type === 'second_half_price' && quantity >= 2) {
    // 第二件半价：第二件及以后每件半价
    const fullPriceItems = Math.floor(quantity / 2) * 2
    const halfPriceItems = quantity - fullPriceItems
    const totalPrice = (fullPriceItems / 2) * originalPrice + (fullPriceItems / 2) * originalPrice * 0.5 + halfPriceItems * originalPrice
    price = totalPrice / quantity // 平均单价
    discount = originalPrice - price
    discountText = '第二件半价'
  } else if (type === 'buy_one_get_one' && quantity >= 2) {
    // 买一送一：每两件算一件价格
    const paidItems = Math.ceil(quantity / 2)
    price = (paidItems * originalPrice) / quantity
    discount = originalPrice - price
    discountText = '买一送一'
  }

  return {
    price: Math.max(0, price),
    originalPrice: originalPrice,
    discount: Math.max(0, discount),
    discountText
  }
}

/**
 * 计算订单总金额（包含促销活动和优惠券）
 * @param {Array} items - 订单商品列表
 * @param {Object} promotion - 促销活动对象（可选）
 * @param {Object} coupon - 优惠券对象（可选）
 * @param {Number} deliveryFee - 配送费
 * @returns {Object} 包含各项金额的对象
 */
export function calculateOrderTotal(items, promotion = null, coupon = null, deliveryFee = 0) {
  // 计算商品总价（考虑促销活动）
  let productTotal = 0
  
  items.forEach(item => {
    const itemPrice = item.price || 0
    const itemQuantity = item.quantity || 1
    
    // 如果商品参与促销活动，计算促销价格
    if (promotion && promotion.productIds && promotion.productIds.includes(item.productId || item.id)) {
      const promotionPrice = calculatePromotionPrice(itemPrice, promotion, itemQuantity)
      productTotal += promotionPrice.price * itemQuantity
    } else {
      productTotal += itemPrice * itemQuantity
    }
  })

  // 计算优惠券折扣
  const couponDiscount = coupon ? calculateCouponDiscount(productTotal, coupon) : 0

  // 计算最终金额
  const finalAmount = Math.max(0, productTotal + deliveryFee - couponDiscount)

  return {
    productTotal: productTotal,
    deliveryFee: deliveryFee,
    couponDiscount: couponDiscount,
    finalAmount: finalAmount
  }
}

/**
 * 计算满减活动优惠金额
 * @param {Number} totalAmount - 订单总金额
 * @param {Object} promotion - 满减活动对象
 * @param {Array} promotion.fullReduceRules - 满减规则列表，格式: [{fullAmount: 满额, reduceAmount: 减额}, ...]
 * @returns {Number} 满减优惠金额
 */
export function calculateFullReduceDiscount(totalAmount, promotion) {
  if (!promotion || !promotion.fullReduceRules || !Array.isArray(promotion.fullReduceRules)) {
    return 0
  }

  if (promotion.type !== 'full_reduce' || promotion.status !== 'active') {
    return 0
  }

  // 按满额从高到低排序，优先使用满额高的规则
  const sortedRules = [...promotion.fullReduceRules].sort((a, b) => {
    const fullA = Number(a.fullAmount) || 0
    const fullB = Number(b.fullAmount) || 0
    return fullB - fullA
  })

  // 找到满足条件的最大满减规则
  let maxDiscount = 0
  for (const rule of sortedRules) {
    const fullAmount = Number(rule.fullAmount) || 0
    const reduceAmount = Number(rule.reduceAmount) || 0

    if (totalAmount >= fullAmount && reduceAmount > maxDiscount) {
      maxDiscount = reduceAmount
    }
  }

  // 优惠金额不能超过订单总金额
  return Math.min(maxDiscount, totalAmount)
}

/**
 * 格式化价格显示
 * @param {Number} price - 价格
 * @param {Number} decimals - 小数位数，默认2
 * @returns {String} 格式化后的价格字符串
 */
export function formatPrice(price, decimals = 2) {
  if (price === null || price === undefined || isNaN(price)) {
    return '0.00'
  }
  return Number(price).toFixed(decimals)
}

