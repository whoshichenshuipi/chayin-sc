package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 核心数据DTO
 * 用于销售统计的核心数据概览
 *
 * @author naicha
 * @since 2024-01-25
 */
@Data
public class CoreDataDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 今日订单量
     */
    private Long todayOrderCount;

    /**
     * 今日销售额
     */
    private BigDecimal todaySalesAmount;

    /**
     * 本月订单量
     */
    private Long monthOrderCount;

    /**
     * 本月销售额
     */
    private BigDecimal monthSalesAmount;

    /**
     * 店铺评分
     */
    private BigDecimal shopRating;

    /**
     * 复购率（百分比）
     */
    private BigDecimal repurchaseRate;
}

