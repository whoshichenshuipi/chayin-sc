package com.naicha.hou.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 商家审核请求DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class MerchantAuditRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商家ID
     */
    @NotNull(message = "商家ID不能为空")
    private Long merchantId;

    /**
     * 审核结果：approved-通过，rejected-拒绝
     */
    @NotBlank(message = "审核结果不能为空")
    private String result;

    /**
     * 审核意见
     */
    @NotBlank(message = "审核意见不能为空")
    private String comment;

    /**
     * 驳回原因（拒绝时必填）
     */
    private String rejectReason;

    /**
     * 是否自动生成账号（审核通过时）
     */
    private Boolean autoGenerateAccount = true;
}

