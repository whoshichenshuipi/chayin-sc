package com.naicha.hou.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 订单退款处理请求DTO
 *
 * @author naicha
 * @since 2024-01-15
 */
@Data
public class OrderRefundProcessRequestDTO {

    /**
     * 退款申请ID
     */
    @NotNull(message = "退款申请ID不能为空")
    private Long refundId;

    /**
     * 处理动作：approve-同意，reject-拒绝
     */
    @NotBlank(message = "处理动作不能为空")
    private String action;

    /**
     * 处理备注
     */
    private String remark;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 拒绝详细说明
     */
    private String rejectDetail;
}
