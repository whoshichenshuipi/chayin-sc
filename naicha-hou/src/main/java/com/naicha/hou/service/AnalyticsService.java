package com.naicha.hou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.dto.*;

import java.util.List;

/**
 * 数据分析服务接口
 *
 * @author naicha
 * @since 2024-01-25
 */
public interface AnalyticsService {

    /**
     * 获取客户画像概览
     */
    CustomerProfileDTO getCustomerProfile(Long merchantId);

    /**
     * 获取消费频次分析
     */
    ConsumptionFrequencyDTO getConsumptionFrequency(Long merchantId);

    /**
     * 获取消费偏好分析
     */
    List<ConsumptionPreferenceDTO> getConsumptionPreference(Long merchantId);

    /**
     * 获取地域分布分析
     */
    List<RegionDistributionDTO> getRegionDistribution(Long merchantId);

    /**
     * 分页查询客户详情列表
     */
    IPage<CustomerDetailDTO> getCustomerDetailList(Long merchantId, Integer current, Integer size);

    /**
     * 导出客户数据
     */
    void exportCustomerData(Long merchantId);

    // ========== 销售统计相关方法 ==========

    /**
     * 获取核心数据概览
     */
    CoreDataDTO getCoreData(Long merchantId);

    /**
     * 获取订单量趋势数据
     */
    TrendChartDTO getOrderTrend(Long merchantId, Integer days);

    /**
     * 获取销售额趋势数据
     */
    TrendChartDTO getSalesTrend(Long merchantId, Integer days);

    /**
     * 获取商品销量排行
     */
    IPage<ProductRankingDTO> getProductRanking(Long merchantId, Integer current, Integer size);

    /**
     * 获取热门商品
     */
    IPage<HotProductDTO> getHotProducts(Long merchantId, Integer current, Integer size);

    /**
     * 获取滞销商品
     */
    IPage<SlowProductDTO> getSlowProducts(Long merchantId, Integer current, Integer size);

    /**
     * 导出核心数据
     */
    void exportCoreData(Long merchantId);

    /**
     * 导出商品数据
     */
    void exportProductData(Long merchantId, String tab);

    /**
     * 导出全部数据
     */
    void exportAllData(Long merchantId);
}

