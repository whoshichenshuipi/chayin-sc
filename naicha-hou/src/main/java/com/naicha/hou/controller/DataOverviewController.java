package com.naicha.hou.controller;

import com.naicha.hou.common.Result;
import com.naicha.hou.dto.DataOverviewDTO;
import com.naicha.hou.service.DataOverviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 数据概览控制器
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/data-overview")
@RequiredArgsConstructor
@Validated
@Tag(name = "数据概览", description = "数据统计分析相关接口")
public class DataOverviewController {

    private final DataOverviewService dataOverviewService;

    /**
     * 获取数据概览
     */
    @GetMapping("/overview")
    @Operation(summary = "获取数据概览", description = "根据周期和时间范围获取数据概览统计")
    public Result<DataOverviewDTO> getDataOverview(
            @Parameter(description = "统计周期：day, week, month, year", required = true)
            @RequestParam("period") String period,
            @Parameter(description = "开始日期（格式根据period而定：day/week为YYYY-MM-DD，month为YYYY-MM，year为YYYY）", required = true)
            @RequestParam("startDate") String startDate,
            @Parameter(description = "结束日期（格式根据period而定：day/week为YYYY-MM-DD，month为YYYY-MM，year为YYYY）", required = true)
            @RequestParam("endDate") String endDate) {

        log.info("获取数据概览，周期: {}, 开始日期: {}, 结束日期: {}", period, startDate, endDate);

        DataOverviewDTO overview = dataOverviewService.getDataOverview(period, startDate, endDate);
        return Result.success("查询成功", overview);
    }
}

