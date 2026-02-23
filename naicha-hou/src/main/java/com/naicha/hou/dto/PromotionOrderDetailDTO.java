package com.naicha.hou.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 促销活动订单明细DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class PromotionOrderDetailDTO {

    /**
     * 订单号
     */
    private String orderId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 下单时间
     */
    private LocalDateTime orderTime;

    /**
     * 原价
     */
    private BigDecimal originalAmount;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 实付金额
     */
    private BigDecimal finalAmount;

    /**
     * 订单状态
     */
    private String status;
}

