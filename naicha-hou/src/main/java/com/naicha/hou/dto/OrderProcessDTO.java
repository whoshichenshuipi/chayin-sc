package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单处理DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class OrderProcessDTO implements Serializable {

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
     * 处理类型：1-接单 2-开始制作 3-制作完成 4-发货
     */
    private Integer processType;

    /**
     * 处理备注
     */
    private String processRemark;

    /**
     * 预计完成时间
     */
    private LocalDateTime estimatedTime;

    /**
     * 处理人员
     */
    private String processor;
}
