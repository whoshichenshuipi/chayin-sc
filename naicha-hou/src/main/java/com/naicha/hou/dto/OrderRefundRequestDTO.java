package com.naicha.hou.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单退款申请请求DTO
 *
 * @author naicha
 * @since 2024-01-15
 */
@Data
public class OrderRefundRequestDTO {

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 处理类型：cancel-取消申请，refund-退款申请
     */
    @NotBlank(message = "处理类型不能为空")
    private String processType;

    /**
     * 申请原因
     */
    @NotBlank(message = "申请原因不能为空")
    private String reason;

    /**
     * 详细说明
     */
    private String detailReason;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款类型：full-全额退款，partial-部分退款
     */
    private String refundType;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 附件列表
     */
    private List<OrderRefundAttachmentDTO> attachments;
}
