package com.naicha.hou.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单退款申请DTO
 *
 * @author naicha
 * @since 2024-01-15
 */
@Data
public class OrderRefundDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 客户姓名
     */
    private String customerName;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 处理类型：cancel-取消申请，refund-退款申请
     */
    private String processType;

    /**
     * 申请原因
     */
    private String reason;

    /**
     * 详细说明
     */
    private String detailReason;

    /**
     * 订单金额
     */
    private BigDecimal totalAmount;

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
     * 处理状态：pending-待处理，approved-已同意，rejected-已拒绝，completed-已完成
     */
    private String status;

    /**
     * 处理备注
     */
    private String processRemark;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 拒绝详细说明
     */
    private String rejectDetail;

    /**
     * 申请时间
     */
    private LocalDateTime applyTime;

    /**
     * 处理时间
     */
    private LocalDateTime processTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 订单状态
     */
    private String orderStatus;

    /**
     * 商品信息
     */
    private List<OrderItemDTO> items;

    /**
     * 附件信息
     */
    private List<OrderRefundAttachmentDTO> attachments;
}
