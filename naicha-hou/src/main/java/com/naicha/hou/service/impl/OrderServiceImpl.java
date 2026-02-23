package com.naicha.hou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.naicha.hou.common.ResultCode;
import com.naicha.hou.dto.*;
import com.naicha.hou.entity.Order;
import com.naicha.hou.entity.OrderItem;
import com.naicha.hou.entity.Product;
import com.naicha.hou.exception.BusinessException;
import com.naicha.hou.mapper.OrderItemMapper;
import com.naicha.hou.mapper.OrderMapper;
import com.naicha.hou.mapper.ProductMapper;
import com.naicha.hou.service.AddressService;
import com.naicha.hou.service.CouponService;
import com.naicha.hou.service.OrderService;
import com.naicha.hou.service.PromotionUserCartService;
import com.naicha.hou.mapper.PromotionMapper;
import com.naicha.hou.entity.Promotion;
import com.naicha.hou.entity.PromotionUserCart;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 订单服务实现类
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final AddressService addressService;
    private final ProductMapper productMapper;
    private final CouponService couponService;
    private final PromotionUserCartService promotionUserCartService;
    private final PromotionMapper promotionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(Long userId, OrderCreateDTO createDTO) {
        log.info("创建订单，用户ID: {}, 订单数据: {}", userId, createDTO);

        // 1. 验证收货地址
        AddressDTO address = addressService.getAddressDetail(createDTO.getAddressId(), userId);
        if (address == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "收货地址不存在");
        }

        // 2. 查询用户营销活动参与记录（如果有购物车项ID）
        List<String> cartItemIds = createDTO.getItems().stream()
            .filter(item -> item.getCartItemId() != null && !item.getCartItemId().isEmpty())
            .map(OrderCreateDTO.OrderItemCreateDTO::getCartItemId)
            .distinct()
            .toList();
        
        List<PromotionUserCart> promotionUserCarts = new java.util.ArrayList<>();
        try {
            if (!cartItemIds.isEmpty()) {
                promotionUserCarts = promotionUserCartService.getByUserIdAndCartItemIds(userId, cartItemIds);
            }
        } catch (Exception e) {
            log.warn("查询用户营销活动参与记录失败，用户ID: {}, 错误: {}", userId, e.getMessage());
            // 如果查询失败，继续执行，不影响订单创建
            promotionUserCarts = new java.util.ArrayList<>();
        }
        
        // 构建购物车项ID到营销活动参与记录的映射
        java.util.Map<String, PromotionUserCart> cartItemPromotionMap = new java.util.HashMap<>();
        for (PromotionUserCart promotionUserCart : promotionUserCarts) {
            if ("active".equals(promotionUserCart.getStatus())) {
                cartItemPromotionMap.put(promotionUserCart.getCartItemId(), promotionUserCart);
            }
        }

        // 3. 验证商品并计算总金额（应用营销活动折扣）
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal promotionDiscountAmount = BigDecimal.ZERO;
        Long merchantId = null;
        ObjectMapper objectMapper = new ObjectMapper();
        
        for (OrderCreateDTO.OrderItemCreateDTO itemDTO : createDTO.getItems()) {
            Product product = productMapper.selectById(itemDTO.getProductId());
            if (product == null) {
                throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND, "商品不存在: " + itemDTO.getProductId());
            }
            
            // 验证商品状态
            if (product.getStatus() != 1) { // 1表示上架状态
                throw new BusinessException(ResultCode.PRODUCT_OFFLINE, "商品已下架: " + product.getName());
            }
            
            // 验证库存
            if (product.getStock() == null || product.getStock() < itemDTO.getQuantity()) {
                throw new BusinessException(ResultCode.PRODUCT_OUT_OF_STOCK, 
                    "商品库存不足: " + product.getName() + "，当前库存: " + product.getStock());
            }
            
            // 获取商家ID（从第一个商品获取，所有商品必须属于同一商家）
            if (merchantId == null) {
                merchantId = product.getMerchantId();
            } else if (!merchantId.equals(product.getMerchantId())) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED, "订单商品必须属于同一商家");
            }
            
            // 计算商品原价小计
            BigDecimal itemPrice = product.getPrice() != null ? product.getPrice() : itemDTO.getPrice();
            BigDecimal itemOriginalTotal = itemPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            
            // 检查是否有营销活动参与记录
            BigDecimal itemPromotionDiscount = BigDecimal.ZERO;
            if (itemDTO.getCartItemId() != null && cartItemPromotionMap.containsKey(itemDTO.getCartItemId())) {
                PromotionUserCart promotionUserCart = cartItemPromotionMap.get(itemDTO.getCartItemId());
                Promotion promotion = promotionMapper.selectById(promotionUserCart.getPromotionId());
                
                if (promotion != null && "active".equals(promotion.getStatus())) {
                    // 验证活动时间
                    LocalDateTime now = LocalDateTime.now();
                    if (promotion.getStartTime() != null && promotion.getEndTime() != null 
                        && !now.isBefore(promotion.getStartTime()) && !now.isAfter(promotion.getEndTime())) {
                        
                        // 根据活动类型计算折扣
                        itemPromotionDiscount = calculatePromotionDiscount(
                            promotion, itemPrice, itemDTO.getQuantity(), objectMapper);
                    }
                }
            }
            
            // 应用营销活动折扣后的金额
            BigDecimal itemTotal = itemOriginalTotal.subtract(itemPromotionDiscount);
            totalAmount = totalAmount.add(itemTotal);
            promotionDiscountAmount = promotionDiscountAmount.add(itemPromotionDiscount);
        }
        
        if (merchantId == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "无法确定商家ID");
        }

        // 4. 验证优惠券（如果提供）
        BigDecimal couponDiscountAmount = BigDecimal.ZERO;
        if (createDTO.getCouponId() != null) {
            CouponDTO coupon = couponService.getCouponById(createDTO.getCouponId());
            if (coupon == null) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED, "优惠券不存在");
            }
            
            // 验证优惠券商家是否匹配
            if (!merchantId.equals(coupon.getMerchantId())) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED, "优惠券不适用于此商家");
            }
            
            // 验证优惠券使用门槛
            if (coupon.getThreshold() != null && coupon.getThreshold().compareTo(BigDecimal.ZERO) > 0) {
                if (totalAmount.compareTo(coupon.getThreshold()) < 0) {
                    throw new BusinessException(ResultCode.VALIDATE_FAILED, 
                        "订单金额未达到优惠券使用门槛: " + coupon.getThreshold() + "元");
                }
            }
            
            // 计算优惠金额
            if ("cash".equals(coupon.getType())) {
                // 现金券：直接减去优惠金额
                couponDiscountAmount = coupon.getDiscount();
            } else if ("discount".equals(coupon.getType())) {
                // 折扣券：按比例计算
                couponDiscountAmount = totalAmount.multiply(coupon.getDiscount())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } else if ("reduce".equals(coupon.getType())) {
                // 满减券：直接减去优惠金额
                couponDiscountAmount = coupon.getDiscount();
            }
            
            // 优惠金额不能超过订单总金额
            if (couponDiscountAmount.compareTo(totalAmount) > 0) {
                couponDiscountAmount = totalAmount;
            }
        }

        // 5. 计算总优惠金额（营销活动折扣 + 优惠券折扣）
        BigDecimal totalDiscountAmount = promotionDiscountAmount.add(couponDiscountAmount);
        
        // 6. 计算实付金额
        BigDecimal payAmount = totalAmount.subtract(couponDiscountAmount);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
            payAmount = BigDecimal.ZERO;
        }

        // 7. 生成订单号
        String orderNo = generateOrderNo();

        // 8. 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setMerchantId(merchantId);
        order.setTotalAmount(totalAmount.add(promotionDiscountAmount)); // 订单总金额包含营销活动折扣前的金额
        order.setDiscountAmount(totalDiscountAmount); // 总优惠金额（营销活动 + 优惠券）
        order.setPayAmount(payAmount);
        order.setStatus(0); // 待支付
        order.setPayMethod(createDTO.getPayMethod());
        order.setRemark(createDTO.getRemark());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setDeleted(0);
        
        orderMapper.insert(order);

        // 9. 标记营销活动参与记录为已使用
        if (!promotionUserCarts.isEmpty()) {
            List<Long> promotionUserCartIds = promotionUserCarts.stream()
                .map(PromotionUserCart::getId)
                .toList();
            promotionUserCartService.batchUpdateStatusToUsed(promotionUserCartIds);
        }

        // 10. 创建订单项并扣除库存
        for (OrderCreateDTO.OrderItemCreateDTO itemDTO : createDTO.getItems()) {
            Product product = productMapper.selectById(itemDTO.getProductId());
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(itemDTO.getProductId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(itemDTO.getProductImage() != null ? itemDTO.getProductImage() : 
                (product.getImages() != null && !product.getImages().isEmpty() ? 
                    product.getImages().split(",")[0] : ""));
            
            BigDecimal itemPrice = product.getPrice() != null ? product.getPrice() : itemDTO.getPrice();
            orderItem.setPrice(itemPrice);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setTotalAmount(itemPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
            orderItem.setCreatedAt(LocalDateTime.now());
            orderItem.setUpdatedAt(LocalDateTime.now());
            
            orderItemMapper.insert(orderItem);
            
            // 扣除库存
            int newStock = product.getStock() - itemDTO.getQuantity();
            product.setStock(newStock);
            product.setUpdatedAt(LocalDateTime.now());
            productMapper.updateById(product);
        }

        log.info("订单创建成功，订单ID: {}, 订单号: {}", order.getId(), orderNo);
        return order.getId();
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        // 格式：ORD + 年月日时分秒 + 4位随机数
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timeStr = LocalDateTime.now().format(formatter);
        Random random = new Random();
        int randomNum = random.nextInt(9000) + 1000; // 1000-9999
        return "ORD" + timeStr + randomNum;
    }

    @Override
    public IPage<OrderDTO> getOrderPage(OrderQueryDTO queryDTO) {
        Page<OrderDTO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<OrderDTO> result = orderMapper.selectOrderPage(page, queryDTO);
        
        // 为每个订单设置状态文本和商品列表
        result.getRecords().forEach(this::enrichOrderData);
        
        return result;
    }

    @Override
    public OrderDTO getOrderDetail(Long orderId) {
        OrderDTO orderDTO = orderMapper.selectOrderDetail(orderId);
        if (orderDTO != null) {
            enrichOrderData(orderDTO);
        }
        return orderDTO;
    }

    @Override
    public OrderDTO getOrderByOrderNo(String orderNo) {
        OrderDTO orderDTO = orderMapper.selectOrderByOrderNo(orderNo);
        if (orderDTO != null) {
            enrichOrderData(orderDTO);
        }
        return orderDTO;
    }

    @Override
    @Transactional
    public boolean updateOrderStatus(OrderStatusUpdateDTO updateDTO) {
        Order order = new Order();
        order.setId(updateDTO.getOrderId());
        order.setStatus(updateDTO.getStatus());
        order.setRemark(updateDTO.getRemark());
        order.setUpdatedAt(LocalDateTime.now());
        
        int result = orderMapper.updateById(order);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean processOrder(Long orderId) {
        OrderStatusUpdateDTO updateDTO = new OrderStatusUpdateDTO();
        updateDTO.setOrderId(orderId);
        updateDTO.setStatus(2); // 已接单
        updateDTO.setRemark("订单已接单，开始制作");
        return updateOrderStatus(updateDTO);
    }

    @Override
    @Transactional
    public boolean shipOrder(Long orderId) {
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(4); // 已发货
        order.setRemark("订单已发货");
        order.setDeliveryTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        
        int result = orderMapper.updateById(order);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean completeOrder(Long orderId) {
        OrderStatusUpdateDTO updateDTO = new OrderStatusUpdateDTO();
        updateDTO.setOrderId(orderId);
        updateDTO.setStatus(5); // 已完成
        updateDTO.setRemark("订单已完成");
        return updateOrderStatus(updateDTO);
    }

    @Override
    @Transactional
    public boolean cancelOrder(Long orderId) {
        OrderStatusUpdateDTO updateDTO = new OrderStatusUpdateDTO();
        updateDTO.setOrderId(orderId);
        updateDTO.setStatus(6); // 已取消
        updateDTO.setRemark("订单已取消");
        return updateOrderStatus(updateDTO);
    }

    @Override
    @Transactional
    public boolean payOrder(Long orderId, String payMethod) {
        log.info("支付订单，订单ID: {}, 支付方式: {}", orderId, payMethod);
        
        // 查询订单信息
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }
        
        // 验证订单状态
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, 
                "订单状态错误，只有待支付订单可以支付。当前状态: " + getStatusText(order.getStatus()));
        }
        
        // 更新订单状态为已支付
        order.setStatus(1); // 已支付
        order.setPayMethod(payMethod);
        order.setPayTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        
        int result = orderMapper.updateById(order);
        
        if (result > 0) {
            log.info("订单支付成功，订单ID: {}, 支付方式: {}", orderId, payMethod);
            return true;
        } else {
            log.error("订单支付失败，订单ID: {}", orderId);
            return false;
        }
    }

    @Override
    public Object getOrderStatistics(Long id, boolean isMerchant) {
        Map<String, Object> statistics = new HashMap<>();
        
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getDeleted, 0);
        
        if (isMerchant) {
            wrapper.eq(Order::getMerchantId, id);
        } else {
            wrapper.eq(Order::getUserId, id);
        }
        
        // 待支付订单（状态0）
        wrapper.eq(Order::getStatus, 0);
        long pendingCount = orderMapper.selectCount(wrapper);
        
        // 处理中订单（状态1-4：已支付、已接单、制作中、已发货）
        wrapper.clear();
        wrapper.eq(Order::getDeleted, 0);
        if (isMerchant) {
            wrapper.eq(Order::getMerchantId, id);
        } else {
            wrapper.eq(Order::getUserId, id);
        }
        wrapper.in(Order::getStatus, 1, 2, 3, 4);
        long processingCount = orderMapper.selectCount(wrapper);
        
        // 已完成订单（状态5）
        wrapper.clear();
        wrapper.eq(Order::getDeleted, 0);
        if (isMerchant) {
            wrapper.eq(Order::getMerchantId, id);
        } else {
            wrapper.eq(Order::getUserId, id);
        }
        wrapper.eq(Order::getStatus, 5);
        long completedCount = orderMapper.selectCount(wrapper);
        
        // 已取消订单（状态6）
        wrapper.clear();
        wrapper.eq(Order::getDeleted, 0);
        if (isMerchant) {
            wrapper.eq(Order::getMerchantId, id);
        } else {
            wrapper.eq(Order::getUserId, id);
        }
        wrapper.eq(Order::getStatus, 6);
        long cancelledCount = orderMapper.selectCount(wrapper);
        
        // 全部订单
        wrapper.clear();
        wrapper.eq(Order::getDeleted, 0);
        if (isMerchant) {
            wrapper.eq(Order::getMerchantId, id);
        } else {
            wrapper.eq(Order::getUserId, id);
        }
        long totalCount = orderMapper.selectCount(wrapper);
        
        statistics.put("total", totalCount);
        statistics.put("pendingCount", pendingCount);
        statistics.put("processingCount", processingCount);
        statistics.put("completedCount", completedCount);
        statistics.put("cancelledCount", cancelledCount);
        
        return statistics;
    }

    /**
     * 丰富订单数据
     */
    private void enrichOrderData(OrderDTO orderDTO) {
        // 设置状态文本
        orderDTO.setStatusText(getStatusText(orderDTO.getStatus()));
        
        // 查询订单商品列表
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderDTO.getId());
        List<OrderItem> orderItems = orderItemMapper.selectList(wrapper);
        
        // 转换为DTO
        List<OrderItemDTO> itemDTOs = orderItems.stream().map(item -> {
            OrderItemDTO itemDTO = new OrderItemDTO();
            BeanUtils.copyProperties(item, itemDTO);
            return itemDTO;
        }).toList();
        
        orderDTO.setItems(itemDTOs);
    }

    @Override
    public IPage<OrderDTO> getPendingOrderPage(OrderProcessQueryDTO queryDTO) {
        Page<OrderDTO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        // 构建查询条件
        OrderQueryDTO orderQuery = new OrderQueryDTO();
        orderQuery.setOrderNo(queryDTO.getOrderNo());
        orderQuery.setStatus(queryDTO.getStatus());
        orderQuery.setStartTime(queryDTO.getStartTime());
        orderQuery.setEndTime(queryDTO.getEndTime());
        orderQuery.setMerchantId(queryDTO.getMerchantId());
        orderQuery.setPageNum(queryDTO.getPageNum());
        orderQuery.setPageSize(queryDTO.getPageSize());
        
        IPage<OrderDTO> result = orderMapper.selectOrderPage(page, orderQuery);
        
        // 为每个订单设置状态文本和商品列表
        result.getRecords().forEach(this::enrichOrderData);
        
        return result;
    }

    @Override
    @Transactional
    public boolean acceptOrder(OrderProcessDTO processDTO) {
        Order order = new Order();
        order.setId(processDTO.getOrderId());
        order.setStatus(2); // 已接单
        order.setRemark(processDTO.getProcessRemark());
        order.setUpdatedAt(LocalDateTime.now());
        
        int result = orderMapper.updateById(order);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean startMaking(OrderProcessDTO processDTO) {
        Order order = new Order();
        order.setId(processDTO.getOrderId());
        order.setStatus(3); // 制作中
        order.setRemark(processDTO.getProcessRemark());
        order.setUpdatedAt(LocalDateTime.now());
        
        int result = orderMapper.updateById(order);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean finishMaking(OrderProcessDTO processDTO) {
        Order order = new Order();
        order.setId(processDTO.getOrderId());
        order.setStatus(4); // 已发货
        order.setRemark(processDTO.getProcessRemark());
        order.setDeliveryTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        
        int result = orderMapper.updateById(order);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean batchProcessOrders(List<Long> orderIds, Integer processType) {
        try {
            for (Long orderId : orderIds) {
                OrderProcessDTO processDTO = new OrderProcessDTO();
                processDTO.setOrderId(orderId);
                processDTO.setProcessType(processType);
                
                switch (processType) {
                    case 1 -> acceptOrder(processDTO);
                    case 2 -> startMaking(processDTO);
                    case 3 -> finishMaking(processDTO);
                    default -> {
                        log.warn("未知的处理类型: {}", processType);
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            log.error("批量处理订单失败", e);
            return false;
        }
    }

    @Override
    public Object getOrderProcessStatistics(Long merchantId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 查询各种状态的订单数量
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getMerchantId, merchantId);
        wrapper.eq(Order::getDeleted, 0);
        
        // 待处理订单（已支付）
        wrapper.eq(Order::getStatus, 1);
        long pendingCount = orderMapper.selectCount(wrapper);
        
        // 已接单订单
        wrapper.clear();
        wrapper.eq(Order::getMerchantId, merchantId);
        wrapper.eq(Order::getDeleted, 0);
        wrapper.eq(Order::getStatus, 2);
        long acceptedCount = orderMapper.selectCount(wrapper);
        
        // 制作中订单
        wrapper.clear();
        wrapper.eq(Order::getMerchantId, merchantId);
        wrapper.eq(Order::getDeleted, 0);
        wrapper.eq(Order::getStatus, 3);
        long makingCount = orderMapper.selectCount(wrapper);
        
        // 今日处理订单数
        wrapper.clear();
        wrapper.eq(Order::getMerchantId, merchantId);
        wrapper.eq(Order::getDeleted, 0);
        wrapper.ge(Order::getUpdatedAt, LocalDateTime.now().toLocalDate().atStartOfDay());
        wrapper.in(Order::getStatus, 2, 3, 4);
        long todayProcessedCount = orderMapper.selectCount(wrapper);
        
        statistics.put("pendingCount", pendingCount);
        statistics.put("acceptedCount", acceptedCount);
        statistics.put("makingCount", makingCount);
        statistics.put("todayProcessedCount", todayProcessedCount);
        
        return statistics;
    }

    /**
     * 计算营销活动折扣金额
     *
     * @param promotion 营销活动
     * @param originalPrice 商品原价
     * @param quantity 购买数量
     * @param objectMapper JSON对象映射器
     * @return 折扣金额
     */
    private BigDecimal calculatePromotionDiscount(Promotion promotion, BigDecimal originalPrice, 
                                                  Integer quantity, ObjectMapper objectMapper) {
        String type = promotion.getType();
        BigDecimal discount = BigDecimal.ZERO;

        try {
            if ("discount".equals(type) && promotion.getDiscountRate() != null) {
                // 限时折扣：如8表示8折，即原价 * (1 - 0.8) = 原价 * 0.2
                BigDecimal discountRate = promotion.getDiscountRate();
                if (discountRate.compareTo(BigDecimal.ZERO) > 0 && discountRate.compareTo(BigDecimal.TEN) <= 0) {
                    BigDecimal totalPrice = originalPrice.multiply(BigDecimal.valueOf(quantity));
                    BigDecimal discountRateDecimal = discountRate.divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP);
                    BigDecimal actualPrice = totalPrice.multiply(discountRateDecimal);
                    discount = totalPrice.subtract(actualPrice);
                }
            } else if ("second_half_price".equals(type) && quantity >= 2) {
                // 第二件半价：第二件及以后每件半价
                int fullPriceItems = (quantity / 2) * 2;
                int halfPriceItems = quantity - fullPriceItems;
                BigDecimal totalPrice = originalPrice.multiply(BigDecimal.valueOf(quantity));
                BigDecimal actualPrice = originalPrice.multiply(BigDecimal.valueOf(fullPriceItems / 2))
                    .add(originalPrice.multiply(BigDecimal.valueOf(fullPriceItems / 2)).multiply(new BigDecimal("0.5")))
                    .add(originalPrice.multiply(BigDecimal.valueOf(halfPriceItems)));
                discount = totalPrice.subtract(actualPrice);
            } else if ("buy_one_get_one".equals(type) && quantity >= 2) {
                // 买一送一：每两件算一件价格
                int paidItems = (quantity + 1) / 2; // 向上取整
                BigDecimal totalPrice = originalPrice.multiply(BigDecimal.valueOf(quantity));
                BigDecimal actualPrice = originalPrice.multiply(BigDecimal.valueOf(paidItems));
                discount = totalPrice.subtract(actualPrice);
            } else if ("full_reduce".equals(type) && promotion.getFullReduceRules() != null) {
                // 满减活动：需要根据订单总金额计算（这里简化处理，实际应该在订单总金额基础上计算）
                // 注意：满减活动通常针对订单总金额，而不是单个商品
                // 这里暂时返回0，由调用方在订单总金额基础上计算
                discount = BigDecimal.ZERO;
            }
        } catch (Exception e) {
            log.warn("计算营销活动折扣失败，活动ID: {}, 错误: {}", promotion.getId(), e.getMessage());
        }

        return discount.max(BigDecimal.ZERO);
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        
        return switch (status) {
            case 0 -> "待支付";
            case 1 -> "已支付";
            case 2 -> "已接单";
            case 3 -> "制作中";
            case 4 -> "已发货";
            case 5 -> "已完成";
            case 6 -> "已取消";
            case 7 -> "已退款";
            default -> "未知";
        };
    }
}
