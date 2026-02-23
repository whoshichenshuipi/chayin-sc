package com.naicha.hou.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.naicha.hou.dto.FinanceExpenseRecordDTO;
import com.naicha.hou.dto.FinanceIncomeRecordDTO;
import com.naicha.hou.dto.FinanceQueryDTO;
import com.naicha.hou.dto.FinanceSummaryDTO;
import com.naicha.hou.mapper.FinanceMapper;
import com.naicha.hou.service.FinanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 财务管理服务实现类
 *
 * @author naicha
 * @since 2024-01-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinanceServiceImpl implements FinanceService {

    private final FinanceMapper financeMapper;

    @Override
    public FinanceSummaryDTO getFinanceSummary(Long merchantId, FinanceQueryDTO queryDTO) {
        log.info("获取财务概览，商家ID: {}, 查询条件: {}", merchantId, queryDTO);

        LocalDateTime startTime = queryDTO.getStartTime();
        LocalDateTime endTime = queryDTO.getEndTime();

        BigDecimal totalIncome = financeMapper.calculateTotalIncome(merchantId, startTime, endTime);
        BigDecimal totalExpense = financeMapper.calculateTotalExpense(merchantId, startTime, endTime);
        BigDecimal pendingAmount = financeMapper.calculatePendingAmount(merchantId, startTime, endTime);
        BigDecimal netProfit = totalIncome.subtract(totalExpense);

        FinanceSummaryDTO summary = new FinanceSummaryDTO();
        summary.setTotalIncome(totalIncome != null ? totalIncome : BigDecimal.ZERO);
        summary.setTotalExpense(totalExpense != null ? totalExpense : BigDecimal.ZERO);
        summary.setNetProfit(netProfit != null ? netProfit : BigDecimal.ZERO);
        summary.setPendingAmount(pendingAmount != null ? pendingAmount : BigDecimal.ZERO);

        return summary;
    }

    @Override
    public IPage<FinanceIncomeRecordDTO> getIncomeRecords(FinanceQueryDTO queryDTO) {
        log.info("分页查询收入记录，查询条件: {}", queryDTO);

        Page<FinanceIncomeRecordDTO> page = new Page<>(
                queryDTO.getCurrent() != null ? queryDTO.getCurrent() : 1,
                queryDTO.getSize() != null ? queryDTO.getSize() : 20
        );

        IPage<FinanceIncomeRecordDTO> result = financeMapper.selectIncomeRecords(
                page,
                queryDTO.getMerchantId(),
                queryDTO.getStartTime(),
                queryDTO.getEndTime()
        );

        return result;
    }

    @Override
    public IPage<FinanceExpenseRecordDTO> getExpenseRecords(FinanceQueryDTO queryDTO) {
        log.info("分页查询支出记录，查询条件: {}", queryDTO);

        Page<FinanceExpenseRecordDTO> page = new Page<>(
                queryDTO.getCurrent() != null ? queryDTO.getCurrent() : 1,
                queryDTO.getSize() != null ? queryDTO.getSize() : 20
        );

        IPage<FinanceExpenseRecordDTO> result = financeMapper.selectExpenseRecords(
                page,
                queryDTO.getMerchantId(),
                queryDTO.getStartTime(),
                queryDTO.getEndTime()
        );

        return result;
    }

    @Override
    public void exportIncomeData(Long merchantId, FinanceQueryDTO queryDTO) {
        log.info("导出收入明细，商家ID: {}, 查询条件: {}", merchantId, queryDTO);
        // TODO: 实现导出功能，可以导出为Excel或CSV
    }

    @Override
    public void exportExpenseData(Long merchantId, FinanceQueryDTO queryDTO) {
        log.info("导出支出明细，商家ID: {}, 查询条件: {}", merchantId, queryDTO);
        // TODO: 实现导出功能，可以导出为Excel或CSV
    }
}

