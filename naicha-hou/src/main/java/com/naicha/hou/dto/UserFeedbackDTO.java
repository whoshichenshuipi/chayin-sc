package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户反馈DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class UserFeedbackDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 反馈ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 反馈类型：bug-功能问题, ui-界面问题, suggestion-建议类
     */
    private String type;

    /**
     * 反馈标题
     */
    private String title;

    /**
     * 反馈内容
     */
    private String content;

    /**
     * 处理状态：pending-待处理, processing-处理中, completed-已处理
     */
    private String status;

    /**
     * 负责人
     */
    private String assignee;

    /**
     * 处理说明
     */
    private String assignNote;

    /**
     * 处理结果
     */
    private String result;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 分配时间
     */
    private LocalDateTime assignTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;
}

