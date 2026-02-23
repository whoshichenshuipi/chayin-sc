package com.naicha.hou.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class CouponDTO {

    /**
     * 优惠券ID
     */
    private Long id;

    /**
     * 商家ID
     */
    private Long merchantId;

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
     * 发放数量
     */
    private Integer totalQuantity;

    /**
     * 剩余数量
     */
    private Integer remainingQuantity;

    /**
     * 领取人数
     */
    private Integer receivedCount;

    /**
     * 使用人数
     */
    private Integer usedCount;

    /**
     * 适用商品范围: all-全店通用, specific-指定商品
     */
    private String productScope;

    /**
     * 指定商品ID列表
     */
    private List<Long> productIds;

    /**
     * 优惠券描述
     */
    private String description;

    /**
     * 领取开始时间
     */
    private LocalDateTime receiveStartTime;

    /**
     * 领取结束时间
     */
    private LocalDateTime receiveEndTime;

    /**
     * 使用有效期开始时间
     */
    private LocalDateTime validStartTime;

    /**
     * 使用有效期结束时间
     */
    private LocalDateTime validEndTime;

    /**
     * 状态: pending-未开始, active-进行中, paused-已暂停, ended-已结束
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 使用明细列表
     */
    private List<CouponUsageDetailDTO> usageDetails;
}

