package com.naicha.hou.dto;

import lombok.Data;

/**
 * 订单退款统计数据DTO
 *
 * @author naicha
 * @since 2024-01-15
 */
@Data
public class OrderRefundStatsDTO {

    /**
     * 待处理取消申请数量
     */
    private Integer pendingCancel;

    /**
     * 待处理退款申请数量
     */
    private Integer pendingRefund;

    /**
     * 已完成退款数量
     */
    private Integer completedRefund;

    /**
     * 本月退款总额
     */
    private Double totalRefundAmount;
}
