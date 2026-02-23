package com.naicha.hou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户反馈实体类
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_feedback")
public class UserFeedback implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 反馈ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 反馈类型：bug-功能问题, ui-界面问题, suggestion-建议类
     */
    @TableField("type")
    private String type;

    /**
     * 反馈标题
     */
    @TableField("title")
    private String title;

    /**
     * 反馈内容
     */
    @TableField("content")
    private String content;

    /**
     * 处理状态：pending-待处理, processing-处理中, completed-已处理
     */
    @TableField("status")
    private String status;

    /**
     * 负责人（管理员或团队名称）
     */
    @TableField("assignee")
    private String assignee;

    /**
     * 处理说明（分配时填写）
     */
    @TableField("assign_note")
    private String assignNote;

    /**
     * 处理结果（完成时填写）
     */
    @TableField("result")
    private String result;

    /**
     * 提交时间
     */
    @TableField("submit_time")
    private LocalDateTime submitTime;

    /**
     * 分配时间
     */
    @TableField("assign_time")
    private LocalDateTime assignTime;

    /**
     * 完成时间
     */
    @TableField("complete_time")
    private LocalDateTime completeTime;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 删除标志 0-未删除 1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}

