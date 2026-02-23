package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户反馈查询DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class UserFeedbackQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 反馈类型：bug-功能问题, ui-界面问题, suggestion-建议类
     */
    private String type;

    /**
     * 处理状态：pending-待处理, processing-处理中, completed-已处理
     */
    private String status;

    /**
     * 搜索关键词（标题或内容）
     */
    private String keyword;

    /**
     * 当前页
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;
}

