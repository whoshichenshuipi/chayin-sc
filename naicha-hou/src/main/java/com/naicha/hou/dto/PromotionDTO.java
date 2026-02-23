package com.naicha.hou.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 促销活动DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class PromotionDTO {

    /**
     * 促销活动ID
     */
    private Long id;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 活动名称
     */
    private String name;

    /**
     * 活动类型: discount-限时折扣, full_reduce-满减活动, buy_one_get_one-买一送一, second_half_price-第二件半价
     */
    private String type;

    /**
     * 活动描述
     */
    private String description;

    /**
     * 折扣比例（限时折扣使用）
     */
    private BigDecimal discountRate;

    /**
     * 满减规则列表
     */
    private List<FullReduceRuleDTO> fullReduceRules;

    /**
     * 是否允许叠加优惠券
     */
    private Boolean allowCoupon;

    /**
     * 赠送方式
     */
    private String giftType;

    /**
     * 参与限制
     */
    private Integer participationLimit;

    /**
     * 活动开始时间
     */
    private LocalDateTime startTime;

    /**
     * 活动结束时间
     */
    private LocalDateTime endTime;

    /**
     * 状态: pending-未开始, active-进行中, paused-已暂停, ended-已结束
     */
    private String status;

    /**
     * 参与人数
     */
    private Integer participantCount;

    /**
     * 活动订单数
     */
    private Integer orderCount;

    /**
     * 活动销售额
     */
    private BigDecimal salesAmount;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 活动商品ID列表
     */
    private List<Long> productIds;

    /**
     * 赠送商品ID列表（买A送B时使用）
     */
    private List<Long> giftProductIds;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 活动订单明细列表
     */
    private List<PromotionOrderDetailDTO> orderDetails;

    /**
     * 满减规则DTO
     */
    @Data
    public static class FullReduceRuleDTO {
        private BigDecimal fullAmount;
        private BigDecimal reduceAmount;
    }
}

