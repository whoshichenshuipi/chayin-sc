package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 客户画像概览DTO
 *
 * @author naicha
 * @since 2024-01-25
 */
@Data
public class CustomerProfileDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总客户数
     */
    private Long totalCustomers;

    /**
     * 活跃客户数（最近30天内有订单）
     */
    private Long activeCustomers;

    /**
     * 新客户数（本月）
     */
    private Long newCustomers;
}

