package com.naicha.hou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.dto.FinanceExpenseRecordDTO;
import com.naicha.hou.dto.FinanceIncomeRecordDTO;
import com.naicha.hou.dto.FinanceQueryDTO;
import com.naicha.hou.dto.FinanceSummaryDTO;

/**
 * 财务管理服务接口
 *
 * @author naicha
 * @since 2024-01-20
 */
public interface FinanceService {

    /**
     * 获取财务概览
     */
    FinanceSummaryDTO getFinanceSummary(Long merchantId, FinanceQueryDTO queryDTO);

    /**
     * 分页查询收入记录
     */
    IPage<FinanceIncomeRecordDTO> getIncomeRecords(FinanceQueryDTO queryDTO);

    /**
     * 分页查询支出记录
     */
    IPage<FinanceExpenseRecordDTO> getExpenseRecords(FinanceQueryDTO queryDTO);

    /**
     * 导出收入明细
     */
    void exportIncomeData(Long merchantId, FinanceQueryDTO queryDTO);

    /**
     * 导出支出明细
     */
    void exportExpenseData(Long merchantId, FinanceQueryDTO queryDTO);
}

