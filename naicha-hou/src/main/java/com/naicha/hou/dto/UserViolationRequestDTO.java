package com.naicha.hou.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * 用户违规处理请求DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class UserViolationRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 处理类型：warning-警告, limit-限制下单, freeze-冻结账号
     */
    @NotBlank(message = "处理类型不能为空")
    private String type;

    /**
     * 处理原因
     */
    @NotBlank(message = "处理原因不能为空")
    private String reason;

    /**
     * 限制天数（仅当type为limit时有效，1-30天）
     */
    private Integer limitDays;
}

