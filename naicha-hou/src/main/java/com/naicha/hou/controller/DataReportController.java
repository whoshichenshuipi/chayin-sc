package com.naicha.hou.controller;

import com.naicha.hou.common.Result;
import com.naicha.hou.dto.DataReportDTO;
import com.naicha.hou.service.DataReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 数据报表控制器
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/data-report")
@RequiredArgsConstructor
@Validated
@Tag(name = "数据报表", description = "数据报表生成相关接口")
public class DataReportController {

    private final DataReportService dataReportService;

    /**
     * 生成数据报表
     */
    @GetMapping("/generate")
    @Operation(summary = "生成数据报表", description = "根据报表类型、周期和时间范围生成数据报表")
    public Result<DataReportDTO> generateReport(
            @Parameter(description = "报表类型：operation, sales, user, merchant, finance", required = true)
            @RequestParam("type") String reportType,
            @Parameter(description = "周期类型：daily, weekly, monthly, yearly", required = true)
            @RequestParam("period") String period,
            @Parameter(description = "开始日期（格式根据period而定：daily/weekly为YYYY-MM-DD，monthly为YYYY-MM，yearly为YYYY）", required = true)
            @RequestParam("startDate") String startDate,
            @Parameter(description = "结束日期（格式根据period而定：daily/weekly为YYYY-MM-DD，monthly为YYYY-MM，yearly为YYYY）", required = true)
            @RequestParam("endDate") String endDate) {

        log.info("生成数据报表，报表类型: {}, 周期: {}, 开始日期: {}, 结束日期: {}", reportType, period, startDate, endDate);

        DataReportDTO report = dataReportService.generateReport(reportType, period, startDate, endDate);
        return Result.success("报表生成成功", report);
    }
}

