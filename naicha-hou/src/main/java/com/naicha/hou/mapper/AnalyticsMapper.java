package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.naicha.hou.dto.CustomerDetailDTO;
import com.naicha.hou.dto.ConsumptionPreferenceDTO;
import com.naicha.hou.dto.RegionDistributionDTO;
import com.naicha.hou.dto.ProductRankingDTO;
import com.naicha.hou.dto.HotProductDTO;
import com.naicha.hou.dto.SlowProductDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据分析Mapper接口
 *
 * @author naicha
 * @since 2024-01-25
 */
@Mapper
public interface AnalyticsMapper extends BaseMapper<Object> {

    /**
     * 获取客户画像概览
     */
    Long selectTotalCustomers(@Param("merchantId") Long merchantId);

    /**
     * 获取活跃客户数（最近30天内有订单）
     */
    Long selectActiveCustomers(@Param("merchantId") Long merchantId, @Param("startTime") LocalDateTime startTime);

    /**
     * 获取新客户数（本月）
     */
    Long selectNewCustomers(@Param("merchantId") Long merchantId, @Param("startTime") LocalDateTime startTime);

    /**
     * 获取消费频次数据（返回频次段和数量）
     */
    List<java.util.Map<String, Object>> selectFrequencyData(@Param("merchantId") Long merchantId, @Param("startTime") LocalDateTime startTime);

    /**
     * 获取消费偏好数据
     */
    List<ConsumptionPreferenceDTO> selectPreferenceData(@Param("merchantId") Long merchantId, @Param("startTime") LocalDateTime startTime);

    /**
     * 获取地域分布数据
     */
    List<RegionDistributionDTO> selectRegionDistribution(@Param("merchantId") Long merchantId);

    /**
     * 分页查询客户详情列表
     */
    IPage<CustomerDetailDTO> selectCustomerDetailPage(
            Page<CustomerDetailDTO> page,
            @Param("merchantId") Long merchantId);

    // ========== 销售统计相关方法 ==========

    /**
     * 获取今日订单量
     */
    Long selectTodayOrderCount(@Param("merchantId") Long merchantId, @Param("startTime") LocalDateTime startTime);

    /**
     * 获取今日销售额
     */
    BigDecimal selectTodaySalesAmount(@Param("merchantId") Long merchantId, @Param("startTime") LocalDateTime startTime);

    /**
     * 获取本月订单量
     */
    Long selectMonthOrderCount(@Param("merchantId") Long merchantId, @Param("startTime") LocalDateTime startTime);

    /**
     * 获取本月销售额
     */
    BigDecimal selectMonthSalesAmount(@Param("merchantId") Long merchantId, @Param("startTime") LocalDateTime startTime);

    /**
     * 获取店铺评分
     */
    BigDecimal selectShopRating(@Param("merchantId") Long merchantId);

    /**
     * 获取复购率（最近30天内有2次以上订单的客户数 / 总客户数）
     */
    BigDecimal selectRepurchaseRate(@Param("merchantId") Long merchantId, @Param("startTime") LocalDateTime startTime);

    /**
     * 获取订单量趋势数据（按日期分组）
     */
    List<java.util.Map<String, Object>> selectOrderTrendData(
            @Param("merchantId") Long merchantId,
            @Param("startTime") LocalDateTime startTime);

    /**
     * 获取销售额趋势数据（按日期分组）
     */
    List<java.util.Map<String, Object>> selectSalesTrendData(
            @Param("merchantId") Long merchantId,
            @Param("startTime") LocalDateTime startTime);

    /**
     * 获取商品销量排行
     */
    List<ProductRankingDTO> selectProductRanking(
            @Param("merchantId") Long merchantId,
            @Param("startTime") LocalDateTime startTime,
            @Param("limit") Integer limit);

    /**
     * 获取热门商品（增长率较高的商品）
     */
    List<HotProductDTO> selectHotProducts(
            @Param("merchantId") Long merchantId,
            @Param("currentStartTime") LocalDateTime currentStartTime,
            @Param("previousStartTime") LocalDateTime previousStartTime,
            @Param("limit") Integer limit);

    /**
     * 获取滞销商品（销量低且库存高的商品）
     */
    List<SlowProductDTO> selectSlowProducts(
            @Param("merchantId") Long merchantId,
            @Param("startTime") LocalDateTime startTime,
            @Param("limit") Integer limit);
}

