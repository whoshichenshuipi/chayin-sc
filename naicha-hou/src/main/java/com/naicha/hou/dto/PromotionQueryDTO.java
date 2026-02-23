package com.naicha.hou.dto;

import lombok.Data;

/**
 * 促销活动查询DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class PromotionQueryDTO {

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 活动名称（模糊搜索）
     */
    private String name;

    /**
     * 活动类型: discount-限时折扣, full_reduce-满减活动, buy_one_get_one-买一送一, second_half_price-第二件半价
     */
    private String type;

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

