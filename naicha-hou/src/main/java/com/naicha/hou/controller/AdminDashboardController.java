package com.naicha.hou.controller;

import com.naicha.hou.common.Result;
import com.naicha.hou.dto.AdminDashboardDTO;
import com.naicha.hou.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员仪表盘控制器
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Validated
@Tag(name = "管理员仪表盘", description = "管理员仪表盘相关接口")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    /**
     * 获取管理员仪表盘数据
     */
    @GetMapping("/data")
    @Operation(summary = "获取管理员仪表盘数据", description = "获取统计卡片、趋势图表和最近活动数据")
    public Result<AdminDashboardDTO> getDashboardData(
            @Parameter(description = "趋势图表天数，默认30天")
            @RequestParam(value = "days", required = false, defaultValue = "30") Integer days) {

        log.info("获取管理员仪表盘数据，天数: {}", days);

        AdminDashboardDTO dashboardData = adminDashboardService.getDashboardData(days);
        return Result.success("查询成功", dashboardData);
    }
}

