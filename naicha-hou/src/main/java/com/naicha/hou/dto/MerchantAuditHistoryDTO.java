package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商家审核历史DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class MerchantAuditHistoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商家ID
     */
    private Long id;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核人ID
     */
    private Long auditorId;

    /**
     * 审核人姓名
     */
    private String auditorName;

    /**
     * 审核结果：approved-通过，rejected-拒绝
     */
    private String result;

    /**
     * 审核意见
     */
    private String comment;

    /**
     * 驳回原因
     */
    private String rejectReason;

    /**
     * 申请时间（创建时间）
     */
    private LocalDateTime applyTime;
}

