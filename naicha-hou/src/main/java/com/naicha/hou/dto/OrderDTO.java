package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class OrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 用户电话
     */
    private String userPhone;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 商家名称
     */
    private String merchantName;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 订单状态 0-待支付 1-已支付 2-已接单 3-制作中 4-已发货 5-已完成 6-已取消 7-已退款
     */
    private Integer status;

    /**
     * 订单状态文本
     */
    private String statusText;

    /**
     * 支付方式
     */
    private String payMethod;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 配送时间
     */
    private LocalDateTime deliveryTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 配送地址
     */
    private String deliveryAddress;

    /**
     * 订单商品列表
     */
    private List<OrderItemDTO> items;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
