package com.naicha.hou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单退款申请实体
 *
 * @author naicha
 * @since 2024-01-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("order_refund")
public class OrderRefund {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 订单号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 商家ID
     */
    @TableField("merchant_id")
    private Long merchantId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 处理类型：cancel-取消申请，refund-退款申请
     */
    @TableField("process_type")
    private String processType;

    /**
     * 申请原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 详细说明
     */
    @TableField("detail_reason")
    private String detailReason;

    /**
     * 订单金额
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 退款金额
     */
    @TableField("refund_amount")
    private BigDecimal refundAmount;

    /**
     * 退款类型：full-全额退款，partial-部分退款
     */
    @TableField("refund_type")
    private String refundType;

    /**
     * 退款原因
     */
    @TableField("refund_reason")
    private String refundReason;

    /**
     * 处理状态：pending-待处理，approved-已同意，rejected-已拒绝，completed-已完成
     */
    @TableField("status")
    private String status;

    /**
     * 处理备注
     */
    @TableField("process_remark")
    private String processRemark;

    /**
     * 拒绝原因
     */
    @TableField("reject_reason")
    private String rejectReason;

    /**
     * 拒绝详细说明
     */
    @TableField("reject_detail")
    private String rejectDetail;

    /**
     * 申请时间
     */
    @TableField("apply_time")
    private LocalDateTime applyTime;

    /**
     * 处理时间
     */
    @TableField("process_time")
    private LocalDateTime processTime;

    /**
     * 完成时间
     */
    @TableField("complete_time")
    private LocalDateTime completeTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
