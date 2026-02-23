package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单发货请求DTO
 *
 * @author naicha
 * @since 2024-01-15
 */
@Data
public class OrderShipRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 配送方式：self-商家自配送 third_party-第三方配送
     */
    private String deliveryType;

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
     * 预计送达时间
     */
    private LocalDateTime estimatedTime;

    /**
     * 发货备注
     */
    private String remark;
}

