package com.naicha.hou.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 优惠券使用明细DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class CouponUsageDetailDTO {

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 领取时间
     */
    private LocalDateTime receivedTime;

    /**
     * 使用时间
     */
    private LocalDateTime usedTime;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 状态: unused-未使用, used-已使用, expired-已过期
     */
    private String status;
}

