package com.naicha.hou.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理员仪表盘Mapper接口
 *
 * @author naicha
 * @since 2024-01-01
 */
@Mapper
public interface AdminDashboardMapper {

    /**
     * 获取商家总数
     */
    Long selectTotalMerchants();

    /**
     * 获取用户总数
     */
    Long selectTotalUsers();

    /**
     * 获取订单总数
     */
    Long selectTotalOrders();

    /**
     * 获取平台收入（所有已支付订单的总金额）
     */
    BigDecimal selectPlatformRevenue();

    /**
     * 获取用户增长趋势数据
     *
     * @param days 天数
     * @param startTime 开始时间
     * @return 趋势数据列表，包含日期和数量
     */
    List<Map<String, Object>> selectUserGrowthTrend(@Param("days") Integer days, @Param("startTime") LocalDateTime startTime);

    /**
     * 获取订单统计趋势数据
     *
     * @param days 天数
     * @param startTime 开始时间
     * @return 趋势数据列表，包含日期、订单数量和订单金额
     */
    List<Map<String, Object>> selectOrderStatsTrend(@Param("days") Integer days, @Param("startTime") LocalDateTime startTime);

    /**
     * 获取最近活动列表
     *
     * @param limit 限制条数
     * @return 活动列表
     */
    List<Map<String, Object>> selectRecentActivities(@Param("limit") Integer limit);
}

