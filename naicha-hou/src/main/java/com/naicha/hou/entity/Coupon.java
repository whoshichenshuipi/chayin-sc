package com.naicha.hou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券实体类
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("coupon")
public class Coupon implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 优惠券ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商家ID
     */
    @TableField("merchant_id")
    private Long merchantId;

    /**
     * 优惠券名称
     */
    @TableField("name")
    private String name;

    /**
     * 类型: cash-现金券, discount-折扣券, reduce-满减券
     */
    @TableField("type")
    private String type;

    /**
     * 使用门槛(元), 0表示无门槛
     */
    @TableField("threshold")
    private BigDecimal threshold;

    /**
     * 优惠金额/折扣比例
     */
    @TableField("discount")
    private BigDecimal discount;

    /**
     * 发放数量
     */
    @TableField("total_quantity")
    private Integer totalQuantity;

    /**
     * 剩余数量
     */
    @TableField("remaining_quantity")
    private Integer remainingQuantity;

    /**
     * 适用商品范围: all-全店通用, specific-指定商品
     */
    @TableField("product_scope")
    private String productScope;

    /**
     * 优惠券描述
     */
    @TableField("description")
    private String description;

    /**
     * 领取开始时间
     */
    @TableField("receive_start_time")
    private LocalDateTime receiveStartTime;

    /**
     * 领取结束时间
     */
    @TableField("receive_end_time")
    private LocalDateTime receiveEndTime;

    /**
     * 使用有效期开始时间
     */
    @TableField("valid_start_time")
    private LocalDateTime validStartTime;

    /**
     * 使用有效期结束时间
     */
    @TableField("valid_end_time")
    private LocalDateTime validEndTime;

    /**
     * 状态: pending-未开始, active-进行中, paused-已暂停, ended-已结束
     */
    @TableField("status")
    private String status;

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

