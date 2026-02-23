package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 财务查询DTO
 *
 * @author naicha
 * @since 2024-01-20
 */
@Data
public class FinanceQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 当前页
     */
    private Integer current;

    /**
     * 每页大小
     */
    private Integer size;
}

