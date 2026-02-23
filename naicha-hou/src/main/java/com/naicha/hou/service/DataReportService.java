package com.naicha.hou.service;

import com.naicha.hou.dto.DataReportDTO;

/**
 * 数据报表服务接口
 *
 * @author naicha
 * @since 2024-01-01
 */
public interface DataReportService {

    /**
     * 生成数据报表
     *
     * @param reportType 报表类型：operation, sales, user, merchant, finance
     * @param period 周期类型：daily, weekly, monthly, yearly
     * @param startDate 开始日期（格式根据 period 而定）
     * @param endDate 结束日期（格式根据 period 而定）
     * @return 报表数据DTO
     */
    DataReportDTO generateReport(String reportType, String period, String startDate, String endDate);
}

