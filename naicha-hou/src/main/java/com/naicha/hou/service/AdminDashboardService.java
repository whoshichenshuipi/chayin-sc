package com.naicha.hou.service;

import com.naicha.hou.dto.AdminDashboardDTO;

/**
 * 管理员仪表盘服务接口
 *
 * @author naicha
 * @since 2024-01-01
 */
public interface AdminDashboardService {

    /**
     * 获取管理员仪表盘数据
     *
     * @param days 趋势图表的天数，默认30天
     * @return 仪表盘数据
     */
    AdminDashboardDTO getDashboardData(Integer days);
}

