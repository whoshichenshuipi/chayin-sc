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
 * 优惠券创建DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class CouponCreateDTO {

    /**
     * 优惠券名称
     */
    @NotBlank(message = "优惠券名称不能为空")
    private String name;

    /**
     * 优惠券类型: cash-现金券, discount-折扣券, reduce-满减券
     */
    @NotBlank(message = "优惠券类型不能为空")
    private String type;

    /**
     * 使用门槛(元), 0表示无门槛
     */
    @NotNull(message = "使用门槛不能为空")
    @PositiveOrZero(message = "使用门槛不能为负数")
    private BigDecimal threshold;

    /**
     * 优惠金额/折扣比例
     */
    @NotNull(message = "优惠金额/折扣比例不能为空")
    @Positive(message = "优惠金额/折扣比例必须大于0")
    private BigDecimal discount;

    /**
     * 发放数量
     */
    @NotNull(message = "发放数量不能为空")
    @Positive(message = "发放数量必须大于0")
    private Integer totalQuantity;

    /**
     * 领取时间范围 [开始时间, 结束时间]
     */
    @NotNull(message = "领取时间不能为空")
    private List<LocalDateTime> receiveTime;

    /**
     * 使用有效期范围 [开始时间, 结束时间]
     */
    @NotNull(message = "使用有效期不能为空")
    private List<LocalDateTime> validTime;

    /**
     * 适用商品范围: all-全店通用, specific-指定商品
     */
    @NotBlank(message = "适用商品范围不能为空")
    private String productScope;

    /**
     * 指定商品ID列表（当productScope为specific时必填）
     */
    private List<Long> productIds;

    /**
     * 优惠券描述
     */
    private String description;
}

