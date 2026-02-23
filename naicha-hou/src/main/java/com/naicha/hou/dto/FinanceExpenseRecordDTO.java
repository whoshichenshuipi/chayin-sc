package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 财务支出记录DTO
 *
 * @author naicha
 * @since 2024-01-20
 */
@Data
public class FinanceExpenseRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 支出时间
     */
    private LocalDateTime expenseTime;

    /**
     * 支出类型：platform_fee-平台手续费，coupon_cost-优惠券成本，refund-退款金额，other-其他支出
     */
    private String expenseType;

    /**
     * 支出金额
     */
    private BigDecimal amount;

    /**
     * 备注
     */
    private String description;

    /**
     * 关联订单ID
     */
    private String relatedOrderId;
}

