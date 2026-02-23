package com.naicha.hou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.common.Result;
import com.naicha.hou.dto.*;
import com.naicha.hou.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据分析控制器
 *
 * @author naicha
 * @since 2024-01-25
 */
@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Validated
@Tag(name = "数据分析接口", description = "客户分析、销售统计等数据分析接口")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * 获取客户画像概览
     */
    @GetMapping("/customer-profile")
    @Operation(summary = "获取客户画像概览", description = "获取总客户数、活跃客户数、新客户数")
    public Result<CustomerProfileDTO> getCustomerProfile(
            @Parameter(description = "商家ID", required = true)
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId) {

        log.info("获取客户画像概览，商家ID: {}", merchantId);

        CustomerProfileDTO profile = analyticsService.getCustomerProfile(merchantId);
        return Result.success("查询成功", profile);
    }

    /**
     * 获取消费频次分析
     */
    @GetMapping("/consumption-frequency")
    @Operation(summary = "获取消费频次分析", description = "获取客户消费频次分布数据")
    public Result<ConsumptionFrequencyDTO> getConsumptionFrequency(
            @Parameter(description = "商家ID", required = true)
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId) {

        log.info("获取消费频次分析，商家ID: {}", merchantId);

        ConsumptionFrequencyDTO frequency = analyticsService.getConsumptionFrequency(merchantId);
        return Result.success("查询成功", frequency);
    }

    /**
     * 获取消费偏好分析
     */
    @GetMapping("/consumption-preference")
    @Operation(summary = "获取消费偏好分析", description = "获取客户消费偏好分布数据")
    public Result<List<ConsumptionPreferenceDTO>> getConsumptionPreference(
            @Parameter(description = "商家ID", required = true)
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId) {

        log.info("获取消费偏好分析，商家ID: {}", merchantId);

        List<ConsumptionPreferenceDTO> preferences = analyticsService.getConsumptionPreference(merchantId);
        return Result.success("查询成功", preferences);
    }

    /**
     * 获取地域分布分析
     */
    @GetMapping("/region-distribution")
    @Operation(summary = "获取地域分布分析", description = "获取客户地域分布数据")
    public Result<List<RegionDistributionDTO>> getRegionDistribution(
            @Parameter(description = "商家ID", required = true)
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId) {

        log.info("获取地域分布分析，商家ID: {}", merchantId);

        List<RegionDistributionDTO> regions = analyticsService.getRegionDistribution(merchantId);
        return Result.success("查询成功", regions);
    }

    /**
     * 分页查询客户详情列表
     */
    @PostMapping("/customer-detail")
    @Operation(summary = "分页查询客户详情列表", description = "根据条件分页查询客户详情")
    public Result<IPage<CustomerDetailDTO>> getCustomerDetailList(
            @Parameter(description = "商家ID", required = true)
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId,
            @RequestBody java.util.Map<String, Object> params) {

        Integer current = params != null && params.get("current") != null ? 
                ((Number) params.get("current")).intValue() : 1;
        Integer size = params != null && params.get("size") != null ? 
                ((Number) params.get("size")).intValue() : 20;

        log.info("分页查询客户详情列表，商家ID: {}, 当前页: {}, 每页大小: {}", merchantId, current, size);

        IPage<CustomerDetailDTO> result = analyticsService.getCustomerDetailList(merchantId, current, size);
        return Result.success("查询成功", result);
    }

    /**
     * 导出客户数据
     */
    @PostMapping("/export/customer-data")
    @Operation(summary = "导出客户数据", description = "导出客户数据为Excel或CSV")
    public Result<String> exportCustomerData(
            @Parameter(description = "商家ID", required = true)
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId) {

        log.info("导出客户数据，商家ID: {}", merchantId);

        analyticsService.exportCustomerData(merchantId);
        return Result.success("导出成功");
    }

    // ========== 销售统计相关接口 ==========

    /**
     * 获取核心数据概览
     */
    @GetMapping("/core-data")
    @Operation(summary = "获取核心数据概览", description = "获取今日订单量、今日销售额、本月订单量、本月销售额、店铺评分、复购率")
    public Result<CoreDataDTO> getCoreData(
            @Parameter(description = "商家ID", required = true)
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId) {

        log.info("获取核心数据概览，商家ID: {}", merchantId);

        CoreDataDTO coreData = analyticsService.getCoreData(merchantId);
        return Result.success("查询成功", coreData);
    }

    /**
     * 获取订单量趋势
     */
    @GetMapping("/order-trend")
    @Operation(summary = "获取订单量趋势", description = "获取指定天数内的订单量趋势数据")
    public Result<TrendChartDTO> getOrderTrend(
            @Parameter(description = "商家ID", required = true)
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId,
            @Parameter(description = "天数，默认7天")
            @RequestParam(value = "days", required = false, defaultValue = "7") Integer days) {

        log.info("获取订单量趋势，商家ID: {}, 天数: {}", merchantId, days);

        TrendChartDTO trend = analyticsService.getOrderTrend(merchantId, days);
        return Result.success("查询成功", trend);
    }

    /**
     * 获取销售额趋势
     */
    @GetMapping("/sales-trend")
    @Operation(summary = "获取销售额趋势", description = "获取指定天数内的销售额趋势数据")
    public Result<TrendChartDTO> getSalesTrend(
            @Parameter(description = "商家ID", required = true)
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId,
            @Parameter(description = "天数，默认30天")
            @RequestParam(value = "days", required = false, defaultValue = "30") Integer days) {

        log.info("获取销售额趋势，商家ID: {}, 天数: {}", merchantId, days);

        TrendChartDTO trend = analyticsService.getSalesTrend(merchantId, days);
        return Result.success("查询成功", trend);
    }

    /**
     * 获取商品销量排行
     */
    @PostMapping("/product-ranking")
    @Operation(summary = "获取商品销量排行", description = "分页查询商品销量排行数据")
    public Result<IPage<ProductRankingDTO>> getProductRanking(
            @Parameter(description = "商家ID", required = true)
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId,
            @RequestBody java.util.Map<String, Object> params) {

        Integer current = params != null && params.get("current") != null ? 
                ((Number) params.get("current")).intValue() : 1;
        Integer size = params != null && params.get("size") != null ? 
                ((Number) params.get("size")).intValue() : 20;

        log.info("获取商品销量排行，商家ID: {}, 当前页: {}, 每页大小: {}", merchantId, current, size);

        IPage<ProductRankingDTO> result = analyticsService.getProductRanking(merchantId, current, size);
        return Result.success("查询成功", result);
    }

    /**
     * 获取热门商品
     */
    @PostMapping("/hot-products")
    @Operation(summary = "获取热门商品", description = "分页查询热门商品数据（增长率较高的商品）")
    public Result<IPage<HotProductDTO>> getHotProducts(
            @Parameter(description = "商家ID", required = true)
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId,
            @RequestBody java.util.Map<String, Object> params) {

        Integer current = params != null && params.get("current") != null ? 
                ((Number) params.get("current")).intValue() : 1;
        Integer size = params != null && params.get("size") != null ? 
                ((Number) params.get("size")).intValue() : 20;

        log.info("获取热门商品，商家ID: {}, 当前页: {}, 每页大小: {}", merchantId, current, size);

        IPage<HotProductDTO> result = analyticsService.getHotProducts(merchantId, current, size);
        return Result.success("查询成功", result);
    }

    /**
     * 获取滞销商品
     */
    @PostMapping("/slow-products")
    @Operation(summary = "获取滞销商品", description = "分页查询滞销商品数据（销量低且库存高的商品）")
    public Result<IPage<SlowProductDTO>> getSlowProducts(
            @Parameter(description = "商家ID", required = true)
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId,
            @RequestBody java.util.Map<String, Object> params) {

        Integer current = params != null && params.get("current") != null ? 
                ((Number) params.get("current")).intValue() : 1;
        Integer size = params != null && params.get("size") != null ? 
                ((Number) params.get("size")).intValue() : 20;

        log.info("获取滞销商品，商家ID: {}, 当前页: {}, 每页大小: {}", merchantId, current, size);

        IPage<SlowProductDTO> result = analyticsService.getSlowProducts(merchantId, current, size);
        return Result.success("查询成功", result);
    }

    /**
     * 导出核心数据
     */
    @PostMapping("/export/core-data")
    @Operation(summary = "导出核心数据", description = "导出核心数据为Excel或CSV")
    public Result<String> exportCoreData(
            @Parameter(description = "商家ID", required = true)
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId) {

        log.info("导出核心数据，商家ID: {}", merchantId);

        analyticsService.exportCoreData(merchantId);
        return Result.success("导出成功");
    }

    /**
     * 导出商品数据
     */
    @PostMapping("/export/product-data")
    @Operation(summary = "导出商品数据", description = "导出商品数据为Excel或CSV")
    public Result<String> exportProductData(
            @Parameter(description = "商家ID", required = true)
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId,
            @RequestBody java.util.Map<String, Object> params) {

        String tab = params != null && params.get("tab") != null ? 
                (String) params.get("tab") : "ranking";

        log.info("导出商品数据，商家ID: {}, 标签: {}", merchantId, tab);

        analyticsService.exportProductData(merchantId, tab);
        return Result.success("导出成功");
    }

    /**
     * 导出全部数据
     */
    @PostMapping("/export/all-data")
    @Operation(summary = "导出全部数据", description = "导出全部经营数据为Excel或CSV")
    public Result<String> exportAllData(
            @Parameter(description = "商家ID", required = true)
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId) {

        log.info("导出全部数据，商家ID: {}", merchantId);

        analyticsService.exportAllData(merchantId);
        return Result.success("导出成功");
    }
}

