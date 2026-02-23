package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 财务收入记录DTO
 *
 * @author naicha
 * @since 2024-01-20
 */
@Data
public class FinanceIncomeRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 下单时间
     */
    private LocalDateTime orderTime;

    /**
     * 商品名称（订单商品汇总）
     */
    private String productName;

    /**
     * 订单金额（原价）
     */
    private BigDecimal originalAmount;

    /**
     * 平台手续费
     */
    private BigDecimal platformFee;

    /**
     * 收入金额（订单金额 - 平台手续费）
     */
    private BigDecimal incomeAmount;

    /**
     * 结算状态：settled-已结算，pending-待结算
     */
    private String settlementStatus;

    /**
     * 结算时间
     */
    private LocalDateTime settlementTime;
}

