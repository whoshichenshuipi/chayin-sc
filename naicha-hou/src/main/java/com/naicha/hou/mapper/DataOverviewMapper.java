package com.naicha.hou.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据概览Mapper接口
 *
 * @author naicha
 * @since 2024-01-01
 */
@Mapper
public interface DataOverviewMapper {

    /**
     * 获取用户总数
     */
    Long selectTotalUsers();

    /**
     * 获取时间段内的新增用户数
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 新增用户数
     */
    Long selectNewUsers(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 获取上期时间段内的新增用户数（用于计算增长率）
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 新增用户数
     */
    Long selectPreviousPeriodNewUsers(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 获取商家总数
     */
    Long selectTotalMerchants();

    /**
     * 获取时间段内的新增商家数
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 新增商家数
     */
    Long selectNewMerchants(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 获取上期时间段内的新增商家数
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 新增商家数
     */
    Long selectPreviousPeriodNewMerchants(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 获取活跃商家数（时间段内有订单的商家）
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 活跃商家数
     */
    Long selectActiveMerchants(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 获取订单总数
     */
    Long selectTotalOrders();

    /**
     * 获取时间段内的新增订单数
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 新增订单数
     */
    Long selectNewOrders(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 获取上期时间段内的新增订单数
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 新增订单数
     */
    Long selectPreviousPeriodNewOrders(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 获取时间段内的完成订单数
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 完成订单数
     */
    Long selectCompletedOrders(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 获取交易总额
     */
    BigDecimal selectTotalTransactionAmount();

    /**
     * 获取时间段内的交易额
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 交易额
     */
    BigDecimal selectCurrentPeriodTransactionAmount(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 获取上期时间段内的交易额
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 交易额
     */
    BigDecimal selectPreviousPeriodTransactionAmount(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 获取时间段内的平均订单金额
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 平均订单金额
     */
    BigDecimal selectAvgOrderAmount(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 获取销量趋势数据（根据周期聚合）
     *
     * @param period 周期类型：day, week, month, year
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 趋势数据列表
     */
    List<Map<String, Object>> selectSalesTrend(@Param("period") String period,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 获取交易额趋势数据（根据周期聚合）
     *
     * @param period 周期类型：day, week, month, year
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 趋势数据列表
     */
    List<Map<String, Object>> selectTransactionTrend(@Param("period") String period,
                                                       @Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 获取订单状态占比数据
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 状态占比数据列表
     */
    List<Map<String, Object>> selectOrderStatusDistribution(@Param("startTime") LocalDateTime startTime,
                                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 获取用户增长趋势数据（根据周期聚合）
     *
     * @param period 周期类型：day, week, month, year
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 趋势数据列表
     */
    List<Map<String, Object>> selectUserGrowthTrend(@Param("period") String period,
                                                     @Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);
}

