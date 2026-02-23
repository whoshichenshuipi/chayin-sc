package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商家评分查询DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class MerchantRatingQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商家ID（可选，为空则查询所有商家）
     */
    private Long merchantId;

    /**
     * 商家名称（模糊查询）
     */
    private String merchantName;

    /**
     * 评分筛选（1-5星）
     */
    private Integer rating;

    /**
     * 开始时间（评价时间范围）
     */
    private LocalDateTime startTime;

    /**
     * 结束时间（评价时间范围）
     */
    private LocalDateTime endTime;

    /**
     * 当前页
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 20;
}

