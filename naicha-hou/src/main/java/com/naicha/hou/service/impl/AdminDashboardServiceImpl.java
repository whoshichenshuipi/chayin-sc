package com.naicha.hou.service.impl;

import com.naicha.hou.dto.AdminDashboardDTO;
import com.naicha.hou.dto.TrendChartDTO;
import com.naicha.hou.mapper.AdminDashboardMapper;
import com.naicha.hou.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员仪表盘服务实现类
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final AdminDashboardMapper adminDashboardMapper;

    @Override
    public AdminDashboardDTO getDashboardData(Integer days) {
        log.info("获取管理员仪表盘数据，天数: {}", days);

        if (days == null || days <= 0) {
            days = 30; // 默认30天
        }

        AdminDashboardDTO dashboard = new AdminDashboardDTO();

        // 获取统计卡片数据
        AdminDashboardDTO.StatsData stats = new AdminDashboardDTO.StatsData();
        Long totalMerchants = adminDashboardMapper.selectTotalMerchants();
        Long totalUsers = adminDashboardMapper.selectTotalUsers();
        Long totalOrders = adminDashboardMapper.selectTotalOrders();
        BigDecimal platformRevenue = adminDashboardMapper.selectPlatformRevenue();

        stats.setTotalMerchants(totalMerchants != null ? totalMerchants : 0L);
        stats.setTotalUsers(totalUsers != null ? totalUsers : 0L);
        stats.setTotalOrders(totalOrders != null ? totalOrders : 0L);
        stats.setPlatformRevenue(platformRevenue != null ? platformRevenue : BigDecimal.ZERO);
        dashboard.setStats(stats);

        // 获取用户增长趋势数据
        LocalDateTime userStartTime = LocalDateTime.now().minusDays(days);
        List<Map<String, Object>> userTrendRaw = adminDashboardMapper.selectUserGrowthTrend(days, userStartTime);
        TrendChartDTO userGrowthTrend = new TrendChartDTO();
        if (userTrendRaw != null && !userTrendRaw.isEmpty()) {
            List<String> categories = userTrendRaw.stream()
                    .map(row -> (String) row.get("date"))
                    .collect(Collectors.toList());
            List<Number> data = userTrendRaw.stream()
                    .map(row -> ((Number) row.get("count")).longValue())
                    .collect(Collectors.toList());
            userGrowthTrend.setCategories(categories);
            userGrowthTrend.setData(data);
        } else {
            // 如果没有数据，返回空列表
            userGrowthTrend.setCategories(new ArrayList<>());
            userGrowthTrend.setData(new ArrayList<>());
        }
        dashboard.setUserGrowthTrend(userGrowthTrend);

        // 获取订单统计趋势数据
        LocalDateTime orderStartTime = LocalDateTime.now().minusDays(days);
        List<Map<String, Object>> orderTrendRaw = adminDashboardMapper.selectOrderStatsTrend(days, orderStartTime);
        AdminDashboardDTO.OrderStatsTrend orderStatsTrend = new AdminDashboardDTO.OrderStatsTrend();
        if (orderTrendRaw != null && !orderTrendRaw.isEmpty()) {
            List<String> categories = orderTrendRaw.stream()
                    .map(row -> (String) row.get("date"))
                    .collect(Collectors.toList());
            List<Long> orderCounts = orderTrendRaw.stream()
                    .map(row -> ((Number) row.get("orderCount")).longValue())
                    .collect(Collectors.toList());
            List<BigDecimal> orderAmounts = orderTrendRaw.stream()
                    .map(row -> (BigDecimal) row.get("orderAmount"))
                    .collect(Collectors.toList());
            orderStatsTrend.setCategories(categories);
            orderStatsTrend.setOrderCounts(orderCounts);
            orderStatsTrend.setOrderAmounts(orderAmounts);
        } else {
            // 如果没有数据，返回空列表
            orderStatsTrend.setCategories(new ArrayList<>());
            orderStatsTrend.setOrderCounts(new ArrayList<>());
            orderStatsTrend.setOrderAmounts(new ArrayList<>());
        }
        dashboard.setOrderStatsTrend(orderStatsTrend);

        // 获取最近活动列表
        List<Map<String, Object>> activitiesRaw = adminDashboardMapper.selectRecentActivities(10);
        List<AdminDashboardDTO.RecentActivityDTO> recentActivities = new ArrayList<>();
        if (activitiesRaw != null && !activitiesRaw.isEmpty()) {
            for (Map<String, Object> activity : activitiesRaw) {
                AdminDashboardDTO.RecentActivityDTO activityDTO = new AdminDashboardDTO.RecentActivityDTO();
                activityDTO.setType((String) activity.get("type"));
                activityDTO.setTitle((String) activity.get("title"));
                activityDTO.setDescription((String) activity.get("description"));
                
                // 格式化时间戳
                if (activity.get("activity_time") != null) {
                    LocalDateTime activityTime = (LocalDateTime) activity.get("activity_time");
                    activityDTO.setTimestamp(activityTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                }
                
                recentActivities.add(activityDTO);
            }
        }
        dashboard.setRecentActivities(recentActivities);

        return dashboard;
    }
}

