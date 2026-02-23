package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商家查询DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class MerchantQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商家名称（店铺名称，模糊查询）
     */
    private String name;

    /**
     * 状态 0-待审核 1-已审核 2-已拒绝 3-已禁用
     */
    private Integer status;

    /**
     * 联系人姓名（模糊查询）
     */
    private String contactName;

    /**
     * 联系电话（模糊查询）
     */
    private String contactPhone;

    /**
     * 开始时间（注册时间范围）
     */
    private LocalDateTime startTime;

    /**
     * 结束时间（注册时间范围）
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

