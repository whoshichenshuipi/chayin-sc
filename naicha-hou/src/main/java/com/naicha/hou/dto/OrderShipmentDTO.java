package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单配送DTO
 *
 * @author naicha
 * @since 2024-01-15
 */
@Data
public class OrderShipmentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 订单状态 0-待支付 1-已支付 2-已接单 3-制作中 4-已发货 5-已完成 6-已取消 7-已退款
     */
    private Integer status;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 客户电话
     */
    private String phone;

    /**
     * 收货地址
     */
    private String address;

    /**
     * 详细地址
     */
    private String addressDetail;

    /**
     * 配送方式：self-商家自配送 third_party-第三方配送
     */
    private String deliveryType;

    /**
     * 配送方式文本
     */
    private String deliveryTypeText;

    /**
     * 配送员姓名（自配送）
     */
    private String deliveryPerson;

    /**
     * 配送员电话（自配送）
     */
    private String deliveryPhone;

    /**
     * 配送公司（第三方配送）
     */
    private String deliveryCompany;

    /**
     * 第三方配送单号（第三方配送）
     */
    private String thirdPartyOrderNo;

    /**
     * 配送状态：pending-待发货 shipping-配送中 delivered-已送达 exception-配送异常
     */
    private String shippingStatus;

    /**
     * 配送状态文本
     */
    private String shippingStatusText;

    /**
     * 发货时间
     */
    private LocalDateTime shippingTime;

    /**
     * 发货时间文本
     */
    private String shippingTimeText;

    /**
     * 预计送达时间
     */
    private LocalDateTime estimatedTime;

    /**
     * 预计送达时间文本
     */
    private String estimatedTimeText;

    /**
     * 实际送达时间
     */
    private LocalDateTime actualDeliveryTime;

    /**
     * 配送进度历史
     */
    private List<ShippingProgressDTO> progressHistory;

    /**
     * 配送备注
     */
    private String remark;
}

