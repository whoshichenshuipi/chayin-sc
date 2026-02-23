package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券DTO（包含优惠券信息和用户领取状态）
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class UserCouponDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户优惠券ID（coupon_user表的主键）
     */
    private Long id;

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 优惠券名称
     */
    private String name;

    /**
     * 类型: cash-现金券, discount-折扣券, reduce-满减券
     */
    private String type;

    /**
     * 使用门槛(元), 0表示无门槛
     */
    private BigDecimal threshold;

    /**
     * 优惠金额/折扣比例
     */
    private BigDecimal discount;

    /**
     * 适用商品范围: all-全店通用, specific-指定商品
     */
    private String productScope;

    /**
     * 优惠券描述
     */
    private String description;

    /**
     * 使用有效期开始时间
     */
    private LocalDateTime validStartTime;

    /**
     * 使用有效期结束时间
     */
    private LocalDateTime validEndTime;

    /**
     * 领取时间
     */
    private LocalDateTime receivedTime;

    /**
     * 使用时间
     */
    private LocalDateTime usedTime;

    /**
     * 使用订单ID
     */
    private Long orderId;

    /**
     * 使用订单号
     */
    private String orderNo;

    /**
     * 状态: unused-未使用, used-已使用, expired-已过期
     */
    private String status;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 商家名称
     */
    private String merchantName;
}

