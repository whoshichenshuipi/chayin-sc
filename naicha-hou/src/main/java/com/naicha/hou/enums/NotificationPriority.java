package com.naicha.hou.enums;

import lombok.Getter;

/**
 * 通知优先级枚举
 *
 * @author naicha
 * @since 2024-01-01
 */
@Getter
public enum NotificationPriority {

    /**
     * 低优先级
     */
    LOW(1, "低"),

    /**
     * 普通优先级
     */
    NORMAL(2, "普通"),

    /**
     * 重要优先级
     */
    IMPORTANT(3, "重要"),

    /**
     * 紧急优先级
     */
    URGENT(4, "紧急");

    /**
     * 优先级代码
     */
    private final Integer code;

    /**
     * 优先级描述
     */
    private final String description;

    NotificationPriority(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     */
    public static NotificationPriority getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (NotificationPriority priority : values()) {
            if (priority.getCode().equals(code)) {
                return priority;
            }
        }
        return null;
    }

    /**
     * 根据代码获取描述
     */
    public static String getDescriptionByCode(Integer code) {
        NotificationPriority priority = getByCode(code);
        return priority != null ? priority.getDescription() : "未知";
    }
}

