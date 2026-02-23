package com.naicha.hou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知设置实体类
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("notification_setting")
public class NotificationSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设置ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（管理员ID或商家ID）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户类型：admin-管理员，merchant-商家
     */
    @TableField("user_type")
    private String userType;

    /**
     * 通知类型
     */
    @TableField("notification_type")
    private String notificationType;

    /**
     * 是否启用弹窗提醒：0-否，1-是
     */
    @TableField("enable_popup")
    private Integer enablePopup;

    /**
     * 是否启用声音提醒：0-否，1-是
     */
    @TableField("enable_sound")
    private Integer enableSound;

    /**
     * 是否启用邮件提醒：0-否，1-是
     */
    @TableField("enable_email")
    private Integer enableEmail;

    /**
     * 是否启用短信提醒：0-否，1-是
     */
    @TableField("enable_sms")
    private Integer enableSms;

    /**
     * 免打扰开始时间（HH:mm格式）
     */
    @TableField("quiet_start_time")
    private String quietStartTime;

    /**
     * 免打扰结束时间（HH:mm格式）
     */
    @TableField("quiet_end_time")
    private String quietEndTime;

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

