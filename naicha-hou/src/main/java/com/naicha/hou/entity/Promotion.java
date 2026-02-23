package com.naicha.hou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 促销活动实体类
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("promotion")
public class Promotion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 促销活动ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商家ID
     */
    @TableField("merchant_id")
    private Long merchantId;

    /**
     * 活动名称
     */
    @TableField("name")
    private String name;

    /**
     * 活动类型: discount-限时折扣, full_reduce-满减活动, buy_one_get_one-买一送一, second_half_price-第二件半价
     */
    @TableField("type")
    private String type;

    /**
     * 活动描述
     */
    @TableField("description")
    private String description;

    /**
     * 折扣比例（限时折扣使用，如8表示8折）
     */
    @TableField("discount_rate")
    private BigDecimal discountRate;

    /**
     * 满减规则（JSON格式存储，用于满减活动）
     */
    @TableField("full_reduce_rules")
    private String fullReduceRules;

    /**
     * 是否允许叠加优惠券 0-否 1-是
     */
    @TableField("allow_coupon")
    private Integer allowCoupon;

    /**
     * 赠送方式（买一送一使用）: same-买A送A, different-买A送B
     */
    @TableField("gift_type")
    private String giftType;

    /**
     * 参与限制（第二件半价使用），0表示不限制
     */
    @TableField("participation_limit")
    private Integer participationLimit;

    /**
     * 活动开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 活动结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 状态: pending-未开始, active-进行中, paused-已暂停, ended-已结束
     */
    @TableField("status")
    private String status;

    /**
     * 参与人数
     */
    @TableField("participant_count")
    private Integer participantCount;

    /**
     * 活动订单数
     */
    @TableField("order_count")
    private Integer orderCount;

    /**
     * 活动销售额
     */
    @TableField("sales_amount")
    private BigDecimal salesAmount;

    /**
     * 优惠金额
     */
    @TableField("discount_amount")
    private BigDecimal discountAmount;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 删除标志 0-未删除 1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}

