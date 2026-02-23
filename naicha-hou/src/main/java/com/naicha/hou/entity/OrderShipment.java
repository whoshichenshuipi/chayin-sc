package com.naicha.hou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单配送实体类
 *
 * @author naicha
 * @since 2024-01-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("order_shipment")
public class OrderShipment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配送ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 订单号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 配送方式：self-商家自配送 third_party-第三方配送
     */
    @TableField("delivery_type")
    private String deliveryType;

    /**
     * 配送员姓名（自配送）
     */
    @TableField("delivery_person")
    private String deliveryPerson;

    /**
     * 配送员电话（自配送）
     */
    @TableField("delivery_phone")
    private String deliveryPhone;

    /**
     * 配送公司（第三方配送）
     */
    @TableField("delivery_company")
    private String deliveryCompany;

    /**
     * 第三方配送单号（第三方配送）
     */
    @TableField("third_party_order_no")
    private String thirdPartyOrderNo;

    /**
     * 配送状态：pending-待发货 shipping-配送中 delivered-已送达 exception-配送异常
     */
    @TableField("shipping_status")
    private String shippingStatus;

    /**
     * 发货时间
     */
    @TableField("shipping_time")
    private LocalDateTime shippingTime;

    /**
     * 预计送达时间
     */
    @TableField("estimated_time")
    private LocalDateTime estimatedTime;

    /**
     * 实际送达时间
     */
    @TableField("actual_delivery_time")
    private LocalDateTime actualDeliveryTime;

    /**
     * 配送备注
     */
    @TableField("remark")
    private String remark;

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

