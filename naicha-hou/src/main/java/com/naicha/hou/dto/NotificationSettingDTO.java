package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 通知设置DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class NotificationSettingDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 启用的通知类型列表
     */
    private List<String> notificationTypes;

    /**
     * 通知方式列表（popup, sound, email, sms）
     */
    private List<String> notificationMethods;

    /**
     * 免打扰开始时间
     */
    private String quietStartTime;

    /**
     * 免打扰结束时间
     */
    private String quietEndTime;

    /**
     * 是否启用声音（商家专用）
     */
    private Boolean soundEnabled;

    /**
     * 大额订单阈值（商家专用）
     */
    private Integer largeOrderThreshold;
}

