package com.naicha.hou.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单退款查询DTO
 *
 * @author naicha
 * @since 2024-01-15
 */
@Data
public class OrderRefundQueryDTO {

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 处理类型：cancel-取消申请，refund-退款申请
     */
    private String processType;

    /**
     * 处理状态：pending-待处理，approved-已同意，rejected-已拒绝，completed-已完成
     */
    private String status;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 页大小
     */
    private Integer pageSize = 20;
}
