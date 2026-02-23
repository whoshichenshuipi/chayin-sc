package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 通知查询DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class NotificationQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 优先级
     */
    private Integer priority;

    /**
     * 是否已读（0-未读，1-已读，null-全部）
     */
    private Integer isRead;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 20;
}

