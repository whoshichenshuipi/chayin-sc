package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户优惠券查询DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class UserCouponQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 状态: unused-未使用, used-已使用, expired-已过期
     */
    private String status;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 页大小
     */
    private Integer pageSize = 20;
}

