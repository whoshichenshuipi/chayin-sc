package com.naicha.hou.service.impl;

import com.naicha.hou.dto.DataOverviewDTO;
import com.naicha.hou.dto.TrendChartDTO;
import com.naicha.hou.mapper.DataOverviewMapper;
import com.naicha.hou.service.DataOverviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据概览服务实现类
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataOverviewServiceImpl implements DataOverviewService {

    private final DataOverviewMapper dataOverviewMapper;

    @Override
    public DataOverviewDTO getDataOverview(String period, String startDate, String endDate) {
        log.info("获取数据概览，周期: {}, 开始日期: {}, 结束日期: {}", period, startDate, endDate);

        // 解析日期并转换为LocalDateTime
        LocalDateTime startTime = parseDate(startDate, period, true);
        LocalDateTime endTime = parseDate(endDate, period, false);

        // 计算上期时间段（用于计算增长率）
        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(startTime, endTime);
        LocalDateTime previousStartTime = startTime.minusDays(daysDiff + 1);
        LocalDateTime previousEndTime = startTime.minusSeconds(1);

        DataOverviewDTO overview = new DataOverviewDTO();

        // 设置用户统计数据
        DataOverviewDTO.UserStats userStats = new DataOverviewDTO.UserStats();
        userStats.setTotal(dataOverviewMapper.selectTotalUsers());
        Long newUsers = dataOverviewMapper.selectNewUsers(startTime, endTime);
        userStats.setNewUsers(newUsers != null ? newUsers : 0L);
        
        Long previousNewUsers = dataOverviewMapper.selectPreviousPeriodNewUsers(previousStartTime, previousEndTime);
        userStats.setGrowth(calculateGrowthRate(newUsers, previousNewUsers));
        overview.setUserStats(userStats);

        // 设置商家统计数据
        DataOverviewDTO.MerchantStats merchantStats = new DataOverviewDTO.MerchantStats();
        merchantStats.setTotal(dataOverviewMapper.selectTotalMerchants());
        Long newMerchants = dataOverviewMapper.selectNewMerchants(startTime, endTime);
        merchantStats.setNewMerchants(newMerchants != null ? newMerchants : 0L);
        
        Long activeMerchants = dataOverviewMapper.selectActiveMerchants(startTime, endTime);
        merchantStats.setActiveMerchants(activeMerchants != null ? activeMerchants : 0L);
        
        Long previousNewMerchants = dataOverviewMapper.selectPreviousPeriodNewMerchants(previousStartTime, previousEndTime);
        merchantStats.setGrowth(calculateGrowthRate(newMerchants, previousNewMerchants));
        overview.setMerchantStats(merchantStats);

        // 设置订单统计数据
        DataOverviewDTO.OrderStats orderStats = new DataOverviewDTO.OrderStats();
        orderStats.setTotal(dataOverviewMapper.selectTotalOrders());
        Long newOrders = dataOverviewMapper.selectNewOrders(startTime, endTime);
        orderStats.setNewOrders(newOrders != null ? newOrders : 0L);
        
        Long completedOrders = dataOverviewMapper.selectCompletedOrders(startTime, endTime);
        orderStats.setCompletedOrders(completedOrders != null ? completedOrders : 0L);
        
        Long previousNewOrders = dataOverviewMapper.selectPreviousPeriodNewOrders(previousStartTime, previousEndTime);
        orderStats.setGrowth(calculateGrowthRate(newOrders, previousNewOrders));
        overview.setOrderStats(orderStats);

        // 设置交易统计数据
        DataOverviewDTO.TransactionStats transactionStats = new DataOverviewDTO.TransactionStats();
        transactionStats.setTotal(dataOverviewMapper.selectTotalTransactionAmount());
        
        BigDecimal currentAmount = dataOverviewMapper.selectCurrentPeriodTransactionAmount(startTime, endTime);
        transactionStats.setCurrent(currentAmount != null ? currentAmount : BigDecimal.ZERO);
        
        BigDecimal avgOrderAmount = dataOverviewMapper.selectAvgOrderAmount(startTime, endTime);
        transactionStats.setAvgOrder(avgOrderAmount != null ? avgOrderAmount : BigDecimal.ZERO);
        
        BigDecimal previousAmount = dataOverviewMapper.selectPreviousPeriodTransactionAmount(previousStartTime, previousEndTime);
        transactionStats.setGrowth(calculateGrowthRate(currentAmount, previousAmount));
        overview.setTransactionStats(transactionStats);

        // 设置销量趋势数据
        List<Map<String, Object>> salesTrendRaw = dataOverviewMapper.selectSalesTrend(period, startTime, endTime);
        TrendChartDTO salesTrend = buildTrendChart(salesTrendRaw);
        overview.setSalesTrend(salesTrend);

        // 设置交易额趋势数据
        List<Map<String, Object>> transactionTrendRaw = dataOverviewMapper.selectTransactionTrend(period, startTime, endTime);
        TrendChartDTO transactionTrend = buildTransactionTrendChart(transactionTrendRaw);
        overview.setTransactionTrend(transactionTrend);

        // 设置订单状态占比数据
        List<Map<String, Object>> orderStatusRaw = dataOverviewMapper.selectOrderStatusDistribution(startTime, endTime);
        List<DataOverviewDTO.OrderStatusItem> orderStatusDistribution = buildOrderStatusDistribution(orderStatusRaw);
        overview.setOrderStatusDistribution(orderStatusDistribution);

        // 设置用户增长趋势数据
        List<Map<String, Object>> userGrowthTrendRaw = dataOverviewMapper.selectUserGrowthTrend(period, startTime, endTime);
        TrendChartDTO userGrowthTrend = buildTrendChart(userGrowthTrendRaw);
        overview.setUserGrowthTrend(userGrowthTrend);

        return overview;
    }

    /**
     * 解析日期字符串为LocalDateTime
     */
    private LocalDateTime parseDate(String dateStr, String period, boolean isStart) {
        DateTimeFormatter formatter;
        switch (period) {
            case "day":
            case "week":
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                break;
            case "month":
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                break;
            case "year":
                formatter = DateTimeFormatter.ofPattern("yyyy");
                break;
            default:
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        }

        LocalDateTime dateTime;
        try {
            if ("month".equals(period)) {
                // 月份格式：yyyy-MM
                dateTime = LocalDateTime.parse(dateStr + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                if (!isStart) {
                    // 结束日期设为月末
                    dateTime = dateTime.plusMonths(1).minusDays(1).withHour(23).withMinute(59).withSecond(59);
                }
            } else if ("year".equals(period)) {
                // 年份格式：yyyy
                dateTime = LocalDateTime.parse(dateStr + "-01-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                if (!isStart) {
                    // 结束日期设为年末
                    dateTime = dateTime.plusYears(1).minusDays(1).withHour(23).withMinute(59).withSecond(59);
                }
            } else {
                // 日期格式：yyyy-MM-dd
                dateTime = LocalDateTime.parse(dateStr, formatter);
                if (!isStart) {
                    dateTime = dateTime.withHour(23).withMinute(59).withSecond(59);
                } else {
                    dateTime = dateTime.withHour(0).withMinute(0).withSecond(0);
                }
            }
        } catch (Exception e) {
            log.error("解析日期失败: {}", dateStr, e);
            dateTime = isStart ? LocalDateTime.now().minusDays(7) : LocalDateTime.now();
        }
        return dateTime;
    }

    /**
     * 计算增长率
     */
    private BigDecimal calculateGrowthRate(Number current, Number previous) {
        if (previous == null || previous.doubleValue() == 0) {
            if (current != null && current.doubleValue() > 0) {
                return BigDecimal.valueOf(100);
            }
            return BigDecimal.ZERO;
        }
        
        double currentValue = current != null ? current.doubleValue() : 0;
        double previousValue = previous.doubleValue();
        double growth = ((currentValue - previousValue) / previousValue) * 100;
        
        return BigDecimal.valueOf(growth).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 构建趋势图表数据
     */
    private TrendChartDTO buildTrendChart(List<Map<String, Object>> rawData) {
        TrendChartDTO chart = new TrendChartDTO();
        if (rawData != null && !rawData.isEmpty()) {
            List<String> categories = rawData.stream()
                    .map(row -> (String) row.get("date"))
                    .collect(Collectors.toList());
            List<Number> data = rawData.stream()
                    .map(row -> ((Number) row.get("count")).longValue())
                    .collect(Collectors.toList());
            chart.setCategories(categories);
            chart.setData(data);
        } else {
            chart.setCategories(new ArrayList<>());
            chart.setData(new ArrayList<>());
        }
        return chart;
    }

    /**
     * 构建交易额趋势图表数据
     */
    private TrendChartDTO buildTransactionTrendChart(List<Map<String, Object>> rawData) {
        TrendChartDTO chart = new TrendChartDTO();
        if (rawData != null && !rawData.isEmpty()) {
            List<String> categories = rawData.stream()
                    .map(row -> (String) row.get("date"))
                    .collect(Collectors.toList());
            List<Number> data = rawData.stream()
                    .map(row -> ((BigDecimal) row.get("amount")).doubleValue())
                    .collect(Collectors.toList());
            chart.setCategories(categories);
            chart.setData(data);
        } else {
            chart.setCategories(new ArrayList<>());
            chart.setData(new ArrayList<>());
        }
        return chart;
    }

    /**
     * 构建订单状态占比数据
     */
    private List<DataOverviewDTO.OrderStatusItem> buildOrderStatusDistribution(List<Map<String, Object>> rawData) {
        List<DataOverviewDTO.OrderStatusItem> items = new ArrayList<>();
        if (rawData != null && !rawData.isEmpty()) {
            for (Map<String, Object> row : rawData) {
                DataOverviewDTO.OrderStatusItem item = new DataOverviewDTO.OrderStatusItem();
                item.setName((String) row.get("name"));
                item.setCount(((Number) row.get("count")).longValue());
                
                Object percentageObj = row.get("percentage");
                if (percentageObj instanceof Number) {
                    item.setPercentage(BigDecimal.valueOf(((Number) percentageObj).doubleValue()));
                } else if (percentageObj instanceof BigDecimal) {
                    item.setPercentage((BigDecimal) percentageObj);
                } else {
                    item.setPercentage(BigDecimal.ZERO);
                }
                
                items.add(item);
            }
        }
        return items;
    }
}

