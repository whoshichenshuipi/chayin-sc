package com.naicha.hou.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * 用户反馈完成处理DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class UserFeedbackCompleteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 反馈ID
     */
    @NotNull(message = "反馈ID不能为空")
    private Long feedbackId;

    /**
     * 处理结果
     */
    @NotBlank(message = "处理结果不能为空")
    private String result;

    /**
     * 是否通知用户
     */
    private Boolean notifyUser = true;
}

