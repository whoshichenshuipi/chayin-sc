package com.naicha.hou.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 促销活动创建DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class PromotionCreateDTO {

    /**
     * 活动名称
     */
    @NotBlank(message = "活动名称不能为空")
    private String name;

    /**
     * 活动类型: discount-限时折扣, full_reduce-满减活动, buy_one_get_one-买一送一, second_half_price-第二件半价
     */
    @NotBlank(message = "活动类型不能为空")
    private String type;

    /**
     * 活动时间范围 [开始时间, 结束时间]
     */
    @NotNull(message = "活动时间不能为空")
    private List<LocalDateTime> timeRange;

    /**
     * 活动描述
     */
    private String description;

    // ========== 限时折扣相关 ==========
    /**
     * 活动商品ID列表（限时折扣、买一送一、第二件半价使用）
     */
    private List<Long> productIds;

    /**
     * 折扣比例（限时折扣使用，如8表示8折）
     */
    @Positive(message = "折扣比例必须大于0")
    private BigDecimal discountRate;

    // ========== 满减活动相关 ==========
    /**
     * 满减规则列表
     */
    private List<FullReduceRuleDTO> fullReduceRules;

    /**
     * 是否允许叠加平台优惠券
     */
    private Boolean allowCoupon;

    // ========== 买一送一相关 ==========
    /**
     * 赠送方式: same-买A送A, different-买A送B
     */
    private String giftType;

    /**
     * 赠送商品ID列表（买A送B时使用）
     */
    private List<Long> giftProductIds;

    // ========== 第二件半价相关 ==========
    /**
     * 参与限制，0表示不限制
     */
    @PositiveOrZero(message = "参与限制不能为负数")
    private Integer participationLimit;

    /**
     * 满减规则DTO
     */
    @Data
    public static class FullReduceRuleDTO {
        /**
         * 满额
         */
        @NotNull(message = "满额不能为空")
        @Positive(message = "满额必须大于0")
        private BigDecimal fullAmount;

        /**
         * 减额
         */
        @NotNull(message = "减额不能为空")
        @Positive(message = "减额必须大于0")
        private BigDecimal reduceAmount;
    }
}

