package com.naicha.hou.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据报表Mapper接口
 *
 * @author naicha
 * @since 2024-01-01
 */
@Mapper
public interface DataReportMapper {

    /**
     * 获取报表摘要数据
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 摘要数据
     */
    Map<String, Object> selectReportSummary(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 获取销量趋势数据（用于报表）
     *
     * @param period 周期类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 趋势数据列表
     */
    List<Map<String, Object>> selectSalesTrendForReport(@Param("period") String period,
                                                        @Param("startTime") LocalDateTime startTime,
                                                        @Param("endTime") LocalDateTime endTime);

    /**
     * 获取上期销量数据（用于计算增长率）
     *
     * @param period 周期类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 销量总数
     */
    Long selectPreviousPeriodSales(@Param("period") String period,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 获取交易额趋势数据（用于报表）
     *
     * @param period 周期类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 趋势数据列表
     */
    List<Map<String, Object>> selectTransactionTrendForReport(@Param("period") String period,
                                                              @Param("startTime") LocalDateTime startTime,
                                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 获取上期交易额（用于计算增长率）
     *
     * @param period 周期类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 交易额
     */
    BigDecimal selectPreviousPeriodTransaction(@Param("period") String period,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 获取订单状态分布数据（用于报表）
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 状态分布数据
     */
    List<Map<String, Object>> selectOrderStatusForReport(@Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 获取用户增长趋势数据（用于报表）
     *
     * @param period 周期类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 趋势数据列表
     */
    List<Map<String, Object>> selectUserGrowthTrendForReport(@Param("period") String period,
                                                              @Param("startTime") LocalDateTime startTime,
                                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 获取上期新增用户数（用于计算增长率）
     *
     * @param period 周期类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 新增用户数
     */
    Long selectPreviousPeriodNewUsers(@Param("period") String period,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 获取活跃用户数
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 活跃用户数
     */
    Long selectActiveUsers(@Param("startTime") LocalDateTime startTime,
                           @Param("endTime") LocalDateTime endTime);

    /**
     * 获取报表详细表格数据
     *
     * @param period 周期类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 表格数据列表
     */
    List<Map<String, Object>> selectReportTableData(@Param("period") String period,
                                                     @Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);
}

