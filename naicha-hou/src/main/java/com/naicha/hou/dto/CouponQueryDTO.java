package com.naicha.hou.dto;

import lombok.Data;

/**
 * 优惠券查询DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class CouponQueryDTO {

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 优惠券名称（模糊搜索）
     */
    private String name;

    /**
     * 状态: pending-未开始, active-进行中, paused-已暂停, ended-已结束
     */
    private String status;

    /**
     * 当前页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;
}

