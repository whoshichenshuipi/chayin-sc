package com.naicha.hou.service;

import com.naicha.hou.dto.DataOverviewDTO;

/**
 * 数据概览服务接口
 *
 * @author naicha
 * @since 2024-01-01
 */
public interface DataOverviewService {

    /**
     * 获取数据概览
     *
     * @param period 周期类型：day, week, month, year
     * @param startDate 开始日期（格式：YYYY-MM-DD 或 YYYY-MM 或 YYYY）
     * @param endDate 结束日期（格式：YYYY-MM-DD 或 YYYY-MM 或 YYYY）
     * @return 数据概览DTO
     */
    DataOverviewDTO getDataOverview(String period, String startDate, String endDate);
}

