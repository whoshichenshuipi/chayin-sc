package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 配送进度更新请求DTO
 *
 * @author naicha
 * @since 2024-01-15
 */
@Data
public class ShippingProgressUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 配送状态
     */
    private String status;

    /**
     * 进度描述
     */
    private String description;

    /**
     * 预计送达时间
     */
    private LocalDateTime estimatedTime;
}

