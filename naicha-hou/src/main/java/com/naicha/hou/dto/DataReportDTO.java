package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 数据报表DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class DataReportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 生成时间
     */
    private String generateTime;

    /**
     * 报表周期（daily/weekly/monthly/yearly）
     */
    private String period;

    /**
     * 日期范围描述
     */
    private String dateRange;

    /**
     * 报表摘要数据
     */
    private ReportSummary summary;

    /**
     * 图表数据
     */
    private ChartData charts;

    /**
     * 详细数据表格
     */
    private List<ReportTableItem> tableData;

    /**
     * 报表摘要
     */
    @Data
    public static class ReportSummary implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 总用户数
         */
        private Long totalUsers;

        /**
         * 总订单数
         */
        private Long totalOrders;

        /**
         * 总交易额
         */
        private BigDecimal totalAmount;

        /**
         * 活跃商家数
         */
        private Long activeMerchants;
    }

    /**
     * 图表数据
     */
    @Data
    public static class ChartData implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 销量趋势数据
         */
        private TrendChartWithGrowth sales;

        /**
         * 交易额趋势数据
         */
        private TrendChartWithGrowth transaction;

        /**
         * 订单状态分布数据
         */
        private OrderStatusDistribution orderStatus;

        /**
         * 用户增长趋势数据
         */
        private UserGrowthChart userGrowth;
    }

    /**
     * 带增长率的趋势图表
     */
    @Data
    public static class TrendChartWithGrowth implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 分类（日期）
         */
        private List<String> categories;

        /**
         * 数据值
         */
        private List<Number> data;

        /**
         * 增长率
         */
        private BigDecimal growth;
    }

    /**
     * 订单状态分布
     */
    @Data
    public static class OrderStatusDistribution implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 状态数据列表
         */
        private List<OrderStatusItem> data;

        /**
         * 完成率
         */
        private BigDecimal completionRate;

        /**
         * 取消率
         */
        private BigDecimal cancelRate;

        /**
         * 待处理率
         */
        private BigDecimal pendingRate;
    }

    /**
     * 订单状态项
     */
    @Data
    public static class OrderStatusItem implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 状态名称
         */
        private String name;

        /**
         * 占比值
         */
        private BigDecimal value;

        /**
         * 颜色
         */
        private String color;
    }

    /**
     * 用户增长图表
     */
    @Data
    public static class UserGrowthChart implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 分类（日期）
         */
        private List<String> categories;

        /**
         * 数据值
         */
        private List<Number> data;

        /**
         * 新增用户数
         */
        private Long newUsers;

        /**
         * 活跃用户数
         */
        private Long activeUsers;

        /**
         * 增长率
         */
        private BigDecimal growth;
    }

    /**
     * 报表表格数据项
     */
    @Data
    public static class ReportTableItem implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 日期
         */
        private String date;

        /**
         * 新增用户数
         */
        private Long newUsers;

        /**
         * 新增订单数
         */
        private Long newOrders;

        /**
         * 销量
         */
        private Long sales;

        /**
         * 交易额
         */
        private BigDecimal transaction;

        /**
         * 平均订单金额
         */
        private BigDecimal avgOrderValue;

        /**
         * 完成率
         */
        private BigDecimal completionRate;
    }
}

