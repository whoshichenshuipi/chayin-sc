package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 管理员仪表盘数据DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class AdminDashboardDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 统计卡片数据
     */
    private StatsData stats;

    /**
     * 用户增长趋势数据
     */
    private TrendChartDTO userGrowthTrend;

    /**
     * 订单统计趋势数据
     */
    private OrderStatsTrend orderStatsTrend;

    /**
     * 最近活动列表
     */
    private List<RecentActivityDTO> recentActivities;

    /**
     * 统计卡片数据
     */
    @Data
    public static class StatsData implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 商家总数
         */
        private Long totalMerchants;

        /**
         * 用户总数
         */
        private Long totalUsers;

        /**
         * 订单总数
         */
        private Long totalOrders;

        /**
         * 平台收入（累计已支付订单金额）
         */
        private BigDecimal platformRevenue;
    }

    /**
     * 订单统计趋势数据
     */
    @Data
    public static class OrderStatsTrend implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 日期分类
         */
        private List<String> categories;

        /**
         * 订单数量数据
         */
        private List<Long> orderCounts;

        /**
         * 订单金额数据
         */
        private List<BigDecimal> orderAmounts;
    }

    /**
     * 最近活动DTO
     */
    @Data
    public static class RecentActivityDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 活动类型：merchant_register, system_update, user_feedback, order_created等
         */
        private String type;

        /**
         * 活动标题
         */
        private String title;

        /**
         * 活动描述
         */
        private String description;

        /**
         * 活动时间
         */
        private String timestamp;
    }
}

