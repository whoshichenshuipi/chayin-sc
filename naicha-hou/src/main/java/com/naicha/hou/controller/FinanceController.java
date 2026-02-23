package com.naicha.hou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.common.Result;
import com.naicha.hou.dto.FinanceExpenseRecordDTO;
import com.naicha.hou.dto.FinanceIncomeRecordDTO;
import com.naicha.hou.dto.FinanceQueryDTO;
import com.naicha.hou.dto.FinanceSummaryDTO;
import com.naicha.hou.service.FinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;

/**
 * 财务管理控制器
 *
 * @author naicha
 * @since 2024-01-20
 */
@Slf4j
@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
@Validated
@Tag(name = "财务管理接口", description = "财务收入、支出、统计等管理接口")
public class FinanceController {

    private final FinanceService financeService;

    /**
     * 获取财务概览
     */
    @GetMapping("/summary")
    @Operation(summary = "获取财务概览", description = "获取总收入、总支出、净利润、待结算金额")
    public Result<FinanceSummaryDTO> getFinanceSummary(
            @Parameter(description = "商家ID", required = true)
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId,
            @Parameter(description = "开始时间")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        log.info("获取财务概览，商家ID: {}, 开始时间: {}, 结束时间: {}", merchantId, startTime, endTime);

        FinanceQueryDTO queryDTO = new FinanceQueryDTO();
        queryDTO.setMerchantId(merchantId);
        queryDTO.setStartTime(startTime);
        queryDTO.setEndTime(endTime);

        FinanceSummaryDTO summary = financeService.getFinanceSummary(merchantId, queryDTO);
        return Result.success("查询成功", summary);
    }

    /**
     * 分页查询收入记录
     */
    @PostMapping("/income/records")
    @Operation(summary = "分页查询收入记录", description = "根据条件分页查询收入记录")
    public Result<IPage<FinanceIncomeRecordDTO>> getIncomeRecords(
            @Parameter(description = "商家ID", required = true)
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId,
            @Valid @RequestBody FinanceQueryDTO queryDTO) {

        log.info("分页查询收入记录，商家ID: {}, 查询条件: {}", merchantId, queryDTO);

        queryDTO.setMerchantId(merchantId);
        IPage<FinanceIncomeRecordDTO> result = financeService.getIncomeRecords(queryDTO);
        return Result.success("查询成功", result);
    }

    /**
     * 分页查询支出记录
     */
    @PostMapping("/expense/records")
    @Operation(summary = "分页查询支出记录", description = "根据条件分页查询支出记录")
    public Result<IPage<FinanceExpenseRecordDTO>> getExpenseRecords(
            @Parameter(description = "商家ID", required = true)
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId,
            @Valid @RequestBody FinanceQueryDTO queryDTO) {

        log.info("分页查询支出记录，商家ID: {}, 查询条件: {}", merchantId, queryDTO);

        queryDTO.setMerchantId(merchantId);
        IPage<FinanceExpenseRecordDTO> result = financeService.getExpenseRecords(queryDTO);
        return Result.success("查询成功", result);
    }

    /**
     * 导出收入明细
     */
    @PostMapping("/income/export")
    @Operation(summary = "导出收入明细", description = "导出收入明细数据")
    public Result<String> exportIncomeData(
            @Parameter(description = "商家ID", required = true)
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId,
            @Valid @RequestBody FinanceQueryDTO queryDTO) {

        log.info("导出收入明细，商家ID: {}, 查询条件: {}", merchantId, queryDTO);

        financeService.exportIncomeData(merchantId, queryDTO);
        return Result.success("导出成功");
    }

    /**
     * 导出支出明细
     */
    @PostMapping("/expense/export")
    @Operation(summary = "导出支出明细", description = "导出支出明细数据")
    public Result<String> exportExpenseData(
            @Parameter(description = "商家ID", required = true)
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId,
            @Valid @RequestBody FinanceQueryDTO queryDTO) {

        log.info("导出支出明细，商家ID: {}, 查询条件: {}", merchantId, queryDTO);

        financeService.exportExpenseData(merchantId, queryDTO);
        return Result.success("导出成功");
    }
}

