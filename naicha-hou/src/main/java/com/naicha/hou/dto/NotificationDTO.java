package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class NotificationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 接收者ID
     */
    private Long recipientId;

    /**
     * 接收者类型
     */
    private String recipientType;

    /**
     * 通知类型
     */
    private String type;

    /**
     * 通知类型描述
     */
    private String typeDescription;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 优先级描述
     */
    private String priorityDescription;

    /**
     * 是否已读
     */
    private Integer isRead;

    /**
     * 关联数据ID
     */
    private Long relatedId;

    /**
     * 关联数据类型
     */
    private String relatedType;

    /**
     * 操作按钮列表
     */
    private List<NotificationAction> actions;

    /**
     * 扩展数据
     */
    private Object extraData;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 通知操作按钮
     */
    @Data
    public static class NotificationAction implements Serializable {
        private Long id;
        private String text;
        private String type; // primary, success, warning, danger
        private String action; // 操作类型
    }
}

