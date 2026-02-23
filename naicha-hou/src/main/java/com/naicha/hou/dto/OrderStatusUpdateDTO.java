package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单状态更新DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class OrderStatusUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 新状态
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
