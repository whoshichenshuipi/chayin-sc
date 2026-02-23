package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 财务概览DTO
 *
 * @author naicha
 * @since 2024-01-20
 */
@Data
public class FinanceSummaryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总收入
     */
    private BigDecimal totalIncome;

    /**
     * 总支出
     */
    private BigDecimal totalExpense;

    /**
     * 净利润
     */
    private BigDecimal netProfit;

    /**
     * 待结算金额
     */
    private BigDecimal pendingAmount;
}

