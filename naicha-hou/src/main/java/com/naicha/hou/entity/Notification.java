package com.naicha.hou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知实体类
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("notification")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接收者ID（管理员ID或商家ID）
     */
    @TableField("recipient_id")
    private Long recipientId;

    /**
     * 接收者类型：admin-管理员，merchant-商家
     */
    @TableField("recipient_type")
    private String recipientType;

    /**
     * 通知类型
     */
    @TableField("type")
    private String type;

    /**
     * 通知标题
     */
    @TableField("title")
    private String title;

    /**
     * 通知内容
     */
    @TableField("content")
    private String content;

    /**
     * 优先级：1-低，2-普通，3-重要，4-紧急
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 是否已读：0-未读，1-已读
     */
    @TableField("is_read")
    private Integer isRead;

    /**
     * 关联数据ID（如订单ID、商家ID等）
     */
    @TableField("related_id")
    private Long relatedId;

    /**
     * 关联数据类型（如：order、merchant等）
     */
    @TableField("related_type")
    private String relatedType;

    /**
     * 操作按钮（JSON格式，存储操作选项）
     */
    @TableField("actions")
    private String actions;

    /**
     * 扩展数据（JSON格式）
     */
    @TableField("extra_data")
    private String extraData;

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

