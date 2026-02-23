package com.naicha.hou.enums;

import lombok.Getter;

/**
 * 通知类型枚举
 *
 * @author naicha
 * @since 2024-01-01
 */
@Getter
public enum NotificationType {

    // 管理员通知类型
    /**
     * 商家入驻申请提醒
     */
    MERCHANT_APPLICATION("merchant_application", "商家入驻申请", "admin"),

    /**
     * 消费者申诉提醒
     */
    USER_APPEAL("user_appeal", "消费者申诉", "admin"),

    /**
     * 系统故障提醒
     */
    SYSTEM_ERROR("system_error", "系统故障", "admin"),

    /**
     * 合规检查报告提醒
     */
    COMPLIANCE_REPORT("compliance_report", "合规检查报告", "admin"),

    // 商家通知类型
    /**
     * 新订单提醒
     */
    NEW_ORDER("new_order", "新订单提醒", "merchant"),

    /**
     * 消费者取消订单申请
     */
    ORDER_CANCEL("order_cancel", "订单取消申请", "merchant"),

    /**
     * 售后申请
     */
    AFTER_SALE("after_sale", "售后申请", "merchant"),

    /**
     * 库存预警提醒
     */
    STOCK_WARNING("stock_warning", "库存预警", "merchant"),

    /**
     * 结算单生成提醒
     */
    SETTLEMENT("settlement", "结算单生成", "merchant");

    /**
     * 类型代码
     */
    private final String code;

    /**
     * 类型描述
     */
    private final String description;

    /**
     * 接收者角色 (admin/merchant)
     */
    private final String recipientRole;

    NotificationType(String code, String description, String recipientRole) {
        this.code = code;
        this.description = description;
        this.recipientRole = recipientRole;
    }

    /**
     * 根据代码获取枚举
     */
    public static NotificationType getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (NotificationType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据代码获取描述
     */
    public static String getDescriptionByCode(String code) {
        NotificationType type = getByCode(code);
        return type != null ? type.getDescription() : "未知";
    }
}

