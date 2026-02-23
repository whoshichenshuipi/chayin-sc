package com.naicha.hou.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量处理订单退款请求DTO
 *
 * @author naicha
 * @since 2024-01-15
 */
@Data
public class BatchOrderRefundProcessRequestDTO {

    /**
     * 退款申请ID列表
     */
    @NotNull(message = "退款申请ID列表不能为空")
    private List<Long> refundIds;

    /**
     * 处理动作：batch_approve-批量同意，batch_reject-批量拒绝
     */
    @NotBlank(message = "处理动作不能为空")
    private String action;

    /**
     * 处理备注
     */
    private String remark;
}
