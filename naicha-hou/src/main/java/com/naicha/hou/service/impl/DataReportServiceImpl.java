package com.naicha.hou.service.impl;

import com.naicha.hou.dto.DataReportDTO;
import com.naicha.hou.mapper.DataReportMapper;
import com.naicha.hou.service.DataReportService;
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
 * 数据报表服务实现类
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataReportServiceImpl implements DataReportService {

    private final DataReportMapper dataReportMapper;

    @Override
    public DataReportDTO generateReport(String reportType, String period, String startDate, String endDate) {
        log.info("生成数据报表，报表类型: {}, 周期: {}, 开始日期: {}, 结束日期: {}", reportType, period, startDate, endDate);

        // 解析日期并转换为LocalDateTime
        LocalDateTime startTime = parseDate(startDate, period, true);
        LocalDateTime endTime = parseDate(endDate, period, false);

        // 计算上期时间段（用于计算增长率）
        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(startTime, endTime);
        LocalDateTime previousStartTime = startTime.minusDays(daysDiff + 1);
        LocalDateTime previousEndTime = startTime.minusSeconds(1);

        DataReportDTO report = new DataReportDTO();

        // 设置生成时间和日期范围
        report.setGenerateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // 转换周期格式（前端使用daily/weekly/monthly/yearly，后端使用day/week/month/year）
        String periodForMapping = convertPeriod(period);
        report.setPeriod(period);
        report.setDateRange(String.format("%s 至 %s", startDate, endDate));

        // 获取报表摘要
        Map<String, Object> summaryMap = dataReportMapper.selectReportSummary(startTime, endTime);
        DataReportDTO.ReportSummary summary = new DataReportDTO.ReportSummary();
        if (summaryMap != null) {
            summary.setTotalUsers(((Number) summaryMap.get("totalUsers")).longValue());
            summary.setTotalOrders(((Number) summaryMap.get("totalOrders")).longValue());
            summary.setTotalAmount((BigDecimal) summaryMap.get("totalAmount"));
            summary.setActiveMerchants(((Number) summaryMap.get("activeMerchants")).longValue());
        }
        report.setSummary(summary);

        // 构建图表数据
        DataReportDTO.ChartData charts = new DataReportDTO.ChartData();
        
        // 销量趋势数据
        List<Map<String, Object>> salesTrendRaw = dataReportMapper.selectSalesTrendForReport(periodForMapping, startTime, endTime);
        DataReportDTO.TrendChartWithGrowth salesChart = buildTrendChartWithGrowth(salesTrendRaw);
        Long previousSales = dataReportMapper.selectPreviousPeriodSales(periodForMapping, previousStartTime, previousEndTime);
        Long currentSales = salesTrendRaw != null ? salesTrendRaw.stream()
                .mapToLong(row -> ((Number) row.get("count")).longValue())
                .sum() : 0L;
        salesChart.setGrowth(calculateGrowthRate(currentSales, previousSales));
        charts.setSales(salesChart);

        // 交易额趋势数据
        List<Map<String, Object>> transactionTrendRaw = dataReportMapper.selectTransactionTrendForReport(periodForMapping, startTime, endTime);
        DataReportDTO.TrendChartWithGrowth transactionChart = buildTransactionChartWithGrowth(transactionTrendRaw);
        BigDecimal previousTransaction = dataReportMapper.selectPreviousPeriodTransaction(periodForMapping, previousStartTime, previousEndTime);
        BigDecimal currentTransaction = transactionTrendRaw != null ? transactionTrendRaw.stream()
                .map(row -> (BigDecimal) row.get("amount"))
                .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;
        transactionChart.setGrowth(calculateGrowthRate(currentTransaction, previousTransaction));
        charts.setTransaction(transactionChart);

        // 订单状态分布数据
        List<Map<String, Object>> orderStatusRaw = dataReportMapper.selectOrderStatusForReport(startTime, endTime);
        DataReportDTO.OrderStatusDistribution orderStatus = buildOrderStatusDistribution(orderStatusRaw);
        charts.setOrderStatus(orderStatus);

        // 用户增长趋势数据
        List<Map<String, Object>> userGrowthTrendRaw = dataReportMapper.selectUserGrowthTrendForReport(periodForMapping, startTime, endTime);
        DataReportDTO.UserGrowthChart userGrowthChart = buildUserGrowthChart(userGrowthTrendRaw);
        Long previousNewUsers = dataReportMapper.selectPreviousPeriodNewUsers(periodForMapping, previousStartTime, previousEndTime);
        Long currentNewUsers = userGrowthTrendRaw != null ? userGrowthTrendRaw.stream()
                .mapToLong(row -> ((Number) row.get("count")).longValue())
                .sum() : 0L;
        userGrowthChart.setGrowth(calculateGrowthRate(currentNewUsers, previousNewUsers));
        
        Long activeUsers = dataReportMapper.selectActiveUsers(startTime, endTime);
        userGrowthChart.setNewUsers(currentNewUsers);
        userGrowthChart.setActiveUsers(activeUsers != null ? activeUsers : 0L);
        charts.setUserGrowth(userGrowthChart);

        report.setCharts(charts);

        // 获取详细表格数据
        List<Map<String, Object>> tableDataRaw = dataReportMapper.selectReportTableData(periodForMapping, startTime, endTime);
        List<DataReportDTO.ReportTableItem> tableData = buildTableData(tableDataRaw);
        report.setTableData(tableData);

        return report;
    }

    /**
     * 转换周期格式
     */
    private String convertPeriod(String period) {
        switch (period) {
            case "daily":
                return "day";
            case "weekly":
                return "week";
            case "monthly":
                return "month";
            case "yearly":
                return "year";
            default:
                return "day";
        }
    }

    /**
     * 解析日期字符串为LocalDateTime
     */
    private LocalDateTime parseDate(String dateStr, String period, boolean isStart) {
        LocalDateTime dateTime;
        try {
            if ("monthly".equals(period)) {
                // 月份格式：yyyy-MM
                dateTime = LocalDateTime.parse(dateStr + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                if (!isStart) {
                    dateTime = dateTime.plusMonths(1).minusDays(1).withHour(23).withMinute(59).withSecond(59);
                }
            } else if ("yearly".equals(period)) {
                // 年份格式：yyyy
                dateTime = LocalDateTime.parse(dateStr + "-01-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                if (!isStart) {
                    dateTime = dateTime.plusYears(1).minusDays(1).withHour(23).withMinute(59).withSecond(59);
                }
            } else {
                // 日期格式：yyyy-MM-dd
                dateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
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
     * 构建销量趋势图表
     */
    private DataReportDTO.TrendChartWithGrowth buildTrendChartWithGrowth(List<Map<String, Object>> rawData) {
        DataReportDTO.TrendChartWithGrowth chart = new DataReportDTO.TrendChartWithGrowth();
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
     * 构建交易额趋势图表
     */
    private DataReportDTO.TrendChartWithGrowth buildTransactionChartWithGrowth(List<Map<String, Object>> rawData) {
        DataReportDTO.TrendChartWithGrowth chart = new DataReportDTO.TrendChartWithGrowth();
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
     * 构建订单状态分布
     */
    private DataReportDTO.OrderStatusDistribution buildOrderStatusDistribution(List<Map<String, Object>> rawData) {
        DataReportDTO.OrderStatusDistribution distribution = new DataReportDTO.OrderStatusDistribution();
        List<DataReportDTO.OrderStatusItem> items = new ArrayList<>();
        BigDecimal totalCount = BigDecimal.ZERO;
        BigDecimal completedCount = BigDecimal.ZERO;
        BigDecimal canceledCount = BigDecimal.ZERO;
        BigDecimal pendingCount = BigDecimal.ZERO;

        if (rawData != null && !rawData.isEmpty()) {
            for (Map<String, Object> row : rawData) {
                DataReportDTO.OrderStatusItem item = new DataReportDTO.OrderStatusItem();
                item.setName((String) row.get("name"));
                
                Object percentageObj = row.get("percentage");
                BigDecimal percentage = percentageObj instanceof Number ?
                        BigDecimal.valueOf(((Number) percentageObj).doubleValue()) :
                        (BigDecimal) percentageObj;
                item.setValue(percentage);
                item.setColor((String) row.get("color"));
                
                items.add(item);
                
                Long count = ((Number) row.get("count")).longValue();
                totalCount = totalCount.add(BigDecimal.valueOf(count));
                
                String name = item.getName();
                if ("已完成".equals(name)) {
                    completedCount = completedCount.add(BigDecimal.valueOf(count));
                } else if ("已取消".equals(name)) {
                    canceledCount = canceledCount.add(BigDecimal.valueOf(count));
                } else if (!"已完成".equals(name) && !"已取消".equals(name) && !"已退款".equals(name)) {
                    pendingCount = pendingCount.add(BigDecimal.valueOf(count));
                }
            }
        }

        distribution.setData(items);
        
        // 计算完成率、取消率、待处理率
        if (totalCount.compareTo(BigDecimal.ZERO) > 0) {
            distribution.setCompletionRate(completedCount.multiply(BigDecimal.valueOf(100))
                    .divide(totalCount, 2, RoundingMode.HALF_UP));
            distribution.setCancelRate(canceledCount.multiply(BigDecimal.valueOf(100))
                    .divide(totalCount, 2, RoundingMode.HALF_UP));
            distribution.setPendingRate(pendingCount.multiply(BigDecimal.valueOf(100))
                    .divide(totalCount, 2, RoundingMode.HALF_UP));
        } else {
            distribution.setCompletionRate(BigDecimal.ZERO);
            distribution.setCancelRate(BigDecimal.ZERO);
            distribution.setPendingRate(BigDecimal.ZERO);
        }

        return distribution;
    }

    /**
     * 构建用户增长图表
     */
    private DataReportDTO.UserGrowthChart buildUserGrowthChart(List<Map<String, Object>> rawData) {
        DataReportDTO.UserGrowthChart chart = new DataReportDTO.UserGrowthChart();
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
     * 构建表格数据
     */
    private List<DataReportDTO.ReportTableItem> buildTableData(List<Map<String, Object>> rawData) {
        List<DataReportDTO.ReportTableItem> items = new ArrayList<>();
        if (rawData != null && !rawData.isEmpty()) {
            for (Map<String, Object> row : rawData) {
                DataReportDTO.ReportTableItem item = new DataReportDTO.ReportTableItem();
                item.setDate((String) row.get("date"));
                item.setNewUsers(((Number) row.get("newUsers")).longValue());
                item.setNewOrders(((Number) row.get("newOrders")).longValue());
                item.setSales(((Number) row.get("sales")).longValue());
                
                Object transactionObj = row.get("transaction");
                if (transactionObj instanceof BigDecimal) {
                    item.setTransaction((BigDecimal) transactionObj);
                } else if (transactionObj instanceof Number) {
                    item.setTransaction(BigDecimal.valueOf(((Number) transactionObj).doubleValue()));
                }
                
                Object avgOrderValueObj = row.get("avgOrderValue");
                if (avgOrderValueObj instanceof BigDecimal) {
                    item.setAvgOrderValue((BigDecimal) avgOrderValueObj);
                } else if (avgOrderValueObj instanceof Number) {
                    item.setAvgOrderValue(BigDecimal.valueOf(((Number) avgOrderValueObj).doubleValue()));
                }
                
                Object completionRateObj = row.get("completionRate");
                if (completionRateObj instanceof BigDecimal) {
                    item.setCompletionRate((BigDecimal) completionRateObj);
                } else if (completionRateObj instanceof Number) {
                    item.setCompletionRate(BigDecimal.valueOf(((Number) completionRateObj).doubleValue()));
                }
                
                items.add(item);
            }
        }
        return items;
    }
}

