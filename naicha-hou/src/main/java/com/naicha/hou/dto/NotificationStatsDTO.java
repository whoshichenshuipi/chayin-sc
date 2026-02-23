package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 通知统计DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class NotificationStatsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总通知数
     */
    private Integer totalCount;

    /**
     * 未读通知数
     */
    private Integer unreadCount;

    /**
     * 已读通知数
     */
    private Integer readCount;

    /**
     * 按类型统计
     */
    private java.util.Map<String, Integer> countByType;

    /**
     * 按优先级统计
     */
    private java.util.Map<String, Integer> countByPriority;
}

