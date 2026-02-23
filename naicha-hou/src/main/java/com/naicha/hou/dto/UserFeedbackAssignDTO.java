package com.naicha.hou.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * 用户反馈分配处理DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class UserFeedbackAssignDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 反馈ID
     */
    @NotNull(message = "反馈ID不能为空")
    private Long feedbackId;

    /**
     * 分配给（负责人或团队名称）
     */
    @NotBlank(message = "负责人不能为空")
    private String assignee;

    /**
     * 处理说明
     */
    @NotBlank(message = "处理说明不能为空")
    private String note;
}

