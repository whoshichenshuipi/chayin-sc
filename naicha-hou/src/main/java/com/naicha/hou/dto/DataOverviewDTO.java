package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 数据概览DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class DataOverviewDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户统计数据
     */
    private UserStats userStats;

    /**
     * 商家统计数据
     */
    private MerchantStats merchantStats;

    /**
     * 订单统计数据
     */
    private OrderStats orderStats;

    /**
     * 交易统计数据
     */
    private TransactionStats transactionStats;

    /**
     * 销量趋势数据
     */
    private TrendChartDTO salesTrend;

    /**
     * 交易额变化数据
     */
    private TrendChartDTO transactionTrend;

    /**
     * 订单状态占比数据
     */
    private List<OrderStatusItem> orderStatusDistribution;

    /**
     * 用户增长趋势数据
     */
    private TrendChartDTO userGrowthTrend;

    /**
     * 用户统计数据
     */
    @Data
    public static class UserStats implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 用户总数
         */
        private Long total;

        /**
         * 新增用户数（当前时间段）
         */
        private Long newUsers;

        /**
         * 增长率（与上期相比）
         */
        private BigDecimal growth;
    }

    /**
     * 商家统计数据
     */
    @Data
    public static class MerchantStats implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 商家总数
         */
        private Long total;

        /**
         * 新增商家数（当前时间段）
         */
        private Long newMerchants;

        /**
         * 活跃商家数（当前时间段有订单的商家）
         */
        private Long activeMerchants;

        /**
         * 增长率（与上期相比）
         */
        private BigDecimal growth;
    }

    /**
     * 订单统计数据
     */
    @Data
    public static class OrderStats implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 订单总数
         */
        private Long total;

        /**
         * 新增订单数（当前时间段）
         */
        private Long newOrders;

        /**
         * 完成订单数（当前时间段）
         */
        private Long completedOrders;

        /**
         * 增长率（与上期相比）
         */
        private BigDecimal growth;
    }

    /**
     * 交易统计数据
     */
    @Data
    public static class TransactionStats implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 交易总额
         */
        private BigDecimal total;

        /**
         * 本期交易额
         */
        private BigDecimal current;

        /**
         * 平均订单金额
         */
        private BigDecimal avgOrder;

        /**
         * 增长率（与上期相比）
         */
        private BigDecimal growth;
    }

    /**
     * 订单状态占比项
     */
    @Data
    public static class OrderStatusItem implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 状态名称
         */
        private String name;

        /**
         * 数量
         */
        private Long count;

        /**
         * 占比（百分比）
         */
        private BigDecimal percentage;
    }
}

