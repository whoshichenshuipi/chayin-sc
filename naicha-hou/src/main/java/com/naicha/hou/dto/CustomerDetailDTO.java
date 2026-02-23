package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户详情DTO
 *
 * @author naicha
 * @since 2024-01-25
 */
@Data
public class CustomerDetailDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 联系方式
     */
    private String phone;

    /**
     * 消费频次（次/周）
     */
    private Double frequency;

    /**
     * 消费偏好
     */
    private String preference;

    /**
     * 所属区域
     */
    private String region;

    /**
     * 累计消费金额
     */
    private BigDecimal totalAmount;

    /**
     * 最近订单时间
     */
    private LocalDateTime lastOrderTime;
}

