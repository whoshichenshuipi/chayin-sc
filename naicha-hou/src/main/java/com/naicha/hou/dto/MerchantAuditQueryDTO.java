package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商家审核查询DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class MerchantAuditQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 店铺名称（模糊查询）
     */
    private String shopName;

    /**
     * 审核状态（0-待审核，1-已审核，2-已拒绝）
     */
    private Integer status;

    /**
     * 开始时间（申请/审核时间范围）
     */
    private LocalDateTime startTime;

    /**
     * 结束时间（申请/审核时间范围）
     */
    private LocalDateTime endTime;

    /**
     * 当前页
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 20;
}

