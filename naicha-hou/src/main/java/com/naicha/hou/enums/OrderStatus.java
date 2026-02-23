package com.naicha.hou.enums;

import lombok.Getter;

/**
 * 订单状态枚举
 *
 * @author naicha
 * @since 2024-01-15
 */
@Getter
public enum OrderStatus {

    /**
     * 0 - 待支付
     */
    PENDING_PAYMENT(0, "待支付"),

    /**
     * 1 - 已支付
     */
    PAID(1, "已支付"),

    /**
     * 2 - 已接单
     */
    ACCEPTED(2, "已接单"),

    /**
     * 3 - 制作中
     */
    MAKING(3, "制作中"),

    /**
     * 4 - 已发货
     */
    SHIPPED(4, "已发货"),

    /**
     * 5 - 已完成
     */
    COMPLETED(5, "已完成"),

    /**
     * 6 - 已取消
     */
    CANCELLED(6, "已取消"),

    /**
     * 7 - 已退款
     */
    REFUNDED(7, "已退款");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String description;

    OrderStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据状态码获取枚举
     */
    public static OrderStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据状态码获取描述
     */
    public static String getDescriptionByCode(Integer code) {
        OrderStatus status = getByCode(code);
        return status != null ? status.getDescription() : "未知";
    }

    /**
     * 判断状态码是否有效
     */
    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }

    /**
     * 判断是否可以发货
     */
    public static boolean canShip(Integer status) {
        return status != null && (status == PAID.getCode() || status == ACCEPTED.getCode() || status == MAKING.getCode());
    }

    /**
     * 判断是否可以完成
     */
    public static boolean canComplete(Integer status) {
        return status != null && status == SHIPPED.getCode();
    }

    /**
     * 判断是否可以取消
     */
    public static boolean canCancel(Integer status) {
        return status != null && (status == PENDING_PAYMENT.getCode() || status == PAID.getCode() || status == ACCEPTED.getCode());
    }
}
