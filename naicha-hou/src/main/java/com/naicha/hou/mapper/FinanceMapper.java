package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.naicha.hou.dto.FinanceExpenseRecordDTO;
import com.naicha.hou.dto.FinanceIncomeRecordDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 财务管理Mapper接口
 *
 * @author naicha
 * @since 2024-01-20
 */
@Mapper
public interface FinanceMapper extends BaseMapper<Object> {

    /**
     * 分页查询收入记录
     */
    IPage<FinanceIncomeRecordDTO> selectIncomeRecords(Page<FinanceIncomeRecordDTO> page, @Param("merchantId") Long merchantId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 分页查询支出记录
     */
    IPage<FinanceExpenseRecordDTO> selectExpenseRecords(Page<FinanceExpenseRecordDTO> page, @Param("merchantId") Long merchantId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 计算总收入
     */
    BigDecimal calculateTotalIncome(@Param("merchantId") Long merchantId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 计算总支出
     */
    BigDecimal calculateTotalExpense(@Param("merchantId") Long merchantId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 计算待结算金额
     */
    BigDecimal calculatePendingAmount(@Param("merchantId") Long merchantId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}

