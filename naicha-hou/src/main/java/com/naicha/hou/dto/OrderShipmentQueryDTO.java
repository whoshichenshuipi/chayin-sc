package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单配送查询DTO
 *
 * @author naicha
 * @since 2024-01-15
 */
@Data
public class OrderShipmentQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 配送状态
     */
    private String shippingStatus;

    /**
     * 配送方式
     */
    private String deliveryType;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 页大小
     */
    private Integer pageSize = 20;
}

