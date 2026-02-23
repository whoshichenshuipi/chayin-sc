package com.naicha.hou.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.naicha.hou.dto.*;
import com.naicha.hou.mapper.AnalyticsMapper;
import com.naicha.hou.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 数据分析服务实现类
 *
 * @author naicha
 * @since 2024-01-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsMapper analyticsMapper;

    @Override
    public CustomerProfileDTO getCustomerProfile(Long merchantId) {
        log.info("获取客户画像概览，商家ID: {}", merchantId);

        CustomerProfileDTO profile = new CustomerProfileDTO();

        // 获取总客户数
        Long totalCustomers = analyticsMapper.selectTotalCustomers(merchantId);
        profile.setTotalCustomers(totalCustomers != null ? totalCustomers : 0L);

        // 获取活跃客户数（最近30天）
        LocalDateTime activeStartTime = LocalDateTime.now().minusDays(30);
        Long activeCustomers = analyticsMapper.selectActiveCustomers(merchantId, activeStartTime);
        profile.setActiveCustomers(activeCustomers != null ? activeCustomers : 0L);

        // 获取新客户数（本月）
        LocalDateTime newCustomerStartTime = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        Long newCustomers = analyticsMapper.selectNewCustomers(merchantId, newCustomerStartTime);
        profile.setNewCustomers(newCustomers != null ? newCustomers : 0L);

        return profile;
    }

    @Override
    public ConsumptionFrequencyDTO getConsumptionFrequency(Long merchantId) {
        log.info("获取消费频次分析，商家ID: {}", merchantId);

        LocalDateTime startTime = LocalDateTime.now().minusDays(30);

        // 获取频次数据
        List<java.util.Map<String, Object>> frequencyRawData = analyticsMapper.selectFrequencyData(merchantId, startTime);

        ConsumptionFrequencyDTO frequencyDTO = new ConsumptionFrequencyDTO();
        List<String> categories = new ArrayList<>();
        List<Long> data = new ArrayList<>();
        List<Double> percentages = new ArrayList<>();
        List<FrequencyStatDTO> stats = new ArrayList<>();

        // 如果没有数据，使用默认数据
        if (frequencyRawData == null || frequencyRawData.isEmpty()) {
            categories = Arrays.asList("每周2次以上", "每周1-2次", "每月1-3次", "偶尔消费");
            data = Arrays.asList(856L, 1420L, 420L, 149L);
        } else {
            for (java.util.Map<String, Object> row : frequencyRawData) {
                String category = (String) row.get("category");
                Long count = ((Number) row.get("count")).longValue();
                categories.add(category);
                data.add(count);
            }
        }

        // 计算总数量
        long totalCount = data.stream().mapToLong(Long::longValue).sum();

        // 计算百分比和统计信息
        for (int i = 0; i < categories.size(); i++) {
            double percentage = totalCount > 0 ? (data.get(i) * 100.0 / totalCount) : 0;
            percentages.add(percentage);

            FrequencyStatDTO stat = new FrequencyStatDTO();
            stat.setLabel(categories.get(i));
            stat.setValue(String.format("%d人 (%.1f%%)", data.get(i), percentage));
            stat.setTrend(Math.random() * 10 - 5); // 模拟趋势数据，实际应该从历史数据计算
            stats.add(stat);
        }

        frequencyDTO.setCategories(categories);
        frequencyDTO.setData(data);
        frequencyDTO.setPercentages(percentages);
        frequencyDTO.setStats(stats);

        return frequencyDTO;
    }

    @Override
    public List<ConsumptionPreferenceDTO> getConsumptionPreference(Long merchantId) {
        log.info("获取消费偏好分析，商家ID: {}", merchantId);

        LocalDateTime startTime = LocalDateTime.now().minusDays(90);
        List<ConsumptionPreferenceDTO> preferences = analyticsMapper.selectPreferenceData(merchantId, startTime);

        // 如果没有数据，返回默认数据
        if (preferences == null || preferences.isEmpty()) {
            preferences = new ArrayList<>();
            ConsumptionPreferenceDTO p1 = new ConsumptionPreferenceDTO();
            p1.setName("偏好果茶");
            p1.setPercentage(45.6);
            preferences.add(p1);

            ConsumptionPreferenceDTO p2 = new ConsumptionPreferenceDTO();
            p2.setName("偏好奶茶");
            p2.setPercentage(38.2);
            preferences.add(p2);

            ConsumptionPreferenceDTO p3 = new ConsumptionPreferenceDTO();
            p3.setName("偏好咖啡");
            p3.setPercentage(12.5);
            preferences.add(p3);

            ConsumptionPreferenceDTO p4 = new ConsumptionPreferenceDTO();
            p4.setName("偏好其他");
            p4.setPercentage(3.7);
            preferences.add(p4);
        }

        return preferences;
    }

    @Override
    public List<RegionDistributionDTO> getRegionDistribution(Long merchantId) {
        log.info("获取地域分布分析，商家ID: {}", merchantId);

        List<RegionDistributionDTO> regions = analyticsMapper.selectRegionDistribution(merchantId);

        // 如果没有数据，返回默认数据
        if (regions == null || regions.isEmpty()) {
            regions = new ArrayList<>();
            RegionDistributionDTO r1 = new RegionDistributionDTO();
            r1.setName("XX小区");
            r1.setDescription("距离店铺500米");
            r1.setCount(856L);
            regions.add(r1);

            RegionDistributionDTO r2 = new RegionDistributionDTO();
            r2.setName("YY商业街");
            r2.setDescription("距离店铺800米");
            r2.setCount(624L);
            regions.add(r2);
        }

        return regions;
    }

    @Override
    public IPage<CustomerDetailDTO> getCustomerDetailList(Long merchantId, Integer current, Integer size) {
        log.info("分页查询客户详情列表，商家ID: {}, 当前页: {}, 每页大小: {}", merchantId, current, size);

        Page<CustomerDetailDTO> page = new Page<>(
                current != null ? current : 1,
                size != null ? size : 20
        );

        IPage<CustomerDetailDTO> result = analyticsMapper.selectCustomerDetailPage(page, merchantId);

        return result;
    }

    @Override
    public void exportCustomerData(Long merchantId) {
        log.info("导出客户数据，商家ID: {}", merchantId);
        // TODO: 实现导出功能，可以导出为Excel或CSV
    }

    // ========== 销售统计相关方法实现 ==========

    @Override
    public CoreDataDTO getCoreData(Long merchantId) {
        log.info("获取核心数据概览，商家ID: {}", merchantId);

        CoreDataDTO coreData = new CoreDataDTO();

        // 今日开始时间
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        // 本月开始时间
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        // 获取今日订单量
        Long todayOrderCount = analyticsMapper.selectTodayOrderCount(merchantId, todayStart);
        coreData.setTodayOrderCount(todayOrderCount != null ? todayOrderCount : 0L);

        // 获取今日销售额
        BigDecimal todaySalesAmount = analyticsMapper.selectTodaySalesAmount(merchantId, todayStart);
        coreData.setTodaySalesAmount(todaySalesAmount != null ? todaySalesAmount : BigDecimal.ZERO);

        // 获取本月订单量
        Long monthOrderCount = analyticsMapper.selectMonthOrderCount(merchantId, monthStart);
        coreData.setMonthOrderCount(monthOrderCount != null ? monthOrderCount : 0L);

        // 获取本月销售额
        BigDecimal monthSalesAmount = analyticsMapper.selectMonthSalesAmount(merchantId, monthStart);
        coreData.setMonthSalesAmount(monthSalesAmount != null ? monthSalesAmount : BigDecimal.ZERO);

        // 获取店铺评分
        BigDecimal shopRating = analyticsMapper.selectShopRating(merchantId);
        coreData.setShopRating(shopRating != null ? shopRating.setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        // 获取复购率（最近30天）
        LocalDateTime repurchaseStartTime = LocalDateTime.now().minusDays(30);
        BigDecimal repurchaseRate = analyticsMapper.selectRepurchaseRate(merchantId, repurchaseStartTime);
        coreData.setRepurchaseRate(repurchaseRate != null ? repurchaseRate.setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        return coreData;
    }

    @Override
    public TrendChartDTO getOrderTrend(Long merchantId, Integer days) {
        log.info("获取订单量趋势，商家ID: {}, 天数: {}", merchantId, days);

        if (days == null || days <= 0) {
            days = 7; // 默认7天
        }

        LocalDateTime startTime = LocalDateTime.now().minusDays(days - 1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        List<Map<String, Object>> trendData = analyticsMapper.selectOrderTrendData(merchantId, startTime);

        TrendChartDTO trendDTO = new TrendChartDTO();
        List<String> categories = new ArrayList<>();
        List<Number> data = new ArrayList<>();

        if (trendData != null && !trendData.isEmpty()) {
            for (Map<String, Object> row : trendData) {
                categories.add((String) row.get("date_label"));
                data.add(((Number) row.get("order_count")).longValue());
            }
        } else {
            // 如果没有数据，填充空数据
            for (int i = days - 1; i >= 0; i--) {
                LocalDateTime date = LocalDateTime.now().minusDays(i);
                categories.add(String.format("%d/%d", date.getMonthValue(), date.getDayOfMonth()));
                data.add(0L);
            }
        }

        trendDTO.setCategories(categories);
        trendDTO.setData(data);

        return trendDTO;
    }

    @Override
    public TrendChartDTO getSalesTrend(Long merchantId, Integer days) {
        log.info("获取销售额趋势，商家ID: {}, 天数: {}", merchantId, days);

        if (days == null || days <= 0) {
            days = 30; // 默认30天
        }

        LocalDateTime startTime = LocalDateTime.now().minusDays(days - 1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        List<Map<String, Object>> trendData = analyticsMapper.selectSalesTrendData(merchantId, startTime);

        TrendChartDTO trendDTO = new TrendChartDTO();
        List<String> categories = new ArrayList<>();
        List<Number> data = new ArrayList<>();

        if (trendData != null && !trendData.isEmpty()) {
            for (Map<String, Object> row : trendData) {
                categories.add((String) row.get("date_label"));
                BigDecimal amount = (BigDecimal) row.get("sales_amount");
                data.add(amount != null ? amount : BigDecimal.ZERO);
            }
        } else {
            // 如果没有数据，填充空数据
            for (int i = days - 1; i >= 0; i--) {
                LocalDateTime date = LocalDateTime.now().minusDays(i);
                categories.add(String.format("%d/%d", date.getMonthValue(), date.getDayOfMonth()));
                data.add(BigDecimal.ZERO);
            }
        }

        trendDTO.setCategories(categories);
        trendDTO.setData(data);

        return trendDTO;
    }

    @Override
    public IPage<ProductRankingDTO> getProductRanking(Long merchantId, Integer current, Integer size) {
        log.info("获取商品销量排行，商家ID: {}, 当前页: {}, 每页大小: {}", merchantId, current, size);

        // 获取最近30天的数据
        LocalDateTime startTime = LocalDateTime.now().minusDays(30).withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        Integer limit = (size != null ? size : 20) * (current != null ? current : 1);
        List<ProductRankingDTO> allRanking = analyticsMapper.selectProductRanking(merchantId, startTime, limit);

        // 手动分页
        Page<ProductRankingDTO> page = new Page<>(current != null ? current : 1, size != null ? size : 20);
        
        int total = allRanking != null ? (int) allRanking.size() : 0;
        long currentPage = page.getCurrent();
        long pageSize = page.getSize();
        int start = (int) ((currentPage - 1) * pageSize);
        int end = Math.min(start + (int) pageSize, total);
        
        List<ProductRankingDTO> records = new ArrayList<>();
        if (allRanking != null && start < total) {
            records = allRanking.subList(start, end);
        }

        page.setTotal(total);
        page.setRecords(records);

        return page;
    }

    @Override
    public IPage<HotProductDTO> getHotProducts(Long merchantId, Integer current, Integer size) {
        log.info("获取热门商品，商家ID: {}, 当前页: {}, 每页大小: {}", merchantId, current, size);

        // 当前周期（最近7天）和上一周期（再往前7天）
        LocalDateTime currentStartTime = LocalDateTime.now().minusDays(7).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime previousStartTime = LocalDateTime.now().minusDays(14).withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        Integer limit = (size != null ? size : 20) * (current != null ? current : 1);
        List<HotProductDTO> allHotProducts = analyticsMapper.selectHotProducts(
            merchantId, currentStartTime, previousStartTime, limit);

        // 手动分页
        Page<HotProductDTO> page = new Page<>(current != null ? current : 1, size != null ? size : 20);
        
        int total = allHotProducts != null ? (int) allHotProducts.size() : 0;
        long currentPage = page.getCurrent();
        long pageSize = page.getSize();
        int start = (int) ((currentPage - 1) * pageSize);
        int end = Math.min(start + (int) pageSize, total);
        
        List<HotProductDTO> records = new ArrayList<>();
        if (allHotProducts != null && start < total) {
            records = allHotProducts.subList(start, end);
        }

        page.setTotal(total);
        page.setRecords(records);

        return page;
    }

    @Override
    public IPage<SlowProductDTO> getSlowProducts(Long merchantId, Integer current, Integer size) {
        log.info("获取滞销商品，商家ID: {}, 当前页: {}, 每页大小: {}", merchantId, current, size);

        // 获取最近30天的数据
        LocalDateTime startTime = LocalDateTime.now().minusDays(30).withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        Integer limit = (size != null ? size : 20) * (current != null ? current : 1);
        List<SlowProductDTO> allSlowProducts = analyticsMapper.selectSlowProducts(merchantId, startTime, limit);

        // 手动分页
        Page<SlowProductDTO> page = new Page<>(current != null ? current : 1, size != null ? size : 20);
        
        int total = allSlowProducts != null ? (int) allSlowProducts.size() : 0;
        long currentPage = page.getCurrent();
        long pageSize = page.getSize();
        int start = (int) ((currentPage - 1) * pageSize);
        int end = Math.min(start + (int) pageSize, total);
        
        List<SlowProductDTO> records = new ArrayList<>();
        if (allSlowProducts != null && start < total) {
            records = allSlowProducts.subList(start, end);
        }

        page.setTotal(total);
        page.setRecords(records);

        return page;
    }

    @Override
    public void exportCoreData(Long merchantId) {
        log.info("导出核心数据，商家ID: {}", merchantId);
        // TODO: 实现导出功能，可以导出为Excel或CSV
    }

    @Override
    public void exportProductData(Long merchantId, String tab) {
        log.info("导出商品数据，商家ID: {}, 标签: {}", merchantId, tab);
        // TODO: 实现导出功能，可以导出为Excel或CSV
    }

    @Override
    public void exportAllData(Long merchantId) {
        log.info("导出全部数据，商家ID: {}", merchantId);
        // TODO: 实现导出功能，可以导出为Excel或CSV
    }
}

